/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;

/**
 * @author iversen
 * 
 */
public class ExternalPriceModelModelTest {

    private ExternalPriceModelModel model;

    @Before
    public void beforeClass() {
        model = spy(new ExternalPriceModelModel());

    }

    @Test
    public void getContentAsJSON() {
        // given
        String priceModel = "{ \"plan name\": \"super-monthly\", \"phases\": [ { \"type\": \"TRIAL\", \"prices\": [] }, { \"type\": \"EVERGREEN\", \"prices\": [ { \"currency\": \"GBP\", \"value\": 750 }, { \"currency\": \"USD\", \"value\": 1000 } ] } ] }";
        PriceModelContent priceModelContent = new PriceModelContent(
                MediaType.APPLICATION_JSON, priceModel.getBytes());
        model.setSelectedPriceModelContent(priceModelContent);

        // when
        String result = model.getContentAsJSON();

        // then
        assertEquals(priceModel, result);
    }
}
