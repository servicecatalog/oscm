/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                           
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.bean;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.*;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.bridge.BridgeDataManager;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.DomainObjectException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.InvalidUserSession;

/**
 * Implementation of DataManager as Stateless Session Bean
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class DataServiceBean implements DataService {

    private static final String EVALUATING_SESSION_CONTEXT_FAILED = "Evaluating session context failed";

    /**
     * Cache for the logged in user.
     */
    private static ThreadLocal<String> CURRENT_USER = new ThreadLocal<String>();

    /**
     * Cache for the user to make is accessible to the history listener. This
     * user might be different to the logged in user, in case a user acts on
     * behalf of another organization
     */
    private static ThreadLocal<String> CURRENT_HISTORY_USER = new ThreadLocal<String>();

    /**
     * Cache for asynchronous user, e.g. trigger.
     */
    private static final ThreadLocal<Long> CURRENT_ASYNC_USER = new ThreadLocal<Long>();

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(DataServiceBean.class);

    @Resource(name = "BSSDS")
    DataSource ds;

    public static String getCurrentHistoryUser() {
        return CURRENT_HISTORY_USER.get();
    }

    /**
     * Internally register some objects to have them available via ThreadLocal.
     */
    private void setThreadLocals() {
        initCurrentUser();
        BridgeDataManager.registerEntityManager(em);
    }

    void initCurrentUser() {
        String currentUser = findCurrentUser();
        String lastCurrentUser = CURRENT_USER.get();
        if (!currentUser.equals(lastCurrentUser)) {
            CURRENT_USER.set(currentUser);
            CURRENT_HISTORY_USER.set(findCurrentHistoryUser());
        }
    }

    /**
     * EntityManager to be used for all persistence operations
     */
    @PersistenceContext(name = "persistence/em", unitName = "oscm-domainobjects")
    protected EntityManager em;

    @Resource
    private WebServiceContext webServiceContext;

    /**
     * Session Context to read user key (caller principal)s
     */
    @Resource
    protected SessionContext sessionCtx;

    /**
     * Default constructor.
     */
    public DataServiceBean() {
    }

    /**
     * Helper method to create and throw an ObjectNotFound exception.
     * 
     * @param objclass
     *            the object class of the not found object
     * @param businessKey
     *            the not found business key
     */
    protected DomainObjectException.ClassEnum class2Enum(Class<?> objclass) {

        DomainObjectException.ClassEnum classEnum = null;
        if (objclass == Organization.class) {
            classEnum = DomainObjectException.ClassEnum.ORGANIZATION;
        } else if (objclass == PlatformUser.class) {
            classEnum = DomainObjectException.ClassEnum.USER;
        } else if (objclass == Subscription.class) {
            classEnum = DomainObjectException.ClassEnum.SUBSCRIPTION;
        } else if (objclass == Product.class) {
            classEnum = DomainObjectException.ClassEnum.SERVICE;
        } else if (objclass == TechnicalProduct.class) {
            classEnum = DomainObjectException.ClassEnum.TECHNICAL_SERVICE;
        } else if (objclass == ParameterDefinition.class) {
            classEnum = DomainObjectException.ClassEnum.PARAMETER_DEFINITION;
        } else if (objclass == ParameterOption.class) {
            classEnum = DomainObjectException.ClassEnum.PARAMETER_OPTION;
        } else if (objclass == Parameter.class) {
            classEnum = DomainObjectException.ClassEnum.PARAMETER;
        } else if (objclass == PricedParameter.class) {
            classEnum = DomainObjectException.ClassEnum.PRICED_PARAMETER;
        } else if (objclass == OrganizationRole.class) {
            classEnum = DomainObjectException.ClassEnum.ORGANIZATION_ROLE;
        } else if (objclass == Report.class) {
            classEnum = DomainObjectException.ClassEnum.REPORT;
        } else if (objclass == Event.class) {
            classEnum = DomainObjectException.ClassEnum.EVENT;
        } else if (objclass == TechnicalProductOperation.class) {
            classEnum = DomainObjectException.ClassEnum.TECHNICAL_SERVICE_OPERATION;
        } else if (objclass == OperationParameter.class) {
            classEnum = DomainObjectException.ClassEnum.OPERATION_PARAMETER;
        } else if (objclass == RoleDefinition.class) {
            classEnum = DomainObjectException.ClassEnum.ROLE_DEFINITION;
        } else if (objclass == PaymentType.class) {
            classEnum = DomainObjectException.ClassEnum.PAYMENT_TYPE;
        } else if (objclass == SupportedCurrency.class) {
            classEnum = DomainObjectException.ClassEnum.SUPPORTED_CURRENCY;
        } else if (objclass == UdaDefinition.class) {
            classEnum = DomainObjectException.ClassEnum.UDA_DEFINITION;
        } else if (objclass == Uda.class) {
            classEnum = DomainObjectException.ClassEnum.UDA;
        } else if (objclass == SupportedCountry.class) {
            classEnum = DomainObjectException.ClassEnum.SUPPORTED_COUNTRY;
        } else if (objclass == OrganizationToCountry.class) {
            classEnum = DomainObjectException.ClassEnum.ORGANIZATION_TO_COUNTRY;
        } else if (objclass == OrganizationReference.class) {
            classEnum = DomainObjectException.ClassEnum.ORGANIZATION_REFERENCE;
        } else if (objclass == Marketplace.class) {
            classEnum = DomainObjectException.ClassEnum.MARKETPLACE;
        } else if (objclass == Tag.class) {
            classEnum = DomainObjectException.ClassEnum.TAG;
        } else if (objclass == TechnicalProductTag.class) {
            classEnum = DomainObjectException.ClassEnum.TECHNICAL_SERVICE_TAG;
        } else if (objclass == UserRole.class) {
            classEnum = DomainObjectException.ClassEnum.USER_ROLE;
        } else if (objclass == ProductReview.class) {
            classEnum = DomainObjectException.ClassEnum.PRODUCT_REVIEW;
        } else if (objclass == BillingContact.class) {
            classEnum = DomainObjectException.ClassEnum.BILLING_CONTACT;
        } else if (objclass == PaymentInfo.class) {
            classEnum = DomainObjectException.ClassEnum.PAYMENT_INFO;
        } else if (objclass == MarketplaceToOrganization.class) {
            classEnum = DomainObjectException.ClassEnum.MARKETPLACE_TO_ORGANIZATION;
        } else if (objclass == ProductToPaymentType.class) {
            classEnum = DomainObjectException.ClassEnum.PRODUCT_TO_PAYMENTTYPE;
        } else if (objclass == PSP.class) {
            classEnum = DomainObjectException.ClassEnum.PSP;
        } else if (objclass == Category.class) {
            classEnum = DomainObjectException.ClassEnum.CATEGORY;
        } else if (objclass == MarketingPermission.class) {
            classEnum = DomainObjectException.ClassEnum.MARKETING_PERMISSION;
        } else if (objclass == TriggerDefinition.class) {
            classEnum = DomainObjectException.ClassEnum.TRIGGER_DEFINITION;
        } else if (objclass == ReportResultCache.class) {
            classEnum = DomainObjectException.ClassEnum.REPORT_RESULT_CACHE;
        } else if (objclass == OrganizationSetting.class) {
            classEnum = DomainObjectException.ClassEnum.ORGANIZATION_SETTING;
        } else if (objclass == PlatformSetting.class) {
            classEnum = DomainObjectException.ClassEnum.PLATFORM_SETTING;
        } else if (objclass == LocalizedResource.class) {
            classEnum = DomainObjectException.ClassEnum.LOCALIZED_RESOURCE;
        } else if (objclass == SupportedLanguage.class) {
            classEnum = DomainObjectException.ClassEnum.SUPPORTED_LANGUAGE;
        } else if (objclass == UserGroup.class) {
            classEnum = DomainObjectException.ClassEnum.USER_GROUP;
        } else if (objclass == UserGroupToUser.class) {
            classEnum = DomainObjectException.ClassEnum.USER_GROUP_TO_USER;
        } else if (objclass == UserGroupToInvisibleProduct.class) {
            classEnum = DomainObjectException.ClassEnum.USER_GROUP_TO_INVISIBLE_PRODUCT;
        } else if (objclass == OperationRecord.class) {
            classEnum = DomainObjectException.ClassEnum.OPERATION_RECORD;
        } else if (objclass == BillingAdapter.class) {
            classEnum = DomainObjectException.ClassEnum.BILLING_ADAPTER;
        } else if (objclass == UnitRoleAssignment.class) {
            classEnum = DomainObjectException.ClassEnum.UNIT_ROLE_ASSIGNMENT;
        } else if (objclass == MarketplaceAccess.class) {
            classEnum = DomainObjectException.ClassEnum.MARKETPLACE_ACCESS;
        } else if (objclass == Tenant.class) {
            classEnum = DomainObjectException.ClassEnum.TENANT;
        } else if (objclass == TenantSetting.class) {
            classEnum = DomainObjectException.ClassEnum.TENANT_SETTING;
        }
        
        return classEnum;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public DomainObject<?> find(DomainObject<?> idobj) {
        setThreadLocals();
        String[] businessKeyElements = PersistenceReflection
                .getBusinessKey(DomainObject.getDomainClass(idobj));
        String className = DomainObject.getDomainClass(idobj).getName();
        String queryName = className.substring(className.lastIndexOf(".") + 1)
                + ".findByBusinessKey";
        if (businessKeyElements == null) {
            throw new SaaSSystemException("No BusinessKey defined for "
                    + className);
        }
        // Parameter names correspond to field names
        Query qry = em.createNamedQuery(queryName);
        if (qry == null) {
            throw new SaaSSystemException("Could not create query " + queryName);
        }
        for (String par : businessKeyElements) {
            Object val = PersistenceReflection.getValue(idobj, par);
            qry = qry.setParameter(par, val);
        }
        try {
            DomainObject<?> domobj = (DomainObject<?>) qry.getSingleResult();
            return domobj;
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            StringBuffer qrykey = new StringBuffer("(");
            for (String par : businessKeyElements) {
                qrykey.append(par + "='");
                qrykey.append(PersistenceReflection.getValue(idobj, par)
                        .toString());
                qrykey.append("',");
            }
            qrykey.append(")");
            String msgText = "Non-Unique Business Key Search for "
                    + PersistenceReflection.getDomainClassName(idobj)
                    + " and BusinessKey " + qrykey.toString();
            throw new SaaSSystemException(msgText, e);
        } catch (Exception e) {
            throw new SaaSSystemException(e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser find(PlatformUser pu) {
        Query qry;
        if (isNotDefaultTenant(pu.getTenantId())) {
            qry = em.createNamedQuery("PlatformUser.findByBusinessKey",
                    PlatformUser.class);
            qry.setParameter("tenantId", pu.getTenantId());
        } else {
            qry = em.createNamedQuery("PlatformUser.findByUserId",
                    PlatformUser.class);
        }
        qry.setParameter("userId", pu.getUserId());
        try {
            PlatformUser singleResult = (PlatformUser) qry.getSingleResult();
            assignTenantId(singleResult, singleResult.getOrganization());
            return singleResult;
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            String qrykey = "(" + "tenantId='" + pu.getTenantId() + "',"
                    + "userId='" + pu.getUserId() + ")";
            String msgText = "Non-Unique Business Key Search for PlatformUser and BusinessKey "
                    + qrykey;
            throw new SaaSSystemException(msgText, e);
        } catch (Exception e) {
            throw new SaaSSystemException(e);
        }
    }

    protected void assignTenantId(PlatformUser singleResult,
            Organization organization) {
        Tenant tenant = organization.getTenant();
        if (tenant != null) {
            singleResult.setTenantId(tenant.getTenantId());
        }
    }

    private boolean isNotDefaultTenant(String tenantId) {
        String defaultTenant = getDefaultTenant();
        return StringUtils.isNotBlank(tenantId)
                && !StringUtils.equals(tenantId, defaultTenant);
    }

    private String getDefaultTenant() {
        TypedQuery<ConfigurationSetting> query = em.createNamedQuery(
                "ConfigurationSetting.findByInfoAndContext",
                ConfigurationSetting.class);
        query.setParameter("informationId",
                ConfigurationKey.SSO_DEFAULT_TENANT_ID);
        query.setParameter("contextId", Configuration.GLOBAL_CONTEXT);
        try {
            return query.getSingleResult().getValue();
        } catch (Exception exc) {
            return null;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public DomainObject<?> getReferenceByBusinessKey(
            DomainObject<?> findTemplate) throws ObjectNotFoundException {
        setThreadLocals();
        DomainObject<?> result;
        if (findTemplate instanceof PlatformUser) {
            result = find((PlatformUser) findTemplate);
        } else {
            result = find(findTemplate);
        }
        if (result == null) {
            DomainObjectException.ClassEnum classEnum = class2Enum(findTemplate
                    .getClass());
            Map<String, String> businessKeyMap = PersistenceReflection
                    .getBusinessKeys(findTemplate);
            throw new ObjectNotFoundException(classEnum, getBusinessKey(
                    businessKeyMap, classEnum));
        }
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void persist(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException {
        setThreadLocals();
        validateBusinessKeyUniqueness(obj);
        em.persist(obj);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void persist(DomainHistoryObject<?> obj)
            throws NonUniqueBusinessKeyException {
        setThreadLocals();
        em.persist(obj);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public <T extends DomainObject<?>> T find(Class<T> clazz, Object key) {
        setThreadLocals();
        return em.find(clazz, key);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public <T extends DomainObject<?>> T find(Class<T> objclazz, long id) {
        setThreadLocals();
        return em.find(objclazz, new Long(id));
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void remove(DomainObject<?> obj) {
        setThreadLocals();
        if (em.contains(obj)) {
            em.remove(obj);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<DomainHistoryObject<?>> findHistory(DomainObject<?> obj) {
        if (obj == null) {
            return null;
        }
        String className = DomainObject.getDomainClass(obj).getName();
        String histClassName = className
                .substring(className.lastIndexOf(".") + 1) + "History";
        String qryString = histClassName + ".findByObject";
        Query query = em.createNamedQuery(qryString);
        query.setParameter("objKey", Long.valueOf(obj.getKey()));
        List<?> qryresult = query.getResultList();
        List<DomainHistoryObject<?>> result = new ArrayList<DomainHistoryObject<?>>();
        for (Object o : qryresult) {
            if (!(o instanceof DomainHistoryObject<?>)) {
                throw new SaaSSystemException(
                        "findHistory loaded Non-History Object !");
            }
            result.add((DomainHistoryObject<?>) o);
        }
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public DomainHistoryObject<?> findLastHistory(DomainObject<?> obj) {
        if (obj == null) {
            return null;
        }
        String className = DomainObject.getDomainClass(obj).getName();
        String histClassName = className
                .substring(className.lastIndexOf(".") + 1) + "History";
        String qryString = histClassName + ".findByObjectDesc";
        Query query = em.createNamedQuery(qryString);
        query.setParameter("objKey", Long.valueOf(obj.getKey()));
        query.setMaxResults(1);
        List<?> qryresult = query.getResultList();
        if (qryresult.isEmpty()) {
            return null;
        }
        return (DomainHistoryObject<?>) qryresult.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Session getSession() {
        return em.unwrap(Session.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean contains(Object arg0) {
        return em.contains(arg0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Query createNamedQuery(String jpql) {
        return em.createNamedQuery(jpql);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public <T> TypedQuery<T> createNamedQuery(String jpql, Class<T> resultClass) {
        return em.createNamedQuery(jpql, resultClass);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Query createQuery(String arg0) {
        return em.createQuery(arg0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Query createNativeQuery(String arg0) {
        return em.createNativeQuery(arg0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Query createNativeQuery(String arg0, Class<?> objclass) {
        return em.createNativeQuery(arg0, objclass);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void flush() {
        em.flush();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void clear() {
        em.clear();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Object merge(Object arg0) {
        return em.merge(arg0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void refresh(Object arg0) {
        em.refresh(arg0);
    }

    public void remove(Object arg0) {
        if (em.contains(arg0)) {
            em.remove(arg0);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public <T extends DomainObject<?>> T getReference(Class<T> objclass, long id)
            throws ObjectNotFoundException {
        setThreadLocals();
        T result = find(objclass, id);
        if (result == null) {
            throw new ObjectNotFoundException(class2Enum(objclass),
                    String.valueOf(id));
        } else if (result instanceof PlatformUser) {
            PlatformUser user = (PlatformUser) result;
            assignTenantId(user, user.getOrganization());
        }
        return result;
    }

    /**
     * returns the current user key given by the getCallerPrincipal method
     * 
     * @return User key
     */
    private String findCurrentUser() {
        if (sessionCtx == null) {
            return "";
        }
        if (sessionCtx.getCallerPrincipal() == null) {
            return "";
        }
        return sessionCtx.getCallerPrincipal().getName();
    }

    /**
     * Returns the key user to be used in the history objects. In case a user
     * acts on behalf of the current in user, then the key of the master user
     * will be returned.
     */
    private String findCurrentHistoryUser() {
        if (sessionCtx == null) {
            return "";
        }
        if (sessionCtx.getCallerPrincipal() == null) {
            return "";
        }
        final PlatformUser user = getCurrentUserIfPresent();
        if (user == null) {
            return sessionCtx.getCallerPrincipal().getName();
        }
        OnBehalfUserReference onBehalf = user.getMaster();
        long key;
        if (onBehalf == null) {
            key = user.getKey();
        } else {
            key = onBehalf.getMasterUser().getKey();
        }
        return String.valueOf(key);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void validateBusinessKeyUniqueness(DomainObject<?> obj)
            throws NonUniqueBusinessKeyException {
        Map<String, String> businessKeyMap = PersistenceReflection
                .getBusinessKeys(obj);
        if (businessKeyMap != null) {
            DomainObject<?> search;
            if (obj instanceof PlatformUser) {
                search = find((PlatformUser) obj);
            } else {
                search = find(obj);
            }
            if (search != null && obj.getKey() != search.getKey()) {
                DomainObjectException.ClassEnum classEnum = class2Enum(obj
                        .getClass());
                String businessKey = getBusinessKey(businessKeyMap, classEnum);
                NonUniqueBusinessKeyException e = new NonUniqueBusinessKeyException(
                        classEnum, businessKey);
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        e,
                        LogMessageIdentifier.WARN_NON_UNIQUE_BUSINESS_KEY_WITH_TYPE,
                        classEnum.name(), businessKey);
                throw e;
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser getCurrentUser() {

        PlatformUser user = getUserFromSessionContext(false);

        return user;
    }

    @Override
    public void setCurrentUserKey(Long key) {
        CURRENT_ASYNC_USER.set(key);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PlatformUser getCurrentUserIfPresent() {

        PlatformUser user = getUserFromSessionContext(true);

        return user;
    }

    private PlatformUser getUserFromSessionContext(boolean lookupOnly) {

        try {
            return loadUserFromSessionContext(lookupOnly);
        } catch (ObjectNotFoundException e) {
            if (lookupOnly) {
                // no exception when just looking for user object
                return null;
            }
            String name = "-";
            try {
                if (sessionCtx != null
                        && sessionCtx.getCallerPrincipal() != null)
                    name = sessionCtx.getCallerPrincipal().getName();
            } catch (IllegalStateException ex) {
                // name is set before
            }
            InvalidUserSession ius = new InvalidUserSession(
                    "User with key '"
                            + name
                            + "' cannot be found! The user might have been deleted by administrator.",
                    e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                    e,
                    LogMessageIdentifier.ERROR_USER_OPERATE_NOT_PERMITTED_THE_USER_ALREADY_DELETED);
            throw ius;
        } catch (Exception e) {
            if (mayBeWebServiceSSLContext()) {
                return loadUserFromSSLContext(lookupOnly);
            }
            if (lookupOnly) {
                // no exception when just looking for user object
                return null;
            }
            InvalidUserSession ius = new InvalidUserSession(
                    EVALUATING_SESSION_CONTEXT_FAILED, e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.ACCESS_LOG,
                    ius, LogMessageIdentifier.WARN_ACCESS_DINIED_NO_USER);
            throw ius;
        }
    }

    /**
     * @param lookupOnly
     * @return
     * @throws ObjectNotFoundException
     */
    private PlatformUser loadUserFromSessionContext(boolean lookupOnly)
            throws ObjectNotFoundException {
        String name = CURRENT_ASYNC_USER.get() == null ? null
                : CURRENT_ASYNC_USER.get().toString();
        if (name == null) {
            // determine the caller
            Principal callerPrincipal = sessionCtx.getCallerPrincipal();
            if (callerPrincipal == null) {
                return null;
            }
            name = callerPrincipal.getName();
        }
        // try to parse the name to long - must work if it is a user key
        long parseLong = Long.parseLong(name);
        // determine the user object of the caller
        PlatformUser user = getReference(PlatformUser.class, parseLong);
        Organization org = user.getOrganization();
        if (checkOrgDeregistration(org, lookupOnly)) {
            // org still valid => return user
            Tenant tenant = org.getTenant();
            if (tenant != null) {
                assignTenantId(user, org);
            }
            return user;
        }
        // lookup case => org not valid => no user
        return null;
    }

    /**
     * @param lookupOnly
     * @return
     */
    private PlatformUser loadUserFromSSLContext(boolean lookupOnly) {
        Organization org = getOrganizationForDistinguishedName(lookupOnly);
        if (lookupOnly && org == null) {
            return null;
        }
        if (!checkOrgDeregistration(org, lookupOnly)) {
            // lookup case => org not valid => no user
            return null;
        }
        List<PlatformUser> platformUsers = org.getVisiblePlatformUsers();
        for (PlatformUser platformUser : platformUsers) {
            if (platformUser.isOrganizationAdmin()) {
                return platformUser;
            }
        }
        if (lookupOnly) {
            // no exception when just looking for user object
            return null;
        }
        InvalidUserSession ius = new InvalidUserSession(
                EVALUATING_SESSION_CONTEXT_FAILED);
        logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.ACCESS_LOG, ius,
                LogMessageIdentifier.WARN_ACCESS_DINIED_NO_USER);
        throw ius;
    }

    private boolean mayBeWebServiceSSLContext() {
        if (webServiceContext == null) {
            return false;
        }

        try {
            final Principal principal = webServiceContext.getUserPrincipal();
            return principal != null && principal.getName() != null
                    && principal.getName().indexOf('=') > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Organization getOrganizationForDistinguishedName(boolean lookupOnly) {
        final Set<String> principalNames = Collections
                .singleton(webServiceContext.getUserPrincipal().getName());
        final TypedQuery<Organization> query = createNamedQuery(
                "Organization.getOrgsForDN", Organization.class);
        query.setParameter("dn", principalNames);
        try {
            return query.getSingleResult();
        } catch (Exception e) {
            if (lookupOnly) {
                // no exception when just looking for user object
                return null;
            }
            final InvalidUserSession ius = new InvalidUserSession(
                    EVALUATING_SESSION_CONTEXT_FAILED, e);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ius,
                    LogMessageIdentifier.WARN_NO_OR_MULTIPLE_ENTRIES_FOUND_FOR_WEB_SERVICE);
            throw ius;
        }
    }

    /**
     * Checks if the organization has meanwhile been deregistered.
     * 
     * @param org
     *            the organization is question
     * @param lookupOnly
     *            defines whether a deregistered organization causes an
     *            exception or just returns <code>false</code>
     * 
     * @return <code>true</code> if the organization is still registered,
     *         <code>false</code> or exception otherwise
     */
    private boolean checkOrgDeregistration(Organization org, boolean lookupOnly) {
        if (org.getDeregistrationDate() != null) {
            if (lookupOnly) {
                return false;
            }
            InvalidUserSession ius = new InvalidUserSession(
                    "Organization with key '" + org.getKey()
                            + "' is deregistered!");
            logger.logError(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                    ius, LogMessageIdentifier.ERROR_USER_OPERATE_NOT_PERMITTED,
                    org.getOrganizationId());
            throw ius;
        }
        return true;
    }

    /**
     * Returns the value for the given classEnum from the given map.
     * 
     * @return Result string or "" if no business key attribute is defined in
     *         classEnum
     */
    private String getBusinessKey(Map<String, String> businessKeyMap,
            DomainObjectException.ClassEnum classEnum) {
        String value = businessKeyMap.get(classEnum.getIdFieldName());
        if (value == null) {
            value = businessKeyMap.toString();
        }
        return value;
    }

    @Override
    public DataSet executeQueryForRawData(SqlQuery sqlQuery) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.prepareStatement(sqlQuery.getQuery());
            for (Integer parameterIndex : sqlQuery.getParameters().keySet()) {
                stmt.setObject(parameterIndex.intValue(), sqlQuery
                        .getParameters().get(parameterIndex));
            }
            rs = stmt.executeQuery();
            return convertToDataSet(rs, sqlQuery.getMax());
        } catch (Exception e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_SQL);
            throw sse;
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    DataSet convertToDataSet(ResultSet rs, int max) throws SQLException {
        DataSet dataSet = new DataSet();
        for (int index = 1; index <= rs.getMetaData().getColumnCount(); index++) {
            dataSet.getMetaData().add(index,
                    rs.getMetaData().getColumnName(index),
                    rs.getMetaData().getColumnTypeName(index),
                    rs.getMetaData().getColumnType(index));
        }
        int i = 0;
        while (i++ < max && rs.next()) {
            List<Object> values = new ArrayList<Object>();
            for (int index = 1; index <= rs.getMetaData().getColumnCount(); index++) {
                values.add(rs.getObject(index));
            }
            dataSet.addRow(values);
        }
        return dataSet;
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {
                logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    private void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {
                logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) {
                logger.logError(Log4jLogger.SYSTEM_LOG, ignore,
                        LogMessageIdentifier.ERROR_CLOSE_RESOURCE_FAILED);
            }
        }
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void persistPlatformUserWithTenant(PlatformUser pu, String tenantId)
            throws NonUniqueBusinessKeyException {
        setThreadLocals();
        // Query namedQuery =
        // createNamedQuery("PlatformUser.findByUserIdAndTenant");
        // namedQuery.setParameter("userId", pu.getUserId());
        // namedQuery.setParameter("tenantId", tenantId);
        // List users = namedQuery.getResultList();
        PlatformUser user = find(pu);
        if (user != null) {
            DomainObjectException.ClassEnum classEnum = class2Enum(pu
                    .getClass());
            throw new NonUniqueBusinessKeyException(classEnum, pu.getUserId()
                    + " " + tenantId);
        }
        em.persist(pu);
    }

}
