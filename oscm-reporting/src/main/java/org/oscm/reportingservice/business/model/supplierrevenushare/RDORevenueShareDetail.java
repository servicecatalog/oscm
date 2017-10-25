/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.supplierrevenushare;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * @author tang
 * 
 */
public class RDORevenueShareDetail extends RDO {

    private static final long serialVersionUID = 342136860238646244L;

    private String currency = "";
    private String service = "";
    private String marketplace = "";
    private String customer = "";
    private String partner = "";
    private String revenue = "";
    private String revenueMinusShares = "";
    private String partnerRevenue = "";
    private String partnerSharePercentage = "";
    private String marketplaceRevenue = "";
    private String marketplaceSharePercentage = "";
    private String operatorRevenue = "";
    private String operatorRevenuePercentage = "";

    public RDORevenueShareDetail() {
        super();
    }

    public RDORevenueShareDetail(int parentEntryNr, int entryNr) {
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPartnerRevenue() {
        return partnerRevenue;
    }

    public void setPartnerRevenue(String partnerRevenue) {
        this.partnerRevenue = partnerRevenue;
    }

    public String getPartnerSharePercentage() {
        return partnerSharePercentage;
    }

    public void setPartnerSharePercentage(String partnerSharePercentage) {
        this.partnerSharePercentage = partnerSharePercentage;
    }

    public String getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(String marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
    }

    public String getMarketplaceSharePercentage() {
        return marketplaceSharePercentage;
    }

    public void setMarketplaceSharePercentage(String marketplaceSharePercentage) {
        this.marketplaceSharePercentage = marketplaceSharePercentage;
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

    public void setOperatorRevenuePercentage(String operatorSharePercentage) {
        this.operatorRevenuePercentage = operatorSharePercentage;
    }

    public String getRevenueMinusShares() {
        return revenueMinusShares;
    }

    public void setRevenueMinusShares(String revenueMinusShares) {
        this.revenueMinusShares = revenueMinusShares;
    }

}
