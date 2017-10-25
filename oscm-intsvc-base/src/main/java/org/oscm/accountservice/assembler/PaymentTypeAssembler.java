/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPaymentType;

/**
 * @author weiser
 * 
 */
public class PaymentTypeAssembler extends BaseAssembler {

    private static final String FIELD_NAME_PAYMENT_TYPE_ID = "paymentTypeId";

    /**
     * Converts the PaymentType domain to the value object. If <code>null</code>
     * is passed in, <code>null</code> will be returned.
     * 
     * @param paymentType
     *            the PaymentType domain object to convert
     * @return the resulting value object or <code>null</code>
     */
    public static VOPaymentType toVOPaymentType(PaymentType paymentType,
            LocalizerFacade localizerFacade) {
        if (paymentType == null) {
            return null;
        }
        VOPaymentType voPaymentType = new VOPaymentType();
        voPaymentType.setPaymentTypeId(paymentType.getPaymentTypeId());
        voPaymentType.setCollectionType(paymentType.getCollectionType());
        voPaymentType.setName(localizerFacade.getText(paymentType.getKey(),
                LocalizedObjectTypes.PAYMENT_TYPE_NAME));
        updateValueObject(voPaymentType, paymentType);
        return voPaymentType;
    }

    /**
     * Converts the PaymentType value to the domain object. If <code>null</code>
     * is passed in, <code>null</code> will be returned.
     * 
     * @param voPaymentType
     *            the PaymentType value object to convert
     * @return the resulting domain object or <code>null</code>
     * @throws ValidationException
     *             Thrown in case the payment type could not be validated.
     */
    public static PaymentType toPaymentType(VOPaymentType voPaymentType)
            throws ValidationException {
        if (voPaymentType == null) {
            return null;
        }
        BLValidator.isId(FIELD_NAME_PAYMENT_TYPE_ID,
                voPaymentType.getPaymentTypeId(), true);
        PaymentType paymentType = new PaymentType();
        copyAttributes(paymentType, voPaymentType);
        return paymentType;
    }

    private static void copyAttributes(PaymentType paymentType,
            VOPaymentType voPaymentType) {
        paymentType.setKey(voPaymentType.getKey());
        paymentType.setPaymentTypeId(voPaymentType.getPaymentTypeId());
        paymentType.setCollectionType(voPaymentType.getCollectionType());
    }

    private static void validate(VOPaymentType vo) throws ValidationException {
        BLValidator.isId("paymentTypeId", vo.getPaymentTypeId(), true);
    }

    public static PaymentType updatePaymentType(VOPaymentType vopt,
            PaymentType pt) throws ConcurrentModificationException,
            ValidationException {
        validate(vopt);
        verifyVersionAndKey(pt, vopt);
        copyAttributes(pt, vopt);
        return pt;
    }

    /**
     * Converts a list of {@link PaymentType}s into a set of
     * {@link VOPaymentType}s.
     * 
     * @param types
     *            the payment type list to convert
     * @return the result set
     */
    public static Set<VOPaymentType> toVOPaymentTypes(List<PaymentType> types,
            LocalizerFacade localizerFacade) {
        Set<VOPaymentType> result = new HashSet<VOPaymentType>();
        if (types != null) {
            for (PaymentType type : types) {
                result.add(toVOPaymentType(type, localizerFacade));
            }
        }
        return result;
    }
}
