/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 13.07.2010.                                                      
 *                                                                              
 *  Completion Time: 13.07.2010.                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.domobjects.SteppedPrice;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Assembler for stepped price.
 * 
 * @author Aleh Khomich.
 * 
 */
public class SteppedPriceAssembler extends BaseAssembler {

    public static final String FIELD_NAME_PRICE = "price";
    
    public static final String FIELD_NAME_LIMIT = "limit";

    /**
     * Converts a stepped price domain object into the corresponding value
     * object representation.
     * 
     * @param steppedPrice
     *            The domain object to be assembled.
     * @return The value object representation of the priced product role.
     */
    public static VOSteppedPrice toVOSteppedPrice(SteppedPrice steppedPrice) {
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();

        updateValueObject(voSteppedPrice, steppedPrice);
        voSteppedPrice.setLimit(steppedPrice.getLimit());
        voSteppedPrice.setPrice(steppedPrice.getPrice());
        return voSteppedPrice;
    }

    /**
     * Converts a list of stepped prices domain objects into the corresponding
     * value object representation.
     * 
     * @param steppedPriceList
     *            The domain objects to be assembled.
     * @return The value object representation of the stepped prices.
     */
    public static List<VOSteppedPrice> toVOSteppedPrices(
            List<SteppedPrice> steppedPriceList) {
        ArrayList<VOSteppedPrice> voSteppedPriceList = new ArrayList<VOSteppedPrice>();
        for (SteppedPrice currentSteppedPrice : steppedPriceList) {
            VOSteppedPrice voSteppedPrice = toVOSteppedPrice(currentSteppedPrice);
            voSteppedPriceList.add(voSteppedPrice);
        }
        return voSteppedPriceList;
    }

    /**
     * Validates the value object and returns a domain object representation of
     * it. Only the primitive typed fields will be considered.
     * 
     * @param voSteppedPrice
     *            VO of stepped price to be converted to a domain object.
     * @return The domain object representation of the stepped price.
     * @throws ValidationException
     *             Thrown in case a negative price is configured.
     */
    public static SteppedPrice toSteppedPrice(VOSteppedPrice voSteppedPrice)
            throws ValidationException {
        SteppedPrice steppedPrice = new SteppedPrice();
        validateSteppedPrice(voSteppedPrice);
        copyAttributes(voSteppedPrice, steppedPrice);

        return steppedPrice;
    }

    /**
     * Validation of stepped price list.
     * 
     * @param steppedPriceList
     *            VO stepped price list for validation.
     * @throws ValidationException
     *             On validation error.
     */
    public static void validateSteppedPrice(
            List<VOSteppedPrice> steppedPriceList) throws ValidationException {
        if (steppedPriceList != null) {
            Set<Long> limitSet = new HashSet<Long>();
            for (VOSteppedPrice step : steppedPriceList) {
                validateSteppedPrice(step);
                Long limit = step.getLimit();
                if (limitSet.contains(limit)) {
                    String param = "";
                    if (step.getLimit() != null) {
                        param = String.valueOf(step.getLimit().longValue());
                    }
                    throw new ValidationException(
                            ReasonEnum.STEPPED_PRICING_DUPLICATE_LIMIT,
                            FIELD_NAME_LIMIT, new Object[] { param });
                }
                limitSet.add(limit);
            }
        }
    }

    /**
     * Validation of stepped price.
     * 
     * @param steppedPrice
     *            VO stepped price for validation.
     * @throws ValidationException
     *             On validation error.
     */
    public static void validateSteppedPrice(VOSteppedPrice steppedPrice)
            throws ValidationException {
        if (steppedPrice == null) {
            throw new IllegalArgumentException("Input value is null");
        }

        if (steppedPrice.getLimit() != null) {
            BLValidator.isPositiveAndNonZeroNumber(FIELD_NAME_LIMIT,
                    steppedPrice.getLimit().longValue());
        }

        BLValidator.isNonNegativeNumber(FIELD_NAME_PRICE,
                steppedPrice.getPrice());
        BLValidator.isValidPriceScale(FIELD_NAME_PRICE, steppedPrice.getPrice());
    }

    /**
     * Validates the value object, verifies the version and returns the updated
     * domain object representation of it. Only the primitive typed fields will
     * be considered.
     * 
     * @param pricedProductRole
     *            The role to be converted to a domain object.
     * @param doPricedProductRole
     *            The role as domain object that should be updated.
     * @return The domain object representation of the priced product role.
     * @throws ValidationException
     *             Thrown in case a negative price is configured.
     * @throws ConcurrentModificationException
     *             Thrown in case the passed value object's version does not
     *             match the domain object.
     */
    public static SteppedPrice updateSteppedPrice(
            VOSteppedPrice voSteppedPrice, SteppedPrice steppedPrice)
            throws ValidationException, ConcurrentModificationException {
        verifyVersionAndKey(steppedPrice, voSteppedPrice);
        validateSteppedPrice(voSteppedPrice);
        copyAttributes(voSteppedPrice, steppedPrice);
        return steppedPrice;
    }

    /**
     * Copies the attributes from the value object to the domain object.
     * 
     * @param voSteppedPrice
     *            The value object to copy the attributes from.
     * @param steppedPrice
     *            The domain objects which attributes will be updated.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown in case a negative price is configured.
     * @throws ConcurrentModificationException
     *             Thrown in case the passed value object's version does not
     *             match the domain object.
     */
    public static SteppedPrice copyAttributes(VOSteppedPrice voSteppedPrice,
            SteppedPrice steppedPrice) {
        steppedPrice.setLimit(voSteppedPrice.getLimit());
        steppedPrice.setPrice(voSteppedPrice.getPrice());
        return steppedPrice;
    }

}
