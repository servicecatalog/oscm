/*******************************************************************************
 *  
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Enes Sejfi                                                     
 *                                                                              
 *  Creation Date: 15.06.2012                                                       
 *                                                                              
 *  Completion Time: 22.06.2012
 *                                                                              
 *******************************************************************************/
package org.oscm.marketplace.bean;

import java.math.BigDecimal;
import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.persistence.Query;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;
import org.oscm.internal.vo.VOService;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.landingpageService.local.LandingpageType;
import org.oscm.landingpageService.local.VOLandingpageService;
import org.oscm.landingpageService.local.VOPublicLandingpage;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.marketplace.assembler.LandingpageAssembler;
import org.oscm.marketplace.assembler.LandingpageProductAssembler;
import org.oscm.permission.PermissionCheck;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.ArgumentValidator;


@Stateless
@Local(LandingpageServiceLocal.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class LandingpageServiceBeanLocal implements LandingpageServiceLocal {

    private static final String IS_PRODUCT_ON_MPL = "SELECT ce FROM CatalogEntry ce WHERE ce.product=:service AND ce.marketplace=:marketplace";

    @EJB
    protected DataService dm;

    @EJB
    private LocalizerServiceLocal localizer;

    @EJB
    MarketplaceCacheService marketplaceCache;

    @Inject
    UserGroupServiceLocalBean userGroupService;

    class ProductComparatorRating implements Comparator<Product> {

        final ProductComparatorName nameComp;

        public ProductComparatorRating(LocalizerFacade facade) {
            nameComp = new ProductComparatorName(facade);
        }

        @Override
        public int compare(Product x, Product y) {
            BigDecimal averageRatingX = new BigDecimal(0);
            BigDecimal averageRatingY = new BigDecimal(0);

            ProductFeedback xFeedback = x.getProductTemplate()
                    .getProductFeedback();
            if (xFeedback != null) {
                averageRatingX = xFeedback.getAverageRating();
            }

            ProductFeedback yFeedback = y.getProductTemplate()
                    .getProductFeedback();
            if (yFeedback != null) {
                averageRatingY = yFeedback.getAverageRating();
            }

            int compareTo = averageRatingY.compareTo(averageRatingX);
            if (compareTo == 0) {
                // on same ratings compare names
                return nameComp.compare(x, y);
            }
            return compareTo;
        }
    }

    class ProductComparatorName implements Comparator<Product> {

        LocalizerFacade facade;

        public ProductComparatorName(LocalizerFacade facade) {
            super();
            this.facade = facade;
        }

        @Override
        public int compare(Product x, Product y) {
            String nameX = ProductAssembler.getServiceName(x, facade);
            String nameY = ProductAssembler.getServiceName(y, facade);
            return nameX.compareTo(nameY);
        }
    }

    public static EnumSet<ServiceStatus> FILTER_OUT_WITH_STATUS = EnumSet.of(
            ServiceStatus.DELETED, ServiceStatus.SUSPENDED);

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LandingpageServiceBeanLocal.class);

    @Override
    public LandingpageType loadLandingpageType(String marketplaceId)
            throws ObjectNotFoundException {
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        Marketplace marketplace = loadMarketplace(marketplaceId);
        return marketplace.getPublicLandingpage() != null ? LandingpageType.PUBLIC
                : LandingpageType.ENTERPRISE;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public VOPublicLandingpage loadPublicLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        // preconditions
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        Marketplace marketplace = loadMarketplace(marketplaceId);
        PermissionCheck.owns(marketplace,
                dm.getCurrentUser().getOrganization(), logger, null);
        PublicLandingpage landingpage = marketplace.getPublicLandingpage();
        if (landingpage == null)
            return null;

        // load
        VOPublicLandingpage voLandingpage = LandingpageAssembler
                .toVOLandingpage(landingpage);
        voLandingpage
                .setLandingpageServices(getLandingpageServices(landingpage));

        logger.logDebug("loadLandingpageConfig(String) exited");
        return voLandingpage;
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public void savePublicLandingpageConfig(VOPublicLandingpage landingpage)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, FillinOptionNotSupportedException {

        // preconditions
        ArgumentValidator.notNull("landingpage", landingpage);
        ArgumentValidator.notEmptyString("landingpage.marketplaceId",
                landingpage.getMarketplaceId());
        Marketplace marketplace = loadMarketplace(landingpage
                .getMarketplaceId());
        PermissionCheck.owns(marketplace,
                dm.getCurrentUser().getOrganization(), logger, null);
        checkRatingEnablement(marketplace, landingpage);

        // switch from enterprise landing page, or update
        if (marketplace.getEnterpriseLandingpage() != null) {
            dm.remove(marketplace.getEnterpriseLandingpage());
            marketplace.setEnterpiseLandingpage(null);
            PublicLandingpage newLandingpage = LandingpageAssembler
                    .toLandingpage(landingpage);
            newLandingpage.setMarketplace(marketplace);
            marketplace.setPublicLandingpage(newLandingpage);
        } else {
            LandingpageAssembler.updateLandingpage(
                    marketplace.getPublicLandingpage(), landingpage);
        }

        // set landing page products
        PublicLandingpage dbLandingpage = marketplace.getPublicLandingpage();
        deleteLandingpageProducts(dbLandingpage);
        int position = 1;
        for (VOLandingpageService voLandingpageService : landingpage
                .getLandingpageServices()) {
            Product product = loadProduct(voLandingpageService.getService());
            if (product != null
                    && product.getStatus().isStatusValidForLandingPage()) {
                checkMarketplace(product, marketplace);
                voLandingpageService.setPosition(position);
                position++;
                LandingpageProduct landingpageProduct = LandingpageProductAssembler
                        .toLandingpageProduct(voLandingpageService);
                landingpageProduct.setProduct(product);
                dbLandingpage.getLandingpageProducts().add(landingpageProduct);
                landingpageProduct.setLandingpage(dbLandingpage);
            }
        }
        dm.persist(dbLandingpage);
        dm.flush();
        marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());
        logger.logDebug("saveLandingpageConfig(VOLandingpage) exited");
    }

    private void checkRatingEnablement(Marketplace marketplace,
            VOPublicLandingpage landingpage)
            throws FillinOptionNotSupportedException {
        if (!marketplace.isReviewEnabled()
                && FillinCriterion.RATING_DESCENDING.equals(landingpage
                        .getFillinCriterion()))
            throw new FillinOptionNotSupportedException();
    }

    private void checkMarketplace(Product product, Marketplace marketplace)
            throws ConcurrentModificationException {

        Query query = dm.createQuery(IS_PRODUCT_ON_MPL)
                .setParameter("service", product)
                .setParameter("marketplace", marketplace);

        List<?> catalogEntries = query.getResultList();
        if (catalogEntries == null || catalogEntries.isEmpty()) {
            throw new ConcurrentModificationException();
        }

    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public void saveEnterpriseLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            NonUniqueBusinessKeyException {

        // check preconditions
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        Marketplace marketplace = loadMarketplace(marketplaceId);
        PermissionCheck.owns(marketplace,
                dm.getCurrentUser().getOrganization(), logger, null);

        // remove old landing page, if needed
        PublicLandingpage oldLandingpage = marketplace.getPublicLandingpage();
        if (oldLandingpage != null) {
            marketplace.setPublicLandingpage(null);
            dm.remove(oldLandingpage);
        }

        // save new landing page
        EnterpriseLandingpage newLandingpage = new EnterpriseLandingpage();
        newLandingpage.setMarketplace(marketplace);
        dm.persist(newLandingpage);
        marketplace.setEnterpiseLandingpage(newLandingpage);

        marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public void resetLandingpage(String marketplaceId)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            OperationNotPermittedException {

        // check preconditions
        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);
        Marketplace marketplace = loadMarketplace(marketplaceId);
        PermissionCheck.owns(marketplace,
                dm.getCurrentUser().getOrganization(), logger, null);

        // switch from enterprise landing page to public landing page, if needed
        if (marketplace.getEnterpriseLandingpage() != null) {
            dm.remove(marketplace.getEnterpriseLandingpage());
            marketplace.setEnterpiseLandingpage(null);
            PublicLandingpage defaultPage = PublicLandingpage.newDefault();
            marketplace.setPublicLandingpage(defaultPage);
            defaultPage.setMarketplace(marketplace);
            dm.persist(defaultPage);
            dm.flush();
        }

        // set defaults
        marketplace.getPublicLandingpage().setDefaults();
        deleteLandingpageProducts(marketplace.getPublicLandingpage());

        dm.persist(marketplace.getPublicLandingpage());
        dm.flush();

        marketplaceCache.resetConfiguration(marketplace.getMarketplaceId());
        logger.logDebug("resetLandingPage(String) exited");
    }

    @Override
    @RolesAllowed("MARKETPLACE_OWNER")
    public List<VOService> availableServices(
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException {

        ArgumentValidator.notEmptyString("marketplaceId", marketplaceId);

        Marketplace marketplace = loadMarketplace(marketplaceId);
        PermissionCheck.owns(marketplace,
                dm.getCurrentUser().getOrganization(), logger, null);

        List<VOService> result = new LinkedList<VOService>();
        LocalizerFacade facade = new LocalizerFacade(localizer, dm
                .getCurrentUser().getLocale());

        Query query = dm
                .createNamedQuery("Product.getPublishedProductTemplates")
                .setParameter("filterOutWithStatus", FILTER_OUT_WITH_STATUS)
                .setParameter("marketplace", marketplace);

        @SuppressWarnings("unchecked")
        List<Product> products = query.getResultList();
        for (Product product : products) {
            result.add(ProductAssembler.toVOProduct(product, facade,
                    PerformanceHint.ONLY_FIELDS_FOR_LISTINGS));
        }
        return result;
    }

    @Override
    public List<VOService> servicesForPublicLandingpage(String marketplaceId,
            String locale) throws ObjectNotFoundException {

        ArgumentValidator.notNull("marketplaceId", marketplaceId);
        ArgumentValidator.notNull("locale", locale);
        List<VOService> voServices = new ArrayList<VOService>();

        // get featured product list
        Marketplace marketplace = loadMarketplace(marketplaceId);
        PublicLandingpage landingpage = marketplace.getPublicLandingpage();
        PlatformUser currentUser = dm.getCurrentUserIfPresent();
        List<Long> invisibleProductKeys = getInvisibleProducts(currentUser);

        voServices = getFeaturedServices(landingpage, currentUser,
                invisibleProductKeys, locale);

        // restrict or fill list
        adaptFeaturedServiceList(marketplace, voServices, currentUser,
                invisibleProductKeys, locale);

        return voServices;
    }

    @Override
    public void removeProductFromLandingpage(Marketplace marketplace,
            Product product) {
        PublicLandingpage landingpage = marketplace.getPublicLandingpage();
        if (landingpage == null) {
            return;
        }

        List<LandingpageProduct> landingpageProducts = landingpage
                .getLandingpageProducts();
        for (LandingpageProduct landingpageProduct : landingpageProducts) {
            if (landingpageProduct.getProduct().getProductId()
                    .equals(product.getProductId())) {
                landingpageProducts.remove(landingpageProduct);
                dm.remove(landingpageProduct);
                break;
            }
        }

    }

    @Override
    public PublicLandingpage createDefaultLandingpage() {
        return PublicLandingpage.newDefault();
    }

    private Marketplace loadMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        return (Marketplace) dm.getReferenceByBusinessKey(new Marketplace(
                marketplaceId));
    }

    private Product loadProduct(VOService voService) {
        return dm.find(Product.class, voService.getKey());
    }

    private List<VOLandingpageService> getLandingpageServices(
            PublicLandingpage landingpage) {
        List<VOLandingpageService> result = new LinkedList<VOLandingpageService>();

        LocalizerFacade facade = new LocalizerFacade(localizer, dm
                .getCurrentUser().getLocale());

        for (LandingpageProduct landingpageProduct : loadLandingpageProducts(landingpage)) {
            VOLandingpageService voLandingpageService = LandingpageProductAssembler
                    .toVOLandingpageService(landingpageProduct);

            VOService voService = ProductAssembler.toVOProduct(
                    landingpageProduct.getProduct(), facade,
                    PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
            voLandingpageService.setService(voService);
            result.add(voLandingpageService);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<LandingpageProduct> loadLandingpageProducts(
            PublicLandingpage landingpage) {
        return dm.createNamedQuery("LandingpageProduct.getLandingpageProducts")
                .setParameter("landingpage", landingpage)
                .setParameter("filterOutWithStatus", FILTER_OUT_WITH_STATUS)
                .getResultList();
    }

    private void deleteLandingpageProducts(PublicLandingpage dbLandingpage) {
        dm.createNamedQuery("LandingpageProduct.deleteLandingpageProducts")
                .setParameter("landingpage", dbLandingpage).executeUpdate();
        dbLandingpage.getLandingpageProducts().clear();
    }

    private boolean isUserLoggedIn(PlatformUser currentUser) {
        return currentUser != null;
    }

    private List<VOService> getFeaturedServices(PublicLandingpage landingpage,
            PlatformUser currentUser, List<Long> invisibleProductKeys,
            String locale) {

        // determine products and convert to VO services
        List<VOService> voServices = new ArrayList<VOService>();
        LocalizerFacade facade = new LocalizerFacade(localizer, locale);
        for (LandingpageProduct landingpageProduct : landingpage
                .getLandingpageProducts()) {
            Product product = landingpageProduct.getProduct();

            boolean visibleOnLandingPage = isProductVisibleOnLandingPage(
                    product, landingpage, currentUser);

            if (visibleOnLandingPage) {
                if (isUserLoggedIn(currentUser)) {
                    // logged in; take customer specific product if existing
                    Query query = dm
                            .createNamedQuery(
                                    "Product.getSpecificCustomerProduct")
                            .setParameter("template", product)
                            .setParameter("customer",
                                    currentUser.getOrganization());

                    @SuppressWarnings("unchecked")
                    List<Product> custProducts = query.getResultList();
                    if (custProducts.size() > 0) {
                        product = custProducts.get(0);
                    }
                }

                if (ServiceStatus.ACTIVE.equals(product.getStatus())
                        && isVisibleProduct(invisibleProductKeys, product)) {
                    VOService voService = ProductAssembler.toVOProduct(product,
                            facade, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                    voServices.add(voService);
                }

            }
        }
        return voServices;
    }

    private boolean isProductVisibleOnLandingPage(Product product,
            PublicLandingpage landingPage, PlatformUser currentUser) {

        CatalogEntry catalogEntry = product
                .getCatalogEntryForMarketplace(landingPage.getMarketplace());
        if (catalogEntry == null) {
            return false;
        }
        if (!catalogEntry.isVisibleInCatalog()) {
            return false;
        }
        if (!catalogEntry.isAnonymousVisible() && !isUserLoggedIn(currentUser)) {
            return false;
        }
        return true;
    }

    private boolean isVisibleProduct(List<Long> invisibleProductKeys,
            Product product) {
        return (invisibleProductKeys == null)
                || (!invisibleProductKeys.contains(Long.valueOf(product
                        .getKey())));
    }

    List<Product> removeInvisibleProducts(List<Product> products,
            List<Long> invisibleProductKeys) {
        if (invisibleProductKeys == null) {
            return products;
        }
        Set<Long> invisibleProductKeySet = new HashSet<Long>();
        invisibleProductKeySet.addAll(invisibleProductKeys);

        List<Product> visibleProducts = new ArrayList<Product>();

        for (int i = 0; i < products.size(); i++) {
            Product prod = products.get(i);
            if (!invisibleProductKeySet.contains(Long.valueOf(prod.getKey()))) {
                visibleProducts.add(prod);
            }
        }

        return visibleProducts;
    }

    List<Long> getInvisibleProducts(PlatformUser currentUser)
            throws ObjectNotFoundException {
        if (currentUser != null && !currentUser.isOrganizationAdmin()) {
            return userGroupService.getInvisibleProductKeysForUser(currentUser
                    .getKey());
        }
        return null;
    }

    void adaptFeaturedServiceList(Marketplace marketplace,
            List<VOService> voServices, PlatformUser currentUser,
            List<Long> invisibleProductKeys, String locale) {
        int servicesMax = marketplace.getPublicLandingpage()
                .getNumberServices();
        int servicesSize = voServices.size();
        if (servicesSize > servicesMax) {
            for (int i = servicesSize - 1; i >= servicesMax; i--) {
                voServices.remove(i);
            }
        } else {
            if (servicesSize == servicesMax) {
                // nothing to do
                return;
            }

            FillinCriterion fillinCriterion = marketplace
                    .getPublicLandingpage().getFillinCriterion();
            if (fillinCriterion == FillinCriterion.NO_FILLIN) {
                // nothing to do
                return;
            }

            List<Product> products = new ArrayList<Product>();
            if (isUserLoggedIn(currentUser)) {
                products = retrieveCustomerProducts(marketplace, currentUser);
            } else {
                products = retrieveTemplateProducts(marketplace);
            }

            // FillinCriterion.ACTIVATION_DESCENDING sorted by database

            Set<Long> keys = getKeysForLocalization(products);

            LocalizerFacade facade = new LocalizerFacade(localizer, locale);
            // read at least name for comparison
            facade.prefetch(new ArrayList<Long>(keys), Arrays.asList(
                    LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                    LocalizedObjectTypes.PRODUCT_SHORT_DESCRIPTION,
                    LocalizedObjectTypes.PRODUCT_MARKETING_DESC,
                    LocalizedObjectTypes.PRODUCT_LICENSE_DESC,
                    LocalizedObjectTypes.PRODUCT_CUSTOM_TAB_NAME));
            if (fillinCriterion == FillinCriterion.RATING_DESCENDING) {
                Collections.sort(products, new ProductComparatorRating(facade));
            }

            if (fillinCriterion == FillinCriterion.NAME_ASCENDING) {
                Collections.sort(products, new ProductComparatorName(facade));
            }

            products = removeInvisibleProducts(products, invisibleProductKeys);

            int fillInNumber = numberToFillin(servicesMax, servicesSize,
                    products.size());

            // convert to VO services
            for (int i = 0; i < fillInNumber; i++) {
                VOService voService = ProductAssembler.toVOProduct(
                        products.get(i), facade,
                        PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                voServices.add(voService);
            }
        }
    }

    Set<Long> getKeysForLocalization(List<Product> products) {
        Set<Long> keys = new HashSet<Long>();
        for (Product p : products) {
            // Bug #12479 Only template keys because the localized sources
            // are only on templates
            keys.add(Long.valueOf(p.getTemplateOrSelf().getKey()));
        }
        return keys;
    }

    private int numberToFillin(int servicesMax, int servicesSize,
            int productSize) {
        int fillInNumber = servicesMax - servicesSize;
        if (fillInNumber > productSize) {
            fillInNumber = productSize;
        }
        return fillInNumber;
    }

    private List<Product> retrieveTemplateProducts(Marketplace marketplace) {
        Query query = dm
                .createNamedQuery("Product.getActivePublishedProductTemplates");
        query.setParameter("marketplaceId", marketplace.getMarketplaceId());

        @SuppressWarnings("unchecked")
        List<Product> products = query.getResultList();
        return products;
    }

    private List<Product> retrieveCustomerProducts(Marketplace marketplace,
            PlatformUser currentUser) {
        Query query = dm.createNamedQuery("Product.getActivePublishedProducts");
        query.setParameter("customer", currentUser.getOrganization());
        query.setParameter("marketplaceId", marketplace.getMarketplaceId());

        @SuppressWarnings("unchecked")
        List<Product> products = query.getResultList();
        return products;
    }

}
