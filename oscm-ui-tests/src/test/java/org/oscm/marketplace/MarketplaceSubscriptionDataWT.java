/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 04-05-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;
import org.oscm.webtest.WebTester;

@RunWith(Parameterized.class)
public class MarketplaceSubscriptionDataWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @Parameterized.Parameter(0)
  public String subscriptionName;

  @Parameterized.Parameter(1)
  public String referenceNo;

  @Parameterized.Parameter(2)
  public String testBilling;

  @Parameterized.Parameters(name = "subscriptionName={0}, referenceNo={1}, testBilling={2}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"ASubscriptionName", "Ref_num_001", "Test billing 001"},
          {"BSubscriptionName", "Ref_num_002", "Test billing 002"},
          {"CSubscriptionName", "Ref_num_003", "Test billing 003"},
          {"DSubscriptionName", "Ref_num_004", "Test billing 004"},
          {"ESubscriptionName810", "Ref_num_005", "Test billing 005"},
          {"FSubscriptionName810", "Ref_num_006", "Test billing 006"},
          {"GSubscriptionName810", "Ref_num_007", "Test billing 007"},
          {"HSubscriptionName810", "Ref_num_008", "Test billing 008"},
          {"ISubscriptionName810", "Ref_num_009", "Test billing 009"},
          {"XSubscriptionName810", "Ref_num_010", "Test billing 010"},
          {"YSubscriptionName810", "Ref_num_011", "Test billing 011"},
          {"ZSubscriptionName810", "Ref_num_012", "Test billing 012"},
        });
  }

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = tester.getProperty(PortalTester.BES_ADMIN_USER_ID);
    String userpassword = tester.getProperty(PortalTester.BES_ADMIN_USER_PWD);
    tester.loginMarketplace(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
    ;
  }

  @AfterClass
  public static void cleanUp() {
    tester.logoutMarketplace();
    tester.close();
  }

  @Test
  public void test01_createSubscriptions() throws InterruptedException {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SHOW_SERVICE_DETAILS_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_GETITNOW);
    tester
        .getDriver()
        .findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME))
        .clear();
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_SUBNAME, subscriptionName);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_INPUT_REFNUMBER, referenceNo);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_NEXT);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_LINK);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_DISPLAY_NAME,
        testBilling);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_ORG_NAME, "Test org");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_EMAIL,
        "email@email.com");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_ADDRESS,
        "Some address, Some country");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_NEW_BILLING_ADDRESS_SAVE);
    Thread.sleep(2000);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_NEXT);

    tester.waitForElementVisible(
        By.id(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE),
        WebTester.IMPLICIT_WAIT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_CHECKBOX_LICENSEAGREE);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SUBSCRIPTION_BUTTON_CONFIRM);
    assertTrue(tester.getExecutionResult());

    // FIXME Remove when new marketplace subscription page is finished
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);
  }
}
