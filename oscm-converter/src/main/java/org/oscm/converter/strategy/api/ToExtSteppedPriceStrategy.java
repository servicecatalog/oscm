/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.vo.VOSteppedPrice;

public class ToExtSteppedPriceStrategy extends AbstractConversionStrategy implements ConversionStrategy<SteppedPrice, VOSteppedPrice> {

    @Override
    public VOSteppedPrice convert(SteppedPrice steppedPrice) {
        if (steppedPrice == null) {
            return null;
        }
        VOSteppedPrice voSteppedPrice = new VOSteppedPrice();

        voSteppedPrice.setKey(steppedPrice.getKey());
        voSteppedPrice.setVersion(steppedPrice.getVersion());
        voSteppedPrice.setLimit(steppedPrice.getLimit());
        voSteppedPrice.setPrice(steppedPrice.getPrice());

        return voSteppedPrice;
    }

}
