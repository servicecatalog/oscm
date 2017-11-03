/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 4, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class AccountServiceBeanPermissionIT extends EJBTestBase {

    AccountService as;
    DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new AccountServiceBean());

        as = container.get(AccountService.class);
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

    private Set<VOPaymentType> setupPaymentConfiguration(
            final PlatformUser user, final String... paymentTypes)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Iterator<OrganizationRoleType> it = user.getOrganization()
                        .getGrantedRoleTypes().iterator();
                OrganizationRoleType role = null;
                while (it.hasNext()) {
                    role = it.next();
                    if (role != OrganizationRoleType.CUSTOMER) {
                        break;
                    }
                }
                Organizations.addPaymentTypesToOrganizationRef(ds,
                        user.getOrganization().getOrganizationId(), role,
                        paymentTypes, null, null);
                return null;
            }
        });

        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // return
        Set<VOPaymentType> set = new HashSet<>();
        for (String type : paymentTypes) {
            VOPaymentType pt = new VOPaymentType();
            pt.setPaymentTypeId(type);
            set.add(pt);
        }
        return set;
    }

    private void validatePaymentConfiguration(final PlatformUser user,
            Set<VOPaymentType> expected) throws Exception {
        Set<String> result = runTX(new Callable<Set<String>>() {

            @Override
            public Set<String> call() throws Exception {
                Organization org = ds.getReference(Organization.class,
                        user.getOrganization().getKey());
                Iterator<OrganizationRoleType> it = user.getOrganization()
                        .getGrantedRoleTypes().iterator();
                OrganizationRoleType role = null;
                while (it.hasNext()) {
                    role = it.next();
                    if (role != OrganizationRoleType.CUSTOMER) {
                        break;
                    }
                }
                List<OrganizationRefToPaymentType> types = org.getPaymentTypes(
                        false, role,
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                Set<String> result = new HashSet<>();
                for (OrganizationRefToPaymentType dpt : types) {
                    result.add(dpt.getPaymentType().getPaymentTypeId());
                }
                return result;
            }
        });
        Assert.assertEquals(expected.size(), result.size());
        for (VOPaymentType type : expected) {
            Assert.assertTrue(result.contains(type.getPaymentTypeId()));
            result.remove(type.getPaymentTypeId());
        }
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void updateCustomerDiscount_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            as.updateCustomerDiscount(new VOOrganization());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void updateCustomerDiscount_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            as.updateCustomerDiscount(new VOOrganization());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePaymentConfiguration_asReseller() throws Exception {
        // given
        PlatformUser user = givenReseller();
        Set<VOPaymentType> expected = setupPaymentConfiguration(user, INVOICE,
                CREDIT_CARD, DIRECT_DEBIT);

        // when
        as.savePaymentConfiguration(expected, null, expected, null);

        // then
        validatePaymentConfiguration(user, expected);
    }

    @Test
    public void savePaymentConfiguration_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        try {
            // when
            as.savePaymentConfiguration(null, null, null, null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getCustomerPaymentConfiguration_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        List<VOOrganizationPaymentConfiguration> list = as
                .getCustomerPaymentConfiguration();

        // then
        assertNotNull(list);
    }

    @Test
    public void getCustomerPaymentConfiguration_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        try {
            // when
            as.getCustomerPaymentConfiguration();
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getDefaultPaymentConfiguration_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        Set<VOPaymentType> list = as.getDefaultPaymentConfiguration();

        // then
        assertNotNull(list);
    }

    @Test
    public void getDefaultPaymentConfiguration_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        try {
            // when
            as.getDefaultPaymentConfiguration();
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getDefaultServicePaymentConfiguration_asReseller()
            throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        Set<VOPaymentType> list = as.getDefaultServicePaymentConfiguration();

        // then
        assertNotNull(list);
    }

    @Test
    public void getDefaultServicePaymentConfiguration_asBroker()
            throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        try {
            // when
            as.getDefaultServicePaymentConfiguration();
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void getServicePaymentConfiguration_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        List<VOServicePaymentConfiguration> list = as
                .getServicePaymentConfiguration();

        // then
        assertNotNull(list);
    }

    @Test
    public void getServicePaymentConfiguration_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        try {
            // when
            as.getServicePaymentConfiguration();
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

}
