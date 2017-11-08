/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.marketplace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.vo.VOMarketplace;

/**
 * @author barzu
 */
public class MarketplaceServiceManagePartnerBeanRolesIT extends EJBTestBase {

    private MarketplaceServiceManagePartner service;
    private DataService ds;
    private PlatformUser user;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        ds = mock(DataService.class);
        container.addBean(ds);
        MarketplaceServiceLocalBean mpLocal = mock(MarketplaceServiceLocalBean.class);
        container.addBean(mpLocal);
        container.addBean(new MarketplaceServiceManagePartnerBean());

        service = container.get(MarketplaceServiceManagePartner.class);

        user = new PlatformUser();
        user.setKey(1L);
        user.setLocale("en");
        doReturn(user).when(ds).getCurrentUser();

        Marketplace marketplace = MarketplaceServiceManagePartnerBeanTest
                .givenMarketplace();
        doReturn(marketplace).when(mpLocal).getMarketplace(anyString());
    }

    @Test
    public void updateMarketplace_PlatformOperator() throws Exception {
        // given
        container.login(user.getKey(), ROLE_PLATFORM_OPERATOR);
        // when
        Response response = service.updateMarketplace(
                MarketplaceServiceManagePartnerBeanTest.givenVOMarketplace(),
                MarketplaceServiceManagePartnerBeanTest
                        .givenPOMarketplacePriceModel(),
                MarketplaceServiceManagePartnerBeanTest
                        .givenPOPartnerPriceModel());
        // then
        assertNotNull(response.getResult(VOMarketplace.class));
    }

    @Test
    public void updateMarketplace_MarketplaceOwner() throws Exception {
        // given
        container.login(user.getKey(), ROLE_MARKETPLACE_OWNER);
        // when
        Response response = service.updateMarketplace(
                MarketplaceServiceManagePartnerBeanTest.givenVOMarketplace(),
                MarketplaceServiceManagePartnerBeanTest
                        .givenPOMarketplacePriceModel(),
                MarketplaceServiceManagePartnerBeanTest
                        .givenPOPartnerPriceModel());
        // then
        assertNotNull(response.getResult(VOMarketplace.class));
    }

    @Test
    public void updateMarketplace_ServiceManager() throws Exception {
        container.login(user.getKey(), ROLE_SERVICE_MANAGER);
        try {
            service.updateMarketplace(null, null, null);
            fail();
        } catch (EJBException ex) {
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void updateMarketplace_TechnologyManager() throws Exception {
        container.login(user.getKey(), ROLE_TECHNOLOGY_MANAGER);
        try {
            service.updateMarketplace(null, null, null);
            fail();
        } catch (EJBException ex) {
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void updateMarketplace_ResellerManager() throws Exception {
        container.login(user.getKey(), ROLE_RESELLER_MANAGER);
        try {
            service.updateMarketplace(null, null, null);
            fail();
        } catch (EJBException ex) {
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void updateMarketplace_BrokerManager() throws Exception {
        container.login(user.getKey(), ROLE_BROKER_MANAGER);
        try {
            service.updateMarketplace(null, null, null);
            fail();
        } catch (EJBException ex) {
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void updateMarketplace_Admin() throws Exception {
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        try {
            service.updateMarketplace(null, null, null);
            fail();
        } catch (EJBException ex) {
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void updateMarketplace_Customer() throws Exception {
        container.login(user.getKey());
        try {
            service.updateMarketplace(null, null, null);
            fail();
        } catch (EJBException ex) {
            assertTrue(ex.getCause() instanceof EJBAccessException);
        }
    }
}
