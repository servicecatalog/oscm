/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.03.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ct.login;

import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.login.LoginException;

/**
 * Provides the login handling for the glassfish application server.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ProgrammaticLoginHandler implements LoginHandler {

    @Override
    public void login(String username, String password) throws Exception {
        final SecurityService securityService = SystemInstance.get()
                .getComponent(SecurityService.class);
        final Object token;
        try {
            securityService.disassociate();

            token = securityService.login(
                    username,
                    password);
            if (AbstractSecurityService.class.isInstance(securityService)
                    && AbstractSecurityService.class.cast(securityService)
                    .currentState() == null) {
                securityService.associate(token);
            }
        } catch (final LoginException e) {
            throw new SecurityException("cannot log user "
                    + username, e);
        }
    }

    @Override
    public void logout() throws Exception {
        final SecurityService securityService = SystemInstance.get()
                .getComponent(SecurityService.class);
        securityService.logout(securityService.disassociate());
    }

}
