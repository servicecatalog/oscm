package org.oscm.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class EJBClient {

  public static <T> T getEJB(
      String baseUrl, String authMode, Class<T> remoteInterface, String userName, String password)
      throws NamingException {

    if ("OIDC".equals(authMode)) {
      password = "WS" + password;
    }

    Properties properties = new Properties();
    properties.put(Context.SECURITY_PRINCIPAL, userName);
    properties.put(Context.SECURITY_CREDENTIALS, password);
    properties.put(
        "java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
    properties.put("java.naming.provider.url", baseUrl + "/tomee/ejb");

    Context context = new InitialContext(properties);
    T service = (T) context.lookup(remoteInterface.getName());
    return service;
  }
}
