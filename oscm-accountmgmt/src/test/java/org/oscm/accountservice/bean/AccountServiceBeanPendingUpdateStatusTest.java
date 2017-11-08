/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author Qiu
 * 
 */
public class AccountServiceBeanPendingUpdateStatusTest {

    private AccountServiceBean accountServiceBean;
    private ApplicationServiceLocal appManager;
    private Subscription sub;

    @Before
    public void setup() {
        accountServiceBean = new AccountServiceBean();
        appManager = mock(ApplicationServiceLocal.class);
        accountServiceBean.appManager = appManager;

    }

    @Test
    public void activateSuspendedSubscription_SuspendedToActive() {
        // given
        sub = prepareSubscription(SubscriptionStatus.SUSPENDED);
        // when
        accountServiceBean.revokeSuspendedSubscription(sub);
        // then
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
    }

    @Test
    public void activateSuspendedSubscription_SuspendedUpdToPendingUpd() {
        // given
        sub = prepareSubscription(SubscriptionStatus.SUSPENDED_UPD);
        // when
        accountServiceBean.revokeSuspendedSubscription(sub);
        // then
        assertEquals(SubscriptionStatus.PENDING_UPD, sub.getStatus());
    }

    @Test
    public void suspendChargeableActiveSubscription_ActiveToSuspended() {
        // given
        sub = prepareSubscription(SubscriptionStatus.ACTIVE);
        // when
        accountServiceBean.suspendChargeableActiveSubscription(sub);
        // then
        assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
    }

    @Test
    public void suspendChargeableActiveSubscription_PendingUpdToSuspendedUpd() {
        // given
        sub = prepareSubscription(SubscriptionStatus.PENDING_UPD);
        // when
        accountServiceBean.suspendChargeableActiveSubscription(sub);
        // then
        assertEquals(SubscriptionStatus.SUSPENDED_UPD, sub.getStatus());
    }

    private Subscription prepareSubscription(SubscriptionStatus status) {
        Subscription sub = new Subscription();
        sub.setStatus(status);
        sub.setBillingContact(new BillingContact());
        PriceModel pm = new PriceModel();
        Product prod = new Product();
        prod.setPriceModel(pm);
        pm.setType(PriceModelType.PER_UNIT);
        sub.setProduct(prod);
        return sub;
    }
}
