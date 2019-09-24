/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.identityservice.local;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.domobjects.*;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.VOUserDetails;

import javax.ejb.Local;

@Local
public interface IdentityServiceLocal {

    /**
     * Import user for the given user details. Import is executed in a separate
     * transaction.
     * 
     * @param user
     *            must contain id, email, roles and organization id
     * @param marketplaceId
     *            The marketplace to be included in the user registration mail
     * @throws ObjectNotFoundException
     *             If the given organization or marketplace does not exist
     */
    void importUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            ObjectNotFoundException;

    /**
     * Performs the concrete operation to create the user for the current
     * organization, after a required confirmation of a notification listener
     * has been retrieved. If none is required, it will be executed
     * synchronously.
     * 
     * @param tp
     *            The trigger process containing the detail information for the
     *            handling of the request.
     * @return the created user
     * @throws NonUniqueBusinessKeyException
     *             Thrown if the generation of a unique user ID fails
     * @throws MailOperationException
     *             Thrown if a password mail could not be sent.
     * @throws ValidationException
     *             Thrown if entered values (e. g. user ID) do not match the
     *             validation constraints
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     * 
     */
    VOUserDetails createUserInt(TriggerProcess tp)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException;

    /**
     * Retrieves the platform user matching the userId in the VOUser parameter
     * and returns the user details including the key information.
     * 
     * @param userId
     *            The user identifying attributes' representation.
     * @param validateOrganization
     *            <code>true</code> if the calling user must be part of the same
     *            organization as the requested user.
     * @return The platform user corresponding to the given identifying
     *         attributes.
     * @throws ObjectNotFoundException
     *             Thrown in case the user does not exist.
     * @throws OperationNotPermittedException
     *             Thrown in case the found platform user belongs to a different
     *             organization than the caller and the parameter
     *             validateOrganization is set to <code>true</code>.
     */
    PlatformUser getPlatformUser(String userId, boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Changes the status of the given platform user and sends the corresponding
     * mail to the user.
     * 
     * @param user
     *            The user for which the status is changed.
     * @param newStatus
     *            The account status to be set.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    void setUserAccountStatus(PlatformUser user, UserAccountStatus newStatus)
            throws OrganizationAuthoritiesException;

    /**
     * Removes a platform user. The user must not be registered for any
     * subscription. It is NOT checked whether the user is the last admin for
     * the organization or not.
     * 
     * @param user
     *            The user to be deleted.
     * @param marketplace
     *            The marketplace to get customized texts from
     * @throws UserDeletionConstraintException
     *             Thrown in case the user is still registered for one
     *             subscription.
     * @throws ObjectNotFoundException
     *             Thrown in case the given user does not exist.
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     */
    void deletePlatformUser(PlatformUser user, Marketplace marketplace)
            throws UserDeletionConstraintException, ObjectNotFoundException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Creates an administrative user for the organization and locks his account
     * with state {@link UserAccountStatus#LOCKED_NOT_CONFIRMED}. Furthermore
     * sends an email to the user in this case.
     * 
     * @param userDetails
     *            The information for the user to be created.
     * @param organization
     *            The organization the administrative user will be created for.
     * @param password
     *            The password to be set for the user. If it is
     *            <code>null</code>, a one-time password will be generated.
     * @param serviceKey
     *            The id of the service the user wants to subscribe. If this
     *            value is <code>null</code> it'll be ignored for further
     *            processing.
     * @param marketplace
     *            The context marketplace (id will be attached to the login URL
     *            that will be sent to the user) - optional
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case a user with the same business key already
     *             exists in the system.
     * @throws ObjectNotFoundException
     *             Thrown in case the organization cannot be found.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws MailOperationException
     *             Thrown if either a password or the confirmation mail couldn't
     *             be sent.
     */
    void createOrganizationAdmin(VOUserDetails userDetails,
            Organization organization, String password, Long serviceKey,
            Marketplace marketplace) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException, ValidationException,
            MailOperationException;

    PlatformUser getPlatformUser(String userId, String tenantKey,
        boolean validateOrganization) throws ObjectNotFoundException,
        OperationNotPermittedException;

    /**
     * Returns the administrative platform users that have never confirmed their
     * account in a time frame determined by the given current time and the
     * configuration setting for
     * {@link ConfigurationKey#PERMITTED_PERIOD_UNCONFIRMED_ORGANIZATIONS}.
     * 
     * @param currentTime
     *            The time to be considered for the list retrieval.
     */
    List<PlatformUser> getOverdueOrganizationAdmins(long currentTime);

    /**
     * Modify an existing user profile.
     * 
     * @param existingUser
     *            the user read from the database
     * @param tempUser
     *            a temporary domain object representing the 'to save' state
     * @param modifyOwnUser
     *            <code>true</code> if the calling user is only allowed to
     *            change its own user profile
     * @param sendMail
     *            if true an email is send otherwise not
     * @return the saved PlatformUser
     * @throws TechnicalServiceOperationException
     *             Thrown in case an error occurs on service side when modifying
     *             the user data
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case a service should be notified about a user data
     *             change but is not reachable
     * @throws OperationNotPermittedException
     *             Thrown in case a caller attempts to modify the user account
     *             details for another user.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case there already is a user with the given id for
     *             the organization.
     * @throws ValidationException
     * @throws ConcurrentModificationException
     */
    PlatformUser modifyUserData(PlatformUser existingUser,
            VOUserDetails updatedUser, boolean modifyOwnUser, boolean sendMail)
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ValidationException,
            ConcurrentModificationException;

    /**
     * Resets the password of the specified user, resets the failed login
     * counter to 0 and activates the account again.
     * 
     * @param user
     *            The user whose password has to be reset.
     * @param marketplace
     *            The marketplace to get customized texts from
     * @throws MailOperationException
     *             Thrown in case the user could not be notified about the new
     *             password.
     */
    void resetPasswordForUser(PlatformUser user, Marketplace marketplace)
            throws MailOperationException;

    /**
     * Determines those on-behalf users that are inactive for a longer time than
     * specified in the configuration setting
     * {@link ConfigurationKey#PERMITTED_PERIOD_INACTIVE_ON_BEHALF_USERS} and
     * removes them from the system.
     * 
     * @return <code>true</code> if the operation was executed successfully,
     *         <code>false</code> otherwise.
     */
    boolean removeInactiveOnBehalfUsers();

    /**
     * Determines those on-behalf users that are inactive for a longer time than
     * specified in the configuration setting
     * {@link ConfigurationKey#PERMITTED_PERIOD_INACTIVE_ON_BEHALF_USERS} and
     * removes them from the system.
     * 
     * @return <code>true</code> if the operation was executed successfully,
     *         <code>false</code> otherwise.
     */
    boolean removeInactiveOnBehalfUsersImpl();

    /**
     * Assigns the given roles to the specified user. If the user already has
     * these roles, the method has no effect. Different roles which are already
     * assigned to the user remain unchanged.
     * 
     * @param user
     *            the user to whom the roles are to be assigned
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
    void grantUserRoles(PlatformUser user, List<UserRoleType> roles)
            throws ObjectNotFoundException, OperationNotPermittedException,
            UserRoleAssignmentException;

    /**
     * Returns all non 'on behalf' users of the organization the calling user
     * belongs to.
     * 
     * @return the list of {@link PlatformUser}s
     */
    List<PlatformUser> getOrganizationUsers();

    /**
     * Checks which user roles can be assigned to the passed user - depending on
     * the organization roles its organization has assigned.
     * 
     * @param pu
     *            the {@link PlatformUser} to get the available roles for
     * @return the set of role types
     */
    Set<UserRoleType> getAvailableUserRolesForUser(PlatformUser pu);

    /**
     * Resets the password for the passed user (if no platform session is
     * active) and sends a corresponding mail (with the customized text from the
     * marketplace if so).
     * 
     * @param platformUser
     *            the user to reset the password for
     * @param marketplaceId
     *            the id of the marketplace to get potential text customizations
     *            from
     * @throws UserActiveException
     *             if the user has active platform sessions
     * @throws MailOperationException
     *             on errors while sending the mail
     */
    void resetUserPassword(PlatformUser platformUser, String marketplaceId)
            throws UserActiveException, MailOperationException;

    void deleteUser(PlatformUser pUser, String marketplaceId)
            throws OperationNotPermittedException,
            UserDeletionConstraintException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * verify the if the user is logged in
     * 
     * @param userKey
     *            key of user
     * @return
     */
    boolean isUserLoggedIn(long userKey);

    void sendUserUpdatedMail(PlatformUser existingUser, PlatformUser oldUser);

    void sendAdministratorNotifyMail(PlatformUser administrator, String userId);

    void notifySubscriptionsAboutUserUpdate(PlatformUser existingUser);

    void setUserRolesInt(Set<UserRoleType> roles, PlatformUser pUser)
            throws UserModificationConstraintException, UserActiveException,
            OperationNotPermittedException, UserRoleAssignmentException,
            ObjectNotFoundException;

    void verifyIdUniquenessAndLdapAttributes(PlatformUser existingUser,
            PlatformUser modUser) throws NonUniqueBusinessKeyException;

    void sendMailToCreatedUser(String password, boolean userLocalLdap,
            Marketplace marketplace, PlatformUser pu)
            throws MailOperationException;

    /**
     * Method creates platform user
     * 
     * @param user
     *            - details of user that has to be created
     * @param marketplaceId
     *            - marketplace identifier
     * @return created user details
     * 
     * @throws NonUniqueBusinessKeyException
     * @throws MailOperationException
     * @throws ValidationException
     * @throws UserRoleAssignmentException
     * @throws OperationPendingException
     */
    VOUserDetails createUser(VOUserDetails user, String marketplaceId)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException;

    /**
     * Method creates platform user and assigns user groups to him with given
     * roles.
     *
     * @param user
     *            - details of user that has to be created
     * @param roles
     *            - roles to which user will be assigned
     * @param marketplaceId
     *            - marketplace identifier
     * @param groups
     *            - user group(unit) and role mapping
     * @return created user details
     *
     * @throws NonUniqueBusinessKeyException
     * @throws MailOperationException
     * @throws ValidationException
     * @throws UserRoleAssignmentException
     * @throws OperationPendingException
     */
    VOUserDetails createUserWithGroups(VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId,
            Map<Long, UnitUserRole> userGroupKeyToRole)
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException;

    /**
     * This method is used to grant unit role to given user. Only organization
     * administrator is allowed to use this method.
     *
     * @param user
     *            - platform user
     * @param role
     *            - unit role which has to be granted
     */
    public void grantUnitRole(PlatformUser user, UserRoleType role);

    /**
     * This method is used to revoke unit role from given user. Only
     * organization administrator is allowed to use this method.
     *
     * @param user
     *            - platform user
     * @param role
     *            - unit role which has to be revoked
     * @throws UserModificationConstraintException
     */
    public void revokeUnitRole(PlatformUser user, UserRoleType role)
            throws UserModificationConstraintException;
    
    /**
     * Retrieves the platform user matching the userId and orgId in the VOUser parameter
     * and returns the user details including the key information.
     * 
     * @param userId
     *            The user identifying attributes' representation.
     * @param orgId
     *            The organization identifying attributes' representation.
     * @return The platform user corresponding to the given identifying
     *         attributes.
     * @throws ObjectNotFoundException
     *             Thrown in case the user does not exist.

     */
    PlatformUser getPlatformUserByOrganization(String userId, String orgId)
            throws ObjectNotFoundException;
    
    
    
    /**
     * @param tenantId the id of the used tenant
     * @param token the current berear token to access the OIDC provider 
     * @return if the synchronization was successful or not. 
     */
    default boolean synchronizeUsersWithOIDCProvider(String tenantId) {
        return false;
    }
}
