/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import org.oscm.internal.base.BasePO;

/**
 * @author tokoda
 * 
 */
public class POServiceForPricing extends BasePO {

    private static final long serialVersionUID = 5735420189765146381L;

    private String serviceId;

    private POOrganization vendor;

    public POServiceForPricing(long key, int version) {
        super(key, version);
    }

    public POServiceForPricing() {
        super(0, 0);
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public POOrganization getVendor() {
        return vendor;
    }

    public void setVendor(POOrganization vendor) {
        this.vendor = vendor;
    }

}
