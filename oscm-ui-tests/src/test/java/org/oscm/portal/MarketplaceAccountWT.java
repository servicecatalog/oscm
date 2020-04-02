package org.oscm.portal;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.webtest.PortalHtmlElements;
import org.oscm.webtest.PortalTester;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MarketplaceAccountWT {

    private static PortalTester tester;

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

    @BeforeClass
    public static void setup() throws Exception {
        tester = new PortalTester();
        String userid = PlaygroundSuiteTest.supplierOrgAdminId;
        String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
        tester.loginMarketplace(userid, userpassword, PlaygroundSuiteTest.marketPlaceId);
    }

    @AfterClass
    public static void cleanUp() {
        tester.logoutMarketplace();
        tester.close();
    }

    @Test
    public void test01fillPersonalData() {

        tester.clickElement(PortalHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
        tester.clickElementXPath(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SHOW_BUTTON);
        tester.selectDropdown(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_TITLE_DROPDOWN, "MR");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_FIRST_NAME_FIELD, "John");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LAST_NAME_FIELD, "Smith");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_EMAIL_FIELD, "john.smith@mail.com");
        tester.selectDropdown(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_LANGUAGE_DROPDOWN, "en");
        tester.clickElementXPath(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_BUTTON);
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_FIELD, "Smith's organization");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_EMAIL_FIELD, "smiths@mail.com");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_ORGANIZATION_ADDRESS_FIELD, "Australia\nSmall Village 10");
        tester.selectDropdown(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_COUNTRY_DROPDOWN, "AU");
        tester.clickElementXPath(PortalHtmlElements.MARKETPLACE_ACCOUNT_PROFILE_SAVE_BUTTON);

        assertTrue(tester.readInfoMessage().contains("Your profile has been successfully saved"));
        assertTrue(tester.verifyFoundElement(By.id(PortalHtmlElements.MARKETPLACE_SPAN_WELCOME)));
    }

    @Test
    public void test02addBillingAddress() throws InterruptedException {

        tester.clickElement(PortalHtmlElements.MARKETPLACE_ACCOUNT_BUTTON);
        tester.clickElement(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_BUTTON);
        tester.clickElementXPath(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_ADD_BILLING_ADDRESS_BUTTON);
        tester.waitForElement(By.id(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD), 10);
        Thread.sleep(1000);
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD, "Smiths billing");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_DISPLAY_FIELD, "Smiths billing");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_NAME_FIELD, "Smith's organization");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_EMAIL_FIELD, "smiths@mail.com");
        tester.writeValue(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_ADDRESS_FIELD, "Australia\nSmall Village 10");
        tester.clickElement(PortalHtmlElements.MARKETPLACE_ACCOUNT_PAYMENT_SAVE_BUTTON);

        assertTrue(tester.readInfoMessage().contains("The billing address has been successfully save"));
    }
}
