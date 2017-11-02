/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                        
 *                                                                              
 *  Creation Date: May 18, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.oscm.ui.common.SteppedPriceComparator;
import org.oscm.ui.common.VOFinder;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSteppedPrice;

import static org.oscm.ui.model.UdaRow.HIDDEN_PWD;

/**
 * ParameterRow with prices
 * 
 */
public class PricedParameterRow extends ParameterRow implements Serializable {

    private static final long serialVersionUID = 1L;
    private VOPricedParameter pricedParameter;
    private VOPricedOption pricedOption;
    private VOSteppedPrice steppedPrice;
    private boolean selected;

    // Do not remove the code. Code is commented till billing will be ready.
    public PricedParameterRow() {

    }

    public static List<PricedParameterRow> createPricedParameterRowListForService(
            VOService service) {
        return createPricedParameterRowList(service, true, true, true, true,
                false);
    }

    public static List<PricedParameterRow> createPricedParameterRowListForSubscription(
            VOService service) {
        return createPricedParameterRowList(service, true, true, true, false,
                false);
    }

    public static List<PricedParameterRow> createPricedParameterRowListForPriceModelRoles(
            VOService service) {
        return createPricedParameterRowList(service, true, false, false, true,
                false);
    }

    public static List<PricedParameterRow> createPricedParameterRowListForPriceModel(
            VOService service) {
        return createPricedParameterRowList(service, true, true, false, true,
                false);
    }

