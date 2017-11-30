/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.marketplace;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.UserBean;

/**
 * Created by PLGrubskiM on 2016-09-14.
 */
public class AccountNavigationBeanTest {

    private AccountNavigationBean accountNavigationBean;
    private ApplicationBean appBean;
    private UserBean userBean;
    private boolean isLoggedInAndAdmin;
    private boolean isLoggedInAndSubscriptionManager;
    private boolean isAdministrationAvailable;
    private boolean isLoggedInAndUnitAdmin;

    @Before
    public void setup() {
        accountNavigationBean = new AccountNavigationBean(){
            @Override
            public boolean isLoggedInAndAdmin() {
                return isLoggedInAndAdmin;
            }

            @Override
            public boolean isLoggedInAndUnitAdmin() {
                return isLoggedInAndUnitAdmin;
            }

            @Override
            public boolean isLoggedInAndSubscriptionManager() {
                return isLoggedInAndSubscriptionManager;
            }

            @Override
            public boolean isAdministrationAvailable() {
                return isAdministrationAvailable;
            }

        };

        isLoggedInAndAdmin = true;
        isLoggedInAndUnitAdmin = true;
        isLoggedInAndSubscriptionManager = true;
        isAdministrationAvailable = true;

        appBean = mock(ApplicationBean.class);
        userBean = mock(UserBean.class);

        doReturn("adminPortalAddress").when(userBean).getAdminPortalAddress();

        accountNavigationBean.setApplicationBean(appBean);
        accountNavigationBean.setUserBean(userBean);

    }

    @Test
    public void getLinkMapFull() {
        //when
        final Map<String, String> linkMap = accountNavigationBean.getLinkMap();
        //then
        // assert all links included
        assertTrue(linkMap.size() == 8);
        assertTrue(linkMap.containsKey("marketplace.account.title"));
        assertTrue(linkMap.containsKey("marketplace.account.profile.title"));
        assertTrue(linkMap.containsKey("marketplace.account.payments.title"));
        assertTrue(linkMap.containsKey("marketplace.account.subscriptions.title"));
        assertTrue(linkMap.containsKey("marketplace.account.users.title"));
        assertTrue(linkMap.containsKey("marketplace.account.processes.title"));
        assertTrue(linkMap.containsKey("marketplace.account.operations.title"));
        assertTrue(linkMap.containsKey("marketplace.account.administration"));
    }

    @Test
    public void getLinkMap_NotAdmin() {
        //given
        isLoggedInAndAdmin = false;
        //when
        final Map<String, String> linkMap = accountNavigationBean.getLinkMap();
        //then
        // only "payment" link not added to the list
        assertTrue(linkMap.size() == 7);
        assertTrue(linkMap.containsKey("marketplace.account.title"));
        assertTrue(linkMap.containsKey("marketplace.account.profile.title"));
        assertTrue(linkMap.containsKey("marketplace.account.subscriptions.title"));
        assertTrue(linkMap.containsKey("marketplace.account.units.title"));
        assertTrue(linkMap.containsKey("marketplace.account.processes.title"));
        assertTrue(linkMap.containsKey("marketplace.account.operations.title"));
        assertTrue(linkMap.containsKey("marketplace.account.administration"));
    }

    @Test
    public void getLinkMap_NotAdmin_NotUnitAdmin() {
        //given
        isLoggedInAndAdmin = false;
        isLoggedInAndUnitAdmin = false;
        //when
        final Map<String, String> linkMap = accountNavigationBean.getLinkMap();
        //then
        // no units and no users page
        assertTrue(linkMap.size() == 6);
        assertTrue(linkMap.containsKey("marketplace.account.title"));
        assertTrue(linkMap.containsKey("marketplace.account.profile.title"));
        assertTrue(linkMap.containsKey("marketplace.account.subscriptions.title"));
        assertTrue(linkMap.containsKey("marketplace.account.processes.title"));
        assertTrue(linkMap.containsKey("marketplace.account.operations.title"));
        assertTrue(linkMap.containsKey("marketplace.account.administration"));
    }

    @Test
    public void getLinkMap_Basic() {
        //given
        isLoggedInAndAdmin = false;
        isLoggedInAndUnitAdmin = false;
        isLoggedInAndSubscriptionManager = false;
        isAdministrationAvailable = false;
        //when
        final Map<String, String> linkMap = accountNavigationBean.getLinkMap();
        //then
        // only four basic pages
        assertTrue(linkMap.size() == 4);
        assertTrue(linkMap.containsKey("marketplace.account.title"));
        assertTrue(linkMap.containsKey("marketplace.account.profile.title"));
        assertTrue(linkMap.containsKey("marketplace.account.processes.title"));
        assertTrue(linkMap.containsKey("marketplace.account.operations.title"));
    }

    @Test
    public void getLinkKeys() {
        //when
        final Map<String, String> linkMap = accountNavigationBean.getLinkMap();
        final List<String> linkKeys = accountNavigationBean.getLinkKeys();
        //then
        for (String key : linkKeys) {
            assertTrue(linkMap.containsKey(key));
        }
    }


}
