/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.QueryBasedObjectFactory;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.AuditLogDataInterceptor;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.Invariants;
import org.oscm.validation.VersionAndKeyValidator;
import org.oscm.validator.BLValidator;
import org.oscm.validator.OrganizationRoleValidator;
import org.oscm.validator.ProductValidator;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceOperationException.Reason;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOLocalizedText;

@Stateless
@Local(ServiceProvisioningPartnerServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class,
        AuditLogDataInterceptor.class })
public class ServiceProvisioningPartnerServiceLocalBean implements
        ServiceProvisioningPartnerServiceLocal {

    static final String FIELD_REVENUE_SHARE = "revenue share";

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = ImageResourceServiceLocal.class)
    ImageResourceServiceLocal imgrsl;

    @EJB(beanInterface = ServiceProvisioningServiceLocal.class)
    ServiceProvisioningServiceLocal spsl;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @Resource
    SessionContext sessionCtx;

    @EJB
    LandingpageServiceLocal landingpageService;

    @EJB
    ServiceAuditLogCollector audit;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ServiceProvisioningPartnerServiceLocalBean.class);

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public RevenueShareModel getOperatorRevenueShare(long productKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Product product = dm.getReference(Product.class, productKey);
        verifyOwningPermission(product);
        validateOperatorRevenueShare(product);

        return product.getCatalogEntries().get(0).getOperatorPriceModel();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public RevenueShareModel getDefaultOperatorRevenueShare(long productKey)
            throws ObjectNotFoundException, OperationNotPermittedException {
        Product product = dm.getReference(Product.class, productKey);
        verifyOwningPermission(product);
        validateDefaultOperatorRevenueShare(product);

        return product.getVendor().getOperatorPriceModel();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<RevenueShareModelType, RevenueShareModel> getRevenueShareModelsForProduct(
            long serviceKey, boolean isStatusCheckNeeded)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException {

        Product product = loadProduct(serviceKey, isStatusCheckNeeded);

        verifyOwningPermission(product);

        checkTemplateOrPartnerSpecificCopy(product);

        CatalogEntry ce = product.getCatalogEntries().get(0);
        if (product.isCopy()) {
            validateRevenueShareOfProductCopy(ce);
        }
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = getPriceModelsForEntry(ce);

        return revenueShareModels;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void saveOperatorRevenueShare(long serviceKey,
            RevenueShareModel newRevenueShare, int newRevenueShareVersion)
            throws ValidationException, ConcurrentModificationException,
            ObjectNotFoundException, ServiceOperationException {

        ArgumentValidator.notNull("newRevenueShare", newRevenueShare);
        Product product = dm.getReference(Product.class, serviceKey);
        validateProductTemplate(product);
        validateOperatorRevenueShare(product);

        CatalogEntry ce = product.getCatalogEntries().get(0);
        try {
            updateRevenueShare(newRevenueShare, ce.getOperatorPriceModel(),
                    newRevenueShareVersion);
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<RevenueShareModelType, RevenueShareModel> saveRevenueShareModelsForProduct(
            long serviceKey, RevenueShareModel brokerRevenueShareNew,
            RevenueShareModel resellerRevenueShareNew,
            int brokerRevenueShareNewVersion, int resellerRevenueShareNewVersion)
            throws ObjectNotFoundException, ServiceOperationException,
            NonUniqueBusinessKeyException, ValidationException,
            ConcurrentModificationException {

        Product product = dm.getReference(Product.class, serviceKey);

        checkTemplateOrPartnerSpecificCopy(product);
        if (brokerRevenueShareNew == null && resellerRevenueShareNew == null) {
            ValidationException ve = new ValidationException(
                    ValidationException.ReasonEnum.ONE_OF_PARTNER_REVENUE_SHARE_MANDATORY,
                    null, null);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ve,
                    LogMessageIdentifier.WARN_ONE_OF_PARTNER_REVENUE_SHARE_MANDATORY);
            throw ve;
        }

        CatalogEntry ce = product.getCatalogEntries().get(0);

        if (product.isCopy()) {
            validateRevenueShareOfProductCopy(ce);
        } else {
            Invariants.assertNotNull(ce.getMarketplace(),
                    "marketplace for service must not be null");
        }

        ce = updatePartnerRevenueShareForCatalogEntry(ce,
                brokerRevenueShareNew, resellerRevenueShareNew,
                brokerRevenueShareNewVersion, resellerRevenueShareNewVersion);

        return getPriceModelsForEntry(ce);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public CatalogEntry getCatalogEntryForProduct(long serviceKey)
            throws ObjectNotFoundException, ServiceOperationException {

        Product product = dm.getReference(Product.class, serviceKey);
        checkTemplateOrPartnerSpecificCopy(product);

        if (product.getCatalogEntries().isEmpty()) {
            return null;
        }
        return product.getCatalogEntries().get(0);
    }

    Product loadProduct(long serviceKey, boolean isStatusCheckNeeded)
            throws ObjectNotFoundException, ServiceStateException {
        Product product = dm.getReference(Product.class, serviceKey);
        if (isStatusCheckNeeded) {
            if (product.getStatus() != ServiceStatus.ACTIVE
                    && product.getStatus() != ServiceStatus.INACTIVE
                    && product.getStatus() != ServiceStatus.SUSPENDED) {
                ServiceStateException sse = new ServiceStateException(
                        product.getStatus(), ServiceStatus.ACTIVE.name() + ", "
                                + ServiceStatus.INACTIVE.name() + ", "
                                + ServiceStatus.SUSPENDED.name(),
                        ProductAssembler.getProductId(product));
                logger.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.WARN_INVALID_SERVICE_STATUS,
                        ServiceStatus.ACTIVE.name() + ", "
                                + ServiceStatus.INACTIVE.name() + ", "
                                + ServiceStatus.SUSPENDED.name(), product
                                .getStatus().name(), product.getProductId());
                throw sse;
            }
        }
        return product;
    }

    void verifyOwningPermission(Product product)
            throws OperationNotPermittedException {
        Organization supplier = dm.getCurrentUser().getOrganization();
        if (!supplier.hasRole(OrganizationRoleType.PLATFORM_OPERATOR)
                && !supplier.hasRole(OrganizationRoleType.MARKETPLACE_OWNER)) {
            PermissionCheck.owns(product, supplier, logger, sessionCtx);
        }
    }

    void checkTemplateOrPartnerSpecificCopy(Product product)
            throws ServiceOperationException {
        if (product.getOwningSubscription() != null
                || product.getTargetCustomer() != null) {
            ServiceOperationException sof = new ServiceOperationException(
                    Reason.OPERATION_ALLOWED_ONLY_FOR_TEMPLATE_SERVICE_OR_COPY_FOR_RESALE);
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    sof,
                    LogMessageIdentifier.ERROR_OPERATION_ALLOWED_ONLY_FOR_TEMPLATE_SERVICE_OR_COPY_FOR_RESALE,
                    Long.toString(product.getKey()));
            throw sof;
        }
    }

    void validateRevenueShareOfProductCopy(CatalogEntry entry) {
        if (entry.getBrokerPriceModel() == null
                && entry.getResellerPriceModel() == null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The price model of catalog entry of a service copy must not be null.");
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_MISSING_PRICE_MODEL_FOR_COPY);
            throw sse;
        }
    }

    void validateOperatorRevenueShare(Product product) {
        CatalogEntry entry = product.getCatalogEntries().get(0);
        if (ServiceType.TEMPLATE == entry.getProduct().getType()
                && entry.getOperatorPriceModel() == null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The catalog entry for the service template "
                            + product.getKey()
                            + " does not have an operator price model.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_MISSING_OPERATOR_PRICE_MODEL_FOR_SERVICE_TEMPLATE,
                    String.valueOf(product.getKey()));
            throw sse;
        } else if (ServiceType.TEMPLATE != entry.getProduct().getType()
                && entry.getOperatorPriceModel() != null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The catalog entry for the service copy "
                            + product.getKey()
                            + " has an operator price model.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_AVAILABLE_OPERATOR_PRICE_MODEL_FOR_SERVICE_COPY,
                    String.valueOf(product.getKey()));
            throw sse;
        }
    }

    void validateDefaultOperatorRevenueShare(Product product) {
        if (product.getVendor().hasRole(OrganizationRoleType.SUPPLIER)
                && product.getVendor().getOperatorPriceModel() == null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The supplier organization "
                            + product.getVendor().getOrganizationId()
                            + " does not have an operator price model.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_MISSING_OPERATOR_PRICE_MODEL_FOR_SUPPLIER,
                    product.getVendor().getOrganizationId());
            throw sse;
        } else if (!product.getVendor().hasRole(OrganizationRoleType.SUPPLIER)
                && product.getVendor().getOperatorPriceModel() != null) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The non supplier organization "
                            + product.getVendor().getOrganizationId()
                            + " has an operator price model.");
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_AVAILABLE_OPERATOR_PRICE_MODEL_FOR_NON_SUPPLIER,
                    product.getVendor().getOrganizationId());
            throw sse;
        }
    }

    Map<RevenueShareModelType, RevenueShareModel> getPriceModelsForEntry(
            CatalogEntry ce) {
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = new HashMap<RevenueShareModelType, RevenueShareModel>();
        // The marketplace is not allowed to have null partner models as they
        // are mandatory.
        Marketplace marketplace = ce.getMarketplace();
        if (marketplace != null) {
            Invariants.assertNotNull(marketplace.getBrokerPriceModel(),
                    "broker price model of marketplace must not be null");

            Invariants.assertNotNull(marketplace.getResellerPriceModel(),
                    "reseller price model of marketplace must not be null");
        }

        // First get the partner models of the catalog entry
        RevenueShareModel brokerPriceModel = ce.getBrokerPriceModel();
        RevenueShareModel resellerPriceModel = ce.getResellerPriceModel();

        // If they do not exist in the catalog entry, then retrieve the partner
        // price models from the marketplace
        if (brokerPriceModel == null) {
            if (marketplace != null) {
                brokerPriceModel = new RevenueShareModel();
                brokerPriceModel.setRevenueShare(marketplace
                        .getBrokerPriceModel().getRevenueShare());
                brokerPriceModel.setRevenueShareModelType(marketplace
                        .getBrokerPriceModel().getRevenueShareModelType());
            }
        }

        if (resellerPriceModel == null) {
            if (marketplace != null) {
                resellerPriceModel = marketplace.getResellerPriceModel();
                resellerPriceModel = new RevenueShareModel();
                resellerPriceModel.setRevenueShare(marketplace
                        .getResellerPriceModel().getRevenueShare());
                resellerPriceModel.setRevenueShareModelType(marketplace
                        .getResellerPriceModel().getRevenueShareModelType());
            }
        }

        if (brokerPriceModel != null) {
            revenueShareModels.put(RevenueShareModelType.BROKER_REVENUE_SHARE,
                    brokerPriceModel);
        }
        if (resellerPriceModel != null) {
            revenueShareModels.put(
                    RevenueShareModelType.RESELLER_REVENUE_SHARE,
                    resellerPriceModel);
        }

        return revenueShareModels;

    }

    CatalogEntry updatePartnerRevenueShareForCatalogEntry(
            CatalogEntry catalogEntry, RevenueShareModel brokerRevenueShareNew,
            RevenueShareModel resellerRevenueShareNew,
            int brokerRevenueShareNewVersion, int resellerRevenueShareNewVersion)
            throws ValidationException, ConcurrentModificationException,
            NonUniqueBusinessKeyException {
        try {
            if (brokerRevenueShareNew != null) {
                RevenueShareModel brokerRevenueShareToBeUpdated = catalogEntry
                        .getBrokerPriceModel();
                updateRevenueShareForCatalogEntry(catalogEntry,
                        brokerRevenueShareToBeUpdated, brokerRevenueShareNew,
                        RevenueShareModelType.BROKER_REVENUE_SHARE,
                        brokerRevenueShareNewVersion);
            }
            if (resellerRevenueShareNew != null) {
                RevenueShareModel resellerRevenueShareToBeUpdated = catalogEntry
                        .getResellerPriceModel();
                updateRevenueShareForCatalogEntry(catalogEntry,
                        resellerRevenueShareToBeUpdated,
                        resellerRevenueShareNew,
                        RevenueShareModelType.RESELLER_REVENUE_SHARE,
                        resellerRevenueShareNewVersion);
            }
            dm.persist(catalogEntry);
        } catch (ValidationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
        return catalogEntry;
    }

    RevenueShareModel updateRevenueShareForCatalogEntry(
            CatalogEntry catalogEntry,
            RevenueShareModel revenueShareToBeUpdated,
            RevenueShareModel revenueShareNew, RevenueShareModelType type,
            int version) throws NonUniqueBusinessKeyException,
            ValidationException, ConcurrentModificationException {

        if (revenueShareToBeUpdated == null) {
            revenueShareToBeUpdated = createNewRevenueShareModel(type);

            revenueShareToBeUpdated = updateRevenueShare(revenueShareNew,
                    revenueShareToBeUpdated, version);
            dm.persist(revenueShareToBeUpdated);
            if (type == RevenueShareModelType.BROKER_REVENUE_SHARE) {
                catalogEntry.setBrokerPriceModel(revenueShareToBeUpdated);
            } else {
                catalogEntry.setResellerPriceModel(revenueShareToBeUpdated);
            }
        } else {
            revenueShareToBeUpdated = updateRevenueShare(revenueShareNew,
                    revenueShareToBeUpdated, version);
        }
        return revenueShareToBeUpdated;
    }

    private RevenueShareModel createNewRevenueShareModel(
            RevenueShareModelType type) {
        RevenueShareModel revenueShare = new RevenueShareModel();
        revenueShare.setRevenueShareModelType(type);
        return revenueShare;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Product> getTemplateProducts() {
        Query query = dm.createNamedQuery("Product.getTemplatesInAllStates");
        List<Product> templateProducts = ParameterizedTypes.list(
                query.getResultList(), Product.class);
        return templateProducts;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Product> getProductsForVendor() {
        Organization vendor = dm.getCurrentUser().getOrganization();

        Query query = dm
                .createNamedQuery("Product.getPartnerSpecificCopiesForVendor");
        query.setParameter("vendorKey", Long.valueOf(vendor.getKey()));
        query.setParameter("status", EnumSet.of(ServiceStatus.ACTIVE,
                ServiceStatus.INACTIVE, ServiceStatus.SUSPENDED));

        List<Product> result = ParameterizedTypes.list(query.getResultList(),
                Product.class);
        return result;
    }

    RevenueShareModel updateRevenueShare(RevenueShareModel revenueShare,
            RevenueShareModel toBeUpdated, int version)
            throws ValidationException, ConcurrentModificationException {
        String fieldName = FIELD_REVENUE_SHARE + " for "
                + toBeUpdated.getRevenueShareModelType();
        BLValidator.isInRange(fieldName, revenueShare.getRevenueShare(),
                RevenueShareModel.MIN_REVENUE_SHARE,
                RevenueShareModel.MAX_REVENUE_SHARE);
        if (toBeUpdated.getKey() != 0) {
            VersionAndKeyValidator.verify(toBeUpdated, revenueShare, version);
        }
        toBeUpdated.setRevenueShare(revenueShare.getRevenueShare());
        return toBeUpdated;
    }

    @Override
    public Product grantResalePermission(String templateId, String grantorId,
            String granteeId, OfferingType resaleType)
            throws ValidationException, ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException,
            ServiceOperationException, ConcurrentModificationException,
            OrganizationAuthorityException, ServiceStateException {
        ArgumentValidator.notNull("templateId", templateId);
        ArgumentValidator.notNull("grantorId", grantorId);
        ArgumentValidator.notNull("granteeId", granteeId);
        BLValidator.isValidEnumValue("resaleType", resaleType,
                OfferingType.BROKER, OfferingType.RESELLER);
        Organization caller = dm.getCurrentUser().getOrganization();
        OrganizationRoleValidator.containsSupplier(
                caller.getGrantedRoleTypes(), caller.getOrganizationId());

        Organization granteeOrg = loadOrganization(granteeId);
        validateGrantee(granteeOrg);
        validateResaleType(resaleType, granteeOrg);

        Organization grantorOrg = loadOrganization(grantorId);
        Product productTemplate = loadProduct(templateId, grantorOrg.getKey());
        validateProductTemplate(productTemplate);
        ProductValidator.validateInactiveOrActive(
                productTemplate.getProductId(), productTemplate.getStatus());
        PermissionCheck.owns(productTemplate, grantorOrg, logger, sessionCtx);

        CatalogEntry templCatalogEntry = validateCatalogEntry(productTemplate);
        validatePriceModel(productTemplate);

        Product resaleCopy = loadProductCopyForVendor(granteeOrg,
                productTemplate);
        if (resaleCopy != null) {
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    null,
                    LogMessageIdentifier.WARN_RESALE_PRODUCT_COPY_ALREADY_EXISTS,
                    Long.toString(resaleCopy.getKey()));

            if (resaleCopy.getStatus() == ServiceStatus.DELETED) {
                resaleCopy.setStatus(ServiceStatus.INACTIVE);
                dm.flush();
            }
        } else {
            // Copy the product and set a reference to the broker/reseller
            // organization
            resaleCopy = productTemplate.copyForResale(granteeOrg);
            resaleCopy.setStatus(ServiceStatus.INACTIVE);
            dm.persist(resaleCopy);

            // Create a new catalog entry without a marketplace
            CatalogEntry resaleCatalogEntry = QueryBasedObjectFactory
                    .createCatalogEntry(resaleCopy, null);

            copyPartnerPriceModel(resaleType, templCatalogEntry,
                    templCatalogEntry.getMarketplace(), resaleCatalogEntry);
            resaleCatalogEntry.setVisibleInCatalog(true);
            resaleCatalogEntry.setAnonymousVisible(true);

            dm.persist(resaleCatalogEntry);
            dm.flush();

            if (resaleType == OfferingType.RESELLER) {
                copyResourcesForReseller(granteeOrg, productTemplate,
                        resaleCopy);
            }
        }

        audit.assignResellerBroker(dm, resaleCopy, granteeId, resaleType, true);
        return resaleCopy;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @SuppressWarnings("unchecked")
    public List<Product> getPartnerProductsForTemplate(long serviceKey)
            throws ObjectNotFoundException, ServiceOperationException {

        Product product = dm.getReference(Product.class, serviceKey);
        validateProductTemplate(product);

        Query query = dm
                .createNamedQuery("Product.getPartnerCopiesForTemplate");
        query.setParameter("template", product);
        return query.getResultList();
    }

    private Organization loadOrganization(String orgID)
            throws ObjectNotFoundException {
        Organization org = new Organization();
        org.setOrganizationId(orgID);
        return (Organization) dm.getReferenceByBusinessKey(org);
    }

    private Product loadProduct(String productID, long vendorKey)
            throws ObjectNotFoundException {
        Product product = new Product();
        product.setProductId(productID);
        product.setVendorKey(vendorKey);
        return Product.class.cast(dm.getReferenceByBusinessKey(product));
    }

    private void validateProductTemplate(Product product)
            throws ServiceOperationException {
        if (product.isCopy()) {
            // specified product is not a template
            ServiceOperationException sof = new ServiceOperationException(
                    Reason.SERVICE_IS_NOT_A_TEMPLATE);
            logger.logError(Log4jLogger.SYSTEM_LOG, sof,
                    LogMessageIdentifier.ERROR_SERVICE_IS_NOT_A_TEMPLATE,
                    Long.toString(product.getKey()));
            throw sof;
        }
    }

    private CatalogEntry validateCatalogEntry(Product productTemplate)
            throws ServiceOperationException {
        CatalogEntry templCatalogEntry = productTemplate.getCatalogEntries()
                .get(0);
        if ((templCatalogEntry == null)
                || (templCatalogEntry.getMarketplace() == null)) {
            // specified product is not published to a marketplace
            ServiceOperationException sof = new ServiceOperationException(
                    Reason.SERVICE_NOT_ASSIGNED_TO_MARKETPLACE);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sof,
                    LogMessageIdentifier.ERROR_SERVICE_NOT_ASSIGNED_TO_MARKETPLACE,
                    Long.toString(productTemplate.getKey()));
            throw sof;
        }
        return templCatalogEntry;
    }

    private void validatePriceModel(Product productTemplate)
            throws ServiceOperationException {
        if (productTemplate.getPriceModel() == null) {
            ServiceOperationException sof = new ServiceOperationException(
                    Reason.MISSING_PRICE_MODEL_FOR_TEMPLATE);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sof,
                    LogMessageIdentifier.ERROR_MISSING_PRICE_MODEL_FOR_TEMPLATE,
                    Long.toString(productTemplate.getKey()));
            throw sof;
        }
    }

    private void validateGrantee(Organization granteeOrg)
            throws OrganizationAuthorityException {
        if (!granteeOrg.hasRole(OrganizationRoleType.BROKER)
                && !granteeOrg.hasRole(OrganizationRoleType.RESELLER)) {
            OrganizationAuthorityException ioa = new OrganizationAuthorityException(
                    "Insufficient authorities for organization '"
                            + granteeOrg.getOrganizationId()
                            + "'. Required role: '"
                            + OrganizationRoleType.BROKER.name() + " or "
                            + OrganizationRoleType.RESELLER.name() + "'.",
                    new Object[] { OrganizationRoleType.BROKER.name(),
                            OrganizationRoleType.RESELLER.name() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ioa,
                    LogMessageIdentifier.WARN_ORGANIZATION_ROLE_REQUIRED,
                    Long.toString(granteeOrg.getKey()),
                    OrganizationRoleType.BROKER.toString() + ", "
                            + OrganizationRoleType.RESELLER.toString());
            throw ioa;
        }
    }

    private void validateResaleType(OfferingType resaleType,
            Organization granteeOrg) throws ValidationException {
        if (((resaleType == OfferingType.BROKER) && !granteeOrg
                .hasRole(OrganizationRoleType.BROKER))
                || ((resaleType == OfferingType.RESELLER) && !granteeOrg
                        .hasRole(OrganizationRoleType.RESELLER))) {
            throw new ValidationException(
                    String.format(
                            "ResaleType %s doesn't match the roles of grantee organization.",
                            resaleType.toString()));
        }
    }

    Product loadProductCopyForVendor(Organization vendor, Product product) {
        Query query = dm.createNamedQuery("Product.getProductCopyForVendor");
        query.setParameter("vendorKey", Long.valueOf(vendor.getKey()));
        query.setParameter("product", product);

        List<Product> resultList = ParameterizedTypes.list(
                query.getResultList(), Product.class);
        Product result = null;
        if (resultList.size() > 0) {
            result = resultList.get(0);
        }

        return result;
    }

    private void copyResourcesForReseller(Organization granteeOrg,
            Product productTemplate, Product resaleCopy)
            throws ConcurrentModificationException {
        // Copy the localized resources, that a reseller can change
        List<VOLocalizedText> locPmLicences = localizer.getLocalizedValues(
                productTemplate.getPriceModel().getKey(),
                LocalizedObjectTypes.PRICEMODEL_LICENSE);
        localizer
                .setLocalizedValues(resaleCopy.getKey(),
                        LocalizedObjectTypes.RESELLER_PRICEMODEL_LICENSE,
                        locPmLicences);

        // Copy the default payment types of the reseller
        spsl.copyDefaultPaymentEnablement(resaleCopy, granteeOrg);
    }

    private void copyPartnerPriceModel(OfferingType resaleType,
            CatalogEntry templCatalogEntry, Marketplace templMarketplace,
            CatalogEntry resaleCatalogEntry)
            throws NonUniqueBusinessKeyException {
        if (resaleType == OfferingType.BROKER) {
            RevenueShareModel templBrokerPM = templCatalogEntry
                    .getBrokerPriceModel();
            if (templBrokerPM == null) {
                templBrokerPM = templMarketplace.getBrokerPriceModel();
            }
            resaleCatalogEntry.setBrokerPriceModel(templBrokerPM.copy());
            dm.persist(resaleCatalogEntry.getBrokerPriceModel());
        } else {
            RevenueShareModel templResellerPM = templCatalogEntry
                    .getResellerPriceModel();
            if (templResellerPM == null) {
                templResellerPM = templMarketplace.getResellerPriceModel();
            }
            resaleCatalogEntry.setResellerPriceModel(templResellerPM.copy());
            dm.persist(resaleCatalogEntry.getResellerPriceModel());
        }
    }

    @Override
    public Product revokeResalePermission(String templateId, String grantorId,
            String granteeId) throws ObjectNotFoundException,
            ServiceOperationException, OrganizationAuthorityException,
            OperationNotPermittedException {
        ArgumentValidator.notNull("templateId", templateId);
        ArgumentValidator.notNull("grantorId", grantorId);
        ArgumentValidator.notNull("granteeId", granteeId);

        Organization caller = dm.getCurrentUser().getOrganization();
        OrganizationRoleValidator.containsSupplier(
                caller.getGrantedRoleTypes(), caller.getOrganizationId());

        Organization granteeOrg = loadOrganization(granteeId);
        validateGrantee(granteeOrg);

        Organization grantorOrg = loadOrganization(grantorId);
        Product productTemplate = loadProduct(templateId, grantorOrg.getKey());
        validateProductTemplate(productTemplate);

        Product resaleCopy = loadProductCopyForVendor(granteeOrg,
                productTemplate);
        if (resaleCopy != null) {
            if (resaleCopy.getStatus() != ServiceStatus.DELETED) {
                resaleCopy.setStatus(ServiceStatus.DELETED);
                removeProductFromLandingpage(resaleCopy);
                dm.flush();
            }
        }

        // add for audit log at May 20 2013 begin
        OfferingType resellType = null;
        if (granteeOrg.hasRole(OrganizationRoleType.BROKER)) {
            resellType = OfferingType.BROKER;
        } else {
            resellType = OfferingType.RESELLER;
        }
        audit.assignResellerBroker(dm, resaleCopy, granteeId, resellType, false);
        // add for audit log at May 20 2013 end
        return resaleCopy;
    }

    void removeProductFromLandingpage(Product product) {

        List<CatalogEntry> catalogEntries = product.getCatalogEntries();

        Marketplace marketplace = null;
        if (product.getCatalogEntries().size() > 0) {
            marketplace = catalogEntries.get(0).getMarketplace();
        }
        if (marketplace != null) {
            landingpageService.removeProductFromLandingpage(marketplace,
                    product);
        }
    }

    @Override
    public List<Product> loadSuppliedTemplateServices() {
        Organization organization = dm.getCurrentUser().getOrganization();
        EnumSet<ServiceType> serviceTypes = getServiceTypesForOrg(organization);
        return executeQueryLoadTemplateServices(serviceTypes, organization);
    }

    private EnumSet<ServiceType> getServiceTypesForOrg(
            Organization currentUsersOrg) {
        EnumSet<ServiceType> serviceTypes;
        if (currentUsersOrg.hasRole(OrganizationRoleType.SUPPLIER)) {
            serviceTypes = EnumSet.of(ServiceType.TEMPLATE);
        } else {
            serviceTypes = EnumSet.of(ServiceType.PARTNER_TEMPLATE);
        }
        return serviceTypes;
    }

    @SuppressWarnings("unchecked")
    List<Product> executeQueryLoadTemplateServices(
            EnumSet<ServiceType> serviceTypes, Organization vendor) {
        Query query = dm
                .createNamedQuery("Product.getProductTemplatesForVendor");
        query.setParameter("vendorKey", Long.valueOf(vendor.getKey()));
        query.setParameter("productTypes", serviceTypes);
        query.setParameter("filterOutWithStatus",
                EnumSet.of(ServiceStatus.OBSOLETE, ServiceStatus.DELETED));
        return query.getResultList();
    }
}
