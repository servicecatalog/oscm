/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-8-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.accountNavigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.UserBean;

/**
 * @author Yuyin
 * 
 */
public class AccountNavigationCtrlTest {

    private AccountNavigationCtrl ctrl;
    private ApplicationBean abMock;
    private ConfigurationService cnfgSrv;
    private UserBean userMock;

    private final String CURRENT_URL = "https://estgoebel:8180/oscm-portal/marketplace/index?mId=\"e1d423ce\"";

    @SuppressWarnings({ "serial" })
    @Before
    public void setup() throws Exception {
        abMock = mock(ApplicationBean.class);
        cnfgSrv = mock(ConfigurationService.class);
        userMock = mock(UserBean.class);

        when(Boolean.valueOf(abMock.isReportingAvailable()))
                .thenReturn(Boolean.TRUE);
        when(abMock.getServerBaseUrl()).thenReturn("baseURL");
        ctrl = new AccountNavigationCtrl() {

            @Override
            protected ConfigurationService getConfigurationService() {
                return cnfgSrv;
            }

            @Override
            public VOUserDetails getUserFromSessionWithoutException() {
                VOUserDetails mockUser = mock(VOUserDetails.class);
                return mockUser;
            }
        };
        ctrl = spy(ctrl);
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndAdmin();
        doReturn(getDefaultHidePaymentConfigurationSetting()).when(cnfgSrv)
                .getVOConfigurationSetting(
                        ConfigurationKey.HIDE_PAYMENT_INFORMATION,
                        Configuration.GLOBAL_CONTEXT);
        ctrl.setApplicationBean(abMock);
        AccountNavigationModel model = new AccountNavigationModel() {
            @Override
            public UserBean getUserBean() {
                return userMock;
            }

            @Override
            public ApplicationBean getAppBean() {
                return abMock;
            }
        };
        ctrl.setModel(model);
    }

    @Test
    public void getModel() {
        AccountNavigationModel model = ctrl.getModel();
        assertEquals(9, model.getHiddenElement().size());
        assertEquals(10, model.getLink().size());
        assertEquals(10, model.getTitle().size());
        assertTrue(model.getLink().get(0),
                model.getLink().get(0).endsWith("index.jsf"));
        assertEquals(AccountNavigationModel.MARKETPLACE_ACCOUNT_TITLE,
                model.getTitle().get(0));
    }

    @Test
    public void computeBaseUrl() {
        // when
        ctrl.computeBaseUrl(
                "https://estgoebel:8180/oscm-portal/marketplace/index?mId=\"ed123ge\"");
        AccountNavigationModel model = ctrl.getModel();
        assertEquals("https://estgoebel:8180/oscm-portal", model.getBaseUrl());

    }

    @Test
    public void getLink() {
        AccountNavigationModel model = ctrl.getModel();
        ctrl.computeBaseUrl(CURRENT_URL);

        assertEquals("https://estgoebel:8180/oscm-portal", model.getBaseUrl());

        List<String> result = ctrl.getLink();
        assertEquals(10, result.size());

        assertEquals(result.get(0),
                "https://estgoebel:8180/oscm-portal/marketplace/account/index.jsf");
        assertEquals(result.get(1),
                "https://estgoebel:8180/oscm-portal/marketplace/account/profile.jsf");
    }

    @Test
    public void getTitle() {
        List<String> result = ctrl.getTitle();
        assertEquals(10, result.size());
        assertEquals(AccountNavigationModel.MARKETPLACE_ACCOUNT_TITLE,
                result.get(0));
    }

    @Test
    public void getHiddenElement() {
        List<String> result = ctrl.getHiddenElement();
        assertEquals(9, result.size());
        assertEquals(HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PROFILE,
                result.get(0));
    }

    @Test
    public void isReportingAvailable_visible() {
        doReturn(Boolean.FALSE).when(abMock).isUIElementHidden(
                eq(HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS));
        boolean result = ctrl.isReportingAvailable();
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isReportingAvailable_inVisible() {
        doReturn(Boolean.TRUE).when(abMock).isUIElementHidden(
                eq(HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS));
        boolean result = ctrl.isReportingAvailable();
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkVisible_ProfileInVisible() {
        ctrl.getModel();
        doReturn(Boolean.TRUE).when(abMock).isUIElementHidden(
                eq(HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PROFILE));
        boolean result = ctrl.isLinkVisible(1);
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkVisible_Title() {
        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(0);
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkVisible_SubscriptionMenu() {
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndUnitAdmin();
        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(3);
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkHidden_SubscriptionMenu() {
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndAdmin();
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndSubscriptionManager();
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndUnitAdmin();
        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(3);
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkVisible_OrgUnits() {
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndAdmin();
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndSubscriptionManager();
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndUnitAdmin();
        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(5);
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkVisible_loggedInAndAdmin_Hidden_Users() {
        doReturn(Boolean.TRUE).when(abMock).isUIElementHidden(anyString());
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndAdmin();
        doReturn(Boolean.TRUE).when(ctrl).isAdministrationAccess();

        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(4);
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkVisible_loggedInAndAdmin_Hidden_Administration() {
        doReturn(Boolean.TRUE).when(abMock).isUIElementHidden(anyString());
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndAdmin();
        doReturn(Boolean.TRUE).when(ctrl).isAdministrationAccess();

        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(9);
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isLinkHidden_OrgUnits() {
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndAdmin();
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndSubscriptionManager();
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndUnitAdmin();

        ctrl.getModel();
        boolean result = ctrl.isLinkVisible(5);
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void menuSizeForUnitAdmin() {
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndAdmin();
        doReturn(Boolean.FALSE).when(ctrl).isLoggedInAndSubscriptionManager();
        doReturn(Boolean.TRUE).when(ctrl).isLoggedInAndUnitAdmin();
        ctrl.getModel();
        int visibleLinks = 0;
        for (int i = 0; i < ctrl.getLink().size(); i++) {
            if (ctrl.isLinkVisible(i)) {
                visibleLinks++;
            }
        }
        assertEquals(6, visibleLinks);
    }

    @Test
    public void isPaymentAvailable_inVisible() {

        // given
        VOConfigurationSetting setting = new VOConfigurationSetting(
                ConfigurationKey.HIDE_PAYMENT_INFORMATION,
                Configuration.GLOBAL_CONTEXT, "TRUE");
        doReturn(setting).when(cnfgSrv).getVOConfigurationSetting(
                ConfigurationKey.HIDE_PAYMENT_INFORMATION,
                Configuration.GLOBAL_CONTEXT);

        // when
        boolean isPaymentAvailable = ctrl.isPaymentAvailable();

        // then
        assertFalse(isPaymentAvailable);
    }

    private VOConfigurationSetting getDefaultHidePaymentConfigurationSetting() {

        return new VOConfigurationSetting(
                ConfigurationKey.HIDE_PAYMENT_INFORMATION,
                Configuration.GLOBAL_CONTEXT, "FALSE");
    }

}
