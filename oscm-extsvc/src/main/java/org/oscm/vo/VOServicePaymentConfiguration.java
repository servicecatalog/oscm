/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-04-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a payment configuration for a service.
 */
public class VOServicePaymentConfiguration implements Serializable {

    private static final long serialVersionUID = -7516498164611849134L;

    private VOService service;
    private Set<VOPaymentType> enabledPaymentTypes = new HashSet<VOPaymentType>();

    /**
     * Sets the payment types that are to be available for the service.
     * 
     * @param enabledPaymentTypes
     *            the payment types
     */
    public void setEnabledPaymentTypes(Set<VOPaymentType> enabledPaymentTypes) {
        this.enabledPaymentTypes = enabledPaymentTypes;
    }

    /**
     * Retrieves the payment types that can be used for the service.
     * 
     * @return the payment types
     */
    public Set<VOPaymentType> getEnabledPaymentTypes() {
        return enabledPaymentTypes;
    }

    /**
     * Retrieves the service for which the payment configuration is defined.
     * 
     * @return the service
     */
    public VOService getService() {
        return service;
    }

    /**
     * Sets the service for which the payment configuration is to be defined.
     * 
     * @param service
     *            the service
     */
    public void setService(VOService service) {
        this.service = service;
    }

}
