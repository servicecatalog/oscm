/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 07.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.samlsp.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.samlsp.ws.base.WebserviceSAMLSPTestSetup;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;

/**
 * @author gao
 * 
 */
public class MockSTSWSTest {

    private static VOFactory factory = new VOFactory();
    private IdentityService idS;
    private IdentityService adminIdentityService;
    private final String testUserId1 = "MockSTSTest_Issuer";
    private final String testUserId2 = "MockSTSTest_DigestValue";
    private final String testUserId3 = "MockSTSTest_SignatureValue";
    private final String testUserId4 = "MockSTSTest_X509Certificate";
    private final String testUserId5 = "MockSTSTest_NameID";
    private final String testUserId6 = "MockSTSTest_KeyIdentifier";
    private final String testUserId7 = "MockSTSTest_CipherValue";
    private VOUserDetails user1;
    private VOUserDetails user2;
    private VOUserDetails user3;
    private VOUserDetails user4;
    private VOUserDetails user5;
    private VOUserDetails user6;
    private VOUserDetails user7;

    private final static String EXCEPTION_SUBSTRING = "WSSTUBE0025";

    @Before
    public void setUp() throws Exception {
        new WebserviceSAMLSPTestSetup();
        adminIdentityService = ServiceFactory.getSTSServiceFactory()
                .getIdentityService("administrator", "admin123");
        createUsers();
    }

    @After
    public void clean() throws Exception {
        deleteUsers();
    }

    @Test
    public void testSecuredWS_OK() throws Exception {
        
        // given
        idS = adminIdentityService;

        // when
        VOUserDetails user = invokeWSMethod();

        // then
        assertEquals("administrator", user.getUserId());
    }
    
    

    @Test
    public void testSecuredWS_Modify_Issuer() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_Issuer", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    @Test
    public void testSecuredWS_Modify_DigestValue() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_DigestValue", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    @Test
    public void testSecuredWS_Modify_SignatureValue() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_SignatureValue", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    @Test
    public void testSecuredWS_Modify_X509Certificate() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_X509Certificate", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    @Test
    public void testSecuredWS_Modify_NameID() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_NameID", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    @Test
    public void testSecuredWS_Modify_KeyIdentifier() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_KeyIdentifier", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    @Test
    public void testSecuredWS_Modify_CipherValue() throws Exception {
        idS = ServiceFactory.getSTSServiceFactory().getIdentityService(
                "MockSTSTest_CipherValue", "admin123");
        try {
            invokeWSMethod();
            fail();
        } catch (WebServiceException e) {
            assertThat(e.getMessage(), containsString(EXCEPTION_SUBSTRING));
        }
    }

    private VOUserDetails invokeWSMethod() throws Exception {
        VOUserDetails user = idS.getCurrentUserDetails();
        return user;
    }

    private VOUserDetails createUser(String userId) throws Exception {
        VOUserDetails user = factory.createUserVO(userId);
        String organizationId = adminIdentityService.getCurrentUserDetails()
                .getOrganizationId();
        user.setOrganizationId(organizationId);
        List<UserRoleType> userRoles = new ArrayList<UserRoleType>();
        userRoles.add(UserRoleType.PLATFORM_OPERATOR);
        adminIdentityService.createUser(user, userRoles, null);
        user = adminIdentityService.getUserDetails(user);
        return user;
    }

    private void createUsers() throws Exception {
        user1 = createUser(testUserId1);
        user2 = createUser(testUserId2);
        user3 = createUser(testUserId3);
        user4 = createUser(testUserId4);
        user5 = createUser(testUserId5);
        user6 = createUser(testUserId6);
        user7 = createUser(testUserId7);
    }

    private void deleteUsers() throws Exception {
        adminIdentityService.deleteUser(user1, "");
        adminIdentityService.deleteUser(user2, "");
        adminIdentityService.deleteUser(user3, "");
        adminIdentityService.deleteUser(user4, "");
        adminIdentityService.deleteUser(user5, "");
        adminIdentityService.deleteUser(user6, "");
        adminIdentityService.deleteUser(user7, "");
    }

}
