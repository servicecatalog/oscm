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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplaceAccountPersonalWT {

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
    Thread.sleep(500);
    tester.selectDropdown(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN, "MR");
    Thread.sleep(500);
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

  @Test
  public void test04SaveColorInputs() throws InterruptedException {
    // given themable colors as hex values
    String primaryColor = "#269534";
    String fontColor = "#071181";
    String navbarBgColor = "#bb1c37";
    String navbarLinkColor = "#ffd9ec";
    String inputBgColor = "#ffbbbb";

    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_ACCOUNT);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON_ID);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_PERSONAL_NAV);
    Thread.sleep(500);

    // when setting color values and Saving "tabUser" page.
    tester.writeColorValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_PRIMARY_COLOR_INPUT, primaryColor);
    tester.writeColorValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_FONT_COLOR_INPUT, fontColor);
    tester.writeColorValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_NAVBAR_COLOR_INPUT, navbarBgColor);
    tester.writeColorValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_NAVBAR_LINK_COLOR_INPUT,
        navbarLinkColor);
    tester.writeColorValue(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_INPUT_COLOR_INPUT, inputBgColor);

    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_USER_SAVE_BUTTON);

    // then verify Success message and saved color values.
    tester.readContentOfMessage();
    assertTrue(tester.readInfoMessage().contains("Your profile has been successfully saved"));

    tester.verifyEqualElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_PRIMARY_COLOR_INPUT, primaryColor);
    tester.verifyEqualElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_FONT_COLOR_INPUT, fontColor);
    tester.verifyEqualElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_NAVBAR_COLOR_INPUT, navbarBgColor);
    tester.verifyEqualElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_NAVBAR_LINK_COLOR_INPUT,
        navbarLinkColor);
    tester.verifyEqualElement(
        MarketplaceHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_INPUT_COLOR_INPUT, inputBgColor);

    // verify :root element properties
    tester.getRootPropertyValue("--oscm-primary-h");
  }
}
