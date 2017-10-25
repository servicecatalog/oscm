/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
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
public class RDOSupplierRevenueShareReports extends RDO {

    private static final long serialVersionUID = -567023589679127945L;

    private List<RDOSupplierRevenueShareReport> reports = new ArrayList<RDOSupplierRevenueShareReport>();
    private String serverTimeZone = "";
    
    public RDOSupplierRevenueShareReports() {
        super();
    }

    public RDOSupplierRevenueShareReports(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public List<RDOSupplierRevenueShareReport> getReports() {
        return reports;
    }

    public void setReports(List<RDOSupplierRevenueShareReport> reports) {
        this.reports = reports;
    }
    
    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }
}
