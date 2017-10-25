/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-12-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.partnerrevenue;

import org.oscm.reportingservice.business.model.RDO;

/**
 * @author Administrator
 * 
 */
public class RDORevenueDetailServiceCustomer extends RDO {

    private static final long serialVersionUID = -4358217440993428973L;

    private String customer = "";

    /**
     * BROKER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/ServiceCustomerRevenue[@key='key']/@
     * b r o k e r R e v e n u e
     */
    private String revenue = "";

    /**
     * BROKER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/ServiceCustomerRevenue/@
     * b r o k e r R e v e n u e S h a r e P e r c e n t a g e
     * <p>
     */
    private String revenueShare = "";

    /**
     * //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/ServiceCustomerRevenue/@
     * t o t a l A m o u n t
     */
    private String amount = "";

    /**
     * //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/ServiceCustomerRevenue/@purc
     * h a s e P r i c e
     */
    private String purchasePrice = "";

    private String currency = "";

    private String vendor = "";

    private String service = "";

    public RDORevenueDetailServiceCustomer() {

    }

    public RDORevenueDetailServiceCustomer(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(String revenueShare) {
        this.revenueShare = revenueShare;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

}
