/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 14.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.math.BigDecimal;
import java.util.List;

import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Assembler to handle price models.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PriceModelAssembler extends BaseAssembler {

    public static final String FIELD_NAME_PERIOD = "period";

    public static final String FIELD_NAME_FREE_PERIOD = "freePeriod";

    public static final String FIELD_NAME_PRICE_PERIOD = "pricePerPeriod";

    public static final String FIELD_NAME_PRICE_USERASSIGNMENT = "pricePerUserAssignment";

    public static final String FIELD_NAME_ONE_TIME_FEE = "oneTimeFee";

    public static VOPriceModel toVOPriceModel(PriceModel priceModel,
            LocalizerFacade facade) {
        return toVOPriceModel(priceModel, facade, PerformanceHint.ALL_FIELDS);

    }

    /**
     * Converts a price model domain object to a corresponding value object.
     * 
     * @param priceModel
     *            The price model to be converted.
     * @param facade
     *            The localizer facade object.
     * @return The value object representation of the price model.
     */
    public static VOPriceModel toVOPriceModel(PriceModel priceModel,
            LocalizerFacade facade, PerformanceHint scope) {
        if (priceModel == null) {
            return null;
        }
        VOPriceModel voPM = new VOPriceModel();
        switch (scope) {
        case ONLY_FIELDS_FOR_LISTINGS:
            fillBaseFields(priceModel, voPM);
            break;
        default:
            fillAllFields(priceModel, facade, voPM);
        }
        updateValueObject(voPM, priceModel);
        return voPM;
    }

    /**
     * Sets the most important attributes in the transfer object. Usually, these
     * fields are required for listings.
     */
    private static void fillBaseFields(PriceModel priceModel,
            VOPriceModel voPM) {
        voPM.setExternal(priceModel.isExternal());
        voPM.setType(priceModel.getType());
        voPM.setUuid(priceModel.getUuid());
        if (priceModel.isChargeable()) {
            voPM.setPeriod(priceModel.getPeriod());
            voPM.setOneTimeFee(priceModel.getOneTimeFee());
            voPM.setPricePerPeriod(priceModel.getPricePerPeriod());
            final ServiceAccessType authType = priceModel.getProduct()
                    .getTechnicalProduct().getAccessType();
            if (authType == ServiceAccessType.DIRECT) {
                voPM.setPricePerUserAssignment(BigDecimal.ZERO);
            } else {
                voPM.setPricePerUserAssignment(
                        priceModel.getPricePerUserAssignment());
            }
            voPM.setCurrencyISOCode(
                    priceModel.getCurrency().getCurrencyISOCode());
        }
    }

    /**
     * Sets all attributes in the transfer object. Warning: a lot of data must
     * be loaded from the database. This will result in slow performance, if
     * called for multiple domain objects.
     */
    private static void fillAllFields(PriceModel priceModel,
            LocalizerFacade facade, VOPriceModel voPM) {
        fillBaseFields(priceModel, voPM);
        if (priceModel.isChargeable()) {

            voPM.setRoleSpecificUserPrices(
                    PricedProductRoleAssembler.toVOPricedProductRoles(
                            priceModel.getRoleSpecificUserPrices(), facade));

            // stepped pricing information
            voPM.setSteppedPrices(SteppedPriceAssembler
                    .toVOSteppedPrices(priceModel.getSteppedPrices()));

        }

        String license = facade.getText(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_LICENSE);
        voPM.setLicense(license);
        voPM.setConsideredEvents(EventAssembler
                .toVOPricedEvent(priceModel.getConsideredEvents(), facade));
        String description = facade.getText(priceModel.getKey(),
                LocalizedObjectTypes.PRICEMODEL_DESCRIPTION);
        voPM.setDescription(description);
        voPM.setFreePeriod(priceModel.getFreePeriod());
        // for the priced parameter
        voPM.setSelectedParameters(ParameterAssembler.toVOPricedParameters(
                priceModel.getSelectedParameters(), facade));

        // presentation data of the price model
        if (priceModel.isExternal()) {
            LocalizedBillingResource localizedPriceModelPresentation = facade
                    .getLocalizedPriceModelResource(priceModel.getUuid());
            if (localizedPriceModelPresentation != null) {
                voPM.setRelatedSubscription(localizedPriceModelPresentation.getResourceType() == LocalizedBillingResourceType.PRICEMODEL_SUBSCRIPTION);
                voPM.setPresentation(
                        localizedPriceModelPresentation.getValue());
                voPM.setPresentationDataType(
                        localizedPriceModelPresentation.getDataType());
            }
        }
    }

    /**
     * Validates that the prices defined for the price model are all
     * non-negative (if set). The events themselves are not checked.
     * 
     * @param inputValue
     *            The price model to be validated.
     * @throws ValidationException
     *             Thrown in case one of the prices defined for the price model
     *             is negative.
     */
    public static void validatePriceModelSettings(VOPriceModel inputValue)
            throws ValidationException {
        notNegativeAndScaleInRange(FIELD_NAME_PRICE_PERIOD,
                inputValue.getPricePerPeriod());
        notNegativeAndScaleInRange(FIELD_NAME_PRICE_USERASSIGNMENT,
                inputValue.getPricePerUserAssignment());
        notNegativeAndScaleInRange(FIELD_NAME_ONE_TIME_FEE,
                inputValue.getOneTimeFee());
        BLValidator.isNonNegativeNumber(FIELD_NAME_FREE_PERIOD,
                inputValue.getFreePeriod());
        if (inputValue.isChargeable()) {
            BLValidator.isNotNull(FIELD_NAME_PERIOD, inputValue.getPeriod());
        }

        // validate priced product roles for pricemodel. parameter and also
        // options
        List<VOPricedRole> pmRoleSpecificPrices = inputValue
                .getRoleSpecificUserPrices();
        PricedProductRoleAssembler
                .validatePricedProductRoles(pmRoleSpecificPrices);

        for (VOPricedParameter param : inputValue.getSelectedParameters()) {
            PricedProductRoleAssembler.validatePricedProductRoles(
                    param.getRoleSpecificUserPrices());
            for (VOPricedOption option : param.getPricedOptions()) {
                PricedProductRoleAssembler.validatePricedProductRoles(
                        option.getRoleSpecificUserPrices());
            }
        }

        // validate stepped prices
        List<VOSteppedPrice> steppedPriceList = inputValue.getSteppedPrices();
        SteppedPriceAssembler.validateSteppedPrice(steppedPriceList);
    }

    private static void notNegativeAndScaleInRange(String member,
            BigDecimal number) throws ValidationException {
        BLValidator.isNonNegativeNumber(FIELD_NAME_PRICE_PERIOD, number);
        BLValidator.isValidPriceScale(member, number);
    }

    /**
     * Validates that (and only that) no user based prices are set on the price
     * model and children that can have user based prices.
     * 
     * @param priceModel
     *            the {@link VOPriceModel} to validate
     */
    public static void validateForDirectAccess(VOPriceModel priceModel)
            throws ValidationException {
        isZero(FIELD_NAME_PRICE_USERASSIGNMENT,
                priceModel.getPricePerUserAssignment());
        isUserSteppedPricesEmpty(priceModel.getSteppedPrices());
        List<VOPricedParameter> parameters = priceModel.getSelectedParameters();
        for (VOPricedParameter param : parameters) {
            isZero(ParameterAssembler.FIELD_NAME_PRICE_PER_USER,
                    param.getPricePerUser());
            List<VOPricedOption> options = param.getPricedOptions();
            if (options != null) {
                for (VOPricedOption po : options) {
                    isZero(ParameterAssembler.FIELD_NAME_PRICE_PER_USER,
                            po.getPricePerUser());
                }
            }
        }
    }

    private static void isUserSteppedPricesEmpty(
            List<VOSteppedPrice> steppedPrices) throws ValidationException {
        if (steppedPrices != null && !steppedPrices.isEmpty()) {
            throw new ValidationException(ReasonEnum.DIRECT_ACCESS_USER_PRICE,
                    FIELD_NAME_PRICE_USERASSIGNMENT, new Object[0]);
        }
    }

    private static void isZero(String member, BigDecimal price)
            throws ValidationException {
        if (price.compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException(ReasonEnum.DIRECT_ACCESS_USER_PRICE,
                    member, new Object[0]);
        }
    }
}
