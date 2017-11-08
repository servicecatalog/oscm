/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

/**
 * @author Mike J&auml;ger
 * 
 */
public class HttpClientStub extends HttpClient {

    public static boolean throwIOException = false;
    public static boolean throwHTTPException = false;

    @Override
    public int executeMethod(HttpMethod method) throws IOException,
            HttpException {
        if (throwHTTPException) {
            throw new HttpException("exception caused by test");
        }
        if (throwIOException) {
            throw new IOException("exception caused by test");
        }
        return 0;
    }

    /**
     * Resets the test related members to default.
     */
    public static void reset() {
        throwIOException = false;
        throwHTTPException = false;
    }

}
