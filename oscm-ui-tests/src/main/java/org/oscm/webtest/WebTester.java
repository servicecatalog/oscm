/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 20 6, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.webtest;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.oscm.webtest.authentication.AuthenticationContext;
import org.oscm.webtest.authentication.InternalAuthenticationContext;
import org.oscm.webtest.authentication.OIDCAuthenticationContext;
import org.oscm.webtest.exception.ConfigurationException;

/**
 * Helper class for integration web tests using selenium and java mail.
 *
 * @author miethaner
 */
public class WebTester {

  public static final int IMPLICIT_WAIT = 10;
  // property keys
  public static final String BES_HTTPS_URL = "bes.https.url";
  public static final String BES_ADMIN_USER_ID = "bes.user.id";
  public static final String BES_ADMIN_USER_PWD = "bes.user.password";

  public static final String OIDC_SUPPLIER_ID = "oidc.supplier.id";
  public static final String OIDC_SUPPLIER_PASSWORD = "oidc.supplier.password";

  public static final String APP_HTTPS_URL = "app.https.url";
  public static final String APP_ADMIN_USER_ID = "app.user.id";
  public static final String APP_ADMIN_USER_PWD = "app.user.password";

  public static final String AUTH_MODE = "auth.mode";
  public static final String CHROME_DRIVER_PATH = "chrome.driver.path";

  protected static final Logger logger = LogManager.getLogger(WebTester.class.getName());
  // web element keys
  protected static final String ATTRIUBTE_VALUE = "value";
  protected WebDriver driver;
  protected Properties prop;
  private int waitingTime;

  // path schemas
  private static final String PROPERTY_PATH =
      "../oscm-ui-tests/src/main/resources/webtest.properties";

  AuthenticationContext authenticationCtx;

  public WebTester() throws Exception {

    loadPropertiesFile();
    ChromeOptions options = new ChromeOptions();
    options.setHeadless(true);
    options.setAcceptInsecureCerts(true);
    options.addArguments("--no-sandbox", "--lang=en");

    System.setProperty("webdriver.chrome.driver", prop.getProperty(CHROME_DRIVER_PATH));

    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
    setWaitingTime(IMPLICIT_WAIT);
    setAuthenticationContext();
  }

  private void setAuthenticationContext() {
    String authMode = prop.getProperty(AUTH_MODE);
    switch (authMode) {
      case "INTERNAL":
        authenticationCtx = new InternalAuthenticationContext(driver);
        break;
      case "OIDC":
        authenticationCtx = new OIDCAuthenticationContext(driver);
        break;
      default:
        throw new ConfigurationException(
            "Invalid authentication mode set or related property is missing. Current value: "
                + authMode);
    }
  }

  /** Load properties from personal devruntime folder */
  private void loadPropertiesFile() throws Exception {

    Map<String, String> env = System.getenv();
    String localhost = env.get("HOSTNAME");
    if (StringUtils.isEmpty(localhost)) {
      localhost = InetAddress.getLocalHost().getHostName();
    }
    String filePath = String.format(PROPERTY_PATH, localhost);

    prop = new Properties();
    FileInputStream fis = new FileInputStream(filePath);
    prop.load(fis);
    fis.close();
  }

  public String getProperty(String key) {
    return prop.getProperty(key);
  }

  /** Closes all open resources of the helper */
  public void close() {
    driver.close();
  }

  /**
   * found the text between two given text in String
   *
   * @param msg
   * @param before
   * @param after
   * @return
   */
  protected String findTextBetween(String msg, String before, String after) {

    msg = msg.substring(msg.indexOf(before) + before.length(), msg.indexOf(after));

    return msg;
  }

  /**
   * Reads the error message from the page notification.
   *
   * @return the error message
   * @throws NoSuchElementException if error message is not present
   */
  public String readErrorMessage() {
    return "";
  }

  public String readInfoMessage() {
    return "";
  }

  public boolean getExecutionResult() {
    return false;
  }

  /**
   * Verifies if found the required element
   *
   * @param id the element id
   * @param value the value to compare with
   * @return true if equal
   * @throws NoSuchElementException if element is not present
   */
  public boolean verifyFoundElement(By by) {

    try {
      if (driver.findElement(by) != null) return true;
    } catch (NoSuchElementException e) {
      logger.error("Element is not present");
      return false;
    }
    logger.error("Cannot find elements");
    return false;
  }

  /**
   * Verifies if the content of the element with the given id is equal to the given value.
   *
   * @param id the element id
   * @param value the value to compare with
   * @return true if equal
   * @throws NoSuchElementException if element is not present
   */
  public boolean verifyEqualElement(String id, String value) {
    WebElement element = driver.findElement(By.id(id));
    if (element == null) return false;

    String attribute = element.getAttribute(ATTRIUBTE_VALUE);

    if (attribute != null && attribute.equals(value)) {
      log(String.format("Element with id %s and value %s is valid", id, value));
      return true;
    } else {
      logger.warn(String.format("Element with id %s is invalid (%s != %s)", id, value, attribute));
      return false;
    }
  }

