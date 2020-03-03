package org.oscm.app;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.webtest.app.AppControllerTester;
import org.oscm.webtest.app.AppPathSegments;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppOpenstackControllerWT {

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

    userID = controllerTester.getProperty(AppControllerTester.APP_ADMIN_USER_ID);
    userPassword = controllerTester.getProperty(AppControllerTester.APP_ADMIN_USER_PWD);
      controllerTester.loginAppController(
          userID, userPassword, AppPathSegments.APP_PATH_CONTROLLER_OPENSTACK);
  }

  @AfterClass
  public static void cleanUp() {
    controllerTester.close();
  }

  @Test
  public void test01setSettingsIntoSpecificController() throws Exception {

      controllerTester.changeValueInputInSpecificField("62:0", 69, changedUserID);
      controllerTester.changeValueInputInSpecificField("62:1", 68, changedPassword);
      controllerTester.changeValueInputInSpecificField("62:2", 69, "https://webiste.com");
      controllerTester.changeValueInputInSpecificField("62:3", 69, String.valueOf(userKey));
      controllerTester.changeValueInputInSpecificField("62:4", 69, "Website");
      controllerTester.changeValueInputInSpecificField("62:5", 69, "https://template/...");
      controllerTester.changeValueInputInSpecificField("62:6", 69, String.valueOf(userKey));

      controllerTester.buttonClickEvent(75);

      assertEquals(changedUserID, controllerTester.readValue("62:0", 69));
      assertEquals(changedPassword, controllerTester.readValue("62:1", 68));
      assertEquals("https://webiste.com", controllerTester.readValue("62:2", 69));
      assertEquals("Website", controllerTester.readValue("62:4", 69));
      assertEquals("https://template/...", controllerTester.readValue("62:5", 69));

      assertTrue(controllerTester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test02undoSettingsIntoSpecificController() throws Exception {

      controllerTester.changeValueInputInSpecificField("62:0", 69, userID);
      controllerTester.changeValueInputInSpecificField("62:1", 68, userPassword);
      controllerTester.changeValueInputInSpecificField("62:2", 69, "");
      controllerTester.changeValueInputInSpecificField("62:3", 69, "");
      controllerTester.changeValueInputInSpecificField("62:4", 69, "");
      controllerTester.changeValueInputInSpecificField("62:5", 69, "");
      controllerTester.changeValueInputInSpecificField("62:6", 69, "");

      controllerTester.buttonClickEvent(76);

      assertEquals(changedUserID, controllerTester.readValue("62:0", 69));
      assertEquals(changedPassword, controllerTester.readValue("62:1", 68));
      assertEquals("https://webiste.com", controllerTester.readValue("62:2", 69));
      assertEquals("Website", controllerTester.readValue("62:4", 69));
      assertEquals("https://template/...", controllerTester.readValue("62:5", 69));

  }

  @Test
  public void test03importServiceTemplate() throws Exception {

      createdFile = folder.newFile("vcenter.csv");
      FileUtils.writeStringToFile(
          createdFile, "TKey,Name,Identifier,URL,UserId,Password,", "UTF-8");
      controllerTester.uploadFileEvent("//input[@id='templateForm:file']", createdFile);
      controllerTester.buttonDefaultClickEvent("//input[@name='templateForm:j_idt112']");

      assertTrue(controllerTester.readInfoMessage().contains("imported successfully"));
  }

  @Test
  public void test04removeServiceTemplate() throws Exception {

      controllerTester.buttonDefaultClickEvent("//td[@id='templateForm:j_idt87:0:j_idt94']/a");

      assertTrue(controllerTester.readInfoMessage().contains("deleted successfully"));

  }

  @Test
  public void test05setSettingsIntoController() throws Exception {

      controllerTester.changeValueInputInSpecificField("49:1", 56, changedUserID);
      controllerTester.changeValueInputInSpecificField("49:2", 56, userKey);
      controllerTester.changeValueInputInSpecificField("49:3", 55, changedPassword);

      controllerTester.buttonClickEvent(75);

      assertEquals(changedUserID, controllerTester.readValue("49:1", 56));
      assertEquals(userKey, controllerTester.readValue("49:2", 56));
      assertEquals(changedPassword, controllerTester.readValue("49:3", 55));

  }

  @Test
  public void test06undoSettingsIntoController() throws Exception {

      controllerTester.changeValueInputInSpecificField("49:1", 56, userID);
      controllerTester.changeValueInputInSpecificField("49:2", 56, "1000");
      controllerTester.changeValueInputInSpecificField("49:3", 55, userPassword);

      controllerTester.buttonClickEvent(76);

      assertEquals(userID, controllerTester.readValue("49:1", 56));
      assertEquals("1000", controllerTester.readValue("49:2", 56));
      assertEquals(userPassword, controllerTester.readValue("49:3", 55));

  }
}
