/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.enumtypes.UserAccountStatus;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.types.exceptions.SecurityCheckException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.UserActiveException;
import org.oscm.types.exceptions.UserDeletionConstraintException;
import org.oscm.types.exceptions.UserModificationConstraintException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Remote interface of the identity management service.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface IdentityService {
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
    @WebMethod
    public void notifyOnLoginAttempt(@WebParam(name = "user") VOUser user,
            @WebParam(name = "attemptSuccessful") boolean attemptSuccessful)
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
    @WebMethod
    public VOUserDetails getUserDetails(@WebParam(name = "user") VOUser user)
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
    @WebMethod
    public VOUserDetails createUser(
            @WebParam(name = "user") VOUserDetails user,
            @WebParam(name = "roles") List<UserRoleType> roles,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public void changePassword(
            @WebParam(name = "oldPassword") String oldPassword,
            @WebParam(name = "newPassword") String newPassword)
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
    @WebMethod
    public void confirmAccount(@WebParam(name = "user") VOUser user,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public List<UserRoleType> getAvailableUserRoles(
            @WebParam(name = "user") VOUser user)
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
    @WebMethod
    public void grantUserRoles(@WebParam(name = "user") VOUser user,
            @WebParam(name = "roles") List<UserRoleType> roles)
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
    @WebMethod
    public void setUserRoles(@WebParam(name = "user") VOUser user,
            @WebParam(name = "roles") List<UserRoleType> roles)
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
     */
    @WebMethod
    public void deleteUser(@WebParam(name = "user") VOUser user,
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws UserDeletionConstraintException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException;

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
    @WebMethod
    public void revokeUserRoles(@WebParam(name = "user") VOUser user,
            @WebParam(name = "roles") List<UserRoleType> roles)
            throws ObjectNotFoundException,
            UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException;

    /**
     * Retrieves the users that are registered for the calling user's
     * organization, including detailed information on each user.
     * <p>
     * Required role: administrator or subscription manager of the organization
     * 
     * @return the list of users
     */
    @WebMethod
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
    @WebMethod
    public void lockUserAccount(@WebParam(name = "user") VOUser user,
            @WebParam(name = "newStatus") UserAccountStatus newStatus,
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException;

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
    @WebMethod
    public VOUserDetails updateUser(@WebParam(name = "user") VOUserDetails user)
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
    @WebMethod
    public void requestResetOfUserPassword(
            @WebParam(name = "user") VOUser user,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public void unlockUserAccount(@WebParam(name = "user") VOUser user,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public VOUser getUser(@WebParam(name = "user") VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationRemovedException;

    /**
     * Returns detailed information on the calling user; the user must be logged
     * in for the method to be successful.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the user details
     */
    @WebMethod
    public VOUserDetails getCurrentUserDetails();

    /**
     * Returns detailed information on the calling user; no exception is thrown
     * if the user is not logged in.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the user details
     */
    @WebMethod
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
    @WebMethod
    public void sendAccounts(@WebParam(name = "email") String email,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public List<VOUserDetails> searchLdapUsers(
            @WebParam(name = "userIdPattern") String userIdPattern)
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
    @WebMethod
    public void importLdapUsers(
            @WebParam(name = "users") List<VOUserDetails> users,
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException;

    /**
     * Creates a user with the given ID (SAML_SP authentication mode) or with an
     * artificial ID and the given password (INTERNAL authentication mode) in
     * the specified organization. The user account status is set to active, the
     * user role to administrator. All the remaining account data is copied from
     * the calling user.
     * <p>
     * The specified organization must:
     * <ul>
     * <li>be a customer of the calling user's organization, which in turn must
     * have both the technology provider and supplier role.
     * <li>not be configured for LDAP-based user management.
     * <li>have allowed the calling user's organization to log in on its behalf.
     * This is achieved via a subscription whose underlying technical service
     * has the <code>allowingOnBehalfActing</code> flag set.
     * </ul>
     * <p>
     * Required role: service manager of a supplier organization or technology
     * manager of a technology provider organization
     * 
     * @param organizationId
     *            the ID of the organization to create the user for
     * @param password
     *            with INTERNAL authentication mode: the password of the new
     *            user; with SAML_SP authentication mode: the user ID of the new
     *            user
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
    @WebMethod
    public VOUserDetails createOnBehalfUser(
            @WebParam(name = "organizationId") String organizationId,
            @WebParam(name = "password") String password)
            throws ObjectNotFoundException, OperationNotPermittedException,
            NonUniqueBusinessKeyException;

    /**
     * Deletes the current user if it is an on-behalf user. If the current user
     * is a regular user, the method has no effect.
     * <p>
     * Required role: any user role in an organization
     */
    @WebMethod
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
    @WebMethod
    public void refreshLdapUser() throws ValidationException;

    /**
     * Imports and registers one or more users in the calling user's
     * organization based on the given data in CSV (comma-separated values)
     * format.
     * <p>
     * The user data must be provided in a file in CSV format and UTF-8
     * encoding. The data for one user must be given in one line, empty lines
     * are ignored. The individual data fields in a line must be separated by a
     * comma each. If an optional field does not contain any data, it must be
     * empty and separated by a comma from the next field.
     * <p>
     * The user data fields must be provided in the following sequence: <br>
     * user ID (mandatory) <br>
     * email address (mandatory) <br>
     * language (mandatory) <br>
     * title (optional; <code>MR</code> or <code>MS</code>) <br>
     * first name (optional) <br>
     * last name (optional) <br>
     * one or more user roles, separated by a comma (optional;
     * <code>ORGANIZATION_ADMIN</code>, <code>SUBSCRIPTION_MANAGER</code>,
     * <code>PLATFORM_OPERATOR</code>, <code>MARKETPLACE_OWNER</code>,
     * <code>SERVICE_MANAGER</code>, <code>TECHNOLOGY_MANAGER</code>,
     * <code>BROKER_MANAGER</code>, <code>RESELLER_MANAGER</code>)
     * <p>
     * The data in the first name, last name, and user role fields must be
     * enclosed in double quotes ("). Double quotes in a field (first and last
     * name) have to be escaped by double quotes.
     * <p>
     * <b>Example:</b> Users to be imported into a technology provider
     * organization:<br>
     * <code>user1,user1@org.com,en,MR,"John","Doe","ORGANIZATION_ADMIN,
     * TECHNOLOGY_MANAGER"<br>
     * user2,user2@org.com,en,,,,"TECHNOLOGY_MANAGER"<br>
     * user3,user3@org.com,en,MR,,"Miller","TECHNOLOGY_MANAGER"</code>
     * <p>
     * Required role: administrator of the organization
     * 
     * @param csvData
     *            a byte array with the comma-separated data in UTF-8 encoding
     * @param marketplaceId
     *            the identifier of the marketplace to be included in the
     *            registration mail sent to each user
     * @throws BulkUserImportException
     *             if the CSV data have syntax errors
     * @throws ObjectNotFoundException
     *             if the given marketplace is not found
     */
    @WebMethod
    public void importUsersInOwnOrganization(
            @WebParam(name = "csvData") byte[] csvData,
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws BulkUserImportException, ObjectNotFoundException;

    /**
     * Imports and registers one or more users in the specified organization
     * based on the given data in CSV (comma-separated values) format.
     * <p>
     * The user data must be provided in a file in CSV format and UTF-8
     * encoding. The data for one user must be given in one line, empty lines
     * are ignored. The individual data fields in a line must be separated by a
     * comma each. If an optional field does not contain any data, it must be
     * empty and separated by a comma from the next field.
     * <p>
     * The user data fields must be provided in the following sequence: <br>
     * user ID (mandatory) <br>
     * email address (mandatory) <br>
     * language (mandatory) <br>
     * title (optional; <code>MR</code> or <code>MS</code>) <br>
     * first name (optional) <br>
     * last name (optional) <br>
     * one or more user roles, separated by a comma (optional;
     * <code>ORGANIZATION_ADMIN</code>, <code>SUBSCRIPTION_MANAGER</code>,
     * <code>PLATFORM_OPERATOR</code>, <code>MARKETPLACE_OWNER</code>,
     * <code>SERVICE_MANAGER</code>, <code>TECHNOLOGY_MANAGER</code>,
     * <code>BROKER_MANAGER</code>, <code>RESELLER_MANAGER</code>)
     * <p>
     * The data in the first name, last name, and user role fields must be
     * enclosed in double quotes ("). Double quotes in a field (first and last
     * name) have to be escaped by double quotes.
     * <p>
     * <b>Example:</b> Users to be imported into a technology provider
     * organization:<br>
     * <code>user1,user1@org.com,en,MR,"John","Doe","ORGANIZATION_ADMIN,
     * TECHNOLOGY_MANAGER"<br>
     * user2,user2@org.com,en,,,,"TECHNOLOGY_MANAGER"<br>
     * user3,user3@org.com,en,MR,,"Miller","TECHNOLOGY_MANAGER"</code>
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @param csvData
     *            a byte array with the comma-separated data in UTF-8 encoding
     * @param organizationId
     *            the identifier of the organization into which the users are to
     *            be imported, or <code>null</code> for the calling user's
     *            organization
     * @param marketplaceId
     *            the identifier of the marketplace to be included in the
     *            registration mail sent to each user
     * @throws BulkUserImportException
     *             if the CSV data have syntax errors
     * @throws ObjectNotFoundException
     *             if the given organization or marketplace is not found
     */
    @WebMethod
    public void importUsers(@WebParam(name = "csvData") byte[] csvData,
            @WebParam(name = "organizationId") String organizationId,
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws BulkUserImportException, ObjectNotFoundException;

    /**
     * @deprecated Replaced by {@link #addRevokeUserUnitAssignment()}.
     */
    @WebMethod
    @Deprecated
    public boolean addRevokeUserGroupAssignment(
            @WebParam(name = "groupName") String groupName,
            @WebParam(name = "usersToBeAdded") List<VOUser> usersToBeAdded,
            @WebParam(name = "usersToBeRevoked") List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException,
            ConcurrentModificationException;

    /**
     * Assigns users to an organizational unit or removes users from an
     * organizational unit.
     * 
     * <p>
     * Required role: administrator of the organization
     * 
     * @param unitName
     *            the name of the organizational unit to which to assign users
     *            or from which to remove users
     * @param usersToBeAdded
     *            the users which are to be assigned to the organizational unit
     * @param usersToBeRevoked
     *            the users which are to be removed from the organizational unit
     * @return true: the operation was completed
     * @throws ObjectNotFoundException
     *             if a specific domain object is not found although its
     *             existence is presumed
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions,
     *             the context is invalid, or the method is called for removing
     *             users from the default user group
     * @throws MailOperationException
     *             if there are problems in sending mails to the users who are
     *             assigned to or removed from the organizational unit
     * @throws NonUniqueBusinessKeyException
     *             if a user key is not unique
     */
    @WebMethod
    public boolean addRevokeUserUnitAssignment(
            @WebParam(name = "unitName") String unitName,
            @WebParam(name = "usersToBeAdded") List<VOUser> usersToBeAdded,
            @WebParam(name = "usersToBeRevoked") List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException;
}
