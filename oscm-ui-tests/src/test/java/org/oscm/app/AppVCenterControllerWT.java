package org.oscm.app;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.webtest.app.AppControllerTester;
import org.oscm.webtest.app.AppHtmlElements;
import org.oscm.webtest.app.AppPathSegments;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppVCenterControllerWT {

  private static AppControllerTester controllerTester;
  private static String userKey;
  private static String changedUserID;
  private static String changedPassword;
  private static String userID;
  private static String userPassword;
  public static File createdFile;

  @Rule public TestWatcher testWatcher = new JUnitHelper();
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @BeforeClass
  public static void setup() throws Exception {
    controllerTester = new AppControllerTester();

    userKey = "1000";
    changedUserID = "newUser";
    changedPassword = "Password12";

    userID = controllerTester.getProperties(AppControllerTester.APP_ADMIN_USER_ID);
    userPassword = controllerTester.getProperties(AppControllerTester.APP_ADMIN_USER_PWD);
    controllerTester.loginAppController(
        userID, userPassword, AppPathSegments.APP_PATH_CONTROLLER_VCENTER);
  }

  @AfterClass
  public static void cleanUp() {
    controllerTester.close();
  }

  @Test
  public void test01setSettingsAPIvSphere() throws Exception {
    if (controllerTester.getAuthenticationMode().equals("OIDC")) {
      controllerTester.log("OIDC MODE SKIPPING TEST");
    } else {
      controllerTester.changeValueInputInBalancerField("url", "https://webiste.com");
      controllerTester.changeValueInputInBalancerField("user", userID);
      controllerTester.changeValueInputInBalancerField("pwd", userPassword);

      controllerTester.buttonDefaultClickEvent("//input[@name='balancer_form:j_idt120']");
      controllerTester.readDefaultInfoMessage(
          AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_SECOND);

      assertEquals("https://webiste.com", controllerTester.readDefaultValue("url"));
      assertEquals(userID, controllerTester.readDefaultValue("user"));
      assertEquals(userPassword, controllerTester.readDefaultValue("pwd"));
    }
  }

  @Test
  public void test02importServiceTemplate() throws Exception {
    if (controllerTester.getAuthenticationMode().equals("OIDC")) {
      controllerTester.log("OIDC MODE SKIPPING TEST");
    } else {
      createdFile = folder.newFile("vcenter.csv");
      FileUtils.writeStringToFile(
          createdFile, "TKey,Name,Identifier,URL,UserId,Password,", "UTF-8");
      controllerTester.uploadFileEvent("//input[@id='csv_form:csvFile']", createdFile);
      controllerTester.buttonDefaultClickEvent("//input[@name='csv_form:j_idt138']");

      assertTrue(
          controllerTester
              .readDefaultInfoMessage(
                  AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_SECOND)
              .contains("saved successfully"));
    }
  }

  @Test
  public void test03setSettingsIntoController() throws Exception {
    if (controllerTester.getAuthenticationMode().equals("OIDC")) {
      controllerTester.log("OIDC MODE SKIPPING TEST");
    } else {
      controllerTester.changeValueInputInSpecificField("47:1", 54, changedUserID);
      controllerTester.changeValueInputInSpecificField("47:2", 54, userKey);
      controllerTester.changeValueInputInSpecificField("47:3", 53, changedPassword);

      controllerTester.buttonClickEvent(58);
      controllerTester.readDefaultInfoMessage(
          AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_FIRST);

      assertEquals(changedUserID, controllerTester.readValue("47:1", 54));
      assertEquals(userKey, controllerTester.readValue("47:2", 54));
      assertEquals(changedPassword, controllerTester.readValue("47:3", 53));
    }
  }
}
