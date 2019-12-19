/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 08.08.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URISyntaxException;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.internal.intf.SessionService;

/**
 * @author goebel
 */
public class OidcLogoutFilterTest {
  OidcLogoutFilter filter = spy(new OidcLogoutFilter());

  private HttpServletRequest requestMock;
  private HttpServletResponse responseMock;
  private FilterChain chainMock;
  private HttpSession httpSessionMock;
  ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);

  @SuppressWarnings("boxing")
  @Before
  public void setup() throws Exception {
    requestMock = mock(HttpServletRequest.class);
    responseMock = mock(HttpServletResponse.class);
    chainMock = mock(FilterChain.class);
    mockSession();
    doNothing().when(chainMock).doFilter(any(), any());

    doReturn("yes").when(requestMock).getParameter(eq("logout"));

    SessionService ssm = mock(SessionService.class);
    doReturn(ssm).when(filter).getSessionService();
    doReturn(1).when(ssm).deletePlatformSession(anyString());
  }

  @Test
  public void doFilter() throws Exception {
    // given
    mockLogoutRequest();
    
    String logoutUrl = givenLogoutUrl();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());
    assertEquals(logoutUrl, ac.getValue());
  }   

  @Test
  public void doFilter_mpUrl() throws Exception {
    // given
    mockLogoutRequest();
    
    String logoutUrl = givenLogoutUrl();

    // when
    filter.doFilter(requestMock, responseMock, chainMock);

    // then
    verify(responseMock, times(1)).sendRedirect(ac.capture());
    assertEquals(logoutUrl, ac.getValue());
  }
  
  @Test
  public void buildLogoutUrl()  throws Exception { 
    // given
    final String uri = "https://oscmhost:8081/oscm-portal/marketplace/logout.jsf";
    
    // when
    String url = filter.buildLogoutUrl(uri);
  
    // then
    assertEquals("https://oscmhost:9091/oscm-identity/logout?state=https://oscmhost:8081/oscm-portal/marketplace/index.jsf", url);
  }
 
  private void mockLogoutRequest() {
    doReturn(sb("https://oscmhost:8081/oscm-portal/marketplace/logout.jsf")).when(requestMock)
        .getRequestURL();
  }
  
  private StringBuffer sb(String val) {
    StringBuffer sb = new StringBuffer();
    sb.append(val);
    return sb;
  }

  protected String givenLogoutUrl() throws URISyntaxException {
    String logoutUrl = "https://oscmserver.intern.org:9091/oscm-identity/logout?state=redirectUrl";
    doReturn(logoutUrl).when(filter).buildLogoutUrl(anyString());
    return logoutUrl;
  }
  
  protected void mockSession() {
    httpSessionMock = mock(HttpSession.class);
    doReturn("045f12a6d1").when(httpSessionMock).getId();
    doReturn(httpSessionMock).when(requestMock).getSession();
  }
}
