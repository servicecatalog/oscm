/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.assembler;

import org.oscm.domobjects.TriggerDefinition;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Assembler to convert the trigger definition value objects to the trigger
 * definition domain object and vice versa.
 * 
 * @author pock
 * 
 */
public class TriggerDefinitionAssembler extends BaseAssembler {

    public static final String FIELD_NAME_TYPE = "type";

    public static final String FIELD_NAME_TARGET_TYPE = "targetType";

    public static final String FIELD_NAME_TARGET = "target_url";

    public static final String FIELD_NAME_NAME = "name";

    private static Log4jLogger logger = LoggerFactory
            .getLogger(TriggerDefinitionAssembler.class);

    /**
     * Creates a new VOTriggerDefinition object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param domObj
     *            The domain object containing the values to be set.
     * @return The created value object or null if the domain object was null.
     */
    public static VOTriggerDefinition toVOTriggerDefinition(
            TriggerDefinition domObj) {
        if (domObj == null) {
            return null;
        }
        VOTriggerDefinition vo = new VOTriggerDefinition();
        vo.setType(domObj.getType());
        vo.setTargetType(domObj.getTargetType());
        vo.setTarget(domObj.getTarget());
        vo.setSuspendProcess(domObj.isSuspendProcess());
        vo.setName(domObj.getName());

        if (domObj.getOrganization() != null) {
            VOOrganization org = new VOOrganization();
            org.setKey(domObj.getOrganization().getKey());
            org.setName(domObj.getOrganization().getName());

            vo.setOrganization(org);
        }

        updateValueObject(vo, domObj);
        return vo;
    }

    /**
     * Creates a new VOTriggerDefinition object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param domObj
     *            The domain object containing the values to be set.
     * @param hasTriggerProcess
     *            <code>true</code> If there are trigger processes exist for
     *            current trigger definition, <code>false</code> otherwise
     * @return The created value object or null if the domain object was null.
     */
    public static VOTriggerDefinition toVOTriggerDefinition(
            TriggerDefinition domObj, boolean hasTriggerProcess) {
        VOTriggerDefinition vo = toVOTriggerDefinition(domObj);
        vo.setHasTriggerProcess(hasTriggerProcess);
        return vo;
    }

    /**
     * Updates the fields in the TriggerDefinition object to reflect the changes
     * performed in the value object.
     * 
     * @param domObj
     *            The domain object to be updated.
     * @param vo
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static TriggerDefinition updateTriggerDefinition(
            TriggerDefinition domObj, VOTriggerDefinition vo)
            throws ValidationException, ConcurrentModificationException {
        verifyVersionAndKey(domObj, vo);
        copyAttributes(domObj, vo);
        return domObj;
    }

    /**
     * Converts a value object trigger definition to a domain object
     * representation.
     * 
     * @param vo
     *            The trigger definition in value object representation. Must
     *            not be <code>null</code>.
     * @return The domain object representation of the trigger definition.
     * @throws ValidationException
     */
    public static TriggerDefinition toTriggerDefinition(VOTriggerDefinition vo)
            throws ValidationException {
        final TriggerDefinition domObj = new TriggerDefinition();
        copyAttributes(domObj, vo);
        return domObj;
    }

    private static void copyAttributes(TriggerDefinition domObj,
            VOTriggerDefinition vo) throws ValidationException {
        validate(vo);
        domObj.setType(vo.getType());
        domObj.setTargetType(vo.getTargetType());
        domObj.setTarget(vo.getTarget());
        domObj.setSuspendProcess(vo.isSuspendProcess());
        domObj.setName(vo.getName());
    }

    public static boolean isOnlyNameChanged(VOTriggerDefinition vo,
            TriggerDefinition triggerDefinition) {
        if (!vo.getTarget().equals(triggerDefinition.getTarget())) {
            return false;
        }
        if (!vo.getTargetType().equals(triggerDefinition.getTargetType())) {
            return false;
        }
        if (!vo.getType().equals(triggerDefinition.getType())) {
            return false;
        }
        if ((!vo.isSuspendProcess()) && (triggerDefinition.isSuspendProcess())) {
            return false;
        }
        if ((vo.isSuspendProcess()) && (!triggerDefinition.isSuspendProcess())) {
            return false;
        }
        return true;
    }

    /**
     * Validate a trigger definition value object.
     * 
     * @param vo
     *            the value object to validate
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    private static void validate(VOTriggerDefinition vo)
            throws ValidationException {
        BLValidator.isDescription(FIELD_NAME_TARGET, vo.getTarget(), true);
        BLValidator.isNotNull(FIELD_NAME_TYPE, vo.getType());
        BLValidator.isNotNull(FIELD_NAME_TARGET_TYPE, vo.getTargetType());
        BLValidator.isNotNull(FIELD_NAME_NAME, vo.getName());
        if (vo.isSuspendProcess() && !vo.getType().isSuspendProcess()) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.TRIGGER_TYPE_SUPPORTS_NO_PROCESS_SUSPENDING,
                    FIELD_NAME_TYPE, new Object[] { vo.getType().name() });
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    vf,
                    LogMessageIdentifier.ERROR_TRIGGER_TYPE_NOT_SUPPORTED_PROCESS_SUSPENDING,
                    vo.getType().name());
            throw vf;
        }
    }

}
