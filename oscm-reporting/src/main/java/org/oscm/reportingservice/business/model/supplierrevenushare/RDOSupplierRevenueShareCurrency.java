/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 11, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.supplierrevenushare;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * @author tokoda
 * 
 */
public class RDOSupplierRevenueShareCurrency extends RDO {

    private static final long serialVersionUID = -4910764424233184141L;

    private String currency = "";
    private String totalRevenue = "";
    private String totalRemainingRevenue = "";

    private String directTotalRevenue = "";
    private String directTotalRemainingRevenue = "";
    private String directProvisionToOperator = "";
    private String directProvisionToMarketplaceOwner = "";
    private List<RDORevenueShareSummary> directRevenueSummaries = new ArrayList<RDORevenueShareSummary>();
    private List<RDORevenueShareDetail> directRevenueShareDetails = new ArrayList<RDORevenueShareDetail>();

    private String brokerTotalRevenue = "";
    private String brokerProvisionToMarketplaceOwner = "";
    private String brokerProvisionToOperator = "";
    private String brokerTotalRemainingRevenue = "";
    private String brokerPercentageRevenue = "";
    private String brokerTotalShareAmount = "";
    private List<RDORevenueShareSummary> brokerRevenueSummaries = new ArrayList<RDORevenueShareSummary>();
    private List<RDORevenueShareDetail> brokerRevenueShareDetails = new ArrayList<RDORevenueShareDetail>();

    private String resellerTotalRevenue = "";
    private String resellerProvisionToMarketplaceOwner = "";
    private String resellerProvisionToOperator = "";
    private String resellerTotalRemainingRevenue = "";
    private String resellerPercentageRevenue = "";
    private String resellerTotalShareAmount = "";
    private List<RDORevenueShareSummary> resellerRevenueSummaries = new ArrayList<RDORevenueShareSummary>();
    private List<RDORevenueShareDetail> resellerRevenueShareDetails = new ArrayList<RDORevenueShareDetail>();

    public RDOSupplierRevenueShareCurrency() {
        super();
    }

