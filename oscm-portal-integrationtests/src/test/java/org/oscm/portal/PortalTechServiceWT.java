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
import org.openqa.selenium.support.ui.Select;
import org.oscm.webtest.PortalHtmlElements;
import org.oscm.webtest.PortalPathSegments;
import org.oscm.webtest.PortalTester;

/**
 * Integration web test to create a technical service.
 * 
 * @author sxu
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalTechServiceWT {

    private static final String FILE_PATH_IMPORT_TECHSERVICE = "technicalservice.xml.path";
    private static final String IMPORT_TECHSERV_NAME = "technicalservice.name";

    private static PortalTester tester;

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

    @BeforeClass
    public static void setup() throws Exception {
        tester = new PortalTester();
        String userid = PlaygroundSuiteTest.supplierOrgAdminId;
        String userpassword = PlaygroundSuiteTest.supplierOrgAdminPwd;
        tester.loginPortal(userid, userpassword);
    }

    @AfterClass
    public static void cleanUp() {
        tester.logoutPortal();
        tester.close();
    }

    @Test
    public void test01importTechService() throws Exception {

        tester.visitPortal(PortalPathSegments.IMPORT_TECHNICALSERVICE);

        String pathFile = tester.getPropertie(FILE_PATH_IMPORT_TECHSERVICE);
        tester.log("Import file " + pathFile + " as technical service.");
        tester.getDriver()
                .findElement(By
                        .id(PortalHtmlElements.IMPORT_TECHSERVICE_UPLOAD_INPUT))
                .sendKeys(pathFile);
        tester.wait(5);
        tester.clickElement(
                PortalHtmlElements.IMPORT_TECHSERVICE_UPLOAD_BUTTON);

        assertTrue(tester.getExecutionResult());

    }

    @Test
    public void test02updateTechService() throws Exception {

        String importTechSerName = tester.getPropertie(IMPORT_TECHSERV_NAME);
        tester.visitPortal(PortalPathSegments.UPDATE_TECHNICALSERVICE);
        Select dropdownServiceName = new Select(tester.getDriver().findElement(
                By.id(PortalHtmlElements.UPDATE_TECHSERVICE_DROPDOWN_SERVICENAME)));
        dropdownServiceName.selectByVisibleText(importTechSerName);

        tester.waitForElementVisible(
                By.id(PortalHtmlElements.UPDATE_TECHSERVICE_BUTTONLINK_SAVE),
                10);
        setParamDescription("APP_CONTROLLER_ID",
                PlaygroundSuiteTest.controllerId);
        setParamDescription("PARAM_EMAIL",
                PortalTester.TECHSERVICE_PARAM_EMAIL);
        setParamDescription("PARAM_USER", PortalTester.TECHSERVICE_PARAM_USER);
        setParamDescription("PARAM_PWD", PortalTester.TECHSERVICE_PARAM_PWD);
        setParamDescription("PARAM_MESSAGETEXT",
                PortalTester.TECHSERVICE_PARAM_MESSAGETEXT);

        tester.clickElement(
                PortalHtmlElements.UPDATE_TECHSERVICE_BUTTONLINK_SAVE);
        assertTrue(tester.getExecutionResult());
        PlaygroundSuiteTest.techServiceName = importTechSerName;
    }

    private void setParamDescription(String paramId, String paramDescription) {
        String descriptionXpath = "//table[@id='"
                + PortalHtmlElements.UPDATE_TECHSERVICE_PARAM_TABLE
                + "']//span[.= '" + paramId + "']/../../td[2]/input";
        tester.getDriver().findElement(By.xpath(descriptionXpath)).clear();
        tester.getDriver().findElement(By.xpath(descriptionXpath))
                .sendKeys(paramDescription);
    }

}
