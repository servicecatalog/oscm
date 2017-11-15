/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.12.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author stavreva
 * 
 */
public class SubscriptionServiceBeanSubscriptionActivation {

    private SubscriptionServiceBean subBean = spy(new SubscriptionServiceBean());
    private AccountServiceLocal accService = mock(AccountServiceLocal.class);
    private Subscription subscription = new Subscription();
    private BillingContact billingContact = new BillingContact();
    private PaymentType pType = new PaymentType();
    private PaymentInfo pInfo = new PaymentInfo();
    private boolean isActivationAllowed = false;
    private static long SERVICE_KEY = 1L;

    @Before
    public void setup() {
        Product p = new Product();
        p.setKey(SERVICE_KEY);
        PriceModel pModel = new PriceModel();
        pModel.setType(PriceModelType.PRO_RATA);
        p.setPriceModel(pModel);
        subscription.setProduct(p);
        pInfo.setPaymentType(pType);
        subBean.accountService = accService;
    }

    @Test
    public void isActivationAllowed_activateNullSubscription()
            throws ObjectNotFoundException {
        assertFalse(subBean.isActivationAllowed(null, true));
    }

    @Test
    public void isActivationAllowed_NullSubscription()
            throws ObjectNotFoundException {
        assertFalse(subBean.isActivationAllowed(null, false));
    }

    @Test
    public void isActivationAllowed_notActivateExpiredSubscription()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setBillingContact(billingContact);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, false);

        // then
        assertFalse(isActivationAllowed);
    }

    @Test
    public void isActivationAllowed_activateExpiredSubscription()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setBillingContact(billingContact);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertTrue(isActivationAllowed);
    }

    @Test
    public void isActivationAllowed_activateNotExpiredSubscription()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setBillingContact(billingContact);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertFalse(isActivationAllowed);
    }

    @SuppressWarnings("boxing")
    @Test
    public void isActivationAllowed_SuspendedSubscriptionWithBillingContact1()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(billingContact);
        subscription.setPaymentInfo(pInfo);
        doReturn(true).when(accService).isPaymentTypeEnabled(SERVICE_KEY, 0);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, false);

        // then
        assertTrue(isActivationAllowed);
    }

    @SuppressWarnings("boxing")
    @Test
    public void isActivationAllowed_SuspendedSubscriptionWithBillingContact2()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(billingContact);
        subscription.setPaymentInfo(pInfo);
        doReturn(true).when(accService).isPaymentTypeEnabled(SERVICE_KEY, 0);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertTrue(isActivationAllowed);
    }

    @SuppressWarnings("boxing")
    @Test
    public void isActivationAllowed_SuspendedSubscriptionNullBillingContact1()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(null);
        subscription.setPaymentInfo(pInfo);
        doReturn(true).when(accService).isPaymentTypeEnabled(SERVICE_KEY, 0);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, false);

        // then
        assertFalse(isActivationAllowed);
    }

    @SuppressWarnings("boxing")
    @Test
    public void isActivationAllowed_SuspendedSubscriptionNullBillingContact2()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(null);
        subscription.setPaymentInfo(pInfo);
        doReturn(true).when(accService).isPaymentTypeEnabled(SERVICE_KEY, 0);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertFalse(isActivationAllowed);
    }

    @SuppressWarnings("boxing")
    @Test
    public void isActivationAllowed_SuspendedSubscriptionNoAvailablePayment()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(billingContact);
        subscription.setPaymentInfo(pInfo);
        doReturn(false).when(accService).isPaymentTypeEnabled(SERVICE_KEY, 0);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertFalse(isActivationAllowed);
    }

    @SuppressWarnings("boxing")
    @Test
    public void isActivationAllowed_SuspendedSubscriptionNoPaymentEnabled()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(null);
        subscription.setPaymentInfo(pInfo);
        doReturn(false).when(accService).isPaymentTypeEnabled(SERVICE_KEY, 0);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertFalse(isActivationAllowed);
    }

    @Test
    public void isActivationAllowed_SuspendedSubscriptionNoPaymentInfo()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(billingContact);
        subscription.setPaymentInfo(null);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertFalse(isActivationAllowed);
    }

    @Test
    public void isActivationAllowed_SuspendedSubscriptionNoPaymentInfoFreeService()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(billingContact);
        subscription.getProduct().getPriceModel()
                .setType(PriceModelType.FREE_OF_CHARGE);
        subscription.setPaymentInfo(null);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertTrue(isActivationAllowed);
    }

    @Test
    public void isActivationAllowed_SuspendedSubscriptionNoBillingContactFreeService()
            throws ObjectNotFoundException {
        // given
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setBillingContact(null);
        subscription.getProduct().getPriceModel()
                .setType(PriceModelType.FREE_OF_CHARGE);
        subscription.setPaymentInfo(null);

        // when
        isActivationAllowed = subBean.isActivationAllowed(subscription, true);

        // then
        assertTrue(isActivationAllowed);
    }
}
