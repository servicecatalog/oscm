/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import java.util.List;

import org.oscm.converter.api.Converter;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.vo.VOPricedEvent;
import org.oscm.vo.VOSteppedPrice;

public class ToExtPricedEventStrategy extends AbstractConversionStrategy implements ConversionStrategy<PricedEvent, VOPricedEvent> {

    @Override
    public VOPricedEvent convert(PricedEvent pricedEvent) {
        if (pricedEvent == null) {
            return null;
        }

        VOPricedEvent voPricedEvent = new VOPricedEvent();

        voPricedEvent.setKey(pricedEvent.getKey());
        voPricedEvent.setVersion(pricedEvent.getVersion());
        List<SteppedPrice> steppedPrices = pricedEvent.getSteppedPrices();
        List<VOSteppedPrice> voSteppedPrices = Converter.convertList(steppedPrices, SteppedPrice.class, VOSteppedPrice.class, getDataService());
        voPricedEvent.setSteppedPrices(voSteppedPrices);
        voPricedEvent.setEventPrice(pricedEvent.getEventPrice());

        return voPricedEvent;
    }

}
