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

import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.oscm.webtest.AzureHtmlElements;
import org.oscm.webtest.PortalHtmlElements;

public class OIDCAuthenticationContext implements AuthenticationContext {

  private static final Logger logger =
      LogManager.getLogger(OIDCAuthenticationContext.class.getName());
  private WebDriver driver;

  public OIDCAuthenticationContext(WebDriver driver) {
    this.driver = driver;
  }

  @Override
  public void loginPortal(String user, String password)
      throws InterruptedException, LoginException {
    WebElement loginInput = driver.findElement(By.id(AzureHtmlElements.AZURE_INPUT_LOGIN));
    loginInput.sendKeys(user);
    logger.info("User login input entered: " + user);
    Thread.sleep(1000);
    driver.findElement(By.id(AzureHtmlElements.AZURE_BUTTON_NEXT)).click();
    logger.info("Proceeding to password input");
    Thread.sleep(1000);
    WebElement passwordInput = driver.findElement(By.id(AzureHtmlElements.AZURE_INPUT_PASSWORD));
    passwordInput.sendKeys(password);
    logger.info("User password input entered");
    Thread.sleep(1000);
    logger.info("Proceeding to keep signed page");
    driver.findElement(By.id(AzureHtmlElements.AZURE_BUTTON_NEXT)).click();
    Thread.sleep(1000);
    logger.info("Proceeding to OSCM page");
    driver.findElement(By.id(AzureHtmlElements.AZURE_BUTTON_NEXT)).click();
    Thread.sleep(1000);
    final String title = driver.getTitle();
    if (title.contains(PortalHtmlElements.PORTAL_TITLE) || title.contains("Marketplace")) {
      logger.info("Logged in to OSCM Portal with user: " + user);
    } else {
      logger.info("Logging to OSCM Portal with user: " + user + " failed.");
      throw new LoginException(
          "Expected portal title: "
              + PortalHtmlElements.PORTAL_TITLE
              + " but it was: "
              + driver.getTitle());
    }
  }

  @Override
  public void loginMarketplace(String user, String password)
      throws LoginException, InterruptedException {
    loginPortal(user, password);
  }

  @Override
  public void loginMarketplacePlayground(String user, String password)
      throws LoginException, InterruptedException {
    loginPortal(user, password);
  }
}
