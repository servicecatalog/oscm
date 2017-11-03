/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014年11月13日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author gaowenxin
 * 
 */
public class PaymentTypeDaoIT extends EJBTestBase {

    private DataService ds;
    private PaymentTypeDao dao;

    private Organization supplier;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new PaymentTypeDao());

        ds = container.get(DataService.class);
        dao = container.get(PaymentTypeDao.class);
        initData();
        container.login(supplier.getKey());
    }

    @Test
    public void retrievePaymentTypeForCustomer_NoResults() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<OrganizationRefToPaymentType> result = dao
                        .retrievePaymentTypeForCustomer(supplier);
                assertNotNull(result);
                assertTrue(result.isEmpty());
                return null;
            }
        });
    }

    @Test
    public void retrieveTechnicalProduct_SeveralProviders() throws Exception {
    }

    private void initData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
    }
}
