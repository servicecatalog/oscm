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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oscm.webtest.WebTester;

import javax.security.auth.login.LoginException;

/** Helper class for integration web tests for oscm-app/default.jsf */
public class AppTester extends WebTester {

  public static final String ERROR_MSG_CONTROLLER_EXISTS = "Controller ID already exists.";
  private String appAdminMailAddress = "";
  private String appBaseUrl = "";
  private String bssUserId = "";
  private String bssUserKey = "";
  private String bssUserPwd = "";

  private String base;
  private final String head = "https://";

  public AppTester() throws Exception {
    super();

    baseUrl = loadUrl(APP_SECURE, APP_HTTPS_URL, APP_HTTP_URL);
    base = baseUrl.replace(head, "");
  }

  /**
   * Attempts a login to the OSCM portal with the given credentials. Note that this method assumes
   * the webdriver to be at the login page.
   *
   * @param user the user name
   * @param password the password
   * @throws InterruptedException
   * @throws LoginException
   */
  public void loginAppConfig(String user, String password)
      throws LoginException, InterruptedException {

    String url = head + user + ":" + password + "@" + base + AppPathSegments.APP_CONFIGURATION;
    driver.get(url);
    driver.manage().window().maximize();

    wait(IMPLICIT_WAIT);

    if (verifyFoundElement(By.id(AppHtmlElements.APP_CONFIG_FORM1))) {
      logger.info(String.format("Login to %s successfully with userID: %s", url, user));
    } else {
      String info = String.format("Login to %s failed with userID:%s", url, user);
      logger.error(info);
      throw new LoginException(info);
    }
  }

  /**
   * Navigates the webdriver to the given page of the OSCM portal.
   *
   * @throws Exception
   */
  public void visitAppConfig() throws Exception {
    String target = baseUrl + AppPathSegments.APP_CONFIGURATION;
    driver.navigate().to(target);

    if (verifyFoundElement(By.id(AppHtmlElements.APP_CONFIG_FORM1))) {
      logger.error(String.format("Navigate to %s failed : HTTP Status 404 - Not Found", target));
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
  public void logoutAppConfig() throws Exception {}

  /**
   * Reads the error message from the page notification.
   *
   * @return the error message
   */
  public String readErrorMessage() {
    WebElement element =
        driver.findElement(By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG));
    return element
        .findElement(By.xpath(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_ERROR))
        .getText();
  }

  /**
   * Reads the info message from the page notification.
   *
   * @return the info message
   */
  public String readInfoMessage() {
    WebElement element =
        driver.findElement(By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG));
    return element
        .findElement(By.className(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK))
        .getText();
  }

  public boolean getExecutionResult() {
    waitForElement(By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG), 5);

