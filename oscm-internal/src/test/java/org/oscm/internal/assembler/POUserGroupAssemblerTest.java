/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.usergroupmgmt.POService;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.validator.ADMValidator;

/**
 * Unit test of POUserGroupAssembler
 * 
 * @author qiu
 * 
 */
public class POUserGroupAssemblerTest {

    @Test
    public void toPOUserGroup_null() throws Exception {
        // when
        POUserGroup poUserGroup = POUserGroupAssembler.toPOUserGroup(null);
        // then
        assertNull(poUserGroup);
    }

    @Test
    public void toPOUserGroup() throws Exception {
        // given
        UserGroup userGroup = prepareUserGroup(123L);
        // when
        POUserGroup poUserGroup = POUserGroupAssembler.toPOUserGroup(userGroup);
        // then
        verifyPOWithDO(poUserGroup, userGroup);
        verifyUser(poUserGroup, userGroup);
        verifyProduct(poUserGroup, userGroup);

    }

    @Test
    public void toPOUserGroup_onlyListingFields() throws Exception {
        // given
        UserGroup userGroup = prepareUserGroup(123L);
        // when
        POUserGroup poUserGroup = POUserGroupAssembler.toPOUserGroup(userGroup,
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        // then
        verifyPOWithDO(poUserGroup, userGroup);
        verifyPOOnlyWithLstingingFields(poUserGroup);

    }

    @Test
    public void toPOUserGroups_null() throws Exception {
        // when
        List<POUserGroup> poUserGroup = POUserGroupAssembler
                .toPOUserGroups(null);
        // then
        assertNull(poUserGroup);
    }

    @Test
    public void toPOUserGroups() throws Exception {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        Map<UserGroup, UnitRoleType> groupsWithRoles = new HashMap<UserGroup, UnitRoleType>();
        groupsWithRoles.put(userGroups.get(0), UnitRoleType.ADMINISTRATOR);
        groupsWithRoles.put(userGroups.get(1), UnitRoleType.USER);
        // when
        List<POUserGroup> poUserGroups = POUserGroupAssembler
                .toPOUserGroups(userGroups);
        // then
        assertEquals(2, poUserGroups.size());
        verifyPOWithDO(poUserGroups.get(0), userGroups.get(0));
        verifyPOWithDO(poUserGroups.get(1), userGroups.get(1));
    }

    @Test
    public void toPOUserGroupsWithRoles() throws Exception {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        // when
        List<POUserGroup> poUserGroups = POUserGroupAssembler
                .toPOUserGroups(userGroups);
        // then
        assertEquals(2, poUserGroups.size());
        verifyPOWithDO(poUserGroups.get(0), userGroups.get(0));
        verifyPOWithDO(poUserGroups.get(1), userGroups.get(1));
    }

    @Test
    public void toPOUserGroupWithUsers() throws Exception {
        // given
        UserGroup userGroup = prepareUserGroup(1209L);
        // when
        POUserGroup poUserGroup = POUserGroupAssembler
                .toPOUserGroupWithUsers(userGroup);
        // then
        assertEquals(1, poUserGroup.getUsersAssignedToUnit().size());
        verifyPOWithDO(poUserGroup, userGroup);
        verifyUserInUnit(poUserGroup, userGroup);
    }

    @Test
    public void toPOUserGroups_onlyListingFields() throws Exception {
        // given
        List<UserGroup> userGroups = prepareUserGroups(2);
        // when
        List<POUserGroup> poUserGroups = POUserGroupAssembler.toPOUserGroups(
                userGroups, PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
        // then
        assertEquals(2, poUserGroups.size());
        verifyPOWithDO(poUserGroups.get(0), userGroups.get(0));
        verifyPOWithDO(poUserGroups.get(1), userGroups.get(1));
        verifyPOOnlyWithLstingingFields(poUserGroups.get(0));
        verifyPOOnlyWithLstingingFields(poUserGroups.get(1));
    }

    private void verifyPOOnlyWithLstingingFields(POUserGroup poUserGroup) {
        assertEquals(0, poUserGroup.getUsers().size());
        assertEquals(0, poUserGroup.getVisibleServices().size());
    }

    @Test
    public void toUserGroup() throws Exception {
        // given
        POUserGroup poUserGroup = preparePOUserGroup(123L);
        // when
        UserGroup userGroup = POUserGroupAssembler.toUserGroup(poUserGroup);
        // then
        verifyPOWithDO(poUserGroup, userGroup);
        verifyProducts(poUserGroup, userGroup);
        verifyUser(poUserGroup, userGroup);
    }

    private void verifyProducts(POUserGroup poUserGroup, UserGroup userGroup) {
        List<POService> services = poUserGroup.getInvisibleServices();
        List<Product> products = userGroup.getInvisibleProducts();
        assertEquals(services.size(), products.size());
        assertEquals(services.get(0).getKey(), products.get(0).getKey());

    }

    @Test(expected = ValidationException.class)
    public void toUserGroup_tooLongName() throws Exception {
        // given
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup
                .setGroupName(longText(ADMValidator.LENGTH_USER_GROUP_NAME + 1));
        // when
        POUserGroupAssembler.toUserGroup(poUserGroup);
    }

    @Test
    public void toUserGroup_tooLongDescription() throws Exception {
        // given
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup.setGroupName("Group name");
        poUserGroup
                .setGroupDescription(longText(ADMValidator.LENGTH_DESCRIPTION + 1));
        // when
        try {
            POUserGroupAssembler.toUserGroup(poUserGroup);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals("Wrong reason", ReasonEnum.LENGTH, ve.getReason());
            assertEquals("Wrong parameter", "groupDescription", ve.getMember());
        }
    }

    @Test
    public void toUserGroup_tooLongReferenceId() throws Exception {
        // given
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup.setGroupName("Group name");
        poUserGroup
                .setGroupReferenceId(longText(ADMValidator.LENGTH_DESCRIPTION + 1));

        // when
        try {
            POUserGroupAssembler.toUserGroup(poUserGroup);
            fail("ValidationException expected");
        } catch (ValidationException ve) {
            // then
            assertEquals("Wrong reason", ReasonEnum.LENGTH, ve.getReason());
            assertEquals("Wrong parameter", "groupReferenceId", ve.getMember());
        }
    }

    @Test(expected = ValidationException.class)
    public void toUserGroup_null() throws Exception {
        // when
        POUserGroupAssembler.toUserGroup(null);
    }

    @Test(expected = ValidationException.class)
    public void toUserGroup_noGroupName() throws Exception {
        // given
        POUserGroup poUserGroup = new POUserGroup();
        // when
        POUserGroupAssembler.toUserGroup(poUserGroup);
    }

    @Test(expected = ValidationException.class)
    public void toUserGroups_ValidationException() throws Exception {
        POUserGroupAssembler.toUserGroups(null);
    }

    @Test
    public void toUserGroups() throws Exception {
        // given
        List<POUserGroup> poUserGroups = preparePOUserGroups(2);
        // when
        List<UserGroup> userGroups = POUserGroupAssembler
                .toUserGroups(poUserGroups);
        // then
        assertEquals(2, userGroups.size());
        verifyPOWithDO(poUserGroups.get(0), userGroups.get(0));
        verifyPOWithDO(poUserGroups.get(1), userGroups.get(1));
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

    private void verifyUser(POUserGroup poUserGroup, UserGroup userGroup) {
        PlatformUser user = userGroup.getUserGroupToUsers().get(0)
                .getPlatformuser();
        POUserDetails poUser = poUserGroup.getUsers().get(0);
        assertEquals(poUser.getUserId(), user.getUserId());
        assertEquals(poUser.getFirstName(), user.getFirstName());
        assertEquals(poUser.getLocale(), user.getLocale());
    }

    private void verifyUserInUnit(POUserGroup poUserGroup, UserGroup userGroup) {
        POUserInUnit poUserInUnit = poUserGroup.getUsersAssignedToUnit().get(0);

        PlatformUser user = userGroup.getUserGroupToUsers().get(0)
                .getPlatformuser();
        assertEquals(poUserInUnit.getUserId(), user.getUserId());
        assertEquals(poUserInUnit.getPoUser().getKey(), user.getKey());
        assertTrue(poUserInUnit.isSelected());
        assertEquals(poUserInUnit.getRoleInUnit(), UnitRoleType.USER.name());
    }

    private void verifyProduct(POUserGroup poUserGroup, UserGroup userGroup) {
        Product product = userGroup.getUserGroupToInvisibleProducts().get(0)
                .getProduct();
        POService poService = poUserGroup.getInvisibleServices().get(0);
        assertEquals(poService.getKey(), product.getKey());
    }

    private UserGroup prepareUserGroup(long key) {
        UserGroup userGroup = new UserGroup();
        userGroup.setKey(key);
        userGroup.setName("name");
        userGroup.setDescription("description");
        userGroup.setReferenceId("a referenceId");
        prepareUsers(userGroup);
        prepareProducts(userGroup);
        userGroup.setOrganization(new Organization());
        return userGroup;
    }

    private List<UserGroup> prepareUserGroups(int groupNum) {
        List<UserGroup> userGroups = new ArrayList<UserGroup>();
        for (int i = 0; i < groupNum; i++) {
            userGroups.add(prepareUserGroup(i));
        }
        return userGroups;
    }

    private void prepareUsers(UserGroup userGroup) {
        List<UserGroupToUser> userGroupToUsers = new ArrayList<UserGroupToUser>();
        UserGroupToUser userGroupToUser = new UserGroupToUser();
        userGroupToUser.setUserGroup(userGroup);
        userGroupToUser.setPlatformuser(preparePlatformUser());
        UnitRoleAssignment unitRoleAssignment = new UnitRoleAssignment();
        UnitUserRole unitUserRole = new UnitUserRole();
        unitUserRole.setRoleName(UnitRoleType.USER);
        unitRoleAssignment.setUnitUserRole(unitUserRole);
        userGroupToUser.setUnitRoleAssignments(Arrays
                .asList(unitRoleAssignment));
        userGroupToUsers.add(userGroupToUser);
        userGroup.setUserGroupToUsers(userGroupToUsers);
    }

    private void prepareProducts(UserGroup userGroup) {
        List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<UserGroupToInvisibleProduct>();
        UserGroupToInvisibleProduct userGroupToInvisibleProduct = new UserGroupToInvisibleProduct();
        userGroupToInvisibleProduct.setUserGroup(userGroup);
        userGroupToInvisibleProduct.setProduct(prepareProduct());
        userGroupToInvisibleProducts.add(userGroupToInvisibleProduct);
        userGroup.setUserGroupToInvisibleProducts(userGroupToInvisibleProducts);
    }

    private PlatformUser preparePlatformUser() {
        Organization org = new Organization();
        PlatformUser user = new PlatformUser();
        user.setUserId("userId");
        user.setFirstName("firstName");
        user.setLocale("en");
        user.setOrganization(org);
        return user;
    }

    private Product prepareProduct() {
        Product p = new Product();
        p.setKey(123);
        return p;
    }

    private POUserGroup preparePOUserGroup(long key) {
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup.setKey(key);
        poUserGroup.setDefault(true);
        poUserGroup.setGroupName("groupName");
        poUserGroup.setGroupDescription("description");
        poUserGroup.setGroupReferenceId("referenceId");
        poUserGroup.setInvisibleServices(prepareServices());
        poUserGroup.setUsers(prepareUsers());
        return poUserGroup;
    }

    private List<POService> prepareServices() {
        List<POService> services = new ArrayList<POService>();
        POService service = new POService();
        service.setKey(123L);
        service.setServiceName("serviceName");
        services.add(service);
        return services;

    }

    private List<POUserDetails> prepareUsers() {
        List<POUserDetails> users = new ArrayList<POUserDetails>();
        POUserDetails user = new POUserDetails();
        user.setKey(123L);
        user.setUserId("userId");
        users.add(user);
        return users;
    }

    private List<POUserGroup> preparePOUserGroups(int groupNum) {
        List<POUserGroup> poUserGroups = new ArrayList<POUserGroup>();
        for (int i = 0; i < groupNum; i++) {
            poUserGroups.add(preparePOUserGroup(i));
        }
        return poUserGroups;
    }

    private String longText(int length) {
        char text[] = new char[length];
        Arrays.fill(text, 'a');
        return new String(text);
    }

}
