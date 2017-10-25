/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 17, 2012                                                      
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
public class RDOPartnerReports extends RDO {

    private static final long serialVersionUID = -3842077045998592362L;

    private String serverTimeZone = "";
    private List<RDOPartnerReport> reports = new ArrayList<RDOPartnerReport>();

    public RDOPartnerReports() {
        super();
    }

    public RDOPartnerReports(int parentEntryNr, int entryNr) {
        super(parentEntryNr, entryNr);
    }

    public List<RDOPartnerReport> getReports() {
        return reports;
    }

    public void setReports(List<RDOPartnerReport> reports) {
        this.reports = reports;
    }
    
    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }

}
