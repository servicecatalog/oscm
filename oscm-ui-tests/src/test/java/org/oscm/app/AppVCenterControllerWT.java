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

  private static AppControllerTester instanceTester;
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
    instanceTester = new AppControllerTester();

    userKey = "1000";
    changedUserID = "newUser";
    changedPassword = "Password12";

    userID = instanceTester.getProperties(AppControllerTester.APP_ADMIN_USER_ID);
    userPassword = instanceTester.getProperties(AppControllerTester.APP_ADMIN_USER_PWD);
    instanceTester.loginAppController(
        userID, userPassword, AppPathSegments.APP_PATH_CONTROLLER_VCENTER);
  }

  @AfterClass
  public static void cleanUp() {
    instanceTester.close();
  }

  @Test
  public void test01setSettingsAPIvSphere() throws Exception {
    instanceTester.changeValueInputInBalancerField("url", "https://webiste.com");
    instanceTester.changeValueInputInBalancerField("user", userID);
    instanceTester.changeValueInputInBalancerField("pwd", userPassword);

    instanceTester.buttonDefaultClickEvent("//input[@name='balancer_form:j_idt120']");
    instanceTester.readDefaultInfoMessage(
        AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_SECOND);

    assertEquals("https://webiste.com", instanceTester.readDefaultValue("url"));
    assertEquals(userID, instanceTester.readDefaultValue("user"));
    assertEquals(userPassword, instanceTester.readDefaultValue("pwd"));
  }

  @Test
  public void test02importServiceTemplate() throws Exception {
    createdFile = folder.newFile("vcenter.csv");
    FileUtils.writeStringToFile(createdFile, "TKey,Name,Identifier,URL,UserId,Password,", "UTF-8");
    instanceTester.uploadFileEvent("//input[@id='csv_form:csvFile']", createdFile);
    instanceTester.buttonDefaultClickEvent("//input[@name='csv_form:j_idt138']");

    assertTrue(
        instanceTester
            .readDefaultInfoMessage(
                AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_SECOND)
            .contains("saved successfully"));
  }

  @Test
  public void test03setSettingsIntoController() throws Exception {
    instanceTester.changeValueInputInSpecificField("47:1", 54, changedUserID);
    instanceTester.changeValueInputInSpecificField("47:2", 54, userKey);
    instanceTester.changeValueInputInSpecificField("47:3", 53, changedPassword);

    instanceTester.buttonClickEvent(58);
    instanceTester.readDefaultInfoMessage(
        AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER_FIRST);

    assertEquals(changedUserID, instanceTester.readValue("47:1", 54));
    assertEquals(userKey, instanceTester.readValue("47:2", 54));
    assertEquals(changedPassword, instanceTester.readValue("47:3", 53));
  }
}
