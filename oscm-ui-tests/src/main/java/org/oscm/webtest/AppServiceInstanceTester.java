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

/** Helper class for integration web tests for oscm-app/controller/controller/?cid=abc */
public class AppServiceInstanceTester extends WebTester {

  private String base = "";
  private String head = "";

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
   * @param user the user name
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

    if (verifyFoundElement(By.id(AppHtmlElements.APP_SERVICEINSTANCE_TABLE_ID))) {
      logger.info(String.format("Login to %s successfully with userid:%s", url, userid));
    } else {
      String info = String.format("Login to %s failed with userid:%s", url, userid);
      logger.info(info);
      throw new LoginException(info);
    }
  }

  /**
   * Navigates the webdriver to the given page of the OSCM portal.
   *
   * @param page the page of the portal
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
  public void logoutAppServiceInstance() throws Exception {}
}
