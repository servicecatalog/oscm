/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.apache.commons.lang3.StringUtils;
import org.oscm.accountservice.assembler.BillingContactAssembler;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.assembler.PaymentInfoAssembler;
import org.oscm.accountservice.assembler.UdaAssembler;
import org.oscm.accountservice.dataaccess.UdaAccess;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.OnBehalfUserReference;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.OperationRecord;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModifiedEntityType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.id.IdGenerator;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.auditlog.interceptor.AuditLogDataInterceptor;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.SubscriptionSearchService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.InvalidPhraseException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OperationPendingException.ReasonEnum;
import org.oscm.internal.types.exception.OperationStateException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.types.exception.PriceModelException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceChangedException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.types.exception.SubscriptionAlreadyExistsException;
import org.oscm.internal.types.exception.SubscriptionMigrationException;
import org.oscm.internal.types.exception.SubscriptionMigrationException.Reason;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.SubscriptionStillActiveException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOSubscriptionIdAndOrganizations;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserSubscription;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.notification.vo.VONotification;
import org.oscm.notification.vo.VOProperty;
import org.oscm.operation.data.OperationResult;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationFullTextFilter;
import org.oscm.permission.PermissionCheck;
import org.oscm.provisioning.data.User;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.assembler.RoleAssembler;
import org.oscm.serviceprovisioningservice.assembler.TechServiceOperationParameterAssembler;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.string.Strings;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.dao.BillingContactDao;
import org.oscm.subscriptionservice.dao.MarketplaceDao;
import org.oscm.subscriptionservice.dao.ModifiedEntityDao;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.subscriptionservice.dao.ProductDao;
import org.oscm.subscriptionservice.dao.SessionDao;
import org.oscm.subscriptionservice.dao.SubscriptionDao;
import org.oscm.subscriptionservice.dao.SubscriptionHistoryDao;
import org.oscm.subscriptionservice.dao.UsageLicenseDao;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.operations.NotifyProvisioningServiceHandler;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.payloads.NotifyProvisioningServicePayload;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.techproductoperation.bean.OperationRecordServiceLocalBean;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.triggerservice.bean.TriggerProcessIdentifiers;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.triggerservice.notification.VONotificationBuilder;
import org.oscm.triggerservice.validator.TriggerProcessValidator;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.types.exceptions.UserAlreadyAssignedException;
import org.oscm.types.exceptions.UserNotAssignedException;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.PaymentDataValidator;
import org.oscm.validator.ADMValidator;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Session Bean implementation class of SubscriptionService (Remote IF) and
 * SubscriptionServiceLocal (Local IF)
 */
@Stateless
@Remote(SubscriptionService.class)
@Local(SubscriptionServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class,
        AuditLogDataInterceptor.class })
