/**
 * ***************************************************************************** Copyright FUJITSU
 * LIMITED 2018 *****************************************************************************
 */
package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.FacesContextStub;

public class SessionBeanTest {

  private static final String MARKETPLACE_ID = "FUJITSU";

  private static final String WHITE_LABEL_PATH = "/oscm-portal";
  private static final String WHITE_LABEL_URL =
      "http://localhost:8180/oscm-portal/marketplace/css/mp.css";

  private static final String WHITE_LABEL_URI = "/oscm-portal/marketplace/css/mp.css";

  private static final String DEFAULT_BOOTSTRAP_URL =
      "http://localhost:8180/oscm-portal/customBootstrap/css/darkCustom.css";

  private static final String DEFAULT_BOOTSTRAP_URI =
      "/oscm-portal/customBootstrap/css/darkCustom.css";

  private static final String WHITE_LABEL_BASE_URI = "/marketplace";

  private static final String BRANDING_BASE_URL = "http://localhost:8180/oscm-portal/marketplace";

  private static final String BRANDING_URL = BRANDING_BASE_URL + "/css/mp_custom.css";

  private static final String CUSTOM_BOOTSTRAP_URL =
      BRANDING_BASE_URL + "/css/bootstrap_custom.css";

  private SessionBean sessionBean;
  private MarketplaceService marketplaceServiceMock;
  private HttpServletRequest req;
  private Cookie[] cookies;

  private FacesContext fcContextMock;

  @Before
  public void setUp() throws Exception {
    sessionBean = spy(new SessionBean());
    doReturn(MARKETPLACE_ID).when(sessionBean).getMarketplaceId();

    // Mock the faces context of the SessionBean
    fcContextMock = mock(FacesContext.class);
    doReturn(fcContextMock).when(sessionBean).getFacesContext();

    // Mock the external context of the faces context
    ExternalContext extContextMock = mock(ExternalContext.class);
    doReturn(extContextMock).when(fcContextMock).getExternalContext();
    doReturn(WHITE_LABEL_PATH).when(extContextMock).getRequestContextPath();
    req = mock(HttpServletRequest.class);
    doReturn(req).when(extContextMock).getRequest();
    doReturn(req).when(sessionBean).getRequest();

    // Mock the marketplace service
    marketplaceServiceMock = mock(MarketplaceService.class);
    doReturn(marketplaceServiceMock).when(sessionBean).getMarketplaceService();
    cookies = new Cookie[1];
    doReturn(cookies).when(req).getCookies();
  }

  @Test
  public void isAutoOpenMpLogonDialog_true() {
    // given
    doReturn(Boolean.TRUE.toString())
        .when(req)
        .getParameter(Constants.REQ_PARAM_AUTO_OPEN_MP_LOGIN_DIALOG);

    // when
    boolean value = sessionBean.isAutoOpenMpLogonDialog();

    // then
    assertTrue(value);
  }

  @Test
  public void isAutoOpenMpLogonDialog_false() {
    // given
    doReturn(Boolean.FALSE.toString())
        .when(req)
        .getParameter(Constants.REQ_PARAM_AUTO_OPEN_MP_LOGIN_DIALOG);

    // when
    boolean value = sessionBean.isAutoOpenMpLogonDialog();

    // then
    assertFalse(value);
  }

  @Test
  public void isAutoOpenMpLogonDialog_notSet() {
    // given
    doReturn(null).when(req).getParameter(Constants.REQ_PARAM_AUTO_OPEN_MP_LOGIN_DIALOG);

    // when
    boolean value = sessionBean.isAutoOpenMpLogonDialog();

    // then
    assertFalse(value);
  }

  @Test
  public void isAutoOpenMpLogonDialog_someBullshit() {
    // given
    doReturn("bullshit").when(req).getParameter(Constants.REQ_PARAM_AUTO_OPEN_MP_LOGIN_DIALOG);

    // when
    boolean value = sessionBean.isAutoOpenMpLogonDialog();

    // then
    assertFalse(value);
  }

  @Test
  public void testGetWhiteLabeBrandingUrl() throws Exception {
    assertEquals(WHITE_LABEL_URI, sessionBean.getWhiteLabelBrandingUrl());
  }

