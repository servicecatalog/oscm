/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 29, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.communicationservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.types.constants.Configuration;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * @author barzu
 * 
 */
public class CommunicationServiceBeanContextPathTest {

    private static final String BASE_URL = "BASE_URL";
    private static final String BASE_URL_WITH_SLASH = "BASE_URL/";
    private static final String BASE_URL_WITH_TWO_SLASHES = "BASE_URL//";

    private CommunicationServiceBean commSrv;

    @Before
    public void setup() throws Exception {
        commSrv = new CommunicationServiceBean();

        ConfigurationServiceLocal confServ = mock(ConfigurationServiceLocal.class);
        ConfigurationSetting cs = mock(ConfigurationSetting.class);
        doReturn(cs).when(confServ).getConfigurationSetting(
                any(ConfigurationKey.class), anyString());
        doReturn(BASE_URL).when(confServ).getBaseURL();
        commSrv.confSvc = confServ;

        doReturn(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL)).when(
                commSrv.confSvc)
                .getConfigurationSetting(eq(ConfigurationKey.BASE_URL),
                        eq(Configuration.GLOBAL_CONTEXT));
    }

    @Test
    public void getMarketplaceUrl_Marketplace() throws Exception {
        String url = commSrv.getMarketplaceUrl("abc");
        assertEquals(BASE_URL + Marketplace.MARKETPLACE_ROOT + "?mId=abc", url);
    }

    @Test
    public void getMarketplaceUrl_Marketplace_NoMId() throws Exception {
        String url = commSrv.getMarketplaceUrl(null);
        assertEquals(BASE_URL, url);
    }

    @Test
    public void getMarketplaceUrl_BaseUrlWithSlash() throws Exception {
        // given a base url with an ending slash
        doReturn(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL_WITH_SLASH))
                .when(commSrv.confSvc).getConfigurationSetting(
                        eq(ConfigurationKey.BASE_URL),
                        eq(Configuration.GLOBAL_CONTEXT));
        // when
        String url = commSrv.getMarketplaceUrl("abc");

        // then the ending slash of the base url is trailed.
        assertEquals(BASE_URL + Marketplace.MARKETPLACE_ROOT + "?mId=abc", url);
    }

    @Test
    public void getMarketplaceUrl_BaseUrlWithTwoSlashes() throws Exception {
        // given a base url with two ending slashes
        doReturn(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL_WITH_TWO_SLASHES))
                .when(commSrv.confSvc).getConfigurationSetting(
                        eq(ConfigurationKey.BASE_URL),
                        eq(Configuration.GLOBAL_CONTEXT));
        // when
        String url = commSrv.getMarketplaceUrl("abc");

        // then the two ending slashes of the base url are trailed.
        assertEquals(BASE_URL + Marketplace.MARKETPLACE_ROOT + "?mId=abc", url);
    }

    @Test
    public void getBaseUrl() {

        // when
        String baseUrl = commSrv.getBaseUrl();

        // then the value of the BASE_URL configuration setting is returned
        assertEquals(BASE_URL, baseUrl);
    }
}
