/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usergroupmgmt;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletingUnitWithSubscriptionsNotPermittedException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.paginator.PaginationUsersInUnit;

/**
 * Service providing the functionality to manage user group.
 * 
 * @author qiu
 * 
 */
@Remote
public interface UserGroupService {

    /**
     * create new user group for organization
     * 
     * @param group
     *            the object containing the data of the user group
     * @param marketplaceId
     *            the id of marketplace
     * @throws ValidationException
     * @throws NonUniqueBusinessKeyException
     * @throws OperationNotPermittedException
     * @throws MailOperationException
     * @throws ObjectNotFoundException
     * @throws ConcurrentModificationException
     * 
     */
    public POUserGroup createGroup(POUserGroup group, String marketplaceId)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, MailOperationException,
            ObjectNotFoundException, ConcurrentModificationException;

    /**
     * update existed user group
     * 
     * @param group
     *            the object containing the data of the user group
     * @param marketplaceId
     *            the id of marketplace
     * @param usersToAssign
     *            the users to assign
     * @param usersToDeAssign
     *            the users to deassign
     * @param usersToRoleUpdate
     *            the users to update
     * @throws ValidationException
     * @throws OperationNotPermittedException
     * @throws ConcurrentModificationException
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     * @throws MailOperationException
     * @throws UserRoleAssignmentException
     */
    public POUserGroup updateGroup(POUserGroup group, String marketplaceId,
            List<POUserInUnit> usersToAssign,
            List<POUserInUnit> usersToUnassign,
            List<POUserInUnit> usersToRoleUpdate) throws ValidationException,
            OperationNotPermittedException, ConcurrentModificationException,
            ObjectNotFoundException, NonUniqueBusinessKeyException,
            MailOperationException, UserRoleAssignmentException;

    /**
     * delete user group
     * 
     * @param group
     *            the object containing the data of the user group
     * @throws ValidationException
     * @throws OperationNotPermittedException
     * @throws ConcurrentModificationException
     * @throws ObjectNotFoundException
     * @throws MailOperationException
     * @throws DeletingUnitWithSubscriptionsNotPermittedException
     */
    public boolean deleteGroup(POUserGroup group) throws ValidationException,
            OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException, MailOperationException,
            DeletingUnitWithSubscriptionsNotPermittedException;

    /**
     * get user groups of current user's organization
     * 
     * @return POUserGroup list
     * 
     */
    public List<POUserGroup> getGroupsForOrganization();

    /**
     * get user groups of current user's organization without default group
     * 
     * @return POUserGroup list
     * 
     */
    public List<POUserGroup> getGroupsForOrganizationWithoutDefault();

    /**
     * get user groups of current user's organization without default group,
     * only load fields for listing
     * 
     * @return list of POUserGroup
     */
    public List<POUserGroup> getGroupListForOrganizationWithoutDefault();

    /**
     * get user groups of specified user without default group
     * 
     * @param userKey
     * @return POUserGroup list
     * 
     */
    public List<POUserGroup> getUserGroupsForUserWithoutDefault(long userKey);

    /**
     * get user groups of specified user without default group, only load fields
     * for listing
     * 
     * @param userKey
     * @return POUserGroup list
     * 
     */
    public List<POUserGroup> getUserGroupListForUserWithoutDefault(long userKey);

    /**
     * get details of user group by key
     * 
     * @param groupKey
     *            the key of user group
     * @return POUserGroup
     * @throws ObjectNotFoundException
     */
    public POUserGroup getUserGroupDetails(long groupKey)
            throws ObjectNotFoundException;

    /**
     * get details of user group by key, only load fields for listing
     * 
     * @param groupKey
     *            the key of user group
     * @return POUserGroup
     * @throws ObjectNotFoundException
     */
    public POUserGroup getUserGroupDetailsForList(long groupKey)
            throws ObjectNotFoundException;

