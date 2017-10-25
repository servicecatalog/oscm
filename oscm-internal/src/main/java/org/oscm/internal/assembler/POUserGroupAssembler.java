/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.assembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.POService;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usermanagement.DataConverter;
import org.oscm.internal.usermanagement.POUser;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.validator.BLValidator;

/**
 * Assembler to convert the POUserGroup to the according domain object and vice
 * versa.
 * 
 * @author qiu
 * 
 */
public class POUserGroupAssembler extends BasePOAssembler {

    private static final String FIELD_NAME_GROUP_NAME = "groupName";
    private static final String FIELD_NAME_GROUP_DESCRIPTION = "groupDescription";
    private static final String FIELD_NAME_GROUP_REFERENCE_ID = "groupReferenceId";
    private static final String FIELD_NAME_POUSERGROUP = "POUserGroup";

    /**
     * Create a UserGroup object and fills the fields with the corresponding
     * data from the PO.
     * 
     * @param poUserGroup
     *            The PO converted to DO.
     * @return The domain object.
     */
    public static UserGroup toUserGroup(POUserGroup poUserGroup)
            throws ValidationException {
        validate(poUserGroup);
        UserGroup userGroup = new UserGroup();
        copyAttributes(userGroup, poUserGroup);
        return userGroup;
    }

    /**
     * Create UserGroup objects and fills the fields with the corresponding data
     * from the POs.
     * 
     * @param poUserGroups
     *            The PO list converted to DO.
     * @return The domain objects.
     */
    public static List<UserGroup> toUserGroups(List<POUserGroup> poUserGroups)
            throws ValidationException {
        BLValidator.isNotNull("poUserGroups", poUserGroups);
        List<UserGroup> userGroups = new ArrayList<UserGroup>();
        for (POUserGroup poUserGroup : poUserGroups) {
            userGroups.add(toUserGroup(poUserGroup));
        }
        return userGroups;
    }

    /**
     * Create a new POUserGroup object and fills the fields with the
     * corresponding fields from the given domain object.
     * 
     * @param userGroup
     *            The domain object containing the values to be set.
     * @return The created po or null if the domain object was null.
     */
    public static POUserGroup toPOUserGroup(UserGroup userGroup) {
        return toPOUserGroup(userGroup, PerformanceHint.ALL_FIELDS);
    }
    
    public static POUserGroup toPOUserGroupWithUsers(UserGroup userGroup) {
        if (userGroup == null) {
            return null;
        }
        POUserGroup poUserGroup = fillBasicUserGroupValues(userGroup);
        List<POUserInUnit> assignedUsers = new ArrayList<POUserInUnit>();
        for (UserGroupToUser userGroupToUser : userGroup.getUserGroupToUsers()) {
            POUserInUnit poUserInUnit = new POUserInUnit();
            POUser poUser = new POUser();
            poUser.setKey(userGroupToUser.getPlatformuser_tkey());
            poUser.setUserId(userGroupToUser.getPlatformuser().getUserId());
            poUserInUnit.setPoUser(poUser);
            poUserInUnit.setSelected(true);
            Tenant tenant = userGroup.getOrganization().getTenant();
            if (tenant != null) {
                poUserInUnit.setTenantId(tenant.getTenantId());
            }
            if (!userGroupToUser.getUnitRoleAssignments().isEmpty()) {
                UnitRoleAssignment unitRoleAssignment = userGroupToUser.getUnitRoleAssignments().get(0);
                poUserInUnit.setRoleInUnit(unitRoleAssignment.getUnitUserRole().getRoleName().name());
            }
            List<UserRoleType> assignedRoles = new ArrayList<UserRoleType>();
            for (RoleAssignment roleAssignment : userGroupToUser
                    .getPlatformuser().getAssignedRoles()) {
                assignedRoles.add(roleAssignment.getRole().getRoleName());
            }
            poUserInUnit.setAssignedRoles(assignedRoles);
            assignedUsers.add(poUserInUnit);
        }

        poUserGroup.setUsersAssignedToUnit(assignedUsers);
        return poUserGroup;
    }
    
    private static POUserGroup fillBasicUserGroupValues(UserGroup userGroup) {
        POUserGroup poUserGroup = new POUserGroup();
        updatePresentationObject(poUserGroup, userGroup);
        fillPOUserGroup(poUserGroup, userGroup);
        return poUserGroup;
    }

    public static POUserGroup toPOUserGroup(UserGroup userGroup,
            PerformanceHint scope) {
        if (userGroup == null) {
            return null;
        }
        POUserGroup poUserGroup = fillBasicUserGroupValues(userGroup);
        if (scope.equals(PerformanceHint.ALL_FIELDS)) {
            fillUsers(poUserGroup, userGroup);
            fillProducts(poUserGroup, userGroup);
        }
        return poUserGroup;
    }

    public static List<POUserGroup> toPOUserGroups(List<UserGroup> userGroups,
            PerformanceHint scope) {
        if (userGroups == null) {
            return null;
        }
        List<POUserGroup> poUserGroups = new ArrayList<POUserGroup>();
        for (UserGroup userGroup : userGroups) {
            poUserGroups.add(toPOUserGroup(userGroup, scope));
        }
        return poUserGroups;
    }

