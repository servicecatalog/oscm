/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020
 *
 *  Creation Date: 10.01.2020
 *
 *******************************************************************************/
package org.oscm.webtest.authentication;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class OIDCAuthenticationContext implements AuthenticationContext {

  private static final Logger logger = Logger.getLogger(OIDCAuthenticationContext.class);
  private WebDriver driver;

  public OIDCAuthenticationContext(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public void loginPortal(String user, String password) {
      //TODO: login implementation
  }
}
