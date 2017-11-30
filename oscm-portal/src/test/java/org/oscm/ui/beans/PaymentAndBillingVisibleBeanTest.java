/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 27.08.15 14:22
 *
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Created by ChojnackiD on 2015-08-27.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentAndBillingVisibleBeanTest {

    @Mock
    private PaymentInfoBean paymentInfoBean;
    @Mock
    private UserBean userBean;
    @Mock
    private BillingContactBean billingContactBean;

    private PaymentAndBillingVisibleBean ctrl = new PaymentAndBillingVisibleBean();

    @Before
    public void setUp() throws Exception {
        ctrl.setUserBean(userBean);
        ctrl.setBillingContactBean(billingContactBean);
    }

    @Test
    public void isPaymentVisible_Admin() {
        // given
        List<VOPaymentType> paymentInfoTypeList = new ArrayList<>();
        paymentInfoTypeList.add(new VOPaymentType());
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.FALSE);
        when(paymentInfoBean.getEnabledPaymentTypes()).thenReturn(paymentInfoTypeList);

        // when
        boolean result = ctrl.isPaymentVisible(paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertTrue(result);
    }

    @Test
    public void isPaymentVisible_AdminNotVisible() {
        // given
        List<VOPaymentType> paymentInfoTypeList = new ArrayList<>();
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.FALSE);
        when(paymentInfoBean.getEnabledPaymentTypes()).thenReturn(paymentInfoTypeList);

        // when
        boolean result = ctrl.isPaymentVisible(paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertFalse(result);
    }

    @Test
    public void isPaymentVisible_SubscriptionManager() {
        // given
        List<VOPaymentInfo> paymentInfoTypeList = new ArrayList<>();
        paymentInfoTypeList.add(new VOPaymentInfo());
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndAllowedToSubscribe())).thenReturn(Boolean.TRUE);
        when(paymentInfoBean.getPaymentInfosForSubscription()).thenReturn(paymentInfoTypeList);

        // when
        boolean result = ctrl.isPaymentVisible((Collection) paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertTrue(result);
    }

    //
    @Test
    public void isPaymentVisible_SubscriptionManagerNotVisible() {
        // given
        List<VOPaymentInfo> paymentInfoTypeList = new ArrayList<>();
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(paymentInfoBean.getPaymentInfosForSubscription()).thenReturn(paymentInfoTypeList);

        // when
        boolean result = ctrl.isPaymentVisible((Collection) paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertFalse(result);
    }

    @Test
    public void isPaymentVisible_AdminAndSubgMgr() {
        // given
        List<VOPaymentInfo> paymentInfoList = new ArrayList<>();
        paymentInfoList.add(new VOPaymentInfo());
        List<VOPaymentType> paymentInfoTypeList = new ArrayList<>();
        paymentInfoTypeList.add(new VOPaymentType());
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(paymentInfoBean.getPaymentInfosForSubscription()).thenReturn(paymentInfoList);
        when(paymentInfoBean.getEnabledPaymentTypes()).thenReturn(paymentInfoTypeList);

        // when
        boolean result = ctrl.isPaymentVisible(paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertTrue(result);
    }

    @Test
    public void isPaymentVisible_AdminAndSubgMgrNotVisible() {
        // given
        List<VOPaymentInfo> paymentInfoTypeList = new ArrayList<>();
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(paymentInfoBean.getPaymentInfosForSubscription()).thenReturn(paymentInfoTypeList);

        // when
        boolean result = ctrl.isPaymentVisible((Collection) paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertFalse(result);
    }

    @Test
    public void isPaymentVisible_NotVisible() {
        // given
        List<VOPaymentType> paymentInfoTypeList = new ArrayList<>();
        paymentInfoTypeList.add(new VOPaymentType());
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.FALSE);

        // when
        boolean result = ctrl.isPaymentVisible(paymentInfoTypeList, paymentInfoBean.getPaymentInfosForSubscription());

        // then
        assertFalse(result);
    }

    @Test
    public void isBillingContactVisible_Admin() {
        // given
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.TRUE);

        // when
        boolean result = ctrl.isBillingContactVisible();

        // then
        assertTrue(result);
    }

    @Test
    public void isBillingContactVisible_SubscriptionManager() {
        // given
        List<VOBillingContact> billingContactList = new ArrayList<>();
        billingContactList.add(new VOBillingContact());
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(billingContactBean.getBillingContacts()).thenReturn(billingContactList);

        // when
        boolean result = ctrl.isBillingContactVisible();

        // then
        assertTrue(result);
    }

    @Test
    public void isBillingContactVisible_SubscriptionManagerNotVisible() {
        // given
        List<VOBillingContact> billingContactList = new ArrayList<>();
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(billingContactBean.getBillingContacts()).thenReturn(billingContactList);

        // when
        boolean result = ctrl.isBillingContactVisible();

        // then
        assertFalse(result);
    }

    @Test
    public void isBillingContactVisible_AdminAndSubMgr() {
        // given
        List<VOBillingContact> billingContactList = new ArrayList<>();
        billingContactList.add(new VOBillingContact());
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(billingContactBean.getBillingContacts()).thenReturn(billingContactList);

        // when
        boolean result = ctrl.isBillingContactVisible();

        // then
        assertTrue(result);
    }

    @Test
    public void isBillingContactVisible_AdminAndSubMgrVisible() {
        // given
        List<VOBillingContact> billingContactList = new ArrayList<>();
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.TRUE);
        when(billingContactBean.getBillingContacts()).thenReturn(billingContactList);

        // when
        boolean result = ctrl.isBillingContactVisible();

        // then
        assertTrue(result);
    }

    @Test
    public void isBillingContactVisible_NotVisible() {
        // given
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager())).thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(Boolean.FALSE);

        // when
        boolean result = ctrl.isBillingContactVisible();

        // then
        assertFalse(result);
    }

}
