/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usergroupmgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.assembler.POUserGroupAssembler;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usermanagement.POUser;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

/**
 * Unit Test for UserGroupServiceBean.
 * 
 * @author qiu
 * 
 */
public class UserGroupServiceBeanTest {

    private UserGroupServiceBean userGroupService;
    private UserGroupServiceLocalBean userGroupServiceLocal;
    private final String MARKETPLACEID = "marketplaceId";

    @Before
    public void setup() {
        userGroupService = spy(new UserGroupServiceBean());
        userGroupServiceLocal = mock(UserGroupServiceLocalBean.class);
        userGroupService.userGroupService = userGroupServiceLocal;
    }

    @Test
    public void createGroup() throws Exception {
        // when
        userGroupService.createGroup(preparePOUserGroup(123L), MARKETPLACEID);
        // then
        verify(userGroupServiceLocal, times(1)).createUserGroup(
                any(UserGroup.class), anyListOf(Product.class),
                anyListOf(Product.class), anyString());

    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createGroup_NonUniqueBusinessKeyException() throws Exception {
        // given
        doThrow(new NonUniqueBusinessKeyException())
                .when(userGroupServiceLocal).createUserGroup(
                        any(UserGroup.class), anyListOf(Product.class),
                        anyListOf(Product.class), anyString());
        // when
        userGroupService.createGroup(preparePOUserGroup(123L), MARKETPLACEID);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void createGroup_OperationNotPermittedException() throws Exception {
        // given
        doThrow(new OperationNotPermittedException()).when(
                userGroupServiceLocal)
                .createUserGroup(any(UserGroup.class),
                        anyListOf(Product.class), anyListOf(Product.class),
                        anyString());
        // when
        userGroupService.createGroup(preparePOUserGroup(123L), MARKETPLACEID);
    }

    @Test(expected = ValidationException.class)
    public void createGroup_ValidationException() throws Exception {
        userGroupService.createGroup(new POUserGroup(), MARKETPLACEID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void createGroup_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(userGroupServiceLocal)
                .createUserGroup(any(UserGroup.class),
                        anyListOf(Product.class), anyListOf(Product.class),
                        anyString());
        // when
        userGroupService.createGroup(preparePOUserGroup(123L), MARKETPLACEID);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateGroup() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        
        // when
        userGroupService.updateGroup(preparePOUserGroup(123L), MARKETPLACEID,
                preparePOUserInUnitList(1), preparePOUserInUnitList(2), preparePOUserInUnitList(1));
        // then
        verify(userGroupServiceLocal, times(1)).updateUserGroup(
                any(UserGroup.class), anyListOf(Product.class),
                anyListOf(Product.class), anyString(),
                anyMap(),
                anyListOf(PlatformUser.class),
                anyMap());
    }

    @Test(expected = ValidationException.class)
    public void updateGroup_ValidationException() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        POUserGroup poUserGroup = preparePOUserGroup(123L);
        poUserGroup.setGroupName("");
        // when
        userGroupService.updateGroup(poUserGroup, MARKETPLACEID,
                preparePOUserInUnitList(1), preparePOUserInUnitList(2), preparePOUserInUnitList(1));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = OperationNotPermittedException.class)
    public void updateGroup_OperationNotPermittedException() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        doThrow(new OperationNotPermittedException()).when(
                userGroupServiceLocal).updateUserGroup(any(UserGroup.class),
                anyListOf(Product.class), anyListOf(Product.class),
                anyString(), anyMap(),
                anyListOf(PlatformUser.class), anyMap());
        // when
        userGroupService.updateGroup(preparePOUserGroup(123L), MARKETPLACEID,
                preparePOUserInUnitList(1), preparePOUserInUnitList(2), preparePOUserInUnitList(1));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updateGroup_ConcurrentModificationException() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        // when
        userGroupService.updateGroup(preparePOUserGroup(0l), MARKETPLACEID,
                preparePOUserInUnitList(1), preparePOUserInUnitList(2), preparePOUserInUnitList(1));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateGroup_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        // when
        userGroupService.updateGroup(preparePOUserGroup(0l), MARKETPLACEID,
                preparePOUserInUnitList(1), preparePOUserInUnitList(2), preparePOUserInUnitList(1));
    }

    //Bug-12657
    @Test
    public void updateGroup_nullInvisibleServices() throws Exception {
        // given
        POUserGroup poUserGroup = preparePOUserGroup(123L);
        poUserGroup.setInvisibleProducts(null);
        final UserGroup userGroup = POUserGroupAssembler.toUserGroup(poUserGroup);
        doReturn(userGroup).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        doReturn(null).when(userGroupService).getInvisibleProducts(anyLong());
        // when
        userGroupService.updateGroup(poUserGroup, MARKETPLACEID,
                preparePOUserInUnitList(1), preparePOUserInUnitList(2), preparePOUserInUnitList(1));
        // then
        verify(userGroupServiceLocal, times(1)).updateUserGroup(
                any(UserGroup.class), anyListOf(Product.class),
                anyListOf(Product.class), anyString(),
                anyMap(),
                anyListOf(PlatformUser.class),
                anyMap());
    }

    @Test
    public void deleteGroup() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        // when
        userGroupService.deleteGroup(preparePOUserGroup(123L));
        // then
        verify(userGroupServiceLocal, times(1)).deleteUserGroup(
                any(UserGroup.class));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteGroup_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        // when
        userGroupService.deleteGroup(preparePOUserGroup(123L));

    }

