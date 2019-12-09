/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 02.10.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.oscm.internal.types.exception.DuplicateTenantIdException.Reason.TENANT_ALREADY_EXISTS;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.assembler.PaymentTypeAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.auditlog.AuditLogOperationGroups;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.configurationservice.assembler.ConfigurationSettingAssembler;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.CsvCreator;
import org.oscm.converter.LocaleHandler;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.PSPAccount;
import org.oscm.domobjects.PSPSetting;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.interceptor.ServiceProviderInterceptor;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.AuditLogTooManyRowsException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.DuplicateTenantIdException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.PSPIdentifierForSellerException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDataException.Reason;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.*;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.operatorservice.queries.SubscriptionUsageQuery;
import org.oscm.paymentservice.assembler.PSPAccountAssembler;
import org.oscm.paymentservice.assembler.PSPAssembler;
import org.oscm.paymentservice.assembler.PSPSettingAssembler;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.timerservice.bean.TimerServiceBean;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.validator.OrganizationRoleValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Bean implementation of the operator related functionality.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Remote(OperatorService.class)
@Stateless
@RolesAllowed("ORGANIZATION_ADMIN")
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class OperatorServiceBean implements OperatorService {

    private final static int DB_SEARCH_LIMIT = 100;
    public static final int ID_INDEX = 1;
    public static final int FIRST_NAME_INDEX = 2;
    public static final int LAST_NAME_INDEX = 3;
    public static final int USERID_INDEX = 0;
    public static final int EMAIL_INDEX = 1;
    public static final int ORGN_NAME_INDEX = 2;
    public static final int ORG_ID_INDEX = 3;
    public static final int STATUS_INDEX = 4;
    public static final int TKEY_INDEX_5 = 5;
    public static final int TKEY_INDEX = 0;

    private static Log4jLogger logger = LoggerFactory
            .getLogger(OperatorServiceBean.class);

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = IdentityServiceLocal.class)
    private IdentityServiceLocal im;

    @EJB(beanInterface = AccountServiceLocal.class)
    protected AccountServiceLocal accMgmt;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = PaymentServiceLocal.class)
    protected PaymentServiceLocal payProc;

    @EJB
    protected TimerServiceBean timerMgmt;

    @EJB(beanInterface = BillingServiceLocal.class)
    protected BillingServiceLocal billing;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal configService;

    @EJB(beanInterface = SearchServiceLocal.class)
    protected SearchServiceLocal searchService;

    @EJB(beanInterface = ImageResourceServiceLocal.class)
    private ImageResourceServiceLocal imgSrv;

    @EJB(beanInterface = AuditLogServiceBean.class)
    protected AuditLogServiceBean auditLogService;

    @EJB(beanInterface = MarketplaceServiceLocal.class)
    protected MarketplaceServiceLocal marketplaceService;

    @Resource
    protected SessionContext sessionCtx;

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOOrganization registerOrganization(VOOrganization organization,
            VOImageResource voImageResource, VOUserDetails orgInitialUser,
            LdapProperties organizationProperties, String marketplaceID,
            OrganizationRoleType... rolesToGrant)
            throws NonUniqueBusinessKeyException, ValidationException,
            OrganizationAuthorityException, IncompatibleRolesException,
            MailOperationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, ImageException {
        try {

            if (rolesToGrant == null) {
                rolesToGrant = new OrganizationRoleType[0];
            }

            final Marketplace marketplace = checkMarketplaceIDForCreateOrganization(marketplaceID,
                    rolesToGrant);

            if (rolesAreInvalid(rolesToGrant)) {
                OrganizationAuthorityException iao = new OrganizationAuthorityException(
                        "Creation of organization failed, invalid role to be granted",
                        new Object[] { OrganizationRoleType.SUPPLIER + ", "
                                + OrganizationRoleType.TECHNOLOGY_PROVIDER
                                + ", " + OrganizationRoleType.BROKER + ", "
                                + OrganizationRoleType.RESELLER });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, iao,
                        LogMessageIdentifier.WARN_REGISTER_ORGANIZATION_FAILED_INVALID_ROLE_GRANTED);
                throw iao;
            }

            if (operatorRevenueShareIsInvalid(
                    organization.getOperatorRevenueShare(),
                    Arrays.asList(rolesToGrant))) {
                ValidationException validationException = new ValidationException(
                        ValidationException.ReasonEnum.INVALID_OPERATOR_REVENUE_SHARE,
                        null, null);
                logger.logError(Log4jLogger.SYSTEM_LOG, validationException,
                        LogMessageIdentifier.ERROR_REGISTER_ORGANIZATION_FAILED_INVALID_OPERATOR_REVENUE_SHARE);
                throw validationException;
            }

            Organization organizationToCreate;
            if (rolesToGrant.length > 0) {
                organizationToCreate = OrganizationAssembler
                        .toVendor(organization);
            } else {
                if (voImageResource != null) {
                    // only suppliers and technology providers are allowed to
                    // save images
                    sessionCtx.setRollbackOnly();
                    throw new ImageException(
                            "Saving an image is only allowed for a supplier or technology provider organization.",
                            ImageException.Reason.NO_SELLER);
                }
                organizationToCreate = OrganizationAssembler
                        .toCustomer(organization);
            }
            if (organization.getOperatorRevenueShare() != null) {
                createOperatorPriceModel(organizationToCreate,
                        organization.getOperatorRevenueShare());
            }

            ImageResource imageResource = null;
            if (voImageResource != null) {
                imageResource = new ImageResource();
                imageResource.setContentType(voImageResource.getContentType());
                imageResource.setBuffer(voImageResource.getBuffer());
                imageResource.setImageType(voImageResource.getImageType());
            }

            if (marketplace != null && marketplace.getTenant() != null) {
                Tenant orgTenant = new Tenant();
                orgTenant.setKey(marketplace.getTenant().getKey());
                organizationToCreate.setTenant(orgTenant);
            }

            Organization createdOrganization = accMgmt.registerOrganization(
                    organizationToCreate, imageResource, orgInitialUser,
                    organizationProperties != null
                            ? organizationProperties.asProperties() : null,
                    organization.getDomicileCountry(), marketplaceID,
                    organization.getDescription(), rolesToGrant);

            addInvoice(createdOrganization);

            VOOrganization voOrganization = OrganizationAssembler
                    .toVOOrganization(createdOrganization, false,
                            new LocalizerFacade(localizer,
                                    dm.getCurrentUser().getLocale()));

            return voOrganization;
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ImageException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (IncompatibleRolesException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (MailOperationException e) {
            // BE07787: Don't persist the organization and the
            // organization admin if the mail server is unreachable
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    /**
     * when creating a customer, the roles should be null or empty, then the
     * marketplaceID is mandatory.
     * 
     * @param marketplaceID
     * @param roles
     * @throws ValidationException
     * @throws ObjectNotFoundException
     */
    private Marketplace checkMarketplaceIDForCreateOrganization(String marketplaceID,
                                                                OrganizationRoleType... roles)
            throws ValidationException, ObjectNotFoundException {
        if (roles.length == 0) {
            if (marketplaceID == null || marketplaceID.trim().length() == 0) {
                ValidationException validationException = new ValidationException(
                        "Creation of customer failed, the marketplaceID must not be null");
                throw validationException;
            } else {
                final Marketplace marketplace = validateMarketplaceID(marketplaceID);
                return marketplace;

            }
        }
        return null;
    }

    /**
     * validate the marketplace with the marketplaceID is existing
     * 
     * @param marketplaceId
     * @throws ObjectNotFoundException
     */
    private Marketplace validateMarketplaceID(String marketplaceId)
            throws ObjectNotFoundException {
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(marketplaceId);
        try {
            marketplace = (Marketplace) dm
                    .getReferenceByBusinessKey(marketplace);
            return marketplace;
        } catch (ObjectNotFoundException e) {
            logger.logDebug("Marketplace not found: " + e.getMessage());
            throw e;
        }
    }

    private boolean rolesAreInvalid(OrganizationRoleType... roles) {
        for (OrganizationRoleType role : roles) {
            if ((role == null) || (role != OrganizationRoleType.SUPPLIER
                    && role != OrganizationRoleType.TECHNOLOGY_PROVIDER
                    && role != OrganizationRoleType.BROKER
                    && role != OrganizationRoleType.RESELLER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an operator revenue share corresponds to the organization
     * role(s)
     * 
     * @param operatorRevenueShare
     *            an operator revenue share
     * @param roles
     *            the organization roles
     * @return <code>true</code> if operatorRevenueShare is <code>NULL</code>,
     *         but the organization is a supplier or if operatorRevenueShare is
     *         not <code>NULL</code>, but the organization is no supplier
     *         otherwise <code>false</code>
     */
    private boolean operatorRevenueShareIsInvalid(
            BigDecimal operatorRevenueShare,
            Collection<OrganizationRoleType> roles) {
        if (roles.contains(OrganizationRoleType.SUPPLIER)) {
            return (operatorRevenueShare == null);
        } else {
            return (operatorRevenueShare != null);
        }
    }

    /**
     * Create a new operator price model for a supplier organization and persist
     * it
     * 
     * @param org
     *            a supplier organization domain object
     * @param operatorRevenueShare
     *            the operator revenue share
     */
    private void createOperatorPriceModel(Organization org,
            BigDecimal operatorRevenueShare)
            throws NonUniqueBusinessKeyException {
        RevenueShareModel operatorPriceModel = new RevenueShareModel();
        operatorPriceModel.setRevenueShare(operatorRevenueShare);
        operatorPriceModel.setRevenueShareModelType(
                RevenueShareModelType.OPERATOR_REVENUE_SHARE);

        org.setOperatorPriceModel(operatorPriceModel);
        dm.persist(operatorPriceModel);
    }

    /**
     * Update the operator revenue share for a supplier organization
     * 
     * @param org
     *            a supplier organization domain object
     * @param operatorRevenueShare
     *            the operator revenue share
     * @throws ValidationException
     *             if the operator revenue share doesn't correspond to the
     *             organization role(s)
     */
    private void updateOperatorRevenueShare(Organization org,
            BigDecimal operatorRevenueShare)
            throws NonUniqueBusinessKeyException, ValidationException {
        if (operatorRevenueShareIsInvalid(operatorRevenueShare,
                org.getGrantedRoleTypes())) {
            ValidationException validationException = new ValidationException(
                    ValidationException.ReasonEnum.INVALID_OPERATOR_REVENUE_SHARE,
                    null, null);
            logger.logError(Log4jLogger.SYSTEM_LOG, validationException,
                    LogMessageIdentifier.ERROR_UPDATE_ORGANIZATION_FAILED_INVALID_OPERATOR_REVENUE_SHARE);
            throw validationException;
        } else if (operatorRevenueShare != null) {
            if (org.getOperatorPriceModel() != null) {
                org.getOperatorPriceModel()
                        .setRevenueShare(operatorRevenueShare);
            } else {
                createOperatorPriceModel(org, operatorRevenueShare);
            }
        }
    }

    /**
     * Adds INVOICE as payment type for the organization if the roles contain
     * the supplier role.
     * 
     * @param org
     *            The organization to add the payment type to.
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     */
    private void addInvoice(Organization org)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        if (!org.hasRole(OrganizationRoleType.SUPPLIER)
                && !org.hasRole(OrganizationRoleType.RESELLER)) {
            return;
        }
        Organization operator = dm.getCurrentUser().getOrganization();
        if (!org.canPermitPaymentType(PaymentType.INVOICE)) {
            PaymentType paymentType = new PaymentType();
            paymentType.setPaymentTypeId(PaymentType.INVOICE);
            paymentType = (PaymentType) dm
                    .getReferenceByBusinessKey(paymentType);
            OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
            OrganizationReference orgRef = findOrCreateOrganizationReference(
                    operator, org);
            apt.setOrganizationReference(orgRef);
            apt.setPaymentType(paymentType);
            OrganizationRole role = (OrganizationRole) dm
                    .getReferenceByBusinessKey(new OrganizationRole(
                            orgRef.getReferenceType() == OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER
                                    ? OrganizationRoleType.RESELLER
                                    : OrganizationRoleType.SUPPLIER));
            apt.setOrganizationRole(role);
            if (isSupplierSetsInvoiceAsDefaultEnabled()) {
                apt.setUsedAsDefault(true);
                apt.setUsedAsServiceDefault(true);
            }

            org.getSourcesForType(orgRef.getReferenceType()).get(0)
                    .getPaymentTypes().add(apt);
            dm.persist(apt);
        }
    }

    private OrganizationReference findOrCreateOrganizationReference(
            Organization operator, Organization org) {
        final OrganizationReferenceType refType;
        if (org.hasRole(OrganizationRoleType.RESELLER)) {
            refType = OrganizationReferenceType.PLATFORM_OPERATOR_TO_RESELLER;
        } else {
            refType = OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER;
        }
        return OperatorServiceDataHandler
                .findOrCreateOrganizationReference(operator, org, refType, dm);
    }

    /**
     * Return true if {@link ConfigurationKey#SUPPLIER_SETS_INVOICE_AS_DEFAULT}
     * has the value <code>true</code>.
     * 
     * @return <code>true</code> if the configuration key has the value
     *         <code>true</code>.
     */
    private boolean isSupplierSetsInvoiceAsDefaultEnabled() {

        ConfigurationSetting cs = configService.getConfigurationSetting(
                ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT,
                Configuration.GLOBAL_CONTEXT);
        String value;
        if (cs == null || cs.getValue() == null) {
            value = ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT
                    .getFallBackValue();
        } else {
            value = cs.getValue();
        }
        boolean b = Boolean.parseBoolean(value);

        return b;
    }

    private void addOrganizationToRoleIntern(String organizationId,
            OrganizationRoleType role)
            throws OrganizationAuthorityException, IncompatibleRolesException,
            ObjectNotFoundException, AddMarketingPermissionException {

        if (role == null || role != OrganizationRoleType.SUPPLIER
                && role != OrganizationRoleType.TECHNOLOGY_PROVIDER
                && role != OrganizationRoleType.RESELLER
                && role != OrganizationRoleType.BROKER) {
            OrganizationAuthorityException iao = new OrganizationAuthorityException(
                    "Updating the organization failed, invalid role to be granted",
                    new Object[] { OrganizationRoleType.SUPPLIER + " or "
                            + OrganizationRoleType.TECHNOLOGY_PROVIDER + " or "
                            + OrganizationRoleType.RESELLER + " or "
                            + OrganizationRoleType.BROKER });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, iao,
                    LogMessageIdentifier.WARN_UPDATE_ORGANIZATION_FAILED_INVALID_ROLE_GRANTED);
            throw iao;
        }

        Organization org = accMgmt.addOrganizationToRole(organizationId, role);
        try {
            addInvoice(org);
        } catch (NonUniqueBusinessKeyException e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_ADD_INVOICE_PAYMENT_TYPE);
            throw sse;
        }

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void addOrganizationToRole(String organizationId,
            OrganizationRoleType role) throws OrganizationAuthorityException,
            IncompatibleRolesException, ObjectNotFoundException,
            OrganizationAuthoritiesException, AddMarketingPermissionException {
        try {
            addOrganizationToRoleIntern(organizationId, role);
        } catch (OrganizationAuthorityException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (IncompatibleRolesException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (ObjectNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (AddMarketingPermissionException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    private void addAvailablePaymentTypesIntern(VOOrganization supplier,
            Set<String> types)
            throws ObjectNotFoundException, OrganizationAuthorityException,
            PSPIdentifierForSellerException, PaymentDataException {

        Organization operator = dm.getCurrentUser().getOrganization();

        Organization organization = getOrganizationInt(
                supplier.getOrganizationId());
        OrganizationRole role = null;
        Set<OrganizationToRole> grantedRoles = organization.getGrantedRoles();
        for (OrganizationToRole organizationToRole : grantedRoles) {
            if (organizationToRole.getOrganizationRole()
                    .getRoleName() == OrganizationRoleType.SUPPLIER
                    || organizationToRole.getOrganizationRole()
                            .getRoleName() == OrganizationRoleType.RESELLER) {
                role = organizationToRole.getOrganizationRole();
                break;
            }
        }
        if (role == null) {
            OrganizationAuthorityException ioa = new OrganizationAuthorityException(
                    "Insufficient authorization. Required role for organization "
                            + organization.getOrganizationId() + ": "
                            + OrganizationRoleType.SUPPLIER + ", "
                            + OrganizationRoleType.RESELLER,
                    new Object[] { OrganizationRoleType.SUPPLIER + ", "
                            + OrganizationRoleType.RESELLER });
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ioa,
                    LogMessageIdentifier.WARN_ORGANIZATION_ROLE_REQUIRED,
                    Long.toString(organization.getKey()),
                    OrganizationRoleType.SUPPLIER.name() + ", "
                            + OrganizationRoleType.RESELLER.name());
            throw ioa;
        }

        List<OrganizationRefToPaymentType> availablePaymentTypes = organization
                .getPaymentTypes(false, role.getRoleName(),
                        operator.getOrganizationId());
        for (String paymentTypeId : types) {
            PaymentType paymentType = new PaymentType();
            paymentType.setPaymentTypeId(paymentTypeId);
            try {
                paymentType = (PaymentType) dm
                        .getReferenceByBusinessKey(paymentType);
            } catch (ObjectNotFoundException e) {
                Query query = dm.createNamedQuery("PaymentType.getAll");
                boolean notFirst = false;
                StringBuffer validTypes = new StringBuffer();
                for (PaymentType pt : ParameterizedTypes
                        .iterable(query.getResultList(), PaymentType.class)) {
                    if (notFirst) {
                        validTypes.append(", ");
                    }
                    notFirst = true;
                    validTypes.append(pt.getPaymentTypeId());
                }
                String message = "Unknown paymenttype '%s'. Valid values are: %s";
                throw new PaymentDataException(
                        String.format(message, paymentTypeId,
                                validTypes.toString()),
                        Reason.UNKNOWN_PAYMENT_TYPE);
            }
            if (!orgHasAvailablePaymentType(paymentType,
                    availablePaymentTypes)) {
                if (paymentType
                        .getCollectionType() == PaymentCollectionType.PAYMENT_SERVICE_PROVIDER
                        && (organization.getPspAccountForPsp(
                                paymentType.getPsp()) == null
                                || organization
                                        .getPspAccountForPsp(
                                                paymentType.getPsp())
                                        .getPspIdentifier() == null)) {
                    // validate that the supplier's organization has the
                    // psp-identifier set for itself. If not, throw an exception
                    PSPIdentifierForSellerException mpi = new PSPIdentifierForSellerException(
                            "Supplier '" + organization.getKey()
                                    + "' is missing the setting of the PSP identifier.");
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, mpi,
                            LogMessageIdentifier.WARN_DEFINE_SUPPORTED_PAYMENT_TYPE_FAILED,
                            Long.toString(organization.getKey()));
                    throw mpi;
                }
                OrganizationReference orgRef = findOrCreateOrganizationReference(
                        operator, organization);
                OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
                apt.setOrganizationReference(orgRef);
                apt.setPaymentType(paymentType);
                apt.setOrganizationRole(role);
                try {
                    dm.persist(apt);
                } catch (NonUniqueBusinessKeyException e) {
                    // should not happen because AvailablePaymentType has no
                    // business key
                    logger.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_NONUNIQUEKEY_EXCEPTION_ALTHOUGH_NO_BUSINESS_KEY);
                    throw new SaaSSystemException(
                            "Persisting new available payment failed.", e);
                }
            }
        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void addAvailablePaymentTypes(VOOrganization supplier,
            Set<String> types)
            throws ObjectNotFoundException, OrganizationAuthorityException,
            PSPIdentifierForSellerException, PaymentDataException {

        // Delegate the call to the internal method.
        // Add transaction management here if needed.
        addAvailablePaymentTypesIntern(supplier, types);
    }

    private boolean orgHasAvailablePaymentType(PaymentType type,
            List<OrganizationRefToPaymentType> available) {
        for (OrganizationRefToPaymentType apt : available) {
            if (type.getKey() == apt.getPaymentType().getKey()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public boolean retryFailedPaymentProcesses()
            throws OrganizationAuthoritiesException {

        boolean result = payProc.reinvokePaymentProcessing();

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOTimerInfo> getTimerExpirationInformation()
            throws OrganizationAuthoritiesException {

        List<VOTimerInfo> expirationDates = timerMgmt
                .getCurrentTimerExpirationDates();

        return expirationDates;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public boolean startBillingRun() throws OrganizationAuthoritiesException {

        boolean result = billing.startBillingRun(System.currentTimeMillis());

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOTimerInfo> reInitTimers()
            throws OrganizationAuthoritiesException, ValidationException {

        timerMgmt.reInitTimers();
        List<VOTimerInfo> expirationDates = getTimerExpirationInformation();

        return expirationDates;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void setUserAccountStatus(VOUser voUser, UserAccountStatus newStatus)
            throws ObjectNotFoundException, ValidationException,
            OrganizationAuthoritiesException {

        BLValidator.isUserId(UserDataAssembler.FIELD_NAME_USER_ID,
                voUser.getUserId(), true);

        PlatformUser user = new PlatformUser();
        user = dm.getReference(PlatformUser.class, voUser.getKey());

        im.setUserAccountStatus(user, newStatus);

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void setDistinguishedName(String organizationId,
            String distinguishedName)
            throws ObjectNotFoundException, DistinguishedNameException,
            OrganizationAuthoritiesException, ValidationException {
        Organization organization = getOrganizationInt(organizationId);

        BLValidator.isDN("distinguishedName", distinguishedName, false);
        organization.setDistinguishedName(distinguishedName);
        try {
            accMgmt.checkDistinguishedName(organization);
        } catch (DistinguishedNameException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public byte[] getOrganizationBillingData(long from, long to,
            String organizationId)
            throws ObjectNotFoundException, OrganizationAuthoritiesException {

        Organization organization = getOrganizationInt(organizationId);

        final List<String> fragments = new ArrayList<>();
        try {
            List<BillingResult> resultList = billing
                    .generateBillingForAnyPeriod(from, to,
                            organization.getKey());
            if (resultList != null) {
                for (BillingResult billingResult : resultList) {
                    fragments.add(billingResult.getResultXML());
                }
            }

            return XMLConverter.combine("Billingdata", fragments);
        } catch (BillingRunFailed ex) {
            throw new SaaSSystemException(ex);
        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    @Interceptors({ ServiceProviderInterceptor.class })
    public void resetPasswordForUser(String userId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, OrganizationAuthoritiesException {

        // retrieve the platform user
        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        user = (PlatformUser) dm.getReferenceByBusinessKey(user);

        // reset the password, no marketplace context available
        im.resetPasswordForUser(user, null);

    }

    private Organization getOrganizationInt(String organizationId)
            throws ObjectNotFoundException {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) dm
                .getReferenceByBusinessKey(organization);
        return organization;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void addCurrency(String currencyISOCode)
            throws OrganizationAuthoritiesException, ValidationException {

        ArgumentValidator.notNull("currencyISOCode", currencyISOCode);

        Currency currency = null;
        try {
            currency = Currency.getInstance(currencyISOCode);
        } catch (IllegalArgumentException e) {
            ValidationException ve = new ValidationException(
                    ReasonEnum.INVALID_CURRENCY, "currencyISOCode",
                    new Object[] { currencyISOCode });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                    LogMessageIdentifier.WARN_INVALID_CURRENCY,
                    currencyISOCode);
            throw ve;
        }

        SupportedCurrency sp = new SupportedCurrency();
        sp.setCurrency(currency);
        try {
            dm.persist(sp);
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_CURRENCY_ADDED, currencyISOCode);
        } catch (NonUniqueBusinessKeyException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_CURRENCY_NOT_PERSISTED,
                    currencyISOCode);
        }

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOConfigurationSetting> getConfigurationSettings()
            throws OrganizationAuthoritiesException {

        List<ConfigurationSetting> settings = configService
                .getAllConfigurationSettings();

        List<VOConfigurationSetting> result = ConfigurationSettingAssembler
                .toVOConfigurationSettings(settings);

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void saveConfigurationSetting(VOConfigurationSetting configurationSetting)
            throws OrganizationAuthoritiesException, ValidationException,
            ConcurrentModificationException, DuplicateTenantIdException {

        ArgumentValidator.notNull("configurationSetting", configurationSetting);
        ConfigurationSetting st = dm.find(ConfigurationSetting.class, configurationSetting.getKey());

        if (configurationSetting.getKey() > 0 && st == null) {
            throw new ConcurrentModificationException();
        }
        if (st == null) {
            //creation
            st = new ConfigurationSetting();
            ConfigurationSettingAssembler.createConfigurationSetting(configurationSetting, st);
            try {
                dm.persist(st);
            } catch (NonUniqueBusinessKeyException e) {
                sessionCtx.setRollbackOnly();
                try {
                    throw e;
                } catch (NonUniqueBusinessKeyException e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            //modification
            BaseAssembler.verifyVersionAndKey(st, configurationSetting);
            try {
                ConfigurationSetting temp = new ConfigurationSetting();
                temp.setContextId(configurationSetting.getContextId());
                temp.setInformationId(configurationSetting.getInformationId());
                temp.setValue(configurationSetting.getValue());
                ConfigurationSettingAssembler.updateConfigurationSetting(configurationSetting, temp);
                dm.validateBusinessKeyUniqueness(temp);
            } catch (NonUniqueBusinessKeyException e) {
                e.printStackTrace();
                try {
                    throw e;
                } catch (NonUniqueBusinessKeyException e1) {
                    e1.printStackTrace();
                }
            }
            ConfigurationSettingAssembler.updateConfigurationSetting(configurationSetting, st);
        }
        dm.flush();
//        VOConfigurationSetting newVOConfigurationSetting = ConfigurationSettingAssembler.toVOConfigurationSetting(st);
//        return newVOConfigurationSetting;



//        validateDefaultTenantIdUniqueness(setting);
//
//        ConfigurationSetting dbSetting = configService.getConfigurationSetting(
//                setting.getInformationId(), setting.getContextId());
//        dbSetting = ConfigurationSettingAssembler
//                .updateConfigurationSetting(setting, dbSetting);
//
//        // necessary because of key and version check in BaseAssembler.
//        // Search in configService.getConfigurationSetting uses the business
//        // keys. Setting the key value should not provoke an error
//        setting.setKey(dbSetting.getKey());
//
//        configService.setConfigurationSetting(dbSetting);

    }

    private void validateDefaultTenantIdUniqueness(
            VOConfigurationSetting setting)
            throws ValidationException, DuplicateTenantIdException {
        if (setting.getInformationId()
                .equals(ConfigurationKey.SSO_DEFAULT_TENANT_ID)) {
            final String id = setting.getValue();
            Query query = dm.createNamedQuery("Tenant.findByBusinessKey");
            query.setParameter("tenantId", id);
            List<Object[]> resultList = query.getResultList();
            if (!resultList.isEmpty()) {
                throw new DuplicateTenantIdException(
                        "Default tenant ID not unique", TENANT_ALREADY_EXISTS);
            }
        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOOperatorOrganization getOrganization(String organizationId)
            throws OrganizationAuthoritiesException, ObjectNotFoundException {

        Organization organization = getOrganizationInt(organizationId);
        VOOperatorOrganization vo = OrganizationAssembler
                .toVOOperatorOrganization(organization,
                        isImageDefined(organization), new LocalizerFacade(
                                localizer, dm.getCurrentUser().getLocale()));

        return vo;
    }

    /**
     * Returns true if an image is defined for the product with the given key.
     * 
     * @return true if an image is defined for the product with the given key.
     */
    public boolean isImageDefined(Organization organization) {

        boolean flag = imgSrv.read(organization.getKey(),
                ImageType.ORGANIZATION_IMAGE) != null;

        return flag;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOOperatorOrganization updateOrganization(
            VOOperatorOrganization voOrganization,
            VOImageResource voImageResource)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            ValidationException, ConcurrentModificationException,
            DistinguishedNameException, OrganizationAuthorityException,
            IncompatibleRolesException, PSPIdentifierForSellerException,
            PaymentDataException, ImageException,
            AddMarketingPermissionException, NonUniqueBusinessKeyException {
        try {
            return updateOrganizationIntern(voOrganization, voImageResource);
        } catch (ObjectNotFoundException | OrganizationAuthorityException
                | DistinguishedNameException | ConcurrentModificationException
                | ValidationException | ImageException | PaymentDataException
                | PSPIdentifierForSellerException
                | IncompatibleRolesException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    VOOperatorOrganization updateOrganizationIntern(
            VOOperatorOrganization voOrganization,
            VOImageResource voImageResource) throws ObjectNotFoundException,
            ValidationException, ConcurrentModificationException,
            DistinguishedNameException, OrganizationAuthorityException,
            IncompatibleRolesException, PSPIdentifierForSellerException,
            PaymentDataException, ImageException,
            AddMarketingPermissionException, NonUniqueBusinessKeyException {

        // Get the corresponding organization object
        Organization tmpOrganization = new Organization();
        tmpOrganization.setOrganizationId(voOrganization.getOrganizationId());
        Organization organizationObj = (Organization) dm
                .getReferenceByBusinessKey(tmpOrganization);

        if (isVendor(voOrganization)) {
            organizationObj = OrganizationAssembler
                    .updateVendor(organizationObj, voOrganization);
        } else {
            if (voImageResource != null) {
                // only suppliers and technology providers are allowed to
                // save images
                throw new ImageException(
                        "Saving an image for organization '"
                                + organizationObj.getKey()
                                + "' is not allowed because it is not a supplier or technology provider.",
                        ImageException.Reason.NO_SELLER);
            }
            organizationObj = OrganizationAssembler
                    .updateCustomer(organizationObj, voOrganization);
        }

        accMgmt.checkDistinguishedName(organizationObj);
        updateDomicileCountry(voOrganization, organizationObj);
        updateTenant(organizationObj, voOrganization);
        // important: add the new roles before setting the PSP identifier,
        // so the updated roles are validated when setting the PSP identifier
        updateOrganizationRoles(voOrganization, organizationObj);
        updatePaymentTypes(voOrganization);

        updateOperatorRevenueShare(organizationObj,
                voOrganization.getOperatorRevenueShare());

        updateOrganizationDescription(organizationObj.getKey(),
                voOrganization.getDescription());

        // check if image is set, WS 1.1 will send null at this point
        if (voImageResource != null) {
            final ImageResource imageResource = new ImageResource();
            imageResource.setObjectKey(organizationObj.getKey());
            imageResource.setContentType(voImageResource.getContentType());
            imageResource.setBuffer(voImageResource.getBuffer());
            imageResource.setImageType(voImageResource.getImageType());
            accMgmt.processImage(imageResource, voOrganization.getKey());
        }

        dm.flush();
        voOrganization = OrganizationAssembler.toVOOperatorOrganization(
                organizationObj, false, createLocalizerFacade());

        return voOrganization;
    }

    private void updatePaymentTypes(VOOperatorOrganization voOrganization)
            throws ObjectNotFoundException, OrganizationAuthorityException,
            PSPIdentifierForSellerException, PaymentDataException {
        List<VOPaymentType> desiredPaymentypes = voOrganization
                .getPaymentTypes();
        if (desiredPaymentypes != null && desiredPaymentypes.size() > 0) {
            HashSet<String> voTypes = new HashSet<>();
            for (VOPaymentType type : desiredPaymentypes) {
                voTypes.add(type.getPaymentTypeId());
            }
            addAvailablePaymentTypesIntern(voOrganization, voTypes);
        }
    }

    private void updateOrganizationRoles(VOOperatorOrganization voOrganization,
            Organization organizationObj)
            throws OrganizationAuthorityException, IncompatibleRolesException,
            ObjectNotFoundException, AddMarketingPermissionException {

        List<OrganizationRoleType> desiredRoles = voOrganization
                .getOrganizationRoles();
        OrganizationRoleValidator.containsMultipleSellerRoles(desiredRoles,
                LogMessageIdentifier.WARN_ADDING_INCOMPATIBLE_ROLE_TO_ORGANIZATION_FAILED);
        if (desiredRoles != null && desiredRoles.size() > 0) {
            for (OrganizationRoleType role : desiredRoles) {
                if (!organizationObj.hasRole(role)) {
                    addOrganizationToRoleIntern(
                            voOrganization.getOrganizationId(), role);
                }
            }
        }
    }

    private void updateDomicileCountry(VOOperatorOrganization voOrganization,
            Organization organizationObj) throws ObjectNotFoundException {
        SupportedCountry sc = new SupportedCountry(
                voOrganization.getDomicileCountry());
        sc = (SupportedCountry) dm.getReferenceByBusinessKey(sc);
        organizationObj.setDomicileCountry(sc);
    }

    private void updateTenant(Organization organization,
            VOOperatorOrganization voOrganization)
            throws ObjectNotFoundException {

        long tenantKey = voOrganization.getTenantKey();

        if (voOrganization.getTenantKey() == 0) {
            organization.setTenant(null);
        } else {
            Tenant tenant = dm.getReference(Tenant.class, tenantKey);
            organization.setTenant(tenant);
        }
    }

    private boolean isVendor(VOOperatorOrganization voOrganization) {
        boolean isVendor = false;
        if (voOrganization.getOrganizationRoles() != null) {
            for (OrganizationRoleType role : voOrganization
                    .getOrganizationRoles()) {
                if (role == OrganizationRoleType.SUPPLIER
                        || role == OrganizationRoleType.TECHNOLOGY_PROVIDER
                        || role == OrganizationRoleType.RESELLER
                        || role == OrganizationRoleType.BROKER) {
                    isVendor = true;
                    break;
                }
            }
        }
        return isVendor;
    }

    LocalizerFacade createLocalizerFacade() {
        return new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    }

    private void updateOrganizationDescription(long key, String description) {
        if (description != null) {
            localizer.storeLocalizedResource(dm.getCurrentUser().getLocale(),
                    key, LocalizedObjectTypes.ORGANIZATION_DESCRIPTION,
                    description);
        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOOrganization> getOrganizations(String organizationIdPattern,
            List<OrganizationRoleType> organizationRoleTypes)
            throws OrganizationAuthoritiesException {
        return getOrganizationsWithLimit(organizationIdPattern,
                organizationRoleTypes, DB_SEARCH_LIMIT);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOOrganization> getOrganizationsWithLimit(
            String organizationIdPattern,
            List<OrganizationRoleType> organizationRoleTypes,
            Integer queryLimit) throws OrganizationAuthoritiesException {
        Query query = dm
                .createNamedQuery("Organization.findOrganizationsByIdAndRole");
        query.setMaxResults(queryLimit.intValue());
        query.setParameter("organizationId", organizationIdPattern);
        if (organizationRoleTypes.isEmpty()) {
            organizationRoleTypes.add(OrganizationRoleType.CUSTOMER);
            organizationRoleTypes.add(OrganizationRoleType.SUPPLIER);
            organizationRoleTypes.add(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        }
        query.setParameter("organizationRoleTypes", organizationRoleTypes);

        LocalizerFacade lf = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        List<VOOrganization> result = new ArrayList<>();
        for (Organization org : ParameterizedTypes
                .iterable(query.getResultList(), Organization.class)) {
            result.add(OrganizationAssembler.toVOOrganization(org, false, lf));
        }

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOUserDetails> getUsers()
            throws OrganizationAuthoritiesException {
        Query query = dm.createQuery(
                "select pu.dataContainer.userId, pu.dataContainer.email,o.dataContainer.name, o.dataContainer.organizationId, pu.dataContainer.status, pu.key from PlatformUser pu left join pu.organization o");

        List<VOUserDetails> result = new ArrayList<>();
        final List resultList = query.getResultList();
        for (Object o : resultList) {
            Object[] row = (Object[]) o;
            final VOUserDetails userDetails = new VOUserDetails();
            userDetails.setUserId((String) row[USERID_INDEX]);
            userDetails.setEMail((String) row[EMAIL_INDEX]);
            userDetails.setOrganizationName((String) row[ORGN_NAME_INDEX]);
            userDetails.setOrganizationId((String) row[ORG_ID_INDEX]);
            userDetails.setStatus((UserAccountStatus) row[STATUS_INDEX]);
            userDetails.setKey((Long) row[TKEY_INDEX_5]);
            result.add(userDetails);
        }
        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOUserDetails> getUnassignedUsersByOrg(Long subscriptionKey,
            Long organizationKey) {
        Query query = dm.createNativeQuery(
                "select distinct usr.tkey, usr.userid, usr.firstname, usr.lastname from  PlatformUser as usr "
                        + " where not exists (select 1 from UsageLicense as lic1 where lic1.subscription_tkey=:subscriptionKey and lic1.user_tkey=usr.tkey) and usr.organizationkey=:organizationKey");

        query.setParameter("subscriptionKey", subscriptionKey);
        query.setParameter("organizationKey", organizationKey);
        List<VOUserDetails> result = new ArrayList<>();
        List<Object[]> resultList = query.getResultList();
        VOUserDetails pu;
        for (Object[] cols : resultList) {
            pu = new VOUserDetails();
            pu.setKey(((BigInteger) cols[TKEY_INDEX]).longValue());
            pu.setUserId((String) cols[ID_INDEX]);
            pu.setFirstName((String) cols[FIRST_NAME_INDEX]);
            pu.setLastName((String) cols[LAST_NAME_INDEX]);
            result.add(pu);
        }
        return result;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "SUBSCRIPTION_MANAGER",
            "UNIT_ADMINISTRATOR" })
    public List<VOUserDetails> getSubscriptionOwnersForAssignment(
            Long organizationKey) {
        Query query = dm.createNativeQuery(
                "select distinct usr.tkey, usr.userid, usr.firstname, usr.lastname "
                        + "from platformuser as usr "
                        + "left outer join roleassignment as ass on usr.tkey=ass.user_tkey "
                        + "where usr.organizationkey=:organizationKey and (ass.userrole_tkey=8 or ass.userrole_tkey=9 or ass.userrole_tkey=1);");
        query.setParameter("organizationKey", organizationKey);
        List<VOUserDetails> result = new ArrayList<>();
        List<Object[]> resultList = query.getResultList();
        VOUserDetails pu;
        for (Object[] cols : resultList) {
            pu = new VOUserDetails();
            pu.setKey(((BigInteger) cols[TKEY_INDEX]).longValue());
            pu.setUserId((String) cols[ID_INDEX]);
            pu.setFirstName((String) cols[FIRST_NAME_INDEX]);
            pu.setLastName((String) cols[LAST_NAME_INDEX]);
            result.add(pu);
        }
        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void saveConfigurationSettings(List<VOConfigurationSetting> settings)
            throws OrganizationAuthoritiesException, ValidationException,
            ConcurrentModificationException, DuplicateTenantIdException {

        for (VOConfigurationSetting setting : settings) {
            saveConfigurationSetting(setting);
        }

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public boolean startPaymentProcessing()
            throws OrganizationAuthoritiesException {

        boolean result = payProc.chargeForOutstandingBills();

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Map<String, String> getAvailableAuditLogOperations() {

        return AuditLogOperationGroups.getInstance().getAvailableOperations();

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public byte[] getUserOperationLog(List<String> operationIds, long fromDate,
            long toDate)
            throws ValidationException, AuditLogTooManyRowsException {

        BLValidator.isValidDateRange(new Date(fromDate), new Date(toDate));
        byte[] resultCsv = auditLogService.loadAuditLogs(operationIds, fromDate,
                toDate);
        return resultCsv;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Map<String, String> getAvailableAuditLogOperationGroups() {
        return AuditLogOperationGroups.getInstance()
                .getAvailableOperationGroups();

    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOPSP> getPSPs() {

        List<VOPSP> result = new ArrayList<>();
        Query query = dm.createNamedQuery("PSP.getAll");
        List<PSP> psps = ParameterizedTypes.list(query.getResultList(),
                PSP.class);
        result = PSPAssembler.toVos(psps, new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale()));

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOPSP savePSP(VOPSP psp)
            throws ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException, ObjectNotFoundException {

        ArgumentValidator.notNull("psp", psp);
        PSP pspToStore = null;
        if (psp.getKey() != 0) {
            pspToStore = dm.getReference(PSP.class, psp.getKey());
        } else {
            pspToStore = new PSP();
            psp.setVersion(0);
        }
        handleSettings(pspToStore, psp);

        // if the identifier was changed or the object is not managed by the em
        if (dm.contains(pspToStore)
                && !pspToStore.getIdentifier().equals(psp.getId())
                || !dm.contains(pspToStore)) {
            // validate BK uniqueness
            PSP template = new PSP();
            template.setIdentifier(psp.getId());
            dm.validateBusinessKeyUniqueness(template);
        }

        pspToStore = PSPAssembler.updatePSP(psp, pspToStore);
        if (!dm.contains(pspToStore)) {
            dm.persist(pspToStore);
        }
        dm.flush();
        VOPSP result = PSPAssembler.toVo(pspToStore, new LocalizerFacade(
                localizer, dm.getCurrentUser().getLocale()));

        return result;
    }

    /**
     * Handles the settings of the PSP, creates new ones, updates existing ones
     * and deletes missing ones.
     * 
     * @param pspToStore
     *            The domain object containing the already existing settings.
     * @param psp
     *            The value object containing the target set of settings.
     * @throws ValidationException
     *             If the validation of the specified settings fails.
     * @throws ConcurrentModificationException
     *             Thrown in case the psp setting has changed in the meantime.
     * @throws ObjectNotFoundException
     *             Thrown in case the psp setting cannot be found.
     */
    private void handleSettings(PSP pspToStore, VOPSP psp)
            throws ConcurrentModificationException, ValidationException,
            ObjectNotFoundException {
        List<VOPSPSetting> pspSettings = psp.getPspSettings();
        PSPSettingAssembler.validateVOSettings(pspSettings);
        Map<Long, PSPSetting> existingSettings = new HashMap<>();
        for (PSPSetting setting : pspToStore.getSettings()) {
            existingSettings.put(Long.valueOf(setting.getKey()), setting);
        }
        for (VOPSPSetting setting : pspSettings) {
            PSPSetting updatedSetting = existingSettings
                    .remove(Long.valueOf(setting.getKey()));
            if (updatedSetting == null) {
                if (setting.getKey() != 0) {
                    updatedSetting = dm.getReference(PSPSetting.class,
                            setting.getKey());
                } else {
                    updatedSetting = new PSPSetting();
                    setting.setVersion(0);
                }
            }
            updatedSetting = PSPSettingAssembler.updatePSPSetting(setting,
                    updatedSetting);
            if (!pspToStore.getSettings().contains(updatedSetting)) {
                pspToStore.addPSPSetting(updatedSetting);
            }
        }
        // finally remove all settings that were not contained in the input
        for (PSPSetting settingToRemove : existingSettings.values()) {
            pspToStore.removePSPSetting(settingToRemove);
            dm.remove(settingToRemove);
        }
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOPSPAccount> getPSPAccounts(VOOrganization organization)
            throws ObjectNotFoundException {

        ArgumentValidator.notNull("organization", organization);
        List<VOPSPAccount> accounts = new ArrayList<>();

        Organization org = dm.getReference(Organization.class,
                organization.getKey());
        List<PSPAccount> pspAccounts = org.getPspAccounts();
        accounts = PSPAccountAssembler.toVos(pspAccounts, new LocalizerFacade(
                localizer, dm.getCurrentUser().getLocale()));

        return accounts;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOPSPAccount savePSPAccount(VOOrganization organization,
            VOPSPAccount account)
            throws ObjectNotFoundException, OrganizationAuthorityException,
            ConcurrentModificationException, ValidationException {

        ArgumentValidator.notNull("organization", organization);
        ArgumentValidator.notNull("account", account);
        ArgumentValidator.notNull("account.psp", account.getPsp());

        Organization org = dm.getReference(Organization.class,
                organization.getKey());
        if (!org.hasRole(OrganizationRoleType.SUPPLIER)
                && !org.hasRole(OrganizationRoleType.RESELLER)) {
            OrganizationAuthorityException oae = new OrganizationAuthorityException(
                    String.format(
                            "Insufficient authorities for organization '%s'. Required role '%s'.",
                            organization.getOrganizationId(),
                            OrganizationRoleType.SUPPLIER.name() + ", "
                                    + OrganizationRoleType.RESELLER.name()),
                    new Object[] { OrganizationRoleType.SUPPLIER.name() + ", "
                            + OrganizationRoleType.RESELLER.name() });
            logger.logError(Log4jLogger.SYSTEM_LOG, oae,
                    LogMessageIdentifier.ERROR_OPERATION_FAILED_ORGANIZATION_NOT_ROLE_AS_SUPPLIER,
                    "creating PSP account",
                    Long.toString(organization.getKey()),
                    String.valueOf(OrganizationRoleType.SUPPLIER) + ", "
                            + String.valueOf(OrganizationRoleType.RESELLER));
            throw oae;
        }

        PSP psp = dm.getReference(PSP.class, account.getPsp().getKey());
        PSPAccount pspAccount = null;
        if (account.getKey() != 0) {
            pspAccount = dm.getReference(PSPAccount.class, account.getKey());
        } else {
            pspAccount = new PSPAccount();
            account.setVersion(0);
        }

        // validate that not two accounts are created for the same org and psp
        // pair
        if (org.getPspAccountForPsp(psp) != null && !dm.contains(pspAccount)) {
            String[] params = new String[] { org.getOrganizationId(),
                    psp.getIdentifier() };
            ValidationException ve = new ValidationException(
                    ReasonEnum.DUPLICATE_PSP_ACCOUNT, "account", params);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                    LogMessageIdentifier.WARN_DUPLICATE_PSP_ACCOUNT, params);
            throw ve;
        }

        pspAccount = PSPAccountAssembler.updatePSPAccount(account, pspAccount);
        pspAccount.setOrganization(org);
        pspAccount.setPsp(psp);

        if (!dm.contains(pspAccount)) {
            try {
                dm.persist(pspAccount);
            } catch (NonUniqueBusinessKeyException e) {
                // Must not occur, as PSPAccount has no business key
                SaaSSystemException sse = new SaaSSystemException(
                        "Creating of PSPAccount failed", e);
                logger.logError(
                        LogMessageIdentifier.ERROR_NONUNIQUEKEY_EXCEPTION_ALTHOUGH_NO_BUSINESS_KEY);
                throw sse;
            }
        }

        dm.flush();
        VOPSPAccount result = PSPAccountAssembler.toVo(pspAccount,
                new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOPaymentType> getPaymentTypes(VOPSP psp)
            throws ObjectNotFoundException {

        ArgumentValidator.notNull("psp", psp);

        PSP currentPsp = dm.getReference(PSP.class, psp.getKey());
        List<PaymentType> paymentTypes = currentPsp.getPaymentTypes();
        List<VOPaymentType> result = new ArrayList<>();
        result.addAll(PaymentTypeAssembler.toVOPaymentTypes(paymentTypes,
                new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale())));

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOPaymentType savePaymentType(VOPSP psp, VOPaymentType paymentType)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, ConcurrentModificationException {

        ArgumentValidator.notNull("psp", psp);
        ArgumentValidator.notNull("paymentType", paymentType);

        PSP currentPsp = dm.getReference(PSP.class, psp.getKey());

        PaymentType pt;
        if (paymentType.getKey() != 0) {
            pt = dm.getReference(PaymentType.class, paymentType.getKey());
        } else {
            pt = PaymentTypeAssembler.toPaymentType(paymentType);
        }

        // if the identifier was changed or the object is not managed by the em
        if (dm.contains(pt)
                && !pt.getPaymentTypeId().equals(paymentType.getPaymentTypeId())
                || !dm.contains(pt)) {
            // validate BK uniqueness
            PaymentType template = new PaymentType();
            template.setPaymentTypeId(paymentType.getPaymentTypeId());
            template.setKey(paymentType.getKey());
            dm.validateBusinessKeyUniqueness(template);
        }

        pt.setPsp(currentPsp);

        if (!dm.contains(pt)) {
            dm.persist(pt);
        } else {
            PaymentTypeAssembler.updatePaymentType(paymentType, pt);
        }
        dm.flush();

        VOPaymentType result = PaymentTypeAssembler.toVOPaymentType(pt,
                new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));
        localizer.storeLocalizedResource(dm.getCurrentUser().getLocale(),
                result.getKey(), LocalizedObjectTypes.PAYMENT_TYPE_NAME,
                paymentType.getName());

        return result;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public byte[] getSupplierRevenueList(long month) {

        PriceConverter priceConverter = new PriceConverter(LocaleHandler
                .getLocaleFromString(dm.getCurrentUser().getLocale()));
        SupplierRevenueSqlResult result = SupplierRevenueSqlResult
                .executeQuery(dm, month);
        String header = String
                .format("FROM,TO,ID,NAME,AMOUNT,CURRENCY,MARKETPLACE%n");
        StringBuffer csvResult = new StringBuffer(header);
        List<SupplierRevenueSqlResult.RowData> rowDataList = result
                .getRowData();
        for (SupplierRevenueSqlResult.RowData rowData : rowDataList) {
            String[] row = new String[7];
            row[0] = rowData.fromDate;
            row[1] = rowData.toDate;
            row[2] = rowData.supplierId;
            row[3] = rowData.supplierName;
            row[4] = priceConverter
                    .getValueToDisplay(new BigDecimal(rowData.amount), false);
            row[5] = rowData.currency;
            row[6] = rowData.marketplace;
            csvResult.append(
                    String.format("%s%n", CsvCreator.createCsvLine(row)));
        }
        byte[] resultCsv = XMLConverter.toUTF8(csvResult.toString());

        return resultCsv;
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void deleteConfigurationSetting(Long key)
            throws ObjectNotFoundException {
        ConfigurationSetting cs = dm.getReference(ConfigurationSetting.class,
                key);
        cs.setValue(cs.getInformationId().getFallBackValue());
        configService.setConfigurationSetting(cs);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public VOConfigurationSetting getConfigurationSetting(Long key)
            throws ObjectNotFoundException {
        ConfigurationSetting cs = dm.getReference(ConfigurationSetting.class,
                key);
        return ConfigurationSettingAssembler.toValueObject(cs);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public Collection<VOSubscriptionUsageEntry> getSubscriptionUsageReport() {
        return SubscriptionUsageQuery.execute(dm);
    }

    public void setDm(DataService dm) {
        this.dm = dm;
    }
}
