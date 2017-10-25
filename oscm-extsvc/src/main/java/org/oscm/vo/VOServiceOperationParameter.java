/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-01-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import org.oscm.types.enumtypes.OperationParameterType;

/**
 * Represents a parameter of an operation defined for a technical service.
 * 
 */
public class VOServiceOperationParameter extends BaseVO {

    private static final long serialVersionUID = -4857777143451365879L;

    /**
     * The identifier of the parameter.
     */
    private String parameterId;

    /**
     * The name of the parameter.
     */
    private String parameterName;

    /**
     * Indicates if the parameter is mandatory.
     */
    private boolean mandatory;

    /**
     * The type of the parameter values.
     */
    private OperationParameterType type;

    /**
     * The value of the parameter.
     */
    private String parameterValue;

    /**
     * Retrieves the identifier of the parameter.
     * 
     * @return the parameter ID
     */
    public String getParameterId() {
        return parameterId;
    }

    /**
     * Sets the identifier of the parameter.
     * 
     * @param parameterId
     *            the parameter ID
     */
    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    /**
     * Retrieves the name of the parameter.
     * 
     * @return the parameter name
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Sets the name of the parameter.
     * 
     * @param parameterName
     *            the parameter name
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Retrieves the current value set for the parameter.
     * 
     * @return the parameter value
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * Sets the value of the parameter.
     * 
     * @param parameterValue
     *            the parameter value
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    /**
     * Returns whether the parameter must be set for the service operation.
     * 
     * @return <code>true</code> if the parameter is mandatory,
     *         <code>false</code> otherwise
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Specifies whether the parameter must be set for the service operation.
     * 
     * @param mandatory
     *            <code>true</code> if the parameter is mandatory,
     *            <code>false</code> otherwise
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Retrieves the type of the parameter values.
     * 
     * @return the type
     */
    public OperationParameterType getType() {
        return type;
    }

    /**
     * Sets the type of the parameter values.
     * 
     * @param type
     *            the type
     */
    public void setType(OperationParameterType type) {
        this.type = type;
    }

}
