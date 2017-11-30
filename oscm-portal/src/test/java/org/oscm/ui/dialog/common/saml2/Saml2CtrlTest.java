/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.10.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.common.saml2;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.types.exception.NotExistentTenantException;
import org.oscm.internal.types.exception.SAML2AuthnRequestException;
import org.oscm.logging.Log4jLogger;
import org.oscm.saml2.api.AuthnRequestGenerator;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.filter.AuthenticationSettings;

/**
 * @author roderus
 * 
 */
public class Saml2CtrlTest {

    private static final String TEST_RELAY_STATE = "some_relay_state";
    private static final String OUTCOME_MARKETPLACE_ERROR = "marketplace/logout";
    private static final String OUTCOME_PUBLIC_ERROR_PAGE = "publicErrorPage";
    private static final String ERROR_GENERATE_AUTHNREQUEST = "error.generating.authnrequest";
    private static final String ERROR_INVALID_IDP_URL = "error.invalid.idpUrl";
    private static final String DUMMY_AUTHREQUEST = "some_encoded_authnrequest";
    private static final String DUMMY_RELAYSTATE = "some_relaystate";
    private static final String DUMMY_ACSURL = "https://some.acs.url.de";
    private static final String DUMMY_REQID = "some_requestid";

    private Saml2Ctrl saml2Ctrl;
    private AuthnRequestGenerator authnReqGenMock;
    private HttpServletRequest requestMock;
    private ConfigurationService configServiceMock;
    private Log4jLogger loggerMock;
    private Saml2Model saml2ModelMock;
    private UiDelegate uiDelegateMock;
    private AuthenticationSettings authSett = mock(AuthenticationSettings.class);

    @Before
    public void setup() throws Exception {
        requestMock = mock(HttpServletRequest.class);
        saml2Ctrl = spy(new Saml2Ctrl() {
            @Override
            protected HttpServletRequest getRequest() {
                return requestMock;
            }

            @Override
            protected ConfigurationService getConfigurationService() {
                return configServiceMock;
            }
        });

        saml2ModelMock = mock(Saml2Model.class);

        // usually injected as configured in faces-config.xml
        saml2Ctrl.setModel(saml2ModelMock);

        uiDelegateMock = mock(UiDelegate.class);
        saml2Ctrl.ui = uiDelegateMock;

        configServiceMock = mock(ConfigurationService.class);
        authnReqGenMock = mock(AuthnRequestGenerator.class);

        loggerMock = mock(Log4jLogger.class);

        doNothing().when(saml2Ctrl).storeRequestIdInSession(anyString());
        doReturn(TEST_RELAY_STATE).when(saml2Ctrl).getRelayState();
        doReturn(authnReqGenMock).when(saml2Ctrl).getAuthnRequestGenerator();
        doReturn(requestMock).when(saml2Ctrl.ui).getRequest();
        doReturn(loggerMock).when(saml2Ctrl).getLogger();

        doReturn(DUMMY_AUTHREQUEST).when(authnReqGenMock)
                .getEncodedAuthnRequest();
        doReturn(DUMMY_RELAYSTATE).when(saml2Ctrl).getRelayState();
        doReturn(new URL(DUMMY_ACSURL)).when(saml2Ctrl).getAcsUrl();
        doReturn(DUMMY_REQID).when(authnReqGenMock).getRequestId();
        doNothing().when(saml2Ctrl).storeRequestIdInSession(anyString());
        doReturn(authSett).when(saml2Ctrl).getAuthenticationSettings();
        doReturn("https://some.different.acs.url.de").when(authSett).getIdentityProviderURL();
        doReturn("some_issuerid").when(authSett).getIssuer();
    }

    @Test
    public void initModelAndCheckForErrors_OK() throws NotExistentTenantException {
        // given

        // when
        String outcome = saml2Ctrl.initModelAndCheckForErrors();

        // then
        assertNull(outcome);
        verify(saml2ModelMock).setEncodedAuthnRequest(
                matches(DUMMY_AUTHREQUEST));
        verify(saml2ModelMock).setRelayState(matches(DUMMY_RELAYSTATE));
        verify(saml2ModelMock).setAcsUrl(matches(DUMMY_ACSURL));
        verify(saml2Ctrl).storeRequestIdInSession(matches(DUMMY_REQID));
    }

    @Test
    public void initModelAndCheckForErrors_AuthnRequestError_Portal()
            throws Exception {
        // given
        doThrow(new SAML2AuthnRequestException()).when(authnReqGenMock)
                .getEncodedAuthnRequest();
        doReturn(Boolean.FALSE).when(saml2Ctrl).isOnMarketplace();

        // when
        String outcome = saml2Ctrl.initModelAndCheckForErrors();

        // then
        assertEquals(OUTCOME_PUBLIC_ERROR_PAGE, outcome);
        verify(loggerMock).logError(anyInt(), any(Exception.class),
                eq(LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED));
        verify(uiDelegateMock).handleError(anyString(),
                matches(ERROR_GENERATE_AUTHNREQUEST));
    }

