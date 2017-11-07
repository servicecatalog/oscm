/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class PricingServiceBeanMarketplacesIT extends EJBTestBase {

    private DataService ds;
    private PricingService pricingService;

    private static final String OPEN_MP_ID = "OPEN_MP";

    private Organization broker;

    private long brokerUserKey;

    private final BigDecimal ZERO = new BigDecimal("0.00");

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new PricingServiceBean());

        ds = container.get(DataService.class);
        pricingService = container.get(PricingService.class);

        createPartnerOrgs();
        createMarketplaces();
    }

    private void createPartnerOrgs() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create a broker org
                broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);

                PlatformUser brokerUserForOrg = Organizations
                        .createUserForOrg(ds, broker, true, "admin");

                PlatformUsers.grantRoles(ds, brokerUserForOrg,
                        UserRoleType.BROKER_MANAGER);

                brokerUserKey = brokerUserForOrg.getKey();

                return null;
            }
        });
    }

    private void createMarketplaces() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Marketplaces.createMarketplace(broker, OPEN_MP_ID, true, ds);

                return null;
            }
        });
    }

    private PlatformUser givenTechProvider() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.TECHNOLOGY_MANAGER);
                return user;
            }
        });
    }

    @Test
    public void getPricingForMarketplace_invalidRole() throws Exception {
        // given
        container.login(givenTechProvider().getKey(),
                UserRoleType.TECHNOLOGY_MANAGER.name());

        // when
        try {
            pricingService.getPricingForMarketplace(OPEN_MP_ID);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    private PlatformUser givenMarketplaceManager() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds, org,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.MARKETPLACE_OWNER);
                return user;
            }
        });
    }

    /**
     * testcase for fixing Bug#9414
     **/
    @Test
    public void getPricingForMarketplace_WithMarketplaceManagerRole()
            throws Exception {
        // given
        container.login(givenMarketplaceManager().getKey(),
                UserRoleType.MARKETPLACE_OWNER.name());
        // when
        Response response = pricingService.getPricingForMarketplace(OPEN_MP_ID);
        // then
        assertNotNull(response);
        assertTrue(response.getResults().size() > 0);
    }

    @Test
    public void getPricingForMarketplace() throws Exception {
        // given
        container.login(brokerUserKey, UserRoleType.BROKER_MANAGER.name());

        // when
        Response response = pricingService.getPricingForMarketplace(OPEN_MP_ID);

        // then
        assertNotNull(response);

        List<Object> pricings = response.getResults();
        assertNotNull("List of marketplaces expected - ", pricings);
        assertEquals("One pricing expected - ", 1, pricings.size());

        POMarketplacePricing poPricing = (POMarketplacePricing) pricings.get(0);
        assertEquals("Open marketplace expected - ", OPEN_MP_ID,
                poPricing.getMarketplace().getMarketplaceId());
        assertEquals("Marketplace revenue share should be zero", ZERO,
                poPricing.getMarketplacePriceModel().getRevenueShare()
                        .getRevenueShare());
        assertEquals("Broker revenue share should be zero", ZERO,
                poPricing.getPartnerPriceModel().getRevenueShareBrokerModel()
                        .getRevenueShare());
        assertEquals("Reseller revenue share should be zero", ZERO,
                poPricing.getPartnerPriceModel().getRevenueShareResellerModel()
                        .getRevenueShare());
    }
}
