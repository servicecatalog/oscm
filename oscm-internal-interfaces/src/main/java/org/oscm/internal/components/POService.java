/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components;

import java.io.Serializable;

/**
 * PO containing data needed for a service selector.
 * 
 * @author barzu
 */
public class POService implements Serializable {

    private static final long serialVersionUID = 6779859052633338587L;

    private long key;
    private String serviceId;
    private String vendorOrganizationId;

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String id) {
        this.serviceId = id;
    }

    public String getVendorOrganizationId() {
        return vendorOrganizationId;
    }

    public void setVendorOrganizationId(String vendorId) {
        this.vendorOrganizationId = vendorId;
    }

}
