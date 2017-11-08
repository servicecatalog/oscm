/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.PlatformUser;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Unit test for the PasswordRecoveryLink.
 * 
 * @author Yu
 * 
 */
public class PasswordRecoveryLinkTest {

    private PasswordRecoveryLink link;
    private static final String BASE_URL = "BASE_URL";
    private ConfigurationServiceLocal configs;
    private String[] urlParam = new String[3];
    private PlatformUser pUser;
    private final long currentTime = System.currentTimeMillis();
    private final String marketplaceId = "mp1";

    @Before
    public void setUp() throws Exception {
        configs = mock(ConfigurationServiceLocal.class);
        pUser = new PlatformUser();
        pUser.setUserId("user1");
        doReturn(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL)).when(configs)
                .getConfigurationSetting(eq(ConfigurationKey.BASE_URL),
                        eq(Configuration.GLOBAL_CONTEXT));

        link = new PasswordRecoveryLink(false, configs);
    }

    @Test
    public void encodeAndDecodeMPLink_Ok() throws Exception {
        // when
        String result = link.encodePasswordRecoveryLink(pUser, currentTime,
                marketplaceId);
        result = result.substring(result.indexOf("token=") + 6);
        urlParam = PasswordRecoveryLink.decodeRecoveryPasswordLink(result);
        // then
        assertEquals("mp1", urlParam[2]);
        assertEquals("user1", urlParam[0]);
        assertEquals(3, urlParam.length);
    }

    @Test
    public void encodeAndDecodeClassicPortalLink_Ok() throws Exception {
        // given
        // when
        String result = link.encodePasswordRecoveryLink(pUser, currentTime, "");
        result = result.substring(result.indexOf("token=") + 6);
        urlParam = PasswordRecoveryLink.decodeRecoveryPasswordLink(result);
        // then
        assertEquals("user1", urlParam[0]);
        assertEquals(2, urlParam.length);
    }

    @Test
    public void encodeAndDecodeMarketpalcePortalLink_Ok() throws Exception {
        // given
        link = new PasswordRecoveryLink(true, configs);
        // when
        String result = link.encodePasswordRecoveryLink(pUser, currentTime,
                "mp1");
        result = result.substring(result.indexOf("token=") + 6);
        urlParam = PasswordRecoveryLink.decodeRecoveryPasswordLink(result);
        // then
        assertEquals("user1", urlParam[0]);
        assertEquals("mp1", urlParam[2]);
        assertEquals(3, urlParam.length);
    }

    @Test
    public void removeTrailingSlashes_withTwoSlashes() throws Exception {
        // given
        StringBuffer url = new StringBuffer();
        url.append(BASE_URL + "//");
        // when
        link.removeTrailingSlashes(url);
        // then
        assertEquals(BASE_URL.length(), url.length());
    }
    
    @Test
    public void decodeRecoveryPasswordLink_NullString() throws Exception {
        // when
        String[] result = PasswordRecoveryLink.decodeRecoveryPasswordLink("");
        // then
        assertNull(result);
    }
}
