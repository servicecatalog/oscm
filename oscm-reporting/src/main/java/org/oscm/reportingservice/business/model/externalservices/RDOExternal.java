/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.externalservices;

import java.util.ArrayList;
import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * RDO for the technical service of type external. Holds no billing information
 * since the external services have neither subscriptions nor price models.
 * Everything is handled outside of BSS. It just hold a list of external
 * suppliers.
 * 
 * @author afschar
 * 
 **/
public class RDOExternal extends RDO {

    private static final long serialVersionUID = 1269462373532220613L;

    private List<RDOExternalSupplier> suppliers = new ArrayList<RDOExternalSupplier>();

    private String serverTimeZone = "";
    
    public List<RDOExternalSupplier> getExternalSuppliers() {
        return suppliers;
    }

    public void setExternalSuppliers(List<RDOExternalSupplier> value) {
        suppliers = value;
    }
    
    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public String getServerTimeZone() {
        return serverTimeZone;
    }
}
