/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueParameterMap;
import org.oscm.billing.external.context.ContextValueString;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import javax.ws.rs.client.WebTarget;

public class PriceModelPluginTest {

    public static final String PM_UUID = "f042c6ca-6f6e-49eb-90d2-fb381f9c2a82";
    public static final String PM_FILE_TYPE = "application/pdf";
    public static final String PM_FILE_NAME_EN = "en/t2_large_eu_frankfurt_windows.pdf";
    public static final String PM_FILE_NAME_DE = "de/t2_large_eu_frankfurt_windows.pdf";
    public static final String PM_TAG_EN = "Starting at 54.06 $/Month";
    public static final String PM_TAG_DE = "Ab 54,06 $/Monat";
    public static final String LOCALE_EN = "en";
    public static final String LOCALE_DE = "de";

    private static final byte[] PM_FILE_EN = new byte[] { 0x25, 0x50, 0x44,
            0x46, 0x2d, 0x31, 0x2e, 0x35, 0x0d, 0x0a, 0x25, (byte) 0xb5,
            (byte) 0xb5, (byte) 0xb5, (byte) 0xb5, 0x0d, 0x0a, 0x31, 0x20,
            0x30, 0x20, 0x6f, 0x62, 0x6a, 0x0d, 0x0a, 0x3c, 0x3c };

    private static final byte[] PM_FILE_DE = new byte[] { 0x25, 0x50, 0x44,
            0x46, 0x2d, 0x31, 0x2e, 0x35, 0x0d, 0x0a, 0x25, (byte) 0xb5,
            (byte) 0xb5, (byte) 0xb5, (byte) 0xb5, 0x0d, 0x0a, 0x31, 0x20,
            0x30, 0x20, 0x6f, 0x62, 0x6a, 0x0d, 0x0a, 0x3e, 0x3e };

    public static final String PRICE_MODEL_DATA[] = { PM_UUID, LOCALE_EN,
            PM_FILE_TYPE, PM_FILE_NAME_EN, PM_TAG_EN, LOCALE_DE, PM_FILE_TYPE,
            PM_FILE_NAME_DE, PM_TAG_DE };

    private PriceModelPlugin priceModelPlugin;
    private ConfigProperties properties;
    private RestDAO restDao;

    @Before
    public void setup() throws Exception {
        priceModelPlugin = Mockito.spy(new PriceModelPlugin());
        properties = Mockito.mock(ConfigProperties.class);
        restDao = Mockito.mock(RestDAO.class);
        priceModelPlugin.properties = properties;
        priceModelPlugin.restDao = restDao;
    }

    @Test
    public void getPriceModel() throws Exception {
        // given
        Mockito.doReturn(
                "http://localhost:8680/oscm-file-billing/rest/priceModel")
                .when(properties)
                .getConfigProperty(PriceModelPlugin.PRICEMODEL_URL);
        Mockito.doReturn(Arrays.asList(PRICE_MODEL_DATA)).when(restDao)
                .getPriceModelData(Mockito.any(WebTarget.class));
        Mockito.doReturn(PM_FILE_EN).when(priceModelPlugin)
                .getPriceModelFile(PM_FILE_NAME_EN);
        Mockito.doReturn(PM_FILE_DE).when(priceModelPlugin)
                .getPriceModelFile(PM_FILE_NAME_DE);

        Map<ContextKey, ContextValue<?>> context = createContext();

        // when
        PriceModel priceModel = priceModelPlugin.getPriceModel(context,
                createLocaleSet());

        // then
        assertNotNull(priceModel);
        assertEquals("Wrong price model ID", PM_UUID, priceModel.getId()
                .toString());
        assertEquals("Wrong context", context, priceModel.getContext());
        Set<Locale> locales = priceModel.getLocales();
        assertEquals("Wrong number of locales", 2, locales.size());

        for (Locale locale : locales) {
            assertTrue(
                    "Wrong locale langue",
                    Arrays.asList(LOCALE_EN, LOCALE_DE).contains(
                            locale.getLanguage()));
            PriceModelContent pmContent = priceModel.get(locale);
            assertEquals("Wrong price model content type", PM_FILE_TYPE,
                    pmContent.getContentType());

            switch (locale.getLanguage()) {
            case LOCALE_EN:
                assertEquals("Wrong price model tag", PM_TAG_EN,
                        pmContent.getTag());
                assertEquals("Wrong price model content", PM_FILE_EN,
                        pmContent.getContent());
                break;
            case LOCALE_DE:
                assertEquals("Wrong price model tag", PM_TAG_DE,
                        pmContent.getTag());
                assertEquals("Wrong price model content", PM_FILE_DE,
                        pmContent.getContent());
                break;
            default:
                break;
            }
        }
    }

    private Set<Locale> createLocaleSet() {
        Set<Locale> locales = new HashSet<Locale>();
        locales.add(new Locale(LOCALE_EN));
        locales.add(new Locale(LOCALE_DE));
        return locales;
    }

    private Map<ContextKey, ContextValue<?>> createContext() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("INSTANCE_TYPE", "t2.medium");
        parameterMap.put("REGION", "eu-west-1");
        parameterMap.put("OS", "linux");
        Map<ContextKey, ContextValue<?>> context = new HashMap<>();
        context.put(ContextKey.CUSTOMER_ID, new ContextValueString("d3fad567"));
        context.put(ContextKey.SERVICE_PARAMETERS,
                new ContextValueParameterMap(parameterMap));
        return context;
    }

    @Test
    public void getPriceModel_priceModel_not_found() throws Exception {
        // given
        Mockito.doReturn(
                "http://localhost:8680/oscm-file-billing/rest/priceModel")
                .when(properties)
                .getConfigProperty(PriceModelPlugin.PRICEMODEL_URL);
        Mockito.doReturn(null).when(restDao)
                .getPriceModelData(Mockito.any(WebTarget.class));

        // when
        PriceModel priceModel = priceModelPlugin.getPriceModel(
                new HashMap<ContextKey, ContextValue<?>>(), null);

        // then
        assertNull(priceModel);
    }

}
