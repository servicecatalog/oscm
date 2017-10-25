/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.10.2011                                                      
 *                                                                              
 *  Completion Time: 10.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.assembler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.PSPSetting;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOPSPSetting;

/**
 * Assembler for the psp setting entities.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPSettingAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PSPSettingAssembler.class);

    private static final String FIELD_NAME_SETTING_KEY = "settingKey";
    private static final String FIELD_NAME_SETTING_VALUE = "settingValue";

    public static PSPSetting updatePSPSetting(VOPSPSetting voSetting,
            PSPSetting setting) throws ConcurrentModificationException,
            ValidationException {
        validate(voSetting);
        verifyVersionAndKey(setting, voSetting);
        copyAttributes(voSetting, setting);
        return setting;
    }

    public static VOPSPSetting toVoPspSetting(PSPSetting setting) {
        VOPSPSetting result = new VOPSPSetting();
        result.setSettingKey(setting.getSettingKey());
        result.setSettingValue(setting.getSettingValue());
        updateValueObject(result, setting);
        return result;
    }

    public static List<VOPSPSetting> toVoPspSettings(List<PSPSetting> settings) {
        List<VOPSPSetting> result = new ArrayList<VOPSPSetting>();
        for (PSPSetting setting : settings) {
            result.add(toVoPspSetting(setting));
        }
        return result;
    }

    private static void copyAttributes(VOPSPSetting voSetting,
            PSPSetting setting) {
        setting.setSettingKey(voSetting.getSettingKey());
        setting.setSettingValue(voSetting.getSettingValue());
    }

    private static void validate(VOPSPSetting voSetting)
            throws ValidationException {
        BLValidator.isDescription(FIELD_NAME_SETTING_KEY,
                voSetting.getSettingKey(), true);
        BLValidator.isDescription(FIELD_NAME_SETTING_VALUE,
                voSetting.getSettingValue(), false);
    }

    /**
     * Ensures that there are no two entries with the same key.
     * 
     * @param settings
     *            The settings to check.
     * @throws ValidationException
     *             Thrown in case the settings could not be validated.
     */
    public static void validateVOSettings(List<VOPSPSetting> settings)
            throws ValidationException {
        Set<String> keys = new HashSet<String>();
        for (VOPSPSetting setting : settings) {
            validate(setting);
            boolean newEntry = keys.add(setting.getSettingKey());
            if (!newEntry) {
                ValidationException ve = new ValidationException(
                        ReasonEnum.DUPLICATE_VALUE, FIELD_NAME_SETTING_KEY,
                        new Object[] { setting.getSettingKey() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_DUPLICATE_PSP_SETTING_KEY);
                throw ve;
            }
        }
    }
}
