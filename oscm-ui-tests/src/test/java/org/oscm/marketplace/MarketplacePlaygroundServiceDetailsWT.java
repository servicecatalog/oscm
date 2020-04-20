package org.oscm.marketplace;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.openqa.selenium.By;
import org.oscm.portal.JUnitHelper;
import org.oscm.portal.PlaygroundSuiteTest;
import org.oscm.webtest.MarketplaceHtmlElements;
import org.oscm.webtest.MarketplacePathSegments;
import org.oscm.webtest.PortalTester;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MarketplacePlaygroundServiceDetailsWT {

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

    @AfterClass
    public static void cleanUp() {
        tester.logoutMarketplacePlayground();
        tester.close();
    }

    @Before
    public void navigate(){

    }

    @Test
    public void test01_createReview (){
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
    public void test02_deleteReview(){
        tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SHOW_SERVICE_DETAILS_BUTTON);
        tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_SERVICE_DETAILS_WRITE_REVIEW_BUTTON);
        tester.clickElement(MarketplaceHtmlElements.MARKETPLACE_REVIEW_REMOVE_BUTTON);
        assertFalse(tester.verifyFoundElement(By.id(MarketplaceHtmlElements.MARKETPLACE_REVIEW_BLOCK)));
    }
}
