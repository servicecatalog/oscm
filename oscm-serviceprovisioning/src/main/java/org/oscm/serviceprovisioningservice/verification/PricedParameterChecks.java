/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 14.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.verification;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOPricedParameter;

/**
 * Class to ensure contraints to parameters are met objects to be created.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PricedParameterChecks {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PricedParameterChecks.class);

    /**
     * Verifies that the parameter is based on a value type that is allowed for
     * priced parameters.
     * 
     * @param param
     *            The parameter the priced parameter should be based on. M
     * @param voPricedParam
     *            The value object input to be related to the parameter.
     * @throws ValidationException
     *             Thrown in case no parameter definition is set or in case the
     *             base type of the parameter does not allow the definition of a
     *             priced parameter.
     */
    public static void isValidBaseParam(Parameter param,
            VOPricedParameter voPricedParam) throws ValidationException {
        ParameterDefinition parameterDefinition = param
                .getParameterDefinition();
        if (parameterDefinition == null
                || parameterDefinition.getValueType() == ParameterValueType.STRING) {
            ValidationException ve = new ValidationException(
                    ReasonEnum.PRICED_PARAM_WRONG_BASE, null,
                    new Object[] { voPricedParam });
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ve,
                    LogMessageIdentifier.WARN_VALIDATION_EXCEPTION_PRICED_PARAM_WRONG_BASE);
            throw ve;
        }
    }

    /**
     * Ensures that the parameter definition is set for the priced parameter. If
     * none is set, a ValidationException will be thrown.
     * 
     * @param voPP
     *            The priced parameter value object to be checked.
     * @throws ValidationException
     */
    public static void validateParamDefSet(VOPricedParameter voPP)
            throws ValidationException {
        if (voPP.getVoParameterDef() == null) {
            ValidationException ve = new ValidationException(
                    ReasonEnum.PRICED_PARAM_WRONG_BASE, null,
                    new Object[] { voPP });
            logger.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    ve,
                    LogMessageIdentifier.WARN_VALIDATION_EXCEPTION_PRICED_PARAM_WRONG_BASE);
            throw ve;
        }
    }

}
