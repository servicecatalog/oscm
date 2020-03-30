/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 10 7, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.portal;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.webtest.PortalHtmlElements;
import org.oscm.webtest.PortalPathSegments;
import org.oscm.webtest.PortalTester;
import org.oscm.webtest.WebTester;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplaceSubscriptionWT {
  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginPortal(userid, userpassword);
  }

  @AfterClass
  public static void cleanUp() {
    tester.logoutMarketplace();
    tester.close();
  }

  @Test
  public void test01gotoMarketPlace() throws Exception {

    tester.visitPortal(PortalPathSegments.GOTO_MARKETPLACE);
    tester.waitForElement(
        By.id(PortalHtmlElements.GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE), WebTester.IMPLICIT_WAIT);
    tester.selectDropdown(
        PortalHtmlElements.GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE, PlaygroundSuiteTest.marketPlaceId);
    tester.waitForElementVisible(
        By.id(PortalHtmlElements.GOTO_MARKETPLACE_BUTTONLINK_GOTO), WebTester.IMPLICIT_WAIT);
    tester.clickElement(PortalHtmlElements.GOTO_MARKETPLACE_BUTTONLINK_GOTO);
    Assert.assertTrue(
        tester.verifyFoundElement(By.id(PortalHtmlElements.MARKETPLACE_SPAN_WELCOME)));
  }

  @Test
  public void test02createSubscription() {

    String referenceNo = WebTester.getCurrentTime();
    String subscriptionName = "sub_" + PlaygroundSuiteTest.currentTimestampe;
    tester.visitMarketplace(PortalPathSegments.INDEX_MARKETPLACE);
    tester.waitForElement(By.id(PortalHtmlElements.MARKETPLACE_LINK_SERVICE_NAME), WebTester.IMPLICIT_WAIT);
    tester.clickElement(PortalHtmlElements.MARKETPLACE_LINK_SERVICE_NAME);
    tester.waitForElement(
        By.xpath(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW),
        WebTester.IMPLICIT_WAIT);

    tester.clickElementXPath(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW);
    tester.waitForElement(
        By.id(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT),
        WebTester.IMPLICIT_WAIT);
    tester
        .getDriver()
        .findElement(By.id(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME))
        .clear();
    tester.writeValue(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME, subscriptionName);
    tester.writeValue(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER, referenceNo);
    tester.clickElement(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT);
    tester.waitForElementVisible(
        By.id(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE),
        WebTester.IMPLICIT_WAIT);
    tester.clickElement(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE);
    tester.clickElement(PortalHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTONLINK_CONFIRM);
    Assert.assertTrue(tester.getExecutionResult());
  }
}
