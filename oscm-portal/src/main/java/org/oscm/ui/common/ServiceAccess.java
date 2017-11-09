/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 08.12.2010                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.common;

import javax.naming.CommunicationException;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;

/**
 * Abstract class to hide the implementation details for different service
 * access types (EBJ, Customized).
 */
public abstract class ServiceAccess {

    public final static String SESS_ATTR_SERVICE_ACCESS = "ADM_SERVICE_ACCESS";

    private final static String CUSTOM_SERVICE_ACCESS_CLASS = "org.oscm.ui.common.CustomServiceAccess";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ServiceAccess.class);

    /**
     * Get the service access implementation for the given session.
     * 
     * @param session
     *            The current http session.
     * @return the service access implementation for the given session.
     */
    public static ServiceAccess getServiceAcccessFor(HttpSession session) {
        ServiceAccess serviceAccess = (ServiceAccess) session
                .getAttribute(SESS_ATTR_SERVICE_ACCESS);
        if (serviceAccess == null) {
            serviceAccess = createServiceAccess();
            session.setAttribute(SESS_ATTR_SERVICE_ACCESS, serviceAccess);
        }
        return serviceAccess;
    }

    /**
     * Create a new service access instance.
     * 
     * The current HTTP session.
     * 
     * @return the new service access instance.
     */
    private static ServiceAccess createServiceAccess() {
        ServiceAccess serviceAccess = createServiceAccess(CUSTOM_SERVICE_ACCESS_CLASS);
        if (serviceAccess == null) {
            serviceAccess = new EJBServiceAccess();
        }
        return serviceAccess;
    }

    /**
     * Create a new instance for the class with the given classname.
     * 
     * @param classname
     *            The name of the class to instantiate.
     * @return the created instance.
     */
    public static ServiceAccess createServiceAccess(String classname) {
        try {
            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            if (classLoader == null) {
                classLoader = ServiceAccess.class.getClassLoader();
            }
            Class<?> c = null;
            try {
                c = classLoader.loadClass(classname);
            } catch (ClassNotFoundException e) {
                return null;
            }
            return (ServiceAccess) c.newInstance();
        } catch (Throwable t) {
            throw new SaaSSystemException("Instantiation of ServiceAccess "
                    + classname + " failed!", t);
        }
    }

    /**
     * Perform a login.
     * 
     * @param organizationId
     *            the organization identifier
     * @param userId
     *            the user identifier
     * @param password
     *            the password
     * @param request
     *            the current HTTP servlet request
     * @param response
     *            the current HTTP servlet response
     */
    public void login(VOUser userObject, String password,
            HttpServletRequest request, HttpServletResponse response)
            throws CommunicationException, LoginException {

        doLogin(userObject, password, request, response);

        if (createSession()) {
            createPlatformSession(request);
        }
    }

    /**
     * Perform a login.
     * 
     * @param organizationId
     *            the organization identifier
     * @param userId
     *            the user identifier
     * @param password
     *            the password
     * @param request
     *            the current HTTP servlet request
     * @param response
     *            the current HTTP servlet response
     */
    protected abstract void doLogin(VOUser userObject, String password,
            HttpServletRequest request, HttpServletResponse response)
            throws CommunicationException, LoginException;

    /**
     * Create a new platform session entry in the database.
     * 
     * @param sessionId
     *            the sessionId.
     * @throws ValidationException
     */
    protected void createPlatformSession(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            ServiceAccess serviceAccess = ServiceAccess
                    .getServiceAcccessFor(session);

            SessionService service = serviceAccess
                    .getService(SessionService.class);
            service.createPlatformSession(session.getId());

        } catch (ValidationException e) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_PERSIST_FAILED_SESSION_ID_NOT_SET);
        }
    }

    protected abstract boolean createSession();

    /**
     * Get a reference to the requested service interface.
     * 
     * @param <T>
     *            the service interface
     * @param clazz
     *            the class of the service interface
     * @return the requested service
     */
    public abstract <T> T getService(Class<T> clazz);

}
