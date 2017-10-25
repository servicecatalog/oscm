/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-09                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.types.enumtypes.ParameterModificationType;
import org.oscm.types.enumtypes.ParameterType;
import org.oscm.types.enumtypes.ParameterValueType;

/**
 * Represents the definition of a parameter for a technical service.
 * 
 */
public class VOParameterDefinition extends BaseVO {

    private static final long serialVersionUID = 6447762328727717451L;

    /**
     * Default constructor.
     */
    public VOParameterDefinition() {
    }

    /**
     * Constructs a parameter definition with the given settings.
     * 
     * @param parameterType
     *            the type of the parameter
     * @param parameterId
     *            the identifier of the parameter
     * @param description
     *            the text describing the parameter
     * @param valueType
     *            the data type of the parameter
     * @param defaultValue
     *            the default value for the parameter
     * @param minValue
     *            the minimum value for the parameter
     * @param maxValue
     *            the maximum value for the parameter
     * @param mandatory
     *            <code>true</code> if a value for the parameter must be set at
     *            a subscription, <code>false</code> otherwise
     * @param configurable
     *            <code>true</code> if the parameter is to be configurable by a
     *            supplier, <code>false</code> otherwise
     * @param parameterOptions
     *            the parameter options, if the data type is
     *            <code>ENUMERATION</code>
     */
    public VOParameterDefinition(ParameterType parameterType,
            String parameterId, String description,
            ParameterValueType valueType, String defaultValue, Long minValue,
            Long maxValue, boolean mandatory, boolean configurable,
            List<VOParameterOption> parameterOptions) {
        super();
        this.parameterType = parameterType;
        this.parameterId = parameterId;
        this.description = description;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.configurable = configurable;
        this.mandatory = mandatory;
        this.parameterOptions = parameterOptions;
        this.modificationType = ParameterModificationType.STANDARD;
    }

    /**
     * Constructs a parameter definition with the given settings.
     * 
     * @param parameterType
     *            the type of the parameter
     * @param parameterId
     *            the identifier of the parameter
     * @param description
     *            the text describing the parameter
     * @param valueType
     *            the data type of the parameter
     * @param defaultValue
     *            the default value for the parameter
     * @param minValue
     *            the minimum value for the parameter
     * @param maxValue
     *            the maximum value for the parameter
     * @param mandatory
     *            <code>true</code> if a value for the parameter must be set at
     *            a subscription, <code>false</code> otherwise
     * @param configurable
     *            <code>true</code> if the parameter is to be configurable by a
     *            supplier, <code>false</code> otherwise
     * @param modificationType
     *            the number of times the parameter value can be set and
     *            modified
     * @param parameterOptions
     *            the parameter options, if the data type is
     *            <code>ENUMERATION</code>
     */
    public VOParameterDefinition(ParameterType parameterType,
            String parameterId, String description,
            ParameterValueType valueType, String defaultValue, Long minValue,
            Long maxValue, boolean mandatory, boolean configurable,
            ParameterModificationType modificationType,
            List<VOParameterOption> parameterOptions) {
        super();
        this.parameterType = parameterType;
        this.parameterId = parameterId;
        this.description = description;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.configurable = configurable;
        this.mandatory = mandatory;
        this.parameterOptions = parameterOptions;
        this.modificationType = modificationType;
    }

    /**
     * The parameter options.
     */
    private List<VOParameterOption> parameterOptions = new ArrayList<VOParameterOption>();

    /**
     * The default value of the parameter.
     */
    private String defaultValue;
    /**
     * The minimum value for the parameter.
     */
    private Long minValue;
    /**
     * The maximum value for the parameter.
     */
    private Long maxValue;
    /**
     * Specifies whether the parameter is mandatory.
     */
    private boolean mandatory;
    /**
     * Specifies whether the parameter is configurable by suppliers.
     */
    private boolean configurable;

    /**
     * The type of the parameter.
     */
    private ParameterType parameterType;

    /**
     * The identifier of the parameter.
     */
    private String parameterId;

    /**
     * The data type of the parameter.
     */
    private ParameterValueType valueType;

    /**
     * The number of times the parameter value can be set and modified.
     **/
    private ParameterModificationType modificationType;

    /**
     * The description of the parameter.
     */
    private String description;

    /**
     * Retrieves the type of the parameter.
     * 
     * @return the parameter type
     */
    public ParameterType getParameterType() {
        return parameterType;
    }

    /**
     * Retrieves the number of times the parameter value can be set and
     * modified.
     * 
     * @return the parameter modification type
     */
    public ParameterModificationType getModificationType() {
        return modificationType;
    }

    /**
     * Retrieves the identifier of the parameter.
     * 
     * @return the parameter ID
     */
    public String getParameterId() {
        return parameterId;
    }

    /**
     * Retrieves the data type of the parameter.
     * 
     * @return the data type
     */
    public ParameterValueType getValueType() {
        return valueType;
    }

