package org.oscm.marketplace;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.PortalTester;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplaceAccountWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginMarketplace(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  @AfterClass
  public static void cleanUp() {
    tester.logoutMarketplace();
    tester.close();
  }

  @Test
  public void test01fillOrganizationData() {

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_BUTTON);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_FIELD,
        "Smiths organization");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_EMAIL_FIELD,
        "smiths.organization@email.com");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_ADDRESS_FIELD,
        "Australia\nSmall Village 10");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_COUNTRY_DROPDOWN, "AU");
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON);

    assertTrue(tester.readInfoMessage().contains("Your profile has been successfully saved"));
    assertTrue(tester.verifyFoundElement(By.id(MarketplaceHtmlElements.MARKETPLACE_SPAN_WELCOME)));
  }

  @Test
  public void test02fillPersonalData() throws InterruptedException {

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON);
    if (tester
            .getDriver()
            .findElements(
                By.xpath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_BUTTON))
            .size()
        != 0) {
      tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_BUTTON);
    }
    tester.selectDropdown(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN, "MR");
    Thread.sleep(1000);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD, "Jacob");
    tester.writeValue(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD, "Smith");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD, "jacob.smith@email.com");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN, "en");
    if (tester
            .getDriver()
            .findElements(
                By.id(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON))
            .size()
        != 0) {
      tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON);
    } else {
      tester.clickElement(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON_OIDC);
    }

    assertTrue(tester.readInfoMessage().contains("Your profile has been successfully saved"));
  }

  @Test
  public void test03addBillingAddress() throws InterruptedException {

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_BUTTON);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_ADD_BILLING_ADDRESS_BUTTON);
    tester.waitForElement(
        By.id(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD), 10);
    Thread.sleep(1000);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD, "Smiths billing");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD, "Smiths billing");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_NAME_FIELD, "Smiths organization");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_EMAIL_FIELD,
        "smiths.organization@email.com");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_ADDRESS_FIELD,
        "Australia\nSmall Village 10");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_SAVE_BUTTON);

    assertTrue(tester.readInfoMessage().contains("The billing address has been successfully save"));
  }

  @Test
  public void test04addPaymentType() throws InterruptedException {
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_MANAGE_PAYMENT_TABLE);
    Thread.sleep(1000);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_NAME_FIELD, "Smiths payment");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_SAVE_BUTTON);

    assertTrue(
        tester.readInfoMessage().contains("The payment information has been successfully saved"));
  }

  @Test
  public void test05addUser() throws InterruptedException {

      tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
      tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_SHOW_BUTTON);
    if (tester
            .getDriver()
            .findElements(By.xpath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_ADD_USER_BUTTON))
            .size()
            != 0) {
      tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_ADD_USER_BUTTON);
      tester.selectDropdown(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN, "MS");
      tester.writeValue(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD, "Isabella");
      tester.writeValue(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD, "Smith");
      tester.writeValue(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD,
          "isabella.smith@email.com");
      tester.writeValue(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_USERID_FIELD, "isabelSmith");
      tester.selectDropdown(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN, "de");
      tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_CREATE_BUTTON);
      Thread.sleep(1000);
      assertTrue(
          tester
              .readInfoMessage()
              .contains("The user isabelSmith has been successfully registered."));
    }
  }

  @Test
  public void test06addUserRoles() throws InterruptedException {
    if (tester
            .getDriver()
            .findElements(
                By.xpath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_ADD_ROLE_CHECKBOX))
            .size()
        != 0) {
      tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_ADD_ROLE_CHECKBOX);
      tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_ROLE_ASSIGN_BUTTON);
      Thread.sleep(1000);
      assertTrue(
          tester
              .readInfoMessage()
              .contains("The changes for user isabelSmith have been successfully saved"));
    }
  }

  @Test
  public void test07addOrganization() throws InterruptedException {
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_SHOW_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_UNIT_BUTTON);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_UNIT_BUTTON);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ORG_NAME_FIELD,
        "Isabels organization");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ORG_ID_FIELD, "isabelorg");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ORG_DESCRIPTION_FIELD,
        "Isabel's family organization");
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_CREATE_BUTTON);
    Thread.sleep(1000);
    assertTrue(
        tester
            .readInfoMessage()
            .contains(
                "The organizational unit Isabels organization has been successfully created."));
  }

  @Test
  public void test08addUsersToOrganization() throws InterruptedException {
    tester.clickElement(
        "editGroupForm:usersInGroup:" + PlaygroundSuiteTest.supplierOrgAdminId + ":assignCheckbox");
    if (tester
            .getDriver()
            .findElements(
                By.id(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_USER_CHECKBOX))
            .size()
        != 0) {
      tester.clickElement(
          MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ADD_USER_CHECKBOX);
    }
    tester.selectDropdown(
        "editGroupForm:usersInGroup:" + PlaygroundSuiteTest.supplierOrgAdminId + ":unitRole",
        "ADMINISTRATOR");
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_ASSIGN_ROLE_BUTTON);
    Thread.sleep(1000);
    assertTrue(
        tester
            .readInfoMessage()
            .contains("The organizational unit Isabels organization has been successfully saved."));
  }

  @Test
  public void test09deleteOrganization() throws InterruptedException {
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_USERS_SHOW_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_UNIT_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_DELETE_INPUT);
    Thread.sleep(1000);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_ORGANIZATION_DELETE_CONFIRM_BUTTON);
    Thread.sleep(1000);
    assertTrue(
        tester
            .readInfoMessage()
            .contains(
                "The organizational unit Isabels organization has been successfully deleted."));
  }

  @Test
  public void test10addProcessTrigger() throws InterruptedException {
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_SHOW_BUTTON);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_ADD_PROCESS_BUTTON);
    Thread.sleep(1000);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_NAME_FIELD, "Trigger");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TYPE_DROPDOWN,
        "SUBSCRIBE_TO_SERVICE");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TARGET_TYPE_DROPDOWN, "REST_SERVICE");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TARGET_URL_FIELD,
        "https://webiste.io");
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TARGET_SAVE_BUTTON);

    assertTrue(
        tester.readInfoMessage().contains("The trigger definition has been successfully created."));
  }
}