    public RDOSupplierRevenueShareCurrency(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(String totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getBrokerTotalRevenue() {
        return brokerTotalRevenue;
    }

    public void setBrokerTotalRevenue(String brokerTotalRevenue) {
        this.brokerTotalRevenue = brokerTotalRevenue;
    }

    public String getBrokerPercentageRevenue() {
        return brokerPercentageRevenue;
    }

    public void setBrokerPercentageRevenue(String brokerPercentageRevenue) {
        this.brokerPercentageRevenue = brokerPercentageRevenue;
    }

    public String getResellerTotalRevenue() {
        return resellerTotalRevenue;
    }

    public void setResellerTotalRevenue(String resellerTotalRevenue) {
        this.resellerTotalRevenue = resellerTotalRevenue;
    }

    public String getResellerPercentageRevenue() {
        return resellerPercentageRevenue;
    }

    public void setResellerPercentageRevenue(String resellerPercentageRevenue) {
        this.resellerPercentageRevenue = resellerPercentageRevenue;
    }

    public String getDirectProvisionToMarketplaceOwner() {
        return directProvisionToMarketplaceOwner;
    }

    public void setDirectProvisionToMarketplaceOwner(
            String directProvisionToMarketplaceOwner) {
        this.directProvisionToMarketplaceOwner = directProvisionToMarketplaceOwner;
    }

    public String getBrokerProvisionToMarketplaceOwner() {
        return brokerProvisionToMarketplaceOwner;
    }

    public void setBrokerProvisionToMarketplaceOwner(
            String brokerProvisionToMarketplaceOwner) {
        this.brokerProvisionToMarketplaceOwner = brokerProvisionToMarketplaceOwner;
    }

    public String getResellerProvisionToMarketplaceOwner() {
        return resellerProvisionToMarketplaceOwner;
    }

    public void setResellerProvisionToMarketplaceOwner(
            String resellerProvisionToMarketplaceOwner) {
        this.resellerProvisionToMarketplaceOwner = resellerProvisionToMarketplaceOwner;
    }

    public List<RDORevenueShareSummary> getDirectRevenueSummaries() {
        return directRevenueSummaries;
    }

    public void setDirectRevenueSummaries(
            List<RDORevenueShareSummary> directRevenueSummaries) {
        this.directRevenueSummaries = directRevenueSummaries;
    }

    public List<RDORevenueShareSummary> getResellerRevenueSummaries() {
        return resellerRevenueSummaries;
    }

    public void setResellerRevenueSummaries(
            List<RDORevenueShareSummary> resellerRevenueSummaries) {
        this.resellerRevenueSummaries = resellerRevenueSummaries;
    }

    public List<RDORevenueShareSummary> getBrokerRevenueSummaries() {
        return brokerRevenueSummaries;
    }

    public void setBrokerRevenueSummaries(
            List<RDORevenueShareSummary> brokerRevenueSummaries) {
        this.brokerRevenueSummaries = brokerRevenueSummaries;
    }

    public void setDirectTotalRevenue(String directTotalRevenue) {
        this.directTotalRevenue = directTotalRevenue;
    }

    public String getDirectTotalRevenue() {
        return directTotalRevenue;
    }

    public List<RDORevenueShareDetail> getDirectRevenueShareDetails() {
        return directRevenueShareDetails;
    }

    public void setDirectRevenueShareDetails(
            List<RDORevenueShareDetail> directRevenueShareDetails) {
        this.directRevenueShareDetails = directRevenueShareDetails;
    }

    public List<RDORevenueShareDetail> getResellerRevenueShareDetails() {
        return resellerRevenueShareDetails;
    }

    public void setResellerRevenueShareDetails(
            List<RDORevenueShareDetail> resellerRevenueShareDetails) {
        this.resellerRevenueShareDetails = resellerRevenueShareDetails;
    }

    public List<RDORevenueShareDetail> getBrokerRevenueShareDetails() {
        return brokerRevenueShareDetails;
    }

    public void setBrokerRevenueShareDetails(
            List<RDORevenueShareDetail> brokerRevenueShareDetails) {
        this.brokerRevenueShareDetails = brokerRevenueShareDetails;
    }

    public String getTotalRemainingRevenue() {
        return totalRemainingRevenue;
    }

    public void setTotalRemainingRevenue(String totalRemainigRevenue) {
        this.totalRemainingRevenue = totalRemainigRevenue;
    }

    public String getDirectTotalRemainingRevenue() {
        return directTotalRemainingRevenue;
    }

    public void setDirectTotalRemainingRevenue(
            String directTotalRemainingRevenue) {
        this.directTotalRemainingRevenue = directTotalRemainingRevenue;
    }

    public String getBrokerTotalRemainingRevenue() {
        return brokerTotalRemainingRevenue;
    }

    public void setBrokerTotalRemainingRevenue(
            String brokerTotalRemainingRevenue) {
        this.brokerTotalRemainingRevenue = brokerTotalRemainingRevenue;
    }

    public String getResellerTotalRemainingRevenue() {
        return resellerTotalRemainingRevenue;
    }

    public void setResellerTotalRemainingRevenue(
            String resellerTotalRemainingRevenue) {
        this.resellerTotalRemainingRevenue = resellerTotalRemainingRevenue;
    }

    public String getDirectProvisionToOperator() {
        return directProvisionToOperator;
    }

    public void setDirectProvisionToOperator(String directProvisionToOperator) {
        this.directProvisionToOperator = directProvisionToOperator;
    }

    public String getBrokerProvisionToOperator() {
        return brokerProvisionToOperator;
    }

    public void setBrokerProvisionToOperator(String brokerProvisionToOperator) {
        this.brokerProvisionToOperator = brokerProvisionToOperator;
    }

    public String getResellerProvisionToOperator() {
        return resellerProvisionToOperator;
    }

    public void setResellerProvisionToOperator(
            String resellerProvisionToOperator) {
        this.resellerProvisionToOperator = resellerProvisionToOperator;
    }

    public String getBrokerTotalShareAmount() {
        return brokerTotalShareAmount;
    }

    public void setBrokerTotalShareAmount(String bokerTotalShareAmount) {
        this.brokerTotalShareAmount = bokerTotalShareAmount;
    }

    public String getResellerTotalShareAmount() {
        return resellerTotalShareAmount;
    }

    public void setResellerTotalShareAmount(String resellerTotalShareAmount) {
        this.resellerTotalShareAmount = resellerTotalShareAmount;
    }

}
