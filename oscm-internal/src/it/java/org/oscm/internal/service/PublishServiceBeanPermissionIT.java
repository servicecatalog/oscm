/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class PublishServiceBeanPermissionIT extends EJBTestBase {

    private PublishService publishService;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new PublishServiceBean());

        publishService = container.get(PublishService.class);
        ds = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.setupSomeCountries(ds);
                UserRoles.createSetupRoles(ds);
                EJBTestBase.createOrganizationRoles(ds);
                createPaymentTypes(ds);
                return null;
            }
        });
    }

    private PlatformUser givenReseller() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                PlatformUser user = Organizations.createUserForOrg(ds, reseller,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.RESELLER_MANAGER);
                return user;
            }
        });
    }

    private PlatformUser givenBroker() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                PlatformUser user = Organizations.createUserForOrg(ds, reseller,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user, UserRoleType.BROKER_MANAGER);
                return user;
            }
        });
    }

    @Test
    public void getBrokerOrganizations_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        try {
            // when
            publishService.getBrokers(0L);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getResellerOrganizations_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        try {
            // when
            publishService.getResellers(0L);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getResellerOrganizations_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        try {
            // when
            publishService.getResellers(0L);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

}
