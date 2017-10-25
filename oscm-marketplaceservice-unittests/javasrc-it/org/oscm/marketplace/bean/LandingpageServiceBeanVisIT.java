/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: stavreva
 *
 *  Creation Date: 10.07.2012
 *
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOService;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplace.cache.MarketplaceCacheServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.enumtypes.FillinCriterion;

public class LandingpageServiceBeanVisIT extends EJBTestBase {

    private String locale = "en";
    private DataService ds;
    private LocalizerServiceLocal localizer;
    private LandingpageServiceLocal landingpageService;
    private Organization platformOperatorOrg;
    private static PlatformUser platformOperatorUser;
    private TechnicalProduct technicalProduct;
    private Organization supplierOrg;
    private Organization customerOrg;
    private Organization brokerOrg;
    private Organization resellerOrg;
    private static final String SUPPLIER_MP_ID = "SUPPLIER_MP_ID";
    private static final String SUPPLIER_MP_ID2 = "SUPPLIER_MP_ID2";
    private Marketplace supplierMp;
    private Marketplace supplierMp2;
    private Product product = null;
    private Product customerProduct = null;
    private Product productFill = null;
    private Product customerProductFill = null;
    private Product brokerProductFill = null;
    private Product resellerProductFill = null;
    private long provisioningDate = 1111111100010L;
    private String templateName = "template_";
    private String customerCopyName = "customer_";
    private String brokerCopyName = "broker_";
    private String resellerCopyName = "reseller_";
    private String productId = "productId";
    private String productIdFill = "productIdFill";

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

