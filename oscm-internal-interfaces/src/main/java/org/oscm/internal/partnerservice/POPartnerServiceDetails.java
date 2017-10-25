/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.partnerservice;

import java.io.Serializable;

import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOPriceModel;

public class POPartnerServiceDetails implements Serializable {

    private static final long serialVersionUID = -6212333485876532308L;

    private long serviceKey;
    private String serviceName;
    private String serviceShortDescription;
    private String serviceDescription;
    private boolean autoAssignUserEnabled;
    private ServiceStatus status;
    private VOPriceModel priceModel;

    public long getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(long serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceShortDescription() {
        return serviceShortDescription;
    }

    public void setServiceShortDescription(String serviceShortDescription) {
        this.serviceShortDescription = serviceShortDescription;
    }

    public VOPriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(VOPriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public boolean isAutoAssignUserEnabled() {
        return autoAssignUserEnabled;
    }

    public void setAutoAssignUserEnabled(boolean autoAssignUserEnabled) {
        this.autoAssignUserEnabled = autoAssignUserEnabled;
    }

}
