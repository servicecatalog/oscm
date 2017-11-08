/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class ProfileServiceBeanIT extends EJBTestBase {

    private ProfileService ps;
    private IdentityService idServ;
    private AccountServiceLocal accServ;
    private VOUserDetails userDetails;
    private VOOrganization organization;

    @Captor
    ArgumentCaptor<VOOrganization> capturedOrg;
    @Captor
    ArgumentCaptor<VOUserDetails> capturedUser;
    @Captor
    ArgumentCaptor<String> capturedMid;
    @Captor
    ArgumentCaptor<VOImageResource> capturedImageResource;

    @Override
    protected void setup(TestContainer container) throws Exception {
        MockitoAnnotations.initMocks(this);
        idServ = mock(IdentityService.class);
        accServ = mock(AccountServiceLocal.class);
        container.addBean(idServ);
        container.addBean(accServ);
        container.addBean(new ProfileServiceBean());

        userDetails = new VOUserDetails();
        userDetails.setKey(1L);
        userDetails.setVersion(23);
        userDetails.setFirstName("firstName");
        userDetails.setLastName("lastName");
        userDetails.setEMail("mail@mailhost.de");
        userDetails.setLocale("de");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setUserRoles(new HashSet<UserRoleType>(Collections
                .singleton(UserRoleType.SERVICE_MANAGER)));
        userDetails.setOrganizationRoles(Collections
                .singleton(OrganizationRoleType.SUPPLIER));

        organization = new VOOrganization();
        organization.setKey(84L);
        organization.setVersion(32);
        organization.setOrganizationId("orgId");
        organization.setEmail("mail");
        organization.setSupportEmail("supMail");
        organization.setPhone("phone");
        organization.setUrl("url");
        organization.setAddress("address");
        organization.setDomicileCountry("DE");
        organization.setDescription("description text goes here");
        organization.setName("name");
        organization.setImageDefined(false);

        doAnswer(new Answer<VOUserDetails>() {
            public VOUserDetails answer(InvocationOnMock invocation)
                    throws Throwable {
                return userDetails;
            }
        }).when(idServ).getCurrentUserDetails();
        doAnswer(new Answer<VOOrganization>() {
            public VOOrganization answer(InvocationOnMock invocation)
                    throws Throwable {
                return organization;
            }
        }).when(accServ).getOrganizationDataFallback();
        doNothing().when(accServ).updateAccountInformation(
                capturedOrg.capture(), capturedUser.capture(),
                capturedMid.capture(), capturedImageResource.capture());

        ps = container.get(ProfileService.class);
    }

    @Test
    public void getProfile_ValidateDelegationNoAdmin() throws Exception {
        // when
        ps.getProfile();
        // then
        verify(idServ, times(1)).getCurrentUserDetails();
        verify(accServ, times(0)).getOrganizationDataFallback();
    }

    @Test
    public void getProfile_ValidateDelegationAdmin() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(idServ).isCallerOrganizationAdmin();
        organization = new VOOrganization();
        // when
        ps.getProfile();
        // then
        verify(idServ, times(1)).getCurrentUserDetails();
        verify(accServ, times(1)).getOrganizationDataFallback();
    }

    @Test
    public void getProfile_ValidateMappingNoAdmin() throws Exception {
        // when
        POProfile profile = ps.getProfile();
        // then
        assertNotNull(profile);
        validateUserData(profile.getUser());
        assertNull(profile.getOrganization());
    }

    @Test
    public void getProfile_ValidateMappingAdmin() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(idServ).isCallerOrganizationAdmin();
        // when
        POProfile profile = ps.getProfile();
        // then
        assertNotNull(profile);
        validateUserData(profile.getUser());
        validateOrganizationData(profile.getOrganization());
    }

    @Test
    public void getProfile_NoImageDefined() throws Exception {
        doReturn(Boolean.TRUE).when(idServ).isCallerOrganizationAdmin();
        POProfile profile = ps.getProfile();
        assertNotNull(profile);
        assertFalse(profile.getOrganization().isImageDefined());
    }

    @Test
    public void getProfile_ImageDefined() throws Exception {
        doReturn(Boolean.TRUE).when(idServ).isCallerOrganizationAdmin();
        organization.setImageDefined(true);

        POProfile profile = ps.getProfile();
        assertNotNull(profile);
        assertTrue(profile.getOrganization().isImageDefined());
    }

    @Test
    public void save_validateDelegation() throws Exception {
        // re-use get profile method to benefit from object creation
        POProfile profile = ps.getProfile();
        ps.saveProfile(profile, null);
        verify(accServ, times(1)).updateAccountInformation(
                any(VOOrganization.class), any(VOUserDetails.class),
                anyString(), any(VOImageResource.class));
    }

    @Test
    public void save_validateUserData() throws Exception {
        // re-use get profile method to benefit from object creation
        POProfile profile = ps.getProfile();
        ps.saveProfile(profile, "mId");
        VOUserDetails value = capturedUser.getValue();
        validateUserData(value);
        assertEquals("mId", capturedMid.getValue());
    }

    @Test
    public void save_validateOrganizationData() throws Exception {
        // re-use get profile method to benefit from object creation
        doReturn(Boolean.TRUE).when(idServ).isCallerOrganizationAdmin();
        POProfile profile = ps.getProfile();
        ps.saveProfile(profile, "mId2");
        VOOrganization value = capturedOrg.getValue();
        validateOrganizationData(value);
        assertEquals("mId2", capturedMid.getValue());
    }

    @Test
    public void save_validateImageNoneDefined() throws Exception {
        // re-use get profile method to benefit from object creation
        userDetails.setUserRoles(Collections
                .singleton(UserRoleType.ORGANIZATION_ADMIN));
        POProfile profile = ps.getProfile();
        ps.saveProfile(profile, "mId2");
        VOImageResource value = capturedImageResource.getValue();
        assertNull(value);
    }

    @Test
    public void save_validateImageDefined() throws Exception {
        // re-use get profile method to benefit from object creation
        userDetails.setUserRoles(Collections
                .singleton(UserRoleType.ORGANIZATION_ADMIN));
        POProfile profile = ps.getProfile();
        // the profile service does not set the image details, this is only done
        // by the controller to save the image. for tests, set the data manually
        profile.setImage(new POImageResource(new VOImageResource()));
        ps.saveProfile(profile, "mId2");
        VOImageResource value = capturedImageResource.getValue();
        assertNotNull(value);
    }

    private void validateOrganizationData(VOOrganization value) {
        assertEquals(organization.getKey(), value.getKey());
        assertEquals(organization.getVersion(), value.getVersion());
        assertEquals(organization.getOrganizationId(),
                value.getOrganizationId());
        assertEquals(organization.getEmail(), value.getEmail());
        assertEquals(organization.getSupportEmail(), value.getSupportEmail());
        assertEquals(organization.getPhone(), value.getPhone());
        assertEquals(organization.getUrl(), value.getUrl());
        assertEquals(organization.getAddress(), value.getAddress());
        assertEquals(organization.getDomicileCountry(),
                value.getDomicileCountry());
        assertEquals(organization.getDescription(), value.getDescription());
        assertEquals(organization.getName(), value.getName());

        // despite the settings taken from the PO, the updated value object must
        // be same as the one retrieved from the account service stub
        assertSame(organization, value);
    }

    private void validateOrganizationData(POOrganization org) {
        assertNotNull(org);
        assertEquals(organization.getKey(), org.getKey());
        assertEquals(organization.getVersion(), org.getVersion());
        assertEquals(organization.getOrganizationId(), org.getIdentifier());
        assertEquals(organization.getEmail(), org.getMail());
        assertEquals(organization.getSupportEmail(), org.getSupportEmail());
        assertEquals(organization.getPhone(), org.getPhone());
        assertEquals(organization.getUrl(), org.getWebsiteUrl());
        assertEquals(organization.getAddress(), org.getAddress());
        assertEquals(organization.getDomicileCountry(), org.getCountryISOCode());
        assertEquals(organization.getDescription(), org.getDescription());
        assertEquals(organization.getName(), org.getName());
        assertTrue(org.getOrganizationRoles().contains(
                OrganizationRoleType.SUPPLIER));
    }

    private void validateUserData(VOUserDetails value) {
        assertEquals(userDetails.getKey(), value.getKey());
        assertEquals(userDetails.getVersion(), value.getVersion());
        assertEquals(userDetails.getFirstName(), value.getFirstName());
        assertEquals(userDetails.getLastName(), value.getLastName());
        assertEquals(userDetails.getSalutation(), value.getSalutation());
        assertEquals(userDetails.getEMail(), value.getEMail());
        assertEquals(userDetails.getLocale(), value.getLocale());

        // despite the settings taken from the PO, the updated value object must
        // be same as the one retrieved from the identity service stub
        assertTrue(userDetails == value);
    }

    private void validateUserData(POUser user) {
        assertNotNull(user);
        assertEquals(userDetails.getKey(), user.getKey());
        assertEquals(userDetails.getVersion(), user.getVersion());
        assertEquals(userDetails.getFirstName(), user.getFirstName());
        assertEquals(userDetails.getLastName(), user.getLastName());
        assertEquals(userDetails.getEMail(), user.getMail());
        assertEquals(userDetails.getSalutation(), user.getTitle());
        assertEquals(userDetails.getLocale(), user.getLocale());
        assertTrue(user.getUserRoles().contains(UserRoleType.SERVICE_MANAGER));
    }

}
