/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.UUID;

import org.junit.Test;
import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueString;
import org.oscm.billing.external.pricemodel.service.PriceModel;

public class ExternalPriceModelPayloadTest {

    private static UUID UUID = new UUID(0, 10000);
    private static String SUBSCRIPTION_ID = "Trial Subscription";
    private static String TENANT_ID = "89407ff7";

    private PriceModel preparePriceModel() {
        PriceModel priceModel = new PriceModel(UUID);

        HashMap<ContextKey, ContextValue<?>> context = new HashMap<ContextKey, ContextValue<?>>();
        context.put(ContextKey.SUBSCRIPTION_ID, new ContextValueString(
                SUBSCRIPTION_ID));
        context.put(ContextKey.TENANT_ID, new ContextValueString(TENANT_ID));
        priceModel.setContext(context);

        return priceModel;
    }

    @Test
    public void testGetInfo() {
        ExternalPriceModelPayload payload = new ExternalPriceModelPayload();
        payload.setPriceModel(preparePriceModel());

        assertEquals(SUBSCRIPTION_ID, payload.getInfo());
    }

}
