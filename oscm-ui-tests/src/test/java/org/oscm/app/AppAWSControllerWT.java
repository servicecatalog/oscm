package org.oscm.app;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.webtest.app.AppControllerTester;
import org.oscm.webtest.app.AppPathSegments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppAWSControllerWT {

  private static AppControllerTester instanceTester;
  private static String userKey;
  private static String changedUserID;
  private static String changedPassword;
  private static String userID;
  private static String userPassword;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    instanceTester = new AppControllerTester();

    userKey = "1000";
    changedUserID = "newUser";
    changedPassword = "Password12";

    userID = instanceTester.getProperties(AppControllerTester.APP_ADMIN_USER_ID);
    userPassword = instanceTester.getProperties(AppControllerTester.APP_ADMIN_USER_PWD);
    instanceTester.loginAppController(
        userID, userPassword, AppPathSegments.APP_PATH_CONTROLLER_AWS);
  }

  @AfterClass
  public static void cleanUp() {
    instanceTester.close();
  }

  @Test
  public void test01setSettingsIntoSpecificController() throws Exception {
    instanceTester.changeValueInputInSpecificField("62:0", 68, changedUserID);
    instanceTester.changeValueInputInSpecificField("62:1", 68, changedPassword);

    instanceTester.buttonClickEvent(75);

    assertEquals(changedUserID, instanceTester.readValue("62:0", 68));
    assertEquals(changedPassword, instanceTester.readValue("62:1", 68));

    assertTrue(instanceTester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test02undoSettingsIntoSpecificController() throws Exception {
    instanceTester.changeValueInputInSpecificField("62:0", 68, "nothing");
    instanceTester.changeValueInputInSpecificField("62:1", 68, "nothing");

    instanceTester.buttonClickEvent(76);

    assertEquals(changedUserID, instanceTester.readValue("62:0", 68));
    assertEquals(changedPassword, instanceTester.readValue("62:1", 68));
  }

  @Test
  public void test03setSettingsIntoController() throws Exception {
    instanceTester.changeValueInputInSpecificField("49:1", 56, changedUserID);
    instanceTester.changeValueInputInSpecificField("49:2", 56, userKey);
    instanceTester.changeValueInputInSpecificField("49:3", 55, changedPassword);

    instanceTester.buttonClickEvent(75);

    assertEquals(changedUserID, instanceTester.readValue("49:1", 56));
    assertEquals(userKey, instanceTester.readValue("49:2", 56));
    assertEquals(changedPassword, instanceTester.readValue("49:3", 55));
  }

  @Test
  public void test04undoSettingsIntoController() throws Exception {
    instanceTester.changeValueInputInSpecificField("49:1", 56, userID);
    instanceTester.changeValueInputInSpecificField("49:2", 56, "1000");
    instanceTester.changeValueInputInSpecificField("49:3", 55, userPassword);

    instanceTester.buttonClickEvent(76);

    assertEquals(userID, instanceTester.readValue("49:1", 56));
    assertEquals("1000", instanceTester.readValue("49:2", 56));
    assertEquals(userPassword, instanceTester.readValue("49:3", 55));
  }
}
