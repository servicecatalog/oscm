/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.internal.types.exception.BillingApplicationException;

public class LocalizedBillingResourceAssemblerTest {

    final String INVALID_JSON = "{key";
    final String VALID_JSON = "{\"key\":\"value\"}";
    final UUID PRICEMODLELUUID1 = UUID
            .fromString("392b159e-e60a-41c4-8495-4fd4b6252ed9");

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void isValidJSONwithInvalid() throws BillingApplicationException {
        // then
        assertFalse(LocalizedBillingResourceAssembler.isValidJSON(INVALID_JSON));
    }

    @Test
    public void isValidJSONwithValid() throws BillingApplicationException {
        // then
        assertTrue(LocalizedBillingResourceAssembler.isValidJSON(VALID_JSON));
    }

    @Test
    public void createPriceModelTag() {
        // given
        String priceModelTag = "Price Model Tag";

        // when
        LocalizedBillingResource returnValue = LocalizedBillingResourceAssembler
                .createPriceModelTag(Locale.ENGLISH, PRICEMODLELUUID1,
                        priceModelTag);

        // then
        assertEquals("Wrong resource type",
                LocalizedBillingResourceType.PRICEMODEL_TAG,
                returnValue.getResourceType());
        assertEquals("Wrong data type", MediaType.TEXT_PLAIN,
                returnValue.getDataType());
        assertTrue("Wrong value",
                Arrays.equals(priceModelTag.getBytes(), returnValue.getValue()));
    }

    @Test
    public void createPriceModel() throws BillingApplicationException {
        // given
        String priceModelTag = "TAG";
        PriceModelContent priceModelContent = new PriceModelContent(
                MediaType.APPLICATION_JSON, VALID_JSON.getBytes(),
                priceModelTag);
        // when
        LocalizedBillingResource result = LocalizedBillingResourceAssembler
                .createPriceModel(PRICEMODLELUUID1, new Locale("en"),
                        priceModelContent,
                        LocalizedBillingResourceType.PRICEMODEL_SERVICE);

        // then
        assertEquals(MediaType.APPLICATION_JSON, result.getDataType());
        assertEquals(LocalizedBillingResourceType.PRICEMODEL_SERVICE,
                result.getResourceType());
        assertEquals(VALID_JSON, new String(result.getValue()));
    }

    @Test
    public void createLocalizedBillingResources()
            throws BillingApplicationException {
        // given
        PriceModel priceModel = createExternalPriceModel();

        // when
        List<LocalizedBillingResource> resultList = LocalizedBillingResourceAssembler
                .createLocalizedBillingResources(priceModel, null);

        // then
        assertEquals(4, resultList.size());
    }

    private PriceModel createExternalPriceModel() {
        PriceModel externalPriceModel = new PriceModel(PRICEMODLELUUID1);
        String content1 = "{\"key1\":\"value1\"}";
        String tag1 = "tag1";
        String content2 = "{\"key2\":\"value2\"}";
        String tag2 = "tag2";

        PriceModelContent priceModelContent1 = new PriceModelContent(
                MediaType.APPLICATION_JSON, content1.getBytes(), tag1);
        PriceModelContent priceModelContent2 = new PriceModelContent(
                MediaType.APPLICATION_JSON, content2.getBytes(), tag2);
        externalPriceModel.put(new Locale("en"), priceModelContent1);
        externalPriceModel.put(new Locale("de"), priceModelContent2);
        Map<ContextKey, ContextValue<?>> context = new HashMap<>();
        externalPriceModel.setContext(context);
        return externalPriceModel;
    }

}
