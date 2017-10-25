/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.supplierrevenue;

import java.util.Comparator;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

public class RDOPlatformRevenueDetails extends RDO {

    private static final long serialVersionUID = -5980864155615324159L;

    private String name;
    private String country;
    private String billingKey;
    private String marketplace;
    private String currency;
    private String amount;

    /**
     * Revenues are ordered alphabetically by default.
     */
    public static Comparator<RDOPlatformRevenueDetails> DEFAULT_ORDER = new Comparator<RDOPlatformRevenueDetails>() {

        public int compare(RDOPlatformRevenueDetails o1,
                RDOPlatformRevenueDetails o2) {
            return o1.currency.compareTo(o2.currency);
        }

    };

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = CharConverter.convertToSBC(name);
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = CharConverter.convertToSBC(marketplace);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBillingKey() {
        return billingKey;
    }

    public void setBillingKey(String billingKey) {
        this.billingKey = billingKey;
    }

}
