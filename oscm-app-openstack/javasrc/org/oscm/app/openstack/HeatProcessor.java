/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Dec 6, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.oscm.app.openstack.controller.OpenStackController;
import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.openstack.controller.StackStatus;
import org.oscm.app.openstack.data.CreateStackRequest;
import org.oscm.app.openstack.data.Stack;
import org.oscm.app.openstack.data.UpdateStackRequest;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.oscm.app.openstack.i18n.Messages;
import org.oscm.app.openstack.proxy.ProxyAuthenticator;
import org.oscm.app.openstack.proxy.ProxySettings;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.intf.APPTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make Heat API calls to create, update and delete stacks.
 */
public class HeatProcessor {

    private static final Logger logger = LoggerFactory
            .getLogger(HeatProcessor.class);
    private static URLStreamHandler streamHandler;

    /**
     * Sets the URL stream handler. <b>Should only be used for unit testing!</b>
     * 
     * @param streamHandler
     */
    public static void setURLStreamHandler(URLStreamHandler streamHandler) {
        HeatProcessor.streamHandler = streamHandler;
    }

    /**
     * Creates a stack. If the given stack name is not unique a random number
     * will be attached.
     * 
     * @param ph
     *            contains all parameters for authentication and creating the
     *            stack
     * @throws HeatException
     */
    public void createStack(PropertyHandler ph)
            throws HeatException, APPlatformException {
        logger.debug(
                "HeatProcessor.createStack() stackname: " + ph.getStackName());
        HeatClient heatClient = createHeatClient(ph);
        String template = getTemplate(ph, "create");

        String id = String.valueOf((ph.getSettings().getSubscriptionId()
                + ph.getSettings().getOrganizationId()).hashCode());
        if (!ph.getStackName().endsWith(id)) {
            ph.setStackName(ph.getStackName() + id);
        }

        CreateStackRequest request = (CreateStackRequest) new CreateStackRequest(
                ph.getStackName()).withTemplate(template)
                        .withParameters(ph.getTemplateParameters());
        try {
            Stack created = heatClient.createStack(request);
            ph.setStackId(created.getId());
        } catch (HeatException ex) {
            try {
                ph.setStackId(getStackDetails(ph).getId());
            } catch (HeatException ex2) {
                throw ex;
            }
        }
    }

    private HeatClient createHeatClient(PropertyHandler ph)
            throws HeatException, APPlatformException {
        OpenStackConnection connection = new OpenStackConnection(
                ph.getKeystoneUrl());
        KeystoneClient client = new KeystoneClient(connection);
        try {
            client.authenticate(ph.getUserName(), ph.getPassword(),
                    ph.getDomainName(), ph.getTenantId());
        } catch (OpenStackConnectionException ex) {
            throw new HeatException(
                    "Failed to connect to Heat: " + ex.getMessage(),
                    ex.getResponseCode());
        }
        return new HeatClient(connection);
    }

    /**
     * Change an existing stack. The stack is identified by its name.
     * 
     * @param ph
     * @throws HeatException
     */
    public void updateStack(PropertyHandler ph)
            throws HeatException, APPlatformException {
        logger.debug(
                "HeatProcessor.updateStack() stackname: " + ph.getStackName());
        String template = getTemplate(ph, "update");
        UpdateStackRequest request = (UpdateStackRequest) new UpdateStackRequest(
                ph.getStackName()).withTemplate(template)
                        .withParameters(ph.getTemplateParameters());
        createHeatClient(ph).updateStack(request);
    }

    private String getTemplate(PropertyHandler ph, String type)
            throws AbortException {
        String url = null;
        try {
            String name = ph.getTemplateName();

            try {
                APPTemplateService templateService = getTemplateService();

                Template t = templateService.getTemplate(name,
                        OpenStackController.ID, ph.getTPAuthentication());

                return new String(t.getContent(), StandardCharsets.UTF_8);
            } catch (NamingException | APPlatformException e) {
                // service or template not found, continue with url
            }

            url = ph.getTemplateUrl();
            return getText(url);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AbortException(
                    Messages.getAll("error_" + type + "_failed_customer"),
                    Messages.getAll("error_provider_template_read_exception",
                            type, url == null ? "-" : url,
                            e.getClass().getName() + " - " + e.getMessage(),
                            "-"));
        }
    }

    /**
     * Protected method for unit test purposes.
     */
    protected APPTemplateService getTemplateService() throws NamingException {
        return InitialContext.doLookup(APPTemplateService.JNDI_NAME);
    }

