/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.PriceModels;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class ChargeableSubscriptionsIT extends EJBTestBase {

    private DataService ds;
    private BillingServiceBean bs;

    public void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        bs = new BillingServiceBean();

        container.addBean(new ConfigurationServiceStub());
        container.addBean(bs);

        ds = container.get(DataService.class);
        bs.dm = ds;
    }

    @Test
    public void hasChargeableSubscriptions() throws Exception {
        // given
        final long orgKey = 1L;
        givenPriceModelHistory(orgKey, Long.valueOf(1L), true);

        // when
        Boolean result = runTX(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Boolean.valueOf(bs
                        .orgHasChargeableSubscriptions(new Long(orgKey)));
            }
        });

        // then
        assertTrue(result.booleanValue());
    }

    @Test
    public void hasNoChargeableSubscriptions() throws Exception {
        // given
        final long orgKey = 1L;
        givenPriceModelHistory(orgKey, Long.valueOf(1L), false);

        // when
        Boolean result = runTX(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Boolean.valueOf(bs
                        .orgHasChargeableSubscriptions(new Long(orgKey)));
            }
        });

        // then
        assertFalse(result.booleanValue());
    }

    private void givenPriceModelHistory(final long orgKey,
            final Long activationDate, final boolean isChargeable)
            throws Exception {
        final long prdObjKey = 20L;
        final long pmObjKey = 30L;
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                PriceModels.createPriceModelHistory(ds, pmObjKey, isChargeable);
                Subscriptions.createSubscriptionHistory(ds, 1L, new Date(),
                        activationDate, 7, orgKey, prdObjKey);
                Products.createProductHistory(ds, prdObjKey,
                        Long.valueOf(pmObjKey));
                return null;
            }
        });
    }

}
