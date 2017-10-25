/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-2                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import java.math.BigDecimal;

import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.serviceprovisioningservice.verification.UpdateParameterCheck;
import org.xml.sax.Attributes;

/**
 * @author yuyin
 * 
 */
class TechnicalProductParameterImportParser {

    private static final String ATTRIBUTE_MANDATORY = "mandatory";
    private static final String ATTRIBUTE_MIN_VALUE = "minValue";
    private static final String ATTRIBUTE_MAX_VALUE = "maxValue";
    private static final String ATTRIBUTE_CONFIGURABLE = "configurable";
    private static final String ATTRIBUTE_MODIFICATIONTYPE = "modificationType";
    private static final String ATTRIBUTE_DEFAULT = "default";

    /**
     * The technical product to which the parameter definition belongs
     */
    private TechnicalProduct techProduct;

    /**
     * If the parameter value type is Enumeration, save the default value to
     * tempDefaultValueForEnumeration
     */
    private String tempDefaultValueForEnumeration;
    /**
     * Parse new parameter if the value of createAction is true; if false,
     * updating parameter.
     */
    private boolean createAction;

    /**
     * Parameter definition to be created or updated.
     */
    private ParameterDefinition paramDef;

    /**
     * @param techProduct
     *            the techProduct to set
     */
    void setTechProduct(TechnicalProduct techProduct) {
        this.techProduct = techProduct;
    }

    /**
     * @param tempValue
     *            the tempDefaultValueForEnumeration to set
     */
    void setTempDefaultValueForEnumeration(String tempValue) {
        this.tempDefaultValueForEnumeration = tempValue;
    }

    /**
     * @return the tempDefaultValueForEnumeration
     */
    String getTempDefaultValueForEnumeration() {
        return tempDefaultValueForEnumeration;
    }

    /**
     * @param id
     *            the parameter identifier
     * @param valueType
     *            the parameter value type
     * @param atts
     *            the parameter attributes
     * @param techProd
     *            technical product to which the parameter definition belongs
     * @param paramDefinition
     *            parameter definition to be created or updated
     * @return The created or updated parameter definition
     * @throws UpdateConstraintException
     * @throws ImportException
     */
    ParameterDefinition parseParameterDef(String id, String valueType,
            Attributes atts, TechnicalProduct techProd,
            ParameterDefinition paramDefinition)
            throws UpdateConstraintException, ImportException {
        this.paramDef = paramDefinition;
        this.techProduct = techProd;
        String[] optionalValues = getOptionalValues(atts, ATTRIBUTE_MANDATORY,
                ATTRIBUTE_DEFAULT, ATTRIBUTE_MIN_VALUE, ATTRIBUTE_MAX_VALUE,
                ATTRIBUTE_CONFIGURABLE, ATTRIBUTE_MODIFICATIONTYPE);

        parseOptionalValue(valueType, optionalValues[1], ATTRIBUTE_DEFAULT);
        parseOptionalValue(valueType, optionalValues[2], ATTRIBUTE_MIN_VALUE);
        parseOptionalValue(valueType, optionalValues[3], ATTRIBUTE_MAX_VALUE);

        return createOrUpdateProductParameterDefinition(id,
                ParameterValueType.valueOf(valueType), optionalValues[0], // mandatory
                optionalValues[1], // default value
                optionalValues[2], // minimum value
                optionalValues[3], // maximum value
                optionalValues[4], // configurable
                optionalValues[5]);// modificationType
    }

    private void parseOptionalValue(String valueType, String value,
            String attribute) throws ImportException {
        if (value == null || value.length() == 0) {
            return;
        }

        ParameterValueType type = ParameterValueType.valueOf(valueType);
        try {
            switch (type) {
            case BOOLEAN:
                if (!isBoolValue(value))
                    throw new IllegalArgumentException();
                break;
            case INTEGER:
                Integer.parseInt(value);
                break;
            case LONG:
                Long.parseLong(value);
                break;
            case DURATION:
                new BigDecimal(value);
                break;
            default:
                // strings ok, enumarations handled later
                break;
            }
        } catch (IllegalArgumentException ex) {
            throw new ImportException(String.format(
                    "The value '%s' for the attribute '%s' does not match with the required type %s.",
                    value, attribute, type.name()));
        }

    }

