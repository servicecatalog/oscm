/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.subscriptionservice.local.SubscriptionWithRoles;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author weiser
 * 
 */
public class DataConverterTest {

    private static final String ROLENAME = "rolename";

    private PlatformUser user;
    private DataConverter dc;
    private LocalizerFacade lf;
    private Set<SettingType> ma;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        user = new PlatformUser();
        user.setEmail("email@provider.de");
        user.setFirstName("firstName");
        user.setKey(1234);
        user.setLastName("lastName");
        user.setLocale("en");
        user.setSalutation(Salutation.MR);
        user.setUserId("userId");
        user.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        user.setOrganization(new Organization());

        RoleAssignment ra = new RoleAssignment();
        ra.setUser(user);
        ra.setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
        user.setAssignedRoles(Collections.singleton(ra));

        dc = new DataConverter();

        lf = mock(LocalizerFacade.class);

        when(lf.getText(anyLong(), eq(LocalizedObjectTypes.ROLE_DEF_NAME)))
                .thenReturn(ROLENAME);

        ma = mock(Set.class);
    }

    @Test
    public void toPOUser_Null() {
        assertNull(dc.toPOUser(null));
    }

    @Test
    public void toPOUser() {
        POUser poUser = dc.toPOUser(user);

        assertNotNull(poUser);
        validatePOUser(user, poUser);
    }

    @Test
    public void toPOUserDetails_Null() {
        assertNull(dc.toPOUserDetails(null, null));
    }

    @Test
    public void toPOUserDetails() {
        Set<UserRoleType> availableRoles = EnumSet.of(
                UserRoleType.ORGANIZATION_ADMIN, UserRoleType.SERVICE_MANAGER);

        POUserDetails poUser = dc.toPOUserDetails(user, availableRoles);

        assertNotNull(poUser);
        validatePOUserDetails(user, poUser);
        assertNotNull(poUser.getAvailableRoles());
        assertSame(availableRoles, poUser.getAvailableRoles());
    }

    @Test
    public void toPOUserDetails_AvailableNull() {
        POUserDetails poUser = dc.toPOUserDetails(user, null);

        assertNotNull(poUser);
        validatePOUserDetails(user, poUser);
        assertEquals(EnumSet.noneOf(UserRoleType.class),
                poUser.getAvailableRoles());
    }

    @Test
    public void toPlatformUser() throws Exception {
        POUserDetails ud = dc.toPOUserDetails(user,
                EnumSet.noneOf(UserRoleType.class));

        PlatformUser pu = dc.toPlatformUser(ud);

        validate(ud, pu);
    }

    @Test
    public void toPlatformUser_Null() throws Exception {
        PlatformUser pu = dc.toPlatformUser((POUserDetails) null);

        assertNull(pu);
    }

    @Test
    public void toPlatformUser_FromPOUserInUnit() throws Exception {
        POUserInUnit ud = dc.toPoUserInUnit(user, UnitRoleType.USER.name());

        PlatformUser pu = dc.toPlatformUser(ud);

        validate(ud, pu);
    }

    @Test
    public void updatePlatformUser() throws Exception {
        POUserInUnit ud = dc.toPoUserInUnit(user, UnitRoleType.USER.name());

        PlatformUser pu = dc.updatePlatformUser(ud, user);

        validate(ud, pu);
    }

    @Test
    public void toPOUserInUnit() throws Exception {
        POUserInUnit ud = dc.toPoUserInUnit(user, UnitRoleType.USER.name());

        validate(ud, user);
    }

    @Test
    public void toPOUserAndSubscriptionsNew() {
        List<SubscriptionWithRoles> list = prepareSubscriptions();

        POUserAndSubscriptions uas = dc.toPOUserAndSubscriptionsNew(list,
                EnumSet.noneOf(UserRoleType.class), lf);

        verify(uas);
    }

    @Test
    public void toPOUserAndSubscriptionsNew_ListNull() {
        Set<UserRoleType> set = EnumSet.noneOf(UserRoleType.class);
        POUserAndSubscriptions u = dc
                .toPOUserAndSubscriptionsNew(null, set, lf);

        assertNotNull(u);
        assertSame(set, u.getAvailableRoles());
        assertNull(u.getEmail());
        assertNull(u.getFirstName());
        assertNull(u.getFirstName());
        assertNull(u.getLastName());
        assertNull(u.getLocale());
        assertNull(u.getSalutation());
        assertNull(u.getStatus());
        assertNull(u.getUserId());
        assertTrue(u.getSubscriptions().isEmpty());
    }

    @Test
    public void toPOUserAndSubscriptionsExisting() {
        List<SubscriptionWithRoles> list = prepareSubscriptions();
        Set<UserRoleType> set = EnumSet.noneOf(UserRoleType.class);

        POUserAndSubscriptions result = dc.toPOUserAndSubscriptionsExisting(
                user, list, set, new ArrayList<UsageLicense>(), lf, ma);

        assertNotNull(result);
        validatePOUserDetails(user, result);
        assertNotNull(result.getAvailableRoles());
        assertSame(set, result.getAvailableRoles());
        assertNotNull(result.getMappedAttributes());
    }

    @Test
    public void matchAssigned() {
        List<UsageLicense> assignedSubs = prepareAssignments();
        List<POSubscription> subs = preparePOSubscriptions();

        List<POSubscription> result = dc.matchAssigned(assignedSubs, subs, lf);

        assertEquals(2, result.size());

        POSubscription sub = result.get(0);
        assertTrue(sub.isAssigned());
        assertNull(sub.getUsageLicense().getPoServieRole());

        sub = result.get(1);
        assertTrue(sub.isAssigned());
        assertNotNull(sub.getUsageLicense().getPoServieRole());
        assertEquals("roleA", sub.getUsageLicense().getPoServieRole().getId());

    }

    @Test
    public void matchAssigned_NoAssignment() {
        List<POSubscription> subs = preparePOSubscriptions();

        List<POSubscription> result = dc.matchAssigned(
                new ArrayList<UsageLicense>(), subs, lf);

        assertEquals(2, result.size());
        for (POSubscription sub : result) {
            assertFalse(sub.isAssigned());
            assertNull(sub.getUsageLicense().getPoServieRole());
        }
    }

    @Test
    public void toPOServiceRole() {
        RoleDefinition rd = new RoleDefinition();
        rd.setKey(1234);
        rd.setRoleId("roleid");

        POServiceRole role = dc.toPOServiceRole(lf, rd);

        assertNotNull(role);
        assertEquals(rd.getRoleId(), role.getId());
        assertEquals(rd.getKey(), role.getKey());
        assertEquals(rd.getVersion(), role.getVersion());
        assertEquals(ROLENAME, role.getName());
    }

    @Test
    public void toPOServiceRole_Null() {
        POServiceRole role = dc.toPOServiceRole(lf, null);

        assertNull(role);
    }

    @Test
    public void isUserInformationUpdated_True() {
        // given
        POUserDetails poUser = new POUserDetails();
        poUser.setVersion(-1);
        // when
        boolean result = dc.isUserInformationUpdated(poUser, user);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isUserInformationUpdated_False() {
        // given
        POUserDetails poUser = new POUserDetails();
        poUser.setVersion(1);
        // when
        boolean result = dc.isUserInformationUpdated(poUser, user);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isUserRoleUpdated_False() {
        // given
        Set<UserRoleType> roles = new HashSet<UserRoleType>();
        roles.add(UserRoleType.ORGANIZATION_ADMIN);
        // when
        boolean result = dc.isUserRoleUpdated(roles, user);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isUserRoleUpdated_True() {
        // given
        Set<UserRoleType> roles = new HashSet<UserRoleType>();
        roles.add(UserRoleType.BROKER_MANAGER);
        // when
        boolean result = dc.isUserRoleUpdated(roles, user);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    private static void validatePOUserDetails(PlatformUser user,
            POUserDetails poUser) {
        validatePOUser(user, poUser);
        assertEquals(user.getSalutation(), poUser.getSalutation());
        assertEquals(user.getLocale(), poUser.getLocale());
        assertSame(user.getStatus(), poUser.getStatus());
        assertNotNull(poUser.getAssignedRoles());
        assertEquals(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN),
                poUser.getAssignedRoles());
    }

    private static void validatePOUser(PlatformUser user, POUser poUser) {
        assertEquals(user.getEmail(), poUser.getEmail());
        assertEquals(user.getFirstName(), poUser.getFirstName());
        assertEquals(user.getKey(), poUser.getKey());
        assertEquals(user.getLastName(), poUser.getLastName());
        assertEquals(user.getUserId(), poUser.getUserId());
        assertEquals(user.getVersion(), poUser.getVersion());
    }

    private static void validate(POUserDetails exp, PlatformUser act) {
        assertEquals(exp.getEmail(), act.getEmail());
        assertEquals(exp.getFirstName(), act.getFirstName());
        assertEquals(exp.getLastName(), act.getLastName());
        assertEquals(exp.getUserId(), act.getUserId());
        assertEquals(exp.getLocale(), act.getLocale());
        assertEquals(exp.getSalutation(), act.getSalutation());
        assertNull(act.getStatus());
    }

    private static void validate(POUserInUnit exp, PlatformUser act) {
        assertEquals(exp.getPoUser().getEmail(), act.getEmail());
        assertEquals(exp.getFirstName(), act.getFirstName());
        assertEquals(exp.getLastName(), act.getLastName());
        assertEquals(exp.getUserId(), act.getUserId());
        assertEquals(exp.getLocale(), act.getLocale());
        assertEquals(exp.getSalutation(), act.getSalutation());
    }

    private static List<SubscriptionWithRoles> prepareSubscriptions() {
        List<SubscriptionWithRoles> result = new ArrayList<SubscriptionWithRoles>();

        SubscriptionWithRoles swr = new SubscriptionWithRoles();
        Subscription sub = new Subscription();
        sub.setKey(1);
        sub.setSubscriptionId("sub1");
        swr.setSubscription(sub);
        result.add(swr);

        swr = new SubscriptionWithRoles();
        sub = new Subscription();
        sub.setKey(2);
        sub.setSubscriptionId("sub2");
        swr.setSubscription(sub);

        RoleDefinition rd = new RoleDefinition();
        rd.setKey(10);
        rd.setRoleId("roleA");
        swr.getRoles().add(rd);
        rd = new RoleDefinition();
        rd.setKey(11);
        rd.setRoleId("roleB");
        swr.getRoles().add(rd);
        result.add(swr);

        return result;
    }

    private static void verify(POUserAndSubscriptions uas) {
        assertEquals(EnumSet.noneOf(UserRoleType.class),
                uas.getAvailableRoles());
        assertEquals(2, uas.getSubscriptions().size());

        POSubscription sub = uas.getSubscriptions().get(0);
        assertEquals("sub1", sub.getId());
        assertEquals(1, sub.getKey());
        assertTrue(sub.getRoles().isEmpty());

        sub = uas.getSubscriptions().get(1);
        assertEquals("sub2", sub.getId());
        assertEquals(2, sub.getKey());
        assertEquals(2, sub.getRoles().size());

        POServiceRole sr = sub.getRoles().get(0);
        assertEquals("roleA", sr.getId());
        assertEquals(ROLENAME, sr.getName());
        assertEquals(10, sr.getKey());

        sr = sub.getRoles().get(1);
        assertEquals("roleB", sr.getId());
        assertEquals(ROLENAME, sr.getName());
        assertEquals(11, sr.getKey());
    }

    private static List<POSubscription> preparePOSubscriptions() {
        List<POSubscription> result = new ArrayList<POSubscription>();

        POSubscription sub = new POSubscription();
        sub.setKey(1);
        sub.setId("sub1");
        result.add(sub);

        sub = new POSubscription();
        sub.setKey(2);
        sub.setId("sub2");

        POServiceRole rd = new POServiceRole();
        rd.setKey(10);
        rd.setId("roleA");
        sub.getRoles().add(rd);
        rd = new POServiceRole();
        rd.setKey(11);
        rd.setId("roleB");
        sub.getRoles().add(rd);
        result.add(sub);

        return result;
    }

    private static List<UsageLicense> prepareAssignments() {
        List<UsageLicense> result = new ArrayList<UsageLicense>();

        UsageLicense lic = new UsageLicense();
        Subscription sub = new Subscription();
        sub.setKey(1);
        sub.setSubscriptionId("sub1");
        lic.setSubscription(sub);
        result.add(lic);

        lic = new UsageLicense();
        sub = new Subscription();
        sub.setKey(2);
        sub.setSubscriptionId("sub2");
        lic.setSubscription(sub);

        RoleDefinition rd = new RoleDefinition();
        rd.setKey(10);
        rd.setRoleId("roleA");
        lic.setRoleDefinition(rd);
        result.add(lic);

        return result;
    }

}
