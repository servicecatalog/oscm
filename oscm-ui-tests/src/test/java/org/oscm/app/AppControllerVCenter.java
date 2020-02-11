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
public class AppControllerVCenter {

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
    instanceTester.loginAppServiceInstance(userid, userpassword, "-vmware/");
  }

  @AfterClass
  public static void cleanUp() {
    instanceTester.close();
  }

  @Test
  public void test01setSettingsAPIvSphere() throws Exception {
    instanceTester.changeValueInputInBalancerField("url", "https://webiste.com");
    instanceTester.changeValueInputInBalancerField("user", userid);
    instanceTester.changeValueInputInBalancerField("pwd", userpassword);

    instanceTester.buttonDefaultClickEvent("//input[@name='balancer_form:j_idt120']");
    instanceTester.readDefaultInfoMessage(
        AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_FIELD_UPPER);

    assertEquals("https://webiste.com", instanceTester.readDefaultValue("url"));
    assertEquals(userid, instanceTester.readDefaultValue("user"));
    assertEquals(userpassword, instanceTester.readDefaultValue("pwd"));
  }

  @Test
  public void test02importServiceTemplate() throws Exception {
    createdFile = folder.newFile("vcenter.csv");
    FileUtils.writeStringToFile(createdFile, "TKey,Name,Identifier,URL,UserId,Password,", "UTF-8");
    instanceTester.uploadFileEvent("//input[@id='csv_form:csvFile']", createdFile);
    instanceTester.buttonDefaultClickEvent("//input[@name='csv_form:j_idt138']");

    assertTrue(
        instanceTester
            .readDefaultInfoMessage(AppHtmlElements.APP_CONFIG_LICLASS_STATUS_MSG_FIELD_UPPER)
            .contains("saved successfully"));
  }

  @Test
  public void test03setSettingsIntoController() throws Exception {
    instanceTester.changeValueInputInSpecificField("47:1", 54, changedUserID);
    instanceTester.changeValueInputInSpecificField("47:2", 54, String.valueOf(userkey));
    instanceTester.changeValueInputInSpecificField("47:3", 53, changedPassword);

    instanceTester.buttonClickEvent(58);

    assertEquals(changedUserID, instanceTester.readValue("47:1", 54));
    assertEquals(String.valueOf(userkey), instanceTester.readValue("47:2", 54));
    assertEquals(changedPassword, instanceTester.readValue("47:3", 53));
  }
}
