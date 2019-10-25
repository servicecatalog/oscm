/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 24.10.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identity.model.UserInfo;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author worf
 *
 */
public class IdentityServiceBeanOidcTest {
    
    private IdentityServiceBean bean;
    private ApiIdentityClient client;
    
    @Before
    public void setup() throws Exception {
        bean = spy(new IdentityServiceBean());
        client = Mockito.mock(ApiIdentityClient.class);
        when(bean.getApiIdeintyClient(anyString())).thenReturn(client);
    }

    @Test
    public void testLoadUserDetailsFromOIDCProvider() throws RegistrationException, IdentityClientException {
        //given
        String userId = "test";
        String tenantId = "default";
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        
        //when
        when(client.getUser(anyString())).thenReturn(userInfo);
        VOUserDetails result = bean.loadUserDetailsFromOIDCProvider(userId, tenantId);

        //then
        assertEquals(userId, result.getUserId());
    }
    
    @Test (expected = RegistrationException.class)
    public void testLoadUserDetailsFromOIDCProviderException() throws RegistrationException, IdentityClientException {
        //given
        IdentityClientException expected = new IdentityClientException("testError");

        //when
        when(client.getUser(anyString())).thenThrow(expected);
        VOUserDetails result = bean.loadUserDetailsFromOIDCProvider("", "");

        //then exception
    }
    
    @Test
    public void testCreateAccessGroupInOIDCProvider() throws RegistrationException, IdentityClientException {
        //given
        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        GroupInfo groupInfo = new GroupInfo();
        
        groupInfo.setName("test");
        groupInfo.setDescription("test");
        groupInfo.setId("1");
        org.setName("testOrg");
        user.setOrganization(org);

        DataService ds = mock(DataService.class);
        bean.dm = ds;
        
        String userId = "test";
        String tenantId = "default";
        String expected = "1";
        
        //when
        when(ds.getCurrentUser()).thenReturn(user);
        when(client.createGroup(anyString(), anyString())).thenReturn(groupInfo);
        String result = bean.createAccessGroupInOIDCProvider(userId, tenantId);

        //then
        assertEquals(expected, result);
    }
    
    @Test (expected = RegistrationException.class)
    public void testCreateAccessGroupInOIDCProviderException() throws RegistrationException, IdentityClientException {
        //given
        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        IdentityClientException expected = new IdentityClientException("testError");
        DataService ds = mock(DataService.class);
        bean.dm = ds;
        
        org.setName("testOrg");
        user.setOrganization(org);

        //when
        when(ds.getCurrentUser()).thenReturn(user);
        when(client.createGroup(anyString(), anyString())).thenThrow(expected);
        String result = bean.createAccessGroupInOIDCProvider("", "");

        //then exception
    }


}
