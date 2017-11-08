/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-12-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.accountmgmt.AccountServiceManagement;
import org.oscm.internal.accountmgmt.AccountServiceManagementBean;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOOrganization;

/**
 * @author Administrator
 * 
 */
public class AccountServiceManagementIT extends EJBTestBase {

    private AccountServiceManagement accountService;
    private DataService dataService;
    private PlatformUser platformUser;
    private Organization org;
    private VOOrganization voOrg;
    private long supplierKey;
    private AccountServiceManagementBean accountServiceBean;
    private PlatformUser supplierUser;
    private PlatformUser serviceManager;
    private PlatformUser resellerManager;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        dataService = mock(DataService.class);
        container.addBean(dataService);

        platformUser = new PlatformUser();
        org = new Organization();
        org.setKey(1111);
        org.setCutOffDay(12);
        platformUser.setOrganization(org);
        voOrg = new VOOrganization();
        voOrg.setKey(org.getKey());

        accountServiceBean = spy(new AccountServiceManagementBean());
        container.addBean(accountServiceBean);
        accountService = container.get(AccountServiceManagement.class);

        doAnswer(new Answer<PlatformUser>() {
            @Override
            public PlatformUser answer(InvocationOnMock invocation)
                    throws Throwable {
                return platformUser;
            }
        }).when(dataService).getCurrentUser();

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.createOrganization(
                        dataService, OrganizationRoleType.SUPPLIER);
                supplierUser = Organizations.createUserForOrg(dataService,
                        supplier, true, "SuppAdmin");
                return supplier;
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization serviceManagerOrg = Organizations
                        .createOrganization(dataService,
                                OrganizationRoleType.SUPPLIER);
                serviceManager = Organizations.createUserForOrg(dataService,
                        serviceManagerOrg, false, "ServiceManager");
                return serviceManagerOrg;
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization resellerManagerOrg = Organizations
                        .createOrganization(dataService,
                                OrganizationRoleType.SUPPLIER);
                resellerManager = Organizations.createUserForOrg(dataService,
                        resellerManagerOrg, false, "ResellerManager");
                return resellerManagerOrg;
            }
        });

    }

    @Test
    public void getCutOffDayOfOrganization_SerivceManger() throws Exception {
        // given
        container.login(supplierKey, ROLE_SERVICE_MANAGER);
        // when
        int result = accountService.getCutOffDayOfOrganization();
        // then
        assertEquals(12, result);
    }

    @Test
    public void getCutOffDayOfOrganization_RESELLER() throws Exception {

        // given
        container.login(supplierKey, ROLE_RESELLER_MANAGER);
        // when
        int result = accountService.getCutOffDayOfOrganization();
        // then
        assertEquals(12, result);
    }

    @Test(expected = EJBException.class)
    public void getCutOffDayOfOrganization_brokenManger() throws Exception {
        // given
        container.login(supplierKey, ROLE_BROKER_MANAGER);
        // when
        accountService.getCutOffDayOfOrganization();
    }

    @Test
    public void setCutOffDayOfOrganization_SerivceManger() throws Exception {
        // given
        container.login(1, ROLE_SERVICE_MANAGER);
        // when
        accountService.setCutOffDayOfOrganization(13, voOrg);
        int result = accountService.getCutOffDayOfOrganization();
        // then
        assertEquals(13, result);
    }

    @Test
    public void setCutOffDayOfOrganization_RESELLER() throws Exception {
        // given
        container.login(1, ROLE_RESELLER_MANAGER);
        // when
        accountService.setCutOffDayOfOrganization(13, voOrg);
        int result = accountService.getCutOffDayOfOrganization();
        // then
        assertEquals(13, result);
    }

    @Test(expected = EJBException.class)
    public void setCutOffDayOfOrganization_brokenManger() throws Exception {
        // given
        container.login(1, ROLE_BROKER_MANAGER);
        // when
        accountService.setCutOffDayOfOrganization(13, voOrg);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void setCutOffDayOfOrganization_concurrentHandling()
            throws Exception {
        // given
        container.login(1, ROLE_SERVICE_MANAGER);
        voOrg.setVersion(-1);
        // when
        accountService.setCutOffDayOfOrganization(13, voOrg);
    }

    @Test
    public void getOrgainizationData_ServiceManager() throws Exception {
        container.login(serviceManager.getKey(),
                UserRoleType.SERVICE_MANAGER.toString());
        final VOOrganization voOrganization = accountService
                .getOrganizationData();
        assertNotNull(voOrganization);
    }

    @Test
    public void getOrgainizationData_ResellerManager() throws Exception {
        container.login(resellerManager.getKey(),
                UserRoleType.RESELLER_MANAGER.toString());
        final VOOrganization voOrganization = accountService
                .getOrganizationData();
        assertNotNull(voOrganization);
    }

    @Test
    public void getOrgainizationData_OrgAdmin() throws Exception {
        container.login(supplierUser.getKey(),
                UserRoleType.ORGANIZATION_ADMIN.toString());
        try {
            accountService.getOrganizationData();
            fail("Expected EJBAccessException saying that only a user having the SERVICE_MANAGER role or RESELLER_MANAGER can call it");
        } catch (EJBException e) {
            assertTrue(
                    "Expected cause must be an EJBAccessException saying that only a user having the SERVICE_MANAGER  or RESELLER_MANAGER role can call it",
                    e.getCause() instanceof EJBAccessException);
        }

    }
}
