/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-08-19                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operation.data;

import java.io.Serializable;

/**
 * Represents an operation parameter, which has a name and a value.
 * 
 */
public class OperationParameter implements Serializable {

    private static final long serialVersionUID = 2694990320329028897L;

    private String name;
    private String value;

    /**
     * Retrieves the name of the operation parameter.
     * 
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the operation parameter.
     * 
     * @param name
     *            the parameter name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the value of the operation parameter.
     * 
     * @return the parameter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the operation parameter.
     * 
     * @param value
     *            the parameter value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
