package org.oscm.ws.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WSProperties {

  private static final String BASE_URL = "base.url";
  private static final String MAIL_URL = "mail.url";
  private static final String DEFAULT_USER_KEY = "user.administrator.key";
  private static final String DEFAULT_USER_PWD = "user.administrator.password";
  private static final String TRUST_STORE = "javax.net.ssl.trustStore";
  private static final String TRUST_STORE_PWD = "javax.net.ssl.trustStorePassword";
  private static final String AUTH_MODE = "auth.mode";
  private static final String REALM_NAME = "openejb.authentication.realmName";
  private static final String NAMING_FACTORY = "java.naming.factory.initial";
  private static final String NAMING_PROVIDER = "java.naming.provider.url";
  private static final String SUPPLIER_USER_ID = "user.supplier.id";
  private static final String SUPPLIER_USER_PWD = "user.supplier.password";

  private static final String WS_TESTS_PROPRTY_FILE = "wstests.properties";

  private static WSProperties WS_PROPERTIES;

  private Properties properties;

  public static WSProperties load() throws IOException {
    if (WS_PROPERTIES == null) {
      WS_PROPERTIES = new WSProperties();
    }
    return WS_PROPERTIES;
  }

  private WSProperties() throws IOException {
    loadProperties();
    logProperties();
  }

  public String getMailUrl() {
    return properties.getProperty(MAIL_URL);
  }

  public String getBaseUrl() {
    return properties.getProperty(BASE_URL);
  }

  public String getDefaultUserKey() {
    return properties.getProperty(DEFAULT_USER_KEY);
  }

  public String getDefaultUserPassword() {
    return properties.getProperty(DEFAULT_USER_PWD);
  }

  public String getSupplierUserId() {
    return properties.getProperty(SUPPLIER_USER_ID);
  }

  public String getSupplierUserPassword() {
    return properties.getProperty(SUPPLIER_USER_PWD);
  }

  public String getAuthMode() {
    return properties.getProperty(AUTH_MODE);
  }

  public String getTrustStore() {
    return properties.getProperty(TRUST_STORE);
  }

  public String getTrustStorePassword() {
    return properties.getProperty(TRUST_STORE_PWD);
  }

  public String getNamingProvider() {
    return properties.getProperty(NAMING_PROVIDER);
  }

  public String getNamingFactory() {
    return properties.getProperty(NAMING_FACTORY);
  }

  public String getRealmName() {
    return properties.getProperty(REALM_NAME);
  }

  public String getRealmNameProperty() {
    return REALM_NAME;
  }

  private void replacePlaceholders() {
    Properties result = new Properties();
    for (Object keyValue : properties.keySet()) {
      String key = (String) keyValue;
      String value = properties.getProperty(key);
      Pattern pattern = Pattern.compile("[$]\\{[\\w.]+\\}");
      Matcher matcher = pattern.matcher(value);
      if (matcher.find()) {
        String match = matcher.toMatchResult().group();
        match = match.substring(2);
        match = match.substring(0, match.length() - 1);
        if (match.startsWith("env.")) {
          String envValue = System.getenv(match.substring(4));
          if (envValue != null) {
            value = matcher.replaceAll(envValue);
          }
        }
      }
      result.setProperty(key, value);
    }
    properties = result;
  }

  private void loadProperties() throws IOException {
    if (properties == null) {
      InputStream stream = getClass().getClassLoader().getResourceAsStream(WS_TESTS_PROPRTY_FILE);
      properties = new Properties();
      properties.load(stream);
      replacePlaceholders();
    }
  }

  private void logProperties() {
    StringBuilder sb =
        new StringBuilder("Starting WebService test with the following " + "properties:\n");
    for (Object key : properties.keySet()) {
      sb.append("\n\t").append(key).append("=").append(properties.getProperty((String) key));
    }

    System.out.println(sb.toString());
  }
}
