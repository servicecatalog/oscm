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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.oscm.webtest.WebTester;

/**
 * Integration web test to create an organization.
 * 
 * @author miethaner
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalOrganizationWT {

    private static final String USER = "administrator";
    private static final String PASSWORD = "admin123";

    private static final String ORG = "mp_owner_31";
    private static final String ORG_ADMIN = ORG + "_admin";

    private static final int PASSWORD_LENGTH = 8;

    private static WebTester tester;
    private static String password;

    @BeforeClass
    public static void setup() throws Exception {
        tester = new WebTester();
        tester.visitPortal("");
        tester.loginPortal(USER, PASSWORD);
    }

    @AfterClass
    public static void cleanUp() {
        tester.logoutPortal();
        tester.close();
    }

    @Test
    public void test01Create() {

        tester.visitPortal("operator/createOrganization.jsf");

        // org admin
        tester.writeValue("editForm:administratorEmail",
                tester.getEmailAddress());
        tester.writeValue("editForm:administratorUserId", ORG_ADMIN);
        tester.selectDropdown("editForm:administratorLocale", "en");

        // org
        tester.clickElement("editForm:checkboxRoleTechnologyProvider");
        tester.waitForElement("editForm:image", 10);
        tester.clickElement("editForm:checkboxRoleSupplier");
        tester.waitForElement("editForm:operatorRevenueShare", 10);
        tester.writeValue("editForm:operatorRevenueShare", "5");
        tester.writeValue("editForm:organizationName", ORG);
        tester.writeValue("editForm:organizationEmail",
                tester.getEmailAddress());
        tester.selectDropdown("editForm:organizationLocale", "en");
        tester.writeValue("editForm:organizationPhone", "123");
        tester.writeValue("editForm:organizationUrl", "http://abc.de");
        tester.writeValue("editForm:organizationAddress", "ADDRESS");
        tester.selectDropdown("editForm:organizationCountry", "DE");

        tester.clickElement("editForm:saveButtonLink");

        PlaygroundSuiteTest.supplierOrgId = tester.readInfoMessage()
                .split(" ")[2];

        System.out.println(PlaygroundSuiteTest.supplierOrgId);
    }

    @Test
    public void test02ReadEmail() throws Exception {

        Thread.sleep(30000);

        String body = tester.readLatestEmailWithSubject("Account created");

        String phrase = "Your initial password is: ";

        assertNotNull(body);

        int index = body.indexOf(phrase);

        assertTrue(index > 0);

        password = body.substring(index + phrase.length(),
                index + phrase.length() + PASSWORD_LENGTH);

        System.out.println(password);
    }

    @Test
    public void test03ChangePassword() {

        tester.logoutPortal();

        tester.loginPortal(ORG_ADMIN, password);

        tester.writeValue("passwordform:currentPassword", password);
        tester.writeValue("passwordform:password", PASSWORD);
        tester.writeValue("passwordform:password2", PASSWORD);

        tester.clickElement("passwordform:changeButtonLink");
    }
}
