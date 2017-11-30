/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2013-2-26
 *
 *******************************************************************************/

package org.oscm.ui.dialog.classic.completePwdRecovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.ParameterEncoder;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.passwordrecovery.PasswordRecoveryService;

/**
 * @author Mao
 *
 */
@SuppressWarnings("boxing")
public class CompletePwdRecoveryCtrlTest {

    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_ERROR = "error";
    private static final String ADMIN = "admin";
    private static final String ADMIN2 = "admin2";
    private static final String ADMIN3 = "admin3_deleted";
    private CompletePwdRecoveryModel model;
    private PasswordRecoveryService pwdRecoveryService;
    private IdentityServiceLocal ids;
    private CompletePwdRecoveryCtrl completePwdRecoveryCtrl;
    private String token;
    private PlatformUser pUser;
    private final List<String> messages = new ArrayList<String>();
    private UiDelegate uiDelegate;
    private boolean isMarketplaceSet = false;

    @Before
    public void before() throws Exception {
        model = new CompletePwdRecoveryModel();
        model.setUserId(ADMIN);
        model.setNewPassword(ADMIN);
        pUser = prepareUser("admin", System.currentTimeMillis());
        token = getConfirmUrl(pUser, null);
        messages.clear();
        pwdRecoveryService = mock(PasswordRecoveryService.class);
        ids = mock(IdentityServiceLocal.class);
        doReturn(pUser).when(ids).getPlatformUser(anyString(), anyBoolean());
        uiDelegate = mock(UiDelegate.class);
        doReturn("MarketplaceId").when(uiDelegate).getMarketplaceId();
        completePwdRecoveryCtrl = new CompletePwdRecoveryCtrl() {

            @Override
            protected void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key) {
                messages.add(key);
            }

            @Override
            protected PasswordRecoveryService getPasswordRecoveryService() {
                return pwdRecoveryService;
            }

            @Override
            protected boolean isMarketplaceSet(HttpServletRequest httpRequest) {
                return isMarketplaceSet;
            }

            @Override
            protected HttpServletRequest getRequest() {
                return null;
            }
        };
        completePwdRecoveryCtrl.model = model;
        completePwdRecoveryCtrl.ui = uiDelegate;

    }

    @Test
    public void getInitialize_OK() throws Exception {
        // given
        token = getConfirmUrl(prepareUser(ADMIN, System.currentTimeMillis()),
                null);
        completePwdRecoveryCtrl.setToken(token);
        doReturn(ADMIN).when(pwdRecoveryService).confirmPasswordRecoveryLink(
                anyString(), anyString());
        // then
        verify(pwdRecoveryService, times(1)).confirmPasswordRecoveryLink(token,
                null);
    }

    @Test
    public void getInitialize_OK_Marketplace() throws Exception {
        // given
        isMarketplaceSet = true;
        token = getConfirmUrl(prepareUser(ADMIN, System.currentTimeMillis()),
                "MarketplaceId");
        completePwdRecoveryCtrl.setToken(token);
        doReturn(ADMIN).when(pwdRecoveryService).confirmPasswordRecoveryLink(
                anyString(), anyString());
        // then
        verify(pwdRecoveryService, times(1)).confirmPasswordRecoveryLink(token,
                "MarketplaceId");
    }

    @Test
    public void getInitialize_Token_Expired() throws Exception {
        // given
        Date current = new Date();
        long time = current.getTime() - 86400020;
        token = getConfirmUrl(prepareUser(ADMIN2, time), null);
        completePwdRecoveryCtrl.setToken(token);
        doReturn(null).when(pwdRecoveryService).confirmPasswordRecoveryLink(
                anyString(), anyString());

        // then
        assertEquals(1, messages.size());
        assertEquals(BaseBean.ERROR_RECOVERPASSWORD_INVALID_LINK,
                messages.get(0));
        verify(pwdRecoveryService, times(1)).confirmPasswordRecoveryLink(token,
                null);
    }

    @Test
    public void getInitialize_Token_Invalid() throws Exception {
        // given
        completePwdRecoveryCtrl.setToken(null);

        // then
        verify(pwdRecoveryService, never()).confirmPasswordRecoveryLink(token,
                null);

        // given
        completePwdRecoveryCtrl.setToken("");

        // then
        assertEquals(2, messages.size());
        assertEquals(BaseBean.ERROR_RECOVERPASSWORD_INVALID_LINK, messages.get(0));
        verify(pwdRecoveryService, never()).confirmPasswordRecoveryLink(token, null);
    }

    @Test
    public void getInitialize_User_Deleted() throws Exception {
        // given
        token = getConfirmUrl(prepareUser(ADMIN3, System.currentTimeMillis()),
                null);
        completePwdRecoveryCtrl.setToken(token);

        // then
        boolean isShowError = completePwdRecoveryCtrl.isShowError();
        assertEquals(true, isShowError);
        assertEquals(1, messages.size());
        assertEquals(BaseBean.ERROR_RECOVERPASSWORD_INVALID_LINK,
                messages.get(0));
        verify(pwdRecoveryService, times(1)).confirmPasswordRecoveryLink(token,
                null);
    }

    @Test
    public void getInitialize_User_Deleted_Marketplace() throws Exception {
        // given
        isMarketplaceSet = true;
        token = getConfirmUrl(prepareUser(ADMIN3, System.currentTimeMillis()),
                "MarketplaceId");
        completePwdRecoveryCtrl.setToken(token);

        // then
        boolean isShowError = completePwdRecoveryCtrl.isShowError();
        assertEquals(true, isShowError);
        assertEquals(1, messages.size());
        assertEquals(BaseBean.ERROR_RECOVERPASSWORD_INVALID_LINK,
                messages.get(0));
        verify(pwdRecoveryService, times(1)).confirmPasswordRecoveryLink(token,
                "MarketplaceId");
    }

    @Test
    public void completePasswordRecovery_OK() {
        // given
        completePwdRecoveryCtrl.setModel(model);
        doReturn(Boolean.TRUE).when(pwdRecoveryService)
        .completePasswordRecovery(anyString(), anyString());
        // when
        String result = completePwdRecoveryCtrl.completePasswordRecovery();
        // then
        assertEquals(OUTCOME_SUCCESS, result);
        assertEquals(1, messages.size());
        assertEquals(BaseBean.INFO_RECOVERPASSWORD_SUCCESS, messages.get(0));
        verify(pwdRecoveryService, times(1)).completePasswordRecovery(
                eq(ADMIN), eq(ADMIN));
    }

    @Test
    public void completePasswordRecovery_Fail() {
        // given
        completePwdRecoveryCtrl.setModel(model);
        doReturn(Boolean.FALSE).when(pwdRecoveryService)
        .completePasswordRecovery(anyString(), anyString());
        // when
        String result = completePwdRecoveryCtrl.completePasswordRecovery();
        // then
        assertEquals(OUTCOME_ERROR, result);
        assertEquals(1, messages.size());
        assertEquals(BaseBean.ERROR_USER_PWD_RESET, messages.get(0));
        verify(pwdRecoveryService, times(1)).completePasswordRecovery(
                eq(ADMIN), eq(ADMIN));
    }

    @Test
    public void getModel() {
        assertNotNull(completePwdRecoveryCtrl.getModel());
    }

    @Test
    public void getModel_Null() {
        completePwdRecoveryCtrl.setModel(null);
        assertNull(completePwdRecoveryCtrl.getModel());
    }

    /**
     * Generate Confirm Url
     *
     * @param pUser
     * @return String
     * @throws Exception
     */
    private String getConfirmUrl(PlatformUser pUser, String marketplaceId)
            throws Exception {
        StringBuffer token = new StringBuffer();
        String[] urlParam = new String[3];
        urlParam[0] = pUser.getUserId();
        urlParam[1] = Long.toString(pUser.getPasswordRecoveryStartDate());
        urlParam[2] = (marketplaceId == null) ? "" : marketplaceId;
        token.append(URLEncoder.encode(
                ParameterEncoder.encodeParameters(urlParam), "UTF-8"));
        token.append("&et");
        return token.toString();
    }

    /**
     * Prepare user data for test
     *
     * @param userId
     *
     * @param passwordRecoveryStartDate
     *            Start Date of Password Recovery.
     * @return PlatformUser
     */
    private PlatformUser prepareUser(String userId,
            long passwordRecoveryStartDate) {
        pUser = new PlatformUser();
        pUser.setUserId(userId);
        pUser.setPasswordRecoveryStartDate(passwordRecoveryStartDate);
        return pUser;
    }
}
