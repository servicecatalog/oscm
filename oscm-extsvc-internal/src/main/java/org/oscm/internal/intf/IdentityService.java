/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationRemovedException;
import org.oscm.internal.types.exception.SecurityCheckException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Remote interface of the identity management service.
 * 
 */
@Remote
public interface IdentityService {

    /**
     * An organization administrator can import multiple users to its own
     * organization via .csv file import (UTF-8 encoding). The field separator
     * is the comma. One line contains the properties of a single user in the
     * following order:<br>
     * <br>
     * 
     * User ID (mandatory), Email (mandatory), Language, Locale (mandatory),
     * Title ("MR" or "MS"), First name, Last name, One or several user roles<br>
     * <br>
     * 
     * As first and last name may contain a comma and multiple roles are also
     * separated by a comma (field delimiter), the data fields have to be put in
     * double quotes. Optional and not used fields have to be empty. Double
     * quotes inside a field (first and last name) have to be escaped by double
     * quotes.<br>
     * <br>
     * 
     * Sample for users to be imported to a technology provider organization:<br>
     * <br>
     * 
     * "user1,user1@org.com,en,MR,"John","Doe","ORGANIZATION_ADMIN,
     * TECHNOLOGY_MANAGER"<br>
     * "user2,user2@org.com,en,,,,"TECHNOLOGY_MANAGER"<br>
     * "user3,user3@org.com,en,MR,,"Miller","TECHNOLOGY_MANAGER"
     * 
     * @param csvData
     *            comma separated text in UTF-8 encoding
     * @param organizationId
     *            the organization of the users to be imported. Null if the
     *            organization of the current user is to be used.
     * @param marketplaceId
     *            The marketplace to be included in the user registration mail
     * @throws BulkUserImportException
     *             if the given CSV data have syntax errors
     * @throws ObjectNotFoundException
     *             if the given organization or marketplace does not exist
     * @throws OperationNotPermittedException
     *             if the current user is not PLATFORM_OPERATOR and tries to
     *             import users to external organization
     * @throws IllegalArgumentException
     */
    public void importUsersInOwnOrganization(byte[] csvData,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException;

    /**
     * The platform operator can import multiple users to given organization
     * organization or its own organization via .csv file import (UTF-8
     * encoding). The field separator is the comma. One line contains the
     * properties of a single user in the following order:<br>
     * <br>
     * 
     * User ID (mandatory), Email (mandatory), Language, Locale (mandatory),
     * Title ("MR" or "MS"), First name, Last name, One or several user roles<br>
     * <br>
     * 
     * As first and last name may contain a comma and multiple roles are also
     * separated by a comma (field delimiter), the data fields have to be put in
     * double quotes. Optional and not used fields have to be empty. Double
     * quotes inside a field (first and last name) have to be escaped by double
     * quotes.<br>
     * <br>
     * 
     * Sample for users to be imported to a technology provider organization:<br>
     * <br>
     * 
     * "user1,user1@org.com,en,MR,"John","Doe","ORGANIZATION_ADMIN,
     * TECHNOLOGY_MANAGER"<br>
     * "user2,user2@org.com,en,,,,"TECHNOLOGY_MANAGER"<br>
     * "user3,user3@org.com,en,MR,,"Miller","TECHNOLOGY_MANAGER"
     * 
     * @param csvData
     *            comma separated text in UTF-8 encoding
     * @param organizationId
     *            the organization of the users to be imported. Null if the
     *            organization of the current user is to be used.
     * @param marketplaceId
     *            The marketplace to be included in the user registration mail
     * @throws BulkUserImportException
     *             if the given CSV data have syntax errors
     * @throws ObjectNotFoundException
     *             if the given organization or marketplace does not exist
     * @throws OperationNotPermittedException
     *             if the current user is not PLATFORM_OPERATOR and tries to
     *             import users to external organization
     * @throws IllegalArgumentException
     */
    public void importUsers(byte[] csvData, String organizationId,
            String marketplaceId) throws BulkUserImportException,
            ObjectNotFoundException, IllegalArgumentException;

    /**
     * Informs the services on whether a login attempt of a user succeeded. If
     * the login fails three consecutive times, the user account is locked. A
     * successful login resets the counter for unsuccessful login attempts.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param user
     *            the user who performed the login attempt. The key information
     *            must be set.
     * @param attemptSuccessful
     *            <code>true</code> if the login attempt was successful,
     *            <code>false</code> otherwise
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws SecurityCheckException
     *             if the user account is locked
     * @throws ValidationException
     *             if the user's organization is configured for LDAP
     *             authentication and a mandatory LDAP parameter cannot be
     *             resolved
     */

    public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
            throws ObjectNotFoundException, SecurityCheckException,
            ValidationException;

    /**
     * Retrieves detailed information on the specified user.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object containing the data required for identifying
     *            the user: the user ID must be set, the numeric key is ignored
     * @return the user details
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     */

    public VOUserDetails getUserDetails(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Adds a user to the calling user's organization. The new user can then log
     * in to the platform.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param user
     *            the value object containing the data of the new user
     * @param roles
     *            the roles to be set for the new user
     * @param marketplaceId
     *            optionally, the ID of the relevant marketplace. The ID is
     *            attached to the login URL that is sent to the user. Customized
     *            texts are also retrieved from this marketplace; by default,
     *            the standard texts of the platform are used.
     * @return the saved user details, or <code>null</code> if the operation was
     *         suspended
     * @throws NonUniqueBusinessKeyException
     *             if the user key is not unique
     * @throws MailOperationException
     *             if the account confirmation mail with the initial password
     *             cannot be sent
     * @throws ValidationException
     *             if the validation of a value object fails
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     * @throws OperationPendingException
     *             if another request to create a user with the same user ID is
     *             pending
     */

    public VOUserDetails createUser(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException;

    /**
     * Changes the password of the calling user. For verification, the old
     * password must be provided in addition to the new one.
     * <p>
     * If the user account status was
     * {@link UserAccountStatus#PASSWORD_MUST_BE_CHANGED} before, it is set to
     * {@link UserAccountStatus#ACTIVE} after the password has been changed
     * successfully.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param oldPassword
     *            the user's old password
     * @param newPassword
     *            the user's new password
     * @throws SecurityCheckException
     *             if the old password is not correct
     * @throws ValidationException
     *             if the new password is longer than 100 bytes
     */

    public void changePassword(String oldPassword, String newPassword)
            throws SecurityCheckException, ValidationException;

    /**
     * Confirms the initial administrator account of an organization by changing
     * its status from {@link UserAccountStatus#LOCKED_NOT_CONFIRMED} to
     * {@link UserAccountStatus#ACTIVE}.
     * <p>
     * After successful confirmation, the user is able to log in to the platform
     * at the user interface. Note that a login via the Web service interface is
     * possible without a previous account confirmation.
     * <p>
     * The method also succeeds if the account status is already
     * <code>ACTIVE</code>. For other statuses, it fails.
     * <p>
     * Required role: none
     * 
     * @param user
     *            the value object specifying the user whose account is to be
     *            confirmed
     * @param marketplaceId
     *            optionally, the ID of the relevant marketplace. The ID is
     *            attached to the login URL that is sent to the user. Customized
     *            texts are also retrieved from this marketplace; by default,
     *            the standard texts of the platform are used.
     * @throws OperationNotPermittedException
     *             if the current status of the user account is not
     *             {@link UserAccountStatus#LOCKED_NOT_CONFIRMED} or
     *             {@link UserAccountStatus#ACTIVE}
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws MailOperationException
     *             if the confirmation mail with the organization context URL
     *             cannot be sent
     */

    public void confirmAccount(VOUser user, String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            MailOperationException;

    /**
     * Returns the roles that can be assigned to the specified user.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user for whom the roles are to
     *            be retrieved
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     * @return the list of user roles
     */

    public List<UserRoleType> getAvailableUserRoles(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Assigns the given roles to the specified user. If the user already has
     * these roles, the method has no effect. Different roles which are already
     * assigned to the user remain unchanged.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user to whom the roles are to
     *            be assigned
     * @param roles
     *            the roles to be set
     * @throws ObjectNotFoundException
     *             if a value object is not found
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     */

    public void grantUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException;

    /**
     * Assigns the given roles to the specified user and removes other roles, if
     * necessary. If the user already has the roles to be assigned, the method
     * has no effect.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user to whom the roles are to
     *            be assigned
     * @param roles
     *            the roles to be set
     * @throws ObjectNotFoundException
     *             if a value object is not found
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     * @throws UserActiveException
     *             if the specified user is not the calling user and is
     *             currently active, so that existing roles cannot be removed
     * @throws UserModificationConstraintException
     *             if the specified user is the last administrator of the
     *             organization and you try to remove the administrator role
     */

    public void setUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserModificationConstraintException, UserRoleAssignmentException,
            UserActiveException;

    /**
     * Removes the specified user from the calling user's organization. The user
     * must not be registered for any subscription or be the last administrator
     * of the organization.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user to be deleted
     * @param marketplaceId
     *            optionally, the ID of the marketplace from which to get
     *            customized texts. By default, the standard texts of the
     *            platform are used.
     * @throws UserDeletionConstraintException
     *             if the user is registered for a subscription, is the last
     *             administrator of the organization, or tries to delete himself
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws ConcurrentModificationException
     *             if the data stored for the given user or organization is
     *             changed by another user in the time between reading and
     *             writing it
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     */

    public void deleteUser(VOUser user, String marketplaceId)
            throws UserDeletionConstraintException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Removes the given roles from the specified user. Different roles assigned
     * to the user remain unchanged.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user from which the roles are
     *            to be removed
     * @param roles
     *            the roles to be removed
     * @throws ObjectNotFoundException
     * @throws UserModificationConstraintException
     *             if the specified user is the last administrator of the
     *             organization and you try to remove the administrator role
     * @throws UserActiveException
     *             if the specified user is not the calling user and is
     *             currently active, so that roles cannot be removed
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role deassignment
     */

    public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException,
            UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException;

    /**
     * Retrieves the users that are registered for the calling user's
     * organization, including detailed information on each user.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the list of users
     */

    public List<VOUserDetails> getUsersForOrganization();

    /**
     * Locks the account of the given user by setting the specified status.
     * <p>
     * The account status to be set must be one of the <code>LOCKED*</code>
     * statuses.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user whose account is to be
     *            locked
     * @param newStatus
     *            the account status to be set; must be one of the
     *            <code>LOCKED*</code> statuses
     * @param marketplaceId
     *            optionally, the ID of the marketplace from which to get
     *            customized texts. By default, the standard texts of the
     *            platform are used.
     * @throws OperationNotPermittedException
     *             if the specified account status is
     *             {@link UserAccountStatus#ACTIVE} or
     *             {@link UserAccountStatus#PASSWORD_MUST_BE_CHANGED}, or if the
     *             specified user is not a member of the calling user's
     *             organization
     * @throws ObjectNotFoundException
     * @throws ConcurrentModificationException
     *             if the data stored for the given user is changed by another
     *             user in the time between reading and writing it
     */

    public void lockUserAccount(VOUser user, UserAccountStatus newStatus,
            String marketplaceId) throws OperationNotPermittedException,
            ObjectNotFoundException, ConcurrentModificationException;

    /**
     * Updates the data stored for the calling user, except for
     * <ul>
     * <li>the key information and</li>
     * <li>the user role.</li>
     * </ul>
     * Required role: any user role in an organization
     * 
     * @param user
     *            the value object specifying the data to be set
     * @return the updated user details, including new version information
     * @throws OperationNotPermittedException
     *             if the caller attempts to modify the data of another user
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws ValidationException
     *             if the validation of a value object fails
     * @throws NonUniqueBusinessKeyException
     *             if the user key is not unique
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     * @throws ConcurrentModificationException
     *             if the data stored for the given user is changed by another
     *             user in the time between reading and writing it
     */

    public VOUserDetails updateUser(VOUserDetails user)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, NonUniqueBusinessKeyException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ConcurrentModificationException;

    /**
     * Requests the generation of a new one-time password for the specified
     * user. The password is sent to the user by mail. The status of the user
     * account is set to {@link UserAccountStatus#PASSWORD_MUST_BE_CHANGED},
     * indicating that the password must be changed at the next login.
     * <p>
     * If the user account was locked, the lock is not removed, but the password
     * is reset nevertheless.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user whose password is to be
     *            reset
     * @param marketplaceId
     *            optionally, the ID of the marketplace from which to get
     *            customized texts. By default, the standard texts of the
     *            platform are used.
     * @throws MailOperationException
     *             if the mail with the new one-time password cannot be sent to
     *             the user
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws UserActiveException
     *             if the user is currently active so that the password cannot
     *             be reset
     * @throws ConcurrentModificationException
     *             if the data stored for the given user is changed by another
     *             user in the time between reading and writing it
     */

    public void requestResetOfUserPassword(VOUser user, String marketplaceId)
            throws MailOperationException, ObjectNotFoundException,
            OperationNotPermittedException, UserActiveException,
            ConcurrentModificationException;

    /**
     * Unlocks the account of the given user and sets its status to
     * {@link UserAccountStatus#ACTIVE}. The counter for unsuccessful login
     * attempts is reset.
     * <p>
     * For the method to execute successfully, the current account status must
     * be one of the <code>LOCKED*</code> statuses.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the value object specifying the user whose account is to be
     *            unlocked
     * @param marketplaceId
     *            optionally, the ID of the marketplace from which to get
     *            customized texts. By default, the standard texts of the
     *            platform are used.
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions,
     *             or if the current account status is
     *             {@link UserAccountStatus#ACTIVE} or
     *             {@link UserAccountStatus#PASSWORD_MUST_BE_CHANGED}
     * @throws ConcurrentModificationException
     *             if the data stored for the given user is changed by another
     *             user in the time between reading and writing it
     */

    public void unlockUserAccount(VOUser user, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException;

    /**
     * Retrieves the user with the ID specified in the <code>VOUser</code>
     * parameter and returns a <code>VOUser</code> object that contains the key
     * information and the account status.
     * <p>
     * Required role: none
     * 
     * @param user
     *            the value object containing the data required to identify the
     *            user
     * @return the value object with the key information and account status
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     * @throws OrganizationRemovedException
     *             if the user's organization has been deleted
     */

    public VOUser getUser(VOUser user) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException;

    /**
     * Returns detailed information on the calling user; the user must be logged
     * in for the method to be successful.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the user details
     */

    public VOUserDetails getCurrentUserDetails();

    /**
     * Returns detailed information on the calling user; no exception is thrown
     * if the user is not logged in.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the user details
     */

    public VOUserDetails getCurrentUserDetailsIfPresent();

    /**
     * Creates a list of organization ID and user ID tuples for the specified
     * email address and sends this list to the email address.
     * 
     * @param email
     *            the email address
     * @param marketplaceId
     *            optionally, the ID of the marketplace from which to get
     *            customized texts. By default, the standard texts of the
     *            platform are used.
     * @throws ValidationException
     *             if the validation in the service layer fails
     * @throws MailOperationException
     *             if the mail cannot be sent
     */

    public void sendAccounts(String email, String marketplaceId)
            throws ValidationException, MailOperationException;

    /**
     * Searches the configured remote LDAP system for users whose ID matches the
     * given user ID pattern.
     * <p>
     * Required role: none
     * 
     * @param userIdPattern
     *            the user ID pattern for the users to be found
     * @return the users found in the LDAP system, including detailed
     *         information on each user
     * @throws ValidationException
     *             if a mandatory LDAP parameter cannot be resolved
     */

    public List<VOUserDetails> searchLdapUsers(String userIdPattern)
            throws ValidationException;

    /**
     * Creates a user in the calling user's organization for each of the given
     * value objects for which an entry exists in the configured remote LDAP
     * system.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param users
     *            the list of users to be imported
     * @param marketplaceId
     *            optionally, the ID of the relevant marketplace. The ID is
     *            attached to the login URL that is sent to the users.
     *            Customized texts are also retrieved from this marketplace; by
     *            default, the standard texts of the platform are used.
     * @throws NonUniqueBusinessKeyException
     *             if there is already a platform user whose ID is identical to
     *             the ID of a user in the list
     * @throws ValidationException
     *             if the user validation fails, if no LDAP entry exists, if
     *             multiple LDAP entries are found for a user in the list, or if
     *             a mandatory LDAP parameter cannot be resolved
     * @throws MailOperationException
     *             if the mail with the access information cannot be sent to at
     *             least one of the new users
     */

    public void importLdapUsers(List<VOUserDetails> users, String marketplaceId)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException;

    /**
     * Creates a user with the given password in the specified organization. An
     * artificial user ID is generated. The user account status is set to
     * active, the user role to administrator. All the remaining account data is
     * copied from the calling user.
     * <p>
     * The specified organization must be a customer of the calling user's
     * organization, which must have both the technology provider and supplier
     * role. Additionally, the customer organization must have allowed the
     * calling user's organization to log in on its behalf. This is achieved via
     * a subscription whose underlying technical service has the
     * <code>allowingOnBehalfActing</code> flag set.
     * <p>
     * Required role: service manager of a supplier organization or technology
     * manager of a technology provider organization
     * 
     * @param organizationId
     *            the ID of the organization to create the user for
     * @param password
     *            the password of the new user, For the SAML_SP authentication
     *            mode, the password parameter is the userId of the new user.
     * @return the details of the new user
     * @throws ObjectNotFoundException
     *             if the specified organization is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions,
     *             i.e. the specified organization is not a customer of the
     *             calling user's organization
     * @throws NonUniqueBusinessKeyException
     *             if the generated user ID is already in use
     */

    public VOUserDetails createOnBehalfUser(String organizationId,
            String password) throws ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException;

    /**
     * Deletes the current user if it is an on-behalf user. If the current user
     * is a regular user, the method has no effect.
     * <p>
     * Required role: any user role in an organization
     */

    public void cleanUpCurrentUser();

    /**
     * Updates the user attributes of the calling user according to the same
     * attributes in the remote LDAP system that is configured for the user's
     * organization. If the calling user's organization is not configured to use
     * a remote LDAP system, the method has no effect.
     * <p>
     * Required role: any user role in an organization
     * 
     * @throws ValidationException
     *             if not all mandatory LDAP parameters can be resolved for the
     *             underlying LDAP managed organization
     */

    public void refreshLdapUser() throws ValidationException;

    /**
     * Check if the user has admin role in the session context.
     * <p>
     * Required role: any user role in an organization
     * 
     */
    public boolean isCallerOrganizationAdmin();

    /**
     * Assigns users to an organizational unit (user group) or removes users
     * from an organizational unit.
     * 
     * <p>
     * Required role: administrator of the organization
     * 
     * @param unitName
     *            the name of the organizational unit to which to assign users
     *            or from which to remove users
     * @param usersToBeAdded
     *            the users which are to be assigned to the user group
     * @param usersToBeRevoked
     *            the users which are to be removed from the user group
     * @return true: the operation was completed
     * @throws ObjectNotFoundException
     *             if a specific domain object is not found although its
     *             existence is presumed
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions,
     *             the context is invalid, or the method is called for removing
     *             users from the default organizational unit
     * @throws MailOperationException
     *             if there are problems in sending mails to the users who are
     *             assigned to or removed from the organizational unit
     * @throws NonUniqueBusinessKeyException
     *             if a user key is not unique
     * 
     */
    public boolean addRevokeUserUnitAssignment(String unitName,
            List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException;

    /**
     * Searches the configured remote LDAP system for users if more than search
     * limit value.
     * <p>
     * Required role: none
     * 
     * @param userIdPattern
     *            the user ID pattern for the users to be found
     * @return the users found in the LDAP system if more than search limit
     *         value
     * @throws ValidationException
     *             if a mandatory LDAP parameter cannot be resolved
     */
    boolean searchLdapUsersOverLimit(String userIdPattern)
            throws ValidationException;

    /**
     * Assigns the given role to the specified user. If the user already has
     * this role or the given role is not related to administrator's units, the method has no effect.
     * <p>
     * Required role: administrator of the user's organization or unit administrator
     * 
     * @param user
     *            the value object specifying the user to whom the role is to
     *            be assigned
     * @param role
     *            the role to be set
     * @throws ObjectNotFoundException
     *             if a value object is not found
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     */
    void grantUnitRole(VOUser user, UserRoleType role)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Removes the given role from the specified user. If the user already has
     * this role or the given role is not related to administrator's units, the method has no effect.
     * <p>
     * Required role: administrator of the user's organization or unit administrator
     * 
     * @param user
     *            the value object specifying the user from which the roles are
     *            to be removed
     * @param role
     *            the role to be removed
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     *             if the specified user is not a member of the calling user's
     *             organization
     */
    void revokeUnitRole(VOUser user, UserRoleType role)
            throws ObjectNotFoundException, OperationNotPermittedException;
}
