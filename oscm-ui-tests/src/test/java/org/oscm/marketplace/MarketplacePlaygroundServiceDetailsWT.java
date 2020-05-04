/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 22-04-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplacePlaygroundServiceDetailsWT {

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

  @Before
  public void navigate() {}

  @Test
  public void test01_createReview() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SHOW_SERVICE_DETAILS_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_WRITE_REVIEW_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_REVIEW_STARS_5);
    tester.writeValue(MarketplaceHtmlElements.MARKETPLACE_REVIEW_TITLE_INPUT, "Review");
    tester.writeValue(MarketplaceHtmlElements.MARKETPLACE_REVIEW_COMMENT_INPUT, "Comment");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_REVIEW_SAVE_BUTTON);
    assertTrue(tester.verifyFoundElement(By.id(MarketplaceHtmlElements.MARKETPLACE_REVIEW_BLOCK)));
  }

  @Test
  public void test02_deleteReview() {
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_WRITE_REVIEW_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_REVIEW_REMOVE_BUTTON);
    assertFalse(tester.verifyFoundElement(By.id(MarketplaceHtmlElements.MARKETPLACE_REVIEW_BLOCK)));
  }

  @Test
  public void test03_deactivateService() throws InterruptedException {
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_DEACTIVATE_SERVICE_LINK);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_DEACTIVATE_SERVICE_REASON_INPUT,
        "Reason");
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_DEACTIVATE_SERVICE_REASON_BUTTON);
    Thread.sleep(1000);

    assertTrue(
        tester
            .getStyleClass(
                MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_SERVICE_ACTIVATION_INDICATOR)
            .contains("serverSubscriptionSuspended"));
  }

  @Test
  public void test04_reactivateService() throws InterruptedException {
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_REACTIVATE_SERVICE_LINK);
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_REACTIVATE_SERVICE_REASON_BUTTON);
    Thread.sleep(1000);

    assertTrue(
        tester
            .getStyleClass(
                MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_SERVICE_ACTIVATION_INDICATOR)
            .contains("serverSubscriptionActive"));
  }
}