  /**
   * Clicks the element with the given id.
   *
   * @param id the element id
   * @throws NoSuchElementException if element is not present
   */
  public void clickElement(String id) {
    try {
      driver.findElement(By.id(id)).click();
    } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
      driver.findElement(By.id(id)).click();
    }
    log(String.format("Clicked the element with id %s", id));
  }

  /**
   * Clicks the element with the given xpath.
   *
   * @param xpath the element xpath
   * @throws NoSuchElementException if element is not present
   */
  public void clickElementXPath(String xpath) {
    driver.findElement(By.xpath(xpath)).click();

    log(String.format("Clicked the element with xpath %s", xpath));
  }

  /**
   * Reads the value of the element with the given id. This is used for fields that use the value
   * attribute, e.g. input fields.
   *
   * @return the value of the element
   * @throws NoSuchElementException if element is not present
   */
  public String readValue(String id) {
    WebElement element = driver.findElement(By.id(id));
    return element.getAttribute(ATTRIUBTE_VALUE);
  }

  /**
   * Reads the value of the element with the given xpath. This is used for fields that use the value
   * attribute, e.g. input fields.
   *
   * @return the value of the element
   * @throws NoSuchElementException if element is not present
   */
  public String readTextXPath(String xpath) {
    WebElement element = driver.findElement(By.xpath(xpath));
    return element.getText();
  }

  /**
   * Reads the text of the element with the given id. This is used for text within an element, e.g.
   * &lt;p id="id"&gt;text&lt;/p&gt;
   *
   * @return the text of the element
   * @throws NoSuchElementException if element is not present
   */
  public String readText(String id) {
    WebElement element = driver.findElement(By.id(id));
    return element.getText();
  }

  /**
   * Takes the given value as input for the element with the given id.
   *
   * @param id the element id
   * @param value the input value
   * @throws NoSuchElementException if element is not present
   */
  public void writeValue(String id, String value) {
    try {
      WebElement element = driver.findElement(By.id(id));
      element.clear();
      element.sendKeys(value);
    } catch (StaleElementReferenceException e) {
      WebElement element = driver.findElement(By.id(id));
      element.clear();
      element.sendKeys(value);
    }
    log(String.format("Wrote value: %s to element with id %s", value, id));
  }

  /**
   * Takes the given value as input for the element with the given xpath.
   *
   * @param xpath the element xpath
   * @param value the input value
   * @throws NoSuchElementException if element is not present
   */
  public void writeValueXPath(String xpath, String value) {
    try {
      WebElement element = driver.findElement(By.xpath(xpath));
      element.clear();
      element.sendKeys(value);
    } catch (StaleElementReferenceException e) {
      WebElement element = driver.findElement(By.xpath(xpath));
      element.clear();
      element.sendKeys(value);
    }
    log(String.format("Wrote value: %s to element with id %s", value, xpath));
  }

  /**
   * Clear input for the element with the given xpath.
   *
   * @param xpath the element xpath
   * @throws NoSuchElementException if element is not present
   */
  public void clearInput(String xpath) {
    WebElement element = driver.findElement(By.xpath(xpath));
    element.sendKeys(Keys.BACK_SPACE);
    log(String.format("Clear input element with id %s", xpath));
  }

  /**
   * Selects in the dropdown (select) element with the given id the option with the given value.
   *
   * @param id the element id
   * @param value the option value
   * @throws NoSuchElementException if element is not present
   */
  public void selectDropdown(String id, String value) {
    Select select = new Select(driver.findElement(By.id(id)));
    select.selectByValue(value);
    log(String.format("Selected value: %s from element with id %s", value, id));
  }

  public void selectDropdownByIndex(String id, int index) {
    Select select = new Select(driver.findElement(By.id(id)));
    select.selectByIndex(index);
    log(String.format("Selected index: %s from element with id %s", index, id));
  }

  /**
   * Submits the form with the given id.
   *
   * @param id the element id
   * @throws NoSuchElementException if element is not present
   */
  public void submitForm(String id) {
    driver.findElement(By.id(id)).submit();

    log(String.format("Submitted form with id %s", id));
  }

  /**
   * Waits for the element with the given id to be present or until the given amount of seconds has
   * passed.
   *
   * @param id the element id
   * @param seconds the seconds until timeout
   * @throws TimeoutException if the timeout is reached
   */
  public void waitForElement(By by, int seconds) {
    (new WebDriverWait(driver, seconds)).until(ExpectedConditions.presenceOfElementLocated(by));
  }

  /**
   * Waits for the element with the given id to be present or until the given amount of seconds has
   * passed.
   *
   * @param id the element id
   * @param seconds the seconds until timeout
   * @throws TimeoutException if the timeout is reached
   */
  public void waitForElementVisible(By by, int seconds) {
    (new WebDriverWait(driver, seconds)).until(ExpectedConditions.elementToBeClickable(by));
  }

  /**
   * Waits for the element with the given id to be present or until the given amount of seconds has
   * passed.
   *
   * @param id the element id
   * @param seconds the seconds until timeout
   * @throws InterruptedException
   * @throws TimeoutException if the timeout is reached
   */
  public void wait(int seconds) throws InterruptedException {
    driver.manage().timeouts().implicitlyWait(seconds, TimeUnit.SECONDS);
  }

  /**
   * Returns the current URL that the webdriver is visiting.
   *
   * @return the current URL
   */
  public String getCurrentUrl() {
    return driver.getCurrentUrl();
  }

  public void log(String msg) {
    logger.info(msg);
  }

  public WebDriver getDriver() {
    return driver;
  }

  public void setDriver(WebDriver driver) {
    this.driver = driver;
  }

  public static String getCurrentTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    Date date = new Date();
    return formatter.format(date);
  }

  public int getWaitingTime() {
    return waitingTime;
  }

  public void setWaitingTime(int waitingTime) {
    this.waitingTime = waitingTime;
  }

  public String getAuthenticationMode() {
    return prop.getProperty(AUTH_MODE);
  }

  public String getStyleClass(String id) {
    try {
      WebElement element = driver.findElement(By.id(id));
      return element.getAttribute("class");
    } catch (StaleElementReferenceException e) {
      WebElement element = driver.findElement(By.id(id));
      return element.getAttribute("class");
    }
  }
}
