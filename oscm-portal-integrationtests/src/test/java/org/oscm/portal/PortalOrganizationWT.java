/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: Feb 8, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.portal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.security.auth.login.LoginException;

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
import org.oscm.webtest.WebTester;

/**
 * Integration web test to create an organization.
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalOrganizationWT {

    private static final String ORG = PlaygroundSuiteTest.currentTimestampe;
    private static final String ORG_ADMIN = "mp_admin_" + ORG;
    private static final int PASSWORD_LENGTH = 8;
    private static final int USERKEY_LENGTH = 5;
    private static String passwordOrgAdmin = "";
    private static PortalTester tester;

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

    @BeforeClass
    public static void setup() throws Exception {

        tester = new PortalTester();
        String userid = tester.getPropertie(WebTester.BES_ADMIN_USER_ID);
        String userpassword = tester.getPropertie(WebTester.BES_ADMIN_USER_PWD);
        tester.loginPortal(userid, userpassword);
    }

    @AfterClass
    public static void cleanUp() {

        tester.logoutPortal();
        tester.close();
    }

    @Test
    public void test01createSupplierOrg() throws Exception {
        tester.visitPortal(PortalPathSegments.CREATE_ORGANIZATION);

        tester.writeValue(
                PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ADMINEMAIL,
                tester.getEmailAddress());
        tester.writeValue(
                PortalHtmlElements.CREATE_ORGANIZATION_INPUT_DESIRED_USERID,
                ORG_ADMIN);
        tester.selectDropdown(
                PortalHtmlElements.CREATE_ORGANIZATION_DROPDOWN_LANGUAGE, "en");

        tester.clickElement(
                PortalHtmlElements.CREATE_ORGANIZATION_CHECKBOX_TPROVIDER);
        tester.waitForElement(
                By.id(PortalHtmlElements.CREATE_ORGANIZATION_FORM_UPLOADIMAGE),
                10);
        tester.clickElement(
                PortalHtmlElements.CREATE_ORGANIZATION_CHECKBOX_SUPPLIER);
        tester.waitForElement(By.id(
                PortalHtmlElements.CREATE_ORGANIZATION_INPUT_REVENUESHARE), 10);
        tester.writeValue(
                PortalHtmlElements.CREATE_ORGANIZATION_INPUT_REVENUESHARE, "5");
        tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGNAME,
                ORG);
        tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGEMAIL,
                tester.getEmailAddress());
        tester.selectDropdown(
                PortalHtmlElements.CREATE_ORGANIZATION_DROPDOWN_ORGLOCALE,
                "en");
        tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGPHONE,
                "123");
        tester.writeValue(PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGURL,
                "http://abc.de");
        tester.writeValue(
                PortalHtmlElements.CREATE_ORGANIZATION_INPUT_ORGADDRESS,
                "ADDRESS");
        tester.selectDropdown(
                PortalHtmlElements.CREATE_ORGANIZATION_DROPDOWN_ORGCOUNTRY,
                "DE");

        tester.clickElement(PortalHtmlElements.CREATE_ORGANIZATION_BUTTON_SAVE);

        assertTrue(tester.getExecutionResult());
        PlaygroundSuiteTest.supplierOrgName = ORG;
        PlaygroundSuiteTest.supplierOrgId = tester.readInfoMessage()
                .split(" ")[2];
        PlaygroundSuiteTest.supplierOrgAdminId = ORG_ADMIN;
        PlaygroundSuiteTest.supplierOrgAdminMail = tester.getEmailAddress();
    }

    @Test
    public void test02readEmailForPassword() throws Exception {
        Thread.sleep(30000);

        String body = tester.readLatestEmailWithSubject(
                tester.getPropertie("email.createaccount.head"));

        String phrasePassword = tester
                .getPropertie("email.createaccount.phrase.password") + " ";
        assertNotNull(body);

        int index = body.indexOf(phrasePassword);
        assertTrue(index > 0);
        passwordOrgAdmin = body.substring(index + phrasePassword.length(),
                index + phrasePassword.length() + PASSWORD_LENGTH);
        assertTrue(passwordOrgAdmin != "");
        tester.log("password from " + tester.getEmailAddress() + " is: "
                + passwordOrgAdmin);
    }

    @Test
    public void test03ChangePassword()
            throws LoginException, InterruptedException {
        tester.logoutPortal();
        tester.loginPortal(PlaygroundSuiteTest.supplierOrgAdminId,
                passwordOrgAdmin);

        tester.writeValue(PortalHtmlElements.PORTAL_PASSWORD_INPUT_CURRENT,
                passwordOrgAdmin);
        tester.writeValue(PortalHtmlElements.PORTAL_PASSWORD_INPUT_CHANGE,
                tester.getPropertie(WebTester.BES_ADMIN_USER_PWD));
        tester.writeValue(PortalHtmlElements.PORTAL_PASSWORD_INPUT_REPEAT,
                tester.getPropertie(WebTester.BES_ADMIN_USER_PWD));
        tester.clickElement(PortalHtmlElements.PORTAL_PASSWORD_BUTTON_SAVE);
        tester.wait(WebTester.IMPLICIT_WAIT);
        String currentURL = tester.getCurrentUrl();
        assertTrue(currentURL
                .contains(PortalPathSegments.IMPORT_TECHNICALSERVICE));
        PlaygroundSuiteTest.supplierOrgAdminPwd = tester
                .getPropertie(WebTester.BES_ADMIN_USER_PWD);
    }

    @Test
    public void test04readEmailForUserkey() throws Exception {

        String body = tester.readLatestEmailWithSubject(
                tester.getPropertie("email.createaccount.head"));
        String phraseUserKey = tester
                .getPropertie("email.createaccount.phrase.userkey") + " ";
        assertNotNull(body);

        int index = body.indexOf(phraseUserKey);
        assertTrue(index > 0);
        String userKey = body.substring(index + phraseUserKey.length(),
                index + phraseUserKey.length() + USERKEY_LENGTH);
        assertTrue(userKey != "");
        tester.log("userKey from " + PlaygroundSuiteTest.supplierOrgAdminId
                + " is: " + userKey);
        PlaygroundSuiteTest.supplierOrgAdminUserkey = userKey;
    }
}
