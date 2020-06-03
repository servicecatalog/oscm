/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 03.06.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.operatorservice.bean;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import javax.ejb.EJBAccessException;
import javax.ejb.SessionContext;
import javax.interceptor.InvocationContext;

import org.junit.Before;
import org.junit.Test;

/** @author goebel */
public class UserRolesInterceptorTest {

  private UserRolesInterceptor interceptor;
  private InvocationContext invokeContext;

  @Before
  public void setup() throws Exception {
    OperatorServiceBean bean = new OperatorServiceBean();
    interceptor = new UserRolesInterceptor();
    interceptor.ejbCtx = mock(SessionContext.class);
    invokeContext = mock(InvocationContext.class);
    doReturn(null).when(invokeContext).proceed();
  }

  @Test
  public void checkAccess_getConfigurationSettings_Success() throws Exception {
    // given
    givenPlatformOperatorRole();
    InvocationContext ic = givenInvokation("getConfigurationSettings");

    // when
    interceptor.checkAccess(ic);

    // then
    verify(interceptor.ejbCtx, times(1)).isCallerInRole("PLATFORM_OPERATOR");
  }

  @Test(expected = EJBAccessException.class)
  public void checkAccess_getConfigurationSettings_NoOperatorRole() throws Exception {
    // given
    givenNoPlatformOperatorRole();
    InvocationContext ic = givenInvokation("getConfigurationSettings");

    // when
    interceptor.checkAccess(ic);
  }

  @Test
  public void checkAccess_getUnassignedUsersByOrg_Success() throws Exception {
    // given
    givenSubscriptionManagerRole();
    givenUnitAdminRole();
    givenOrgAdminRole();
    InvocationContext ic = givenInvokation("getUnassignedUsersByOrg");

    // when
    interceptor.checkAccess(ic);

    // then
    verify(interceptor.ejbCtx, times(1)).isCallerInRole(anyString());
  }

  @Test
  public void checkAccess_getUnassignedUsersByOrg_OneRole() throws Exception {
    // given
    givenUnitAdminRole();
    InvocationContext ic = givenInvokation("getUnassignedUsersByOrg");

    // when
    interceptor.checkAccess(ic);

    // then
    verify(interceptor.ejbCtx, times(1)).isCallerInRole("UNIT_ADMINISTRATOR");
  }

  @Test(expected = EJBAccessException.class)
  public void checkAccess_getConfigurationSettings_NoRole() throws Exception {
    // given
    givenNoRole();
    InvocationContext ic = givenInvokation("getUnassignedUsersByOrg");

    // when
    interceptor.checkAccess(ic);
  }

  @Test
  public void checkAccess_registerOrganization_Success() throws Exception {
    // given
    givenPlatformOperatorRole();
    InvocationContext ic = givenInvokation("registerOrganization");

    // when
    interceptor.checkAccess(ic);

    // then
    verify(interceptor.ejbCtx, times(1)).isCallerInRole("PLATFORM_OPERATOR");
  }

  @Test(expected = EJBAccessException.class)
  public void checkAccess_registerOrganization_NoRole() throws Exception {
    // given
    givenNoRole();
    InvocationContext ic = givenInvokation("registerOrganization");

    // when
    interceptor.checkAccess(ic);
  }

  public void checkAccess_getOrganization_Success() throws Exception {
    // given
    givenNoRole();
    InvocationContext ic = givenInvokation("getOrganization");

    // when
    interceptor.checkAccess(ic);

    // then
    verify(interceptor.ejbCtx, never()).isCallerInRole(anyString());
  }

  InvocationContext givenInvokation(String method) {
    Method m = getMethod(method);
    doReturn(m).when(invokeContext).getMethod();
    return invokeContext;
  }

  void givenOrgAdminRole() {
    doReturn(Boolean.TRUE).when(interceptor.ejbCtx).isCallerInRole("ORGANIZATION_ADMIN");
  }

  void givenUnitAdminRole() {
    doReturn(Boolean.TRUE).when(interceptor.ejbCtx).isCallerInRole("UNIT_ADMINISTRATOR");
  }

  void givenSubscriptionManagerRole() {
    doReturn(Boolean.TRUE).when(interceptor.ejbCtx).isCallerInRole("SUBSCRIPTION_MANAGER");
  }

  void givenNoSubscriptionManagerRole() {
    doReturn(Boolean.FALSE).when(interceptor.ejbCtx).isCallerInRole("SUBSCRIPTION_MANAGER");
  }

  void givenPlatformOperatorRole() {
    doReturn(Boolean.TRUE).when(interceptor.ejbCtx).isCallerInRole("PLATFORM_OPERATOR");
  }

  void givenNoPlatformOperatorRole() {
    doReturn(Boolean.FALSE).when(interceptor.ejbCtx).isCallerInRole("PLATFORM_OPERATOR");
  }

  void givenNoRole() {
    doReturn(Boolean.FALSE).when(interceptor.ejbCtx).isCallerInRole(anyString());
  }

  Method getMethod(String name) {
    for (Method m : OperatorServiceBean.class.getDeclaredMethods()) {
      if (m.getName().equals(name)) {
        return m;
      }
    }
    throw new RuntimeException(String.format("Methode %s Not found", name));
  }
}
