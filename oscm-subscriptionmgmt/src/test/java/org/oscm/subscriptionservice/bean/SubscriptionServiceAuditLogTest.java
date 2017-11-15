/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 08.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;

public class SubscriptionServiceAuditLogTest {
    private SubscriptionServiceBean service;
    private DataService ds;

    @Before
    public void setup() {
        service = spy(new SubscriptionServiceBean());
        service.audit = mock(SubscriptionAuditLogCollector.class);
        ds = mock(DataService.class);
        service.modUpgBean = mock(ModifyAndUpgradeSubscriptionBean.class);
        service.dataManager = ds;
    }

    @Test
    public void testUnsubscribe() {
        // given
        Subscription sub = givenSubscription();

        // when
        service.unsubscribe(sub);

        // verify
        verify(service.modUpgBean, times(1))
                .deleteModifiedEntityForSubscription(eq(sub));
        verify(service.audit, times(1)).unsubscribeFromService(eq(ds), eq(sub));
    }

    private Subscription givenSubscription() {
        Subscription subscription = new Subscription();
        subscription.setProduct(new Product());
        return subscription;
    }
}
