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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * Represents the cost related data for parameters.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class XParameterData {

    private BigDecimal costs = BigDecimal.ZERO;
    private PricingPeriod period;

    // contains never null objects
    private final Map<String, XParameterIdData> idData = new HashMap<String, XParameterIdData>();

    public BigDecimal getCosts() {
        return costs;
    }

    void addCosts(BigDecimal subCosts) {
        costs = costs.add(subCosts);
    }

    public PricingPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PricingPeriod period) {
        this.period = period;
    }

    public Collection<XParameterIdData> getIdData() {
        return idData.values();
    }

    /**
     * Retrieves the parameter id record currently managed for the given
     * identifier. If none is found, a new one is created and added to the
     * structure.
     * 
     * @param parameterId
     *            The identifier of the current parameter.
     * @param parameterValueType
     *            The value type of the parameter.
     * @param parameterType
     *            The type of the parameter.
     * @param steppedPrices
     *            The stepped prices for the parameter.
     * @return The parameter id managed for the current costs, never null.
     */
    public XParameterIdData getIdDataInstance(String parameterId,
            ParameterType parameterType, ParameterValueType parameterValueType) {

        if (idData.containsKey(parameterId)) {
            return idData.get(parameterId);
        }

        XParameterIdData result = new XParameterIdData(parameterId,
                parameterType, parameterValueType, this);
        idData.put(parameterId, result);
        return result;
    }
}
