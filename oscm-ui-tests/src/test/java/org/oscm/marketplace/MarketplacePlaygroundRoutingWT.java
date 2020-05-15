/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 15-04-2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.marketplace;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

public class MarketplacePlaygroundRoutingWT {

  private static PortalTester tester;

  @Rule public TestWatcher testWatcher = new JUnitHelper();

  @BeforeClass
  public static void setup() throws Exception {
    tester = new PortalTester();
    String userid = PlaygroundSuiteTest.supplierOrgAdminId;
    String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
    tester.loginMarketplacePlayground(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
  }

  // FIXME test ignored due #787
  @Ignore
  @Test
  public void test01_routing() {
    tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_TOGGLE_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_BROWSE_SERVICES_LINK);
    assertTrue(tester.getCurrentUrl().contains(MarketplacePathSegments.MARKETPLACE_SERVICES));
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_TOGGLE_BUTTON);
    tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_NAVBAR_HOME_LINK);
    assertTrue(tester.getCurrentUrl().contains(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE));
  }
}
