/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                    
 *                                                                              
 *  Creation Date: 11.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.oscm.test.matchers.BesMatchers.isAccessDenied;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.internal.intf.OperatorService;

public class OperatorServiceBeanBillingRunIT extends EJBTestBase {
    DataService ds;
    OperatorService operatorService;

    Organization platformOrg;
    PlatformUser platformUser;

    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new SharesDataRetrievalServiceBean());
        container.addBean(new SharesCalculatorBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new BillingServiceBean());
        container.addBean(new DataServiceBean());
        container.addBean(new OperatorServiceBean());

        ds = container.get(DataService.class);
        operatorService = container.get(OperatorService.class);
    }

    @Test
    public void startBillingRun_NoLogin() throws Exception {
        try {
            operatorService.startBillingRun();
            fail();
        } catch (EJBException ex) {
            assertThat(ex, isAccessDenied());
        }
    }

    @Test
    public void startBillingRun_WrongRole() throws Exception {
        container.login(1, "WRONG_ROLE");
        try {
            operatorService.startBillingRun();
            fail();
        } catch (EJBException ex) {
            assertThat(ex, isAccessDenied());
        }
    }

    @Test
    public void startBillingRun_AdminRole() throws Exception {
        container.login("1", ROLE_ORGANIZATION_ADMIN);
        try {
            operatorService.startBillingRun();
            fail();
        } catch (EJBException ex) {
            assertThat(ex, isAccessDenied());
        }
    }

    @Test
    public void startBillingRun_NoData() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        boolean result = operatorService.startBillingRun();
        assertTrue(result);
    }

}
