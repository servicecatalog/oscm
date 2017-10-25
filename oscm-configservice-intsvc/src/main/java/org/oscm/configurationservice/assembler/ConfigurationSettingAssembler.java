/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                   
 *                                                                              
 *  Creation Date: 28.01.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.configurationservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Provides the assembling functionality to handle configuration setting
 * objects.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ConfigurationSettingAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ConfigurationSettingAssembler.class);

    private static final String FIELD_NAME_CONTEXT_ID = "contextId";

    /**
     * Converts a given domain object to a value object containing the same
     * data.
     * 
     * @param doSetting
     *            The domain object to be converted.
     * @return A value object representing the same configuration setting as the
     *         domain object.
     */
    public static VOConfigurationSetting toValueObject(
            ConfigurationSetting doSetting) {
        if (doSetting == null) {
            return null;
        }
        VOConfigurationSetting voSetting = new VOConfigurationSetting();
        copyToVOAttributes(doSetting, voSetting);
        updateValueObject(voSetting, doSetting);
        return voSetting;
    }

    /**
     * Converts a list of domain objects to value objects.
     * 
     * @param settings
     *            A list of domain objects to be converted.
     * @return A list of value objects corresponding to the given domain
     *         objects.
     */
    public static List<VOConfigurationSetting> toVOConfigurationSettings(
            List<ConfigurationSetting> settings) {
        List<VOConfigurationSetting> result = new ArrayList<VOConfigurationSetting>();
        for (ConfigurationSetting setting : settings) {
            result.add(toValueObject(setting));
        }
        return result;
    }

    /**
     * Copies VO configuration setting values from an existing configuration
     * setting object.
     * 
     * @param voObj
     *            VO configuration setting object
     * @param domObj
     *            domain configuration setting object
     * @return A domain object representation of the value object.
     * @throws ValidationException
     *             Thrown in case the configuration setting violates
     *             configuration setting validation rules.
     * @throws ConcurrentModificationException
     *             Thrown in case the value object's version does not match the
     *             current domain object's.
     */
    public static ConfigurationSetting updateConfigurationSetting(
            VOConfigurationSetting voObj, ConfigurationSetting domObj)
            throws ValidationException, ConcurrentModificationException {

        if (domObj.getKey() != 0) {
            verifyVersionAndKey(domObj, voObj);
        }
        validate(domObj, voObj);

        copyToDomainAttributes(domObj, voObj);
        return domObj;
    }

    /**
     * Validates the given configuration setting value object.
     * 
     * @param voConfigurationSetting
     *            The value object to be validated
     * @throws ValidationException
     *             If the specified value object does not represent a valid
     *             configuration setting
     */
    static void validate(ConfigurationSetting dbConfigSetting,
            VOConfigurationSetting voConfigurationSetting)
            throws ValidationException {
        validateReadonly(dbConfigSetting, voConfigurationSetting);

        String name = voConfigurationSetting.getInformationId().name();
        String type = voConfigurationSetting.getInformationId().getType();
        String value = voConfigurationSetting.getValue();
        BLValidator.isNotNull(FIELD_NAME_CONTEXT_ID,
                voConfigurationSetting.getContextId());
        if (voConfigurationSetting.getInformationId().isMandatory()) {
            BLValidator.isNotBlank(name, value);
        } else if (value == null || value.trim().length() == 0) {
            return;
        }
        value =value.trim();
        voConfigurationSetting.setValue(value);
        
        if (ConfigurationKey.TYPE_LONG.equals(type)) {
            BLValidator.isLong(name, value);
            // length
            BLValidator.isDescription(name, value, false);
            // range
            long longValue = Long.parseLong(value);
            Long minValue = voConfigurationSetting.getInformationId()
                    .getMinValue();
            Long maxValue = voConfigurationSetting.getInformationId()
                    .getMaxValue();
            BLValidator.isInRange(name, longValue, minValue, maxValue);
        } else if (ConfigurationKey.TYPE_URL.equals(type)) {
            BLValidator.isUrl(name, value, false);
            // length is proven by isUrl()
        } else if (ConfigurationKey.TYPE_MAIL.equals(type)) {
            BLValidator.isEmail(name, value, false);
            // length is proven by isEmail()
        } else if (ConfigurationKey.TYPE_BOOLEAN.equals(type)) {
            BLValidator.isBoolean(name, value);
            // length does not have to be proven
        } else if (ConfigurationKey.TYPE_STRING.equals(type)) {
            Long length = voConfigurationSetting.getInformationId()
                    .getLength();
            BLValidator.isLongEnough(name, value, length);
            BLValidator.isDescription(name, value, false);
        } else {
            BLValidator.isDescription(name, value, false);
        }

    }

    /**
     * Checks if a readonly configuration setting has been modified. In this
     * case, throw a ValidationException
     * 
     * @param dbConfigurationSetting
     *            the configuration setting stored in the db
     * @param voConfigurationSetting
     *            the VOConfigurationSetting
     * @throws ValidationException
     *             thrown in case the readonly configuration setting to be
     *             modified has a different value than the one stored in the db.
     */
    private static void validateReadonly(
            ConfigurationSetting dbConfigurationSetting,
            VOConfigurationSetting voConfigurationSetting)
            throws ValidationException {
        boolean isReadonly = dbConfigurationSetting.getInformationId()
                .isReadonly();
        if (isReadonly) {
            String domObjValue = dbConfigurationSetting.getValue();
            if (domObjValue == null)
                domObjValue = "";
            String voValue = voConfigurationSetting.getValue();
            if (voValue == null)
                voValue = "";
            if (!voValue.equals(domObjValue)) {
                ValidationException vf = new ValidationException(
                        ReasonEnum.READONLY, voConfigurationSetting
                                .getInformationId().name(), new Object[] {
                                voConfigurationSetting.getInformationId()
                                        .name(),
                                voConfigurationSetting.getValue() });

                logger.logWarn(Log4jLogger.SYSTEM_LOG, vf,
                        LogMessageIdentifier.WARN_VALIDATION_FAILED);
                throw vf;
            }

        }
    }

    /**
     * Copies domain object attributes to VO attributes.
     * 
     * @param domObj
     *            domain configuration setting object
     * @param voObj
     *            VO configuration setting object
     */
    private static void copyToVOAttributes(ConfigurationSetting domObj,
            VOConfigurationSetting voObj) {
        voObj.setInformationId(domObj.getInformationId());
        voObj.setValue(domObj.getValue());
        voObj.setContextId(domObj.getContextId());
    }

    /**
     * Copies VO attributes to domain object attributes.
     * 
     * @param domObj
     *            domain configuration setting object
     * @param voObj
     *            VO configuration setting object
     */
    private static void copyToDomainAttributes(ConfigurationSetting domObj,
            VOConfigurationSetting voObj) {
        domObj.setInformationId(voObj.getInformationId());
        domObj.setContextId(voObj.getContextId());
        domObj.setValue(voObj.getValue());
    }
}
