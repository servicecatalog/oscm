/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.supplierrevenushare;

import java.util.ArrayList;
import java.util.List;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * @author tokoda
 * 
 */
public class RDOSupplierRevenueShareReport extends RDO {

    private static final long serialVersionUID = 7885657844636251852L;

    private String supplier = "";
    private String periodStart = "";
    private String periodEnd = "";
    private String address = "";
    private String country = "";
    private String serverTimeZone = "";

    private List<RDOSupplierRevenueShareCurrency> currencies = new ArrayList<RDOSupplierRevenueShareCurrency>();

    public RDOSupplierRevenueShareReport() {
        super();
    }

    public RDOSupplierRevenueShareReport(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = CharConverter.convertToSBC(supplier);
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

    public List<RDOSupplierRevenueShareCurrency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<RDOSupplierRevenueShareCurrency> currencies) {
        this.currencies = currencies;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }
    
    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }

}
