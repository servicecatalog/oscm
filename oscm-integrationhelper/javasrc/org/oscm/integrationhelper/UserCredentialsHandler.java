/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.integrationhelper;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class is an implementation of a CallbackHandler. It checks the
 * instance(s) of the <code>Callback</code> object(s) passed in to retrieve or
 * display the requested information.
 */
public class UserCredentialsHandler implements CallbackHandler {

    private static final String PROPERTY_FILE_NAME = "tokenhandler.properties";
    private static final WsProxyInfo wsProxyInfo = new WsProxyInfo(
            PROPERTY_FILE_NAME);

    /**
     * Retrieve or display the information requested in the provided Callbacks.
     * 
     * @param callbacks
     *            an array of <code>Callback</code> objects provided by an
     *            underlying security service which contains the information
     *            requested to be retrieved or displayed.
     * 
     * @throws IOException
     *             if an input or output error occurs.
     * 
     * @throws UnsupportedCallbackException
     *             if the implementation of this method does not support one or
     *             more of the Callbacks specified in the <code>callbacks</code>
     *             parameter.
     */
    @Override
    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(wsProxyInfo
                        .getUserCredentials().getUser());
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(wsProxyInfo
                        .getUserCredentials().getPassword().toCharArray());
            } else {
                throw new UnsupportedCallbackException(callback,
                        "Unrecognized Callback");
            }
        }
    }

}
