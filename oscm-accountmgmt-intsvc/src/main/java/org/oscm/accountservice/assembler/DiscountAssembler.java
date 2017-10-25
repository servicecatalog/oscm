/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 21.11.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import java.math.BigDecimal;

import org.oscm.domobjects.Discount;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VODiscount;

/**
 * Assembler to convert the discount value object to the according domain object
 * and vice versa.
 * 
 * @author Enes Sejfi
 */
public class DiscountAssembler extends BaseAssembler {
    public static final String FIELD_NAME_VALUE = "value";

    public static final String FIELD_NAME_STARTDATE = "startDate";

    public static final BigDecimal MAX_DISCOUNT_VALUE = new BigDecimal("100.00");

    private static final BigDecimal MIN_DISCOUNT_VALUE = BigDecimal.ZERO;

    /**
     * Converts a discount domain object to a VO object.
     * 
     * @param domObj
     *            domain discount object
     * @return VO discount object
     */
    public static VODiscount toVODiscount(Discount domObj) {
        if (domObj == null) {
            return null;
        }
        VODiscount voResult = new VODiscount();
        updateValueObject(voResult, domObj);
        copyToVOAttributes(domObj, voResult);

        return voResult;
    }

    /**
     * Converts a VO discount to a domain object.
     * 
     * @param voObj
     *            VO discount object
     * @return new discount domain object
     * @throws ValidationException
     *             the validation exception
     */
    public static Discount toDiscount(VODiscount voObj)
            throws ValidationException {
        if (voObj == null) {
            return null;
        }

        validate(voObj);
        final Discount domObj = new Discount();
        copyToDomainAttributes(domObj, voObj);
        return domObj;
    }

    /**
     * Creates a discount domain object from the VO discount object.
     * 
     * @param voObj
     *            VO discount object
     * @param domObj
     *            domain discount object
     * @return A domain object representation of the value object.
     * @throws ValidationException
     *             Thrown in case the discount violates discount validation
     *             rules.
     * @throws ConcurrentModificationException
     *             Thrown in case the value object's version does not match the
     *             current domain object's.
     */
    public static Discount updateDiscount(VODiscount voObj, Discount domObj)
            throws ValidationException, ConcurrentModificationException {
        if (domObj.getKey() != 0) {
            verifyVersionAndKey(domObj, voObj);
        }
        validate(voObj);
        copyToDomainAttributes(domObj, voObj);
        return domObj;
    }

    /**
     * Validates the VO discount attributes.
     * 
     * @param voObj
     *            VO discount object
     * @throws ValidationException
     *             A ValidationException is thrown if discount.value is null or
     *             the scale is not right, discount.value is less than 0 and
     *             greater than 100 or discount.startDate > discount.endDate
     */
    static void validate(VODiscount voObj) throws ValidationException {
        BLValidator.isNotNull(FIELD_NAME_VALUE, voObj.getValue());
        BLValidator.isInRange(FIELD_NAME_VALUE, voObj.getValue(),
                MIN_DISCOUNT_VALUE, MAX_DISCOUNT_VALUE);
        BLValidator
                .isEqual(FIELD_NAME_VALUE, voObj.getValue(), BigDecimal.ZERO);
        BLValidator.isValidPriceScale(FIELD_NAME_VALUE, voObj.getValue());
        if (voObj.getStartTime() != null) {
            BLValidator.isInRange(FIELD_NAME_STARTDATE, voObj.getStartTime()
                    .longValue(), voObj.getStartTime(), voObj.getEndTime());
        }
    }

    /**
     * Copies VO attributes to domain object attributes.
     * 
     * @param domObj
     *            domain discount object
     * @param voObj
     *            VO discount object
     */
    private static void copyToDomainAttributes(Discount domObj, VODiscount voObj) {
        domObj.setKey(voObj.getKey());
        domObj.setValue(voObj.getValue());
        domObj.setStartTime(voObj.getStartTime());
        domObj.setEndTime(voObj.getEndTime());
    }

    /**
     * Copies domain object attributes to VO attributes.
     * 
     * @param domObj
     *            domain discount object
     * @param voObj
     *            VO discount object
     */
    private static void copyToVOAttributes(Discount domObj, VODiscount voObj) {
        voObj.setValue(domObj.getValue());
        voObj.setStartTime(domObj.getStartTime());
        voObj.setEndTime(domObj.getEndTime());
    }
}
