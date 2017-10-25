/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jan 11, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;

/**
 * Assembler to handle parameter option and concrete parameter settings.
 * 
 * @author Pravi
 * 
 */
public class ParameterOptionAssembler extends BaseAssembler {

    public static List<VOParameterOption> toVOParameterOptions(
            List<ParameterOption> parameterOptions, LocalizerFacade facade,
            String paramDefID) {

        List<VOParameterOption> options = new ArrayList<VOParameterOption>();
        if (parameterOptions != null) {
            for (ParameterOption option : parameterOptions) {
                VOParameterOption voOption = toVOParameterOption(option,
                        facade, paramDefID);
                options.add(voOption);
            }
        }
        return options;
    }

    private static VOParameterOption toVOParameterOption(
            ParameterOption option, LocalizerFacade facade, String paramDefID) {
        String optionId = option.getOptionId();
        String optionDescription = facade.getText(option.getKey(),
                LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
        VOParameterOption voParameterOption = new VOParameterOption(optionId,
                optionDescription, paramDefID);
        updateValueObject(voParameterOption, option);
        return voParameterOption;

    }

    public static List<ParameterOption> toParameterOptions(
            List<VOParameterOption> vos, ParameterDefinition paramDef) {
        List<ParameterOption> options = new ArrayList<ParameterOption>();
        for (VOParameterOption vo : vos) {
            options.add(toParameterOption(vo, paramDef));
        }
        return options;
    }

    private static ParameterOption toParameterOption(VOParameterOption vo,
            ParameterDefinition parameterDefinition) {
        ParameterOption option = new ParameterOption();
        option.setOptionId(vo.getOptionId());
        option.setParameterDefinition(parameterDefinition);
        return option;
    }

    public static List<VOPricedOption> toVOPricedOptions(
            PricedParameter pricedParam, LocalizerFacade facade) {
        List<VOPricedOption> options = new ArrayList<VOPricedOption>();
        for (PricedOption po : pricedParam.getPricedOptionList()) {
            options.add(PricedOptionAssembler.toVOPricedOption(po, facade));
        }
        return options;
    }

    public static List<PricedOption> toPricedOptions(VOPricedParameter vopp,
            PricedParameter pricedParameter) {
        List<PricedOption> options = new ArrayList<PricedOption>();
        for (VOPricedOption voPO : vopp.getPricedOptions()) {
            options.add(PricedOptionAssembler.toPricedOption(voPO,
                    pricedParameter));
        }
        return options;
    }

    /**
     * The method delegates the call to the
     * <code>updatePriceOption(PricedParameter parameterToSet,
            PricedOption existingPO)</code> for each priceOption.
     * 
     * @param pricedParameter
     * @param ppWithNewValue
     */
    public static void updatePriceOptions(PricedParameter pricedParameter,
            PricedParameter ppWithNewValue) {
        for (PricedOption existingPO : pricedParameter.getPricedOptionList()) {
            updatePriceOption(ppWithNewValue, existingPO);
        }
    }

    /**
     * The existing priced options are updated with the new prices
     * 
     * @param ppWithNewValue
     * @param existingPO
     */
    private static void updatePriceOption(PricedParameter ppWithNewValue,
            PricedOption existingPO) {
        for (PricedOption newPo : ppWithNewValue.getPricedOptionList()) {
            if (newPo.getParameterOptionKey() == existingPO
                    .getParameterOptionKey()) {
                existingPO.setPricePerSubscription(newPo
                        .getPricePerSubscription());
                existingPO.setPricePerUser(newPo.getPricePerUser());
                existingPO.setRoleSpecificUserPrices(newPo
                        .getRoleSpecificUserPrices());
                break;
            }
        }
    }

}
