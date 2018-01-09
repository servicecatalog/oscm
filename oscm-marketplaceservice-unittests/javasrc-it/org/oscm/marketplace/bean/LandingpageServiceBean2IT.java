/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: brandstetter                                                    
 *                                                                              
 *  Creation Date: 20.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductFeedback;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOService;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

public class LandingpageServiceBean2IT extends EJBTestBase {

    private final String locale = "en";
    private DataService ds;
    private LandingpageServiceLocal landingpageService;
    private LocalizerServiceLocal localizer;
    private Organization platformOperatorOrg;
    private static PlatformUser platformOperatorUser;
    private TechnicalProduct technicalProduct;
    private Organization supplierOrg;
    private static final String SUPPLIER_MP_ID = "SUPPLIER_MP_ID";
    private Marketplace supplierMp;
    private long supplierKey;
    private static final int NUMBER_OF_TEMP_PRODUCTS_PUBLISHED = 6;
    private static final int NUMBER_OF_CUST_PRODUCTS_PUBLISHED = 2;
    private static final int NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLISHED = 2;
    private static final int NUMBER_OF_TEMP_PRODUCTS_NOT_ACTIVE = 3;
    private static final int NUMBER_OF_TEMP_PRODUCTS_NOT_VISIBLE_IN_CATALOG = 3;
    private static final int NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLIC = 3;
    private final List<Product> customerProducts = new ArrayList<>();
    private List<Product> templateProductsPublished = new ArrayList<>();
    private List<Product> templateProductsNotPublished = new ArrayList<>();
    private List<Product> templateProductsNotActive = new ArrayList<>();
    private List<Product> templateProductsNotVisible = new ArrayList<>();
    private List<Product> templateProductsNotPublic = new ArrayList<>();
    private final List<ProductFeedback> productFeedbacks = new ArrayList<>();
    private final String idPrefixPublished = "productId_";
    private final String idPrefixNotPublished = "z_productIdNotPublished_";
    private final String idPrefixNotActive = "zz_productIdNotActive_";
    private final String idPrefixNotVisible = "zz_productIdNotVisible_";
    private final String idPrefixNotPublic = "zz_productIdNotPublic_";
    private final String[] templateProdNamesPublished = { "f0", "x1", "z2",
            "a3", "j4", "c5" };
    private final String[] templateProdNamesNotPublished = { "b6", "a7" };
    private final String[] templateProdNamesNotPublic = { "zz1", "zz2", "zz3" };
    private final String[] customerProdNames = { "bb0", "aa1" };
    private final BigDecimal[] averageRatingsPublished = {
            BigDecimal.valueOf(3.0), BigDecimal.valueOf(1.1),
            BigDecimal.valueOf(2.2), BigDecimal.valueOf(1.3),
            BigDecimal.valueOf(0.0), BigDecimal.valueOf(1.5) };
    private final List<ProductAndPosition> inputData = new ArrayList<>();
    private UserGroupServiceLocalBean userGroupService;

    private class ProductAndPosition {
        Product product;
        int position;

        public ProductAndPosition(Product product, int position) {
            super();
            this.product = product;
            this.position = position;
        }

        public Product getProduct() {
            return product;
        }

    }

    class ProductAndPositionComparator
            implements Comparator<ProductAndPosition> {
        @Override
        public int compare(ProductAndPosition x, ProductAndPosition y) {
            return x.position - y.position;
        }
    }

