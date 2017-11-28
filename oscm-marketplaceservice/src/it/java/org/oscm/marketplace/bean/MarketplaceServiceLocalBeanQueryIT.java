/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBTransactionRequiredException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author tokoda
 * 
 */
public class MarketplaceServiceLocalBeanQueryIT extends EJBTestBase {

    private DataService mgr;
    private MarketplaceServiceLocal marketplaceLocalService;

    private Organization platformOperator;
    private long poUserKey;
    private Organization supplier;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new MarketplaceServiceLocalBean());
        mgr = container.get(DataService.class);
        marketplaceLocalService = container.get(MarketplaceServiceLocal.class);

        platformOperator = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        PlatformUser platformOperatorUser = Organizations.createUserForOrg(mgr,
                platformOperator, true, "poUser");
        PlatformUsers.grantRoles(mgr, platformOperatorUser,
                UserRoleType.PLATFORM_OPERATOR);
        poUserKey = platformOperatorUser.getKey();

        supplier = Organizations.createOrganization(mgr, "supplier",
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Organizations.createUserForOrg(mgr, supplier, true, "supplierUser");
        PlatformUsers.grantRoles(mgr, platformOperatorUser,
                UserRoleType.SERVICE_MANAGER);
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void getAllMarketplace_TransactionMandatory() {
        marketplaceLocalService.getAllMarketplaces();
    }

    @Test
    public void getAllMarketplace_NoMarketplace() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // given
                container.login(poUserKey, ROLE_PLATFORM_OPERATOR);
                // when
                List<Marketplace> result = marketplaceLocalService
                        .getAllMarketplaces();
                // then
                assertEquals(0, result.size());
                return null;
            }
        });
    }

    @Test
    public void getAllMarketplace_marketplaceExist() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // given
                container.login(poUserKey, ROLE_PLATFORM_OPERATOR);
                Marketplaces.createMarketplace(supplier, "mp1", true, mgr);
                // when
                List<Marketplace> result = marketplaceLocalService
                        .getAllMarketplaces();
                // then
                assertEquals(1, result.size());
                assertEquals("mp1", result.get(0).getMarketplaceId());
                return null;
            }
        });
    }

    @Test
    public void getMarketplacesWithRestrictedAccess() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // given
                container.login(poUserKey, ROLE_PLATFORM_OPERATOR);

                List<Organization> accessibleOrganizations = new ArrayList<>();
                accessibleOrganizations.add(supplier);
                accessibleOrganizations.add(platformOperator);

                List<Organization> otherAccessibleOrganizations = new ArrayList<>();
                otherAccessibleOrganizations.add(platformOperator);

                Marketplaces
                        .createMarketplaceWithRestrictedAccessAndAccessibleOrganizations(
                                supplier, "mp1000", mgr,
                                accessibleOrganizations);
                Marketplaces
                        .createMarketplaceWithRestrictedAccessAndAccessibleOrganizations(
                                supplier, "mp2000", mgr,
                                accessibleOrganizations);
                Marketplaces
                        .createMarketplaceWithRestrictedAccessAndAccessibleOrganizations(
                                supplier, "mp3000", mgr,
                                otherAccessibleOrganizations);

                // when
                List<Marketplace> result = marketplaceLocalService
                        .getMarketplacesForOrganizationWithRestrictedAccess(
                                supplier.getKey());
                // then
                assertEquals(2, result.size());
                boolean hasFirstMp = false;
                boolean hasSecondMp = false;
                for (Marketplace mp : result) {
                    if ("mp1000".equals(mp.getMarketplaceId())) {
                        hasFirstMp = true;
                    } else if ("mp2000".equals(mp.getMarketplaceId())) {
                        hasSecondMp = true;
                    }
                }
                assertTrue(hasFirstMp && hasSecondMp);
                return null;
            }
        });
    }

}
