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

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplacePlaygroundAccountPersonalWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginMarketplacePlayground(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  @Test
  public void test01fillOrganizationData() {

    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON_ID);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_NAV);
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
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON_ID);

    tester.readContentOfMessage();
    assertTrue(tester.readInfoMessage().contains("Your profile has been successfully saved"));
  }

  @Test
  public void test02fillPersonalData() throws InterruptedException {

    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON_ID);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_NAV);
    tester.selectDropdown(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN, "MR");
    Thread.sleep(1000);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD, "Jacob");
    tester.writeValue(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD, "Smith");
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD, "jacob.smith@email.com");
    tester.selectDropdown(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN, "en");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON);

    tester.readContentOfMessage();
    assertTrue(tester.readInfoMessage().contains("Your profile has been successfully saved"));
  }

  @Test
  public void test03customerAttributes() {

    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON_ID);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_NAV);
    tester.writeValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_VALUE_FIELD, "value");
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ATTRIBUTE_SAVE_BUTTON);

    tester.readContentOfMessage();
    assertTrue(tester.readInfoMessage().contains("The attributes have been successfully saved"));
  }
}