    public static List<POUserGroup> toPOUserGroups(Map<UserGroup, UnitRoleType> groupsWithRoles,
            PerformanceHint scope) {
        if (groupsWithRoles == null) {
            return null;
        }
        Collection<UserGroup> userGroups = groupsWithRoles.keySet();
        if (userGroups == null) {
            return null;
        }
        List<POUserGroup> poUserGroups = new ArrayList<POUserGroup>();
        for (UserGroup userGroup : userGroups) {
            POUserGroup poUserGroup = toPOUserGroup(userGroup, scope);
            poUserGroup.setSelectedRole(groupsWithRoles.get(userGroup).name());
            poUserGroups.add(poUserGroup);
        }
        return poUserGroups;
    }

    public static List<POUserGroup> toPOUserGroups(List<UserGroup> userGroups) {
        return toPOUserGroups(userGroups, PerformanceHint.ALL_FIELDS);
    }

    public static List<Product> toProducts(List<POService> services) {
        if (services == null) {
            return null;
        }
        List<Product> products = new ArrayList<Product>();
        for (POService service : services) {
            Product p = new Product();
            p.setKey(service.getKey());
            p.setProductId(service.getProductId());

            products.add(p);
        }
        return products;
    }

    private static void fillUsers(POUserGroup poUserGroup, UserGroup userGroup) {
        List<PlatformUser> users = userGroup.getUsers();
        List<POUserDetails> poUsers = new ArrayList<POUserDetails>();
        DataConverter dc = new DataConverter();
        for (PlatformUser user : users) {
            poUsers.add(dc.toPOUserDetails(user, null));
        }
        poUserGroup.setUsers(poUsers);
    }

    private static void fillProducts(POUserGroup poUserGroup,
            UserGroup userGroup) {
        List<Product> products = userGroup.getInvisibleProducts();
        List<POService> services = new ArrayList<POService>();
        for (Product p : products) {
            POService poService = new POService(p.getKey(), p.getVersion());
            services.add(poService);
        }
        poUserGroup.setInvisibleServices(services);
    }

    private static void fillPOUserGroup(POUserGroup poUserGroup,
            UserGroup userGroup) {
        poUserGroup.setDefault(userGroup.isDefault());
        poUserGroup.setGroupDescription(userGroup.getDescription());
        poUserGroup.setGroupReferenceId(userGroup.getReferenceId());
        poUserGroup.setGroupName(userGroup.getName());
    }

    private static void copyAttributes(UserGroup userGroup,
            POUserGroup poUserGroup) {
        userGroup.setKey(poUserGroup.getKey());
        userGroup.setDescription(poUserGroup.getGroupDescription());
        userGroup.setName(poUserGroup.getGroupName());
        userGroup.setIsDefault(poUserGroup.isDefault());
        userGroup.setReferenceId(poUserGroup.getGroupReferenceId());
        copyServices(userGroup, poUserGroup);
        copyUsers(userGroup, poUserGroup);
    }

    private static void copyServices(UserGroup userGroup,
            POUserGroup poUserGroup) {
        List<POService> invisibleServices = poUserGroup.getInvisibleServices();
        List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<UserGroupToInvisibleProduct>();
        for (POService service : invisibleServices) {
            Product p = new Product();
            p.setKey(service.getKey());
            p.setProductId(service.getProductId());
            UserGroupToInvisibleProduct userGroupToInvisibleProduct = new UserGroupToInvisibleProduct();
            userGroupToInvisibleProduct.setProduct(p);
            userGroupToInvisibleProduct.setUserGroup(userGroup);
            userGroupToInvisibleProducts.add(userGroupToInvisibleProduct);
        }
        userGroup.setUserGroupToInvisibleProducts(userGroupToInvisibleProducts);

    }

    private static void copyUsers(UserGroup userGroup, POUserGroup poUserGroup) {
        List<POUserDetails> users = poUserGroup.getUsers();
        List<UserGroupToUser> userGroupToUsers = new ArrayList<UserGroupToUser>();
        for (POUserDetails user : users) {
            PlatformUser p = new PlatformUser();
            p.setKey(user.getKey());
            p.setUserId(user.getUserId());
            p.setEmail(user.getEmail());
            p.setLocale(user.getLocale());
            UserGroupToUser userGroupToUser = new UserGroupToUser();
            userGroupToUser.setUserGroup(userGroup);
            userGroupToUser.setPlatformuser(p);
            userGroupToUsers.add(userGroupToUser);
        }
        userGroup.setUserGroupToUsers(userGroupToUsers);
    }

    private static void validate(POUserGroup poUserGroup)
            throws ValidationException {
        BLValidator.isNotNull(FIELD_NAME_POUSERGROUP, poUserGroup);
        BLValidator.isUserGroupName(FIELD_NAME_GROUP_NAME,
                poUserGroup.getGroupName(), true);
        BLValidator.isDescription(FIELD_NAME_GROUP_DESCRIPTION,
                poUserGroup.getGroupDescription(), false);
        BLValidator.isDescription(FIELD_NAME_GROUP_REFERENCE_ID,
                poUserGroup.getGroupReferenceId(), false);
    }
}
