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
public class RDORevenueDetail extends RDO {

    private static final long serialVersionUID = -1763886619989883055L;

    private String vendor = "";

    /**
     * BROKER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='Id']]/BrokerRevenuePerSupplier/@amo
     * u n t
     * <p>
     * RESELLER: //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='Id']]/ResellerRevenuePerSupplier/@a
     * m o u n t
     */
    private String revenue = "";

    /**
     * //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='x']]/BrokerRevenuePerSupplier/@tota
     * l A m o u n t
     */
    private String amount = "";

    /**
     * //Currency[@id=
     * 'EUR']/Supplier[OrganizationData[@id='x']]/BrokerRevenuePerSupplier/@purchase
     * P r i c e
     */
    private String purchasePrice = "";

    private String currency = "";

    private List<RDORevenueDetailService> services = new ArrayList<RDORevenueDetailService>();

    public RDORevenueDetail() {
        super();
    }

    public RDORevenueDetail(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<RDORevenueDetailService> getServices() {
        return services;
    }

    public void setServices(List<RDORevenueDetailService> services) {
        this.services = services;
    }

}
