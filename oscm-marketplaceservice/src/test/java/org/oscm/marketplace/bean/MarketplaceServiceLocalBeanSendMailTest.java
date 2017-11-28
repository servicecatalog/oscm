/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 28, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.types.enumtypes.EmailType;

/**
 * Tests email sending from {@link MarketplaceServiceBean}
 * 
 * @author barzu
 */
public class MarketplaceServiceLocalBeanSendMailTest {

    private static final String REL_PATH_MP_ADMIN = "/shop/updateMarketplace.jsf";

    private MarketplaceServiceLocalBean mpSrv;

    @Before
    public void setup() throws Exception {
        mpSrv = new MarketplaceServiceLocalBean();

        DataService dm = mock(DataService.class);
        mpSrv.ds = dm;
        Query query = mock(Query.class);
        doReturn(query).when(dm).createNamedQuery(anyString());
        PlatformUser user = new PlatformUser();
        user.setLocale("en");
        doReturn(user).when(mpSrv.ds).getCurrentUser();

        CommunicationServiceLocal cm = mock(CommunicationServiceLocal.class);
        mpSrv.commService = cm;

        mpSrv.localizer = mock(LocalizerServiceBean.class);
    }

    @Test
    public void sendNotification_getMarketplaceUrl() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("abc");
        doReturn("mpName").when(mpSrv.localizer).getLocalizedTextFromDatabase(
                anyString(), anyLong(),
                eq(LocalizedObjectTypes.MARKETPLACE_NAME));

        mpSrv.sendNotification(EmailType.MARKETPLACE_OWNER_ASSIGNED, mp,
                Collections.singletonList(new PlatformUser()));

        // once for marketplace administration URL
        verify(mpSrv.commService, times(1)).getBaseUrl();
        // once for public marketplace
        verify(mpSrv.commService, times(1)).getMarketplaceUrl(eq("abc"));
    }

    /**
     * The base URL is the URL of the BES portal default page. The
     * marketplace administration URL must be built correctly based on the
     * base URL.
     */
    @Test
    public void sendNotification_TrailingSlash() throws Exception {
        sendNotification_mpAdminUrl("url/", "url" + REL_PATH_MP_ADMIN);
    }

    /**
     * The base URL is the URL of the BES portal default page. The
     * marketplace administration URL must be built correctly based on the
     * base URL.
     */
    @Test
    public void sendNotification_NoTrailingSlash() throws Exception {
        sendNotification_mpAdminUrl("url", "url" + REL_PATH_MP_ADMIN);
    }

    /**
     * The base URL is the URL of the BES portal default page. The
     * marketplace administration URL must be built correctly based on the
     * base URL.
     */
    @Test
    public void sendNotification_Params_TrailingSlash() throws Exception {
        sendNotification_mpAdminUrl("url/?mId=abc", "url" + REL_PATH_MP_ADMIN
                + "?mId=abc");
    }

    /**
     * The base URL is the URL of the BES portal default page. The
     * marketplace administration URL must be built correctly based on the
     * base URL.
     */
    @Test
    public void sendNotification_Params_NoTrailingSlash() throws Exception {
        sendNotification_mpAdminUrl("url?mId=abc", "url" + REL_PATH_MP_ADMIN
                + "?mId=abc");
    }

    private void sendNotification_mpAdminUrl(String givenContextUrl,
            String expectedAdminUrl) throws Exception {
        // given
        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("abc");
        doReturn(givenContextUrl).when(mpSrv.commService).getBaseUrl();

        // when
        mpSrv.sendNotification(EmailType.MARKETPLACE_OWNER_ASSIGNED, mp,
                Collections.singletonList(new PlatformUser()));

        // then
        verify(mpSrv.commService, times(1)).sendMail(any(PlatformUser.class),
                any(EmailType.class),
                argThat(adminUrlEquals(expectedAdminUrl)),
                any(Marketplace.class));
    }

    private ArgumentMatcher<Object[]> adminUrlEquals(final String s) {
        return new ArgumentMatcher<Object[]>() {

            @Override
            public boolean matches(Object argument) {
                Object[] arguments = ((Object[]) argument);
                return ((String) arguments[2]).equals(s);
            }
        };
    }
}
