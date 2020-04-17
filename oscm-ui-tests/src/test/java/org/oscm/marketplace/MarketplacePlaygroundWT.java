/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 02-04-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplacePlaygroundWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginMarketplacePlayground(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  @AfterClass
  public static void cleanUp() {
    tester.logoutMarketplacePlayground();
    tester.close();
  }

  @Test
  public void test01_navBarLandingPage() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);
    final WebElement homeLink =
        tester.getDriver().findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_HOME_LINK));
    final String homeStyle = tester.getStyleClass(homeLink);
    final WebElement browseLink =
        tester
            .getDriver()
            .findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_BROWSE_SERVICES_LINK));
    final String browseStyle = tester.getStyleClass(browseLink);

    assertTrue(homeStyle.contains("active"));
    assertFalse(browseStyle.contains("active"));
  }

  @Test
  public void test02_navBarServicesPage() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_SERVICES);
    final WebElement homeLink =
        tester.getDriver().findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_HOME_LINK));
    final String homeStyle = tester.getStyleClass(homeLink);
    final WebElement browseLink =
        tester
            .getDriver()
            .findElement(By.id(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_BROWSE_SERVICES_LINK));
    final String browseStyle = tester.getStyleClass(browseLink);

    assertFalse(homeStyle.contains("active"));
    assertTrue(browseStyle.contains("active"));
  }

  @Test
  public void test03_cloudTag() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_SERVICES);

    assertTrue(tester.verifyFoundElement(By.linkText("cloud tag")));
  }

  @Test
  public void test04_landingPageCategories() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);

    assertTrue(
        tester.verifyFoundElement(
            By.id(MarketplaceHtmlElements.MARKETPLACE_LANDING_PAGE_CATEGORY_LINK)));
  }

  @Test
  public void test05_gotoMarketplacePlayground() throws Exception {
    tester.visitPortal(PortalPathSegments.GOTO_MARKETPLACE);
    tester.waitForElement(
        By.id(MarketplaceHtmlElements.GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE),
        WebTester.IMPLICIT_WAIT);
    tester.selectDropdown(
        MarketplaceHtmlElements.GOTO_MARKETPLACE_DROPDOWN_MARKETPLACE, "pg_marketplace");
    tester.waitForElementVisible(
        By.id(MarketplaceHtmlElements.GOTO_MARKETPLACE_BUTTONLINK_GOTO), WebTester.IMPLICIT_WAIT);
    tester.clickElement(MarketplaceHtmlElements.GOTO_MARKETPLACE_BUTTONLINK_GOTO);
    Assert.assertTrue(
        tester.getCurrentUrl().contains(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE));
  }
}