    @Test
    public void initModelAndCheckForErrors_AuthnRequestError_MP()
            throws Exception {
        // given
        doThrow(new SAML2AuthnRequestException()).when(authnReqGenMock)
                .getEncodedAuthnRequest();
        doReturn(Boolean.TRUE).when(saml2Ctrl).isOnMarketplace();

        // when
        String outcome = saml2Ctrl.initModelAndCheckForErrors();

        // then
        assertEquals(OUTCOME_MARKETPLACE_ERROR, outcome);
        verify(loggerMock).logError(anyInt(), any(Exception.class),
                eq(LogMessageIdentifier.ERROR_AUTH_REQUEST_GENERATION_FAILED));
        verify(uiDelegateMock).handleError(anyString(),
                matches(ERROR_GENERATE_AUTHNREQUEST));
    }

    @Test
    public void initModelAndCheckForErrors_URLError_Portal()
            throws Exception {
        // given
        doThrow(new MalformedURLException()).when(saml2Ctrl).getAcsUrl();
        doReturn(Boolean.FALSE).when(saml2Ctrl).isOnMarketplace();

        // when
        String outcome = saml2Ctrl.initModelAndCheckForErrors();

        // then
        assertEquals(OUTCOME_PUBLIC_ERROR_PAGE, outcome);
        verify(loggerMock).logError(anyInt(), any(Exception.class),
                eq(LogMessageIdentifier.ERROR_MISSING_IDP_URL));
        verify(uiDelegateMock).handleError(anyString(),
                matches(ERROR_INVALID_IDP_URL));
    }

    @Test
    public void initModelAndCheckForErrors_URLError_MP()
            throws Exception {
        // given
        doThrow(new MalformedURLException()).when(saml2Ctrl).getAcsUrl();
        doReturn(Boolean.TRUE).when(saml2Ctrl).isOnMarketplace();

        // when
        String outcome = saml2Ctrl.initModelAndCheckForErrors();

        // then
        assertEquals(OUTCOME_MARKETPLACE_ERROR, outcome);
        verify(loggerMock).logError(anyInt(), any(Exception.class),
                eq(LogMessageIdentifier.ERROR_MISSING_IDP_URL));
        verify(uiDelegateMock).handleError(anyString(),
                matches(ERROR_INVALID_IDP_URL));
    }

    @Test
    public void getAcsUrl_OK() throws Exception {
        // given
        final String expected = "https://some.different.acs.url.de";
        doCallRealMethod().when(saml2Ctrl).getAcsUrl();

        // when
        String actual = saml2Ctrl.getAcsUrl().toExternalForm();

        // then
        assertEquals(expected, actual);
    }

    @Test(expected = MalformedURLException.class)
    public void getAcsUrl_UIError() throws Exception {
        // given
        doReturn("nope").when(authSett).getIdentityProviderURL();
        doCallRealMethod().when(saml2Ctrl).getAcsUrl();

        // when
        saml2Ctrl.getAcsUrl().toExternalForm();

        // then exception
    }

    @Test(expected = MalformedURLException.class)
    public void getAcsUrl_UIErrorNull() throws Exception {
        // given
        doReturn(null).when(authSett).getIdentityProviderURL();
        doCallRealMethod().when(saml2Ctrl).getAcsUrl();

        // when
        saml2Ctrl.getAcsUrl().toExternalForm();

        // then exception
    }

    @Test
    public void getIssuer_OK() throws Exception {
        // given
        final String expectedIssuer = "some_issuerid";

        // when
        String actualIssuer = saml2Ctrl.getIssuer();

        // then
        assertEquals(expectedIssuer, actualIssuer);
    }

    @Test(expected = SAML2AuthnRequestException.class)
    public void getIssuer_Error() throws Exception {
        // given

        doReturn(null).when(authSett).getIssuer();

        // when
        saml2Ctrl.getIssuer();

        // then exception
        fail();
    }

    @Test
    public void getSaml2PostUrl() {
        // given
        doReturn(
                new StringBuffer(
                        "http://ttttttttt:8180/oscm-portal/saml2/redirectToIdp.jsf"))
                .when(requestMock).getRequestURL();
        doReturn("/saml2/redirectToIdp.jsf").when(requestMock).getServletPath();

        // when
        String url = saml2Ctrl.getSaml2PostUrl();

        // then
        assertEquals(url, "http://ttttttttt:8180/oscm-portal"
                + Saml2Ctrl.SAML_SP_REDIRECT_IFRAME);

    }
}
