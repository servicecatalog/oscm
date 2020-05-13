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

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

// FIXME remove ignore and fix

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplacePlaygroundAccountWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = tester.getProperty(PortalTester.BES_ADMIN_USER_ID);
    String userpassword = tester.getProperty(PortalTester.BES_ADMIN_USER_PWD);
    tester.loginMarketplacePlayground(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  @Test
  public void test01addBillingAddress() throws Exception {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SHOW_PAYMENTS_BUTTON);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_ADD_BILLING_ADDRESS);
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

    assertTrue(tester.getExecutionResult());
  }

  @Test
  public void test02addBillingAddressFailure() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SHOW_PAYMENTS_BUTTON);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_ADD_BILLING_ADDRESS);
    tester.waitForElement(
        By.id(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD), 10);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_SAVE_BUTTON);

    assertTrue(
        tester.verifyFoundElement(
            By.id(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_BILLING_MODAL_ERROR)));
  }

  @Test
  public void test03removeBillingAddress() throws InterruptedException {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SHOW_PAYMENTS_BUTTON);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_EDIT_BILLING_ADDRESS);
    Thread.sleep(1000);
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_DELETE_BILLING_ADDRESS);
    Thread.sleep(1000);
    tester.clickElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_DELETE_BILLING_CONFIRM);

    assertTrue(tester.getExecutionResult());
  }

  @Test
  public void test04addPaymentTypeFailure() throws InterruptedException {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SHOW_PAYMENTS_BUTTON);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_ADD_PAYMENT_TYPE);

    Thread.sleep(1000);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_ADD_PAYMENT_TYPE_NEXT);

    assertTrue(
        tester.verifyFoundElement(
            By.id(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_PAYMENT_MODAL_ERROR)));
  }

  @Test
  public void test05updatePaymentType() throws InterruptedException {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_SHOW_PAYMENTS_BUTTON);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENTS_EDIT_PAYMENT);

    Thread.sleep(1000);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_NAME_FIELD, "Smiths payment");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_PAYMENT_SAVE_BUTTON);

    assertTrue(tester.getExecutionResult());
  }
}
