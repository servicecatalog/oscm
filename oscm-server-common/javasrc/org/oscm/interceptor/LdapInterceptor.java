/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 9, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import java.util.Properties;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.internal.types.exception.UnsupportedOperationException;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author farmaki
 * 
 */
public class LdapInterceptor {

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    ConfigurationServiceLocal configService;

    /**
     * Ensures that LDAP support is disabled if OSCM acts as a SAML SP.
     */
    @AroundInvoke
    public Object ensureLdapDisabledForServiceProvider(InvocationContext context)
            throws Exception {
        Object result = null;
        Object organizationProperties = null;

        Object[] parameters = context.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] instanceof LdapProperties
                    || parameters[i] instanceof Properties) {
                organizationProperties = parameters[i];
            }
        }

        if (configService.isServiceProvider() && organizationProperties != null) {
            UnsupportedOperationException e = new UnsupportedOperationException(
                    "It is forbidden to perform this operation if a OSCM acts as a SAML service provider.");

            Log4jLogger logger = LoggerFactory.getLogger(context.getTarget()
                    .getClass());
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_OPERATION_FORBIDDEN_FOR_SAML_SP);
            throw e;
        }

        result = context.proceed();
        return result;
    }
}