    /**
     * get invisible product keys for user
     * 
     * @param userKey
     *            the key of user
     * @return list of invisible product keys
     * @throws ObjectNotFoundException
     */
    public List<Long> getInvisibleProductKeysForUser(long userKey)
            throws ObjectNotFoundException;

    /**
     * get group list of current user's organization, only load fields for
     * listing
     * 
     * @return group list
     */
    public List<POUserGroup> getGroupListForOrganization();

    /**
     * get user number for specified group
     * 
     * @param groupKey
     * @param isDefaultGroup
     * @return user number
     */
    public long getUserCountForGroup(long groupKey, boolean isDefaultGroup);

    /**
     * get all user ids for specified none default group
     * 
     * @param groupKey
     * @return the list of user id
     */
    public List<String> getAssignedUserIdsForUserGroup(long groupKey);

    /**
     * get all invisible product keys for specified group
     * 
     * @param groupKey
     * @return the list of invisible product keys
     */
    public List<Long> getInvisibleProductKeysForGroup(long groupKey);

    /**
     * get all user groups to which user is assigned and role that user has in
     * particular group
     * 
     * @param userId
     * @return the list of user groups
     */
    public List<POUserGroup> getUserGroupListForUserWithRoles(String userId);

    /**
     * Get all Units, which are assigned to user with specific role (for example
     * User or Administrator).
     * 
     * @param userKey
     *            user key
     * @param userRoleKey
     *            user role key
     * @return list of units matching criteria
     */
    public List<POUserGroup> getUserGroupsForUserWithRole(long userKey,
            long userRoleKey);

    /**
     * Get all Units without default one, which are assigned to user with
     * specific role (for example User or Administrator).
     * 
     * @param userKey
     *            user key
     * @param userRoleKey
     *            user role key
     * @return list of units matching criteria
     */
    public List<POUserGroup> getUserGroupsForUserWithRoleWithoutDefault(
            long userKey, long userRoleKey);

    /**
     * This method is used to removing UNIT_ADMINISTRATOR role from the current
     * user when he loses user group. If user no longer can be subscription
     * owner, this method removes owner from his subscriptions.
     * 
     * @return true - if UNIT_ADMINISTRATOR role has been removed, false -
     *         otherwise
     */
    public boolean handleRemovingCurrentUserFromGroup();

    /**
     * get all user groups to which user is assigned and role that user has in
     * particular group without default group
     * 
     * @param userId
     * @param userKey
     * @return the list of user groups
     */
    List<POUserGroup> getUserGroupListForUserWithRolesWithoutDefault(
            String userId, long userKey);

    /**
     * get details of user group by key including details of users assigned to
     * this user group.
     * 
     * @param groupKey
     *            the key of user group
     * @return POUserGroup
     * @throws ObjectNotFoundException
     */
    public POUserGroup getUserGroupDetailsWithUsers(long groupKey)
            throws ObjectNotFoundException;

    /**
     * 
     * @param pagination
     *            - sorting, filtering, paging details
     * @param selectedGroupId
     *            - the key of user group
     * @return sorted and filtered list of users
     * @throws OrganizationAuthoritiesException
     */
    Response getUsersForGroup(PaginationUsersInUnit pagination,
            String selectedGroupId) throws OrganizationAuthoritiesException;

    /**
     * get number of users assigned to this user group.
     * 
     * @param selectedGroupId
     *            - the key of user group
     * @param pagination
     *            - sorting, filtering, paging details
     * @return number of users
     * @throws OrganizationAuthoritiesException
     */
    Integer getCountUsersForGroup(PaginationUsersInUnit pagination,
            String selectedGroupId) throws OrganizationAuthoritiesException;

    /**
     * get all invisible product keys with information about visibility for
     * specified user group.
     * 
     * @param groupKey
     * @return the list of invisible products for the given user group.
     */
    List<POUserGroupToInvisibleProduct> getInvisibleProducts(long userGroupKey);
}