    if (!verifyFoundElement(By.className(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_ERROR))
        && verifyFoundElement(By.className(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK))) {
      logger.info(readInfoMessage());
      return true;
    } else {
      logger.info(readErrorMessage());
      return false;
    }
  }

  public void registerController(String controllerId, String orgId) throws Exception {
    WebElement inputCid =
        driver.findElement(
            By.xpath(
                "//input[contains(@id,'"
                    + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWCONTROLLERID
                    + "')]"));
    inputCid.clear();
    inputCid.sendKeys(controllerId);

    WebElement inputOrgid =
        driver.findElement(
            By.xpath(
                "//input[contains(@id,'"
                    + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWORGID
                    + "')]"));
    inputOrgid.clear();
    inputOrgid.sendKeys(orgId);

    driver.findElement(By.name("configurationSettings:j_idt62")).click();

    if (!getExecutionResult()) {
      if (readErrorMessage().contains(ERROR_MSG_CONTROLLER_EXISTS)) {
        throw new Exception(ERROR_MSG_CONTROLLER_EXISTS);
      } else throw new Exception("other error");
    }
  }

  public void removeCreatedController() throws InterruptedException {
    if (driver.findElement(By.xpath("//td[contains(.,'a.ess.sample')]")).isDisplayed()) {
      driver.findElement(By.xpath("//td[@id='configurationSettings:j_idt52:0:j_idt58']/a")).click();

      logger.info("Old controller was deleted");
    } else {
      logger.error("Couldn't find created controller");
    }
    Thread.sleep(1000);
    driver.findElement(By.name("configurationSettings:j_idt62")).click();
  }

  public void changeOrganizationID(String index, String value) throws InterruptedException {
    WebElement field = driver.findElement(By.xpath(index));

    field.clear();
    field.sendKeys(value);
    Thread.sleep(1000);
    logger.info(String.format("Change organization ID into: %s for Azure Controller", value, index));

    driver.findElement(By.name("configurationSettings:j_idt62")).click();
    logger.info("Clicked save configuration button in controllers configurations");
  }

  private void clearNewEntry() {
    WebElement inputCid =
        driver.findElement(
            By.xpath(
                "//input[contains(@id,'"
                    + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWCONTROLLERID
                    + "')]"));
    inputCid.clear();
    WebElement inputOrgid =
        driver.findElement(
            By.xpath(
                "//input[contains(@id,'"
                    + AppHtmlElements.APP_CONFIG_FORM1_INPUT_END_NEWORGID
                    + "')]"));
    inputOrgid.clear();
  }

  public void changeOrgIdOnController(String controllerId, String newOrdId) throws Exception {
    clearNewEntry();
    log(
        "xpath := //form[@id='"
            + AppHtmlElements.APP_CONFIG_FORM1
            + "']/table/tbody[1]/tr/td[./text()='"
            + controllerId
            + "']/../td[2]/input");
    WebElement input =
        driver.findElement(
            By.xpath(
                "//form[@id='"
                    + AppHtmlElements.APP_CONFIG_FORM1
                    + "']/table/tbody[1]/tr/td[./text()='"
                    + controllerId
                    + "']/../td[2]/input"));
    input.clear();
    input.sendKeys(newOrdId);

    driver
        .findElement(
            By.xpath(
                "//form[@id='"
                    + AppHtmlElements.APP_CONFIG_FORM1
                    + "']"
                    + "//input[@class='"
                    + AppHtmlElements.APP_CONFIG_FORM_BUTTON_CLASS
                    + "']"))
        .click();

    if (!getExecutionResult()) throw new Exception();
  }

  private void changeInputValueForm2(int index, String keyword) throws Exception {
    WebElement input = getSettingWebElement(index);
    input.clear();
    input.sendKeys(keyword);

    logger.info(String.format("Wrote value: %s to element with id %s", keyword, index));

    driver
        .findElement(
            By.xpath(
                "//form[@id='"
                    + AppHtmlElements.APP_CONFIG_FORM2
                    + "']"
                    + "//input[@class='"
                    + AppHtmlElements.APP_CONFIG_FORM_BUTTON_CLASS
                    + "']"))
        .click();

    logger.info("Clicked save configuration button in APP settings");

    if (!getExecutionResult()) throw new Exception();
  }

  public WebElement getSettingWebElement(int index) {
    return driver.findElement(
        By.xpath(
            "//form[@id='"
                + AppHtmlElements.APP_CONFIG_FORM2
                + "']/table/tbody[1]/tr["
                + index
                + "]/td[2]/input"));
  }

  public void testConnection() {
    driver
        .findElement(By.xpath("//input[@id='configurationSettings:j_idt52:2:pingButton']"))
        .click();

    logger.info("Clicked test connection hyperlink");
  }

  public String readValue(String id) {
    WebElement element = driver.findElement(By.id(id));
    return element.getText();
  }

  public void setAppAdminMailAddress(String appAdminMailAddress) throws Exception {
    changeInputValueForm2(1, appAdminMailAddress);
    this.appAdminMailAddress = appAdminMailAddress;
  }

  public void setBssUserId(String bssUserId) throws Exception {
    changeInputValueForm2(3, bssUserId);
    this.bssUserId = bssUserId;
  }

  public void setBssUserKey(String bssUserKey) throws Exception {
    changeInputValueForm2(4, bssUserKey);
    this.bssUserKey = bssUserKey;
  }

  public void setBssUserPwd(String bssUserPwd) throws Exception {
    changeInputValueForm2(5, bssUserPwd);
    this.bssUserPwd = bssUserPwd;
  }
}
