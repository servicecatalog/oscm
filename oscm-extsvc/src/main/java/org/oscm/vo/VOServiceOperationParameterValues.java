/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-01-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parameter of a service operation with its possible values as
 * defined for a technical service and application instance.
 * 
 */
public class VOServiceOperationParameterValues extends
        VOServiceOperationParameter {

    private static final long serialVersionUID = 1026061993219503616L;

    private List<String> values = new ArrayList<>();

    /**
     * Retrieves the values that can be specified for the service operation
     * parameter.
     * 
     * @return a list of the parameter values
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Sets the values that can be specified for the service operation
     * parameter.
     * 
     * @param values
     *            a list of the parameter values
     */
    public void setValues(List<String> values) {
        this.values = values;
    }

}
