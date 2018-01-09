/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;
import org.mockito.Matchers;

import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.bean.HibernateIndexer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.bean.LdapAccessStub;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Scenario;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOTechnicalService;

public class AccountServiceBean2IT extends EJBTestBase {

    protected DataService mgr;

    private AccountService accountMgmt;

    private VOBillingContact voBillingContactUnsaved;

    private LocalizerServiceLocal localizer;

    private MarketingPermissionServiceLocal marketingPermissionMock;

    @Override
    public void setup(final TestContainer container) throws Exception {
        container.login("1");
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace) {
            }
        });
        container.addBean(new LdapAccessStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new IdentityServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        marketingPermissionMock = mock(MarketingPermissionServiceLocal.class);
        container.addBean(marketingPermissionMock);
        container.addBean(new AccountServiceBean());

        mgr = container.get(DataService.class);
        accountMgmt = container.get(AccountService.class);
        localizer = container.get(LocalizerServiceLocal.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Scenario.setup(container, true);
                return null;
            }
        });

        container.login(Scenario.getCustomerAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long key = Scenario.getSupplier().getKey();
                localizer.storeLocalizedResource("en", key,
                        LocalizedObjectTypes.ORGANIZATION_DESCRIPTION,
                        "english description");
                return null;
            }
        });
        voBillingContactUnsaved = new VOBillingContact();
        voBillingContactUnsaved.setAddress("company address");
        voBillingContactUnsaved.setCompanyName("company name");
        voBillingContactUnsaved.setEmail("company@email.com");
        voBillingContactUnsaved.setId("bcname");
        accountMgmt.saveBillingContact(voBillingContactUnsaved);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetSupplier_NonExistingSupplier() throws Exception {
        accountMgmt.getSeller("bla", "en");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetSupplier_NullSupplierId() throws Exception {
        accountMgmt.getSeller(null, "en");
    }

    @Test
    public void testGetSupplier_NullLocale() throws Exception {
        VOOrganization supplier = accountMgmt.getSeller(Scenario.getSupplier()
                .getOrganizationId(), null);
        assertNotNull(supplier);
        assertEquals("english description", supplier.getDescription());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetSupplier_NoSupplierOrg() throws Exception {
        accountMgmt.getSeller(Scenario.getCustomer().getOrganizationId(), "en");
    }

    @Test
    public void testGetSupplier_GoodCase() throws Exception {
        VOOrganization supplier = accountMgmt.getSeller(Scenario.getSupplier()
                .getOrganizationId(), "en");
        assertNotNull(supplier);
        assertEquals(Scenario.getSupplier().getOrganizationId(),
                supplier.getOrganizationId());
        assertTrue(supplier.getKey() > 0);
    }

    @Test
    public void removeMarketingPermission_Unauthorized() throws Exception {
        try {
            container.login(2);
            accountMgmt.removeSuppliersFromTechnicalService(
                    new VOTechnicalService(), new ArrayList<String>());
            fail("Operation should have failed");
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("EJBAccessException"));
        }
    }

    @Test
    public void removeMarketingPermission_NullTechnicalService()
            throws Exception {
        try {
            container.login(1, ROLE_TECHNOLOGY_MANAGER);
            accountMgmt.removeSuppliersFromTechnicalService(null,
                    new ArrayList<String>());
            fail("Operation should have failed");
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("IllegalArgumentException"));
        }
    }

    @Test
    public void removeMarketingPermission_NullOrgList() throws Exception {
        try {
            container.login(1, ROLE_TECHNOLOGY_MANAGER);
            accountMgmt.removeSuppliersFromTechnicalService(
                    new VOTechnicalService(), null);
            fail("Operation should have failed");
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("IllegalArgumentException"));
        }
    }

    @Test
    public void removeMarketingPermission_GoodCase() throws Exception {
        container.login(1, ROLE_TECHNOLOGY_MANAGER);
        accountMgmt.removeSuppliersFromTechnicalService(
                new VOTechnicalService(), new ArrayList<String>());
        verify(marketingPermissionMock, times(1)).removeMarketingPermission(
                Matchers.anyLong(), Matchers.anyListOf(String.class));
    }

    @Test
    public void getSuppliersForTechnicalService_NotAuthorized()
            throws Exception {
        try {
            accountMgmt
                    .getSuppliersForTechnicalService(new VOTechnicalService());
            fail();
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("EJBAccessException"));
        }
    }

    @Test
    public void getSuppliersForTechnicalService_NullArgument() throws Exception {
        container.login(1, ROLE_TECHNOLOGY_MANAGER);
        try {
            accountMgmt.getSuppliersForTechnicalService(null);
            fail();
        } catch (EJBException e) {
            assertTrue(e.getMessage().contains("IllegalArgumentException"));
        }
    }

    @Test
    public void getSuppliersForTechnicalService_Positive() throws Exception {
        container.login(Scenario.getSupplierAdminUser().getKey(),
                ROLE_TECHNOLOGY_MANAGER);
        VOTechnicalService ts = new VOTechnicalService();
        ts.setKey(25);
        when(
                marketingPermissionMock
                        .getSuppliersForTechnicalService(Matchers.anyLong()))
                .thenReturn(Arrays.asList(Scenario.getSupplier()));
        List<VOOrganization> result = accountMgmt
                .getSuppliersForTechnicalService(ts);
        verify(marketingPermissionMock, times(1))
                .getSuppliersForTechnicalService(Matchers.anyLong());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Scenario.getSupplier().getKey(), result.get(0).getKey());
    }
}
