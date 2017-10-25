/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;

/**
 * Represents a subscription, enhanced by specific details, for example, for
 * billing and payment.
 * 
 */
public class VOSubscriptionDetails extends VOSubscription implements
        Serializable {

    private static final long serialVersionUID = -29305697615476575L;

    private List<VOUsageLicense> usageLicenses = new ArrayList<VOUsageLicense>();
    private VOPriceModel priceModel;
    private VOService subscribedService;
    private VOBillingContact billingContact;
    private VOPaymentInfo paymentInfo;

    /**
     * Retrieves the subscription's underlying marketable service.
     * 
     * @return the service
     */
    public VOService getSubscribedService() {
        return subscribedService;
    }

    /**
     * Sets the marketable service underlying to the subscription.
     * 
     * @param subscribedService
     *            the service
     */
    public void setSubscribedService(VOService subscribedService) {
        this.subscribedService = subscribedService;
    }

    /**
     * Retrieves the usage licenses for the subscription, i.e. the assigned
     * users with their service roles.
     * 
     * @return the list of usage licenses
     */
    public List<VOUsageLicense> getUsageLicenses() {
        return usageLicenses;
    }

    /**
     * Sets the usage licenses for the subscription, i.e. the assigned users
     * with their service roles.
     * 
     * @param licenses
     *            the list of usage licenses
     */
    public void setUsageLicenses(List<VOUsageLicense> licenses) {
        this.usageLicenses = licenses;
    }

    /**
     * Retrieves the applicable price model. This may be the price model defined
     * for the underlying service, a specific price model defined for the
     * customer who subscribed to the service, or a specific price model defined
     * for the subscription.
     * 
     * @return the price model
     */
    public VOPriceModel getPriceModel() {
        return priceModel;
    }

    /**
     * Sets the price model for the subscription.
     * 
     * @param priceModel
     *            the price model
     */
    public void setPriceModel(VOPriceModel priceModel) {
        this.priceModel = priceModel;
    }

    /**
     * Retrieves the billing contact of the customer organization the
     * subscription belongs to.
     * 
     * @return the billing contact
     */
    public VOBillingContact getBillingContact() {
        return billingContact;
    }

    /**
     * Sets the billing contact of the customer organization the subscription
     * belongs to.
     * 
     * @param billingContact
     *            the billing contact
     */
    public void setBillingContact(VOBillingContact billingContact) {
        this.billingContact = billingContact;
    }

    /**
     * Retrieves the payment information applicable to the subscription.
     * 
     * @return the payment information
     */
    public VOPaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    /**
     * Sets the payment information for the subscription.
     * 
     * @param paymentInfo
     *            the payment information
     */
    public void setPaymentInfo(VOPaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

}
