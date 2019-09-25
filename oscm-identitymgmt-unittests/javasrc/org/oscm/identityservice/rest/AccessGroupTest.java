package org.oscm.identityservice.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.oscm.identityservice.model.AccessGroupModel;

public class AccessGroupTest {
    
    AccessGroup accessGroup;

    @Before
    public void setUp() {
       
    }
    
    @Test
    public void testCreateUrl() {
        // given
        String expected = "http://oscm-identity:9090/oscm-identity/tenants/tenantid/groups";

        // when
        String result = AccessGroup.createUrlForGroups("tenantid");

        // then
        assertEquals(expected, result);
    }
    
    @Test
    public void testCreateUrlToAddUser() {
        // given
        String expected = "http://oscm-identity:9090/oscm-identity/tenants/tenantid/groups/groupId/members";

        // when
        String result = AccessGroup.createUrlToAddUser("tenantid", "groupId");

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testGetAccesGroupModel() {
        //given
        AccessGroupModel expected = new AccessGroupModel();
        expected.setDescription("TenantId: . Organization:");
        expected.setName("OSCM_default");
        
        //when
        AccessGroupModel result = AccessGroup.getAccessGroupModel("", "", "");
        
        //then
        assertEquals(expected, result);
    }
    
    @Test
    public void testPojoToJsonString() {
        //given
        String expected = "{\"id\":\"test\",\"name\":\"test\",\"description\":\"test\"}";
        AccessGroupModel accessGroupModel = new AccessGroupModel();
        accessGroupModel.setId("test");
        accessGroupModel.setName("test");
        accessGroupModel.setDescription("test");
       
        //when
        String result = AccessGroup.pojoToJsonString(accessGroupModel);
        
        //then
        assertEquals(expected, result);
    }
    
    @Test
    public void testCreateAccessGroupModelFromJson() {
        
        String testJsonString = "{\"id\":\"test\",\"name\":\"test\",\"description\":\"test\"}";
        AccessGroupModel expected = new AccessGroupModel();
        expected.setId("test");
        expected.setName("test");
        expected.setDescription("test");
       
        //when
        AccessGroupModel result = AccessGroup.createAccessGroupModelFromJson(testJsonString);
        
        //then
        assertEquals(expected, result);
    }
    
    
    
}
