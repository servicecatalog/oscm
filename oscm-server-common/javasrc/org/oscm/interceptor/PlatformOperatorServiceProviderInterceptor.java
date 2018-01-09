/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 20.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.exception.UnsupportedOperationException;

/**
 * In case OSCM is in SAML_SP mode, checks if the user, given as an argument to
 * the invoked method, is the initial platform operator (user key 1000).
 * 
 * @author roderus
 * 
 */
public class PlatformOperatorServiceProviderInterceptor {

    @Inject
    ConfigurationServiceLocal configService;

    @EJB
    IdentityService identityService;

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {

        if (configService.isServiceProvider()) {

            long userKey = identityService.getCurrentUserDetails().getKey();

            if (userKey != 1000L) {
                UnsupportedOperationException e = new UnsupportedOperationException(
                        "In SAML_SP mode the password can only be reset for the platform operator with key 1000.");

                Log4jLogger logger = LoggerFactory.getLogger(context
                        .getTarget().getClass());
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_OPERATION_ONLY_FOR_1000);
                throw e;

            }
        }

        return context.proceed();
    }

}
