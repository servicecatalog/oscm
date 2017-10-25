/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.partnerrevenue;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * @author kulle
 * 
 */
public class RDORevenueDetailService extends RDO {

    private static final long serialVersionUID = 3485492508627857560L;

    /**
     * BROKER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/@
     * b r o k e r R e v e n u e
     * <p>
     * RESELLER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/@
     * r e s e l l e r R e v e n u e
     */
    private String revenue = "";

    private String service = "";

    /**
     * BROKER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/@brokerRevenue
     * S h a r e P e r c e n t a g e
     * <p>
     */
    private String revenueShare = "";

    /**
     * //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='id']]/Service[@key='key']/ServiceRevenue/@
     * t o t a l A m o u n t
     */
    private String amount = "";

    private String currency = "";

    private String vendor = "";

    private String purchasePrice = "";

    private List<RDORevenueDetailServiceCustomer> servicesPerCustomer = new ArrayList<RDORevenueDetailServiceCustomer>();

    public RDORevenueDetailService() {
        super();
    }

    public RDORevenueDetailService(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public List<RDORevenueDetailServiceCustomer> getServicesPerCustomer() {
        return servicesPerCustomer;
    }

    public void setServicesPerCustomer(
            List<RDORevenueDetailServiceCustomer> servicesPerCustomer) {
        this.servicesPerCustomer = servicesPerCustomer;
    }

}
