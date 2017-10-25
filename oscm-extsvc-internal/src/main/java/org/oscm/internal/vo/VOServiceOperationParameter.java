/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.types.enumtypes.OperationParameterType;

/**
 * Represents an operation parameter defined for a technical service.
 * 
 * @author Yuyin
 */
public class VOServiceOperationParameter extends BaseVO {

    private static final long serialVersionUID = -3753713435649738577L;

    /**
     * The identifier of the parameter.
     */
    private String parameterId;

    /**
     * The name of the parameter.
     */
    private String parameterName;

    /**
     * Specifies if the parameter is mandatory or not.
     */
    private boolean mandatory;

    /**
     * The parameter type
     */
    private OperationParameterType type;

    /**
     * The value of the parameter.
     */
    private String parameterValue;

    /**
     * @return the parameterId
     */
    public String getParameterId() {
        return parameterId;
    }

    /**
     * @param parameterId
     *            the parameterId to set
     */
    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    /**
     * @return the parameterName
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * @param parameterName
     *            the parameterName to set
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * @return the parameterValue
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * @param parameterValue
     *            the parameterValue to set
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public OperationParameterType getType() {
        return type;
    }

    public void setType(OperationParameterType type) {
        this.type = type;
    }
}
