/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 22.10.2010                                                      
 *                                                                              
 *  Completion Time: 22.10.2010                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

/**
 * Represents the billing result XML relevant UDA data.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UdaBillingData {

    /**
     * The UDA value.
     */
    private String value;

    /**
     * The UDA identifier.
     */
    private String identifier;

    public UdaBillingData(String value, String identifier) {
        this.value = value;
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
