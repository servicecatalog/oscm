package org.oscm.identityservice.bean;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identity.ApiIdentityClient;
import org.oscm.identity.IdentityClient;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.GroupInfo;
import org.oscm.internal.types.exception.RegistrationException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class IdentityServiceBeanOidcTest {

    public IdentityServiceBean bean;
    public ApiIdentityClient client;

    @Before
    public void setup() {
        this.bean = spy(new IdentityServiceBean());
        this.bean.dm = mockDataService();
        this.client = mock(ApiIdentityClient.class);
        when(this.bean.getIdentityClient(anyString())).thenReturn(this.client);
    }

    @Test
    public void createAccessGroupInOIDCProviderSuccess() throws IdentityClientException, RegistrationException {
        final Set<GroupInfo> groupInfoSet = new HashSet<>();
        final GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId("id");

        when(this.client.getGroups()).thenReturn(groupInfoSet);
        when(this.client.createGroup(anyString(), anyString())).thenReturn(groupInfo);

        final String result = bean.createAccessGroupInOIDCProvider("", "Test");

        assertNotNull(result);
        assertEquals("id", result);
    }


    @Test(expected = RegistrationException.class)
    public void createAccessGroupInOIDCProviderGroupAlreadyExist() throws IdentityClientException, RegistrationException {
        final Set<GroupInfo> groupInfoSet = new HashSet<>();
        final GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("Test");

        when(this.client.getGroups()).thenReturn(groupInfoSet);

        try {
            bean.createAccessGroupInOIDCProvider("", "Test");
        } finally {
            verify(client, never()).createGroup(anyString(), anyString());
        }
    }

    @Test(expected = RegistrationException.class)
    public void createAccessGroupInOIDCProviderRegistrationIdentityException() throws IdentityClientException, RegistrationException {
        final IdentityClientException exception = mock(IdentityClientException.class);
        final Set<GroupInfo> groupInfoSet = new HashSet<>();
        final GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("Test");

        when(exception.getReason()).thenReturn(IdentityClientException.Reason.OIDC_ERROR);
        when(client.getGroups()).thenReturn(groupInfoSet);
        when(client.createGroup(anyString(), anyString())).thenThrow(exception);

        try {
            bean.createAccessGroupInOIDCProvider("", "Test");
        } finally {
            verify(client, times(1)).createGroup(anyString(), anyString());
        }
    }

    private static DataService mockDataService() {
        final DataService dataService = mock(DataService.class);
        final PlatformUser platformUser = mock(PlatformUser.class);
        final Organization organization = mock(Organization.class);
        when(dataService.getCurrentUser()).thenReturn(platformUser);
        when(platformUser.getOrganization()).thenReturn(organization);
        when(organization.getName()).thenReturn("Org");
        return dataService;
    }

}
