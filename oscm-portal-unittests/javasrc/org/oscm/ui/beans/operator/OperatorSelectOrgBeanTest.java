/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Jun 1, 2012
 *
 * <p>*****************************************************************************
 */
package org.oscm.ui.beans.operator;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;

/** @author ZhouMin */
public class OperatorSelectOrgBeanTest {
  private VOOperatorOrganization org;
  private OperatorSelectOrgCtrl bean;
  private final ApplicationBean appBean = mock(ApplicationBean.class);
  private final OperatorService operatorService = mock(OperatorService.class);
  private HttpServletRequest request;

  @Before
  public void setup() throws Exception {
    request = mock(HttpServletRequest.class);
    bean =
        new OperatorSelectOrgCtrl() {

          private static final long serialVersionUID = -9126265695343363133L;

          @Override
          protected OperatorService getOperatorService() {
            return operatorService;
          }

          @Override
          protected HttpServletRequest getRequest() {
            return request;
          }
        };
    bean = spy(bean);
    HttpSession s = mock(HttpSession.class);
    doReturn(s).when(request).getSession();
    
    org = new VOOperatorOrganization();
    org.setOrganizationId("organizationId");
    doReturn(org.getOrganizationId()).when(s).getAttribute(eq("organizationId"));
    when(operatorService.getOrganization(anyString())).thenReturn(org);
    bean.ui = mock(UiDelegate.class);
    bean.setModel(new OperatorSelectOrgModel());
    bean.init();
    when(bean.ui.findBean(eq(OperatorSelectOrgCtrl.APPLICATION_BEAN))).thenReturn(appBean);
    when(bean.getApplicationBean()).thenReturn(appBean);
  }

  @Test
  public void getOrganization_invalidLocale() throws Exception {
    // given
    org.setLocale("en");
    List<String> localesStr = new ArrayList<String>();
    localesStr.add("en");
    localesStr.add("de");
    doReturn(localesStr).when(appBean).getActiveLocales();
    bean.setOrganizationId("orgId");

    // when
    bean.getOrganization();

    // then
    verify(appBean, times(1)).checkLocaleValidation("en");
  }

  @Test
  public void suggestOrg() throws Exception {
    // given
    org.setName("organizationName");

    doReturn("SUPPLIER").when(request).getParameter(eq(Constants.REQ_PARAM_ORGANIZATION_ROLE_TYPE));
    bean.setOrganizationId("orgId");

    // when
    Map<String, String> res = bean.getSuggestedOrgs();

    // then
    verify(operatorService, times(2)).getOrganizationIdentifiers(Matchers.any());
  }
}