    /**
     * Checks if the parameter data type is <code>BOOLEAN</code>.
     * 
     * @return <code>true</code> if the data type of the parameter is
     *         <code>BOOLEAN</code>, <code>false</code> otherwise
     */
    public boolean isValueTypeBoolean() {
        return valueType == ParameterValueType.BOOLEAN;
    }

    /**
     * Checks if the parameter data type is <code>INTEGER</code>.
     * 
     * @return <code>true</code> if the data type of the parameter is
     *         <code>INTEGER</code>, <code>false</code> otherwise
     */
    public boolean isValueTypeInteger() {
        return valueType == ParameterValueType.INTEGER;
    }

    /**
     * Checks if the parameter data type is <code>LONG</code>.
     * 
     * @return <code>true</code> if the data type of the parameter is
     *         <code>LONG</code>, <code>false</code> otherwise
     */
    public boolean isValueTypeLong() {
        return valueType == ParameterValueType.LONG;
    }

    /**
     * Checks if the parameter data type is <code>STRING</code>.
     * 
     * @return <code>true</code> if the data type of the parameter is
     *         <code>STRING</code>, <code>false</code> otherwise
     */
    public boolean isValueTypeString() {
        return valueType == ParameterValueType.STRING;
    }

    /**
     * Checks if the parameter data type is <code>DURATION</code>.
     * 
     * @return <code>true</code> if the data type of the parameter is
     *         <code>DURATION</code>, <code>false</code> otherwise
     */
    public boolean isValueTypeDuration() {
        return valueType == ParameterValueType.DURATION;
    }

    /**
     * Checks if the parameter data type is <code>PWD</code>.
     *
     * @return <code>true</code> if the data type of the parameter is
     *         <code>PWD</code>, <code>false</code> otherwise.
     */
    public boolean isValueTypePWD() {
        return valueType == ParameterValueType.PWD;
    }

    /**
     * Checks if the parameter data type is <code>ENUMERATION</code>.
     * 
     * @return <code>true</code> if the data type of the parameter is
     *         <code>ENUMERATION</code>, <code>false</code> otherwise.
     */
    public boolean isValueTypeEnumeration() {
        return valueType == ParameterValueType.ENUMERATION;
    }

    /**
     * Retrieves the text describing the parameter.
     * 
     * @return the parameter description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retrieves the options of the parameter.
     * 
     * @return the parameter options
     */
    public List<VOParameterOption> getParameterOptions() {
        return parameterOptions;
    }

    /**
     * Retrieves the default value of the parameter.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retrieves the minimum value for the parameter.
     * 
     * @return the minimum value
     */
    public Long getMinValue() {
        return minValue;
    }

    /**
     * Retrieves the maximum value for the parameter.
     * 
     * @return the maximum value
     */
    public Long getMaxValue() {
        return maxValue;
    }

    /**
     * Checks whether a value for the parameter must be set at a subscription.
     * 
     * @return <code>true</code> if the parameter is mandatory,
     *         <code>false</code> otherwise
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Checks whether the parameter can be configured by a supplier.
     * 
     * @return <code>true</code> if the parameter is configurable,
     *         <code>false</code> otherwise
     * 
     */
    public boolean isConfigurable() {
        return configurable;
    }

    /**
     * Specifies whether a value for the parameter must be set at a
     * subscription.
     * 
     * @param mandatory
     *            <code>true</code> if the parameter is mandatory,
     *            <code>false</code> otherwise
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Specifies whether the parameter can be configured by a supplier.
     * 
     * @param configurable
     *            <code>true</code> if the parameter is configurable,
     *            <code>false</code> otherwise
     * 
     */
    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    /**
     * Sets the options of the parameter, if the data type is
     * <code>ENUMERATION</code>
     * 
     * @param parameterOptions
     *            the parameter options
     */
    public void setParameterOptions(List<VOParameterOption> parameterOptions) {
        this.parameterOptions = parameterOptions;
    }

    /**
     * Sets the default value for the parameter.
     * 
     * @param defaultValue
     *            the default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the minimum value for the parameter.
     * 
     * @param minValue
     *            the minimum value
     */
    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    /**
     * Sets the maximum value for the parameter.
     * 
     * @param maxValue
     *            the maximum value
     */
    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Sets the type of the parameter.
     * 
     * @param parameterType
     *            the parameter type
     */
    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
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
     * Sets the data type for the parameter.
     * 
     * @param valueType
     *            the data type
     */
    public void setValueType(ParameterValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * Sets the text describing the parameter.
     * 
     * @param description
     *            the parameter description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the number of times the parameter value can be set and modified.
     * 
     * @param modificationType
     *            the parameter modification type
     */
    public void setModificationType(ParameterModificationType modificationType) {
        this.modificationType = modificationType;
    }

}