public class SubscriptionServiceBean
        implements SubscriptionService, SubscriptionServiceLocal {

    public static final String KEY_PAIR_NAME = "Key pair name";
    public static final String AMAZONAWS_COM = "amazonaws.com";
    private static final int PAYMENTTYPE_INVOICE = 3;
    private static final String VMS_NUMBER_PARAM = "VMS_NUMBER";

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(SubscriptionServiceBean.class);

    @EJB(beanInterface = ApplicationServiceLocal.class)
    protected ApplicationServiceLocal appManager;

    @EJB(beanInterface = SessionServiceLocal.class)
    protected SessionServiceLocal prodSessionMgmt;

    @EJB
    protected DataService dataManager;

    @EJB
    protected IdentityServiceLocal idManager;

    @EJB
    protected TenantProvisioningServiceBean tenantProvisioning;

    @EJB
    protected CommunicationServiceLocal commService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    ConfigurationServiceLocal cfgService;

    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal subscriptionListService;

    @EJB(beanInterface = TriggerQueueServiceLocal.class)
    protected TriggerQueueServiceLocal triggerQS;

    @EJB(beanInterface = TaskQueueServiceLocal.class)
    public TaskQueueServiceLocal tqs;

    @EJB(beanInterface = AccountServiceLocal.class)
    public AccountServiceLocal accountService;

    @EJB
    public SubscriptionSearchService subscriptionSearchService;

    @EJB
    SubscriptionAuditLogCollector audit;

    @Resource
    protected SessionContext sessionCtx;

    @Inject
    public TerminateSubscriptionBean terminateBean;

    @Inject
    public ManageSubscriptionBean manageBean;

    @Inject
    public ValidateSubscriptionStateBean stateValidator;

    @Inject
    public ModifyAndUpgradeSubscriptionBean modUpgBean;

    @Inject
    public OperationRecordServiceLocalBean operationRecordBean;

    @Inject
    UserGroupServiceLocalBean userGroupService;

    @EJB
    public TriggerService triggerService;

    private static final List<SubscriptionStatus> VISIBLE_SUBSCRIPTION_STATUS = Arrays
            .asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED,
                    SubscriptionStatus.PENDING, SubscriptionStatus.PENDING_UPD,
                    SubscriptionStatus.SUSPENDED,
                    SubscriptionStatus.SUSPENDED_UPD);

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscription subscribeToService(VOSubscription subscription,
            VOService service, List<VOUsageLicense> users,
            VOPaymentInfo paymentInfo, VOBillingContact billingContact,
            List<VOUda> udas)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, PaymentInformationException,
            ServiceParameterException, ServiceChangedException,
            PriceModelException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            SubscriptionAlreadyExistsException, OperationPendingException,
            MandatoryUdaMissingException, MandatoryCustomerUdaMissingException,
            ConcurrentModificationException, SubscriptionStateException {

        ArgumentValidator.notNull("subscription", subscription);
        ArgumentValidator.notNull("service", service);

        Subscription sub;
        PlatformUser currentUser = dataManager.getCurrentUser();

        Product prod = dataManager.getReference(Product.class,
                service.getKey());
        checkIfServiceAvailable(service.getKey(), service.getServiceId(),
                currentUser);
        checkIfSubscriptionAlreadyExists(prod);
        verifyIdAndKeyUniqueness(currentUser, subscription);

        if (isPaymentInfoHidden() && prod.getPriceModel().isChargeable()) {
            if (billingContact == null) {
                billingContact = createBillingContactForOrganization(
                        currentUser);
            }
            if (paymentInfo == null) {
                Organization organization = currentUser.getOrganization();
                paymentInfo = createPaymentInfoForOrganization(organization);
            }
        }
        validateSettingsForSubscribing(subscription, service, paymentInfo,
                billingContact);
        validateUserAssignmentForSubscribing(service, users);

        validateTriggerProcessForCreateSubscription(subscription);

        TriggerProcess triggerProcess = createTriggerProcessForCreateSubscription(
                subscription, service, users, paymentInfo, billingContact,
                udas);

        VOSubscription voSub = null;
        TriggerDefinition triggerDefinition = triggerProcess
                .getTriggerDefinition();

        if (triggerDefinition == null) {
            try {
                sub = subscribeToServiceInt(triggerProcess);

                voSub = SubscriptionAssembler.toVOSubscription(sub,
                        new LocalizerFacade(localizer,
                                dataManager.getCurrentUser().getLocale()));

                autoAssignUser(prod, sub);
            } catch (ObjectNotFoundException | ValidationException
                    | ServiceChangedException | PriceModelException
                    | PaymentInformationException
                    | NonUniqueBusinessKeyException
                    | TechnicalServiceNotAliveException
                    | TechnicalServiceOperationException
                    | ServiceParameterException
                    | ConcurrentModificationException
                    | MandatoryUdaMissingException | SubscriptionStateException
                    | MandatoryCustomerUdaMissingException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            triggerProcess.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createUnsubscribeFromService(
                            dataManager, TriggerType.SUBSCRIBE_TO_SERVICE,
                            subscription.getSubscriptionId()));
            dataManager.merge(triggerProcess);
        }

        return voSub;
    }

    @Override
    public boolean isPaymentInfoHidden() {
        return !cfgService.isPaymentInfoAvailable();
    }

    private VOBillingContact createBillingContactForOrganization(
            PlatformUser user) throws NonUniqueBusinessKeyException {
        Organization organization = user.getOrganization();
        BillingContact orgBillingContact = new BillingContact();
        String email = organization.getEmail() == null ? " "
                : organization.getEmail();

        String address = organization.getAddress() == null ? " "
                : organization.getAddress();
        List<BillingContact> billingContacts = getBillingContactDao()
                .getBillingContactsForOrganization(organization.getKey(), email,
                        address);
        if (!billingContacts.isEmpty()) {
            orgBillingContact = billingContacts.get(0);
        } else {
            orgBillingContact.setAddress(address);
            orgBillingContact.setCompanyName(organization.getName());
            orgBillingContact.setOrganization_tkey(organization.getKey());
            orgBillingContact.setOrgAddressUsed(true);
            orgBillingContact.setEmail(email);
            String organizationId = organization.getName() == null
                    ? user.getUserId() : organization.getName();
            orgBillingContact.setBillingContactId(organizationId
                    + DateFactory.getInstance().getTransactionTime());
            orgBillingContact.setOrganization(organization);
            dataManager.persist(orgBillingContact);
        }

        return BillingContactAssembler.toVOBillingContact(orgBillingContact);
    }

    private VOPaymentInfo createPaymentInfoForOrganization(
            Organization organization) throws NonUniqueBusinessKeyException {
        PaymentInfo paInfo = new PaymentInfo(
                DateFactory.getInstance().getTransactionTime());
        paInfo.setOrganization_tkey(organization.getKey());
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId(PaymentType.INVOICE);
        paymentType = (PaymentType) dataManager.find(paymentType);
        paInfo.setOrganization(organization);
        paInfo.setPaymentType(paymentType);
        LocalizerFacade localizerFacade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());

        paInfo.setPaymentInfoId(localizerFacade.getText(PAYMENTTYPE_INVOICE,
                LocalizedObjectTypes.PAYMENT_TYPE_NAME));
        try {
            paInfo = (PaymentInfo) dataManager
                    .getReferenceByBusinessKey(paInfo);
        } catch (ObjectNotFoundException onfe) {
            dataManager.persist(paInfo);
        }

        return PaymentInfoAssembler.toVOPaymentInfo(paInfo, localizerFacade);
    }

    private void autoAssignUser(Product prod, Subscription sub)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException {

        TechnicalProduct techProd = prod.getTechnicalProduct();

        if (ProvisioningType.SYNCHRONOUS.equals(techProd.getProvisioningType())
                && prod.isAutoAssignUserEnabled() != null
                && prod.isAutoAssignUserEnabled().booleanValue()) {
            VOService svc = new VOService();
            svc.setKey(prod.getKey());
            assignUsersForSubscription(sub.getSubscriptionId(), svc);
        }
    }

    void assignUsersForSubscription(String subscriptionId, VOService service)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException {

        PlatformUser currentUser = dataManager.getCurrentUser();
        assignUsersForSubscription(subscriptionId, service, currentUser);

    }

    void assignUsersForSubscription(String subscriptionId, VOService service,
            PlatformUser userToAssign) throws ObjectNotFoundException,
            OperationNotPermittedException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            ConcurrentModificationException {
        List<VORoleDefinition> serviceRoles = getServiceRolesForService(
                service);
        // If the service roles are defined for technical service,
        // assign the first role to user to avoid assignment failure.
        // If service roles are not defined, set default role to null.
        List<VOUsageLicense> usersToBeAdded = new ArrayList<>();
        VOUsageLicense lic = new VOUsageLicense();
        lic.setUser(UserDataAssembler.toVOUserDetails(userToAssign));
        lic.setRoleDefinition((serviceRoles == null || serviceRoles.isEmpty())
                ? null : serviceRoles.get(0));
        usersToBeAdded.add(lic);

        TriggerProcess proc = new TriggerProcess();
        proc.addTriggerProcessParameter(TriggerProcessParameterName.OBJECT_ID,
                subscriptionId);
        proc.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subscriptionId);
        proc.addTriggerProcessParameter(
                TriggerProcessParameterName.USERS_TO_ADD, usersToBeAdded);
        proc.addTriggerProcessParameter(
                TriggerProcessParameterName.USERS_TO_REVOKE, null);

        addRevokeUserInt(proc);
    }

    /**
     * @param subscription
     * @return
     * @throws OperationPendingException
     */
    private void validateTriggerProcessForCreateSubscription(
            VOSubscription subscription) throws OperationPendingException {
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        String subscriptionId = subscription.getSubscriptionId();
        if (validator.isSubscribeOrUnsubscribeServicePending(subscriptionId)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to create a subscription or unsubscribe from the subscription with identifier '%s'",
                            subscriptionId),
                    ReasonEnum.SUBSCRIBE_TO_SERVICE,
                    new Object[] { subscriptionId });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_SUBSCRIBE_TO_SERVICE_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subscriptionId);
            throw ope;
        }
    }

    private TriggerProcess createTriggerProcessForCreateSubscription(
            VOSubscription subscription, VOService service,
            List<VOUsageLicense> users, VOPaymentInfo paymentInfo,
            VOBillingContact billingContact, List<VOUda> udas) {
        TriggerMessage message = new TriggerMessage(
                TriggerType.SUBSCRIBE_TO_SERVICE);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess triggerProcess = list.get(0).getTrigger();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.OBJECT_ID,
                subscription.getSubscriptionId());
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subscription);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PRODUCT, service);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USERS, users);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PAYMENTINFO, paymentInfo);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, billingContact);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.UDAS, udas);
        return triggerProcess;
    }

    void saveUdasForSubscription(List<VOUda> udas, Subscription subscription)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, OperationNotPermittedException,
            ConcurrentModificationException, MandatoryUdaMissingException,
            MandatoryCustomerUdaMissingException {
        Organization supplier = subscription.getProduct()
                .getSupplierOrResellerTemplate().getVendor();
        UdaAccess udaAccess = new UdaAccess(dataManager, sessionCtx);
        udaAccess.saveUdasForSubscription(udas, supplier, subscription);
    }

    /**
     * Checks if the technical product the marketing service belongs to has
     * service roles defined and if the roles defined for the user assignments
     * are part of this set and defined.
     * 
     * @param product
     *            the product to subscribe to
     * @param users
     *            the users to assign
     * @throws ObjectNotFoundException
     *             if the product or a role wasn't found
     * @throws OperationNotPermittedException
     *             if the role to assign doesn't belong to the products
     *             technical product or if no role is set when using service
     *             roles.
     */
    private void validateUserAssignmentForSubscribing(VOService product,
            List<VOUsageLicense> users)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Product prod = dataManager.getReference(Product.class,
                product.getKey());
        if (users == null) {
            return;
        }
        for (VOUsageLicense lic : users) {
            getAndCheckServiceRole(lic, prod);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Subscription subscribeToServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ServiceChangedException,
            PriceModelException, PaymentInformationException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ServiceParameterException,
            SubscriptionAlreadyExistsException, ConcurrentModificationException,
            MandatoryUdaMissingException, MandatoryCustomerUdaMissingException,
            SubscriptionStateException {

        PlatformUser currentUser = dataManager.getCurrentUser();
        Organization organization = currentUser.getOrganization();

        // read parameters from trigger process
        VOSubscription subscription = tp
                .getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(VOSubscription.class);
        VOService product = tp
                .getParamValueForName(TriggerProcessParameterName.PRODUCT)
                .getValue(VOService.class);
        VOPaymentInfo voPaymentInfo = tp
                .getParamValueForName(TriggerProcessParameterName.PAYMENTINFO)
                .getValue(VOPaymentInfo.class);
        VOBillingContact voBillingContact = tp
                .getParamValueForName(
                        TriggerProcessParameterName.BILLING_CONTACT)
                .getValue(VOBillingContact.class);
        List<?> udas = tp.getParamValueForName(TriggerProcessParameterName.UDAS)
                .getValue(List.class);
        PlatformUser owner = dataManager.getReference(PlatformUser.class,
                tp.getUser().getKey());

        Product productTemplate = dataManager.getReference(Product.class,
                product.getKey());
        checkIfSubscriptionAlreadyExists(productTemplate);

        UserGroup unit = getUnit(subscription.getUnitKey(),
                subscription.getUnitName(), organization.getKey());

        validateSettingsForSubscribing(subscription, product, voPaymentInfo,
                voBillingContact);

        Organization vendor = productTemplate.getVendor();

        Organization supplier = dataManager
                .getReference(Product.class, product.getKey())
                .getSupplierOrResellerTemplate().getVendor();

        List<VOUda> originalCustomerUdas = getUdasForCustomer("CUSTOMER",
                dataManager.getCurrentUser().getOrganization().getKey(),
                supplier);

        OrganizationReference refVendorCust = null;
        if (!organization.getVendorsOfCustomer().contains(vendor)) {
            refVendorCust = new OrganizationReference(vendor, organization,
                    OrganizationReferenceType.getOrgRefTypeForSourceRoles(
                            vendor.getGrantedRoleTypes()));
            dataManager.persist(refVendorCust);
        }
        if (vendor.getGrantedRoleTypes().contains(OrganizationRoleType.BROKER)
                && !organization.getVendorsOfCustomer()
                        .contains(productTemplate.getTemplate().getVendor())) {
            refVendorCust = new OrganizationReference(
                    productTemplate.getTemplate().getVendor(), organization,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            dataManager.persist(refVendorCust);
        }
        if (refVendorCust != null) {
            enableDefaultPaymentsForCustomer(refVendorCust);
        }

        // Look for the marketplace where the service is published
        Marketplace mp = null;
        Product publishedService = productTemplate
                .getType() == ServiceType.CUSTOMER_TEMPLATE
                        ? productTemplate.getTemplate() : productTemplate;
        List<Marketplace> mps = getMarketplaceDao()
                .getMarketplaceByService(publishedService);
        for (Marketplace m : mps) {
            mp = m; // current assumption is that there's only one marketplace
            break;
        }

        // Create a new subscription object
        Subscription newSub = new Subscription();
        newSub.setUuid(UUID.randomUUID());
        newSub.setEventPublished(false);

        Long creationTime = Long
                .valueOf(DateFactory.getInstance().getTransactionTime());
        newSub.setCreationDate(creationTime);
        newSub.setStatus(SubscriptionStatus.PENDING);
        // set default cut-off day (db unique constrain)
        newSub.setCutOffDay(1);
        newSub.setSubscriptionId(subscription.getSubscriptionId().trim());
        newSub.setPurchaseOrderNumber(subscription.getPurchaseOrderNumber());
        newSub.setOrganization(organization);
        // for subscribing service, set the current user as subscription owner
        newSub.setOwner(owner);

        verifyUnitAndRoles(currentUser, unit, newSub);

        Product theProduct = productTemplate.copyForSubscription(
                productTemplate.getTargetCustomer(), newSub);

        if (theProduct.getPriceModel() != null) {
            // FIXME LG clean
            // The first target pricemodel version is created when subscription
            // is still in PENDING, but must be fitered for billing. Set the
            // indicating flag before persisting.
            theProduct.getPriceModel().setProvisioningCompleted(false);
            if (theProduct.getPriceModel().isExternal()) {
                newSub.setExternal(true);
            }
        }

        // to avoid id conflicts in high load scenarios add customer
        // organization hash
        theProduct.setProductId(
                theProduct.getProductId() + organization.hashCode());
        theProduct.setOwningSubscription(null);
        // subscription copies do not have/need a CatalogEntry
        theProduct.setCatalogEntries(new ArrayList<CatalogEntry>());

        try {
            dataManager.persist(theProduct);
        } catch (NonUniqueBusinessKeyException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The product copy for product '" + product.getKey()
                            + "' cannot be stored, as the business key already exists.",
                    e);
            LOG.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_CREATE_CUSTOMER_FOR_SPECIFIC_PRICEMODEL_FAILED,
                    Long.toString(dataManager.getCurrentUser().getKey()));
            throw sse;
        }

        copyLocalizedPricemodelValues(theProduct, productTemplate);

        // update the subscription's configurable parameter
        List<Parameter> modifiedParametersForLog = updateConfiguredParameterValues(
                theProduct, product.getParameters(), null);

        // now bind the product and the price model to the subscription:
        newSub.bindToProduct(theProduct);

        // register the marketplace the subscription was coming from
        newSub.setMarketplace(mp);

        // Link the passed payment information to this subscription
        if (voPaymentInfo != null) {
            PaymentInfo paymentInfo = dataManager
                    .getReference(PaymentInfo.class, voPaymentInfo.getKey());
            newSub.setPaymentInfo(paymentInfo);
        }
        if (voBillingContact != null) {
            BillingContact bc = dataManager.getReference(BillingContact.class,
                    voBillingContact.getKey());
            newSub.setBillingContact(bc);
        }

        // persist the subscription. This is essential to ensure the
        // subscription exists and also eliminates all potential problems
        // with a subsequent call to the application.
        dataManager.persist(newSub);

        // save subscription attributes before provisioning call, to have them
        // available at the API
        saveUdasForSubscription(ParameterizedTypes.list(udas, VOUda.class),
                newSub);
        dataManager.flush();

        // send customer udas to corresponding app
        appManager.saveAttributes(newSub);

        theProduct.setOwningSubscription(newSub);
        createAllowOnBehalfActingReference(newSub);
        TenantProvisioningResult provisioningResult = createInstanceAndAddUsersToSubscription(
                tp, newSub);
        newSub.setSuccessMessage(provisioningResult.getResultMesage());

        dataManager.flush();

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.SUBSCRIPTION_CREATION,
                        tp.getTriggerProcessParameters(), vendor));

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.SUBSCRIBE_TO_SERVICE,
                        tp.getTriggerProcessParameters(), dataManager
                                .getCurrentUser().getOrganization()));

        sendSubscriptionCreatedMailToAdministrators(newSub, newSub.getProduct()
                .getTechnicalProduct().isAllowingOnBehalfActing());

        // Used for Autoassign a service which was supended via trigger
        // (subscribeToService)
        autoAssignUserForTriggerProcess(tp, product, owner, newSub);

        audit.editSubscriptionParameterConfiguration(dataManager, theProduct,
                modifiedParametersForLog);

        audit.subscribeToService(dataManager, newSub);
        logSubscriptionAttributeForCreation(newSub,
                ParameterizedTypes.list(udas, VOUda.class),
                originalCustomerUdas);

        return newSub;
    }

    private void verifyUnitAndRoles(PlatformUser currentUser, UserGroup unit,
            Subscription newSub) throws OperationNotPermittedException {
        if (!currentUser.isOrganizationAdmin()) {
            boolean isUnitAdmin = currentUser.isUnitAdmin();
            boolean isSubMgr = currentUser.isSubscriptionManager();
            boolean isUnitToBeAssigned = unit != null;
            boolean unitIsMandatory = isUnitAdmin && !isSubMgr;
            boolean unitIsForbidden = !isUnitAdmin && isSubMgr;

            if (isUnitToBeAssigned && unitIsForbidden) {
                throw new OperationNotPermittedException();
            } else if (unitIsMandatory && !isUnitToBeAssigned) {
                throw new OperationNotPermittedException();
            }
        }
        if (currentUser.isOrganizationAdmin() || currentUser.isUnitAdmin()) {
            newSub.setUserGroup(unit);
        }
    }

    private void autoAssignUserForTriggerProcess(TriggerProcess tp,
            VOService product, PlatformUser owner, Subscription newSub)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceParameterException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            ConcurrentModificationException {
        Product prod = dataManager.getReference(Product.class,
                product.getKey());
        TechnicalProduct techProd = prod.getTechnicalProduct();

        if (ProvisioningType.SYNCHRONOUS.equals(techProd.getProvisioningType())
                && tp.getTriggerDefinition() != null
                && (prod.isAutoAssignUserEnabled() != null
                        && prod.isAutoAssignUserEnabled().booleanValue())
                && newSub.getUsageLicenseForUser(owner) == null) {
            // TODO 1. assign users only for SYNCHRONOUS case.
            // 2. extract code to another method (more readability).
            if (owner != dataManager.getCurrentUser()) {
                assignUsersForSubscription(newSub.getSubscriptionId(), product,
                        owner);
            } else {
                assignUsersForSubscription(newSub.getSubscriptionId(), product);
            }

        }
    }

    List<VOUda> getUdasForCustomer(String targetType, long targetObjectKey,
            Organization supplier) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("targetType", targetType);
        UdaTargetType type = UdaAssembler.toUdaTargetType(targetType);
        Organization customer = dataManager.getCurrentUser().getOrganization();

        UdaAccess udaAccess = new UdaAccess(dataManager, sessionCtx);
        List<Uda> udas = udaAccess.getUdasForTypeTargetAndCustomer(
                targetObjectKey, type, supplier, customer);
        List<VOUda> voUdas = new ArrayList<>();
        for (Uda uda : udas) {
            voUdas.add(UdaAssembler.toVOUda(uda, new LocalizerFacade(localizer,
                    dataManager.getCurrentUser().getLocale())));
        }

        return voUdas;
    }

    void logSubscriptionAttributeForEdit(Subscription sub,
            List<VOUda> udaList) {
        Organization customer = dataManager.getCurrentUser().getOrganization();
        for (VOUda voUda : udaList) {
            audit.editSubscriptionAndCustomerAttributeByCustomer(dataManager,
                    customer, sub, voUda.getUdaDefinition().getUdaId(),
                    voUda.getUdaValue(),
                    voUda.getUdaDefinition().getTargetType());
        }
    }

    void logSubscriptionAttributeForCreation(Subscription sub,
            List<VOUda> udaList, List<VOUda> customerUdaList) {
        Map<String, String> customerAttributesMap = new HashMap<>();
        for (VOUda voUda : customerUdaList) {
            String parameterName = voUda.getUdaDefinition().getUdaId();
            String parameterValue = voUda.getUdaValue();
            customerAttributesMap.put(parameterName, parameterValue);
        }

        for (VOUda voUda : udaList) {
            String parameterName = voUda.getUdaDefinition().getUdaId();
            String targetType = voUda.getUdaDefinition().getTargetType();
            String parameterValue = voUda.getUdaValue();
            String defaultValue = voUda.getUdaDefinition().getDefaultValue();
            defaultValue = defaultValue == null ? "" : defaultValue;

            if (!UdaTargetType.CUSTOMER.toString().equals(targetType)) {
                if (!parameterValue.equals(defaultValue)) {
                    audit.editSubscriptionAndCustomerAttributeByCustomer(
                            dataManager, null, sub, parameterName,
                            parameterValue,
                            voUda.getUdaDefinition().getTargetType());
                }
            } else {
                String existingValue = customerAttributesMap
                        .get(voUda.getUdaDefinition().getUdaId());
                existingValue = existingValue == null ? "" : existingValue;
                Organization customer = dataManager.getCurrentUser()
                        .getOrganization();
                if (!(parameterValue.equals(existingValue)
                        || parameterValue.equals(defaultValue))) {
                    audit.editSubscriptionAndCustomerAttributeByCustomer(
                            dataManager, customer, null, parameterName,
                            parameterValue,
                            voUda.getUdaDefinition().getTargetType());
                }
            }
        }
    }

    void logSubscriptionOwner(Subscription sub, PlatformUser oldOwner) {
        audit.editSubscriptionOwner(dataManager, sub, oldOwner);
    }

    /**
     * Get the subscription attributes that are changed by user
     */
    List<VOUda> getUpdatedSubscriptionAttributes(List<VOUda> inputUdaList,
            List<Uda> existingUdas) {
        Map<String, String> existingAttributesMap = new HashMap<>();
        List<VOUda> updatedList = new ArrayList<>();
        for (Uda uda : existingUdas) {
            existingAttributesMap.put(uda.getUdaDefinition().getUdaId(),
                    uda.getUdaValue());
        }
        for (VOUda input : inputUdaList) {
            String existingValue = existingAttributesMap
                    .get(input.getUdaDefinition().getUdaId());
            String defaultValue = input.getUdaDefinition()
                    .getDefaultValue() == null ? ""
                            : input.getUdaDefinition().getDefaultValue();
            String inputValue = input.getUdaValue() == null ? ""
                    : input.getUdaValue();
            if (existingValue == null && !inputValue.equals(defaultValue)) {
                updatedList.add(input);
            }
            if (existingValue != null && !inputValue.equals(existingValue)) {
                updatedList.add(input);
            }
        }
        return updatedList;
    }

    /**
     * Enables the payment types that are enabled for new customer for the
     * provided {@link OrganizationReference}.
     * 
     * @param refSuplCust
     *            the reference to enable the default payment types for
     */
    private void enableDefaultPaymentsForCustomer(
            OrganizationReference refSuplCust) {
        Organization supplier = refSuplCust.getSource();
        Organization customer = refSuplCust.getTarget();
        Set<OrganizationToRole> roles = customer.getGrantedRoles();
        OrganizationRole role = null;
        for (OrganizationToRole organizationToRole : roles) {
            OrganizationRole tmp = organizationToRole.getOrganizationRole();
            if (tmp.getRoleName() == OrganizationRoleType.CUSTOMER) {
                role = tmp;
                break;
            }
        }
        // the suppliers default configuration
        List<OrganizationRefToPaymentType> refs = supplier.getPaymentTypes(true,
                refSuplCust
                        .getReferenceType() == OrganizationReferenceType.RESELLER_TO_CUSTOMER
                                ? OrganizationRoleType.RESELLER
                                : OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.PLATFORM_OPERATOR.name());
        for (OrganizationRefToPaymentType ref : refs) {
            OrganizationRefToPaymentType newRef = new OrganizationRefToPaymentType();
            newRef.setOrganizationReference(refSuplCust);
            newRef.setPaymentType(ref.getPaymentType());
            newRef.setOrganizationRole(role);
            try {
                dataManager.persist(newRef);
            } catch (NonUniqueBusinessKeyException e) {
                SaaSSystemException sse = new SaaSSystemException(
                        "Caught NonUniqueBusinessKeyException although there is no business key",
                        e);
                LOG.logError(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.ERROR_UNEXPECTED_BK_VIOLATION);
                throw sse;
            }
            refSuplCust.getPaymentTypes().add(newRef);
        }
    }

    /**
     * Add users to the subscription assign the users to the subscription, but
     * do not inform the product about it at this time (will be done later) We
     * assume that the users already exist as platform users ! We start with the
     * admins: if a user is in both lists, we just can ignore his entry in
     * "users", as he is already entered from "admins" and has the correct
     * isAdmin-flag ! We have to ensure that at least 1 user is assigned
     * 
     * @throws ServiceParameterException
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     * @throws TechnicalServiceOperationException
     * @throws TechnicalServiceNotAliveException
     */
    private TenantProvisioningResult createInstanceAndAddUsersToSubscription(
            TriggerProcess tp, Subscription subscription)
            throws ServiceParameterException, ObjectNotFoundException,
            OperationNotPermittedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        List<PlatformUser> addedUsers = new ArrayList<>();
        List<UsageLicense> addedUserLicenses = new ArrayList<>();
        List<?> users = tp
                .getParamValueForName(TriggerProcessParameterName.USERS)
                .getValue(List.class);

        if (users != null) {
            for (Object o : users) {
                VOUsageLicense lic = VOUsageLicense.class.cast(o);
                PlatformUser usr = idManager.getPlatformUser(
                        lic.getUser().getUserId(),
                        dataManager.getCurrentUser().getTenantId(), true); // not
                                                                           // found?
                                                                           // =>
                                                                           // throws
                // ObjectNotFoundException
                RoleDefinition role = getAndCheckServiceRole(lic,
                        subscription.getProduct());
                try {
                    addUserToSubscription(subscription, usr, role);
                    addedUsers.add(usr);
                    UsageLicense usageLicenseForUser = subscription
                            .getUsageLicenseForUser(usr);
                    // add the user's license to the list for later mail
                    // sending
                    if (usageLicenseForUser != null) {
                        addedUserLicenses.add(usageLicenseForUser);
                    }
                } catch (UserAlreadyAssignedException e) {
                    // Most probably the user already has been in the users
                    // list (or he/she is in the list twice)
                    // Let's ignore it!
                    // But log this event !
                    LOG.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_USER_APPEAR_MORE_THAN_ONCE,
                            Long.toString(usr.getKey()),
                            Long.toString(subscription.getKey()));
                }
            }
        }

        boolean directLogin = subscription.getProduct().getTechnicalProduct()
                .getAccessType() == ServiceAccessType.DIRECT;
        if (!directLogin) {
            verifyParameterNamedUser(subscription);
        }

        // Call tenant provisioning to create instance! Afterwards the
        // instance is started as well
        TenantProvisioningResult provisioningResult = tenantProvisioning
                .createProductInstance(subscription);

        if (provisioningResult.isAsyncProvisioning()) {
            return provisioningResult;
        }
        PriceModel pm = subscription.getProduct().getPriceModel();
        pm.setProvisioningCompleted(true);
        subscription.setActivationDate(subscription.getCreationDate());
        activateSubscriptionFirstTime(subscription);
        String instanceId = provisioningResult.getProductInstanceId();
        subscription.setProductInstanceId(instanceId);
        subscription.setAccessInfo(provisioningResult.getAccessInfo());
        subscription.setBaseURL(provisioningResult.getBaseUrl());
        subscription.setLoginPath(provisioningResult.getLoginPath());

        // inform the product about the users
        try {
            informProductAboutNewUsers(subscription, addedUsers);
        } catch (SubscriptionStateException e) {
            // should never be reached because state is set to
            // active
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_INFORM_PRODUCT_ABOUT_NEW_USER_FAILED);
        }

        // Send an email to the organization admin. Is performed
        // after the save operation to ensure everything worked fine and
        // no mail is sent if storing fails.
        sendToSubscriptionAddedMail(subscription, addedUserLicenses);

        return provisioningResult;
    }

    /**
     * Send the subscription created email to the users assigned to the
     * subscription. The mail also includes the access information.
     * 
     * @param subscription
     *            the created subscription to get the id and access info (url or
     *            descriptive text) from
     * @param usageLicenses
     *            the usage licenses of the users added to the subscription
     */
    private void sendToSubscriptionAddedMail(Subscription subscription,
            List<UsageLicense> usageLicenses) {

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            return;
        }
        EmailType emailType = useAccessInfo(subscription)
                ? EmailType.SUBSCRIPTION_USER_ADDED_ACCESSTYPE_DIRECT
                : EmailType.SUBSCRIPTION_USER_ADDED;

        Long marketplaceKey = null;
        if (subscription.getMarketplace() != null) {
            marketplaceKey = Long
                    .valueOf(subscription.getMarketplace().getKey());
        }

        SendMailPayload payload = new SendMailPayload();
        for (UsageLicense usageLicense : usageLicenses) {
            String accessInfo = getAccessInfo(subscription,
                    usageLicense.getUser());

            if (isUsableAWSAccessInfo(accessInfo)) {
                payload.addMailObjectForUser(usageLicense.getUser().getKey(),
                        EmailType.SUBSCRIPTION_USER_ADDED_ACCESSINFO,
                        new Object[] { subscription.getSubscriptionId(),
                                getPublicDNS(accessInfo),
                                getIPAddress(accessInfo),
                                getKeyPairName(accessInfo) },
                        marketplaceKey);
            } else {
                payload.addMailObjectForUser(usageLicense.getUser().getKey(),
                        emailType, new Object[] {
                                subscription.getSubscriptionId(), accessInfo },
                        marketplaceKey);
            }
        }
        TaskMessage message = new TaskMessage(SendMailHandler.class, payload);
        tqs.sendAllMessages(Collections.singletonList(message));
    }

    private boolean useAccessInfo(Subscription subscription) {
        ServiceAccessType accessType = subscription.getProduct()
                .getTechnicalProduct().getAccessType();
        return accessType == ServiceAccessType.DIRECT
                || accessType == ServiceAccessType.USER;
    }

    String getAccessInfo(Subscription subscription, PlatformUser user) {
        String accessInfo;
        if (useAccessInfo(subscription)) {
            accessInfo = subscription.getAccessInfo();
            if (accessInfo == null) {
                accessInfo = localizer.getLocalizedTextFromDatabase(
                        user.getLocale(),
                        subscription.getProduct().getTechnicalProduct()
                                .getKey(),
                        LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
                if (accessInfo != null) {
                    accessInfo = accessInfo.replaceAll("<p>", "\n");
                    accessInfo = accessInfo.replaceAll("</p>", "\n");
                }
            }
        } else {
            accessInfo = getSubscriptionUrl(subscription);
        }
        if (accessInfo == null) {
            accessInfo = "";
        }
        return accessInfo;
    }

    /**
     * Gets the URL to access the subscription.
     * 
     * @param subscription
     *            the subscription for which we want to know the URL
     * @return the URL to access the subscription
     */
    String getSubscriptionUrl(Subscription subscription) {
        StringBuilder url = new StringBuilder();
        String baseUrl = cfgService.getBaseURL();
        String technicalProductBaseUrl = subscription.getProduct()
                .getTechnicalProduct().getBaseURL();

        if (ADMValidator.isHttpsScheme(technicalProductBaseUrl)) {
            baseUrl = cfgService
                    .getConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                            Configuration.GLOBAL_CONTEXT)
                    .getValue();
        }

        url.append(baseUrl);
        if (url.length() == 0 || url.charAt(url.length() - 1) != '/') {
            url.append('/');
        }
        url.append("opt/");
        url.append(Long.toHexString(subscription.getKey()));
        url.append('/');
        return url.toString();
    }

    /**
     * Creates the 'allowing on behalf acting reference' between the technology
     * provider and the customer's organization.
     * 
     * @throws NonUniqueBusinessKeyException
     */
    private void createAllowOnBehalfActingReference(Subscription subscription)
            throws NonUniqueBusinessKeyException {

        // fetch the technical product
        TechnicalProduct technicalProduct = subscription.getProduct()
                .getTechnicalProduct();

        // if allow on behalf acting is true
        if (technicalProduct.isAllowingOnBehalfActing()) {

            // get source (techn. prov.) and target (supplier) organization
            Organization source = technicalProduct.getOrganization();
            Organization target = dataManager.getCurrentUser()
                    .getOrganization();

            // check if a reference does not already exist
            if (!isOnBehalfReferenceExisting(source, target)) {
                // create and persist reference
                OrganizationReference reference = new OrganizationReference(
                        source, target,
                        OrganizationReferenceType.ON_BEHALF_ACTING);
                dataManager.persist(reference);
            }
        }
    }

    private boolean isOnBehalfReferenceExisting(Organization sourceOrganization,
            Organization targetOrganization) {
        return getOrganizationReference(sourceOrganization,
                targetOrganization) != null;
    }

    private OrganizationReference getOrganizationReference(
            Organization sourceOrganization, Organization targetOrganization) {
        for (OrganizationReference reference : targetOrganization
                .getSourcesForType(
                        OrganizationReferenceType.ON_BEHALF_ACTING)) {
            if (reference.getSource().getKey() == sourceOrganization.getKey()
                    && reference.getTargetKey() == targetOrganization
                            .getKey()) {
                return reference;
            }
        }
        return null;
    }

    private void sendSubscriptionCreatedMailToAdministrators(
            Subscription subscription, boolean actingOnBehalf) {

        EmailType emailType = actingOnBehalf
                ? EmailType.SUBSCRIPTION_CREATED_ON_BEHALF_ACTING
                : EmailType.SUBSCRIPTION_CREATED;

        Long marketplaceKey = null;
        if (subscription.getMarketplace() != null) {
            marketplaceKey = Long
                    .valueOf(subscription.getMarketplace().getKey());
        }

        SendMailPayload payload = new SendMailPayload();
        List<PlatformUser> users = manageBean
                .getCustomerAdminsAndSubscriptionOwner(subscription);
        for (PlatformUser user : users) {
            payload.addMailObjectForUser(user.getKey(), emailType,
                    new Object[] { subscription.getSubscriptionId() },
                    marketplaceKey);
        }
        TaskMessage message = new TaskMessage(SendMailHandler.class, payload);
        tqs.sendAllMessages(Collections.singletonList(message));
    }

    /**
     * Validates that subscribing to the given product is possible with the
     * specified subscription data.
     * 
     * @param subscription
     *            The subscription to be created.
     * @param product
     *            The product to subscribe to.
     * @param voBillingContact
     * @throws ValidationException
     *             Thrown in case the validation of the subscription failed.
     * @throws ObjectNotFoundException
     *             Thrown in case the product could not be found.
     * @throws OperationNotPermittedException
     *             Thrown in case the user is not permitted to perform this
     *             operation.
     * @throws ServiceChangedException
     *             Thrown in case the product has been modified in the meantime.
     * @throws PriceModelException
     *             Thrown in case the product is not useable as it has no price
     *             model.
     * @throws PaymentInformationException
     *             Thrown in case the product is chargeable but the customer
     *             does not have a payment information stored.
     * @throws ConcurrentModificationException
     * @throws NonUniqueBusinessKeyException
     */
    private void validateSettingsForSubscribing(VOSubscription subscription,
            VOService product, VOPaymentInfo paymentInfo,
            VOBillingContact voBillingContact)
            throws ValidationException, ObjectNotFoundException,
            OperationNotPermittedException, ServiceChangedException,
            PriceModelException, PaymentInformationException,
            ConcurrentModificationException, NonUniqueBusinessKeyException {
        String subscriptionId = subscription.getSubscriptionId();
        BLValidator.isId("subscriptionId", subscriptionId, true);
        String pon = subscription.getPurchaseOrderNumber();
        BLValidator.isDescription("purchaseOrderNumber", pon, false);

        Product productTemplate = dataManager.getReference(Product.class,
                product.getKey());
        // check product and org settings
        Organization targetCustomer = productTemplate.getTargetCustomer();
        PlatformUser currentUser = dataManager.getCurrentUser();
        Organization organization = currentUser.getOrganization();

        if (targetCustomer == null) {
            // if it is no customer specific product, check if we have one
            // for the subscriber
            List<Product> resultList = getProductDao()
                    .getCopyForCustomer(productTemplate, organization);
            if (resultList.size() > 0) {
                ServiceChangedException sce = new ServiceChangedException(
                        ServiceChangedException.Reason.SERVICE_MODIFIED);
                LOG.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, sce,
                        LogMessageIdentifier.WARN_CUSTOMER_MUST_SUBSCRIBE_SPECIFIC_PRODUCT,
                        organization.getOrganizationId(),
                        productTemplate.getProductId(),
                        resultList.get(0).getProductId());
                throw sce;
            }
        } else if (organization.getKey() != targetCustomer.getKey()) {
            // if it is a specific one but not specified for the subscriber
            String message = String.format(
                    "Customer specific product '%s' is not specified for customer '%s'.",
                    productTemplate.getProductId(),
                    organization.getOrganizationId());
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    message);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_CUSTOMER_SPECIFIC_PRODUCT_NOT_FOR_THE_CUSTOMER,
                    productTemplate.getProductId(),
                    organization.getOrganizationId());
            throw onp;
        }

        checkIfProductIsUptodate(productTemplate, product);

        // now check the price model related to the product; if it is
        // chargeable, the organization must have specified a payment
        // information. If he has not, throw an exception
        PriceModel priceModel = productTemplate.getPriceModel();
        if (priceModel == null
                && productTemplate.getType() == ServiceType.PARTNER_TEMPLATE) {
            priceModel = productTemplate.getTemplate().getPriceModel();
        }

        if (priceModel == null) {
            PriceModelException mpme = new PriceModelException(
                    PriceModelException.Reason.NOT_DEFINED);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, mpme,
                    LogMessageIdentifier.WARN_SUBSCRIBE_PRODUCT_FAILED,
                    Long.toString(productTemplate.getKey()));
            throw mpme;
        }

        if (priceModel.isChargeable() && !isPaymentInfoHidden()) {
            PaymentDataValidator.validateNotNull(paymentInfo, voBillingContact);
            PaymentInfo pi = dataManager.getReference(PaymentInfo.class,
                    paymentInfo.getKey());
            BillingContact bc = dataManager.getReference(BillingContact.class,
                    voBillingContact.getKey());
            validatePaymentInfoAndBillingContact(pi, bc, paymentInfo,
                    voBillingContact);
            PermissionCheck.owns(pi, organization, LOG);
            PermissionCheck.owns(bc, organization, LOG);

            PaymentDataValidator.validatePaymentTypeSupportedBySupplier(
                    organization, productTemplate, pi.getPaymentType());
            PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
        }

    }

    UserGroup getUnit(long unitKey, String unitName, long organizationKey)
            throws ObjectNotFoundException {
        UserGroup unit = null;
        if (unitKey == 0L) {
            if (unitName == null || unitName.trim().length() == 0) {
                return null;
            }
            unit = new UserGroup();
            unit.setName(unitName);
            unit.setOrganization_tkey(organizationKey);
            unit = (UserGroup) dataManager.getReferenceByBusinessKey(unit);
        } else {
            try {
                unit = dataManager.getReference(UserGroup.class, unitKey);
                if (unit.getOrganization_tkey() != organizationKey) {
                    throw new ObjectNotFoundException(
                            "The unit does not belong to your organization.");
                }
            } catch (ObjectNotFoundException e) {
                e.setMessageParams(new String[] { unitName });
                throw e;
            }
        }
        return unit;
    }

    private void checkIfServiceAvailable(long productKey, String productId,
            PlatformUser currentUser)
            throws OperationNotPermittedException, ObjectNotFoundException {
        if (currentUser.isOrganizationAdmin()) {
            return;
        }
        List<Long> invisibleProductKeys = userGroupService
                .getInvisibleProductKeysForUser(currentUser.getKey());
        if (invisibleProductKeys.contains(Long.valueOf(productKey))) {
            String message = String.format("Service '%s' is not avalible.",
                    productId);
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    message);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_SERVICE_NOT_AVAILABLE, productId);
            throw onp;
        }
    }

    /**
     * Checks if there already exists a subscription to the technical service to
     * which the specified VOService belongs to.
     * 
     * @param product
     *            the VOService for which to check if already has active
     *            subscriptions.
     */
    private void checkIfSubscriptionAlreadyExists(Product product)
            throws SubscriptionAlreadyExistsException, ObjectNotFoundException {

        // Fetch the technical product to which the defined product belongs to.
        TechnicalProduct technicalProduct = product.getTechnicalProduct();

        // Only in case one subscription is allowed check the number of already
        // existing subscriptions.
        if (technicalProduct.isOnlyOneSubscriptionAllowed()) {
            Organization organization = dataManager.getCurrentUser()
                    .getOrganization();

            Long numberOfSubscriptions = getSubscriptionDao()
                    .getNumberOfVisibleSubscriptions(technicalProduct,
                            organization);
            // If there are already subscriptions to the technical product
            // based on the product, throw an exception.
            if (numberOfSubscriptions.longValue() > 0) {

                Object[] params = new Object[] { product.getProductId() };

                SubscriptionAlreadyExistsException subAlreadyExistsException = new SubscriptionAlreadyExistsException(
                        params);
                LOG.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        subAlreadyExistsException,
                        LogMessageIdentifier.WARN_USER_SUBSCRIBE_SERVICE_FAILED_ONLY_ONE_ALLOWED,
                        Long.toString(dataManager.getCurrentUser().getKey()),
                        Long.toString(product.getKey()),
                        Long.toString(organization.getKey()));
                throw subAlreadyExistsException;
            }
        }
    }

    /**
     * Checks if there already exists a subscription with the same subscriptions
     * name for this Organization.
     * 
     * @param currentUser
     *            current platform user
     * @param voSubscription
     *            subscription
     * @throws NonUniqueBusinessKeyException
     */
    private void verifyIdAndKeyUniqueness(PlatformUser currentUser,
            VOSubscription voSubscription)
            throws NonUniqueBusinessKeyException {
        Subscription newSubscription = new Subscription();
        newSubscription
                .setOrganizationKey(currentUser.getOrganization().getKey());
        newSubscription.setSubscriptionId(voSubscription.getSubscriptionId());
        dataManager.validateBusinessKeyUniqueness(newSubscription);
    }

    /**
     * Its responsible for updating the values for the subscribed product from
     * the parameter list.
     * 
     * @param product
     *            the product to update
     * @param parameters
     *            the list of parameters
     * @param subscription
     *            - target subscription
     */
    List<Parameter> updateConfiguredParameterValues(Product product,
            List<VOParameter> parameters, Subscription subscription) {
        Map<String, Parameter> paramMap = new HashMap<>();
        if (product.getParameterSet() != null) {
            for (Parameter parameter : product.getParameterSet()
                    .getParameters()) {
                paramMap.put(
                        parameter.getParameterDefinition().getParameterId(),
                        parameter);
            }
        }

        // reload all the parameter values from old subscription
        if (subscription != null && subscription.getParameterSet() != null
                && !subscription.getProduct().equals(product)) {
            List<Parameter> params = subscription.getParameterSet()
                    .getParameters();
            if (params != null && params.size() > 0) {
                for (Parameter param : params) {
                    String parameterId = param.getParameterDefinition()
                            .getParameterId();
                    Parameter uParam = paramMap.get(parameterId);
                    if (uParam != null) {
                        uParam.setValue(param.getValue());
                    }
                }
            }
        }

        List<Parameter> modifiedParametesForLog = new ArrayList<>();

        for (VOParameter voParameter : parameters) {
            String parameterID = voParameter.getParameterDefinition()
                    .getParameterId();
            Parameter param = paramMap.get(parameterID);
            if (param != null) {
                String oldValue = param.getValue();
                param.setValue(voParameter.getValue());

                String defaultValue = param.getParameterDefinition()
                        .getDefaultValue();
                if ((oldValue != null && !oldValue.equals(param.getValue()))
                        || (oldValue == null && param.getValue() != null
                                && !param.getValue().equals(defaultValue))) {
                    modifiedParametesForLog.add(param);
                }
            }
        }
        dataManager.flush();
        return modifiedParametesForLog;
    }

    /**
     * If a product with a customer specific price model, was copied the
     * subscription specific one would refer to the template's one when getting
     * localized values - so we have to copy those ones from the original price
     * model
     * 
     * @param targetProduct
     * @param sourceProduct
     */
    private void copyLocalizedPricemodelValues(Product targetProduct,
            Product sourceProduct) {
        List<VOLocalizedText> localizedValues;
        final PriceModel priceModelTarget = targetProduct.getPriceModel();
        final PriceModel priceModelSource = sourceProduct
                .getType() == ServiceType.PARTNER_TEMPLATE
                        ? sourceProduct.getTemplate().getPriceModel()
                        : sourceProduct.getPriceModel();
        if ((priceModelTarget != null) && (priceModelSource != null)) {
            long targetKey = targetProduct.getPriceModel().getKey();
            long sourceKey = priceModelSource.getKey();
            localizedValues = localizer.getLocalizedValues(sourceKey,
                    LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            if (localizedValues != null && !localizedValues.isEmpty()) {
                localizer.storeLocalizedResources(targetKey,
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                        localizedValues);
            }
            // license copy
            if (sourceProduct.getVendor().getGrantedRoleTypes()
                    .contains(OrganizationRoleType.RESELLER)) {
                localizedValues = localizer.getLocalizedValues(
                        sourceProduct.getKey(),
                        LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE);
            } else {
                localizedValues = localizer.getLocalizedValues(sourceKey,
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);
            }
            if (localizedValues != null && !localizedValues.isEmpty()) {
                localizer.storeLocalizedResources(targetKey,
                        LocalizedObjectTypes.PRICEMODEL_LICENSE,
                        localizedValues);
            }
        }
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public boolean addRevokeUser(String subscriptionId,
            List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException, OperationPendingException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        Subscription sub = manageBean.checkSubscriptionOwner(subscriptionId, 0);

        stateValidator.checkAddRevokeUserAllowed(sub);
        // validation
        if (usersToBeAdded != null) {
            for (VOUsageLicense lic : usersToBeAdded) {
                try {
                    PlatformUser user = dataManager.getReference(
                            PlatformUser.class, lic.getUser().getKey());
                    getAndCheckServiceRole(lic, sub.getProduct());
                    // fill user ID for trigger process
                    lic.getUser().setUserId(user.getUserId());
                } catch (ObjectNotFoundException e) {
                    throw new ObjectNotFoundException(
                            DomainObjectException.ClassEnum.USER,
                            String.valueOf(lic.getUser().getUserId()));
                }
            }
        }
        if (usersToBeRevoked != null) {
            for (VOUser entry : usersToBeRevoked) {
                try {
                    PlatformUser user = dataManager
                            .getReference(PlatformUser.class, entry.getKey());
                    // fill user ID for trigger process
                    entry.setUserId(user.getUserId());
                } catch (ObjectNotFoundException e) {
                    throw new ObjectNotFoundException(
                            DomainObjectException.ClassEnum.USER,
                            String.valueOf(entry.getUserId()));
                }
            }
        }
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        List<TriggerProcessIdentifier> pendingAddRevokeUsers = validator
                .getPendingAddRevokeUsers(subscriptionId, usersToBeAdded,
                        usersToBeRevoked);
        if (!pendingAddRevokeUsers.isEmpty()) {
            String userIds = determineUserIds(pendingAddRevokeUsers);
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request for subscription '%s' to add or revoke the users: %s",
                            subscriptionId, userIds),
                    ReasonEnum.ADD_REVOKE_USER,
                    new Object[] { subscriptionId, userIds });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_ADD_REVOKE_USER_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subscriptionId);
            throw ope;
        }
        validateTriggerProcessForSubscription(sub);

        // now send a suspending message for the processing
        TriggerMessage message = new TriggerMessage(
                TriggerType.ADD_REVOKE_USER);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess proc = list.get(0).getTrigger();
        proc.addTriggerProcessParameter(TriggerProcessParameterName.OBJECT_ID,
                subscriptionId);
        proc.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subscriptionId);
        proc.addTriggerProcessParameter(
                TriggerProcessParameterName.USERS_TO_ADD, usersToBeAdded);
        proc.addTriggerProcessParameter(
                TriggerProcessParameterName.USERS_TO_REVOKE, usersToBeRevoked);

        try {
            // if processing is not suspended, call finishing method
            TriggerDefinition triggerDefinition = proc.getTriggerDefinition();
            if (triggerDefinition == null) {
                addRevokeUserInt(proc);
                return true;
            } else if (triggerDefinition.isSuspendProcess()) {
                proc.setTriggerProcessIdentifiers(TriggerProcessIdentifiers
                        .createAddRevokeUser(dataManager,
                                TriggerType.ADD_REVOKE_USER, subscriptionId,
                                usersToBeAdded, usersToBeRevoked));
                dataManager.merge(proc);
            }
        } catch (ObjectNotFoundException | SubscriptionStateException
                | TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return false;
    }

    void validateTriggerProcessForSubscription(Subscription subscription)
            throws OperationPendingException {
        validateTriggerProcessForUnsubscribeFromService(
                subscription.getSubscriptionId());
        validateTriggerProcessForUpgradeSubscriptionBySubscriptionKey(
                subscription.getKey(), subscription.getSubscriptionId());
        validateTriggerProcessForModifySubscriptionBySubscriptionKey(
                subscription.getKey(), subscription.getSubscriptionId());
    }

    /**
     * Determines the user identifiers that are contained in the passed in list
     * of process identifiers.
     * 
     * @param pendingAddRevokeUsers
     *            The list of process identifiers to check.
     * @return The user identifiers.
     */
    private String determineUserIds(
            List<TriggerProcessIdentifier> pendingAddRevokeUsers) {
        StringBuilder userIds = new StringBuilder();
        for (TriggerProcessIdentifier id : pendingAddRevokeUsers) {
            if (userIds.length() != 0) {
                userIds.append(", ");
            }
            if (TriggerProcessIdentifierName.USER_TO_ADD.equals(id.getName())
                    || TriggerProcessIdentifierName.USER_TO_REVOKE
                            .equals(id.getName())) {
                userIds.append(id.getValue());
            }
        }
        return userIds.toString();
    }

    /**
     * Tries to read the service role that should be assigned (if not
     * <code>null</code>). If the role exists, it will be checked if it belongs
     * to the product's technical product.
     * 
     * @param lic
     *            the {@link VOUsageLicense}
     * @param prod
     *            the {@link Product}
     * @return <code>null</code> if no service roles are defined on the
     *         technical product and the specified one on the
     *         {@link VOUsageLicense} is <code>null</code>. Otherwise the read
     *         role will be returned if valid
     * @throws ObjectNotFoundException
     *             in case the role wasn't found.
     * @throws OperationNotPermittedException
     *             in case the read role doesn't belong to the subscriptions
     *             technical product or no role is specified when service roles
     *             have to be used on the technical product
     */
    private RoleDefinition getAndCheckServiceRole(VOUsageLicense lic,
            Product prod)
            throws ObjectNotFoundException, OperationNotPermittedException {
        TechnicalProduct tp = prod.getTechnicalProduct();
        List<RoleDefinition> roles = tp.getRoleDefinitions();
        if (roles == null || roles.isEmpty()) {
            lic.setRoleDefinition(null);
            return null;
        }
        if (lic.getRoleDefinition() == null) {
            String message = "User assignment to technical service '%s' without service role not possible.";
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    String.format(message, Long.valueOf(tp.getKey())));
            LOG.logError(Log4jLogger.SYSTEM_LOG, onp,
                    LogMessageIdentifier.ERROR_USER_ASSIGNMENT_TO_TECHNICAL_SERVICE_FAILED_NO_SERVICE_ROLE,
                    Long.toString(tp.getKey()));
            throw onp;
        }
        RoleDefinition role = dataManager.getReference(RoleDefinition.class,
                lic.getRoleDefinition().getKey());
        for (RoleDefinition roleDefinition : roles) {
            if (role.getKey() == roleDefinition.getKey()) {
                return role;
            }
        }
        String message = "Role '%s' is not defined on technical service '%s'";
        OperationNotPermittedException onp = new OperationNotPermittedException(
                String.format(message, Long.valueOf(role.getKey()),
                        Long.valueOf(tp.getKey())));
        LOG.logError(Log4jLogger.SYSTEM_LOG, onp,
                LogMessageIdentifier.ERROR_NO_ROLE_FOR_TECHNICAL_SERVICE,
                Long.toString(role.getKey()), Long.toString(tp.getKey()));
        throw onp;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void addRevokeUserInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException {

        TriggerProcessParameter mod;
        mod = tp.getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION);
        String subscriptionId = mod.getValue(String.class);

        mod = tp.getParamValueForName(TriggerProcessParameterName.USERS_TO_ADD);
        List<?> usersToBeAdded = mod.getValue(List.class);

        mod = tp.getParamValueForName(
                TriggerProcessParameterName.USERS_TO_REVOKE);
        List<?> usersToBeRevoked = mod.getValue(List.class);

        // Try to find the subscription
        Subscription subscription = manageBean.loadSubscription(subscriptionId,
                0);

        List<UsageLicense> addedUsersLicenses = new ArrayList<>();

        List<PlatformUser> usersToAdd = new ArrayList<>();
        // loop for adding users
        if (usersToBeAdded != null) {
            for (Object entry : usersToBeAdded) {
                final VOUsageLicense lic = (VOUsageLicense) entry;
                final PlatformUser usr = dataManager.getReference(
                        PlatformUser.class, lic.getUser().getKey());
                final RoleDefinition roleDef = getAndCheckServiceRole(lic,
                        subscription.getProduct());
                final UsageLicense usageLicenseForUser = subscription
                        .getUsageLicenseForUser(usr);
                try {
                    if (usageLicenseForUser == null) {
                        // Create a new usage License
                        final UsageLicense newUsageLicense = addUserToSubscription(
                                subscription, usr, roleDef);
                        usersToAdd.add(usr);
                        // store the new user's licenses for later mail sending
                        addedUsersLicenses.add(newUsageLicense);
                    } else {
                        // Update an existing usage license:
                        BaseAssembler.verifyVersionAndKey(usageLicenseForUser,
                                lic);
                        modifyUserRole(subscription, usr, roleDef);
                    }
                } catch (UserNotAssignedException
                        | UserAlreadyAssignedException e) {
                    // Must not happen here
                    throw new AssertionError(e);
                }
            }
        }

        // loop for revoking users
        if (usersToBeRevoked != null) {
            List<PlatformUser> platformUsers = new ArrayList<>();
            for (Object qryUsr : usersToBeRevoked) {
                PlatformUser usr = dataManager.getReference(PlatformUser.class,
                        VOUser.class.cast(qryUsr).getKey());
                platformUsers.add(usr);
            }
            revokeUserFromSubscription(subscription, platformUsers);
        }
        verifyParameterNamedUser(subscription);

        informProductAboutNewUsers(subscription, usersToAdd);
        sendToSubscriptionAddedMail(subscription, addedUsersLicenses);

        // notify all listeners
        triggerQS.sendAllNonSuspendingMessages(TriggerMessage.create(
                TriggerType.ADD_REVOKE_USER, tp.getTriggerProcessParameters(),
                dataManager.getCurrentUser().getOrganization()));

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void modifyUserRole(Subscription subscription, PlatformUser usr,
            RoleDefinition roleDef) throws SubscriptionStateException,
            UserNotAssignedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        stateValidator.checkModifyUserRoleAllowed(subscription);

        UsageLicense license = subscription.getUsageLicenseForUser(usr);
        RoleDefinition oldRoleDef = null;
        if (license != null) {
            oldRoleDef = license.getRoleDefinition();
            if (oldRoleDef == roleDef) {
                // nothing to do - role not changed; avoid sending of mail and
                // notification of service
                return;
            }
        }
        UsageLicense targetLicense = subscription.changeRole(usr, roleDef);

        // Inform application about changed role
        if (canModifyApplicationUsers(subscription) && targetLicense != null) {
            appManager.updateUsers(subscription,
                    Collections.singletonList(targetLicense));

            // log deassign user role for service
            audit.deassignUserRoleForService(dataManager, subscription, usr,
                    oldRoleDef);
            // log assign user role for service
            audit.assignUserRoleForService(dataManager, subscription, usr,
                    roleDef);
        }
        try {
            String accessInfo = targetLicense == null ? ""
                    : getAccessInfo(subscription, targetLicense.getUser());

            commService.sendMail(usr,
                    EmailType.SUBSCRIPTION_ACCESS_GRANTED, new Object[] {
                            subscription.getSubscriptionId(), accessInfo },
                    subscription.getMarketplace());

        } catch (MailOperationException e) {
            // only log the exception and proceed
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_GRANT_ROLE_IN_SUBSCRIPTION_CONFIRMING_FAILED);
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public UsageLicense addUserToSubscription(Subscription subscription,
            PlatformUser user, RoleDefinition serviceRole)
            throws UserAlreadyAssignedException {

        final UsageLicense usageLicense = subscription.addUser(user,
                serviceRole);

        audit.assignUserToSubscription(dataManager, subscription, usageLicense);
        return usageLicense;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void revokeUserFromSubscription(final Subscription subscription,
            final List<PlatformUser> users) throws SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        stateValidator.checkAddRevokeUserAllowed(subscription);
        revokeUserFromSubscriptionInt(subscription, users);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void revokeUserFromSubscriptionInt(final Subscription subscription,
            final List<PlatformUser> users)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        List<UsageLicense> usageLicenses = doRevokeUserFromSubscriptionInt(
                subscription, users);
        audit.deassignUserFromSubscription(dataManager, subscription,
                usageLicenses);
    }

    /**
     * Worker method for revokeUserFromSubscription, allows to ignore
     * lastUser-Check (only for use in unsubscribe)
     * 
     * @param subscription
     *            The subscription to be changed.
     * @param users
     *            The users to be revoked from the subscription.
     * @return the list of usage licenses
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    private List<UsageLicense> doRevokeUserFromSubscriptionInt(
            Subscription subscription, List<PlatformUser> users)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        List<UsageLicense> licenseList = new ArrayList<>();

        for (PlatformUser user : users) {
            UsageLicense license = subscription.revokeUser(user);

            // if the subscription is invalid (e.g. instance creation
            // failed) no call to the application is performed
            if (isValidSubscription(subscription) && license != null) {
                licenseList.add(license);
            }
        }

        // Bug 9998. while the subscription is still pending, no call to
        // ProvisioningService.deleteUsers(String, List<User>) has to be
        // performed.
        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            appManager.deleteUsers(subscription, licenseList);
        }

        for (UsageLicense license : licenseList) {
            // explicitly remove the user's usage license
            if (license != null) {
                dataManager.remove(license);
            }
            dataManager.flush();
        }
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE
                && subscription.getProduct().getTechnicalProduct()
                        .getAccessType() != ServiceAccessType.DIRECT) {
            // as the user gets no mail when being added to a pending
            // subscription, the notification about the removal will also not be
            // sent.
            Long marketplaceKey = null;
            if (subscription.getMarketplace() != null) {
                marketplaceKey = Long
                        .valueOf(subscription.getMarketplace().getKey());
            }

            SendMailPayload payload = new SendMailPayload();
            for (PlatformUser user : users) {
                payload.addMailObjectForUser(user.getKey(),
                        EmailType.SUBSCRIPTION_USER_REMOVED,
                        new Object[] { subscription.getSubscriptionId() },
                        marketplaceKey);
            }
            TaskMessage message = new TaskMessage(SendMailHandler.class,
                    payload);
            tqs.sendAllMessages(Collections.singletonList(message));
        }
        return licenseList;
    }

    private boolean isValidSubscription(Subscription subscription) {
        return subscription.getStatus() != SubscriptionStatus.INVALID;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Subscription> getSubscriptionsForUserInt(PlatformUser user) {
        return getSubscriptionDao().getSubscriptionsForUser(user);
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public List<VOUserSubscription> getSubscriptionsForUser(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException {
        return getSubscriptionsForUser(user, PerformanceHint.ALL_FIELDS);
    }

    @RolesAllowed("ORGANIZATION_ADMIN")
    public List<VOUserSubscription> getSubscriptionsForUser(VOUser user,
            PerformanceHint performanceHint)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("user", user);

        PlatformUser platformUser = idManager.getPlatformUser(user.getUserId(),
                dataManager.getCurrentUser().getTenantId(), true);
        LocalizerFacade facade = new LocalizerFacade(localizer,
                platformUser.getLocale());
        List<Subscription> subs = getSubscriptionsForUserInt(platformUser);
        ArrayList<VOUserSubscription> result = new ArrayList<>();
        for (Subscription sub : subs) {
            VOUserSubscription voSub = SubscriptionAssembler
                    .toVOUserSubscription(sub, platformUser, facade,
                            performanceHint);
            result.add(voSub);
        }
        return result;
    }

    @Override
    public List<VOUserSubscription> getSubscriptionsForCurrentUser() {
        ArrayList<VOUserSubscription> result = new ArrayList<>();
        PlatformUser user = dataManager.getCurrentUser();
        List<Subscription> subs = getSubscriptionsForUserInt(user);
        LocalizerFacade facade = new LocalizerFacade(localizer,
                user.getLocale());
        SubscriptionAssembler.prefetchData(subs, facade);
        for (Subscription sub : subs) {
            VOUserSubscription voSub = SubscriptionAssembler
                    .toVOUserSubscription(sub, user, facade);
            result.add(voSub);
        }
        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public List<VOSubscription> getSubscriptionsForOrganization() {
        return getSubscriptionsForOrganizationWithFilter(null);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public List<VOSubscription> getSubscriptionsForOrganization(
            PerformanceHint performanceHint) {
        return getSubscriptionsForOrganizationWithFilter(null, performanceHint);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOSubscription> getAllSubscriptionsForOrganization(
            PerformanceHint performanceHint) {
        ArrayList<VOSubscription> result = new ArrayList<>();
        List<Subscription> subscriptions = subscriptionListService
                .getAllSubscriptionsForOrganization();
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        for (Subscription sub : subscriptions) {
            VOSubscription voSub = SubscriptionAssembler.toVOSubscription(sub,
                    facade, performanceHint);
            result.add(voSub);
        }
        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus) {
        return getSubscriptionsForOrganizationWithFilter(requiredStatus,
                PerformanceHint.ALL_FIELDS);
    }

    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus,
            PerformanceHint performanceHint) {
        ArrayList<VOSubscription> result = new ArrayList<>();

        // load all subscriptions
        List<Subscription> subs = subscriptionListService
                .getSubscriptionsForOrganization(requiredStatus);

        // create transfer objects
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        for (Subscription sub : subs) {
            VOSubscription voSub = SubscriptionAssembler.toVOSubscription(sub,
                    facade, performanceHint);
            result.add(voSub);
        }

        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public boolean validateSubscriptionIdForOrganization(
            String subscriptionId) {
        // load all subscriptions
        List<Subscription> subs = subscriptionListService
                .getSubscriptionsForOrganization(null);

        boolean subscriptionIdAlreadyExists = false;
        for (Subscription sub : subs) {
            if (sub.getSubscriptionId().equals(subscriptionId)) {
                subscriptionIdAlreadyExists = true;
                break;
            }
        }
        return subscriptionIdAlreadyExists;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscriptionDetails getSubscriptionDetails(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        Subscription subscription = manageBean
                .checkSubscriptionOwner(subscriptionId, 0);

        return getSubscriptionDetails(subscription);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscriptionDetails getSubscriptionDetails(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {

        Subscription subscription = manageBean.checkSubscriptionOwner(null,
                subscriptionKey);

        return getSubscriptionDetails(subscription);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscriptionDetails getSubscriptionDetailsWithoutOwnerCheck(
            long subscriptionKey) throws ObjectNotFoundException {
        Subscription subscription = manageBean.loadSubscription(null,
                subscriptionKey);
        return getSubscriptionDetails(subscription);
    }

    private VOSubscriptionDetails getSubscriptionDetails(
            Subscription subscription) {
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());

        return SubscriptionAssembler.toVOSubscriptionDetails(subscription,
                facade);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public boolean unsubscribeFromService(String subscriptionId)
            throws ObjectNotFoundException, SubscriptionStillActiveException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationPendingException,
            OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        manageBean.checkSubscriptionOwner(subscriptionId, 0);

        // Find the subscription and validate if operation can be performed
        validateSubsciptionForUnsubscribe(subscriptionId);

        validateTriggerProcessForUnsubscribeFromService(subscriptionId);

        TriggerProcess triggerProcess = createTriggerProcessForUnsubscribeFromService(
                subscriptionId);

        TriggerDefinition triggerDefinition = triggerProcess
                .getTriggerDefinition();
        if (triggerDefinition == null) {
            try {
                unsubscribeFromServiceInt(triggerProcess);
                return true;
            } catch (ObjectNotFoundException | SubscriptionStillActiveException
                    | SubscriptionStateException
                    | TechnicalServiceNotAliveException
                    | TechnicalServiceOperationException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            triggerProcess.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createUnsubscribeFromService(
                            dataManager, TriggerType.UNSUBSCRIBE_FROM_SERVICE,
                            subscriptionId));
            dataManager.merge(triggerProcess);
        }

        return false;
    }

    /**
     * @param subscriptionId
     * @return
     */
    private TriggerProcess createTriggerProcessForUnsubscribeFromService(
            String subscriptionId) {
        TriggerMessage message = new TriggerMessage(
                TriggerType.UNSUBSCRIBE_FROM_SERVICE);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess triggerProcess = list.get(0).getTrigger();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.OBJECT_ID, subscriptionId);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subscriptionId);
        return triggerProcess;
    }

    private void validateTriggerProcessForUnsubscribeFromService(
            String subscriptionId) throws OperationPendingException {
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        if (validator.isSubscribeOrUnsubscribeServicePending(subscriptionId)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to unsubscribe from the subscription or create a subscription with identifier '%s'",
                            subscriptionId),
                    ReasonEnum.UNSUBSCRIBE_FROM_SERVICE,
                    new Object[] { subscriptionId });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_UNSUBSCRIBE_FROM_SERVICE_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subscriptionId);
            throw ope;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void unsubscribeFromServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, SubscriptionStateException,
            SubscriptionStillActiveException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        String subId = tp
                .getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(String.class);
        Subscription subscription = validateSubsciptionForUnsubscribe(subId);

        stopInstance(subscription);
        unsubscribe(subscription);

        manageBean.removeUsageLicenses(subscription);

        operationRecordBean
                .removeOperationsForSubscription(subscription.getKey());

        // rename the subscription as last step because id is still used for
        // exceptions and mails rename to allow the reuse of the id
        final String oldSubscriptionId = subscription.getSubscriptionId();
        subscription
                .setSubscriptionId(String.valueOf(System.currentTimeMillis()));
        subscription.setEventPublished(false);

        boolean removed = removeOnBehalfActingReference(subscription);

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.UNSUBSCRIBE_FROM_SERVICE,
                        tp.getTriggerProcessParameters(), dataManager
                                .getCurrentUser().getOrganization()));

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                buildNotification(oldSubscriptionId));

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.SUBSCRIPTION_TERMINATION,
                        tp.getTriggerProcessParameters(),
                        subscription.getProduct().getVendor()));

        sendConfirmDeactivationEmail(removed, oldSubscriptionId, subscription);

    }

    void unsubscribe(Subscription subscription) {
        // delete product
        final Product product = subscription.getProduct();
        product.setStatus(ServiceStatus.DELETED);
        final Product asyncTempProduct = subscription.getAsyncTempProduct();
        if (asyncTempProduct != null) {
            subscription.setAsyncTempProduct(null);
            dataManager.remove(asyncTempProduct);
        }
        modUpgBean.deleteModifiedEntityForSubscription(subscription);

        // deactivate subscription
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        long txTime = DateFactory.getInstance().getTransactionTime();
        subscription.setDeactivationDate(Long.valueOf(txTime));

        // log
        audit.unsubscribeFromService(dataManager, subscription);
    }

    /**
     * @param subscription
     */
    private void stopInstance(Subscription subscription) {
        boolean stopInstance = subscription
                .getStatus() != SubscriptionStatus.INVALID;
        if (stopInstance) {
            try {
                appManager.deleteInstance(subscription);
            } catch (SaaSApplicationException e) {
                LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_UNSUBSCRIBE_SUBSCRIPTION_FAILED,
                        Long.toString(subscription.getKey()));
                manageBean.sendTechnicalServiceErrorMail(subscription);
            }
        }
    }

    private VONotification buildNotification(String subscriptionId) {
        VONotificationBuilder builder = new VONotificationBuilder();
        return builder.addParameter(VOProperty.SUBSCRIPTION_SUBSCRIPTION_ID,
                subscriptionId).build();
    }

    private boolean removeOnBehalfActingReference(Subscription subscription) {
        // execute isLastSubscription named query
        Long isLastSubscription = getSubscriptionDao()
                .hasSubscriptionsBasedOnOnBehalfServicesForTp(subscription);
        // if is last subscription...
        if (isLastSubscription.longValue() == 0) {
            // search for the reference...
            Organization sourceOrganization = subscription.getProduct()
                    .getTechnicalProduct().getOrganization();
            Organization targetOrganization = subscription.getOrganization();
            OrganizationReference reference = getOrganizationReference(
                    sourceOrganization, targetOrganization);

            // if found remove it
            if (reference != null) {
                removeTemporaryOnbehalfUsers(sourceOrganization,
                        targetOrganization);
                dataManager.remove(reference);
                dataManager.flush();
                return true;
            }
        }
        return false;
    }

    private void removeTemporaryOnbehalfUsers(Organization sourceOrganization,
            Organization targetOrganization) {
        for (PlatformUser onbehalfUser : sourceOrganization
                .getOnBehalfUsersFor(targetOrganization)) {
            removeActiveSessionsForOnbehalfUser(onbehalfUser);
            OnBehalfUserReference master = onbehalfUser.getMaster();
            onbehalfUser.getOrganization().getPlatformUsers()
                    .remove(onbehalfUser);
            dataManager.remove(master);
        }
    }

    private void removeActiveSessionsForOnbehalfUser(
            PlatformUser onbehalfUser) {
        List<Session> sessions = getSessionDao()
                .getActiveSessionsForUser(onbehalfUser);
        for (Session s : sessions) {
            dataManager.remove(s);
        }

    }

    private void sendConfirmDeactivationEmail(boolean removed,
            final String oldSubscriptionId, final Subscription subscription) {

        EmailType emailType = removed
                ? EmailType.SUBSCRIPTION_DELETED_ON_BEHALF_ACTING
                : EmailType.SUBSCRIPTION_DELETED;
        Marketplace marketplace = subscription.getMarketplace();
        List<PlatformUser> users = manageBean
                .getCustomerAdminsAndSubscriptionOwner(subscription);
        for (PlatformUser user : users) {
            try {
                commService.sendMail(user, emailType,
                        new Object[] { oldSubscriptionId }, marketplace);
            } catch (MailOperationException e) {
                // only log the exception and proceed
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_DELETION_OF_SUBSCRIPTION_CONFIRMING_FAILED);
            }
        }
    }

    /**
     * Obtains the subscription and verifies that the current settings allow a
     * unsubscribe operation.
     * 
     * @param subId
     *            The subscription id to unsubscribe from.
     * @return The subscription to unsubscribe from.
     * @throws SubscriptionStateException
     *             Thrown in case the operation cannot be performed due to the
     *             current state of the subscription.
     * @throws SubscriptionStillActiveException
     *             Thrown in case the subscription is still active.
     * @throws ObjectNotFoundException
     *             Thrown in case the subscription cannot be found.
     */
    private Subscription validateSubsciptionForUnsubscribe(String subId)
            throws SubscriptionStateException, SubscriptionStillActiveException,
            ObjectNotFoundException {
        Subscription subscription = manageBean.loadSubscription(subId, 0);
        stateValidator.checkUnsubscribingAllowed(subscription);

        // Check whether there are active sessions
        List<Session> activeSessions = prodSessionMgmt
                .getProductSessionsForSubscriptionTKey(subscription.getKey());
        if (activeSessions.size() > 0) {
            // there are still active sessions, so deletion must
            // fail: throw exception
            sessionCtx.setRollbackOnly();
            SubscriptionStillActiveException ssa = new SubscriptionStillActiveException(
                    "There are still " + activeSessions.size()
                            + " sessions active for the subscription with key '"
                            + subscription.getKey() + "'",
                    SubscriptionStillActiveException.Reason.ACTIVE_SESSIONS);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ssa,
                    LogMessageIdentifier.WARN_SUBSCRIPTION_DELETION_FAILED);
            throw ssa;
        }
        return subscription;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Subscription loadSubscription(long subscriptionKey)
            throws ObjectNotFoundException {
        return dataManager.getReference(Subscription.class, subscriptionKey);
    }

    /**
     * Check if one of the platform parameter constraints are violated.
     * 
     * @param subscription
     *            the subscription
     * @param currentUser
     *            the current user
     * @throws SubscriptionMigrationException
     *             in case one of the platform parameters is violated
     */
    private void checkPlatformParameterConstraints(Subscription subscription,
            Product targetProduct, PlatformUser currentUser)
            throws SubscriptionMigrationException {
        ParameterSet paramSet = targetProduct.getParameterSet();
        if (paramSet == null || paramSet.getParameters() == null
                || paramSet.getParameters().isEmpty()) {
            return;
        }
        for (Parameter param : paramSet.getParameters()) {
            ParameterDefinition def = param.getParameterDefinition();
            if (def.getParameterType() == ParameterType.PLATFORM_PARAMETER
                    && param.getValue() != null) {
                String subscriptionId = subscription.getSubscriptionId();
                if (PlatformParameterIdentifiers.NAMED_USER
                        .equals(def.getParameterId())) {
                    int current = subscription.getUsageLicenses().size();
                    long max = param.getLongValue();
                    if (current > max) {
                        sessionCtx.setRollbackOnly();
                        SubscriptionMigrationException e = new SubscriptionMigrationException(
                                "Parameter check failed",
                                Reason.PARAMETER_USERS,
                                new Object[] { subscriptionId,
                                        String.valueOf(current),
                                        String.valueOf(max) });
                        LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                                LogMessageIdentifier.ERROR_MIGRATE_SUBSCRIPTION_AS_CHECK_PARAMETER,
                                Long.toString(currentUser.getKey()),
                                Long.toString(subscription.getKey()),
                                Long.toString(
                                        subscription.getOrganizationKey()),
                                def.getParameterId());
                        throw e;
                    }
                } else if (PlatformParameterIdentifiers.PERIOD
                        .equals(def.getParameterId())) {
                    long usedTime = DateFactory.getInstance()
                            .getTransactionTime()
                            - subscription.getActivationDate().longValue();
                    if (usedTime > param.getLongValue()) {
                        sessionCtx.setRollbackOnly();
                        SubscriptionMigrationException e = new SubscriptionMigrationException(
                                "Parameter check failed",
                                Reason.PARAMETER_PERIOD,
                                new Object[] { subscriptionId });
                        LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                                LogMessageIdentifier.ERROR_MIGRATE_SUBSCRIPTION_AS_CHECK_PARAMETER,
                                Long.toString(currentUser.getKey()),
                                Long.toString(subscription.getKey()),
                                Long.toString(
                                        subscription.getOrganizationKey()),
                                def.getParameterId());
                        throw e;
                    }
                }
            }
        }
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOService> getUpgradeOptions(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        Subscription subscription = manageBean
                .checkSubscriptionOwner(subscriptionId, 0);

        return getUpgradeOptions(subscription);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOService> getUpgradeOptions(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Subscription subscription = manageBean.checkSubscriptionOwner(null,
                subscriptionKey);

        return getUpgradeOptions(subscription);
    }

    private List<VOService> getUpgradeOptions(Subscription subscription)
            throws OperationNotPermittedException {
        // 1. retrieve the related product
        Organization organization = dataManager.getCurrentUser()
                .getOrganization();
        PermissionCheck.owns(subscription, organization, LOG);
        Product product = subscription.getProduct();

        // 2. retrieve and return the compatible products
        List<Product> compatibleProducts = product.getCompatibleProductsList();
        // 3. get customer specific products
        long supplierKey = product.getVendor().getKey();
        compatibleProducts = replaceByCustomerSpecificProducts(
                compatibleProducts, supplierKey, organization);

        List<VOService> result = new ArrayList<>(compatibleProducts.size());
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        for (Product prod : compatibleProducts) {
            result.add(ProductAssembler.toVOProduct(prod, facade));
        }

        return result;
    }

    /**
     * Replaces global products by customer specific products based on the
     * global ones if existing. Global and specific products that are not active
     * will be removed.
     * 
     * @param products
     *            the products to get customer specific ones for
     * @param supplierKey
     *            the supplier key identifying the owner of the products
     * @param organization
     *            the customer organization to get customer specific products
     *            for
     * @return the list containing the customer specific products and the global
     *         ones not having customer specific versions
     */
    List<Product> replaceByCustomerSpecificProducts(List<Product> products,
            long supplierKey, Organization organization) {
        // read customer specific products
        List<Product> customerProducts = getProductDao()
                .getProductForCustomerOnly(supplierKey, organization);
        // replace products by customer specific products if existent
        Map<Long, Product> keyToProduct = new HashMap<>();
        for (Product prod : customerProducts) {
            keyToProduct.put(Long.valueOf(prod.getTemplate().getKey()), prod);
        }
        List<Product> temp = new ArrayList<>(products.size());
        for (Product prod : products) {
            Long key = Long.valueOf(prod.getKey());
            if (keyToProduct.containsKey(key)) {
                Product custSpec = keyToProduct.get(key);
                if (custSpec.getStatus() == ServiceStatus.ACTIVE) {
                    temp.add(custSpec);
                }
            } else if (prod.getStatus() == ServiceStatus.ACTIVE) {
                temp.add(prod);
            }
        }
        return temp;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscription upgradeSubscription(VOSubscription subscription,
            VOService service, VOPaymentInfo paymentInfo,
            VOBillingContact billingContact, List<VOUda> udas)
            throws ObjectNotFoundException, OperationNotPermittedException,
            SubscriptionMigrationException, PaymentInformationException,
            SubscriptionStateException, ServiceChangedException,
            PriceModelException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException, MandatoryCustomerUdaMissingException,
            NonUniqueBusinessKeyException, ValidationException {

        ArgumentValidator.notNull("subscription", subscription);
        ArgumentValidator.notNull("service", service);

        PlatformUser currentUser = dataManager.getCurrentUser();

        if (isPaymentInfoHidden() && service.getPriceModel().isChargeable()) {
            Organization organization = currentUser.getOrganization();
            billingContact = createBillingContactForOrganization(currentUser);
            paymentInfo = createPaymentInfoForOrganization(organization);
        }

        manageBean.checkSubscriptionOwner(subscription.getSubscriptionId(),
                subscription.getKey());

        validateSettingsForUpgrading(subscription, service, paymentInfo,
                billingContact);

        validateTriggerProcessForUpgradeSubscription(subscription);

        TriggerProcess triggerProcess = createTriggerProcessForUpgradeSubscription(
                subscription, service, paymentInfo, billingContact, udas);

        TriggerDefinition triggerDefinition = triggerProcess
                .getTriggerDefinition();

        Subscription upgradedSub = null;
        if (triggerDefinition == null) {
            try {
                upgradedSub = upgradeSubscriptionInt(triggerProcess);
            } catch (ObjectNotFoundException | SubscriptionStateException
                    | OperationNotPermittedException | ServiceChangedException
                    | PriceModelException | PaymentInformationException
                    | SubscriptionMigrationException
                    | ConcurrentModificationException
                    | TechnicalServiceNotAliveException
                    | MandatoryUdaMissingException
                    | NonUniqueBusinessKeyException | ValidationException
                    | MandatoryCustomerUdaMissingException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            triggerProcess.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createUpgradeSubscription(
                            dataManager, TriggerType.UPGRADE_SUBSCRIPTION,
                            subscription));
            dataManager.merge(triggerProcess);
        }

        return SubscriptionAssembler.toVOSubscription(upgradedSub,
                new LocalizerFacade(localizer,
                        dataManager.getCurrentUser().getLocale()));
    }

    private TriggerProcess createTriggerProcessForUpgradeSubscription(
            VOSubscription subscription, VOService service,
            VOPaymentInfo paymentInfo, VOBillingContact billingContact,
            List<VOUda> udas) {
        TriggerMessage message = new TriggerMessage(
                TriggerType.UPGRADE_SUBSCRIPTION);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess triggerProcess = list.get(0).getTrigger();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.OBJECT_ID,
                subscription.getSubscriptionId());
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subscription);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PRODUCT, service);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PAYMENTINFO, paymentInfo);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, billingContact);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.UDAS, udas);
        return triggerProcess;
    }

    private void validateTriggerProcessForUpgradeSubscription(
            VOSubscription subscription) throws OperationPendingException {
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        if (validator.isModifyOrUpgradeSubscriptionPending(subscription)) {
            String subID = subscription.getSubscriptionId();
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to upgrade or modify the subscription with ID '%s'",
                            subID),
                    ReasonEnum.UPGRADE_SUBSCRIPTION, new Object[] { subID });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_UPGRADE_SUBSCRIPTION_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subID);
            throw ope;
        }
    }

    private void validateTriggerProcessForUpgradeSubscriptionBySubscriptionKey(
            long subscriptionKey, String subscriptionID)
            throws OperationPendingException {
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        if (validator.isUpgradeSubscriptionPending(subscriptionKey)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to upgrade or modify the subscription with ID '%s'",
                            subscriptionID),
                    ReasonEnum.UPGRADE_SUBSCRIPTION,
                    new Object[] { subscriptionID });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_ADD_REVOKE_USER_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subscriptionID);
            throw ope;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Subscription upgradeSubscriptionInt(TriggerProcess tp)
            throws ObjectNotFoundException, SubscriptionStateException,
            OperationNotPermittedException, ServiceChangedException,
            PriceModelException, PaymentInformationException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, NonUniqueBusinessKeyException,
            ValidationException, MandatoryUdaMissingException,
            MandatoryCustomerUdaMissingException {

        VOSubscription current = tp
                .getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(VOSubscription.class);
        VOService voTargetProduct = tp
                .getParamValueForName(TriggerProcessParameterName.PRODUCT)
                .getValue(VOService.class);
        VOPaymentInfo voPaymentInfo = tp
                .getParamValueForName(TriggerProcessParameterName.PAYMENTINFO)
                .getValue(VOPaymentInfo.class);
        VOBillingContact voBillingContact = tp
                .getParamValueForName(
                        TriggerProcessParameterName.BILLING_CONTACT)
                .getValue(VOBillingContact.class);
        List<VOUda> udas = ParameterizedTypes
                .list(tp.getParamValueForName(TriggerProcessParameterName.UDAS)
                        .getValue(List.class), VOUda.class);

        Product dbTargetProduct = dataManager.getReference(Product.class,
                voTargetProduct.getKey());
        Subscription dbSubscription = dataManager
                .getReference(Subscription.class, current.getKey());

        PlatformUser currentUser = dataManager.getCurrentUser();
        Subscription subscription = manageBean
                .loadSubscription(current.getSubscriptionId(), 0);
        subscription.setEventPublished(false);
        BaseAssembler.verifyVersionAndKey(subscription, current);
        Product initialProduct = subscription.getProduct();
        PaymentInfo initialPaymentInfo = subscription.getPaymentInfo();
        BillingContact initialBillingContact = subscription.getBillingContact();

        validateSettingsForUpgrading(current, voTargetProduct, voPaymentInfo,
                voBillingContact);

        // validates parameter version and parameter values
        checkIfParametersAreModified(subscription, dbSubscription,
                initialProduct, dbTargetProduct,
                voTargetProduct.getParameters(), true);

        PaymentInfo paymentInfo = null;
        BillingContact bc = null;

        if (voPaymentInfo != null) {
            // Valid payment information have been passed -> use it for the
            // upgraded subscription
            paymentInfo = dataManager.getReference(PaymentInfo.class,
                    voPaymentInfo.getKey());
        }
        if (voBillingContact != null) {
            bc = dataManager.getReference(BillingContact.class,
                    voBillingContact.getKey());
        }

        // log payment info before subscription changed!
        audit.editPaymentType(dataManager, subscription, paymentInfo);
        // log billing address before subscription changed!
        audit.editBillingAddress(dataManager, subscription, bc);

        // update subscription
        subscription.setPaymentInfo(paymentInfo);
        subscription.setBillingContact(bc);

        try {
            appManager.saveAttributes(dbSubscription);
        } catch (TechnicalServiceOperationException e) {
            // Log and continue
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_WS_ERROR,
                    subscription.getSubscriptionId(), e.getMessage());
        }

        List<Uda> existingUdas = manageBean.getExistingUdas(subscription);
        List<VOUda> updatedList = getUpdatedSubscriptionAttributes(udas,
                existingUdas);
        logSubscriptionAttributeForEdit(subscription, updatedList);

        // save subscription attributes before provisioning call, to have them
        // available at the API
        if (dbTargetProduct.getTechnicalProduct().getProvisioningType()
                .equals(ProvisioningType.ASYNCHRONOUS)) {
            saveUdasForAsyncModifyOrUpgradeSubscription(udas, dbSubscription);
        } else {
            saveUdasForSubscription(udas, subscription);
        }

        // product and parameters are copied
        copyProductAndModifyParametersForUpgrade(subscription, dbTargetProduct,
                currentUser, voTargetProduct.getParameters());

        if (dbTargetProduct.getTechnicalProduct().getProvisioningType()
                .equals(ProvisioningType.SYNCHRONOUS)) {
            // bugfix 8068
            String oldServiceId = initialProduct.getTemplate() != null
                    ? initialProduct.getTemplate().getProductId()
                    : initialProduct.getProductId();
            String newServiceId = dbTargetProduct.getTemplate() != null
                    ? dbTargetProduct.getTemplate().getProductId()
                    : dbTargetProduct.getProductId();

            // remove old product
            dataManager.remove(initialProduct);

            // finally send confirmation mail to the organization admin
            modUpgBean.sendConfirmUpgradationEmail(subscription, oldServiceId,
                    newServiceId);
        } else if (dbTargetProduct.getTechnicalProduct().getProvisioningType()
                .equals(ProvisioningType.ASYNCHRONOUS)) {
            long subscriptionKey = subscription.getKey();
            if (paymentInfo != null) {
                modUpgBean.storeModifiedEntity(subscriptionKey,
                        ModifiedEntityType.SUBSCRIPTION_PAYMENTINFO,
                        String.valueOf(paymentInfo.getKey()));
            }
            if (bc != null) {
                modUpgBean.storeModifiedEntity(subscriptionKey,
                        ModifiedEntityType.SUBSCRIPTION_BILLINGCONTACT,
                        String.valueOf(bc.getKey()));
            }
            subscription.setPaymentInfo(initialPaymentInfo);
            subscription.setBillingContact(initialBillingContact);
        }
        dataManager.flush();

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.UPGRADE_SUBSCRIPTION,
                        tp.getTriggerProcessParameters(), dataManager
                                .getCurrentUser().getOrganization()));

        // log upDowngrade subscription
        audit.upDowngradeSubscription(dataManager, subscription, initialProduct,
                dbTargetProduct);

        return subscription;
    }

    /**
     * Validates the data of the specified subscription and target product to
     * evaluate if the upgrade is possible.
     * 
     * @param current
     *            The current subscription.
     * @param newProduct
     *            The product to migrate to.
     * @param voBillingContact
     * @throws ObjectNotFoundException
     *             Thrown in case the subscription or target product cannot be
     *             found.
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow a
     *             migration.
     * @throws OperationNotPermittedException
     *             Thrown in case the caller tries to modify another
     *             organization's object.
     * @throws ServiceChangedException
     *             Thrown in case the target product definition has been
     *             modified in the meantime.
     * @throws PriceModelException
     *             Thrown in case the target product is missing a price model.
     * @throws PaymentInformationException
     *             Thrown in case the calling organization does not have a
     *             payment record.
     * @throws SubscriptionMigrationException
     *             Thrown in case the migration is not possible as the products
     *             are not compatible.
     */
    void validateSettingsForUpgrading(VOSubscription current,
            VOService newProduct, VOPaymentInfo paymentInfo,
            VOBillingContact voBillingContact) throws ObjectNotFoundException,
            SubscriptionStateException, OperationNotPermittedException,
            ServiceChangedException, PriceModelException,
            PaymentInformationException, SubscriptionMigrationException {
        // 1. retrieve the subscription details and the target product
        Subscription subscription = manageBean
                .loadSubscription(current.getSubscriptionId(), 0);

        stateValidator.checkModifyAllowedForUpgrading(subscription);

        PlatformUser currentUser = dataManager.getCurrentUser();
        Organization organization = currentUser.getOrganization();
        PermissionCheck.owns(subscription, organization, LOG);

        Product currentProduct = subscription.getProduct();
        Product targetProduct = dataManager.getReference(Product.class,
                newProduct.getKey());
        // check if the product has been changed in the meantime or it
        // is inactive....
        checkIfProductIsUptodate(targetProduct, newProduct);

        // 2. check if the payment information parameter is set correctly
        PriceModel targetPriceModel = targetProduct.getPriceModel();

        if (targetPriceModel == null) {
            PriceModelException mpme = new PriceModelException(
                    PriceModelException.Reason.NOT_DEFINED);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, mpme,
                    LogMessageIdentifier.WARN_MIGRATE_PRODUCT_FAILED,
                    Long.toString(targetProduct.getKey()));
            throw mpme;
        }
        if (targetPriceModel.isChargeable() && !isPaymentInfoHidden()) {
            PaymentDataValidator.validateNotNull(paymentInfo, voBillingContact);
            PaymentInfo pi = dataManager.getReference(PaymentInfo.class,
                    paymentInfo.getKey());
            BillingContact bc = dataManager.getReference(BillingContact.class,
                    voBillingContact.getKey());
            PermissionCheck.owns(pi, organization, LOG);
            PermissionCheck.owns(bc, organization, LOG);
            PaymentDataValidator.validatePaymentTypeSupportedBySupplier(
                    organization, targetProduct, pi.getPaymentType());
            PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
        }

        // 3. now check if the new product is really compatible to the
        // current one
        List<Product> compatibleProducts = currentProduct
                .getCompatibleProductsList();
        // we won't find products in this list that are not active
        compatibleProducts = replaceByCustomerSpecificProducts(
                compatibleProducts, currentProduct.getVendorKey(),
                organization);

        boolean isCompatibleProduct = false;
        for (Product compatibleProduct : compatibleProducts) {
            if (compatibleProduct.getKey() == targetProduct.getKey()) {
                isCompatibleProduct = true;
            }
        }
        if (!isCompatibleProduct) {
            sessionCtx.setRollbackOnly();
            SubscriptionMigrationException smf = new SubscriptionMigrationException(
                    "Migration of subscription failed",
                    Reason.INCOMPATIBLE_SERVICES,
                    new Object[] { subscription.getSubscriptionId(),
                            targetProduct.getCleanProductId() });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, smf,
                    LogMessageIdentifier.WARN_MIGRATE_PRODUCT_FAILED_NOT_COMPATIBLE,
                    Long.toString(currentUser.getKey()),
                    Long.toString(subscription.getKey()),
                    Long.toString(subscription.getOrganizationKey()),
                    Long.toString(targetProduct.getKey()),
                    Long.toString(subscription.getProduct().getKey()));
            throw smf;
        }
    }

    /**
     * Copies the product and modifies the given parameters. Also informs the
     * technical service about the changed parameter set.
     * 
     * @param subscription
     *            the subscription to create a copy of the product for
     * @param targetProduct
     *            the product to copy
     * @param currentUser
     *            the currently logged in user
     * @param voTargetParameters
     *            the list of modified or configurable parameters
     * @throws SubscriptionMigrationException
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     */
    void copyProductAndModifyParametersForUpdate(Subscription subscription,
            Product targetProduct, PlatformUser currentUser,
            List<VOParameter> voTargetParameters)
            throws SubscriptionMigrationException,
            TechnicalServiceNotAliveException {
        Product targetProductCopy = null;
        ProvisioningType provisioningType = targetProduct.getTechnicalProduct()
                .getProvisioningType();
        if (provisioningType.equals(ProvisioningType.SYNCHRONOUS)) {
            targetProductCopy = targetProduct;
        } else if (provisioningType.equals(ProvisioningType.ASYNCHRONOUS)) {
            targetProductCopy = copyProductForSubscription(targetProduct,
                    subscription, true);
        }

        List<Parameter> modifiedParametersForLog = updateConfiguredParameterValues(
                targetProductCopy, voTargetParameters, subscription);

        // verify the platform parameter and send the new parameter to the
        // technical product
        checkPlatformParameterConstraints(subscription, targetProductCopy,
                currentUser);
        try {
            if (provisioningType.equals(ProvisioningType.ASYNCHRONOUS)) {
                subscription.setAsyncTempProduct(targetProductCopy);
                handleAsyncUpdateSubscription(subscription, targetProductCopy);
            } else if (provisioningType.equals(ProvisioningType.SYNCHRONOUS)) {
                // BUG 9998. It has to be ensured, that no call on pending
                // subscription
                if (subscription.getStatus() != SubscriptionStatus.PENDING) {
                    appManager.modifySubscription(subscription);
                }
            }
        } catch (TechnicalServiceNotAliveException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (TechnicalServiceOperationException e1) {
            sessionCtx.setRollbackOnly();
            Object[] params;
            String subscriptionId = subscription.getSubscriptionId();
            if (e1.getMessageParams() != null
                    && e1.getMessageParams().length > 1) {
                params = new Object[] { subscriptionId, e1.getMessage(),
                        e1.getMessageParams()[1] };
            } else {
                params = new Object[] { subscriptionId, e1.getMessage(), "" };
            }
            SubscriptionMigrationException smf = new SubscriptionMigrationException(
                    "Modify ParameterSet failed", Reason.PARAMETER, params);
            LOG.logError(Log4jLogger.SYSTEM_LOG, smf,
                    LogMessageIdentifier.ERROR_MODIFY_PARAMETER_SET_FAILED);
            throw smf;
        }
        audit.editSubscriptionParameterConfiguration(dataManager,
                targetProductCopy, modifiedParametersForLog);
    }

    /**
     * Copies the product and modifies the given parameters. Also informs the
     * technical service about the changed parameter set.
     * 
     * @param subscription
     *            the subscription to create a copy of the product for
     * @param targetProduct
     *            the product to copy
     * @param currentUser
     *            the currently logged in user
     * @param voTargetParameters
     *            the list of modified or configurable parameters
     * @throws SubscriptionMigrationException
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     * @throws ObjectNotFoundException
     */
    void copyProductAndModifyParametersForUpgrade(Subscription subscription,
            Product targetProduct, PlatformUser currentUser,
            List<VOParameter> voTargetParameters)
            throws SubscriptionMigrationException,
            TechnicalServiceNotAliveException, ObjectNotFoundException {
        ProvisioningType provisioningType = targetProduct.getTechnicalProduct()
                .getProvisioningType();
        Product targetProductCopy = copyProductForSubscription(targetProduct,
                subscription, false);
        List<Parameter> modifiedParametersForLog = updateConfiguredParameterValues(
                targetProductCopy, voTargetParameters, subscription);

        // verify the platform parameter
        checkPlatformParameterConstraints(subscription, targetProductCopy,
                currentUser);
        try {
            if (provisioningType.equals(ProvisioningType.ASYNCHRONOUS)) {
                subscription.setAsyncTempProduct(targetProductCopy);
                handleAsyncUpgradeSubscription(subscription, targetProductCopy);
            } else if (provisioningType.equals(ProvisioningType.SYNCHRONOUS)) {
                targetProductCopy.getPriceModel()
                        .setProvisioningCompleted(true);
                subscription.bindToProduct(targetProductCopy);

                // BE08022: Activate the subscription if it is up-/downgraded
                if (isActivationAllowed(subscription, true)) {
                    appManager.activateInstance(subscription);
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                }

                // BUG 9998. It has to be ensured, that no call on pending
                // subscription
                if (subscription.getStatus() != SubscriptionStatus.PENDING) {
                    appManager.upgradeSubscription(subscription);
                }
            }
        } catch (TechnicalServiceNotAliveException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (TechnicalServiceOperationException e1) {
            sessionCtx.setRollbackOnly();
            Object[] params;
            String subscriptionId = subscription.getSubscriptionId();
            if (e1.getMessageParams() != null
                    && e1.getMessageParams().length > 1) {
                params = new Object[] { subscriptionId, e1.getMessage(),
                        e1.getMessageParams()[1] };
            } else {
                params = new Object[] { subscriptionId, e1.getMessage(), "" };
            }
            SubscriptionMigrationException smf = new SubscriptionMigrationException(
                    "Modify ParameterSet failed", Reason.PARAMETER, params);
            LOG.logError(Log4jLogger.SYSTEM_LOG, smf,
                    LogMessageIdentifier.ERROR_MODIFY_PARAMETER_SET_FAILED);
            throw smf;
        }
        audit.editSubscriptionParameterConfiguration(dataManager,
                targetProductCopy, modifiedParametersForLog);
    }

    private Product copyProductForSubscription(Product targetProduct,
            Subscription subscription, boolean provisiongCompleted) {
        Product targetProductCopy = targetProduct.copyForSubscription(
                targetProduct.getTargetCustomer(), subscription);

        if (targetProductCopy.getPriceModel() != null) {
            // FIXME LG clean
            // For asynchronous upgrade the first target pricemodel version is
            // created when subscription is still in PENDING_UPD, but must be
            // fitered for billing. Set the indicating flag before persisting.
            targetProductCopy.getPriceModel()
                    .setProvisioningCompleted(provisiongCompleted);
        }

        try {
            dataManager.persist(targetProductCopy);
        } catch (NonUniqueBusinessKeyException ex) {
            // this must never happen as the product copy must have unique
            // id
            SaaSSystemException se = new SaaSSystemException(
                    "The product copy method didn't create a unique productId.",
                    ex);
            LOG.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_CREATE_UNIQUE_PRODUCT_ID_FAILED_IN_PRODUCT_COPY);
            throw se;
        }
        copyLocalizedPricemodelValues(targetProductCopy, targetProduct);
        return targetProductCopy;
    }

    /**
     * Sets subscription status and informs the technical service about the
     * changed parameter set when asynchronously update subscription.
     * 
     * @param subscription
     *            the subscription to update
     * @param targetProduct
     *            the target product owned by the subscription
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    void handleAsyncUpdateSubscription(Subscription subscription,
            Product targetProduct) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        EnumSet<SubscriptionStatus> set = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.SUSPENDED);
        SubscriptionStatus status = subscription.getStatus();
        if (set.contains(status)) {
            appManager.asyncModifySubscription(subscription, targetProduct);
            switch (status) {
            case ACTIVE:
                subscription.setStatus(SubscriptionStatus.PENDING_UPD);
                break;
            case SUSPENDED:
                subscription.setStatus(SubscriptionStatus.SUSPENDED_UPD);
                break;
            default:
                break;
            }
        }
    }

    /**
     * Sets subscription status and informs the technical service about the
     * changed parameter set when asynchronously upgrade subscription.
     * 
     * @param subscription
     *            the subscription to update
     * @param targetProduct
     *            the target product owned by the subscription
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    void handleAsyncUpgradeSubscription(Subscription subscription,
            Product targetProduct) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        EnumSet<SubscriptionStatus> set = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.EXPIRED, SubscriptionStatus.SUSPENDED);
        SubscriptionStatus status = subscription.getStatus();
        if (set.contains(status)) {
            appManager.asyncUpgradeSubscription(subscription, targetProduct);
            switch (status) {
            case ACTIVE:
                subscription.setStatus(SubscriptionStatus.PENDING_UPD);
                break;
            case SUSPENDED:
                subscription.setStatus(SubscriptionStatus.SUSPENDED_UPD);
                break;
            case EXPIRED:
            default:
                break;
            }
        }
    }

    boolean isActivationAllowed(Subscription subscription,
            boolean activateExpiredSubscription)
            throws ObjectNotFoundException {
        if (subscription == null) {
            return false;
        }

        if (activateExpiredSubscription
                && (subscription.getStatus() == SubscriptionStatus.EXPIRED)) {
            return true;
        }

        if (subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            return false;
        }

        if (!subscription.getPriceModel().isChargeable()) {
            return true;
        }

        if (subscription.getBillingContact() == null) {
            return false;
        }

        PaymentType subPayType = null;
        PaymentInfo paymentInfo = subscription.getPaymentInfo();
        if (paymentInfo != null) {
            subPayType = paymentInfo.getPaymentType();
        }
        if (subPayType == null) {
            return false;
        }

        long serviceKey = subscription.getProduct().getKey();
        boolean paymentEnabled;
        paymentEnabled = accountService.isPaymentTypeEnabled(serviceKey,
                subPayType.getKey());

        return paymentEnabled;

    }

    /**
     * Checks if the product read from the database is in active state and equal
     * to the one passed in as voproduct comparing the versions.
     * 
     * @param prod
     *            the product just read from the database
     * @param voProd
     *            the product passed in through the interface
     * @throws ServiceChangedException
     *             in case the product read from the database isn't active or
     *             version based different to the passed voproduct
     */
    private void checkIfProductIsUptodate(Product prod, VOService voProd)
            throws ServiceChangedException {
        if (prod.getStatus() != ServiceStatus.ACTIVE) {
            throw new ServiceChangedException(
                    ServiceChangedException.Reason.SERVICE_INACCESSIBLE);
        }
        if (prod.getVersion() != voProd.getVersion()) {
            throw new ServiceChangedException(
                    ServiceChangedException.Reason.SERVICE_MODIFIED);
        }
        ParameterSet parameterSet = prod.getParameterSet();
        if (parameterSet != null) {
            List<Parameter> parameters = parameterSet.getParameters();
            for (Parameter doParameter : parameters) {
                for (VOParameter param : voProd.getParameters()) {
                    if (param.getKey() == doParameter.getKey()
                            && param.getVersion() != doParameter.getVersion()) {
                        throw new ServiceChangedException(
                                ServiceChangedException.Reason.SERVICE_MODIFIED);
                    }
                }
            }
        }
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscriptionDetails modifySubscription(VOSubscription subscription,
            List<VOParameter> parameters, List<VOUda> udas)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException, SubscriptionStateException,
            MandatoryCustomerUdaMissingException {

        ArgumentValidator.notNull("subscription", subscription);

        Subscription subFromDB = manageBean.checkSubscriptionOwner(
                subscription.getSubscriptionId(), subscription.getKey());

        validateSubscriptionSettings(subscription);

        validateTriggerProcessForModifySubscription(subscription);

        PlatformUser currentUser = dataManager.getCurrentUser();
        boolean canModify = PermissionCheck
                .shouldWeProceedWithUpdatingSubscription(subscription,
                        subFromDB, currentUser);

        VOSubscriptionDetails result;
        if (canModify) {
            TriggerProcess triggerProcess = createTriggerProcessForModifySubscription(
                    subscription, parameters, udas);

            result = processTriggerProcessForSubscriptionModification(
                    subscription, triggerProcess);
        } else {
            LocalizerFacade facade = new LocalizerFacade(localizer,
                    currentUser.getLocale());
            result = SubscriptionAssembler.toVOSubscriptionDetails(subFromDB,
                    facade);
        }

        return result;
    }

    private VOSubscriptionDetails processTriggerProcessForSubscriptionModification(
            VOSubscription subscription, TriggerProcess triggerProcess)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, OperationNotPermittedException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, MandatoryUdaMissingException,
            MandatoryCustomerUdaMissingException, SubscriptionStateException {
        VOSubscriptionDetails result = null;
        TriggerDefinition triggerDefinition = triggerProcess
                .getTriggerDefinition();
        if (triggerDefinition == null) {
            try {
                result = modifySubscriptionInt(triggerProcess);
            } catch (ObjectNotFoundException | NonUniqueBusinessKeyException
                    | OperationNotPermittedException
                    | SubscriptionMigrationException
                    | ConcurrentModificationException
                    | TechnicalServiceNotAliveException | ValidationException
                    | MandatoryUdaMissingException | SubscriptionStateException
                    | MandatoryCustomerUdaMissingException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            triggerProcess.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createUpgradeSubscription(
                            dataManager, TriggerType.MODIFY_SUBSCRIPTION,
                            subscription));
            dataManager.merge(triggerProcess);
        }
        return result;
    }

    private TriggerProcess createTriggerProcessForModifySubscription(
            VOSubscription subscription, List<VOParameter> parameters,
            List<VOUda> udas) {
        TriggerMessage message = new TriggerMessage(
                TriggerType.MODIFY_SUBSCRIPTION);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess triggerProcess = list.get(0).getTrigger();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.OBJECT_ID,
                subscription.getSubscriptionId());
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, subscription);
        List<VOParameter> params = parameters;
        if (params == null) {
            params = new ArrayList<>();
        }

        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PARAMETERS, params);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.UDAS, udas);
        return triggerProcess;
    }

    private void validateTriggerProcessForModifySubscription(
            VOSubscription subscription) throws OperationPendingException {
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        String subID = subscription.getSubscriptionId();
        if (validator.isModifyOrUpgradeSubscriptionPending(subscription)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to modify or upgrade the subscription with key '%s'",
                            subID),
                    ReasonEnum.MODIFY_SUBSCRIPTION, new Object[] { subID });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_MODIFY_SUBSCRIPTION_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subID);
            throw ope;
        }
    }

    private void validateTriggerProcessForModifySubscriptionBySubscriptionKey(
            long subscriptionKey, String subscriptionID)
            throws OperationPendingException {
        TriggerProcessValidator validator = new TriggerProcessValidator(
                dataManager);
        if (validator.isModifySubscriptionPending(subscriptionKey)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to modify or upgrade the subscription with key '%s'",
                            subscriptionID),
                    ReasonEnum.MODIFY_SUBSCRIPTION,
                    new Object[] { subscriptionID });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_ADD_REVOKE_USER_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    subscriptionID);
            throw ope;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VOSubscriptionDetails modifySubscriptionInt(TriggerProcess tp)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, OperationNotPermittedException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, MandatoryUdaMissingException,
            SubscriptionStateException, MandatoryCustomerUdaMissingException {

        VOSubscription subscription = tp
                .getParamValueForName(TriggerProcessParameterName.SUBSCRIPTION)
                .getValue(VOSubscription.class);
        List<VOParameter> modifiedParameters = ParameterizedTypes.list(
                tp.getParamValueForName(TriggerProcessParameterName.PARAMETERS)
                        .getValue(List.class),
                VOParameter.class);
        List<VOUda> udas = ParameterizedTypes
                .list(tp.getParamValueForName(TriggerProcessParameterName.UDAS)
                        .getValue(List.class), VOUda.class);

        Subscription dbSubscription = validateSubscriptionSettings(
                subscription);

        final PlatformUser currentUser = dataManager.getCurrentUser();
        Organization organization = currentUser.getOrganization();

        UserGroup unit = getUnit(subscription.getUnitKey(),
                subscription.getUnitName(), organization.getKey());

        stateValidator.checkModifyAllowedForUpdating(dbSubscription);

        // get initial values of subscription
        String dbSubscriptionId = dbSubscription.getSubscriptionId();
        boolean subIdChanged = !dbSubscriptionId
                .equals(subscription.getSubscriptionId());
        String dbReferenceId = dbSubscription.getPurchaseOrderNumber();
        boolean refIdChanged = dbReferenceId == null
                && subscription.getPurchaseOrderNumber() != null
                || dbReferenceId != null && !dbReferenceId
                        .equals(subscription.getPurchaseOrderNumber());
        PlatformUser dbOwner = dbSubscription.getOwner();
        Product dbProduct = dbSubscription.getProduct();
        String dbPurchaseNumber = dbSubscription.getPurchaseOrderNumber();
        UserGroup dbUnit = dbSubscription.getUserGroup();

        // set new values for subscription
        dbSubscription.setSubscriptionId(subscription.getSubscriptionId());
        dbSubscription
                .setPurchaseOrderNumber(subscription.getPurchaseOrderNumber());
        if (currentUser.isOrganizationAdmin() || currentUser.isUnitAdmin()) {
            dbSubscription.setUserGroup(unit);
        }
        manageBean.setSubscriptionOwner(dbSubscription,
                subscription.getOwnerId(), true);

        try {
            appManager.saveAttributes(dbSubscription);
        } catch (TechnicalServiceOperationException e) {
            // Log and continue
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_TECH_SERVICE_WS_ERROR,
                    subscription.getSubscriptionId(), e.getMessage());
        }

        List<Uda> existingUdas = manageBean.getExistingUdas(dbSubscription);
        List<VOUda> updatedUdas = getUpdatedSubscriptionAttributes(udas,
                existingUdas);

        boolean changedValues = false;
        try {
            changedValues = subIdChanged || refIdChanged
                    || checkIfParametersAreModified(dbSubscription,
                            dbSubscription, dbProduct, dbProduct,
                            modifiedParameters, false)
                    || updatedUdas.size() > 0;
        } catch (ServiceChangedException e) {
            throw new ConcurrentModificationException(e.getMessage());
        }

        boolean asynch = dbProduct.getTechnicalProduct().getProvisioningType()
                .equals(ProvisioningType.ASYNCHRONOUS);

        logSubscriptionAttributeForEdit(dbSubscription, updatedUdas);
        logSubscriptionOwner(dbSubscription, dbOwner);

        if (changedValues && asynch) {
            long subscriptionKey = dbSubscription.getKey();
            modUpgBean.storeModifiedEntity(subscriptionKey,
                    ModifiedEntityType.SUBSCRIPTION_SUBSCRIPTIONID,
                    subscription.getSubscriptionId());
            modUpgBean.storeModifiedEntity(subscriptionKey,
                    ModifiedEntityType.SUBSCRIPTION_ORGANIZATIONID,
                    dbSubscription.getOrganization().getOrganizationId());
            modUpgBean.storeModifiedEntity(subscriptionKey,
                    ModifiedEntityType.SUBSCRIPTION_PURCHASEORDERNUMBER,
                    subscription.getPurchaseOrderNumber());
            modUpgBean.storeModifiedEntity(subscriptionKey,
                    ModifiedEntityType.SUBSCRIPTION_OWNERID,
                    subscription.getOwnerId());
            modUpgBean.storeModifiedEntity(subscriptionKey,
                    ModifiedEntityType.SUBSCRIPTION_UNIT,
                    String.valueOf(subscription.getUnitKey()));

            // save subscription attributes before provisioning call, to have
            // them
            // available at the API
            saveUdasForAsyncModifyOrUpgradeSubscription(udas, dbSubscription);
        } else {
            saveUdasForSubscription(udas, dbSubscription);
        }
        dataManager.flush();

        if (changedValues) {
            copyProductAndModifyParametersForUpdate(dbSubscription, dbProduct,
                    currentUser, modifiedParameters);
        }

        if (changedValues && asynch) {
            // set initial values again
            dbSubscription.setSubscriptionId(dbSubscriptionId);
            dbSubscription.setPurchaseOrderNumber(dbPurchaseNumber);
            String dbOwnerId = dbOwner == null ? null : dbOwner.getUserId();
            manageBean.setSubscriptionOwner(dbSubscription, dbOwnerId, true);
            dbSubscription.setUserGroup(dbUnit);
        }

        LocalizerFacade facade = new LocalizerFacade(localizer,
                currentUser.getLocale());
        VOSubscriptionDetails result = SubscriptionAssembler
                .toVOSubscriptionDetails(dbSubscription, facade);

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.MODIFY_SUBSCRIPTION,
                        tp.getTriggerProcessParameters(), dataManager
                                .getCurrentUser().getOrganization()));

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.SUBSCRIPTION_MODIFICATION,
                        tp.getTriggerProcessParameters(),
                        dbSubscription.getProduct().getVendor()));

        return result;
    }

    /**
     * Validates the settings of the specified subscription object.
     * 
     * @param subscription
     *            The subscription to be validated.
     * @return The domain object representation of the subscription to be
     *         modified.
     * @throws ValidationException
     *             Thrown in case the settings could not be validated.
     * @throws ObjectNotFoundException
     *             Thrown in case the subscription could not be found.
     * @throws OperationNotPermittedException
     *             Thrown in case the caller tries to modify another
     *             organization's object.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case there already is a subscription with the given
     *             id for the current organization (only checked in caes of
     *             changing the current id).
     * @throws ConcurrentModificationException
     */
    private Subscription validateSubscriptionSettings(
            VOSubscription subscription) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            NonUniqueBusinessKeyException, ConcurrentModificationException {

        subscription.setSubscriptionId(
                BaseAssembler.trim(subscription.getSubscriptionId()));

        String subscriptionId = subscription.getSubscriptionId();
        BLValidator.isId("subscriptionId", subscriptionId, true);
        BLValidator.isDescription("purchaseOrderNumber",
                subscription.getPurchaseOrderNumber(), false);
        Subscription subscriptionToModify = dataManager
                .getReference(Subscription.class, subscription.getKey());
        subscriptionToModify.setEventPublished(false);
        PermissionCheck.owns(subscriptionToModify,
                dataManager.getCurrentUser().getOrganization(), LOG);
        BaseAssembler.verifyVersionAndKey(subscriptionToModify, subscription);

        String ownerId = subscription.getOwnerId();
        if (ownerId != null && ownerId.length() != 0) {
            checkRolesForSubscriptionOwner(ownerId,
                    dataManager.getCurrentUser().getTenantId());
        }
        if (!subscriptionToModify.getSubscriptionId().equals(subscriptionId)) {
            Subscription sub = new Subscription();
            sub.setOrganization(subscriptionToModify.getOrganization());
            sub.setSubscriptionId(subscriptionId);
            dataManager.validateBusinessKeyUniqueness(sub);

            List<VOTriggerProcess> triggers = triggerService
                    .getAllActionsForOrganizationRelatedSubscription();
            for (VOTriggerProcess voTriggerProcess : triggers) {
                if (voTriggerProcess.getSubscription().getSubscriptionId()
                        .equals(subscriptionId)) {
                    NonUniqueBusinessKeyException e = new NonUniqueBusinessKeyException(
                            DomainObjectException.ClassEnum.SUBSCRIPTION,
                            subscriptionId);
                    throw e;
                }
            }
            // Validate unique subscirptionId and organization in temporary
            // table
            Long result = getModifiedEntityDao()
                    .countSubscriptionOfOrganizationAndSubscription(
                            subscriptionToModify, subscriptionId);
            if (result.longValue() > 0) {
                NonUniqueBusinessKeyException ex = new NonUniqueBusinessKeyException();
                LOG.logError(Log4jLogger.SYSTEM_LOG, ex,
                        LogMessageIdentifier.ERROR_SUBSCRIPTIONID_ALREADY_EXIST_IN_MODIFIEDENTITY,
                        subscriptionId, subscriptionToModify.getOrganization()
                                .getOrganizationId());
                throw ex;
            }
        }
        return subscriptionToModify;
    }

    void checkRolesForSubscriptionOwner(String ownerId, String tenantId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        PlatformUser owner = idManager.getPlatformUser(ownerId, tenantId, true);
        if (!owner.hasSubscriptionOwnerRole()) {
            String rolesString = UserRoleType.ORGANIZATION_ADMIN + ", "
                    + UserRoleType.SUBSCRIPTION_MANAGER;
            String message = "Add subscription owner failed. User '%s' does not have required roles: %s";
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    String.format(message, ownerId, rolesString));
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                    LogMessageIdentifier.WARN_ADD_SUBSCRIPTION_OWNER_FAILED,
                    ownerId, rolesString);
            throw onp;
        }
    }

    /**
     * To avoid unneeded migrations, check if parameters really have changed and
     * if they can be changed
     * 
     * @param dbSourceProduct
     *            the current product
     * @param voTargetParameters
     *            the parameters to modify
     * @return <code>true</code> if at least one parameter will be changed.
     * @throws ConcurrentModificationException
     * @throws ValidationException
     */
    boolean checkIfParametersAreModified(Subscription subscription,
            Subscription dbSubscription, Product dbSourceProduct,
            Product dbTargetProduct, List<VOParameter> voTargetParameters,
            boolean upgrade)
            throws SubscriptionMigrationException, ServiceChangedException,
            ConcurrentModificationException, ValidationException {

        boolean result = false;

        verifyIfParameterConcurrentlyChanged(dbTargetProduct,
                voTargetParameters, upgrade);

        // load db target product parameter and cache the values for better
        // performance in a map
        Map<String, Parameter> dbTargetParameterMap = new HashMap<>();
        if (dbTargetProduct.getParameterSet() != null) {
            for (Parameter dbTargetParameter : dbTargetProduct.getParameterSet()
                    .getParameters()) {
                dbTargetParameterMap.put(dbTargetParameter
                        .getParameterDefinition().getParameterId(),
                        dbTargetParameter);
            }
        }

        Map<String, Parameter> dbSubscriptionParameterMap = new HashMap<>();
        if (dbSubscription.getParameterSet() != null) {
            for (Parameter dbSubParameter : dbSubscription.getParameterSet()
                    .getParameters()) {
                dbSubscriptionParameterMap.put(dbSubParameter
                        .getParameterDefinition().getParameterId(),
                        dbSubParameter);
            }
        }

        for (VOParameter voTargetParameter : voTargetParameters) {
            String dbParameterId = voTargetParameter.getParameterDefinition()
                    .getParameterId();
            if (dbSubscriptionParameterMap.containsKey(dbParameterId)) {
                Parameter dbTargetParameter = dbTargetParameterMap
                        .get(dbParameterId);
                Parameter dbSubscriptionParameter = dbSubscriptionParameterMap
                        .get(dbParameterId);

                if (!upgrade && voTargetParameter.getParameterDefinition()
                        .getModificationType()
                        .equals(ParameterModificationType.ONE_TIME)) {
                    if (!compareParameterValue(dbTargetParameter,
                            dbSubscriptionParameter)) {
                        throw new ValidationException(
                                ValidationException.ReasonEnum.ONE_TIME_PARAMETER_NOT_ALLOWED,
                                null,
                                new Object[] { dbTargetParameter
                                        .getParameterDefinition()
                                        .getParameterId() });

                    }
                }

                if (upgrade && !isParameterUpOrDowngradeValid(dbTargetParameter,
                        voTargetParameter)) {
                    String sourceProductId = dbSourceProduct.getTemplateOrSelf()
                            .getProductId();
                    String targetProductId = dbTargetProduct.getTemplateOrSelf()
                            .getProductId();

                    SubscriptionMigrationException e = new SubscriptionMigrationException(
                            "Incompatible parameter found",
                            Reason.INCOMPATIBLE_PARAMETER,
                            new Object[] { subscription.getSubscriptionId(),
                                    sourceProductId, targetProductId,
                                    voTargetParameter.getParameterDefinition()
                                            .getParameterId() });

                    LOG.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                            e,
                            LogMessageIdentifier.WARN_NOT_CONFIGURABLE_PARAMETER_OF_SUBSCRIPTION_MODIFIED,
                            dataManager.getCurrentUser().getUserId(),
                            dbParameterId,
                            Long.toString(dbSourceProduct.getKey()));
                    throw e;
                }

                if (dbTargetParameter.isValueSet()) {
                    // if currently no value is set, even null or empty as new
                    // value mean a change
                    if (!dbTargetParameter.getValue()
                            .equals(voTargetParameter.getValue())) {
                        result = true;
                    }
                } else {
                    // if no value is currently set, the new value only needs to
                    // be set if not null and not empty
                    if (voTargetParameter.getValue() != null
                            && voTargetParameter.getValue().trim()
                                    .length() > 0) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Verifies the version of a parameter.
     * 
     * @param dbTargetProduct
     *            target product
     * @param voTargetParameters
     *            VO target parameters
     * @throws ConcurrentModificationException
     *             Is thrown if the parameter version changed or the parameter
     *             does not exist anymore.
     */
    void verifyIfParameterConcurrentlyChanged(Product dbTargetProduct,
            List<VOParameter> voTargetParameters, boolean upgrade)
            throws ServiceChangedException, ConcurrentModificationException {

        List<Parameter> dbTargetParameterList;
        if (dbTargetProduct.getParameterSet() == null
                || dbTargetProduct.getParameterSet().getParameters() == null) {
            dbTargetParameterList = new LinkedList<>();
        } else {
            dbTargetParameterList = dbTargetProduct.getParameterSet()
                    .getParameters();
        }

        if (upgrade
                && dbTargetParameterList.size() != voTargetParameters.size()) {
            // Parameter changed, thus throw ex
            throw new ServiceChangedException(
                    ServiceChangedException.Reason.SERVICE_MODIFIED);
        }

        Map<String, Parameter> dbParameterMap = new HashMap<>();
        for (Parameter parameter : dbTargetParameterList) {
            dbParameterMap.put(
                    parameter.getParameterDefinition().getParameterId(),
                    parameter);
        }

        for (VOParameter voParameter : voTargetParameters) {
            Parameter dbParameter = dbParameterMap
                    .get(voParameter.getParameterDefinition().getParameterId());

            if (dbParameter == null) {
                String message = String.format("Parameter '%s' does not exist.",
                        voParameter.getParameterDefinition().getParameterId());

                throw new ServiceChangedException(message);
            }
            BaseAssembler.verifyVersionAndKey(dbParameter, voParameter);
        }
    }

    /**
     * Matrix : <br/>
     * 1. NOT CONFIGURABLE or CONFIGURABLE --> NOT CONFIGURABLE = OK, if values
     * are identical or parameter modification type is one time<br/>
     * 
     * 2. Otherwise true is returned
     * 
     * @param dbParameter
     *            source (subscription) parameter
     * @param targetParameter
     *            VO target parameter
     * @return see matrix. False is also returned if the parameter id of the
     *         both parameters are not the same
     */
    boolean isParameterUpOrDowngradeValid(Parameter dbParameter,
            VOParameter targetParameter) {

        if (!dbParameter.getParameterDefinition().getParameterId().equals(
                targetParameter.getParameterDefinition().getParameterId())) {
            return false;
        }

        if (dbParameter.getParameterDefinition()
                .getModificationType() != targetParameter
                        .getParameterDefinition().getModificationType()) {
            return false;
        }

        boolean targetOneTime = targetParameter.getParameterDefinition()
                .getModificationType()
                .equals(ParameterModificationType.ONE_TIME);

        // non-configurable non-onetime parameter now only has default value,
        // bug #9422
        return !(!dbParameter.isConfigurable() && !targetOneTime)
                || compareParameterValue(dbParameter, targetParameter);

    }

    /**
     * Compares the value of both parameters. A NULL value and empty String are
     * treated identically.
     * 
     * @param sourceParameter
     *            source (subscription) parameter
     * @param targetParameter
     *            target parameter
     * @return true, if identical, otherwise false
     */
    private boolean compareParameterValue(Parameter sourceParameter,
            Parameter targetParameter) {
        String sourceParameterValue = sourceParameter.getValue() == null ? ""
                : sourceParameter.getValue().trim();
        String targetParameterValue = targetParameter.getValue() == null ? ""
                : targetParameter.getValue().trim();

        return sourceParameterValue.equals(targetParameterValue);
    }

    /**
     * Compares the value of both parameters. A NULL value and empty String are
     * treated identically.
     * 
     * @param sourceParameter
     *            source (subscription) parameter
     * @param targetParameter
     *            VO target parameter
     * @return true, if identical, otherwise false
     */
    private boolean compareParameterValue(Parameter sourceParameter,
            VOParameter targetParameter) {
        String sourceParameterValue = sourceParameter.getValue() == null ? ""
                : sourceParameter.getValue().trim();
        String targetParameterValue = targetParameter.getValue() == null ? ""
                : targetParameter.getValue().trim();

        return sourceParameterValue.equals(targetParameterValue);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public List<String> getSubscriptionIdentifiers()
            throws OrganizationAuthoritiesException {

        Organization org = dataManager.getCurrentUser().getOrganization();

        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING);
        return getSubscriptionDao().getSubscriptionIdsForMyCustomers(org,
                states);
    }

    private List<Subscription> getQueryResultListSubIdsAndOrgs(
            Set<SubscriptionStatus> states) {

        List<Subscription> result = new ArrayList<>();
        result.addAll(executeQueryLoadSubIdsAndOrgsForMyCustomers(states));

        Organization org = dataManager.getCurrentUser().getOrganization();
        if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
            result.addAll(executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers());
        }

        return result;
    }

    List<Subscription> getQueryResultListSubIdsAndOrgs(
            Set<SubscriptionStatus> states, Pagination pagination) {

        List<Subscription> result = new ArrayList<>();
        Organization org = dataManager.getCurrentUser().getOrganization();
        if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
            result.addAll(executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers(
                    states, pagination));
        } else {
            result.addAll(executeQueryLoadSubIdsAndOrgsForMyCustomers(states,
                    pagination));
        }

        return result;
    }

    List<Subscription> executeQueryLoadSubIdsAndOrgsForMyCustomers(
            Set<SubscriptionStatus> states) {
        Organization org = dataManager.getCurrentUser().getOrganization();
        return getSubscriptionDao().getSubscriptionsForMyCustomers(org, states);
    }

    List<Subscription> executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers() {
        Organization org = dataManager.getCurrentUser().getOrganization();
        return getSubscriptionDao().getSubscriptionsForMyBrokerCustomers(org);
    }

    List<Subscription> executeQueryLoadSubIdsAndOrgsForMyCustomers(
            Set<SubscriptionStatus> states, Pagination pagination) {

        return getSubscriptionDao().getSubscriptionsForMyCustomers(
                dataManager.getCurrentUser(), states, pagination);
    }

    List<Subscription> executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers(
            Set<SubscriptionStatus> states, Pagination pagination) {

        return getSubscriptionDao().getSubscriptionsForMyBrokerCustomers(
                dataManager.getCurrentUser(), states, pagination);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public List<VOOrganization> getCustomersForSubscriptionId(
            String subscriptionId) throws OrganizationAuthoritiesException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        List<VOOrganization> result = new ArrayList<>();

        Organization offerer = dataManager.getCurrentUser().getOrganization();
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING);

        List<Organization> customers = getOrganizationDao()
                .getCustomersForSubscriptionId(offerer, subscriptionId, states);

        LocalizerFacade lf = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        for (Organization customer : customers) {
            result.add(OrganizationAssembler.toVOOrganization(customer, false,
                    lf, PerformanceHint.ONLY_IDENTIFYING_FIELDS));
        }

        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean expireOverdueSubscriptions(long currentTime) {
        return terminateBean.expireOverdueSubscriptions(currentTime);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean expireSubscription(Subscription subscriptionToExpire) {
        return terminateBean.expireSubscription(subscriptionToExpire);
    }

    /**
     * Checks if the usercount of the subscription is lower or equal to the
     * value of the product parameter NAMED_USER. If this condition does not
     * hold, an ProductParameterException will be thrown.
     * 
     * @param subscription
     *            The subscription to be checked.
     * @throws ServiceParameterException
     *             Thrown in case the user count of the subscription is greater
     *             than the value of the product parameter NAMED_USER
     */
    void verifyParameterNamedUser(Subscription subscription)
            throws ServiceParameterException {

        if (subscription == null) {
            return;
        }
        ParameterSet parameterSet = subscription.getParameterSet();
        if (parameterSet == null || parameterSet.getParameters() == null) {
            return;
        }
        for (Parameter parameter : parameterSet.getParameters()) {
            if (parameter.getParameterDefinition()
                    .getParameterType() == ParameterType.PLATFORM_PARAMETER
                    && PlatformParameterIdentifiers.NAMED_USER.equals(parameter
                            .getParameterDefinition().getParameterId())) {
                // if it is not defined to be mandatory, it can be empty
                if (Strings.isEmpty(parameter.getValue())) {
                    return;
                }
                try {
                    // If a value is set, it must be ensured by validation that
                    // it is an integer - so the NumberFormatException should
                    // not happen
                    if (subscription.getActiveUsers().size() > Integer
                            .parseInt(parameter.getValue())) {
                        sessionCtx.setRollbackOnly();
                        String text = "Subscription '"
                                + subscription.getSubscriptionId()
                                + "'/Product '"
                                + subscription.getProduct().getProductId()
                                + "'";
                        ServiceParameterException e = new ServiceParameterException(
                                text, ParameterType.PLATFORM_PARAMETER,
                                PlatformParameterIdentifiers.NAMED_USER,
                                new Object[] { parameter.getValue(),
                                        subscription.getSubscriptionId() });
                        LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                                LogMessageIdentifier.WARN_TOO_MANY_NAMED_USER_FOR_SUBSCRIPTION,
                                subscription.getSubscriptionId(),
                                subscription.getProduct().getProductId());
                        throw e;
                    }
                } catch (NumberFormatException e) {
                    LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.WARN_PROCESS_NAMED_USER_FAILED_AS_NUMBER_FORMAT,
                            parameter.getValue(),
                            subscription.getProduct().getProductId());
                }
            }
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void informProductAboutNewUsers(Subscription sub,
            List<PlatformUser> users) throws SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        stateValidator.checkInformProductAboutNewUsers(sub);
        if (!canModifyApplicationUsers(sub)) {
            return;
        }
        List<UsageLicense> licenses = getUsageLicensesForUsers(sub, users);
        User[] createdUsers = appManager.createUsers(sub, licenses);
        mapApplicationUserIdToLicense(licenses, createdUsers);
    }

    /**
     * Maps the application user id back to the usage license and sets the
     * affected usage license dirty
     * 
     * @param licenses
     *            the usage licenses to update
     * @param createdUsers
     *            the user array containing the application user id
     */
    private void mapApplicationUserIdToLicense(List<UsageLicense> licenses,
            User[] createdUsers) {
        if (createdUsers == null) {
            return;
        }
        String applicationUserId;
        for (UsageLicense license : licenses) {
            for (User createdUser : createdUsers) {
                applicationUserId = createdUser.getApplicationUserId();
                if (createdUser.getUserId()
                        .equals(license.getUser().getUserId())
                        && applicationUserId != null) {
                    license.setApplicationUserId(applicationUserId);
                    break;
                }
            }
        }
    }

    /**
     * Determines the usage license for the given users of the subscription.
     * 
     * @param sub
     *            the subscription to get the usage licenses from
     * @param users
     *            the users to get the usage licenses for
     * @return the list of usage licenses
     */
    private List<UsageLicense> getUsageLicensesForUsers(Subscription sub,
            List<PlatformUser> users) {
        List<UsageLicense> result = new ArrayList<>();
        List<UsageLicense> licenses = sub.getUsageLicenses();
        for (PlatformUser user : users) {
            for (UsageLicense license : licenses) {
                if (user.getKey() == license.getUser().getKey()) {
                    result.add(license);
                }
            }
        }
        return result;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void abortAsyncSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);

        completeAsyncSubscription(subscriptionId, organizationId, reason, true);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void abortAsyncSubscription(UUID subscriptionUUID)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionUUID", subscriptionUUID);

        VOSubscription subscription = getSubscription(subscriptionUUID);
        if (subscription == null) {
            throw new ObjectNotFoundException("Subscription with UUID "
                    + subscriptionUUID + " not found!");
        }

        completeAsyncSubscription(subscription.getSubscriptionId(),
                subscription.getOrganizationId(), null,
                false);

    }

    /**
     * @param subscriptionId
     * @param organizationId
     * @param reason
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws SubscriptionStateException
     */
    void completeAsyncSubscription(String subscriptionId, String organizationId,
            List<VOLocalizedText> reason, boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            SubscriptionStateException {
        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);
        if (validateOrganization) {
            manageBean.validateTechnoloyProvider(subscription);
        }
        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new SubscriptionStateException(
                    "Operation not allowed for subscription "
                            + subscription.getSubscriptionId(),
                    SubscriptionStateException.Reason.ONLY_PENDING);
        }

        abortSubscription(subscriptionId, subscription);
        removeLocalizedResources(reason, subscription);
        deleteProduct(subscription);
        List<PlatformUser> receivers = loadReceiversForAbortAsyncSubscription(
                subscription);
        sendSubscriptionAbortEmail(subscriptionId, organizationId, subscription,
                receivers);
    }

    private void deleteProduct(Subscription subscription) {
        Product product = subscription.getProduct();
        product.setStatus(ServiceStatus.DELETED);
    }

    private void abortSubscription(String subscriptionId,
            Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.INVALID);
        subscription.setSubscriptionId(subscriptionId + "#"
                + String.valueOf(System.currentTimeMillis()));
    }

    void removeLocalizedResources(List<VOLocalizedText> reason,
            Subscription subscription) {
        long key = subscription.getKey();
        localizer.removeLocalizedValues(key,
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS);
        localizer.storeLocalizedResources(key,
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS,
                reason);
    }

    void sendSubscriptionAbortEmail(String subscriptionId,
            String organizationId, Subscription subscription,
            List<PlatformUser> receivers) {

        for (PlatformUser platformUser : receivers) {
            LocalizerFacade facade = new LocalizerFacade(localizer,
                    platformUser.getLocale());
            String text = facade.getText(subscription.getKey(),
                    LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS);

            try {
                commService.sendMail(platformUser,
                        EmailType.SUBSCRIPTION_INVALIDATED,
                        new Object[] { subscriptionId, organizationId, text },
                        subscription.getMarketplace());
            } catch (MailOperationException e) {
                // only log the exception and proceed
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_INVALIDATION_SUBSCRIPTION_CONFIRMING_FAILED);
            }
        }
    }

    /**
     * The mail for aborting asynchronous subscription is sent to:
     * 
     * - administrators of the technology provider organization;<br/>
     * - administrators of the customer organization;<br/>
     * - subscription owner if it is not already administrator.
     */
    List<PlatformUser> loadReceiversForAbortAsyncSubscription(
            Subscription subscription) {

        return manageBean.getCustomerAndTechnicalProductAdminForSubscription(
                subscription);
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void completeAsyncSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("instance", instanceInfo);

        completeAsynProvisioning(instanceInfo, subscriptionId, organizationId,
                true);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void completeAsyncSubscription(UUID subscriptionUUID,
            VOInstanceInfo instanceInfo) throws ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {

        ArgumentValidator.notNull("subscriptionUUID", subscriptionUUID);
        ArgumentValidator.notNull("instance", instanceInfo);

        VOSubscription subscription = getSubscription(subscriptionUUID);

        if (subscription == null) {
            throw new ObjectNotFoundException("Subscription with UUID "
                    + subscriptionUUID + " not found!");
        }

        completeAsynProvisioning(instanceInfo, subscription.getSubscriptionId(),
                subscription.getOrganizationId(), false);
    }

    public VOSubscription getSubscription(UUID uuid) {
        Subscription subscription = getSubscriptionDao().getSubscription(uuid);
        VOSubscription voSubscription = SubscriptionAssembler.toVOSubscription(
                subscription, null, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        return voSubscription;
    }

    /**
     * @param instanceInfo
     * @param subscription
     * @throws ValidationException
     * @throws TechnicalServiceOperationException
     * @throws OperationNotPermittedException
     * @throws SubscriptionStateException
     * @throws ObjectNotFoundException
     */
    void completeAsynProvisioning(VOInstanceInfo instanceInfo,
            String subscriptionId, String organizationId,
            boolean validateOrganization) throws ValidationException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            SubscriptionStateException, ObjectNotFoundException {

        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);

        String productInstanceId = instanceInfo.getInstanceId();
        TechnicalProduct techProduct = subscription.getProduct()
                .getTechnicalProduct();
        if (getSubscriptionDao().checkIfProductInstanceIdExists(
                productInstanceId, techProduct)) {
            ValidationException ex = new ValidationException(
                    "The product instance ID already exist");
            LOG.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_PRODUCT_INSTANCE_ID_ALREADY_EXIST);
            throw ex;
        }

        updateInstanceInfoForCompletion(subscription, instanceInfo);

        if (validateOrganization) {
            manageBean.validateTechnoloyProvider(subscription);
        }
        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new SubscriptionStateException(
                    "Operation not allowed for subscription "
                            + subscription.getSubscriptionId(),
                    SubscriptionStateException.Reason.ONLY_PENDING);
        }

        subscription.setActivationDate(
                Long.valueOf(DateFactory.getInstance().getTransactionTime()));

        boolean deactivateInstance = !modUpgBean
                .isPaymentValidOrFree(subscription);

        if (deactivateInstance) {
            subscription.setStatus(SubscriptionStatus.SUSPENDED);
        } else {
            activateSubscriptionFirstTime(subscription);
        }

        NotifyProvisioningServicePayload payload = new NotifyProvisioningServicePayload(
                subscription.getKey(), deactivateInstance);

        TaskMessage message = new TaskMessage(
                NotifyProvisioningServiceHandler.class, payload);
        tqs.sendAllMessages(Collections.singletonList(message));

        PriceModel pm = subscription.getProduct().getPriceModel();
        pm.setProvisioningCompleted(true);
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void updateAsyncSubscriptionProgress(String subscriptionId,
            String organizationId, List<VOLocalizedText> progress)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);

        updateAsyncProgress(subscriptionId, organizationId, progress, true);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateAsyncSubscriptionProgress(UUID subscriptionUUID,
            String progress)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionUUID", subscriptionUUID);

        VOSubscription subscription = getSubscription(subscriptionUUID);
        if (subscription == null) {
            throw new ObjectNotFoundException("Subscription with UUID "
                    + subscriptionUUID + " not found!");
        }

        List<VOLocalizedText> progressList = new ArrayList<>();
        VOLocalizedText text = new VOLocalizedText();
        text.setLocale(Locale.ENGLISH.toString());
        text.setText(progress);
        progressList.add(text);

        updateAsyncProgress(subscription.getSubscriptionId(),
                subscription.getOrganizationId(),
                progressList, false);

    }

    /**
     * @param subscriptionId
     * @param organizationId
     * @param progress
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws SubscriptionStateException
     */
    void updateAsyncProgress(String subscriptionId, String organizationId,
            List<VOLocalizedText> progress, boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            SubscriptionStateException {
        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);
        if (validateOrganization) {
            manageBean.validateTechnoloyProvider(subscription);
        }
        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new SubscriptionStateException(
                    "Operation not allowed for subscription "
                            + subscription.getSubscriptionId(),
                    SubscriptionStateException.Reason.ONLY_PENDING);
        }

        localizer.storeLocalizedResources(subscription.getKey(),
                LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS,
                progress);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean notifyAboutTimedoutSubscriptions(long currentTime) {
        // we could also use a separate query inclusing the checked conditions
        List<Subscription> subscriptions = getSubscriptionDao()
                .getSubscriptionsByStatus(SubscriptionStatus.PENDING);
        for (Subscription subscription : subscriptions) {
            Long timeout = subscription.getProduct().getTechnicalProduct()
                    .getProvisioningTimeout();
            if (!subscription.isTimeoutMailSent()
                    && subscription.getCreationDate().longValue()
                            + timeout.longValue() < currentTime) {
                List<PlatformUser> users = subscription.getProduct()
                        .getTechnicalProduct().getOrganization()
                        .getPlatformUsers();
                if (notifyTechnicalAdminsAboutTimeout(subscription, users)) {
                    // if at least one mail was sent, set the mail sent flag of
                    // the subscription
                    subscription.setTimeoutMailSent(true);
                }
            }
        }
        return true;
    }

    /**
     * Tries to send a notification mail to the admin users contained in the
     * list of users of the technical product owner organization informing about
     * a timed out pending subscription.
     * 
     * @param subscription
     *            the subscription that timed ot in pending state
     * @param users
     *            the users of the organization owning the technical product
     * @return <code>true</code> if at leas one mail was sent otherwise
     *         <code>false</code>
     */
    private boolean notifyTechnicalAdminsAboutTimeout(Subscription subscription,
            List<PlatformUser> users) {
        boolean result = false;
        String organizationId = subscription.getOrganization()
                .getOrganizationId();
        String subscriptionId = subscription.getSubscriptionId();
        for (PlatformUser user : users) {
            if (!user.isOrganizationAdmin()) {
                continue;
            }
            try {
                commService.sendMail(user, EmailType.SUBSCRIPTION_TIMEDOUT,
                        new Object[] { subscriptionId, organizationId },
                        subscription.getMarketplace());
                // at least one admin could be informed
                result = true;
            } catch (MailOperationException e) {
                // only log the exception and proceed
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_TIMEOUT_OF_SUBSCRIPTION_NOTIFYING_FAILED);
            }
        }
        return result;
    }

    /**
     * Checks if the users of the subscription can be modified. this is not
     * possible if the subscription is in pending or invalid state because no
     * product instance is there in this case.
     * 
     * @param subscription
     *            the subscription to modify the application side users for
     * @return <code>true</code> if product instance can be informed otherwise
     *         <code>false</code>
     */
    private boolean canModifyApplicationUsers(Subscription subscription) {
        return !(subscription.getStatus() == SubscriptionStatus.PENDING
                || subscription.getStatus() == SubscriptionStatus.INVALID);
    }

    /**
     * It returns a list of VOSubscriptionIdAndOrganizations objects, which
     * contain the subscriptionIdentifier and the associated customers of the
     * subscription in form of list.If there are no subscriptions an empty list
     * is returned.
     * 
     * The role of organization as a supplier, a broker or a reseller is needed
     * to execute this method.
     * 
     * @return List<VOSubscriptionIdAndOrganizations>
     */
    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public List<VOSubscriptionIdAndOrganizations> getCustomerSubscriptions()
            throws OrganizationAuthoritiesException {
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING);
        LocalizerFacade lf = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());

        List<Subscription> queryResultList = getQueryResultListSubIdsAndOrgs(
                states);
        Map<String, VOSubscriptionIdAndOrganizations> mapSubIdsAndOrgs = getSubIdsAndOrgs(
                lf, queryResultList);

        return new ArrayList<>(mapSubIdsAndOrgs.values());
    }

    /**
     * It returns a list of VOSubscriptionIdAndOrganizations objects, which
     * contain the subscriptionIdentifier and the associated customers of the
     * subscription in form of list.If there are no subscriptions an empty list
     * is returned.
     * 
     * The role of organization as a supplier, a broker or a reseller is needed
     * to execute this method.
     * 
     * @return List<VOSubscriptionIdAndOrganizations>
     */
    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public List<VOSubscriptionIdAndOrganizations> getSubscriptionsForTerminate()
            throws OrganizationAuthoritiesException {
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.EXPIRED);
        LocalizerFacade lf = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());

        List<Subscription> queryResultList = getQueryResultListSubIdsAndOrgs(
                states);
        Map<String, VOSubscriptionIdAndOrganizations> mapSubIdsAndOrgs = getSubIdsAndOrgs(
                lf, queryResultList);

        return new ArrayList<>(mapSubIdsAndOrgs.values());
    }

    private Map<String, VOSubscriptionIdAndOrganizations> getSubIdsAndOrgs(
            LocalizerFacade lf, List<Subscription> subscriptionList) {
        Map<String, VOSubscriptionIdAndOrganizations> mapSubIdsAndOrgs = new LinkedHashMap<>();

        for (Subscription subscription : subscriptionList) {
            String subId = subscription.getSubscriptionId();
            Organization customer = subscription.getOrganization();

            if (mapSubIdsAndOrgs.containsKey(subId)) {
                VOSubscriptionIdAndOrganizations subAndOrgs = mapSubIdsAndOrgs
                        .get(subId);
                List<VOOrganization> customersForSubId = subAndOrgs
                        .getOrganizations();
                customersForSubId.add(
                        OrganizationAssembler.toVOOrganization(customer, false,
                                lf, PerformanceHint.ONLY_IDENTIFYING_FIELDS));
                subAndOrgs.setOrganizations(customersForSubId);
                mapSubIdsAndOrgs.put(subId, subAndOrgs);

            } else {
                VOSubscriptionIdAndOrganizations subAndOrgs = new VOSubscriptionIdAndOrganizations();
                subAndOrgs.setSubscriptionId(subId);

                List<VOOrganization> customers = new ArrayList<>();

                customers.add(OrganizationAssembler.toVOOrganization(customer,
                        false, lf, PerformanceHint.ONLY_IDENTIFYING_FIELDS));
                subAndOrgs.setOrganizations(customers);

                mapSubIdsAndOrgs.put(subId, subAndOrgs);
            }
        }
        return mapSubIdsAndOrgs;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public VOSubscriptionDetails getSubscriptionForCustomer(
            String organizationId, String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        // find organization of customer
        Organization customerTmpl = new Organization();
        customerTmpl.setOrganizationId(organizationId);
        Organization customer = (Organization) dataManager
                .getReferenceByBusinessKey(customerTmpl);

        final PlatformUser currentUser = dataManager.getCurrentUser();
        PermissionCheck.sellerOfCustomer(currentUser.getOrganization(),
                customer, LOG, sessionCtx);

        Subscription subscriptionTmpl = new Subscription();
        subscriptionTmpl.setSubscriptionId(subscriptionId);
        subscriptionTmpl.setOrganizationKey(customer.getKey());
        Subscription subscription = (Subscription) dataManager
                .getReferenceByBusinessKey(subscriptionTmpl);

        // fill subscription VO
        LocalizerFacade facade = new LocalizerFacade(localizer,
                currentUser.getLocale());
        VOSubscriptionDetails voSubscriptionDetail = SubscriptionAssembler
                .toVOSubscriptionDetails(subscription, facade);

        // log view subscription
        audit.viewSubscription(dataManager, subscription, customer);

        return voSubscriptionDetail;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VORoleDefinition> getServiceRolesForSubscription(
            String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        Subscription sub = manageBean.checkSubscriptionOwner(subscriptionId, 0);

        return getServiceRolesForSubscription(sub);
    }

    @Override
    public List<VORoleDefinition> getServiceRolesForSubscription(
            long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException {

        Subscription sub = manageBean.checkSubscriptionOwner(null,
                subscriptionKey);

        return getServiceRolesForSubscription(sub);
    }

    private List<VORoleDefinition> getServiceRolesForSubscription(
            Subscription subscription) {
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        List<RoleDefinition> roleDefinitions = subscription.getProduct()
                .getTechnicalProduct().getRoleDefinitions();

        return RoleAssembler.toVORoleDefinitions(roleDefinitions, facade);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public List<VORoleDefinition> getServiceRolesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);

        Product prod = dataManager.getReference(Product.class,
                service.getKey());
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        List<RoleDefinition> roleDefinitions = prod.getTechnicalProduct()
                .getRoleDefinitions();

        return RoleAssembler.toVORoleDefinitions(roleDefinitions, facade);
    }

    @Override
    public void executeServiceOperation(VOSubscription subscription,
            VOTechnicalServiceOperation operation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, ConcurrentModificationException,
            ValidationException, SubscriptionStateException,
            NonUniqueBusinessKeyException {
        ArgumentValidator.notNull("subscription", subscription);
        ArgumentValidator.notNull("operation", operation);

        Subscription sub = manageBean
                .loadSubscription(subscription.getSubscriptionId(), 0);

        stateValidator.checkExecuteServiceOperationAllowed(sub);

        TechnicalProductOperation op = dataManager.getReference(
                TechnicalProductOperation.class, operation.getKey());

        BaseAssembler.verifyVersionAndKey(sub, subscription);
        BaseAssembler.verifyVersionAndKey(op, operation);

        List<VOServiceOperationParameter> list = operation
                .getOperationParameters();
        Map<String, String> params = new HashMap<>();
        if (list != null) {
            for (VOServiceOperationParameter p : list) {
                params.put(p.getParameterId(), p.getParameterValue());
            }
        }

        checkIfParamsModified(op, list);
        String transactionid = IdGenerator.generateArtificialIdentifier();

        OperationResult result = manageBean.executeServiceOperation(sub, op,
                params, transactionid);

        createOperationRecord(sub, op, transactionid,
                result.isAsyncExecution());
    }

    private void createOperationRecord(Subscription subscription,
            TechnicalProductOperation operation, String transactionid,
            boolean isAsyncExecution) throws NonUniqueBusinessKeyException {
        Date executiondate = DateFactory.getInstance().getTransactionDate();
        OperationRecord record = givenOperationRecord(subscription, operation,
                executiondate, transactionid, isAsyncExecution);
        operationRecordBean.createOperationRecord(record);
    }

    OperationRecord givenOperationRecord(Subscription subscription,
            TechnicalProductOperation operation, Date executiondate,
            String transactionid, boolean isAsyncExecution) {
        OperationRecord record = new OperationRecord();
        record.setTransactionid(transactionid);
        record.setSubscription(subscription);
        record.setTechnicalProductOperation(operation);
        record.setExecutiondate(executiondate);
        record.setUser(dataManager.getCurrentUser());
        if (isAsyncExecution) {
            record.setStatus(OperationStatus.RUNNING);
        } else {
            record.setStatus(OperationStatus.COMPLETED);
        }
        return record;
    }

    private void checkIfParamsModified(TechnicalProductOperation op,
            List<VOServiceOperationParameter> list)
            throws ConcurrentModificationException {
        List<OperationParameter> opParams = op.getParameters();
        Map<Long, OperationParameter> opParamsMap = new HashMap<>();
        if (opParams != null) {
            for (OperationParameter p : opParams) {
                opParamsMap.put(Long.valueOf(p.getKey()), p);
            }
        }
        if (opParamsMap.size() != list.size()) {
            throw new ConcurrentModificationException();
        }
        for (VOServiceOperationParameter parameter : list) {
            OperationParameter param = opParamsMap
                    .get(Long.valueOf(parameter.getKey()));
            if (param == null) {
                ConcurrentModificationException cme = new ConcurrentModificationException(
                        parameter);
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                        LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION,
                        parameter.getClass().getSimpleName());
                throw cme;
            } else {
                BaseAssembler.verifyVersionAndKey(param, parameter);
            }
        }

    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public void terminateSubscription(VOSubscription subscrVO, String reason)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ConcurrentModificationException,
            SubscriptionStateException {

        ArgumentValidator.notNull("subscription", subscrVO);
        Subscription subscription = loadSubscription(subscrVO.getKey());
        subscription.setEventPublished(false);
        BaseAssembler.verifyVersionAndKey(subscription, subscrVO);

        // Remove corresponding operation record of the subscription
        operationRecordBean
                .removeOperationsForSubscription(subscription.getKey());

        terminateBean.terminateSubscription(subscription, reason);
    }

    @Override
    public boolean hasCurrentUserSubscriptions() {

        String userKey = sessionCtx.getCallerPrincipal().getName();
        Long userKeyLong;
        try {
            userKeyLong = Long.valueOf(userKey);
        } catch (NumberFormatException e) {
            SaaSSystemException saaSSystemException = new SaaSSystemException(
                    e);
            LOG.logError(Log4jLogger.SYSTEM_LOG, saaSSystemException,
                    LogMessageIdentifier.ERROR_WRONG_USER_KEY_IN_SESSION,
                    userKey);
            throw saaSSystemException;
        }
        Long count = getSubscriptionDao().hasCurrentUserSubscriptions(
                userKeyLong, VISIBLE_SUBSCRIPTION_STATUS);
        return count.longValue() > 0;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOSubscriptionDetails modifySubscriptionPaymentData(
            VOSubscription subscription, VOBillingContact billingContact,
            VOPaymentInfo paymentInfo) throws ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException,
            PaymentInformationException, SubscriptionStateException,
            PaymentDataException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        ArgumentValidator.notNull("subscription", subscription);

        Subscription sub = manageBean.checkSubscriptionOwner(
                subscription.getSubscriptionId(), subscription.getKey());

        PlatformUser user = dataManager.getCurrentUser();
        Organization customer = user.getOrganization();
        PermissionCheck.owns(sub, customer, LOG);
        BaseAssembler.verifyVersionAndKey(sub, subscription);
        EnumSet<SubscriptionStatus> set = EnumSet.of(
                SubscriptionStatus.DEACTIVATED, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.INVALID);
        if (set.contains(sub.getStatus())) {
            Object[] params = new Object[] { sub.getStatus().name() };
            SubscriptionStateException sse = new SubscriptionStateException(
                    SubscriptionStateException.Reason.SUBSCRIPTION_INVALID_STATE,
                    null, params);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.WARN_SUBSCRIPTION_STATE_INVALID,
                    sub.getStatus().name());
            throw sse;
        }

        if (!sub.getPriceModel().isChargeable()) {
            if (billingContact != null || paymentInfo != null) {
                PaymentDataException pde = new PaymentDataException(
                        "No payment info required for a non-chargeable price model",
                        PaymentDataException.Reason.NO_PAYMENT_REQUIRED);
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, pde,
                        LogMessageIdentifier.WARN_NO_PAYMENT_INFO_REQUIRED);
                throw pde;
            }
        }

        PaymentInfo pi = null;
        BillingContact bc = null;

        if (sub.getPriceModel().isChargeable()) {
            PaymentDataValidator.validateNotNull(paymentInfo, billingContact);
            pi = dataManager.getReference(PaymentInfo.class,
                    paymentInfo.getKey());
            bc = dataManager.getReference(BillingContact.class,
                    billingContact.getKey());
            PermissionCheck.owns(pi, customer, LOG);
            PermissionCheck.owns(bc, customer, LOG);
            BaseAssembler.verifyVersionAndKey(pi, paymentInfo);
            BaseAssembler.verifyVersionAndKey(bc, billingContact);

            PaymentDataValidator.validatePaymentTypeSupportedBySupplier(
                    customer, sub.getProduct(), pi.getPaymentType());
            PaymentDataValidator.validatePaymentInfoDataForUsage(pi);
            SubscriptionStatus current = sub.getStatus();
            if (current.isSuspendedOrSuspendedUpd()) {
                try {
                    appManager.activateInstance(sub);
                } catch (TechnicalServiceNotAliveException
                        | TechnicalServiceOperationException e) {
                    sessionCtx.setRollbackOnly();
                    throw e;
                }
                sub.setStatus(current.getNextForPaymentTypeRevoked());
            }
        }

        // log before subscription changed!
        audit.editPaymentType(dataManager, sub, pi);
        audit.editBillingAddress(dataManager, sub, bc);

        // change subscription
        sub.setPaymentInfo(pi);
        sub.setBillingContact(bc);

        // flush to get the correct version of the subscription
        dataManager.flush();

        return SubscriptionAssembler.toVOSubscriptionDetails(sub,
                new LocalizerFacade(localizer, user.getLocale()));
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER" })
    public void reportIssue(String subscriptionId, String subject,
            String issueText) throws IllegalArgumentException,
            ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, ValidationException {
        manageBean.reportIssue(subscriptionId, subject, issueText);
    }

    void activateSubscriptionFirstTime(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        Organization chargingOrganization = subscription.getProduct()
                .determineChargingOrganization();
        subscription.setCutOffDay(chargingOrganization.getCutOffDay());
    }

    /**
     * Check if PaymentInfo or BillingContact is concurrently modified.
     * 
     * @param paymentInfo
     *            the existing PaymentInfo stored in database
     * @param billingContact
     *            the existing BillingContact stored in database
     * @param voPaymentInfo
     * @param voBillingContact
     * @throws ConcurrentModificationException
     */
    void validatePaymentInfoAndBillingContact(PaymentInfo paymentInfo,
            BillingContact billingContact, VOPaymentInfo voPaymentInfo,
            VOBillingContact voBillingContact)
            throws ConcurrentModificationException {
        BaseAssembler.verifyVersionAndKey(paymentInfo, voPaymentInfo);
        BaseAssembler.verifyVersionAndKey(billingContact, voBillingContact);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public List<Subscription> getSubscriptionsForManagers()
            throws OrganizationAuthoritiesException {
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.PENDING_UPD, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD);

        return getQueryResultListSubIdsAndOrgs(states);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "BROKER_MANAGER", "RESELLER_MANAGER" })
    public List<Subscription> getSubscriptionsForManagers(Pagination pagination)
            throws OrganizationAuthoritiesException {
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.PENDING_UPD, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD);

        return getQueryResultListSubIdsAndOrgs(states, pagination);
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void completeAsyncModifySubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("instance", instance);

        completeAsyncModification(subscriptionId, organizationId, instance,
                true);
    }

    @Override
    public void completeAsyncModifySubscription(UUID subscriptionUUID,
            VOInstanceInfo instance) throws ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionUUID", subscriptionUUID);
        ArgumentValidator.notNull("instance", instance);

        VOSubscription subscription = getSubscription(subscriptionUUID);

        if (subscription == null) {
            throw new ObjectNotFoundException("Subscription with UUID "
                    + subscriptionUUID + " not found!");
        }

        completeAsyncModification(subscription.getSubscriptionId(),
                subscription.getOrganizationId(), instance,
                false);
    }

    void completeAsyncModification(String subscriptionId, String organizationId,
            VOInstanceInfo instance, boolean validateOrganization)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceOperationException, OperationNotPermittedException {
        Subscription subscription = modUpgBean.findSubscriptionForAsyncCallBack(
                subscriptionId, organizationId);

        stateValidator.checkCompleteModifyAllowed(subscription);

        updateInstanceInfoForCompletion(subscription, instance);

        if (validateOrganization) {
            manageBean.validateTechnoloyProvider(subscription);
        }

        modUpgBean.setStatusForModifyComplete(subscription);

        Map<String, Parameter> paramMap = new HashMap<>();
        if (subscription.getParameterSet() != null) {
            for (Parameter parameter : subscription.getParameterSet()
                    .getParameters()) {
                paramMap.put(
                        parameter.getParameterDefinition().getParameterId(),
                        parameter);
            }
        }

        Product asyncTempProduct = subscription.getAsyncTempProduct();
        if (asyncTempProduct.getParameterSet() != null) {
            for (Parameter tempParam : asyncTempProduct.getParameterSet()
                    .getParameters()) {
                String parameterID = tempParam.getParameterDefinition()
                        .getParameterId();
                Parameter param = paramMap.get(parameterID);
                if (param != null) {
                    param.setValue(tempParam.getValue());
                }
            }
        }
        subscription.setAsyncTempProduct(null);
        dataManager.remove(asyncTempProduct);

        modUpgBean.updateSubscriptionAttributesForAsyncUpdate(subscription);

        List<PlatformUser> receivers = manageBean
                .getCustomerAdminsAndSubscriptionOwner(subscription);

        for (PlatformUser platformUser : receivers) {
            try {
                commService.sendMail(platformUser,
                        EmailType.SUBSCRIPTION_PARAMETER_MODIFIED,
                        new Object[] { subscriptionId },
                        subscription.getMarketplace());
            } catch (MailOperationException e) {
                // only log the exception and proceed
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_MODIFY_PARAMETER_OF_SUBSCRIPTION_CONFIRMING_FAILED);
            }
        }
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void abortAsyncModifySubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);

        Subscription subscription = modUpgBean.findSubscriptionForAsyncCallBack(
                subscriptionId, organizationId);

        stateValidator.checkAbortAllowedForModifying(subscription);

        abortAsyncUpgradeOrModifySubscription(subscription, organizationId,
                reason, true);
    }

    @Override
    public void abortAsyncModifySubscription(UUID subscriptionUUID,
            String reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionUUID", subscriptionUUID);

        VOSubscription subscription = getSubscription(subscriptionUUID);

        if (subscription == null) {
            throw new ObjectNotFoundException("Subscription with UUID "
                    + subscriptionUUID + " not found!");
        }

        Subscription subscriptionModified = modUpgBean
                .findSubscriptionForAsyncCallBack(
                        subscription.getSubscriptionId(),
                        subscription.getOrganizationId());

        stateValidator.checkAbortAllowedForModifying(subscriptionModified);

        List<VOLocalizedText> localizedText = new ArrayList<>();
        VOLocalizedText text = new VOLocalizedText();
        text.setLocale(Locale.ENGLISH.getLanguage());
        text.setText(reason);
        localizedText.add(text);

        abortAsyncUpgradeOrModifySubscription(subscriptionModified,
                subscription.getOrganizationId(),
                localizedText, false);
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void completeAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, TechnicalServiceOperationException,
            OperationNotPermittedException, SubscriptionStateException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("instance", instance);

        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);

        stateValidator.checkCompleteUpgradeAllowed(subscription);
        updateInstanceInfoForCompletion(subscription, instance);

        manageBean.validateTechnoloyProvider(subscription);

        Product initialProduct = subscription.getProduct();
        Product asyncTempProduct = subscription.getAsyncTempProduct();
        subscription.bindToProduct(asyncTempProduct);
        subscription.setAsyncTempProduct(null);

        modUpgBean.updateSubscriptionAttributesForAsyncUpgrade(subscription);
        modUpgBean.setStatusForUpgradeComplete(subscription);

        String oldServiceId = initialProduct.getTemplate() != null
                ? initialProduct.getTemplate().getProductId()
                : initialProduct.getProductId();
        String newServiceId = asyncTempProduct.getTemplate() != null
                ? asyncTempProduct.getTemplate().getProductId()
                : asyncTempProduct.getProductId();
        dataManager.remove(initialProduct);

        dataManager.flush();

        PriceModel pm = subscription.getProduct().getPriceModel();
        pm.setProvisioningCompleted(true);

        if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
            manageBean.suspend(subscription);
        }
        modUpgBean.sendConfirmUpgradationEmail(subscription, oldServiceId,
                newServiceId, instance.getAccessInfo());
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void abortAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);

        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);
        stateValidator.checkAbortAllowedForUpgrading(subscription);

        abortAsyncUpgradeOrModifySubscription(subscription, organizationId,
                reason, true);
    }

    void abortAsyncUpgradeOrModifySubscription(Subscription subscription,
            String organizationId, List<VOLocalizedText> reason,
            boolean validateOrganization)
            throws OperationNotPermittedException {

        if (validateOrganization) {
            manageBean.validateTechnoloyProvider(subscription);
        }

        final SubscriptionStatus currentState = subscription.getStatus();

        subscription.setStatus(currentState.getNextForAbort());

        Product asyncTempProduct = subscription.getAsyncTempProduct();
        subscription.setAsyncTempProduct(null);
        dataManager.remove(asyncTempProduct);

        modUpgBean.deleteModifiedEntityForSubscription(subscription);

        // send notify mail to administrators of the customer organization,
        // subscription owner and technology provider organization
        sendAbortAsyncModifySubscriptionEmail(subscription, organizationId,
                reason);
    }

    void sendAbortAsyncModifySubscriptionEmail(Subscription subscription,
            String organizationId, List<VOLocalizedText> reason) {
        localizer.removeLocalizedValues(subscription.getKey(),
                LocalizedObjectTypes.SUBSCRIPTION_MODIFICATION_REASON);
        localizer.storeLocalizedResources(subscription.getKey(),
                LocalizedObjectTypes.SUBSCRIPTION_MODIFICATION_REASON, reason);

        dataManager.flush();

        List<PlatformUser> receivers = manageBean
                .getCustomerAndTechnicalProductAdminForSubscription(
                        subscription);
        for (PlatformUser platformUser : receivers) {
            LocalizerFacade facade = new LocalizerFacade(localizer,
                    platformUser.getLocale());
            String text = facade.getText(subscription.getKey(),
                    LocalizedObjectTypes.SUBSCRIPTION_MODIFICATION_REASON);
            try {
                commService.sendMail(platformUser,
                        EmailType.SUBSCRIPTION_PARAMETER_MODIFY_ABORT,
                        new Object[] { subscription.getSubscriptionId(),
                                organizationId, text },
                        subscription.getMarketplace());
            } catch (MailOperationException e) {
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_MODIFY_PARAMETER_OF_SUBSCRIPTION_ABORT_CONFIRMING_FAILED);
            }
        }
    }

    @Override
    public List<VOServiceOperationParameterValues> getServiceOperationParameterValues(
            VOSubscription subscription, VOTechnicalServiceOperation operation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException, ConcurrentModificationException,
            TechnicalServiceOperationException {
        ArgumentValidator.notNull("subscription", subscription);
        ArgumentValidator.notNull("operation", operation);

        Subscription sub = manageBean
                .loadSubscription(subscription.getSubscriptionId(), 0);
        TechnicalProductOperation op = dataManager.getReference(
                TechnicalProductOperation.class, operation.getKey());

        BaseAssembler.verifyVersionAndKey(sub, subscription);
        BaseAssembler.verifyVersionAndKey(op, operation);

        Map<String, List<String>> operationParameterValues = manageBean
                .getOperationParameterValues(sub, op);
        List<VOServiceOperationParameterValues> result = new LinkedList<>();
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        for (Entry<String, List<String>> e : operationParameterValues
                .entrySet()) {
            OperationParameter param = op.findParameter(e.getKey());
            if (param != null) {
                VOServiceOperationParameterValues vo = TechServiceOperationParameterAssembler
                        .toVOServiceOperationParameterValues(param, facade,
                                e.getValue());
                result.add(vo);
            }
        }
        return result;
    }

    void updateInstanceInfoForCompletion(Subscription subscription,
            VOInstanceInfo info) throws TechnicalServiceOperationException {
        new SubscriptionInstanceInfo(localizer, subscription)
                .validateAndUpdateInstanceInfoForCompletion(info);
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void updateAccessInformation(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            OperationNotPermittedException, ValidationException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("instance", instance);

        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);
        manageBean.validateTechnoloyProvider(subscription);
        checkIPAddressChangedAndSendMailToUsers(subscription, instance);
        updateInstanceInfo(subscription, instance);
    }

    void checkIPAddressChangedAndSendMailToUsers(Subscription subscription,
            VOInstanceInfo instance) {
        String currentAccessInfo = instance.getAccessInfo();
        String lastAccessInfo = null;
        if (isUsableAWSAccessInfo(currentAccessInfo)) {
            List<String> accessInfos = getSubscriptionHistoryDao()
                    .getAccessInfos(subscription, instance);
            for (String oldAccessInfo : accessInfos) {
                if (isUsableAWSAccessInfo(oldAccessInfo)) {
                    lastAccessInfo = oldAccessInfo;
                    break;
                }
            }
            if (checkIPAddressChanged(lastAccessInfo, currentAccessInfo)) {
                sendMailForIPAddressChanged(subscription, currentAccessInfo);
            }
        }
    }

    boolean checkIPAddressChanged(String lastAccessInfo,
            String currentAccessInfo) {
        return (lastAccessInfo != null && !lastAccessInfo.isEmpty()
                && !lastAccessInfo.equals(currentAccessInfo));
    }

    private void sendMailForIPAddressChanged(Subscription subscription,
            String currentAccessInfo) {
        List<UsageLicense> userLicenses = getUsageLicenseDao()
                .getUsersforSubscription(subscription);
        EmailType emailType = EmailType.SUBSCRIPTION_ACCESSINFO_CHANGED;
        Long marketplaceKey = null;
        if (subscription.getMarketplace() != null) {
            marketplaceKey = Long
                    .valueOf(subscription.getMarketplace().getKey());
        }
        SendMailPayload payload = new SendMailPayload();
        for (UsageLicense usageLicense : userLicenses) {
            payload.addMailObjectForUser(usageLicense.getUser().getKey(),
                    emailType,
                    new Object[] { subscription.getSubscriptionId(),
                            getPublicDNS(currentAccessInfo),
                            getIPAddress(currentAccessInfo),
                            getKeyPairName(currentAccessInfo) },
                    marketplaceKey);
        }
        TaskMessage message = new TaskMessage(SendMailHandler.class, payload);
        tqs.sendAllMessages(Collections.singletonList(message));
    }

    boolean isAWSAccessInfo(String accessInfo) {
        try {
            String subAccessInfo = accessInfo.substring(accessInfo.indexOf(":"),
                    accessInfo.indexOf(KEY_PAIR_NAME));
            return subAccessInfo.contains(AMAZONAWS_COM);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    boolean isUsableAWSAccessInfo(String accessInfo) {
        return !(accessInfo == null || accessInfo.isEmpty())
                && isAWSAccessInfo(accessInfo)
                && !hasNullAccessInfo(accessInfo);
    }

    boolean hasNullAccessInfo(String accessInfo) {
        return getPublicDNS(accessInfo).isEmpty()
                || getIPAddress(accessInfo).isEmpty()
                || getKeyPairName(accessInfo).isEmpty();
    }

    String getPublicDNS(String accessInfo) {
        try {
            return accessInfo.substring(0, accessInfo.indexOf(KEY_PAIR_NAME));
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    String getIPAddress(String accessInfo) {
        try {
            return accessInfo
                    .substring(accessInfo.indexOf("-"), accessInfo.indexOf("."))
                    .substring(1).replace('-', '.');
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    String getKeyPairName(String accessInfo) {
        try {
            return accessInfo.substring(accessInfo.indexOf(KEY_PAIR_NAME),
                    accessInfo.length());
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    void updateInstanceInfo(Subscription subscription, VOInstanceInfo info)
            throws ValidationException {
        new SubscriptionInstanceInfo(localizer, subscription)
                .validateAndUpdateInstanceInfo(info);
    }

    /**
     * Save udas for asynchronously modify or upgrade subscription. Store uda
     * value in modifieduda temporarily.
     * 
     * @param udas
     *            The list of VOUda with new value.
     * @param dbSubscription
     *            The subscription to be modified or upgraded.
     */
    void saveUdasForAsyncModifyOrUpgradeSubscription(List<VOUda> udas,
            Subscription dbSubscription) throws MandatoryUdaMissingException,
            MandatoryCustomerUdaMissingException, ValidationException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            OperationNotPermittedException, ConcurrentModificationException {
        Organization supplier = dbSubscription.getProduct()
                .getSupplierOrResellerTemplate().getVendor();
        manageBean.getUdaAccess().validateUdaAndAdaptTargetKey(udas, supplier,
                dbSubscription);
        List<VOUda> newUdas = new ArrayList<>();
        List<VOUda> defaultValueUdas = new ArrayList<>();
        for (VOUda voUda : udas) {
            if (voUda.getKey() > 0) {
                modUpgBean.storeModifiedUda(voUda.getKey(),
                        ModifiedEntityType.UDA_VALUE, voUda.getUdaValue(),
                        dbSubscription.getKey(),
                        voUda.getUdaDefinition().isEncrypted());
            } else {
                newUdas.add(voUda);
            }
        }

        for (VOUda voUda : newUdas) {
            VOUda defaultValueUda = new VOUda();
            defaultValueUda.setTargetObjectKey(voUda.getTargetObjectKey());
            defaultValueUda.setUdaDefinition(voUda.getUdaDefinition());
            if (voUda.getUdaDefinition().getDefaultValue() == null) {
                defaultValueUda.setUdaValue("");
            } else {
                defaultValueUda.setUdaValue(
                        voUda.getUdaDefinition().getDefaultValue());
            }
            defaultValueUdas.add(defaultValueUda);
        }
        manageBean.getUdaAccess().saveUdas(defaultValueUdas,
                dbSubscription.getOrganization());

        for (VOUda voUda : newUdas) {
            Uda uda = new Uda();
            uda.setTargetObjectKey(voUda.getTargetObjectKey());
            uda.setUdaDefinitionKey(voUda.getUdaDefinition().getKey());
            uda = (Uda) dataManager.getReferenceByBusinessKey(uda);
            modUpgBean.storeModifiedUda(uda.getKey(),
                    ModifiedEntityType.UDA_VALUE, voUda.getUdaValue(),
                    dbSubscription.getKey(),
                    uda.getUdaDefinition().isEncrypted());
        }
    }

    public SubscriptionHistoryDao getSubscriptionHistoryDao() {
        return new SubscriptionHistoryDao(dataManager);
    }

    public UsageLicenseDao getUsageLicenseDao() {
        return new UsageLicenseDao(dataManager);
    }

    public SubscriptionDao getSubscriptionDao() {
        return new SubscriptionDao(dataManager);
    }

    public MarketplaceDao getMarketplaceDao() {
        return new MarketplaceDao(dataManager);
    }

    public ModifiedEntityDao getModifiedEntityDao() {
        return new ModifiedEntityDao(dataManager);
    }

    public OrganizationDao getOrganizationDao() {

        return new OrganizationDao(dataManager);
    }

    public ProductDao getProductDao() {
        return new ProductDao(dataManager);
    }

    public SessionDao getSessionDao() {
        return new SessionDao(dataManager);
    }

    public BillingContactDao getBillingContactDao() {
        return new BillingContactDao(dataManager);
    }

    @Override
    @RolesAllowed({ "TECHNOLOGY_MANAGER" })
    public void updateAsyncOperationProgress(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress)
            throws OperationNotPermittedException, OperationStateException {
        ArgumentValidator.notNull("transactionId", transactionId);
        ArgumentValidator.notNull("status", status);

        operationRecordBean.updateOperationStatus(transactionId, status,
                progress);
    }

    @Override
    @RolesAllowed({ "TECHNOLOGY_MANAGER" })
    public void updateAsyncSubscriptionStatus(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("instance", instanceInfo);

        Subscription subscription = manageBean.findSubscription(subscriptionId,
                organizationId);
        subscription.setStatus(SubscriptionStatus.PENDING);
        List<PlatformUser> receivers = manageBean
                .getCustomerAdminsAndSubscriptionOwner(subscription);

        for (PlatformUser platformUser : receivers) {
            try {
                commService.sendMail(platformUser,
                        EmailType.SUBSCRIPTION_INSTANCE_NOT_FOUND,
                        new Object[] { subscriptionId,
                                instanceInfo.getInstanceId() },
                        subscription.getMarketplace());
            } catch (MailOperationException e) {
                // only log the exception and proceed
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_MODIFY_PARAMETER_OF_SUBSCRIPTION_CONFIRMING_FAILED);
            }
        }

    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    private List<Subscription> getSubscriptionsForUserInt(PlatformUser user,
            PaginationFullTextFilter pagination) {
        String fullTextFilterValue = pagination.getFullTextFilterValue();
        List<Subscription> subscriptions = Collections.emptyList();
        if (StringUtils.isNotEmpty(fullTextFilterValue)) {
            Collection<Long> subscriptionKeys = Collections.emptySet();
            try {
                subscriptionKeys = getFilteredOutSubscriptionKeys(
                        fullTextFilterValue);
            } catch (InvalidPhraseException e) {
                LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR);
            } catch (ObjectNotFoundException e) {
                LOG.logDebug("No subscription keys found");
            }
            if (!subscriptionKeys.isEmpty()) {
                subscriptions = getSubscriptionDao()
                        .getSubscriptionsForUserWithSubscriptionKeys(user,
                                pagination, subscriptionKeys);
            }
        } else {
            subscriptions = getSubscriptionDao().getSubscriptionsForUser(user,
                    pagination);
        }
        return subscriptions;
    }

    /**
     * Implementation of method which should return set of Long object, which
     * represents subscriptions retunred in full text search process
     * 
     * @param filterValue
     *            Text enetered by user to filter subscriptions by
     * @return Set of primary keys of subscriptions which are valid against the
     *         filter value or empty (not null!) set.
     */
    private Collection<Long> getFilteredOutSubscriptionKeys(String filterValue)
            throws InvalidPhraseException, ObjectNotFoundException {
        return subscriptionSearchService.searchSubscriptions(filterValue);
    }

    @Override
    public List<Subscription> getSubscriptionsForCurrentUserWithFiltering(
            PaginationFullTextFilter pagination) {
        PlatformUser user = dataManager.getCurrentUser();
        return getSubscriptionsForUserInt(user, pagination);
    }

    @Override
    public Integer getSubscriptionsSizeForCurrentUserWithFiltering(
            PaginationFullTextFilter pagination) {
        PlatformUser user = dataManager.getCurrentUser();
        return getSubscriptionsForUserInt(user, pagination).size();
    }

    @Override
    public UsageLicense getSubscriptionUsageLicense(PlatformUser user,
            Long subKey) {

        return getSubscriptionDao().getUserLicense(user,
                subKey == null ? -1 : subKey.longValue());
    }

    @Override
    public void removeSubscriptionOwner(Subscription sub) {
        sub.setOwner(null);
        dataManager.merge(sub);
    }

    @Override
    public Subscription getMySubscriptionDetails(long key) {
        return getSubscriptionDao().getMySubscriptionDetails(key);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public boolean unsubscribeFromService(Long key)
            throws ObjectNotFoundException, SubscriptionStillActiveException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationPendingException,
            OperationNotPermittedException {
        Subscription mySubscriptionDetails = getMySubscriptionDetails(
                key.longValue());
        return unsubscribeFromService(
                mySubscriptionDetails.getSubscriptionId());
    }

    @Override
    @RolesAllowed({ "TECHNOLOGY_MANAGER" })
    public void notifySubscriptionAboutVmsNumber(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("organizationId", organizationId);
        ArgumentValidator.notNull("instance", instanceInfo);

//        PlatformUser user = dataManager.getCurrentUser();
//
//        Subscription subscription = manageBean.findSubscription(subscriptionId,
//                organizationId);
//        PermissionCheck.owns(subscription.getProduct().getTechnicalProduct(),
//                user.getOrganization(), LOG, null);
//
//        List<ParameterDefinition> parameterDefinitions = subscription
//                .getProduct().getTechnicalProduct().getParameterDefinitions();
//
//        for (ParameterDefinition parameterDefinition : parameterDefinitions) {
//            if (!parameterDefinition.getParameterId()
//                    .equals(VMS_NUMBER_PARAM)) {
//                continue;
//            }
//            Parameter parameter = getSubscriptionDao()
//                    .getParameterForSubscription(parameterDefinition,
//                            subscription.getParameterSet());
//            if (parameter == null) {
//                parameter = new Parameter();
//                parameter.setParameterDefinition(parameterDefinition);
//                parameter.setParameterSet(subscription.getParameterSet());
//            }
//            parameter.setValue(Integer.toString(instanceInfo.getVmsNumber()));
//            dataManager.merge(parameter);
//            return;
//        }
    }
}
