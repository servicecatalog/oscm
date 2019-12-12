/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: Peter Pock
 *
 *  Creation Date: 18.02.2009
 *
 *  Completion Time: 18.02.2009
 *
 *******************************************************************************/

package org.oscm.accountservice.bean;

import org.oscm.accountservice.assembler.*;
import org.oscm.accountservice.dao.PaymentTypeDao;
import org.oscm.accountservice.dataaccess.UdaAccess;
import org.oscm.accountservice.dataaccess.UdaDefinitionAccess;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.data.SendMailStatus.SendMailStatusItem;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.id.IdGenerator;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.interceptor.*;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.ImageType.ImageOwnerType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.*;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.string.Strings;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.triggerservice.bean.TriggerProcessIdentifiers;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.triggerservice.validator.TriggerProcessValidator;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.*;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.ImageValidator;
import org.oscm.validation.PaymentDataValidator;
import org.oscm.validator.OrganizationRoleValidator;
import org.oscm.vo.BaseAssembler;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

/**
 * Session Bean implementation class AccountServiceBean
 */
@DeclareRoles("ORGANIZATION_ADMIN")
@Stateless
@Remote(AccountService.class)
@Local(AccountServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class,
        AuditLogDataInterceptor.class })
public class AccountServiceBean implements AccountService, AccountServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AccountServiceBean.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @EJB(beanInterface = IdentityServiceLocal.class)
    protected IdentityServiceLocal im;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    private SubscriptionServiceLocal sm;

    @EJB(beanInterface = CommunicationServiceLocal.class)
    protected CommunicationServiceLocal cs;

    @EJB(beanInterface = LdapAccessServiceLocal.class)
    LdapAccessServiceLocal ldapAccess;

    @EJB(beanInterface = LdapSettingsManagementServiceLocal.class)
    LdapSettingsManagementServiceLocal ldapSettingsMgmt;

    @EJB(beanInterface = PaymentServiceLocal.class)
    protected PaymentServiceLocal paymentService;

    @EJB(beanInterface = TriggerQueueServiceLocal.class)
    protected TriggerQueueServiceLocal triggerQS;

    @EJB(beanInterface = ApplicationServiceLocal.class)
    protected ApplicationServiceLocal appManager;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal configService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = ImageResourceServiceLocal.class)
    protected ImageResourceServiceLocal imgSrv;

    @EJB(beanInterface = MarketingPermissionServiceLocal.class)
    protected MarketingPermissionServiceLocal marketingPermissionService;

    @EJB(beanInterface = MarketplaceServiceLocal.class)
    protected MarketplaceServiceLocal marketplaceService;

    @EJB
    SubscriptionAuditLogCollector subscriptionAuditLogCollector;

    @Inject
    UserLicenseServiceLocalBean userLicenseService;

    @Inject
    PaymentTypeDao paymentTypeDao;

    @Resource
    protected SessionContext sessionCtx;

    final static String SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION = "SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION";
    final static String DEFAULT_USERGROUP_NAME = "default";

    /**
     * Default constructor.
     */
    public AccountServiceBean() {
        // Default constructor.
    }

    /*
     * Read the caller principal name from the session context and get the
     * organization for this name.
     *
     * @return the organization for the caller principal name.
     */
    Organization getOrganization() {

        // determine the correlating organization
        Organization result = dm.getCurrentUser().getOrganization();

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void deregisterOrganization() throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        Organization organization = getOrganization();
        deregisterOrganization(organization);

    }

    @Override
    public String getOrganizationId(long subscriptionKey)
            throws ObjectNotFoundException, ServiceParameterException,
            OperationNotPermittedException, SubscriptionStateException {

        String organizationId = null;
        Subscription subscription = sm.loadSubscription(subscriptionKey);

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            ServiceParameterException ex = new ServiceParameterException(
                    "subscription '" + subscriptionKey + "' has expired.",
                    ParameterType.PLATFORM_PARAMETER,
                    PlatformParameterIdentifiers.PERIOD, null);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_GET_ORGANIZATION_ID_FAILED);
            throw ex;
        } else if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
                SubscriptionStateException e = new SubscriptionStateException(
                        "Subscription '" + subscriptionKey + "' not active.",
                        SubscriptionStateException.Reason.ONLY_ACTIVE);
                logger.logError(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        e, LogMessageIdentifier.ERROR_SUBSCRIPTION_NOT_ACTIVE,
                        Long.toString(subscriptionKey));
                throw e;
            }
            OperationNotPermittedException ex = new OperationNotPermittedException(
                    "subscription '" + subscriptionKey + "' is not active.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_GET_ORGANIZATION_ID_FAILED);
            throw ex;
        }

        organizationId = subscription.getOrganization().getOrganizationId();

        return organizationId;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public VOOrganization getOrganizationData() {

        final Organization organization = getOrganization();
        VOOrganization result = OrganizationAssembler.toVOOrganization(
                organization, isImageDefined(organization), new LocalizerFacade(
                        localizer, dm.getCurrentUser().getLocale()));

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOOrganization getOrganizationDataFallback() {

        VOOrganization result = getOrganizationData();
        if ((result.getDescription() == null
                || result.getDescription().isEmpty())
                && !"en".equals(dm.getCurrentUser().getLocale())) {
            final Organization organization = getOrganization();
            String description = getEnDescription(organization.getKey());
            result.setDescription(description);
        }

        return result;
    }

    String getEnDescription(long orgKey) {
        LocalizerFacade localizerEn = new LocalizerFacade(localizer, "en");
        return localizerEn.getText(orgKey,
                LocalizedObjectTypes.ORGANIZATION_DESCRIPTION);
    }

    @Override
    public String getLocalizedAttributeName(long key, String locale) {
        List<VOLocalizedText> texts = localizer.getLocalizedValues(key,
                LocalizedObjectTypes.CUSTOM_ATTRIBUTE_NAME);
        for (VOLocalizedText text : texts) {
            if (text.getLocale().equals(locale)) {
                return text.getText();
            }
        }
        return "";
    }

    /**
     * Returns true if an image is defined for the product with the given key.
     *
     * @param product
     *            The product.
     * @return true if an image is defined for the product with the given key.
     */
    public boolean isImageDefined(Organization organization) {

        boolean flag = imgSrv.read(organization.getKey(),
                ImageType.ORGANIZATION_IMAGE) != null;

        return flag;
    }

    @Override
    public VOOrganization registerCustomer(VOOrganization organization,
            VOUserDetails admin, String password, Long serviceKey,
            String marketplaceId, String sellerId)
            throws NonUniqueBusinessKeyException, ValidationException,
            ObjectNotFoundException, MailOperationException,
            RegistrationException {

        ArgumentValidator.notNull("organization", organization);
        ArgumentValidator.notNull("admin", admin);

        try {
            // if CUSTOMER_SELF_REGISTRATION_ENABLED is set to false, a
            // RegistrationException will be thrown
            if (!configService.isCustomerSelfRegistrationEnabled()) {
                RegistrationException rf = new RegistrationException(
                        "Do not permit self-registration.",
                        RegistrationException.Reason.SELFREGISTRATION_NOT_ALLOWED);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, rf,
                        LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED,
                        organization.getOrganizationId());
                throw rf;
            }
            // validate the organization value object,
            // create the domain object and persist it
            Organization org = OrganizationAssembler.toCustomer(organization);

            Organization storedSupplier = null;
            // if a supplier set create the relation with customer
            if (sellerId != null && sellerId.length() > 0) {

                Organization supplier = new Organization();
                supplier.setOrganizationId(sellerId);
                storedSupplier = (Organization) dm
                        .getReferenceByBusinessKey(supplier);

                // if the specified supplier organization does not have the role
                // supplier, throw an exception
                if (!storedSupplier.hasRole(OrganizationRoleType.SUPPLIER)
                        && !storedSupplier.hasRole(OrganizationRoleType.BROKER)
                        && !storedSupplier
                                .hasRole(OrganizationRoleType.RESELLER)) {
                    RegistrationException rf = new RegistrationException(
                            "The organization '" + storedSupplier.getKey()
                                    + "' specified for registration does not have the SUPPLIER, BROKER or RESELLER role.",
                            RegistrationException.Reason.TARGET_ORG_INVALID);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, rf,
                            LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED,
                            organization.getOrganizationId());
                    throw rf;
                }

            }

            setDomicileCountry(org, organization.getDomicileCountry());

            // set default cut-off day (db unique constrain)
            org.setCutOffDay(1);

            updateOrganizationDescription(org.getKey(),
                    organization.getDescription());

            org.setRegistrationDate(
                    DateFactory.getInstance().getTransactionTime());
            org = saveOrganizationWithUniqueIdAndInvoicePayment(org,
                    admin.getLocale());

            OrganizationReference refSuplCust = null;
            if (storedSupplier != null) {
                refSuplCust = new OrganizationReference(storedSupplier, org,
                        OrganizationReferenceType.getOrgRefTypeForSourceRoles(
                                storedSupplier.getGrantedRoleTypes()));
                dm.persist(refSuplCust);
            }

            dm.persist(createDefaultUserGroup(org));

            assignOrganizationRole(org, OrganizationRoleType.CUSTOMER);

            Marketplace marketplace = getMarketplace(marketplaceId);

            createOrganizationAdmin(org, admin, password, serviceKey,
                    marketplace);

            dm.flush();
            dm.refresh(org);

            if (refSuplCust != null) {
                enableSuppliersDefaultPaymentsForCustomer(refSuplCust);
            }

            organization = OrganizationAssembler.toVOOrganization(org,
                    isImageDefined(org), new LocalizerFacade(localizer, "en"));

        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (MailOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return organization;
    }

    /**
     * If a persist operation fails with an business key violation although the
     * object does not have a business key defined, log an error and throw a
     * system exception.
     *
     * @param e
     *            The caught exception.
     */
    private void handleImpossibleBKViolation(NonUniqueBusinessKeyException e) {
        SaaSSystemException sse = new SaaSSystemException(
                "Caught SaasNonUniqueBusinessKeyException although there is no business key",
                e);
        logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                LogMessageIdentifier.ERROR_UNEXPECTED_BK_VIOLATION);
        throw sse;
    }

    /**
     * @see org.oscm.internal.intf.AccountService#updateAccountInformation(org.oscm.internal.vo.VOOrganization,
     *      org.oscm.internal.vo.VOUserDetails, java.lang.String,
     *      org.oscm.internal.vo.VOImageResource)
     */
    @Override
    public void updateAccountInformation(VOOrganization voOrganization,
            VOUserDetails voUser, String marketplaceId,
            VOImageResource voImageResource)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ObjectNotFoundException,
            DistinguishedNameException, ConcurrentModificationException,
            ImageException {

        try {
            Organization organization = null;
            if (voOrganization != null) {
                PlatformUser currentUser = dm.getCurrentUser();
                organization = currentUser.getOrganization();

                if (organization.hasRole(OrganizationRoleType.SUPPLIER)
                        || organization.hasRole(
                                OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        || organization.hasRole(OrganizationRoleType.RESELLER)
                        || organization.hasRole(OrganizationRoleType.BROKER)) {
                    OrganizationAssembler.updateVendor(organization,
                            voOrganization);
                } else {
                    if (voImageResource != null) {
                        // only suppliers and technology providers are allowed
                        // to save images
                        throw new ImageException(
                                "Saving an image for organization '"
                                        + organization.getKey()
                                        + "' is not allowed because it is not a supplier or technology provider.",
                                ImageException.Reason.NO_SELLER);
                    }
                    OrganizationAssembler.updateCustomer(organization,
                            voOrganization);
                }

                setDomicileCountry(organization,
                        voOrganization.getDomicileCountry());

                updateOrganizationDescription(voOrganization.getKey(),
                        voOrganization.getDescription());

                if (voImageResource != null) {
                    final ImageResource imageResource = new ImageResource();
                    imageResource.setObjectKey(organization.getKey());
                    imageResource
                            .setContentType(voImageResource.getContentType());
                    imageResource.setBuffer(voImageResource.getBuffer());
                    imageResource.setImageType(voImageResource.getImageType());
                    processImage(imageResource, organization.getKey());
                }
            }

            updateAccountInformation(organization, voUser, marketplaceId);

        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (TechnicalServiceNotAliveException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (TechnicalServiceOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (OperationNotPermittedException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (DistinguishedNameException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ImageException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

    }

    /**
     * @see org.oscm.accountservice.local.AccountServiceLocal#updateAccountInformation(org.oscm.internal.vo.VOOrganization,
     *      org.oscm.internal.vo.VOUserDetails, java.lang.String)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateAccountInformation(Organization organization,
            VOUserDetails voUser, String marketplaceId)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, DistinguishedNameException,
            ConcurrentModificationException {

        PlatformUser currentUser = dm.getCurrentUser();
        PlatformUser oldUser = currentUser.getEmail() != null
                ? UserDataAssembler.copyPlatformUser(currentUser) : null;

        if (voUser != null) {
            im.modifyUserData(currentUser, voUser, true, false);
        }
        if (organization != null) {
            if (!currentUser.isOrganizationAdmin()) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "User is not permitted to modify the organization data.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        onp,
                        LogMessageIdentifier.WARN_ORGANIZATION_DATA_MODIFICATION_FAILED,
                        currentUser.getUserId(),
                        Long.toString(organization.getKey()));
                throw onp;
            }

            checkDistinguishedName(organization);
            sendMail(marketplaceId, currentUser, oldUser);
        }

        if (voUser != null && organization == null) {
            sendMail(marketplaceId, currentUser, oldUser);
        }

    }

    private void sendMail(String marketplaceId, PlatformUser currentUser,
            PlatformUser oldUser) {
        // Send an email to the current organization admin
        Marketplace marketplace = getMarketplace(marketplaceId);

        // bugfix 8183
        List<PlatformUser> platformUsers = new LinkedList<>();
        platformUsers.add(currentUser);

        if (oldUser != null && !oldUser.getEmail().trim()
                .equals(currentUser.getEmail().trim())) {
            // If user email is set, memorize email address
            platformUsers.add(oldUser);
        }

        SendMailStatus<PlatformUser> mailStatus = cs.sendMail(
                EmailType.ORGANIZATION_UPDATED, null, marketplace,
                platformUsers.toArray(new PlatformUser[platformUsers.size()]));

        for (SendMailStatusItem<PlatformUser> sendMailStatusItem : mailStatus
                .getMailStatus()) {
            if (sendMailStatusItem.errorOccurred()) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        sendMailStatusItem.getException(),
                        LogMessageIdentifier.WARN_MAIL_ORGANIZATION_UPDATED_FAILED);
            }
        }
    }

    protected Marketplace getMarketplace(String marketplaceId) {
        if (marketplaceId == null || marketplaceId.trim().length() == 0) {
            return null;
        }
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        try {
            marketplace = (Marketplace) dm
                    .getReferenceByBusinessKey(marketplace);
        } catch (ObjectNotFoundException e) {
            // if we don't find it, return null
            logger.logDebug("Marketplace not found: " + e.getMessage());
            marketplace = null;
        }
        return marketplace;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public VOOrganization getMyCustomer(VOOrganization org, String locale)
            throws ObjectNotFoundException {
        PlatformUser user = dm.getCurrentUser();
        Organization seller = user.getOrganization();
        OrganizationReferenceType referenceType = getCustomerReferenceType(
                seller);
        Organization customer = getOrganization(org.getKey(),
                org.getOrganizationId(), OrganizationRoleType.CUSTOMER);
        OrganizationReference customerSupplierAssociation = getOrgReferenceByKey(
                customer, seller.getKey(), referenceType);

        VOOrganization result = OrganizationAssembler
                .toVOOrganizationWithDiscount(customer, false,
                        customerSupplierAssociation.getDiscount(),
                        new LocalizerFacade(localizer, locale));
        return result;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOOrganization> getMyCustomers() {
        List<VOOrganization> result = new ArrayList<>();

        PlatformUser user = dm.getCurrentUser();
        Organization seller = user.getOrganization();

        List<Organization> list = getCustomers(seller);
        OrganizationReferenceType referenceType = getCustomerReferenceType(
                seller);

        for (Organization customer : list) {
            OrganizationReference customerSupplierAssociation = getOrgReferenceByKey(
                    customer, seller.getKey(), referenceType);
            result.add(OrganizationAssembler.toVOOrganizationWithDiscount(
                    customer, false, customerSupplierAssociation.getDiscount(),
                    new LocalizerFacade(localizer, user.getLocale())));
        }

        return result;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOOrganization> getMyCustomersOptimization() {
        List<VOOrganization> result = new ArrayList<>();

        PlatformUser user = dm.getCurrentUser();
        Organization seller = user.getOrganization();

        List<Organization> list = getCustomersOptimization(seller);

        for (Organization customer : list) {
            result.add(OrganizationAssembler.toVOOrganization(customer, false,
                    null, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS));
        }

        return result;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Organization> getCustomers(Organization seller) {

        OrganizationReferenceType referenceType = getCustomerReferenceType(
                seller);
        Query query = dm.createNamedQuery("Organization.getForSupplierKey");
        query.setParameter("supplierKey", Long.valueOf(seller.getKey()));
        query.setParameter("referenceType", referenceType);
        List<Organization> list = ParameterizedTypes.list(query.getResultList(),
                Organization.class);

        return list;
    }

    @SuppressWarnings("boxing")
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Organization> getCustomersOptimization(Organization seller) {
        final String queryString = "SELECT o.tkey, o.organizationid, o.name, o.address "
                + "FROM Organization o, OrganizationReference orgref "
                + "WHERE orgref.targetkey=o.tkey "
                + "AND orgref.sourcekey=:supplierKey "
                + "AND orgref.referencetype = :referenceType "
                + "ORDER BY o.tkey ASC";
        final String referenceType = getReferenceTypeAsString(seller);
        Query query = dm.createNativeQuery(queryString);
        query.setParameter("supplierKey", Long.valueOf(seller.getKey()));
        query.setParameter("referenceType", referenceType);
        List<Object[]> result = ParameterizedTypes.list(query.getResultList(),
                Object[].class);
        List<Organization> customerList = new ArrayList<>();
        for (Object[] resultElement : result) {
            Organization customer = new Organization();
            customer.setKey(new Long(resultElement[0].toString()));
            customer.setOrganizationId(resultElement[1].toString());
            if (resultElement[2] != null) {
                customer.setName(resultElement[2].toString());
            }
            if (resultElement[3] != null) {
                customer.setAddress(resultElement[3].toString());
            }
            customerList.add(customer);
        }
        return customerList;
    }

    private String getReferenceTypeAsString(Organization seller) {
        final String referenceType;
        Set<OrganizationRoleType> sourceRoles = seller.getGrantedRoleTypes();
        if (sourceRoles.contains(OrganizationRoleType.SUPPLIER)) {
            referenceType = "SUPPLIER_TO_CUSTOMER";
        } else if (sourceRoles.contains(OrganizationRoleType.RESELLER)) {
            referenceType = "RESELLER_TO_CUSTOMER";
        } else if (sourceRoles.contains(OrganizationRoleType.BROKER)) {
            referenceType = "BROKER_TO_CUSTOMER";
        } else {
            referenceType = "";
        }
        return referenceType;
    }

    private OrganizationReference getOrgReferenceByKey(Organization customer,
            long supplierId, OrganizationReferenceType referenceType) {

        for (OrganizationReference customerSupplierAssociation : customer
                .getSources()) {

            if (customerSupplierAssociation.getSourceKey() == supplierId
                    && customerSupplierAssociation.getTargetKey() == customer
                            .getKey()
                    && customerSupplierAssociation
                            .getReferenceType() == referenceType) {
                return customerSupplierAssociation;
            }
        }

        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean removeOverdueOrganizations(long currentTime) {

        boolean successfulExecution = true;
        List<PlatformUser> overdueOrganizationAdmins = im
                .getOverdueOrganizationAdmins(currentTime);

        for (PlatformUser userToBeRemoved : overdueOrganizationAdmins) {
            try {
                // call has to be made by calling into the container again, so
                // that the new transactional behaviour is considered.
                prepareForNewTransaction().removeOverdueOrganization(
                        userToBeRemoved.getOrganization());
            } catch (Exception e) {
                successfulExecution = false;
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_ORGANIZATION_DELETION_FAILED,
                        Long.toString(
                                userToBeRemoved.getOrganization().getKey()));
                // logging is sufficient for now, so simply proceed
            }
        }

        return successfulExecution;
    }

    private AccountServiceLocal prepareForNewTransaction() {
        DateFactory.getInstance().takeCurrentTime();
        return sessionCtx.getBusinessObject(AccountServiceLocal.class);
    }

    /**
     * Internal method with the only purpose to delegate the call to the
     * deregistration method. As the business logic has to be considered as one
     * transaction for every call, the transaction annotation is set
     * accordingly.
     *
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     * @see AccountServiceLocal#removeOverdueOrganization(Organization)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void removeOverdueOrganization(Organization organization)
            throws DeletionConstraintException, ObjectNotFoundException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        // new transaction, so load organization first
        Organization savedOrganization = dm.getReference(Organization.class,
                organization.getKey());
        deregisterOrganization(savedOrganization);

    }

    private void deregisterOrganization(Organization organization)
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        // If the organization has never held any subscription, its data
        // (including its users data and its payment info) are irrevocably
        // deleted.

        if (organization.hasAtLeastOneRole(OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.RESELLER, OrganizationRoleType.BROKER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.PLATFORM_OPERATOR,
                OrganizationRoleType.MARKETPLACE_OWNER)) {
            throw new DeletionConstraintException(
                    "Only deregistration of customer organizations is allowed!");
        }

        List<Subscription> subscriptionList = organization.getSubscriptions();
        if (subscriptionList == null || subscriptionList.isEmpty()) {
            long userKey = 0;
            // delete the users
            try {
                List<PlatformUser> userList = organization.getPlatformUsers();
                if (userList != null) {
                    for (PlatformUser user : userList) {
                        try {
                            userKey = user.getKey();
                            im.deletePlatformUser(user, null);
                        } catch (ObjectNotFoundException e) {
                            // if the user is already deleted, do nothing
                        }
                    }
                }
            } catch (UserDeletionConstraintException e) {
                // the organization never had any subscription
                // this exception should never be caught
                SaaSSystemException se = new SaaSSystemException(
                        "Cannot delete the platform users although the organization never had any subscription!",
                        e);
                logger.logError(Log4jLogger.SYSTEM_LOG, se,
                        LogMessageIdentifier.ERROR_USER_DELETION_FAILED,
                        Long.toString(userKey));
                throw se;
            }

            // delete the organization
            dm.remove(organization);
            dm.flush();
        } else {
            for (Subscription sub : subscriptionList) {
                if (sub.getStatus() != SubscriptionStatus.DEACTIVATED) {
                    // deregister is not possible
                    DeletionConstraintException sdce = new DeletionConstraintException(
                            ClassEnum.ORGANIZATION,
                            organization.getOrganizationId(),
                            ClassEnum.SUBSCRIPTION);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, sdce,
                            LogMessageIdentifier.WARN_ORGANIZATION_DEREGISTRATION_FAILED);
                    throw sdce;
                }
            }
            organization.setDeregistrationDate(Long
                    .valueOf(DateFactory.getInstance().getTransactionTime()));

        }

    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void addSuppliersForTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws ObjectNotFoundException, OperationNotPermittedException,
            AddMarketingPermissionException {

        // check constraints
        ArgumentValidator.notNull("technicalService", technicalService);
        ArgumentValidator.notNullNotEmpty("organizationIds", organizationIds);
        Organization provider = dm.getCurrentUser().getOrganization();
        PermissionCheck.owns(
                dm.find(TechnicalProduct.class, technicalService.getKey()),
                provider, logger, null);

        marketingPermissionService.addMarketingPermission(provider,
                technicalService.getKey(), organizationIds);

    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void removeSuppliersFromTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException,
            MarketingPermissionNotFoundException {

        ArgumentValidator.notNull("technicalService", technicalService);
        ArgumentValidator.notNull("organizationIds", organizationIds);
        marketingPermissionService.removeMarketingPermission(
                technicalService.getKey(), organizationIds);

    }

    //TODO: Implement uniqueness check here
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @Interceptors({ LdapInterceptor.class })
    public Organization registerOrganization(Organization organization,
            ImageResource imageResource, VOUserDetails user,
            Properties organizationProperties, String domicileCountry,
            String marketplaceId, String description,
            OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException, ObjectNotFoundException,
            IncompatibleRolesException, OrganizationAuthorityException {

        if(checkIfOrganizationAlreadyExists(organization)) {
            throw new NonUniqueBusinessKeyException(
                    ClassEnum.ORGANIZATION, organization.getOrganizationId());
        }

        long tenantKey = organization.getTenant() == null ? 0
                : organization.getTenant().getKey();
        if (checkIfPlatformUserInGivenTenantExists(tenantKey,
                user.getUserId())) {
            throw new NonUniqueBusinessKeyException(
                    DomainObjectException.ClassEnum.USER, user.getUserId());
        }
        for (OrganizationRoleType roleToSet : roles) {
            if (roleToSet.equals(OrganizationRoleType.PLATFORM_OPERATOR)) {
                OrganizationAuthorityException ioa = new OrganizationAuthorityException(
                        "New Organization to be created must not be of role "
                                + OrganizationRoleType.PLATFORM_OPERATOR,
                        new Object[] {
                                OrganizationRoleType.PLATFORM_OPERATOR });
                throw ioa;
            }
        }

        // set default cut-off day (db unique constraint)
        organization.setCutOffDay(1);
        organization.setRegistrationDate(
                DateFactory.getInstance().getTransactionTime());
        Organization storedOrganization = saveOrganizationWithUniqueIdAndInvoicePayment(
                organization, user.getLocale());

        grantAccessToTheMarketplace(marketplaceId, storedOrganization);

        setDomicileCountry(storedOrganization, domicileCountry);

        updateOrganizationDescription(organization.getKey(), description);

        if (imageResource != null) {
            imageResource.setObjectKey(storedOrganization.getKey());
            processImage(imageResource, storedOrganization.getKey());
        }

        dm.persist(createDefaultUserGroup(storedOrganization));

        try {

            if (organizationProperties != null) {
                // provide meaningful defaults if mandatory parameters not given
                if (!organizationProperties
                        .containsKey(SettingType.LDAP_CONTEXT_FACTORY.name())) {
                    organizationProperties.put(
                            SettingType.LDAP_CONTEXT_FACTORY.name(),
                            ldapSettingsMgmt.getDefaultValueForSetting(
                                    SettingType.LDAP_CONTEXT_FACTORY));
                }
                if (!organizationProperties
                        .containsKey(SettingType.LDAP_ATTR_UID.name())) {
                    organizationProperties.put(SettingType.LDAP_ATTR_UID.name(),
                            ldapSettingsMgmt.getDefaultValueForSetting(
                                    SettingType.LDAP_ATTR_UID));
                }

                ldapSettingsMgmt.setOrganizationSettings(
                        storedOrganization.getOrganizationId(),
                        organizationProperties);

                Properties propsResolved = ldapSettingsMgmt
                        .getOrganizationSettingsResolved(
                                storedOrganization.getOrganizationId());

                LdapConnector connector = new LdapConnector(ldapAccess,
                        propsResolved);
                connector.ensureAllMandatoryLdapPropertiesPresent();

                user = connector.validateLdapProperties(user);
                storedOrganization.setRemoteLdapActive(true);
            }

            OrganizationRoleValidator.containsMultipleSellerRoles(
                    Arrays.asList(roles),
                    LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INCOMPATIBLE_ROLES_GRANTED);
            assignOrganizationRole(storedOrganization, roles);
            boolean foundCustRole = false;
            for (OrganizationRoleType roleToSet : roles) {
                if (roleToSet.equals(OrganizationRoleType.CUSTOMER)) {
                    foundCustRole = true;
                    break;
                }
            }
            // assign CUSTOMER role for any organization if is not assigned
            if (!foundCustRole) {
                assignOrganizationRole(storedOrganization,
                        OrganizationRoleType.CUSTOMER);
            }

            createAdminWithCorrespondingUserRoles(organization, user,
                    marketplaceId, roles);
        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (IncompatibleRolesException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        dm.flush();
        dm.refresh(storedOrganization);

        addSelfReferenceAsCustomer(storedOrganization);
        dm.flush();
        dm.refresh(storedOrganization);

        return storedOrganization;
    }

    private void grantAccessToTheMarketplace(String marketplaceId,
            Organization storedOrganization)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {

        if (marketplaceId == null || "".equals(marketplaceId)) {
            return;
        }

        Marketplace marketplace = marketplaceService
                .getMarketplaceForId(marketplaceId);

        if (marketplace.isRestricted()) {
            marketplaceService.grantAccessToMarketPlaceToOrganization(
                    marketplace, storedOrganization);
        }
    }

    UserGroup createDefaultUserGroup(Organization org) {
        UserGroup group = new UserGroup();
        group.setOrganization(org);
        group.setIsDefault(true);
        group.setName(DEFAULT_USERGROUP_NAME);
        return group;
    }

    /**
     * Create the organization admin. The first user is created implicitly. He
     * will receive the user roles that correspond the the given organization
     * roles.
     *
     */
    void createAdminWithCorrespondingUserRoles(Organization organization,
            VOUserDetails user, String marketplaceId,
            OrganizationRoleType... roles) throws ValidationException,
            MailOperationException, NonUniqueBusinessKeyException {

        user.setUserRoles(OrganizationRoleType
                .correspondingUserRoles(Arrays.asList(roles)));
        createOrganizationAdmin(organization, user, null, null,
                getMarketplace(marketplaceId));

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Organization addOrganizationToRole(String organizationId,
            OrganizationRoleType role) throws ObjectNotFoundException,
            AddMarketingPermissionException, IncompatibleRolesException {

        Organization org = new Organization();
        org.setOrganizationId(organizationId);
        org = (Organization) dm.getReferenceByBusinessKey(org);

        if (!org.hasRole(role)) {
            // Check if the new role is incompatible with the existing roles
            Set<OrganizationRoleType> roleTypes = org.getGrantedRoleTypes();
            roleTypes.add(role);
            OrganizationRoleValidator.containsMultipleSellerRoles(roleTypes,
                    LogMessageIdentifier.WARN_ADDING_INCOMPATIBLE_ROLE_TO_ORGANIZATION_FAILED);

            OrganizationRole orgRole = new OrganizationRole();
            orgRole.setRoleName(role);
            orgRole = (OrganizationRole) dm.find(orgRole);
            OrganizationToRole orgToRole = new OrganizationToRole();
            orgToRole.setOrganization(org);
            orgToRole.setOrganizationRole(orgRole);

            try {
                dm.persist(orgToRole);
                dm.flush();
                dm.refresh(org);

                addMarketingPermissions(org);

                // Bug 7549 add customer to supplier entry
                addSelfReferenceAsCustomer(org);
            } catch (NonUniqueBusinessKeyException e) {
                // The organizationToRole entity has no business key, so this
                // scenario should never occur. So if it does, log it and throw
                // a SaasSystemException
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_NONUNIQUEKEY_EXCEPTION_ALTHOUGH_NO_BUSINESS_KEY);
                throw new SaaSSystemException(
                        "Persisting new role relation of organization failed.",
                        e);
            }
        }

        return org;
    }

    /**
     * Checks if the organization is supplier as well as technology provider. If
     * it is, the organization will be registered as a supplier of itself. For
     * all technical services a marketing permission is created.
     *
     * @param organization
     *            The organization to update the supplier list for.
     * @throws AddMarketingPermissionException
     * @throws ObjectNotFoundException
     */
    private void addMarketingPermissions(Organization organization)
            throws ObjectNotFoundException, AddMarketingPermissionException {
        // get granted roles
        List<OrganizationRoleType> roleList = new ArrayList<>();
        for (OrganizationToRole orgToRole : organization.getGrantedRoles()) {
            roleList.add(orgToRole.getOrganizationRole().getRoleName());
        }

        // if supplier and technology provider => create marketing permission
        if (roleList.contains(OrganizationRoleType.SUPPLIER) && roleList
                .contains(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
            if (!organization.getGrantedSuppliers().contains(organization)) {
                for (TechnicalProduct tp : organization
                        .getTechnicalProducts()) {
                    marketingPermissionService.addMarketingPermission(
                            organization, tp.getKey(),
                            Collections.singletonList(
                                    organization.getOrganizationId()));
                }
            }
            dm.flush();
            dm.refresh(organization);
        }
    }

    /**
     * The vendor organization will be registered as a customer of itself.
     *
     * @param organization
     *            The organization to update the customer list for.
     * @throws NonUniqueBusinessKeyException
     */
    void addSelfReferenceAsCustomer(Organization organization)
            throws NonUniqueBusinessKeyException {
        Set<OrganizationRoleType> sourceRoles = new HashSet<>();
        for (OrganizationToRole orgToRole : organization.getGrantedRoles()) {
            sourceRoles.add(orgToRole.getOrganizationRole().getRoleName());
        }
        OrganizationReferenceType referenceType = OrganizationReferenceType
                .getOrgRefTypeForSourceRoles(sourceRoles);
        if (referenceType != null) {
            // use vendor of customer list because the list size is often
            // shorter than get customer of vendor
            if (!organization.getVendorsOfCustomer().contains(organization)) {
                OrganizationReference ref = new OrganizationReference(
                        organization, organization, referenceType);
                dm.persist(ref);
            }
            dm.flush();
            dm.refresh(organization);
        }
    }

    /**
     * Creates the given user for the given organization.
     *
     * @param referenceOrganization
     *            The organization the user belongs to.
     * @param userToCreate
     *            The user to be created.
     * @param password
     *            The password to be created for the user. If it is
     *            <code>null</code>, a password will be generated.
     * @param serviceKey
     *            The id of the service the user wants to subscribe. If this
     *            value is <code>null</code> it'll be ignored for further
     *            processing.
     * @param marketplace
     *            the marketplace context
     * @throws ValidationException
     *             Thrown in case the user data could not be validated.
     * @throws MailOperationException
     *             Thrown in case the mail could not be deliver
     */
    private void createOrganizationAdmin(Organization referenceOrganization,
            VOUserDetails userToCreate, String password, Long serviceKey,
            Marketplace marketplace) throws ValidationException,
            MailOperationException, NonUniqueBusinessKeyException {

        // create the organization admin
        if (marketplace != null) {
            referenceOrganization.setTenant(marketplace.getTenant());
        }
        userToCreate
                .setOrganizationId(referenceOrganization.getOrganizationId());
        try {
            im.createOrganizationAdmin(userToCreate, referenceOrganization,
                    password, serviceKey, marketplace);
        } catch (NonUniqueBusinessKeyException e) {
            // this might happen if another organization already contains a user
            // with the same name (user id's must be unique system wide!)
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORGANIZATION_REGISTRATION_FAILED,
                    referenceOrganization.getOrganizationId());
            throw e;
        } catch (ObjectNotFoundException e) {
            // this should never happen because we created the organization
            // some steps before
            SaaSSystemException se = new SaaSSystemException(
                    "Organization '" + referenceOrganization.getOrganizationId()
                            + "' not found although we created him!");
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_ORGANIZATION_REGISTRATION_FAILED);
            throw se;
        }
    }

    /**
     * Creates a random id for the organization, set's the value on it and
     * persists it. Additionally a {@link PaymentInfo} of
     * {@link PaymentType#INVOICE} will be created.
     *
     * @param organization
     *            The organization to be enhanced and persisted.
     * @return The saved organization.
     */
    private Organization saveOrganizationWithUniqueIdAndInvoicePayment(
            Organization organization, String locale) {
        // we use a random number as custumerId, if the number is already
        // used we must try another number
        int i = 0;
        String organizationId = null;
        while (organizationId == null) {
            organizationId = IdGenerator.generateArtificialIdentifier();
            organization.setOrganizationId(organizationId);
            try {
                dm.persist(organization);
            } catch (NonUniqueBusinessKeyException e) {
                // stop after 100 tries
                i++;
                if (i > 100) {
                    SaaSSystemException se = new SaaSSystemException(
                            "No free organizationId found!");
                    logger.logError(Log4jLogger.SYSTEM_LOG, se,
                            LogMessageIdentifier.ERROR_ORGANIZATION_REGISTRATION_FAILED);
                    throw se;
                }
                organizationId = null;
            }
        }
        // create the payment info of type INVOICE for each new organization
        // because it cannot be explicitly created (at least from UI)
        try {
            PaymentType pt = findPaymentType(PaymentType.INVOICE);
            PaymentInfo pi = new PaymentInfo(
                    DateFactory.getInstance().getTransactionTime());
            pi.setPaymentType(pt);
            pi.setExternalIdentifier(null);
            String paymentInfoId = getLocalizedPaymentInfoId(locale,
                    pt.getKey());

            if (paymentInfoId == null || paymentInfoId.trim().length() == 0) {
                paymentInfoId = PaymentType.INVOICE;
            }
            pi.setPaymentInfoId(paymentInfoId);
            pi.setOrganization(organization);
            dm.persist(pi);
            organization.getPaymentInfos().add(pi);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "PaymentType INVOICE not found.", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_PAYMENT_TYPE_INVOICE_NOT_FOUND);
            throw se;
        } catch (NonUniqueBusinessKeyException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "PaymentInfo of type INVOICE cannot be created.", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_PAYMENT_TYPE_INVOICE_CREATION_FAILED);
            throw se;
        }

        return organization;
    }

    private String getLocalizedPaymentInfoId(String locale, long key) {

        List<VOLocalizedText> texts = localizer.getLocalizedValues(key,
                LocalizedObjectTypes.PAYMENT_TYPE_NAME);
        for (VOLocalizedText text : texts) {
            if (text.getLocale().equals(locale)) {
                return text.getText();
            }

        }
        return null;
    }

    /**
     * Assigns the given organization roles to the specified organization.
     *
     * @param organization
     *            The organization to be granted the authorities.
     * @param roles
     *            The roles to be granted.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case the organization to role relation violates a
     *             business key constraint. As there is none, this won't occur
     *             currently.
     */
    private void assignOrganizationRole(Organization organization,
            OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException {
        // grant customer authority to the organization
        for (OrganizationRoleType roleToSet : roles) {
            OrganizationRole role = new OrganizationRole();
            role.setRoleName(roleToSet);
            role = (OrganizationRole) dm.find(role);
            OrganizationToRole orgToRole = new OrganizationToRole();
            orgToRole.setOrganization(organization);
            orgToRole.setOrganizationRole(role);
            dm.persist(orgToRole);
        }
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    @Interceptors({ LdapInterceptor.class })
    public VOOrganization registerKnownCustomer(VOOrganization organization,
            VOUserDetails user, LdapProperties organizationProperties,
            String marketplaceId)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException, MailOperationException,
            ObjectNotFoundException, OperationPendingException {

        ArgumentValidator.notNull("organization", organization);
        ArgumentValidator.notNull("user", user);
        ArgumentValidator.notNull("marketplaceId", marketplaceId);

        VOOrganization result = null;
        Marketplace mp = new Marketplace(marketplaceId);
        mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
        if (mp.getTenant() != null) {
            organization.setTenantKey(mp.getTenant().getKey());
        } else {
            organization.setTenantKey(0L);
        }

        validateOrganizationDataForRegistration(organization, user);
        OrganizationAssembler.toCustomer(organization);

        TriggerProcessValidator validator = new TriggerProcessValidator(dm);
        if (validator.isRegisterCustomerForSupplierPending(user)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to register a user with conflicting id '%s' or mail address '%s'.",
                            user.getUserId(), user.getEMail()),
                    OperationPendingException.ReasonEnum.REGISTER_CUSTOMER_FOR_SUPPLIER,
                    new Object[] { user.getUserId(), user.getEMail() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_REGISTER_CUSTOMER_FOR_SUPPLIER_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    user.getUserId(), user.getEMail());
            throw ope;
        }

        // validate LDAP properties (if given)
        if (organizationProperties != null) {
            Properties props = organizationProperties.asProperties();
            // provide meaningful defaults if mandatory parameters not given
            if (!props.containsKey(SettingType.LDAP_CONTEXT_FACTORY.name())) {
                props.put(SettingType.LDAP_CONTEXT_FACTORY.name(),
                        ldapSettingsMgmt.getDefaultValueForSetting(
                                SettingType.LDAP_CONTEXT_FACTORY));
            }
            if (!props.containsKey(SettingType.LDAP_ATTR_UID.name())) {
                props.put(SettingType.LDAP_ATTR_UID.name(), ldapSettingsMgmt
                        .getDefaultValueForSetting(SettingType.LDAP_ATTR_UID));
            }

            Properties propsResolved = ldapSettingsMgmt
                    .getSettingsResolved(props);

            LdapConnector connector = new LdapConnector(ldapAccess,
                    propsResolved);
            connector.ensureAllMandatoryLdapPropertiesPresent();

            connector.validateLdapProperties(user);
        }

        TriggerMessage message = new TriggerMessage(
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess triggerProcess = list.get(0).getTrigger();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.ORGANIZATION, organization);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.USER, user);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.ORGANIZATION_PROPERTIES,
                organizationProperties);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.MARKETPLACE_ID, marketplaceId);

        TriggerDefinition triggerDefinition = triggerProcess
                .getTriggerDefinition();
        if (triggerDefinition == null) {
            try {
                result = registerKnownCustomerInt(triggerProcess);
            } catch (OrganizationAuthoritiesException | ValidationException
                    | MailOperationException
                    | NonUniqueBusinessKeyException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            triggerProcess.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createRegisterCustomerForSupplier(
                            dm, triggerDefinition.getType(), user));
            dm.merge(triggerProcess);
        }

        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VOOrganization registerKnownCustomerInt(TriggerProcess tp)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException, MailOperationException,
            ObjectNotFoundException {

        VOOrganization organization = tp
                .getParamValueForName(TriggerProcessParameterName.ORGANIZATION)
                .getValue(VOOrganization.class);
        VOUserDetails user = tp
                .getParamValueForName(TriggerProcessParameterName.USER)
                .getValue(VOUserDetails.class);
        LdapProperties ldapProperties = tp
                .getParamValueForName(
                        TriggerProcessParameterName.ORGANIZATION_PROPERTIES)
                .getValue(LdapProperties.class);
        Properties organizationProperties = (ldapProperties == null) ? null
                : ldapProperties.asProperties();
        String marketplaceId = tp
                .getParamValueForName(
                        TriggerProcessParameterName.MARKETPLACE_ID)
                .getValue(String.class);

        Organization seller = validateOrganizationDataForRegistration(
                organization, user);
        Organization customer;
        try {
            customer = registerOrganization(
                    OrganizationAssembler.toCustomer(organization), null, user,
                    organizationProperties, organization.getDomicileCountry(),
                    marketplaceId, organization.getDescription(),
                    OrganizationRoleType.CUSTOMER);
        } catch (IncompatibleRolesException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNEXPECTED_INCOMPATIBLE_ROLES_EXCEPTION);
            throw sse;
        } catch (OrganizationAuthorityException e) {
            // must not happen as customers won't have platform operator role
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_AUTHORITY_TO_BE_GRANTED_INVALID);
            throw sse;
        }

        OrganizationReference refSuplCust = saveCustomerReference(seller,
                customer);

        enableSuppliersDefaultPaymentsForCustomer(refSuplCust);
        if (isSupplierSetsInvoiceASDefault()) {
            setPaymentInfoInvoice(refSuplCust);
        }
        VOOrganization result = OrganizationAssembler.toVOOrganization(customer,
                false, new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        triggerQS.sendAllNonSuspendingMessages(TriggerMessage.create(
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                tp.getTriggerProcessParameters(),
                dm.getCurrentUser().getOrganization()));

        return result;
    }

    OrganizationReference saveCustomerReference(Organization source,
            Organization target) throws NonUniqueBusinessKeyException {
        OrganizationReferenceType referenceType = getCustomerReferenceType(
                source);
        OrganizationReference refSuplCust = new OrganizationReference(source,
                target, referenceType);
        dm.persist(refSuplCust);
        dm.flush();
        return refSuplCust;
    }

    private OrganizationReferenceType getCustomerReferenceType(
            Organization source) {
        OrganizationReferenceType orgReferenceType = OrganizationReferenceType
                .getOrgRefTypeForSourceRoles(source.getGrantedRoleTypes());
        if (orgReferenceType == null) {
            String rolesString = OrganizationRoleType.SUPPLIER + ", "
                    + OrganizationRoleType.RESELLER + ", "
                    + OrganizationRoleType.BROKER;
            SaaSSystemException sse = new SaaSSystemException(
                    "Finding the organization reference type failed because the source organization "
                            + source.getKey()
                            + " has none of the following roles: "
                            + rolesString);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.WARN_ORGANIZATION_ROLE_REQUIRED,
                    Long.toString(source.getKey()), rolesString);
            throw sse;
        } else {
            return orgReferenceType;
        }
    }

    /*
     * Set the domicile country for the given organization and country code. The
     * domicile country is a reference to the supported countries of the parent
     * organization. The parent organization is either the platform operator or
     * the supplier.
     */
    void setDomicileCountry(Organization toBeSet, String countryCode)
            throws ObjectNotFoundException {
        SupportedCountry sc = new SupportedCountry(countryCode);
        sc = (SupportedCountry) dm.find(sc);
        if (sc == null) {
            ObjectNotFoundException e = new ObjectNotFoundException(
                    ClassEnum.ORGANIZATION_TO_COUNTRY, countryCode);
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_EX_OBJECT_NOT_FOUND_EXCEPTION_ORGANIZATION_TO_COUNTRY,
                    countryCode);
            throw e;
        }
        toBeSet.setDomicileCountry(sc);
    }

    /**
     * Validates the organization data and the current user's authorization to
     * register a new organization.
     *
     * @param organization
     *            The organization data to be validated.
     * @param user
     *            The user to be created for the organization.
     * @return The invoking organization.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the invoking user's organization is not a
     *             supplier.
     * @throws ValidationException
     *             Thrown in case the organization or user data cannot be
     *             validated.
     * @return The calling user's organization.
     */
    private Organization validateOrganizationDataForRegistration(
            VOOrganization organization, VOUserDetails user)
            throws ValidationException, NonUniqueBusinessKeyException {
        Organization caller = dm.getCurrentUser().getOrganization();
        String id = organization.getOrganizationId();
        if (id != null && id.length() > 0) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.EMPTY_VALUE,
                    OrganizationAssembler.FIELD_NAME_ORGANIZATION_ID,
                    new Object[] { id });
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.WARN_NON_EMPTY_ORGANIZATION_ID_SPECIFIED,
                    "Organization");
            throw vf;
        }
        if (checkIfPlatformUserInGivenTenantExists(organization.getTenantKey(),
                user.getUserId())) {
            throw new NonUniqueBusinessKeyException(
                    DomainObjectException.ClassEnum.USER, user.getUserId());
        }
        return caller;
    }

    private boolean checkIfOrganizationAlreadyExists(
            Organization organization) {
        Query query = dm
                .createNamedQuery("Organization.findOrganizationsByName");
        query.setParameter("tenantId", organization.getTenant() == null ? "default" : organization.getTenant().getTenantId());
        query.setParameter("name", organization.getDataContainer().getName());
        try {
            Organization organizationEntity = (Organization) query.getSingleResult();
            if (organizationEntity != null) {
                return true;
            }
        } catch (NoResultException e) {
            // That is good. No organization with that name exists
        }
            return false;
    }

    // TODO: move it to tenant service as the operator service bean is also
    // using the same code.
    boolean checkIfPlatformUserInGivenTenantExists(long tenantKey,
            String userId) {
        if (tenantKey != 0) {
            Query query = dm
                    .createNamedQuery("PlatformUser.findByUserIdAndTenantKey");
            query.setParameter("userId", userId);
            query.setParameter("tenantKey", tenantKey);
            try {
                PlatformUser pu = (PlatformUser) query.getSingleResult();
                if (pu != null) {
                    return true;
                }
            } catch (NoResultException e) {
                // That is good. No user for that tenant exists.
            }
            return false;
        }
        Query query = dm.createNamedQuery("PlatformUser.findByUserId");
        query.setParameter("userId", userId);
        try {
            PlatformUser pu = (PlatformUser) query.getSingleResult();
            if (pu != null) {
                return true;
            }
        } catch (NoResultException e) {
            // That is good. No user for that tenant exists.
        }
        return false;
    }

    /**
     * Read a payment type with a certain id from the database
     *
     * @param typeId
     *            the id of the payment type to find (formerly PaymentInfoType
     *            enumeration values)
     *
     * @return the payment type
     * @throws ObjectNotFoundException
     *             in case the payment type wasn't found
     */
    private PaymentType findPaymentType(String typeId)
            throws ObjectNotFoundException {
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId(typeId);
        paymentType = (PaymentType) dm.getReferenceByBusinessKey(paymentType);
        return paymentType;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public Set<VOPaymentType> getAvailablePaymentTypesForOrganization() {

        Organization organization = dm.getCurrentUser().getOrganization();

        final OrganizationRoleType role;
        if (organization.getGrantedRoleTypes()
                .contains(OrganizationRoleType.SUPPLIER)) {
            role = OrganizationRoleType.SUPPLIER;
        } else if (organization.getGrantedRoleTypes()
                .contains(OrganizationRoleType.RESELLER)) {
            role = OrganizationRoleType.RESELLER;
        } else {
            role = null;
        }
        List<OrganizationRefToPaymentType> types;
        types = organization.getPaymentTypes(false, role,
                getPlatformOperatorReference().getOrganizationId());

        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        Set<VOPaymentType> result = new HashSet<>();
        for (OrganizationRefToPaymentType orgToPT : types) {
            result.add(PaymentTypeAssembler
                    .toVOPaymentType(orgToPT.getPaymentType(), lf));
        }

        return result;
    }

    @Override
    public Set<VOPaymentType> getAvailablePaymentTypesFromOrganization(
            Long serviceKey)
            throws OrganizationAuthoritiesException, ObjectNotFoundException {

        List<PaymentType> ptIntersection = getAvailablePaymentTypesIntersection(
                serviceKey);

        Set<VOPaymentType> result = new HashSet<>();
        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (PaymentType iter : ptIntersection) {
            result.add(PaymentTypeAssembler.toVOPaymentType(iter, lf));
        }

        return result;
    }

    @Override
    public boolean isPaymentTypeEnabled(long serviceKey, long paymentTypeKey)
            throws ObjectNotFoundException {
        List<PaymentType> ptIntersection = getAvailablePaymentTypesIntersection(
                Long.valueOf(serviceKey));
        boolean isEnabled = false;
        for (PaymentType pt : ptIntersection) {
            if (pt.getKey() == paymentTypeKey) {
                isEnabled = true;
                break;
            }
        }

        return isEnabled;
    }

    List<PaymentType> getAvailablePaymentTypesIntersection(Long serviceKey)
            throws ObjectNotFoundException {

        ArgumentValidator.notNull("serviceKey", serviceKey);

        Organization customer = dm.getCurrentUser().getOrganization();

        // payment types are only defined for templates of supplier or reseller
        Product prod = dm.getReference(Product.class, serviceKey.longValue())
                .getSupplierOrResellerTemplate();
        Organization supplier = prod.getVendor();

        List<OrganizationRefToPaymentType> types;
        if (relationExists(customer, supplier)) {
            // return the customer specific configuration
            types = customer.getPaymentTypes(false,
                    OrganizationRoleType.CUSTOMER,
                    supplier.getOrganizationId());
        } else {
            // return the suppliers default configuration
            types = supplier.getPaymentTypes(true,
                    supplier.getGrantedRoleTypes()
                            .contains(OrganizationRoleType.RESELLER)
                                    ? OrganizationRoleType.RESELLER
                                    : OrganizationRoleType.SUPPLIER,
                    OrganizationRoleType.PLATFORM_OPERATOR.name());
        }

        // return the product payment configuration
        List<ProductToPaymentType> ptProd = prod.getPaymentTypes();

        // build intersection (product and customer payment types)
        List<PaymentType> ptIntersection = new ArrayList<>();
        for (OrganizationRefToPaymentType iterCust : types) {
            for (ProductToPaymentType iterProd : ptProd) {
                if (iterCust.getPaymentType()
                        .equals(iterProd.getPaymentType())) {
                    ptIntersection.add(iterCust.getPaymentType());
                }
            }
        }

        return ptIntersection;
    }

    /**
     * Checks if the supplier-customer-relation exists between the supplier and
     * the customer organization
     *
     * @param customer
     *            the customer organization
     * @param supplier
     *            the supplier organization
     * @return <code>true</code> if they have the relation, otherwise
     *         <code>false</code>.
     */
    private boolean relationExists(Organization customer,
            Organization supplier) {

        boolean result = false;
        final OrganizationReferenceType type;
        if (supplier.getGrantedRoleTypes()
                .contains(OrganizationRoleType.SUPPLIER)) {
            type = OrganizationReferenceType.SUPPLIER_TO_CUSTOMER;
        } else {

            type = OrganizationReferenceType.RESELLER_TO_CUSTOMER;
        }
        List<OrganizationReference> supplierOrgReferences = customer
                .getSourcesForType(type);
        for (OrganizationReference orgRef : supplierOrgReferences) {
            if (orgRef.getSource() == supplier) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOBillingContact> getBillingContacts() {

        Organization organization = dm.getCurrentUser().getOrganization();
        List<BillingContact> billingContacts = organization
                .getBillingContacts();
        List<VOBillingContact> result = BillingContactAssembler
                .toVOBillingContacts(billingContacts);

        return result;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public List<VOOrganizationPaymentConfiguration> getCustomerPaymentConfiguration() {

        Organization seller = dm.getCurrentUser().getOrganization();
        Query query = dm.createNamedQuery("Organization.getForSupplierKey");
        query.setParameter("supplierKey", Long.valueOf(seller.getKey()));
        query.setParameter("referenceType", getCustomerReferenceType(seller));
        Iterable<Organization> customers = ParameterizedTypes
                .iterable(query.getResultList(), Organization.class);
        List<VOOrganizationPaymentConfiguration> result = new ArrayList<>();
        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());

        for (Organization cust : customers) {
            if (cust.getDeregistrationDate() == null) {
                VOOrganization voCust = OrganizationAssembler
                        .toVOOrganization(cust);
                VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
                conf.setOrganization(voCust);
                conf.setEnabledPaymentTypes(new HashSet<VOPaymentType>());
                result.add(conf);
            }
        }

        List<OrganizationRefToPaymentType> paymentTypes = paymentTypeDao
                .retrievePaymentTypeForCustomer(seller);
        for (OrganizationRefToPaymentType pType : paymentTypes) {
            Organization cust = pType.getOrganizationReference().getTarget();
            if (cust.getDeregistrationDate() == null) {
                VOOrganization voCust = OrganizationAssembler
                        .toVOOrganization(cust);
                VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
                conf.setOrganization(voCust);
                conf.setEnabledPaymentTypes(new HashSet<VOPaymentType>());
                if (result.contains(conf)) {
                    conf = result.get(result.indexOf(conf));
                    conf.getEnabledPaymentTypes().add(PaymentTypeAssembler
                            .toVOPaymentType(pType.getPaymentType(), lf));
                }
            }
        }

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOBillingContact saveBillingContact(VOBillingContact billingContact)
            throws ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException {

        ArgumentValidator.notNull("billingContact", billingContact);
        BillingContact bc = dm.find(BillingContact.class,
                billingContact.getKey());
        Organization organization = dm.getCurrentUser().getOrganization();
        if (billingContact.getKey() > 0 && bc == null) {
            throw new ConcurrentModificationException();
        }
        if (bc == null) {
            // creation
            bc = new BillingContact();
            bc.setOrganization(organization);
            BillingContactAssembler.updateBillingContact(bc, billingContact);
            try {
                dm.persist(bc);
            } catch (NonUniqueBusinessKeyException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else {
            // modification
            PermissionCheck.owns(bc, organization, logger);
            BaseAssembler.verifyVersionAndKey(bc, billingContact);
            try {
                BillingContact temp = new BillingContact();
                temp.setOrganization(organization);
                temp.setKey(bc.getKey());
                BillingContactAssembler.updateBillingContact(temp,
                        billingContact);
                dm.validateBusinessKeyUniqueness(temp);
            } catch (NonUniqueBusinessKeyException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
            BillingContactAssembler.updateBillingContact(bc, billingContact);
        }
        dm.flush();
        VOBillingContact newVOBillingContact = BillingContactAssembler
                .toVOBillingContact(bc);

        return newVOBillingContact;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void deleteBillingContact(VOBillingContact billingContact)
            throws ObjectNotFoundException, ConcurrentModificationException,
            OperationNotPermittedException {

        ArgumentValidator.notNull("billingContact", billingContact);

        Organization organization = dm.getCurrentUser().getOrganization();
        BillingContact bc = dm.getReference(BillingContact.class,
                billingContact.getKey());
        // check if calling organization is owner
        PermissionCheck.owns(bc, organization, logger);
        // check for concurrent change
        BaseAssembler.verifyVersionAndKey(bc, billingContact);
        // check if usable subscriptions still use it
        Set<Subscription> subscriptions = bc.getSubscriptions();
        for (Subscription sub : subscriptions) {
            suspendChargeableActiveSubscription(sub);
            sub.setBillingContact(null);
        }
        // finally remove
        dm.remove(bc);

    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public boolean savePaymentConfiguration(
            Set<VOPaymentType> defaultConfiguration,
            List<VOOrganizationPaymentConfiguration> customerConfigurations,
            Set<VOPaymentType> defaultServiceConfiguration,
            List<VOServicePaymentConfiguration> serviceConfigurations)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OperationPendingException {

        ArgumentValidator.notNull("defaultConfiguration", defaultConfiguration);
        ArgumentValidator.notNull("defaultServiceConfiguration",
                defaultServiceConfiguration);

        TriggerProcessValidator validator = new TriggerProcessValidator(dm);
        if (validator.isSavePaymentConfigurationPending()) {
            OperationPendingException ope = new OperationPendingException(
                    "Saving payment configuration failed.",
                    OperationPendingException.ReasonEnum.SAVE_PAYMENT_CONFIGURATION,
                    new Object[] {});
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    LogMessageIdentifier.WARN_SAVE_PAYMENT_CONFIGURATION_FAILED_DUE_TO_TRIGGER_CONFLICT);
            throw ope;
        }

        PaymentConfigurationFilter filter = new PaymentConfigurationFilter(dm);
        boolean result = true;

        try {
            List<TriggerMessage> messages = new ArrayList<>();

            // collect messages for: customer default configuration
            if (filter.isDefaultCustomerConfigurationChanged(
                    defaultConfiguration)) {
                messages.add(new TriggerMessage(
                        TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        TriggerProcessParameterName.DEFAULT_CONFIGURATION));
            }
            // collect messages for: customer configurations
            Map<TriggerMessage, VOOrganizationPaymentConfiguration> ccmap = new HashMap<>();
            customerConfigurations = filter
                    .filterCustomerConfiguration(customerConfigurations);
            for (VOOrganizationPaymentConfiguration orgConf : customerConfigurations) {
                TriggerMessage m = new TriggerMessage(
                        TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        TriggerProcessParameterName.CUSTOMER_CONFIGURATION);
                messages.add(m);
                ccmap.put(m, orgConf);
            }

            // collect messages for: service default configuration
            if (filter.isDefaultServiceConfigurationChanged(
                    defaultServiceConfiguration)) {
                messages.add(new TriggerMessage(
                        TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION));
            }

            // collect messages for: service configuration
            Map<TriggerMessage, VOServicePaymentConfiguration> scmap = new HashMap<>();
            serviceConfigurations = filter
                    .filterServiceConfiguration(serviceConfigurations);
            for (VOServicePaymentConfiguration conf : serviceConfigurations) {
                TriggerMessage m = new TriggerMessage(
                        TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION);
                messages.add(m);
                scmap.put(m, conf);
            }

            // send messages
            List<TriggerProcessMessageData> resultList = triggerQS
                    .sendSuspendingMessages(messages);

            initSuspendedInTransactionList();

            // process result
            for (TriggerProcessMessageData r : resultList) {
                TriggerProcess triggerProc = null;
                if (r.getParameterName() == TriggerProcessParameterName.DEFAULT_CONFIGURATION) {
                    triggerProc = r.getTrigger();
                    triggerProc.addTriggerProcessParameter(
                            TriggerProcessParameterName.DEFAULT_CONFIGURATION,
                            defaultConfiguration);
                } else if (r
                        .getParameterName() == TriggerProcessParameterName.CUSTOMER_CONFIGURATION) {
                    triggerProc = r.getTrigger();
                    triggerProc.addTriggerProcessParameter(
                            TriggerProcessParameterName.CUSTOMER_CONFIGURATION,
                            ccmap.get(r.getMessageData()));
                } else if (r
                        .getParameterName() == TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION) {
                    triggerProc = r.getTrigger();
                    triggerProc.addTriggerProcessParameter(
                            TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION,
                            defaultServiceConfiguration);
                } else {
                    triggerProc = r.getTrigger();
                    triggerProc.addTriggerProcessParameter(
                            TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                            scmap.get(r.getMessageData()));
                }

                TriggerDefinition triggerDefinition = triggerProc
                        .getTriggerDefinition();
                if (triggerDefinition == null) {
                    savePaymentConfigurationInt(triggerProc);
                } else if (triggerDefinition.isSuspendProcess()) {
                    result = false;
                    triggerProc.setTriggerProcessIdentifiers(
                            TriggerProcessIdentifiers
                                    .createSavePaymentConfiguration(dm,
                                            triggerDefinition.getType()));
                    dm.merge(triggerProc);
                }
            }

        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        removeSuspendedTransactionSubKeyList();
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void savePaymentConfigurationInt(TriggerProcess tp)
            throws ObjectNotFoundException, OperationNotPermittedException {

        Organization supplier = dm.getCurrentUser().getOrganization();

        // default customer configuration
        TriggerProcessParameter param = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION);
        if (param != null) {
            Set<VOPaymentType> defaultConfiguration = ParameterizedTypes
                    .set(param.getValue(Set.class), VOPaymentType.class);
            getUpdatedPaymentTypeMap(defaultConfiguration, supplier, true);
        }

        // default service configuration
        param = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION);
        if (param != null) {
            Set<VOPaymentType> defaultConfiguration = ParameterizedTypes
                    .set(param.getValue(Set.class), VOPaymentType.class);
            getUpdatedPaymentTypeMap(defaultConfiguration, supplier, false);
        }

        // customer configuration
        param = tp.getParamValueForName(
                TriggerProcessParameterName.CUSTOMER_CONFIGURATION);
        if (param != null) {
            VOOrganizationPaymentConfiguration customerConfiguration = param
                    .getValue(VOOrganizationPaymentConfiguration.class);

            Set<VOPaymentType> defaultConfiguration = new HashSet<>();
            final OrganizationRoleType roleType = supplier
                    .getVendorRoleForPaymentConfiguration();
            List<OrganizationRefToPaymentType> defaultPaymentTypes = supplier
                    .getPaymentTypes(true, roleType,
                            OrganizationRoleType.PLATFORM_OPERATOR.name());
            final LocalizerFacade lf = new LocalizerFacade(localizer,
                    dm.getCurrentUser().getLocale());
            for (OrganizationRefToPaymentType orgToPt : defaultPaymentTypes) {
                defaultConfiguration.add(PaymentTypeAssembler
                        .toVOPaymentType(orgToPt.getPaymentType(), lf));
            }

            OrganizationRole role = new OrganizationRole();
            role.setRoleName(OrganizationRoleType.CUSTOMER);
            role = (OrganizationRole) dm.getReferenceByBusinessKey(role);
            Map<VOPaymentType, PaymentType> map = getUpdatedPaymentTypeMap(
                    defaultConfiguration, supplier, true);
            Organization cust = new Organization();
            cust.setOrganizationId(customerConfiguration.getOrganization()
                    .getOrganizationId());
            cust = (Organization) dm.getReferenceByBusinessKey(cust);
            Set<VOPaymentType> set = customerConfiguration
                    .getEnabledPaymentTypes();
            List<OrganizationRefToPaymentType> paymentTypes = cust
                    .getPaymentTypes(false, OrganizationRoleType.CUSTOMER,
                            supplier.getOrganizationId());
            configureCustomerPaymentTypes(map, role, cust, supplier, set,
                    paymentTypes);
        }

        // service configuration
        param = tp.getParamValueForName(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION);
        if (param != null) {
            VOServicePaymentConfiguration conf = param
                    .getValue(VOServicePaymentConfiguration.class);
            configureServicePaymentTypes(conf, supplier);
        }
        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.SAVE_PAYMENT_CONFIGURATION,
                        tp.getTriggerProcessParameters(),
                        dm.getCurrentUser().getOrganization()));

    }

    /**
     * Configures the payment configuration for the passed wrapper.
     *
     * @param conf
     *            the service payment configuration
     * @param supplier
     *            the calling supplier {@link Organization}
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    private void configureServicePaymentTypes(
            VOServicePaymentConfiguration conf, Organization supplier)
            throws ObjectNotFoundException, OperationNotPermittedException {
        // try to find the product
        Product product = dm.getReference(Product.class,
                conf.getService().getKey());
        // check if i'm the owner
        PermissionCheck.owns(product, supplier, logger, sessionCtx);
        // check if it is a template
        new PaymentConfigurationFilter(dm).checkIsTemplate(supplier, product);

        Map<String, PaymentType> idToPt = loadPaymentTypeForSupplier(supplier);

        Map<PaymentType, List<Subscription>> ptToSubs = loadPtToSubListMap(
                product, idToPt);

        Map<String, ProductToPaymentType> idToRef = loadProductToPaymenttype(
                product);

        Set<VOPaymentType> types = conf.getEnabledPaymentTypes();
        for (VOPaymentType pt : types) {
            PaymentType type = idToPt.remove(pt.getPaymentTypeId());
            if (type == null) {
                throwWarningForTypeEqualsNull(supplier, product, pt);
            }

            ProductToPaymentType ref = idToRef.remove(pt.getPaymentTypeId());
            if (ref == null) {
                createNewReference(product, type);
                // activate suspended subscriptions of this service if
                // chargeable using this payment type
                List<Subscription> list = getAffectedSubscriptions(ptToSubs,
                        type);
                for (Subscription sub : list) {
                    // do a check because the customer may not use the payment
                    // type
                    if (isOwningSubscription(supplier.getKey(), sub)
                            && PaymentDataValidator
                                    .isPaymentTypeSupportedBySupplier(
                                            sub.getOrganization(),
                                            sub.getProduct(), type)) {
                        if (!checkSubKeyInSuspendedTransactionSubKeyList(
                                sub.getKey())) {
                            revokeSuspendedSubscription(sub);
                        }
                    }
                }

            }
        }

        // remove the remaining references
        for (Map.Entry<String, ProductToPaymentType> entry : idToRef
                .entrySet()) {
            PaymentType type = removeReference(product, entry);
            // suspend active subscriptions of this service if chargeable using
            // this payment type
            List<Subscription> list = getAffectedSubscriptions(ptToSubs, type);
            for (Subscription sub : list) {
                if (isOwningSubscription(supplier.getKey(), sub)) {
                    addToSuspendedInTransactionList(sub.getKey());
                    suspendChargeableActiveSubscription(sub);
                }
            }
        }
    }

    private Map<PaymentType, List<Subscription>> loadPtToSubListMap(
            Product product, Map<String, PaymentType> idToPt) {
        Map<PaymentType, List<Subscription>> ptToSubs = new HashMap<>();
        Set<PaymentType> ptSet = new HashSet<>(idToPt.values());
        for (ProductToPaymentType p : product.getPaymentTypes()) {
            ptSet.add(p.getPaymentType());
        }
        for (PaymentType p : ptSet) {
            ptToSubs.put(p, new ArrayList<Subscription>());

        }
        Query query = dm.createNamedQuery("Subscription.getForProduct");
        query.setParameter("product", product);
        query.setParameter("status", EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.SUSPENDED, SubscriptionStatus.PENDING_UPD,
                SubscriptionStatus.SUSPENDED_UPD));
        List<Subscription> allSubsList = ParameterizedTypes
                .list(query.getResultList(), Subscription.class);
        for (Subscription s : allSubsList) {
            PaymentInfo paymentInfo = s.getPaymentInfo();
            if (paymentInfo != null
                    && ptSet.contains(paymentInfo.getPaymentType())) {
                ptToSubs.get(paymentInfo.getPaymentType()).add(s);
            }
        }
        return ptToSubs;
    }

    /**
     * add new Subscription Key into the list from session when it is just set
     * as suspended/suspended_upd for paymenttype in Customers/Service
     * configuration
     *
     * @param subKey
     */
    private void addToSuspendedInTransactionList(long subKey) {

        if (sessionCtx == null || sessionCtx.getContextData() == null
                || sessionCtx.getContextData()
                        .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION) == null) {
            // No subKeylist is initialized
            return;
        }

        List<Long> list = new ArrayList<>();
        if (sessionCtx.getContextData()
                .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION) instanceof List<?>
                && ((List<?>) sessionCtx.getContextData()
                        .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION))
                                .size() > 0) {
            list = ParameterizedTypes.list(
                    (List<?>) sessionCtx.getContextData()
                            .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION),
                    Long.class);
        }
        list.add(new Long(subKey));

        sessionCtx.getContextData().put(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION,
                list);
    }

    private void initSuspendedInTransactionList() {
        if (sessionCtx == null || sessionCtx.getContextData() == null)
            return;

        sessionCtx.getContextData().put(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION,
                new ArrayList<>());
    }

    /**
     * remove the List of Suspended Transaction Subscription Key as null
     */
    private void removeSuspendedTransactionSubKeyList() {

        if (sessionCtx == null || sessionCtx.getContextData() == null
                || sessionCtx.getContextData()
                        .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION) == null)
            return;

        sessionCtx.getContextData()
                .remove(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION);
    }

    /**
     * check if the list of Keys of suspended subscription in transactions
     * contains the input parameter key
     */
    private boolean checkSubKeyInSuspendedTransactionSubKeyList(long subKey) {

        if (sessionCtx != null && sessionCtx.getContextData() != null
                && sessionCtx.getContextData()
                        .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION) != null
                && sessionCtx.getContextData()
                        .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION) instanceof List<?>
                && ((List<?>) sessionCtx.getContextData()
                        .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION))
                                .size() > 0) {

            List<Long> list = ParameterizedTypes.list(
                    (List<?>) sessionCtx.getContextData()
                            .get(SUSPENDED_SUBSCRIPTIONS_IN_TRANSACTION),
                    Long.class);

            return list.contains(new Long(subKey));
        }
        return false;
    }

    private PaymentType removeReference(Product product,
            Map.Entry<String, ProductToPaymentType> entry) {
        ProductToPaymentType ref = entry.getValue();
        PaymentType type = ref.getPaymentType();
        product.getPaymentTypes().remove(ref);

        dm.remove(ref);
        return type;
    }

    private void createNewReference(Product product, PaymentType type) {
        ProductToPaymentType ref;
        ref = new ProductToPaymentType();
        ref.setPaymentType(type);
        ref.setProduct(product);
        try {
            dm.persist(ref);
            product.getPaymentTypes().add(ref);
            dm.flush();
            dm.refresh(product);
        } catch (NonUniqueBusinessKeyException e) {
            // must not happen as we just checked if it exists
            handleImpossibleBKViolation(e);
        }
    }

    private void throwWarningForTypeEqualsNull(Organization supplier,
            Product product, VOPaymentType pt)
            throws OperationNotPermittedException {
        String message = String.format(
                "Supplier '%s' tried to enable payment type '%s' which is not activated for a service.",
                supplier.getOrganizationId(), pt.getPaymentTypeId());
        OperationNotPermittedException e = new OperationNotPermittedException(
                message);
        logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.WARN_CONFIGURE_PAYMENT_FAILED_PAYMENT_NOT_ACTIVATED,
                supplier.getOrganizationId(), String.valueOf(product.getKey()));
        sessionCtx.setRollbackOnly();
        throw e;
    }

    private Map<String, ProductToPaymentType> loadProductToPaymenttype(
            Product product) {
        List<ProductToPaymentType> existing = product.getPaymentTypes();
        Map<String, ProductToPaymentType> idToRef = new HashMap<>();
        for (ProductToPaymentType ref : existing) {
            idToRef.put(ref.getPaymentType().getPaymentTypeId(), ref);
        }
        return idToRef;
    }

    private Map<String, PaymentType> loadPaymentTypeForSupplier(
            Organization supplier) {
        // the payment types that are enabled for the supplier
        Map<String, PaymentType> idToPt = new HashMap<>();
        final OrganizationRoleType role = supplier
                .getVendorRoleForPaymentConfiguration();
        List<OrganizationRefToPaymentType> refs = supplier.getPaymentTypes(
                false, role, OrganizationRoleType.PLATFORM_OPERATOR.name());
        for (OrganizationRefToPaymentType ref : refs) {
            PaymentType pt = ref.getPaymentType();
            idToPt.put(pt.getPaymentTypeId(), pt);
        }
        return idToPt;
    }

    /**
     * Returns a list of subscriptions in state
     * {@link SubscriptionStatus#ACTIVE} or {@link SubscriptionStatus#SUSPENDED}
     * or {@link SubscriptionStatus#PENDING_UPD} or
     * {@link SubscriptionStatus#SUSPENDED_UPD} that have the passed
     * {@link Product} as the template of their product and use a payment info
     * of the passed {@link PaymentType}.
     *
     * @param ptToSubs
     *            the {@link ptToSubs}
     * @param type
     *            the {@link PaymentType}
     * @return the list of affected {@link Subscription}s
     */
    private List<Subscription> getAffectedSubscriptions(
            Map<PaymentType, List<Subscription>> ptToSubs, PaymentType type) {
        return ptToSubs.get(type);
    }

    /**
     * Checks the currently stored payment types for a customer and removes the
     * ones that are not supported anymore (also suspends the related
     * subscriptions) and creates the ones that are specified but have not been
     * supported so far.
     *
     * @param map
     *            The supplier specific map of payment types and basic default
     *            settings.
     * @param role
     *            The role to set for the new payment types.
     * @param cust
     *            The customer organization to update the payment types for.
     * @param seller
     *            The supplier that defines the payment types for the customer.
     * @param targetPaymentSettings
     *            The payment type settings for the concrete customer.
     * @param paymentTypes
     *            The currently available payment types for the customer.
     * @throws OperationNotPermittedException
     *             in case the seller organization is neither supplier nor
     *             reseller of the customer
     */
    private void configureCustomerPaymentTypes(
            Map<VOPaymentType, PaymentType> map, OrganizationRole role,
            Organization cust, Organization seller,
            Set<VOPaymentType> targetPaymentSettings,
            List<OrganizationRefToPaymentType> paymentTypes)
            throws OperationNotPermittedException {
        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (OrganizationRefToPaymentType orgToPt : paymentTypes) {
            VOPaymentType pt = PaymentTypeAssembler
                    .toVOPaymentType(orgToPt.getPaymentType(), lf);
            // delete the existing ones not contained in the set
            if (!targetPaymentSettings.remove(pt)) {
                suspendChargeableActiveSubscriptions(cust,
                        orgToPt.getPaymentType(), seller.getKey());
                dm.remove(orgToPt);
            }
        }

        OrganizationReference orgRef = new PaymentConfigurationFilter(dm)
                .checkSellerRelationship(seller, cust);

        // save the remaining payment types
        for (VOPaymentType type : targetPaymentSettings) {
            // find the organization reference

            // if required, persist new OrgRefToPaymentType object
            OrganizationRefToPaymentType orgToPt = orgRef
                    .getPaymentReferenceForType(
                            map.get(type).getPaymentTypeId());
            if (orgToPt == null) {
                orgToPt = new OrganizationRefToPaymentType();
                orgToPt.setOrganizationReference(orgRef);
                orgToPt.setOrganizationRole(role);
                orgToPt.setPaymentType(map.get(type));
                orgToPt.setUsedAsDefault(false);
                try {
                    dm.persist(orgToPt);
                    orgRef.getPaymentTypes().add(orgToPt);
                    dm.flush();
                    dm.refresh(orgToPt);
                    dm.refresh(orgRef);
                    dm.refresh(seller);
                    dm.refresh(cust);
                } catch (NonUniqueBusinessKeyException e) {
                    handleImpossibleBKViolation(e);
                }
            }

            // finally link the existing payment info objects of suspended
            // subscriptions to that object
            Set<SubscriptionStatus> states = EnumSet.of(
                    SubscriptionStatus.SUSPENDED,
                    SubscriptionStatus.SUSPENDED_UPD);
            List<Subscription> suspendedSubscriptions = cust
                    .getSubscriptionsForStateAndPaymentType(states,
                            orgToPt.getPaymentType().getPaymentTypeId());
            revokeSuspendedSubscriptions(suspendedSubscriptions,
                    seller.getKey(), orgToPt.getPaymentType());
        }
    }

    /**
     * Reads the organization reference from the supplier to the customer. If it
     * does not exist, an exception is generated.
     *
     * @param source
     *            The source organization
     * @param target
     *            The target organization
     * @param refType
     *            The type of reference to set
     *
     * @return The reference between both of the organizations.
     */
    private OrganizationReference retrieveOrgRef(Organization source,
            Organization target, OrganizationReferenceType refType) {
        OrganizationReference orgRef = new OrganizationReference(source, target,
                refType);
        orgRef = (OrganizationReference) dm.find(orgRef);

        if (orgRef == null) {
            SaaSSystemException sse = new SaaSSystemException(String.format(
                    "Organization %s has no reference of type %s to organization %s",
                    target, refType, source));
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_ORGANIZATION_HAS_NO_REFERENCE_TYPE,
                    String.valueOf(target), String.valueOf(refType),
                    String.valueOf(source));
            throw sse;
        }
        return orgRef;
    }

    /**
     * Updates the default settings for the available payment types on server
     * side according to the settings in the value objects. The method does
     * modify domain objects, as the default settings rae updated.
     *
     * @param defaultConfiguration
     *            The set of payment types containing the target settings for a
     *            payment to be set as default or not.
     * @param supplier
     *            The supplier object the available payment type settings will
     *            be obtained for.
     * @param customerDefault
     *            <code>true</code> if the customer default should be updated,
     *            <code>false</code> if the service default should be updated
     * @return A map of value object payment types (key) and the corresponding
     *         domain objects (value), reflecting the changes in the default
     *         settings.
     */
    private Map<VOPaymentType, PaymentType> getUpdatedPaymentTypeMap(
            Set<VOPaymentType> defaultConfiguration, Organization supplier,
            boolean customerDefault) {
        // save default - entries must exist - only default flag has to be
        // updated
        final OrganizationRoleType role = supplier
                .getVendorRoleForPaymentConfiguration();
        Map<VOPaymentType, PaymentType> map = new HashMap<>();
        List<OrganizationRefToPaymentType> types = supplier.getPaymentTypes(
                false, role, OrganizationRoleType.PLATFORM_OPERATOR.name());
        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (OrganizationRefToPaymentType orgToPT : types) {
            VOPaymentType temp = PaymentTypeAssembler
                    .toVOPaymentType(orgToPT.getPaymentType(), lf);
            map.put(temp, orgToPT.getPaymentType());
            if (customerDefault) {
                if (orgToPT.isUsedAsDefault() != defaultConfiguration
                        .contains(temp)) {
                    orgToPT.setUsedAsDefault(!orgToPT.isUsedAsDefault());
                }
            } else {
                if (orgToPT.isUsedAsServiceDefault() != defaultConfiguration
                        .contains(temp)) {
                    orgToPT.setUsedAsServiceDefault(
                            !orgToPT.isUsedAsServiceDefault());
                }
            }
        }
        return map;
    }

    /**
     * Suspends active subscriptions that have a chargeable price price model.
     * Has to be called when the supplier disables the payment type of the
     * payment info used by the customer. For those subscriptions that will be
     * suspended, the associated service instance will be deactivated.
     *
     * @param customer
     *            The customer of which the related subscriptions should be
     *            suspended
     * @param paymentType
     *            The payment type for which the related subscriptions should be
     *            suspended
     */
    private void suspendChargeableActiveSubscriptions(Organization customer,
            PaymentType paymentType, long sellerKey) {
        List<PaymentInfo> paymentInfos = new ArrayList<>();
        for (PaymentInfo pi : customer.getPaymentInfos()) {
            if (pi.getPaymentType() == paymentType) {
                paymentInfos.add(pi);
            }
        }
        for (PaymentInfo pi : paymentInfos) {
            Set<Subscription> subscriptions = pi.getSubscriptions();
            for (Subscription subscription : subscriptions) {
                if (isOwningSubscription(sellerKey, subscription)) {
                    addToSuspendedInTransactionList(subscription.getKey());
                    suspendChargeableActiveSubscription(subscription);
                }
            }
        }
    }

    /**
     * If the provided subscription is active or pending_upd and has a
     * chargeable price model, its state will be set to
     * {@link SubscriptionStatus#SUSPENDED} or
     * {@link SubscriptionStatus#SUSPENDED_UPD} and the associated service
     * instance will be deactivated.
     *
     * @param subscription
     *            the subscription to suspend
     */
    protected void suspendChargeableActiveSubscription(
            Subscription subscription) {
        if (subscription == null) {
            return;
        }
        SubscriptionStatus current = subscription.getStatus();
        if (!current.isActiveOrPendingUpd()
                || !subscription.getPriceModel().isChargeable()) {
            return;
        }

        subscription.setStatus(current.getNextForPaymentTypeRemoved());

        // call service to deactivate instance
        try {
            appManager.deactivateInstance(subscription);
        } catch (TechnicalServiceNotAliveException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_DEACTIVATE_INSTANCE);
        } catch (TechnicalServiceOperationException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_DEACTIVATE_INSTANCE);
        }
    }

    /**
     * Activates suspended subscriptions of the provided customer. Must be
     * called in Case the customer enters a new valid payment info or the
     * customer has a payment info that will be enabled by the supplier (e. g.
     * after disabling it). Also sets the reference of the payment types to the
     * OrgnizationRefToPaymentype object.
     *
     * @param list
     *            the subscriptions that have to be reactivated.
     */
    private void revokeSuspendedSubscriptions(List<Subscription> list,
            long sellerKey, PaymentType type) {
        for (Subscription subscription : list) {
            if (isOwningSubscription(sellerKey, subscription)
                    && PaymentDataValidator.isPaymentTypeSupportedBySupplier(
                            subscription.getOrganization(),
                            subscription.getProduct(), type)) {
                if (!checkSubKeyInSuspendedTransactionSubKeyList(
                        subscription.getKey())) {
                    revokeSuspendedSubscription(subscription);
                }
            }
        }
    }

    private boolean isOwningSubscription(long sellerKey,
            Subscription subscription) {
        Product product = subscription.getProduct().getTemplate();
        if (sellerKey == product.getVendorKey()) {
            return true;
        }

        if (product.getType() == ServiceType.PARTNER_TEMPLATE
                && product.getVendor().getGrantedRoleTypes()
                        .contains(OrganizationRoleType.BROKER)
                && product.getTemplate().getVendorKey() == sellerKey) {
            return true;
        }
        return false;
    }

    /**
     * If the provided {@link Subscription} is in state
     * {@link SubscriptionStatus#SUSPENDED}, the state will be set to
     * {@link SubscriptionStatus#ACTIVE} and the call to activate the service
     * instance will be executed. Exceptions occurring during the web service
     * call will only be logged.
     *
     * If the given {@link Subscription} is in
     * {@link SubscriptionStatus#SUSPENDED_UPD}, the state will be set to
     * {@link SubscriptionStatus#PENDING_UPD}.
     *
     * @param subscription
     *            the {@link Subscription} to activate
     */
    protected void revokeSuspendedSubscription(Subscription subscription) {
        if (subscription.getBillingContact() == null) {
            return;
        }
        final SubscriptionStatus current = subscription.getStatus();
        if (!current.isSuspendedOrSuspendedUpd()) {
            return;
        }
        subscription.setStatus(current.getNextForPaymentTypeRevoked());

        try {
            appManager.activateInstance(subscription);
        } catch (TechnicalServiceNotAliveException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ACTIVATE_INSTANCE);
        } catch (TechnicalServiceOperationException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ACTIVATE_INSTANCE);
        }
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public Set<VOPaymentType> getDefaultPaymentConfiguration() {

        Organization supplier = dm.getCurrentUser().getOrganization();

        final OrganizationRoleType role;
        if (supplier.getGrantedRoleTypes()
                .contains(OrganizationRoleType.SUPPLIER)) {
            role = OrganizationRoleType.SUPPLIER;
        } else if (supplier.getGrantedRoleTypes()
                .contains(OrganizationRoleType.RESELLER)) {
            role = OrganizationRoleType.RESELLER;
        } else {
            role = null;
        }
        Set<VOPaymentType> result = new HashSet<>();
        List<OrganizationRefToPaymentType> defaultPaymentTypes = supplier
                .getPaymentTypes(true, role,
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (OrganizationRefToPaymentType orgToPt : defaultPaymentTypes) {
            result.add(PaymentTypeAssembler
                    .toVOPaymentType(orgToPt.getPaymentType(), lf));
        }

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public VOPaymentInfo savePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, PaymentDeregistrationException,
            ValidationException, OperationNotPermittedException,
            PaymentDataException {

        ArgumentValidator.notNull("paymentInfo", paymentInfo);
        PaymentDataValidator
                .validateVOPaymentType(paymentInfo.getPaymentType());

        PaymentType pt = findPaymentType(
                paymentInfo.getPaymentType().getPaymentTypeId());
        Organization customer = dm.getCurrentUser().getOrganization();
        PaymentInfo pi = dm.getReference(PaymentInfo.class,
                paymentInfo.getKey());
        PermissionCheck.owns(pi, customer, logger);
        BaseAssembler.verifyVersionAndKey(pi, paymentInfo);

        try {
            PaymentInfo temp = new PaymentInfo(
                    DateFactory.getInstance().getTransactionTime());
            temp.setOrganization(customer);
            temp.setKey(pi.getKey());
            PaymentInfoAssembler.updatePaymentInfo(temp, paymentInfo);
            dm.validateBusinessKeyUniqueness(temp);
        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        PaymentInfoAssembler.updatePaymentInfo(pi, paymentInfo);
        handlePaymentTypeChange(pt, pi, customer);
        dm.flush();
        VOPaymentInfo newVOpaymentInfo = PaymentInfoAssembler
                .toVOPaymentInfo(pi, new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        return newVOpaymentInfo;
    }

    /**
     * Checks if the payment type has changed. If so, the payment info will be
     * unregistered on PSP side. For all Subscriptions using the payment info to
     * change it will be checked if the new payment type is supported by its
     * supplier - then the suspended subscription will be activated - or not -
     * then active and chargeable subscription will be suspended.
     *
     * @param newPt
     *            the new {@link PaymentType} to set
     * @param pi
     *            the {@link PaymentInfo} to modify
     * @param customer
     *            the customer {@link Organization}
     * @throws PaymentDeregistrationException
     *             Thrown in case of failing deletion on PSP side
     * @throws OperationNotPermittedException
     *             Thrown from deletion logic if the caller is not the owner of
     *             the provided {@link PaymentInfo}
     */
    protected void handlePaymentTypeChange(PaymentType newPt, PaymentInfo pi,
            Organization customer) throws PaymentDeregistrationException,
            OperationNotPermittedException {
        PaymentType oldPt = pi.getPaymentType();
        if (oldPt.getKey() != newPt.getKey()) {
            // the payment type has changed
            if (oldPt
                    .getCollectionType() == PaymentCollectionType.PAYMENT_SERVICE_PROVIDER
                    && newPt.getCollectionType() == PaymentCollectionType.ORGANIZATION
                    && !Strings.isEmpty(pi.getExternalIdentifier())) {
                // and and the collection type too and we have a external
                // identifier set
                paymentService.deregisterPaymentInPSPSystem(pi);
                pi.setExternalIdentifier(null);
            }
            // suspend active subscriptions
            Set<Subscription> subs = pi.getSubscriptions();
            for (Subscription sub : subs) {
                if (!PaymentDataValidator.isPaymentTypeSupportedBySupplier(
                        customer, sub.getProduct(), newPt)) {
                    suspendChargeableActiveSubscription(sub);
                } else {
                    revokeSuspendedSubscription(sub);
                }
            }
            // now update the payment type
            pi.setPaymentType(newPt);
        }
    }

    /**
     * Makes the payment types configured as available by default by the
     * supplier available for the new customer.
     *
     * @param supplier
     *            the supplier organization
     * @param customer
     *            the customer organization
     */
    private void enableSuppliersDefaultPaymentsForCustomer(
            OrganizationReference ref) {
        List<OrganizationRefToPaymentType> paymentTypes = ref.getSource()
                .getPaymentTypes(true, OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.PLATFORM_OPERATOR.name());

        Organization seller = dm.getCurrentUser().getOrganization();
        if (seller.getGrantedRoleTypes()
                .contains(OrganizationRoleType.RESELLER)) {
            paymentTypes = ref.getSource().getPaymentTypes(true,
                    OrganizationRoleType.RESELLER,
                    OrganizationRoleType.PLATFORM_OPERATOR.name());
        }

        OrganizationRole role = getOrganizationRole(ref.getTarget(),
                OrganizationRoleType.CUSTOMER);

        for (OrganizationRefToPaymentType orgToPt : paymentTypes) {
            OrganizationRefToPaymentType refToPt = createOrgRefToPt(role,
                    orgToPt.getPaymentType(), false, ref);
            ref.getPaymentTypes().add(refToPt);
        }
    }

    /**
     * Create a new organization reference to payment type domain object with
     * the given parameters.
     *
     * @param role
     *            the role context
     * @param paymentType
     *            the target payment type
     * @param usedAsDefault
     *            the used as default or not
     * @param ref
     *            the organization reference
     * @return the created {@link OrganizationRefToPaymentType}
     */
    private OrganizationRefToPaymentType createOrgRefToPt(OrganizationRole role,
            PaymentType paymentType, boolean usedAsDefault,
            OrganizationReference ref) {
        OrganizationRefToPaymentType toSave = new OrganizationRefToPaymentType();
        toSave.setUsedAsDefault(usedAsDefault);
        toSave.setUsedAsServiceDefault(usedAsDefault);
        toSave.setOrganizationReference(ref);
        toSave.setOrganizationRole(role);
        toSave.setPaymentType(paymentType);
        try {
            dm.persist(toSave);
        } catch (NonUniqueBusinessKeyException e) {
            handleImpossibleBKViolation(e);
        }
        return toSave;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOOrganization updateCustomerDiscount(VOOrganization voOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, ConcurrentModificationException {

        ArgumentValidator.notNull("organization", voOrganization);

        try {
            // find old value
            Organization tmpOrganization = new Organization();
            tmpOrganization
                    .setOrganizationId(voOrganization.getOrganizationId());
            Organization organization = (Organization) dm
                    .getReferenceByBusinessKey(tmpOrganization);
            int oldVersion = organization.getVersion();

            if (organization.hasRole(OrganizationRoleType.SUPPLIER)
                    || organization
                            .hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                    || organization.hasRole(OrganizationRoleType.RESELLER)
                    || organization.hasRole(OrganizationRoleType.BROKER)) {
                organization = OrganizationAssembler.updateVendor(organization,
                        voOrganization);
            } else {
                organization = OrganizationAssembler
                        .updateCustomer(organization, voOrganization);
            }

            // check if the user tried to change the organization data
            dm.flush();
            if (organization.getVersion() != oldVersion
                    || !equals(organization.getDomicileCountryCode(),
                            voOrganization.getDomicileCountry())
                    || !equals(
                            localizer.getLocalizedTextFromDatabase(
                                    dm.getCurrentUser().getLocale(),
                                    organization.getKey(),
                                    LocalizedObjectTypes.ORGANIZATION_DESCRIPTION),
                            voOrganization.getDescription())) {
                String message = String.format(
                        "Change of Organization '%s' data is not allowed. Only the discount may be changed.",
                        organization.getOrganizationId());
                OperationNotPermittedException e = new OperationNotPermittedException(
                        message);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_METHOD_FAILED_WITH_APPLICATION_EXCEPTION);
                throw e;
            }

            Integer discountVersion = voOrganization.getDiscount() != null
                    ? Integer.valueOf(voOrganization.getDiscount().getVersion())
                    : null;
            Discount discount = updateCustomerDiscount(organization,
                    DiscountAssembler.toDiscount(voOrganization.getDiscount()),
                    discountVersion);

            voOrganization = OrganizationAssembler.toVOOrganizationWithDiscount(
                    organization, isImageDefined(organization), discount,
                    new LocalizerFacade(localizer,
                            dm.getCurrentUser().getLocale()));
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return voOrganization;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Discount updateCustomerDiscount(Organization organization,
            Discount discount, Integer discountVersion)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException {

        // check if current organization is supplier for the customer
        Organization supplier = dm.getCurrentUser().getOrganization();
        PermissionCheck.supplierOfCustomer(supplier, organization, logger,
                null);
        Discount discountValue = processDiscountValue(organization, supplier,
                discount, discountVersion);

        return discountValue;
    }

    private Discount processDiscountValue(Organization organization,
            Organization supplier, Discount newDiscount,
            Integer discountVersion)
            throws ObjectNotFoundException, ConcurrentModificationException {

        Query query = dm
                .createNamedQuery("Discount.findForOrganizationAndSupplier");
        query.setParameter("organization", organization);
        query.setParameter("supplier", supplier);
        Discount dbDiscount = null;
        try {
            dbDiscount = (Discount) query.getSingleResult();
        } catch (NoResultException e) {
            // ignore, discount is optional
        }

        if (dbDiscount != null && newDiscount != null) {
            if (dbDiscount.getVersion() != discountVersion.intValue()
                    || (newDiscount.getKey() == 0
                            && dbDiscount.getVersion() == 0
                            && discountVersion.intValue() == 0)) {
                ConcurrentModificationException cme = new ConcurrentModificationException(
                        "Discount value changed concurrently");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                        LogMessageIdentifier.WARN_METHOD_FAILED_WITH_APPLICATION_EXCEPTION);
                throw cme;
            }
        }

        // type of email message
        EmailType emailType = null;
        if (newDiscount == null || newDiscount.getValue() == null
                || BigDecimalComparator.isZero(newDiscount.getValue())) {
            // discount will be deleted
            if (dbDiscount != null && dm.contains(dbDiscount)) {
                dbDiscount.getOrganizationReference().setDiscount(null);
                dm.remove(dbDiscount);
                dbDiscount = null;
                emailType = EmailType.ORGANIZATION_DISCOUNT_DELETED;
            }
        } else {
            if (dbDiscount == null) {
                // new discount will be created
                dbDiscount = new Discount();
                OrganizationReference ref = new OrganizationReference(
                        getOrganization(), organization,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                ref = (OrganizationReference) dm.getReferenceByBusinessKey(ref);
                dbDiscount.setOrganizationReference(ref);
                emailType = EmailType.ORGANIZATION_DISCOUNT_ADDED;
            } else {
                // check is value will be really updated and define mail
                // type
                emailType = getMailType(dbDiscount, newDiscount.getValue(),
                        newDiscount.getStartTime(), newDiscount.getEndTime());
            }

            // set value for discount
            dbDiscount.setValue(newDiscount.getValue());
            dbDiscount.setStartTime(newDiscount.getStartTime());
            dbDiscount.setEndTime(newDiscount.getEndTime());

            if (!dm.contains(dbDiscount)) {
                try {
                    dm.persist(dbDiscount);
                } catch (NonUniqueBusinessKeyException e) {
                    handleImpossibleBKViolation(e);
                }
            }
        }

        dm.flush();

        if (isSupplierSetsInvoiceASDefault()) {
            setPaymentInfoInvoice(retrieveOrgRef(supplier, organization,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER));
        }

        // Send an email to the current organization admin
        try {
            if (emailType != null) {
                cs.sendMail(organization, emailType, null, null);
            }
        } catch (MailOperationException e) {
            // log the problem and proceed
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_MAIL_DISCOUNT_CREATED_FAILED);
        }
        return dbDiscount;
    }

    private void updateOrganizationDescription(long key, String description) {
        if (description != null) {
            localizer.storeLocalizedResource(dm.getCurrentUser().getLocale(),
                    key, LocalizedObjectTypes.ORGANIZATION_DESCRIPTION,
                    description);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void processImage(ImageResource imageResource, long organizationKey)
            throws ValidationException {

        if (imageResource == null) {

            return;
        }

        if (imageResource.getImageType() == null || imageResource.getImageType()
                .getOwnerType() != ImageOwnerType.ORGANIZATION) {
            SaaSSystemException se = new SaaSSystemException(
                    "Only images belonging to an organization can be saved.");
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_IMAGES_NOT_BELONG_TO_ORGANIZATION);
            throw se;
        }

        boolean isImageDeleted = imageResource.getBuffer() == null;
        if (isImageDeleted) {
            imgSrv.delete(organizationKey, imageResource.getImageType());
        } else {

            ImageValidator.validate(imageResource.getBuffer(),
                    imageResource.getContentType(),
                    imageResource.getImageType());

            imgSrv.save(imageResource);
        }

    }

    /**
     * Define was difference in discount, return needed mail type.
     *
     * @param oldDiscount
     *            Old discount.
     * @param newDiscountValue
     *            New discount percent.
     * @param newStartTime
     *            New beginning time.
     * @param newEndTime
     *            New ending time.
     * @return Mail type or null if there were no differences.
     */
    protected EmailType getMailType(Discount oldDiscount,
            BigDecimal newDiscountValue, Long newStartTime, Long newEndTime) {

        EmailType mailType = null;

        if (oldDiscount == null) {
            return null;
        }

        BigDecimal oldDiscountValue = oldDiscount.getValue();
        if (oldDiscountValue != null
                && oldDiscountValue.compareTo(newDiscountValue) != 0) {
            // discount value was updated
            mailType = EmailType.ORGANIZATION_DISCOUNT_UPDATED;
            return mailType;
        }

        Long oldDiscountBegin = oldDiscount.getStartTime();
        if (oldDiscountBegin != null && newStartTime != null
                && oldDiscountBegin.longValue() != newStartTime.longValue()) {
            mailType = EmailType.ORGANIZATION_DISCOUNT_UPDATED;
            return mailType;
        }
        if ((oldDiscountBegin == null && newStartTime != null)
                || (oldDiscountBegin != null && newStartTime == null)) {
            mailType = EmailType.ORGANIZATION_DISCOUNT_UPDATED;
            return mailType;
        }
        Long oldDiscountEnd = oldDiscount.getEndTime();
        if (oldDiscountEnd != null && newEndTime != null
                && oldDiscountEnd.longValue() != newEndTime.longValue()) {
            mailType = EmailType.ORGANIZATION_DISCOUNT_UPDATED;
            return mailType;
        }
        if ((oldDiscountEnd == null && newEndTime != null)
                || (oldDiscountEnd != null && newEndTime == null)) {
            mailType = EmailType.ORGANIZATION_DISCOUNT_UPDATED;
            return mailType;
        }

        return mailType;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean sendDiscountEndNotificationMail(long currentTimeMillis)
            throws MailOperationException {

        // getting organization with ending in one week discount
        List<OrganizationReference> organizationRefList = getOrganizationForDiscountEndNotificiation(
                currentTimeMillis);

        // send mails
        if (organizationRefList != null) {
            for (OrganizationReference organizationRef : organizationRefList) {
                Organization cust = organizationRef.getTarget();
                String email = cust.getEmail();
                if (email != null && email.trim().length() != 0) {
                    cs.sendMail(cust, EmailType.ORGANIZATION_DISCOUNT_ENDING,
                            null, null);
                }
            }
        }

        return true;
    }

    /**
     * Getting list of organization to sending info mail about ending discount
     * in one week (seven days).
     *
     * @param currentTimeMillis
     *            Current millisecond.
     * @return Organization list for sending notification.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<OrganizationReference> getOrganizationForDiscountEndNotificiation(
            long currentTimeMillis) {

        // define the first and the last millisecond of needed day:
        // define date + 7 days
        long firstMillis = getMillisecondInFuture(currentTimeMillis, 7);
        long lastMillis = getMillisecondInFuture(currentTimeMillis, 8) - 1;

        // getting list of organization to sending info mail about ending
        // discount
        Query query = dm.createNamedQuery(
                "OrganizationReference.findOrganizationForDiscountEndNotification");
        query.setParameter("firstMillis", Long.valueOf(firstMillis));
        query.setParameter("lastMillis", Long.valueOf(lastMillis));
        List<OrganizationReference> list = ParameterizedTypes
                .list(query.getResultList(), OrganizationReference.class);

        return list;
    }

    /**
     * Return first millisecond of needed days in future.
     *
     * @param currentDayTimeMillis
     *            Current millisecond.
     * @param daysInFuture
     *            Number of days in future.
     * @return The first millisecond of needed day in future.
     */
    private long getMillisecondInFuture(long currentDayTimeMillis,
            int daysInFuture) {
        long firstMillis = 0;

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(currentDayTimeMillis);

        Calendar currentCalendarFirstMillis = currentCalendar;

        currentCalendarFirstMillis.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendarFirstMillis.set(Calendar.MINUTE, 0);
        currentCalendarFirstMillis.set(Calendar.SECOND, 0);
        currentCalendarFirstMillis.set(Calendar.MILLISECOND, 0);

        currentCalendarFirstMillis.add(Calendar.DAY_OF_MONTH, daysInFuture);

        firstMillis = currentCalendarFirstMillis.getTimeInMillis();

        return firstMillis;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void checkDistinguishedName(Organization organization)
            throws DistinguishedNameException {

        String dn = organization.getDistinguishedName();
        if (dn != null && dn.length() > 0) {
            Query query = dm
                    .createNamedQuery("Organization.countOrgsWithSameDN");
            query.setParameter("distinguishedName", dn);
            query.setParameter("organization", organization);
            Long result = (Long) query.getSingleResult();
            if (result.longValue() > 0) {
                DistinguishedNameException e = new DistinguishedNameException();
                logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_DUPLICATE_ORG_WITH_DISTINGUISHED_NAME,
                        dn);
                throw e;
            }
        }

    }

    @Override
    public Set<String> getUdaTargetTypes() {

        Organization organization = dm.getCurrentUser().getOrganization();
        Set<String> result = new HashSet<>();
        for (UdaTargetType type : UdaTargetType.values()) {
            if (orgHasUdaRoles(organization, type)) {
                result.add(type.name());
            }
        }

        return result;
    }

    @Override
    public void saveUdaDefinitions(List<VOUdaDefinition> udaDefinitionsToSave,
            List<VOUdaDefinition> udaDefinitionsToDelete)
            throws ValidationException, OrganizationAuthoritiesException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException {
        ArgumentValidator.notNull("udaDefinitionsToSave", udaDefinitionsToSave);
        ArgumentValidator.notNull("udaDefinitionsToDelete",
                udaDefinitionsToDelete);
        Organization org = dm.getCurrentUser().getOrganization();
        UdaDefinitionAccess udaAccess = new UdaDefinitionAccess(dm, sessionCtx,
                localizer);
        udaAccess.saveUdaDefinitions(udaDefinitionsToSave, org);
        udaAccess.deleteUdaDefinitions(udaDefinitionsToDelete, org);
    }

    /**
     * Checks if the provided {@link Organization} has at least one role that is
     * required by the provided {@link UdaTargetType}
     *
     * @param organization
     *            the {@link Organization} to check the roles of
     * @param type
     *            the {@link UdaTargetType} with the required roles
     * @return true if one of the required roles is present otherwise false.
     */
    private static boolean orgHasUdaRoles(Organization organization,
            UdaTargetType type) {
        Set<OrganizationRoleType> roles = type.getRoles();
        for (OrganizationRoleType role : roles) {
            if (organization.hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<VOUdaDefinition> getUdaDefinitions() {

        Organization organization = dm.getCurrentUser().getOrganization();
        List<UdaDefinition> defs = new UdaDefinitionAccess(dm, sessionCtx)
                .getOwnUdaDefinitions(organization);
        List<VOUdaDefinition> result = new ArrayList<>();
        for (UdaDefinition def : defs) {
            VOUdaDefinition voUdaDefinition = UdaAssembler
                    .toVOUdaDefinition(def, new LocalizerFacade(localizer,
                            dm.getCurrentUser().getLocale()));
            voUdaDefinition.setLanguage(dm.getCurrentUser().getLocale());
            result.add(voUdaDefinition);
        }
        return result;
    }

    @Override
    public List<VOUda> getUdas(String targetType, long targetObjectKey,
            boolean checkSeller)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("targetType", targetType);
        // get the current organization
        Organization organization = dm.getCurrentUser().getOrganization();
        UdaTargetType type = UdaAssembler.toUdaTargetType(targetType);
        // initial UdaAccess
        UdaAccess udaAccess = new UdaAccess(dm, sessionCtx);
        List<Uda> udas = udaAccess.getUdasForTypeAndTarget(targetObjectKey,
                type, organization, checkSeller);
        List<VOUda> voUdas = new ArrayList<>();
        for (Uda uda : udas) {
            // convert to VO list
            voUdas.add(UdaAssembler.toVOUda(uda, new LocalizerFacade(localizer,
                    dm.getCurrentUser().getLocale())));
        }

        return voUdas;
    }

    @Override
    public void saveUdas(List<VOUda> udas) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            MandatoryUdaMissingException, OrganizationAuthoritiesException {

        ArgumentValidator.notNull("udas", udas);
        if (udas.isEmpty()) {
            return;
        }
        UdaAssembler.validate(udas.get(0));
        long targetObjectKey = udas.get(0).getTargetObjectKey();
        String targetType = udas.get(0).getUdaDefinition().getTargetType();
        List<VOUda> originalUdas = getUdas(targetType,
                udas.get(0).getTargetObjectKey(), false);
        UdaAccess udaAccess = new UdaAccess(dm, sessionCtx);
        udaAccess.saveUdas(udas, dm.getCurrentUser().getOrganization());
        List<VOUda> updatedUdas = getUpdatedSubscriptionAttributes(originalUdas,
                udas);
        if (UdaTargetType.CUSTOMER_SUBSCRIPTION.toString().equals(targetType)) {
            Subscription subscription = dm.getReference(Subscription.class,
                    targetObjectKey);
            logSubscriptionAttribute(subscription, updatedUdas);
        }

    }

    /**
     * Return true if the configuration key SUPPLIER_SETS_PAYMENT_TYPE_INVOICE
     * has the value true.
     *
     * @return true if the configuration key has the value true.
     */
    private boolean isSupplierSetsInvoiceASDefault() {
        return Boolean
                .parseBoolean(
                        configService
                                .getConfigurationSetting(
                                        ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT,
                                        Configuration.GLOBAL_CONTEXT)
                                .getValue());
    }

    /**
     * Set the payment info of the organization to INVOICE.
     *
     * @param cust
     *            The organization for which the payment info is set.
     * @param supp
     *            The organization defining the payment type..
     */
    private void setPaymentInfoInvoice(OrganizationReference ref) {
        try {
            Organization seller = ref.getSource();
            if (seller.getGrantedRoleTypes()
                    .contains(OrganizationRoleType.BROKER)) {
                // broker does not have payment types
                return;
            }
            PaymentType pt = findPaymentType(PaymentType.INVOICE);
            Organization po = getPlatformOperatorReference();

            OrganizationReferenceType refType = OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER;
            if (seller.getGrantedRoleTypes()
                    .contains(OrganizationRoleType.RESELLER)) {
                refType = OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER;
            }
            OrganizationReference poToSupp = retrieveOrgRef(po, seller,
                    refType);
            List<OrganizationRefToPaymentType> paymentTypes = poToSupp
                    .getPaymentTypes();
            OrganizationRefToPaymentType temp = null;
            for (OrganizationRefToPaymentType e : paymentTypes) {
                if (e.getPaymentType().getPaymentTypeId()
                        .equals(PaymentType.INVOICE.toString())) {
                    if (!e.isUsedAsDefault() || !e.isUsedAsServiceDefault()) {
                        e.setUsedAsDefault(true);
                        e.setUsedAsServiceDefault(true);
                    }
                    temp = e;
                    break;
                }
            }
            if (temp == null) {
                createOrgRefToPt(
                        getOrganizationRole(ref.getSource(),
                                OrganizationRoleType.SUPPLIER),
                        pt, true, poToSupp);
                dm.flush();
            }

            temp = null;
            paymentTypes = ref.getPaymentTypes();

            for (OrganizationRefToPaymentType e : paymentTypes) {
                if (e.getPaymentType().getPaymentTypeId()
                        .equals(PaymentType.INVOICE.toString())) {
                    temp = e;
                    break;
                }
            }
            if (temp == null) {
                createOrgRefToPt(getOrganizationRole(ref.getTarget(),
                        OrganizationRoleType.CUSTOMER), pt, false, ref);
            }
        } catch (ObjectNotFoundException e) {
            // The PaymentType INVOICE must be created during setup.
            SaaSSystemException se = new SaaSSystemException(
                    "PaymentType INVOICE not found.", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_PAYMENT_TYPE_INVOICE_NOT_FOUND);
            throw se;
        }
    }

    /**
     * Reads the platform operator organization and returns it. In case it
     * cannot be found, a system exception will be logged and thrown.
     *
     * @return The platform operator organization.
     */
    protected Organization getPlatformOperatorReference() {
        // find the platform operator and set the payment for
        // the supplier
        Organization platformOperator = new Organization();
        platformOperator.setOrganizationId(
                OrganizationRoleType.PLATFORM_OPERATOR.name());
        try {
            platformOperator = (Organization) dm
                    .getReferenceByBusinessKey(platformOperator);
        } catch (ObjectNotFoundException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "Platform operator organization could not be found!", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_PLATFORM_OPERATOR_ORGANIZATION_NOT_FOUND);
            throw sse;
        }
        return platformOperator;
    }

    /**
     * Gets from the granted roles list of an organization the role with the
     * given roleType
     *
     * @param organization
     *            The organization holding the granted roles.
     *
     *            The organization role type for which we search the
     *            organization to role object.
     * @param roleType
     *            The organization role type for which we search the
     *            organization to role object.
     * @return the role object with the given organization role type or
     *         <code>null</code> if the roleType is not granted to the
     *         organization.
     */
    private OrganizationRole getOrganizationRole(Organization organization,
            OrganizationRoleType roleType) {
        Set<OrganizationToRole> list = organization.getGrantedRoles();
        if (list == null) {
            return null;
        }
        for (OrganizationToRole orgToRole : list) {
            if (orgToRole.getOrganizationRole().getRoleName() == roleType) {
                return orgToRole.getOrganizationRole();
            }
        }
        return null;
    }

    @Override
    public List<String> getSupportedCountryCodes() {
        Query query = dm
                .createNamedQuery("SupportedCountry.getAllCountryCodes");
        List<String> result = ParameterizedTypes.list(query.getResultList(),
                String.class);
        return result;
    }

    @Override
    public VOImageResource loadImageOfOrganization(long organizationKey) {

        // load image
        ImageResource imageResource = imgSrv.read(organizationKey,
                ImageType.ORGANIZATION_IMAGE);
        if (imageResource == null) { // nothing found?
            return null;
        }

        // return VOImageResource
        VOImageResource vo = new VOImageResource();
        vo.setImageType(ImageType.ORGANIZATION_IMAGE);
        vo.setBuffer(imageResource.getBuffer());
        vo.setContentType(imageResource.getContentType());

        return vo;
    }

    @Override
    public VOOrganization getSeller(String sellerId, String locale)
            throws ObjectNotFoundException {

        Organization supplier = getOrganization(0, sellerId,
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.BROKER,
                OrganizationRoleType.RESELLER);
        VOOrganization result = OrganizationAssembler.toVOOrganization(supplier,
                isImageDefined(supplier),
                new LocalizerFacade(localizer, locale));

        return result;
    }

    @Override
    @RolesAllowed("ORGANIZATION_ADMIN")
    public void deletePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, PaymentDeregistrationException {

        ArgumentValidator.notNull("paymentInfo", paymentInfo);
        Organization organization = dm.getCurrentUser().getOrganization();
        PaymentInfo pi = dm.getReference(PaymentInfo.class,
                paymentInfo.getKey());
        // check if calling organization is owner
        PermissionCheck.owns(pi, organization, logger);
        if (pi.getPaymentType().getPaymentTypeId()
                .equals(PaymentType.INVOICE)) {
            String message = String.format(
                    "Organization '%s' tried to delete payment info of type 'INVOICE'.",
                    organization.getOrganizationId());
            OperationNotPermittedException e = new OperationNotPermittedException(
                    message);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_PAYMENT_INFO_INVOICE_NOT_DELETABLE,
                    organization.getOrganizationId());
            throw e;
        }
        // check for concurrent change
        BaseAssembler.verifyVersionAndKey(pi, paymentInfo);
        Set<Subscription> subscriptions = pi.getSubscriptions();
        for (Subscription sub : subscriptions) {
            suspendChargeableActiveSubscription(sub);
            sub.setPaymentInfo(null);
        }
        if (pi.getPaymentType()
                .getCollectionType() == PaymentCollectionType.PAYMENT_SERVICE_PROVIDER
                && !Strings.isEmpty(pi.getExternalIdentifier())) {
            paymentService.deregisterPaymentInPSPSystem(pi);
        }
        dm.remove(pi);

    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOPaymentInfo> getPaymentInfos() {
        return getPaymentInfosInt();
    }

    private List<VOPaymentInfo> getPaymentInfosInt() {
        Organization organization = dm.getCurrentUser().getOrganization();
        List<PaymentInfo> paymentInfos = organization.getPaymentInfos();
        List<VOPaymentInfo> result = PaymentInfoAssembler
                .toVOPaymentInfos(paymentInfos, new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<VOPaymentInfo> getPaymentInfosForOrgAdmin() {
        return getPaymentInfosInt();
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public Set<VOPaymentType> getAvailablePaymentTypes() {

        Query query = dm.createNamedQuery("PaymentType.getAll");
        List<PaymentType> list = ParameterizedTypes.list(query.getResultList(),
                PaymentType.class);
        Set<VOPaymentType> result = PaymentTypeAssembler.toVOPaymentTypes(list,
                new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        return result;
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public Set<VOPaymentType> getDefaultServicePaymentConfiguration() {

        HashSet<VOPaymentType> result = new HashSet<>();
        Organization supplier = dm.getCurrentUser().getOrganization();
        List<OrganizationRefToPaymentType> ref = supplier
                .getDefaultServicePaymentTypes();
        final LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (OrganizationRefToPaymentType iter : ref) {
            VOPaymentType vo = PaymentTypeAssembler
                    .toVOPaymentType(iter.getPaymentType(), lf);
            result.add(vo);
        }

        return result;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration() {
        return getServicePaymentConfiguration(PerformanceHint.ALL_FIELDS);
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration(
            PerformanceHint performanceHint) {

        Organization supplier = dm.getCurrentUser().getOrganization();
        Query query = dm.createNamedQuery(
                "Product.getProductsForVendorPaymentConfiguration");
        query.setParameter("vendorKey", Long.valueOf(supplier.getKey()));
        query.setParameter("statusToIgnore", EnumSet.of(ServiceStatus.DELETED));
        List<Product> productList = ParameterizedTypes
                .list(query.getResultList(), Product.class);

        List<VOServicePaymentConfiguration> result = new ArrayList<>();

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        ProductAssembler.prefetchData(productList, facade, performanceHint);
        for (Product prod : productList) {
            VOService voService = ProductAssembler.toVOProduct(prod, facade,
                    performanceHint);
            List<ProductToPaymentType> paymentTypes = prod.getPaymentTypes();

            Set<VOPaymentType> set = new HashSet<>();
            for (ProductToPaymentType prodToPt : paymentTypes) {
                set.add(PaymentTypeAssembler
                        .toVOPaymentType(prodToPt.getPaymentType(), facade));
            }

            VOServicePaymentConfiguration conf = new VOServicePaymentConfiguration();
            conf.setService(voService);
            conf.setEnabledPaymentTypes(set);
            result.add(conf);
        }

        return result;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public List<VOOrganization> getSuppliersForTechnicalService(
            VOTechnicalService technicalService)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("technicalService", technicalService);
        List<Organization> orgs = marketingPermissionService
                .getSuppliersForTechnicalService(technicalService.getKey());
        LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        List<VOOrganization> result = OrganizationAssembler
                .toVOOrganizations(orgs, lf);

        return result;
    }

    @Override
    public List<VOUdaDefinition> getUdaDefinitionsForCustomer(String supplierId)
            throws ObjectNotFoundException {

        ArgumentValidator.notNull("supplierId", supplierId);
        // find the supplier organization
        Organization supplier = getOrganization(0, supplierId,
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.BROKER,
                OrganizationRoleType.RESELLER);
        UdaDefinitionAccess udaAccess = new UdaDefinitionAccess(dm, sessionCtx);
        // get the UDA definitions that can be created/modified by a customer
        List<UdaDefinition> defs = udaAccess
                .getReadableUdaDefinitionsFromSupplier(supplier,
                        OrganizationRoleType.CUSTOMER);
        List<VOUdaDefinition> voUdaDefs = new ArrayList<>();
        for (UdaDefinition def : defs) {
            // convert to VO list
            voUdaDefs.add(
                    UdaAssembler.toVOUdaDefinition(def, new LocalizerFacade(
                            localizer, dm.getCurrentUser().getLocale())));
        }

        return voUdaDefs;
    }

    /**
     * Get the organization by key or organization ID which must have at least
     * one of the given roles.
     *
     * @param key
     * @param orgId
     * @return organization object
     * @throws ObjectNotFoundException
     */
    private Organization getOrganization(long key, String orgId,
            OrganizationRoleType... roles) throws ObjectNotFoundException {
        Organization org = new Organization();
        if (key != 0L) {
            org = dm.getReference(Organization.class, key);
        } else {
            org.setOrganizationId(orgId);
            org = (Organization) dm.getReferenceByBusinessKey(org);
        }
        if (!org.hasAtLeastOneRole(roles)) {
            String roleString = "";
            for (OrganizationRoleType role : roles) {
                roleString += " " + role.name();
            }
            ObjectNotFoundException onf = new ObjectNotFoundException(
                    ClassEnum.ORGANIZATION, orgId);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onf,
                    LogMessageIdentifier.WARN_READ_ORGANIZATION_FAILED_WRONG_TYPE,
                    orgId, roleString);
            throw onf;
        }
        return org;
    }

    @Override
    public List<VOUda> getUdasForCustomer(String targetType,
            long targetObjectKey, String supplierId)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException {
        ArgumentValidator.notNull("targetType", targetType);
        ArgumentValidator.notNull("supplierId", supplierId);
        UdaTargetType type = UdaAssembler.toUdaTargetType(targetType);
        Organization customer = dm.getCurrentUser().getOrganization();
        // find the seller organization
        Organization supplier = getOrganization(0, supplierId,
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.BROKER,
                OrganizationRoleType.RESELLER);

        UdaAccess udaAccess = new UdaAccess(dm, sessionCtx);
        List<Uda> udas = udaAccess.getUdasForTypeTargetAndCustomer(
                targetObjectKey, type, supplier, customer);
        List<VOUda> voUdas = new ArrayList<>();
        for (Uda uda : udas) {
            // convert to VO list
            voUdas.add(UdaAssembler.toVOUda(uda, new LocalizerFacade(localizer,
                    dm.getCurrentUser().getLocale())));
        }

        return voUdas;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<PlatformUser> getOrganizationAdmins(long organizationKey) {
        Query query = dm.createNamedQuery("Organization.getAdministrators");
        query.setParameter("orgkey", Long.valueOf(organizationKey));
        return ParameterizedTypes.list(query.getResultList(),
                PlatformUser.class);
    }

    List<VOUda> getUpdatedSubscriptionAttributes(List<VOUda> existingUdas,
            List<VOUda> inputUdaList) {
        Map<String, String> existingAttributesMap = new HashMap<>();
        List<VOUda> updatedList = new ArrayList<>();
        for (VOUda voUda : existingUdas) {
            existingAttributesMap.put(voUda.getUdaDefinition().getUdaId(),
                    voUda.getUdaValue());
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

    void logSubscriptionAttribute(Subscription sub, List<VOUda> udaList) {
        for (VOUda voUda : udaList) {
            String parameterName = voUda.getUdaDefinition().getUdaId();
            String parameterValue = voUda.getUdaValue();
            subscriptionAuditLogCollector
                    .editSubscriptionAttributeByServiceManager(dm, sub,
                            parameterName, parameterValue);
        }

    }

    @Override
    public long countRegisteredUsers() {
        return userLicenseService.countRegisteredUsers();
    }

    @Override
    public boolean checkUserNum() throws MailOperationException {
        return userLicenseService.checkUserNum();
    }

}
