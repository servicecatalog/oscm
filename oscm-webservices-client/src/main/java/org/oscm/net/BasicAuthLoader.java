/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Helper class to create an URL object that retrieves the content specified by
 * the given URL with credentials. Internally the content is pre-fetched with
 * the given credentials. The returned URL has a custom protocol handler that
 * simply returns the pre-fetched content.
 * 
 * @author hoffmann
 * 
 */
public class BasicAuthLoader {

    /**
     * Creates a new URL object that retrieves the content specified by the
     * given URL with credentials. Internally the content is pre-fetched with
     * the given credentials. The returned URL has a custom protocol handler
     * that simply returns the pre-fetched content.
     * 
     * @param url
     *            the URL to read
     * @param username
     * @param password
     * @return an URL with a custom protocol handler
     * @throws IOException
     *             if an I/O exception occurs.
     */
    public static URL load(final URL url, final String username,
            final String password) throws IOException {

        final byte[] content = getUrlContent(url, username, password);

        final URLStreamHandler handler = new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return new URLConnection(url) {
                    @Override
                    public void connect() throws IOException {
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(content);
                    }
                };
            }
        };
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), url
                .getFile(), handler);
    }

    /**
     * Retrieves the content under the given URL with username and passwort
     * authentication.
     * 
     * @param url
     *            the URL to read
     * @param username
     * @param password
     * @return the read content.
     * @throws IOException
     *             if an I/O exception occurs.
     */
    private static byte[] getUrlContent(URL url, String username,
            String password) throws IOException {
        final HttpClient client = new HttpClient();

        // Set credentials:
        client.getParams().setAuthenticationPreemptive(true);
        final Credentials credentials = new UsernamePasswordCredentials(
                username, password);
        client.getState()
                .setCredentials(
                        new AuthScope(url.getHost(), url.getPort(),
                                AuthScope.ANY_REALM), credentials);

        // Retrieve content:
        final GetMethod method = new GetMethod(url.toString());
        final int status = client.executeMethod(method);
        if (status != HttpStatus.SC_OK) {
            throw new IOException("Error " + status + " while retrieving "
                    + url);
        }
        return method.getResponseBody();
    }

}
