/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 20 6, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.app;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.app.AppHtmlElements;
import org.oscm.webtest.app.AppTester;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppConfigurationWT {

  private static AppTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new AppTester();
    String userID = tester.getProperty(AppTester.APP_ADMIN_USER_ID);
    String userPassword = tester.getProperty(AppTester.APP_ADMIN_USER_PWD);
    tester.loginAppConfig(userID, userPassword);
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    tester.logoutAppConfig();
    tester.close();
  }

  @Test
  public void test01checkConnection() {
      tester.testConnection();

      assertFalse(tester.getExecutionResult());

  }

  @Test
  public void test02createNewControllerId() throws Exception {
      try {
        tester.registerController(
            AppHtmlElements.TEST_CONTROLLER_ID, PlaygroundSuiteTest.supplierOrgId);
      } catch (Exception e) {
        if (e.getMessage() != null
            && e.getMessage().contentEquals(AppTester.ERROR_MSG_CONTROLLER_EXISTS)) {
          tester.changeOrgIdOnController(
              AppHtmlElements.TEST_CONTROLLER_ID, PlaygroundSuiteTest.supplierOrgId);
        }
      }

      assertTrue(tester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test03removeControllerId() throws InterruptedException {
      tester.removeCreatedController();
      tester.waitForElement(By.className(AppHtmlElements.APP_CONFIG_DIV_CLASS_STATUS_MSG), 5);

      assertNotEquals(
          AppHtmlElements.TEST_CONTROLLER_ID,
          tester.readValue(AppHtmlElements.APP_CONTROLLER_TABLE_FIELD));
      assertTrue(tester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test04setConfiguration() throws Exception {
      tester.setAppAdminMailAddress(PlaygroundSuiteTest.supplierOrgAdminMail);
      tester.setBssUserId(tester.getProperty(AppTester.APP_ADMIN_USER_ID));
      tester.setBssUserKey("10");
      tester.setBssUserPwd(tester.getProperty(AppTester.APP_ADMIN_USER_PWD));

      assertTrue(tester.readInfoMessage().contains("saved successfully"));

  }
}
