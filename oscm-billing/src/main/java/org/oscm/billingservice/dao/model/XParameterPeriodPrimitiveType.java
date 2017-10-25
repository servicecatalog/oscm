/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 07.09.2011                                                      
 *                                                                              
 *  Completion Time:  07.09.2011                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.util.List;

/**
 * Represents parameter value settings for primitive value types.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XParameterPeriodPrimitiveType extends XParameterPeriodValue {

    private final XParameterIdData parent;
    private final RolePricingData rolePrices;
    private String value;

    public XParameterPeriodPrimitiveType(XParameterIdData idData,
            RolePricingData rolePrices, List<SteppedPriceData> steppedPrices) {

        this.rolePrices = rolePrices;
        if (rolePrices != null) {
            this.rolePrices.setParent(this);
        }

        idData.addPeriodValue(this);
        this.parent = idData;

        SteppedPriceDetail steppedPriceDetail = new SteppedPriceDetail();
        steppedPriceDetail.setParent(this);
        steppedPriceDetail.setPriceData(steppedPrices);
        this.setSteppedPrices(steppedPriceDetail);
    }

    @Override
    public XParameterOption getParameterOption() {
        return null;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean isParameterOption() {
        return false;
    }

    @Override
    public XParameterIdData getParent() {
        return parent;
    }

    @Override
    public RolePricingData getRolePrices() {
        return rolePrices;
    }

}
