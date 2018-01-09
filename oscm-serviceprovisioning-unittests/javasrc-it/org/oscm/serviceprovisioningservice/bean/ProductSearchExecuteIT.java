/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Category;
import org.oscm.domobjects.CategoryToCatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.Sorting;
import org.oscm.internal.vo.ListCriteria;
import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.CatalogEntries;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class ProductSearchExecuteIT extends EJBTestBase {
    private static final String SUPPLIER_ID = "MySupplier";
    private static final String BROKER_ID = "MyBroker";
    private static final String RESELLER_ID = "MyReseller";
    private static final String SUPPLIER_MP_ID = "MySupplierMP";
    private static final String BROKER_MP_ID = "MyBrokerMP";
    private static final String RESELLER_MP_ID = "MyResellerMP";
    private static final String TECHPRODUCT_ID = "testTechnicalProduct";
    private static final String PRODUCT_ID = "testProduct";
    private static final String CATEGORY_ID_SUPPLIER = "cat0";
    private static final String CATEGORY_ID_BROKER = "cat1";
    private static final String CATEGORY_ID_RESELLER = "cat2";

    private DataService ds;
    private ProductSearch productSearch;

    private Organization supplier;
    private long supplierUserKey;
    private Organization broker;
    private long brokerUserKey;
    private Organization reseller;
    private long resellerUserKey;
    private Product supplierProduct;
    private Product customerSpecificSupplierProduct;
    private Product brokerProduct;
    private Product resellerProduct;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());

        ds = container.get(DataService.class);

        createSupplier();
        createBroker();
        createReseller();
    }

    private void createSupplier() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(ds, SUPPLIER_ID,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                PlatformUser supplierAdmin = Organizations.createUserForOrg(ds,
                        supplier, true, "admin");
                supplierUserKey = supplierAdmin.getKey();

                Marketplaces.createMarketplace(supplier, SUPPLIER_MP_ID, true,
                        ds);

                return null;
            }
        });
    }

    private void createSupplierProduct(final String marketplaceId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create technical product and product without price model
                supplierProduct = Products.createProduct(
                        supplier.getOrganizationId(), PRODUCT_ID,
                        TECHPRODUCT_ID, ds, ServiceAccessType.LOGIN);
                supplierProduct.setStatus(ServiceStatus.ACTIVE);

                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(marketplaceId);
                mp = (Marketplace) ds.find(mp);

                CatalogEntries.create(ds, mp, supplierProduct, true);

                return null;
            }
        });
    }

    private void createCustomerSpecificSupplierProduct() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                customerSpecificSupplierProduct = Products
                        .createCustomerSpecifcProduct(ds, supplier,
                                supplierProduct, ServiceStatus.ACTIVE);

                return null;
            }
        });
    }

    private void createReseller() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                reseller = Organizations.createOrganization(ds, RESELLER_ID,
                        OrganizationRoleType.RESELLER);

                PlatformUser resellerAdmin = Organizations.createUserForOrg(ds,
                        reseller, true, "admin");
                resellerUserKey = resellerAdmin.getKey();

                Marketplaces.createMarketplace(reseller, RESELLER_MP_ID, true,
                        ds);

                return null;
            }
        });
    }

    private void createResellerProduct(final String marketplaceId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resellerProduct = Products
                        .createProductResaleCopy(supplierProduct, reseller, ds);

                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(marketplaceId);
                mp = (Marketplace) ds.find(mp);

                CatalogEntries.create(ds, mp, resellerProduct, true);

                return null;
            }
        });
    }

    private void createBroker() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                broker = Organizations.createOrganization(ds, BROKER_ID,
                        OrganizationRoleType.BROKER);

                PlatformUser brokerAdmin = Organizations.createUserForOrg(ds,
                        broker, true, "admin");
                brokerUserKey = brokerAdmin.getKey();

                Marketplaces.createMarketplace(broker, BROKER_MP_ID, true, ds);

                return null;
            }
        });
    }

    private void createBrokerProduct(final String marketplaceId)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                brokerProduct = Products
                        .createProductResaleCopy(supplierProduct, broker, ds);

                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(marketplaceId);
                mp = (Marketplace) ds.find(mp);

                CatalogEntries.create(ds, mp, brokerProduct, true);

                return null;
            }
        });
    }

    private ProductSearch createProductSearch(String marketplaceId)
            throws Exception {
        return createProductSearch(marketplaceId, null);
    }

    @Test
    public void execute_supplierProductOnOwnMP() throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals("Supplier product not visible on own marketplace",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        supplierProduct.getKey(), service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_customerSpecificSupplierProduct() throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createCustomerSpecificSupplierProduct();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only customer specific supplier product should be visible",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        customerSpecificSupplierProduct.getKey(),
                        service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_bug11156() throws Exception {
        // given
        createSupplierProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_SUPPLIER);
        createCustomerSpecificSupplierProduct();
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID,
                        CATEGORY_ID_SUPPLIER);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only customer specific supplier product should be visible",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        customerSpecificSupplierProduct.getKey(),
                        service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_supplierProductOnResellerMP() throws Exception {
        // given
        createSupplierProduct(RESELLER_MP_ID);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Supplier product has been published on reseller marketplace, but it is visible on supplier marketplace",
                        0, searchResult.getResultSize());

                return null;
            }
        });
    }

    @Test
    public void execute_brokerProductOnOwnMP() throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createBrokerProduct(BROKER_MP_ID);

        container.login(brokerUserKey, ROLE_BROKER_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(BROKER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only broker product should be visible on broker marketplace",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key", brokerProduct.getKey(),
                        service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_brokerProductOnOwnMP_supplLoginOnSupplMP()
            throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createBrokerProduct(BROKER_MP_ID);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only supplier product should be visible on supplier marketplace",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        supplierProduct.getKey(), service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_bug10055_Supplier() throws Exception {
        // given
        createSupplierProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_SUPPLIER);
        createBrokerProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_BROKER);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID,
                        CATEGORY_ID_SUPPLIER);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only supplier product should be visible under the category",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        supplierProduct.getKey(), service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_bug10055_Broker() throws Exception {
        // given
        createSupplierProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_SUPPLIER);
        createBrokerProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_BROKER);

        container.login(brokerUserKey, ROLE_BROKER_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID,
                        CATEGORY_ID_BROKER);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only broker product should be visible under the category",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key", brokerProduct.getKey(),
                        service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_bug10055_Reseller() throws Exception {
        // given
        createSupplierProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_SUPPLIER);
        createBrokerProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_BROKER);
        createResellerProductWithCategory(SUPPLIER_MP_ID, CATEGORY_ID_RESELLER);
        container.login(resellerUserKey, ROLE_RESELLER_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID,
                        CATEGORY_ID_RESELLER);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only broker product should be visible under the category",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        resellerProduct.getKey(), service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    private ProductSearch createProductSearch(String marketplaceId,
            String categoryId) throws Exception {
        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setFilter("");
        listCriteria.setSorting(Sorting.ACTIVATION_DESCENDING);
        listCriteria.setLimit(10);
        listCriteria.setOffset(0);
        if (categoryId != null) {
            listCriteria.setCategoryId(categoryId);
        }
        ProductSearch pSearch = new ProductSearch(ds, marketplaceId,
                listCriteria, "en", "en", null);

        return pSearch;
    }

    private void createSupplierProductWithCategory(final String marketplaceId,
            final String categoryId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create technical product and product without price model
                supplierProduct = Products.createProduct(
                        supplier.getOrganizationId(), PRODUCT_ID,
                        TECHPRODUCT_ID, ds, ServiceAccessType.LOGIN);
                supplierProduct.setStatus(ServiceStatus.ACTIVE);
                createCategory(ds, categoryId, marketplaceId, supplierProduct);

                return null;
            }
        });
    }

    private void createBrokerProductWithCategory(final String marketplaceId,
            final String categoryId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                brokerProduct = Products
                        .createProductResaleCopy(supplierProduct, broker, ds);
                createCategory(ds, categoryId, marketplaceId, brokerProduct);

                return null;
            }
        });
    }

    private void createResellerProductWithCategory(final String marketplaceId,
            final String categoryId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                resellerProduct = Products
                        .createProductResaleCopy(supplierProduct, reseller, ds);
                createCategory(ds, categoryId, marketplaceId, resellerProduct);

                return null;
            }
        });
    }

    private void createCategory(DataService ds, String categoryId,
            String marketplaceId, Product product) throws Exception {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId(marketplaceId);
        mp = (Marketplace) ds.find(mp);
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setMarketplace(mp);
        ds.persist(category);
        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(product);
        ce.setMarketplace(mp);
        ce.setAnonymousVisible(true);
        ce.setVisibleInCatalog(true);
        ds.persist(ce);
        CategoryToCatalogEntry cc = new CategoryToCatalogEntry();
        cc.setCatalogEntry(ce);
        cc.setCategory(ds.getReference(Category.class, category.getKey()));
        ds.persist(cc);
    }

    @Test
    public void execute_resellerProductOnOwnMP() throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createResellerProduct(RESELLER_MP_ID);

        container.login(resellerUserKey, ROLE_RESELLER_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(RESELLER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only reseller product should be visible on reseller marketplace",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        resellerProduct.getKey(), service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_resellerProductOnOwnMP_supplLoginOnSupplMP()
            throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createResellerProduct(RESELLER_MP_ID);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Only supplier product should be visible on supplier marketplace",
                        1, searchResult.getResultSize());
                Product service = searchResult.getServices().get(0);
                assertEquals("Wrong result service key",
                        supplierProduct.getKey(), service.getKey());
                assertEquals("Wrong result service ID", PRODUCT_ID,
                        service.getCleanProductId());

                return null;
            }
        });
    }

    @Test
    public void execute_AllProductsOnSupplierMP_supplLoginOnSupplMP()
            throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createCustomerSpecificSupplierProduct();
        createBrokerProduct(SUPPLIER_MP_ID);
        createResellerProduct(SUPPLIER_MP_ID);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Customer specific supplier product and broker/reseller products should be visible",
                        3, searchResult.getResultSize());

                Set<Long> expectedProductIds = new HashSet<>(Arrays.asList(
                        Long.valueOf(customerSpecificSupplierProduct.getKey()),
                        Long.valueOf(brokerProduct.getKey()),
                        Long.valueOf(resellerProduct.getKey())));

                Set<Long> resultedProductIds = new HashSet<>();
                for (Product service : searchResult.getServices()) {
                    resultedProductIds.add(Long.valueOf(service.getKey()));
                }

                assertEquals("Wrong product key(s) in result",
                        expectedProductIds, resultedProductIds);

                return null;
            }
        });
    }

    @Test
    public void execute_AllProductsOnSupplierMP_brokerLoginOnSupplMP()
            throws Exception {
        // given
        createSupplierProduct(SUPPLIER_MP_ID);
        createCustomerSpecificSupplierProduct();
        createBrokerProduct(SUPPLIER_MP_ID);
        createResellerProduct(SUPPLIER_MP_ID);

        container.login(brokerUserKey, ROLE_BROKER_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // when
                productSearch = createProductSearch(SUPPLIER_MP_ID);
                ProductSearchResult searchResult = productSearch.execute();

                // then
                assertEquals(
                        "Supplier/broker/reseller products should be visible",
                        3, searchResult.getResultSize());

                Set<Long> expectedProductIds = new HashSet<>(
                        Arrays.asList(Long.valueOf(supplierProduct.getKey()),
                                Long.valueOf(brokerProduct.getKey()),
                                Long.valueOf(resellerProduct.getKey())));

                Set<Long> resultedProductIds = new HashSet<>();
                for (Product service : searchResult.getServices()) {
                    resultedProductIds.add(Long.valueOf(service.getKey()));
                }

                assertEquals("Wrong product key(s) in result",
                        expectedProductIds, resultedProductIds);

                return null;
            }
        });
    }
}