    @Test(expected = ConcurrentModificationException.class)
    public void deleteGroup_ConcurrentModificationException() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        // when
        userGroupService.deleteGroup(preparePOUserGroup(0l));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteGroup_OperationNotPermittedException() throws Exception {
        // given
        doReturn(prepareUserGroup(123L)).when(userGroupServiceLocal)
                .getUserGroupDetails(anyLong());
        doThrow(new OperationNotPermittedException()).when(
                userGroupServiceLocal).deleteUserGroup(any(UserGroup.class));
        // when
        userGroupService.deleteGroup(preparePOUserGroup(123L));
    }

    @Test
    public void getGroupsForOrganization() {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        doReturn(userGroups).when(userGroupServiceLocal)
                .getUserGroupsForOrganization();
        // when
        List<POUserGroup> poUserGroups = userGroupService
                .getGroupsForOrganization();
        // then
        verifyPOsWithDOs(poUserGroups, userGroups);
    }

    @Test
    public void getGroupsForOrganizationWithoutDefault() {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        doReturn(userGroups).when(userGroupServiceLocal)
                .getUserGroupsForOrganizationWithoutDefault();
        // when
        List<POUserGroup> poUserGroups = userGroupService
                .getGroupsForOrganizationWithoutDefault();
        // then
        verifyPOsWithDOs(poUserGroups, userGroups);
    }

    @Test
    public void getGroupListForOrganizationWithoutDefault() {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        doReturn(userGroups).when(userGroupServiceLocal)
                .getUserGroupsForOrganizationWithoutDefault();

        // when
        List<POUserGroup> poUserGroups = userGroupService
                .getGroupListForOrganizationWithoutDefault();

        // then
        verifyPOsWithDOs(poUserGroups, userGroups);
    }

    @Test
    public void getUserGroupDetails() throws Exception {
        // given
        UserGroup userGroup = prepareUserGroup(123);
        doReturn(userGroup).when(userGroupServiceLocal).getUserGroupDetails(
                anyLong());
        // when
        POUserGroup poUserGroup = userGroupService
                .getUserGroupDetails(anyLong());
        // then
        verifyPOWithDO(poUserGroup, userGroup);
    }

    @Test
    public void getGroupListForOrganization() {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        doReturn(userGroups).when(userGroupServiceLocal)
                .getUserGroupsForOrganization();
        // when
        List<POUserGroup> poUserGroups = userGroupService
                .getGroupListForOrganization();

        // then
        verifyPOsWithDOs(poUserGroups, userGroups);
    }

    @Test
    public void getUserCountForGroup() throws Exception {
        // given
        POUserGroup poUserGroup = preparePOUserGroup(123L);
        doReturn(Long.valueOf(1L)).when(userGroupServiceLocal)
                .getUserCountForGroup(anyLong(), anyBoolean());
        // when
        long count = userGroupService.getUserCountForGroup(
                poUserGroup.getKey(), poUserGroup.isDefault());

        // then
        assertEquals(Long.valueOf(1L), Long.valueOf(count));
    }

