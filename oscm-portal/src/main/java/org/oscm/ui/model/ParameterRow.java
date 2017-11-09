/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Feb 1, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.util.List;

import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;

/**
 * A super class for all the Parameter related models.
 * 
 * @author pravi
 * 
 */
public class ParameterRow {
    private VOParameter parameter;
    private VOParameterOption parameterOption;
    private VOParameterDefinition parameterDefinition;

    private int optionIndex;
    private String passwordValueToStore;

    public ParameterRow() {

    }

    public ParameterRow(VOParameter parameter,
            VOParameterOption parameterOption, boolean initDefault) {
        this.parameter = parameter;
        this.parameterOption = parameterOption;
        parameterDefinition = parameter.getParameterDefinition();

        if (initDefault && parameter.getValue() == null) {
            parameter.setValue(parameterDefinition.getDefaultValue());
            if (parameter.getValue() == null
                    && parameterDefinition.getValueType() == ParameterValueType.ENUMERATION) {
                parameter.setValue(parameterDefinition.getParameterOptions()
                        .get(0).getOptionId());
            }
        }

        // Bugzilla Bug 7841
        if (parameterDefinition.isValueTypeBoolean())
            parameterDefinition.setMandatory(false);
    }

    public ParameterRow(VOParameterDefinition voParameterDef,
            VOParameterOption parameterOption) {
        this.parameterOption = parameterOption;
        parameterDefinition = voParameterDef;
    }

    public VOParameterOption getParameterOption() {
        return parameterOption;
    }

    public boolean getBooleanParameterValue() {
        return Boolean.parseBoolean(parameter.getValue());
    }

    public void setBooleanParameterValue(boolean value) {
        parameter.setValue(String.valueOf(value));
    }

    public VOParameter getParameter() {
        return parameter;
    }

    public VOParameterDefinition getParameterDefinition() {
        return parameterDefinition;
    }

    public boolean isOption() {
        return parameterOption != null;
    }

    public String getValueTypeToLower() {
        return parameterDefinition.getValueType().toString().toLowerCase();
    }

    public String getDescription() {
        if (isOption()) {
            return getParameterOption().getOptionDescription();
        }
        return getParameterDefinition().getDescription();
    }

    public Long getMinValue() {
        if (parameterDefinition != null) {
            return parameterDefinition.getMinValue();
        }
        return null;
    }

    public Long getMaxValue() {
        if (parameterDefinition != null) {
            return parameterDefinition.getMaxValue();
        }
        return null;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return isOption() ? getParameterDefinition().getParameterId() + "\\"
                + getParameterOption().getOptionDescription()
                : getParameterDefinition().getParameterId();
    }

    public String getRangeLabel() {
        Long min = getMinValue();
        Long max = getMaxValue();
        if (min != null && max != null) {
            return min.longValue() + "-" + max.longValue();
        } else if (min != null) {
            return ">= " + min.longValue();
        } else if (max != null) {
            return "<= " + max.longValue();
        }
        return "";
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public void setOptionIndex(int optionIndex) {
        this.optionIndex = optionIndex;
    }

    public boolean isRangeRendered() {
        VOParameterDefinition def = getParameterDefinition();
        return (def.isValueTypeDuration() || def.isValueTypeInteger() || def
                .isValueTypeLong());
    }

    /**
     * Checks whether the parameter value is required. If the parameter is set
     * to be configurable for the customer, the supplier is not required to
     * provide a value for it, by creating/updating service. If the parameter is
     * set to be not configurable for the customer, the parameter definition
     * decides if the value is required (mandatory flag).
     * 
     * @return <code>true</code> if the parameter value is required,
     *         <code>false</code> otherwise
     */
    public boolean isRequired() {
        if (parameter.isConfigurable()) {
            return false;
        }
        return parameterDefinition.isMandatory();
    }

    /**
     * Checks whether the parameter value is of type ONE_TIME. If the parameter
     * is set to be ONE_TIME, the parameter is only allowed to be modified on
     * the Subscribe page.
     * 
     * @return <code>true</code> if the parameter value is ONE_TIME,
     *         <code>false</code> otherwise
     * */

    public boolean isOneTimeParameter() {
        return (parameterDefinition.getModificationType() == ParameterModificationType.ONE_TIME);
    }

    /**
     * Check if parameter is of type ONE_TIME and no user option.
     * 
     * @return <code>true</code> if the parameter is not a user option and
     *         defined with the modification type
     *         <code>ParameterModificationType.ONE_TIME</code>, otherwise
     *         <code>false</code>.
     */
    public boolean isNonConfigurableOneTimeParameter() {
        return !getParameter().isConfigurable() && isOneTimeParameter();
    }

    public VOParameterOption getSelectedEnumerateOption() {
        VOParameterOption enumerateOption = null;
        if (ParameterValueType.ENUMERATION == parameterDefinition
                .getValueType()) {
            List<VOParameterOption> options = parameterDefinition
                    .getParameterOptions();
            for (VOParameterOption option : options) {
                if (option.getOptionId().equalsIgnoreCase(parameter.getValue())) {
                    enumerateOption = option;
                }
            }
        }
        return enumerateOption;
    }

    public boolean isA4JSupportDisabled() {
        return !parameterDefinition.isConfigurable()
                || !parameterDefinition.isMandatory();
    }
    
    public boolean isPasswordType(){
        return getParameterDefinition().getParameterId().contains("_PWD") || getParameterDefinition().getValueType().equals(ParameterValueType.PWD);
    }

    public String getPasswordValueToStore() {
        return this.passwordValueToStore;
    }

    public void setPasswordValueToStore(String passwordValueToStore) {
        this.passwordValueToStore = passwordValueToStore;
    }
}
