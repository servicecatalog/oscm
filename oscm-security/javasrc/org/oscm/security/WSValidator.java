/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 28.07.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import javax.security.auth.login.LoginException;

import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.validate.Credential;

/**
 * @author stavreva
 *
 */
public class WSValidator
        extends org.apache.openejb.server.cxf.OpenEJBLoginValidator {
        
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Credential validate(Credential credential, RequestData requestData) {
        try {

            final SecurityService securityService = SystemInstance.get()
                    .getComponent(SecurityService.class);
            final Object token;
            try {
                securityService.disassociate();

                token = securityService.login(
                        credential.getUsernametoken().getName(),
                        credential.getUsernametoken().getPassword());
                if (AbstractSecurityService.class.isInstance(securityService)
                        && AbstractSecurityService.class.cast(securityService)
                                .currentState() == null) {
                    securityService.associate(token);
                }
            } catch (final LoginException e) {
                throw new SecurityException("cannot log user "
                        + credential.getUsernametoken().getName(), e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return credential;
    }

}
