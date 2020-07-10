/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: Florian Walker
 *
 * <p>Creation Date: 28.04.2011
 *
 * <p>Completion Time: 28.04.2011
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.beans;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.BrandService;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.common.Constants;

/**
 * Tests the skin bean.
 *
 * @author Florian Walker
 */
public class SkinBeanTest {

  private final String stageContent = "STAGE_CONTENT_DEFAULT";
  private final String stageContentBranded = "STAGE_CONTENT_BRANDED";

  private SkinBean skinBean;

  private BrandService brandServiceMock;
  private HttpSession httpSessionMock;
  private UIViewRoot viewRootMock;

  @Before
  public void before() {
    // mock the BrandService' get getMarketplaceStage() method
    brandServiceMock = mock(BrandService.class);
    when(brandServiceMock.getMarketplaceStage(anyString(), anyString())).thenReturn(stageContent);

    // mock the HttpSession's getAttribute() method
    httpSessionMock = mock(HttpSession.class);
    when(httpSessionMock.getAttribute(anyString())).thenReturn(null);

    // mock the HttpServletRequest's getSession() method
    HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
    when(httpServletRequestMock.getSession()).thenReturn(httpSessionMock);

    // mock the ExternalContext's getRequest() method
    ExternalContext externalContextMock = mock(ExternalContext.class);
    when(externalContextMock.getRequest()).thenReturn(httpServletRequestMock);

    // mock the UIViewRoot's getLocale() method
    viewRootMock = mock(UIViewRoot.class);
    when(viewRootMock.getLocale()).thenReturn(Locale.FRANCE);

    // mock the FacesContext's getExternalContext() and getViewRoot()
    // methods
    final FacesContext facesContextMock = mock(FacesContext.class);
    when(facesContextMock.getExternalContext()).thenReturn(externalContextMock);
    when(facesContextMock.getViewRoot()).thenReturn(viewRootMock);

    // spy the SkinBean to only partial mock - overriding the methods below
    skinBean = spy(new SkinBean());
    doReturn(brandServiceMock).when(skinBean).getBrandManagementService();
    doReturn(facesContextMock).when(skinBean).getFacesContext();
    doReturn(httpServletRequestMock).when(skinBean).getRequest();
  }

  /** Anonymous case => browser locale. */
  @Test
  public void testGetMarketPlaceStage_browseLocale() {
    when(viewRootMock.getLocale()).thenReturn(Locale.ENGLISH);
    when(brandServiceMock.getMarketplaceStage(anyString(), eq(Locale.ENGLISH.getLanguage())))
        .thenReturn(stageContent);

    String expectedDiv = MessageFormat.format(SkinBean.MARKETPLACE_STAGE_BRANDED, stageContent);
    Assert.assertEquals(expectedDiv, skinBean.getMarketplaceStage());
  }

  /** vouser in session. */
  @Test
  public void testGetMarketPlaceStage_userLocaleSessionOK() {
    // given
    VOUserDetails voUser = new VOUserDetails();
    voUser.setLocale(Locale.CHINA.getLanguage());
    when(httpSessionMock.getAttribute(eq(Constants.SESS_ATTR_USER))).thenReturn(voUser);

    // when
    when(brandServiceMock.getMarketplaceStage(anyString(), eq(Locale.CHINA.getLanguage())))
        .thenReturn(stageContentBranded);
    String expectedDiv =
        MessageFormat.format(SkinBean.MARKETPLACE_STAGE_BRANDED, stageContentBranded);

    // then
    Assert.assertEquals(expectedDiv, skinBean.getMarketplaceStage());
  }

  /** session available but no vo => browser locale */
  @Test
  public void testGetMarketPlaceStage_userLocaleSessionNoVo() {
    // given
    when(brandServiceMock.getMarketplaceStage(anyString(), eq(Locale.FRANCE.getLanguage())))
        .thenReturn(stageContent);

    // then
    String expectedDiv = MessageFormat.format(SkinBean.MARKETPLACE_STAGE_BRANDED, stageContent);
    Assert.assertEquals(expectedDiv, skinBean.getMarketplaceStage());
  }

  @Test
  public void testGetMarketPlaceStage_default() {
    when(brandServiceMock.getMarketplaceStage(anyString(), eq(Locale.FRANCE.getLanguage())))
        .thenReturn("");
    when(skinBean.getRequestContextPath()).thenReturn("");
    Assert.assertEquals(
        MessageFormat.format(SkinBean.MARKETPLACE_STAGE_DEFAULT, ""),
        skinBean.getMarketplaceStage());
  }

  /** Caching test (just for the coverage) */
  @Test
  public void testGetMarketPlaceStage_cache() {
    // given
    when(viewRootMock.getLocale()).thenReturn(Locale.ENGLISH);
    when(brandServiceMock.getMarketplaceStage(anyString(), eq(Locale.ENGLISH.getLanguage())))
        .thenReturn(stageContentBranded);

    // then
    String expectedDiv =
        MessageFormat.format(SkinBean.MARKETPLACE_STAGE_BRANDED, stageContentBranded);

    // then
    Assert.assertEquals(expectedDiv, skinBean.getMarketplaceStage());
    Assert.assertEquals(expectedDiv, skinBean.getMarketplaceStage());
    Assert.assertEquals(expectedDiv, skinBean.getMarketplaceStage());
  }
}