    private static String getText(String url) throws Exception {

        HttpURLConnection connection = connectUsingProxy(url);

        StringBuilder response = new StringBuilder();
        String inputLine;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        try {
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine).append("\n");
        } finally {
            in.close();
        }
        return response.toString();
    }

    private static HttpURLConnection connectUsingProxy(String restUri)
            throws MalformedURLException, IOException, HeatException {

        URL url = new URL(null, restUri, streamHandler);
        HttpURLConnection connection;
        try {

            if (ProxySettings.useProxyByPass(url)) {
                connection = (HttpURLConnection) url
                        .openConnection(Proxy.NO_PROXY);
            } else {

                String proxyHost = System
                        .getProperty(ProxySettings.HTTPS_PROXY_HOST);
                String proxyPort = System
                        .getProperty(ProxySettings.HTTPS_PROXY_PORT);
                String proxyUser = System
                        .getProperty(ProxySettings.HTTPS_PROXY_USER);
                String proxyPassword = System
                        .getProperty(ProxySettings.HTTPS_PROXY_PASSWORD);

                int proxyPortInt = 0;

                try {
                    proxyPortInt = Integer.parseInt(proxyPort);
                } catch (NumberFormatException e) {
                    // ignore
                }
                if (proxyHost != null && proxyPortInt > 0) {
                    // TODO check proxy type for HTTPS protocol
                    Proxy proxy = new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(proxyHost, proxyPortInt));

                    if (proxyUser != null && proxyUser.length() > 0
                            && proxyPassword != null
                            && proxyPassword.length() > 0) {

                        Authenticator.setDefault(new ProxyAuthenticator(
                                proxyUser, proxyPassword));

                    }

                    connection = (HttpURLConnection) url.openConnection(proxy);
                } else {
                    connection = (HttpURLConnection) url
                            .openConnection(Proxy.NO_PROXY);
                }
                if (url.getProtocol().equals("https")) {
                    // TODO
                    // This setting is only needed for K5.
                    // We have to support multi protocols.
                    SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
                    sslcontext.init(null, null, null);
                    ((HttpsURLConnection) connection)
                            .setSSLSocketFactory(sslcontext.getSocketFactory());
                }

            }

        } catch (ClassCastException e) {
            throw new HeatException(
                    "Connection to Heat could not be created. Expected http(s) connection for URL: "
                            + restUri);
        } catch (NoSuchAlgorithmException e) {
            throw new HeatException(
                    "NoSuchAlgorithmException occurred in SSLContext HeatProcessor");
        } catch (KeyManagementException e) {
            throw new HeatException(
                    "KeyManagementException occurred in SSLContext HeatProcessor");
        }
        return connection;
    }

    /**
     * Delete a stack. The stack is identified by its name.
     * 
     * @param ph
     * @throws HeatException
     */
    public void deleteStack(PropertyHandler ph)
            throws HeatException, APPlatformException {
        logger.debug(
                "HeatProcessor.deleteStack() stackname: " + ph.getStackName());
        createHeatClient(ph).deleteStack(ph.getStackName());
    }

    public Stack getStackDetails(PropertyHandler ph)
            throws HeatException, APPlatformException {
        return createHeatClient(ph).getStackDetails(ph.getStackName());
    }

    public boolean resumeStack(PropertyHandler ph)
            throws HeatException, APPlatformException {
        if (!createHeatClient(ph).checkServerExists(ph.getStackName())) {
            throw new InstanceNotAliveException(Messages
                    .getAll("error_activating_failed_instance_not_found"));
        }

        if (StackStatus.SUSPEND_COMPLETE.name().equals(createHeatClient(ph)
                .getStackDetails(ph.getStackName()).getStatus())) {
            createHeatClient(ph).resumeStack(ph.getStackName(),
                    ph.getStackId());
            return true;
        }
        return false;
    }

    public boolean suspendStack(PropertyHandler ph)
            throws HeatException, APPlatformException {
        if (!createHeatClient(ph).checkServerExists(ph.getStackName())) {
            throw new InstanceNotAliveException(Messages
                    .getAll("error_deactivating_failed_instance_not_found"));
        }

        if (!StackStatus.SUSPEND_COMPLETE.name().equals(createHeatClient(ph)
                .getStackDetails(ph.getStackName()).getStatus())) {
            createHeatClient(ph).suspendStack(ph.getStackName(),
                    ph.getStackId());
            return true;
        }
        return false;
    }

}
