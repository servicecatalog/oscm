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

import static org.junit.Assert.assertTrue;

public class MarketplacePlaygroundSubscriptionWT {

    private static PortalTester tester;

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

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
    public void test01_createSubscriptionWithoutPayment(){
        String referenceNo = WebTester.getCurrentTime();
        String subscriptionName = "sub_" + PlaygroundSuiteTest.currentTimestampe;
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
