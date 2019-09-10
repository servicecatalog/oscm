/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 30.07.2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.types.exception.MarketplaceRemovedException;
import org.oscm.ui.common.Constants;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/** @author goebel */
public class OidcFilterTest {

  OidcFilter filter = spy(new OidcFilter());

  private HttpServletRequest requestMock;
  private HttpServletResponse responseMock;
  private HttpSession httpSessionMock;
  private FilterChain chainMock;
  AuthenticationSettings as;
  ArgumentCaptor<String> ac;

  private String tenantID = null;
  private final String DEFAULT_TENANT_ID = null;

  private String marketplaceId = "SampleMP";

  private String token;

  @SuppressWarnings("boxing")
  @Before
  public void setup() throws Exception {

    requestMock = mock(HttpServletRequest.class);
    responseMock = mock(HttpServletResponse.class);
    chainMock = mock(FilterChain.class);
    doNothing().when(chainMock).doFilter(any(), any());

    filter.excludeUrlPattern = "(.*/a4j/.*|^/marketplace/[^/\\?#]*([\\?#].*)?)";

    filter.authSettings = as = mock(AuthenticationSettings.class);
    doNothing().when(as).init(anyString());
    when(as.isServiceProvider()).thenReturn(true);

    TenantResolver tr = mockTenantResolver();
    filter.setTenantResolver(tr);

    mockSession();

    doReturn("oscm-portal/marketplace/").when(requestMock).getServletPath();

    mockRequestURL();
    doNothing().when(filter).makeTokenValidationRequest(any(), any(), any(), any());
    ac = ArgumentCaptor.forClass(String.class);

    token = null;
  }

