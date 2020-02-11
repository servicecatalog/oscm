/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 20 6, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oscm.webtest.WebTester;

import javax.security.auth.login.LoginException;
import java.io.File;

/** Helper class for integration web tests for oscm-app/controller/controller/?cid=abc */
public class AppServiceInstanceTester extends WebTester {

  private String base = "";
  private String head = "";
  protected static final Logger logger = LogManager.getLogger(WebTester.class.getName());

  public AppServiceInstanceTester() throws Exception {
    super();

    baseUrl = loadUrl(APP_SECURE, APP_HTTPS_URL, APP_HTTP_URL);
    if (baseUrl.contains("https")) {
      head = "https://";
    } else {
      head = "http://";
    }
    base = baseUrl.replace(head, "");
  }

  /**
   * Attempts a login to the OSCM portal with the given credentials. Note that this method assumes
   * the webdriver to be at the login page.
   *
   * @param userid the user name
   * @param password the password
   * @throws InterruptedException
   * @throws Exception
   */
  public void loginAppServiceInstance(String userid, String password, String controllerId)
      throws LoginException, InterruptedException {

    String url =
        head
            + userid
            + ":"
            + password
            + "@"
            + base
            + AppPathSegments.APP_SERVICE_INSTANCE
            + controllerId;
    driver.get(url);
    driver.manage().window().maximize();

    wait(IMPLICIT_WAIT);

    if (verifyFoundElement(By.id(AppHtmlElements.APP_ACCORDION_AREA))) {
      logger.info(String.format("Login to %s successfully with userid: %s", url, userid));
    } else {
      String info = String.format("Login to %s failed with userid: %s", url, userid);
      logger.info(info);
      throw new LoginException(info);
    }
  }

  /**
   * Navigates the webdriver to the given page of the OSCM portal.
   *
   * @throws Exception
   */
  public void visitAppServiceInstance() throws Exception {
    String target = baseUrl + AppPathSegments.APP_SERVICE_INSTANCE;
    driver.navigate().to(target);

    if (verifyFoundElement(By.id(AppHtmlElements.APP_SERVICEINSTANCE_TABLE_ID))) {
      logger.info(String.format("Navigate to %s failed : HTTP Status 404 - Not Found", target));
      throw new Exception("Page not found!");
    } else {
      logger.info(String.format("Navigate to %s successfully", target));
    }
  }

  /**
   * Logs out the current user from the OSCM portal. Note that this method assumes that there is a
   * logged in user and that the driverApp is at a portal page.
   *
   * @throws Exception
   */

  public void buttonClickEvent(int idKey) throws Exception {
    Thread.sleep(1000);
    WebElement element = driver
        .findElement(
            By.xpath(
                "//input[@name='"
                    + AppHtmlElements.APP_SAMPLECONTROLLER_FORM_ID
                    + ":j_idt"
                    + idKey
                    + "']"));
    logger.info(String.format("Clicked button %s", element.getTagName()));
    element.click();

    Thread.sleep(1000);

    if (idKey == 75 && !getExecutionResult()) {
      if (readErrorMessage().contains(AppTester.ERROR_MSG_CONTROLLER_EXISTS)) {
        throw new Exception(AppTester.ERROR_MSG_CONTROLLER_EXISTS);
      } else {
        throw new Exception("other error");
      }
    }
  }

  public void buttonDefaultClickEvent(String idKey) throws Exception {
    Thread.sleep(1000);
    WebElement element = driver.findElement(By.xpath(idKey));
    logger.info(String.format("Clicked button %s", element.getTagName()));
    element.click();
    Thread.sleep(1000);
  }

  public void uploadFileEvent(String index, File file) {
    WebElement element = driver.findElement(By.xpath(index));

    element.sendKeys(file.toPath().toString());
    logger.info("File with .csv extension uploaded");
  }

  public void changeValueInputInSpecificField(String index, int idKey, String value) {
    WebElement field =
        driver.findElement(
            By.xpath(
                "//input[@name='"
                    + AppHtmlElements.APP_SAMPLECONTROLLER_FORM_ID
                    + ":j_idt"
                    + index
                    + ":j_idt"
                    + idKey
                    + "']"));
    field.clear();
    field.sendKeys(value);
  }

  public void changeValueInputInBalancerField(String index, String value) {
    WebElement field = getWebElement(index);

    field.clear();
    field.sendKeys(value);

    logger.info(String.format("Wrote value: %s to element with id %s", value, index));
  }

  public String readDefaultValue(String index) {
    WebElement element = getWebElement(index);
    return element.getAttribute("value");
  }

  public String readValue(String index, int idKey) {
    WebElement element =
            driver.findElement(
                    By.xpath(
                            "//input[@name='"
                                    + AppHtmlElements.APP_SAMPLECONTROLLER_FORM_ID
                                    + ":j_idt"
                                    + index
                                    + ":j_idt"
                                    + idKey
                                    + "']"));
    return element.getAttribute("value");
  }

  public WebElement getWebElement(String index) {
    return driver.findElement(
            By.xpath(
                    "//input[@id='"
                            + AppHtmlElements.APP_VSPHERE_API_SETTINGS
                            + ":vsphere_api_"
                            + index
                            + "']"));
  }

  public boolean getExecutionResult() {
    waitForElement(By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG), 10);

    if (!verifyFoundElement(By.className(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_ERROR_AT_CONTROLLER))
            && verifyFoundElement(By.className(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER))) {
      logger.info(readInfoMessage());
      return true;
    } else {
      logger.info(readErrorMessage());
      return false;
    }
  }

  /**
   * Reads the info message from the page notification.
   *
   * @return the info message
   */
  public String readInfoMessage() {
    return driver.findElement(By.xpath(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER)).getText();
  }

  public String readErrorMessage() {
    return driver.findElement(By.xpath(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_ERROR_AT_CONTROLLER)).getText();
  }

  public String readDefaultInfoMessage(String elementXPath) {
    return driver.findElement(By.xpath(elementXPath)).getText();
  }

  public String getProperties(String propertie) {
    return prop.getProperty(propertie);
  }
}
