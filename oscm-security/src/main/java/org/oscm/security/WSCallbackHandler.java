/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: 28.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author stavreva
 *
 */
public class WSCallbackHandler implements CallbackHandler {

    String name;
    String password;

    public WSCallbackHandler(String name, String password) {
        System.out.println("Callback Handler - constructor called");
        this.name = name;
        this.password = password;
    }

    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        System.out.println("Callback Handler - handle called");
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) callbacks[i];
                nameCallback.setName(name);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callbacks[i];
                passwordCallback.setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                        "The submitted Callback is unsupported");
            }
        }
    }

}
