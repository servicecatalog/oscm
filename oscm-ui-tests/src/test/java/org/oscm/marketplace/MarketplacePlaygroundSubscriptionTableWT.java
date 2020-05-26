/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 12-05-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;
import org.oscm.webtest.WebTester;

import static org.junit.Assert.assertEquals;

public class MarketplacePlaygroundSubscriptionTableWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = tester.getProperty(PortalTester.BES_ADMIN_USER_ID);
    String userpassword = tester.getProperty(PortalTester.BES_ADMIN_USER_PWD);
    tester.loginMarketplacePlayground(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  @AfterClass
  public static void cleanUp() {
    tester.logoutMarketplacePlayground();
    tester.close();
  }

  @Test
  public void test01checkSubscriptionTableInAccountPage() throws InterruptedException {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SHOW_SUBSCRIPTIONS_BUTTON);
    tester.waitForElement(
        By.xpath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FILTER_FIELD),
        WebTester.IMPLICIT_WAIT);
    tester.writeValueXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FILTER_FIELD, "h");
    Thread.sleep(2000);
    assertEquals(
        "HSubscriptionName810",
        tester.readTextXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FIRST_ROW));

    tester.clearInput(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FILTER_FIELD);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SECOND_PAGE);
    Thread.sleep(2000);
    assertEquals(
        "BSubscriptionName",
        tester.readTextXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FIRST_ROW));

    tester.writeValueXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SEARCH_FIELD, "GSubscri");
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SEARCH_BUTTON);
    Thread.sleep(2000);
    assertEquals(
        "GSubscriptionName810",
        tester.readTextXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_FIRST_ROW));

    tester.clearInput(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SEARCH_FIELD);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SEARCH_BUTTON);
    Thread.sleep(2000);
    tester.clickElementXPath(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_EXPAND);
    tester.clickElementXPath(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_MANAGE_SUBSCRIPTIONS);
    Thread.sleep(2000);
    assertEquals(
        "ZSubscriptionName810",
        tester.readTextXPath(
            MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SUBSCRIPTIONS_SERVICE_DETAILS));
  }
}
