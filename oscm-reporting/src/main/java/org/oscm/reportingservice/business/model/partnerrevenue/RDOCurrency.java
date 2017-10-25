/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
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
public class RDOCurrency extends RDO {

    private static final long serialVersionUID = -7563705030507873038L;

    /**
     * sum(//Currency[@id='EUR']/BrokerRevenue/@totalAmount)
     */
    private String totalAmount = "";

    /**
     * BROKER: //Currency[@id='EUR']/BrokerRevenue/@amount
     * <p>
     * RESELLER: //Currency[@id='EUR']/ResellerRevenue/@amount
     */
    private String totalRevenue = "";

    /**
     * //Currency[@id='EUR']/BrokerRevenue/@amount
     */
    private String brokerRevenue = "";

    /**
     * totalAmount-totalRevenue
     * //Currency[@id='EDE']/ResellerRevenue/@purchasePrice
     */
    private String remainingAmount = "";

    private String currency = "";

    private List<RDORevenueDetail> revenueDetails = new ArrayList<RDORevenueDetail>();

    public RDOCurrency() {
        super();
    }

    public RDOCurrency(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(String totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<RDORevenueDetail> getRevenueDetails() {
        return revenueDetails;
    }

    public void setRevenueDetails(List<RDORevenueDetail> revenueDetails) {
        this.revenueDetails = revenueDetails;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBrokerRevenue() {
        return brokerRevenue;
    }

    public void setBrokerRevenue(String brokerRevenue) {
        this.brokerRevenue = brokerRevenue;
    }

    public String getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(String remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
