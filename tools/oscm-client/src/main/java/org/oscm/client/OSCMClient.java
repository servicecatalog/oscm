package org.oscm.client;

import org.oscm.client.configuration.Configuration;
import org.oscm.internal.intf.OperatorService;
import org.oscm.intf.IdentityService;

public class OSCMClient {

  private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
  private static final String TRUST_STORE_PWD_PROPERTY = "javax.net.ssl.trustStorePassword";

  public static void main(String[] args) throws Exception {
    Configuration configuration = new Configuration();

    System.setProperty(TRUST_STORE_PROPERTY, configuration.getTrustStoreLocation());
    System.setProperty(TRUST_STORE_PWD_PROPERTY, configuration.getTrustStorePassword());

    String baseUrl = configuration.getBaseUrl();
    String userKey = "user key";
    String userPwd = "uer password";
    String authMode = "INTERNAL or OIDC";

    // example of accessing WS
    IdentityService identityService =
        WSClient.getWS(baseUrl, authMode, IdentityService.class, userKey, userPwd);

    // example of accessing EJB
    OperatorService operatorService =
        EJBClient.getEJB(baseUrl, authMode, OperatorService.class, userKey, userPwd);
  }
}
