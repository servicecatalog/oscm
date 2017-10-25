/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 07.09.2011                                                      
 *                                                                              
 *  Completion Time: 07.09.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

/**
 * Class to represent the option related details for a parameter, used to
 * provide billing relevant information.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XParameterOption {

    private final XParameterPeriodEnumType parent;
    private String id;
    private RolePricingData rolePrices;

    public XParameterOption(XParameterPeriodEnumType parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public RolePricingData getRolePrices() {
        return rolePrices;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRolePrices(RolePricingData rolePrices) {
        this.rolePrices = rolePrices;
        if (rolePrices != null) {
            parent.addTotalCostsForRoles(rolePrices.getCosts());
            this.rolePrices.setParent(parent);
        }

    }

}
