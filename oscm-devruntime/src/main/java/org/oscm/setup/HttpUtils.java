/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                   
 *                                                                              
 *  Creation Date: 03.07.2009                                                      
 *                                                                              
 *  Completion Time:                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * Provides some basic functionality required to reset the example service
 * 
 * @author Peter Pock
 * 
 */
public class HttpUtils {

    /**
     * parameters: serviceResetUrl
     */
    public static void main(String args[]) {
        if (args.length != 1) {
            throw new RuntimeException(
                    "Usage: java HttpUtils <serviceResetUrl>");
        }
        performHttpGet(args[0]);
    }

    /**
     * Perform an HTTP GET request
     * 
     * @param url
     */
    public static final void performHttpGet(String url) {

        if (url == null || url.length() == 0) {
            return;
        }

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            InputStream response = method.getResponseBodyAsStream();
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while (i != -1) {
                    i = response.read(buf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }

}
