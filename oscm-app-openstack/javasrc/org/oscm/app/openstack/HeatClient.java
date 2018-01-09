/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-11-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.oscm.app.openstack.data.CreateStackRequest;
import org.oscm.app.openstack.data.Stack;
import org.oscm.app.openstack.data.UpdateStackRequest;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for communication with OpenStack Heat API.
 */
public class HeatClient {

    private final OpenStackConnection connection;
    private static final Logger logger = LoggerFactory
            .getLogger(HeatClient.class);

    private static enum InstanceType {
        NOVA("OS::Nova::Server"), EC2("AWS::EC2::Instance"), TROVE(
                "OS::Trove::Instance");

        private final String text;

        private InstanceType(final String text) {
            this.text = text;
        }

        public String getString() {
            return this.text;
        }
    }

    private static InstanceType getInstanceType(final String instanceType) {
        InstanceType[] types = InstanceType.values();
        for (InstanceType type : types) {
            if (type.getString().equalsIgnoreCase(instanceType)) {
                return type;
            }
        }
        return null;
    }

    public HeatClient(OpenStackConnection connection) {
        this.connection = connection;
    }

    public Stack createStack(CreateStackRequest request) throws HeatException {
        logger.debug("HeatClient.createStack() Endpoint: "
                + connection.getHeatEndpoint());
        String uri = connection.getHeatEndpoint() + "/stacks";
        try {
            RESTResponse response = connection.processRequest(uri, "POST",
                    request.getJSON());
            JSONObject responseJson = new JSONObject(
                    response.getResponseBody());
            JSONObject stack = responseJson.getJSONObject("stack");
            Stack result = new Stack();
            result.setId(stack.getString("id"));
            logger.debug("HeatClient.createStack() Responsecode: "
                    + response.getResponseCode());
            return result;
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        } catch (JSONException ex) {
            logger.error("HeatClient.createStack()", ex);
            throw new HeatException(ex.getMessage());
        }
    }

    public void updateStack(UpdateStackRequest request) throws HeatException {
        logger.debug("HeatClient.updateStack() Endpoint: "
                + connection.getHeatEndpoint());
        String uri;
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(request.getStackName(), "UTF-8");
            RESTResponse response = connection.processRequest(uri, "PUT",
                    request.getJSON());
            logger.debug("HeatClient.updateStack() Responsecode: "
                    + response.getResponseCode());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            if (ex.getResponseCode() == 400) {
                throw new HeatException("Update not allowed in this status.",
                        400);
            }
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        }
    }

    public void resumeStack(String stackName, String stackId)
            throws HeatException {
        String uri;
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(stackName, "UTF-8") + '/' + stackId
                    + "/actions";
            connection.processRequest(uri, "POST", "{\"resume\":null}");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        }
    }

    public void suspendStack(String stackName, String stackId)
            throws HeatException {
        String uri;
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(stackName, "UTF-8") + '/' + stackId
                    + "/actions";
            connection.processRequest(uri, "POST", "{\"suspend\":null}");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        }
    }

    public boolean checkServerExists(String stackName) throws HeatException {
        String serverId = getServerIdByStackResource(stackName);
        if (serverId == null || serverId.length() < 1) {
            return false;
        }

        logger.debug("HeatClient.checkServerExists() Endpoint: "
                + connection.getNovaEndpoint());
        String uri;
        try {
            uri = connection.getNovaEndpoint() + "/servers/"
                    + URLEncoder.encode(serverId, "UTF-8");
            RESTResponse response = connection.processRequest(uri, "GET");

            String body = response.getResponseBody();

            logger.debug("HeatClient.checkServerExists() Responsecode: "
                    + response.getResponseCode());

            if (body.contains(serverId)) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        }
        return false;
    }

    public List<String> getServerIds(String stackName) throws HeatException {
        logger.debug("HeatClient.getServerId() Endpoint: "
                + connection.getHeatEndpoint());
        String uri;
        List<String> serverIds = new LinkedList<String>();
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(stackName, "UTF-8") + "/resources";

            RESTResponse response = connection.processRequest(uri, "GET");
            String body = response.getResponseBody();
            JSONObject responseJson = new JSONObject(body);
            JSONArray resources = responseJson.getJSONArray("resources");
            for (int i = 0; i < resources.length(); i++) {
                JSONObject resource = resources.getJSONObject(i);
                InstanceType type = getInstanceType(
                        resource.optString("resource_type"));
                if (type == null) {
                    continue;
                }
                switch (type) {
                case NOVA:
                case EC2:
                case TROVE:
                    serverIds.add(resource.optString("physical_resource_id"));
                    break;
                default:
                    continue;
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        } catch (JSONException e) {
            logger.error("HeatClient.getStackDetails()", e);
            throw new HeatException(e.getMessage());
        }
        return serverIds;
    }

    private String getServerIdByStackResource(String stackName)
            throws HeatException {
        logger.debug("HeatClient.getServerId() Endpoint: "
                + connection.getHeatEndpoint());
        String uri;
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(stackName, "UTF-8") + "/resources";

            RESTResponse response = connection.processRequest(uri, "GET");
            String body = response.getResponseBody();
            JSONObject responseJson = new JSONObject(body);
            JSONArray resources = responseJson.getJSONArray("resources");
            for (int i = 0; i < resources.length(); i++) {
                JSONObject resource = resources.getJSONObject(i);
                InstanceType type = getInstanceType(
                        resource.optString("resource_type"));
                if (type == null) {
                    continue;
                }
                switch (type) {
                case NOVA:
                case EC2:
                case TROVE:
                    return resource.optString("physical_resource_id");
                default:
                    continue;
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        } catch (JSONException e) {
            logger.error("HeatClient.getStackDetails()", e);
            throw new HeatException(e.getMessage());
        }
        return null;
    }

    public Stack getStackDetails(String stackName) throws HeatException {
        logger.debug("HeatClient.getStackDetails() Endpoint: "
                + connection.getHeatEndpoint());
        String uri;
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(stackName, "UTF-8");

            RESTResponse response = connection.processRequest(uri, "GET");
            String body = response.getResponseBody();
            logger.debug("HeatClient.getStackDetails() Responsecode: "
                    + response.getResponseCode());
            JSONObject responseJson = new JSONObject(body);
            JSONObject stack = responseJson.getJSONObject("stack");
            Stack result = new Stack();
            result.setId(stack.getString("id"));
            result.setStatus(stack.getString("stack_status"));
            try {
                result.setStatusReason(stack.getString("stack_status_reason"));
            } catch (JSONException e) {
                logger.error("HeatClient.getStackDetails()", e);
                result.setStatusReason("n/a");
            }

            try {
                JSONArray outputs = stack.getJSONArray("outputs");
                for (int i = 0; i < outputs.length(); i++) {
                    JSONObject output = outputs.getJSONObject(i);
                    result.addOutput(output.optString("output_key"),
                            output.optString("output_value"));
                }
            } catch (JSONException e) {
                // ignore if output is not yet here
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        } catch (JSONException e) {
            logger.error("HeatClient.getStackDetails()", e);
            throw new HeatException(e.getMessage());
        }
    }

    public void deleteStack(String stackName) throws HeatException {
        logger.debug("HeatClient.deleteStack() Endpoint: "
                + connection.getHeatEndpoint());
        String uri;
        try {
            uri = connection.getHeatEndpoint() + "/stacks/"
                    + URLEncoder.encode(stackName, "UTF-8");

            RESTResponse response = connection.processRequest(uri, "DELETE");
            logger.debug("HeatClient.deleteStack() Responsecode: "
                    + response.getResponseCode());
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
