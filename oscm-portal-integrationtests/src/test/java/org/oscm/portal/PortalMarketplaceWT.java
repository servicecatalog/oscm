/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Feb 8, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.portal;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.oscm.webtest.PortalHtmlElements;
import org.oscm.webtest.PortalPathSegments;
import org.oscm.webtest.PortalTester;

/**
 * Integration web test to create an new marketplace.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalMarketplaceWT {

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

    private static final String MARKETPLACE = "mp_"
            + PlaygroundSuiteTest.currentTimestampe;
    private String marketplaceId;
    private static PortalTester tester;

    @BeforeClass
    public static void setup() throws Exception {
        tester = new PortalTester();
        String userid = tester.getPropertie(PortalTester.BES_ADMIN_USER_ID);
        String userpassword = tester
                .getPropertie(PortalTester.BES_ADMIN_USER_PWD);
        tester.loginPortal(userid, userpassword);
    }

    @AfterClass
    public static void cleanUp() {
        tester.logoutPortal();
        tester.close();
    }

    @Test
    public void test01create() throws Exception {
        marketplaceId = "";

        tester.visitPortal(PortalPathSegments.CREATE_MARKETPLACE);

        tester.writeValue(PortalHtmlElements.CREATE_MARKETPLACE_INPUT_NAME,
                MARKETPLACE);
        tester.writeValue(PortalHtmlElements.CREATE_MARKETPLACE_INPUT_ORG_ID,
                PlaygroundSuiteTest.supplierOrgId);

        tester.clickElement(PortalHtmlElements.CREATE_MARKETPLACE__BUTTON_SAVE);

        assertTrue(tester.getExecutionResult());
        PlaygroundSuiteTest.marketPlaceId = MARKETPLACE;
    }

    // @Test
    public void remove() throws Exception {

        marketplaceId = tester.getCreatedId(tester.readInfoMessage());

        if (marketplaceId == null || marketplaceId == "")
            throw new Exception(
                    "Marketplace " + MARKETPLACE + " doesn't exists!");

        tester.visitPortal(PortalPathSegments.DELETE_MARKETPLACE);
        tester.selectDropdown(
                PortalHtmlElements.DELETE_MARKETPLACE_DROPDOWN_IDLIST,
                marketplaceId);
        tester.waitForElement(
                By.id(PortalHtmlElements.DELETE_MARKETPLACE_BUTTON_DELETE), 10);
        tester.clickElement(
                PortalHtmlElements.DELETE_MARKETPLACE_BUTTON_DELETE);
        tester.waitForElement(
                By.id(PortalHtmlElements.DELETE_MARKETPLACE_BUTTON_CONFIRM),
                10);
        tester.clickElement(
                PortalHtmlElements.DELETE_MARKETPLACE_BUTTON_CONFIRM);

        assertTrue(tester.getExecutionResult());
    }

}
