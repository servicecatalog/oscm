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
    String userid = tester.getPropertie(AppTester.APP_ADMIN_USER_ID);
    String userpassword = tester.getPropertie(AppTester.APP_ADMIN_USER_PWD);
    tester.loginAppConfig(userid, userpassword);
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    tester.logoutAppConfig();
    tester.close();
  }

  @Test
  public void test01setConfiguration() throws Exception {

    tester.setAppAdminMailAddress(PlaygroundSuiteTest.supplierOrgAdminMail);
    tester.setBssUserId(tester.getPropertie(AppTester.APP_ADMIN_USER_ID));
    tester.setBssUserKey("10");
    tester.setBssUserPwd(tester.getPropertie(AppTester.APP_ADMIN_USER_PWD));

    assertTrue(tester.readInfoMessage().contains("saved successfully"));
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
  public void test03removeControllerId() {
    tester.removeCreatedController();

    assertNotEquals(
        AppHtmlElements.TEST_CONTROLLER_ID,
        tester.readValue(AppHtmlElements.APP_CONTROLLER_TABLE_FIELD));
  }

  @Test
  public void test04checkConnection() {
    tester.testConnection();

    assertFalse(tester.getExecutionResult());
  }
}
