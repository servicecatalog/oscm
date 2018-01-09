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
import java.net.HttpURLConnection;

/**
 * Value object containing important response data from a REST request.
 *
 * @author Dirk Bernsau
 *
 */
public class RESTResponse {

    private int responseCode;
    private String responseBody;
    private String token;

    /**
     * Creates a new instance based on the given connection.
     *
     * @param connection
     *            the connection object
     * @throws IOException
     */
    public RESTResponse(HttpURLConnection connection) throws IOException {

        responseCode = connection.getResponseCode();
        token = connection.getHeaderField("X-Subject-Token");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
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
    }

    /**
     * Returns the status code of the HTTP response.
     *
     * @return the response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Returns the actual content of the HTTP response.
     *
     * @return the content
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Returns the Header of the HTTP response.
     *
     * @return the response Header
     */
    public String getToken() {
        return token;
    }
}
