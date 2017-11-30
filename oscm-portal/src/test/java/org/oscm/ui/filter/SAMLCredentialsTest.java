/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.07.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.logging.Log4jLogger;
import org.oscm.saml2.api.SAMLResponseExtractor;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.Constants;
import org.oscm.internal.types.exception.UserIdNotFoundException;

/**
 * @author stavreva
 * 
 */
public class SAMLCredentialsTest {

    private static final byte[] VALID_SAML_RESPONSE = "valid".getBytes();
    private static final String INVALID_SAML_RESPONSE = "invalid";
    private static final byte[] SAML_RESPONSE_WITHOUT_USERID = "without_userid".getBytes();
    private static final String REQ_ID = "reqid";
    private static final String USER_ID = "userid";
    private static final String CALLER_UI = "UI";
    private static final String TENANT_ID = "TENANT_ID";

    private static HttpServletRequest reqMock;
    private static HttpSession sessionMock;
    private static Log4jLogger loggerMock;
    private static SAMLResponseExtractor samlResponseMock;

    @BeforeClass
    public static void setup() throws Exception {
        reqMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        loggerMock = mock(Log4jLogger.class);
        samlResponseMock = mock(SAMLResponseExtractor.class);

        doReturn(sessionMock).when(reqMock).getSession();

        doReturn(USER_ID).when(samlResponseMock).getUserId(VALID_SAML_RESPONSE.toString());
        doReturn(TENANT_ID).when(samlResponseMock).getTenantID(anyString());

        doReturn(VALID_SAML_RESPONSE).when(samlResponseMock).decode(
                VALID_SAML_RESPONSE.toString());
        doReturn(SAML_RESPONSE_WITHOUT_USERID).when(samlResponseMock).decode(
                SAML_RESPONSE_WITHOUT_USERID.toString());
        doThrow(new UnsupportedEncodingException()).when(samlResponseMock)
                .decode(INVALID_SAML_RESPONSE);

        doThrow(new UserIdNotFoundException()).when(samlResponseMock)
                .getUserId(SAML_RESPONSE_WITHOUT_USERID.toString());
        doThrow(new UserIdNotFoundException()).when(samlResponseMock)
                .getUserId(INVALID_SAML_RESPONSE);
    }

    @Test
    public void getUserIdFromSAMLResponse_nullResponse() throws Exception {

        // given
        doReturn(null).when(reqMock).getParameter("SAMLResponse");

        // when
        SAMLCredentials samlUserCredentials = new SAMLCredentials(reqMock);

        // then
        assertNull(samlUserCredentials.getUserId());
        assertNull(samlUserCredentials.generatePassword());
    }

    @Test
    public void getUserIdFromSAMLResponse_nullRequestId() throws Exception {

        // given
        doReturn(VALID_SAML_RESPONSE.toString()).when(reqMock)
                .getParameter("SAMLResponse");
        doReturn(null).when(sessionMock).getAttribute(
                Constants.SESS_ATTR_IDP_REQUEST_ID);

        // when
        SAMLCredentials samlUserCredentials = new SAMLCredentials(reqMock);

        // then
        assertNull(samlUserCredentials.getUserId());
        assertNull(samlUserCredentials.generatePassword());
    }

    @Test
    public void getUserIdFromSAMLResponse_validResponse() throws Exception {

        // given
        doReturn(VALID_SAML_RESPONSE.toString()).when(reqMock)
                .getParameter("SAMLResponse");
        doReturn(REQ_ID).when(sessionMock).getAttribute(
                Constants.SESS_ATTR_IDP_REQUEST_ID);

        // when
        SAMLCredentials spySamlCredentials = spy(new SAMLCredentials(reqMock));
        when(spySamlCredentials.getTenantId()).thenReturn(TENANT_ID);
        when(spySamlCredentials.getLogger()).thenReturn(loggerMock);
        when(spySamlCredentials.getSAMLResponse()).thenReturn(samlResponseMock);

        // then
        assertEquals(USER_ID, spySamlCredentials.getUserId());
        assertEquals(CALLER_UI + REQ_ID + TENANT_ID + new String(VALID_SAML_RESPONSE),
                spySamlCredentials.generatePassword());
    }

    @Test
    public void getUserIdFromSAMLResponse_responseWithoutUserId()
            throws Exception {

        // given
        doReturn(SAML_RESPONSE_WITHOUT_USERID.toString()).when(reqMock).getParameter(
                "SAMLResponse");
        doReturn(REQ_ID).when(sessionMock).getAttribute(
                Constants.SESS_ATTR_IDP_REQUEST_ID);

        // when
        SAMLCredentials spySamlCredentials = spy(new SAMLCredentials(reqMock));
        when(spySamlCredentials.getLogger()).thenReturn(loggerMock);
        when(spySamlCredentials.getSAMLResponse()).thenReturn(samlResponseMock);
        when(spySamlCredentials.getTenantId()).thenReturn(TENANT_ID);
        spySamlCredentials.getUserId();

        // then
        verify(loggerMock, times(1))
                .logError(
                        eq(LogMessageIdentifier.ERROR_GET_USER_FROM_SAML_RESPONSE_FAILED),
                        eq(new String(SAML_RESPONSE_WITHOUT_USERID)));
        assertEquals(CALLER_UI + REQ_ID + TENANT_ID + new String(SAML_RESPONSE_WITHOUT_USERID),
                spySamlCredentials.generatePassword());
    }

    @Test
    public void getUserIdFromSAMLResponse_invalid() throws Exception {

        // given
        doReturn(INVALID_SAML_RESPONSE).when(reqMock).getParameter(
                "SAMLResponse");

        // when
        SAMLCredentials spySamlCredentials = spy(new SAMLCredentials(reqMock));
        when(spySamlCredentials.getLogger()).thenReturn(loggerMock);
        when(spySamlCredentials.getSAMLResponse()).thenReturn(samlResponseMock);
        when(spySamlCredentials.getRequestId()).thenReturn("reqId");
        spySamlCredentials.getUserId();
        spySamlCredentials.generatePassword();

        // then
        verify(loggerMock, times(2)).logError(
                eq(LogMessageIdentifier.ERROR_DECODE_SAML_RESPONSE_FAILED),
                eq(INVALID_SAML_RESPONSE));
    }

}
