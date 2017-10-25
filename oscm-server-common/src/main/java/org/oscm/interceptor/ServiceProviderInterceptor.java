/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 2, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.UnsupportedOperationException;

/**
 * @author farmaki
 * 
 */
public class ServiceProviderInterceptor {

    @EJB
    ConfigurationServiceLocal configService;

    /**
     * Checks if OSCM acts as a SAML service provider. If so, an
     * UnsupportedOperationException will be thrown.
     */
    @AroundInvoke
    public Object ensureIsNotServiceProvider(InvocationContext context)
            throws Exception {
        Object result = null;

        if (configService.isServiceProvider()) {
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
