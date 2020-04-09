/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 10 7, 2018
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.*;

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
        By.id(MarketplaceHtmlElements.GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE),
        WebTester.IMPLICIT_WAIT);
    tester.selectDropdown(
        MarketplaceHtmlElements.GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE,
        PlaygroundSuiteTest.marketPlaceId);
    tester.waitForElementVisible(
        By.id(MarketplaceHtmlElements.GOTO_MARKETPLACE_BUTTONLINK_GOTO), WebTester.IMPLICIT_WAIT);
    tester.clickElement(MarketplaceHtmlElements.GOTO_MARKETPLACE_BUTTONLINK_GOTO);
    Assert.assertTrue(tester.getCurrentUrl().contains(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE));
  }

  @Test
  public void test02createSubscription() {

    String referenceNo = WebTester.getCurrentTime();
    String subscriptionName = "sub_" + PlaygroundSuiteTest.currentTimestampe;
    tester.visitMarketplace(MarketplacePathSegments.INDEX_MARKETPLACE);
    tester.waitForElement(
        By.id(MarketplaceHtmlElements.MARKETPLACE_LINK_SERVICE_NAME), WebTester.IMPLICIT_WAIT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_LINK_SERVICE_NAME);
    tester.waitForElement(
        By.xpath(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW),
        WebTester.IMPLICIT_WAIT);

    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW);
    tester.waitForElement(
        By.id(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT),
        WebTester.IMPLICIT_WAIT);
    tester
        .getDriver()
        .findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME))
        .clear();
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME, subscriptionName);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER, referenceNo);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTONLINK_NEXT);
    tester.waitForElementVisible(
        By.id(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE),
        WebTester.IMPLICIT_WAIT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTONLINK_CONFIRM);
    Assert.assertTrue(tester.getExecutionResult());
  }
}
