/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 12.03.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ct.login;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory to provide the correct implementation of the LoginHandler.
 * 
 * <p>
 * <b>NOTE:</b> For now only glassfish handling is supported.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@Deprecated
public class LoginHandlerFactory {

    public static LoginHandler getInstance() throws ClassNotFoundException,
            IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException,
            SecurityException, NoSuchMethodException {
        LoginHandler result = null;
        Class<?> handler = Class
                .forName("org.oscm.ct.login.ProgrammaticLoginHandler");
        Constructor<?> defaultConstructor = handler
                .getDeclaredConstructor(new Class[0]);
        result = (LoginHandler) defaultConstructor.newInstance((Object[]) null);
        return result;
    }

}