    private void createPlatformOperator() throws Exception {
        if (platformOperatorOrg == null) {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    platformOperatorOrg = Organizations.createOrganization(ds,
                            OrganizationRoleType.PLATFORM_OPERATOR);

                    platformOperatorUser = Organizations.createUserForOrg(ds,
                            platformOperatorOrg, true, "Administrator");
                    PlatformUsers.grantRoles(ds, platformOperatorUser,
                            UserRoleType.PLATFORM_OPERATOR);
                    return null;
                }
            });
        }
    }

    private void createSupplierOrganization() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplierOrg = Organizations.createOrganization(ds,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(ds, supplierOrg, true, "admin");
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.MARKETPLACE_OWNER);
                supplierKey = createUserForOrg.getKey();
                return null;
            }
        });
    }

    private PlatformUser createPlatformUser(final String userId,
            final Organization org) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser user = PlatformUsers.createUser(ds, userId, org);
                return user;
            }
        });
    }

    private void createSupplierMarketplace() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplierMp = Marketplaces.createMarketplace(supplierOrg,
                        SUPPLIER_MP_ID, false, ds);
                return null;
            }
        });
    }

    private TechnicalProduct createTechnicalProduct() throws Exception {
        return runTX(new Callable<TechnicalProduct>() {
            @Override
            public TechnicalProduct call() throws Exception {
                technicalProduct = TechnicalProducts.createTechnicalProduct(ds,
                        supplierOrg, "tp", false, ServiceAccessType.LOGIN);
                return technicalProduct;
            }
        });
    }

    private List<Product> createProducts(final int num, final String idPrefix,
            final String[] names, final ServiceStatus status,
            final boolean published, final boolean visibleInCatalog,
            final boolean publicAccess, final int numCustomerProducts,
            final BigDecimal[] rating, final long creationTimeBase)
            throws Exception {
        return runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() throws Exception {

                List<Product> productList = new ArrayList<>();
                for (int i = 0; i < num; i++) {
                    Product product = Products.createProduct(supplierOrg,
                            technicalProduct, false, idPrefix + i,
                            "priceModelId_" + i, ds);
                    product.getPriceModel().getKey();
                    product.setProvisioningDate(creationTimeBase + (i * 100));
                    product.setStatus(status);
                    productList.add(product);
                    ds.persist(product);

                    if (names != null) {
                        localizer.storeLocalizedResource(locale,
                                product.getKey(),
                                LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                                names[i]);
                    }

                    if (published) {
                        CatalogEntry catalogEntry = new CatalogEntry();
                        catalogEntry.setProduct(product);
                        catalogEntry.setMarketplace(supplierMp);
                        catalogEntry.setVisibleInCatalog(visibleInCatalog);
                        catalogEntry.setAnonymousVisible(publicAccess);
                        catalogEntry.setMarketplace(supplierMp);
                        ds.persist(catalogEntry);
                    }

                    if (rating != null) {
                        ProductFeedback productFeedback = new ProductFeedback();
                        productFeedback.setProduct(product);
                        productFeedback.setAverageRating(rating[i]);
                        ds.persist(productFeedback);
                        productFeedbacks.add(productFeedback);
                    }

                    if (i < numCustomerProducts) {
                        Product custSpecProduct;
                        custSpecProduct = Products.createCustomerSpecifcProduct(
                                ds, supplierOrg, product, status);
                        custSpecProduct.getPriceModel().getKey();
                        custSpecProduct.setProvisioningDate(
                                product.getProvisioningDate() + 1);
                        customerProducts.add(custSpecProduct);
                        ds.persist(custSpecProduct);

                        localizer.storeLocalizedResource(locale,
                                custSpecProduct.getKey(),
                                LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                                customerProdNames[i]);

                    }

                }
                return productList;
            }
        });

    }

    private void createProducts() throws Exception {

        // candidates for visible on landing page
        templateProductsPublished = createProducts(
                NUMBER_OF_TEMP_PRODUCTS_PUBLISHED, idPrefixPublished,
                templateProdNamesPublished, ServiceStatus.ACTIVE, true, true,
                true, NUMBER_OF_CUST_PRODUCTS_PUBLISHED,
                averageRatingsPublished, 1111111500000L);

        // candidates for not visible on landing page
        templateProductsNotPublished = createProducts(
                NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLISHED, idPrefixNotPublished,
                templateProdNamesNotPublished, ServiceStatus.ACTIVE, false,
                true, true, 0, null, 1111111400000L);

        // candidates for not visible on landing page
        templateProductsNotActive = createProducts(
                NUMBER_OF_TEMP_PRODUCTS_NOT_ACTIVE, idPrefixNotActive, null,
                ServiceStatus.INACTIVE, true, true, true, 0, null,
                1111111300000L);

        // candidates for not visible on landing page
        templateProductsNotVisible = createProducts(
                NUMBER_OF_TEMP_PRODUCTS_NOT_VISIBLE_IN_CATALOG,
                idPrefixNotVisible, null, ServiceStatus.ACTIVE, true, false,
                true, 0, null, 1111111200000L);

        // candidates for visible on landing page (only for logged in user)
        templateProductsNotPublic = createProducts(
                NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLIC, idPrefixNotPublic,
                templateProdNamesNotPublic, ServiceStatus.ACTIVE, true, true,
                false, 0, null, 1111111100000L);
    }

    private Product getCustSpecProductForTemplate(Product productTemplate) {
        int index = templateProductsPublished.indexOf(productTemplate);

        // only the first two product templates have a customer specific product
        if (index > (NUMBER_OF_CUST_PRODUCTS_PUBLISHED - 1)) {
            return null;
        }
        return customerProducts.get(index);
    }

    private void addProductToLandingpage(final Product product,
            final int position) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplierMp = (Marketplace) ds
                        .getReferenceByBusinessKey(supplierMp);

                PublicLandingpage landingpage = supplierMp
                        .getPublicLandingpage();
                List<LandingpageProduct> featuredList = landingpage
                        .getLandingpageProducts();
                LandingpageProduct landingpageProduct = new LandingpageProduct();
                landingpageProduct.setLandingpage(landingpage);
                landingpageProduct.setPosition(position);
                landingpageProduct.setProduct(product);
                featuredList.add(landingpageProduct);
                ds.persist(landingpageProduct);
                return null;
            }
        });
        inputData.add(new ProductAndPosition(product, position));
    }

    private void setLandingpageFillinCriterion(
            final FillinCriterion fillinCriterion) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplierMp = (Marketplace) ds
                        .getReferenceByBusinessKey(supplierMp);
                PublicLandingpage landingpage = supplierMp
                        .getPublicLandingpage();
                landingpage.setFillinCriterion(fillinCriterion);
                ds.persist(landingpage);
                return null;
            }
        });
    }

    private void setLandingpageNumberServices(final int numberServices)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplierMp = (Marketplace) ds
                        .getReferenceByBusinessKey(supplierMp);
                PublicLandingpage landingpage = supplierMp
                        .getPublicLandingpage();
                landingpage.setNumberServices(numberServices);
                ds.persist(landingpage);
                return null;
            }
        });
    }

    private void checkSortingAccordingPosition(int numberOfServices,
            List<VOService> featuredServices, boolean loggedIn)
            throws Exception {

        // sort expected data according to position
        Collections.sort(inputData, new ProductAndPositionComparator());

        for (int i = 0; i < numberOfServices; i++) {
            Product product = inputData.get(i).getProduct();

            // inputData must contains templates only
            Assert.assertNull(product.getTargetCustomer());

            String productId = null;
            long priceModelKey = 0;
            Product custSpecProduct = getCustSpecProductForTemplate(product);
            if (loggedIn && custSpecProduct != null) {
                // customer specific product
                productId = custSpecProduct.getCleanProductId();
                priceModelKey = custSpecProduct.getPriceModel().getKey();
            } else {
                // template product
                productId = product.getProductId();
                priceModelKey = product.getPriceModel().getKey();
            }
            assertEquals(productId, featuredServices.get(i).getServiceId());
            assertEquals(priceModelKey,
                    featuredServices.get(i).getPriceModel().getKey());
        }
    }

    // ---- template products published----
    // index productId provisioDate marketplace template rating productName
    // 0: productId_0 1111111100060 supplierMp ---------- 3.0 ..... f0
    // 1: productId_1 1111111100050 supplierMp ---------- 1.1 ..... x1
    // 2: productId_2 1111111100040 supplierMp ---------- 2.2 ..... z2
    // 3: productId_3 1111111100030 supplierMp ---------- 1.3 ..... a3
    // 4: productId_4 1111111100020 supplierMp ---------- 0.0 ..... j4
    // 5: productId_5 1111111100010 supplierMp ---------- 1.5 ..... c5

    // ---- template products not published----
    // 0: z_productIdNotPublished_0 1111111100020 ---------- ---------- 5.6
    // .....
    // b6
    // 1: z_productIdNotPublished_1 1111111100010 ---------- ---------- 1.7
    // .....
    // a7

    // ---- customer specific products published ----
    // 0: productId_0# 1111111100061 supplierMp productId_0 ....... bb0
    // 1: productId_1# 1111111100051 supplierMp productId_1 ....... aa1

    // ---- inactive template products published ----
    // 0: zz_productIdNotActive_0 1111111100030
    // 1: zz_productIdNotActive_1 1111111100020
    // 2: zz_productIdNotActive_2 1111111100010

    // ---- template products published, not visible in catalog ----
    // 0: zz_productIdNotVisible_0 1111111100030
    // 1: zz_productIdNotVisible_1 1111111100020
    // 2: zz_productIdNotVisible_2 1111111100010

    // ---- template products published, no anonymous access ----
    // 0: zz_productIdNotPublic_0 1111111100300
    // 1: zz_productIdNotPublic_1 1111111100200
    // 2: zz_productIdNotPublic_2 1111111100100

    @Override
    protected void setup(final TestContainer container) throws Exception {
        // container
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new LocalizerServiceBean());
        userGroupService = mock(UserGroupServiceLocalBean.class);
        container.addBean(userGroupService);

        // services
        ds = container.get(DataService.class);
        landingpageService = container.get(LandingpageServiceLocal.class);

        localizer = container.get(LocalizerServiceLocal.class);

        // setup db
        createPlatformOperator();
        createSupplierOrganization();
        createSupplierMarketplace();
        createTechnicalProduct();
        createProducts();
    }

    @Test(expected = IllegalArgumentException.class)
    public void servicesForPublicLandingpage_anonymus_MarketplaceIdNull()
            throws Exception {
        // given
        try {
            // when
            landingpageService.servicesForPublicLandingpage(null, locale);
        } catch (EJBException e) {
            // then
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void servicesForPublicLandingpage_anonymus_LocaleNull()
            throws Exception {
        // given
        try {
            // when
            landingpageService.servicesForPublicLandingpage(SUPPLIER_MP_ID,
                    null);
        } catch (EJBException e) {
            // then
            throw e.getCausedByException();
        }
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_featuredListEmpty()
            throws Exception {
        // given
        setLandingpageFillinCriterion(FillinCriterion.NO_FILLIN);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());

    }

    @Test
    public void servicesForPublicLandingpage_anonymus_featuredListWithOneProduct()
            throws Exception {
        // given
        setLandingpageFillinCriterion(FillinCriterion.NO_FILLIN);
        addProductToLandingpage(templateProductsPublished.get(0), 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(1, featuredServices.size());
        assertEquals(templateProductsPublished.get(0).getProductId(),
                featuredServices.get(0).getServiceId());
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_numberOfServicesIsZero()
            throws Exception {
        // given
        int maxServices = 0;
        setLandingpageNumberServices(maxServices);

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(0), 0);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_checkPosition()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);

        int featuredListSize = 5;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i),
                    featuredListSize + 1 - i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        checkSortingAccordingPosition(maxServices, featuredServices, false);
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_restrictNumberOfServices()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);

        int featuredListSize = 5;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        checkSortingAccordingPosition(maxServices, featuredServices, false);
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_checkPositionWithGaps()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);

        int featuredListSize = 5;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i * 2);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        checkSortingAccordingPosition(maxServices, featuredServices, false);
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_fillCriterionNone()
            throws Exception {
        // given
        setLandingpageFillinCriterion(FillinCriterion.NO_FILLIN);

        int maxServices = 4;
        setLandingpageNumberServices(maxServices);

        int featuredListSize = 3;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(featuredListSize, featuredServices.size());
        checkSortingAccordingPosition(featuredListSize, featuredServices,
                false);
    }

    @Test
    public void servicesForPublicLandingpage_subscriptionManager_fillCriterionNone()
            throws Exception {
        // given
        PlatformUser subManager = createPlatformUser("subManager", supplierOrg);
        setLandingpageFillinCriterion(FillinCriterion.NO_FILLIN);

        int maxServices = 4;
        setLandingpageNumberServices(maxServices);

        int featuredListSize = 5;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        int invisibleFeaturedListSize = 2;
        List<Long> invisibleProductKeysList = new ArrayList<>();
        invisibleProductKeysList
                .add(Long.valueOf(templateProductsPublished.get(2).getKey()));
        invisibleProductKeysList
                .add(Long.valueOf(templateProductsPublished.get(4).getKey()));
        inputData.remove(2);
        inputData.remove(3);

        when(userGroupService
                .getInvisibleProductKeysForUser(subManager.getKey()))
                        .thenReturn(invisibleProductKeysList);

        container.login(subManager.getKey(), ROLE_SUBSCRIPTION_MANAGER);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(featuredListSize - invisibleFeaturedListSize,
                featuredServices.size());
        checkSortingAccordingPosition(
                featuredListSize - invisibleFeaturedListSize, featuredServices,
                true);
    }

    @Test
    public void servicesForPublicLandingpage_mpOwner_returnCustomerSpecProducts()
            throws Exception {
        container.login(supplierKey, ROLE_MARKETPLACE_OWNER);

        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);

        for (int i = 0; i < maxServices; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());

        checkSortingAccordingPosition(maxServices, featuredServices, true);
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_fillCriterionProvisioningDate()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.ACTIVATION_DESCENDING);

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices,
                false);

        // ---- filling services ----
        // check positions
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(1).getServiceId());
        assertEquals(idPrefixPublished + "4",
                featuredServices.get(2).getServiceId());
        assertEquals(idPrefixPublished + "3",
                featuredServices.get(3).getServiceId());
    }

    @Test
    public void servicesForPublicLandingpage_mpOwner_fillCriterionProvisioningDate()
            throws Exception {
        container.login(supplierKey, ROLE_MARKETPLACE_OWNER);

        // given
        int maxServices = 8;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.ACTIVATION_DESCENDING);

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        // only 6 products available + 3 public templates, but maxServices=8
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices, true);

        // ---- filling services ----
        // check positions
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(1).getServiceId());
        assertEquals(idPrefixPublished + "4",
                featuredServices.get(2).getServiceId());
        assertEquals(idPrefixPublished + "3",
                featuredServices.get(3).getServiceId());
        assertEquals(idPrefixPublished + "2",
                featuredServices.get(4).getServiceId());
        assertEquals(idPrefixPublished + "1",
                featuredServices.get(5).getServiceId());
        assertEquals(customerProdNames[1], featuredServices.get(5).getName());

        assertEquals(idPrefixNotPublic + "2",
                featuredServices.get(6).getServiceId());
        assertEquals(idPrefixNotPublic + "1",
                featuredServices.get(7).getServiceId());
    }

    @Test
    public void servicesForPublicLandingpage_anonymous_fillCriterionRating()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.RATING_DESCENDING);

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices,
                false);

        // ---- filling services ----
        // check positions
        assertEquals(idPrefixPublished + "2",
                featuredServices.get(1).getServiceId());
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(2).getServiceId());
        assertEquals(idPrefixPublished + "3",
                featuredServices.get(3).getServiceId());
    }

    /**
     * Bug 9808
     */
    @Test
    public void servicesForPublicLandingpage_anonymous_fillCriterionRatingWithPartnerService()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.RATING_DESCENDING);
        Product brokerService = createBrokerService(
                templateProductsPublished.get(2));

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices,
                false);

        // ---- filling services ----
        // check positions - first the broker service as it was created as last
        // one with same rating as template
        assertEquals(idPrefixPublished + "2",
                featuredServices.get(1).getServiceId());
        assertEquals(brokerService.getKey(), featuredServices.get(1).getKey());

        // then template (same name and rating but older (query criteria)
        assertEquals(idPrefixPublished + "2",
                featuredServices.get(2).getServiceId());
        assertEquals(templateProductsPublished.get(2).getKey(),
                featuredServices.get(2).getKey());

        // finally the next best rated service
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(3).getServiceId());
    }

    /**
     * Bug 9808
     */
    @Test
    public void servicesForPublicLandingpage_anonymous_fillCriterionRatingSecondCriteriaName()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.RATING_DESCENDING);
        setRatingsToSameValue();

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices,
                false);

        // ---- filling services ----
        // check positions - 0 (f0) is in the featured list, so filled up with
        // a3, c5 and j4
        assertEquals(idPrefixPublished + "3",
                featuredServices.get(1).getServiceId());
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(2).getServiceId());
        assertEquals(idPrefixPublished + "4",
                featuredServices.get(3).getServiceId());

        // check names
        assertEquals(templateProdNamesPublished[3],
                featuredServices.get(1).getName());
        assertEquals(templateProdNamesPublished[5],
                featuredServices.get(2).getName());
        assertEquals(templateProdNamesPublished[4],
                featuredServices.get(3).getName());
    }

    @Test
    public void servicesForPublicLandingpage_anonymus_fillCriterionName()
            throws Exception {
        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.NAME_ASCENDING);

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices,
                false);

        // ---- filling services ----
        // check positions
        assertEquals(idPrefixPublished + "3",
                featuredServices.get(1).getServiceId());
        assertEquals(templateProdNamesPublished[3],
                featuredServices.get(1).getName());
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(2).getServiceId());
        assertEquals(templateProdNamesPublished[5],
                featuredServices.get(2).getName());
        assertEquals(idPrefixPublished + "4",
                featuredServices.get(3).getServiceId());
        assertEquals(templateProdNamesPublished[4],
                featuredServices.get(3).getName());
    }

    @Test
    public void servicesForPublicLandingpage_mpOwner_fillCriterionName()
            throws Exception {
        container.login(supplierKey, ROLE_MARKETPLACE_OWNER);

        // given
        int maxServices = 4;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.NAME_ASCENDING);

        int featuredListSize = 1;
        for (int i = 0; i < featuredListSize; i++) {
            addProductToLandingpage(templateProductsPublished.get(i), i);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(maxServices, featuredServices.size());
        // ---- landing page services ----
        // check position 1
        checkSortingAccordingPosition(featuredListSize, featuredServices, true);

        // ---- filling services ----
        // check positions
        assertEquals(idPrefixPublished + "3",
                featuredServices.get(1).getServiceId());
        assertEquals(templateProdNamesPublished[3],
                featuredServices.get(1).getName());
        assertEquals(idPrefixPublished + "1",
                featuredServices.get(2).getServiceId());
        assertEquals(customerProdNames[1], featuredServices.get(2).getName());
        assertEquals(idPrefixPublished + "5",
                featuredServices.get(3).getServiceId());
        assertEquals(templateProdNamesPublished[5],
                featuredServices.get(3).getName());
    }

    @Test
    public void servicesForPublicLandingpage_mpOwner_visibleServices()
            throws Exception {
        container.login(supplierKey, ROLE_MARKETPLACE_OWNER);

        // given
        int maxServices = 20;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.NO_FILLIN);

        int position = 0;
        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_PUBLISHED; i++) {
            addProductToLandingpage(templateProductsPublished.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLISHED; i++) {
            addProductToLandingpage(templateProductsNotPublished.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_ACTIVE; i++) {
            addProductToLandingpage(templateProductsNotActive.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_VISIBLE_IN_CATALOG; i++) {
            addProductToLandingpage(templateProductsNotVisible.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLIC; i++) {
            addProductToLandingpage(templateProductsNotPublic.get(i),
                    position++);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(
                NUMBER_OF_TEMP_PRODUCTS_PUBLISHED
                        + NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLIC,
                featuredServices.size());

        for (VOService service : featuredServices) {
            assertFalse(service.getName().startsWith(idPrefixNotPublished));
            assertFalse(service.getName().startsWith(idPrefixNotActive));
            assertFalse(service.getName().startsWith(idPrefixNotVisible));
        }
    }

    @Test
    public void servicesForPublicLandingpage_anonymous_visibleServices()
            throws Exception {

        // given
        int maxServices = 20;
        setLandingpageNumberServices(maxServices);
        setLandingpageFillinCriterion(FillinCriterion.NO_FILLIN);

        int position = 0;

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_PUBLISHED; i++) {
            addProductToLandingpage(templateProductsPublished.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLISHED; i++) {
            addProductToLandingpage(templateProductsNotPublished.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_ACTIVE; i++) {
            addProductToLandingpage(templateProductsNotActive.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_VISIBLE_IN_CATALOG; i++) {
            addProductToLandingpage(templateProductsNotVisible.get(i),
                    position++);
        }

        for (int i = 0; i < NUMBER_OF_TEMP_PRODUCTS_NOT_PUBLIC; i++) {
            addProductToLandingpage(templateProductsNotPublic.get(i),
                    position++);
        }

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(NUMBER_OF_TEMP_PRODUCTS_PUBLISHED,
                featuredServices.size());

        for (VOService service : featuredServices) {
            assertFalse(service.getName().startsWith(idPrefixNotPublished));
            assertFalse(service.getName().startsWith(idPrefixNotActive));
            assertFalse(service.getName().startsWith(idPrefixNotVisible));
            assertFalse(service.getName().startsWith(idPrefixNotPublic));
        }

    }

    private void setRatingsToSameValue() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                for (Product temp : templateProductsPublished) {
                    Product p = ds.getReference(Product.class, temp.getKey());
                    p.getProductFeedback()
                            .setAverageRating(BigDecimal.valueOf(3));
                }
                return null;
            }
        });

    }

    private Product createBrokerService(final Product prod) throws Exception {
        return runTX(new Callable<Product>() {

            @Override
            public Product call() throws Exception {
                Organization broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                Product template = ds.getReference(Product.class,
                        prod.getKey());
                Marketplace mp = ds.getReference(Marketplace.class,
                        supplierMp.getKey());
                Product p = Products.createProductResaleCopy(template, broker,
                        mp, ds);
                p.setPriceModel(new PriceModel());
                p.setStatus(ServiceStatus.ACTIVE);
                return p;
            }
        });

    }

}
