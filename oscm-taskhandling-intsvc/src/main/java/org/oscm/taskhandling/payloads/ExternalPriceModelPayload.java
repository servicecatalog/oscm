/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.02.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

import java.util.Map;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.pricemodel.service.PriceModel;

public class ExternalPriceModelPayload implements TaskPayload {

    private static final long serialVersionUID = -6203176274561224575L;

    PriceModel priceModel;

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    @Override
    public String getInfo() {
        Map<ContextKey, ContextValue<?>> context = priceModel.getContext();
        ContextValue<?> contextValue = context.get(ContextKey.SUBSCRIPTION_ID);
        String subscriptionId = (String) contextValue.getValue();
        return subscriptionId;
    }

}