    private Organization createOrganization(final OrganizationRoleType... roles)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(ds, roles);
                PlatformUser createUserForOrg = Organizations.createUserForOrg(
                        ds, org, true, "admin" + org.getOrganizationId());
                List<PlatformUser> admin = Collections
                        .singletonList(createUserForOrg);
                org.setPlatformUsers(admin);
                return org;
            }
        });
    }

    private Marketplace createSupplierMarketplace(
            final Organization supplierOrg, final String name)
            throws Exception {
        return runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace supplierMp = Marketplaces
                        .createMarketplace(supplierOrg, name, false, ds);
                return supplierMp;
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

    private Product createProduct(final Organization supplier,
            final Marketplace marketplace, final String productId)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {

                Product product = Products.createProduct(supplier,
                        technicalProduct, false, productId, productId + "Model",
                        marketplace, ds);

                if (!supplier.getGrantedRoleTypes()
                        .contains(OrganizationRoleType.SUPPLIER)) {
                    product.setType(ServiceType.PARTNER_TEMPLATE);
                }
                product.setProvisioningDate(provisioningDate);
                product.setStatus(ServiceStatus.INACTIVE);
                ds.persist(product);

                Query query = ds.createNamedQuery("CatalogEntry.findByService");
                query.setParameter("service", product);

                @SuppressWarnings("unchecked")
                List<CatalogEntry> cEntries = query.getResultList();
                for (CatalogEntry cEntry : cEntries) {
                    cEntry.setVisibleInCatalog(true);
                    cEntry.setAnonymousVisible(true);
                    ds.persist(cEntry);
                }

                localizer.storeLocalizedResource(locale, product.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        templateName + product.getProductId());

                Subscriptions.createSubscription(ds,
                        supplier.getOrganizationId(), product);

                return product;

            }
        });
    }

    private Product createCustomerProduct(final Organization customer,
            final Product product, final String namePrefix) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {

                Product custSpecProduct;
                custSpecProduct = Products.createCustomerSpecifcProduct(ds,
                        customer, product, ServiceStatus.INACTIVE);
                custSpecProduct.getPriceModel();
                custSpecProduct
                        .setProvisioningDate(product.getProvisioningDate());
                ds.persist(custSpecProduct);

                localizer.storeLocalizedResource(locale,
                        custSpecProduct.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        namePrefix + product.getProductId());

                Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), custSpecProduct);

                return custSpecProduct;

            }
        });
    }

    private Product createBrokerProduct(final Organization vendor,
            final Marketplace marketplace, final Product productTemplate)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {

                Product brokerProductFill;
                brokerProductFill = Products.createProductResaleCopy(
                        productTemplate, vendor, marketplace, ds);

                brokerProductFill.setProvisioningDate(provisioningDate);
                brokerProductFill.setStatus(ServiceStatus.INACTIVE);
                ds.persist(brokerProductFill);

                localizer.storeLocalizedResource(locale,
                        brokerProductFill.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        brokerCopyName + productTemplate.getProductId()
                                + vendor.getOrganizationId());

                Subscriptions.createSubscription(ds, vendor.getOrganizationId(),
                        brokerProductFill);

                return brokerProductFill;

            }
        });
    }

    private Product createResellerProduct(final Organization vendor,
            final Marketplace marketplace, final Product productTemplate)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {

                Product resellerProductFill;
                resellerProductFill = Products.createProductResaleCopy(
                        productTemplate, vendor, marketplace, ds);
                resellerProductFill.setProvisioningDate(provisioningDate);
                resellerProductFill.setStatus(ServiceStatus.INACTIVE);
                ds.persist(resellerProductFill);

                localizer.storeLocalizedResource(locale,
                        resellerProductFill.getKey(),
                        LocalizedObjectTypes.PRODUCT_MARKETING_NAME,
                        resellerCopyName + productTemplate.getProductId()
                                + vendor.getOrganizationId());

                Subscriptions.createSubscription(ds, vendor.getOrganizationId(),
                        resellerProductFill);

                return resellerProductFill;

            }
        });
    }

    private Product updateCustomerProduct(final Product product,
            final ServiceStatus status) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {

                Product custSpecProduct = (Product) ds.find(product);
                if (custSpecProduct != null) {
                    custSpecProduct.setStatus(status);
                    ds.persist(custSpecProduct);
                }
                return custSpecProduct;
            }
        });
    }

    private void updateProduct(final Product product,
            final ServiceStatus status, final boolean visibleInCatalog,
            final boolean publicAccess) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Product p = (Product) ds.find(product);
                p.setStatus(status);
                ds.persist(p);

                Query query = ds.createNamedQuery("CatalogEntry.findByService");
                query.setParameter("service", product);

                @SuppressWarnings("unchecked")
                List<CatalogEntry> cEntries = query.getResultList();
                for (CatalogEntry cEntry : cEntries) {
                    cEntry.setVisibleInCatalog(visibleInCatalog);
                    cEntry.setAnonymousVisible(publicAccess);
                    ds.persist(cEntry);
                }
                return null;
            }
        });
    }

    private Boolean isSubscriptionCopy(final VOService vo) throws Exception {
        return runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Product p = ds.find(Product.class, vo.getKey());
                Boolean b = new Boolean(
                        p.getProvisioningDate() != provisioningDate);
                return b;
            }
        });
    }

    private void addProductToLandingpage(final Marketplace marketplace,
            final Product product, final int position) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace mp = (Marketplace) ds
                        .getReferenceByBusinessKey(marketplace);

                PublicLandingpage landingpage = mp.getPublicLandingpage();
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
    }

    private void setLandingpageFillinCriterion(final Marketplace marketplace,
            final FillinCriterion fillinCriterion) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace mp = (Marketplace) ds
                        .getReferenceByBusinessKey(marketplace);
                PublicLandingpage landingpage = mp.getPublicLandingpage();
                landingpage.setFillinCriterion(fillinCriterion);
                ds.persist(landingpage);
                return null;
            }
        });
    }

    private void setLandingpageNumberServices(final Marketplace marketplace,
            final int numberServices) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace mp = (Marketplace) ds
                        .getReferenceByBusinessKey(marketplace);
                PublicLandingpage landingpage = mp.getPublicLandingpage();
                landingpage.setNumberServices(numberServices);
                ds.persist(landingpage);
                return null;
            }
        });
    }

    @Override
    protected void setup(final TestContainer container) throws Exception {
        // container
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new MarketplaceCacheServiceBean());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new LocalizerServiceBean());

        // services
        ds = container.get(DataService.class);
        landingpageService = container.get(LandingpageServiceLocal.class);

        localizer = container.get(LocalizerServiceLocal.class);

        // setup db
        createPlatformOperator();
        supplierOrg = createOrganization(OrganizationRoleType.PLATFORM_OPERATOR,
                OrganizationRoleType.MARKETPLACE_OWNER,
                OrganizationRoleType.SUPPLIER);
        customerOrg = createOrganization(OrganizationRoleType.CUSTOMER);
        brokerOrg = createOrganization(OrganizationRoleType.BROKER);
        resellerOrg = createOrganization(OrganizationRoleType.RESELLER);

        supplierMp = createSupplierMarketplace(supplierOrg, SUPPLIER_MP_ID);
        supplierMp2 = createSupplierMarketplace(supplierOrg, SUPPLIER_MP_ID2);

        createTechnicalProduct();

        // create products for marketplace 1
        product = createProduct(supplierOrg, supplierMp, productId);
        productFill = createProduct(supplierOrg, supplierMp, productIdFill);
        customerProduct = createCustomerProduct(customerOrg, product,
                customerCopyName);
        customerProductFill = createCustomerProduct(customerOrg, productFill,
                customerCopyName);
        brokerProductFill = createBrokerProduct(brokerOrg, supplierMp, product);
        resellerProductFill = createResellerProduct(resellerOrg, supplierMp,
                product);

        // create products for marketplace 2
        Product product2 = createProduct(supplierOrg, supplierMp2,
                productId + "2");
        Product productFill2 = createProduct(supplierOrg, supplierMp2,
                productIdFill + "2");
        createCustomerProduct(customerOrg, product2, customerCopyName);
        createCustomerProduct(customerOrg, productFill2, customerCopyName);

        addProductToLandingpage(supplierMp2, product2, 1);

    }

    private void updateProducts(ServiceStatus status, boolean visibleInCatalog,
            boolean publicAccess) throws Exception {
        updateProduct(product, status, visibleInCatalog, publicAccess);
        updateProduct(productFill, status, visibleInCatalog, publicAccess);
        updateProduct(brokerProductFill, status, visibleInCatalog,
                publicAccess);
        updateProduct(resellerProductFill, status, visibleInCatalog,
                publicAccess);
    }

    private void updateCustomerProducts(ServiceStatus status) throws Exception {
        updateCustomerProduct(customerProduct, status);
        updateCustomerProduct(customerProductFill, status);
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_NotAct_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_NotAct_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_NotAct_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_NotAct_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_Act_TProd_NotAct_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_NotAct_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_NotAct_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_Act_TProd_NotAct_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(2, featuredServices.size());
        for (VOService vo : featuredServices) {
            System.out.println(vo.getNameToDisplay());
            assertFalse(vo.getNameToDisplay().startsWith(templateName));
            assertFalse(vo.getNameToDisplay().startsWith(brokerCopyName));

            assertFalse(vo.getNameToDisplay().startsWith(resellerCopyName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_NotAct_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_NotAct_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_Act_TProd_NotAct_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);

        // 2 customer-spec products
        assertEquals(2, featuredServices.size());
        for (VOService vo : featuredServices) {
            assertFalse(vo.getNameToDisplay().startsWith(templateName));
            assertFalse(vo.getNameToDisplay().startsWith(brokerCopyName));

            assertFalse(vo.getNameToDisplay().startsWith(resellerCopyName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_Act_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_Act_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_Act_TProd_Act_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_Act_NotVis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_Act_NotVis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_Act_TProd_Act_NotVis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_Act_Vis_NotPub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_Act_Vis_NotPub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);

        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            System.out.println(vo.getNameToDisplay());

            assertFalse(vo.getNameToDisplay().startsWith(customerCopyName));
            // assertTrue(vo.getNameToDisplay().startsWith(templateName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_customer_CProd_Act_TProd_Act_Vis_NotPub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            assertTrue(vo.getNameToDisplay().startsWith(customerCopyName)
                    || vo.getNameToDisplay().startsWith(templateName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_Act_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            System.out.println(vo.getNameToDisplay());

            assertFalse(vo.getNameToDisplay().startsWith(customerCopyName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_Act_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            assertFalse(vo.getNameToDisplay().startsWith(customerCopyName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_customer_CProd_Act_TProd_Act_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            assertTrue(vo.getNameToDisplay().startsWith(customerCopyName)
                    || vo.getNameToDisplay().startsWith(templateName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_anonymus_CProd_Act_TProd_NotAct_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_Act_TProd_NotAct_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_Act_TProd_NotAct_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.ACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_NotAct_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_NotAct_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_NotAct_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_NotAct_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_NotAct_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_NotAct_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_NotAct_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_NotAct_Vis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_NotAct_Vis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_NotAct_Vis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.INACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_Act_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_Act_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_Act_NotVis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_Act_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_Act_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_Act_NotVis_Pub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, false, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_Act_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(0, featuredServices.size());
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_Act_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            System.out.println(vo.getNameToDisplay());

            assertFalse(vo.getNameToDisplay().startsWith(customerCopyName));
            // assertTrue(vo.getNameToDisplay().startsWith(templateName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_Act_Vis_NotPub()
            throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, false);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(2, featuredServices.size());
    }

    @Test
    public void lp_anonymus_CProd_NotAct_TProd_Act_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);

        // then
        assertNotNull(featuredServices);
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {

            assertFalse(vo.getNameToDisplay().startsWith(customerCopyName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_supplier_CProd_NotAct_TProd_Act_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(supplierOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);

        // 2 templates + 1 broker copy + 1 reseller copy
        assertEquals(4, featuredServices.size());
        for (VOService vo : featuredServices) {
            System.out.println(vo.getNameToDisplay());

            assertFalse(vo.getNameToDisplay().startsWith(customerCopyName));
            assertFalse(isSubscriptionCopy(vo).booleanValue());
        }
    }

    @Test
    public void lp_customer_CProd_NotAct_TProd_Act_Vis_Pub() throws Exception {
        // given
        setLandingpageNumberServices(supplierMp, 4);
        setLandingpageFillinCriterion(supplierMp,
                FillinCriterion.NAME_ASCENDING);
        updateProducts(ServiceStatus.ACTIVE, true, true);
        updateCustomerProducts(ServiceStatus.INACTIVE);
        addProductToLandingpage(supplierMp, product, 1);

        // when
        container.login(customerOrg.getPlatformUsers().get(0).getKey(),
                ROLE_MARKETPLACE_OWNER);
        List<VOService> featuredServices = landingpageService
                .servicesForPublicLandingpage(SUPPLIER_MP_ID, locale);
        // then
        assertNotNull(featuredServices);
        assertEquals(2, featuredServices.size());
    }
}
