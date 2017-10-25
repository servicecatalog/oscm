/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-09                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOParameterDefinition;

/**
 * Represents a parameter setting for a marketable service, which is based on a
 * parameter definition for a technical service.
 * 
 */
public class VOParameter extends BaseVO {

    private static final long serialVersionUID = -7281298095085484683L;

    /**
     * Default constructor.
     */
    public VOParameter() {

    }

    /**
     * Constructs a parameter setting based on the given parameter definition of
     * a technical service.
     * 
     * @param paramDef
     *            the parameter definition of the technical service
     */
    public VOParameter(VOParameterDefinition paramDef) {
        super();
        if (paramDef == null) {
            throw new IllegalArgumentException(
                    "VOParameterDefinition must not be null");
        }
        parameterDefinition = paramDef;
    }

    /**
     * The definition of the parameter, including the data type and ID.
     */
    private VOParameterDefinition parameterDefinition;

    /**
     * The current value of the parameter.
     */
    private String value;

    private boolean configurable;

    /**
     * Checks whether the parameter can be configured by a customer.
     * 
     * @return <code>true</code> if the parameter is configurable,
     *         <code>false</code> otherwise
     */
    public boolean isConfigurable() {
        return configurable;
    }

    /**
     * Specifies whether the parameter can be configured by a customer.
     * 
     * @param configurable
     *            <code>true</code> if the parameter is to be configurable,
     *            <code>false</code> otherwise
     */
    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    /**
     * Retrieves the definition of the parameter, including the data type and
     * ID.
     * 
     * @return the parameter definition
     */
    public VOParameterDefinition getParameterDefinition() {
        return parameterDefinition;
    }

    /**
     * Sets the definition of the parameter, including the data type and ID.
     * 
     * @param parameterDefinition
     *            the parameter definition
     */
    public void setParameterDefinition(VOParameterDefinition parameterDefinition) {
        this.parameterDefinition = parameterDefinition;
    }

    /**
     * Retrieves the current value of the parameter.
     * 
     * @return the parameter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value for the parameter.
     * 
     * @param value
     *            the parameter value
     */
    public void setValue(String value) {
        this.value = value;
    }

}