    @Test
    public void getAssignedUserIdsForUserGroup() throws Exception {
        // given
        POUserGroup poUserGroup = preparePOUserGroup(123L);
        doReturn(Arrays.asList("userId")).when(userGroupServiceLocal)
                .getAssignedUserIdsForUserGroup(anyLong());
        // when
        List<String> userIds = userGroupService
                .getAssignedUserIdsForUserGroup(poUserGroup.getKey());

        // then
        assertEquals("userId", userIds.get(0));
        assertEquals(1, userIds.size());
    }

    @Test
    public void getInvisibleProductKeysForGroup() throws Exception {

        // when
        userGroupService.getInvisibleProductKeysForGroup(1L);

        // then
        verify(userGroupServiceLocal, times(1))
                .getInvisibleProductKeysForGroup(eq(1L));
    }

    @Test
    public void getInvisibleProductKeysWithUsersFlag() throws Exception {

        // when
        userGroupService.getInvisibleProducts(1L);

        // then
        verify(userGroupServiceLocal, times(1))
                .getInvisibleProducts(eq(1L));
    }

    @Test
    public void handleRemovingCurrentUserFromGroup() {
        // given
        when(userGroupServiceLocal.handleRemovingCurrentUserFromGroup())
                .thenReturn(false);
        // when
        boolean returnValueToCheck = userGroupService
                .handleRemovingCurrentUserFromGroup();

        // then
        assertFalse(returnValueToCheck);
    }

    private UserGroup prepareUserGroup(long key) {
        UserGroup userGroup = new UserGroup();
        userGroup.setKey(key);
        userGroup.setName("name");
        userGroup.setDescription("description");
        userGroup.setReferenceId("reference Id");
        return userGroup;
    }

    private List<UserGroup> prepareUserGroups(int groupNum) {
        List<UserGroup> userGroups = new ArrayList<UserGroup>();
        for (int i = 0; i < groupNum; i++) {
            userGroups.add(prepareUserGroup(i));
        }
        return userGroups;
    }

    private void verifyPOsWithDOs(List<POUserGroup> poUserGroups,
            List<UserGroup> userGroups) {
        int size = poUserGroups.size();
        assertEquals(size, userGroups.size());
        for (int i = 0; i < size; i++) {
            verifyPOWithDO(poUserGroups.get(i), userGroups.get(i));
        }

    }

    private void verifyPOWithDO(POUserGroup poUserGroup, UserGroup userGroup) {
        assertEquals(poUserGroup.getKey(), userGroup.getKey());
        assertEquals(Boolean.valueOf(poUserGroup.isDefault()),
                Boolean.valueOf(userGroup.isDefault()));
        assertEquals(poUserGroup.getGroupName(), userGroup.getName());
        assertEquals(poUserGroup.getGroupDescription(),
                userGroup.getDescription());
        assertEquals(poUserGroup.getGroupReferenceId(),
                userGroup.getReferenceId());
    }

    private POUserGroup preparePOUserGroup(long key) {
        POUserGroup userGroup = new POUserGroup();
        userGroup.setKey(key);
        userGroup.setGroupDescription("groupDescription");
        userGroup.setGroupReferenceId("reference id");
        userGroup.setGroupName("groupName");
        return userGroup;
    }

    private POUserInUnit preparePOUserDetails() {
        POUserInUnit poUserInUnit = new POUserInUnit();
        poUserInUnit.setPoUser(new POUser());
        poUserInUnit.setLocale("en");
        poUserInUnit.getPoUser().setUserId("123456");
        poUserInUnit.getPoUser().setEmail("bes.ma@test.fnst.cn.fujitsu.com");
        List<UserRoleType> assignedRoles = new ArrayList<UserRoleType>();
        assignedRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        poUserInUnit.setAssignedRoles(assignedRoles);
        return poUserInUnit;
    }

    private List<POUserInUnit> preparePOUserInUnitList(int poUsersInUnitsNum) {
        List<POUserInUnit> poUsersInUnits = new ArrayList<POUserInUnit>();
        for (int i = 0; i < poUsersInUnitsNum; i++) {
            poUsersInUnits.add(preparePOUserDetails());
        }
        return poUsersInUnits;
    }
}
