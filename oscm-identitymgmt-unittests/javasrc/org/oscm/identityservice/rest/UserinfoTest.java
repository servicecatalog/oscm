package org.oscm.identityservice.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.identityservice.model.UserinfoModel;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.vo.VOUserDetails;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class UserinfoTest {

    Userinfo userinfo;
    ConfigurationServiceLocal cs;

    @Before
    public void setUp() {
        userinfo = spy(new Userinfo());
    }

    @Test
    public void testCreateUrl() {
        // given
        String expected = "http://oscm-identity:9090/oscm-identity/tenants/token/users/userId";

        // when
        String result = userinfo.createUrl("userId", "token");

        // then
        assertEquals(expected, result);
    }

    @Test(expected = Exception.class)
    public void testGetUserinfoFromIdentityService() throws Exception {
        // given
        String userId = "userId";
        String tenantId = "tenantId";
        String token = "token";

        // when
        VOUserDetails result = userinfo.getUserinfoFromIdentityService(userId, tenantId,
                token);

        // then
    }

    @Test
    public void testMapGenderToSalutationMale() {
        // given
        String gender = "male";
        Salutation expected = Salutation.MR;

        // when
        Salutation result = userinfo.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapGenderToSalutationFemale() {
        // given
        String gender = "female";
        Salutation expected = Salutation.MS;

        // when
        Salutation result = userinfo.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapGenderToSalutationUnknown() {
        // given
        String gender = "?";
        Salutation expected = Salutation.MS;

        // when
        Salutation result = userinfo.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapGenderToSalutationDefault() {
        // given
        String gender = " ";
        Salutation expected = Salutation.MS;

        // when
        Salutation result = userinfo.mapGenderToSalutation(gender);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testMapUserInfoToUserDetailsFirstname() {
        // given
        UserinfoModel userInfoModel = new UserinfoModel();
        userInfoModel.setFirstName("first");

        // when
        VOUserDetails result = userinfo.mapUserInfoToUserDetails(userInfoModel, "");

        // then
        assertEquals("", "first", result.getFirstName());
    }

    @Test
    public void testMapUserInfoToUserDetailsLastname() {
        // given
        UserinfoModel userInfoModel = new UserinfoModel();
        userInfoModel.setLastName("last");

        // when
        VOUserDetails result = userinfo.mapUserInfoToUserDetails(userInfoModel, "");

        // then
        assertEquals("", "last", result.getLastName());
    }

    @Test
    public void testMapUserInfoToUserDetailsAdress() {
        // given
        UserinfoModel userInfoModel = new UserinfoModel();
        userInfoModel.setAddress("teststreet 123");

        // when
        VOUserDetails result = userinfo.mapUserInfoToUserDetails(userInfoModel, "");

        // then
        assertEquals("", "teststreet 123", result.getAddress());
    }

    @Test
    public void testMapUserInfoToUserDetailsEmail() {
        // given
        UserinfoModel userInfoModel = new UserinfoModel();
        userInfoModel.setEmail("first.last@test.com");

        // when
        VOUserDetails result = userinfo.mapUserInfoToUserDetails(userInfoModel, "");

        // then
        assertEquals("", "first.last@test.com", result.getEMail());
    }

    @Test
    public void testMapUserInfoToUserDetailsSaluatation() {
        // given
        UserinfoModel userInfoModel = new UserinfoModel();
        userInfoModel.setGender("male");

        // when
        VOUserDetails result = userinfo.mapUserInfoToUserDetails(userInfoModel, "");

        // then
        assertEquals("", Salutation.MR, result.getSalutation());
    }

    @Test
    public void testCreateUserDetails() {
        //given
         String input = "{\"firstName\": \"first\","
                 + " \"lastName\": \"last\", "
                 + "\"email\": \"first.last@test.com\", "
                 + "\"phone\": \"123\", " 
                 + "\"country\": \"Germany\", "
                 + "\"city\": \"testcity\", "
                 + "\"address\": \"teststreet 123\", "
                 + "\"postalCode\": \"11111\"}";

         //when
         VOUserDetails result = userinfo.createUserDetails(input, "");

         //then
         assertEquals("", Salutation.MS, result.getSalutation());
         assertEquals("", "first.last@test.com", result.getEMail());
         assertEquals("", "teststreet 123", result.getAddress());
         assertEquals("", "last", result.getLastName());
         assertEquals("", "first", result.getFirstName());
         assertEquals("", "123", result.getPhone());
    }

}
