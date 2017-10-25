/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-24                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.usergroupmgmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.assembler.BasePOAssembler;
import org.oscm.internal.assembler.POUserGroupAssembler;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletingUnitWithSubscriptionsNotPermittedException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usermanagement.DataConverter;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.paginator.PaginationUsersInUnit;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

/**
 * Manage user group Service
 * 
 * @author qiu
 * 
 */
@Stateless
@Remote(UserGroupService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class UserGroupServiceBean implements UserGroupService {

    @Inject
    UserGroupServiceLocalBean userGroupService;

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = SubscriptionServiceLocal.class)
    SubscriptionServiceLocal ssl;

    @EJB(beanInterface = SubscriptionListServiceLocal.class)
    SubscriptionListServiceLocal slsl;

    private DataConverter dc = new DataConverter();

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public POUserGroup createGroup(POUserGroup group, String marketplaceId)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, MailOperationException,
            ObjectNotFoundException, ConcurrentModificationException {
        return POUserGroupAssembler.toPOUserGroup(userGroupService
                .createUserGroup(POUserGroupAssembler.toUserGroup(group), null,
                        null, marketplaceId));

    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public POUserGroup updateGroup(POUserGroup group, String marketplaceId,
            List<POUserInUnit> usersToAssign,
            List<POUserInUnit> usersToUnassign,
            List<POUserInUnit> usersToRoleUpdate) throws ValidationException,
            OperationNotPermittedException, ConcurrentModificationException,
            ObjectNotFoundException, NonUniqueBusinessKeyException,
            MailOperationException, UserRoleAssignmentException {
        verifyGroupVersionAndKey(group);
        List<Product> invisibleProducts = verifyProducts(group
                .getInvisibleServices());
        List<Product> visibleProducts = verifyProducts(group
                .getVisibleServices());

        verifyInvisibleProducts(group.getInvisibleProducts(), group.getKey());
        Map<PlatformUser, String> usersToAssignWithRole = convertToPlatformUsersWithRole(usersToAssign);
        Map<PlatformUser, String> usersToUpdateWithRole = convertToPlatformUsersWithRole(usersToRoleUpdate);

        return POUserGroupAssembler.toPOUserGroup(userGroupService
                .updateUserGroup(POUserGroupAssembler.toUserGroup(group),
                        visibleProducts, invisibleProducts, marketplaceId,
                        usersToAssignWithRole,
                        convertToPlatformUsers(usersToUnassign),
                        usersToUpdateWithRole));

    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public boolean handleRemovingCurrentUserFromGroup() {
        if (!userGroupService.handleRemovingCurrentUserFromGroup()) {
            return false;
        }
        PlatformUser currentUser = dm.getCurrentUser();
        if (currentUser.hasSubscriptionOwnerRole()) {
            return true;
        }
        List<Subscription> subscriptions = slsl
                .getSubscriptionsForOwner(currentUser);
        for (Subscription subscription : subscriptions) {
            ssl.removeSubscriptionOwner(subscription);
        }
        return true;

    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public boolean deleteGroup(POUserGroup group) throws ValidationException,
            OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException, MailOperationException,
            DeletingUnitWithSubscriptionsNotPermittedException {
        UserGroup storedGroup = verifyGroupVersionAndKey(group);
        return userGroupService.deleteUserGroup(storedGroup);
    }

    private List<PlatformUser> convertToPlatformUsers(List<POUserInUnit> poUsers) {
        if (poUsers == null) {
            return null;
        }
        DataConverter dc = new DataConverter();
        List<PlatformUser> users = new ArrayList<>();
        for (POUserInUnit poUser : poUsers) {
            users.add(dc.toPlatformUser(poUser));
        }
        return users;
    }

    private Map<PlatformUser, String> convertToPlatformUsersWithRole(
            List<POUserInUnit> poUsers) {
        if (poUsers == null) {
            return null;
        }
        DataConverter dc = new DataConverter();
        Map<PlatformUser, String> users = new HashMap<PlatformUser, String>();
        for (POUserInUnit poUser : poUsers) {
            users.put(dc.toPlatformUser(poUser), poUser.getRoleInUnit());
        }
        return users;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<POUserGroup> getGroupsForOrganization() {
        return POUserGroupAssembler.toPOUserGroups(userGroupService
                .getUserGroupsForOrganization());
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<POUserGroup> getGroupsForOrganizationWithoutDefault() {
        return POUserGroupAssembler.toPOUserGroups(userGroupService
                .getUserGroupsForOrganizationWithoutDefault());
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<POUserGroup> getGroupListForOrganizationWithoutDefault() {
        return POUserGroupAssembler.toPOUserGroups(
                userGroupService.getUserGroupsForOrganizationWithoutDefault(),
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public POUserGroup getUserGroupDetails(long groupKey)
            throws ObjectNotFoundException {
        return POUserGroupAssembler.toPOUserGroup(userGroupService
                .getUserGroupDetails(groupKey));
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public POUserGroup getUserGroupDetailsForList(long groupKey)
            throws ObjectNotFoundException {
        return POUserGroupAssembler.toPOUserGroup(
                userGroupService.getUserGroupDetails(groupKey),
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public POUserGroup getUserGroupDetailsWithUsers(long groupKey)
            throws ObjectNotFoundException {
        return POUserGroupAssembler.toPOUserGroupWithUsers(userGroupService
                .getUserGroupDetails(groupKey));
    }

    @Override
    public List<POUserGroup> getUserGroupsForUserWithoutDefault(long userKey) {
        return POUserGroupAssembler.toPOUserGroups(userGroupService
                .getUserGroupsForUserWithoutDefault(userKey));
    }

    @Override
    public List<POUserGroup> getUserGroupListForUserWithoutDefault(long userKey) {
        return POUserGroupAssembler.toPOUserGroups(
                userGroupService.getUserGroupsForUserWithoutDefault(userKey),
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<POUserGroup> getUserGroupListForUserWithRoles(String userId) {
        return POUserGroupAssembler.toPOUserGroups(
                userGroupService.getUserGroupsForUserWithRoles(userId),
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN" })
    public List<POUserGroup> getUserGroupListForUserWithRolesWithoutDefault(
            String userId, long userKey) {
        return POUserGroupAssembler.toPOUserGroups(userGroupService
                .getUserGroupsForUserWithRolesWithoutDefault(userId, userKey),
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Override
    public List<Long> getInvisibleProductKeysForUser(long userKey)
            throws ObjectNotFoundException {
        return userGroupService.getInvisibleProductKeysForUser(userKey);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<POUserGroup> getGroupListForOrganization() {
        return POUserGroupAssembler.toPOUserGroups(
                userGroupService.getUserGroupsForOrganization(),
                PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public long getUserCountForGroup(long groupKey, boolean isDefaultGroup) {
        return userGroupService.getUserCountForGroup(groupKey, isDefaultGroup);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<String> getAssignedUserIdsForUserGroup(long groupKey) {
        return userGroupService.getAssignedUserIdsForUserGroup(groupKey);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<Long> getInvisibleProductKeysForGroup(long groupKey) {
        return userGroupService.getInvisibleProductKeysForGroup(groupKey);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<POUserGroupToInvisibleProduct> getInvisibleProducts(
            long userGroupKey) {
        List<UserGroupToInvisibleProduct> invisibleProducts = userGroupService
                .getInvisibleProducts(userGroupKey);
        List<POUserGroupToInvisibleProduct> invisibleProductsPO = new ArrayList<POUserGroupToInvisibleProduct>();
        for (UserGroupToInvisibleProduct userGroupToInvisibleProduct : invisibleProducts) {
            POUserGroupToInvisibleProduct poUserGroupToInvisibleProduct = new POUserGroupToInvisibleProduct();
            poUserGroupToInvisibleProduct.setKey(userGroupToInvisibleProduct
                    .getKey());
            poUserGroupToInvisibleProduct
                    .setVersion(userGroupToInvisibleProduct.getVersion());
            poUserGroupToInvisibleProduct
                    .setForAllUsers(userGroupToInvisibleProduct.isForallusers());
            poUserGroupToInvisibleProduct
                    .setServiceKey(userGroupToInvisibleProduct
                            .getProduct_tkey());
            invisibleProductsPO.add(poUserGroupToInvisibleProduct);
        }
        return invisibleProductsPO;
    }

    private List<Product> verifyProducts(List<POService> services)
            throws ObjectNotFoundException, ConcurrentModificationException {
        List<Product> products = new ArrayList<>();
        for (POService service : services) {
            Product product = dm.getReference(Product.class, service.getKey());
            BasePOAssembler.verifyVersionAndKey(product, service);
            products.add(product);
        }
        return products;
    }

    private void verifyInvisibleProducts(
            List<POUserGroupToInvisibleProduct> invisibleProducts,
            long userGroupKey) throws ConcurrentModificationException {
        if (invisibleProducts == null) {
            return;
        }
        for (POUserGroupToInvisibleProduct invisibleProduct : invisibleProducts) {
            UserGroupToInvisibleProduct product;
            try {
                product = dm.getReference(UserGroupToInvisibleProduct.class,
                        invisibleProduct.getKey());
            } catch (ObjectNotFoundException e) {
                throw new ConcurrentModificationException();
            }
            BasePOAssembler.verifyVersionAndKey(product, invisibleProduct);
        }

        List<POUserGroupToInvisibleProduct> currentlyStoredInvisibilities = getInvisibleProducts(userGroupKey);
        boolean entryFound = false;
        for (POUserGroupToInvisibleProduct existingInDb : currentlyStoredInvisibilities) {
            entryFound = false;
            for (POUserGroupToInvisibleProduct invisibleProduct : invisibleProducts) {
                if (existingInDb.getKey() != invisibleProduct.getKey()) {
                    continue;
                }
                entryFound = true;
                if (existingInDb.getVersion() != invisibleProduct.getVersion()) {
                    throw new ConcurrentModificationException();
                }
            }
            if (!entryFound) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private UserGroup verifyGroupVersionAndKey(POUserGroup group)
            throws ObjectNotFoundException, ConcurrentModificationException {
        UserGroup storedGroup = userGroupService.getUserGroupDetails(group
                .getKey());
        BasePOAssembler.verifyVersionAndKey(storedGroup, group);
        return storedGroup;
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<POUserGroup> getUserGroupsForUserWithRole(long userKey,
            long userRoleKey) {
        return POUserGroupAssembler.toPOUserGroups(userGroupService
                .getUserGroupsForUserWithRole(userKey, userRoleKey));
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<POUserGroup> getUserGroupsForUserWithRoleWithoutDefault(
            long userKey, long userRoleKey) {
        return POUserGroupAssembler.toPOUserGroups(userGroupService
                .getUserGroupsForUserWithRoleWithoutDefault(userKey,
                        userRoleKey));
    }

    @Override
    public Response getUsersForGroup(PaginationUsersInUnit pagination,
            String selectedGroupId) throws OrganizationAuthoritiesException {
        List<PlatformUser> users = userGroupService.getUsersForGroup(
                pagination, selectedGroupId);
        List<POUserInUnit> poUsers = new ArrayList<POUserInUnit>();
        for (PlatformUser user : users) {
            POUserInUnit poUser = dc.toPoUserInUnit(user, selectedGroupId);
            poUsers.add(poUser);
        }
        return new Response(poUsers);
    }

    @Override
    public Integer getCountUsersForGroup(PaginationUsersInUnit pagination,
            String selectedGroupId) throws OrganizationAuthoritiesException {
        return userGroupService.getCountUsersForGroup(pagination,
                selectedGroupId);
    }

}
