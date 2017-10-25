/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.converter.strategy.api;

import org.oscm.converter.api.Converter;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.vo.VOPricedOption;
import org.oscm.vo.VOPricedRole;

public class ToExtPricedOptionStrategy extends AbstractConversionStrategy implements ConversionStrategy<PricedOption, VOPricedOption> {

    @Override
    public VOPricedOption convert(PricedOption pricedOption) {
        if (pricedOption == null) {
            return null;
        }

        VOPricedOption voPricedOption = new VOPricedOption();
        voPricedOption.setKey(pricedOption.getKey());
        voPricedOption.setVersion(pricedOption.getVersion());
        voPricedOption.setParameterOptionKey(pricedOption.getParameterOptionKey());
        voPricedOption.setPricePerSubscription(pricedOption.getPricePerSubscription());
        voPricedOption.setPricePerUser(pricedOption.getPricePerUser());
        voPricedOption.setRoleSpecificUserPrices(Converter.convertList(pricedOption.getRoleSpecificUserPrices(), PricedProductRole.class, VOPricedRole.class));

        return voPricedOption;
    }

}
