/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: goebel
 *
 * <p>Creation Date: 27.05.2011
 *
 * <p>Completion Time: 27.05.2011
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace.bean;

import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.id.IdGenerator;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.*;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.assembler.CatalogEntryAssembler;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.security.auth.login.LoginException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Stateless
@Remote(MarketplaceService.class)
@Interceptors({InvocationDateContainer.class, ExceptionMapper.class})
public class MarketplaceServiceBean implements MarketplaceService {

  private static final Log4jLogger logger = LoggerFactory.getLogger(MarketplaceServiceBean.class);

  @EJB(beanInterface = DataService.class)
  protected DataService dm;

  @EJB(beanInterface = LocalizerServiceLocal.class)
  LocalizerServiceLocal localizer;

  @EJB AccountServiceLocal accountService;

  @EJB private ServiceProvisioningServiceLocal provisioningService;

  @Resource protected SessionContext sessionCtx;

  @EJB IdentityServiceLocal identityService;

  @EJB LandingpageServiceLocal landingpageService;

  @EJB MarketplaceServiceLocal marketplaceServiceLocal;

  @EJB ApplicationServiceLocal appServiceLocal;

  @EJB MarketplaceCacheService marketplaceCache;

  @Override
  @RolesAllowed({"SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER"})
  public List<VOMarketplace> getMarketplacesForOrganization() {

    List<Marketplace> marketplacesList = marketplaceServiceLocal.getMarketplacesForSupplier();

    // finally convert all domain objects to VO representation and return
    List<VOMarketplace> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());

    for (Marketplace mp : marketplacesList) {
      result.add(MarketplaceAssembler.toVOMarketplace(mp, facade));
    }

