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

import static org.oscm.webtest.WebTester.IMPLICIT_WAIT;

import java.util.concurrent.TimeUnit;
import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.oscm.webtest.PortalHtmlElements;

public class InternalAuthenticationContext implements AuthenticationContext {

  private static final Logger logger =
      LogManager.getLogger(InternalAuthenticationContext.class.getName());

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
      String info = String.format("Login to OSCM Portal failed with userId:%s", user);
      logger.error(info);
      throw new LoginException(info);
    } catch (NoSuchElementException exc) {
      logger.info(String.format("Login to OSCM Portal successfully with userId: %s", user));
    }
  }

  @Override
  public void loginMarketplace(String user, String password)
      throws LoginException, InterruptedException {
    WebElement userInput = driver.findElement(By.id(PortalHtmlElements.MARKETPLACE_INPUT_USERID));
    userInput.sendKeys(user);

    WebElement pwdInput =
        driver.findElement(By.name(PortalHtmlElements.MARKETPLACE_INPUT_PASSWORD));
    pwdInput.sendKeys(password);

    driver.findElement(By.id(PortalHtmlElements.MARKETPLACE_BUTTON_LOGIN)).click();
    driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);

    try {
      driver.findElement(By.id(PortalHtmlElements.PORTAL_DIV_LOGIN_FAILED));
      String info = String.format("Login to OSCM Portal failed with userId:%s", user);
      logger.error(info);
      throw new LoginException(info);
    } catch (NoSuchElementException exc) {
      logger.info(String.format("Login to OSCM Portal successfully with userId: %s", user));
    }
  }
}
