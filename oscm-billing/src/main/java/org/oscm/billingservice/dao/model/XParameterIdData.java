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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * Contains all parameter related information (value changes) for one parameter.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XParameterIdData {

    private final String id;
    private final XParameterData parent;
    private final ParameterType type;
    private final ParameterValueType valueType;

    // contains never null values
    private final List<XParameterPeriodValue> periodValues = new ArrayList<XParameterPeriodValue>();

    private BigDecimal totalCosts = BigDecimal.ZERO;
    private BigDecimal totalCostsForUser = BigDecimal.ZERO;
    private BigDecimal totalCostsForSubscription = BigDecimal.ZERO;

    public XParameterIdData(String parameterId, ParameterType parameterType,
            ParameterValueType parameterValueType, XParameterData parent) {
        this.id = parameterId;
        this.type = parameterType;
        this.valueType = parameterValueType;
        this.parent = parent;
    }

    public ParameterType getType() {
        return type;
    }

    public ParameterValueType getValueType() {
        return valueType;
    }

    public String getId() {
        return id;
    }

    public List<XParameterPeriodValue> getPeriodValues() {
        return periodValues;
    }

    void addPeriodValue(XParameterPeriodValue periodValue) {
        this.periodValues.add(periodValue);
    }

    void addTotalCosts(BigDecimal totalCosts) {
        getParent().addCosts(totalCosts);
        this.totalCosts = this.totalCosts.add(totalCosts);
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public void addTotalCostsForUser(BigDecimal totalCostsForUser) {
        this.totalCostsForUser = this.totalCostsForUser.add(totalCostsForUser);
    }

    public BigDecimal getTotalCostsForUser() {
        return totalCostsForUser;
    }

    public void addTotalCostsForSubscription(
            BigDecimal totalCostsForSubscription) {
        this.totalCostsForUser = this.totalCostsForSubscription
                .add(totalCostsForSubscription);
    }

    public BigDecimal getTotalCostsForSubscription() {
        return totalCostsForSubscription;
    }

    XParameterData getParent() {
        return parent;
    }

}
