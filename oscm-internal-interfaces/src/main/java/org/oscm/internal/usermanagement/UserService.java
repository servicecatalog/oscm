/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.List;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationInt;

/**
 * @author weiser
 * 
 */
public interface UserService {

    /**
     * Gets the list of users for the calling users organization.
     * 
     * @return the list of users
     */
    List<POUser> getUsers();

    /**
     * Returns the user details for the passed user id
     * 
     * @param userId
     *            the user to get the details for
     * @return the user details
     * @throws SaaSApplicationException
     */
    POUserDetails getUserDetails(String userId, String tenantId) throws SaaSApplicationException;

    /**
     * Saves the user details and its role assignment. Also updates the user
     * data on subscriptions the user is assigned to
     * 
     * @param user
     *            the user to save
     * @return the response
     * @throws SaaSApplicationException
     */
    Response saveUser(POUserDetails user) throws SaaSApplicationException;

    /**
     * Resets the user password and the user status to
     * {@link UserAccountStatus#PASSWORD_MUST_BE_CHANGED}.
     * 
     * @param user
     *            the user to reset the password for
     * @param marketplaceId
     *            the marketplace context
     * @return the response
     * @throws SaaSApplicationException
     */
    Response resetUserPassword(POUser user, String marketplaceId)
            throws SaaSApplicationException;

    /**
     * Deletes the passed user. Will be successful if the user cannot be found
     * (already deleted).
     * 
     * @param user
     *            the user to delete
     * @param marketplaceId
     *            the marketplace context
     * @param tenantId
     *            the tenant context
     * @return the response
     * @throws SaaSApplicationException
     */
    Response deleteUser(POUser user, String marketplaceId, String tenantId)
            throws SaaSApplicationException;

    /**
     * Retrieves the date required for creating a new user. Initializes the user
     * locale to the one of the caller and sets the subscriptions including
     * service roles available for assignment as well as the available user
     * roles.
     * 
     * @return Object representing the user data, available roes and
     *         subscriptions for assignment
     */
    POUserAndSubscriptions getNewUserData();

    /**
     * Creates a new user for the calling organization - contains the basic user
     * data, the assigned roles and subscriptions with the selected service role
     * (if available). User creation and/or adding it to a subscription my be
     * suspended.
     * 
     * @param user
     *            the user to create with its roles and subscriptions to be
     *            assigned
     * @param marketplaceId
     *            the marketplace context
     * @return the {@link Response} - empty on success, {@link ReturnCode}
     *         contained on suspended action
     * @throws SaaSApplicationException
     */
    Response createNewUser(POUserAndSubscriptions user, String marketplaceId)
            throws SaaSApplicationException;

    /**
     * Returns the user details, in scope of given tenant, for the passed user id including assigned and
     * available user roles and assigned and available subscriptions (including
     * assigned and available service roles).
     * 
     * @param userId
     *            the user to get the details for
     * @param tenantId
     *            scope in which user should be found
     * @return the user details including assigned and available roles and
     *         subscriptions
     * @throws SaaSApplicationException
     */
    POUserAndSubscriptions getUserAndSubscriptionDetails(String userId, String tenantId)
            throws SaaSApplicationException;

    Response saveUserAndSubscriptionAssignment(POUserAndSubscriptions user,
            List<POUserGroup> allUserGroupsWithoutDefault)
                    throws SaaSApplicationException;

    /**
     * An organization administrator can import multiple users to its own
     * organization via .csv file import (UTF-8 encoding). The field separator
     * is the comma. One line contains the properties of a single user in the
     * following order:<br>
     * <br>
     * 
     * User ID (mandatory), Email (mandatory), Language, Locale (mandatory),
     * Title ("MR" or "MS"), First name, Last name, One or several user roles
     * <br>
     * <br>
     * 
     * As first and last name may contain a comma and multiple roles are also
     * separated by a comma (field delimiter), the data fields have to be put in
     * double quotes. Optional and not used fields have to be empty. Double
     * quotes inside a field (first and last name) have to be escaped by double
     * quotes.<br>
     * <br>
     * 
     * Sample for users to be imported to a technology provider organization:
     * <br>
     * <br>
     * 
     * "user1,user1@org.com,en,MR,"John","Doe","ORGANIZATION_ADMIN,
     * TECHNOLOGY_MANAGER"<br>
     * "user2,user2@org.com,en,,,,"TECHNOLOGY_MANAGER"<br>
     * "user3,user3@org.com,en,MR,,"Miller","TECHNOLOGY_MANAGER"
     * 
     * @param csvData
     *            comma separated text in UTF-8 encoding
     * @throws SaaSApplicationException
     *             if the given CSV data have syntax errors
     */
    Response importUsersInOwnOrganization(byte[] csvData, String marketplaceId)
            throws SaaSApplicationException;

    /**
     * The platform operator can import multiple users to given organization
     * organization or its own organization via .csv file import (UTF-8
     * encoding). The field separator is the comma. One line contains the
     * properties of a single user in the following order:<br>
     * <br>
     * 
     * User ID (mandatory), Email (mandatory), Language, Locale (mandatory),
     * Title ("MR" or "MS"), First name, Last name, One or several user roles
     * <br>
     * <br>
     * 
     * As first and last name may contain a comma and multiple roles are also
     * separated by a comma (field delimiter), the data fields have to be put in
     * double quotes. Optional and not used fields have to be empty. Double
     * quotes inside a field (first and last name) have to be escaped by double
     * quotes.<br>
     * <br>
     * 
     * Sample for users to be imported to a technology provider organization:
     * <br>
     * <br>
     * 
     * "user1,user1@org.com,en,MR,"John","Doe","ORGANIZATION_ADMIN,
     * TECHNOLOGY_MANAGER"<br>
     * "user2,user2@org.com,en,,,,"TECHNOLOGY_MANAGER"<br>
     * "user3,user3@org.com,en,MR,,"Miller","TECHNOLOGY_MANAGER"
     * 
     * @param csvData
     *            comma separated text in UTF-8 encoding
     * @throws SaaSApplicationException
     *             if the given CSV data have syntax errors
     */
    Response importUsers(byte[] users, String orgID, String marketplaceId)
            throws SaaSApplicationException;

    List<POSubscription> getUserAssignableSubscriptions(Pagination pagination,
            String userId) throws SaaSApplicationException;

    Long getUserAssignableSubscriptionsNumber(PaginationInt pagination,
            String userId, String tenantId) throws SaaSApplicationException;
}
