/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.util.*;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.validator.GenericValidator;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.PriceConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.QueryBasedObjectFactory;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.auditlog.interceptor.AuditLogDataInterceptor;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.enumtypes.ImageType.ImageOwnerType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ServiceOperationException.Reason;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.*;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.assembler.*;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogCollector;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocalizationLocal;
import org.oscm.serviceprovisioningservice.local.TagServiceLocal;
import org.oscm.serviceprovisioningservice.verification.PricedParameterChecks;
import org.oscm.serviceprovisioningservice.verification.ServiceVisibilityCheck;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.string.Strings;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.triggerservice.bean.TriggerProcessIdentifiers;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.triggerservice.validator.TriggerProcessValidator;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validation.ImageValidator;
import org.oscm.validator.BLValidator;
import org.oscm.validator.ProductValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Session Bean implementation class ServiceProvisioningServiceBean
 */
@Stateless
@Remote(ServiceProvisioningService.class)
@Local(ServiceProvisioningServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class,
        AuditLogDataInterceptor.class })
public class ServiceProvisioningServiceBean
        implements ServiceProvisioningService, ServiceProvisioningServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ServiceProvisioningServiceBean.class);

    @EJB(beanInterface = SessionServiceLocal.class)
    private SessionServiceLocal pm;

    @EJB(beanInterface = ApplicationServiceLocal.class)
    ApplicationServiceLocal appManager;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB(beanInterface = ServiceProvisioningServiceLocalizationLocal.class)
    ServiceProvisioningServiceLocalizationLocal spsLocalizer;

    @EJB(beanInterface = ImageResourceServiceLocal.class)
    ImageResourceServiceLocal irm;

    @EJB
    private TenantProvisioningServiceBean tenantProvisioning;

    @EJB(beanInterface = TriggerQueueServiceLocal.class)
    TriggerQueueServiceLocal triggerQS;

    @EJB(beanInterface = TagServiceLocal.class)
    TagServiceLocal tagService;

    @EJB(beanInterface = MarketingPermissionServiceLocal.class)
    private MarketingPermissionServiceLocal ms;

    @EJB(beanInterface = SubscriptionService.class)
    SubscriptionService subscriptionService;

    @EJB
    CommunicationServiceLocal commService;

    @EJB
    LandingpageServiceLocal landingpageService;

    @EJB
    PriceModelAuditLogCollector priceModelAudit;

    @EJB
    ServiceAuditLogCollector serviceAudit;

    @EJB
    SubscriptionAuditLogCollector subscriptionAudit;

    @EJB
    ConfigurationServiceLocal configurationService;

    @EJB
    BillingAdapterLocalBean billingAdapterLocalBean;

    @Resource
    private SessionContext sessionCtx;

    private boolean isLocalizedTextChanged;
    private boolean isDescriptionChanged;
    private boolean isShortDescriptionChanged;
    private boolean isCustomTabNameChanged;

    private static String DEFINEIPDOWNGRADE_ON = "ON";
    private static String DEFINEIPDOWNGRADE_OFF = "OFF";
    private static String BOOLEANVALUEYES = "YES";
    private static String BOOLEANVALUENO = "NO";
    public static BigDecimal DEFAULT_PRICE_VALUE = BigDecimal.ZERO;
    public static Long DEFAULT_STEPPED_PRICE_LIMIT = 0L;

    @Override
    public List<VOService> getServicesForMarketplace(String marketplaceId) {
        return getServicesForMarketplace(marketplaceId,
                PerformanceHint.ALL_FIELDS);
    }

    public List<VOService> getServicesForMarketplace(String marketplaceId,
            PerformanceHint performanceHint) {

        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

        Query query = dm.createNamedQuery(
                "Product.getProductsForCustomerOnMarketplace");
        query.setParameter("customer", dm.getCurrentUser().getOrganization());
        query.setParameter("marketplaceId", marketplaceId);
        List<Product> productList = filterProducts(
                ParameterizedTypes.list(query.getResultList(), Product.class),
                marketplaceId);

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        ProductAssembler.prefetchData(productList, facade, performanceHint);
        List<VOService> voServices = new ArrayList<>();
        for (Product product : productList) {
            voServices.add(ProductAssembler.toVOProduct(product, facade,
                    performanceHint));
        }

        return voServices;
    }

    @Override
    public VOServiceEntry getServiceForMarketplace(Long serviceKey,
            String marketplaceId, String locale)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        ArgumentValidator.notNull("serviceKey", serviceKey);

        String localizerLocale;
        boolean subscriptionLimitReached = false;

        Product product = dm.getReference(Product.class,
                serviceKey.longValue());

        // Check if the product is a subscription copy
        verifyNoSubscriptionCopy(product);

        if (dm.getCurrentUserIfPresent() != null) {
            PlatformUser currentUser = dm.getCurrentUser();

            // Set the locale
            if (locale == null) {
                localizerLocale = currentUser.getLocale();
            } else {
                localizerLocale = locale;
            }

            // show suspended products if user is owner of the given marketplace
            // and has MARKETPLACE_OWNER role
            boolean returnSuspended = false;
            Organization org = currentUser.getOrganization();
            List<Marketplace> userMps = org.getMarketplaces();
            for (Marketplace marketplace : userMps) {
                if (marketplace.getMarketplaceId().equals(marketplaceId)) {
                    returnSuspended = currentUser
                            .hasRole(UserRoleType.MARKETPLACE_OWNER);
                    break;
                }
            }

            if (product.getTargetCustomer() != null) {
                if (product.getTargetCustomer() == org) {
                    // The current product is a customer specific product (CSP)
                    // for this customer
                    if (product.getStatus() == ServiceStatus.ACTIVE
                            || (product.getStatus() == ServiceStatus.SUSPENDED
                                    && returnSuspended)) {
                        Product template = product.getTemplate();
                        if (!existsCatalogEntryForMarketplace(template,
                                marketplaceId)) {
                            return null;
                        }
                    } else {
                        // The product is not active.
                        return null;
                    }
                } else {
                    // The user tries to read a CSP of another user ==> not
                    // allowed
                    OperationNotPermittedException onp = new OperationNotPermittedException(
                            "User is not allowed to access customer specific product.");
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                            LogMessageIdentifier.WARN_ACCESS_PRODUCT_FAILED_NOT_TARGET_CUSTOMER,
                            currentUser.getUserId(),
                            Long.toString(product.getKey()));
                    throw onp;
                }
            } else {
                // the current product is not a customer specific product

                // Published on the current MPL?
                if (!existsCatalogEntryForMarketplace(product, marketplaceId)) {
                    return null;
                }

                // Check if there is a copy (=CSP) for the current customer
                Product customerCopy = getCopyForCustomer(product, org);
                if (customerCopy != null) {
                    // There is a CSP for the current product, check if we can
                    // use it
                    if (customerCopy.getStatus() == ServiceStatus.ACTIVE
                            || (product.getStatus() == ServiceStatus.SUSPENDED
                                    && returnSuspended)) {
                        // The product is active => use it
                        product = customerCopy;
                    } else {
                        // CSP exists but is not visible -> "hide" global
                        // product
                        return null;
                    }
                } else {
                    // No CSP available
                    if (product.getStatus() != ServiceStatus.ACTIVE
                            && !(product.getStatus() == ServiceStatus.SUSPENDED
                                    && returnSuspended)) {
                        return null;
                    }
                }
            }
            // Check if the current user has already subscribed to a product
            // which is based on the same technical service as the current
            // product is based on.
            subscriptionLimitReached = isSubscriptionLimitReached(product);
        } else {
            // No user is logged in => anonymous access

            ArgumentValidator.notNull("locale", locale);
            localizerLocale = locale;

            // Check if it's a customer specific product
            if (product.getType() == ServiceType.CUSTOMER_TEMPLATE) {
                // anonymous access to customer specific product is not allowed
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "Anonymous access to customer specific product not allowed.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        onp,
                        LogMessageIdentifier.WARN_ANONYMOUS_ACCESS_NOT_ALLOWED,
                        Long.toString(product.getKey()));
                throw onp;
            }

            if (product.getStatus() != ServiceStatus.ACTIVE) {
                // Only active product
                return null;
            }

            CatalogEntry catalogEntry = getCatalogEntryForMarketplace(product,
                    marketplaceId);
            if (catalogEntry == null) {
                // No catalog entry for the service (not published yet)
                return null;
            }

            if (!catalogEntry.isAnonymousVisible()) {
                // Not in the public catalog
                return null;
            }
            if (catalogEntry.getMarketplace() == null || !marketplaceId
                    .equals(catalogEntry.getMarketplace().getMarketplaceId())) {
                // Not published to any/the correct marketplace
                return null;
            }
        }

        LocalizerFacade facade = new LocalizerFacade(localizer,
                localizerLocale);
        VOServiceEntry result = ProductAssembler.toVOServiceEntry(product,
                facade, subscriptionLimitReached);

        return result;
    }

    Product getCopyForCustomer(Product template, Organization customer) {
        Query query = dm.createNamedQuery("Product.getCopyForCustomer");
        query.setParameter("template", template);
        query.setParameter("customer", customer);
        List<Product> resultList = ParameterizedTypes
                .list(query.getResultList(), Product.class);
        if (resultList.size() <= 0) {
            return null;
        }
        return resultList.get(0);
    }

    /**
     * Checks if the passed product is a copy which was created for a specific
     * subscription.
     */
    void verifyNoSubscriptionCopy(Product product)
            throws OperationNotPermittedException {
        if (product.getTemplate() == null) {
            return;
        }
        if (ServiceType.isSubscription(product.getType())) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "Access to product copy not allowed.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_ACCESS_PRODUCT_COPY_NOT_ALLOWED,
                    Long.toString(product.getKey()));
            throw onp;
        }
    }

    /**
     * Returns the catalog entry for the marketplace identified by the passed
     * marketplace id. Returns <code>null</code> if no entry exists.
     */
    private CatalogEntry getCatalogEntryForMarketplace(Product product,
            String marketplaceId) {
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        List<CatalogEntry> catalogEntries = product.getCatalogEntries();
        for (CatalogEntry catalogEntry : catalogEntries) {
            Marketplace mpl = catalogEntry.getMarketplace();
            if (mpl != null && mpl.getMarketplaceId().equals(marketplaceId)) {
                return catalogEntry;
            }
        }
        return null;
    }

    /**
     * If a catalog entry for the passed marketplace exists the function returns
     * <code>true</code> otherwise <code>false</code>.
     */
    boolean existsCatalogEntryForMarketplace(Product product,
            String marketplaceId) {
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        return getCatalogEntryForMarketplace(product, marketplaceId) != null
                ? true : false;
    }

    /**
     * The result list may contain marketing products that are available for all
     * organizations, as well as some that are particularly dedicated to the
     * current customer and have been created as copy of those original ones.
     * Remove those from the list also remove inactive products
     * 
     * @param products
     *            the list which should be filtered
     * @param marketplaceId
     *            the marketplace identifier
     */
    List<Product> filterProducts(List<Product> products, String marketplaceId) {
        Set<Long> prodKeysToBeRemoved = new HashSet<>();
        Map<Long, Product> prodKeysToBeReplaced = new HashMap<>();
        PlatformUser currentUser = dm.getCurrentUserIfPresent();

        boolean currentOrgIsMpOwner = isOrganizationMarketplaceOwner(
                currentUser, marketplaceId);
        for (Product product : products) {
            Product template = product.getTemplate();
            boolean replaceTemplate = false;
            if (product.getStatus() != ServiceStatus.ACTIVE
                    && !(currentOrgIsMpOwner && product
                            .getStatus() == ServiceStatus.SUSPENDED)) {
                // remove the customer specific product if not visible to the
                // customer
                prodKeysToBeRemoved.add(product.getKey());
                if (template != null) {
                    // also remove the template if the customer specific is not
                    // active
                    prodKeysToBeRemoved.add(template.getKey());
                }
            } else if (template != null
                    && product.getType() == ServiceType.CUSTOMER_TEMPLATE) {
                // if the customer specific one wasn't removed but it has a
                // template, replace the template by the specific copy
                prodKeysToBeReplaced.put(template.getKey(),
                        product);
                // the original appearance of the copy has to be removed
                // since the copy will re-appear at its template's position
                prodKeysToBeRemoved.add(product.getKey());
                replaceTemplate = true;
            }
            if (template != null && !replaceTemplate
                    && template.getStatus() != ServiceStatus.ACTIVE
                    && !(currentOrgIsMpOwner && template
                            .getStatus() == ServiceStatus.SUSPENDED)) {
                // if the template isn't visible to the customer and won't be
                // replaced, remove it
                prodKeysToBeRemoved.add(template.getKey());
            }
            if (isSubscriptionLimitReached(product)) {
                prodKeysToBeRemoved.add(product.getKey());
            }
        }

        cleanUpProducts(products, prodKeysToBeRemoved, prodKeysToBeReplaced);
        return products;
    }

    boolean isOrganizationMarketplaceOwner(PlatformUser currentUser,
            String marketplaceId) {
        if (currentUser != null && currentUser.isOrganizationAdmin()) {
            List<Marketplace> ownedMarketplaces = currentUser.getOrganization()
                    .getMarketplaces();
            for (Marketplace marketplace : ownedMarketplaces) {
                if (marketplace.getMarketplaceId().equals(marketplaceId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void cleanUpProducts(List<Product> products,
            Set<Long> prodKeysToBeRemoved,
            Map<Long, Product> prodKeysToBeReplaced) {
        ListIterator<Product> productsIterator = products.listIterator();
        while (productsIterator.hasNext()) {
            Long productKey = productsIterator.next().getKey();
            if (prodKeysToBeRemoved.contains(productKey)
                    && !prodKeysToBeReplaced.containsKey(productKey)) {
                // remove obsolete products (only if they won't be replaced)
                productsIterator.remove();
            } else {
                Product specificCopy = prodKeysToBeReplaced.get(productKey);
                if (specificCopy != null) {
                    // replace templates by specific copies
                    productsIterator.set(specificCopy);
                }
            }
        }
    }

    @Override
    public List<VOService> getRelatedServicesForMarketplace(VOService service,
            String marketplaceId, String locale)
            throws ObjectNotFoundException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

        List<VOService> voList = new ArrayList<>();

        Product prod = dm.getReference(Product.class, service.getKey());
        List<Product> resultList;
        Query query;
        String localizerLocale;

        if (dm.getCurrentUserIfPresent() != null) {
            PlatformUser currentUser = dm.getCurrentUser();
            Organization currentUsersOrg = currentUser.getOrganization();
            if (locale == null) {
                localizerLocale = currentUser.getLocale();
            } else {
                localizerLocale = locale;
            }

            query = dm.createNamedQuery(
                    "Product.getRelatedProductsForMarketplace");
            query.setParameter("customer", currentUsersOrg);
            query.setParameter("marketplaceId", marketplaceId);
            query.setParameter("technicalProduct", prod.getTechnicalProduct());
            query.setParameter("vendor", prod.getVendor());
        } else {
            ArgumentValidator.notNull("locale", locale);
            localizerLocale = locale;

            query = dm.createNamedQuery(
                    "Product.getRelatedPublicProductsForMarketplace");
            query.setParameter("marketplaceId", marketplaceId);
            query.setParameter("technicalProduct", prod.getTechnicalProduct());
            query.setParameter("vendor", prod.getVendor());
        }
        // Remove services which should be not visible to the current user
        resultList = filterProducts(
                ParameterizedTypes.list(query.getResultList(), Product.class),
                marketplaceId);

        // Finally remove the passed service
        resultList.remove(prod);

        // Build the VO list
        LocalizerFacade facade = new LocalizerFacade(localizer,
                localizerLocale);
        for (Product product : resultList) {
            voList.add(ProductAssembler.toVOProduct(product, facade));
        }

        return voList;
    }

    boolean hasOneSubscription(Product product) {
        Query query = dm
                .createNamedQuery("Subscription.numberOfVisibleSubscriptions");
        query.setParameter("productKey",
            product.getTechnicalProduct().getKey());
        query.setParameter("orgKey",
            dm.getCurrentUser().getOrganization().getKey());
        long result = ((Long) query.getSingleResult()).longValue();
        return result > 0;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean isSubscriptionLimitReached(Product product) {
        if (dm.getCurrentUserIfPresent() != null && product
                .getTechnicalProduct().isOnlyOneSubscriptionAllowed()) {
            return hasOneSubscription(product);
        }
        return false;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOService> getSuppliedServices() {
        return getSuppliedServices(PerformanceHint.ALL_FIELDS);
    }

    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOService> getSuppliedServices(
            PerformanceHint performanceHint) {

        Organization currentUsersOrg = dm.getCurrentUser().getOrganization();
        EnumSet<ServiceType> serviceTypes = getServiceTypesForOrg(
                currentUsersOrg);
        List<Product> productList = getProductsOfSupplier(currentUsersOrg,
                serviceTypes);
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        ProductAssembler.prefetchData(productList, facade, performanceHint);
        List<VOService> voList = new ArrayList<>();
        for (Product product : productList) {
            voList.add(ProductAssembler.toVOProduct(product, facade,
                    performanceHint));
        }

        return voList;
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

    /**
     * Retrieves the marketing products a supplier has defined so far. Obsolete
     * and deleted products are removed from the list.
     * 
     * @param supplier
     *            The supplier organization for which the products have to be
     *            retrieved.
     * @return A list of subscribable products.
     */
    private List<Product> getProductsOfSupplier(Organization supplier,
            EnumSet<ServiceType> serviceTypes) {
        // the used query only returns those products which are not a copy of
        // another product
        Query query = null;
        query = dm.createNamedQuery("Product.getProductTemplatesForVendor");
        query.setParameter("vendorKey", supplier.getKey());
        query.setParameter("productTypes", serviceTypes);
        query.setParameter("filterOutWithStatus",
                EnumSet.of(ServiceStatus.OBSOLETE, ServiceStatus.DELETED));
        @SuppressWarnings("unchecked")
        List<Product> result = query.getResultList();
        return result;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public String importTechnicalServices(byte[] xml) throws ImportException,
            OperationNotPermittedException, TechnicalServiceActiveException,
            UpdateConstraintException, TechnicalServiceMultiSubscriptions,
            UnchangeableAllowingOnBehalfActingException,
            BillingAdapterNotFoundException {

        ArgumentValidator.notNull("xml", xml);

        TechnicalProductImportParser parser = new TechnicalProductImportParser(
                dm, localizer, dm.getCurrentUser().getOrganization(), pm,
                tenantProvisioning, tagService, ms, configurationService,
                configurationService.isKafkaEnabled());
        try {
            parser.parse(xml);
        } catch (TechnicalServiceActiveException | UpdateConstraintException
                | TechnicalServiceMultiSubscriptions
                | UnchangeableAllowingOnBehalfActingException
                | BillingAdapterNotFoundException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        if (!parser.isXmlValid()) {
            ImportException e = new ImportException(parser.getMessage());
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return parser.getMessage();
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public VOService activateService(VOService service)
            throws ServiceStateException, ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException,
            ConcurrentModificationException {

        // Activate service
        ServiceVisibilityCheck visChecker = new ServiceVisibilityCheck(dm);
        VOService voProduct = setActivationState(service, true, null,
                visChecker);

        // Check constraint about visibility
        try {
            visChecker.validate();
        } catch (ServiceOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return voProduct;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void activateServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceOperationException,
            TechnicalServiceNotAliveException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceNotPublishedException, ConcurrentModificationException {

        TriggerProcessParameter tpParam = tp
                .getParamValueForName(TriggerProcessParameterName.PRODUCT);
        VOService product = tpParam.getValue(VOService.class);

        // obtain the user that should be used for authority checks
        PlatformUser user = tp.getUser();

        Product prod = validateForProductActivation(product);
        setStatus(prod, product, ServiceStatus.ACTIVE, ServiceStatus.INACTIVE,
                user);

        // Update visibility (if catalog entries are given)
        TriggerProcessParameter tpCatEntries = tp.getParamValueForName(
                TriggerProcessParameterName.CATALOG_ENTRIES);
        if (tpCatEntries != null) {
            List<VOCatalogEntry> entries = ParameterizedTypes.list(
                    tpCatEntries.getValue(List.class), VOCatalogEntry.class);
            if (entries != null && !entries.isEmpty()) {
                // Set visibility states of entries
                updateCatalogEntryVisibility(prod, entries);
            }
        }

        triggerQS.sendAllNonSuspendingMessages(TriggerMessage.create(
                TriggerType.ACTIVATE_SERVICE, tp.getTriggerProcessParameters(),
                dm.getCurrentUser().getOrganization()));

    }

    /**
     * Validates the input parameters and availability of the technical product
     * for an activation of a product.
     * 
     * @param product
     *            The product to be activated.
     * @throws ObjectNotFoundException
     *             Thrown in case the product does not exist.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the technical product cannot be reached.
     * @throws ServiceNotPublishedException
     *             Thrown in case the service is currently not published on any
     *             marketplace.
     */
    private Product validateForProductActivation(VOService product)
            throws ObjectNotFoundException, ServiceOperationException,
            TechnicalServiceNotAliveException, ServiceNotPublishedException {

        Product prod = dm.getReference(Product.class, product.getKey());
        PriceModel priceModel;
        if (prod.getVendor().getGrantedRoleTypes()
                .contains(OrganizationRoleType.SUPPLIER)) {
            priceModel = prod.getPriceModel();
        } else {
            priceModel = prod.getTemplate().getPriceModel();
        }
        if (priceModel == null) {
            ServiceOperationException sof = new ServiceOperationException(
                    ServiceOperationException.Reason.MISSING_PRICE_MODEL);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, sof,
                    LogMessageIdentifier.WARN_PRODUCT_AVTIVATION_FAILED_MISSING_PRICE_MODEL,
                    Long.toString(prod.getKey()));
            throw sof;
        }
        if (prod.getCatalogEntries() != null) {
            for (CatalogEntry ce : prod.getCatalogEntries()) {
                if (ce.getMarketplace() == null) {

                    Object[] params = new Object[] { prod.getProductId() };
                    ServiceNotPublishedException snp = new ServiceNotPublishedException(
                            params);
                    logger.logWarn(
                            Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, snp,
                            LogMessageIdentifier.WARN_PRODUCT_AVTIVATION_FAILED_NOT_PUBLISHED_MARKETPLACE,
                            Long.toString(prod.getKey()));
                    throw snp;
                }
            }
        }
        try {
            appManager.validateCommunication(prod.getTechnicalProduct());
        } catch (TechnicalServiceNotAliveException e) {
            TechnicalServiceNotAliveException ex = new TechnicalServiceNotAliveException(
                    TechnicalServiceNotAliveException.Reason.SUPPLIER,
                    new Object[] { prod.getTechnicalProduct()
                            .getTechnicalProductId() },
                    e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_TECH_SERVICE_NOT_AVAILABLE,
                    prod.getTechnicalProduct().getTechnicalProductId());
            throw ex;
        }

        return prod;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public VOService deactivateService(VOService service)
            throws ServiceStateException, ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, OperationPendingException,
            ConcurrentModificationException {

        ServiceVisibilityCheck visChecker = new ServiceVisibilityCheck(dm);
        VOService voProduct = null;
        try {
            // Deactivate service
            voProduct = setActivationState(service, false, null, visChecker);

            // Check constraint about visibility
            try {
                visChecker.validate();
            } catch (ServiceOperationException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }

        } catch (TechnicalServiceNotAliveException e) {
            // Can't occur during deactivate
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_DEACTIVATE_SERVICE);
            throw sse;
        } catch (ServiceNotPublishedException e) {
            // Can't occur during deactivate
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_DEACTIVATE_SERVICE);
            throw sse;
        } catch (ConcurrentModificationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return voProduct;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deactivateServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, ConcurrentModificationException {

        VOService product = tp
                .getParamValueForName(TriggerProcessParameterName.PRODUCT)
                .getValue(VOService.class);
        Product prod = dm.getReference(Product.class, product.getKey());
        setStatus(prod, product, ServiceStatus.INACTIVE, ServiceStatus.ACTIVE,
                tp.getUser());

        // Update visibility (if catalog entries are given)
        TriggerProcessParameter tpCatEntries = tp.getParamValueForName(
                TriggerProcessParameterName.CATALOG_ENTRIES);
        if (tpCatEntries != null) {
            List<VOCatalogEntry> entries = ParameterizedTypes.list(
                    tpCatEntries.getValue(List.class), VOCatalogEntry.class);
            if (entries != null && !entries.isEmpty()) {
                // Set visibility states of entries
                updateCatalogEntryVisibility(prod, entries);
            }
        }

        triggerQS.sendAllNonSuspendingMessages(
                TriggerMessage.create(TriggerType.DEACTIVATE_SERVICE,
                        tp.getTriggerProcessParameters(),
                        dm.getCurrentUser().getOrganization()));

    }

    /**
     * Sets the product state to the given product and all of its subscribable
     * copies. Before that the product is checked for a valid state and if it
     * has not been changed in the meantime.
     * 
     * @param prod
     *            the product to set the state for read from the database
     * @param product
     *            the voproduct passed in
     * @param newStatus
     *            the new state to set
     * @param requiredState
     *            the state in which the product has to be or <code>null</code>
     * @param user
     *            The user on behalf of which the current operation is executed.
     * @throws ServiceStateException
     *             in case the product isn't in the required state
     * @throws OperationNotPermittedException
     *             in case the current organization is not the owner of the
     *             product
     */
    private void setStatus(Product prod, VOService product,
            ServiceStatus newStatus, ServiceStatus requiredState,
            PlatformUser user) throws ServiceStateException,
            OperationNotPermittedException, ServiceOperationException {
        validateForProductStatusChange(prod, product, newStatus, requiredState,
                user);
        if (prod.getStatus() != newStatus) {
            prod.setStatus(newStatus);
        }
    }

    /**
     * Validates if the current caller is permitted to change the state of the
     * product.
     * 
     * @param prod
     *            The product to change the status of.
     * @param product
     *            The value object for the product to be changed.
     * @param newStatus
     *            The status to be set.
     * @param requiredState
     *            The required status of the product.
     * @param user
     *            The user that performs the status change.
     * @throws OperationNotPermittedException
     *             Thrown in case the caller tries to modify the object of
     *             another organization.
     * @throws ServiceStateException
     *             Thrown in case the product state change fails as the current
     *             product state does not allow the intended modification.
     */
    private void validateForProductStatusChange(Product prod, VOService product,
            ServiceStatus newStatus, ServiceStatus requiredState,
            PlatformUser user) throws OperationNotPermittedException,
            ServiceOperationException, ServiceStateException {
        Organization organization = user.getOrganization();

        // ensure the product belongs to the supplier
        PermissionCheck.owns(prod, organization, logger, sessionCtx);
        if (prod.getOwningSubscription() != null) {
            ServiceOperationException pof = new ServiceOperationException(
                    Reason.STATE_CHANGE_FAILED_USED_BY_SUB);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, pof,
                    LogMessageIdentifier.WARN_SUPPLIER_CHANGE_SUBSCRIPTION_STATE_FAILED,
                    Long.toString(organization.getKey()),
                    Long.toString(product.getKey()));
            throw pof;
        }
        if (prod.getStatus() != newStatus) {
            if (requiredState != null && prod.getStatus() != requiredState) {
                throw new ServiceStateException(requiredState,
                        prod.getStatus());
            }
        }
    }

    private List<TechnicalProduct> getTechnicalProductsInt(
            Organization organization,
            OrganizationRoleType organizationRoleType) {
        List<TechnicalProduct> tProds = new ArrayList<>();
        if (organizationRoleType == OrganizationRoleType.SUPPLIER) {
            // retrieve the technical products of all referenced technology
            // providers
            List<Organization> providers = organization
                    .getTechnologyProviders();
            for (Organization provider : providers) {
                tProds.addAll(provider.getTechnicalProducts());
            }
        } else if (organizationRoleType == OrganizationRoleType.TECHNOLOGY_PROVIDER) {
            // retrieve the technical products of the current user's
            // organization
            tProds.addAll(organization.getTechnicalProducts());
        }
        return tProds;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "TECHNOLOGY_MANAGER" })
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType role) throws OrganizationAuthoritiesException {
        return getTechnicalServices(role, PerformanceHint.ALL_FIELDS);
    }

    @RolesAllowed({ "SERVICE_MANAGER", "TECHNOLOGY_MANAGER" })
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType role, PerformanceHint performanceHint) {

        ArgumentValidator.notNull("role", role);

        List<VOTechnicalService> result = new ArrayList<>();
        PlatformUser currentUser = dm.getCurrentUser();

        Organization currentUsersOrg = currentUser.getOrganization();

        boolean excludeNonConfigurableParamDefs = false;
        // retrieve technical products
        List<TechnicalProduct> tProds = new ArrayList<>();
        if (role == OrganizationRoleType.SUPPLIER) {
            // retrieve the technical products of all referenced technology
            // providers
            tProds.addAll(ms.getTechnicalServicesForSupplier(currentUsersOrg));
            // a supplier must not see the non-configurable parameter
            // definitions
            excludeNonConfigurableParamDefs = true;
        } else if (role == OrganizationRoleType.TECHNOLOGY_PROVIDER) {
            // retrieve the technical products of the current user's
            // organization
            tProds.addAll(currentUsersOrg.getTechnicalProducts());
        }

        // convert the objects to value objects
        if (performanceHint == PerformanceHint.ONLY_IDENTIFYING_FIELDS) {
            for (TechnicalProduct tProd : tProds) {
                result.add(TechnicalProductAssembler.toVOTechnicalProduct(tProd,
                        null, null, null, false,
                        PerformanceHint.ONLY_IDENTIFYING_FIELDS));
            }
        } else {
            LocalizerFacade facade = new LocalizerFacade(localizer,
                    currentUser.getLocale());
            for (TechnicalProduct tProd : tProds) {
                List<ParameterDefinition> paramDefs = getPlatformParameterDefinitions(
                        tProd);
                List<Event> platformEvents = getPlatformEvents(tProd);
                result.add(TechnicalProductAssembler.toVOTechnicalProduct(tProd,
                        paramDefs, platformEvents, facade,
                        excludeNonConfigurableParamDefs));
            }
        }

        return result;
    }

    @Override
    public void validateTechnicalServiceCommunication(
            VOTechnicalService technicalService) throws ObjectNotFoundException,
            OperationNotPermittedException, TechnicalServiceNotAliveException {

        ArgumentValidator.notNull("technicalService", technicalService);

        PlatformUser currentUser = dm.getCurrentUser();
        Organization currentUserOrg = currentUser.getOrganization();

        // load the technical product
        TechnicalProduct techProduct = dm.getReference(TechnicalProduct.class,
                technicalService.getKey());

        // check that the current organization is allowed to access the
        // technical product
        boolean accessable = false;
        if (currentUserOrg.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
            accessable = getTechnicalProductsInt(currentUserOrg,
                    OrganizationRoleType.TECHNOLOGY_PROVIDER)
                            .contains(techProduct);
        }
        if (!accessable
                && currentUserOrg.hasRole(OrganizationRoleType.SUPPLIER)) {
            accessable = getTechnicalProductsInt(currentUserOrg,
                    OrganizationRoleType.SUPPLIER).contains(techProduct);
        }
        if (!accessable) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "User is not permitted to access the technical product '"
                            + technicalService.getKey() + "'.");
            logger.logError(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.ERROR_USER_ACCESS_TECHNICAL_PRODUCT_NOT_PERMITTED,
                    Long.toString(technicalService.getKey()));
            throw onp;
        }

        appManager.validateCommunication(techProduct);

    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void deleteTechnicalService(VOTechnicalService technicalService)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            DeletionConstraintException, ConcurrentModificationException {

        ArgumentValidator.notNull("technicalService", technicalService);

        PlatformUser currentUser = dm.getCurrentUser();
        Organization currentUserOrg = currentUser.getOrganization();
        if (!currentUserOrg.hasRole(OrganizationRoleType.TECHNOLOGY_PROVIDER)) {
            OrganizationAuthoritiesException ioa = new OrganizationAuthoritiesException(
                    "Delete technical product +'" + technicalService.getKey()
                            + "' failed.",
                    new Object[] { OrganizationRoleType.TECHNOLOGY_PROVIDER });
            logger.logError(Log4jLogger.SYSTEM_LOG, ioa,
                    LogMessageIdentifier.ERROR_DELETE_TECHNICAL_PRODUCT_FAILED,
                    Long.toString(technicalService.getKey()));
            throw ioa;
        }

        final TechnicalProduct technicalProduct = dm.getReference(
                TechnicalProduct.class, technicalService.getKey());

        verifyTechnicalServiceIsUpToDate(technicalService, technicalProduct,
                false);

        TechnicalProductCleaner cleaner = new TechnicalProductCleaner(dm,
                tenantProvisioning);
        cleaner.cleanupTechnicalProduct(technicalProduct);

        ms.removeMarketingPermissions(technicalProduct);
        dm.remove(technicalProduct);
        tagService.deleteOrphanedTags();
    }

    /**
     * Verifies that the technical service is up to date - the versions of it
     * and all depending objects match the ones stored on server side.
     * 
     * @param technicalService
     *            The value object.
     * @param technicalProduct
     *            The domain object.
     * @param ignoreNonConfigurableParameters
     *            Flag indicating whether to consider non-configurable
     *            parameters or not.
     * @throws ConcurrentModificationException
     */
    void verifyTechnicalServiceIsUpToDate(VOTechnicalService technicalService,
            final TechnicalProduct technicalProduct,
            boolean ignoreNonConfigurableParameters)
            throws ConcurrentModificationException {
        BaseAssembler.verifyVersionAndKey(technicalProduct, technicalService);

        List<Event> events = technicalProduct.getEvents();
        List<VOEventDefinition> eventDefinitions = technicalService
                .getEventDefinitions();
        verifyListConsistency(eventDefinitions, events);

        List<ParameterDefinition> parameterDefinitions = new ArrayList<>(
                technicalProduct.getParameterDefinitions());
        // filter out the non-configurable parameter definitions, if required
        Iterator<ParameterDefinition> iterator = parameterDefinitions
                .iterator();
        while (iterator.hasNext()) {
            ParameterDefinition currentParamDef = iterator.next();
            if (!currentParamDef.isConfigurable()
                    && ignoreNonConfigurableParameters) {
                iterator.remove();
            }
        }
        List<VOParameterDefinition> voParamDefs = technicalService
                .getParameterDefinitions();
        verifyListConsistency(voParamDefs, parameterDefinitions);

        List<RoleDefinition> roleDefinitions = technicalProduct
                .getRoleDefinitions();
        List<VORoleDefinition> voRoleDefinitions = technicalService
                .getRoleDefinitions();
        verifyListConsistency(voRoleDefinitions, roleDefinitions);

        List<TechnicalProductOperation> technicalProductOperations = technicalProduct
                .getTechnicalProductOperations();
        List<VOTechnicalServiceOperation> technicalServiceOperations = technicalService
                .getTechnicalServiceOperations();
        verifyListConsistency(technicalServiceOperations,
                technicalProductOperations);
    }

    /**
     * Verifies the list of value objects against the list of domain objects. If
     * the value object element list contains an entry that has a version clash
     * with the corresponding domain object or in case one of the domain objects
     * is not considered as value object (so it might have been added on server
     * side later), a ConcurrentModificationException is thrown.
     * 
     * @param voList
     *            The value object element list to check.
     * @param doList
     *            The domain object element list to compare against.
     * @throws ConcurrentModificationException
     */
    private void verifyListConsistency(List<? extends BaseVO> voList,
            List<? extends DomainObject<?>> doList)
            throws ConcurrentModificationException {
        Map<Long, DomainObject<?>> keyToDomainObject = new HashMap<>();
        for (DomainObject<?> domainObject : doList) {
            keyToDomainObject.put(domainObject.getKey(),
                    domainObject);
        }
        for (BaseVO valueObject : voList) {
            DomainObject<?> correspondingDomainObject = keyToDomainObject
                    .remove(valueObject.getKey());
            if (correspondingDomainObject != null) {
                BaseAssembler.verifyVersionAndKey(correspondingDomainObject,
                        valueObject);
            }
        }
        if (!keyToDomainObject.isEmpty()) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    String.format(
                            "Technical service has changed, object '%s' was not contained in input, but potentially added in meantime.",
                            keyToDomainObject.values().iterator().next()));
            DomainObject<?> object = keyToDomainObject.values().iterator()
                    .next();
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_OBJECT_CREATED_CONCURRENTLY,
                    (object == null ? "" : object.getClass().getSimpleName()));
            throw cme;
        }
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails createService(VOTechnicalService technicalService,
            VOService service, VOImageResource voImageResource)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            NonUniqueBusinessKeyException, ConcurrentModificationException {

        ArgumentValidator.notNull("technicalService", technicalService);
        ArgumentValidator.notNull("service", service);

        Product product = null;

        TechnicalProduct technicalProduct = dm.getReference(
                TechnicalProduct.class, technicalService.getKey());
        verifyTechnicalServiceIsUpToDate(technicalService, technicalProduct,
                true);
        try {
            product = prepareMarketingProduct(technicalService.getKey(),
                    service, true);
        } catch (ServiceStateException | ConcurrentModificationException e) {
            // this must not happen
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_CREATE_SERVICE);
            throw sse;
        }

        processImage(product.getKey(), voImageResource);

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());

        VOServiceDetails createdProduct = getServiceDetails(product, facade);

        serviceAudit.defineService(dm, product,
                technicalService.getTechnicalServiceId(),
                service.getShortDescription(), service.getDescription(),
                dm.getCurrentUser().getLocale());

        return createdProduct;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails updateService(VOServiceDetails service,
            VOImageResource imageResource) throws ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException, NonUniqueBusinessKeyException,
            ServiceStateException, ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);

        Product storedService = dm.getReference(Product.class,
                service.getKey());
        List<Product> customerProducts = getCustomerSpecificCopyProducts(
                storedService);
        validateProductStatus(customerProducts);

        Product product = prepareMarketingProduct(
                storedService.getTechnicalProduct().getKey(), service, false);

        processImage(product.getKey(), imageResource);

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        VOServiceDetails createdProduct = getServiceDetails(product, facade);

        if (service.getVersion() < createdProduct.getVersion()
                || isLocalizedTextChanged) {
            serviceAudit.updateService(dm, storedService,
                    isShortDescriptionChanged, isDescriptionChanged,
                    isCustomTabNameChanged, dm.getCurrentUser().getLocale());
        }

        updateCustomerSpecificService(product, customerProducts,
                createdProduct);

        return createdProduct;
    }

    void validateProductStatus(List<Product> customerProducts)
            throws ServiceStateException {
        for (Product prod : customerProducts) {
            ProductValidator.validateInactiveOrSuspended(
                    ProductAssembler.getProductId(prod), prod.getStatus());
        }
    }

    void updateCustomerSpecificService(Product templateProduct,
            List<Product> customerProducts, VOServiceDetails createdProduct)
            throws ValidationException {
        for (Product prod : customerProducts) {
            Product product = ProductAssembler
                    .updateCustomerTemplateProduct(prod, createdProduct);
            updateParametersForCustomerTemplate(templateProduct, product);
            dm.flush();
        }
    }

    void updateParametersForCustomerTemplate(Product templateProduct,
            Product customerTemplateProduct) {
        ParameterSet customerTemplateParameterSet = customerTemplateProduct
                .getParameterSet();
        ParameterSet templateParameterSet = templateProduct.getParameterSet();
        Map<Long, Parameter> obsoleteParameters = new HashMap<>();
        if (customerTemplateParameterSet != null) {
            for (final Parameter p : customerTemplateParameterSet
                    .getParameters()) {
                obsoleteParameters.put(
                    p.getParameterDefinition().getKey(), p);
            }
        }
        if (customerTemplateParameterSet == null) {
            customerTemplateProduct.setParameterSet(new ParameterSet());
            customerTemplateParameterSet = customerTemplateProduct
                    .getParameterSet();
        }
        if (templateParameterSet == null) {
            templateParameterSet = new ParameterSet();
        }

        List<Parameter> customerTemplateParameters = customerTemplateParameterSet
                .getParameters();
        for (Parameter param : templateParameterSet.getParameters()) {
            final Parameter oldParam = obsoleteParameters.remove(
                param.getParameterDefinition().getKey());
            if (oldParam == null) {
                // add new param
                customerTemplateParameters
                        .add(param.copy(customerTemplateParameterSet));
            } else {
                // update param
                if (isDifferentFromExistingValue(param, oldParam)) {
                    oldParam.setConfigurable(param.isConfigurable());
                    oldParam.setValue(param.getValue());
                }
                if (!oldParam.isConfigurable()) {
                    removePricedParameters(oldParam);
                }
            }
        }
        // remove obsolete params
        for (Parameter obsoleteParameter : obsoleteParameters.values()) {
            customerTemplateParameters.remove(obsoleteParameter);
            removePricedParameters(obsoleteParameter);
            dm.remove(obsoleteParameter);
        }
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VOServiceDetails getServiceDetails(Product product,
            LocalizerFacade facade) {
        TechnicalProduct tp = product.getTechnicalProduct();
        List<ParameterDefinition> platformParameters = getPlatformParameterDefinitions(
                tp);
        List<Event> platformEvents = getPlatformEvents(tp);
        VOServiceDetails createdProduct = ProductAssembler.toVOProductDetails(
                product, platformParameters, platformEvents,
                isImageDefined(product), facade);

        return createdProduct;
    }

    /**
     * Modifies or creates a marketing product according to the given
     * parameters. The modifications will be stored directly.
     * 
     * @param technicalProductKey
     *            The key of the technical product the marketing product is
     *            based on.
     * @param productToModify
     *            The value object representation of the object as is should
     *            finally look like
     * @param isCreation
     *            Indicates whether the product has to be created from scratch
     *            or if it should only be updated.
     * @return The modified product.
     * @throws ObjectNotFoundException
     *             Thrown in case the technical product or the marketing product
     *             (in case of an update) cannot be found.
     * @throws OperationNotPermittedException
     *             Thrown in case an attempt is made to use parameters defined
     *             for another technical product or in case the specified
     *             technical product is not related to the marketing product.
     * @throws ValidationException
     *             Thrown in case the product identifier is not valid.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case a product with the same identifier does
     *             already exist.
     * @throws ServiceStateException
     *             Thrown in case the product is not in the state
     *             {@link ServiceStatus#INACTIVE}
     * @throws ConcurrentModificationException
     * @throws DeletionConstraintException
     */
    private Product prepareMarketingProduct(long technicalProductKey,
            VOService productToModify, boolean isCreation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, NonUniqueBusinessKeyException,
            ServiceStateException, ConcurrentModificationException {

        // 1. ensure that caller has required authority
        PlatformUser currentUser = dm.getCurrentUser();
        Organization currentUserOrg = currentUser.getOrganization();

        // 2. ensure user as permitted to create a marketing product based on
        // the specified technical product
        TechnicalProduct tProd = dm.getReference(TechnicalProduct.class,
                technicalProductKey);
        PermissionCheck.hasMarketingPermission(tProd, currentUserOrg, dm,
                logger);

        // 3. Now create the marketing product without the parameter set and
        // without price model
        Product product = null;
        String oldProductId = null;
        if (isCreation) {
            product = ProductAssembler.toNewTemplateProduct(productToModify,
                    tProd, currentUserOrg);
        } else {
            product = dm.getReference(Product.class, productToModify.getKey());
            ProductValidator.validateInactiveOrSuspended(
                    ProductAssembler.getProductId(product),
                    product.getStatus());
            oldProductId = product.getProductId();
            if (!oldProductId.equals(productToModify.getServiceId())) {
                validateChangedId(productToModify.getServiceId(),
                        currentUserOrg);
            }
            product = ProductAssembler.updateProduct(product, productToModify);
        }

        // handle the parameter settings
        List<Parameter> parametersToLog = modifyParameters(productToModify,
                currentUser, tProd, product, isCreation);

        // now store the product. due to the cascade definitions, the parameters
        // will be stored as well.
        if (isCreation) {
            dm.persist(product);
            CatalogEntry catalogEntry = QueryBasedObjectFactory
                    .createCatalogEntry(product, null);
            copyOperatorPriceModel(catalogEntry,
                    currentUserOrg.getOperatorPriceModel());
            dm.persist(catalogEntry);
            copyDefaultPaymentEnablement(product, currentUserOrg);
        }
        for (Parameter param : parametersToLog) {
            logUpdateServiceParameters(dm, product, param);
        }
        dm.flush();
        // store localized information, currently only for user's locale
        String productName = productToModify.getName();
        String productDescription = productToModify.getDescription();
        String productShortDescription = productToModify.getShortDescription();
        String productCustomTabName = productToModify.getCustomTabName();
        String userLocale = currentUser.getLocale();
        String oldDescription = localizer.getLocalizedTextFromDatabase(
                userLocale, product.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
        String oldShortDescription = localizer.getLocalizedTextFromDatabase(
                userLocale, product.getKey(),
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
        String oldProductName = localizer.getLocalizedTextFromDatabase(
                userLocale, product.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        String oldCustomTabName = localizer.getLocalizedTextFromDatabase(
                userLocale, product.getKey(),
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);

        isLocalizedTextChanged = false;
        if (productName != null && !productName.equals(oldProductName)) {
            isLocalizedTextChanged = true;
            BLValidator.isName(ProductAssembler.FIELD_NAME_NAME, productName,
                    false);
            localizer.storeLocalizedResource(userLocale, product.getKey(),
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME, productName);
        }

        isDescriptionChanged = false;
        if (productDescription != null
                && !productDescription.equals(oldDescription)) {
            isLocalizedTextChanged = true;
            isDescriptionChanged = true;
            localizer.storeLocalizedResource(userLocale, product.getKey(),
                    LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                    productDescription);
        }

        isShortDescriptionChanged = false;
        if (productShortDescription != null
                && !productShortDescription.equals(oldShortDescription)) {
            isLocalizedTextChanged = true;
            isShortDescriptionChanged = true;
            localizer.storeLocalizedResource(userLocale, product.getKey(),
                    LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                    productShortDescription);
        }

        isCustomTabNameChanged = false;
        if (productCustomTabName != null
                && !productCustomTabName.equals(oldCustomTabName)) {
            isLocalizedTextChanged = true;
            isCustomTabNameChanged = true;
            localizer.storeLocalizedResource(userLocale, product.getKey(),
                    LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME,
                    productCustomTabName);
        }

        if (isCreation) {
            // copy the technical product description to marketing description
            // in all locales except the users one
            List<VOLocalizedText> techDescriptions = localizer
                    .getLocalizedValues(tProd.getKey(),
                            LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC);

            if (techDescriptions != null) {
                long key = product.getKey();
                for (VOLocalizedText desc : techDescriptions) {
                    String locale = desc.getLocale();
                    if (!userLocale.equals(locale)) {
                        localizer.storeLocalizedResource(locale, key,
                                LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                                desc.getText());
                    }
                }
            }
        }
        // before returning, refresh the product reference, as the parameters
        // must be contained
        dm.flush();
        dm.refresh(product);

        return product;
    }

    private void validateChangedId(String serviceId,
            Organization currentUserOrg) throws NonUniqueBusinessKeyException {
        Product productTmpl = new Product();
        productTmpl.setVendor(currentUserOrg);
        productTmpl.setProductId(serviceId);
        dm.validateBusinessKeyUniqueness(productTmpl);
    }

    void copyOperatorPriceModel(CatalogEntry catalogEntry,
            RevenueShareModel operatorPriceModel) {
        RevenueShareModel operatorPriceModelCopy = operatorPriceModel.copy();
        catalogEntry.setOperatorPriceModel(operatorPriceModelCopy);

        try {
            dm.persist(operatorPriceModelCopy);
        } catch (NonUniqueBusinessKeyException e) {
            // must not happen because a RevenueShareModel has no business key
            SaaSSystemException sse = new SaaSSystemException(
                    "Caught unexpected NonUniqueBusinessKeyException", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_UNEXPECTED_BK_VIOLATION);
            throw sse;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void copyDefaultPaymentEnablement(Product product,
            Organization vendor) {
        List<OrganizationRefToPaymentType> types = vendor
                .getDefaultServicePaymentTypes();
        for (OrganizationRefToPaymentType ref : types) {
            ProductToPaymentType ptpt = new ProductToPaymentType(product,
                    ref.getPaymentType());
            product.getPaymentTypes().add(ptpt);
            try {
                dm.persist(ptpt);
            } catch (NonUniqueBusinessKeyException e) {
                // must not happen as the product is a new one without
                // references to payment types
                SaaSSystemException sse = new SaaSSystemException(
                        "Caught unexpected NonUniqueBusinessKeyException", e);
                logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.ERROR_UNEXPECTED_BK_VIOLATION);
                throw sse;
            }
        }
    }

    /**
     * Adapts the parameter settings for a given product according to the
     * specified input parameters. Matching parameters will be updated, new ones
     * will be added. Those that do not exist in the input will be removed.
     * 
     * @param productToModify
     *            The value object representation of the product. It contains
     *            the values that have to be persisted.
     * @param currentUser
     *            The user that invoked the operation.
     * @param tProd
     *            The technical product the marketing product to be changed is
     *            based upon.
     * @param product
     *            The product domain object that corresponds to the product
     *            input parameter.
     * @param isCreation
     *            true if define service,false if update service
     * @throws ObjectNotFoundException
     *             Thrown in case the parameter definitions cannot be found.
     * @throws OperationNotPermittedException
     *             Thrown in case an attempt is made to store parameters that
     *             are based on another technical product's parameter
     *             definitions.
     * @throws ValidationException
     *             Thrown in case the parameter values could not match the
     *             specified datatype.
     * @throws ConcurrentModificationException
     * @throws GeneralSecurityException
     * @throws DeletionConstraintException
     */
    private List<Parameter> modifyParameters(VOService productToModify,
            PlatformUser currentUser, TechnicalProduct tProd, Product product,
            boolean isCreation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, ConcurrentModificationException {
        boolean isDirectAccess = tProd
                .getAccessType() == ServiceAccessType.DIRECT;
        List<VOParameter> parameters = productToModify.getParameters();
        ParameterSet currentParameterSet = product.getParameterSet();
        // create a temporary set of all currently existing parameters.
        // Whenever one is contained in the input, it will be removed from
        // the set. Finally all remaining ones are obsolete and must be
        // removed.
        Map<Long, Parameter> obsoleteParameters = new HashMap<>();
        List<Parameter> parametersToLog = new ArrayList<>();
        // only determine obsolete parameters if there is a parameterset
        if (currentParameterSet != null) {
            for (final Parameter p : currentParameterSet.getParameters()) {
                obsoleteParameters.put(p.getKey(), p);
            }
        }
        if (parameters != null && !parameters.isEmpty()) {
            if (currentParameterSet == null) {
                product.setParameterSet(new ParameterSet());
                currentParameterSet = product.getParameterSet();
            }
            for (VOParameter parameter : parameters) {
                if (parameter == null) {
                    continue;
                }
                ParameterDefinition paramDef = null;
                try {
                    paramDef = dm.getReference(ParameterDefinition.class,
                            parameter.getParameterDefinition().getKey());
                } catch (ObjectNotFoundException e) {
                    sessionCtx.setRollbackOnly();
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.WARN_MARKETING_PRODUCT_CREATION_FAILED);
                    throw e;
                }
                if (!paramDef.isConfigurable()) {
                    OperationNotPermittedException onp = new OperationNotPermittedException(
                            String.format(
                                    "Cannot create parameter for parameter definition '%s' as the definition is non-configurable! User was: '%s'.",
                                paramDef.getKey(),
                                currentUser.getKey()));
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                            LogMessageIdentifier.WARN_NON_CONFIGURABLE_PARAMETER_DEFINITION,
                            String.valueOf(paramDef.getKey()),
                            String.valueOf(currentUser.getKey()));
                    throw onp;
                }

                // validate the parameters
                ParameterAssembler.validateParameter(parameter, paramDef);
                // now ensure that all product related parameters are really
                // belonging to the specified technical product
                if (paramDef
                        .getParameterType() == ParameterType.SERVICE_PARAMETER
                        && paramDef.getTechnicalProduct().getKey() != tProd
                                .getKey()) {
                    sessionCtx.setRollbackOnly();
                    OperationNotPermittedException onp = new OperationNotPermittedException(
                            "Creation of marketing product failed");
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                            LogMessageIdentifier.WARN_MARKETING_PRODUCT_CREATION_FAILED_NOT_ACCESSIBLE_PRODUCT,
                            Long.toString(currentUser.getKey()));
                    throw onp;
                }
                if (paramDef.getValueType() == ParameterValueType.PWD) {
                    parameter.setValue(parameter.getValue());
                }
                // only store the parameter if its value is not null, remove
                // those that have null values and remove all stored parameters
                // that are not present in the input
                List<Parameter> storedParameters = currentParameterSet
                        .getParameters();
                if (isParameterToBeSaved(isDirectAccess, parameter, paramDef)) {
                    final Parameter existingParameter = obsoleteParameters
                            .remove(parameter.getKey());
                    if (existingParameter == null) {
                        final Parameter param = ParameterAssembler
                                .toParameter(parameter);
                        param.setParameterDefinition(paramDef);
                        param.setParameterSet(currentParameterSet);
                        storedParameters.add(param);
                        if (!isCreation || isDifferentFromDefaultValue(param)) {
                            parametersToLog.add(param);
                        }
                    } else {
                        boolean isChanged = isDifferentFromExistingValue(
                                existingParameter,
                                ParameterAssembler.toParameter(parameter));
                        ParameterAssembler.updateParameter(existingParameter,
                                parameter);
                        existingParameter.setParameterDefinition(paramDef);
                        if (isChanged) {
                            parametersToLog.add(existingParameter);
                        }
                        if (!existingParameter.isConfigurable()) {
                            // remove the priced parameters as price modeling
                            // has to be done within the base prices
                            removePricedParameters(existingParameter);
                        }
                    }
                }
            }
        }
        // finally remove all obsolete parameters and their priced parameters
        if (currentParameterSet != null) {
            for (Parameter obsoleteParameter : obsoleteParameters.values()) {
                removePricedParameters(obsoleteParameter);
                dm.remove(obsoleteParameter);
                currentParameterSet.getParameters().remove(obsoleteParameter);
                Parameter parameterToLog = obsoleteParameter
                        .copy(obsoleteParameter.getParameterSet());
                parameterToLog.setConfigurable(false);
                parameterToLog.setValue("");
                parametersToLog.add(parameterToLog);
            }
        }
        return parametersToLog;
    }

    /**
     * When defining service, if the parameter's value and userOption are equals
     * with the default, it will not be logged, otherwise it will be logged.
     */
    boolean isDifferentFromDefaultValue(Parameter parameter) {
        String defaultValue = parameter.getParameterDefinition()
                .getDefaultValue() == null ? ""
                        : parameter.getParameterDefinition().getDefaultValue();
        boolean defaultUserOption = false;
        String inputValue = parameter.getValue() == null ? ""
                : parameter.getValue();
        boolean inputUserOption = parameter.isConfigurable();
        if (defaultValue.equals(inputValue)
                && defaultUserOption == inputUserOption) {
            return false;
        }
        return true;
    }

    /**
     * When updating service, if the parameter's value and userOption are equals
     * with the existing, it will not be logged. Otherwise it will be logged.
     */
    boolean isDifferentFromExistingValue(Parameter oldPara, Parameter newPara) {
        String oldValue = oldPara.getValue() == null ? "" : oldPara.getValue();
        boolean oldUserOption = oldPara.isConfigurable();
        String newValue = newPara.getValue() == null ? "" : newPara.getValue();
        boolean newUserOption = newPara.isConfigurable();
        if (oldValue.equals(newValue) && oldUserOption == newUserOption) {
            return false;
        }
        return true;
    }

    /**
     * Log update service parameters, including inserted parameters and updated
     * parameters
     */
    private void logUpdateServiceParameters(DataService dataService,
            Product product, Parameter parameter) {
        String parameterValue = "";
        if (parameter.getParameterDefinition().getValueType()
                .equals(ParameterValueType.BOOLEAN)) {
            if (parameter.getBooleanValue()) {
                parameterValue = BOOLEANVALUEYES;
            } else {
                parameterValue = BOOLEANVALUENO;
            }

        } else if (parameter.getParameterDefinition().getValueType()
                .equals(ParameterValueType.ENUMERATION)
                && parameter.getValue() != null
                && !parameter.getValue().isEmpty()) {
            parameterValue = localizer.getLocalizedTextFromDatabase(
                    dm.getCurrentUser().getLocale(),
                    parameter.getParameterOption(parameter.getValue()).getKey(),
                    LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
        } else {
            parameterValue = parameter.getValue();
        }
        serviceAudit.updateServiceParameters(dataService, product,
                parameter.getParameterDefinition().getParameterId(),
                parameterValue, parameter.isConfigurable());
    }

    /**
     * Removes all existing {@link PricedParameter}s based on the provided
     * {@link Parameter}. Only to be used on Products that are not billing
     * relevant - so templates that have no subscription assigned
     * 
     * @param parameter
     *            the {@link Parameter} to remove the {@link PricedParameter}s
     *            for
     */
    private void removePricedParameters(Parameter parameter) {
        final Query query = dm
                .createNamedQuery("PricedParameter.getForParameter");
        query.setParameter("parameter", parameter);

        List<PricedParameter> list = ParameterizedTypes
                .list(query.getResultList(), PricedParameter.class);
        for (PricedParameter pricedParameter : list) {
            dm.remove(pricedParameter);
        }

    }

    /**
     * Checks if the parameter can be saved. This is the case for parameters
     * that have a value set or if they are configurable and they are no user
     * related platform parameters for technical products with access type
     * DIRECT.
     * 
     * @param isDirectAccess
     *            <code>true</code> if the technical product uses direct access
     * @param parameter
     *            the value object to check the parameter value and the
     *            configurable flag at
     * @param paramDef
     *            the domain object to get the parameter definition id from
     * @return
     */
    private boolean isParameterToBeSaved(boolean isDirectAccess,
            VOParameter parameter, ParameterDefinition paramDef) {
        return (!GenericValidator.isBlankOrNull(parameter.getValue())
                || parameter.isConfigurable())
                && !(isDirectAccess
                        && (PlatformParameterIdentifiers.CONCURRENT_USER
                                .equals(paramDef.getParameterId())
                                || PlatformParameterIdentifiers.NAMED_USER
                                        .equals(paramDef.getParameterId())));
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails getServiceForCustomer(VOOrganization customer,
            VOService service)
            throws OperationNotPermittedException, ObjectNotFoundException {

        ArgumentValidator.notNull("customer", customer);
        ArgumentValidator.notNull("service", service);

        Organization org = dm.getCurrentUser().getOrganization();

        // now determine customer and product domain objects
        Organization cust = null;
        Product prod = null;
        try {
            cust = dm.getReference(Organization.class, customer.getKey());
            prod = dm.getReference(Product.class, service.getKey());
        } catch (ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_PRODUCT_RETRIEVAL_FOR_CUSTOMER_FAILED,
                    Long.toString(customer.getKey()),
                    Long.toString(org.getKey()));
            throw e;
        }

        // product must belong to supplier
        PermissionCheck.owns(prod, org, logger, sessionCtx);
        // customer must be registered to supplier
        PermissionCheck.supplierOfCustomer(org, cust, logger, sessionCtx);

        Query query = dm.createNamedQuery("Product.getForCustomerAndTemplate");
        query.setParameter("customer", cust);
        query.setParameter("template", prod);

        Product customerProduct = null;
        customerProduct = getProductFromQueryResult(cust, query);

        VOServiceDetails voCustomerProduct = null;
        if (customerProduct != null) {
            LocalizerFacade facade = new LocalizerFacade(localizer,
                    dm.getCurrentUser().getLocale());
            voCustomerProduct = getServiceDetails(customerProduct, facade);
        }

        return voCustomerProduct;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails getServiceForSubscription(VOOrganization customer,
            String subscriptionId)
            throws OrganizationAuthoritiesException, ObjectNotFoundException {

        ArgumentValidator.notNull("customer", customer);
        ArgumentValidator.notNull("subscriptionId", subscriptionId);

        Organization org = dm.getCurrentUser().getOrganization();

        // now determine the customer domain object
        Organization cust = null;
        try {
            cust = dm.getReference(Organization.class, customer.getKey());
        } catch (ObjectNotFoundException e) {
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_PRODUCT_RETRIEVAL_FOR_CUSTOMER_AND_SUBSCRIPTION_FAILED,
                    Long.toString(customer.getKey()), subscriptionId,
                    Long.toString(org.getKey()));
            throw e;
        }

        Query query = dm.createNamedQuery("Product.getForCustomerAndSubId");
        query.setParameter("customer", cust);
        query.setParameter("subscriptionId", subscriptionId);

        Product subscriptionSpecificProduct = null;
        subscriptionSpecificProduct = getProductFromQueryResult(cust, query);

        VOServiceDetails result = null;
        if (subscriptionSpecificProduct != null) {
            LocalizerFacade facade = new LocalizerFacade(localizer,
                    dm.getCurrentUser().getLocale());
            result = getServiceDetails(subscriptionSpecificProduct, facade);
        }

        return result;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public VOServiceDetails getServiceDetails(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);

        Organization org = dm.getCurrentUser().getOrganization();

        Product storedProduct = dm.getReference(Product.class,
                service.getKey());

        if (storedProduct.getType() == ServiceType.PARTNER_SUBSCRIPTION
                && org.getGrantedRoleTypes()
                        .contains(OrganizationRoleType.SUPPLIER)) {
            PermissionCheck.owns(storedProduct.getTemplate().getTemplate(), org,
                    logger, sessionCtx);
        } else {
            PermissionCheck.owns(storedProduct, org, logger, sessionCtx);
        }

        if (storedProduct.getStatus() == ServiceStatus.DELETED) {
            return null;
        }

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        VOServiceDetails result = getServiceDetails(storedProduct, facade);

        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails savePriceModel(VOServiceDetails service,
            VOPriceModel priceModel)
            throws ObjectNotFoundException, OperationNotPermittedException,
            CurrencyException, ValidationException, ServiceStateException,
            PriceModelException, ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notNull("priceModel", priceModel);

        // check here if the Currencies of the product price models are ok
        validateCurrencyUniqunessOfMigrationPath(priceModel, service);

        Product product = prepareProductWithPriceModel(service, priceModel,
                null, ServiceType.TEMPLATE, null);

        dm.flush();
        dm.refresh(product);

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        VOServiceDetails result = getServiceDetails(product, facade);

        return result;
    }

    void validateCurrencyUniqunessOfMigrationPath(VOPriceModel priceModel,
            VOService referencedService) throws PriceModelException {

        Product referencedProduct = dm.find(Product.class,
                referencedService.getKey());
        if (referencedProduct != null) {
            // service is referenced service
            List<ProductReference> compatibleProductList = referencedProduct
                    .getCompatibleProductsTarget();
            for (ProductReference prodRef : compatibleProductList) {
                PriceModel referenceModel = prodRef.getSourceProduct()
                        .getPriceModel();
                if (!isCompatibleCurrency(referenceModel, priceModel)) {
                    PriceModelException pme = new PriceModelException(
                            PriceModelException.Reason.UNMODIFIABLE_CURRENCY);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, pme,
                            LogMessageIdentifier.WARN_SAVE_PRICE_MODEL_FAILED_NOT_SAME_CURRENCY_COMPATIBLE_PRODUCT,
                            referenceModel.getCurrency().getCurrencyISOCode(),
                            priceModel.getCurrencyISOCode(),
                            Long.toString(prodRef.getSourceProduct().getKey()),
                            Long.toString(prodRef.getKey()),
                            dm.getCurrentUser().getUserId());
                    throw pme;
                }
            }
            // service is source of referenced service
            compatibleProductList = referencedProduct.getCompatibleProducts();
            for (ProductReference prodRef : compatibleProductList) {
                PriceModel referenceModel = prodRef.getTargetProduct()
                        .getPriceModel();
                if (!isCompatibleCurrency(referenceModel, priceModel)) {
                    PriceModelException pme = new PriceModelException(
                            PriceModelException.Reason.UNMODIFIABLE_CURRENCY);
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, pme,
                            LogMessageIdentifier.WARN_SAVE_PRICE_MODEL_FAILED_NOT_SAME_CURRENCY_COMPATIBLE_PRODUCT,
                            referenceModel.getCurrency().getCurrencyISOCode(),
                            priceModel.getCurrencyISOCode(),
                            Long.toString(prodRef.getTargetProduct().getKey()),
                            Long.toString(prodRef.getKey()),
                            dm.getCurrentUser().getUserId());
                    throw pme;
                }
            }
        }
    }

    /**
     * Loop through the list of stepped prices and update the values for free
     * amount and additional price (the limit of the predecessor defines the
     * values).
     * 
     * @param list
     *            the list to update
     */
    void updateFreeAmountAndAdditionalPrice(List<SteppedPrice> list) {

        Collections.sort(list, new SteppedPriceComparator());
        int size = list.size();
        for (int i = 1; i < size; i++) {
            SteppedPrice prevStep = list.get(i - 1);
            if (prevStep.getLimit() == null) {
                list.get(i).setFreeEntityCount(0);
                list.get(i).setAdditionalPrice(BigDecimal.ZERO
                        .setScale(PriceConverter.NORMALIZED_PRICE_SCALING));
            } else {
                list.get(i).setFreeEntityCount(prevStep.getLimit().longValue());
                list.get(i)
                        .setAdditionalPrice((BigDecimal
                                .valueOf(prevStep.getLimit().longValue())
                                .subtract(BigDecimal.valueOf(
                                        prevStep.getFreeEntityCount())))
                                                .multiply(prevStep.getPrice())
                                                .add(prevStep
                                                        .getAdditionalPrice())
                                                .setScale(
                                                        PriceConverter.NORMALIZED_PRICE_SCALING,
                                                        RoundingMode.HALF_UP));
            }
        }
        if (size > 0) {
            list.get(0).setFreeEntityCount(0);
            list.get(0).setAdditionalPrice(BigDecimal.ZERO
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING));
            list.get(size - 1).setLimit(null);
        }

    }

    /**
     * Prepares and stores a product by setting a price model on it, considering
     * all the settings of the provided parameters.
     * 
     * @param voProductDetails
     *            The details on the product to be updated.
     * @param voPriceModel
     *            The price model information to be set.
     * @param targetCustomer
     *            The target customer to be set in the copy. Will be ignored if
     *            no copy is required.
     * @param subscription
     *            The subscription of the price model.
     * 
     * @return The updated product.
     * @throws ObjectNotFoundException
     *             Thrown in case the product cannot be found.
     * @throws OperationNotPermittedException
     *             Thrown in case the operation cannot be performed, as an
     *             attempt is made to illegally use data.
     * @throws CurrencyException
     *             Thrown in case the specified currency is not supported by the
     *             server.
     * @throws ValidationException
     *             Thrown in case a price for the price model or one of its
     *             events is negative.
     * @throws ServiceStateException
     *             Thrown in case we don't create a copy and the product's state
     *             isn't {@link ServiceStatus#INACTIVE}
     * @throws ConcurrentModificationException
     */
    Product prepareProductWithPriceModel(VOServiceDetails voProductDetails,
            VOPriceModel voPriceModel, Organization targetCustomer,
            ServiceType productType, Subscription subscription)
            throws ObjectNotFoundException, OperationNotPermittedException,
            CurrencyException, ValidationException, ServiceStateException,
            ConcurrentModificationException {

        // ensure the current user is a supplier
        PlatformUser currentUser = dm.getCurrentUser();
        Organization org = currentUser.getOrganization();

        Product product = dm.getReference(Product.class,
                voProductDetails.getKey());
        validateExternalServiceMustBeFree(voPriceModel,
                product.getTechnicalProduct().getAccessType());

        boolean isCreatePriceModel = product.getPriceModel() == null;
        boolean priceModelCreatedInTransaction = false;
        // indicate whether price model for service exists as a template for
        // price model for customer
        boolean isTemplateExistsForCustomer = false;
        if (productType == ServiceType.CUSTOMER_TEMPLATE) {
            if (product.getTemplate() == null) {
                priceModelCreatedInTransaction = true;
                product = copyCustomerProduct(targetCustomer, currentUser,
                        product);
                if (!isCreatePriceModel) {
                    isTemplateExistsForCustomer = true;
                }
            }
        }

        // ensure the product belongs to the vendor
        PermissionCheck.ownsPriceModel(product, org, logger, sessionCtx);

        if (product.getOwningSubscription() == null) {
            ProductValidator.validateInactiveOrSuspended(
                    ProductAssembler.getProductId(product),
                    product.getStatus());
        }

        boolean newPriceModelCreated = false;
        PriceModel priceModel = product.getPriceModel();
        if (priceModel != null) {
            newPriceModelCreated = priceModelCreatedInTransaction;
            // Validate that the price model has not been changed concurrently.
            if (!priceModelCreatedInTransaction) {
                BaseAssembler.verifyVersionAndKey(priceModel, voPriceModel);
            }
        } else {
            // if there is no price model, the product is not a copy and thus a
            // new price model must be created
            priceModel = new PriceModel();
            newPriceModelCreated = true;
        }

        if (product.getTechnicalProduct()
                .getAccessType() == ServiceAccessType.DIRECT) {
            PriceModelAssembler.validateForDirectAccess(voPriceModel);
        }

        // validate the price model
        PriceModelAssembler.validatePriceModelSettings(voPriceModel);

        // handle all data not related to prices first (just the product
        // reference)
        priceModel.setProduct(product);

        if (voPriceModel.isChargeable()) {
            setPriceModelToChargeable(voPriceModel, priceModel,
                    priceModelCreatedInTransaction, isCreatePriceModel,
                    productType, targetCustomer, subscription,
                    isTemplateExistsForCustomer);
        } else {

            if (voPriceModel.isExternal()) {
                setPriceModelToExternal(voPriceModel, priceModel);
                priceModel.setExternal(true);
                priceModel.setUuid(voPriceModel.getUuid());
            } else {
                setPriceModelToFree(voPriceModel, priceModel);
            }
        }

        product.setPriceModel(priceModel);

        persistProduct(currentUser, product);

        dm.flush();
        boolean descriptionChanged = localizePriceModel(voPriceModel,
                currentUser, priceModel);

        // copy license information from technical service and
        // just update license for current local
        // when price model is already has copied license information just
        // update value for the current local
        boolean licenseChanged = false;
        if (!ServiceType.isSubscription(productType)) {
            licenseChanged = saveLicenseInformationForPriceModel(
                    product.getTechnicalProduct().getKey(), priceModel.getKey(),
                    voPriceModel, currentUser, newPriceModelCreated);
        }
        if (licenseChanged || descriptionChanged) {
            if (ServiceType.isTemplate(productType)) {
                priceModelAudit.addLogEntryForAuditLogData(dm, product,
                        product.getTargetCustomer(), currentUser.getLocale(),
                        descriptionChanged, licenseChanged);
            } else if (ServiceType.isSubscription(productType)) {
                subscriptionAudit.addLogEntryForAuditLogData(dm,
                        product.getOwningSubscription(),
                        currentUser.getLocale(), descriptionChanged,
                        licenseChanged);
            }
        }
        consolidateSteppedPrices(priceModel);

        return product;
    }

    void setPriceModelToFree(VOPriceModel voPriceModel, PriceModel priceModel) {
        PriceModelType oldPriceModelType = priceModel.getType();
        PriceModelHandler priceModelHandler = new PriceModelHandler(dm,
                priceModel, DateFactory.getInstance().getTransactionTime());
        priceModelHandler.resetToNonChargeable(PriceModelType.FREE_OF_CHARGE);
        priceModelAudit.editPriceModelTypeToFree(dm, priceModel,
                voPriceModel.getKey(), oldPriceModelType);
    }

    void setPriceModelToExternal(VOPriceModel voPriceModel,
            PriceModel priceModel) {
        PriceModelHandler priceModelHandler = new PriceModelHandler(dm,
                priceModel, DateFactory.getInstance().getTransactionTime());
        priceModelHandler.resetToNonChargeable(PriceModelType.UNKNOWN);
    }

    private Product copyCustomerProduct(Organization targetCustomer,
            PlatformUser currentUser, Product product) {
        product = product.copyForCustomer(targetCustomer);
        try {
            dm.persist(product);
            if (targetCustomer == null && product.getTemplate() == null) {
                // only templates get a CatalogEntry
                CatalogEntry catalogEntry = QueryBasedObjectFactory
                        .createCatalogEntry(product, null);
                dm.persist(catalogEntry);
            }
        } catch (NonUniqueBusinessKeyException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "The product copy for product '" + product.getKey()
                            + "' cannot be stored, as the business key already exists.",
                    e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_CREATE_CUSTOMER_FOR_SPECIFIC_PRICEMODEL_FAILED,
                    Long.toString(currentUser.getKey()));
            throw sse;
        }
        return product;
    }

    void setPriceModelToChargeable(VOPriceModel voPriceModel,
            PriceModel priceModel, boolean priceModelCreatedInTransaction,
            boolean isCreatePriceModel, ServiceType productType,
            Organization targetCustomer, Subscription subscription,
            boolean isTemplateExistsForCustomer)
            throws ValidationException, OperationNotPermittedException,
            ConcurrentModificationException, CurrencyException {

        // remember old values, needed for auditlog
        SupportedCurrency oldCurrency = priceModel.getCurrency();
        PriceModelType oldPriceModelType = priceModel.getType();
        BigDecimal oldSubscriptionPrice = priceModel.getPricePerPeriod();
        BigDecimal oldOneTimeFee = priceModel.getOneTimeFee();
        BigDecimal oldUserPrice = priceModel.getPricePerUserAssignment();
        int oldFreePeriod = priceModel.getFreePeriod();
        PricingPeriod oldPricingPeriod = priceModel.getPeriod();

        // business code
        SupportedCurrency currency = getSupportedCurrency(voPriceModel,
                priceModel.getProduct());
        priceModel.setCurrency(currency);
        priceModel.setType(voPriceModel.getType());
        priceModel.setPeriod(voPriceModel.getPeriod());
        priceModel.setPricePerPeriod(voPriceModel.getPricePerPeriod());
        priceModel.setPricePerUserAssignment(
                voPriceModel.getPricePerUserAssignment());
        priceModel.setOneTimeFee(voPriceModel.getOneTimeFee());
        priceModel.setFreePeriod(voPriceModel.getFreePeriod());

        Product product = priceModel.getProduct();
        setEvents(voPriceModel, priceModel, product,
                priceModelCreatedInTransaction);

        List<Parameter> parameters = new ArrayList<>();
        if (product.getParameterSet() != null) {
            parameters = product.getParameterSet().getParameters();
        }
        List<VOPricedParameter> selectedParameters = voPriceModel
                .getSelectedParameters();
        List<PricedParameter> pricedParameters = convertAndValidateParameters(
                voPriceModel.getKey(), selectedParameters, parameters,
                priceModel, priceModelCreatedInTransaction, targetCustomer,
                subscription, isTemplateExistsForCustomer);

        setPricedParametersForPriceModel(voPriceModel.getKey(), priceModel,
                pricedParameters, selectedParameters,
                priceModelCreatedInTransaction, targetCustomer);

        List<VOPricedRole> roleSpecificUserPrices = voPriceModel
                .getRoleSpecificUserPrices();
        validatePricedProductRoles(roleSpecificUserPrices,
                priceModel.getProduct());
        setRoleSpecificPrices(voPriceModel.getKey(), priceModel, null, null,
                roleSpecificUserPrices, priceModelCreatedInTransaction,
                targetCustomer, subscription, null);

        List<SteppedPrice> steppedPrices = convertAndValidateSteppedPrices(
                voPriceModel.getKey(), voPriceModel.getSteppedPrices(),
                priceModel, null, null, priceModelCreatedInTransaction);
        if (!steppedPrices.isEmpty() && BigDecimal.ZERO
                .compareTo(priceModel.getPricePerUserAssignment()) != 0) {
            ValidationException ve = new ValidationException(
                    ValidationException.ReasonEnum.STEPPED_USER_PRICING,
                    "pricePerUserAssignment", new Object[] {});
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                    LogMessageIdentifier.WARN_STEPPED_PRICING_MIXED_WITH_BASEPRICE,
                    product.getProductId(), "price model",
                    String.valueOf(voPriceModel.getKey()));

            throw ve;
        }

        priceModelAudit.editPriceModelTypeToChargeable(dm, priceModel,
                voPriceModel.getKey(), oldCurrency, oldPriceModelType,
                oldFreePeriod, oldPricingPeriod);
        priceModelAudit.editSubscriptionPrice(dm, priceModel,
                oldSubscriptionPrice, isCreatePriceModel);
        if (productType.equals(ServiceType.TEMPLATE)
                || productType.equals(ServiceType.CUSTOMER_TEMPLATE)) {
            priceModelAudit.editOneTimeFee(dm, priceModel, oldOneTimeFee,
                    isCreatePriceModel);
        }
        if (voPriceModel.getSteppedPrices() != null
                && voPriceModel.getSteppedPrices().isEmpty()) {
            priceModelAudit.editUserPrice(dm, priceModel, oldUserPrice,
                    isCreatePriceModel);
        }
    }

    void setEvents(VOPriceModel voPriceModel, PriceModel priceModel,
            Product product, boolean priceModelCreatedInTransaction)
            throws ValidationException, OperationNotPermittedException,
            ConcurrentModificationException {
        TechnicalProduct tp = product.getTechnicalProduct();
        List<Event> events = new ArrayList<>(tp.getEvents());
        events.addAll(getPlatformEvents(tp));
        convertAndValidateEvents(voPriceModel.getKey(),
                voPriceModel.getConsideredEvents(), events, priceModel,
                priceModelCreatedInTransaction);
    }

    SupportedCurrency getSupportedCurrency(VOPriceModel priceModel,
            Product product) throws CurrencyException {
        // load the currency information
        SupportedCurrency currency = null;
        if (priceModel.getCurrencyISOCode() != null) {
            currency = new SupportedCurrency();
            currency.setCurrency(
                    Currency.getInstance(priceModel.getCurrencyISOCode()));
            currency = (SupportedCurrency) dm.find(currency);
        }

        if (priceModel.isChargeable()) {
            if (currency == null) {
                // currency is not supported by the system, so throw an
                // exception
                CurrencyException uc = new CurrencyException(
                        "Creation of price model failed.",
                        new Object[] { priceModel.getCurrencyISOCode() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, uc,
                        LogMessageIdentifier.WARN_PRODUCT_CREATION_FAILED_CURRENCY_NOT_SUPPORTED,
                        Long.toString(product.getKey()),
                        priceModel.getCurrencyISOCode(),
                        Long.toString(dm.getCurrentUser().getKey()));
                throw uc;
            }
        }

        return currency;
    }

    private void persistProduct(PlatformUser currentUser, Product product) {
        try {
            dm.persist(product);
            dm.flush();
        } catch (NonUniqueBusinessKeyException e) {
            // the operation is done internally, the user does not know
            // about the copying process.
            // If the persisting-step fails, it is due to internal problems,
            // the user cannot do anything, so throw a system exception
            SaaSSystemException sse = new SaaSSystemException(
                    "The product copy for product '" + product.getKey()
                            + "' cannot be stored, as the business key already exists.",
                    e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_CREATE_CUSTOMER_FOR_SPECIFIC_PRICEMODEL_FAILED,
                    Long.toString(currentUser.getKey()));
            throw sse;
        }
    }

    boolean localizePriceModel(VOPriceModel priceModel,
            PlatformUser currentUser, PriceModel priceModelToStore) {
        boolean localizeChanged = false;
        final String currentDescription = normalize(
                localizer.getLocalizedTextFromDatabase(currentUser.getLocale(),
                        priceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION));

        final String newDescription = normalize(priceModel.getDescription());
        if (!currentDescription.equals(newDescription)) {
            if (priceModel.isChargeable()) {
                localizer.storeLocalizedResource(currentUser.getLocale(),
                        priceModelToStore.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION,
                        newDescription);
            } else {
                localizer.removeLocalizedValues(priceModel.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            }
            localizeChanged = true;
        }
        return localizeChanged;
    }

    private String normalize(String str) {
        return (str == null) ? "" : str;
    }

    /**
     * updates all stepped prices set additional price and free entity count
     */
    private void consolidateSteppedPrices(PriceModel priceModel) {
        updateFreeAmountAndAdditionalPrice(priceModel.getSteppedPrices());
        for (PricedEvent event : priceModel.getConsideredEvents()) {
            updateFreeAmountAndAdditionalPrice(event.getSteppedPrices());
        }
        for (PricedParameter parameter : priceModel.getSelectedParameters()) {
            updateFreeAmountAndAdditionalPrice(parameter.getSteppedPrices());
        }
    }

    boolean saveLicenseInformationForPriceModel(long productKey,
            long priceModelKey, VOPriceModel priceModel,
            PlatformUser currentUser, boolean isCreateNewPriceModel) {
        final String userLocale = currentUser.getLocale();

        List<VOLocalizedText> oldLicenses;
        oldLicenses = getOldLicenses(productKey, priceModel.getKey(),
                isCreateNewPriceModel);
        String newLicense = priceModel.getLicense();
        if (newLicense == null) {
            newLicense = "";
        }
        for (VOLocalizedText localizedText : oldLicenses) {
            String oldLicense = localizedText.getText();
            String locale = localizedText.getLocale();
            if (locale.equals(userLocale)) {
                continue;
            }
            localizer.storeLocalizedResource(locale, priceModelKey,
                    LocalizedObjectTypes.PRICEMODEL_LICENSE, oldLicense);
        }
        localizer.storeLocalizedResource(userLocale, priceModelKey,
                LocalizedObjectTypes.PRICEMODEL_LICENSE, newLicense);
        return !newLicense.isEmpty();
    }

    private List<VOLocalizedText> getOldLicenses(long productKey,
            long priceModelKey, boolean isCreateNewPriceModel) {
        if (isCreateNewPriceModel) {
            return localizer.getLocalizedValues(productKey,
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC);
        }
        return localizer.getLocalizedValues(priceModelKey,
                LocalizedObjectTypes.PRICEMODEL_LICENSE);
    }

    /**
     * Validates the value object input for the priced product roles.
     * 
     * @param roleSpecificUserPrices
     *            The value objects.
     * @param product
     *            The related product.
     * @throws OperationNotPermittedException
     *             Thrown in case a defined role definition is not compatible to
     *             the product.
     */
    void validatePricedProductRoles(List<VOPricedRole> roleSpecificUserPrices,
            Product product) throws OperationNotPermittedException {
        Map<Long, RoleDefinition> keyRoleMap = new HashMap<>();
        for (RoleDefinition rd : product.getTechnicalProduct()
                .getRoleDefinitions()) {
            keyRoleMap.put(rd.getKey(), rd);
        }
        Set<Long> roleDefinitionKeys = new HashSet<>();
        for (VOPricedRole voPpr : roleSpecificUserPrices) {
            long roleDefinitionKey = voPpr.getRole().getKey();
            if (!roleDefinitionKeys.add(roleDefinitionKey)) {
                continue;
            }
            RoleDefinition rdToSet = keyRoleMap
                    .get(roleDefinitionKey);
            if (rdToSet == null) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        String.format(
                                "User '%s' tried to define a price for role definition '%s' which is not supported for product '%s'.",
                            dm.getCurrentUser().getKey(),
                            voPpr.getRole().getKey(),
                            product.getKey()));
                logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                        LogMessageIdentifier.WARN_USER_DEFINE_PRICE_FOR_ROLE_FAILED_NOT_SUPPORTED,
                        Long.toString(dm.getCurrentUser().getKey()),
                        Long.toString(voPpr.getRole().getKey()),
                        Long.toString(product.getKey()));
                throw onp;
            }
        }
    }

    /**
     * Sets defined role specific user prices for the given price model.
     * 
     * @param priceModel
     *            The price model to be enhanced.
     * @param pricedParameter
     *            The priced parameter to be enhanced. Only used if no price
     *            model is set.
     * @param pricedOption
     *            The priced option to be enhanced. Only used if no price model
     *            and priced parameter is set.
     * @param roleSpecificUserPrices
     *            The user prices to be stored.
     * @param targetCustomer
     *            The targetCustomer of the price model.
     * @param subscription
     *            The subscription of the price model.
     * @param priceModelCreatedInTransaction
     *            Indicates whether the price model was created in this
     * @param oldPricedProductRoles
     *            The list of PricedProductRole of template price model. Only
     *            used if price model for service exists as a template for price
     *            model for customer
     * @throws ValidationException
     *             Thrown in case a negative price is specified for the price
     *             per user field.
     * @throws ConcurrentModificationException
     *             Thrown in case the value object's version does not match the
     *             domain object's.
     * @throws OperationNotPermittedException
     *             Thrown in case a passed priced role value object key does not
     *             belong to the domain object.
     */
    void setRoleSpecificPrices(long voPriceModelKey, PriceModel priceModel,
            PricedParameter pricedParameter, PricedOption pricedOption,
            List<VOPricedRole> roleSpecificUserPrices,
            boolean priceModelCreatedInTransaction, Organization targetCustomer,
            Subscription subscription,
            List<PricedProductRole> oldPricedProductRoles)
            throws ValidationException, ConcurrentModificationException,
            OperationNotPermittedException {

        List<RoleDefinition> roleDefinitions = new ArrayList<>();
        List<PricedProductRole> existingProductRolePrices = new ArrayList<>();
        if (priceModel != null) {
            roleDefinitions = priceModel.getProduct().getTechnicalProduct()
                    .getRoleDefinitions();
            existingProductRolePrices = priceModel.getRoleSpecificUserPrices();
        } else if (pricedParameter != null) {
            roleDefinitions = pricedParameter.getPriceModel().getProduct()
                    .getTechnicalProduct().getRoleDefinitions();
            existingProductRolePrices = pricedParameter
                    .getRoleSpecificUserPrices();
        } else if (pricedOption != null) {
            roleDefinitions = pricedOption.getPricedParameter().getPriceModel()
                    .getProduct().getTechnicalProduct().getRoleDefinitions();
            existingProductRolePrices = pricedOption
                    .getRoleSpecificUserPrices();
        }

        Map<Long, RoleDefinition> roleDefinitionMap = new HashMap<>();
        for (RoleDefinition rd : roleDefinitions) {
            roleDefinitionMap.put(rd.getKey(), rd);
        }

        List<PricedProductRole> productRolePrices = new ArrayList<>();

        Map<Long, PricedProductRole> pprMap = new HashMap<>();
        for (PricedProductRole ppr : existingProductRolePrices) {
            pprMap.put(ppr.getRoleDefinition().getKey(), ppr);
        }

        // now parse the input
        for (VOPricedRole voPricedProductRole : roleSpecificUserPrices) {
            Long roleKey = voPricedProductRole.getRole().getKey();
            RoleDefinition roleDefinition = roleDefinitionMap.get(roleKey);
            PricedProductRole pricedProductRole;
            boolean ifLogRequired = (voPriceModelKey != 0);
            if (pprMap.containsKey(roleKey) && roleKey.longValue() != 0) {
                pricedProductRole = pprMap.remove(roleKey);
                validateDomainObjectKey(voPricedProductRole, pricedProductRole,
                        priceModelCreatedInTransaction);
                voPricedProductRole.setKey(pricedProductRole.getKey());
                updatePricedProductRole(voPriceModelKey, voPricedProductRole,
                        pricedProductRole, priceModel, roleDefinition,
                        pricedParameter, targetCustomer, subscription,
                        ifLogRequired);

            } else {
                pricedProductRole = createPricedProductRole(voPriceModelKey,
                        voPricedProductRole, priceModel, roleDefinition,
                        pricedParameter, pricedOption, targetCustomer,
                        subscription, oldPricedProductRoles);
            }
            productRolePrices.add(pricedProductRole);
        }

        // remove the remaining priced product roles, as they are obsolete
        Collection<PricedProductRole> obsoletePricedProductRoles = pprMap
                .values();
        for (PricedProductRole obsoleteRole : obsoletePricedProductRoles) {
            dm.remove(obsoleteRole);
        }

        // now update the affected domain object
        if (priceModel != null) {
            priceModel.setRoleSpecificUserPrices(productRolePrices);
        } else if (pricedParameter != null) {
            pricedParameter.setRoleSpecificUserPrices(productRolePrices);
        } else if (pricedOption != null) {
            pricedOption.setRoleSpecificUserPrices(productRolePrices);
        }

    }

    void updatePricedProductRole(long voPriceModelKey,
            VOPricedRole pricedProductRole, PricedProductRole pprToUpdate,
            PriceModel priceModel, RoleDefinition roleDefinition,
            PricedParameter pricedParameter, Organization targetCustomer,
            Subscription subscription, boolean ifNeedLog)
            throws ValidationException, ConcurrentModificationException {

        BigDecimal oldPricePerUser = pprToUpdate.getPricePerUser();
        PricedProductRoleAssembler.updatePricedProductRole(pricedProductRole,
                pprToUpdate);
        pprToUpdate.setRoleDefinition(roleDefinition);

        if (priceModel != null) {
            priceModelAudit.editServiceRolePrice(dm, voPriceModelKey,
                    priceModel, pprToUpdate, oldPricePerUser, targetCustomer,
                    subscription);
        } else if (pricedParameter != null && ifNeedLog) {
            priceModelAudit.editParameterUserRolePrice(dm, voPriceModelKey,
                    pprToUpdate, oldPricePerUser);
        } else {
            priceModelAudit.editParameterOptionUserRolePrice(dm, pprToUpdate,
                    oldPricePerUser);
        }
    }

    PricedProductRole createPricedProductRole(long voPriceModelKey,
            VOPricedRole pricedProductRole, PriceModel priceModel,
            RoleDefinition roleDefinition, PricedParameter pricedParameter,
            PricedOption pricedOption, Organization targetCustomer,
            Subscription subscription,
            List<PricedProductRole> oldPricedProductRoles)
            throws ValidationException {

        PricedProductRole pprToCreate = PricedProductRoleAssembler
                .toPricedProductRole(pricedProductRole);
        pprToCreate.setRoleDefinition(roleDefinition);
        BigDecimal oldPrice = findOldPrice(oldPricedProductRoles,
                roleDefinition);
        pprToCreate.setPricedParameter(pricedParameter);
        pprToCreate.setPriceModel(priceModel);
        pprToCreate.setPricedOption(pricedOption);

        if (priceModel != null) {
            priceModelAudit.editServiceRolePrice(dm, voPriceModelKey,
                    priceModel, pprToCreate, DEFAULT_PRICE_VALUE,
                    targetCustomer, subscription);
        } else if (pricedParameter != null) {
            priceModelAudit.editParameterUserRolePrice(dm, voPriceModelKey,
                    pprToCreate, DEFAULT_PRICE_VALUE);
        } else if (pricedOption != null) {
            priceModelAudit.editParameterOptionUserRolePrice(dm, pprToCreate,
                    oldPrice);
        }
        return pprToCreate;
    }

    private BigDecimal findOldPrice(
            List<PricedProductRole> oldPricedProductRoles,
            RoleDefinition roleDefinition) {
        BigDecimal oldPrice = DEFAULT_PRICE_VALUE;
        if (oldPricedProductRoles != null) {
            for (PricedProductRole ppr : oldPricedProductRoles) {
                if (ppr.getRoleDefinition().getKey() == roleDefinition
                        .getKey()) {
                    oldPrice = ppr.getPricePerUser();
                }
            }
        }
        return oldPrice;
    }

    /**
     * Validates the {@link VOPricedEvent}s and checks if they are defined based
     * on the event definitions from the products technical products. Tries to
     * match each {@link VOPricedEvent} to one of the existing
     * {@link PricedEvent}s (retrieved from the provided {@link PriceModel}) -
     * if a matching domain object was found, it will be updated and added to
     * the result list; if not, a new one will be created and added to the
     * result list. All remaining domain objects, where no matching value object
     * was found for, will be removed with the data manager.
     * 
     * @param voPricedEvents
     *            the {@link VOPricedEvent}s specifying the new values - can be
     *            a mixture of existing and new priced events
     * @param eventDefinitions
     *            a list of {@link Event} that belongs to the products technical
     *            product used for validating if the {@link PricedEvent}s to
     *            save belong to this set of {@link Event}s
     * @param priceModel
     *            the {@link PriceModel} to get the currently existing
     *            {@link PricedEvent}s from and the one to set to the created
     *            {@link PricedEvent}s
     * @param priceModelCreatedInTransaction
     *            Indicates whether the price model was created in this
     *            transaction or not.
     * 
     * @return the list of {@link PricedEvent} that has to be set to the
     *         {@link PriceModel}
     * @throws ValidationException
     *             in case of an validation error when converting the value
     *             object to a domain object
     * @throws OperationNotPermittedException
     *             in case the priced event does not belong to an event of the
     *             products technical product
     * @throws ConcurrentModificationException
     *             Thrown in case an event's value object's version does not
     *             match the current domain object.
     */
    List<PricedEvent> convertAndValidateEvents(long voPriceModelKey,
            List<VOPricedEvent> voPricedEvents, List<Event> eventDefinitions,
            PriceModel priceModel, boolean priceModelCreatedInTransaction)
            throws ValidationException, OperationNotPermittedException,
            ConcurrentModificationException {
        // helper map to get existing priced events
        Map<Long, PricedEvent> keyToPricedEvent = new HashMap<>();
        Map<Event, PricedEvent> eventToPricedEvent = new HashMap<>();
        List<PricedEvent> existing = priceModel.getConsideredEvents();
        for (PricedEvent sp : existing) {
            keyToPricedEvent.put(sp.getKey(), sp);
            eventToPricedEvent.put(sp.getEvent(), sp);
        }
        // helper map for checking if the event belongs to the product
        Map<Long, Event> keyToEvent = new LinkedHashMap<>();
        for (Event event : eventDefinitions) {
            keyToEvent.put(event.getKey(), event);
        }

        String userId = dm.getCurrentUser().getUserId();
        List<PricedEvent> result = new ArrayList<>();
        for (VOPricedEvent voPricedEvent : voPricedEvents) {
            long eventDefKey = voPricedEvent.getEventDefinition().getKey();
            Event event = keyToEvent.remove(eventDefKey);
            if (event == null) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "Event conversion failed");
                logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                        onp,
                        LogMessageIdentifier.WARN_EVENTS_NOT_BELONG_CORRECT_TECHNICAL_PRODUCT,
                        userId);
                throw onp;
            }

            PricedEvent pricedEvent = null;
            if (keyToPricedEvent
                    .containsKey(voPricedEvent.getKey())) {
                pricedEvent = keyToPricedEvent
                        .remove(voPricedEvent.getKey());
                updatePricedEvent(voPricedEvent, pricedEvent, event,
                        priceModel);
            } else {
                PricedEvent existingPricedEvent = eventToPricedEvent
                        .remove(event);
                validateDomainObjectKey(voPricedEvent, existingPricedEvent,
                        priceModelCreatedInTransaction);
                pricedEvent = createPricedEvent(voPricedEvent, event,
                        priceModel);
            }

            List<SteppedPrice> steppedPrices = convertAndValidateSteppedPrices(
                    voPriceModelKey, voPricedEvent.getSteppedPrices(),
                    priceModel, pricedEvent, null,
                    priceModelCreatedInTransaction);

            validateSteppedPrices(event, pricedEvent, steppedPrices);
            result.add(pricedEvent);
        }

        // set new priced events, to delete the references for events
        // which should be removed
        priceModel.setConsideredEvents(result);
        removePricedEvents(voPriceModelKey, keyToPricedEvent.values());

        return result;
    }

    void updatePricedEvent(VOPricedEvent voPricedEvent, PricedEvent pricedEvent,
            Event event, PriceModel priceModel)
            throws ValidationException, ConcurrentModificationException {

        BigDecimal oldPrice = pricedEvent.getEventPrice();
        pricedEvent = EventAssembler.updatePricedEvent(voPricedEvent,
                pricedEvent);
        pricedEvent.setEvent(event);
        pricedEvent.setPriceModel(priceModel);
        priceModelAudit.editEventPrice(dm, pricedEvent, oldPrice);
    }

    PricedEvent createPricedEvent(VOPricedEvent voPricedEvent, Event event,
            PriceModel priceModel) throws ValidationException {

        PricedEvent pricedEvent = EventAssembler.toPricedEvent(voPricedEvent);
        pricedEvent.setEvent(event);
        pricedEvent.setPriceModel(priceModel);
        priceModelAudit.editEventPrice(dm, pricedEvent, DEFAULT_PRICE_VALUE);
        return pricedEvent;
    }

    void removePricedEvents(long voPriceModelKey,
            Collection<PricedEvent> pricedEvents) {

        for (PricedEvent pricedEvent : pricedEvents) {
            priceModelAudit.removeEventPrice(dm, voPriceModelKey, pricedEvent);
            dm.remove(pricedEvent);
        }
    }

    /**
     * Exception in case event price and stepped prices set
     */
    private void validateSteppedPrices(Event event, PricedEvent pe,
            List<SteppedPrice> steppedPrices) throws ValidationException {
        if (!steppedPrices.isEmpty()
                && BigDecimal.ZERO.compareTo(pe.getEventPrice()) != 0) {
            ValidationException ve = new ValidationException(
                    ValidationException.ReasonEnum.STEPPED_EVENT_PRICING,
                    "eventPrice", new Object[] { event.getEventIdentifier() });
            logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                    LogMessageIdentifier.WARN_STEPPED_PRICING_MIXED_WITH_BASEPRICE,
                    pe.getPriceModel().getProduct().getProductId(),
                    "priced event", String.valueOf(pe.getKey()));
            throw ve;
        }
    }

    /**
     * Throw an OperationNotPermittedException if the key of the value object is
     * not equal to the key of the domain object. The validation is only
     * performed if the flag priceModelCreatedInTransaction is <code>true</code>
     * .
     * 
     * @param vo
     *            The value object representation.
     * @param domainObject
     *            The domain object representation.
     * @param priceModelCreatedInTransaction
     *            Indicates whether a key-clash can be ignored or not.
     * @throws OperationNotPermittedException
     *             Thrown if the key values do not match.
     */
    private void validateDomainObjectKey(BaseVO vo,
            DomainObject<?> domainObject,
            boolean priceModelCreatedInTransaction)
            throws OperationNotPermittedException {
        if (domainObject != null && !priceModelCreatedInTransaction
                && domainObject.getKey() != vo.getKey()) {
            PlatformUser user = dm.getCurrentUser();
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "Saving the price model failed.");
            logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                    LogMessageIdentifier.WARN_STORE_DOMAIN_OBJECT_FAILED_WRONG_TECHNICAL_KEY,
                    user.getUserId(), String.valueOf(domainObject));
            throw onp;
        }
    }

    /**
     * Converts the list of {@link VOSteppedPrice} to be set to a list of
     * {@link SteppedPrice} that can be saved. One of the three parent entities
     * must be passed. Stepped prices that shall be attached on a priced
     * parameter will be validated according to their parameter based
     * constraints (numeric parameters only; no limits out of the defined
     * range).
     * 
     * @param voSteppedPrices
     *            the list of stepped prices to be set
     * @param priceModel
     *            The parent price model if the stepped pricing is done on the
     *            price per user of the price model
     * @param pricedEvent
     *            The parent priced event if the stepped pricing is done based
     *            on the number of occurrence of an event
     * @param pricedParameter
     *            the parent priced parameter if the stepped pricing is done
     *            based on the value of a parameter
     * @param priceModelCreatedInTransaction
     *            Indicates whether the price model was created in this
     *            transaction or not.
     * @return the list of {@link SteppedPrice} to be set at the parent
     * @throws ValidationException
     *             in case of stepped pricing on a parameter of a non numeric
     *             type or violated range limits
     * @throws ConcurrentModificationException
     *             Thrown in case the passed value object's version does not
     *             match the domain object.
     * @throws OperationNotPermittedException
     *             Thrown in case the passed value object key does not belong to
     *             a domain object.
     */
    List<SteppedPrice> convertAndValidateSteppedPrices(long voPriceModelKey,
            List<VOSteppedPrice> voSteppedPrices, PriceModel priceModel,
            PricedEvent pricedEvent, PricedParameter pricedParameter,
            boolean priceModelCreatedInTransaction) throws ValidationException,
            ConcurrentModificationException, OperationNotPermittedException {

        List<SteppedPrice> existing = new ArrayList<>();

        if (pricedEvent == null && pricedParameter == null
                && priceModel != null) {
            existing = priceModel.getSteppedPrices();
        } else if (pricedEvent != null) {
            existing = pricedEvent.getSteppedPrices();
        } else if (pricedParameter != null) {
            existing = pricedParameter.getSteppedPrices();
        }

        Map<Long, SteppedPrice> keyToSteppedPrice = new HashMap<>();

        if (existing != null) {
            for (SteppedPrice sp : existing) {
                keyToSteppedPrice.put(sp.getKey(), sp);
            }
        }
        List<SteppedPrice> result = new ArrayList<>();
        if (voSteppedPrices != null) {
            for (VOSteppedPrice voSteppedPrice : voSteppedPrices) {
                if (pricedParameter != null) {
                    validateSteppedPriceContraints(pricedParameter,
                            voSteppedPrice);
                }

                SteppedPrice steppedPrice;
                if (keyToSteppedPrice
                        .containsKey(voSteppedPrice.getKey())) {

                    steppedPrice = keyToSteppedPrice
                            .remove(voSteppedPrice.getKey());
                    updateSteppedPrice(voSteppedPrice, steppedPrice, priceModel,
                            pricedEvent, pricedParameter);

                } else {
                    if (!priceModelCreatedInTransaction
                            && voSteppedPrice.getKey() != 0) {
                        OperationNotPermittedException onp = new OperationNotPermittedException(
                                "Priced parameter with invalid key.");
                        logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                                LogMessageIdentifier.WARN_PRICED_PARAMETER_WITH_INVALID_KEY);
                        throw onp;
                    }
                    steppedPrice = createSteppedPrice(voSteppedPrice,
                            priceModel, pricedEvent, pricedParameter);
                }
                result.add(steppedPrice);
            }
        }

        // set stepped prices before remove entities, to remove the references
        if (pricedEvent == null && pricedParameter == null
                && priceModel != null) {
            priceModel.setSteppedPrices(result);
        } else if (pricedEvent != null) {
            pricedEvent.setSteppedPrices(result);
        } else if (pricedParameter != null) {
            pricedParameter.setSteppedPrices(result);
        }

        removeSteppedPrices(voPriceModelKey, keyToSteppedPrice.values(),
                pricedEvent, pricedParameter, priceModel);

        return result;
    }

    SteppedPrice createSteppedPrice(VOSteppedPrice voSteppedPrice,
            PriceModel priceModel, PricedEvent pricedEvent,
            PricedParameter pricedParameter) throws ValidationException {

        SteppedPrice steppedPrice = SteppedPriceAssembler
                .toSteppedPrice(voSteppedPrice);
        setSteppedPrice(steppedPrice, priceModel, pricedEvent, pricedParameter);

        if (pricedEvent == null && pricedParameter == null
                && priceModel != null) {
            priceModelAudit.insertUserSteppedPrice(dm, steppedPrice);
        } else if (pricedEvent != null) {
            priceModelAudit.insertEventSteppedPrice(dm, steppedPrice);
        } else if (pricedParameter != null) {
            priceModelAudit.insertParameterSteppedPrice(dm, steppedPrice);
        }
        return steppedPrice;
    }

    private void setSteppedPrice(SteppedPrice steppedPrice,
            PriceModel priceModel, PricedEvent pricedEvent,
            PricedParameter pricedParameter) {

        steppedPrice.setPricedEvent(pricedEvent);
        steppedPrice.setPricedParameter(pricedParameter);
        if (pricedEvent != null || pricedParameter != null) {
            steppedPrice.setPriceModel(null);
        } else {
            steppedPrice.setPriceModel(priceModel);
        }
    }

    void updateSteppedPrice(VOSteppedPrice voSteppedPrice,
            SteppedPrice steppedPrice, PriceModel priceModel,
            PricedEvent pricedEvent, PricedParameter pricedParameter)
            throws ValidationException, ConcurrentModificationException {

        Long oldLimit = steppedPrice.getLimit();
        BigDecimal oldPrice = steppedPrice.getPrice();
        SteppedPriceAssembler.updateSteppedPrice(voSteppedPrice, steppedPrice);
        setSteppedPrice(steppedPrice, priceModel, pricedEvent, pricedParameter);

        if (pricedEvent == null && pricedParameter == null
                && priceModel != null) {
            priceModelAudit.editUserSteppedPrice(dm, steppedPrice, oldPrice,
                    oldLimit);
        } else if (pricedEvent != null) {
            priceModelAudit.editEventSteppedPrice(dm, steppedPrice, oldPrice,
                    oldLimit);
        } else if (pricedParameter != null) {
            priceModelAudit.editParameterSteppedPrice(dm, steppedPrice,
                    oldPrice, oldLimit);
        }
    }

    void removeSteppedPrices(long voPriceModelKey,
            Collection<SteppedPrice> steppedPrices, PricedEvent pricedEvent,
            PricedParameter pricedParameter, PriceModel priceModel) {
        for (SteppedPrice steppedPrice : steppedPrices) {
            // log
            if (voPriceModelKey > 0) {
                /*
                 * For priceModel creation : do not log stepped prices again. It
                 * is already done in createSteppedPrices <br/> For VOPriceModel
                 * update : log removed stepped prices
                 */
                if (pricedEvent == null && pricedParameter == null
                        && priceModel != null) {
                    priceModelAudit.removeUserSteppedPrice(dm, steppedPrice);
                } else if (pricedEvent != null) {
                    priceModelAudit.removeEventSteppedPrice(dm, steppedPrice);
                } else if (pricedParameter != null) {
                    priceModelAudit.removeParameterSteppedPrice(dm,
                            steppedPrice);
                }
            }

            dm.remove(steppedPrice);
        }
    }

    /**
     * Validates the stepped price - priced parameter constraints:
     * <ul>
     * <li>stepped pricing can only be done on parameters of type
     * {@link ParameterValueType#DURATION}, {@link ParameterValueType#INTEGER}
     * and {@link ParameterValueType#LONG}</li>
     * <li>the step limits must be within the data type limits and the defined
     * parameter minimum and maximum value</li>
     * </ul>
     * 
     * @param pricedParameter
     *            the priced parameter the stepped prices will be attached to
     * @param steppedPrice
     *            the stepped price
     * @throws ValidationException
     *             in case of an validation error
     */
    void validateSteppedPriceContraints(PricedParameter pricedParameter,
            VOSteppedPrice steppedPrice) throws ValidationException {

        ParameterDefinition paramDef = pricedParameter.getParameter()
                .getParameterDefinition();
        ParameterValueType type = paramDef.getValueType();
        if (type == ParameterValueType.BOOLEAN
                || type == ParameterValueType.ENUMERATION
                || type == ParameterValueType.STRING
                || type == ParameterValueType.DURATION) {
            // Value based stepped parameter prices can only be defined
            // for parameter types INTEGER and LONG. for all
            // other types the value based stepped prices list must be
            // empty for this check value is already tested: param !=
            // null
            throw new ValidationException(ReasonEnum.STEPPED_PRICING,
                    VOSteppedPrice.FIELD_NAME_PRICED_PARAMETER_VALUE,
                    new Object[] { steppedPrice });
        }

        if (steppedPrice.getLimit() == null) {
            return;
        }
        // If the stepped prices are defined for a parameter value, it must be
        // checked that the limits are in the defined range for the parameter
        // value (minValue, maxValue) or in the logical range – as limit is a
        // long value, e. g. for parameters of type INTEGER, the limit must be
        // also within the java integer range.
        long limit = steppedPrice.getLimit().longValue();
        Long minValue = paramDef.getMinimumValue();
        Long maxValue = paramDef.getMaximumValue();
        if (type == ParameterValueType.INTEGER) {
            BLValidator.isInteger(SteppedPriceAssembler.FIELD_NAME_LIMIT,
                    String.valueOf(limit));
            BLValidator.isInRange(SteppedPriceAssembler.FIELD_NAME_LIMIT, limit,
                    minValue, maxValue);
        } else if (type == ParameterValueType.LONG) {
            BLValidator.isInRange(SteppedPriceAssembler.FIELD_NAME_LIMIT, limit,
                    minValue, maxValue);
        }

    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails savePriceModelForCustomer(VOServiceDetails service,
            VOPriceModel priceModel, VOOrganization customer)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException, ServiceStateException, PriceModelException,
            ServiceOperationException, ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notNull("priceModel", priceModel);
        ArgumentValidator.notNull("customer", customer);

        Organization targetCustomer = dm.getReference(Organization.class,
                customer.getKey());

        PlatformUser currentUser = dm.getCurrentUser();
        validateCustomersRole(targetCustomer, currentUser);
        PermissionCheck.supplierOfCustomer(currentUser.getOrganization(),
                targetCustomer, logger, sessionCtx);

        validateIfCustomerServiceExists(service, targetCustomer);
        validateCurrencyUniqunessOfMigrationPath(priceModel, service);

        Product product = prepareProductWithPriceModel(service, priceModel,
                targetCustomer, ServiceType.CUSTOMER_TEMPLATE, null);

        dm.flush();
        dm.refresh(product);

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        VOServiceDetails result = getServiceDetails(product, facade);

        return result;
    }

    void validateCustomersRole(Organization targetCustomer,
            PlatformUser currentUser) throws OperationNotPermittedException {
        // ensure that the customer is a) authorized as customer and b)
        // registered for the calling
        if (!targetCustomer.hasRole(OrganizationRoleType.CUSTOMER)) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "Creation of customer specific price model failed");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_CREATE_PRICE_MODEL_FOR_ORGANIZATION_FAILED_NOT_AUTHORIZED,
                    currentUser.getUserId(),
                    Long.toString(currentUser.getOrganization().getKey()));
            throw onp;
        }
    }

    private void validateIfCustomerServiceExists(VOServiceDetails service,
            Organization targetCustomer) throws ObjectNotFoundException,
            ServiceOperationException, ServiceStateException {
        // ensure that in case the given product is a not a copy itself, no
        // other copy of it does exist for the current customer. If there
        // already is one, throw an exception. The caller should use the
        // customer copy as argument
        Product referencedProduct = dm.getReference(Product.class,
                service.getKey());
        if (referencedProduct.getTemplate() == null) {
            // check if there is a copy for this customer
            Query query = dm.createNamedQuery("Product.getCopyForCustomer");
            query.setParameter("template", referencedProduct);
            query.setParameter("customer", targetCustomer);
            List<Product> resultList = ParameterizedTypes
                    .list(query.getResultList(), Product.class);
            if (resultList.size() > 0) {
                ServiceOperationException sof = new ServiceOperationException(
                        Reason.CUSTOMER_COPY_ALREADY_EXISTS);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, sof,
                        LogMessageIdentifier.WARN_EX_SERVICE_OPERATION_EXCEPTION_CUSTOMER_COPY_ALREADY_EXISTS);
                throw sof;
            }
        }

        ProductValidator.validateActiveOrInactiveOrSuspended(
                referencedProduct.getProductId(),
                referencedProduct.getStatus());
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails savePriceModelForSubscription(
            VOServiceDetails service, VOPriceModel priceModel)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException, ConcurrentModificationException,
            SubscriptionStateException, PaymentInformationException,
            PriceModelException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notNull("priceModel", priceModel);

        PlatformUser currentUser = dm.getCurrentUser();

        Product product = dm.getReference(Product.class, service.getKey());
        validateExternalServiceMustBeFree(priceModel,
                product.getTechnicalProduct().getAccessType());

        // ensure the subscription belongs to the given product
        Subscription sub = validateSubscription(service, currentUser, product);

        validatePriceModel(priceModel, product, sub);

        // Free trial period value cannot be changed on saving the price model
        // for subscription
        validateFreePeriod(priceModel, product);

        validateOneTimeFee(priceModel, product);

        validateCurrencyUniqunessOfMigrationPath(priceModel, service);
        // as every subscription already has a separate product copy, don't
        // create a new one!! just retrieve the reference to the old
        product = null;
        try {
            product = prepareProductWithPriceModel(service, priceModel,
                    sub.getOrganization(), ServiceType.SUBSCRIPTION, sub);
        } catch (ServiceStateException e) {
            // this must not happen
            throw new SaaSSystemException(e);
        }

        dm.flush();
        dm.refresh(product);

        LocalizerFacade facade = new LocalizerFacade(localizer,
                currentUser.getLocale());
        VOServiceDetails result = getServiceDetails(product, facade);

        return result;
    }

    /**
     * test for not rewriting one-time fee for subscription price model for
     * Price Model for subscription we can not change one-time fee, new value
     * has to be always 0
     */
    private void validateOneTimeFee(VOPriceModel priceModel, Product product)
            throws OperationNotPermittedException {
        BigDecimal oneTimeFeeNew = priceModel.getOneTimeFee();
        BigDecimal oneTimeFeeOld = product.getPriceModel().getOneTimeFee();
        if (oneTimeFeeNew.compareTo(BigDecimal.ZERO) != 0) {
            if (oneTimeFeeNew.compareTo(oneTimeFeeOld) != 0) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "One-time fee can not be changed for a subscription-specific price model.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                        LogMessageIdentifier.WARN_UNCHANGEABLE_SUBSCRIPTION_ONE_TIME_FEE);
                throw onp;
            }
        } else {
            priceModel.setOneTimeFee(oneTimeFeeOld);
        }
    }

    private void validateFreePeriod(VOPriceModel priceModel, Product product)
            throws OperationNotPermittedException {
        int freePeriodNew = priceModel.getFreePeriod();
        int freePeriodOld = product.getPriceModel().getFreePeriod();
        if (freePeriodNew != freePeriodOld) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "Free trial period can not be changed for a subscription-specific price model.");

            logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                    LogMessageIdentifier.WARN_SAVE_PRICE_MODEL_FOR_SUBSCRIPTION_FAILED_FREEPERIOD_UNMODIFIABLE);

            throw onp;
        }
    }

    private void validatePriceModel(VOPriceModel priceModel, Product product,
            Subscription sub)
            throws PaymentInformationException, PriceModelException {
        validatePriceModelType(product.getPriceModel(), priceModel);
        validateCurrency(product.getPriceModel(), priceModel);
        validateTimeUnit(product.getPriceModel(), priceModel);

        if (priceModel.isChargeable()) {

            if (sub.getPaymentInfo() == null
                    || sub.getBillingContact() == null) {
                PaymentInformationException pie = new PaymentInformationException(
                        String.format(
                                "Defining the price model for the subscription %s is not possible, as no payment information has been specified.",
                                sub));
                logger.logWarn(Log4jLogger.SYSTEM_LOG, pie,
                        LogMessageIdentifier.WARN_SAVE_PRICE_MODEL_FAILED_NO_PAYMENT_INFO,
                        sub.toString());
                sessionCtx.setRollbackOnly();
                throw pie;
            }

        }

        // Free of charge value cannot be changed on saving the price model for
        // subscription
        boolean chargeableValueNew = priceModel.isChargeable();
        boolean chargeableValueOld = product.getPriceModel().isChargeable();
        if (chargeableValueNew != chargeableValueOld) {
            PriceModelException pme = new PriceModelException(
                    PriceModelException.Reason.UNMODIFIABLE_CHARGEABLE);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, pme,
                    LogMessageIdentifier.WARN_UNCHANGEABLE_SUBSCRIPTION_CHARGING_CONDITIONS);
            sessionCtx.setRollbackOnly();
            throw pme;
        }
    }

    void validatePriceModelType(PriceModel existingPriceModel,
            VOPriceModel newPriceModel) throws PriceModelException {
        if (!existingPriceModel.getType().equals(newPriceModel.getType())) {
            PriceModelException pme = new PriceModelException(
                    PriceModelException.Reason.UNMODIFIABLE_TYPE);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, pme,
                    LogMessageIdentifier.WARN_PRICE_MODEL_TYPE_UNMODIFIABLE_FOR_SUBSCRIPTION);
            throw pme;
        }
    }

    void validateCurrency(PriceModel existingPriceModel,
            VOPriceModel newPriceModel) throws PriceModelException {
        if (existingPriceModel.isChargeable()) {
            boolean isCurrencyChanged = !(existingPriceModel.getCurrency()
                    .getCurrencyISOCode()
                    .equals(newPriceModel.getCurrencyISOCode()));
            if (isCurrencyChanged) {
                PriceModelException pme = new PriceModelException(
                        PriceModelException.Reason.UNMODIFIABLE_CURRENCY);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, pme,
                        LogMessageIdentifier.WARN_CURRENCY_UNMODIFIABLE_FOR_SUBSCRIPTION);
                throw pme;
            }
        }
    }

    void validateTimeUnit(PriceModel existingPriceModel,
            VOPriceModel newPriceModel) throws PriceModelException {
        if (existingPriceModel.isChargeable()) {
            boolean isTimeUnitChanged = !(existingPriceModel.getPeriod()
                    .equals(newPriceModel.getPeriod()));
            if (isTimeUnitChanged) {
                PriceModelException pme = new PriceModelException(
                        PriceModelException.Reason.UNMODIFIABLE_TIMEUNIT);
                logger.logWarn(Log4jLogger.SYSTEM_LOG, pme,
                        LogMessageIdentifier.WARN_TIME_UNIT_UNMODIFIABLE_FOR_SUBSCRIPTION);
                throw pme;
            }
        }
    }

    private void validateExternalServiceMustBeFree(VOPriceModel priceModel,
            ServiceAccessType sat) throws ValidationException {

        if (priceModel.isChargeable() && sat == ServiceAccessType.EXTERNAL) {
            throw new ValidationException(
                    ReasonEnum.EXTERNAL_SERVICE_MUST_BE_FREE_OF_CHARGE, null,
                    null);

        }
    }

    @Override
    public VOSubscriptionDetails validateSubscription(VOService service)
            throws OperationNotPermittedException, SubscriptionStateException,
            ObjectNotFoundException {
        PlatformUser currentUser = dm.getCurrentUser();
        Product product = dm.getReference(Product.class, service.getKey());
        Subscription subscription = validateSubscription(service, currentUser,
                product);
        VOSubscriptionDetails voSubscriptionDetails = subscriptionService
                .getSubscriptionDetailsWithoutOwnerCheck(subscription.getKey());
        return voSubscriptionDetails;
    }

    private Subscription validateSubscription(VOService service,
            PlatformUser currentUser, Product product)
            throws OperationNotPermittedException, SubscriptionStateException {
        Subscription sub = product.getOwningSubscription();
        if (sub == null) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "Creation of subscription specific price model failed");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_CREATE_PRICE_MODEL_FAILED_NOT_SUBSCRIPTION_DEFINED,
                    currentUser.getUserId(), Long.toString(product.getKey()),
                    Long.toString(service.getKey()));
            throw onp;
        }

        if (sub.getStatus() == SubscriptionStatus.EXPIRED
                || sub.getStatus() == SubscriptionStatus.INVALID
                || sub.getStatus() == SubscriptionStatus.DEACTIVATED) {

            Object[] params = new Object[] { sub.getStatus().name() };
            SubscriptionStateException sse = new SubscriptionStateException(
                    SubscriptionStateException.Reason.SUBSCRIPTION_STATE_CHANGED,
                    null, params);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.WARN_SAVE_PRICE_MODEL_FAILED_SUBSCRIPTION_STATE_INVALID);
            throw sse;
        }
        return sub;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public List<VOService> getCompatibleServices(VOService service)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);

        List<VOService> result = new ArrayList<>();

        // verify that the caller is a supplier
        Organization org = dm.getCurrentUser().getOrganization();

        // retrieve the domain object for the product
        Product prod = dm.getReference(Product.class, service.getKey());

        // ensure that the product belongs to the calling supplier
        PermissionCheck.owns(prod, org, logger, sessionCtx);

        // determine the compatible products
        List<Product> compatibleProducts = prod.getCompatibleProductsList();

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        for (Product compatibleProduct : compatibleProducts) {
            // only consider not deleted and not obsolete target products
            if (compatibleProduct.getStatus() != ServiceStatus.DELETED
                    && compatibleProduct
                            .getStatus() != ServiceStatus.OBSOLETE) {
                // check currency compatibility
                if (isCompatibleCurrency(prod.getPriceModel(),
                        compatibleProduct.getPriceModel())) {
                    result.add(ProductAssembler.toVOProduct(compatibleProduct,
                            facade));
                }
            }
        }

        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public void setCompatibleServices(VOService service,
            List<VOService> compatibleServices)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ServiceCompatibilityException,
            ServiceStateException, ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notNull("compatibleServices", compatibleServices);

        // 1. ensure that the caller is a supplier
        Organization org = dm.getCurrentUser().getOrganization();

        // 2. find the domain objects for the reference product and the
        // compatible products. For each of them ensure that they are owned by
        // the calling supplier. Check relation to the owning technical product
        // as well
        Product referenceProduct = dm.getReference(Product.class,
                service.getKey());
        BaseAssembler.verifyVersionAndKey(referenceProduct, service);
        ProductValidator.validateInactiveOrSuspended(
                ProductAssembler.getProductId(referenceProduct),
                referenceProduct.getStatus());
        PermissionCheck.owns(referenceProduct, org, logger, sessionCtx);
        List<Product> compProducts = new ArrayList<>();
        for (VOService voProd : compatibleServices) {
            Product tempProd = dm.getReference(Product.class, voProd.getKey());
            BaseAssembler.verifyVersionAndKey(tempProd, voProd);
            PermissionCheck.owns(tempProd, org, logger, sessionCtx);

            validateTechnicalProductCompatibility(referenceProduct, tempProd);
            validateCurrencyCompatibility(referenceProduct, tempProd);
            validateMarketplaceCompatibility(referenceProduct, tempProd);
            compProducts.add(tempProd);
        }

        // 3. internal check: ensure that the products are no copies! if they
        // are, throw an exception
        boolean isCopy = false;
        long productKey = 0;
        if (referenceProduct.getTemplate() != null) {
            isCopy = true;
            productKey = referenceProduct.getKey();
        }
        for (Product compatibleProduct : compProducts) {
            if (compatibleProduct.getTemplate() != null) {
                isCopy = true;
                productKey = compatibleProduct.getKey();
            }
        }
        if (isCopy) {
            SaaSSystemException sse = new SaaSSystemException(
                    "Defining product compatibility failed, as product '"
                            + productKey + "' is not a template");
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_INVALID_ARGUMENT_AS_PRODUCT_NOT_TEMPLATE);
            throw sse;
        }

        // 4. Create the compatibility information
        // first determine all products which are currently marked as compatible
        // to the reference product
        Set<Long> currentCompatibleProductKeys = new HashSet<>();
        Set<Long> obsoleteCompatibleProductKeys = new HashSet<>();
        for (Product currentlyCompatibleProduct : referenceProduct
                .getCompatibleProductsList()) {
            currentCompatibleProductKeys
                    .add(currentlyCompatibleProduct.getKey());
            obsoleteCompatibleProductKeys
                    .add(currentlyCompatibleProduct.getKey());
        }

        // now register references, determine and omit duplicates, add only new
        // entries
        List<ProductReference> newReferences = new ArrayList<>();
        for (Product prod : compProducts) {
            if (!currentCompatibleProductKeys
                    .contains(prod.getKey())) {
                ProductReference ref = new ProductReference(referenceProduct,
                        prod);
                newReferences.add(ref);
                currentCompatibleProductKeys.add(prod.getKey());
            }
            obsoleteCompatibleProductKeys.remove(prod.getKey());
        }

        // finally clean up those products which are not marked as compatible
        // anymore, persist the new ones
        List<ProductReference> objToBeRemoved = new ArrayList<>();
        for (ProductReference currentlyCompatibleProductRef : referenceProduct
                .getAllCompatibleProducts()) {
            if (obsoleteCompatibleProductKeys
                    .contains(currentlyCompatibleProductRef
                        .getTargetProduct().getKey())) {
                dm.remove(currentlyCompatibleProductRef);
                objToBeRemoved.add(currentlyCompatibleProductRef);
            }
        }
        referenceProduct.getAllCompatibleProducts().removeAll(objToBeRemoved);
        for (ProductReference newProductRef : newReferences) {
            try {
                dm.persist(newProductRef);
                referenceProduct.getAllCompatibleProducts().add(newProductRef);
            } catch (NonUniqueBusinessKeyException e) {
                // it's a new creation, there is no business key, so this
                // scenario should never occur. If it does, something is
                // completely wrong, so abort by throwing a system exception
                SaaSSystemException sse = new SaaSSystemException(
                        "Persisting the product reference failed", e);
                logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.ERROR_DEFINE_COMPATIBLE_PRODUCT_FAILED);
                throw sse;
            }
        }

        logServiceUpDownGradeOptions(referenceProduct, newReferences,
                DEFINEIPDOWNGRADE_ON);
        logServiceUpDownGradeOptions(referenceProduct, objToBeRemoved,
                DEFINEIPDOWNGRADE_OFF);

    }

    void logServiceUpDownGradeOptions(Product referencrProdut,
            List<ProductReference> targetProducts, String upDowngrade) {
        for (ProductReference productReference : targetProducts) {
            serviceAudit.defineUpDownGradeOptions(dm, referencrProdut,
                    productReference.getTargetProduct(), upDowngrade);
        }
    }

    /**
     * Checks that the target product is published on the same marketplace as
     * the source product.
     * 
     * @param referenceProduct
     *            the source product
     * @param compatibleProduct
     *            the target product
     * @throws ServiceCompatibilityException
     */
    private void validateMarketplaceCompatibility(Product referenceProduct,
            Product compatibleProduct) throws ServiceCompatibilityException {
        Set<Marketplace> mps = new HashSet<>();
        Product s = referenceProduct.getTemplateOrSelf();
        Product t = compatibleProduct.getTemplateOrSelf();
        for (CatalogEntry ce : s.getCatalogEntries()) {
            if (ce.getMarketplace() != null) {
                mps.add(ce.getMarketplace());
            }
        }
        for (CatalogEntry ce : t.getCatalogEntries()) {
            if (ce.getMarketplace() != null
                    && mps.contains(ce.getMarketplace())) {
                return;
            }
        }
        ServiceCompatibilityException ipc = new ServiceCompatibilityException(
                "Definition of product compatibility failed, they are not published on the same marketplace",
                ServiceCompatibilityException.Reason.MARKETPLACE);
        logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ipc,
                LogMessageIdentifier.WARN_DEFINE_COMPATIBILITY_FOR_PRODUCTS_FAILED_NOT_SAME_MARKETPLACE,
                dm.getCurrentUser().getUserId(), Long.toString(s.getKey()),
                Long.toString(t.getKey()));
        throw ipc;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public void deleteService(VOService service)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ServiceOperationException,
            ServiceStateException, ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);

        // 1. Determine the callers organization, load the product and ensure
        // that the callers organization owns the product
        Organization supplier = dm.getCurrentUser().getOrganization();
        Product productToDelete = dm.getReference(Product.class,
                service.getKey());
        BaseAssembler.verifyVersionAndKey(productToDelete, service);
        deleteProduct(supplier, productToDelete);
        serviceAudit.deleteService(dm, productToDelete);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deleteProduct(Organization supplier, Product product)
            throws OperationNotPermittedException, ServiceOperationException,
            ServiceStateException {

        PermissionCheck.owns(product, supplier, logger, sessionCtx);

        List<CatalogEntry> catalogEntries = product.getCatalogEntries();

        Marketplace marketplace = null;
        if (product.getCatalogEntries().size() > 0) {
            marketplace = catalogEntries.get(0).getMarketplace();
        }

        // 2. if the product is subscription specific, throw an exception
        if (product.getOwningSubscription() != null) {
            ServiceOperationException sof = new ServiceOperationException(
                    Reason.DELETION_FAILED_USED_BY_SUB);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, sof,
                    LogMessageIdentifier.WARN_PRODUCT_DELETION_FAILED_STILL_USED_BY_SUBSCRIPTION,
                    Long.toString(product.getKey()),
                    Long.toString(product.getOwningSubscription().getKey()));
            throw sof;
        }

        // 3. if it's a customer specific copy, delete it
        else if (product.getTemplate() != null
                && product.getTargetCustomer() != null) {
            ProductValidator.validateInactiveOrSuspended(
                    ProductAssembler.getProductId(product),
                    product.getStatus());
            deletePriceModelForCustomer(product);
        }

        // 4. if the product is not a copy, set its status to deleted and delete
        // all customer specific copies that are not used by any subscription
        else {
            ProductValidator.validateInactiveOrSuspended(
                    ProductAssembler.getProductId(product),
                    product.getStatus());
            // check if there are active customer specific services
            long count = countNonSubscriptionCopiesInState(product,
                    EnumSet.of(ServiceStatus.ACTIVE));
            if (count > 0) {
                String expected = ServiceStatus.INACTIVE.name() + ", "
                        + ServiceStatus.SUSPENDED.name();
                throw new ServiceStateException(ServiceStatus.ACTIVE, expected,
                        ProductAssembler.getProductId(product));
            }

            // validate that there are no resale permissions for the MS
            validateNotExistingResalePermissions(product);

            product.setStatus(ServiceStatus.DELETED);
            // rename to allow reuse of the id
            product.setProductId(product.getProductId() + "#"
                    + String.valueOf(System.currentTimeMillis()));

            // now delete all it's customer specific copies
            List<Product> customerSpecificProductCopies = getCustomerSpecificProductCopies(
                    product);
            for (Product copy : customerSpecificProductCopies) {
                dm.remove(copy);
            }
        }

        if (marketplace != null) {
            landingpageService.removeProductFromLandingpage(marketplace,
                    product);
        }

    }

    void deletePriceModelForCustomer(Product product) {
        priceModelAudit.deletePriceModel(dm, product.getPriceModel());
        dm.remove(product);
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public boolean statusAllowsDeletion(VOService service)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException {
        ArgumentValidator.notNull("service", service);
        Product productToDelete;

        productToDelete = dm.getReference(Product.class, service.getKey());
        Organization supplier = dm.getCurrentUser().getOrganization();
        PermissionCheck.owns(productToDelete, supplier, logger, sessionCtx);
        BaseAssembler.verifyVersionAndKey(productToDelete, service);
        Set<ServiceStatus> invalidSates = EnumSet.of(ServiceStatus.ACTIVE,
                ServiceStatus.DELETED, ServiceStatus.OBSOLETE);
        boolean rc = !invalidSates.contains(productToDelete.getStatus());
        if (rc) {
            long count = countNonSubscriptionCopiesInState(productToDelete,
                    invalidSates);
            rc = count == 0;
        }
        return rc;
    }

    private long countNonSubscriptionCopiesInState(Product template,
            Set<ServiceStatus> states) {
        Query query = dm.createNamedQuery(
                "Product.countCustomerCopiesForTemplateInState");
        query.setParameter("template", template);
        query.setParameter("status", states);
        Long count = (Long) query.getSingleResult();
        return count.longValue();
    }

    /**
     * Check if there are resale permissions for the given product template
     * 
     * @param template
     *            The product template
     */
    void validateNotExistingResalePermissions(Product template)
            throws ServiceOperationException {

        Query query = dm.createNamedQuery(
                "Product.getPartnerCopiesForTemplateNotInState");
        query.setParameter("template", template);
        query.setParameter("statusToIgnore", ServiceStatus.DELETED);
        @SuppressWarnings("unchecked")
        List<Product> result = query.getResultList();

        if (result.size() > 0) {
            ServiceOperationException sof = new ServiceOperationException(
                    Reason.DELETION_FAILED_EXISTING_RESALE_PERMISSION);
            logger.logError(Log4jLogger.SYSTEM_LOG, sof,
                    LogMessageIdentifier.ERROR_SERVICE_DELETION_FAILED_EXISTING_RESALE_PERMISSION,
                    Long.toString(template.getKey()));
            throw sof;
        }

    }

    /**
     * Returns all customer specific product copies, that are not used in any
     * subscription.
     * 
     * @param template
     *            The product to retrieve the copies for.
     * @return The list of customer specific products.
     */
    private List<Product> getCustomerSpecificProductCopies(Product template) {

        Query query = dm.createNamedQuery("Product.getCustomerCopies");
        query.setParameter("template", template);
        List<Product> result = ParameterizedTypes.list(query.getResultList(),
                Product.class);

        return result;
    }

    /**
     * Returns the platform events defined in the system. No platform events are
     * available if the specified technical service is defined for the
     * <code>DIRECT</code> or <code>USER</code> access type.
     * 
     * @param tp
     *            The technical product to retrieve the events for.
     * 
     * @return The platform events.
     */
    List<Event> getPlatformEvents(TechnicalProduct tp) {

        List<Event> result = new ArrayList<>();
        Query query = dm.createNamedQuery("Event.getAllPlatformEvents");
        query.setParameter("eventType", EventType.PLATFORM_EVENT);
        result = ParameterizedTypes.list(query.getResultList(), Event.class);
        if (tp.getAccessType() == ServiceAccessType.DIRECT
                || tp.getAccessType() == ServiceAccessType.USER) {
            List<Event> copy = new ArrayList<>(result);
            for (Event ed : copy) {
                if (EventType.PLATFORM_EVENT == ed.getEventType()) {
                    result.remove(ed);
                }
            }
        }

        return result;
    }

    /**
     * Reads the platform parameters and removes
     * <ul>
     * <li>the user related ones for technical products of type
     * <code>DIRECT</code> access</li>
     * <li>the maximum number of concurrent user per subscription for technical
     * products of type <code>USER</code> access</li>
     * </ul>
     * 
     * @param tp
     *            the technical product to get the platform parameters for
     * @return the list of platform parameters for the technical product
     */
    private List<ParameterDefinition> getPlatformParameterDefinitions(
            TechnicalProduct tp) {

        Query query = dm.createNamedQuery(
                "ParameterDefinition.getAllPlatformParameterDefinitions");
        query.setParameter("parameterType", ParameterType.PLATFORM_PARAMETER);
        List<ParameterDefinition> result = ParameterizedTypes
                .list(query.getResultList(), ParameterDefinition.class);
        if (tp.getAccessType() == ServiceAccessType.DIRECT
                || tp.getAccessType() == ServiceAccessType.USER) {
            List<ParameterDefinition> copy = new ArrayList<>(result);
            for (ParameterDefinition pd : copy) {
                if (PlatformParameterIdentifiers.CONCURRENT_USER
                        .equals(pd.getParameterId())
                        || (tp.getAccessType() == ServiceAccessType.DIRECT
                                && PlatformParameterIdentifiers.NAMED_USER
                                        .equals(pd.getParameterId()))) {
                    result.remove(pd);
                }
            }
        }

        return result;
    }

    /**
     * Reads the product result list from the given query's single result. If no
     * result is found, <code>null</code> is returned. If the result is not
     * unique, a system exception will be thrown.
     * 
     * @param cust
     *            The customer the product is requested for.
     * @param query
     *            The query object.
     * @return The product returned by the query.
     */
    private Product getProductFromQueryResult(Organization cust, Query query) {
        Product customerProduct = null;
        try {
            customerProduct = (Product) query.getSingleResult();
        } catch (NoResultException e) {
            // product was not found, so return null
            return null;
        } catch (NonUniqueResultException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "Product retrieval failed due to invalid result, found duplicate entry.");
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_RETRIEVAL_CUSTOMER_OR_SUBSCRIPTION_FAILED_RESULT_NOT_UNIQUE,
                    Long.toString(cust.getKey()));
            throw sse;
        }
        return customerProduct;
    }

    @Override
    public VOServiceLocalization getServiceLocalization(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);
        return spsLocalizer.getServiceLocalization(
                dm.getReference(Product.class, service.getKey()));
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER" })
    public void saveServiceLocalization(VOService service,
            VOServiceLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notNull("localization", localization);

        spsLocalizer.saveServiceLocalization(service.getKey(), localization);
    }

    @Override
    public VOPriceModelLocalization getPriceModelLocalization(
            VOPriceModel priceModel)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("priceModel", priceModel);

        if (!dm.getCurrentUser().getOrganization().getGrantedRoleTypes()
                .contains(OrganizationRoleType.SUPPLIER)) {
            throw new OperationNotPermittedException(
                    "only suppliers are allowed to call this method.");
        }
        PriceModel pm = dm.getReference(PriceModel.class, priceModel.getKey());
        if (pm.getProduct() == null) {
            throw new OperationNotPermittedException(
                    "price model has no product assigned.");
        }
        return spsLocalizer.getPriceModelLocalization(pm.getProduct().getKey());
    }

    @Override
    public List<VOLocalizedText> getPriceModelLicenseTemplateLocalization(
            VOServiceDetails service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);

        if (!spsLocalizer
                .checkIsAllowedForLocalizingService(service.getKey())) {
            throw new OperationNotPermittedException(
                    "No rights for getting price model localizations for license.");
        }

        List<VOLocalizedText> templates = localizer.getLocalizedValues(
                service.getTechnicalService().getKey(),
                LocalizedObjectTypes.PRODUCT_LICENSE_DESC);

        return templates;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER" })
    public void savePriceModelLocalization(VOPriceModel priceModel,
            VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("priceModel", priceModel);
        ArgumentValidator.notNull("localization", localization);

        spsLocalizer.savePriceModelLocalizationForSupplier(priceModel.getKey(),
                priceModel.isChargeable(), localization);
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public List<VOService> getServicesForCustomer(VOOrganization customer)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("customer", customer);
        PlatformUser user = dm.getCurrentUser();
        Organization cust = dm.getReference(Organization.class,
                customer.getKey());

        List<Product> list = getCustomerSpecificProducts(cust,
                user.getOrganization());

        LocalizerFacade facade = new LocalizerFacade(localizer,
                user.getLocale());
        List<VOService> voList = new ArrayList<>();
        for (Product product : list) {
            voList.add(ProductAssembler.toVOProduct(product, facade));
        }

        return voList;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<Product> getCustomerSpecificProducts(Organization cust,
            Organization seller) throws OperationNotPermittedException {

        PermissionCheck.supplierOfCustomer(seller, cust, logger, sessionCtx);
        Query query = dm
                .createNamedQuery("Product.getCustomerSpecificProducts");
        query.setParameter("vendorKey", seller.getKey());
        query.setParameter("customer", cust);
        List<Product> list = ParameterizedTypes.list(query.getResultList(),
                Product.class);

        return list;
    }

    @Override
    public List<String> getSupportedCurrencies() {

        Query query = dm.createNamedQuery("SupportedCurrency.getAll");
        List<String> curlist = new ArrayList<>();
        for (SupportedCurrency curr : ParameterizedTypes
                .iterable(query.getResultList(), SupportedCurrency.class)) {
            curlist.add(curr.getCurrencyISOCode());
        }

        return curlist;

    }

    @Override
    public VOImageResource loadImage(Long serviceKey) {

        ArgumentValidator.notNull("serviceKey", serviceKey);

        VOImageResource vo = null;

        Product product = dm.find(Product.class, serviceKey);

        if (product != null) {
            ImageResource imageResource = irm.read(product.getKey(),
                    ImageType.SERVICE_IMAGE);
            while (imageResource == null && product.getTemplate() != null) {
                product = product.getTemplate();
                imageResource = irm.read(product.getKey(),
                        ImageType.SERVICE_IMAGE);
            }
            if (imageResource != null) {
                vo = new VOImageResource();
                vo.setBuffer(imageResource.getBuffer());
                vo.setContentType(imageResource.getContentType());
                vo.setImageType(ImageType.SERVICE_IMAGE);
            }
        }

        return vo;
    }

    @Override
    public VOImageResource loadImageForSupplier(String serviceId,
            String supplierId) throws ObjectNotFoundException {

        ArgumentValidator.notNull("serviceId", serviceId);
        ArgumentValidator.notNull("supplierId", supplierId);

        VOImageResource vo = null;

        Product product = new Product();
        product.setProductId(serviceId);
        Organization supplier = new Organization();
        supplier.setOrganizationId(supplierId);
        // looking for supplier by id
        supplier = (Organization) dm.getReferenceByBusinessKey(supplier);
        product.setVendor(supplier);
        product = (Product) dm.find(product);

        if (product != null) {
            ImageResource imageResource = irm.read(product.getKey(),
                    ImageType.SERVICE_IMAGE);
            if (imageResource != null) {
                vo = new VOImageResource();
                vo.setBuffer(imageResource.getBuffer());
                vo.setContentType(imageResource.getContentType());
                vo.setImageType(ImageType.SERVICE_IMAGE);
            }
        }

        return vo;
    }

    /**
     * Save or delete the images resource in/from the database
     * 
     * @param productKey
     *            the product key
     * @param voImageResource
     *            the image resource to store/delete
     */
    private void processImage(long productKey, VOImageResource voImageResource)
            throws ValidationException {

        if (voImageResource == null) {

            return;
        }

        if (voImageResource.getImageType() == null || voImageResource
                .getImageType().getOwnerType() != ImageOwnerType.SERVICE) {
            SaaSSystemException se = new SaaSSystemException(
                    "Only images belonging to a product can be saved.");
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_IMAGES_NOT_BELONG_TO_PRODUCT);
            throw se;
        }

        boolean isImageDeleted = voImageResource.getBuffer() == null;
        if (isImageDeleted) {
            irm.delete(productKey, voImageResource.getImageType());
        } else {

            try {
                ImageValidator.validate(voImageResource.getBuffer(),
                        voImageResource.getContentType(), 80, 80, 80, 80);
            } catch (ValidationException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }

            ImageResource imageResource = new ImageResource();
            imageResource.setObjectKey(productKey);
            imageResource.setContentType(voImageResource.getContentType());
            imageResource.setBuffer(voImageResource.getBuffer());
            imageResource.setImageType(voImageResource.getImageType());
            irm.save(imageResource);
        }

    }

    /**
     * Returns true if an image is defined for the given product.
     */
    public boolean isImageDefined(Product product) {
        boolean flag = irm.read(product.getKey(),
                ImageType.SERVICE_IMAGE) != null;
        return flag;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public byte[] exportTechnicalServices(
            List<VOTechnicalService> technicalServices)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException {

        ArgumentValidator.notNull("technicalServices", technicalServices);
        Organization provider = dm.getCurrentUser().getOrganization();
        List<TechnicalProduct> toExport = new ArrayList<>();
        for (VOTechnicalService product : technicalServices) {
            TechnicalProduct techProd = findTechnicalProductAndCheckOwner(
                    provider, product);
            toExport.add(techProd);
        }
        TechnicalProductXmlConverter converter = new TechnicalProductXmlConverter();
        byte[] xml = converter.technicalProductToXml(toExport, localizer, dm);
        return xml;
    }

    private void setPricedParametersForPriceModel(long voPriceModelKey,
            PriceModel priceModel, List<PricedParameter> parametersToSet,
            List<VOPricedParameter> selectedParameters,
            boolean priceModelCreatedInTransaction, Organization targetCustomer)
            throws ValidationException, ConcurrentModificationException,
            OperationNotPermittedException {

        if (parametersToSet.isEmpty()) {
            // no parameter is specified, so remove all existing one
            for (PricedParameter pricedParameter : priceModel
                    .getSelectedParameters()) {
                dm.remove(pricedParameter);
            }
            priceModel.setSelectedParameters(parametersToSet);
        } else {
            List<PricedParameter> parametersToBeStored = new ArrayList<>();
            Map<String, PricedParameter> curParameters = new HashMap<>();
            for (PricedParameter pp : priceModel.getSelectedParameters()) {
                if (pp.getParameter().getParameterDefinition()
                        .getValueType() == ParameterValueType.ENUMERATION
                        && pp.getPricedOptionList().isEmpty()) {
                    // remove the enumeration parameters that have no options -
                    // should never happen
                    dm.remove(pp);
                } else {
                    // fill the map
                    curParameters.put(pp.getParameter().getParameterDefinition()
                            .getParameterId(), pp);
                }
            }
            for (PricedParameter parameterToSet : parametersToSet) {

                String parameterId = parameterToSet.getParameter()
                        .getParameterDefinition().getParameterId();
                PricedParameter pricedParameter = curParameters
                        .get(parameterId);
                if (pricedParameter != null) {
                    pricedParameter.setPricePerSubscription(
                            parameterToSet.getPricePerSubscription());
                    pricedParameter
                            .setPricePerUser(parameterToSet.getPricePerUser());

                    ParameterOptionAssembler.updatePriceOptions(pricedParameter,
                            parameterToSet);

                    curParameters.remove(pricedParameter.getParameter()
                            .getParameterDefinition().getParameterId());
                    parametersToBeStored.add(pricedParameter);
                } else {
                    pricedParameter = parameterToSet;
                    parametersToBeStored.add(parameterToSet);
                }

                // handle priced product roles for the current parameter
                VOPricedParameter voPP = getPricedParamForParamId(
                        selectedParameters, parameterId);
                setRoleSpecificPrices(voPriceModelKey, null, pricedParameter,
                        null, voPP.getRoleSpecificUserPrices(),
                        priceModelCreatedInTransaction, targetCustomer, null,
                        null);
            }
            Collection<PricedParameter> obsoletePricedParameters = curParameters
                    .values();
            for (PricedParameter obsoleteParameter : obsoletePricedParameters) {
                dm.remove(obsoleteParameter);
            }
            priceModel.setSelectedParameters(parametersToBeStored);
        }

    }

    /**
     * Iterates over the parameters and returns the one with the given
     * identifier.
     * 
     * @param selectedParameters
     *            The list of vo priced parameters to parse.
     * @param parameterId
     *            The parameter identifier to look for.
     * @return The priced parameter with the given identifier.
     */
    private VOPricedParameter getPricedParamForParamId(
            List<VOPricedParameter> selectedParameters, String parameterId) {
        for (VOPricedParameter voPricedParameter : selectedParameters) {
            if (voPricedParameter.getVoParameterDef().getParameterId()
                    .equals(parameterId)) {
                return voPricedParameter;
            }
        }
        // no priced param found, so the data validation did not work correctly.
        // Throw an exception
        SaaSSystemException sse = new SaaSSystemException(
                "Priced parameter input list does not contain a parameter for identifier '"
                        + parameterId + "'");
        logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                LogMessageIdentifier.ERROR_PRICED_PARAMETER_LIST_NOT_CONTAIN_PARAMETER_FOR_ID,
                parameterId);
        throw sse;
    }

    /**
     * Checks if the given parameters to be set for the price model are
     * contained in the parameter list for the product. If they are, a list of
     * consolidated priced parameters is returned. Otherwise an
     * OperationNotPermitted exception will be thrown.
     * 
     * @param voPricedParameters
     *            The parameters to be set for the price model.
     * @param productParams
     *            The current parameter list of the considered product.
     * @param priceModel
     *            The price model the priced parameters should be added to.
     * @param targetCustomer
     *            The targetCustomer of the price model.
     * @param subscription
     *            The subscription of the price model.
     * @param priceModelCreatedInTransaction
     *            Indicates whether the price model was created in this
     *            transaction or not.
     * @param isTemplateExistsForCustomer
     *            Indicates whether price model for service exists as a template
     *            for price model for customer
     * @return A list of priced parameters added to the price model.
     * @throws ValidationException
     * @throws ConcurrentModificationException
     *             Thrown in case the passed value object's version does not
     *             match the domain object.
     * @throws OperationNotPermittedException
     *             Thrown in case the passed value object key does not belong to
     *             a domain object.
     */
    private List<PricedParameter> convertAndValidateParameters(
            long voPriceModelKey, List<VOPricedParameter> voPricedParameters,
            List<Parameter> productParams, PriceModel priceModel,
            boolean priceModelCreatedInTransaction, Organization targetCustomer,
            Subscription subscription, boolean isTemplateExistsForCustomer)
            throws OperationNotPermittedException, ValidationException,
            ConcurrentModificationException {

        Map<Long, PricedParameter> pricedParameterMap = new HashMap<>();
        Map<Parameter, PricedParameter> paramToPricedParam = new HashMap<>();
        for (PricedParameter pricedParameter : priceModel
                .getSelectedParameters()) {
            pricedParameterMap.put(pricedParameter.getKey(),
                    pricedParameter);
            paramToPricedParam.put(pricedParameter.getParameter(),
                    pricedParameter);
        }
        Map<Long, Parameter> parameters = new LinkedHashMap<>();
        for (Parameter parameter : productParams) {
            parameters.put(
                parameter.getParameterDefinition().getKey(),
                    parameter);
        }
        List<PricedParameter> result = new ArrayList<>();
        for (VOPricedParameter voPricedParameter : voPricedParameters) {
            PricedParameterChecks.validateParamDefSet(voPricedParameter);
            long paramDefKey = voPricedParameter.getVoParameterDef().getKey();
            Parameter parameter = parameters.remove(paramDefKey);
            Product product = priceModel.getProduct();
            if (parameter == null) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "Specified parameter '" + voPricedParameter.getKey()
                                + "' for parameter definition'" + paramDefKey
                                + "' is not defined for the current product '"
                                + product.getKey() + "'.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                        LogMessageIdentifier.WARN_PARAMETER_FOR_PRICE_MODEL_INVALID);
                throw onp;
            }
            // only the configured parameter needs to be saved
            if (!parameter.isConfigurable()) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "Priced Parameter lined up to be saved, is not marked as configurable");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                        LogMessageIdentifier.WARN_NOT_CONFIGURABLE_PARAMETER_PASSED_TO_PRICE_MODEL,
                        Long.toString(parameter.getKey()),
                        Long.toString(product.getKey()));
                throw onp;
            }

            // priced parameters can only be based on non-string parameters. So
            // validate the remaining params
            PricedParameterChecks.isValidBaseParam(parameter,
                    voPricedParameter);

            PricedParameter pricedParameter = null;
            if (pricedParameterMap
                    .containsKey(voPricedParameter.getKey())) {
                pricedParameter = pricedParameterMap
                        .remove(voPricedParameter.getKey());
                pricedParameter = handleParameterUpdate(voPricedParameter,
                        pricedParameter, priceModelCreatedInTransaction);
                pricedParameterMap
                        .remove(voPricedParameter.getKey());
            } else {
                PricedParameter existingPricedParam = paramToPricedParam
                        .remove(parameter);
                validateDomainObjectKey(voPricedParameter, existingPricedParam,
                        priceModelCreatedInTransaction);
                pricedParameter = createPricedParameter(voPricedParameter,
                        parameter, priceModel);
            }

            validateAndSetRolePricesForParam(voPriceModelKey, priceModel,
                    voPricedParameter, pricedParameter,
                    priceModelCreatedInTransaction, targetCustomer,
                    subscription, isTemplateExistsForCustomer);
            List<SteppedPrice> steppedPrices = convertAndValidateSteppedPrices(
                    voPriceModelKey, voPricedParameter.getSteppedPrices(),
                    priceModel, null, pricedParameter,
                    priceModelCreatedInTransaction);
            if (!steppedPrices.isEmpty() && BigDecimal.ZERO.compareTo(
                    pricedParameter.getPricePerSubscription()) != 0) {
                ValidationException ve = new ValidationException(
                        ValidationException.ReasonEnum.STEPPED_PARAMETER_PRICING,
                        "pricePerSubscription",
                        new Object[] { pricedParameter.getParameter()
                                .getParameterDefinition().getParameterId() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                        LogMessageIdentifier.WARN_STEPPED_PRICING_MIXED_WITH_BASEPRICE,
                        priceModel.getProduct().getProductId(),
                        "priced parameter",
                        String.valueOf(pricedParameter.getKey()));
                throw ve;
            }
            result.add(pricedParameter);
        }
        removePricedParameters(voPriceModelKey, pricedParameterMap.values(),
                priceModel);

        return result;
    }

    PricedParameter createPricedParameter(VOPricedParameter voPricedParameter,
            Parameter parameter, PriceModel priceModel)
            throws ValidationException {

        PricedParameter pricedParameter = ParameterAssembler
                .toPricedParameter(voPricedParameter);
        pricedParameter.setParameter(parameter);
        pricedParameter.setPriceModel(priceModel);

        priceModelAudit.editParameterSubscriptionPrice(dm, pricedParameter,
                DEFAULT_PRICE_VALUE);
        priceModelAudit.editParameterUserPrice(dm, pricedParameter,
                DEFAULT_PRICE_VALUE);

        for (PricedOption pricedOption : pricedParameter
                .getPricedOptionList()) {
            priceModelAudit.editParameterOptionSubscriptionPrice(dm,
                    pricedOption, DEFAULT_PRICE_VALUE);
            priceModelAudit.editParameterOptionUserPrice(dm, pricedOption,
                    DEFAULT_PRICE_VALUE);
        }

        return pricedParameter;
    }

    void removePricedParameters(long voPriceModelKey,
            Collection<PricedParameter> pricedParameters,
            PriceModel priceModel) {
        for (PricedParameter pricedParameter : pricedParameters) {
            priceModel.getSelectedParameters().remove(pricedParameter);

            priceModelAudit.removeParameterSubscriptionPrice(dm,
                    voPriceModelKey, pricedParameter);

            dm.remove(pricedParameter);
        }
    }

    /**
     * Updates a domain object parameter and all depending priced options
     * according to the passed value object.
     * 
     * @param voPricedParameter
     *            The value object representation of the priced parameter.
     * @param pricedParameter
     *            The priced parameter domain object.
     * @return The updated priced parameter.
     * @throws ValidationException
     * @throws ConcurrentModificationException
     * @throws OperationNotPermittedException
     *             When a key of an priced parameter is set but unknown and the
     *             priceModelCreatedInTransaction flag is not set.
     */
    PricedParameter handleParameterUpdate(VOPricedParameter voPricedParameter,
            PricedParameter pricedParameter,
            boolean priceModelCreatedInTransaction) throws ValidationException,
            ConcurrentModificationException, OperationNotPermittedException {

        updatePricedParameter(voPricedParameter, pricedParameter);

        // helper map for checking if the priced options belong to the priced
        // parameter
        Map<Long, ParameterOption> keyToParamOption = createParameterOptionMap(
                pricedParameter);

        List<VOPricedOption> voPricedOptions = voPricedParameter
                .getPricedOptions();
        List<PricedOption> pricedOptions = pricedParameter
                .getPricedOptionList();
        List<PricedOption> resultList = new ArrayList<>();
        Map<Long, PricedOption> storedOptionsMap = new HashMap<>();
        for (PricedOption pricedOption : pricedOptions) {
            storedOptionsMap.put(pricedOption.getKey(),
                    pricedOption);
        }

        for (VOPricedOption voPricedOption : voPricedOptions) {
            ParameterOption paramOption = keyToParamOption.remove(
                voPricedOption.getParameterOptionKey());
            if (paramOption == null) {
                OperationNotPermittedException onp = new OperationNotPermittedException(
                        "No ParameterOption found for PricedOption value object.");
                logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                        LogMessageIdentifier.WARN_PRICEDOPTION_NO_OPTION_DEFINED);
                throw onp;
            }
            // new option
            PricedOption pricedOption = null;
            if (!storedOptionsMap
                    .containsKey(voPricedOption.getKey())) {
                if (!priceModelCreatedInTransaction
                        && voPricedOption.getKey() != 0) {
                    OperationNotPermittedException onp = new OperationNotPermittedException(
                            "Priced option value object with invalid key.");
                    logger.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                            LogMessageIdentifier.WARN_PRICED_OPTION_WITH_INVALID_KEY);
                    throw onp;
                }
                pricedOption = createPricedOption(voPricedOption,
                        pricedParameter);
            } else {
                pricedOption = storedOptionsMap
                        .remove(voPricedOption.getKey());
                updatePricedOption(voPricedOption, pricedOption);

            }
            resultList.add(pricedOption);
        }

        // set new list to delete references before entities removed
        pricedParameter.setPricedOptionList(resultList);
        removePricedOptions(storedOptionsMap.values());

        return pricedParameter;
    }

    void updatePricedParameter(VOPricedParameter voPricedParameter,
            PricedParameter pricedParameter)
            throws ValidationException, ConcurrentModificationException {

        BigDecimal oldSubPrice = pricedParameter.getPricePerSubscription();
        BigDecimal oldUserPrice = pricedParameter.getPricePerUser();
        pricedParameter = ParameterAssembler
                .updatePricedParameter(voPricedParameter, pricedParameter);

        priceModelAudit.editParameterSubscriptionPrice(dm, pricedParameter,
                oldSubPrice);
        priceModelAudit.editParameterUserPrice(dm, pricedParameter,
                oldUserPrice);
    }

    PricedOption createPricedOption(VOPricedOption voPricedOption,
            PricedParameter pricedParameter) {
        PricedOption pricedOption = PricedOptionAssembler
                .toPricedOption(voPricedOption, pricedParameter);

        priceModelAudit.editParameterOptionSubscriptionPrice(dm, pricedOption,
                DEFAULT_PRICE_VALUE);
        priceModelAudit.editParameterOptionUserPrice(dm, pricedOption,
                DEFAULT_PRICE_VALUE);
        return pricedOption;
    }

    void updatePricedOption(VOPricedOption voPricedOption,
            PricedOption pricedOption)
            throws ConcurrentModificationException, ValidationException {

        BigDecimal oldPOSubPrice = pricedOption.getPricePerSubscription();
        BigDecimal oldPOUserPrice = pricedOption.getPricePerUser();
        PricedOptionAssembler.updatePricedOption(pricedOption, voPricedOption);

        priceModelAudit.editParameterOptionSubscriptionPrice(dm, pricedOption,
                oldPOSubPrice);
        priceModelAudit.editParameterOptionUserPrice(dm, pricedOption,
                oldPOUserPrice);
    }

    void removePricedOptions(Collection<PricedOption> pricedOptions) {
        for (PricedOption pricedOption : pricedOptions) {
            dm.remove(pricedOption);
        }
    }

    Map<Long, ParameterOption> createParameterOptionMap(
            PricedParameter pricedParameter) {
        Map<Long, ParameterOption> keyToParamOption = new HashMap<>();
        for (ParameterOption parameterOption : pricedParameter.getParameter()
                .getParameterDefinition().getOptionList()) {
            keyToParamOption.put(parameterOption.getKey(),
                    parameterOption);
        }
        return keyToParamOption;
    }

    /**
     * Validates and sets the priced product role settings for the parameter and
     * parameter options.
     * 
     * @param priceModel
     *            The price model that should be modified.
     * @param voPP
     *            The value object representation of the priced parameter.
     * @param pp
     *            The domain object representation of the priced parameter.
     * @param targetCustomer
     *            The targetCustomer of the price model.
     * @param subscription
     *            The subscription of the price model.
     * @param priceModelCreatedInTransaction
     *            Indicates whether the price model was created in this
     *            transaction or not.
     * @param isTemplateExistsForCustomer
     *            Indicates whether price model for service exists as a template
     *            for price model for customer
     * @throws ValidationException
     * @throws OperationNotPermittedException
     * @throws ConcurrentModificationException
     *             Thrown in case the priced role value object version does not
     *             match the domain object's one.
     */
    void validateAndSetRolePricesForParam(long voPriceModelKey,
            PriceModel priceModel, VOPricedParameter voPP, PricedParameter pp,
            boolean priceModelCreatedInTransaction, Organization targetCustomer,
            Subscription subscription, boolean isTemplateExistsForCustomer)
            throws ValidationException, OperationNotPermittedException,
            ConcurrentModificationException {

        Map<Long, List<PricedProductRole>> oldPricedOptionMap = new HashMap<>();
        if (isTemplateExistsForCustomer) {
            oldPricedOptionMap = prepareOldPricedOptionMap(priceModel);
        }
        List<VOPricedOption> pricedOptions = voPP.getPricedOptions();
        for (VOPricedOption pricedOption : pricedOptions) {
            validatePricedProductRoles(pricedOption.getRoleSpecificUserPrices(),
                    priceModel.getProduct());

            // find corresponding domain object representation for the
            // priced option and save the priced product role information to
            // it
            for (PricedOption po : pp.getPricedOptionList()) {
                if (po.getParameterOptionKey() == pricedOption
                        .getParameterOptionKey()) {
                    if (isTemplateExistsForCustomer) {
                        setRoleSpecificPrices(voPriceModelKey, null, null, po,
                                pricedOption.getRoleSpecificUserPrices(),
                                priceModelCreatedInTransaction, targetCustomer,
                                subscription, oldPricedOptionMap.get(po.getParameterOptionKey()));

                    } else {
                        setRoleSpecificPrices(voPriceModelKey, null, null, po,
                                pricedOption.getRoleSpecificUserPrices(),
                                priceModelCreatedInTransaction, targetCustomer,
                                subscription, null);
                    }

                }
            }
        }
        validatePricedProductRoles(voPP.getRoleSpecificUserPrices(),
                priceModel.getProduct());
        setRoleSpecificPrices(voPriceModelKey, null, pp, null,
                voPP.getRoleSpecificUserPrices(),
                priceModelCreatedInTransaction, targetCustomer, subscription,
                null);
    }

    private Map<Long, List<PricedProductRole>> prepareOldPricedOptionMap(
            PriceModel priceModel) {
        Map<Long, List<PricedProductRole>> oldPricedOptionMap = new HashMap<>();
        List<PricedParameter> pricedParameters = priceModel
                .getSelectedParameters();
        if (pricedParameters != null) {
            for (PricedParameter pricedParameter : pricedParameters) {
                List<PricedOption> pricedOptions = pricedParameter
                        .getPricedOptionList();
                if (pricedOptions != null) {
                    for (PricedOption pricedOption : pricedOptions) {
                        oldPricedOptionMap.put(
                            pricedOption.getParameterOptionKey(),
                                pricedOption.getRoleSpecificUserPrices());
                    }
                }
            }
        }
        return oldPricedOptionMap;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public VOTechnicalService createTechnicalService(
            VOTechnicalService technicalService)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException {

        ArgumentValidator.notNull("technicalService", technicalService);

        Organization org = dm.getCurrentUser().getOrganization();

        String billingId = technicalService.getBillingIdentifier();

        if (billingId == null || billingId.trim().length() == 0) {
            billingId = billingAdapterLocalBean.getDefaultBillingIdentifier();
            technicalService.setBillingIdentifier(billingId);
        }

        TechnicalProduct domObj = TechnicalProductAssembler
                .toTechnicalProduct(technicalService);
        domObj.setOrganization(org);
        dm.persist(domObj);
        dm.flush();

        String locale = dm.getCurrentUser().getLocale();

        String license = technicalService.getLicense();
        if (license == null) {
            license = "";
        }
        localizer.storeLocalizedResource(locale, domObj.getKey(),
                LocalizedObjectTypes.PRODUCT_LICENSE_DESC, license);
        String description = technicalService.getTechnicalServiceDescription();
        if (description == null) {
            description = "";
        }
        localizer.storeLocalizedResource(locale, domObj.getKey(),
                LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC, description);
        String accessInfo = technicalService.getAccessInfo();
        if (accessInfo == null) {
            accessInfo = "";
        }
        ServiceAccessType accessType = technicalService.getAccessType();
        if (accessType == ServiceAccessType.DIRECT
                || accessType == ServiceAccessType.USER) {
            localizer.storeLocalizedResource("en", domObj.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                    accessInfo);
        } else {
            localizer.storeLocalizedResource(locale, domObj.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                    accessInfo);
        }

        // Store defined localized tags of this service
        tagService.updateTags(domObj, locale,
                TagAssembler.toTags(technicalService.getTags(), locale));

        // if the organization is supplier and technology provider, it must be
        // able to use its own products, so register it as supplier for itself.
        if (org.hasRole(OrganizationRoleType.SUPPLIER)) {
            try {
                ms.addMarketingPermission(org, domObj.getKey(),
                        Collections.singletonList(org.getOrganizationId()));
            } catch (ObjectNotFoundException e) {
                // should not happen here
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_ADDED,
                        String.valueOf(domObj.getKey()),
                        org.getOrganizationId());
            } catch (AddMarketingPermissionException e) {
                // should not happen here
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_MARKETING_PERMISSION_NOT_ADDED,
                        String.valueOf(domObj.getKey()),
                        org.getOrganizationId());
            }
        }

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        List<ParameterDefinition> paramDefs = getPlatformParameterDefinitions(
                domObj);
        List<Event> platformEvents = getPlatformEvents(domObj);
        VOTechnicalService result = TechnicalProductAssembler
                .toVOTechnicalProduct(domObj, paramDefs, platformEvents, facade,
                        false);

        return result;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void saveTechnicalServiceLocalization(
            VOTechnicalService technicalService)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UpdateConstraintException, ValidationException {

        ArgumentValidator.notNull("technicalService", technicalService);

        Organization provider = dm.getCurrentUser().getOrganization();
        TechnicalProduct techProd = findTechnicalProductAndCheckOwner(provider,
                technicalService);
        String locale = dm.getCurrentUser().getLocale();
        checkLicenseConstrainsAndStore(techProd, locale,
                technicalService.getLicense());
        String description = technicalService.getTechnicalServiceDescription();
        if (description != null) {
            localizer.storeLocalizedResource(locale, technicalService.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_TECHNICAL_DESC,
                    description);
        }
        String accessInfo = technicalService.getAccessInfo();
        if (accessInfo != null) {
            createAccessInfoForDefaultLocale(locale, "en", technicalService,
                    accessInfo);
            localizer.storeLocalizedResource(locale, technicalService.getKey(),
                    LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                    accessInfo);
        }
        List<VORoleDefinition> roles = technicalService.getRoleDefinitions();
        for (VORoleDefinition role : roles) {
            RoleDefinition roleDef = new RoleDefinition();
            roleDef.setRoleId(role.getRoleId());
            roleDef.setTechnicalProduct(techProd);
            RoleDefinition r = (RoleDefinition) dm
                    .getReferenceByBusinessKey(roleDef);
            String name = role.getName();
            if (name != null) {
                BLValidator.isName("role definition name", name, false);
                localizer.storeLocalizedResource(locale, r.getKey(),
                        LocalizedObjectTypes.ROLE_DEF_NAME, name);
            }
            String desc = role.getDescription();
            if (desc != null) {
                localizer.storeLocalizedResource(locale, r.getKey(),
                        LocalizedObjectTypes.ROLE_DEF_DESC, desc);
            }
        }
        List<VOTechnicalServiceOperation> ops = technicalService
                .getTechnicalServiceOperations();
        for (VOTechnicalServiceOperation op : ops) {
            TechnicalProductOperation operation = new TechnicalProductOperation();
            operation.setOperationId(op.getOperationId());
            operation.setTechnicalProduct(techProd);
            TechnicalProductOperation tpo = (TechnicalProductOperation) dm
                    .getReferenceByBusinessKey(operation);
            String name = op.getOperationName();
            if (name != null) {
                BLValidator.isName("technical service operation name", name,
                        false);
                localizer.storeLocalizedResource(locale, tpo.getKey(),
                        LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME,
                        name);
            }
            String desc = op.getOperationDescription();
            if (desc != null) {
                localizer.storeLocalizedResource(locale, tpo.getKey(),
                        LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION,
                        desc);
            }
            List<VOServiceOperationParameter> params = op
                    .getOperationParameters();
            for (VOServiceOperationParameter p : params) {
                String parameterName = p.getParameterName();
                if (parameterName != null) {
                    OperationParameter operationParameter = new OperationParameter();
                    operationParameter.setId(p.getParameterId());
                    operationParameter.setTechnicalProductOperation(tpo);
                    OperationParameter param = (OperationParameter) dm
                            .getReferenceByBusinessKey(operationParameter);
                    BLValidator.isName(
                            "technical service operation parameter name",
                            parameterName, false);
                    localizer.storeLocalizedResource(locale, param.getKey(),
                            LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME,
                            parameterName);
                }
            }
        }
        List<VOEventDefinition> events = technicalService.getEventDefinitions();
        for (VOEventDefinition event : events) {
            String eventDescription = event.getEventDescription();
            if (eventDescription != null) {
                if (event.getEventType() != EventType.PLATFORM_EVENT) {
                    Event eventObj = new Event();
                    eventObj.setEventIdentifier(event.getEventId());
                    eventObj.setEventType(event.getEventType());
                    eventObj.setTechnicalProduct(techProd);
                    Event e = (Event) dm.getReferenceByBusinessKey(eventObj);
                    // only service events can be modified
                    localizer.storeLocalizedResource(locale, e.getKey(),
                            LocalizedObjectTypes.EVENT_DESC, eventDescription);
                }
            }
        }
        List<VOParameterDefinition> parameters = technicalService
                .getParameterDefinitions();
        for (VOParameterDefinition parameter : parameters) {
            String parameterDescription = parameter.getDescription();
            if (parameter
                    .getParameterType() != ParameterType.PLATFORM_PARAMETER) {
                ParameterDefinition parameterDef = new ParameterDefinition();
                parameterDef.setParameterId(parameter.getParameterId());
                parameterDef.setParameterType(parameter.getParameterType());
                parameterDef.setTechnicalProduct(techProd);
                ParameterDefinition p = (ParameterDefinition) dm
                        .getReferenceByBusinessKey(parameterDef);

                // only service parameters can be modified
                if (parameterDescription != null) {
                    localizer.storeLocalizedResource(locale, p.getKey(),
                            LocalizedObjectTypes.PARAMETER_DEF_DESC,
                            parameterDescription);

                }
                List<VOParameterOption> options = parameter
                        .getParameterOptions();
                for (VOParameterOption option : options) {
                    String optionDescription = option.getOptionDescription();
                    if (optionDescription != null) {
                        ParameterOption parameterOption = new ParameterOption();
                        parameterOption.setOptionId(option.getOptionId());
                        parameterOption.setParameterDefinition(p);
                        ParameterOption o = (ParameterOption) dm
                                .getReferenceByBusinessKey(parameterOption);
                        localizer.storeLocalizedResource(locale, o.getKey(),
                                LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC,
                                optionDescription);
                    }
                }
            }
        }
        List<Tag> tags = TagAssembler.toTags(technicalService.getTags(),
                locale);
        tagService.updateTags(techProd, locale, tags);

    }

    private void createAccessInfoForDefaultLocale(String userLocale,
            String defaultLocale, VOTechnicalService technicalService,
            String accessInfo) {
        ServiceAccessType accessType = technicalService.getAccessType();
        if (!userLocale.equals(defaultLocale)
                && (accessType == ServiceAccessType.DIRECT
                        || accessType == ServiceAccessType.USER)) {
            String accessInfoDefaultLocale = localizer
                    .getLocalizedTextFromDatabase(defaultLocale,
                            technicalService.getKey(),
                            LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
            if (accessInfoDefaultLocale == null
                    || accessInfoDefaultLocale.trim().length() <= 0) {
                localizer.storeLocalizedResource(defaultLocale,
                        technicalService.getKey(),
                        LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                        accessInfo);
            }
        }
    }

    /**
     * Checks if the new license of the technical product can be changed. Saving
     * is done when the technical product doesn't have a license in the provided
     * locale or if no marketing products exist based on the technical product.
     * In case the new license isn't set or equals the existing one, it isn't
     * saved.
     * 
     * @param tp
     *            the technical product value object to get the new license from
     * @param locale
     *            the locale that is used for saving the license
     * @param license
     *            The license for the technical product.
     */
    private void checkLicenseConstrainsAndStore(TechnicalProduct tp,
            String locale, String license) {
        if (license == null) {
            return;
        }
        List<VOLocalizedText> localizedValues = localizer.getLocalizedValues(
                tp.getKey(), LocalizedObjectTypes.PRODUCT_LICENSE_DESC);

        String storedLicense = null;
        for (VOLocalizedText text : localizedValues) {
            if (text.getLocale().equals(locale)) {
                storedLicense = text.getText();
            }
        }

        ProductLicenseValidator.validate(tp, storedLicense, license);
        // save in DB only if they are different
        if (!Strings.areStringsEqual(storedLicense, license)) {
            localizer.storeLocalizedResource(locale, tp.getKey(),
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC, license);
        }
    }

    /**
     * Tries to find the technical product represented by the given value object
     * and check if the provided organization is the owner of it.
     * 
     * @param provider
     *            the technology provider
     * @param voTechnicalProduct
     *            the value object to find the domain object for
     * @return the {@link TechnicalProduct}
     * @throws ObjectNotFoundException
     *             in case the technical product wasn't found
     * @throws OperationNotPermittedException
     *             in case the provided organization is not owner of the
     *             technical product
     */
    private TechnicalProduct findTechnicalProductAndCheckOwner(
            Organization provider, VOTechnicalService voTechnicalProduct)
            throws ObjectNotFoundException, OperationNotPermittedException {
        TechnicalProduct techProd = dm.getReference(TechnicalProduct.class,
                voTechnicalProduct.getKey());
        PermissionCheck.owns(techProd, provider, logger, sessionCtx);
        return techProd;
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOCustomerService> getAllCustomerSpecificServices()
            throws OrganizationAuthoritiesException {
        Organization organization = dm.getCurrentUser().getOrganization();
        Query query = dm
                .createNamedQuery("Product.getCustomerProductsForVendor");
        query.setParameter("vendorKey", organization.getKey());
        List<Product> products = ParameterizedTypes.list(query.getResultList(),
                Product.class);
        List<VOCustomerService> result = new ArrayList<>();
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        Set<ServiceStatus> states = EnumSet.of(ServiceStatus.ACTIVE,
                ServiceStatus.INACTIVE, ServiceStatus.SUSPENDED);
        for (Product product : products) {
            if (states.contains(product.getStatus())) {
                result.add(
                        ProductAssembler.toVOCustomerProduct(product, facade));
            }
        }
        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public VOServiceDetails copyService(VOService service, String serviceId)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ServiceStateException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            ValidationException {

        ArgumentValidator.notNull("service", service);
        Organization org = dm.getCurrentUser().getOrganization();
        BLValidator.isId("serviceId", serviceId, true);
        Product product = dm.getReference(Product.class, service.getKey());
        PermissionCheck.owns(product, org, logger, sessionCtx);
        if (product.getOwningSubscription() != null
                || product.getTargetCustomer() != null) {
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    "Copying the service failed");
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, onp,
                    LogMessageIdentifier.WARN_COPY_SERVICE_FAILED_NOT_GLOBAL_TEMPLATE,
                    Long.toString(org.getKey()),
                    Long.toString(product.getKey()));
            throw onp;
        }
        ServiceStatus status = product.getStatus();
        if (status != ServiceStatus.ACTIVE
                && status != ServiceStatus.INACTIVE) {
            ServiceStateException sse = new ServiceStateException(status,
                    ServiceStatus.ACTIVE.name() + ", "
                            + ServiceStatus.INACTIVE.name(),
                    ProductAssembler.getProductId(product));
            logger.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.WARN_COPY_SERVICE_FAILED_INVALID_STATE,
                    Long.toString(org.getKey()),
                    Long.toString(product.getKey()));
            throw sse;
        }
        BaseAssembler.verifyVersionAndKey(product, service);
        // check uniqueness of new service id
        try {
            validateChangedId(serviceId, org);
        } catch (NonUniqueBusinessKeyException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        Product copy = product.copyTemplate(serviceId);
        dm.persist(copy);
        // get the marketplace and the categories for the product to copy
        Marketplace mp = null;
        List<Category> cats = new ArrayList<>();
        if (!product.getCatalogEntries().isEmpty()) {
            CatalogEntry ce = product.getCatalogEntries().get(0);
            mp = ce.getMarketplace();
            for (CategoryToCatalogEntry c : ce.getCategoryToCatalogEntry()) {
                cats.add(c.getCategory());
            }
        }

        // create the catalog entry for the copy with the same marketplace and
        // categories
        CatalogEntry catalogEntry = QueryBasedObjectFactory
                .createCatalogEntry(copy, mp);
        for (Category cat : cats) {
            catalogEntry.addCategory(cat);
        }
        if (product.getCatalogEntries().size() > 0) {
            catalogEntry.setVisibleInCatalog(
                    product.getCatalogEntries().get(0).isVisibleInCatalog());
            catalogEntry.setAnonymousVisible(
                    product.getCatalogEntries().get(0).isAnonymousVisible());
        }

        if (!product.getCatalogEntries().isEmpty()) {
            copyOperatorPriceModel(catalogEntry,
                    product.getCatalogEntries().get(0).getOperatorPriceModel());
        }

        dm.persist(catalogEntry);
        dm.flush();

        // copy the enabled default payment types of the supplier
        copyDefaultPaymentEnablement(copy, org);

        // copy all the localized stuff
        List<VOLocalizedText> locDescs = localizer.getLocalizedValues(
                product.getKey(), LocalizedObjectTypes.PRODUCT_MARKETING_DESC);
        localizer.setLocalizedValues(copy.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_DESC, locDescs);
        List<VOLocalizedText> locShortDescs = localizer.getLocalizedValues(
                product.getKey(),
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION);
        localizer.setLocalizedValues(copy.getKey(),
                LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION, locShortDescs);
        List<VOLocalizedText> locNames = localizer.getLocalizedValues(
                product.getKey(), LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        localizer.setLocalizedValues(copy.getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME, locNames);
        List<VOLocalizedText> locCustomTabNames = localizer.getLocalizedValues(
                product.getKey(), LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME);
        localizer.setLocalizedValues(copy.getKey(),
                LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME,
                locCustomTabNames);

        PriceModel pm = product.getPriceModel();
        if (pm != null) {
            PriceModel pmCopy = copy.getPriceModel();
            List<VOLocalizedText> locPmDescs = localizer.getLocalizedValues(
                    pm.getKey(), LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
            localizer.setLocalizedValues(pmCopy.getKey(),
                    LocalizedObjectTypes.PRICEMODEL_DESCRIPTION, locPmDescs);
            // copy licenses
            copyLicenseInformation(pm, pmCopy);
        }

        // copy the image if existing
        ImageResource imageResource = irm.read(product.getKey(),
                ImageType.SERVICE_IMAGE);
        if (imageResource != null) {
            irm.save(imageResource.copy(copy.getKey()));
        }

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        VOServiceDetails createdProduct = getServiceDetails(copy, facade);

        serviceAudit.copyService(dm, copy, product.getProductId(),
                service.getNameToDisplay());
        return createdProduct;
    }

    /**
     * Copy license information from price model to its copy.
     * 
     * @param source
     *            Original price model.
     * @param destination
     *            Copy price model.
     */
    private void copyLicenseInformation(PriceModel source,
            PriceModel destination) {
        // get local descriptions for technical service and
        // save for marketable service for all languages
        List<VOLocalizedText> licenseDescriptions = localizer
                .getLocalizedValues(source.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE);

        if (licenseDescriptions != null) {
            for (VOLocalizedText localizedText : licenseDescriptions) {
                String license = localizedText.getText();
                String locale = localizedText.getLocale();
                localizer.storeLocalizedResource(locale, destination.getKey(),
                        LocalizedObjectTypes.PRICEMODEL_LICENSE, license);
            }
        }
    }

    @Override
    @RolesAllowed({ "SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER" })
    public List<VOService> setActivationStates(
            List<VOServiceActivation> activations)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("activations", activations);
        List<VOService> resultList = new ArrayList<>();

        ServiceVisibilityCheck visChecker = new ServiceVisibilityCheck(dm);
        for (VOServiceActivation voActivation : activations) {
            VOService result = setActivationState(voActivation, visChecker);
            if (result != null) {
                resultList.add(result);
            }
            dm.flush();
        }

        try {
            visChecker.validate();
        } catch (ServiceOperationException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

        return resultList;
    }

    private VOService setActivationState(VOServiceActivation serviceActivation,
            ServiceVisibilityCheck visChecker) throws ObjectNotFoundException,
            ServiceStateException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ServiceOperationException,
            TechnicalServiceNotAliveException, ServiceNotPublishedException,
            OperationPendingException, ConcurrentModificationException {
        VOService service = serviceActivation.getService();
        boolean active = serviceActivation.isActive();
        List<VOCatalogEntry> catalogEntries = serviceActivation
                .getCatalogEntries();
        return setActivationState(service, active, catalogEntries, visChecker);
    }

    /**
     * Activates or deactivates the given service and updates the visibility
     * flag for all given catalog entries. If a trigger function is defined, the
     * request will be queued.
     */
    private VOService setActivationState(VOService service, boolean activate,
            List<VOCatalogEntry> entries, ServiceVisibilityCheck visChecker)
            throws ServiceStateException, ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException,
            ConcurrentModificationException {

        ArgumentValidator.notNull("service", service);
        PlatformUser currentUser = dm.getCurrentUser();
        Product prod = dm.getReference(Product.class, service.getKey());
        if (activate) {
            validateForProductStatusChange(prod, service, ServiceStatus.ACTIVE,
                    ServiceStatus.INACTIVE, currentUser);
            validateForProductActivation(service);
        } else {
            validateForProductStatusChange(prod, service,
                    ServiceStatus.INACTIVE, ServiceStatus.ACTIVE, currentUser);
        }

        TriggerProcessValidator triggerProcessValidator = new TriggerProcessValidator(
                dm);
        // If there is an existing pending operation throw an exception.
        if (triggerProcessValidator
                .isActivateOrDeactivateServicePending(service)) {
            OperationPendingException ope = new OperationPendingException(
                    String.format(
                            "Operation cannot be performed. There is already another pending request to activate or deactivate the service with ID '%s'",
                            String.valueOf(service.getServiceId())),
                    (activate)
                            ? OperationPendingException.ReasonEnum.ACTIVATE_SERVICE
                            : OperationPendingException.ReasonEnum.DEACTIVATE_SERVICE,
                    new Object[] { String.valueOf(service.getServiceId()) });

            logger.logWarn(Log4jLogger.SYSTEM_LOG, ope,
                    (activate)
                            ? LogMessageIdentifier.WARN_ACTIVATE_SERVICE_FAILED_DUE_TO_TRIGGER_CONFLICT
                            : LogMessageIdentifier.WARN_DEACTIVATE_SERVICE_FAILED_DUE_TO_TRIGGER_CONFLICT,
                    String.valueOf(service.getKey()));

            throw ope;
        }

        TriggerMessage message = new TriggerMessage(
                activate ? TriggerType.ACTIVATE_SERVICE
                        : TriggerType.DEACTIVATE_SERVICE);
        List<TriggerProcessMessageData> list = triggerQS
                .sendSuspendingMessages(Collections.singletonList(message));
        TriggerProcess tProc = list.get(0).getTrigger();
        tProc.addTriggerProcessParameter(TriggerProcessParameterName.OBJECT_ID,
                service.getServiceId());
        tProc.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                service);
        if (entries != null) {
            tProc.addTriggerProcessParameter(
                    TriggerProcessParameterName.CATALOG_ENTRIES, entries);
        }
        tProc.setUser(currentUser);

        VOService voProduct = null;
        TriggerDefinition triggerDefinition = tProc.getTriggerDefinition();
        if (triggerDefinition == null) {
            // if processing is not suspended, call finishing method
            try {
                if (activate) {
                    activateServiceInt(tProc);
                } else {
                    deactivateServiceInt(tProc);
                }

                dm.flush();
                if (entries != null && !entries.isEmpty()) {
                    VOCatalogEntry catalogEntry = entries.get(0);
                    if (catalogEntry.getMarketplace() != null) {
                        serviceAudit.activeOrDeactiveService(dm, prod,
                                catalogEntry.getMarketplace()
                                        .getMarketplaceId(),
                                catalogEntry.getMarketplace().getName(),
                                activate, catalogEntry.isVisibleInCatalog());
                    }
                }

                voProduct = ProductAssembler.toVOProduct(prod,
                        new LocalizerFacade(localizer,
                                currentUser.getLocale()));
            } catch (TechnicalServiceNotAliveException | ServiceStateException
                    | ObjectNotFoundException | OrganizationAuthoritiesException
                    | ServiceOperationException
                    | ConcurrentModificationException e) {
                sessionCtx.setRollbackOnly();
                throw e;
            }
        } else if (triggerDefinition.isSuspendProcess()) {
            // Register this REQUEST for final visibility constraint validation
            if (entries == null) {
                List<CatalogEntry> entry = dm
                        .getReference(Product.class, service.getKey())
                        .getCatalogEntries();
                if (!entry.isEmpty()) {
                    final CatalogEntry ce = entry.get(0);
                    entries = new ArrayList<>();
                    VOCatalogEntry vo = new VOCatalogEntry();
                    vo.setService(service);
                    vo.setVisibleInCatalog(ce.isVisibleInCatalog());
                    vo.setMarketplace(MarketplaceAssembler.toVOMarketplace(
                            ce.getMarketplace(), new LocalizerFacade(localizer,
                                    dm.getCurrentUser().getLocale())));
                    entries.add(vo);
                }
            }

            // If the operation defined by the trigger definition is suspended,
            // then set the trigger process identifiers.
            tProc.setTriggerProcessIdentifiers(
                    TriggerProcessIdentifiers.createDeactivateService(dm,
                            triggerDefinition.getType(), service));
            dm.merge(tProc);
        }

        // Register this REQUEST for final visibility constraint validation
        visChecker.add(prod, entries, activate);

        return voProduct;
    }

    /**
     * Update visibility of catalog entries for given service.
     * 
     * @throws ConcurrentModificationException
     *             when the passed entry has a different marketplace set as the
     *             persisted one
     */
    private void updateCatalogEntryVisibility(Product product,
            List<VOCatalogEntry> entries)
            throws ConcurrentModificationException {

        HashMap<String, CatalogEntry> entryMap;

        // Get all existing catalog entries for this service
        Query query = dm.createNamedQuery("CatalogEntry.findByService");
        query.setParameter("service", product);
        List<CatalogEntry> tempList = ParameterizedTypes
                .list(query.getResultList(), CatalogEntry.class);
        entryMap = new HashMap<>();
        for (CatalogEntry entry : tempList) {
            if (entry.getMarketplace() != null) {
                entryMap.put(entry.getMarketplace().getMarketplaceId(), entry);
            } else {
                entryMap.put(null, entry);
            }
        }

        // Now process all given entries
        for (VOCatalogEntry voEntry : entries) {
            final VOMarketplace mp = voEntry.getMarketplace();

            if (mp != null) {
                CatalogEntry domEntry = entryMap.get(mp.getMarketplaceId());
                if (domEntry == null) {
                    // No catalog entry for the specified marketplace found
                    ConcurrentModificationException cme = new ConcurrentModificationException(
                            voEntry);
                    logger.logError(Log4jLogger.SYSTEM_LOG, cme,
                            LogMessageIdentifier.WARN_MARKETPLACE_MISMATCH_ON_SETTING_VISIBILITY,
                            String.valueOf(product.getKey()));
                    throw cme;
                }

                // Update visibility as specified
                domEntry.setVisibleInCatalog(voEntry.isVisibleInCatalog());
            }
        }

    }

    @Override
    public VOOrganization getServiceSeller(long serviceKey, String locale)
            throws ObjectNotFoundException {
        ArgumentValidator.notNull("locale", locale);

        Product product = dm.getReference(Product.class, serviceKey);
        if (product.getType() == ServiceType.PARTNER_TEMPLATE) {
            product = product.getTemplate();
        }
        return OrganizationAssembler.toVOOrganization(product.getVendor(),
                false, new LocalizerFacade(localizer, locale));
    }

    @Override
    public VOOrganization getServiceSellerFallback(long serviceKey,
            String locale) throws ObjectNotFoundException {
        VOOrganization org = getServiceSeller(serviceKey, locale);
        if ((org.getDescription() == null || org.getDescription().isEmpty())
                && !locale.equals("en")) {
            Product product = dm.getReference(Product.class, serviceKey);
            LocalizerFacade localizerEn = new LocalizerFacade(localizer, "en");
            String description = localizerEn.getText(
                    product.getVendor().getKey(),
                    LocalizedObjectTypes.ORGANIZATION_DESCRIPTION);
            org.setDescription(description);
        }
        return org;
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public List<String> getInstanceIdsForSellers(List<String> organizationIds) {

        ArgumentValidator.notNull("organizationIds", organizationIds);

        List<String> result = new ArrayList<>();
        Organization providerOrg = dm.getCurrentUser().getOrganization();

        if (organizationIds.size() > 0) {
            Query query = dm
                    .createNamedQuery("Subscription.instanceIdsForSuppliers");
            query.setParameter("providerKey",
                providerOrg.getKey());
            query.setParameter("supplierIds", organizationIds);
            query.setParameter("status", EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.SUSPENDED));
            List<String> instanceIds = ParameterizedTypes
                    .list(query.getResultList(), String.class);
            if (instanceIds != null) {
                result.addAll(instanceIds);
            }
        }

        return result;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public VOService suspendService(VOService service, String reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException {

        ArgumentValidator.notNull("service", service);
        ArgumentValidator.notEmptyString("reason", reason);
        PlatformUser currentUser = dm.getCurrentUser();
        Product prod = dm.getReference(Product.class, service.getKey());
        Marketplace mp = validatePermissionForSuspendAndResume(currentUser,
                prod);

        // If the product is a PARTNER_TEMPLATE then get itself
        Product tempOrSelf;
        if (prod.getType() == ServiceType.PARTNER_TEMPLATE) {
            tempOrSelf = prod;
        }
        // Otherwise get the template or self
        else {
            tempOrSelf = prod.getTemplateOrSelf();
        }

        if (tempOrSelf.getStatus() != ServiceStatus.ACTIVE) {
            ServiceStateException e = new ServiceStateException(
                    ServiceStatus.ACTIVE, tempOrSelf.getStatus());
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_SUSPEND_SERVICE_INVALID_STATE,
                    tempOrSelf.getStatus().name());
            throw e;
        }

        tempOrSelf.setStatus(ServiceStatus.SUSPENDED);
        List<Product> list = getCustomerSpecificProductCopies(tempOrSelf);
        for (Product product : list) {
            if (product.getStatus() == ServiceStatus.ACTIVE) {
                product.setStatus(ServiceStatus.SUSPENDED);
            }
        }
        List<PlatformUser> users = tempOrSelf.getVendor().getPlatformUsers();
        for (PlatformUser user : users) {
            if (user.isOrganizationAdmin()
                    || user.hasRole(UserRoleType.SERVICE_MANAGER)) {
                try {
                    commService
                            .sendMail(user, EmailType.SERVICE_SUSPENDED,
                                    new Object[] { tempOrSelf.getProductId(),
                                            reason, currentUser.getEmail() },
                                    mp);
                } catch (MailOperationException e) {
                    logger.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_SEND_SERVICE_SUSPENDED_MAIL_FAILED);
                }
            }
        }
        dm.flush();
        VOService voProduct = ProductAssembler.toVOProduct(prod,
                new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        return voProduct;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public VOService resumeService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException {

        ArgumentValidator.notNull("service", service);
        Product prod = dm.getReference(Product.class, service.getKey());
        validatePermissionForSuspendAndResume(dm.getCurrentUser(), prod);

        // If the product is a PARTNER_TEMPLATE then get itself
        Product tempOrSelf;
        if (prod.getType() == ServiceType.PARTNER_TEMPLATE) {
            tempOrSelf = prod;
        }
        // Otherwise get the template or self
        else {
            tempOrSelf = prod.getTemplateOrSelf();
        }

        if (tempOrSelf.getStatus() != ServiceStatus.SUSPENDED) {
            ServiceStateException e = new ServiceStateException(
                    ServiceStatus.SUSPENDED, tempOrSelf.getStatus());
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_RESUME_SERVICE_INVALID_STATE,
                    tempOrSelf.getStatus().name());
            throw e;
        }
        tempOrSelf.setStatus(ServiceStatus.ACTIVE);
        List<Product> list = getCustomerSpecificProductCopies(tempOrSelf);
        for (Product product : list) {
            if (product.getStatus() == ServiceStatus.SUSPENDED) {
                product.setStatus(ServiceStatus.ACTIVE);
            }
        }
        dm.flush();
        VOService voProduct = ProductAssembler.toVOProduct(prod,
                new LocalizerFacade(localizer,
                        dm.getCurrentUser().getLocale()));

        return voProduct;
    }

    /**
     * Check that the provided service is published to a marketplace owned by
     * the provided user and that it does not belong to a subscription.
     * 
     * @param currentUser
     *            the calling user
     * @param prod
     *            the service to validate
     * @return the marketplace the service is published to
     * @throws OperationNotPermittedException
     *             in case the service is not published to a marketplace owned
     *             by the caller or the service belongs to a subscription
     */
    private Marketplace validatePermissionForSuspendAndResume(
            PlatformUser currentUser, Product prod)
            throws OperationNotPermittedException {
        if (prod.getOwningSubscription() != null) {
            String message = "Service '%s' is related to a subscription.";
            OperationNotPermittedException e = new OperationNotPermittedException(
                    String.format(message, prod.getKey()));
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_VALIDATE_PERMISSION_FOR_SUSPEND_AND_RESUME,
                    message);
            throw e;
        }

        Product tempOrSelf;
        if (prod.getType() == ServiceType.PARTNER_TEMPLATE) {
            tempOrSelf = prod;
        } else {
            tempOrSelf = prod.getTemplateOrSelf();
        }
        List<CatalogEntry> ces = tempOrSelf.getCatalogEntries();
        // the catalog entry may have no marketplace set
        // this happens when the MP got deleted while the service was not active
        if (ces == null || ces.isEmpty()
                || ces.get(0).getMarketplace() == null) {
            String message = "Service '%s' is not published to a marketplace.";
            OperationNotPermittedException e = new OperationNotPermittedException(
                    String.format(message, prod.getKey()));
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_VALIDATE_PERMISSION_FOR_SUSPEND_AND_RESUME,
                    message);
            throw e;
        }
        // currently a service can only be published to one marketplace
        Marketplace mp = ces.get(0).getMarketplace();
        PermissionCheck.owns(mp, currentUser.getOrganization(), logger,
                sessionCtx);
        return mp;
    }

    /**
     * compares the technical product key
     */
    private void validateTechnicalProductCompatibility(Product referenceProduct,
            Product compatibleProd) throws ServiceCompatibilityException {
        if (compatibleProd.getTechnicalProduct().getKey() != referenceProduct
                .getTechnicalProduct().getKey()) {
            ServiceCompatibilityException ipc = new ServiceCompatibilityException(
                    "Definition of product compatibility failed, related technical products do not match",
                    ServiceCompatibilityException.Reason.TECH_SERVICE);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ipc,
                    LogMessageIdentifier.WARN_DEFINE_COMPATIBILITY_FOR_PRODUCTS_FAILED_NOT_SAME_BASE,
                    dm.getCurrentUser().getUserId(),
                    Long.toString(compatibleProd.getKey()),
                    Long.toString(referenceProduct.getKey()));
            throw ipc;
        }
    }

    /**
     * Checks weather all currencies of the dependent price models are equal, or
     * free of charge.
     */
    private void validateCurrencyCompatibility(Product referenceProduct,
            Product compatibleProduct) throws ServiceCompatibilityException {
        PriceModel referencePriceModel = referenceProduct.getPriceModel();
        PriceModel comaptiblePriceModel = compatibleProduct.getPriceModel();
        if (!isCompatibleCurrency(referencePriceModel, comaptiblePriceModel)) {
            ServiceCompatibilityException ipc = new ServiceCompatibilityException(
                    "Definition of product compatibility failed,the price models have different currencies",
                    ServiceCompatibilityException.Reason.CURRENCY);
            logger.logWarn(Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG, ipc,
                    LogMessageIdentifier.WARN_DEFINE_COMPATIBILITY_FOR_PRODUCTS_FAILED_NOT_SAME_CURRENCY,
                    dm.getCurrentUser().getUserId(),
                    Long.toString(referencePriceModel.getKey()),
                    Long.toString(comaptiblePriceModel.getKey()));
            throw ipc;
        }
    }

    /**
     * Returns true if one or both of the price models are free of charge, or
     * the currencies are of the same type.
     */
    private boolean isCompatibleCurrency(PriceModel referencePriceModel,
            PriceModel compatiblePriceModel) {
        if (referencePriceModel != null && compatiblePriceModel != null
                && referencePriceModel.isChargeable()
                && compatiblePriceModel.isChargeable()) {
            if (!referencePriceModel.getCurrency()
                    .equals(compatiblePriceModel.getCurrency())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if one or both of the price models are free of charge, or
     * the currencies are of the same type.
     */
    private boolean isCompatibleCurrency(PriceModel referencePriceModel,
            VOPriceModel compatiblePriceModel) {
        if (referencePriceModel != null && compatiblePriceModel != null
                && referencePriceModel.isChargeable()
                && compatiblePriceModel.isChargeable()) {
            if (!referencePriceModel.getCurrency().getCurrencyISOCode()
                    .equals(compatiblePriceModel.getCurrencyISOCode())) {
                return false;
            }
        }
        return true;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public List<VOCompatibleService> getPotentialCompatibleServices(
            VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);
        Product p = dm.getReference(Product.class, service.getKey());
        Organization supplier = dm.getCurrentUser().getOrganization();
        PermissionCheck.owns(p, supplier, logger, null);

        p = p.getTemplateOrSelf();

        // get the marketplaces the service is published on
        Set<Marketplace> mps = new HashSet<>();
        for (CatalogEntry ce : p.getCatalogEntries()) {
            Marketplace mp = ce.getMarketplace();
            if (mp != null) {
                mps.add(mp);
            }
        }
        List<VOCompatibleService> result = new ArrayList<>();
        if (mps.isEmpty()) {
            // Bug 9850: is no marketplace is set, return an empty list
            return result;
        }

        // get all services based on the same TP, published on the same MPs
        Query q = dm
                .createNamedQuery("Product.getPotentialCompatibleForProduct");
        q.setParameter("marketplaces", mps);
        q.setParameter("vendorKey", supplier.getKey());
        q.setParameter("tp", p.getTechnicalProduct());
        q.setParameter("status",
                EnumSet.of(ServiceStatus.DELETED, ServiceStatus.OBSOLETE));
        List<Product> products = ParameterizedTypes.list(q.getResultList(),
                Product.class);
        products.remove(p);

        // get the currently configured targets
        Set<Long> targetKeys = new HashSet<>();
        q = dm.createNamedQuery("ProductReference.getTargetKeysForProduct");
        q.setParameter("product", p);
        targetKeys
                .addAll(ParameterizedTypes.list(q.getResultList(), Long.class));

        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        ProductAssembler.prefetchData(products, facade,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);

        for (Product prod : products) {
            if (isCompatibleCurrency(p.getPriceModel(), prod.getPriceModel())) {
                VOCompatibleService s = ProductAssembler
                        .toVOCompatibleService(prod, targetKeys, facade);
                result.add(s);
            }
        }

        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public boolean isPartOfUpgradePath(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);

        Organization org = dm.getCurrentUser().getOrganization();

        // retrieve the domain object for the product
        Product prod = dm.getReference(Product.class, service.getKey());

        // ensure that the product belongs to the calling supplier
        PermissionCheck.owns(prod, org, logger, sessionCtx);

        boolean result = isPartOfUpgradePath(prod.getKey());

        return result;
    }

    @Override
    public boolean isPartOfUpgradePath(long serviceKey) {
        Query query = dm.createNamedQuery("Product.countAllReferences");
        query.setParameter("productKey", serviceKey);
        query.setParameter("status",
                EnumSet.of(ServiceStatus.DELETED, ServiceStatus.OBSOLETE));
        Long count = (Long) query.getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public List<Product> getCustomerSpecificCopyProducts(Product template)
            throws OperationNotPermittedException {
        Organization supplier = dm.getCurrentUser().getOrganization();
        PermissionCheck.owns(template, supplier, logger, sessionCtx);

        Set<ServiceType> types = EnumSet.of(ServiceType.CUSTOMER_TEMPLATE);

        Query query = dm.createNamedQuery(
                "Product.getCustomerSpecificCopiesForTemplate");
        query.setParameter("template", template);
        query.setParameter("serviceType", types);
        List<Product> list = ParameterizedTypes.list(query.getResultList(),
                Product.class);

        return list;
    }

    @Override
    public List<VOCustomerService> getServiceCustomerTemplates(
            VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notNull("service", service);

        Product storedService = dm.getReference(Product.class,
                service.getKey());
        List<Product> customerProducts = getCustomerSpecificCopyProducts(
                storedService);

        List<VOCustomerService> result = new ArrayList<>();
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        Set<ServiceStatus> states = EnumSet.of(ServiceStatus.ACTIVE,
                ServiceStatus.INACTIVE, ServiceStatus.SUSPENDED);

        for (Product product : customerProducts) {
            if (states.contains(product.getStatus())) {
                result.add(
                        ProductAssembler.toVOCustomerProduct(product, facade));
            }
        }

        return result;
    }

    @Override
    @RolesAllowed("SERVICE_MANAGER")
    public void deleteService(Long key)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ServiceOperationException,
            ServiceStateException, ConcurrentModificationException {
        VOService vo = new VOService();
        vo.setKey(key.longValue());
        vo = getServiceDetails(vo);
        deleteService(vo);
    }

    @Override
    @RolesAllowed("TECHNOLOGY_MANAGER")
    public void deleteTechnicalService(Long key) throws ObjectNotFoundException,
            OperationNotPermittedException, DeletionConstraintException,
            OrganizationAuthoritiesException, ConcurrentModificationException {
        Organization provider = dm.getCurrentUser().getOrganization();
        VOTechnicalService vo = new VOTechnicalService();
        vo.setKey(key.longValue());
        TechnicalProduct tProd = findTechnicalProductAndCheckOwner(provider,
                vo);
        LocalizerFacade facade = new LocalizerFacade(localizer,
                dm.getCurrentUser().getLocale());
        List<ParameterDefinition> paramDefs = getPlatformParameterDefinitions(
                tProd);
        List<Event> platformEvents = getPlatformEvents(tProd);
        vo = TechnicalProductAssembler.toVOTechnicalProduct(tProd, paramDefs,
                platformEvents, facade, false);
        deleteTechnicalService(vo);
    }
}
