/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.applicationservice.bean;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;

import org.oscm.applicationservice.adapter.OperationServiceAdapterFactory;
import org.oscm.applicationservice.adapter.ProvisioningServiceAdapter;
import org.oscm.applicationservice.adapter.ProvisioningServiceAdapterFactory;
import org.oscm.applicationservice.filter.AttributeFilter;
import org.oscm.applicationservice.filter.ParameterFilter;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ModifiedUda;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UnsupportedOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.kafka.records.Operation;
import org.oscm.kafka.result.PublishingResult;
import org.oscm.kafka.service.Producer;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.validator.BLValidator;

/**
 * Session Bean implementation class ApplicationManagement
 */
@Stateless
@Local(ApplicationServiceLocal.class)
public class ApplicationServiceBean implements ApplicationServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ApplicationServiceBean.class);

    private static final int RETURN_CODE_OK = 0;
    private static final int RETURN_CODE_FAILURE = 1;

    private static final String ERROR_WS_CALL = "Failure while calling webservice.";

    protected static final String SERVICE_PATH = "/opt/";

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal cs;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = DataService.class)
    DataService ds;

    @Inject
    Producer kafkaProducer;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BaseResult asyncCreateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        try {
            BaseResult result = sendAsyncCreateInstance(subscription);
            verifyResult(subscription, result);
            return result;
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public InstanceResult createInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        try {
            InstanceResult result = getPort(subscription).createInstance(
                    toInstanceRequest(subscription), getCurrentUser());
            verifyResult(subscription, result);
            InstanceInfo info = result.getInstance();
            if (info == null) {
                TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                        ERROR_WS_CALL,
                        new Object[] { subscription.getSubscriptionId(),
                                "The webservice call returned no instance" });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                        LogMessageIdentifier.WARN_TECH_SERVICE_WS_NO_INSTANCE,
                        subscription.getSubscriptionId());
                throw ex;
            }
            validateInstanceInfo(info, subscription);

            return result;
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        try {
            BaseResult result = sendDeleteInstance(subscription);
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void modifySubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        List<ServiceParameter> serviceParameterList = ParameterFilter
                .getServiceParameterList(subscription, true);
        List<ServiceAttribute> serviceAttributeList = AttributeFilter
                .getSubscriptionAttributeList(subscription,
                        getModifiedUdas(subscription));
        try {
            BaseResult result = getPort(subscription).modifySubscription(
                    subscription.getProductInstanceId(),
                    subscription.getSubscriptionId(),
                    subscription.getPurchaseOrderNumber(), serviceParameterList,
                    serviceAttributeList, getCurrentUser());
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException
                | TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public User[] createUsersForSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        User[] users = createUsers(subscription,
                subscription.getUsageLicenses());

        return users;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public User[] createUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        if (!isProductUserManagementActive(subscription)) {
            return new User[0];
        }
        if (usageLicenses == null || usageLicenses.isEmpty()) {
            return new User[0];
        }

        UserResult result = null;
        try {
            result = getPort(subscription).createUsers(
                    subscription.getProductInstanceId(),
                    toUserList(usageLicenses), getCurrentUser());
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }
        final User[] users;
        List<User> userList = result.getUsers();
        if (userList == null) {
            users = new User[0];
        } else {
            validateUsers(userList, subscription);
            users = userList.toArray(new User[0]);
        }

        return users;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        if (!isProductUserManagementActive(subscription)) {
            return;
        }
        if (usageLicenses == null || usageLicenses.isEmpty()) {
            return;
        }
        try {
            BaseResult result = getPort(subscription).deleteUsers(
                    subscription.getProductInstanceId(),
                    toUserList(usageLicenses), getCurrentUser());
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        if (!isProductUserManagementActive(subscription)) {
            return;
        }
        if (usageLicenses == null || usageLicenses.isEmpty()) {
            return;
        }
        try {
            BaseResult result = getPort(subscription).updateUsers(
                    subscription.getProductInstanceId(),
                    toUserList(usageLicenses), getCurrentUser());
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void validateCommunication(TechnicalProduct techProduct)
            throws TechnicalServiceNotAliveException {

        if (techProduct.getAccessType() == ServiceAccessType.EXTERNAL) {
            return;
        }

        if (isEventProvisioning(techProduct)) {
            // TODO validate kafka communication???
            return;
        }

        try {
            getPort(techProduct).sendPing("ping");
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    /**
     * Check if the product instance is able to handle BES side user management.
     * 
     * @param subscription
     *            the subscription of the product to check
     * @return <code>true</code> in case the product can handle user management
     *         requests from the BES, other wise <code>false
     */
    private boolean isProductUserManagementActive(Subscription subscription) {
        return subscription.getProduct().getTechnicalProduct()
                .getAccessType() != ServiceAccessType.DIRECT;
    }

    /**
     * Create the ProvisioningServiceStub object for the technical product of
     * the given subscription
     * 
     * @param subscription
     *            the subscription with the technical product for which the stub
     *            is created
     * @return the created ProvisioningServiceStub
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the ProvisioningServiceStub creation failed.
     * 
     */
    private ProvisioningServiceAdapter getPort(Subscription subscription)
            throws TechnicalServiceNotAliveException {
        TechnicalProduct techProduct = subscription.getProduct()
                .getTechnicalProduct();
        try {
            return getPort(techProduct);
        } catch (TechnicalServiceNotAliveException e) {
            TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                    TechnicalServiceNotAliveException.Reason.CUSTOMER,
                    new Object[] { subscription.getSubscriptionId() }, e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_TECH_SERVICE_NOT_ALIVE_CUSTOMER,
                    subscription.getSubscriptionId());
            throw ex;
        }
    }

    /**
     * Create the ProvisioningService port object for the given technical
     * product and endpoint.
     * 
     * @param techProduct
     *            the technical product for which the stub is created
     * @return the created ProvisioningServiceStub
     */
    protected ProvisioningServiceAdapter getPort(TechnicalProduct techProduct)
            throws TechnicalServiceNotAliveException {
        // Get the timeout value for the outgoing WS call from the configuration
        // settings
        Integer wsTimeout = Integer
                .valueOf(cs.getConfigurationSetting(ConfigurationKey.WS_TIMEOUT,
                        Configuration.GLOBAL_CONTEXT).getValue());
        return ProvisioningServiceAdapterFactory
                .getProvisioningServiceAdapter(techProduct, wsTimeout);
    }

    /**
     * Throw a TechnicalProductOperationFailed exception if the result is null
     * or the return code is not OK
     * 
     * @param subscription
     *            the subscription of the technical product which was called
     * @param result
     *            the result to check
     */
    private void verifyResult(Subscription subscription, BaseResult result)
            throws TechnicalServiceOperationException {
        TechnicalServiceOperationException e = null;
        String msg = "";
        if (result == null) {
            msg = "The provisioning call returned null";
            e = new TechnicalServiceOperationException(msg,
                    new Object[] { subscription.getSubscriptionId(), "null" });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_WS_NULL,
                    subscription.getSubscriptionId());
        } else if (result.getRc() != RETURN_CODE_OK) {
            msg = "The provisioning call returned the error code: "
                    + result.getRc();
            e = new TechnicalServiceOperationException(msg, new Object[] {
                    subscription.getSubscriptionId(), result.getDesc() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_WS_ERROR_CODE,
                    subscription.getSubscriptionId(),
                    String.valueOf(result.getRc()), result.getDesc());
        }
        if (e != null) {
            throw e;
        }
    }

    /**
     * Convert a {@link WebServiceException} into a
     * {@link TechnicalServiceOperationException} and return it.
     * 
     * @param subscription
     *            the subscription of the technical product which was called
     * @param e
     *            the caught {@link WebServiceException}
     * @return the {@link TechnicalServiceOperationException} to throw
     */
    private TechnicalServiceOperationException convertWebServiceException(
            Subscription subscription, WebServiceException e) {
        TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                ERROR_WS_CALL, new Object[] { subscription.getSubscriptionId(),
                        e.getMessage() });
        logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                LogMessageIdentifier.WARN_TECH_SERVICE_WS_EXCEPTION,
                subscription.getSubscriptionId(),
                e.getClass().getName() + ": " + e.getMessage());
        return ex;
    }

    /**
     * Checks if the cause of the webexception is a timeout.
     * 
     * @param e
     *            the webexception.
     * @return true if a timeout was the cause of the exception.
     */
    private boolean isTimeoutOccured(WebServiceException e) {
        return (e.getCause() instanceof SocketTimeoutException) ? true : false;
    }

    /**
     * Create a new technical service not alive exception with the reason
     * "timeout"
     * 
     * @param e
     *            the webexception
     * @return a new TechnicalServiceNotAliveException with timeout as reason.
     */
    private TechnicalServiceNotAliveException convertThrowableTimeout(
            Subscription subscription, Throwable e) {
        TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                TechnicalServiceNotAliveException.Reason.TIMEOUT,
                new Object[] { subscription.getSubscriptionId() }, e);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                LogMessageIdentifier.WARN_TECH_SERVICE_NOT_ALIVE_TIMEOUT,
                subscription.getSubscriptionId());
        return ex;
    }

    /**
     * Convert a {@link Throwable} into a
     * {@link TechnicalServiceNotAliveException} and return it.
     * 
     * @param e
     *            the {@link Throwable} to convert
     * @return the {@link TechnicalServiceNotAliveException} to throw
     */
    private TechnicalServiceNotAliveException convertThrowable(Throwable e) {
        TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                TechnicalServiceNotAliveException.Reason.CONNECTION_REFUSED, e);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                LogMessageIdentifier.WARN_TECH_SERVICE_NOT_ALIVE_CONNECTION_REFUSED);
        return ex;
    }

    /**
     * Initializes an InstanceRequest instance with the data needed for the
     * product instance creation.
     * 
     * @param subscription
     *            the subscription to create the product instance for
     * @return the InstanceRequest instance
     */
    InstanceRequest toInstanceRequest(Subscription subscription) {
        InstanceRequest request = new InstanceRequest();
        request.setSubscriptionId(subscription.getSubscriptionId());
        request.setDefaultLocale(subscription.getOrganization().getLocale());
        request.setOrganizationId(
                subscription.getOrganization().getOrganizationId());
        request.setReferenceId(subscription.getPurchaseOrderNumber());
        if (subscription.getProduct().getTechnicalProduct()
                .getAccessType() != ServiceAccessType.DIRECT
                && subscription.getProduct().getTechnicalProduct()
                        .getAccessType() != ServiceAccessType.USER) {
            String url = cs.getBaseURL();
            url += SERVICE_PATH + Long.toHexString(subscription.getKey());
            request.setLoginUrl(url);
        }
        request.setOrganizationName(subscription.getOrganization().getName());
        request.setParameterValue(
                ParameterFilter.getServiceParameterList(subscription, false));
        request.setAttributeValue(AttributeFilter.getSubscriptionAttributeList(
                subscription, Collections.<ModifiedUda> emptyList()));
        return request;
    }

    /**
     * Maps the given list of UsageLicenses to a list of User instances. Sets
     * user (name, id, ...) and subscription (admin, applicationUserId) specific
     * data.
     * 
     * @param usageLicenses
     *            the UsageLicenes to map
     * @return a list of User instances
     */
    private List<User> toUserList(List<UsageLicense> usageLicenses) {
        List<User> users = new ArrayList<>();
        if (usageLicenses != null) {
            for (UsageLicense usageLicense : usageLicenses) {
                User userWithRole = new User();
                userWithRole.setApplicationUserId(
                        usageLicense.getApplicationUserId());
                PlatformUser platformUser = usageLicense.getUser();
                userWithRole.setUserId(platformUser.getUserId());
                userWithRole.setUserFirstName(platformUser.getFirstName());
                userWithRole.setUserLastName(platformUser.getLastName());
                userWithRole.setEmail(platformUser.getEmail());
                userWithRole.setLocale(platformUser.getLocale());
                RoleDefinition userRoleDefinition = usageLicense
                        .getRoleDefinition();
                if (userRoleDefinition != null) {
                    userWithRole
                            .setRoleIdentifier(userRoleDefinition.getRoleId());
                }
                users.add(userWithRole);
            }
        }
        return users;
    }

    private List<ModifiedUda> getModifiedUdas(Subscription subscription) {

        TypedQuery<ModifiedUda> query = ds.createNamedQuery(
                "ModifiedUda.findBySubscription", ModifiedUda.class);
        query.setParameter("subscriptionKey", new Long(subscription.getKey()));

        return query.getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public OperationResult executeServiceOperation(String userId,
            Subscription subscription, String transactionId,
            TechnicalProductOperation operation, Map<String, String> parameters)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        try {
            List<OperationParameter> list = new ArrayList<>();
            if (parameters != null) {
                for (Entry<String, String> e : parameters.entrySet()) {
                    OperationParameter op = new OperationParameter();
                    op.setName(e.getKey());
                    op.setValue(e.getValue());
                    list.add(op);
                }
            }
            OperationService service = getServiceClient(operation);
            OperationResult result = service.executeServiceOperation(userId,
                    subscription.getProductInstanceId(), transactionId,
                    operation.getOperationId(), list);
            String error = result.getErrorMessage();
            if (error != null && error.trim().length() > 0) {
                TechnicalServiceOperationException tsof = new TechnicalServiceOperationException(
                        error, new Object[] { subscription.getSubscriptionId(),
                                error });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, tsof,
                        LogMessageIdentifier.WARN_TECH_SERVICE_WS_ERROR,
                        subscription.getSubscriptionId(), error);
                throw tsof;
            }
            return result;
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    /**
     * Creates the operation service adapter that delegates to the real the
     * port. If a provisioning user is specified, basic authentication will be
     * used.
     * 
     * @param op
     *            the {@link TechnicalProductOperation} to get the action url
     *            from
     * @return the {@link OperationService}
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws WSDLException
     */
    protected OperationService getServiceClient(TechnicalProductOperation op)
            throws IOException, WSDLException, ParserConfigurationException {

        TechnicalProduct techProduct = op.getTechnicalProduct();
        String username = techProduct.getProvisioningUsername();
        String password = techProduct.getProvisioningPassword();

        // Get the timeout value for the outgoing WS call from the configuration
        // settings
        Integer wsTimeout = Integer
                .valueOf(cs.getConfigurationSetting(ConfigurationKey.WS_TIMEOUT,
                        Configuration.GLOBAL_CONTEXT).getValue());

        return OperationServiceAdapterFactory.getOperationServiceAdapter(op,
                wsTimeout, username, password);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void activateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        try {
            BaseResult result = sendActivateInstance(subscription);
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deactivateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        try {
            BaseResult result = sendDeactivateInstance(subscription);
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException e) {
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    /**
     * Validates the {@link InstanceInfo} returned by
     * {@link #createInstance(Subscription)}.
     * 
     * @param info
     *            the {@link InstanceInfo} to validate
     * @param sub
     *            the {@link Subscription} to create the instance for
     * @throws TechnicalServiceOperationException
     *             in case of invalid data
     */
    void validateInstanceInfo(InstanceInfo info, Subscription sub)
            throws TechnicalServiceOperationException {
        try {
            BLValidator.isDescription("instanceId", info.getInstanceId(), true);
            BLValidator.isAccessinfo("accessInfo", info.getAccessInfo(),
                    isAccessInfoMandatory(info, sub));
            BLValidator.isUrl("baseUrl", info.getBaseUrl(),
                    isBaseUrlMandatory(info, sub));
            BLValidator.isDescription("loginPath", info.getLoginPath(),
                    isLoginPathMandatory(info, sub));
        } catch (ValidationException e) {
            String message = String.format("%s validation of field %s failed",
                    e.getReason().name(), e.getMember());
            TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                    "Service createInstance() returned invalid data.",
                    new Object[] { sub.getSubscriptionId(), message }, e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_TECH_SERVICE_VALIDATION_FAILED,
                    sub.getSubscriptionId(), e.getReason().name(),
                    e.getMember());
            throw ex;
        }
    }

    private boolean isAccessInfoMandatory(InstanceInfo info, Subscription sub) {
        ServiceAccessType accessType = sub.getProduct().getTechnicalProduct()
                .getAccessType();
        if ((accessType == ServiceAccessType.DIRECT
                || accessType == ServiceAccessType.USER)
                && (info.getAccessInfo() == null
                        || info.getAccessInfo().trim().length() <= 0)) {
            // mandatory if no AccessInfo available on technical service at
            // least for the fall-back locale 'en'
            String accessInfo = localizer.getLocalizedTextFromDatabase("en",
                    sub.getProduct().getTechnicalProduct().getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
            return accessInfo == null || accessInfo.trim().length() <= 0;
        }
        return false;
    }

    private boolean isBaseUrlMandatory(InstanceInfo info, Subscription sub) {
        ServiceAccessType accessType = sub.getProduct().getTechnicalProduct()
                .getAccessType();
        return accessType == ServiceAccessType.LOGIN
                && info.getLoginPath() != null;
    }

    private boolean isLoginPathMandatory(InstanceInfo info, Subscription sub) {
        ServiceAccessType accessType = sub.getProduct().getTechnicalProduct()
                .getAccessType();
        return accessType == ServiceAccessType.LOGIN
                && info.getBaseUrl() != null;
    }

    /**
     * Validates the applicationUserId of each {@link User} - it must not be
     * longer than 255 characters.
     * 
     * @param userList
     *            the list of {@link User}s to validate
     * @param sub
     *            the {@link Subscription} to create the users for
     * @throws TechnicalServiceOperationException
     *             in case of a to long application user id
     */
    private static void validateUsers(List<User> userList, Subscription sub)
            throws TechnicalServiceOperationException {
        try {
            for (User user : userList) {
                if (user.getApplicationUserId() != null) {
                    BLValidator.isDescription("applicationUserId",
                            user.getApplicationUserId(), true);
                }
            }
        } catch (ValidationException e) {
            String message = "Invalid application user id with more than 255 characters returned.";
            TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                    "Service createUsers() returned invalid data.",
                    new Object[] { sub.getSubscriptionId(), message }, e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_TECH_SERVICE_VALIDATION_FAILED_USERID_MAXLENGTH,
                    sub.getSubscriptionId());
            throw ex;
        }
    }

    @Override
    public void asyncModifySubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        List<ServiceParameter> serviceParameterList = ParameterFilter
                .getServiceParameterList(product, true);
        List<ServiceAttribute> serviceAttributeList = AttributeFilter
                .getSubscriptionAttributeList(subscription,
                        getModifiedUdas(subscription));
        try {
            BaseResult result = sendAsyncModifyInstance(subscription,
                    serviceParameterList, serviceAttributeList);
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException
                | TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }
    }

    @Override
    public void asyncUpgradeSubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        List<ServiceParameter> serviceParameterList = ParameterFilter
                .getServiceParameterList(product, false);
        List<ServiceAttribute> serviceAttributeList = AttributeFilter
                .getSubscriptionAttributeList(subscription,
                        getModifiedUdas(subscription));
        try {
            BaseResult result = sendAsyncUpgradeInstance(subscription,
                    serviceParameterList, serviceAttributeList);
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException
                | TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    public void upgradeSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        List<ServiceParameter> serviceParameterList = ParameterFilter
                .getServiceParameterList(subscription, false);
        List<ServiceAttribute> serviceAttributeList = AttributeFilter
                .getSubscriptionAttributeList(subscription,
                        getModifiedUdas(subscription));
        try {
            BaseResult result = getPort(subscription).upgradeSubscription(
                    subscription.getProductInstanceId(),
                    subscription.getSubscriptionId(),
                    subscription.getPurchaseOrderNumber(), serviceParameterList,
                    serviceAttributeList, getCurrentUser());
            verifyResult(subscription, result);
        } catch (TechnicalServiceOperationException
                | TechnicalServiceNotAliveException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<String, List<String>> getOperationParameterValues(String userId,
            TechnicalProductOperation operation, Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        Map<String, List<String>> result = new HashMap<>();
        if (!operation.isRequestParameterValuesRequired()) {
            return result;
        }
        try {
            OperationService service = getServiceClient(operation);
            List<OperationParameter> list = service.getParameterValues(userId,
                    subscription.getProductInstanceId(),
                    operation.getOperationId());
            if (list != null) {
                for (OperationParameter op : list) {
                    if (!result.containsKey(op.getName())) {
                        result.put(op.getName(), new LinkedList<String>());
                    }
                    List<String> values = result.get(op.getName());
                    values.add(op.getValue());
                }
            }
            return result;
        } catch (UnsupportedOperationException e) {
            TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                    "Requesting parameter values is unsupported.",
                    new Object[] { subscription.getSubscriptionId(),
                            e.getMessage() },
                    e);
            String msg = String.format(
                    "OperationService.getParameterValues() for subscription %s and operation %s",
                    subscription.getSubscriptionId(),
                    operation.getOperationId());
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_UNSUPPORTED_OPERATION, msg);
            throw ex;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }
    }

    private User getCurrentUser() {
        PlatformUser pUser = ds.getCurrentUserIfPresent();
        if (pUser == null) {
            return null;
        }
        User user = new User();
        user.setEmail(pUser.getEmail());
        user.setLocale(pUser.getLocale());
        user.setUserFirstName(pUser.getFirstName());
        user.setUserLastName(pUser.getLastName());
        user.setUserId(pUser.getUserId());
        return user;
    }

    @Override
    public void saveAttributes(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        try {
            sendSaveAttributes(subscription);
        } catch (TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            throw e;
        } catch (WebServiceException e) {
            if (isTimeoutOccured(e)) {
                throw convertThrowableTimeout(subscription, e);
            }
            throw convertWebServiceException(subscription, e);
        } catch (Throwable e) {
            throw convertThrowable(e);
        }
    }

    boolean isEventProvisioning(TechnicalProduct techProduct) {
        return (cs.isKafkaEnabled()
                && techProduct.getAccessType() == ServiceAccessType.DIRECT
                && techProduct.getProvisioningURL().isEmpty() && techProduct
                        .getProvisioningType() == ProvisioningType.ASYNCHRONOUS);
    }

    BaseResult sendAsyncCreateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException {
        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {
            PublishingResult publishingResult = kafkaProducer
                    .publish(subscription, Operation.UPDATE);
            BaseResult baseResult = new BaseResult();
            baseResult.setRc(publishingResult.isSuccess() ? RETURN_CODE_OK
                    : RETURN_CODE_FAILURE);
            // TODO baseResult.setDesc("");
            return baseResult;
        } else {
            return getPort(subscription).asyncCreateInstance(
                    toInstanceRequest(subscription), getCurrentUser());
        }
    }

    BaseResult sendAsyncModifyInstance(Subscription subscription,
            List<ServiceParameter> serviceParameterList,
            List<ServiceAttribute> serviceAttributeList)
            throws TechnicalServiceNotAliveException {
        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {

            Subscription subscriptionModified = getModifiedSubscription(
                    subscription, serviceParameterList);

            PublishingResult publishingResult = kafkaProducer
                    .publish(subscriptionModified, Operation.UPDATE);
            BaseResult baseResult = new BaseResult();
            baseResult.setRc(publishingResult.isSuccess() ? RETURN_CODE_OK
                    : RETURN_CODE_FAILURE);
            // TODO baseResult.setDesc("");
            return baseResult;
        } else {
            return getPort(subscription).asyncModifySubscription(
                    subscription.getProductInstanceId(),
                    subscription.getSubscriptionId(),
                    subscription.getPurchaseOrderNumber(), serviceParameterList,
                    serviceAttributeList, getCurrentUser());
        }
    }

    /**
     * @param subscription
     * @param serviceParameterList
     * @return
     */
    Subscription getModifiedSubscription(Subscription subscription,
            List<ServiceParameter> serviceParameterList) {
        List<Parameter> modifiedParameters = new ArrayList<>();
        ParameterSet paramSet = subscription.getParameterSet();
        paramSet.getParameters().forEach(parameter -> {
            serviceParameterList.forEach(serviceParameter -> {
                if (parameter.getParameterDefinition().getParameterId()
                        .equals(serviceParameter.getParameterId())) {
                    Parameter modifiedParameter = new Parameter();
                    ParameterDefinition paramDef = new ParameterDefinition();
                    paramDef.setParameterId(serviceParameter.getParameterId());
                    modifiedParameter.setParameterDefinition(paramDef);
                    modifiedParameter.setValue(serviceParameter.getValue());
                    modifiedParameters.add(modifiedParameter);
                }
            });
        });

        // put everything for the json: UUID and parameters (id and value)
        Subscription subscriptionModified = new Subscription();
        Product productModified = new Product();
        ParameterSet parameterSetModified = new ParameterSet();
        subscriptionModified.setUuid(subscription.getUuid());
        parameterSetModified.setParameters(modifiedParameters);
        productModified.setParameterSet(parameterSetModified);
        subscriptionModified.setProduct(productModified);
        return subscriptionModified;
    }

    BaseResult sendAsyncUpgradeInstance(Subscription subscription,
            List<ServiceParameter> serviceParameterList,
            List<ServiceAttribute> serviceAttributeList)
            throws TechnicalServiceNotAliveException {
        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {
            // TODO send to kafka
            // System.out.println(getSubscriptionMessage().getJson(subscription));
            return getNotYetSupportedResult();
        } else {
            return getPort(subscription).asyncUpgradeSubscription(
                    subscription.getProductInstanceId(),
                    subscription.getSubscriptionId(),
                    subscription.getPurchaseOrderNumber(), serviceParameterList,
                    serviceAttributeList, getCurrentUser());
        }
    }

    void sendSaveAttributes(Subscription subscription)
            throws TechnicalServiceOperationException,
            TechnicalServiceNotAliveException {
        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {
            // TODO send to kafka, probably not??
        } else {
            String organizationId = subscription.getOrganization()
                    .getOrganizationId();
            getPort(subscription).saveAttributes(organizationId,
                    AttributeFilter.getCustomAttributeList(subscription),
                    getCurrentUser());
        }
    }

    BaseResult sendActivateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException {
        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {
            // TODO send to kafka??
            // System.out.println(getSubscriptionMessage().getJson(subscription));
            return getNotYetSupportedResult();
        } else {
            return getPort(subscription).activateInstance(
                    subscription.getProductInstanceId(), getCurrentUser());
        }
    }

    BaseResult sendDeactivateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException {
        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {
            // TODO send to kafka??
            return getNotYetSupportedResult();
        } else {
            return getPort(subscription).deactivateInstance(
                    subscription.getProductInstanceId(), getCurrentUser());
        }
    }

    BaseResult sendDeleteInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException {
        String instanceId = subscription.getProductInstanceId();
        String organizationId = subscription.getOrganization()
                .getOrganizationId();
        String subscriptionId = subscription.getSubscriptionId();

        if (isEventProvisioning(
                subscription.getProduct().getTechnicalProduct())) {
            PublishingResult publishingResult = kafkaProducer
                    .publish(subscription, Operation.DELETE);
            BaseResult baseResult = new BaseResult();
            baseResult.setRc(publishingResult.isSuccess() ? RETURN_CODE_OK
                    : RETURN_CODE_FAILURE);
            // TODO baseResult.setDesc("");
            return baseResult;
        } else {
            return getPort(subscription).deleteInstance(instanceId,
                    organizationId, subscriptionId, getCurrentUser());
        }
    }

    private BaseResult getNotYetSupportedResult() {
        BaseResult result = new BaseResult();
        result.setDesc("Event-based provisioning is not yet supported!");
        result.setRc(1);
        return result;
    }

}
