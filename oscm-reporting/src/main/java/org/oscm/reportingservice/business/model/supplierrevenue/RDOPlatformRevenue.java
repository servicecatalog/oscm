/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.supplierrevenue;

import java.util.ArrayList;
import java.util.List;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * The class <code>RDOPlatformRevenue</code> stores all data for the platform
 * revenue report. The data is stored as below:
 * 
 * <pre>
 *    supplier name1, supplier id1, currency1, amount1
 *    supplier name1, supplier id1, currency2, amount2
 *    supplier name2, supplier id2, currency1, amount3
 * </pre>
 * 
 * So, the supplier information is highly redundant.But, this structure is
 * required by the report engine.
 * 
 * @author cheld
 * 
 */
public class RDOPlatformRevenue extends RDO {

    private static final long serialVersionUID = -4670795365014729259L;

    /**
     * The point in time, in GMT, when the report was initiated.
     */
    private String creationTime;

    private String from;
    private String to;
    private String name;
    private String address;
    private String country;
    private String noMarketplaces;
    private String noSuppliers;
    private String serverTimeZone = "";

    private List<RDOPlatformRevenueDetails> totalByCurrency = new ArrayList<RDOPlatformRevenueDetails>();
    private List<RDOPlatformRevenueDetails> summaryBySupplier = new ArrayList<RDOPlatformRevenueDetails>();
    private List<RDOPlatformRevenueDetails> summaryByMarketplace = new ArrayList<RDOPlatformRevenueDetails>();
    private List<RDOPlatformRevenueDetails> summaryByCountry = new ArrayList<RDOPlatformRevenueDetails>();
    private List<RDOPlatformRevenueDetails> supplierDetails = new ArrayList<RDOPlatformRevenueDetails>();

    public List<RDOPlatformRevenueDetails> getTotalByCurrency() {
        return totalByCurrency;
    }

    public void setTotalByCurrency(
            List<RDOPlatformRevenueDetails> totalByCurrency) {
        this.totalByCurrency = totalByCurrency;
    }

    public List<RDOPlatformRevenueDetails> getSummaryBySupplier() {
        return summaryBySupplier;
    }

    public void setSummaryBySupplier(
            List<RDOPlatformRevenueDetails> summaryBySupplier) {
        this.summaryBySupplier = summaryBySupplier;
    }

    public List<RDOPlatformRevenueDetails> getSummaryByMarketplace() {
        return summaryByMarketplace;
    }

    public void setSummaryByMarketplace(
            List<RDOPlatformRevenueDetails> summaryByMarketplace) {
        this.summaryByMarketplace = summaryByMarketplace;
    }

    public List<RDOPlatformRevenueDetails> getSupplierDetails() {
        return supplierDetails;
    }

    public void setSupplierDetails(
            List<RDOPlatformRevenueDetails> supplierDetails) {
        this.supplierDetails = supplierDetails;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = CharConverter.convertToSBC(name);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = CharConverter.convertToSBC(address);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNoMarketplaces() {
        return noMarketplaces;
    }

    public void setNoMarketplaces(String noMarketplaces) {
        this.noMarketplaces = noMarketplaces;
    }

    public String getNoSuppliers() {
        return noSuppliers;
    }

    public void setNoSuppliers(String noSuppliers) {
        this.noSuppliers = noSuppliers;
    }
    
    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }

    public List<RDOPlatformRevenueDetails> getSummaryByCountry() {
        return summaryByCountry;
    }

    public void setSummaryByCountry(
            List<RDOPlatformRevenueDetails> summaryByCountry) {
        this.summaryByCountry = summaryByCountry;
    }

}
