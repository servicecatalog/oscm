/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-2-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.brandservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * Unit test for BrandServiceBean
 * 
 * @author Gao
 * 
 */
public class BrandServiceBeanNoDBTest {

    private BrandServiceBean brandServiceBean;
    private DataService ds;
    private LocalizerServiceLocal localizerService;
    private Marketplace marketplace;

    @Before
    public void setup() throws Exception {
        brandServiceBean = new BrandServiceBean();
        ds = mock(DataService.class);
        brandServiceBean.dm = ds;
        localizerService = mock(LocalizerServiceLocal.class);
        brandServiceBean.localizer = localizerService;

        marketplace = new Marketplace();
        marketplace.setKey(1000L);
    }

    @Test
    public void loadMessagePropertiesFromDB_empty() throws Exception {
        // given
        doReturn(marketplace).when(ds).getReferenceByBusinessKey(
                any(Marketplace.class));
        doReturn(new Properties()).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class),
                        eq(Locale.ENGLISH.toString()));
        // when
        Properties result = brandServiceBean.loadMessagePropertiesFromDB(
                "marketplaceId", Locale.ENGLISH.toString());
        // then
        assertEquals(0, result.size());
    }

    @Test
    public void loadMessagePropertiesFromDB_marketplaceIsNull()
            throws Exception {
        // given
        doReturn(null).when(ds).getReferenceByBusinessKey(
                any(Marketplace.class));
        // when
        Properties result = brandServiceBean.loadMessagePropertiesFromDB(
                "marketplaceId", Locale.ENGLISH.toString());
        // then
        verify(localizerService, never()).loadLocalizedPropertiesFromDatabase(
                anyLong(), any(LocalizedObjectTypes.class), anyString());
        assertEquals(0, result.size());
    }

    @Test
    public void loadMessagePropertiesFromDB() throws Exception {
        // given
        doReturn(marketplace).when(ds).getReferenceByBusinessKey(
                any(Marketplace.class));
        Properties properties = prepareProperties("key", "value");
        doReturn(properties).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        any(LocalizedObjectTypes.class),
                        eq(Locale.ENGLISH.toString()));
        // when
        Properties result = brandServiceBean.loadMessagePropertiesFromDB(
                "marketplaceId", Locale.ENGLISH.toString());
        // then
        assertEquals(properties.size(), result.size());
    }

    @Test
    public void loadMessagePropertiesFromDB_bug10739_MsgNotExistsInShop()
            throws Exception {
        // given
        doReturn(marketplace).when(ds).getReferenceByBusinessKey(
                any(Marketplace.class));
        Properties properties = prepareProperties("key", "value");
        doReturn(new Properties()).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        eq(LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES),
                        eq(Locale.ENGLISH.toString()));
        doReturn(properties).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        eq(LocalizedObjectTypes.MESSAGE_PROPERTIES),
                        eq(Locale.ENGLISH.toString()));
        doReturn(new Properties()).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        eq(LocalizedObjectTypes.MAIL_PROPERTIES),
                        eq(Locale.ENGLISH.toString()));
        // when
        Properties result = brandServiceBean.loadMessagePropertiesFromDB(
                "marketplaceId", Locale.ENGLISH.toString());
        // then
        assertEquals(properties.size(), result.size());
    }

    @Test
    public void loadMessagePropertiesFromDB_bug10739_MsgExistsInShop()
            throws Exception {
        // given
        doReturn(marketplace).when(ds).getReferenceByBusinessKey(
                any(Marketplace.class));
        Properties properties = prepareProperties("key", "value");
        Properties properties_shop = prepareProperties("key", "value_shop");
        doReturn(properties_shop).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        eq(LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES),
                        eq(Locale.ENGLISH.toString()));
        doReturn(properties).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        eq(LocalizedObjectTypes.MESSAGE_PROPERTIES),
                        eq(Locale.ENGLISH.toString()));
        doReturn(new Properties()).when(localizerService)
                .loadLocalizedPropertiesFromDatabase(anyLong(),
                        eq(LocalizedObjectTypes.MAIL_PROPERTIES),
                        eq(Locale.ENGLISH.toString()));
        // when
        Properties result = brandServiceBean.loadMessagePropertiesFromDB(
                "marketplaceId", Locale.ENGLISH.toString());
        // then
        assertEquals(properties.size(), result.size());
        assertEquals(properties_shop.getProperty("key"),
                result.getProperty("key"));
    }

    private Properties prepareProperties(String key, String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return properties;
    }
}
