/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.CustomerData;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class SubscriptionsForCustomerIT extends EJBTestBase {

    private DataService dm;
    private BillingDataRetrievalServiceLocal bdrs;

    @Override
    public void setup(final TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        dm = container.get(DataService.class);
        bdrs = container.get(BillingDataRetrievalServiceLocal.class);
    }

    @Test
    public void getSubscriptionsForCustomer_activationDateNull()
            throws Exception {
        // given
        final long orgKey = 1L;
        givenSubscriptionHistory(orgKey, null);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdrs.loadSubscriptionsForCustomer(
                        orgKey, 0, new Date().getTime(), -1));
            }
        });

        // then
        assertTrue(billingInput.getSubscriptionKeys().isEmpty());
    }

    @Test
    public void getSubscriptionsForCustomer_activationDateSet()
            throws Exception {
        // given
        final long orgKey = 1L;
        givenSubscriptionHistory(orgKey, new Long(1));

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdrs.loadSubscriptionsForCustomer(
                        orgKey, 0, new Date().getTime() + 1000, -1));
            }
        });

        assertEquals(1, billingInput.getSubscriptionKeys().size());
    }

    @Test
    public void getSubscriptionsForCustomer_LastBeforePeriod()
            throws Exception {
        // given
        final long orgKey = 1L;
        givenSubscriptionHistory(orgKey,
                new Date(-PricingPeriod.DAY.getMilliseconds() * 35 - 1), 0);
        givenSubscriptionHistory(orgKey,
                new Date(-PricingPeriod.DAY.getMilliseconds() * 35), 1);
        givenSubscriptionHistory(orgKey, new Date(1), 2);
        givenSubscriptionHistory(orgKey, new Date(2), 3);
        givenSubscriptionHistory(orgKey, new Date(3), 4);
        // history of another organization in the period
        givenSubscriptionHistory(2, new Date(4), 0);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(
                        bdrs.loadSubscriptionsForCustomer(orgKey, 4, 10, -1));
            }
        });

        assertEquals(1, billingInput.getSubscriptionKeys().size());
        // must return only the last history entry before start
        List<SubscriptionHistory> list = billingInput
                .getSubscriptionHistoryEntries(1);
        assertEquals(4, list.size());

        SubscriptionHistory sh = list.get(0);
        assertEquals(orgKey, sh.getObjKey());
        assertEquals(4, sh.getObjVersion());
        assertEquals(3, sh.getModdate().getTime());
        sh = list.get(3);
        assertEquals(orgKey, sh.getObjKey());
        assertEquals(1, sh.getObjVersion());
        assertEquals(-PricingPeriod.DAY.getMilliseconds() * 35,
                sh.getModdate().getTime());
    }

    @Test
    public void getSubscriptionsForCustomer_LastBeforePeriodAndInPeriod()
            throws Exception {
        // given
        final long orgKey = 1L;
        givenSubscriptionHistory(orgKey,
                new Date(-PricingPeriod.DAY.getMilliseconds() * 35 - 1), 0);
        givenSubscriptionHistory(orgKey,
                new Date(-PricingPeriod.DAY.getMilliseconds() * 35), 1);
        // before period - not included
        givenSubscriptionHistory(orgKey, new Date(0), 2);
        // before period - included
        givenSubscriptionHistory(orgKey, new Date(1), 3);
        // in period
        givenSubscriptionHistory(orgKey, new Date(2), 4);
        givenSubscriptionHistory(orgKey, new Date(3), 5);
        // history of another organization in the period
        givenSubscriptionHistory(2, new Date(4), 0);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {
            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(
                        bdrs.loadSubscriptionsForCustomer(orgKey, 2, 10, -1));
            }
        });

        assertEquals(1, billingInput.getSubscriptionKeys().size());
        List<SubscriptionHistory> list = billingInput
                .getSubscriptionHistoryEntries(1);
        assertEquals(5, list.size());
    }

    private void givenSubscriptionHistory(final long orgKey,
            final Long activationDate) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscriptionHistory(dm, 1L, new Date(),
                        activationDate, 7, orgKey);
                return null;
            }
        });
    }

    private void givenSubscriptionHistory(final long orgKey, final Date modDate,
            final int version) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscriptionHistory(dm, 1L, modDate,
                        Long.valueOf(1), version, orgKey);
                return null;
            }
        });
    }

}