  @Test
  public void testGetMarketplaceBrandUrl_BrandingUrlSet() throws Exception {
    doReturn(BRANDING_URL).when(marketplaceServiceMock).getBrandingUrl(MARKETPLACE_ID);
    String result = sessionBean.getMarketplaceBrandUrl();
    assertEquals(BRANDING_URL, result);
  }

  @Test
  public void testGetMarketplaceBrandUrl_NullBrandingUrl() throws Exception {
    doReturn(null).when(marketplaceServiceMock).getBrandingUrl(MARKETPLACE_ID);
    String result = sessionBean.getMarketplaceBrandUrl();
    assertEquals(WHITE_LABEL_URI, result);
  }

  @Test
  public void testGetMarketplaceBrandUrl_ObjectNotFoundException() throws Exception {
    doThrow(new ObjectNotFoundException())
        .when(marketplaceServiceMock)
        .getBrandingUrl(MARKETPLACE_ID);
    String result = sessionBean.getMarketplaceBrandUrl();
    assertEquals(WHITE_LABEL_URI, result);
  }

  @Test
  public void getMarketplaceBrandBaseUrl_whitelabel() throws Exception {
    // given
    doReturn(WHITE_LABEL_URL).when(marketplaceServiceMock).getBrandingUrl(MARKETPLACE_ID);
    sessionBean.setMarketplaceBrandUrl(WHITE_LABEL_URI);

    // when
    String result = sessionBean.getMarketplaceBrandBaseUrl();

    // then
    assertEquals(WHITE_LABEL_BASE_URI, result);
  }

  @Test
  public void testGetDefaultBootstrapUrl_NullUrl() throws Exception {
    // TODO Replace new method.
    // doReturn(null).when(marketplaceServiceMock).getCutsomBootstrapUrl(MARKETPLACE_ID);
    String result = sessionBean.getCustomBootstrapUrl();
    assertEquals(DEFAULT_BOOTSTRAP_URI, result);
  }

  @Test
  public void getDefaultBootstrap_ObjectNotFoundException() throws Exception {
    doThrow(new ObjectNotFoundException())
        .when(marketplaceServiceMock)
        .getBrandingUrl(MARKETPLACE_ID);
    String result = sessionBean.getCustomBootstrapUrl();
    assertEquals(DEFAULT_BOOTSTRAP_URI, result);
  }

  @Test
  public void getCustomBootstrap_DefaultBootstrap() throws Exception {
    // given
    // TODO Replace new method.
    // doReturn(DEFAULT_BOOTSTRAP_URL).when(marketplaceServiceMock).getCustomBootstrapUrl(MARKETPLACE_ID);
    sessionBean.setCustomBootstrapUrl(DEFAULT_BOOTSTRAP_URL);

    // when
    String result = sessionBean.getCustomBootstrapUrl();

    // then
    assertEquals(DEFAULT_BOOTSTRAP_URL, result);
  }

  @Test
  public void getCustomBootstrap_customBootstrap() throws Exception {
    // given
    // TODO replace new method.
    // doReturn(CUSTOM_BOOTSTRAP_URL).when(marketplaceServiceMock).getCustomBootstrapUrl(MARKETPLACE_ID);
    sessionBean.setCustomBootstrapUrl(CUSTOM_BOOTSTRAP_URL);

    // when
    String result = sessionBean.getCustomBootstrapUrl();

    // then
    assertEquals(CUSTOM_BOOTSTRAP_URL, result);
  }

  @Test
  public void getCustomBootstrap_null_set() throws Exception {
    // given
    // TODO replace new method.
    // doReturn(BOOTSTRAP_URL).when(marketplaceServiceMock).getCustomBootstrapUrl(MARKETPLACE_ID);
    sessionBean.setCustomBootstrapUrl(null);

    // when
    String result = sessionBean.getCustomBootstrapUrl();

    // then
    assertEquals(DEFAULT_BOOTSTRAP_URI, result);
  }

