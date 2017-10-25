/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.supplierrevenushare;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * @author tang
 * 
 */
public class RDORevenueShareSummary extends RDO {

    private static final long serialVersionUID = 4239119138539685908L;

    private String currency = "";
    private String service = "";
    private String partner = "";
    private String marketplace = "";
    private String revenue = "";
    private String revenueMinusShares = "";
    private String marketplaceRevenuePercentage = "";
    private String marketplaceRevenue = "";
    private String partnerProvision = "";
    private String partnerProvisionPercentage = "";
    private String operatorRevenue = "";
    private String operatorRevenuePercentage = "";

    public RDORevenueShareSummary() {
        super();
    }

    public RDORevenueShareSummary(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(String marketplace) {
        this.marketplace = CharConverter.convertToSBC(marketplace);
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMarketplaceRevenuePercentage() {
        return marketplaceRevenuePercentage;
    }

    public void setMarketplaceRevenuePercentage(
            String marketplaceRevenuePercentage) {
        this.marketplaceRevenuePercentage = marketplaceRevenuePercentage;
    }

    public String getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(String marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
    }

    public void setPartnerProvision(String partnerProvision) {
        this.partnerProvision = partnerProvision;
    }

    public String getPartnerProvision() {
        return partnerProvision;
    }

    public void setPartnerProvisionPercentage(String partnerProvisionPercentage) {
        this.partnerProvisionPercentage = partnerProvisionPercentage;
    }

    public String getPartnerProvisionPercentage() {
        return partnerProvisionPercentage;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getPartner() {
        return partner;
    }

    public String getOperatorRevenue() {
        return operatorRevenue;
    }

    public void setOperatorRevenue(String operatorRevenue) {
        this.operatorRevenue = operatorRevenue;
    }

    public String getOperatorRevenuePercentage() {
        return operatorRevenuePercentage;
    }

    public void setOperatorRevenuePercentage(
            String operatorRevenueSharePercentage) {
        this.operatorRevenuePercentage = operatorRevenueSharePercentage;
    }

    public String getRevenueMinusShares() {
        return revenueMinusShares;
    }

    public void setRevenueMinusShares(String revenueMinusShares) {
        this.revenueMinusShares = revenueMinusShares;
    }

}
