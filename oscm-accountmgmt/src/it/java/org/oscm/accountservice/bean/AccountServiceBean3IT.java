/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 11.03.2011                                                      
 *                                                                              
 *  Completion Time: 14.03.2011                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.LdapAccessServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Tests the non-service-interface methods of the account service bean. Hence
 * the methods have to be invoked in a transaction context. The EJB context is
 * needed to have the injected resources initialized in the bean.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class AccountServiceBean3IT extends EJBTestBase {

    private AccountServiceBean accountServiceBean;
    private DataService dataService;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new LocalizerServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new IdentityServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new AccountServiceBean());
        dataService = container.get(DataService.class);

        accountServiceBean = new AccountServiceBean();
        accountServiceBean.dm = dataService;
    }

    @Test(expected = SaaSSystemException.class)
    public void testGetPlatformOperatorReference_NoHit() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                accountServiceBean.getPlatformOperatorReference();
                return null;
            }
        });
    }

    @Test
    public void testGetPlatformOperatorReference_Hit() throws Exception {
        Organization initialOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                createOrganizationRoles(dataService);
                Organization org = Organizations.createOrganization(
                        dataService, OrganizationRoleType.PLATFORM_OPERATOR);
                org.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR
                        .name());
                dataService.flush();
                return org;
            }
        });
        Organization retrievedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return accountServiceBean.getPlatformOperatorReference();
            }
        });
        assertEquals(initialOrg.getKey(), retrievedOrg.getKey());
    }
}
