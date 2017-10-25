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

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * @author kulle
 * 
 */
public class RDOPartnerReport extends RDO {

    private static final long serialVersionUID = 6557948005779255482L;

    private String periodStart = "";
    private String periodEnd = "";
    private String address = "";
    private String vendor = "";
    private String countryName = "";
    private String vendorType = "";
    private String serverTimeZone = "";
    private List<RDOCurrency> currencies = new ArrayList<RDOCurrency>();

    public RDOPartnerReport() {
        super();
    }

    public RDOPartnerReport(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = CharConverter.convertToSBC(address);
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = CharConverter.convertToSBC(vendor);
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<RDOCurrency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<RDOCurrency> currencies) {
        this.currencies = currencies;
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = vendorType;
    }
    
    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }

}
