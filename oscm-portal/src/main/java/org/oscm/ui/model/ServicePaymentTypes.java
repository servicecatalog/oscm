/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOService;

/**
 * @author weiser
 * 
 */
public class ServicePaymentTypes extends PaymentTypes {

    private final VOService service;

    public ServicePaymentTypes(VOService svc) {
        service = svc;
    }

    public VOService getService() {
        return service;
    }

    public String getServiceId() {
        return service.getServiceId();
    }

    public String getServiceName() {
        return service.getName();
    }

    public String getStatus() {
        return service.getStatus().name();
    }

    protected PaymentTypes newInstance() {
        return new ServicePaymentTypes(getService());
    }

    public String getServiceIdToDisplay() {
        return service.getServiceIdToDisplay();
    }
}
