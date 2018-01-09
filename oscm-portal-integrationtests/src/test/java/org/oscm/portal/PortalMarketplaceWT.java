/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 8, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.portal;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.oscm.webtest.WebTester;

/**
 * Integration web test to create an new marketplace.
 * 
 * @author miethaner
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PortalMarketplaceWT {

    private static final String USER = "administrator";
    private static final String PASSWORD = "admin123";

    private static final String MARKETPLACE = "mp_test";

    private static WebTester tester;

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

        tester.visitPortal("shop/createMarketplace.jsf");

        tester.writeValue("createMarketplaceForm:marketplaceName", MARKETPLACE);
        tester.writeValue("createMarketplaceForm:organizationIdInput",
                PlaygroundSuiteTest.supplierOrgId);

        tester.clickElement("createMarketplaceForm:saveButtonLink");

        System.out.println(tester.readInfoMessage());
    }
}
