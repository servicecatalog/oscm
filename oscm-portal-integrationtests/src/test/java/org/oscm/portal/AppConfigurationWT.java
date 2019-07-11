/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 20 6, 2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.portal;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.oscm.webtest.AppConfigurationTester;
import org.oscm.webtest.AppControllerDBTester;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppConfigurationWT {

    private static AppConfigurationTester tester;
    private static AppControllerDBTester dbTester;
    private static String TEST_CONTROLLER_ID = "ess.sample";

    @Rule
    public TestWatcher testWatcher = new JUnitHelper();

    @BeforeClass
    public static void setup() throws Exception {
        tester = new AppConfigurationTester();
        dbTester = new AppControllerDBTester();
        String userid = tester
                .getPropertie(AppConfigurationTester.APP_ADMIN_USER_ID);
        String userpassword = tester
                .getPropertie(AppConfigurationTester.APP_ADMIN_USER_PWD);
        tester.loginAppConfig(userid, userpassword);
        dbTester.clearSetting(TEST_CONTROLLER_ID);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        tester.logoutAppConfig();
        tester.close();
        dbTester.close();

    }

    @Test
    public void test01setConfiguration() throws Exception {

        tester.setAppAdminMailAddress(PlaygroundSuiteTest.supplierOrgAdminMail);
        tester.setBssUserId(
                tester.getPropertie(AppConfigurationTester.APP_ADMIN_USER_ID));
        tester.setBssUserPwd(
                tester.getPropertie(AppConfigurationTester.APP_ADMIN_USER_PWD));
        tester.setBssUserKey("1000");
    }

    @Test
    public void test02createNewControllerId() throws Exception {

        try {
            tester.registerController(TEST_CONTROLLER_ID,
                    PlaygroundSuiteTest.supplierOrgId);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contentEquals(
                    AppConfigurationTester.ERROR_MSG_CONTROLLER_EXISTS))
                tester.changeOrgIdOnController(TEST_CONTROLLER_ID,
                        PlaygroundSuiteTest.supplierOrgId);
        }

        PlaygroundSuiteTest.controllerId = TEST_CONTROLLER_ID;

        dbTester.insertSetting("BSS_USER_ID",
                PlaygroundSuiteTest.supplierOrgAdminId,
                PlaygroundSuiteTest.controllerId);
        dbTester.insertSetting("BSS_USER_KEY",
                PlaygroundSuiteTest.supplierOrgAdminUserkey,
                PlaygroundSuiteTest.controllerId);
        dbTester.insertSetting("BSS_USER_PWD",
                PlaygroundSuiteTest.supplierOrgAdminPwd,
                PlaygroundSuiteTest.controllerId);
        dbTester.insertSetting("BSS_ORGANIZATION_ID", "PLATFORM_OPERATOR",
                PlaygroundSuiteTest.controllerId);
        dbTester.insertSetting("VERSION", "1.0",
                PlaygroundSuiteTest.controllerId);
        dbTester.insertSetting("APP_PROVISIONING_ON_INSTANCE", "false",
                PlaygroundSuiteTest.controllerId);
        dbTester.updateEncryptPWDasAdmin(PlaygroundSuiteTest.controllerId);
    }
}