    return result;
  }

  @Override
  @RolesAllowed({"SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER"})
  public List<VOCatalogEntry> getMarketplacesForService(VOService service)
      throws ObjectNotFoundException, OperationNotPermittedException {

    List<VOCatalogEntry> result = getMarketplacesForService(service, PerformanceHint.ALL_FIELDS);

    return result;
  }

  @RolesAllowed("SERVICE_MANAGER")
  public List<VOCatalogEntry> getMarketplacesForService(VOService service, PerformanceHint scope)
      throws ObjectNotFoundException, OperationNotPermittedException {

    // validate input
    ArgumentValidator.notNull("service", service);

    // make sure this method was called by a supplier
    Organization supplier = dm.getCurrentUser().getOrganization();

    // first find existing service/product
    Product prod = loadProductAndVerifyOwner(service.getKey(), supplier);
    prod = prod.getTemplateIfSubscriptionOrSelf();

    // second retrieve relevant marketplaces by query
    Query query = dm.createNamedQuery("CatalogEntry.findByService");
    query.setParameter("service", prod);
    List<CatalogEntry> tempList =
        ParameterizedTypes.list(query.getResultList(), CatalogEntry.class);

    // finally convert all domain objects to VO representation and return
    List<VOCatalogEntry> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());

    for (CatalogEntry ce : tempList) {
      result.add(CatalogEntryAssembler.toVOCatalogEntry(ce, facade, scope));
    }

    return result;
  }

  @Override
  @RolesAllowed({"SERVICE_MANAGER", "RESELLER_MANAGER", "BROKER_MANAGER"})
  public VOServiceDetails publishService(VOService service, List<VOCatalogEntry> entries)
      throws ObjectNotFoundException, ValidationException, NonUniqueBusinessKeyException,
          OperationNotPermittedException {

    // validate input
    ArgumentValidator.notNull("service", service);
    ArgumentValidator.notNull("entries", entries);
    BLValidator.isNotEmpty("entries", entries);
    // the following (temporary) assumption hold <br>
    // suppliers may publish to either THEIR local MP XOR a global MP XOR no
    // marketplace at all
    if (entries.size() != 1) {
      ValidationException e =
          new ValidationException(
              ReasonEnum.INVALID_NUMBER_TARGET_CATALOG_ENTRIES,
              null,
              new Object[] {Integer.valueOf(entries.size())});
      logger.logError(
          Log4jLogger.SYSTEM_LOG,
          e,
          LogMessageIdentifier.ERROR_SERVICE_PUBLISHED_ONLY_LOCAL_XOR_GLOBAL_MARKETPLACE);
      throw e;
    }

    CatalogEntry ceNew = CatalogEntryAssembler.toCatalogEntry(entries.get(0));

    Product product =
        marketplaceServiceLocal.publishService(
            service.getKey(), ceNew, entries.get(0).getCategories());

    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    VOServiceDetails modifiedService = provisioningService.getServiceDetails(product, facade);

    return modifiedService;
  }

  @Override
  public VOMarketplace getMarketplaceForSubscription(long subscriptionKey, String locale)
      throws ObjectNotFoundException {

    VOMarketplace vo_mpl = null;
    Subscription subscription = dm.getReference(Subscription.class, subscriptionKey);

    if (subscription.getMarketplace() != null) {
      LocalizerFacade facade =
          new LocalizerFacade(
              localizer,
              (dm.getCurrentUserIfPresent() == null)
                  ? "en"
                  : dm.getCurrentUserIfPresent().getLocale());
      vo_mpl = MarketplaceAssembler.toVOMarketplace(subscription.getMarketplace(), facade);
    }

    return vo_mpl;
  }

  private Product loadProductAndVerifyOwner(long serviceKey, Organization supplier)
      throws ObjectNotFoundException, OperationNotPermittedException {
    Product prod = dm.getReference(Product.class, serviceKey);

    // make sure the calling supplier actually owns the service
    PermissionCheck.owns(prod, supplier, logger, null);
    return prod;
  }

  private Marketplace createMarketplaceIntern(VOMarketplace marketplace)
      throws ValidationException, ObjectNotFoundException, OperationNotPermittedException,
          MarketplaceValidationException {
    boolean isMpIdProvided = false;
    final String providedMarketplaceId = marketplace.getMarketplaceId();
    if (providedMarketplaceId == null || providedMarketplaceId.isEmpty()) {
      marketplace.setMarketplaceId("1"); // dummy Id - will be changed.
    } else {
      isMpIdProvided = true;
    }
    Marketplace mpNew = MarketplaceAssembler.toMarketplace(marketplace);

    mpNew.setCreationDate(DateFactory.getInstance().getTransactionTime());

    // Update owner organization and assign role
    marketplaceServiceLocal.updateOwningOrganization(
        mpNew, marketplace.getOwningOrganizationId(), true);

    // Update related tenant
    marketplaceServiceLocal.updateTenant(mpNew, marketplace.getTenantId());

    PublicLandingpage landingpage = PublicLandingpage.newDefault();
    landingpage.setMarketplace(mpNew);
    mpNew.setPublicLandingpage(landingpage);

    marketplaceServiceLocal.createRevenueModels(
        mpNew, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    Marketplace mp = persistMarketplace(isMpIdProvided, mpNew, marketplace);
    marketplaceServiceLocal.grantPublishingRights(mp);
    return mp;
  }

  private Marketplace persistMarketplace(
      boolean isMpIdProvided, Marketplace mpNew, VOMarketplace marketplace)
      throws MarketplaceValidationException {
    if (isMpIdProvided) {
      return persistMarketplaceWithSpecifiedId(mpNew, marketplace.getMarketplaceId());
    }
    return persistMarketplace(mpNew, marketplace.getOwningOrganizationId());
  }

  boolean findMarketplaceKeyByMarketplaceId(String marketplaceId) {
    Query marketplaceQuery = dm.createNamedQuery("Marketplace.findByBusinessKey");
    marketplaceQuery.setParameter("marketplaceId", marketplaceId);
    List<Marketplace> marketplacelist =
        ParameterizedTypes.list(marketplaceQuery.getResultList(), Marketplace.class);

    if (marketplacelist != null && marketplacelist.size() > 0) {
      return true;
    } else {
      return false;
    }
  }

  Marketplace persistMarketplace(Marketplace mpNew, String suggestedId) {
    // Use owner organization ID if possible, otherwise generate one
    if (suggestedId == null || suggestedId.equals("") || "PLATFORM_OPERATOR".equals(suggestedId)) {
      suggestedId = IdGenerator.generateArtificialIdentifier();
    }

    int i = 0;
    while (true) {
      try {
        if (!findMarketplaceKeyByMarketplaceId(suggestedId)) {
          mpNew.setMarketplaceId(suggestedId);
          dm.persist(mpNew);
          dm.flush();
          break;
        } else {
          suggestedId = IdGenerator.generateArtificialIdentifier();
          if (i++ > 100) { // stop after 100 tries
            throwExceptionNoFreeMarketplace();
          }
        }
      } catch (NonUniqueBusinessKeyException e) {
        throwExceptionNoFreeMarketplace();
      }
    }

    return mpNew;
  }

  private Marketplace persistMarketplaceWithSpecifiedId(Marketplace marketplace, String suggestedId)
      throws MarketplaceValidationException {
    try {
      validateProvidedMpId(suggestedId);
      marketplace.setMarketplaceId(suggestedId);
      dm.persist(marketplace);
    } catch (NonUniqueBusinessKeyException | MarketplaceValidationException e) {
      throw new MarketplaceValidationException(e.getMessage());
    }
    dm.flush();

    return marketplace;
  }

  private void validateProvidedMpId(String id) throws MarketplaceValidationException {
    if (id == null || id.equals("") || "PLATFORM_OPERATOR".equals(id)) {
      throwExceptionInvalidMarketplaceId();
    }
    if (findMarketplaceKeyByMarketplaceId(id)) {
      throwExceptionNotUniqueMarketplaceId();
    }
  }

  private void throwExceptionNoFreeMarketplace() {
    SaaSSystemException se = new SaaSSystemException("No free marketplaceId found!");
    logger.logError(
        Log4jLogger.SYSTEM_LOG, se, LogMessageIdentifier.ERROR_MARKETPLACE_CREATION_FAILED);
    throw se;
  }

  private void throwExceptionInvalidMarketplaceId() throws MarketplaceValidationException {
    MarketplaceValidationException se =
        new MarketplaceValidationException(
            "Provided Marketplace ID does not meet the requirements.");
    logger.logError(Log4jLogger.SYSTEM_LOG, se, LogMessageIdentifier.ERROR_MARKETPLACE_ID_INVALID);
    throw se;
  }

  private void throwExceptionNotUniqueMarketplaceId() throws MarketplaceValidationException {
    MarketplaceValidationException se =
        new MarketplaceValidationException("Provided Marketplace ID is already in use.");
    logger.logError(
        Log4jLogger.SYSTEM_LOG, se, LogMessageIdentifier.ERROR_MARKETPLACE_ID_NOT_UNIQUE);
    throw se;
  }

  @Override
  @RolesAllowed({"MARKETPLACE_OWNER", "PLATFORM_OPERATOR"})
  public List<VOMarketplace> getMarketplacesOwned() {

    Organization org = dm.getCurrentUser().getOrganization();
    List<Marketplace> marketplaces = org.getMarketplaces();
    List<VOMarketplace> result = new ArrayList<>();
    LocalizerFacade facade =
        new LocalizerFacade(
            localizer,
            (dm.getCurrentUserIfPresent() == null)
                ? "en"
                : dm.getCurrentUserIfPresent().getLocale());
    for (Marketplace mp : marketplaces) {
      result.add(MarketplaceAssembler.toVOMarketplace(mp, facade));
    }

    return result;
  }

  @Override
  @RolesAllowed("PLATFORM_OPERATOR")
  public List<VOMarketplace> getMarketplacesForOperator() {

    List<Marketplace> tempList = marketplaceServiceLocal.getAllMarketplaces();
    List<VOMarketplace> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    for (Marketplace mp : tempList) {
      result.add(MarketplaceAssembler.toVOMarketplace(mp, facade));
    }

    return result;
  }

  @Override
  public List<VOMarketplace> getAccessibleMarketplaces() {

    List<Marketplace> tempList =
        marketplaceServiceLocal.getAllAccessibleMarketplacesForOrganization(
            dm.getCurrentUser().getOrganization().getKey());
    List<VOMarketplace> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    for (Marketplace mp : tempList) {
      result.add(MarketplaceAssembler.toVOMarketplace(mp, facade));
    }
    return result;
  }

  @Override
  @RolesAllowed({"MARKETPLACE_OWNER", "PLATFORM_OPERATOR"})
  public VOMarketplace updateMarketplace(VOMarketplace marketplace)
      throws ObjectNotFoundException, OperationNotPermittedException,
          ConcurrentModificationException, ValidationException, UserRoleAssignmentException {

    ArgumentValidator.notNull("marketplace", marketplace);

    Marketplace mp = marketplaceServiceLocal.getMarketplace(marketplace.getMarketplaceId());
    MarketplaceAssembler.updateMarketplace(mp, marketplace);

    boolean ownerAssignmentUpdated =
        marketplaceServiceLocal.updateMarketplace(
            mp, marketplace.getName(), marketplace.getOwningOrganizationId());

    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    VOMarketplace result = MarketplaceAssembler.toVOMarketplace(mp, facade);

    // Send email to all admins of the organization about new assignment
    if (ownerAssignmentUpdated) {
      marketplaceServiceLocal.sendNotification(
          EmailType.MARKETPLACE_OWNER_ASSIGNED, mp, mp.getOrganization().getKey());
    }

    marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());

    return result;
  }

  @Override
  @RolesAllowed("PLATFORM_OPERATOR")
  public VOMarketplace createMarketplace(VOMarketplace marketplace)
      throws OperationNotPermittedException, ObjectNotFoundException, ValidationException,
          UserRoleAssignmentException, MarketplaceValidationException {

    // check if mandatory fields are given
    ArgumentValidator.notNull("marketplace", marketplace);
    BLValidator.isNotBlank("marketplace.name", marketplace.getName());

    if (marketplace.getOwningOrganizationId() == null) {
      // If no owner is given take that of the current used (platform
      // operator)
      String ownID = dm.getCurrentUser().getOrganization().getOrganizationId();
      marketplace.setOwningOrganizationId(ownID);
    }

    VOMarketplace result;
    Marketplace mpNew;
    // Create new MP domain object (and Landingpage) copy from passed VO
    mpNew = createMarketplaceIntern(marketplace);
    List<VOLocalizedText> list =
        Arrays.asList(new VOLocalizedText(dm.getCurrentUser().getLocale(), marketplace.getName()));
    localizer.storeLocalizedResources(mpNew.getKey(), LocalizedObjectTypes.MARKETPLACE_NAME, list);
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    result = MarketplaceAssembler.toVOMarketplace(mpNew, facade);
    List<PlatformUser> admins =
        accountService.getOrganizationAdmins(mpNew.getOrganization().getKey());

    // Add MARKETPLACE_OWNER role to all administrators
    for (PlatformUser admin : admins) {
      identityService.grantUserRoles(
          admin, Collections.singletonList(UserRoleType.MARKETPLACE_OWNER));
    }
    // Send email to all admins of the organization about new assignment
    marketplaceServiceLocal.sendNotification(EmailType.MARKETPLACE_OWNER_ASSIGNED, mpNew, admins);

    return result;
  }

  @Override
  @RolesAllowed("PLATFORM_OPERATOR")
  public void deleteMarketplace(String marketplaceId) throws ObjectNotFoundException {

    ArgumentValidator.notNull("marketplaceId", marketplaceId);

    // check if marketplace still exists
    Marketplace mp = new Marketplace(marketplaceId);
    mp = (Marketplace) dm.getReferenceByBusinessKey(mp);

    // active services have to be deactivated first
    deactivateAllServices(mp, dm);

    // if catalog entries exist: null out the marketplace references
    setMarketplaceReferencesOfCatalogEntriesToNull(mp);

    // if category exist: delete records
    deleteCategory(marketplaceId);

    // if subscriptions exist: null out the marketplace references
    setMarketplaceReferencesOfSubscriptionsToNull(mp);

    // Delete marketplace
    Organization owningOrganization = mp.getOrganization();
    owningOrganization.getMarketplaces().remove(mp);

    marketplaceCache.resetConfiguration(marketplaceId);

    dm.remove(mp);

    // if owningOrganization has no marketplaces left revoke marketplace
    // owner role
    revokeRoleIfNecessary(owningOrganization);
    try {
      dm.persist(owningOrganization);
    } catch (NonUniqueBusinessKeyException e) {
      // cannot be thrown in this case
    }
  }

  private void deleteCategory(String marketplaceId) {
    Query queryCategories = dm.createNamedQuery("Category.findByMarketplaceId");
    queryCategories.setParameter("marketplaceId", marketplaceId);
    List<Category> categories =
        ParameterizedTypes.list(queryCategories.getResultList(), Category.class);
    if (categories != null) {
      for (Category category : categories) {
        dm.remove(category);
      }
    }
  }

  private void revokeRoleIfNecessary(Organization owningOrganization)
      throws ObjectNotFoundException {
    // check if organization has any marketplace
    Query query = dm.createNamedQuery("Marketplace.getByOwner");
    query.setParameter("organizationId", owningOrganization.getOrganizationId());
    List<Marketplace> result = ParameterizedTypes.list(query.getResultList(), Marketplace.class);
    // if result is empty removeOwnerRole
    if (result == null || result.isEmpty()) {
      marketplaceServiceLocal.removeOwnerRole(owningOrganization);
      marketplaceServiceLocal.removeUserRoles(owningOrganization.getOrganizationId());
    }
  }

  /**
   * Deactivates and detaches all services of all suppliers on the given marketplace. For each
   * service that published on the marketplace the service is first deactivated and then the service
   * is 'detached' from the marketplace. This affects the customer specific copies as well.
   *
   * @param mp the marketplace
   * @param dm the data service
   */
  private void deactivateAllServices(Marketplace mp, DataService dm) {
    String mId = mp.getMarketplaceId();
    Query query = dm.createNamedQuery("Product.getTemplatesForMarketplace");
    query.setParameter("marketplaceId", mId);
    List<Product> productList = ParameterizedTypes.list(query.getResultList(), Product.class);
    if (productList != null) {
      for (Product product : productList) {
        if (product.getStatus() == ServiceStatus.ACTIVE) {
          product.setStatus(ServiceStatus.INACTIVE);
        }
        deactivateCustomerServices(product);
      }
    }
  }

  /**
   * Deactivates and detaches all services of the given supplier on the given marketplace. For each
   * service of the supplier that published on the marketplace the service is first deactivated and
   * then the service is 'detached' from the marketplace. This affects the customer specific copies
   * as well.
   *
   * @param mp the marketplace
   * @param supplier the supplier
   * @param dm the data service
   */
  private void unlinkServices(Marketplace mp, Organization supplier, DataService dm) {
    String mId = mp.getMarketplaceId();
    Query query = dm.createNamedQuery("Product.getProductsForVendorOnMarketplace");
    query.setParameter("marketplaceId", mId);
    query.setParameter("vendorKey", Long.valueOf(supplier.getKey()));
    List<Product> productList = ParameterizedTypes.list(query.getResultList(), Product.class);
    if (productList != null) {
      for (Product product : productList) {
        if (product.getStatus() == ServiceStatus.ACTIVE) {
          product.setStatus(ServiceStatus.INACTIVE);
        }
        deactivateCustomerServices(product);
        for (CatalogEntry ce : product.getCatalogEntries()) {
          Marketplace ceMp = ce.getMarketplace();
          if (ceMp != null && mId.equals(ceMp.getMarketplaceId())) {
            ce.setMarketplace(null);
          }
        }
      }
    }
  }

  /**
   * Deactivates all customer specific service copies that are based on the given template.
   *
   * @param template the base service for which the customer specific services should be deactivated
   */
  private void deactivateCustomerServices(Product template) {
    Query query = dm.createNamedQuery("Product.getCustomerCopies");
    query.setParameter("template", template);
    List<Product> productList = ParameterizedTypes.list(query.getResultList(), Product.class);
    if (productList != null) {
      for (Product product : productList) {
        if (product.getStatus() == ServiceStatus.ACTIVE) {
          product.setStatus(ServiceStatus.INACTIVE);
        }
      }
    }
  }

  /*
   * references to marketplace are removed /nulled in the catalog entries
   */
  private void setMarketplaceReferencesOfCatalogEntriesToNull(Marketplace mp) {
    if (mp.getCatalogEntries() != null && mp.getCatalogEntries().size() > 0) {
      for (CatalogEntry catalogEntry : mp.getCatalogEntries()) {
        catalogEntry.setMarketplace(null);
      }
    }
  }

  /*
   * references to marketplace are removed /nulled in the subscriptions
   */
  private void setMarketplaceReferencesOfSubscriptionsToNull(Marketplace mp) {
    Query query = dm.createNamedQuery("Subscription.getForMarketplace");
    query.setParameter("marketplace", mp);
    List<Subscription> subscriptions =
        ParameterizedTypes.list(query.getResultList(), Subscription.class);
    if (subscriptions != null && !subscriptions.isEmpty()) {
      for (Subscription subscription : subscriptions) {
        subscription.setMarketplace(null);
      }
    }
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void addOrganizationsToMarketplace(List<String> organizationIds, String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException,
          OrganizationAuthorityException, OrganizationAlreadyExistsException,
          MarketplaceAccessTypeUneligibleForOperationException {
    ArgumentValidator.notNull("organizationIds", organizationIds);
    String supplierId = "";
    try {
      Marketplace mp = getAndValidateMarketplace(marketplaceId);
      if (mp.isOpen()) {
        String msg = "Adding suppliers for open marketplaces not allowed: marketplace ";
        String[] params = new String[4];
        params[0] = "addSuppliersToMarketplace";
        params[1] = "non-open";
        params[2] = marketplaceId;
        params[3] = "open";
        prepareExceptionAndThrow(msg, params);
      }
      for (String organizationId : organizationIds) {
        organizationId = organizationId.trim();
        Organization supplier = new Organization();
        supplier.setOrganizationId(organizationId);
        supplier = (Organization) dm.getReferenceByBusinessKey(supplier);
        supplierId = organizationId;
        checkSellerRole(supplier);

        MarketplaceToOrganization mtofind = new MarketplaceToOrganization(mp, supplier);
        MarketplaceToOrganization mto = (MarketplaceToOrganization) dm.find(mtofind);
        if (mto == null) {
          mto = mtofind;
          dm.persist(mto);
          dm.flush();
        } else {
          if (!PublishingAccess.PUBLISHING_ACCESS_GRANTED.equals(mto.getPublishingAccess())) {
            mto.setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_GRANTED);
          } else {
            throw new NonUniqueBusinessKeyException();
          }
        }
        marketplaceServiceLocal.sendNotification(
            EmailType.MARKETPLACE_SUPPLIER_ASSIGNED, mp, supplier.getKey());
      }
    } catch (ObjectNotFoundException | OrganizationAuthorityException e) {
      sessionCtx.setRollbackOnly();
      throw e;
    } catch (NonUniqueBusinessKeyException e) {
      sessionCtx.setRollbackOnly();
      OrganizationAlreadyExistsException ex =
          new OrganizationAlreadyExistsException(
              "Supplier "
                  + supplierId
                  + " has already been added to the marketplace "
                  + marketplaceId);
      logger.logError(
          Log4jLogger.SYSTEM_LOG,
          ex,
          LogMessageIdentifier.ERROR_SUPPLIER_ALREADY_ADDED_TO_MARKETPLACE,
          supplierId,
          marketplaceId);
      throw ex;
    }
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void removeOrganizationsFromMarketplace(List<String> organizationIds, String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException,
          MarketplaceAccessTypeUneligibleForOperationException, OrganizationAuthorityException {
    ArgumentValidator.notNull("organizationIds", organizationIds);
    try {
      Marketplace mp = getAndValidateMarketplace(marketplaceId);
      if (mp.isOpen()) {
        String msg = "Removing suppliers for open marketplaces not allowed: marketplace ";
        String[] params = new String[4];
        params[0] = "removeSuppliersFromMarketplace";
        params[1] = "non-open";
        params[2] = marketplaceId;
        params[3] = "open";
        prepareExceptionAndThrow(msg, params);
      }

      for (String organizationId : organizationIds) {
        organizationId = organizationId.trim();
        Organization supplier = new Organization();
        supplier.setOrganizationId(organizationId);
        supplier = (Organization) dm.getReferenceByBusinessKey(supplier);
        checkSellerRole(supplier);
        MarketplaceToOrganization mto = new MarketplaceToOrganization(mp, supplier);
        try {
          mto = (MarketplaceToOrganization) dm.getReferenceByBusinessKey(mto);
          if (PublishingAccess.PUBLISHING_ACCESS_GRANTED.equals(mto.getPublishingAccess())) {
            unlinkServices(mp, supplier, dm);
            dm.remove(mto);
            dm.flush();
            marketplaceServiceLocal.sendNotification(
                EmailType.MARKETPLACE_SUPPLIER_REMOVED, mp, supplier.getKey());
          }

        } catch (ObjectNotFoundException e) {
          // already gone, don't care
        }
      }
    } catch (ObjectNotFoundException e) {
      sessionCtx.setRollbackOnly();
      throw e;
    }
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public List<VOOrganization> getOrganizationsForMarketplace(String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException,
          MarketplaceAccessTypeUneligibleForOperationException {

    Marketplace mp = getAndValidateMarketplace(marketplaceId);
    if (mp.isOpen()) {
      String msg = "getSuppliersForMarketplace() must not be invoked for open marketplaces";
      String[] params = new String[4];
      params[0] = "getSuppliersForMarketplace";
      params[1] = "non-open";
      params[2] = marketplaceId;
      params[3] = "open";

      prepareExceptionAndThrow(msg, params);
    }
    return retrieveMkpToOrgByPublishingAndConvertToVo(
        mp, PublishingAccess.PUBLISHING_ACCESS_GRANTED);
  }

  @Override
  public List<VOOrganization> getSuppliersForMarketplace(String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException {

    List<VOOrganization> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());

    VOMarketplace mp = getMarketplaceById(marketplaceId);
    if (mp.isOpen()) {

      TypedQuery<MarketplaceToOrganization> mtoQuery =
          dm.createNamedQuery(
              "MarketplaceToOrganization.findSuppliersForMpByPublishingAccess",
              MarketplaceToOrganization.class);
      mtoQuery.setParameter("marketplace_tkey", Long.valueOf(mp.getKey()));
      mtoQuery.setParameter("publishingAccess", PublishingAccess.PUBLISHING_ACCESS_DENIED);

      List<MarketplaceToOrganization> mtoList = mtoQuery.getResultList();
      HashSet<Long> set = new HashSet<>();

      for (MarketplaceToOrganization mto : mtoList) {
        set.add(new Long(mto.getOrganization_tkey()));
      }

      TypedQuery<Organization> orgQuery =
          dm.createNamedQuery("Organization.getAllSuppliers", Organization.class);
      List<Organization> orgList = orgQuery.getResultList();

      for (Organization org : orgList) {
        if (!set.contains(new Long(org.getKey()))) {
          result.add(OrganizationAssembler.toVOOrganization(org, false, facade));
        }
      }
    } else {

      TypedQuery<MarketplaceToOrganization> query =
          dm.createNamedQuery(
              "MarketplaceToOrganization.findSuppliersForMpByPublishingAccess",
              MarketplaceToOrganization.class);
      query.setParameter("marketplace_tkey", Long.valueOf(mp.getKey()));
      query.setParameter("publishingAccess", PublishingAccess.PUBLISHING_ACCESS_GRANTED);

      List<MarketplaceToOrganization> list = query.getResultList();

      for (MarketplaceToOrganization mto : list) {
        result.add(OrganizationAssembler.toVOOrganization(mto.getOrganization(), false, facade));
      }
    }

    return result;
  }

  private Marketplace getAndValidateMarketplace(String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException {
    ArgumentValidator.notNull("marketplaceId", marketplaceId);
    marketplaceId = marketplaceId.trim();
    Marketplace mp = new Marketplace(marketplaceId);
    mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
    PermissionCheck.owns(mp, dm.getCurrentUser().getOrganization(), logger, null);
    return mp;
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public List<VOOrganization> getBannedOrganizationsForMarketplace(String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException,
          MarketplaceAccessTypeUneligibleForOperationException {

    Marketplace mp = getAndValidateMarketplace(marketplaceId);
    if (!mp.isOpen()) {
      String msg =
          "getBannedSuppliersForMarketplace() must not be invoked for non-open marketplaces, but was invoked for the non-open marketplace with id ";
      String[] params = new String[4];
      params[0] = "getBannedSuppliersForMarketplace";
      params[1] = "open";
      params[2] = marketplaceId;
      params[3] = "non-open";

      prepareExceptionAndThrow(msg, params);
    }
    return retrieveMkpToOrgByPublishingAndConvertToVo(
        mp, PublishingAccess.PUBLISHING_ACCESS_DENIED);
  }

  private void prepareExceptionAndThrow(String msg, String[] params)
      throws MarketplaceAccessTypeUneligibleForOperationException {
    MarketplaceAccessTypeUneligibleForOperationException e =
        new MarketplaceAccessTypeUneligibleForOperationException(msg + params[2], params[2]);
    logger.logWarn(
        Log4jLogger.SYSTEM_LOG,
        e,
        LogMessageIdentifier.WARN_OPERATION_NOT_ALLOWED_FOR_MARKETPLACE,
        params);
    throw e;
  }

  private ArrayList<VOOrganization> retrieveMkpToOrgByPublishingAndConvertToVo(
      Marketplace mp, PublishingAccess publishingAccess) {
    ArrayList<VOOrganization> result = new ArrayList<>();

    Query query =
        dm.createNamedQuery("MarketplaceToOrganization.findSuppliersForMpByPublishingAccess");
    query.setParameter("marketplace_tkey", Long.valueOf(mp.getKey()));
    query.setParameter("publishingAccess", publishingAccess);

    // finally convert all domain objects to VO representation and
    // return
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());

    List resultList = query.getResultList();
    for (Object object : ParameterizedTypes.iterable(resultList, MarketplaceToOrganization.class)) {
      if (object instanceof MarketplaceToOrganization) {
        MarketplaceToOrganization mto = (MarketplaceToOrganization) object;
        result.add(OrganizationAssembler.toVOOrganization(mto.getOrganization(), false, facade));
      }
    }
    return result;
  }

  private void checkSellerRole(Organization organization) throws OrganizationAuthorityException {
    if (!organization.hasRole(OrganizationRoleType.SUPPLIER)
        && !organization.hasRole(OrganizationRoleType.BROKER)
        && !organization.hasRole(OrganizationRoleType.RESELLER)) {
      OrganizationAuthorityException ioa =
          new OrganizationAuthorityException(
              "Insufficient authorities for organization '"
                  + organization.getOrganizationId()
                  + "'. Required role: '"
                  + OrganizationRoleType.SUPPLIER.name()
                  + " or "
                  + OrganizationRoleType.BROKER.name()
                  + " or "
                  + OrganizationRoleType.RESELLER.name()
                  + "'.",
              new Object[] {
                OrganizationRoleType.SUPPLIER.name()
                    + ", "
                    + OrganizationRoleType.BROKER.name()
                    + ", "
                    + OrganizationRoleType.RESELLER.name()
              });
      logger.logWarn(
          Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
          ioa,
          LogMessageIdentifier.WARN_ORGANIZATION_ROLE_REQUIRED,
          Long.toString(organization.getKey()),
          OrganizationRoleType.SUPPLIER.toString()
              + ", "
              + OrganizationRoleType.BROKER.toString()
              + ", "
              + OrganizationRoleType.RESELLER.toString());
      throw ioa;
    }
  }

  @Override
  public VOMarketplace getMarketplaceById(String marketplaceId) throws ObjectNotFoundException {

    ArgumentValidator.notNull("marketplaceId", marketplaceId);

    Marketplace mp = new Marketplace(marketplaceId);
    mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
    LocalizerFacade facade =
        new LocalizerFacade(
            localizer,
            (dm.getCurrentUserIfPresent() == null)
                ? "en"
                : dm.getCurrentUserIfPresent().getLocale());

    return MarketplaceAssembler.toVOMarketplace(mp, facade);
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void banOrganizationsFromMarketplace(List<String> organizationIds, String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException,
          OrganizationAuthorityException, MarketplaceAccessTypeUneligibleForOperationException,
          OrganizationAlreadyBannedException {

    ArgumentValidator.notNull("organizationIds", organizationIds);
    ArgumentValidator.notNull("marketplaceId", marketplaceId);

    String supplierId = "";
    try {
      Marketplace mp = new Marketplace(marketplaceId.trim());
      mp = (Marketplace) dm.getReferenceByBusinessKey(mp);
      if (!mp.isOpen()) {

        String msg =
            "banSuppliersFromMarketplace() must not be invoked for non-open marketplaces, but was invoked for the non-open marketplace with id ";
        String[] params = new String[4];
        params[0] = "banSuppliersFromMarketplace";
        params[1] = "open";
        params[2] = marketplaceId;
        params[3] = "non-open";

        prepareExceptionAndThrow(msg, params);
      }
      PermissionCheck.owns(mp, dm.getCurrentUser().getOrganization(), logger, null);
      for (String organizationId : organizationIds) {
        organizationId = organizationId.trim();
        Organization supplier = new Organization();
        supplier.setOrganizationId(organizationId);
        supplier = (Organization) dm.getReferenceByBusinessKey(supplier);
        supplierId = organizationId;
        checkSellerRole(supplier);

        MarketplaceToOrganization mtoNew =
            new MarketplaceToOrganization(mp, supplier, PublishingAccess.PUBLISHING_ACCESS_DENIED);

        MarketplaceToOrganization mto = (MarketplaceToOrganization) dm.find(mtoNew);
        if (mto == null) {
          mto = mtoNew;
          dm.persist(mto);
          dm.flush();
        } else {
          if (PublishingAccess.PUBLISHING_ACCESS_GRANTED.equals(mto.getPublishingAccess())) {
            mto.setPublishingAccess(PublishingAccess.PUBLISHING_ACCESS_DENIED);
            unlinkServices(mp, supplier, dm);
          } else if (PublishingAccess.PUBLISHING_ACCESS_DENIED.equals(mto.getPublishingAccess())) {
            throw new NonUniqueBusinessKeyException();
          }
        }
        marketplaceServiceLocal.sendNotification(
            EmailType.MARKETPLACE_SUPPLIER_BANNED, mp, supplier.getKey());
      }
    } catch (ObjectNotFoundException | OrganizationAuthorityException e) {
      sessionCtx.setRollbackOnly();
      throw e;
    } catch (NonUniqueBusinessKeyException e) {
      sessionCtx.setRollbackOnly();
      OrganizationAlreadyBannedException ex =
          new OrganizationAlreadyBannedException(
              "Supplier "
                  + supplierId
                  + " has already been banned from publishing on the marketplace "
                  + marketplaceId);
      logger.logError(
          Log4jLogger.SYSTEM_LOG,
          ex,
          LogMessageIdentifier.ERROR_SUPPLIER_ALREADY_BANNED_FROM_MARKETPLACE,
          supplierId,
          marketplaceId);
      throw ex;
    }
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void liftBanOrganizationsFromMarketplace(
      List<String> organizationIds, String marketplaceId)
      throws ObjectNotFoundException, OperationNotPermittedException,
          MarketplaceAccessTypeUneligibleForOperationException, OrganizationAuthorityException {
    ArgumentValidator.notNull("organizationIds", organizationIds);
    try {
      Marketplace mp = getAndValidateMarketplace(marketplaceId);
      if (!mp.isOpen()) {
        String msg =
            "liftBanSuppliersFromMarketplace() must not be invoked for non-open marketplaces, but was invoked for the non-open marketplace with id ";
        String[] params = new String[4];
        params[0] = "liftBanSuppliersFromMarketplace";
        params[1] = "open";
        params[2] = marketplaceId;
        params[3] = "non-open";
        prepareExceptionAndThrow(msg, params);
      }
      for (String organizationId : organizationIds) {
        organizationId = organizationId.trim();
        Organization supplier = new Organization();
        supplier.setOrganizationId(organizationId);
        supplier = (Organization) dm.getReferenceByBusinessKey(supplier);
        checkSellerRole(supplier);
        MarketplaceToOrganization mto = new MarketplaceToOrganization(mp, supplier);
        try {
          mto = (MarketplaceToOrganization) dm.getReferenceByBusinessKey(mto);
          if (PublishingAccess.PUBLISHING_ACCESS_DENIED.equals(mto.getPublishingAccess())) {
            // we know that no active services of this supplier can
            // exist in this mp because he was banned till now,
            // thus simply remove mto relation
            // => at next time of publishing, a new mto relation
            // with ACESS_GRANTED will be created
            dm.remove(mto);
            marketplaceServiceLocal.sendNotification(
                EmailType.MARKETPLACE_SUPPLIER_LIFTED_BAN, mp, supplier.getKey());
          }

        } catch (ObjectNotFoundException e) {
          // already gone, don't care
        }
      }
      dm.flush();
    } catch (ObjectNotFoundException e) {
      sessionCtx.setRollbackOnly();
      throw e;
    }
  }

  @Override
  public String getBrandingUrl(String marketplaceId) throws ObjectNotFoundException {

    ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

    Marketplace marketplace = new Marketplace();
    marketplace.setMarketplaceId(marketplaceId);
    marketplace = (Marketplace) dm.getReferenceByBusinessKey(marketplace);

    return marketplace.getBrandingUrl();
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void saveBrandingUrl(VOMarketplace marketplace, String brandingUrl)
      throws ObjectNotFoundException, ValidationException, OperationNotPermittedException,
          ConcurrentModificationException {

    ArgumentValidator.notNull("marketplace", marketplace);
    if (brandingUrl != null && brandingUrl.trim().length() == 0) {
      brandingUrl = null;
    }
    BLValidator.isAbsoluteOrRelativeUrl("brandingUrl", brandingUrl, false);

    Marketplace marketplaceObj = new Marketplace();
    marketplaceObj.setMarketplaceId(marketplace.getMarketplaceId());
    marketplaceObj = (Marketplace) dm.getReferenceByBusinessKey(marketplaceObj);

    BaseAssembler.verifyVersionAndKey(marketplaceObj, marketplace);

    PlatformUser user = dm.getCurrentUser();
    Organization organization = user.getOrganization();
    PermissionCheck.owns(marketplaceObj, organization, logger, null);

    marketplaceObj.setBrandingUrl(brandingUrl);
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public List<VOOrganization> getAllOrganizations(String marketplaceId)
      throws ObjectNotFoundException {
    List<VOOrganization> voOrganizations = new ArrayList<>();

    List<Object[]> organizations =
        marketplaceServiceLocal.getOrganizationsWithMarketplaceAccess(marketplaceId);

    for (Object[] object : organizations) {
      VOOrganization voOrganization = new VOOrganization();

      BigInteger orgKey = (BigInteger) object[0];
      voOrganization.setKey(orgKey.longValue());
      voOrganization.setOrganizationId((String) object[1]);
      voOrganization.setName((String) object[2]);
      boolean hasAccess = (object[3] == null) ? false : true;
      voOrganization.setHasGrantedAccessToMarketplace(hasAccess);

      BigInteger noOfSubs = (BigInteger) object[4];
      boolean hasSubscriptions = noOfSubs.intValue() > 0;
      voOrganization.setHasSubscriptions(hasSubscriptions);

      BigInteger noOfServices = (BigInteger) object[5];
      boolean hasPublishedServices = noOfServices.intValue() > 0;
      voOrganization.setHasPublishedServices(hasPublishedServices);

      voOrganizations.add(voOrganization);
    }

    return voOrganizations;
  }

  @Override
  public List<VOMarketplace> getRestrictedMarketplaces() {

    long orgKey = dm.getCurrentUser().getOrganization().getKey();
    List<Marketplace> marketplaces =
        marketplaceServiceLocal.getMarketplacesForOrganizationWithRestrictedAccess(orgKey);

    List<VOMarketplace> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());

    for (Marketplace mp : marketplaces) {
      result.add(MarketplaceAssembler.toVOMarketplace(mp, facade));
    }

    return result;
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void closeMarketplace(
      String marketplaceId, Set<Long> authorizedOrganizations, Set<Long> unauthorizedOrganizations)
      throws OperationNotPermittedException, ObjectNotFoundException, NonUniqueBusinessKeyException,
          TechnicalServiceNotAliveException, TechnicalServiceOperationException {

    Marketplace marketplace = marketplaceServiceLocal.getMarketplaceForId(marketplaceId);

    if (!marketplace.isRestricted()) {
      marketplace = marketplaceServiceLocal.updateMarketplaceAccessType(marketplaceId, true);
    }

    for (Long orgKey : authorizedOrganizations) {
      Organization organization = new Organization();
      organization.setKey(orgKey.longValue());
      marketplaceServiceLocal.grantAccessToMarketPlaceToOrganization(marketplace, organization);
    }

    for (Long orgKey : unauthorizedOrganizations) {
      marketplaceServiceLocal.removeMarketplaceAccess(marketplace.getKey(), orgKey.longValue());
    }

    marketplaceCache.resetConfiguration(marketplaceId);
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void grantAccessToMarketPlaceToOrganization(
      VOMarketplace voMarketplace, VOOrganization voOrganization)
      throws ValidationException, NonUniqueBusinessKeyException {
    Organization organization = OrganizationAssembler.toOrganization(voOrganization);
    Marketplace marketplace = MarketplaceAssembler.toMarketplaceWithKey(voMarketplace);
    marketplaceServiceLocal.grantAccessToMarketPlaceToOrganization(marketplace, organization);

    marketplaceCache.resetConfiguration(voMarketplace.getMarketplaceId());
  }

  @Override
  @RolesAllowed("MARKETPLACE_OWNER")
  public void openMarketplace(String marketplaceId)
      throws OperationNotPermittedException, ObjectNotFoundException,
          NonUniqueBusinessKeyException {
    Marketplace marketplace = marketplaceServiceLocal.getMarketplaceForId(marketplaceId);
    if (!marketplace.isRestricted()) {
      return;
    }
    marketplace = marketplaceServiceLocal.updateMarketplaceAccessType(marketplaceId, false);
    marketplaceServiceLocal.removeMarketplaceAccesses(marketplace.getKey());

    marketplaceCache.resetConfiguration(marketplaceId);
  }

  @Override
  public boolean doesOrganizationHaveAccessMarketplace(String marketplaceId, String organizationId)
      throws LoginException {

    VOMarketplace voMarketplace = null;
    try {
      voMarketplace = getMarketplaceById(marketplaceId);

      if (!voMarketplace.isRestricted()) {
        return true;
      }
      Organization orga = new Organization();
      orga.setOrganizationId(organizationId);
      Organization organization = (Organization) dm.getReferenceByBusinessKey(orga);
      return marketplaceServiceLocal.doesAccessToMarketplaceExistForOrganization(
          voMarketplace.getKey(), organization.getKey());
    } catch (ObjectNotFoundException e) {
      throw new LoginException();
    }
  }

  @Override
  public List<VOOrganization> getAllOrganizationsWithAccessToMarketplace(String marketplaceId) {

    VOMarketplace voMarketplace = null;
    try {
      voMarketplace = getMarketplaceById(marketplaceId);

      if (!voMarketplace.isRestricted()) {
        return new ArrayList<>();
      }

      List<Organization> orgList =
          marketplaceServiceLocal.getAllOrganizationsWithAccessToMarketplace(
              voMarketplace.getKey());

      List<VOOrganization> result = new ArrayList<>();

      for (Organization org : orgList) {
        result.add(OrganizationAssembler.toVOOrganization(org));
      }

      return result;

    } catch (ObjectNotFoundException e) {
      return new ArrayList<>();
    }
  }

  @Override
  public MarketplaceConfiguration getCachedMarketplaceConfiguration(String marketplaceId) {

    return marketplaceCache.getConfiguration(marketplaceId);
  }

  @Override
  public void clearCachedMarketplaceConfiguration(String marketplaceId) {
    marketplaceCache.resetConfiguration(marketplaceId);
  }

  @Override
  @RolesAllowed("PLATFORM_OPERATOR")
  public List<VOMarketplace> getAllMarketplacesForTenant(Long tenantKey)
      throws ObjectNotFoundException {
    List<Marketplace> marketplaces = marketplaceServiceLocal.getAllMarketplacesForTenant(tenantKey);
    List<VOMarketplace> result = new ArrayList<>();
    LocalizerFacade facade = new LocalizerFacade(localizer, dm.getCurrentUser().getLocale());
    for (Marketplace mp : marketplaces) {
      result.add(MarketplaceAssembler.toVOMarketplace(mp, facade));
    }
    return result;
  }

  @Override
  public String getTenantIdFromMarketplace(String marketplaceId) throws ObjectNotFoundException {
    VOMarketplace marketplace = getMarketplaceById(marketplaceId);
    return marketplace.getTenantId();
  }

  @Override
  public String getMarketplaceIdForKey(Long key) throws ObjectNotFoundException {
    Marketplace mp = dm.getReference(Marketplace.class, key);
    return mp.getMarketplaceId();
  }
}
