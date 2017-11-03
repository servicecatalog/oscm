/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.12.2013                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;

public class AccountServiceBeanPaymentTypesTest {

    private AccountServiceBean accountServiceBean = spy(new AccountServiceBean());
    private List<PaymentType> listPT = new ArrayList<PaymentType>();
    private PaymentType pt = new PaymentType();
    private long PT_KEY = 1L;
    private long SERVICE_KEY = 10L;
    private Subscription subscription = new Subscription();
    private BillingContact billingContact = new BillingContact();

    @Before
    public void setup() throws ObjectNotFoundException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        ApplicationServiceLocal appService = mock(ApplicationServiceLocal.class);
        accountServiceBean.appManager = appService;
        doNothing().when(accountServiceBean.appManager).activateInstance(
                any(Subscription.class));
        pt.setKey(PT_KEY);
        doReturn(listPT).when(accountServiceBean)
                .getAvailablePaymentTypesIntersection(any(Long.class));
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
    }

    @Test
    public void isPaymentTypeEnabled_enabled() throws ObjectNotFoundException {
        // given
        listPT.add(pt);

        // when
        boolean isEnabled = accountServiceBean.isPaymentTypeEnabled(
                SERVICE_KEY, PT_KEY);

        // then
        assertTrue(isEnabled);
    }

    @Test
    public void isPaymentTypeEnabled_disabled() throws ObjectNotFoundException {
        // given
        listPT.clear();

        // when
        boolean isEnabled = accountServiceBean.isPaymentTypeEnabled(
                SERVICE_KEY, PT_KEY);

        // then
        assertFalse(isEnabled);
    }

    @Test
    public void activateSuspendedSubscriptions_noBillingContact() {
        // given
        subscription.setBillingContact(null);

        // when
        accountServiceBean.revokeSuspendedSubscription(subscription);

        // then
        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());
    }

    @Test
    public void activateSuspendedSubscriptions_withBillingContact() {
        // given
        subscription.setBillingContact(billingContact);

        // when
        accountServiceBean.revokeSuspendedSubscription(subscription);

        // then
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }
}