  @Test
  public void getMarketplaceBrandBaseUrl_branded_wrongURL() throws Exception {
    // given
    final String brUrl = BRANDING_BASE_URL + "/css/error.jsp";

    doReturn(brUrl).when(marketplaceServiceMock).getBrandingUrl(MARKETPLACE_ID);
    sessionBean.setMarketplaceBrandUrl(brUrl);

    // when
    String result = sessionBean.getMarketplaceBrandBaseUrl();

    // then
    assertEquals(WHITE_LABEL_BASE_URI, result);
  }

  @Test
  public void getMarketplaceBrandBaseUrl_branded_wrongURL2() throws Exception {
    // given
    final String brUrl = BRANDING_BASE_URL + "/css1/error.css";

    doReturn(brUrl).when(marketplaceServiceMock).getBrandingUrl(MARKETPLACE_ID);
    sessionBean.setMarketplaceBrandUrl(brUrl);

    // when
    String result = sessionBean.getMarketplaceBrandBaseUrl();

    // then
    assertEquals(WHITE_LABEL_BASE_URI, result);
  }

  @Test
  public void geCustomBootstrapUrl_defaultBootstrap() throws Exception {
    // given
    // TODO replace method with MarketplaceService mock.
    // doReturn(DEFAULT_BOOTSTRAP_URL).when(marketplaceServiceMock).getCustomBootstrapUrl(MARKETPLACE_ID);
    sessionBean.setCustomBootstrapUrl(DEFAULT_BOOTSTRAP_URL);

    // when
    String result = sessionBean.getCustomBootstrapUrl();

    // then
    assertEquals(DEFAULT_BOOTSTRAP_URL, result);
  }

  @Test
  public void isNameSequenceReversed_Japan() throws Exception {
    setCurrentLocale(Locale.JAPAN);
    boolean result = sessionBean.getNameSequenceReversed();
    assertTrue(result);
  }

  @Test
  public void isNameSequenceReversed_Japanese() throws Exception {
    setCurrentLocale(Locale.JAPANESE);
    boolean result = sessionBean.getNameSequenceReversed();
    assertTrue(result);
  }

  @Test
  public void isNameSequenceReversed_German() throws Exception {
    setCurrentLocale(Locale.GERMAN);
    boolean result = sessionBean.getNameSequenceReversed();
    assertFalse(result);
  }

  @Test
  public void isNameSequenceReversed_English() throws Exception {
    setCurrentLocale(Locale.ENGLISH);
    boolean result = sessionBean.getNameSequenceReversed();
    assertFalse(result);
  }

  @Test
  public void determineSelectedServiceKeyForCustomer_serviceKeyLost() {
    // given selected service by cookie
    sessionBean.setSelectedServiceKeyForCustomer(0);
    cookies[0] = new Cookie(Constants.REQ_PARAM_SERVICE_KEY, "1002");
    doReturn("").when(req).getParameter(Constants.REQ_PARAM_SELECTED_SERVICE_KEY);
    // when
    long key = sessionBean.determineSelectedServiceKeyForCustomer();

    // then
    assertEquals(1002L, key);
  }

  @Test
  public void determineSelectedServiceKeyForCustomer_invalidServiceKey() {
    // given selected service by cookie
    sessionBean.setSelectedServiceKeyForCustomer(0);
    cookies[0] = new Cookie(Constants.REQ_PARAM_SERVICE_KEY, "1002");
    doReturn("Invalid").when(req).getParameter(Constants.REQ_PARAM_SELECTED_SERVICE_KEY);
    // when
    long key = sessionBean.determineSelectedServiceKeyForCustomer();

    // then
    assertEquals(0, key);
  }

  @Test
  public void isValidServiceKey() {
    assertFalse(SessionBean.isValidServiceKey(SessionBean.SERIVE_KEY_ERROR));
    assertFalse(SessionBean.isValidServiceKey(SessionBean.SERIVE_KEY_NOT_SET));
    assertTrue(SessionBean.isValidServiceKey(123L));
  }

  private void setCurrentLocale(Locale locale) {
    fcContextMock = new FacesContextStub(locale);
    UIViewRoot root = mock(UIViewRoot.class);
    doReturn(locale).when(root).getLocale();
    fcContextMock.setViewRoot(root);
  }
}
