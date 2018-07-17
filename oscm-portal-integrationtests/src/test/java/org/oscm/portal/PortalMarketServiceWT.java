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
 * Integration web test to create a marketable service.
 * 
 * @author miethaner
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalMarketServiceWT {

    private static final String TECHSERVICE_IAAS_USER_ID = "DummyUser";
    private static final String TECHSERVICE_IAAS_USER_PWD = "DummyPwd123";
    private static final String marketServiceName = "ms_"
            + PlaygroundSuiteTest.currentTimestampe;
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
    public void test01createService() throws Exception {

        tester.visitPortal(PortalPathSegments.DEFINE_MARKETSERVICE);
        tester.waitForElement(By.id(
                PortalHtmlElements.DEFINE_MARKETSERVICE_DROPDOWN_SERVICENAME),
                5);
        Select dropdownServiceName = new Select(tester.getDriver().findElement(
                By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_DROPDOWN_SERVICENAME)));
        dropdownServiceName.selectByVisibleText("AppSampleService");
        tester.waitForElementVisible(
                By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_BUTTONLINK_SAVE),
                5);
        tester.writeValue(
                PortalHtmlElements.DEFINE_MARKETSERVICE_INPUT_SERVICEID,
                marketServiceName);
        tester.getDriver().findElement(By
                .id(PortalHtmlElements.DEFINE_MARKETSERVICE_INPUT_SERVICENAME))
                .clear();
        tester.writeValue(
                PortalHtmlElements.DEFINE_MARKETSERVICE_INPUT_SERVICENAME,
                marketServiceName);
        setDescriptionValue(PortalTester.TECHSERVICE_PARAM_EMAIL,
                PlaygroundSuiteTest.supplierOrgAdminMail);
        setDescriptionValue(PortalTester.TECHSERVICE_PARAM_MESSAGETEXT,
                "You are welcome!");
        setDescriptionValue(PortalTester.TECHSERVICE_PARAM_USER,
                TECHSERVICE_IAAS_USER_ID);
        setDescriptionValue(PortalTester.TECHSERVICE_PARAM_PWD,
                TECHSERVICE_IAAS_USER_PWD);
        checkCheckBox(PortalTester.TECHSERVICE_PARAM_EMAIL);
        checkCheckBox(PortalTester.TECHSERVICE_PARAM_MESSAGETEXT);
        checkCheckBox(PortalTester.TECHSERVICE_PARAM_USER);
        checkCheckBox(PortalTester.TECHSERVICE_PARAM_PWD);

        tester.waitForElementVisible(
                By.id(PortalHtmlElements.DEFINE_MARKETSERVICE_BUTTONLINK_SAVE),
                10);
        tester.clickElement(
                PortalHtmlElements.DEFINE_MARKETSERVICE_BUTTONLINK_SAVE);
        tester.log("Params: marketServiceName:=" + marketServiceName + " ");
        assertTrue(tester.getExecutionResult());
        PlaygroundSuiteTest.marketServiceName = marketServiceName;
        PlaygroundSuiteTest.techServiceUserId = TECHSERVICE_IAAS_USER_ID;
        PlaygroundSuiteTest.techServiceUserPwd = TECHSERVICE_IAAS_USER_PWD;
    }

    @Test
    public void test02definePreisModel() throws Exception {

        tester.visitPortal(PortalPathSegments.DEFINE_PREICEMODEL);
        Select dropdownServiceName = new Select(tester.getDriver().findElement(
                By.id(PortalHtmlElements.DEFINE_PRICEMODEL_DROPDOWN_SERVICENAME)));
        dropdownServiceName
                .selectByVisibleText(PlaygroundSuiteTest.marketServiceName);
        tester.waitForElementVisible(
                By.id(PortalHtmlElements.DEFINE_PRICEMODEL_BUTTON_SAVE), 10);
        if (!tester.getDriver().findElement(By.id(
                PortalHtmlElements.DEFINE_PRICEMODEL_CHECKBOX_FREE_OF_CHARGE))
                .isSelected()) {
            tester.clickElement(
                    PortalHtmlElements.DEFINE_PRICEMODEL_CHECKBOX_FREE_OF_CHARGE);
        }
        tester.waitForElementVisible(
                By.id(PortalHtmlElements.DEFINE_PRICEMODEL_BUTTON_SAVE), 10);
        tester.clickElement(PortalHtmlElements.DEFINE_PRICEMODEL_BUTTON_SAVE);

        assertTrue(tester.getExecutionResult());

    }

    @Test
    public void test03definePublishOption() throws Exception {

        tester.visitPortal(PortalPathSegments.DEFINE_PUBLISHOPTION);
        tester.waitForElement(By.id(
                PortalHtmlElements.DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME),
                10);
        Select dropdownServiceName = new Select(tester.getDriver().findElement(
                By.id(PortalHtmlElements.DEFINE_PUBLISH_OPTION_DROPDOWN_SERVICENAME)));
        dropdownServiceName
                .selectByVisibleText(PlaygroundSuiteTest.marketServiceName);

        Select dropdownMarketplace = new Select(tester.getDriver().findElement(
                By.id(PortalHtmlElements.DEFINE_PUBLISH_OPTION_DROPDOWN_MARKETPLACE)));
        dropdownMarketplace.selectByValue(PlaygroundSuiteTest.supplierOrgId);

        tester.waitForElementVisible(
                By.id(PortalHtmlElements.DEFINE_PUBLISH_OPTION_BUTTON_SAVE),
                10);
        tester.clickElement(
                PortalHtmlElements.DEFINE_PUBLISH_OPTION_BUTTON_SAVE);

        assertTrue(tester.getExecutionResult());

    }

    @Test
    public void test04activeService() throws Exception {

        tester.visitPortal(PortalPathSegments.ACTIVE_MARKETSERVICE);
        tester.waitForElement(
                By.id(PortalHtmlElements.DEACTIVATION_SERVICE_TABLE), 5);

        String serviceXpath = "//table[@id='"
                + PortalHtmlElements.DEACTIVATION_SERVICE_TABLE
                + "']//span[.= '" + PlaygroundSuiteTest.marketServiceName
                + "']/../../td[1]/input";
        if (!tester.getDriver().findElement(By.xpath(serviceXpath))
                .isSelected()) {
            tester.getDriver().findElement(By.xpath(serviceXpath)).click();
        }
        tester.clickElement(
                PortalHtmlElements.DEACTIVATION_SERVICE_BUTTON_SAVE);
        assertTrue(tester.getExecutionResult());
    }

    private void setDescriptionValue(String description, String value) {
        String descriptionXpath = "//table[@id='"
                + PortalHtmlElements.DEFINE_MARKETSERVICE_PARAM_TABLE
                + "']//span[.= '" + description + "']/../../../td[3]/div/input";
        tester.getDriver().findElement(By.xpath(descriptionXpath)).clear();
        tester.getDriver().findElement(By.xpath(descriptionXpath))
                .sendKeys(value);
    }

    private void checkCheckBox(String label) {
        if (!tester.getDriver()
                .findElement(By
                        .xpath("//*[span='" + label + "']/../../td[2]//input"))
                .isSelected())
            tester.getDriver()
                    .findElement(By.xpath(
                            "//*[span='" + label + "']/../../td[2]//input"))
                    .click();
    }

}
