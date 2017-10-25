/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                          
 *  Creation Date: 08.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contains the role prices for the options for a set of parameters for one
 * price model.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ParameterOptionRolePricingData {

    private final Map<Long, RolePricingData> parameterOptionCosts = new HashMap<Long, RolePricingData>();

    /**
     * Returns the role pricing data for a particular parameter. The result
     * contains, for all available option keys, a map of priced product roles
     * (value) where the key is the role key.
     * 
     * @param paramKey
     *            The key of the parameter to retrieve the information for.
     * @return The role pricing data. Null if no role prices are defined.
     */
    public RolePricingData getRolePricingDataForPricedParameterKey(Long paramKey) {
        return parameterOptionCosts.get(paramKey);
    }

    /**
     * Sets the role pricing information for all options of a parameter
     * identified by the parameter key.
     * 
     * @param paramKey
     *            The key of the parameter the options are specified for.
     * @param dataToSet
     *            The role pricing data to be set.
     */
    public void addRolePricingDataForPricedParameterKey(Long paramKey,
            RolePricingData dataToSet) {
        parameterOptionCosts.put(paramKey, dataToSet);
    }

    /**
     * Returns the keys of the priced parameters the pricing information is
     * stored for.
     * 
     * @return A set of keys of parameters.
     */
    public Set<Long> getPricedParameterKeys() {
        return parameterOptionCosts.keySet();
    }

}
