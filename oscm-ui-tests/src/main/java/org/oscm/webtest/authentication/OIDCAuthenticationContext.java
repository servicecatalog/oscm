/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 10.01.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest.authentication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class OIDCAuthenticationContext implements AuthenticationContext {

  private static final Logger logger =
      LogManager.getLogger(OIDCAuthenticationContext.class.getName());
  private WebDriver driver;

  public OIDCAuthenticationContext(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public void loginPortal(String user, String password) {
    // TODO: login implementation
  }
}
