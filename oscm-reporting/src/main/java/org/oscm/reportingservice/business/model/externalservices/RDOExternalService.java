/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.externalservices;

import org.oscm.reportingservice.business.model.RDO;

/**
 * RDO for the services of a technical service of type external. Holds no
 * billing information since the external services have neither subscriptions
 * nor price models. Everything is handled outside of BSS. This object just
 * holds the activation time and the name of the service itself.
 * 
 * @author afschar
 **/
public class RDOExternalService extends RDO {

    private static final long serialVersionUID = 1269462373532220613L;

    private String startDate;
    private String endDate;
    private String serviceName;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
