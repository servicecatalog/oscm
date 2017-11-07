/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 18.10.2010                                                      
 *                                                                              
 *  Completion Time: 18.10.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Tenant;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Unit tests for the user data assembler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UserDataAssemblerTest {

    private VOUserDetails user;
    private PlatformUser domainUser;

    private final static String NAME_MORE_THAN_100_CHARS = "some name with more than 100 characters: ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd1";
    private final static String NAME_MORE_THAN_255_CHARS = NAME_MORE_THAN_100_CHARS
            + NAME_MORE_THAN_100_CHARS
            + "set of characters to exceed the length, piece of cake!";

    @Before
    public void setUp() throws Exception {
        user = new VOUserDetails();
        user.setAdditionalName("additionalName");
        user.setAddress("address");
        user.setEMail("mail@mailhost.de");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setLocale("de");
        user.setPhone("phone");
        user.setSalutation(Salutation.MR);
        user.setUserId("userId");
        user.setOrganizationId("organizationId");

        domainUser = new PlatformUser();
    }

    @Test
    public void testCopyAttributes() throws Exception {
        UserDataAssembler.copyAttributes(user, domainUser);
        validateAttributes();
    }

    @Test
    public void testToPlatformUserValid() throws Exception {
        domainUser = UserDataAssembler.toPlatformUser(user);
        validateAttributes();
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidAdditionalNameLength()
            throws Exception {
        user.setAdditionalName(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidAddressLength() throws Exception {
        user.setAddress(NAME_MORE_THAN_255_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidEMail() throws Exception {
        user.setEMail("bla");
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidEMailEmpty() throws Exception {
        user.setEMail("");
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidEMailNull() throws Exception {
        user.setEMail("");
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidEMailTooLong() throws Exception {
        user.setEMail("mail@host.de" + NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidFirstNameLength() throws Exception {
        user.setFirstName(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidLastNameLength() throws Exception {
        user.setLastName(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidLocaleLength() throws Exception {
        user.setLocale("fifteen chars ex");
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidLocaleNull() throws Exception {
        user.setLocale(null);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidLocaleEmpty() throws Exception {
        user.setLocale("");
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidPhoneLength() throws Exception {
        user.setPhone(NAME_MORE_THAN_255_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test(expected = ValidationException.class)
    public void testToPlatformUserInvalidUserIdLength() throws Exception {
        user.setUserId(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.toPlatformUser(user);
    }

    @Test
    public void testUpdatePlatformUserValid() throws Exception {
        UserDataAssembler.updatePlatformUser(user, domainUser);
        validateAttributes();
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidAdditionalNameLength()
            throws Exception {
        user.setAdditionalName(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidAddressLength() throws Exception {
        user.setAddress(NAME_MORE_THAN_255_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidEMail() throws Exception {
        user.setEMail("bla");
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidEMailEmpty() throws Exception {
        user.setEMail("");
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidEMailNull() throws Exception {
        user.setEMail("");
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidEMailTooLong() throws Exception {
        user.setEMail("mail@host.de" + NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidFirstNameLength() throws Exception {
        user.setFirstName(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidLastNameLength() throws Exception {
        user.setLastName(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidLocaleLength() throws Exception {
        user.setLocale("fifteen chars ex");
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidLocaleNull() throws Exception {
        user.setLocale(null);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidLocaleEmpty() throws Exception {
        user.setLocale("");
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidPhoneLength() throws Exception {
        user.setPhone(NAME_MORE_THAN_255_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ValidationException.class)
    public void testUpdatePlatformUserInvalidUserIdLength() throws Exception {
        user.setUserId(NAME_MORE_THAN_100_CHARS);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = SaaSSystemException.class)
    public void testUpdatePlatformUserWrongKeys() throws Exception {
        user.setKey(1);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpdatePlatformUserWrongVersion() throws Exception {
        user.setVersion(-1);
        UserDataAssembler.updatePlatformUser(user, domainUser);
    }

    @Test
    public void testToVOUser() throws Exception {
        PlatformUser platformUser = UserDataAssembler.toPlatformUser(user);
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        org.setTenant(new Tenant());
        platformUser.setOrganization(org);
        VOUser voUser = UserDataAssembler.toVOUser(platformUser);
        assertEquals(user.getUserId(), voUser.getUserId());
        assertEquals("orgId", voUser.getOrganizationId());
    }

    @Test
    public void testToVOUserNullInput() throws Exception {
        VOUser voUser = UserDataAssembler.toVOUser(null);
        assertNull(voUser);
    }

    @Test
    public void testOrganizationRolesSet() throws Exception {
        // test obvious get/set for to ensure bean compliance
        // though setter is never actually used
        VOUser voUser = new VOUser();
        voUser.setOrganizationRoles(new HashSet<OrganizationRoleType>());
        assertNotNull(voUser.getOrganizationRoles());
        assertTrue(voUser.getOrganizationRoles().isEmpty());
    }

    @Test
    public void testToVOUserDetails() throws Exception {
        PlatformUser platformUser = UserDataAssembler.toPlatformUser(user);
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        org.setTenant(new Tenant());
        platformUser.setOrganization(org);
        VOUserDetails voUser = UserDataAssembler.toVOUserDetails(platformUser);
        assertEquals("orgId", voUser.getOrganizationId());
        assertEquals(user.getAdditionalName(), voUser.getAdditionalName());
        assertEquals(user.getAddress(), voUser.getAddress());
        assertEquals(user.getEMail(), voUser.getEMail());
        assertEquals(user.getFirstName(), voUser.getFirstName());
        assertEquals(user.getLastName(), voUser.getLastName());
        assertEquals(user.getLocale(), voUser.getLocale());
        assertEquals(Boolean.valueOf(user.hasAdminRole()),
                Boolean.valueOf(voUser.hasAdminRole()));
        assertEquals(user.getPhone(), voUser.getPhone());
        assertEquals(user.getSalutation(), voUser.getSalutation());
        assertEquals(user.getUserId(), voUser.getUserId());
    }

    @Test
    public void testUpdateVOUserDetails() throws Exception {
        PlatformUser platformUser = UserDataAssembler.toPlatformUser(user);
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        org.setTenant(new Tenant());
        platformUser.setOrganization(org);
        VOUserDetails voUser = UserDataAssembler.toVOUserDetails(platformUser);
        assertEquals(user.getAdditionalName(), voUser.getAdditionalName());
        assertEquals(user.getAddress(), voUser.getAddress());
        assertEquals(user.getEMail(), voUser.getEMail());
        assertEquals(user.getFirstName(), voUser.getFirstName());
        assertEquals(user.getLastName(), voUser.getLastName());
        assertEquals(user.getLocale(), voUser.getLocale());
        assertEquals(Boolean.valueOf(user.hasAdminRole()),
                Boolean.valueOf(voUser.hasAdminRole()));
        assertEquals(user.getPhone(), voUser.getPhone());
        assertEquals(user.getSalutation(), voUser.getSalutation());
        assertEquals(user.getUserId(), voUser.getUserId());

        ArrayList<SettingType> settingList = new ArrayList<SettingType>();
        settingList.add(SettingType.LDAP_ATTR_FIRST_NAME);
        settingList.add(SettingType.LDAP_ATTR_LAST_NAME);
        settingList.add(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        settingList.add(SettingType.LDAP_ATTR_EMAIL);
        settingList.add(SettingType.LDAP_ATTR_LOCALE);
        settingList.add(SettingType.LDAP_ATTR_UID);

        String[] values = new String[settingList.size()];
        values[0] = "First Name";
        values[1] = "Last Name";
        values[2] = "Additional Name";
        values[3] = "name@test.com";
        values[4] = "en_US";
        values[5] = null;

        UserDataAssembler.updateVOUserDetails(values, settingList, voUser);
        assertEquals(values[0], voUser.getFirstName());
        assertEquals(values[1], voUser.getLastName());
        assertEquals(values[2], voUser.getAdditionalName());
        assertEquals(values[3], voUser.getEMail());
        assertEquals(values[4].substring(0, 2), voUser.getLocale());
        assertEquals(Boolean.valueOf(user.hasAdminRole()),
                Boolean.valueOf(voUser.hasAdminRole()));
        assertEquals(user.getPhone(), voUser.getPhone());
        assertEquals(user.getSalutation(), voUser.getSalutation());
        assertEquals(user.getUserId(), voUser.getUserId());
    }

    @Test
    public void testUpdatePlatformUser() throws Exception {
        PlatformUser platformUser = UserDataAssembler.toPlatformUser(user);
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        org.setTenant(new Tenant());
        platformUser.setOrganization(org);
        VOUserDetails voUser = UserDataAssembler.toVOUserDetails(platformUser);

        ArrayList<SettingType> settingList = new ArrayList<SettingType>();
        settingList.add(SettingType.LDAP_ATTR_FIRST_NAME);
        settingList.add(SettingType.LDAP_ATTR_LAST_NAME);
        settingList.add(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        settingList.add(SettingType.LDAP_ATTR_EMAIL);
        settingList.add(SettingType.LDAP_ATTR_LOCALE);
        settingList.add(SettingType.LDAP_ATTR_UID);

        String[] values = new String[settingList.size()];
        values[0] = "First Name";
        values[1] = "Last Name";
        values[2] = "Additional Name";
        values[3] = "name@test.com";
        values[4] = "";
        values[5] = null;

        UserDataAssembler.updateVOUserDetails(values, settingList, voUser);

        UserDataAssembler.updatePlatformUser(voUser, settingList, platformUser);
        assertEquals(values[0], platformUser.getFirstName());
        assertEquals(values[1], platformUser.getLastName());
        assertEquals(values[2], platformUser.getAdditionalName());
        assertEquals(values[3], platformUser.getEmail());
        assertEquals(user.getLocale(), platformUser.getLocale());
        assertEquals(Boolean.valueOf(user.hasAdminRole()),
                Boolean.valueOf(platformUser
                        .hasRole(UserRoleType.PLATFORM_OPERATOR)));
        assertEquals(user.getPhone(), platformUser.getPhone());
        assertEquals(user.getSalutation(), platformUser.getSalutation());
        assertEquals(user.getUserId(), platformUser.getUserId());
    }

    @Test
    public void testToVOUserDetailsNullInput() throws Exception {
        VOUser voUser = UserDataAssembler.toVOUserDetails(null);
        assertNull(voUser);
    }

    @Test
    public void testCopyPlatformUser() throws Exception {
        PlatformUser platformUser = UserDataAssembler.toPlatformUser(user);

        Organization org = new Organization();
        org.setOrganizationId("orgId");
        platformUser.setOrganization(org);

        PlatformUser platformUserCopy = UserDataAssembler
                .copyPlatformUser(platformUser);

        assertEquals(platformUser.getUserId(), platformUserCopy.getUserId());
        assertEquals(platformUser.getFirstName(),
                platformUserCopy.getFirstName());
        assertEquals(platformUser.getAdditionalName(),
                platformUserCopy.getAdditionalName());
        assertEquals(platformUser.getLastName(), platformUserCopy.getLastName());
        assertEquals(platformUser.getEmail(), platformUserCopy.getEmail());
        assertEquals(platformUser.getAddress(), platformUserCopy.getAddress());
        assertEquals(platformUser.getPhone(), platformUserCopy.getPhone());
        assertEquals(platformUser.getLocale(), platformUserCopy.getLocale());
        assertEquals(platformUser.getSalutation(),
                platformUserCopy.getSalutation());
    }

    private void validateAttributes() {
        assertEquals(user.getAdditionalName(), domainUser.getAdditionalName());
        assertEquals(user.getAddress(), domainUser.getAddress());
        assertEquals(user.getEMail(), domainUser.getEmail());
        assertEquals(user.getFirstName(), domainUser.getFirstName());
        assertEquals(user.getLastName(), domainUser.getLastName());
        assertEquals(user.getLocale(), domainUser.getLocale());
        assertEquals(Boolean.valueOf(user.hasAdminRole()),
                Boolean.valueOf(domainUser.isOrganizationAdmin()));
        assertEquals(user.getPhone(), domainUser.getPhone());
        assertEquals(user.getSalutation(), domainUser.getSalutation());
        assertEquals(user.getUserId(), domainUser.getUserId());
    }
}
