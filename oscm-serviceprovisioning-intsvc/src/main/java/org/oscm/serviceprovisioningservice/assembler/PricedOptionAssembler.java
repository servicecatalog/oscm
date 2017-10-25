/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPricedOption;

/**
 * Assembler for the priced options.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PricedOptionAssembler extends BaseAssembler {

    public static PricedOption toPricedOption(VOPricedOption voPO,
            PricedParameter pricedParameter) {
        PricedOption po = new PricedOption();
        copyAttributes(po, voPO);
        po.setPricedParameter(pricedParameter);
        return po;
    }

    public static VOPricedOption toVOPricedOption(PricedOption current,
            LocalizerFacade facade) {
        VOPricedOption option = new VOPricedOption();
        option.setPricePerUser(current.getPricePerUser());
        option.setPricePerSubscription(current.getPricePerSubscription());
        option.setParameterOptionKey(current.getParameterOptionKey());
        option.setRoleSpecificUserPrices(PricedProductRoleAssembler
                .toVOPricedProductRoles(current.getRoleSpecificUserPrices(),
                        facade));

        updateValueObject(option, current);
        return option;
    }

    public static PricedOption updatePricedOption(PricedOption optionToUpdate,
            VOPricedOption template) throws ConcurrentModificationException,
            ValidationException {
        verifyVersionAndKey(optionToUpdate, template);
        validatePricedOption(optionToUpdate);
        copyAttributes(optionToUpdate, template);
        return optionToUpdate;
    }

    static void validatePricedOption(PricedOption optionToUpdate)
            throws ValidationException {
        BLValidator.isValidPriceScale("pricePerUser", optionToUpdate.getPricePerUser());
        BLValidator.isValidPriceScale("pricePerSubscription",
                optionToUpdate.getPricePerSubscription());
    }

    private static void copyAttributes(PricedOption optionToUpdate,
            VOPricedOption template) {
        optionToUpdate.setPricePerSubscription(template
                .getPricePerSubscription());
        optionToUpdate.setPricePerUser(template.getPricePerUser());
        optionToUpdate.setParameterOptionKey(template.getParameterOptionKey());
    }

}
