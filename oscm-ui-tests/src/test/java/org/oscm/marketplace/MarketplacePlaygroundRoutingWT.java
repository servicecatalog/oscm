package org.oscm.marketplace;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

import static org.junit.Assert.assertTrue;

public class MarketplacePlaygroundRoutingWT {

    private static PortalTester tester;

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

    @BeforeClass
    public static void setup() throws Exception {
        tester = new PortalTester();
        String userid = PlaygroundSuiteTest.supplierOrgAdminId;
        String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
        tester.loginMarketplacePlayground(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
    }

    @Test
    public void test01_routing(){
        tester.visitMarketplace(MarketplacePathSegments.MARKETPLACE_LANDING_PAGE);
        tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_BROWSE_ALL_SERVICES_BUTTON);
        assertTrue(tester.getCurrentUrl().endsWith(MarketplacePathSegments.MARKETPLACE_SERVICES));
        tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_LANDING_PAGE_CATEGORY_LINK);
        assertTrue(tester.getCurrentUrl().endsWith(MarketplacePathSegments.MARKETPLACE_SERVICES));
    }

}
