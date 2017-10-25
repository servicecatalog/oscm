/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.wsdl.xml.WSDLLocator;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.InputSource;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * WSDL locator implementation to load a WSDL and all related imports using the
 * specified credentials.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BasicAuthWSDLLocator implements WSDLLocator {

    private final static Log4jLogger logger = LoggerFactory
            .getLogger(WSDLLocator.class);

    private String userName;
    private String password;
    private String wsdlBaseURL;
    private String importLocation;
    private InputStream in;

    public BasicAuthWSDLLocator(String wsdlBaseURL, String userName,
            String password) {
        this.wsdlBaseURL = wsdlBaseURL;
        this.userName = userName;
        this.password = password;
    }

    private InputSource createInputSource(String url) throws IOException {
        final HttpClient client = new HttpClient();
        final String proxyHost = System.getProperty("http.proxyHost");
        final String proxyPort = System.getProperty("http.proxyPort", "80");
        if (proxyHost != null && proxyHost.trim().length() > 0) {
            try {
                client.getHostConfiguration().setProxy(proxyHost.trim(),
                        Integer.parseInt(proxyPort));
            } catch (NumberFormatException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_USE_PROXY_DEFINITION_FAILED);
            }
        }

        // Set credentials if specified:
        URL targetURL = new URL(url);
        if (userName != null && userName.length() > 0) {
            client.getParams().setAuthenticationPreemptive(true);
            final Credentials credentials = new UsernamePasswordCredentials(
                    userName, password);
            client.getState().setCredentials(
                    new AuthScope(targetURL.getHost(), targetURL.getPort(),
                            AuthScope.ANY_REALM), credentials);
        }

        // Retrieve content:
        // opening a local resource isn't supported by apache
        // instead open stream directly
        if (targetURL.getProtocol().startsWith("file")) {
            in = targetURL.openStream();
            return new InputSource(in);
        }
        final GetMethod method = new GetMethod(url);
        final int status = client.executeMethod(method);
        if (status != HttpStatus.SC_OK) {
            throw new IOException("Error " + status + " while retrieving "
                    + url);
        }
        in = method.getResponseBodyAsStream();
        return new InputSource(in);
    }

    @Override
    public InputSource getBaseInputSource() {
        try {
            return createInputSource(wsdlBaseURL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBaseURI() {
        return wsdlBaseURL;
    }

    @Override
    public InputSource getImportInputSource(String parentLocation,
            String importLocation) {
        this.importLocation = importLocation;
        try {
            if (parentLocation == null) {
                return createInputSource(importLocation);
            } else {
                URL parentUrl = new URL(parentLocation);
                return createInputSource(new URL(parentUrl, importLocation)
                        .toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLatestImportURI() {
        return importLocation;
    }

    @Override
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // ignore, stream is already closed
            }
        }
    }
}
