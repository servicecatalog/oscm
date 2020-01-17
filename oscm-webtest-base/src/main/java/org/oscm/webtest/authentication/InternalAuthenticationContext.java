/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020
 *
 *  Creation Date: 10.01.2020
 *
 *******************************************************************************/
package org.oscm.webtest.authentication;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.oscm.webtest.PortalHtmlElements;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

import static org.oscm.webtest.WebTester.IMPLICIT_WAIT;

public class InternalAuthenticationContext implements AuthenticationContext {

  private static final Logger logger = Logger.getLogger(InternalAuthenticationContext.class);

  private WebDriver driver;

  public InternalAuthenticationContext(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public void loginPortal(String user, String password) throws LoginException {
    WebElement userInput = driver.findElement(By.id(PortalHtmlElements.PORTAL_INPUT_USERID));
    userInput.sendKeys(user);

    WebElement pwdInput = driver.findElement(By.name(PortalHtmlElements.PORTAL_INPUT_PASSWORD));
    pwdInput.sendKeys(password);

    driver.findElement(By.id(PortalHtmlElements.PORTAL_BUTTON_LOGIN)).click();
    driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);

    try {
      driver.findElement(By.id(PortalHtmlElements.PORTAL_DIV_LOGIN_FAILED));
      String info = "Login to OSCM Portal failed with userId:" + user;
      logger.info(info);
      throw new LoginException(info);
    } catch (NoSuchElementException exc) {
      logger.info("Login to OSCM Portal successfully with userId:" + user);
    }
  }
}
