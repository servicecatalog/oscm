/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 22.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.transport;

import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Auxiliary factory to retrieve either HTTP method objects for productive
 * environment or test related stubs.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class HttpMethodFactory {

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
        HttpMethodFactory.inTestMode = inTestMode;
    }

    public static PostMethod getPostMethod(String url) {
        if (inTestMode) {
            try {
                Class<?> testStubClass = Class
                        .forName("org.oscm.payproc.stubs.PostMethodStub");
                return (PostMethod) testStubClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate test stub");
            }
        }
        return new PostMethod(url);
    }

}
