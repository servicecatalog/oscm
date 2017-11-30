/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24 lut 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;

/**
 * @author BadziakP
 *
 * Provides actions for testing external price models.
 */
@Ignore
public class ExternalPriceModelTest {


    protected PriceModel createExternalPriceModel(UUID id, Locale locale) {
        PriceModelContent priceModelContent = createPriceModelContent();
        return createPriceModel(priceModelContent, id, locale);
    }

    protected PriceModel createPriceModel(PriceModelContent priceModelContent,
            UUID priceModelUUID, Locale locale) {
        PriceModel priceModel = new PriceModel(priceModelUUID);
        priceModel.put(locale, priceModelContent);
        return priceModel;
    }

    protected PriceModelContent createPriceModelContent() {
        String contentType = MediaType.APPLICATION_JSON;
        String priceModelJson = "PRICES:15";
        String priceTag = "15EUR";
        PriceModelContent priceModelContent = new PriceModelContent(contentType,
                priceModelJson.getBytes(), priceTag);
        return priceModelContent;
    }
}
