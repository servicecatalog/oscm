/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.09.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.openstack.controller.ServerStatus;
import org.oscm.app.openstack.data.Server;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tateiwamext
 *
 */
public class NovaClient {

    private final OpenStackConnection connection;
    private final Logger logger = LoggerFactory.getLogger(NovaClient.class);

    private enum IP_TYPE {
        fixed, floating
    }

    /**
     * @param connection
     */
    public NovaClient(OpenStackConnection connection) {
        this.connection = connection;
    }

    /**
     * startServer This method request to OpenStack to start Instance.
     * 
     * @param ph
     *            PropertyHandler: This is used for information when CTMG get
     *            Heat Exception.
     * @param serverId
     *            String: The ID of Instance(VM) which is used for POST start
     *            API
     * @return Boolean: If the request is successful, return Boolean.TRUE. If
     *         the request is failed, return Boolean.FALSE.
     * @throws OpenStackConnectionException
     */
    public Boolean startServer(PropertyHandler ph, String serverId)
            throws OpenStackConnectionException {
        String uri;
        try {
            uri = connection.getNovaEndpoint() + "/servers/"
                    + URLEncoder.encode(serverId, "UTF-8") + "/action";

            connection.processRequest(uri, "POST", "{\"os-start\": null}");
            logger.debug("Start server: " + serverId);
            return Boolean.TRUE;
        } catch (UnsupportedEncodingException e) {
            logger.error("Runtime error happened during encoding", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * stopServer This method request to OpenStack to stop instance.
     * 
     * @param ph
     *            PropertyHandler: This is used for information when CTMG get
     *            Heat Exception.
     * @param serverId
     *            String: The ID of Instance(VM) which is used for POST start
     *            API
     * @return Boolean: If the request is successful, return Boolean.TRUE. If
     *         the request is failed, return Boolean.FALSE.
     * @throws OpenStackConnectionException
     */
    public Boolean stopServer(PropertyHandler ph, String serverId)
            throws OpenStackConnectionException {
        String uri;
        try {
            uri = connection.getNovaEndpoint() + "/servers/"
                    + URLEncoder.encode(serverId, "UTF-8") + "/action";
            connection.processRequest(uri, "POST", "{\"os-stop\": null}");
            logger.debug("Stop server: " + serverId);
            return Boolean.TRUE;
        } catch (UnsupportedEncodingException e) {
            logger.error("Runtime error happened during encoding", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get server details
     * 
     * @param ph
     * @param serverId
     * @return Server object which contain id, name and status
     * @throws OpenStackConnectionException
     */
    public Server getServerDetails(PropertyHandler ph, String serverId,
            boolean moreInfo) throws OpenStackConnectionException {
        String uri;
        Server result = new Server(serverId);
        String flavorName = "-";
        List<String> fixedIP = new ArrayList<String>();
        List<String> floatingIP = new ArrayList<String>();
        try {
            uri = connection.getNovaEndpoint() + "/servers/"
                    + URLEncoder.encode(serverId, "UTF-8");

            RESTResponse response = connection.processRequest(uri, "GET");
            String body = response.getResponseBody();
            logger.debug("NovaClient.getServerDetails() Responsecode: "
                    + response.getResponseCode());
            JSONObject responseJson = new JSONObject(body);
            JSONObject server = responseJson.getJSONObject("server");
            result.setId(server.getString("id"));
            result.setStatus(server.getString("status"));
            result.setName(server.getString("name"));
            if (moreInfo) {
                JSONObject flavor = server.getJSONObject("flavor");
                flavorName = getFlavorName(flavor.getString("id"));
                result.setType(flavorName);
                if (server.has("addresses")) {
                    JSONObject addresses = server.getJSONObject("addresses");
                    Iterator<?> networkNames = addresses.keys();
                    while (networkNames.hasNext()) {
                        String key = (String) networkNames.next();
                        if (addresses.get(key) instanceof JSONArray) {
                            JSONArray networks = addresses.getJSONArray(key);
                            for (int i = 0; i < networks.length(); i++) {
                                JSONObject network = networks.getJSONObject(i);
                                if (network.getString("OS-EXT-IPS:type")
                                        .equals(IP_TYPE.fixed.toString())) {
                                    fixedIP.add(network.getString("addr"));
                                } else if (network.getString("OS-EXT-IPS:type")
                                        .equals(IP_TYPE.floating.toString())) {
                                    floatingIP.add(network.getString("addr"));
                                }
                            }
                        }
                    }
                }
            }
            result.setType(flavorName);
            result.setPrivateIP(fixedIP);
            result.setPublicIP(floatingIP);
            return result;
        } catch (UnsupportedEncodingException e) {
            logger.error("Runtime error happened during encoding", e);
            throw new RuntimeException(e);
        } catch (JSONException e) {
            logger.error("NovaClient.getServerDetails() JSONException occurred",
                    e);
        }
        result.setName("");
        result.setStatus(ServerStatus.UNKNOWN.toString());
        result.setType(flavorName);
        result.setPrivateIP(fixedIP);
        result.setPublicIP(floatingIP);

        return result;
    }

    /**
     * Check the server status is not excepted
     * 
     * @param exceptedStatus:
     *            the status which is excepted.
     * @param ph:
     *            PropertyHandler
     * @param serverId:
     *            server ID
     * @return true: the status is not excepted. Operation can be executed.
     *         false: the status is excepted. Operation cannot be executed.
     * @throws OpenStackConnectionException
     */
    public boolean isNotServerExceptedStatus(ServerStatus exceptedStatus,
            PropertyHandler ph, String serverId)
            throws OpenStackConnectionException {
        boolean result = false;
        Server server = getServerDetails(ph, serverId, false);
        if (!server.getStatus().equals(exceptedStatus.toString())) {
            result = true;
        }
        return result;
    }

    /**
     * @param flavorID
     *            flavor ID
     * @return String flavor name
     * @throws OpenStackConnectionException
     */
    private String getFlavorName(String flavorID)
            throws OpenStackConnectionException {
        // TODO Auto-generated method stub

        String uri;
        try {
            uri = connection.getNovaEndpoint() + "/flavors/"
                    + URLEncoder.encode(flavorID, "UTF-8");

            RESTResponse response = connection.processRequest(uri, "GET");
            String body = response.getResponseBody();
            logger.debug("NovaClient.getFlavorName() Responsecode: "
                    + response.getResponseCode());
            JSONObject responseJson = new JSONObject(body);
            JSONObject flavor = responseJson.getJSONObject("flavor");
            return flavor.getString("name");
        } catch (UnsupportedEncodingException e) {
            logger.error("Runtime error happened during encoding", e);
            throw new RuntimeException(e);
        } catch (JSONException e) {
            logger.error("NovaClient.getFlavorName() JSONException occurred",
                    e);
        }
        return "-";
    }

}
