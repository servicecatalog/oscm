/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.UnsupportedOperationException;

public class UserModificationCheckTest {

    private Set<SettingType> mappedLdapSettings;
    private UserModificationCheck umc;
    private PlatformUser user1;
    private PlatformUser user2;

    @Before
    public void setup() throws Exception {
        mappedLdapSettings = new HashSet<SettingType>();
        umc = new UserModificationCheck(mappedLdapSettings);
        user1 = new PlatformUser();
        setupUser(user1);
        user2 = new PlatformUser();
        setupUser(user2);
    }

    @Test
    public void check_NullObjects() throws Exception {
        umc.check(null, null);
    }

    @Test
    public void check_SameObjects() throws Exception {
        umc.check(user1, user1);
    }

    @Test
    public void check_EqualObjects() throws Exception {
        umc.check(user1, user2);
    }

    @Test
    public void check_EqualAttribsOtherKey() throws Exception {
        user2.setKey(5);
        umc.check(user1, user2);
    }

    @Test
    public void check_ModifiedAdditionalName_NonConflicting() throws Exception {
        user2.setAdditionalName("another additional name");
        umc.check(user1, user2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void check_ModifiedAdditionalName_Conflicting() throws Exception {
        user2.setAdditionalName("another additional name");
        mappedLdapSettings.add(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        umc.check(user1, user2);
    }

    @Test
    public void check_UnmodifiedAdditionalName_Conflicting() throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        umc.check(user1, user2);
    }

    @Test
    public void check_ModifiedFirstName_NonConflicting() throws Exception {
        user2.setFirstName("another first name");
        umc.check(user1, user2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void check_ModifiedFirstName_Conflicting() throws Exception {
        user2.setFirstName("another first name");
        mappedLdapSettings.add(SettingType.LDAP_ATTR_FIRST_NAME);
        umc.check(user1, user2);
    }

    @Test
    public void check_UnmodifiedFirstName_Conflicting() throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_FIRST_NAME);
        umc.check(user1, user2);
    }

    @Test
    public void check_ModifiedLastName_NonConflicting() throws Exception {
        user2.setLastName("another last name");
        umc.check(user1, user2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void check_ModifiedLastName_Conflicting() throws Exception {
        user2.setLastName("another last name");
        mappedLdapSettings.add(SettingType.LDAP_ATTR_LAST_NAME);
        umc.check(user1, user2);
    }

    @Test
    public void check_UnmodifiedLastName_Conflicting() throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_LAST_NAME);
        umc.check(user1, user2);
    }

    @Test
    public void check_ModifiedLocale_NonConflicting() throws Exception {
        user2.setLocale("en");
        umc.check(user1, user2);
    }

    @Test
    public void check_UnmodifiedLocale_Conflicting() throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_LOCALE);
        umc.check(user1, user2);
    }

    @Test
    public void check_ModifiedEmail_NonConflicting() throws Exception {
        user2.setEmail("user2@test.com");
        umc.check(user1, user2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void check_ModifiedEmail_Conflicting() throws Exception {
        user2.setEmail("user2@test.com");
        mappedLdapSettings.add(SettingType.LDAP_ATTR_EMAIL);
        umc.check(user1, user2);
    }

    @Test
    public void check_UnmodifiedEmail_Conflicting() throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_EMAIL);
        umc.check(user1, user2);
    }

    @Test
    public void check_ModifiedUid_NonConflicting() throws Exception {
        user2.setUserId("user2");
        umc.check(user1, user2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void check_ModifiedUid_Conflicting() throws Exception {
        user2.setUserId("user2");
        mappedLdapSettings.add(SettingType.LDAP_ATTR_UID);
        umc.check(user1, user2);
    }

    @Test
    public void check_UnmodifiedUid_Conflicting() throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_UID);
        umc.check(user1, user2);
    }

    private void setupUser(PlatformUser userToSetup) {
        userToSetup.setKey(99);
        userToSetup.setAdditionalName("additional name");
        userToSetup.setEmail("user1@test.com");
        userToSetup.setFirstName("first name");
        userToSetup.setLastName("last name");
        userToSetup.setLocale("de");
        userToSetup.setUserId("user1");
        userToSetup.setAddress("user1 address");
    }

}
