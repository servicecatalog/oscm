package org.oscm.app;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.webtest.app.AppHtmlElements;
import org.oscm.webtest.app.AppServiceInstanceTester;

import java.io.File;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppControllerOpenstack {

  private static AppServiceInstanceTester instanceTester;
  private static final Random RANDOM = new Random();
  private static int userkey;
  private static String changedUserID;
  private static String changedPassword;
  private static String userid;
  private static String userpassword;
  public static File createdFile;

  @Rule public TestWatcher testWatcher = new JUnitHelper();
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @BeforeClass
  public static void setup() throws Exception {
    instanceTester = new AppServiceInstanceTester();

    userkey = RANDOM.nextInt(100000) + 1;
    changedUserID = "newUser";
    changedPassword = "Password12";

    userid = instanceTester.getProperties(AppServiceInstanceTester.APP_ADMIN_USER_ID);
    userpassword = instanceTester.getProperties(AppServiceInstanceTester.APP_ADMIN_USER_PWD);
    instanceTester.loginAppServiceInstance(userid, userpassword, "-openstack/");
  }

  @AfterClass
  public static void cleanUp() {
    instanceTester.close();
  }

  @Test
  public void test01setSettingsIntoSpecificController() throws Exception {
    instanceTester.changeValueInputInSpecificField("62:0", 69, changedUserID);
    instanceTester.changeValueInputInSpecificField("62:1", 68, changedPassword);
    instanceTester.changeValueInputInSpecificField("62:2", 69, "https://webiste.com");
    instanceTester.changeValueInputInSpecificField("62:3", 69, String.valueOf(userkey));
    instanceTester.changeValueInputInSpecificField("62:4", 69, "Website");
    instanceTester.changeValueInputInSpecificField("62:5", 69, "https://template/...");
    instanceTester.changeValueInputInSpecificField("62:6", 69, String.valueOf(userkey));

    instanceTester.buttonClickEvent(75);

    assertEquals(changedUserID, instanceTester.readValue("62:0", 69));
    assertEquals(changedPassword, instanceTester.readValue("62:1", 68));
    assertEquals("https://webiste.com", instanceTester.readValue("62:2", 69));
    assertEquals("Website", instanceTester.readValue("62:4", 69));
    assertEquals("https://template/...", instanceTester.readValue("62:5", 69));

    assertTrue(instanceTester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test02undoSettingsIntoSpecificController() throws Exception {
    instanceTester.changeValueInputInSpecificField("62:0", 69, userid);
    instanceTester.changeValueInputInSpecificField("62:1", 68, userpassword);
    instanceTester.changeValueInputInSpecificField("62:2", 69, "");
    instanceTester.changeValueInputInSpecificField("62:3", 69, "");
    instanceTester.changeValueInputInSpecificField("62:4", 69, "");
    instanceTester.changeValueInputInSpecificField("62:5", 69, "");
    instanceTester.changeValueInputInSpecificField("62:6", 69, "");

    instanceTester.buttonClickEvent(76);

    assertEquals(changedUserID, instanceTester.readValue("62:0", 69));
    assertEquals(changedPassword, instanceTester.readValue("62:1", 68));
    assertEquals("https://webiste.com", instanceTester.readValue("62:2", 69));
    assertEquals("Website", instanceTester.readValue("62:4", 69));
    assertEquals("https://template/...", instanceTester.readValue("62:5", 69));
  }

  @Test
  public void test03importServiceTemplate() throws Exception {
    createdFile = folder.newFile("vcenter.csv");
    FileUtils.writeStringToFile(createdFile, "TKey,Name,Identifier,URL,UserId,Password,", "UTF-8");
    instanceTester.uploadFileEvent("//input[@id='templateForm:file']", createdFile);
    instanceTester.buttonDefaultClickEvent("//input[@name='templateForm:j_idt112']");

    assertTrue(
        instanceTester.readDefaultInfoMessage(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER).contains("imported successfully"));
  }

  @Test
  public void test04removeServiceTemplate() throws Exception {
    instanceTester.buttonDefaultClickEvent("//td[@id='templateForm:j_idt87:0:j_idt94']/a");

    assertTrue(
        instanceTester.readDefaultInfoMessage(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_OK_AT_CONTROLLER).contains("deleted successfully"));
  }

  @Test
  public void test05setSettingsIntoController() throws Exception {
    instanceTester.changeValueInputInSpecificField("49:1", 56, changedUserID);
    instanceTester.changeValueInputInSpecificField("49:2", 56, String.valueOf(userkey));
    instanceTester.changeValueInputInSpecificField("49:3", 55, changedPassword);

    instanceTester.buttonClickEvent(75);

    assertEquals(changedUserID, instanceTester.readValue("49:1", 56));
    assertEquals(String.valueOf(userkey), instanceTester.readValue("49:2", 56));
    assertEquals(changedPassword, instanceTester.readValue("49:3", 55));

    assertTrue(instanceTester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test06undoSettingsIntoController() throws Exception {
    instanceTester.changeValueInputInSpecificField("49:1", 56, userid);
    instanceTester.changeValueInputInSpecificField("49:2", 56, "1000");
    instanceTester.changeValueInputInSpecificField("49:3", 55, userpassword);

    instanceTester.buttonClickEvent(76);

    assertEquals(userid, instanceTester.readValue("49:1", 56));
    assertEquals("1000", instanceTester.readValue("49:2", 56));
    assertEquals(userpassword, instanceTester.readValue("49:3", 55));
  }
}
