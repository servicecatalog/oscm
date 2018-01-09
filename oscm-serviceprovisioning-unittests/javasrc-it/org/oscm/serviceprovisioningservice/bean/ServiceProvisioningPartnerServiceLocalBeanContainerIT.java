/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Aug 9, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.EJBTransactionRequiredException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author tokoda
 * 
 */
public class ServiceProvisioningPartnerServiceLocalBeanContainerIT
        extends EJBTestBase {

    private DataService ds;
    private ServiceProvisioningPartnerServiceLocal localService;

    private Organization broker;
    private long brokerUserKey;

    private Organization tpSup;

    private Organization platformOperator;
    private long poUserKey;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new ServiceProvisioningPartnerServiceLocalBean());

        ds = container.get(DataService.class);
        localService = container
                .get(ServiceProvisioningPartnerServiceLocal.class);

        createOrg();
    }

    private void createOrg() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tpSup = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                platformOperator = Organizations.createOrganization(ds,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                PlatformUser userForPO = Organizations.createUserForOrg(ds,
                        platformOperator, true, "po");
                PlatformUsers.grantRoles(ds, userForPO,
                        UserRoleType.PLATFORM_OPERATOR);
                poUserKey = userForPO.getKey();
                return null;
            }
        });
    }

    private void givenBrokerOrg() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);

                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(ds, broker, true, "admin");
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.BROKER_MANAGER);

                brokerUserKey = createUserForOrg.getKey();

                return null;
            }
        });
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void getTemplateProducts_NoTransaction() {
        // given
        // when
        localService.getTemplateProducts();
        // then EJBTransactionRequiredException happens
    }

    @Test
    public void getTemplateProducts_NoServices() throws Exception {
        // given
        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getTemplateProducts();
            }
        });
        // then
        assertEquals(0, products.size());
    }

    @Test
    public void getTemplateProducts_AllProductTypes() throws Exception {
        // given
        final Product template = createTemplateProduct(
                tpSup.getOrganizationId(), "product", "techProduct",
                ServiceStatus.ACTIVE);
        final Product customerTemplate = createCustomerSpecificCopiedProduct(
                tpSup, template, ServiceStatus.ACTIVE);
        final Product partnerTemplate = createPartnerProductCopy(template,
                tpSup, ServiceStatus.ACTIVE);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscription(ds, tpSup.getOrganizationId(),
                        template);
                Subscriptions.createSubscription(ds, tpSup.getOrganizationId(),
                        customerTemplate);
                Subscriptions.createPartnerSubscription(ds,
                        tpSup.getOrganizationId(),
                        partnerTemplate.getProductId(), "partnerSub", tpSup);
                return null;
            }
        });

        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getTemplateProducts();
            }
        });

        // then
        assertEquals(1, products.size());
        assertEquals(template.getKey(), products.get(0).getKey());
    }

    @Test
    public void getTemplateProducts_AllStates() throws Exception {
        // give
        for (ServiceStatus status : ServiceStatus.values()) {
            createTemplateProduct(tpSup.getOrganizationId(), status.name(),
                    "tech_" + status.name(), status);

        }

        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getTemplateProducts();
            }
        });
        // then
        assertEquals(ServiceStatus.values().length, products.size());
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void getProductsForVendor_NoTransaction() {
        // given
        // when
        localService.getProductsForVendor();
        // then EJBTransactionRequiredException happens
    }

    @Test
    public void getProductsForVendor_NoServices() throws Exception {
        // given
        givenBrokerOrg();
        container.login(brokerUserKey, UserRoleType.BROKER_MANAGER.name());

        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getProductsForVendor();
            }
        });
        // then
        assertEquals(0, products.size());
    }

    @Test
    public void getProductsForVendor_WithCopiedService() throws Exception {
        // given
        givenBrokerOrg();
        container.login(brokerUserKey, UserRoleType.BROKER_MANAGER.name());
        Product template = createTemplateProduct(broker.getOrganizationId(),
                "product", "techProduct", ServiceStatus.ACTIVE);
        createCustomerSpecificCopiedProduct(broker, template,
                ServiceStatus.ACTIVE);
        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getProductsForVendor();
            }
        });
        // then
        assertEquals(2, products.size());
    }

    @Test
    public void getProductsForVendor_WithSubscriptionSpecificCopy()
            throws Exception {
        // given
        givenBrokerOrg();
        container.login(brokerUserKey, UserRoleType.BROKER_MANAGER.name());
        Product template = createTemplateProduct(broker.getOrganizationId(),
                "product", "techProduct", ServiceStatus.ACTIVE);
        createSubscriptionSpecificProductCopy(broker, template);
        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getProductsForVendor();
            }
        });
        // then
        assertEquals(1, products.size());
    }

    @Test
    public void getProductsForVendor_FilterInStatuses() throws Exception {
        // give
        givenBrokerOrg();
        container.login(brokerUserKey, UserRoleType.BROKER_MANAGER.name());

        Product templateInactive = createTemplateProduct(
                broker.getOrganizationId(), "productInactive",
                "techProductInactive", ServiceStatus.INACTIVE);
        Product templateActive = createTemplateProduct(
                broker.getOrganizationId(), "productActive",
                "techProductActive", ServiceStatus.ACTIVE);
        createTemplateProduct(broker.getOrganizationId(), "productDeleted",
                "techProductDeleted", ServiceStatus.DELETED);
        createTemplateProduct(broker.getOrganizationId(), "productObsolete",
                "techProductObsolete", ServiceStatus.OBSOLETE);
        Product templateSuspended = createTemplateProduct(
                broker.getOrganizationId(), "productSuspended",
                "techProductSuspended", ServiceStatus.SUSPENDED);
        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                return localService.getProductsForVendor();
            }
        });
        // then
        assertEquals(3, products.size());
        assertEquals(templateInactive.getKey(), products.get(0).getKey());
        assertEquals(ServiceStatus.INACTIVE, products.get(0).getStatus());

        assertEquals(templateActive.getKey(), products.get(1).getKey());
        assertEquals(ServiceStatus.ACTIVE, products.get(1).getStatus());

        assertEquals(ServiceStatus.SUSPENDED, products.get(2).getStatus());
        assertEquals(templateSuspended.getKey(), products.get(2).getKey());
    }

    @Test
    public void getPartnerProductsForTemplate() throws Exception {
        // give
        Organization broker1 = createOrgWithRole(OrganizationRoleType.BROKER);
        Organization broker2 = createOrgWithRole(OrganizationRoleType.BROKER);
        Organization broker3 = createOrgWithRole(OrganizationRoleType.BROKER);
        Organization reseller1 = createOrgWithRole(
                OrganizationRoleType.RESELLER);
        Organization reseller2 = createOrgWithRole(
                OrganizationRoleType.RESELLER);

        final Product template = createTemplateProduct(
                tpSup.getOrganizationId(), "product", "techProduct",
                ServiceStatus.ACTIVE);
        createCustomerSpecificCopiedProduct(tpSup, template,
                ServiceStatus.ACTIVE);
        createSubscriptionSpecificProductCopy(tpSup, template);

        Product brokerActiveProduct = createPartnerProductCopy(template,
                broker1, ServiceStatus.ACTIVE);
        Product resellerInactiveProduct = createPartnerProductCopy(template,
                reseller1, ServiceStatus.INACTIVE);
        createPartnerProductCopy(template, broker2, ServiceStatus.OBSOLETE);
        createPartnerProductCopy(template, broker3, ServiceStatus.DELETED);
        createPartnerProductCopy(template, reseller2, ServiceStatus.SUSPENDED);

        // when
        List<Product> products = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() throws Exception {
                return localService
                        .getPartnerProductsForTemplate(template.getKey());
            }
        });

        // then
        assertEquals(5, products.size());
        assertEquals(brokerActiveProduct.getKey(), products.get(0).getKey());
        assertEquals(resellerInactiveProduct.getKey(),
                products.get(1).getKey());
    }

    @Test
    public void saveRevenueShareModelsForProduct_NewRevenueShares()
            throws Exception {
        // given
        final Product product = createProductForSave(tpSup, false, "mp", "tp",
                "productA", "pm");
        final RevenueShareModel brokerRevenueShare = new RevenueShareModel();
        brokerRevenueShare.setKey(1);
        brokerRevenueShare.setRevenueShare(new BigDecimal("100.00"));
        final RevenueShareModel resellerRevenueShare = new RevenueShareModel();
        resellerRevenueShare.setKey(0);
        resellerRevenueShare.setRevenueShare(new BigDecimal("100.00"));

        // when
        container.login(poUserKey, ROLE_PLATFORM_OPERATOR);
        Map<RevenueShareModelType, RevenueShareModel> revenueShares = runTX(
                new Callable<Map<RevenueShareModelType, RevenueShareModel>>() {
                    @Override
                    public Map<RevenueShareModelType, RevenueShareModel> call()
                            throws Exception {
                        return localService.saveRevenueShareModelsForProduct(
                                product.getKey(), brokerRevenueShare,
                                resellerRevenueShare, 0, 0);
                    }
                });

        // then
        assertEquals(new BigDecimal("100.00"),
                revenueShares.get(RevenueShareModelType.BROKER_REVENUE_SHARE)
                        .getRevenueShare());
        assertEquals(new BigDecimal("100.00"),
                revenueShares.get(RevenueShareModelType.RESELLER_REVENUE_SHARE)
                        .getRevenueShare());
        // validate stored values in the database
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product result = ds.getReference(Product.class,
                        product.getKey());
                List<CatalogEntry> entries = result.getCatalogEntries();
                assertEquals(1, entries.size());
                CatalogEntry ce = entries.get(0);
                RevenueShareModel brokerRevenueShare = ce.getBrokerPriceModel();
                assertNotNull(brokerRevenueShare);
                assertEquals(new BigDecimal("100.00"),
                        brokerRevenueShare.getRevenueShare());
                RevenueShareModel resellerRevenueShare = ce
                        .getBrokerPriceModel();
                assertNotNull(resellerRevenueShare);
                assertEquals(new BigDecimal("100.00"),
                        resellerRevenueShare.getRevenueShare());

                List<DomainHistoryObject<?>> brokerRevenueHistories = ds
                        .findHistory(brokerRevenueShare);
                assertEquals(1, brokerRevenueHistories.size());
                assertEquals(ModificationType.ADD,
                        brokerRevenueHistories.get(0).getModtype());

                List<DomainHistoryObject<?>> resellerRevenueHistories = ds
                        .findHistory(resellerRevenueShare);
                assertEquals(1, resellerRevenueHistories.size());
                assertEquals(ModificationType.ADD,
                        resellerRevenueHistories.get(0).getModtype());
                return null;
            }
        });
    }

    @Test
    public void saveRevenueShareModelsForProduct_UpdateRevenueShares()
            throws Exception {
        // given
        Product product = createProductForSave(tpSup, true, "mp", "tp",
                "productA", "pm");
        final long productKey = product.getKey();

        CatalogEntry catalogEntryOfProduct = getCatalogEntryForProduct(
                productKey);
        final RevenueShareModel brokerRevenueShare = new RevenueShareModel();
        brokerRevenueShare
                .setKey(catalogEntryOfProduct.getBrokerPriceModel().getKey());
        brokerRevenueShare.setRevenueShare(new BigDecimal("100.00"));
        final RevenueShareModel resellerRevenueShare = new RevenueShareModel();
        resellerRevenueShare
                .setKey(catalogEntryOfProduct.getResellerPriceModel().getKey());
        resellerRevenueShare.setRevenueShare(new BigDecimal("100.00"));

        // when
        container.login(poUserKey, ROLE_PLATFORM_OPERATOR);
        Map<RevenueShareModelType, RevenueShareModel> revenueShares = runTX(
                new Callable<Map<RevenueShareModelType, RevenueShareModel>>() {
                    @Override
                    public Map<RevenueShareModelType, RevenueShareModel> call()
                            throws Exception {
                        return localService.saveRevenueShareModelsForProduct(
                                productKey, brokerRevenueShare,
                                resellerRevenueShare, 0, 0);
                    }
                });

        // then
        assertEquals(new BigDecimal("100.00"),
                revenueShares.get(RevenueShareModelType.BROKER_REVENUE_SHARE)
                        .getRevenueShare());
        assertEquals(new BigDecimal("100.00"),
                revenueShares.get(RevenueShareModelType.RESELLER_REVENUE_SHARE)
                        .getRevenueShare());

        // validate stored values in the database
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product result = ds.getReference(Product.class, productKey);
                List<CatalogEntry> entries = result.getCatalogEntries();
                assertEquals(1, entries.size());
                CatalogEntry ce = entries.get(0);
                RevenueShareModel brokerRevenueShare = ce.getBrokerPriceModel();
                assertNotNull(brokerRevenueShare);
                assertEquals(new BigDecimal("100.00"),
                        brokerRevenueShare.getRevenueShare());
                RevenueShareModel resellerRevenueShare = ce
                        .getBrokerPriceModel();
                assertNotNull(resellerRevenueShare);
                assertEquals(new BigDecimal("100.00"),
                        resellerRevenueShare.getRevenueShare());

                List<DomainHistoryObject<?>> brokerRevenueHistories = ds
                        .findHistory(brokerRevenueShare);
                assertEquals(2, brokerRevenueHistories.size());
                assertEquals(ModificationType.MODIFY,
                        brokerRevenueHistories.get(1).getModtype());

                List<DomainHistoryObject<?>> resellerRevenueHistories = ds
                        .findHistory(resellerRevenueShare);
                assertEquals(2, resellerRevenueHistories.size());
                assertEquals(ModificationType.MODIFY,
                        resellerRevenueHistories.get(1).getModtype());
                return null;
            }
        });
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveRevenueShareModelsForProduct_concurrentExecution()
            throws Exception {
        // given
        Product product = createProductForSave(tpSup, true, "mp", "tp",
                "productA", "pm");
        final long productKey = product.getKey();

        CatalogEntry catalogEntryOfProduct = getCatalogEntryForProduct(
                productKey);
        final RevenueShareModel brokerRevenueShare = new RevenueShareModel();
        brokerRevenueShare
                .setKey(catalogEntryOfProduct.getBrokerPriceModel().getKey());
        brokerRevenueShare.setRevenueShare(new BigDecimal("100.00"));
        final RevenueShareModel resellerRevenueShare = new RevenueShareModel();
        resellerRevenueShare
                .setKey(catalogEntryOfProduct.getResellerPriceModel().getKey());
        resellerRevenueShare.setRevenueShare(new BigDecimal("100.00"));

        // when
        container.login(poUserKey, ROLE_PLATFORM_OPERATOR);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localService.saveRevenueShareModelsForProduct(productKey,
                        brokerRevenueShare, resellerRevenueShare, 0, 0);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                localService.saveRevenueShareModelsForProduct(productKey,
                        brokerRevenueShare, resellerRevenueShare, 0, 0);
                return null;
            }
        });
        fail();

        // then a ConcurrentModificationException occurs
    }

    private Organization createOrgWithRole(final OrganizationRoleType role)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, role);
            }
        });
    }

    private Product createTemplateProduct(final String orgId,
            final String productId, final String techProductId,
            final ServiceStatus status) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product product = Products.createProduct(orgId, productId,
                        techProductId, ds);
                return Products.setStatusForProduct(ds, product, status);
            }
        });

    }

    private Product createCustomerSpecificCopiedProduct(
            final Organization customer, final Product product,
            final ServiceStatus status) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return Products.createCustomerSpecifcProduct(ds, customer,
                        product, status);
            }
        });
    }

    private void createSubscriptionSpecificProductCopy(
            final Organization customer, final Product product)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), product.getProductId(),
                        "subId", System.currentTimeMillis(),
                        System.currentTimeMillis(), customer, 1);
                return null;
            }
        });
    }

    private Product createPartnerProductCopy(final Product template,
            final Organization vender, final ServiceStatus status)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product product = Products.createProductResaleCopy(template,
                        vender, ds);
                return Products.setStatusForProduct(ds, product, status);
            }
        });
    }

    private Product createProductForSave(final Organization mpOwner,
            final boolean withCatalogEntryRevenueShare,
            final String marketplaceId, final String techProductId,
            final String productId, final String priceModelId)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Marketplace marketplace = Marketplaces
                        .createMarketplace(mpOwner, marketplaceId, true, ds);
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        ds, tpSup, techProductId, false,
                        ServiceAccessType.LOGIN);
                return Products.createProduct(tpSup, tp, false, productId,
                        priceModelId, marketplace, ds,
                        withCatalogEntryRevenueShare);
            }
        });
    }

    private CatalogEntry getCatalogEntryForProduct(final long productKey)
            throws Exception {
        return runTX(new Callable<CatalogEntry>() {
            @Override
            public CatalogEntry call() throws Exception {
                Product product = ds.getReference(Product.class, productKey);
                CatalogEntry catalogEntry = product.getCatalogEntries().get(0);
                catalogEntry.setBrokerPriceModel(unproxyEntity(catalogEntry.getBrokerPriceModel()));
                catalogEntry.setResellerPriceModel(unproxyEntity(catalogEntry.getResellerPriceModel()));
                return catalogEntry;
            }
        });
    }
}
