/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningPartnerServiceLocalBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class PricingServiceBeanAllStatesServiceRevenueSharesIT
        extends EJBTestBase {

    private DataService ds;
    private PricingService pricingService;

    private Organization mpOwner;
    private long mpOwnerUserKey;
    private Marketplace marketplace;

    private Product product;
    private Product productWithCatalogEntry;

    private final BigDecimal ZERO = new BigDecimal("0.00");

    @Override
    protected void setup(final TestContainer container) {
    }

    private void setupWithContainer() throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new PricingServiceBean());
        container.addBean(new ServiceProvisioningPartnerServiceLocalBean());

        ds = container.get(DataService.class);
        pricingService = container.get(PricingService.class);

        createMarketplaceOwnerOrg();
        createMarketplace();
        createProducts();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPartnerRevenueShareForAllStatesService_NullService()
            throws Exception {
        // given
        PricingServiceBean pricingServiceBean = new PricingServiceBean();

        // when
        pricingServiceBean.getPartnerRevenueShareForAllStatesService(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPartnerRevenueShareForAllStatesService_ServiceNotFound()
            throws Exception {
        setupWithContainer();
        // given
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());

        // when
        pricingService.getPartnerRevenueShareForAllStatesService(
                new POServiceForPricing());

        // then an ObjectNotFoundException occurs

    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPartnerRevenueShareForAllStatesService_AsNotOwnerSupplier()
            throws Exception {
        setupWithContainer();
        // given a supplier
        // who is not the owner of THIS product
        container.login(givenNotOwnerSupplier().getKey(),
                UserRoleType.SERVICE_MANAGER.name());

        POServiceForPricing poService = new POServiceForPricing(
                product.getKey(), 0);

        // when
        pricingService.getPartnerRevenueShareForAllStatesService(poService);

        // then an OperationNotPermittedException occurs
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPartnerRevenueShareForAllStatesService_AsNotOwnerBroker()
            throws Exception {
        setupWithContainer();
        // given a broker
        // who is not the owner of THIS product
        container.login(givenNotOwnerBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        POServiceForPricing poService = new POServiceForPricing(
                product.getKey(), 0);

        // when
        pricingService.getPartnerRevenueShareForAllStatesService(poService);

        // then an OperationNotPermittedException occurs

    }

    @Test(expected = OperationNotPermittedException.class)
    public void getPartnerRevenueShareForAllStatesService_AsNotOwnerReseller()
            throws Exception {
        setupWithContainer();
        // given a reseller
        // who is not the owner of THIS product
        container.login(givenNotOwnerReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        POServiceForPricing poService = new POServiceForPricing(
                product.getKey(), 0);

        // when
        pricingService.getPartnerRevenueShareForAllStatesService(poService);

        // then an OperationNotPermittedException occurs
    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_AsNotOwnerPlatformOperator()
            throws Exception {
        setupWithContainer();
        // given a platform operator
        // who is not the owner of THIS product
        container.login(givenNotOwnerPlatformOperator().getKey(),
                UserRoleType.PLATFORM_OPERATOR.name());

        POServiceForPricing poService = new POServiceForPricing(
                product.getKey(), 0);

        // when
        Response r = pricingService
                .getPartnerRevenueShareForAllStatesService(poService);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));
    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_invalidRole()
            throws Exception {
        setupWithContainer();
        // given
        container.login(mpOwnerUserKey, UserRoleType.TECHNOLOGY_MANAGER.name());

        // when
        try {
            pricingService.getPartnerRevenueShareForAllStatesService(
                    new POServiceForPricing());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_NullCatalogEntryPriceModel()
            throws Exception {
        setupWithContainer();
        // given a product with no catalog entry
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());
        POServiceForPricing poService = new POServiceForPricing(
                product.getKey(), 0);

        // when
        Response r = pricingService
                .getPartnerRevenueShareForAllStatesService(poService);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));
        POPartnerPriceModel ppm = r.getResult(POPartnerPriceModel.class);
        assertEquals(0, ppm.getRevenueShareBrokerModel().getKey());
        assertEquals(0, ppm.getRevenueShareResellerModel().getKey());
    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_WithCatalogEntryPriceModel()
            throws Exception {
        setupWithContainer();
        // given a product with a catalog entry
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());
        POServiceForPricing poService = new POServiceForPricing(
                productWithCatalogEntry.getKey(), 0);

        // when
        Response r = pricingService
                .getPartnerRevenueShareForAllStatesService(poService);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));
    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_AsBroker()
            throws Exception {
        setupWithContainer();
        // given
        container.login(mpOwnerUserKey, UserRoleType.BROKER_MANAGER.name());
        POServiceForPricing poService = new POServiceForPricing(
                productWithCatalogEntry.getKey(), 0);

        // when
        Response r = pricingService
                .getPartnerRevenueShareForAllStatesService(poService);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));
    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_AsReseller()
            throws Exception {
        setupWithContainer();
        // given
        container.login(mpOwnerUserKey, UserRoleType.RESELLER_MANAGER.name());
        POServiceForPricing poService = new POServiceForPricing(
                productWithCatalogEntry.getKey(), 0);

        // when
        Response r = pricingService
                .getPartnerRevenueShareForAllStatesService(poService);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));

    }

    @Test
    public void getPartnerRevenueShareForAllStatesService_AsMarketplaceOwner()
            throws Exception {
        setupWithContainer();
        // given
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());
        POServiceForPricing poService = new POServiceForPricing(
                productWithCatalogEntry.getKey(), 0);

        // when
        Response r = pricingService
                .getPartnerRevenueShareForAllStatesService(poService);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));
    }

    @Test
    public void getRevenueShareModels() throws Exception {

        // given
        Map<RevenueShareModelType, RevenueShareModel> revenueShareModels = new HashMap<>();
        RevenueShareModel brokerPriceModel = new RevenueShareModel();
        brokerPriceModel.setRevenueShare(ZERO);
        revenueShareModels.put(RevenueShareModelType.BROKER_REVENUE_SHARE,
                brokerPriceModel);
        RevenueShareModel resellerPriceModel = new RevenueShareModel();
        resellerPriceModel.setRevenueShare(ZERO);
        revenueShareModels.put(RevenueShareModelType.RESELLER_REVENUE_SHARE,
                resellerPriceModel);

        // when
        Response r = new PricingServiceBean()
                .getRevenueShareModels(revenueShareModels);

        // then
        assertPartnerModelsExist((POPartnerPriceModel) r.getResults().get(0));
    }

    private void assertPartnerModelsExist(
            POPartnerPriceModel poPartnerPriceModel) {
        assertNotNull(poPartnerPriceModel);
        assertNotNull(poPartnerPriceModel.getRevenueShareBrokerModel());
        assertNotNull(poPartnerPriceModel.getRevenueShareResellerModel());

        assertEquals(ZERO, poPartnerPriceModel.getRevenueShareBrokerModel()
                .getRevenueShare());
        assertEquals(ZERO, poPartnerPriceModel.getRevenueShareResellerModel()
                .getRevenueShare());
    }

    private void createMarketplace() throws Exception {
        runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                marketplace = Marketplaces.createMarketplace(mpOwner, "1234567",
                        true, ds);

                return marketplace;
            }
        });
    }

    private void createProducts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct techProd = TechnicalProducts
                        .createTechnicalProduct(ds, mpOwner, "tp1", false,
                                ServiceAccessType.LOGIN);

                product = Products.createProduct(mpOwner, techProd, false,
                        "ProductId", null, marketplace, ds);

                productWithCatalogEntry = Products.createProduct(mpOwner,
                        techProd, false, "ProductIdWithCe", null, marketplace,
                        ds, true);

                return null;

            }
        });
    }

    private void createMarketplaceOwnerOrg() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(ds,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.BROKER,
                        OrganizationRoleType.RESELLER);

                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(ds, mpOwner, true, "admin");

                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.MARKETPLACE_OWNER);
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.SERVICE_MANAGER);
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.BROKER_MANAGER);
                PlatformUsers.grantRoles(ds, createUserForOrg,
                        UserRoleType.RESELLER_MANAGER);

                mpOwnerUserKey = createUserForOrg.getKey();

                return null;
            }
        });
    }

    private PlatformUser givenNotOwnerSupplier() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.SERVICE_MANAGER);
                return user;
            }
        });
    }

    private PlatformUser givenNotOwnerBroker() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user, UserRoleType.BROKER_MANAGER);
                return user;
            }
        });
    }

    private PlatformUser givenNotOwnerReseller() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.RESELLER_MANAGER);
                return user;
            }
        });
    }

    private PlatformUser givenNotOwnerPlatformOperator() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.PLATFORM_OPERATOR);
                return user;
            }
        });
    }

}
