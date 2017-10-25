/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PricedParameter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOSteppedPrice;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;

/**
 * Assembler to handle parameter definitions and concrete parameter settings.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ParameterAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ParameterAssembler.class);
    
    static final long MS_PER_DAY = 86400000L;
    
    static final String FIELD_NAME_VALUE = "value";
    
    static final String FIELD_NAME_PRICE_PER_USER = "pricePerUser";
    
    static final String FIELD_NAME_PRICE_PER_SUBSCRIPTION = "pricePerSubscription";

    /**
     * Converts the parameters of a product to value objects.
     * 
     * @param parameterSet
     *            The parameter set containing the parameters to be converted.
     * @param facade
     *            The localizer facade object.
     * @return The parameters as value objects.
     */
    public static List<VOParameter> toVOParameters(ParameterSet parameterSet,
            LocalizerFacade facade) {
        if (parameterSet == null) {
            return new ArrayList<VOParameter>();
        }
        List<VOParameter> result = new ArrayList<VOParameter>();
        List<Parameter> parameters = parameterSet.getParameters();
        for (Parameter param : parameters) {
            VOParameterDefinition paramDef = ParameterDefinitionAssembler
                    .toVOParameterDefinition(param.getParameterDefinition(),
                            facade);
            VOParameter voParam = new VOParameter(paramDef);
            voParam.setValue(param.getValue());
            voParam.setConfigurable(param.isConfigurable());
            updateValueObject(voParam, param);
            result.add(voParam);
        }
        return result;
    }

    /**
     * Builds a parameter domain object and sets the key value, the version and
     * the value field according to the parameter.
     * 
     * @param voParameter
     *            The value object representation of the parameter.
     * @return A domain object containing the key, version and value information
     *         of the parameter.
     * @throws ValidationException
     * @throws GeneralSecurityException 
     */
    public static Parameter toParameter(VOParameter voParameter)
            throws ValidationException{
        Parameter parameter = new Parameter();
        parameter.setConfigurable(voParameter.isConfigurable());
        
        String paramValue = voParameter.getValue();
        parameter.setValue(paramValue);

        ParameterDefinition definition = toParameterDefinition(voParameter
                .getParameterDefinition());
        parameter.setParameterDefinition(definition);
        return parameter;
    }

    /**
     * Updates a parameter definition domain object.
     * 
     * @param parameter
     * @param voParameter
     * @return
     * @throws ConcurrentModificationException
     * @throws GeneralSecurityException 
     */
    public static Parameter updateParameter(Parameter parameter,
            VOParameter voParameter) throws ConcurrentModificationException {
        verifyVersionAndKey(parameter, voParameter);
        parameter.setConfigurable(voParameter.isConfigurable());

        String paramValue = voParameter.getValue();
        parameter.setValue(paramValue);
        return parameter;
    }

    public static void validateParameter(VOParameter parameter,
            ParameterDefinition paramDef) throws ValidationException {
        ParameterValueType parameterType = paramDef.getValueType();
        String parameterValue = parameter.getValue();
        if (!isValueRequired(parameter, paramDef)
                && GenericValidator.isBlankOrNull(parameter.getValue())) {
            return;
        }
        if (parameterType == ParameterValueType.BOOLEAN) {
            BLValidator.isBoolean(FIELD_NAME_VALUE, parameterValue);
        } else if (parameterType == ParameterValueType.STRING) {
            BLValidator.isNotBlank(FIELD_NAME_VALUE, parameterValue);
        } else if (parameterType == ParameterValueType.ENUMERATION) {
            // a value must always be set already from the supplier - that's
            // the default for the customer if configurable
            validateEnumeratedParameterValue(parameterValue, paramDef);
        } else if (parameterType == ParameterValueType.DURATION) {
            // the value must be non-negative
            BLValidator.isLong(FIELD_NAME_VALUE, parameterValue);
            long longValue = Long.parseLong(parameterValue);
            BLValidator.isNonNegativeNumber(FIELD_NAME_VALUE, longValue);
            if (longValue % MS_PER_DAY != 0) {
                ValidationException vf = new ValidationException(
                        ReasonEnum.DURATION, FIELD_NAME_VALUE,
                        new Object[] { parameterValue });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, vf,
                        LogMessageIdentifier.WARN_VALIDATION_FAILED);
                throw vf;
            }
        } else {
            Long minValue = paramDef.getMinimumValue();
            Long maxValue = paramDef.getMaximumValue();
            if (parameterType == ParameterValueType.INTEGER) {
                BLValidator.isInteger(FIELD_NAME_VALUE, parameterValue);
                BLValidator.isInRange(FIELD_NAME_VALUE,
                        Integer.parseInt(parameterValue), minValue, maxValue);
            } else if (parameterType == ParameterValueType.LONG) {
                BLValidator.isLong(FIELD_NAME_VALUE, parameterValue);
                BLValidator.isInRange(FIELD_NAME_VALUE,
                        Long.parseLong(parameterValue), minValue, maxValue);
            }
        }
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
    private static boolean isValueRequired(VOParameter parameter,
            ParameterDefinition paramDef) {
        if (parameter.isConfigurable()) {
            return false;
        }
        return paramDef.isMandatory();
    }

    public static ParameterDefinition toParameterDefinition(
            VOParameterDefinition vo) throws ValidationException {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setConfigurable(vo.isConfigurable());
        paramDef.setMandatory(vo.isMandatory());
        paramDef.setDefaultValue(vo.getDefaultValue());
        paramDef.setMinimumValue(vo.getMinValue());
        paramDef.setMaximumValue(vo.getMaxValue());
        paramDef.setModificationType(vo.getModificationType());
        BLValidator.isNotNull(FIELD_NAME_VALUE, vo.getModificationType());
        List<ParameterOption> options = ParameterOptionAssembler
                .toParameterOptions(vo.getParameterOptions(), paramDef);
        paramDef.setOptionList(options);
        return paramDef;
    }

    public static List<VOPricedParameter> toVOPricedParameters(
            List<PricedParameter> selectedParameters, LocalizerFacade facade) {
        List<VOPricedParameter> result = new ArrayList<VOPricedParameter>();
        for (PricedParameter selectedParameter : selectedParameters) {
            result.add(toVOPricedParameter(selectedParameter, facade));
        }
        return result;
    }

    /**
     * Converts a priced parameter to the appropriate value object.
     * 
     * @param pricedParam
     *            The domain object to be assembled.
     * @param facade
     *            The localizer facade.
     * @return The value object representation of the priced parameter.
     */
    public static VOPricedParameter toVOPricedParameter(
            PricedParameter pricedParam, LocalizerFacade facade) {
        Parameter parameter = pricedParam.getParameter();
        VOParameterDefinition paraDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(parameter.getParameterDefinition(),
                        facade);

        VOPricedParameter result = new VOPricedParameter(paraDef);
        result.setPricePerUser(pricedParam.getPricePerUser());
        result.setPricePerSubscription(pricedParam.getPricePerSubscription());
        result.setPricedOptions(ParameterOptionAssembler.toVOPricedOptions(
                pricedParam, facade));
        result.setParameterKey(parameter.getKey());

        result.setRoleSpecificUserPrices(PricedProductRoleAssembler
                .toVOPricedProductRoles(
                        pricedParam.getRoleSpecificUserPrices(), facade));

        result.setSteppedPrices(SteppedPriceAssembler
                .toVOSteppedPrices(pricedParam.getSteppedPrices()));

        updateValueObject(result, pricedParam);
        return result;
    }

    public static PricedParameter toPricedParameter(VOPricedParameter voPP)
            throws ValidationException {
        validatePricedParameter(voPP);
        PricedParameter result = new PricedParameter();
        result.setPricePerSubscription(voPP.getPricePerSubscription());
        result.setPricePerUser(voPP.getPricePerUser());
        result.setPricedOptionList(ParameterOptionAssembler.toPricedOptions(
                voPP, result));
        return result;
    }

    public static PricedParameter updatePricedParameter(VOPricedParameter voPP,
            PricedParameter paramToBeUpdated) throws ValidationException,
            ConcurrentModificationException {
        verifyVersionAndKey(paramToBeUpdated, voPP);
        validatePricedParameter(voPP);
        paramToBeUpdated
                .setPricePerSubscription(voPP.getPricePerSubscription());
        paramToBeUpdated.setPricePerUser(voPP.getPricePerUser());
        return paramToBeUpdated;
    }

    static void validatePricedParameter(VOPricedParameter pricedParameter)
            throws ValidationException {
        BLValidator.isNonNegativeNumber(FIELD_NAME_PRICE_PER_SUBSCRIPTION,
                pricedParameter.getPricePerSubscription());
        BLValidator.isValidPriceScale(FIELD_NAME_PRICE_PER_SUBSCRIPTION,
                pricedParameter.getPricePerSubscription());
        BLValidator.isNonNegativeNumber(FIELD_NAME_PRICE_PER_USER,
                pricedParameter.getPricePerUser());
        BLValidator.isValidPriceScale(FIELD_NAME_PRICE_PER_USER,
                pricedParameter.getPricePerUser());
        List<VOSteppedPrice> steppedPriceList = pricedParameter
                .getSteppedPrices();
        if (steppedPriceList != null) {
            SteppedPriceAssembler.validateSteppedPrice(steppedPriceList);
        }
    }

    private static void validateEnumeratedParameterValue(String parameterValue,
            ParameterDefinition parameterDefinition) throws ValidationException {
        for (ParameterOption option : parameterDefinition.getOptionList()) {
            if (option.getOptionId().equals(parameterValue)) {
                return;
            }
        }
        ValidationException vf = new ValidationException(
                ReasonEnum.ENUMERATION, FIELD_NAME_VALUE, new Object[] {
                        parameterDefinition.getParameterId(), parameterValue });
        logger.logWarn(Log4jLogger.SYSTEM_LOG, vf,
                LogMessageIdentifier.WARN_VALIDATION_FAILED);
        throw vf;
    }
}