  @Test
  public void doFilter() throws IOException, ServletException {
    // given
    givenMPLoginWithoutParameter();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());
    assertRedirectUrl(ac.getValue());
  }

  @Test
  public void doFilter_sessionTokenExists() throws IOException, ServletException {
    // given
    givenTokenFromSession();
    givenMPLoginWithoutParameter();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(0)).sendRedirect(any());
  }

  @Test
  public void doFilter_requestTokenExists() throws IOException, ServletException {
    // given
    givenTokenFromRequest();
    givenMPLoginWithoutParameter();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(any());
  }

  @Test
  public void doFilter_noMPLLogin() throws IOException, ServletException {
    // given
    givenNoMPLogin();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());
    assertRedirectUrl(ac.getValue());
  }

  @Test
  public void doFilter_withLoginParameter() throws IOException, ServletException {
    // given
    givenCustomerTenant();
    givenMPLoginWithParameter();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());

    assertRedirectUrl(ac.getValue());
    assertCustomTenantId(ac.getValue());
  }

  @Test
  public void doFilter_Portal_withTenantID() throws IOException, ServletException {
    // given
    givenCustomerTenant();
    givenPortalLoginWithTenantId();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());

    assertRedirectUrl(ac.getValue());
    assertCustomTenantId(ac.getValue());
  }

  @Test
  public void doFilter_Portal_withMarketplaceID() throws IOException, ServletException {
    // given
    givenCustomerTenant();
    givenPortalLoginWithMarketplaceId();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());

    assertRedirectUrl(ac.getValue());
    assertCustomTenantId(ac.getValue());
  }

  @Test
  public void doFilter_MarketplaceRemovedException() throws Exception {
    // given
    givenCustomerTenant();
    givenMPLoginWithRemovedMarketplace();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);
    verify(filter, times(1)).forward(ac.capture(), any(), any());

    // then
    assertEquals("/public/error.jsf", ac.getValue());
  }

  @Test
  public void doFilter_Destroy() {
    filter.destroy();
  }

  protected void assertRedirectUrl(String anUrl) throws MalformedURLException {
    URL url = new URL(anUrl);
    assertEquals("oscmhost", url.getHost());
    assertEquals("https", url.getProtocol());
    assertEquals("/oscm-identity/login", url.getPath());
  }

  protected void assertCustomTenantId(String anUrl) throws MalformedURLException {
    String query = new URL(anUrl).getQuery();

    assertNotNull(query);
    assertEquals(query, Boolean.TRUE, Boolean.valueOf(query.contains("tenantId=CustomerXY")));
  }

  private StringBuffer sb(String val) {
    StringBuffer sb = new StringBuffer();
    sb.append(val);
    return sb;
  }

  String enc(String val) {
    try {
      return URLEncoder.encode(val, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private void givenMPLoginWithoutParameter() {
    doReturn(sb("https://oscmhost:8081/oscm-portal/marketplace/index.jsf"))
        .when(requestMock)
        .getRequestURL();
  }

  private void givenMPLoginWithParameter() {
    final String mId = String.format("\"%s\"", marketplaceId);
    doReturn(sb("https://oscmhost:8081/oscm-portal/marketplace/index.jsf?mId=" + enc(mId)))
        .when(requestMock)
        .getRequestURL();
  }

  private void givenMPLoginWithRemovedMarketplace() throws Exception {

    final String mId = String.format("\"%s\"", marketplaceId);
    doReturn(sb("https://oscmhost:8081/oscm-portal/marketplace/index.jsf?mId=" + enc(mId)))
        .when(requestMock)
        .getRequestURL();

    doNothing().when(filter).forward(any(), any(), any());

    TenantResolver tr = mockTenantResolver();
    filter.setTenantResolver(tr);

    doThrow(new MarketplaceRemovedException()).when(tr).getTenantID(any(), any());
  }

  private void givenNoMPLogin() {
    final String mId = String.format("\"%s\"", marketplaceId);
    doReturn(sb("https://oscmhost:8081/oscm-portal/index.jsf?mId=" + enc(mId)))
        .when(requestMock)
        .getRequestURL();
  }

  private void givenPortalLoginWithTenantId() {
    final String tId = String.format("\"%s\"", tenantID);
    doReturn(sb("https://oscmhost:8081/oscm-portal/index.jsf?tenantId=" + enc(tId)))
        .when(requestMock)
        .getRequestURL();
  }

  private void givenPortalLoginWithMarketplaceId() {
    final String mId = String.format("\"%s\"", marketplaceId);
    doReturn(sb("https://oscmhost:8081/oscm-portal/index.jsf?mId=" + enc(mId)))
        .when(requestMock)
        .getRequestURL();
  }

  private void givenDefaultTenant() {
    tenantID = null;
  }

  private void givenCustomerTenant() {
    tenantID = "CustomerXY";
  }

  private void givenTokenFromSession() {
    token = "aVerryLongTokenStringCanBeFoundHere";
  }

  private void givenTokenFromRequest() {
    doReturn("aVerryLongTokenStringCanBeFoundHere").when(requestMock).getParameter(eq("id_token"));
    doReturn("aVerryLongTokenStringCanBeFoundHere")
        .when(requestMock)
        .getParameter(eq("access_token"));
  }

  protected void mockRequestURL() {
    doAnswer(
            (new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                StringBuffer url = requestMock.getRequestURL();
                try {
                  return new URL(url.toString()).getQuery();
                } catch (MalformedURLException e) {
                  return null;
                }
              }
            }))
        .when(requestMock)
        .getQueryString();
  }

  protected TenantResolver mockTenantResolver() throws MarketplaceRemovedException {
    TenantResolver tr = mock(TenantResolver.class);
    doAnswer(
            (new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return tenantID;
              }
            }))
        .when(tr)
        .getTenantID(any(), any());

    return tr;
  }

  protected void mockSession() {
    httpSessionMock = mock(HttpSession.class);
    doAnswer(
            (new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return marketplaceId;
              }
            }))
        .when(httpSessionMock)
        .getAttribute(eq(Constants.REQ_PARAM_MARKETPLACE_ID));

    doAnswer(
            (new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return token;
              }
            }))
        .when(httpSessionMock)
        .getAttribute(eq(Constants.SESS_ATTR_ID_TOKEN));

    doAnswer(
            (new Answer<String>() {
              @Override
              public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return token;
              }
            }))
        .when(httpSessionMock)
        .getAttribute(eq(Constants.SESS_ATTR_ACCESS_TOKEN));

    doReturn(httpSessionMock).when(requestMock).getSession();
  }
}
