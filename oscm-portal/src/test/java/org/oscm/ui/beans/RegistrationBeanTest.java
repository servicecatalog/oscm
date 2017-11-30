/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.User;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class RegistrationBeanTest {

    private final static String MARKETPLACE_ID = "MPL_ID";
    private final static String USER_ID = "USER_ID";
    private final static String SUPPLIER_ID = "MY_SUPPLIER";
    private final static Long SERVICE_KEY = new Long(4711L);

    private Map<String, Object> sessionMock = new HashMap<String, Object>();
    private AccountService accountServiceMock = mock(AccountService.class);
    private HttpServletRequest httpServletReqMock = mock(HttpServletRequest.class);
    private User userMock = mock(User.class);
    private UiDelegate uiMock = mock(UiDelegate.class);
    private UserBean userBeanMock = mock(UserBean.class);

    private RegistrationBean registrationBean = new RegistrationBean() {

        private static final long serialVersionUID = 433854026338796442L;

        {
            user = userMock;
            ui = uiMock;
        }

        @Override
        protected Object getSessionAttribute(String key) {
            return sessionMock.get(key);
        }

        @Override
        protected void setSessionAttribute(String key, Object value) {
            sessionMock.put(key, value);
        }

        @Override
        protected String getMarketplaceId() {
            return MARKETPLACE_ID;
        }

        @Override
        protected AccountService getAccountingService() {
            return accountServiceMock;
        }

        @Override
        protected HttpServletRequest getRequest() {
            return httpServletReqMock;
        }

        @Override
        public VOOrganization getOrganization() {
            return new VOOrganization();
        }

        @Override
        public User getUser() {
            return userMock;
        }

        @Override
        UserBean getUserBean() {
            return userBeanMock;
        }

        @Override
        protected void addMessage(final String clientId,
                final FacesMessage.Severity severity, final String key,
                final Object[] params) {

        }

    };

    @Test
    public void init_NotSamlRequest() throws Exception{
        // given
        doReturn(Boolean.FALSE).when(httpServletReqMock).getAttribute(
                Constants.REQ_ATTR_IS_SAML_FORWARD);
        doReturn(Boolean.FALSE).when(userBeanMock).isInternalAuthMode();
        // when
        registrationBean.init();
        // then
        verify(httpServletReqMock, never()).getAttribute(eq(Constants.REQ_PARAM_USER_ID));
        verify(userBeanMock, atLeastOnce()).showRegistration();
    }

    @Test
    public void init_SamlRequest() throws Exception{
        // given
        doReturn(Boolean.TRUE).when(httpServletReqMock).getAttribute(
                Constants.REQ_ATTR_IS_SAML_FORWARD);
        doReturn(USER_ID).when(httpServletReqMock).getAttribute(
                Constants.REQ_PARAM_USER_ID);
        // when
        registrationBean.init();
        // then
        verify(httpServletReqMock, times(1)).getAttribute(eq(Constants.REQ_PARAM_USER_ID));
    }

    @Test
    public void getValueOf_MARKETPLACE_START_SITE() throws Exception {
        String page = registrationBean.getValueOf("MARKETPLACE_START_SITE");
        assertEquals("/marketplace/index.jsf", page);
    }

    @Test
    public void getValueOf_NOT_EXISTING() throws Exception {
        String page = registrationBean.getValueOf("NOT EXISTING");
        assertNull(page);
    }

    @Test
    public void isInternalMode_InternalMode() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(userBeanMock).isInternalAuthMode();
        
        // when
        boolean result = registrationBean.isInternalMode();
        
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isInternalMode_NotInternalMode() throws Exception {
        // given
        doReturn(Boolean.FALSE).when(userBeanMock).isInternalAuthMode();

        // when
        boolean result = registrationBean.isInternalMode();
        
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    // BE08054
    @Test
    public void register_correctSupplierId() throws Exception {
        // given
        prepareRegister();
        // when
        registrationBean.register();

        // Check if the account service was called with the supplierId
        // from the http request
        verify(accountServiceMock, times(1)).registerCustomer(
                any(VOOrganization.class), any(VOUserDetails.class),
                any(String.class), eq(SERVICE_KEY), eq(MARKETPLACE_ID),
                eq(SUPPLIER_ID));
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void register_UserAlreadyExist_InternalMode() throws Exception {
        prepareRegister();
        doReturn(Boolean.TRUE).when(userBeanMock).isInternalAuthMode();
        doThrow(new NonUniqueBusinessKeyException()).when(
                accountServiceMock).registerCustomer(any(VOOrganization.class),
                        any(VOUserDetails.class), any(String.class), eq(SERVICE_KEY),
                        eq(MARKETPLACE_ID), eq(SUPPLIER_ID));

        registrationBean.register();
    }

    @Test
    public void register_UserAlreadyExist_NotInternalMode() throws Exception {
        // given
        new FacesContextStub(Locale.ENGLISH);
        prepareRegister();
        doReturn(Boolean.FALSE).when(userBeanMock).isInternalAuthMode();
        registrationBean.init();
        NonUniqueBusinessKeyException ex = new NonUniqueBusinessKeyException();
        doThrow(ex).when(accountServiceMock).registerCustomer(
                any(VOOrganization.class), any(VOUserDetails.class), any(String.class),
                eq(SERVICE_KEY), eq(MARKETPLACE_ID), eq(SUPPLIER_ID));

        //when
        String result = registrationBean.register();

        // then
        assertEquals(BaseBean.ERROR_USER_ALREADY_EXIST, ex.getMessageKey());        
        assertEquals(BaseBean.OUTCOME_ERROR, result);
    }
    
    /*
     * prepare for register
     * */
     private void prepareRegister() {
         when(httpServletReqMock.getParameter(Constants.REQ_PARAM_SUPPLIER_ID))
                 .thenReturn(SUPPLIER_ID);
         when(httpServletReqMock.getParameter(Constants.REQ_PARAM_SERVICE_KEY))
                 .thenReturn(SERVICE_KEY.toString());
     }

}
