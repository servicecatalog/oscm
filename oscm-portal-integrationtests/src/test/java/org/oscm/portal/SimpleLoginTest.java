package org.oscm.portal;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.webtest.AppConfigurationTester;
import org.oscm.webtest.PortalTester;
import org.oscm.webtest.WebTester;

public class SimpleLoginTest {

    private static PortalTester tester;
    private static AppConfigurationTester appTester;

    @BeforeClass
    public static void setup() throws Exception {
        tester = new PortalTester();
        appTester = new AppConfigurationTester();
    }

    @Test
    public void testPortalLogin() throws Exception {

        String userId = tester.getPropertie(WebTester.BES_ADMIN_USER_ID);
        String userPwd = tester.getPropertie(WebTester.BES_ADMIN_USER_PWD);
        tester.loginPortal(userId, userPwd);
        tester.logoutPortal();
    }

    @Test
    public void testAppLogin() throws Exception {

        String userId = tester.getPropertie(WebTester.BES_ADMIN_USER_ID);
        String userPwd = tester.getPropertie(WebTester.BES_ADMIN_USER_PWD);
        appTester.loginAppConfig(userId, userPwd);
    }


}
