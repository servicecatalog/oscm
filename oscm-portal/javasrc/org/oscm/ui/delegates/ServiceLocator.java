/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: Jun 5, 2012
 *
 *******************************************************************************/
package org.oscm.ui.delegates;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.internal.types.exception.SaaSSystemException;

public class ServiceLocator {

  private static String APP_PROVIDER_URL = "http://oscm-app:8880/tomee/ejb";
  private static String APP_PLATFORM_SERVICE_JNDI_NAME =
      "oscm-app/oscm-app/org.oscm.app.v2_0.service.APPlatformServiceBeanRemote";

  public <T> T findService(final Class<T> clazz) {

    try {
      Properties p = new Properties();
      p.put(
          Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
      Context context = new InitialContext(p);
      T service = clazz.cast(context.lookup(clazz.getName()));
      return service;
    } catch (NamingException e) {
      throw new SaaSSystemException("Service lookup failed!", e);
    }
  }

  public <T> T findRemoteService(final Class<T> clazz) {

    String jndiName = getJndiName(clazz);
    try {
      Properties properties = new Properties();

      properties.put(
          Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
      properties.put(Context.PROVIDER_URL, APP_PROVIDER_URL);

      Context context = new InitialContext(properties);
      T service = clazz.cast(context.lookup(jndiName));
      return service;
    } catch (NamingException e) {
      throw new SaaSSystemException("Service lookup failed!", e);
    }
  }

  private static <T> String getJndiName(final Class<T> clazz) {

    String simpleName = clazz.getSimpleName();

    if (simpleName.contains("APPlatformService")) {
      return APP_PLATFORM_SERVICE_JNDI_NAME;
    }

    throw new SaaSSystemException(
        "Service lookup failed ! Remote access for" + clazz + "not supported");
  }
}
