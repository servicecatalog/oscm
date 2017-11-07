/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class PricingServiceBeanContainerIT extends EJBTestBase {

    private static final String UNKNOWN_MARKETPLACEID = "unknownMarketplaceId";
    private static final String MARKETPLACEID = "1234567";
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private DataService ds;
    private PricingService pricingService;

    private Organization mpOwner;
    private long mpOwnerUserKey;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new MarketplaceServiceLocalBean());
        PricingServiceBean bean = new PricingServiceBean();

        container.addBean(bean);
        ds = container.get(DataService.class);
        pricingService = container.get(PricingService.class);

        createMarketplaceOwnerOrg();
        createMarketplace();
    }

    private void createMarketplace() throws Exception {
        runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace marketplace = Marketplaces
                        .createMarketplace(mpOwner, MARKETPLACEID, true, ds);
                return marketplace;
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

    private PlatformUser givenNotOwnerUser() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                return user;
            }
        });
    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_AsNotOwner()
            throws Exception {

        // given a user with role MARKETPLACE_OWNER,
        // who is not the owner of THIS marketplace
        container.login(givenNotOwnerUser().getKey(),
                UserRoleType.MARKETPLACE_OWNER.name());

        // when
        Response r = pricingService
                .getPartnerRevenueSharesForMarketplace(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareBrokerModel().getRevenueShare());
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareResellerModel().getRevenueShare());
    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_invalidRole()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.TECHNOLOGY_MANAGER.name());

        // when
        try {
            pricingService.getPartnerRevenueSharesForMarketplace(MARKETPLACEID);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_AsPlatformOperator()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.PLATFORM_OPERATOR.name());

        // when
        Response r = pricingService
                .getPartnerRevenueSharesForMarketplace(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareBrokerModel().getRevenueShare());
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareResellerModel().getRevenueShare());

    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_AsMarketplaceOwner()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        Response r = pricingService
                .getPartnerRevenueSharesForMarketplace(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareBrokerModel().getRevenueShare());
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareResellerModel().getRevenueShare());

    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_AsSupplier()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());

        // when
        Response r = pricingService
                .getPartnerRevenueSharesForMarketplace(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareBrokerModel().getRevenueShare());
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareResellerModel().getRevenueShare());

    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_AsBroker()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.BROKER_MANAGER.name());

        // when
        Response r = pricingService
                .getPartnerRevenueSharesForMarketplace(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareBrokerModel().getRevenueShare());
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareResellerModel().getRevenueShare());

    }

    @Test
    public void getPartnerRevenueSharesForMarketplace_AsReseller()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.RESELLER_MANAGER.name());

        // when
        Response r = pricingService
                .getPartnerRevenueSharesForMarketplace(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareBrokerModel().getRevenueShare());
        assertEquals(ZERO, ((POPartnerPriceModel) r.getResults().get(0))
                .getRevenueShareResellerModel().getRevenueShare());

    }

    @Test(expected = EJBException.class)
    public void getMarketplaceRevenueShares_NullMarketplaceId()
            throws Exception {

        // given
        container.login(givenNotOwnerUser().getKey(),
                UserRoleType.MARKETPLACE_OWNER.name());

        // when
        pricingService.getMarketplaceRevenueShares(null);
    }

    @Test
    public void getMarketplaceRevenueShares_AsNotOwner() throws Exception {
        // given a user with role MARKETPLACE_OWNER,
        // who is not the owner of THIS marketplace
        container.login(givenNotOwnerUser().getKey(),
                UserRoleType.MARKETPLACE_OWNER.name());

        // when
        Response r = pricingService.getMarketplaceRevenueShares(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POMarketplacePriceModel) r.getResults().get(0))
                .getRevenueShare().getRevenueShare());
    }

    @Test
    public void getMarketplaceRevenueShares_invalidRole() throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.TECHNOLOGY_MANAGER.name());

        // when
        try {
            pricingService.getMarketplaceRevenueShares(MARKETPLACEID);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getMarketplaceRevenueShares_marketplaceNotFound()
            throws Exception {
        // given
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());

        // when
        pricingService.getMarketplaceRevenueShares(UNKNOWN_MARKETPLACEID);
    }

    @Test
    public void getMarketplaceRevenueShares_AsPlatformOperator()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.PLATFORM_OPERATOR.name());

        // when
        Response r = pricingService.getMarketplaceRevenueShares(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POMarketplacePriceModel) r.getResults().get(0))
                .getRevenueShare().getRevenueShare());
    }

    @Test
    public void getMarketplaceRevenueShares_AsMarketplaceOwner()
            throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.MARKETPLACE_OWNER.name());

        // when
        Response r = pricingService.getMarketplaceRevenueShares(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POMarketplacePriceModel) r.getResults().get(0))
                .getRevenueShare().getRevenueShare());
    }

    @Test
    public void getMarketplaceRevenueShares_AsSupplier() throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());

        // when
        Response r = pricingService.getMarketplaceRevenueShares(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POMarketplacePriceModel) r.getResults().get(0))
                .getRevenueShare().getRevenueShare());
    }

    @Test
    public void getMarketplaceRevenueShares_AsBroker() throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.BROKER_MANAGER.name());

        // when
        Response r = pricingService.getMarketplaceRevenueShares(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POMarketplacePriceModel) r.getResults().get(0))
                .getRevenueShare().getRevenueShare());
    }

    @Test
    public void getMarketplaceRevenueShares_AsReseller() throws Exception {

        // given
        container.login(mpOwnerUserKey, UserRoleType.RESELLER_MANAGER.name());

        // when
        Response r = pricingService.getMarketplaceRevenueShares(MARKETPLACEID);

        // then
        assertNotNull(r.getResults().get(0));
        assertEquals(ZERO, ((POMarketplacePriceModel) r.getResults().get(0))
                .getRevenueShare().getRevenueShare());
    }

    @Test(expected = EJBException.class)
    public void getPartnerRevenueShares_NullMarketplaceId() throws Exception {

        // given
        container.login(givenNotOwnerUser().getKey(),
                UserRoleType.MARKETPLACE_OWNER.name());

        // when
        pricingService.getPartnerRevenueSharesForMarketplace(null);
    }

    @Test
    public void getTemplateServices_InvalidRole() {
        // given
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());
        try {
            // when
            pricingService.getTemplateServices();
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getMarketplacePricingForService_InvalidRole() throws Exception {
        // given
        container.login(mpOwnerUserKey, UserRoleType.SERVICE_MANAGER.name());
        try {
            // when
            pricingService.getMarketplacePricingForService(null);
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePartnerRevenueSharesForServices_InvalidRole()
            throws Exception {
        // given
        container.login(mpOwnerUserKey, ROLE_SERVICE_MANAGER);
        // when
        try {
            pricingService.savePartnerRevenueSharesForServices(null);
            fail();
        } catch (EJBException ex) {
            // then
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }
}
