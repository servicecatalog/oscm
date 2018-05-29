/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-11-03                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.oscm.app.openstack.proxy.ProxyAuthenticator;
import org.oscm.app.openstack.proxy.ProxySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection to the OpenStack Heat API.
 */
public class OpenStackConnection {

    private static final Logger logger = LoggerFactory
            .getLogger(OpenStackConnection.class);

    private static URLStreamHandler streamHandler;

    private String keystoneEndpoint = "";
    private String heatEndpoint = "";
    private String novaEndpoint = "";
    private String authToken = null;

    /**
     * Sets the URL stream handler. <b>Should only be used for unit testing!</b>
     *
     * @param streamHandler
     */
    public static void setURLStreamHandler(URLStreamHandler streamHandler) {
        OpenStackConnection.streamHandler = streamHandler;
    }

    /**
     *
     * @param keystoneEndpoint
     *            The URL to the Keystone API
     */
    public OpenStackConnection(String keystoneEndpoint) {
        this.keystoneEndpoint = removeTrailingSlash(keystoneEndpoint);
    }

    /**
     *
     * @param endpoint
     *            The URL to the Heat API
     */
    public void setHeatEndpoint(String endpoint) {
        heatEndpoint = removeTrailingSlash(endpoint);
    }

    /**
     *
     * @param endpoint
     *            The URL to the Nova API
     */
    public void setNovaEndpoint(String endpoint) {
        novaEndpoint = removeTrailingSlash(endpoint);
    }

    private String removeTrailingSlash(String endpoint) {
        while (endpoint != null && endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return endpoint;
    }

    /**
     *
     * @return The URL to the Heat API
     */
    public String getHeatEndpoint() {
        return heatEndpoint;
    }

    /**
     *
     * @return The URL to the Nova API
     */
    public String getNovaEndpoint() {
        return novaEndpoint;
    }

    /**
     *
     * @return The URL to the Keystone API
     */
    public String getKeystoneEndpoint() {
        return keystoneEndpoint;
    }

    public RESTResponse processRequest(String restUri, String method)
            throws OpenStackConnectionException {
        return processRequest(restUri, method, null);
    }

    public RESTResponse processRequest(String restUri, String method,
            String requestBody) throws OpenStackConnectionException {
        HttpURLConnection connection = null;
        OutputStreamWriter out = null;
        try {
            HttpURLConnection.setFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);
            connection = connectUsingProxy(restUri, connection);
            logger.debug("Sending " + method + " request to " + restUri);

            connection.setRequestMethod(method);
            if (authToken != null) {
                connection.setRequestProperty("X-Auth-Token", authToken);
            }
            connection.setReadTimeout(300000);

            // add payload if present
            if (requestBody != null) {
                if (!requestBody.contains("password")) {
                    logger.debug("   request body:\n" + requestBody);
                }
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setDoOutput(true);
                out = new OutputStreamWriter(connection.getOutputStream());
                out.write(requestBody);
                out.close();
            }
            connection.connect();
            return new RESTResponse(connection);
        } catch (MalformedURLException e) {
            throw new OpenStackConnectionException("invalid URL: " + restUri);
        } catch (IOException e) {
            int responseCode = -1;
            String responseBody = "";
            if (connection != null) {
                responseCode = getResponseCode(connection);
                responseBody = getErrorResponse(connection);
            }
            final String code = " (HTTP " + responseCode + ", URI " + restUri
                    + ", responseBody " + responseBody + "): " + e.getMessage();
            switch (responseCode) {
            case 400:
                throw new OpenStackConnectionException(
                        "either input parameter format error or security key is not correct"
                                + code,
                        responseCode);
            case 401:
                throw new OpenStackConnectionException("unauthorized" + code,
                        responseCode);

            case 404:
                throw new OpenStackConnectionException(
                        "resource not found" + code, responseCode);

            case 504:
                throw new OpenStackConnectionException(
                        " Gateway/proxy timeout." + code, responseCode);

            default:
                throw new OpenStackConnectionException("send failed" + code,
                        responseCode);
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // best effort pattern
                }
            }
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    // best effort pattern
                }
            }
        }
    }

    /**
     * @param connection
     * @return
     */
    private String getErrorResponse(HttpURLConnection connection) {
        String responseBody;
        try {
            responseBody = readErrorResponseBody(connection);
        } catch (IOException e1) {
            responseBody = "Cannot get any response body";
        }
        return responseBody;
    }

    /**
     * @param connection
     * @return
     */
    private int getResponseCode(HttpURLConnection connection) {
        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e1) {
            responseCode = -1;
        }
        return responseCode;
    }

    protected String readErrorResponseBody(HttpURLConnection connection)
            throws IOException {
        String responseBody;
        if (null != connection.getErrorStream()) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 1) {
                        sb.append(line);
                    }
                }
            } finally {
                in.close();
            }
            responseBody = sb.toString();
        } else {
            throw new IOException("Cannot get any response body");
        }
        return responseBody;
    }

    private HttpURLConnection connectUsingProxy(String restUri,
            HttpURLConnection connection) throws MalformedURLException,
            IOException, OpenStackConnectionException {

        URL url = new URL(null, restUri, streamHandler);

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
                    Proxy proxy = resolveProxy(proxyHost, proxyPortInt);

                    if (proxyUser != null && proxyUser.length() > 0
                            && proxyPassword != null
                            && proxyPassword.length() > 0) {

                        Authenticator.setDefault(new ProxyAuthenticator(
                                proxyUser, proxyPassword));

                    }

                    connection = openConnection(url, proxy);
                }

                else {
                    connection = openConnection(url, Proxy.NO_PROXY);
                }

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

        } catch (ClassCastException e) {
            throw new OpenStackConnectionException(
                    "Connection could not be created. Expected http(s) connection for URL: "
                            + restUri);
        } catch (NoSuchAlgorithmException e) {
            throw new OpenStackConnectionException(
                    "NoSuchAlgorithmException occurred in SSLContext");
        } catch (KeyManagementException e) {
            throw new OpenStackConnectionException(
                    "KeyManagementException occurred in SSLContext");
        }
        return connection;
    }

    protected Proxy resolveProxy(String proxyHost, int proxyPortInt) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(proxyHost, proxyPortInt));
        return proxy;
    }

    protected HttpURLConnection openConnection(URL url, Proxy proxy)
            throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }

    /**
     * If authToken is not set the connection will be used without
     * authentication. The three parameters are set as HTTP request properties.
     * <ul>
     * <li>authToken: X-Auth-Token</li>
     * </ul>
     *
     * @param authToken
     *            generated by Keystone
     */
    protected void useAuthentication(String authToken) {
        this.authToken = authToken;
    }

}
