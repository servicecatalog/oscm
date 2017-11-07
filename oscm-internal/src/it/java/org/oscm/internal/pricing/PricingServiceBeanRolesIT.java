/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 14.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

/**
 * Tests the roles of the users calling the methods of
 * {@linkplain PricingServiceBean}
 * 
 * @author barzu
 */
public class PricingServiceBeanRolesIT extends EJBTestBase {

    private PricingService bean;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(mock(DataService.class));
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(mock(MarketplaceServiceLocal.class));
        container.addBean(mock(ServiceProvisioningPartnerServiceLocal.class));
        container.addBean(new PricingServiceBean());
        bean = container.get(PricingService.class);
    }

    @Test
    public void getOperatorRevenueShare_PLATFORM_OPERATOR() throws Exception {
        // given
        container.login(0L, ROLE_PLATFORM_OPERATOR);
        // when
        bean.getOperatorRevenueShare(101L);
    }

    @Test
    public void getOperatorRevenueShare_SERVICE_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_SERVICE_MANAGER);
        // when
        bean.getOperatorRevenueShare(101L);
    }

    @Test
    public void getOperatorRevenueShare_RESELLER_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_RESELLER_MANAGER);
        // when
        bean.getOperatorRevenueShare(101L);
    }

    @Test
    public void getOperatorRevenueShare_BROKER_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_BROKER_MANAGER);
        // when
        bean.getOperatorRevenueShare(101L);
    }

    @Test
    public void getOperatorRevenueShare_MARKETPLACE_OWNER() throws Exception {
        // given
        container.login(0L, ROLE_MARKETPLACE_OWNER);
        try {
            // when
            bean.getOperatorRevenueShare(101L);
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void getOperatorRevenueShare_TECHNOLOGY_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_TECHNOLOGY_MANAGER);
        try {
            // when
            bean.getOperatorRevenueShare(101L);
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void getOperatorRevenueShare_CUSTOMER() throws Exception {
        // given
        container.login(0L);
        try {
            // when
            bean.getOperatorRevenueShare(101L);
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOperatorRevenueShare_PLATFORM_OPERATOR() throws Exception {
        // given
        container.login(0L, ROLE_PLATFORM_OPERATOR);
        // when
        bean.saveOperatorRevenueShare(101L, new PORevenueShare());
    }

    @Test
    public void saveOperatorRevenueShare_SERVICE_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_SERVICE_MANAGER);
        try {
            // when
            bean.saveOperatorRevenueShare(101L, new PORevenueShare());
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOperatorRevenueShare_RESELLER_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_RESELLER_MANAGER);
        try {
            // when
            bean.saveOperatorRevenueShare(101L, new PORevenueShare());
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOperatorRevenueShare_BROKER_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_BROKER_MANAGER);
        try {
            // when
            bean.saveOperatorRevenueShare(101L, new PORevenueShare());
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOperatorRevenueShare_MARKETPLACE_OWNER() throws Exception {
        // given
        container.login(0L, ROLE_MARKETPLACE_OWNER);
        try {
            // when
            bean.saveOperatorRevenueShare(101L, new PORevenueShare());
            fail();
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOperatorRevenueShare_TECHNOLOGY_MANAGER() throws Exception {
        // given
        container.login(0L, ROLE_TECHNOLOGY_MANAGER);
        try {
            // when
            bean.saveOperatorRevenueShare(101L, new PORevenueShare());
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOperatorRevenueShare_CUSTOMER() throws Exception {
        // given
        container.login(0L);
        try {
            // when
            bean.saveOperatorRevenueShare(101L, new PORevenueShare());
        } catch (EJBException e) {
            // then
            assertTrue(e.getCause() instanceof EJBAccessException);
        }
    }

}
