/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-10-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import org.junit.Test;

import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;

/**
 * @author zhaohang
 * 
 */
public class SubscriptionServiceBeanValidatePaymentAndBillingTest {

    private final SubscriptionServiceBean subscriptionServiceBean = new SubscriptionServiceBean();

    private final PaymentInfo paymentInfo = new PaymentInfo();
    private final BillingContact billingContact = new BillingContact();

    @Test
    public void validatePaymentInfoAndBillingContact_Success() throws Exception {
        // given
        VOPaymentInfo voPaymentInfo = setPaymentInfoVersion(0);
        VOBillingContact voBillingContact = setBillingContactVersion(0);

        // when
        subscriptionServiceBean.validatePaymentInfoAndBillingContact(
                paymentInfo, billingContact, voPaymentInfo, voBillingContact);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void validatePaymentInfoAndBillingContact_PaymentInfoConcurrentModified()
            throws Exception {
        // given
        VOPaymentInfo voPaymentInfo = setPaymentInfoVersion(-1);
        VOBillingContact voBillingContact = setBillingContactVersion(0);

        // when
        subscriptionServiceBean.validatePaymentInfoAndBillingContact(
                paymentInfo, billingContact, voPaymentInfo, voBillingContact);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void validatePaymentInfoAndBillingContact_BillingContactConcurrentModified()
            throws Exception {
        // given
        VOPaymentInfo voPaymentInfo = setPaymentInfoVersion(0);
        VOBillingContact voBillingContact = setBillingContactVersion(-1);

        // when
        subscriptionServiceBean.validatePaymentInfoAndBillingContact(
                paymentInfo, billingContact, voPaymentInfo, voBillingContact);
    }

    private VOPaymentInfo setPaymentInfoVersion(int version) {
        VOPaymentInfo vOPaymentInfo = new VOPaymentInfo();
        vOPaymentInfo.setVersion(version);
        return vOPaymentInfo;
    }

    private VOBillingContact setBillingContactVersion(int version) {
        VOBillingContact vOBillingContact = new VOBillingContact();
        vOBillingContact.setVersion(version);
        return vOBillingContact;
    }

}
