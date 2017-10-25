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
import java.util.List;

import org.oscm.accountservice.assembler.PaymentTypeAssembler;
import org.oscm.domobjects.PSP;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPSP;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Assembler for the PSP entity.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PSPAssembler extends BaseAssembler {

    private static final String FIELD_NAME_DISTINGUISHED_NAME = "distinguishedName";
    private static final String FIELD_NAME_IDENTIFIER = "identifier";
    private static final String FIELD_NAME_WSDL_URL = "wsdlUrl";

    public static PSP updatePSP(VOPSP vopsp, PSP psp)
            throws ConcurrentModificationException, ValidationException {
        validate(vopsp);
        verifyVersionAndKey(psp, vopsp);
        copyAttributes(psp, vopsp);
        return psp;
    }

    private static void copyAttributes(PSP psp, VOPSP vopsp) {
        psp.setIdentifier(vopsp.getId());
        psp.setWsdlUrl(vopsp.getWsdlUrl());
        psp.setDistinguishedName(vopsp.getDistinguishedName());
    }

    private static void validate(VOPSP vopsp) throws ValidationException {
        BLValidator.isId(FIELD_NAME_IDENTIFIER, vopsp.getId(), true);
        BLValidator.isUrl(FIELD_NAME_WSDL_URL, vopsp.getWsdlUrl(), true);
        BLValidator.isDN(FIELD_NAME_DISTINGUISHED_NAME,
                vopsp.getDistinguishedName(), false);
    }

    public static VOPSP toVo(PSP psp, LocalizerFacade lf) {
        VOPSP result = new VOPSP();
        result.setId(psp.getIdentifier());
        result.setWsdlUrl(psp.getWsdlUrl());
        result.setDistinguishedName(psp.getDistinguishedName());
        updateValueObject(result, psp);
        result.setPspSettings(PSPSettingAssembler.toVoPspSettings(psp
                .getSettings()));
        result.setPaymentTypes(new ArrayList<VOPaymentType>(
                PaymentTypeAssembler.toVOPaymentTypes(psp.getPaymentTypes(), lf)));
        return result;
    }

    public static List<VOPSP> toVos(List<PSP> psps, LocalizerFacade lf) {
        List<VOPSP> result = new ArrayList<VOPSP>();
        for (PSP psp : psps) {
            result.add(toVo(psp, lf));
        }
        return result;
    }

}
