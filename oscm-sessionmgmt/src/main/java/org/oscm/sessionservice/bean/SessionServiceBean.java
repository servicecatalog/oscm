/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.sessionservice.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.permission.PermissionCheck;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SessionType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.DuplicateEventException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Bean implementation of the product session management component.
 * 
 * @author Mike J&auml;ger
 */
@Stateless
@Local(SessionServiceLocal.class)
@Remote(SessionService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class SessionServiceBean implements SessionServiceLocal, SessionService {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SessionServiceBean.class);

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    SubscriptionServiceLocal subMgmt;

    @EJB(beanInterface = UserGroupServiceLocalBean.class)
    UserGroupServiceLocalBean userGroupService;

    @EJB
    private EventServiceBean evtMgmt;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    private ConfigurationServiceLocal cfgSvc;

    @Resource
    private SessionContext sessionCtx;

    @Override
    public void deleteAllSessions() {

        String nodeName = cfgSvc.getNodeName();

        // also here the logout events have to be created; as this is only
        // invoked during the startup, no concurrent access will occur.
        Query query = dm.createNamedQuery("Session.getAllEntriesForNode");
        query.setParameter("nodeName", nodeName);
        for (Session session : ParameterizedTypes.iterable(
                query.getResultList(), Session.class)) {
            if (session.getSessionType() == SessionType.SERVICE_SESSION) {
                createLogoutEvent(session);
            }
        }

        query = dm.createNamedQuery("Session.deleteAllEntriesForNode");
        query.setParameter("nodeName", nodeName);
        query.executeUpdate();

    }

    @Override
    public void createServiceSession(long subscriptionKey, String sessionId,
            String userToken) throws ObjectNotFoundException,
            ServiceParameterException, OperationNotPermittedException,
            ValidationException {

        ArgumentValidator.notNull("sessionId", sessionId);
        ArgumentValidator.notNull("userToken", userToken);

        BLValidator.isDescription("sessionId", sessionId, true);
        BLValidator.isDescription("userToken", userToken, true);

        PlatformUser currentUser = dm.getCurrentUser();

        Session sessionData = new Session();
        sessionData.setPlatformUserId(currentUser.getUserId());
        sessionData.setPlatformUserKey(currentUser.getKey());
        sessionData.setSubscriptionTKey(Long.valueOf(subscriptionKey));
        sessionData.setUserToken(userToken);
        sessionData.setSessionId(sessionId);
        sessionData.setSessionType(SessionType.SERVICE_SESSION);
        sessionData.setNodeName(cfgSvc.getNodeName());

        Session currentSessionData = (Session) dm.find(sessionData);

        if (currentSessionData == null) {
            try {
                Subscription sub = subMgmt.loadSubscription(sessionData
                        .getSubscriptionTKey().longValue());
                // check that the subscription belongs to the current user's
                // organization
                PermissionCheck
                        .owns(sub, currentUser.getOrganization(), logger);
                if (sub.getStatus() == SubscriptionStatus.EXPIRED) {
                    ServiceParameterException ex = new ServiceParameterException(
                            "Subscription '" + sub.getKey() + "' has expired",
                            ParameterType.PLATFORM_PARAMETER,
                            PlatformParameterIdentifiers.PERIOD, null);
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            ex,
                            LogMessageIdentifier.WARN_CREATE_SERVICE_SESSION_FAILED);
                    throw ex;
                }
                if (sub.getStatus() != SubscriptionStatus.ACTIVE
                        && sub.getStatus() != SubscriptionStatus.PENDING_UPD) {
                    OperationNotPermittedException ex = new OperationNotPermittedException(
                            "subscription '" + sub.getKey()
                                    + "' is not active or pending update.");
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            ex,
                            LogMessageIdentifier.WARN_CREATE_SERVICE_SESSION_FAILED);
                    throw ex;
                }

                verifyParameterConcurrentUser(sub);

                dm.persist(sessionData);
            } catch (NonUniqueBusinessKeyException e) {
                // should not occur, if it does, log the problem and throw a
                // system exception
                SaaSSystemException sse = new SaaSSystemException(
                        "Session entry already exists with the same business key",
                        e);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        sse,
                        LogMessageIdentifier.ERROR_CREATE_PRODUCT_SESSION_FAILED);
                throw sse;
            }
        }

    }

    @Override
    public String deleteServiceSession(long subscriptionKey, String sessionId) {
        ArgumentValidator.notNull("sessionId", sessionId);
        Session findTemplate = new Session();
        findTemplate.setSubscriptionTKey(Long.valueOf(subscriptionKey));
        findTemplate.setSessionId(sessionId);
        findTemplate.setSessionType(SessionType.SERVICE_SESSION);
        Session storedSession = (Session) dm.find(findTemplate);
        if (storedSession != null) {
            createLogoutEvent(storedSession);
            dm.remove(storedSession);
        }
        return cfgSvc.getBaseURL() + Marketplace.MARKETPLACE_ROOT
                + "/logoutPage.jsf?subscriptionKey="
                + Long.toHexString(subscriptionKey);
    }

    @Override
    public String resolveUserToken(long subscriptionKey, String sessionId,
            String userToken) {

        ArgumentValidator.notNull("sessionId", sessionId);
        ArgumentValidator.notNull("userToken", userToken);

        String userId = null;
        Session findTemplate = new Session();
        findTemplate.setSubscriptionTKey(Long.valueOf(subscriptionKey));
        findTemplate.setSessionId(sessionId);
        findTemplate.setSessionType(SessionType.SERVICE_SESSION);
        Session storedSession = (Session) dm.find(findTemplate);
        if (storedSession != null) {

            if (storedSession.getUserToken().equals(userToken)) {
                // remember the userId
                userId = storedSession.getPlatformUserId();

                try {
                    // try to find the usage license of the user to get the
                    // mapped application side user id
                    Subscription sub = dm.getReference(Subscription.class,
                            subscriptionKey);
                    UsageLicense license = findUsageLicense(sub, userId);
                    if (license != null
                            && license.getApplicationUserId() != null) {
                        // if an application user id is set return it
                        userId = license.getApplicationUserId();
                    }
                } catch (ObjectNotFoundException e) {
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_SUBSCRIPTION_USAGELICENSE_NOT_FOUND);
                }

                // reset the user token
                storedSession.setUserToken("");

                // record the login event
                GatheredEvent evt = new GatheredEvent();
                evt.setActor(storedSession.getPlatformUserId());
                evt.setType(EventType.PLATFORM_EVENT);
                evt.setEventId(PlatformEventIdentifier.USER_LOGIN_TO_SERVICE);
                evt.setOccurrenceTime(System.currentTimeMillis());
                evt.setSubscriptionTKey(storedSession.getSubscriptionTKey()
                        .longValue());
                try {
                    evtMgmt.recordEvent(evt);
                } catch (DuplicateEventException e) {
                    // such situation has not to be happen cause we don not set
                    // unique id here, it is null
                    logger.logDebug(e.getMessage());
                    userId = "";
                }
            }
        }

        return userId;
    }

    /**
     * Loads the usage license for the given subscription and user or null if
     * none exists.
     */
    @Override
    public UsageLicense findUsageLicense(Subscription subscription,
            String userId) {
        Query query = dm.createNamedQuery("Subscription.findUsageLicense");
        query.setParameter("userId", userId);
        query.setParameter("subscriptionKey",
                Long.valueOf(subscription.getKey()));

        @SuppressWarnings("unchecked")
        List<UsageLicense> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public void deleteSessionsForSessionId(String sessionId) {

        ArgumentValidator.notNull("sessionId", sessionId);

        // must table be locked before to avoid inconsistency of data? - the
        // answer is no, as in case there is a timeout (and that is when this
        // method is called) there is no forwarding of any user request (due to
        // the timeout) that might cause inconsistencies
        List<Session> productSessions = retrieveProductSessionDataForSessionId(sessionId);

        // needn't create events for platform logout/login, so retrieval of
        // platform events is not required. Important is only that they are
        // deleted by the following operation as well.
        deletePlatformSession(sessionId);

        for (Session session : productSessions) {
            // Sessions for products with access type LOGIN might be used
            // although the BES session is destroyed.
            Subscription sub = null;
            try {
                sub = subMgmt.loadSubscription(session.getSubscriptionTKey()
                        .longValue());
            } catch (ObjectNotFoundException e) {
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_NO_SUBSCRIPTION_FOUND_FOR_PRODUCT_SESSION,
                        session.getSubscriptionTKey().toString());
            }
            if (sub != null
                    && sub.getProduct().getTechnicalProduct().getAccessType() != ServiceAccessType.LOGIN) {

                createLogoutEvent(session);
                dm.remove(session);
            }
        }

    }

    @Override
    public List<Long> getSubscriptionKeysForSessionId(String sessionId) {

        ArgumentValidator.notNull("sessionId", sessionId);

        List<Session> sessions = retrieveProductSessionDataForSessionId(sessionId);

        // now convert and return
        List<Long> result = new ArrayList<>();
        for (Session session : sessions) {
            result.add(session.getSubscriptionTKey());
        }

        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Session getPlatformSessionForSessionId(String sessionId) {
        logger.logDebug("getPlatformSessionForSessionId(String) sessionId");
        // find all entries for the given session id
        Query query = dm.createNamedQuery("Session.findEntriesForSessionId");
        query.setParameter("sessionId", sessionId);
        query.setParameter("sessionType", SessionType.PLATFORM_SESSION);
        return (Session) query.getSingleResult();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Session> getProductSessionsForSubscriptionTKey(
            long subscriptionTKey) {

        // find all entries for the given session id
        Query query = dm.createNamedQuery("Session.findEntriesForSubscription");
        query.setParameter("subscriptionTKey", Long.valueOf(subscriptionTKey));
        query.setParameter("sessionType", SessionType.SERVICE_SESSION);
        return ParameterizedTypes.list(query.getResultList(),
                Session.class);
    }

    @Override
    public void createPlatformSession(String sessionId)
            throws ValidationException {

        ArgumentValidator.notNull("sessionId", sessionId);
        BLValidator.isDescription("sessionId", sessionId, true);

        PlatformUser user = dm.getCurrentUser();

        Session sessionData = new Session();
        sessionData.setSessionId(sessionId);
        sessionData.setPlatformUserId(user.getUserId());
        sessionData.setPlatformUserKey(user.getKey());
        sessionData.setSubscriptionTKey(Long.valueOf(0));
        sessionData.setSessionId(sessionId);
        sessionData.setSessionType(SessionType.PLATFORM_SESSION);
        sessionData.setNodeName(cfgSvc.getNodeName());

        Session currentSessionData = (Session) dm.find(sessionData);

        if (currentSessionData == null) {
            try {
                dm.persist(sessionData);
            } catch (NonUniqueBusinessKeyException e) {
                // should not occur, if it does, log the problem and throw a
                // system exception
                SaaSSystemException sse = new SaaSSystemException(
                        "Session entry already exists with the same business key",
                        e);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        sse,
                        LogMessageIdentifier.ERROR_CREATE_PRODUCT_SESSION_FAILED);
                throw sse;
            }
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Session> getSessionsForUserKey(long platformUserKey) {

        Query query = dm.createNamedQuery("Session.getActiveSessionsForUser");
        query.setParameter("userKey", Long.valueOf(platformUserKey));
        return ParameterizedTypes.list(query.getResultList(),
                Session.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean hasTechnicalProductActiveSessions(long technicalProductKey) {

        Query query = dm
                .createNamedQuery("Session.getNumOfActiveSessionsForTechProduct");
        query.setParameter("technicalProductKey",
                Long.valueOf(technicalProductKey));
        Long num = (Long) query.getSingleResult();

        return num.longValue() != 0;
    }

    @Override
    public int deletePlatformSession(String sessionId) {
        ArgumentValidator.notNull("sessionId", sessionId);
        Query query = dm
                .createNamedQuery("Session.deletePlatformSessionsForSessionId");
        query.setParameter("sessionId", sessionId);
        query.setParameter("sessionType", SessionType.PLATFORM_SESSION);
        return query.executeUpdate();
    }

    /**
     * Queries the data in the database for the existing product session data
     * and returns them.
     * 
     * @param sessionId
     *            The sessionId to look for.
     * @return The corresponding session data.
     */
    private List<Session> retrieveProductSessionDataForSessionId(
            String sessionId) {
        // first find all entries for the given session id
        Query query = dm.createNamedQuery("Session.findEntriesForSessionId");
        query.setParameter("sessionId", sessionId);
        query.setParameter("sessionType", SessionType.SERVICE_SESSION);
        return ParameterizedTypes.list(query.getResultList(), Session.class);
    }

    /**
     * Creates a logout event for the given session.
     * 
     * A logout event is only generated in case the user token is not set
     * anymore, what means that the user has really logged in earlier
     * 
     * @param session
     *            The session the logout event will be generated for.
     */
    private void createLogoutEvent(Session session) {
        if (session.getUserToken() != null
                && session.getUserToken().length() == 0) {
            GatheredEvent event = new GatheredEvent();
            event.setActor(session.getPlatformUserId());
            event.setType(EventType.PLATFORM_EVENT);
            event.setEventId(PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE);
            event.setOccurrenceTime(System.currentTimeMillis());
            event.setSubscriptionTKey(session.getSubscriptionTKey().longValue());
            try {
                evtMgmt.recordEvent(event);
            } catch (DuplicateEventException e) {
                // such situation has not to be happen cause we don not set
                // unique id here, it is null
                logger.logDebug(e.getMessage());
            }
        }
    }

    /**
     * Checks if the session count for the subscription is lower or equal to the
     * value of the product parameter CONCURRENT_USER. If this condition does
     * not hold, an ProductParameterException will be thrown.
     * 
     * @param subscription
     *            The subscription to be checked.
     * @throws ServiceParameterException
     *             Thrown in case the session count of the subscription is
     *             greater than the value of the product parameter
     *             CONCURRENT_USER
     */
    private void verifyParameterConcurrentUser(Subscription subscription)
            throws ServiceParameterException {

        if (subscription == null) {
            return;
        }
        ParameterSet parameterSet = subscription.getParameterSet();
        if (parameterSet == null || parameterSet.getParameters() == null) {
            return;
        }
        for (Parameter parameter : parameterSet.getParameters()) {
            if (parameter.getParameterDefinition().getParameterType() == ParameterType.PLATFORM_PARAMETER
                    && PlatformParameterIdentifiers.CONCURRENT_USER
                            .equals(parameter.getParameterDefinition()
                                    .getParameterId())
                    && parameter.getValue() != null) {
                List<Session> list = getProductSessionsForSubscriptionTKey(subscription
                        .getKey());
                if (list != null && list.size() >= parameter.getLongValue()) {
                    sessionCtx.setRollbackOnly();
                    String text = "Subscription '"
                            + subscription.getSubscriptionId() + "'/Product '"
                            + subscription.getProduct().getProductId() + "'";
                    ServiceParameterException e = new ServiceParameterException(
                            text, ParameterType.PLATFORM_PARAMETER,
                            PlatformParameterIdentifiers.CONCURRENT_USER,
                            new Object[] { parameter.getValue() });
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.WARN_TOO_MANY_CONCURRENT_USER_FOR_SUBSCRIPTION,
                            subscription.getSubscriptionId(), subscription
                                    .getProduct().getProductId());
                    throw e;
                }
            }
        }

    }

    @Override
    @RolesAllowed({"ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER", "UNIT_ADMINISTRATOR" })
    public void deleteServiceSessionsForSubscription(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Subscription sub = subMgmt.loadSubscription(subscriptionKey);
        List<UserGroup> administratedUserGroups = userGroupService
                .getUserGroupsForUserWithRole(dm.getCurrentUser().getKey(),
                        UnitRoleType.ADMINISTRATOR.getKey());
        PermissionCheck.owns(sub, dm.getCurrentUser(), administratedUserGroups, logger);
        PermissionCheck
                .owns(sub, dm.getCurrentUser().getOrganization(), logger);
        List<Session> openSessions = getProductSessionsForSubscriptionTKey(subscriptionKey);
        for (Session session : openSessions) {
            dm.remove(session);
        }
    }

    @Override
    @RolesAllowed({"ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR"})
    public int getNumberOfServiceSessions(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Subscription sub = subMgmt.loadSubscription(subscriptionKey);
        List<UserGroup> administratedUserGroups = userGroupService
                .getUserGroupsForUserWithRole(dm.getCurrentUser().getKey(),
                        UnitRoleType.ADMINISTRATOR.getKey());
        PermissionCheck.owns(sub, dm.getCurrentUser(), administratedUserGroups, logger);
        PermissionCheck
                .owns(sub, dm.getCurrentUser().getOrganization(), logger);
        return getProductSessionsForSubscriptionTKey(subscriptionKey).size();
    }

}
