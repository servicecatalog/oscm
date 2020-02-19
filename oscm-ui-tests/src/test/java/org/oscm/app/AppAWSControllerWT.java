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

  private static AppControllerTester controllerTester;
  private static String userKey;
  private static String changedUserID;
  private static String changedPassword;
  private static String userID;
  private static String userPassword;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    controllerTester = new AppControllerTester();

    userKey = "1000";
    changedUserID = "newUser";
    changedPassword = "Password12";

    userID = controllerTester.getProperties(AppControllerTester.APP_ADMIN_USER_ID);
    userPassword = controllerTester.getProperties(AppControllerTester.APP_ADMIN_USER_PWD);
    controllerTester.loginAppController(
        userID, userPassword, AppPathSegments.APP_PATH_CONTROLLER_AWS);
  }

  @AfterClass
  public static void cleanUp() {
    controllerTester.close();
  }

  @Test
  public void test01setSettingsIntoSpecificController() throws Exception {
    controllerTester.changeValueInputInSpecificField("62:0", 68, changedUserID);
    controllerTester.changeValueInputInSpecificField("62:1", 68, changedPassword);

    controllerTester.buttonClickEvent(75);

    assertEquals(changedUserID, controllerTester.readValue("62:0", 68));
    assertEquals(changedPassword, controllerTester.readValue("62:1", 68));

    assertTrue(controllerTester.readInfoMessage().contains("saved successfully"));
  }

  @Test
  public void test02undoSettingsIntoSpecificController() throws Exception {
    controllerTester.changeValueInputInSpecificField("62:0", 68, "nothing");
    controllerTester.changeValueInputInSpecificField("62:1", 68, "nothing");

    controllerTester.buttonClickEvent(76);

    assertEquals(changedUserID, controllerTester.readValue("62:0", 68));
    assertEquals(changedPassword, controllerTester.readValue("62:1", 68));
  }

  @Test
  public void test03setSettingsIntoController() throws Exception {
    controllerTester.changeValueInputInSpecificField("49:1", 56, changedUserID);
    controllerTester.changeValueInputInSpecificField("49:2", 56, userKey);
    controllerTester.changeValueInputInSpecificField("49:3", 55, changedPassword);

    controllerTester.buttonClickEvent(75);

    assertEquals(changedUserID, controllerTester.readValue("49:1", 56));
    assertEquals(userKey, controllerTester.readValue("49:2", 56));
    assertEquals(changedPassword, controllerTester.readValue("49:3", 55));
  }

  @Test
  public void test04undoSettingsIntoController() throws Exception {
    controllerTester.changeValueInputInSpecificField("49:1", 56, userID);
    controllerTester.changeValueInputInSpecificField("49:2", 56, "1000");
    controllerTester.changeValueInputInSpecificField("49:3", 55, userPassword);

    controllerTester.buttonClickEvent(76);

    assertEquals(userID, controllerTester.readValue("49:1", 56));
    assertEquals("1000", controllerTester.readValue("49:2", 56));
    assertEquals(userPassword, controllerTester.readValue("49:3", 55));
  }
}
