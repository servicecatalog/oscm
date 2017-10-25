/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import java.util.List;

import org.oscm.converter.api.Converter;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.PricingPeriod;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOPricedEvent;
import org.oscm.vo.VOPricedParameter;
import org.oscm.vo.VOPricedRole;
import org.oscm.vo.VOSteppedPrice;

public class ToExtPriceModelStrategy extends AbstractConversionStrategy
        implements ConversionStrategy<PriceModel, VOPriceModel> {

    @Override
    public VOPriceModel convert(PriceModel priceModel) {
        if (priceModel == null) {
            return null;
        }
        VOPriceModel voPriceModel = new VOPriceModel();

        voPriceModel.setKey(priceModel.getKey());
        voPriceModel.setType(PriceModelType
                .valueOf(priceModel.getType().name()));
        voPriceModel.setVersion(priceModel.getVersion());
        List<PricedEvent> consideredEvents = priceModel.getConsideredEvents();
        List<VOPricedEvent> voPricedEvents = Converter.convertList(
                consideredEvents, PricedEvent.class, VOPricedEvent.class,
                getDataService());
        voPriceModel.setConsideredEvents(voPricedEvents);
        if (priceModel.getCurrency() != null) {
            voPriceModel.setCurrencyISOCode(priceModel.getCurrency()
                    .getCurrencyISOCode());
        }
        voPriceModel.setFreePeriod(priceModel.getFreePeriod());
        voPriceModel.setOneTimeFee(priceModel.getOneTimeFee());
        if (priceModel.getPeriod() != null) {
            voPriceModel.setPeriod(PricingPeriod.valueOf(priceModel.getPeriod()
                    .name()));
        }
        voPriceModel.setPricePerPeriod(priceModel.getPricePerPeriod());
        voPriceModel.setPricePerUserAssignment(priceModel
                .getPricePerUserAssignment());
        List<VOPricedRole> voPricedRoles = Converter.convertList(
                priceModel.getRoleSpecificUserPrices(),
                PricedProductRole.class, VOPricedRole.class, getDataService());
        voPriceModel.setRoleSpecificUserPrices(voPricedRoles);
        List<VOPricedParameter> voPricedParameters = Converter.convertList(
                priceModel.getSelectedParameters(), PricedParameter.class,
                VOPricedParameter.class, getDataService());
        voPriceModel.setSelectedParameters(voPricedParameters);
        List<VOSteppedPrice> steppedPrices = Converter.convertList(
                priceModel.getSteppedPrices(), SteppedPrice.class,
                VOSteppedPrice.class, getDataService());
        voPriceModel.setSteppedPrices(steppedPrices);

        final List<LocalizedObjectTypes> localizedObjectTypes = priceModel
                .getLocalizedObjectTypes();
        final String locale = getDataService().getCurrentUser().getLocale();
        final List<LocalizedResource> localizedResources = getLocalizedResource(
                localizedObjectTypes, Long.valueOf(priceModel.getKey()), locale);

        for (LocalizedResource resource : localizedResources) {
            if (resource.getObjectType().equals(
                    LocalizedObjectTypes.PRICEMODEL_DESCRIPTION)) {
                voPriceModel.setDescription(resource.getValue());
            } else if (resource.getObjectType().equals(
                    LocalizedObjectTypes.PRICEMODEL_LICENSE)) {
                voPriceModel.setLicense(resource.getValue());
            }

        }

        return voPriceModel;

    }

}
