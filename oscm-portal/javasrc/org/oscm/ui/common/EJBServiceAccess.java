/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.common;

import java.util.Properties;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOUser;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.resolver.IPResolver;
import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * Implementation to access the services with EJB (IIOP)
 */
public class EJBServiceAccess extends ServiceAccess {

    private static String REALM = "bss-realm";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(EJBServiceAccess.class);

    public EJBServiceAccess() {
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        try {
            Properties p = new Properties();
            p.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.openejb.client.LocalInitialContextFactory");
            Context context = new InitialContext(p);
            T service = clazz.cast(context.lookup(clazz.getName()));
            return service;
        } catch (NamingException e) {
            throw new SaaSSystemException("Service lookup failed!", e);
        }
    }

    @Override
    protected void doLogin(VOUser userObject, String password,
            HttpServletRequest request, HttpServletResponse response)
            throws CommunicationException, LoginException {

        if (userObject == null) {
            String ipAddress = IPResolver.resolveIpAddress(request);
            String msg = String.format(
                    "Login failed! User='null' (access from %s)", ipAddress);
            LoginException le = new LoginException(msg);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, le,
                    LogMessageIdentifier.WARN_USER_LOGIN_FAILED, "null",
                    ipAddress);
            throw le;
        }

        try {
            request.getSession();
            request.login(String.valueOf(userObject.getKey()), password);
            ejbLogin(userObject.getKey(), password);

        } catch (Exception e) {
            String ipAddress = IPResolver.resolveIpAddress(request);
            if (e instanceof ServletException) {

                String msg = String.format(
                        "Login failed! User='%s' (access from %s)",
                        userObject.getUserId(), ipAddress);
                LoginException le = new LoginException(msg);
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        le, LogMessageIdentifier.WARN_USER_LOGIN_FAILED,
                        userObject.getUserId(), ipAddress);
                throw le;
            } else {

                logger.logError(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        e, LogMessageIdentifier.ERROR_USER_LOGIN_FAILED,
                        userObject.getUserId(), ipAddress);

                CommunicationException ex = getCausedByCommunicationException(
                        e);
                if (ex != null) {
                    throw ex;
                }

                throw new LoginException(
                        "Login failed! User='" + userObject.getUserId()
                                + "' caused by " + e.getMessage());
            }
        }
    }

    private void ejbLogin(long key, String password) throws LoginException {
        final SecurityService securityService = SystemInstance.get()
                .getComponent(SecurityService.class);
        final Object token;
            securityService.disassociate();

            token = securityService.login(
                    Long.valueOf(key).toString(),
                    password);
            if (AbstractSecurityService.class.isInstance(securityService)
                    && AbstractSecurityService.class.cast(securityService)
                    .currentState() == null) {
                securityService.associate(token);
            }
    }

    @Override
    protected boolean createSession() {
        return true;
    }

    /**
     * Determines if the caught exception was caused by a communication
     * exception and return it. If so, the LDAP server cannot be reached.
     * 
     * @param caughtException
     *            The exception to check the cause for.
     * @return The first found CommunicationException or null.
     */
    private CommunicationException getCausedByCommunicationException(
            Throwable caughtException) {
        Throwable cause = caughtException.getCause();
        while (cause != null) {
            if (cause instanceof CommunicationException) {
                return (CommunicationException) cause;
            }
            if (cause.getMessage()
                    .contains("javax.naming.CommunicationException")) {
                return new CommunicationException();
            }
            cause = cause.getCause();
        }
        return null;
    }

}
