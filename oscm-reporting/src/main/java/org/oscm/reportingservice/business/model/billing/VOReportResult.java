/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result data after report generation.
 * 
 */
public class VOReportResult implements Serializable {

    private static final long serialVersionUID = -2315326849327815032L;

    private List<Object> data = new ArrayList<Object>();
    private String serverTimeZone = "";

    public String getServerTimeZone() {
        return serverTimeZone;
    }

    public void setServerTimeZone(String serverTimeZone) {
        this.serverTimeZone = serverTimeZone;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

}
