package org.oscm.identityservice.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.oscm.identityservice.model.AccessGroupModel;

public class AccessGroupTest {
    
    AccessGroup accessGroup;

    @Before
    public void setUp() {
        accessGroup = spy(new AccessGroup());
    }
    
    @Test
    public void testCreateUrl() {
        // given
        String expected = "http://oscm-identity:9090/oscm-identity/tenants/tenantid/groups";

        // when
        String result = accessGroup.createUrl("tenantid");

        // then
        assertEquals(expected, result);
    }
    
    @Test
    public void testCreateUrlToAddUser() {
        // given
        String expected = "http://oscm-identity:9090/oscm-identity/tenants/tenantid/groups/groupId/members";

        // when
        String result = accessGroup.createUrlToAddUser("tenantid", "groupId");

        // then
        assertEquals(expected, result);
    }

    @Test
    public void testGetAccesGroupModel() {
        //given
        AccessGroupModel expected = new AccessGroupModel();
        expected.setDescription("Used tenant is:");
        expected.setName("default");
        
        //when
        AccessGroupModel result = accessGroup.getAccessGroupModel("", "");
        
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
        String result = accessGroup.pojoToJsonString(accessGroupModel);
        
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
        AccessGroupModel result = accessGroup.createAccessGroupModelFromJson(testJsonString);
        
        //then
        assertEquals(expected, result);
    }
    
    
    
}
