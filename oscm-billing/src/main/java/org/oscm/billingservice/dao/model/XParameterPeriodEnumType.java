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
 * Represents parameter value settings for enumeration value types.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XParameterPeriodEnumType extends XParameterPeriodValue {

    private final XParameterIdData parent;
    private XParameterOption parameterOption;

    public XParameterPeriodEnumType(XParameterIdData parent) {
        parent.addPeriodValue(this);
        this.parent = parent;
    }

    @Override
    public XParameterOption getParameterOption() {
        return parameterOption;
    }

    @Override
    public String getValue() {
        return getParameterOption().getId();
    }

    public void setParameterOption(XParameterOption parameterOption) {
        this.parameterOption = parameterOption;
    }

    @Override
    public boolean isParameterOption() {
        return true;
    }

    @Override
    public XParameterIdData getParent() {
        return parent;
    }

    @Override
    public RolePricingData getRolePrices() {
        return getParameterOption().getRolePrices();
    }

}
