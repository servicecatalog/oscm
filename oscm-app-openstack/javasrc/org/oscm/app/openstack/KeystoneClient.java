/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-11-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keystone is the identity service used by OpenStack for authentication and
 * authorization.
 */
public class KeystoneClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(KeystoneClient.class);

    final static String TYPE_HEAT = "orchestration";

    final static String TYPE_NOVA = "compute";

    private final OpenStackConnection connection;

    /**
     * 
     * @param connection
     *            The connection that needs to be authenticated
     */
    public KeystoneClient(OpenStackConnection connection) {
        this.connection = connection;
    }

    /**
     * Authenticate the connection that was given in the constructor for V3 API.
     * 
     * @param user
     * @param password
     * @param domainName
     * @param tenantId
     * 
     * @throws OpenStackConnectionException
     */
    public void authenticate(String user, String password, String domainName,
            String tenantId)
            throws APPlatformException, OpenStackConnectionException {
        LOGGER.debug("KeystoneClient.authenticate() user: " + user + "  domain:"
                + domainName + " tenant ID:" + tenantId + "  endpoint: "
                + connection.getKeystoneEndpoint());
        String uri = connection.getKeystoneEndpoint() + "/tokens";

        JSONObject request = new JSONObject();
        JSONObject auth = new JSONObject();
        JSONObject identity = new JSONObject();
        JSONObject domain = new JSONObject();
        JSONObject userName = new JSONObject();
        JSONObject passwordInfo = new JSONObject();
        JSONObject projectId = new JSONObject();
        JSONObject project = new JSONObject();
        JSONArray methodArray = new JSONArray();
        try {
            domain.put("name", domainName);
            userName.put("domain", domain);
            userName.put("name", user);
            userName.put("password", password);
            methodArray.put("password");
            passwordInfo.put("user", userName);
            identity.put("password", passwordInfo);
            identity.put("methods", methodArray);
            auth.put("identity", identity);
            projectId.put("id", tenantId);
            project.put("project", projectId);
            auth.put("scope", project);
            request.put("auth", auth);
        } catch (JSONException e) {
            // this can basically not happen with string parameters
            throw new RuntimeException(e);
        }
        LOGGER.debug("URL is " + uri + " request is "
                + request.toString().replaceFirst(
                        "\"password\":\"" + password + "\"",
                        "\"password\":\"******\""));
        RESTResponse response = connection.processRequest(uri, "POST",
                request.toString());

        if (response.getResponseCode() != 201) {
            throw new RuntimeException(
                    "Failed to retrieve token for authentication, response code "
                            + response.getResponseCode());
        }

        String body = response.getResponseBody();
        String authToken = response.getToken();
        try {
            String heatEndpoint = null;
            String novaEndpoint = null;
            JSONObject jsonObj = new JSONObject(body);
            JSONObject token = jsonObj.getJSONObject("token");
            JSONArray catalog = token.getJSONArray("catalog");
            int catalogSize = catalog.length();
            for (int i = 0; i < catalogSize; i++) {
                JSONObject entry = catalog.getJSONObject(i);
                if (entry != null) {
                    String type = entry.getString("type");
                    if (TYPE_HEAT.equals(type)) {
                        JSONArray endpoints = entry.getJSONArray("endpoints");
                        int endpointSize = endpoints.length();
                        for (int j = 0; j < endpointSize; j++) {
                            JSONObject endpoint = endpoints.getJSONObject(j);
                            String endpointUrl = endpoint.getString("url");
                            String interfaceEndpoint = endpoint
                                    .getString("interface");
                            if (interfaceEndpoint.equals("public")) {
                                heatEndpoint = endpointUrl;
                                LOGGER.debug("PUBLIC Heat URL found: "
                                        + heatEndpoint);
                                break;
                            }
                        }
                    } else if (TYPE_NOVA.equals(type)) {
                        JSONArray endpoints = entry.getJSONArray("endpoints");
                        int endpointSize = endpoints.length();
                        for (int j = 0; j < endpointSize; j++) {
                            JSONObject endpoint = endpoints.getJSONObject(j);
                            if (endpoint != null) {
                                String endpointUrl = endpoint.getString("url");
                                String interfaceEndpoint = endpoint
                                        .getString("interface");
                                if (interfaceEndpoint.equals("public")
                                        && endpointUrl != null
                                        && endpointUrl.trim().length() > 0) {
                                    novaEndpoint = endpointUrl;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (heatEndpoint == null) {
                LOGGER.error(
                        "KeystoneClient.authenticate() heat endpoint not defined");
                throw new APPlatformException(
                        Messages.getAll("error_missing_heat_endpoint"));
            } else {
                LOGGER.debug("KeystoneClient.authenticate() heat endpoint: "
                        + heatEndpoint);
            }
            if (novaEndpoint == null) {
                LOGGER.error(
                        "KeystoneClient.authenticate() nova endpoint not defined");
                throw new APPlatformException(
                        Messages.getAll("error_missing_nova_endpoint"));
            } else {
                LOGGER.debug("KeystoneClient.authenticate() nova endpoint: "
                        + novaEndpoint);
            }
            connection.useAuthentication(authToken);
            connection.setHeatEndpoint(heatEndpoint);
            connection.setNovaEndpoint(novaEndpoint);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
