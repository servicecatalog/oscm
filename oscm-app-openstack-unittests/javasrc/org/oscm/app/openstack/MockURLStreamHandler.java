/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Jul 3, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.oscm.app.openstack.controller.ServerStatus;
import org.oscm.app.openstack.controller.StackStatus;

/**
 * @author Dirk Bernsau
 *
 */
public class MockURLStreamHandler extends URLStreamHandler {

    private final Map<String, MockHttpURLConnection> connection;
    private final Map<String, MockHttpsURLConnection> connectionHttps;
    private Map<String, Integer> count;

    public MockURLStreamHandler() {
        List<String> serverNames = new ArrayList<>();
        serverNames.add("server1");
        connection = new HashMap<>();
        put("/v3/auth/tokens",
                new MockHttpURLConnection(201, respTokens(true, true, false)));
        put("/stacks/instanceName", new MockHttpURLConnection(200,
                respStacksInstanceName(StackStatus.CREATE_COMPLETE, true)));
        put("/stacks/Instance4", new MockHttpURLConnection(200,
                respStacksInstanceName(StackStatus.CREATE_COMPLETE, true)));
        put("/stacks/Instance4/resources", new MockHttpURLConnection(200,
                respStacksResources(serverNames, "AWS::EC2::Instance")));
        put("/servers/0-Instance-server1", new MockHttpURLConnection(200,
                respServer("server1", "0-Instance-server1")));
        put("/stacks/Instance4/sID/actions", new MockHttpURLConnection(200,
                respStacksInstance4sIdActions()));
        put("/stacks", new MockHttpURLConnection(200, respStacks()));
        put("/templates/fosi_v2.json",
                new MockHttpURLConnection(200, respTemplatesFosi_v2()));
        put("/templates/fosi_v2_changeResourceId.json",
                new MockHttpURLConnection(200,
                        respTemplatesFosi_v2_changeResourceId()));
        put("/templates/fosi_v2_changeType.json", new MockHttpURLConnection(200,
                respTemplatesFosi_v2_changeType()));
        put("/servers",
                new MockHttpURLConnection(200, repServers(serverNames)));

        // There are using tenant ID for getting server info
        put("/servers/0-Instance-server1/action",
                new MockHttpURLConnection(202, respServerActions()));
        put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.ACTIVE, "testTenantID")));
        put("/flavors/1", new MockHttpURLConnection(200, respFlavor(1, "S-1")));

        connectionHttps = new HashMap<>();
        put("/v3/auth/tokens",
                new MockHttpsURLConnection(201, respTokens(true, true, true)));
        put("/v1/templates/fosi_v2.json",
                new MockHttpsURLConnection(200, respTemplatesFosi_v2()));
        put("/stacks/instanceName", new MockHttpsURLConnection(200,
                respStacksInstanceName(StackStatus.CREATE_COMPLETE, true)));
        put("/stacks/Instance4", new MockHttpsURLConnection(200,
                respStacksInstanceName(StackStatus.CREATE_COMPLETE, true)));
        put("/stacks/Instance4/resources", new MockHttpsURLConnection(200,
                respStacksResources(serverNames, "AWS::EC2::Instance")));
        put("/servers/serverId", new MockHttpsURLConnection(200,
                respServer("Instance4", "serverId")));
        put("/stacks/Instance4/sID/actions", new MockHttpsURLConnection(200,
                respStacksInstance4sIdActions()));
        put("/stacks", new MockHttpsURLConnection(200, respStacks()));
        this.count = new HashMap<>();
    }

    /**
     * @param string
     * @return
     */
    public static String respFlavor(int id, String flavorName) {
        try {
            JSONObject response = new JSONObject();
            JSONObject flavor = new JSONObject();
            flavor.put("id", id);
            flavor.put("name", flavorName);

            response.put("flavor", flavor);
            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return
     */
    private static String repServers(List<String> serverNames) {
        try {
            JSONObject response = new JSONObject();
            JSONArray servers = new JSONArray();
            for (int i = 0; i < serverNames.size(); i++) {
                JSONObject server = new JSONObject();
                JSONArray links = new JSONArray();
                JSONObject linkSelf = new JSONObject();
                JSONObject linkBookmark = new JSONObject();
                String instanceId = Integer.toString(i) + "-Instance-"
                        + serverNames.get(i);
                server.put("id", instanceId);
                linkSelf.put("href",
                        "http://novaendpoint/v2/servers/" + instanceId);
                linkSelf.put("rel", "self");
                links.put(linkSelf);
                linkBookmark.put("href",
                        "http://novaendpoint/servers/" + instanceId);
                linkBookmark.put("rel", "self");
                links.put(linkBookmark);
                server.put("links", links);
                server.put("name", serverNames.get(i));
                servers.put(server);
            }
            response.put("servers", servers);
            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void put(String url, MockHttpsURLConnection mock) {
        connectionHttps.put(url, mock);
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        URLConnection conn = null;
        Integer count = this.count.get(u.getFile()) != null
                ? this.count.get(u.getFile()) : Integer.valueOf(0);
        int newCount = count.intValue();
        if (newCount == 0) {
            newCount = 1;
        } else {
            newCount = count.intValue() + 1;
        }
        this.count.put(u.getFile(), Integer.valueOf(newCount));
        if (u.getProtocol().equals("https")) {
            if (newCount > 1) {
                String newURL = "https://" + u.getHost() + u.getPath() + "/"
                        + newCount;
                URL url = new URL(newURL);
                if (connectionHttps.get(url.getFile()) != null) {
                    conn = connectionHttps.get(url.getFile());
                } else {
                    conn = connectionHttps.get(u.getFile());
                }
            } else {
                conn = connectionHttps.get(u.getFile());
            }
        } else {
            if (newCount > 1) {
                String newURL = "http://" + u.getHost() + u.getPath() + "/"
                        + this.count;
                URL url = new URL(newURL);
                if (connection.get(url.getFile()) != null) {
                    conn = connection.get(url.getFile());
                } else {
                    conn = connection.get(u.getFile());
                }
            } else {
                conn = connection.get(u.getFile());
            }
        }
        if (conn == null) {
            throw new RuntimeException(
                    "Mock connection for " + u.getFile() + " not found!");
        }
        return conn;
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        URLConnection conn = null;
        Integer count = this.count.get(u.getFile()) != null
                ? this.count.get(u.getFile()) : Integer.valueOf(0);
        int newCount = count.intValue();
        if (newCount == 0) {
            newCount = 1;
        } else {
            newCount = count.intValue() + 1;
        }
        this.count.put(u.getFile(), Integer.valueOf(newCount));
        if (u.getProtocol().equals("https")) {
            if (newCount > 1) {
                String newURL = "https://" + u.getHost() + u.getPath() + "/"
                        + newCount;
                URL url = new URL(newURL);
                if (connectionHttps.get(url.getFile()) != null) {
                    conn = connectionHttps.get(url.getFile());
                } else {
                    conn = connectionHttps.get(u.getFile());
                }
            } else {
                conn = connectionHttps.get(u.getFile());
            }
        } else {
            if (newCount > 1) {
                String newURL = "http://" + u.getHost() + u.getPath() + "/"
                        + newCount;
                URL url = new URL(newURL);
                if (connection.get(url.getFile()) != null) {
                    conn = connection.get(url.getFile());
                } else {
                    conn = connection.get(u.getFile());
                }
            } else {
                conn = connection.get(u.getFile());
            }
        }
        if (conn == null) {
            throw new RuntimeException(
                    "Mock connection for " + u.getFile() + " not found!");
        }
        return conn;
    }

    public void put(String url, MockHttpURLConnection mock) {
        connection.put(url, mock);
    }

    public static String respTokens(boolean addPublicURL,
            boolean addPublicURLNova, boolean https) {
        try {
            JSONObject response = new JSONObject();
            JSONObject token = new JSONObject();
            JSONArray serviceCatalog = new JSONArray();

            JSONObject endpointsHeat = new JSONObject();
            JSONArray endpointsListHeat = new JSONArray();
            String httpMethod = https == true ? "https://" : "http://";
            endpointsHeat.put("endpoints", endpointsListHeat);
            if (addPublicURL) {
                JSONObject publicUrl = new JSONObject();
                publicUrl.put("name", KeystoneClient.TYPE_HEAT);
                publicUrl.put("url", httpMethod + "heatendpoint/");
                publicUrl.put("interface", "public");
                endpointsListHeat.put(publicUrl);
            }

            endpointsHeat.put("type", KeystoneClient.TYPE_HEAT);
            serviceCatalog.put(endpointsHeat);

            JSONObject endpointsNova = new JSONObject();

            JSONArray endpointsListNova = new JSONArray();
            endpointsNova.put("endpoints", endpointsListNova);
            if (addPublicURLNova) {
                JSONObject publicUrlNova = new JSONObject();
                publicUrlNova.put("name", KeystoneClient.TYPE_NOVA);
                publicUrlNova.put("url", httpMethod + "novaendpoint/");
                publicUrlNova.put("interface", "public");
                endpointsListNova.put(publicUrlNova);
            }

            endpointsNova.put("type", KeystoneClient.TYPE_NOVA);
            serviceCatalog.put(endpointsNova);

            token.put("id", "authId");
            token.put("catalog", serviceCatalog);

            response.put("token", token);

            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String respStacksInstanceName(StackStatus status,
            boolean withStatusReason, String... stackStatusReason) {
        String reason;
        if (stackStatusReason == null || stackStatusReason.length == 0) {
            reason = "SSR";
        } else {
            reason = Arrays.toString(stackStatusReason);
        }
        try {
            JSONObject response = new JSONObject();
            JSONObject stack = new JSONObject();
            response.put("stack", stack);
            stack.put("stack_name", "SN");
            stack.put("id", "ID");
            stack.put("stack_status",
                    status == null ? "bullshit" : status.name());
            if (withStatusReason) {
                stack.put("stack_status_reason", reason);
            }
            JSONArray outputs = new JSONArray();
            JSONObject output = new JSONObject();
            output.put("output_key", "OK");
            output.put("output_value", "OV");
            outputs.put(output);
            stack.put("outputs", outputs);
            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String respStacksResources(List<String> serverNames,
            String resourceType) {
        try {
            JSONObject response = new JSONObject();
            JSONArray resources = new JSONArray();
            response.put("resources", resources);

            JSONObject volume = new JSONObject();
            volume.put("resource_name", "sys-vol");
            volume.put("physical_resource_id", "12345");
            volume.put("resource_type", "OS::Cinder::Volume");
            resources.put(volume);

            for (int i = 0; i < serverNames.size(); i++) {
                JSONObject server = new JSONObject();
                server.put("resource_name", serverNames.get(i));
                server.put("physical_resource_id", Integer.toString(i)
                        + "-Instance-" + serverNames.get(i));
                server.put("resource_type", resourceType);
                resources.put(server);
            }

            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String respServer(String serverName, String serverId) {
        try {
            JSONObject response = new JSONObject();
            JSONObject server = new JSONObject();
            response.put("server", server);
            server.put("name", serverName);
            server.put("id", serverId);

            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String respServerDetail(String serverName, String serverId,
            ServerStatus status, String tenant_id) {
        try {
            JSONObject response = new JSONObject();
            JSONObject server = new JSONObject();
            response.put("server", server);
            server.put("name", serverName);
            server.put("id", serverId);
            server.put("status", status);
            server.put("tenant_id", tenant_id);
            server.put("accessIPv4", "192.0.2.0");
            JSONObject flavor = new JSONObject();
            flavor.put("id", 1);
            server.put("flavor", flavor);
            JSONObject addresses = new JSONObject();
            JSONArray networkDetail = new JSONArray();
            JSONObject fixedNetwork = new JSONObject();
            JSONObject floatingNetwork = new JSONObject();
            fixedNetwork.put("OS-EXT-IPS-MAC:mac_addr", "fa:16:3e:e5:b7:f8");
            fixedNetwork.put("version", "4");
            fixedNetwork.put("addr", "192.168.0.4");
            fixedNetwork.put("OS-EXT-IPS:type", "fixed");
            floatingNetwork.put("OS-EXT-IPS-MAC:mac_addr", "fa:16:3e:e5:b7:f8");
            floatingNetwork.put("version", "4");
            floatingNetwork.put("addr", "133.162.161.216");
            floatingNetwork.put("OS-EXT-IPS:type", "floating");
            networkDetail.put(fixedNetwork);
            networkDetail.put(floatingNetwork);
            addresses.put(serverName + "-network", networkDetail);
            server.put("addresses", addresses);

            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String respServerActions() {
        JSONObject response = new JSONObject();
        return response.toString();
    }

    private static String respStacksInstance4sIdActions() {
        JSONObject response = new JSONObject();
        return response.toString();
    }

    private static String respStacks() {
        try {
            JSONObject response = new JSONObject();
            JSONObject state = new JSONObject();
            state.put("label", "OK");
            state.put("id", "OK");
            response.put("state", state);
            response.put("id", "1234");
            response.put("stack", "1234");
            response.put("name", "Appliance1");
            response.put("projectUri",
                    "http://test.com/cloud/api/projects/54346");
            JSONObject stack = new JSONObject();
            stack.put("id", "idValue");
            response.put("stack", stack);
            return response.toString();
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String respStackDeleted() {
        JSONObject response = new JSONObject();
        return response.toString();
    }

    public static String respTemplatesFosi_v2() {
        return "{\"AWSTemplateFormatVersion\": \"2010-09-09\","
                + "\"Parameters\": {\"InstanceType\" : {"
                + "\"Description\" : \"Server instance type\","
                + "\"Type\" : \"String\",\"Default\" : \"m1.tiny\"},"
                + "\"ImageId\" : {"
                + "\"Description\" : \"The image to start up\","
                + "\"Type\" : \"String\","
                + "\"Default\" : \"3e0e9883-382a-4068-8962-f883b5e39b31\""
                + "},\"KeyName\" : {"
                + "\"Description\" : \"Reference to keypair\","
                + "\"Type\" : \"String\",\"Default\" : \"dirk\"}},"
                + "\"Resources\": {\"Server\": {"
                + "\"Type\" : \"AWS::EC2::Instance\",\"Properties\" : {"
                + "\"InstanceType\" : { \"Ref\" : \"InstanceType\" },"
                + "\"KeyName\" : { \"Ref\" : \"KeyName\" },"
                + "\"ImageId\" : { \"Ref\" : \"ImageId\" }}"
                + "},\"IP\": { \"Type\": \"AWS::EC2::EIP\"},"
                + "\"IP_Assoc\": {\"Type\": \"AWS::EC2::EIPAssociation\","
                + "\"Properties\": {\"EIP\": { \"Ref\": \"IP\" },"
                + "\"InstanceId\": { \"Ref\": \"Server\" }}}},"
                + "\"Outputs\" : {\"IP_Out\" : {"
                + "\"Description\" : \"IP Address of the access host\","
                + "\"Value\" :  { \"Ref\" : \"IP\" }},\"KP_Out\" : {"
                + "\"Description\" : \"Key pair name\","
                + "\"Value\" :  { \"Ref\" : \"KeyName\" }}" + "}" + "}";
    }

    private static String respTemplatesFosi_v2_changeResourceId() {
        return "{\"AWSTemplateFormatVersion\": \"2010-09-09\","
                + "\"Parameters\": {\"InstanceType\" : {"
                + "\"Description\" : \"Server instance type\","
                + "\"Type\" : \"String\",\"Default\" : \"m1.tiny\"},"
                + "\"ImageId\" : {"
                + "\"Description\" : \"The image to start up\","
                + "\"Type\" : \"String\","
                + "\"Default\" : \"3e0e9883-382a-4068-8962-f883b5e39b31\""
                + "},\"KeyName\" : {"
                + "\"Description\" : \"Reference to keypair\","
                + "\"Type\" : \"String\",\"Default\" : \"dirk\"}},"
                + "\"Resources\": {\"testServer1\": {"
                + "\"Type\" : \"AWS::EC2::Instance\",\"Properties\" : {"
                + "\"InstanceType\" : { \"Ref\" : \"InstanceType\" },"
                + "\"KeyName\" : { \"Ref\" : \"KeyName\" },"
                + "\"ImageId\" : { \"Ref\" : \"ImageId\" }}"
                + "},\"IP\": { \"Type\": \"AWS::EC2::EIP\"},"
                + "\"IP_Assoc\": {\"Type\": \"AWS::EC2::EIPAssociation\","
                + "\"Properties\": {\"EIP\": { \"Ref\": \"IP\" },"
                + "\"InstanceId\": { \"Ref\": \"Server\" }}}},"
                + "\"Outputs\" : {\"IP_Out\" : {"
                + "\"Description\" : \"IP Address of the access host\","
                + "\"Value\" :  { \"Ref\" : \"IP\" }},\"KP_Out\" : {"
                + "\"Description\" : \"Key pair name\","
                + "\"Value\" :  { \"Ref\" : \"KeyName\" }}" + "}" + "}";
    }

    private static String respTemplatesFosi_v2_changeType() {
        return "{" + "\"heat_template_version\": \"2013-05-23\","
                + "\"description\": \"The Heat Orchestration template used for the creation and provisioning of a stack.\","
                + "\"parameters\": {"
                + "\"param_flavor\": {\"type\": \"string\",\"description\": \"flavor\",\"default\": \"economy\"},"
                + "\"param_image_id\": {\"description\": \"The id of the image to provision.\",\"type\": \"string\",\"default\": \"e43477ea-5120-4053-8552-d50fbed91678\"},"
                + "\"KeyName\": {\"description\": \"The name of an already defined key pair in OpenStack, used for enabling SSH access to the web server.\",\"type\": \"string\",\"default\": \"openstackkeypair\"},"
                + "\"vm_name\": {\"type\": \"string\",\"default\": \"20150612-win-vm-economy\"},"
                + "\"network\": {\"type\": \"string\",\"description\": \"internal network uuid\",\"default\": \"06a2a85b-37b3-4b44-b13d-843900655976\"},"
                + "\"ex-network\": {\"type\": \"string\",\"description\": \"external network uuid\",\"default\": \"d2bef1c2-7059-4cd4-b7c7-4fa5180049be\"},"
                + "\"admin_password\": {\"type\": \"string\",\"default\": \"testCtmg2016!\"}"
                + "}," + "\"resources\": {" + "\"Sys-vol\": {"
                + "\"type\": \"OS::Cinder::Volume\","
                + "\"properties\": {\"name\": \"sys-vol-ctmg\",\"size\": 80,\"image\": {\"get_param\": \"param_image_id\"}}"
                + "}," + "\"Server\": {" + "\"type\": \"OS::Nova::Server\","
                + "\"properties\": {"
                + "\"flavor\": {\"get_param\": \"param_flavor\"},"
                + "\"key_name\": {\"get_param\": \"KeyName\"},"
                + "\"image\": {\"get_param\": \"param_image_id\"},"
                + "\"name\": {\"get_param\": \"vm_name\"},"
                + "\"networks\": [{\"uuid\": {\"get_param\": \"network\"}}],"
                + "\"metadata\": {\"admin_pass\": {\"get_param\": \"admin_password\"}},"
                + "\"block_device_mapping\": [{\"device_name\": \"/dev/vda\",\"volume_size\": \"80\",\"volume_id\": {\"get_resource\": \"Sys-vol\"}}]}"
                + "}," + "\"Port\": {" + "\"type\": \"OS::Neutron::Port\","
                + "\"properties\": {\"network_id\": {\"get_param\": \"network\"},"
                + "}," + "\"IP\": {" + "\"type\": \"OS::Neutron::FloatingIP\","
                + "\"properties\": {\"floating_network_id\": {\"get_param\": \"ex-network\"},"
                + "}," + "\"IPAssoc\": {"
                + "\"type\": \"OS::Neutron::FloatingIPAssociation\","
                + "\"properties\": {\"floatingip_id\": {\"get_resource\": \"IP\"},\"port_id\": {\"get_resource\": \"Port\"}}"
                + "}" + "}," + "\"outputs\": {" + "\"KP_Out\": {"
                + "\"description\": \"Key pair name\","
                + "\"value\": {\"get_param\": \"KeyName\"}" + "},"
                + "\"IP_Out\": {"
                + "\"description\": \"IP Address of the access host\","
                + "\"value\": {\"get_attr\": [\"IP\",\"floating_ip_address\"]}"
                + "}" + "}" + "}";
    }
}
