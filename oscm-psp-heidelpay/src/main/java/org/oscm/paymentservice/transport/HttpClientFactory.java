/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 27.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.transport;

import org.apache.commons.httpclient.HttpClient;

/**
 * Auxiliary factory to retrieve either HTTP client objects for productive
 * environment or test related stubs.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class HttpClientFactory {

    /**
     * Indicates whether the factory operates in test mode or not.
     */
    private static boolean inTestMode = false;

    /**
     * Configures the factory for test or for productive mode.
     * 
     * @param inTestMode
     *            The flag to be set. If the value is <code>true</code>, the
     *            factory will only operate with test stubs.
     */
    public static void setTestMode(boolean inTestMode) {
        HttpClientFactory.inTestMode = inTestMode;
    }

    public static HttpClient getHttpClient() {
        if (inTestMode) {
            try {
                Class<?> testStubClass = Class
                        .forName("org.oscm.payproc.stubs.HttpClientStub");
                return (HttpClient) testStubClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate test stub");
            }
        }
        return new HttpClient();
    }

}
