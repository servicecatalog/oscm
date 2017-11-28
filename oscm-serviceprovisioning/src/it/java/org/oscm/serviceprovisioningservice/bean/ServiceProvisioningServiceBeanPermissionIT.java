/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 4, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Test;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServiceProvisioningServiceBeanPermissionIT extends EJBTestBase {

    ServiceProvisioningService sps;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ServiceProvisioningServiceBean());

        sps = container.get(ServiceProvisioningService.class);
    }

    @Test
    public void savePriceModelForSubscription_asReseller() throws Exception {
        // given
        container.login(1L, UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            sps.savePriceModelForSubscription(new VOServiceDetails(),
                    new VOPriceModel());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePriceModelForSubscription_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.savePriceModelForSubscription(new VOServiceDetails(),
                    new VOPriceModel());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePriceModel_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.savePriceModel(new VOServiceDetails(), new VOPriceModel());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePriceModel_asReseller() throws Exception {
        // given
        container.login(1L, UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            sps.savePriceModel(new VOServiceDetails(), new VOPriceModel());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePriceModelForCustomer_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.savePriceModelForCustomer(new VOServiceDetails(),
                    new VOPriceModel(), new VOOrganization());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePriceModelForCustomer_asReseller() throws Exception {
        // given
        container.login(1L, UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            sps.savePriceModelForCustomer(new VOServiceDetails(),
                    new VOPriceModel(), new VOOrganization());
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }

    }

    @Test
    public void updateService_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.updateService(new VOServiceDetails(), null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }

    }

    @Test
    public void updateService_asReseller() throws Exception {
        // given
        container.login(1L, UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            sps.updateService(new VOServiceDetails(), null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }

    }

    @Test
    public void copyService_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.copyService(new VOServiceDetails(), "NewService");
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void copyService_asReseller() throws Exception {
        // given
        container.login(1L, UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            sps.copyService(new VOServiceDetails(), "NewService");
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void setCompatibleServices_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.setCompatibleServices(new VOService(), null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void setCompatibleServices_asReseller() throws Exception {
        // given
        container.login(1L, UserRoleType.RESELLER_MANAGER.name());

        // when
        try {
            sps.setCompatibleServices(new VOService(), null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void saveServiceLocalization_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.saveServiceLocalization(null, null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

    @Test
    public void savePriceModelLocalization_asBroker() throws Exception {
        // given
        container.login(1L, UserRoleType.BROKER_MANAGER.name());

        // when
        try {
            sps.savePriceModelLocalization(null, null);
            fail();
        } catch (EJBException e) {

            // then
            assertTrue(e.getCausedByException() instanceof EJBAccessException);
        }
    }

}