    private boolean isBoolValue(String value) {
        return (Boolean.TRUE.toString().equals(value)
                || Boolean.FALSE.toString().equals(value));
    }

    /**
     * Return true if the given string is null or only contains whitespaces.
     * 
     * @param str
     *            the string to check
     * @return true if the given string is null or only contains whitespaces.
     */
    private boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        return str.trim().length() == 0;
    }

    /**
     * @param atts
     * @param attNames
     * @return String value of optional attributes
     */
    String[] getOptionalValues(Attributes atts, String... attNames) {
        String[] values = new String[attNames.length];
        for (int i = 0; i < attNames.length; i++) {
            values[i] = atts.getValue(attNames[i]);
        }
        return values;
    }

    /**
     * Get the parameter definition for the parameter identifier from the
     * database and create a new parameter definition, if the parameter
     * definition doesn't already exist.
     * 
     * @param id
     *            the parameter identifier
     * @param valueType
     *            the parameter value type
     * @param mandatory
     *            Indicates if the parameter definition is mandatory.
     * @param defaultValue
     *            The default value to be set for the parameter definition.
     * @param minValue
     *            The minimum value allowed for the parameter.
     * @param maxValue
     *            The maximum value allowed for the parameter.
     * @param configurable
     *            Indicates whether the parameter is configurable or not.
     * @param modificationType
     *            Indicates whether the parameter is able to be modified after
     *            subscribing.
     */
    void createParameterDefinition(String id, ParameterValueType valueType,
            String mandatory, String defaultValue, String minValue,
            String maxValue, String configurable, String modificationType)
            throws ImportException {
        paramDef = new ParameterDefinition();
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setParameterId(id);
        paramDef.setValueType(valueType);
        if (!isBlank(mandatory)) {
            paramDef.setMandatory(Boolean.parseBoolean(mandatory));
        } else {
            paramDef.setMandatory(false);
        }
        if (!isBlank(configurable)) {
            paramDef.setConfigurable(Boolean.parseBoolean(configurable));
        } else {
            paramDef.setConfigurable(true);
        }
        if (!isBlank(modificationType)) {
            paramDef.setModificationType(
                    ParameterModificationType.valueOf(modificationType));
        } else {
            paramDef.setModificationType(ParameterModificationType.STANDARD);
        }

        setDefaultValue(defaultValue, minValue, maxValue, valueType);
        if (!isBlank(minValue)) {
            paramDef.setMinimumValue(Long.valueOf(minValue));
        }
        if (!isBlank(maxValue)) {
            paramDef.setMaximumValue(Long.valueOf(maxValue));
        }

        paramDef.setTechnicalProduct(techProduct);
        setCreateAction(true);
    }

    /**
     * Get the parameter definition for the parameter identifier from the
     * database and update it , if the parameter definition already exists.
     * 
     * @param id
     *            the parameter identifier
     * @param valueType
     *            the parameter value type
     * @param mandatory
     *            Indicates if the parameter definition is mandatory.
     * @param defaultValue
     *            The default value to be set for the parameter definition.
     * @param minValue
     *            The minimum value allowed for the parameter.
     * @param maxValue
     *            The maximum value allowed for the parameter.
     * @param configurable
     *            Indicates whether the parameter is configurable or not.
     * @param modificationType
     *            Indicates whether the parameter is able to be modified after
     *            subscribing.
     * @throws UpdateConstraintException
     * @throws ImportException
     */
    void updateParameterDefinition(String id, ParameterValueType valueType,
            String mandatory, String defaultValue, String minValue,
            String maxValue, String configurable, String modificationType)
            throws UpdateConstraintException, ImportException {

        boolean isDirty = false;

        if (paramDef.getValueType() != valueType) {
            paramDef.setValueType(valueType);
            isDirty = true;
        }

        if (!isBlank(mandatory)) {
            boolean flag = Boolean.parseBoolean(mandatory);
            if (paramDef.isMandatory() != flag) {
                paramDef.setMandatory(flag);
                isDirty = true;
            }
        } else {
            if (paramDef.isMandatory()) {
                paramDef.setMandatory(false);
                isDirty = true;
            }
        }

        if (!isBlank(configurable)) {
            boolean flag = Boolean.parseBoolean(configurable);
            if (paramDef.isConfigurable() != flag) {
                paramDef.setConfigurable(flag);
                isDirty = true;
            }
        } else {
            if (!paramDef.isConfigurable()) {
                paramDef.setConfigurable(true);
                isDirty = true;
            }
        }

        if (isBlank(modificationType)) {
            modificationType = ParameterModificationType.STANDARD.name();
        }
        UpdateParameterCheck.updateParameterDefinition(paramDef, techProduct,
                modificationType);
        if (isNewValue(paramDef.getModificationType().name(),
                modificationType)) {
            paramDef.setModificationType(
                    ParameterModificationType.valueOf(modificationType));
            isDirty = true;
        }

        if (isNewValue(paramDef.getDefaultValue(), defaultValue)) {
            setDefaultValue(defaultValue, minValue, maxValue, valueType);
            isDirty = true;
        } else if (valueType == ParameterValueType.ENUMERATION) {
            tempDefaultValueForEnumeration = defaultValue;
        }

        Long val = null;
        if (!isBlank(minValue)) {
            val = Long.valueOf(minValue);
        }
        if (isNewValue(paramDef.getMinimumValue(), val)) {
            paramDef.setMinimumValue(val);
            isDirty = true;
        }

        val = null;
        if (!isBlank(maxValue)) {
            val = Long.valueOf(maxValue);
        }
        if (isNewValue(paramDef.getMaximumValue(), val)) {
            paramDef.setMaximumValue(val);
            isDirty = true;
        }

        if (isDirty & paramDef.definesParametersOfUndeletedProduct()) {
            UpdateConstraintException e = new UpdateConstraintException(
                    ClassEnum.TECHNICAL_SERVICE, getTechProductBusinessKey());
            throw e;
        }
        setCreateAction(false);
    }

    /**
     * Get the parameter definition for the parameter identifier from the
     * database and update it or create a new parameter definition, if the
     * parameter definition doesn't already exist.
     * 
     * @param id
     *            the parameter identifier
     * @param valueType
     *            the parameter value type
     * @param mandatory
     *            Indicates if the parameter definition is mandatory.
     * @param defaultValue
     *            The default value to be set for the parameter definition.
     * @param minValue
     *            The minimum value allowed for the parameter.
     * @param maxValue
     *            The maximum value allowed for the parameter.
     * @param configurable
     *            Indicates whether the parameter is configurable or not.
     * @param modificationType
     *            Indicates whether the parameter is able to be modified after
     *            subscribing.
     * @return The initialized parameter definition.
     * @throws UpdateConstraintException
     * @throws ImportException
     */
    ParameterDefinition createOrUpdateProductParameterDefinition(String id,
            ParameterValueType valueType, String mandatory, String defaultValue,
            String minValue, String maxValue, String configurable,
            String modificationType)
            throws UpdateConstraintException, ImportException {
        if (paramDef == null) {
            createParameterDefinition(id, valueType, mandatory, defaultValue,
                    minValue, maxValue, configurable, modificationType);
        } else {
            updateParameterDefinition(id, valueType, mandatory, defaultValue,
                    minValue, maxValue, configurable, modificationType);
        }
        return paramDef;
    }

    /**
     * Responsible for assigning the default value. The default value should be
     * in the range of the minimum and maximum value. If the default value is
     * not assigned the minimum value will be taken as a default value.
     * 
     * @param defaultValue
     *            The default value for the parameter definition.
     * @param minValue
     *            The minimum value for the parameter definition.
     * @param maxValue
     *            The maximum value for the parameter definition.
     * @param valueType
     *            The value type for the parameter definition.
     * @throws ImportException
     * 
     */
    void setDefaultValue(String defaultValue, String minValue, String maxValue,
            ParameterValueType valueType) throws ImportException {
        isDefaultValueRequired(defaultValue);

        if (valueType == ParameterValueType.BOOLEAN
                || valueType == ParameterValueType.STRING
                || valueType == ParameterValueType.PWD
                || valueType == ParameterValueType.DURATION
                || valueType == ParameterValueType.ENUMERATION) {
            setDefaultValueForNonDigitalTpye(defaultValue, valueType);
        } else {
            setDefaultValueForDigitalTpye(defaultValue, minValue, maxValue);
        }

    }

    /**
     * If the parameter definition is marked as non-configurable, the default
     * value will be set. If no default values is defined, the import operation
     * will fail.
     * 
     * @param defaultValue
     *            The default value for the parameter definition.
     * @throws ImportException
     * 
     */
    void isDefaultValueRequired(String defaultValue) throws ImportException {
        // if the parameter is not configurable and no default value is defined
        if (!paramDef.isConfigurable() && isBlank(defaultValue)) {
            ImportException e = new ImportException(
                    "The parameter default value must be set as it is not configurable");
            throw e;
        }
    }

    /**
     * Set default value to parameter definition when <code> valueType</code> is
     * BOOLEAN, STRING, DURATION or ENUMERATION.
     * 
     * @param defaultValue
     *            The default value for the parameter definition.
     * @param valueType
     *            The value type for the parameter definition.
     * 
     */
    void setDefaultValueForNonDigitalTpye(String defaultValue,
            ParameterValueType valueType) {
        if (valueType == ParameterValueType.ENUMERATION) {
            // default value will be set after the options have been read
            if (!isBlank(defaultValue)) {
                tempDefaultValueForEnumeration = defaultValue;
            }
        } else {
            paramDef.setDefaultValue(
                    isBlank(defaultValue) ? null : defaultValue);
        }
    }

    /**
     * Set default value to parameter definition when <code> valueType</code> is
     * INTEGER or LONG.
     * 
     * @param defaultValue
     *            The default value for the parameter definition.
     * @param minValue
     *            The minimum value for the parameter definition.
     * @param maxValue
     *            The maximum value for the parameter definition.
     * @throws ImportException
     * 
     */
    void setDefaultValueForDigitalTpye(String defaultValue, String minValue,
            String maxValue) throws ImportException {
        if (isBlank(defaultValue)) {
            paramDef.setDefaultValue(null);
        } else {
            double def = Double.parseDouble(defaultValue);
            Double min = null;
            if (!isBlank(minValue)) {
                min = Double.valueOf(minValue);
            }
            Double max = null;
            if (!isBlank(maxValue)) {
                max = Double.valueOf(maxValue);
            }
            if ((max != null && def > max.longValue())
                    || (min != null && def < min.longValue())) {
                throw new ImportException(
                        "The default value should be in the range of minimum and maximum value");
            }
            paramDef.setDefaultValue(defaultValue);
        }
    }

    /**
     * @param oldVal
     * @param newVal
     * @return true if the given oldVal is not equals with newVal
     */
    boolean isNewValue(Object oldVal, Long newVal) {
        return oldVal == null && newVal != null
                || oldVal != null && !oldVal.equals(newVal);
    }

    /**
     * @param oldVal
     * @param newVal
     * @return true if the given oldVal is not equals with newVal
     */
    boolean isNewValue(Object oldVal, String newVal) {
        return oldVal == null && !isBlank(newVal)
                || oldVal != null && !oldVal.equals(newVal);
    }

    /**
     * @return the technical product id or an empty string if the current
     *         technical product is null.
     */
    String getTechProductBusinessKey() {
        if (techProduct == null) {
            return "";
        }
        return techProduct.getTechnicalProductId();
    }

    /**
     * @param createAction
     *            the createAction to set
     */
    void setCreateAction(boolean createAction) {
        this.createAction = createAction;
    }

    /**
     * @return the createAction
     */
    boolean isCreateAction() {
        return createAction;
    }

    /**
     * @param paramDef
     *            the paramDef to set
     */
    void setParamDef(ParameterDefinition paramDef) {
        this.paramDef = paramDef;
    }

    /**
     * @return the paramDef
     */
    ParameterDefinition getParamDef() {
        return paramDef;
    }
}
