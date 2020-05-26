/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: Feb 8, 2017
 *
 * <p>*****************************************************************************
 */
package org.oscm.portal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.webtest.*;

import javax.security.auth.login.LoginException;

import static org.junit.Assert.*;

/** Integration web test to create an organization. */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalOrganizationWT {

  private static String org;
  private static String orgAdmin;
  private static String orgAdminEmail;
  private static int PASSWORD_LENGTH = 8;
  private static int USERKEY_LENGTH = 5;
  private static String orgAdminPassword;
  private static String authMode;
  private static PortalTester tester;

  private static final Logger logger = LogManager.getLogger(PortalOrganizationWT.class.getName());
  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    logger.info("PortalOrganizationWT tests setup");
    tester = new PortalTester();
    String userid = tester.getProperty(WebTester.BES_ADMIN_USER_ID);
    String userpassword = tester.getProperty(WebTester.BES_ADMIN_USER_PWD);
    tester.loginPortal(userid, userpassword);
    authMode = tester.getProperty(WebTester.AUTH_MODE);
    if (authMode.equals("OIDC")) {
      logger.info("Running OIDC mode");
      tester.deleteSupplierGroup("OIDC_UI_TEST_ORG");
      org = "OIDC_UI_TEST_ORG";
      orgAdmin = tester.getProperty(WebTester.OIDC_SUPPLIER_ID);
      orgAdminPassword = tester.getProperty(WebTester.OIDC_SUPPLIER_PASSWORD);
    } else {
      logger.info("Running INTERNAL mode");
      org = PlaygroundSuiteTest.currentTimestampe;
      orgAdmin = "mp_admin_" + org;
    }
    orgAdminEmail = "mp_email_" + org + "@test.com";
  }

  @AfterClass
  public static void cleanUp() {

    tester.logoutPortal();
    tester.close();
  }

  @Test
  public void test01createSupplierOrg() throws Exception {
    tester.visitPortal(PortalPathSegments.CREATE_ORGANIZATION);
    if (!authMode.equals("OIDC")) {
      tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ADMINEMAIL, orgAdminEmail);
      tester.selectDropdown(PortalHtmlElements.CREATE_ORGANIZATION_DROPDOWN_LANGUAGE, "en");
    }
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_DESIRED_USERID, orgAdmin);

    tester.clickElement(PortalHtmlElements.CREATE_ORGANIZATION_CHECKBOX_TPROVIDER);
    tester.waitForElement(By.id(PortalHtmlElements.CREATE_ORGANIZATION_FORM_UPLOADIMAGE), 10);
    tester.clickElement(PortalHtmlElements.CREATE_ORGANIZATION_CHECKBOX_SUPPLIER);
    tester.waitForElement(By.id(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_REVENUESHARE), 10);
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_REVENUESHARE, "5");
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGNAME, org);
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGEMAIL, orgAdminEmail);
    tester.selectDropdown(PortalHtmlElements.CREATE_ORGANIZATION_DROPDOWN_ORGLOCALE, "en");
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGPHONE, "123");
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGURL, "http://abc.de");
    tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGADDRESS, "ADDRESS");
    tester.selectDropdown(PortalHtmlElements.CREATE_ORGANIZATION_DROPDOWN_ORGCOUNTRY, "DE");

    tester.clickElement(PortalHtmlElements.CREATE_ORGANIZATION_BUTTON_SAVE);

    assertTrue(tester.getExecutionResult());
    PlaygroundSuiteTest.supplierOrgName = org;
    PlaygroundSuiteTest.supplierOrgId = tester.readInfoMessage().split(" ")[2];
    PlaygroundSuiteTest.supplierOrgAdminId = orgAdmin;
    PlaygroundSuiteTest.supplierOrgAdminMail = orgAdminEmail;
    PlaygroundSuiteTest.supplierOrgAdminPwd = orgAdminPassword;
  }

  @Test
  public void test02readEmailForPassword() throws Exception {
    if (authMode.equals("OIDC")) {
      logger.info("OIDC MODE SKIPPING TEST");
    } else {
      Thread.sleep(30000);

      String body =
          tester.readLatestEmailWithSubject(EmailMessageElements.EMAIL_CREATE_ACCOUNT_TOPIC);

      String phrasePassword = EmailMessageElements.EMAIL_CREATE_ACCOUNT_PHRASE_PWD + " ";
      assertNotNull(body);

      int index = body.indexOf(phrasePassword);
      assertTrue(index > 0);
      orgAdminPassword =
          body.substring(
              index + phrasePassword.length(), index + phrasePassword.length() + PASSWORD_LENGTH);
      assertTrue(orgAdminPassword != "");
      tester.log(
          "password from " + PlaygroundSuiteTest.supplierOrgAdminMail + " is: " + orgAdminPassword);
    }
  }

  @Test
  public void test03ChangePassword() throws LoginException, InterruptedException {
    if (authMode.equals("OIDC")) {
      logger.info("OIDC MODE SKIPPING TEST");
    } else {
      tester.logoutPortal();
      tester.loginPortal(PlaygroundSuiteTest.supplierOrgAdminId, orgAdminPassword);

      tester.writeValue(PortalHtmlElements.PORTAL_PASSWORD_INPUT_CURRENT, orgAdminPassword);
      tester.writeValue(
          PortalHtmlElements.PORTAL_PASSWORD_INPUT_CHANGE,
          tester.getProperty(WebTester.BES_ADMIN_USER_PWD));
      tester.writeValue(
          PortalHtmlElements.PORTAL_PASSWORD_INPUT_REPEAT,
          tester.getProperty(WebTester.BES_ADMIN_USER_PWD));
      tester.clickElement(PortalHtmlElements.PORTAL_PASSWORD_BUTTON_SAVE);
      tester.wait(WebTester.IMPLICIT_WAIT);
      String currentURL = tester.getCurrentUrl();
      assertTrue(currentURL.contains(PortalPathSegments.IMPORT_TECHNICALSERVICE));
      PlaygroundSuiteTest.supplierOrgAdminPwd = tester.getProperty(WebTester.BES_ADMIN_USER_PWD);
    }
  }

  @Test
  public void test04readEmailForUserKey() throws Exception {

    String body =
        tester.readLatestEmailWithSubject(EmailMessageElements.EMAIL_CREATE_ACCOUNT_TOPIC);
    String phraseUserKey = EmailMessageElements.EMAIL_CREATE_ACCOUNT_PHRASE_KEY + " ";
    assertNotNull(body);

    int index = body.indexOf(phraseUserKey);
    assertTrue(index > 0);
    String userKey =
        body.substring(
            index + phraseUserKey.length(), index + phraseUserKey.length() + USERKEY_LENGTH);
    assertNotEquals("", userKey);
    tester.log("userKey from " + PlaygroundSuiteTest.supplierOrgAdminId + " is: " + userKey);
    PlaygroundSuiteTest.supplierOrgAdminUserkey = userKey;
  }
}
