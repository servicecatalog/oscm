/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.vatservice.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.intf.VatService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOVatRate;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class VatServiceBeanPermissionIT extends EJBTestBase {

    private DataService ds;
    private VatService vatService;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new VatServiceBean());

        ds = container.get(DataService.class);
        vatService = container.get(VatService.class);
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
                Organization broker = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                PlatformUser user = Organizations.createUserForOrg(ds, broker,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user, UserRoleType.BROKER_MANAGER);
                return user;
            }
        });
    }

    @Test
    public void saveDefaultVat_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            vatService.saveDefaultVat(new VOVatRate());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveDefaultVat_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            vatService.saveDefaultVat(new VOVatRate());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveAllVats_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            vatService.saveAllVats(new VOVatRate(),
                    new ArrayList<VOCountryVatRate>(),
                    new ArrayList<VOOrganizationVatRate>());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveVatAllVats_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            vatService.saveAllVats(new VOVatRate(),
                    new ArrayList<VOCountryVatRate>(),
                    new ArrayList<VOOrganizationVatRate>());
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }

    }

    @Test
    public void saveCountryVats_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            vatService.saveCountryVats(new ArrayList<VOCountryVatRate>());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveCountryVats_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            vatService.saveCountryVats(new ArrayList<VOCountryVatRate>());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOrganizationVats_asBroker() throws Exception {
        // given
        container.login(givenBroker().getKey(),
                UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            vatService.saveOrganizationVats(
                    new ArrayList<VOOrganizationVatRate>());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveOrganizationVats_asReseller() throws Exception {
        // given
        container.login(givenReseller().getKey(),
                UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            vatService.saveOrganizationVats(
                    new ArrayList<VOOrganizationVatRate>());
            fail("EJBException expected as operation must fail due to not allowed role!");
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }
}