    /**
     * Create a list with PricedParameterRow objects for the given service.
     * 
     * @param service
     *            the service for which the list is created
     * @param onlyConfigurableParameter
     *            if set to true the list contains only objects for configurable
     *            parameters
     * @param createSteppedPrices
     * @param includeNonPriceableParams
     *            Indicates whether parameters should be contained that cannot
     *            be priced or not.
     * 
     * @param initDefault
     *            give <code>true</code> if the default value should be passed
     *            to the parameter otherwise <code>false</code>
     * @param includeNonConfigurableOneTimeParams
     *            Pass <code>true</code> if the non-configurable parameter, with
     *            modification type ONE_TIME should be included, regardless of
     *            the value passed in <code>onlyConfigurableParameter</code>.
     * 
     * @returns the value object from the list with the requested key or null.
     */
    public static List<PricedParameterRow> createPricedParameterRowList(
            VOService service, boolean onlyConfigurableParameter,
            boolean createSteppedPrices, boolean includeNonPriceableParams,
            boolean initDefault, boolean includeNonConfigurableOneTimeParams) {
        List<PricedParameterRow> result = new ArrayList<PricedParameterRow>();
        if (service == null || service.getParameters() == null) {
            return result;
        }

        for (VOParameter param : service.getParameters()) {

            if (!param.isConfigurable()) {
                // Omit non-configurable one time parameters if required
                // (they must be considered for upgrade but not displayed)
                if (ParameterModificationType.ONE_TIME.equals(param
                        .getParameterDefinition().getModificationType())) {
                    if (!includeNonConfigurableOneTimeParams)
                        continue;
                } else if (onlyConfigurableParameter)
                    continue;
            }
            // if based on a string type parameter definition, don't list the
            // parameter, if it should be excluded
            if (!includeNonPriceableParams
                    && param.getParameterDefinition().getValueType() == ParameterValueType.STRING) {
                continue;
            }
            List<VOPricedParameter> selectedParameters = null;
            if (service.getPriceModel() != null) {
                selectedParameters = service.getPriceModel()
                        .getSelectedParameters();
            }
            VOPricedParameter pricedParam = VOFinder.findPricedParameter(
                    selectedParameters, param);
            if (pricedParam == null) {
                pricedParam = new VOPricedParameter(
                        param.getParameterDefinition());
            }
            PricedParameterRow row = new PricedParameterRow(param, null,
                    pricedParam, null, initDefault);
            result.add(row);

            if (createSteppedPrices) {
                // process stepped prices
                List<VOSteppedPrice> list = pricedParam.getSteppedPrices();
                Collections.sort(list, new SteppedPriceComparator());
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    if (i > 0) {
                        row = new PricedParameterRow(param, null, pricedParam,
                                null, initDefault);
                        result.add(row);
                    }
                    row.setSteppedPrice(list.get(i));
                }
            }
            // process enumerations
            if (param.getParameterDefinition().getValueType() == ParameterValueType.ENUMERATION) {
                List<VOPricedOption> pricedOptionList = null;
                pricedOptionList = pricedParam.getPricedOptions();
                List<VOParameterOption> options = param
                        .getParameterDefinition().getParameterOptions();
                int optionIndex = 0;
                for (VOParameterOption option : options) {
                    VOPricedOption pricedOption = VOFinder.findPricedOption(
                            pricedOptionList, option);
                    if (pricedOption == null) {
                        pricedOption = new VOPricedOption();
                        pricedOption.setParameterOptionKey(option.getKey());
                        if (pricedOptionList != null) {
                            pricedOptionList.add(pricedOption);
                        }
                    }
                    PricedParameterRow optionRow = new PricedParameterRow(
                            param, option, pricedParam, pricedOption,
                            initDefault);
                    optionRow.setOptionIndex(optionIndex);
                    optionIndex++;
                    result.add(optionRow);
                }
            }
        }
        return result;
    }

    public PricedParameterRow(VOParameter parameter,
            VOParameterOption parameterOption,
            VOPricedParameter pricedParameter, VOPricedOption pricedOption) {
        this(parameter, parameterOption, pricedParameter, pricedOption, true);
    }

    public PricedParameterRow(VOParameter parameter,
            VOParameterOption parameterOption,
            VOPricedParameter pricedParameter, VOPricedOption pricedOption,
            boolean initDefault) {
        super(parameter, parameterOption, initDefault);
        this.pricedParameter = pricedParameter;
        this.pricedOption = pricedOption;
    }

    @Override
    public String getDescription() {
        if (isEmptyOrFirstSteppedPrice()) {
            return super.getDescription();
        }
        return null;
    }

    public VOPricedParameter getPricedParameter() {
        return pricedParameter;
    }

    // Do not remove the code. Code is commented till billing will be ready.
    public void setPricedParameter(VOPricedParameter parameter) {
        this.pricedParameter = parameter;
    }

    public VOPricedOption getPricedOption() {
        return pricedOption;
    }

    public VOSteppedPrice getSteppedPrice() {
        return steppedPrice;
    }

    public void setSteppedPrice(VOSteppedPrice steppedPrice) {
        this.steppedPrice = steppedPrice;
    }

    public Long getLimit() {
        return steppedPrice.getLimit();
    }

    public void setLimit(Long limit) {
        steppedPrice.setLimit(limit);
    }

    public boolean isFirstSteppedPrice() {
        if (steppedPrice == null) {
            return false;
        }
        return steppedPrice == getPricedParameter().getSteppedPrices().get(0);
    }

    public boolean isEmptyOrFirstSteppedPrice() {
        if (steppedPrice == null) {
            return true;
        }
        return steppedPrice == getPricedParameter().getSteppedPrices().get(0);
    }

    public boolean isLastSteppedPrice() {
        if (steppedPrice == null) {
            return false;
        }
        return steppedPrice == getPricedParameter().getSteppedPrices().get(
                getPricedParameter().getSteppedPrices().size() - 1);
    }

    // Do not remove the code. Code is commented till billing will be ready.
    public void setPricedOption(VOPricedOption option) {
        this.pricedOption = option;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void rewriteEncryptedValues() {
        if (!this.isPasswordType()) {
            return;
        }
        if (this.getPasswordValueToStore() == null
                || !StringUtils.equals(HIDDEN_PWD,
                        this.getPasswordValueToStore())) {
            this.getParameter().setValue(this.getPasswordValueToStore());
        }
    }

    public void initPasswordValueToStore() {
        if (!this.isPasswordType()) {
            return;
        }
        if (StringUtils.isNotBlank(this.getParameter().getValue())) {
            this.setPasswordValueToStore(HIDDEN_PWD);
        } else {
            this.setPasswordValueToStore("");
        }
    }

}
