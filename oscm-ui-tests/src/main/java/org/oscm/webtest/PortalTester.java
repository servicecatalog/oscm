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

import javax.security.auth.login.LoginException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.oscm.email.MaildevReader;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.IdentityConfiguration;
import org.oscm.identity.exception.IdentityClientException;

/**
 * Helper class for integration web tests using selenium and java mail.
 *
 * @author miethaner
 */
public class PortalTester extends WebTester {
  private static final String EMAIL_HOST = "email.http.url";
  public static final String TECHSERVICE_PARAM_EMAIL = "The receiver of emails.";
  public static final String TECHSERVICE_PARAM_USER = "IAAS user";
  public static final String TECHSERVICE_PARAM_PWD = "IAAS password";
  public static final String TECHSERVICE_PARAM_MESSAGETEXT = "The message text for emails.";
  // path schemas
  private static final String BASE_PATH_PORTAL = "%s/oscm-portal/%s";
  private static final String BASE_PATH_MARKETPLACE = "%s/oscm-portal/marketplace/%s";
  private static final String AUTH_TENANT = "auth.tenant";

  private MaildevReader maildevReader;
  private ApiIdentityClient identityClient;

  public PortalTester() throws Exception {
    super();

    this.maildevReader = new MaildevReader(prop.getProperty(EMAIL_HOST));
    IdentityConfiguration configuration =
        IdentityConfiguration.of().tenantId(prop.getProperty(AUTH_TENANT)).build();
    this.identityClient = new ApiIdentityClient(configuration);
    visitLoginPage();
  }

  /**
   * Attempts a login to the OSCM portal with the given credentials. Note that this method assumes
   * the webdriver to be at the login page.
   *
   * @param user the user name
   * @param password the password
   * @throws InterruptedException
   * @throws Exception
   */
  public void loginPortal(String user, String password)
      throws LoginException, InterruptedException {
    authenticationCtx.loginPortal(user, password);
    log(String.format("Login to portal as %s", user));
  }

  /**
   * Navigates the webdriver to the given page of the OSCM portal.
   *
   * @param segments the page of the portal
   * @throws Exception
   */
  public void visitPortal(String segments) throws Exception {
    String target = String.format(BASE_PATH_PORTAL, prop.getProperty(BES_HTTPS_URL), segments);
    driver.navigate().to(target);

    String actualTitle = driver.getTitle();
    if (actualTitle == null || !actualTitle.contentEquals(PortalHtmlElements.PORTAL_TITLE)) {
      log(String.format("Navigate to %s failed : HTTP Status 404 - Not Found", target));
      throw new Exception("Page not found!");
    } else {
      log(String.format("Navigate to %s successfully", target));
    }
  }

  public void visitLoginPage() throws Exception {
    driver.navigate().to(String.format(BASE_PATH_PORTAL, prop.getProperty(BES_HTTPS_URL), ""));
    String expectedTitle =
        prop.get(AUTH_MODE).equals("OIDC")
            ? AzureHtmlElements.AZURE_TITLE_LOGIN
            : PortalHtmlElements.PORTAL_TITLE;
    String actualTitle = driver.getTitle();
    if (actualTitle == null || !actualTitle.contentEquals(expectedTitle)) {
      log(
          "Navigate to login page failed, title expected:"
              + expectedTitle
              + " but it was: "
              + actualTitle);
      throw new Exception(
          "Navigate to login page failed, title expected:"
              + expectedTitle
              + " but it was: "
              + actualTitle);
    } else {
      log("Navigate to login page successfully");
    }
  }

  /**
   * Logs out the current user from the OSCM portal. Note that this method assumes that there is a
   * logged in user and that the driverApp is at a portal page.
   */
  public void logoutPortal() {
    WebElement logoutLink = driver.findElement(By.id(PortalHtmlElements.PORTAL_LINK_LOGOUT));
    JavascriptExecutor executor = (JavascriptExecutor) driver;
    executor.executeScript("arguments[0].click();", logoutLink);
    log("Login out from OSCM Portal successfully");
  }

  /**
   * Attempts a login to the OSCM marketplace with the given credentials. Note that this method
   * assumes the webdriver to be at the login page.
   *
   * @param user the user name
   * @param password the password
   * @throws Exception
   */
  public void loginMarketplace(String user, String password, String supplierOrgId)
      throws Exception {
    visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE_ID + supplierOrgId);

    driver.findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_TOGGLE_BUTTON)).click();
    driver
        .findElement(By.linkText(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_LOGIN_LINK_TEXT))
        .click();

    authenticationCtx.loginMarketplace(user, password);
  }

  /**
   * Navigates the webdriver to the given page of the OSCM marketplace.
   *
   * @param context the end of path
   */
  public void visitMarketplace(String context) {
    String target = String.format(BASE_PATH_MARKETPLACE, prop.getProperty(BES_HTTPS_URL), context);

    driver.navigate().to(target);

    log(String.format("Navigate to %s successfully", target));
  }

  /**
   * Logs out the current user from the OSCM marketplace. Note that this method assumes that there
   * is a logged in user and that the driverApp is at a marketplace page.
   */
  public void logoutMarketplace() {
    driver.findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_TOGGLE_BUTTON)).click();
    driver
        .findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_USER_TOGGLE_BUTTON))
        .click();
    driver.findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_LOGOUT_LINK)).click();
  }

  /**
   * found the text between two given text in String
   *
   * @param msg
   * @return
   */
  public String getCreatedId(String msg) {

    return findTextBetween(msg, "ID ", " has");
  }

  /**
   * Reads the error message from the page notification.
   *
   * @return the error message
   */
  public String readErrorMessage() {
    WebElement element = driver.findElement(By.id(PortalHtmlElements.PORTAL_SPAN_ERRORS));
    return element.findElement(By.className(PortalHtmlElements.PORTAL_ERRORCLASS)).getText();
  }

  /**
   * Reads the info message from the page notification.
   *
   * @return the info message
   */
  public String readInfoMessage() {
    WebElement element = driver.findElement(By.id(PortalHtmlElements.PORTAL_SPAN_INFOS));
    return element.findElement(By.className(PortalHtmlElements.PORTAL_INFOCLASS)).getText();
  }

  public boolean getExecutionResult() {
    String idPanel = PortalHtmlElements.PORTAL_DIV_SHOWMESSAGE;
    if (driver.getCurrentUrl().contains("/marketplace/"))
      idPanel = PortalHtmlElements.MARKETPLACE_SPAN_SHOWMESSAGE;
    waitForElement(By.id(idPanel), getWaitingTime());
    if (!verifyFoundElement(By.id(PortalHtmlElements.PORTAL_SPAN_ERRORS))
        && verifyFoundElement(By.id(PortalHtmlElements.PORTAL_SPAN_INFOS))) {
      log(readInfoMessage());
      return true;
    } else {
      log(readErrorMessage());
      return false;
    }
  }

  /**
   * Reads the latest email with the given subject from the email inbox.
   *
   * @param subject the email subject
   * @return the email body or null if no email was found
   * @throws Exception
   */
  public String readLatestEmailWithSubject(String subject) throws Exception {
    return maildevReader.getLatestEmailBySubject(subject).getText();
  }

  public void deleteSupplierGroup(String groupName) throws IdentityClientException {
    identityClient.getGroups().stream()
        .filter(groupInfo -> groupInfo.getName().contains(groupName))
        .forEach(
            groupInfo -> {
              try {
                identityClient.deleteGroup(groupInfo.getId());
              } catch (IdentityClientException e) {
                e.printStackTrace();
              }
            });
  }
}
