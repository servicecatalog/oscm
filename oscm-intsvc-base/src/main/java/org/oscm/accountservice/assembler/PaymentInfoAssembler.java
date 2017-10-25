/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: walker
 *                                                                              
 *  Creation Date: 18.03.2011                                                      
 *                                                                              
 *  Completion Time: 21.03.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.accountservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPaymentInfo;

public class PaymentInfoAssembler extends BaseAssembler {

    private static final int PAYMENTTYPE_INVOICE = 3;

    /**
     * Converts a paymentInfo domain object to a value object. Use this method
     * if the payment info is not connected to a subscription. This scenario
     * happens e.g. if the user wants to subscribe to a service. For the the
     * subscribeToService call it is required to pass the payment information
     * for chargeable services. So it needs to be created previously and is not
     * connected with any subscription.
     * 
     * @param paymentInfo
     *            the domain object for which the VO should be created. If
     *            <code>null</code> will be passed, <code>null</code> will be
     *            returned.
     * @return the VO for the paymentInfo domain object.
     */
    public static VOPaymentInfo toVOPaymentInfo(PaymentInfo paymentInfo,
            LocalizerFacade localizerFacade) {
        if (paymentInfo == null) {
            return null;
        }
        VOPaymentInfo voPaymentInfo = new VOPaymentInfo();
        PaymentType paymentType = paymentInfo.getPaymentType();
        voPaymentInfo.setPaymentType(PaymentTypeAssembler.toVOPaymentType(
                paymentType, localizerFacade));
        if (paymentInfo.getVersion() == 0 && paymentType != null
                && paymentType.getKey() == PAYMENTTYPE_INVOICE) {
            voPaymentInfo.setId(localizerFacade.getText(PAYMENTTYPE_INVOICE,
                    LocalizedObjectTypes.PAYMENT_TYPE_NAME));
        } else {
            voPaymentInfo.setId(paymentInfo.getPaymentInfoId());
        }
        voPaymentInfo.setAccountNumber(paymentInfo.getAccountNumber());
        voPaymentInfo.setProviderName(paymentInfo.getProviderName());
        updateValueObject(voPaymentInfo, paymentInfo);
        return voPaymentInfo;
    }

    public static PaymentInfo updatePaymentInfo(PaymentInfo toUpdate,
            VOPaymentInfo vo) throws ValidationException {
        validate(vo);
        // we must not update account and provider as these values are read-only
        // for the VO
        toUpdate.setPaymentInfoId(vo.getId());
        return toUpdate;
    }

    private static void validate(VOPaymentInfo vo) throws ValidationException {
        BLValidator.isId("id", vo.getId(), true);
    }

    public static List<VOPaymentInfo> toVOPaymentInfos(
            List<PaymentInfo> paymentInfos, LocalizerFacade localizerFacade) {
        List<VOPaymentInfo> result = new ArrayList<VOPaymentInfo>();
        if (paymentInfos != null) {
            for (PaymentInfo pi : paymentInfos) {
                result.add(toVOPaymentInfo(pi, localizerFacade));
            }
        }
        return result;
    }
}
