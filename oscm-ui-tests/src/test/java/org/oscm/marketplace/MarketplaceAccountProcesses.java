/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 12-08-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplaceAccountProcesses {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginMarketplace(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  @Test
  public void test01createTrigger() {

    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_NAV_LINK);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_ADD_TRIGGER);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_NAME,
        "Trigger register user");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TYPE, "REGISTER_OWN_USER");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TARGET, "REST_SERVICE");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_URL,
        "https://oscm-core:8080/oscm-soap-mock/NotificationService?wsdl");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_SUSPEND);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_SAVE);

    tester.readContentOfMessage();
    assertTrue(
        tester.readInfoMessage().contains("The trigger definition has been successfully created."));
  }

  @Test
  public void test02checkTrigger() {

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_EDIT);
    String triggerName =
        tester.readText(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_EDIT);

    assertEquals("Trigger register user", triggerName);
  }

  @Test
  public void test03editTrigger() {

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_EDIT);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_NAME,
        "[EDIT] Trigger register user");

    tester.readContentOfMessage();
    assertTrue(
        tester.readInfoMessage().contains("The trigger definition has been successfully saved."));
  }

  @Test
  public void test04createTriggerToRemove() {

    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_NAV_LINK);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_ADD_TRIGGER);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_NAME,
        "Trigger to be removed");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TYPE, "SUBSCRIBE_TO_SERVICE");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_TARGET, "WEB_SERVICE");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_URL,
        "https://trigger/to/be/removed.com");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_SUSPEND);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_SAVE);

    tester.readContentOfMessage();
    assertTrue(
        tester.readInfoMessage().contains("The trigger definition has been successfully created."));
  }

  @Test
  public void test05deleteTrigger() {

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_EDIT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROCESSES_TRIGGER_DELETE);
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_DELETE_BILLING_CONFIRM);

    tester.readContentOfMessage();
    assertTrue(tester.readInfoMessage().contains("The trigger definition has been deleted."));
  }
}
