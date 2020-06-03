/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 20.05.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.operatorservice.bean;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBAccessException;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/** @author goebel */
public class UserRolesInterceptor {

  @Resource SessionContext ejbCtx;

  @AroundInvoke
  public Object checkAccess(InvocationContext context) throws Exception {
    if (!hasAccess(context)) {
      throwIllegalAccess();
    }
    return context.proceed();
  }

  private void throwIllegalAccess() {
    ejbCtx.setRollbackOnly();
    throw new EJBAccessException(
        String.format("Illegal access from principal %s", getPrincipalName()));
  }

  private String getPrincipalName() {
    final Principal p = ejbCtx.getCallerPrincipal();
    return (p != null) ? p.getName() : "Unknown";
  }

  private boolean hasAccess(InvocationContext context) {
    RolesAllowed ra = context.getMethod().getDeclaredAnnotation(RolesAllowed.class);
    if (ra != null) {
      List<String> roles = Arrays.asList(ra.value());
      if (!roles.isEmpty()) {
        for (String role : roles) {
          if (ejbCtx.isCallerInRole(role)) return true;
        }
        return false;
      }
    }
    return true;
  }
}
