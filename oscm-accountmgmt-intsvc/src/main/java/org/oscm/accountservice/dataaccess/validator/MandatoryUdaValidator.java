/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dataaccess.validator;

import java.util.List;

import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.vo.VOUda;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * @author weiser
 * 
 */
public class MandatoryUdaValidator {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MandatoryUdaValidator.class);

    /**
     * Checks if {@link Uda}s of the passed {@link UdaDefinition} are mandatory.
     * 
     * @param def
     *            the {@link UdaDefinition} to check
     * @throws MandatoryUdaMissingException
     *             if the {@link UdaConfigurationType} of the passed
     *             {@link UdaDefinition} is mandatory
     */
    public void checkMandatory(UdaDefinition def)
            throws MandatoryUdaMissingException {
        if (def.getConfigurationType().isMandatory()) {
            throw mandatoryUdaMissingException(def);
        }
    }

    private MandatoryUdaMissingException mandatoryUdaMissingException(
            UdaDefinition def) {
        String message = "Missing mandatory uda %s.";
        MandatoryUdaMissingException e = new MandatoryUdaMissingException(
                String.format(message, def.getUdaId()),
                new Object[] { def.getUdaId() });
        logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.WARN_MISSING_MANDATORY_UDA,
                def.getUdaId());
        return e;
    }

    private MandatoryCustomerUdaMissingException mandatoryCustomerUdaMissingException(
            UdaDefinition def) {
        String message = "Missing mandatory customer uda %s.";
        MandatoryCustomerUdaMissingException e = new MandatoryCustomerUdaMissingException(
                String.format(message, def.getUdaId()),
                new Object[] { def.getUdaId() });
        logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                LogMessageIdentifier.WARN_MISSING_MANDATORY_UDA,
                def.getUdaId());
        return e;
    }

    /**
     * Checks if all mandatory {@link Uda}s are passed for saving. As a fall
     * back it will be checked if {@link Uda}s are already persisted if they are
     * not passed as value object
     * 
     * @param required
     *            the list of mandatory {@link UdaDefinition}s
     * @param existing
     *            the list of existing {@link Uda}s
     * @param passed
     *            the list of passed {@link VOUda}s
     * @throws MandatoryUdaMissingException
     *             in case for one of the mandatory {@link UdaDefinition}s is no
     *             {@link VOUda} passed and no {@link Uda} existing
     */
    public void checkAllRequiredUdasPassed(List<UdaDefinition> required,
            List<Uda> existing, List<VOUda> passed)
            throws MandatoryCustomerUdaMissingException,
            MandatoryUdaMissingException {
        for (UdaDefinition def : required) {
            Uda uda = getExistingUdaForDefinition(def, existing);
            VOUda voUda = getPassedUdaForDefinition(def, passed);
            if (uda == null && voUda == null) {
                if (def.getTargetType() == UdaTargetType.CUSTOMER) {
                    if (def.getDefaultValue() == null
                            || def.getDefaultValue().trim().length() == 0) {
                        throw mandatoryCustomerUdaMissingException(def);
                    }
                } else {
                    throw mandatoryUdaMissingException(def);
                }
            }
        }
    }

    private VOUda getPassedUdaForDefinition(UdaDefinition def,
            List<VOUda> passed) {
        for (VOUda voUda : passed) {
            if (def.getKey() == voUda.getUdaDefinition().getKey()) {
                return voUda;
            }
        }
        return null;
    }

    private Uda getExistingUdaForDefinition(UdaDefinition def,
            List<Uda> existing) {
        for (Uda uda : existing) {
            if (uda.getUdaDefinition() == def) {
                return uda;
            }
        }
        return null;
    }
}
