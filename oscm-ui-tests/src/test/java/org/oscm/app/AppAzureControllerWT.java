package org.oscm.app;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.app.AppControllerTester;
import org.oscm.webtest.app.AppPathSegments;
import org.oscm.webtest.app.AppTester;

import javax.security.auth.login.LoginException;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppAzureControllerWT {

  private static AppControllerTester controllerTester;
  private static AppTester tester;
  private static String userKey;
  private static String changedUserID;
  private static String changedPassword;
  private static String userID;
  private static String userPassword;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    controllerTester = new AppControllerTester();

    userKey = PlaygroundSuiteTest.supplierOrgAdminUserkey;
    changedUserID = PlaygroundSuiteTest.supplierOrgAdminId;
    changedPassword = PlaygroundSuiteTest.supplierOrgAdminPwd;

    userID = controllerTester.getProperties(AppControllerTester.APP_ADMIN_USER_ID);
    userPassword = controllerTester.getProperties(AppControllerTester.APP_ADMIN_USER_PWD);
    controllerTester.loginAppController(
        userID, userPassword, AppPathSegments.APP_PATH_CONTROLLER_AZURE);
  }

  @AfterClass
  public static void cleanUp() {
    controllerTester.close();
  }

  @Test
  public void test01setSettingsIntoController() throws Exception {
    controllerTester.changeValueInputInSpecificField("49:1", 56, changedUserID);
    controllerTester.changeValueInputInSpecificField("49:2", 56, userKey);
    controllerTester.changeValueInputInSpecificField("49:3", 55, changedPassword);

    controllerTester.buttonClickEvent(75);

    assertEquals(changedUserID, controllerTester.readValue("49:1", 56));
    assertEquals(userKey, controllerTester.readValue("49:2", 56));
    assertEquals(changedPassword, controllerTester.readValue("49:3", 55));
  }

  @Test
  public void test02changeOrganizationIdInAppConfigurator() throws Exception {
    tester = new AppTester();
    tester.loginAppConfig(userID, userPassword);

    tester.changeOrganizationID("//input[@id='configurationSettings:j_idt52:1:configurationValue']", PlaygroundSuiteTest.supplierOrgId);

    assertEquals(PlaygroundSuiteTest.supplierOrgId, tester.readValue("configurationSettings:j_idt52:1:configurationValue"));
  }

  @Test
  public void test03checkThatOrganizationIdIsChanged() throws LoginException, InterruptedException {
    controllerTester.loginAppController(
        changedUserID, changedPassword, AppPathSegments.APP_PATH_CONTROLLER_AZURE);

    assertEquals(PlaygroundSuiteTest.supplierOrgId, controllerTester.readValue("49:0", 56));
  }

  @Test
  public void test04undoSettingsIntoController() throws Exception {
    controllerTester.loginAppController(
            changedUserID, changedPassword, AppPathSegments.APP_PATH_CONTROLLER_AZURE);
    controllerTester.changeValueInputInSpecificField("49:1", 56, userID);
    controllerTester.changeValueInputInSpecificField("49:2", 56, "1000");
    controllerTester.changeValueInputInSpecificField("49:3", 55, userPassword);

    controllerTester.buttonClickEvent(76);

    assertEquals(userID, controllerTester.readValue("49:1", 56));
    assertEquals("1000", controllerTester.readValue("49:2", 56));
    assertEquals(userPassword, controllerTester.readValue("49:3", 55));
  }
}
