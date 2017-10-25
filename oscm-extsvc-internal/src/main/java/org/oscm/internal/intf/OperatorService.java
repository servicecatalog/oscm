/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 02.10.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.AuditLogTooManyRowsException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.DuplicateTenantIdException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.PSPIdentifierForSellerException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;

/**
 * Service providing the functionality required by an operator of the
 * application, e.g. including the creation of additional organizations that
 * cannot utilize the self-care.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Remote
public interface OperatorService {

    /**
     * Creates an organization with the given details as well as its initial
     * administrative user. The new organization will be granted the authorities
     * as requested.
     * 
     * @param organization
     *            the basic data of the organization to be created
     * @param imageResource
     *            the logo image of the organization
     * @param orgInitialUser
     *            the initial administrative user for the organization
     * @param organizationProperties
     *            additional optional organization properties (e.g. remote LDAP
     *            properties) that are to be stored in the
     *            <code>OrganizationSetting</code> table, or <code>null</code>
     *            if there are no such data
     * @param rolesToGrant
     *            the roles the new organization will be granted. Can be
     *            {@link OrganizationRoleType#SUPPLIER} or
     *            {@link OrganizationRoleType#TECHNOLOGY_PROVIDER} or
     *            {@link OrganizationRoleType#BROKER} or
     *            {@link OrganizationRoleType#RESELLER}.
     * @return the newly created organization
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case a user with the same identifier already
     *             exists.
     * @throws ValidationException
     *             Thrown in case the new organization viaolates a validation
     *             constraint.
     * @throws OrganizationAuthorityException
     *             Thrown in case the roles that the new organization is to be
     *             granted are not valid.
     * @throws IncompatibleRolesException
     *             Thrown in case the roles that the new organization is to be
     *             granted are not compatible with each other.
     * @throws MailOperationException
     *             Thrown if a password mail could not be sent.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     * @throws ImageException
     *             Thrown if the <code>imageResource</code> is not
     *             <code>null</code> and the <code>organization</code> has no
     *             <code>SUPPLIER</code> or <code>TECHNOLOGY_PROVIDER</code>
     *             role.
     */
    VOOrganization registerOrganization(VOOrganization organization,
            VOImageResource imageResource, VOUserDetails orgInitialUser,
            LdapProperties organizationProperties, String marketplaceID,
            OrganizationRoleType... rolesToGrant)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            ValidationException, OrganizationAuthorityException,
            IncompatibleRolesException, MailOperationException,
            OrganizationAuthoritiesException, ImageException;

    /**
     * Determines the organization belonging to the given organization ID and
     * grants it the specified role, if it does not already have it. If the
     * organization is both supplier and technology provider, for each technical
     * service a marketing permission is created.
     * 
     * @param organizationId
     *            the identifier of the organization
     * @param role
     *            the role to be granted
     * @throws OrganizationAuthorityException
     *             Thrown in case the specified role is not
     *             {@link OrganizationRoleType#SUPPLIER} or
     *             {@link OrganizationRoleType#TECHNOLOGY_PROVIDER} or
     *             {@link OrganizationRoleType#BROKER} or
     *             {@link OrganizationRoleType#RESELLER}.
     * @throws IncompatibleRolesException
     *             Thrown in case the specified role is incompatible with the
     *             existing roles of the organization.
     * @throws ObjectNotFoundException
     *             Thrown in case there is no organization with the given
     *             identifier.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     * @throws AddMarketingPermissionException
     *             If the supplier could not be loaded or the organization does
     *             not have the supplier role.
     */
    void addOrganizationToRole(String organizationId, OrganizationRoleType role)
            throws OrganizationAuthorityException, IncompatibleRolesException,
            ObjectNotFoundException, OrganizationAuthoritiesException,
            AddMarketingPermissionException;

    /**
     * Enables the given payment types for the supplier so that the supplier can
     * offer them to its customers. The customer can only enter payment
     * information for enabled types.
     * 
     * @param supplier
     *            the supplier to enable the payment types for
     * @param types
     *            the payment types to enable
     * @throws ObjectNotFoundException
     *             Thrown in case the supplier organization was not found.
     * @throws OrganizationAuthorityException
     *             Thrown in case the provided organization does not have the
     *             supplier role.
     * @throws PSPIdentifierForSellerException
     *             Thrown in case the supplier does not have a PSP identifier
     *             setting.
     * @throws PaymentDataException
     *             Thrown in case the payment type is unknown. Holds the
     *             information about valid payment types.
     */
    void addAvailablePaymentTypes(VOOrganization supplier, Set<String> types)
            throws ObjectNotFoundException, OrganizationAuthorityException,
            PSPIdentifierForSellerException, PaymentDataException;

    /**
     * Determines all payment processing attempts that failed and that are
     * marked to be retried. Then the payment processing is restarted.
     * 
     * @return <code>true</code> in case all determined payment processing
     *         attempts were completed successfully, <code>false</code>
     *         otherwise.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    boolean retryFailedPaymentProcesses()
            throws OrganizationAuthoritiesException;

    /**
     * Determines all currently registered timers set at the active cluster node
     * and returns their expiration dates.
     * 
     * @return the timer names and expiration dates, one entry for every timer
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    List<VOTimerInfo> getTimerExpirationInformation()
            throws OrganizationAuthoritiesException;

    /**
     * Re-initiates all timers at the active cluster node and returns their
     * expiration dates. The currently registered timers will be removed.
     * 
     * @return the list of re-initialized timers and their expiration dates
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     * @throws ValidationException
     *             Thrown in case the next expiration date negative.
     */
    List<VOTimerInfo> reInitTimers()
            throws OrganizationAuthoritiesException, ValidationException;

    /**
     * Invokes the billing process for all customers for the last billing period
     * and invokes the payment process. Already handled customers will be
     * ignored. This method works Similar to the timer-triggered billing
     * handling.
     * 
     * <p>
     * <b>NOTE:</b> This is a synchronous call and thus might take some time
     * until it is completely executed.
     * </p>
     * 
     * @return <code>true</code> in case the billing passed without any problem
     *         (including PSP invocation), <code>false</code> otherwise.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    boolean startBillingRun() throws OrganizationAuthoritiesException;

    /**
     * Changes the status of the given user and sends the corresponding mail.
     * 
     * @param user
     *            the user for which the status is changed
     * @param newStatus
     *            the account status to be set
     * @throws ObjectNotFoundException
     *             Thrown in case the user was not found.
     * @throws ValidationException
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    void setUserAccountStatus(VOUser user, UserAccountStatus newStatus)
            throws ObjectNotFoundException, ValidationException,
            OrganizationAuthoritiesException;

    /**
     * Sets the distinguished name for an organization to allow secure
     * communication between clients and BES.
     * 
     * @param organizationId
     *            the ID of the organization
     * @param distinguishedName
     *            the distinguished name to be set for secure communication
     * @throws ObjectNotFoundException
     *             Thrown in case the organization was not found.
     * @throws DistinguishedNameException
     *             Thrown in case the provided distinguished name is used by
     *             another organization.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     * @throws ValidationException
     *             Thrown in case the distinguished name contains more than 4096
     *             characters.
     */
    void setDistinguishedName(String organizationId, String distinguishedName)
            throws ObjectNotFoundException, DistinguishedNameException,
            OrganizationAuthoritiesException, ValidationException;

    /**
     * Exports the billing data of the provided organization for the specified
     * time frame in the saved raw XML format.
     * 
     * @param from
     *            Specifies the start of the time frame for the billing data.
     *            Can be <code>null</code> if no start limit is wanted.
     * @param to
     *            Specifies the end of the time frame for the billing data. Can
     *            be <code>null</code> if no end limit is wanted.
     * @param organizationId
     *            The id of the organization to export the billing data for.
     * @return The billing data xml as byte array.
     * @throws ObjectNotFoundException
     *             Thrown in case the organization was not found.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    byte[] getOrganizationBillingData(long from, long to, String organizationId)
            throws ObjectNotFoundException, OrganizationAuthoritiesException;

    /**
     * Resets the password of a specified user and sends an email with the new
     * password to him. Furthermore the account will be unlocked, if it was not
     * especially locked by an operator.
     * 
     * @param userId
     *            The identifier of the user whose password should be reset.
     * @throws ObjectNotFoundException
     *             Thrown in case the organization or the user do not exist.
     * @throws MailOperationException
     *             Thrown in case the user could not be notified about the
     *             modified password.
     * @throws OperationNotPermittedException
     *             Thrown in case the user is managed by an external LDAP system
     *             and the password cannot be reset for this reason.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller does not have the platform operator
     *             role.
     */
    void resetPasswordForUser(String userId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, OrganizationAuthoritiesException;

    /**
     * Adds a new currency to the system that should be supported in price
     * models. If the specified currency does already exist, no changes will be
     * performed.
     * 
     * @param currencyISOCode
     *            The ISO 4217 code representation of the currency to be added.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     * @throws ValidationException
     *             Thrown in case the provided parameter value is not a valid
     *             ISO currency code.
     */
    void addCurrency(String currencyISOCode)
            throws OrganizationAuthoritiesException, ValidationException;

    /**
     * Retrieves the list of all current configuration settings.
     * 
     * @return The current configuration settings.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     */
    List<VOConfigurationSetting> getConfigurationSettings()
            throws OrganizationAuthoritiesException;

    /**
     * Saves the specified configuration setting. If a setting with the same key
     * already exists, it will be updated.
     * 
     * @param setting
     *            The configuration setting to be saved.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     * @throws ValidationException
     *             Thrown in case the wrong value is set to the specific
     *             configuration setting.
     * @throws ConcurrentModificationException
     *             Thrown if the object was changed concurrently
     */
    void saveConfigurationSetting(VOConfigurationSetting setting)
            throws OrganizationAuthoritiesException, ValidationException,
            ConcurrentModificationException, DuplicateTenantIdException;

    /**
     * Saves the specified configuration settings. If a setting with the same
     * key already exists, it will be updated.
     * 
     * @param settings
     *            The configuration settings to be saved.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     * @throws ValidationException
     *             Thrown in case the wrong value is set to the specific
     *             configuration setting.
     * @throws ConcurrentModificationException
     *             Thrown if the object was changed concurrently
     */
    void saveConfigurationSettings(List<VOConfigurationSetting> settings)
            throws OrganizationAuthoritiesException, ValidationException,
            ConcurrentModificationException, DuplicateTenantIdException;

    /**
     * Retrieves the organization with the given identifier.
     * 
     * @param organizationId
     *            The identifier of the organization the retrieve.
     * @return The organization value object for the given identifier.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     * @throws ObjectNotFoundException
     *             Thrown in case no organization can be found for the
     *             identifier.
     */
    VOOperatorOrganization getOrganization(String organizationId)
            throws OrganizationAuthoritiesException, ObjectNotFoundException;

    /**
     * Updates a the organization with the values passed in the VO objects.<br>
     * Updated vales: address, email, locale, name, phone, url, distinguished
     * name, domicile country, description and image/logo.
     * 
     * @param organization
     *            The VO which holds the new values if the organization.
     * @param imageResource
     *            image/logo of the organization
     * @return A new vo based on the updated organization.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     * @throws ObjectNotFoundException
     *             Thrown in case no organization can be found for the
     *             identifier or the desired domicile country was not found.
     * @throws ValidationException
     *             Thrown in case one of the new values violates a validation
     *             constraint.
     * @throws ConcurrentModificationException
     *             Thrown in case the same object will be modified concurrently.
     * @throws DistinguishedNameException
     *             Thrown if the DistinguishedName is not unique
     * @throws OrganizationAuthorityException
     *             Thrown if it's not possible to set the pspid because the
     *             organization related to the specified organization identifier
     *             is not a supplier, or if one of the given organization's
     *             roles is invalid.
     * @throws IncompatibleRolesException
     *             Thrown in case the roles of the given organization are not
     *             compatible with each other.
     * @throws PSPIdentifierForSellerException
     *             Thrown in case the supplier does not have a PSP identifier
     *             setting.
     * @throws PaymentDataException
     *             Thrown in case the payment type is unknown. Holds the
     *             information about valid payment types.
     * @throws ImageException
     *             Thrown if the <code>imageResource</code> is not
     *             <code>null</code> and the <code>organization</code> has no
     *             <code>SUPPLIER</code> or <code>TECHNOLOGY_PROVIDER</code>
     *             role.
     * @throws AddMarketingPermissionException
     *             If the supplier could not be loaded or the organization does
     *             not have the supplier role.
     */
    VOOperatorOrganization updateOrganization(
            VOOperatorOrganization organization, VOImageResource imageResource)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            ValidationException, ConcurrentModificationException,
            DistinguishedNameException, OrganizationAuthorityException,
            IncompatibleRolesException, PSPIdentifierForSellerException,
            PaymentDataException, ImageException,
            AddMarketingPermissionException, NonUniqueBusinessKeyException;

    /**
     * Returns all organizations having the specified filter criteria.
     * 
     * @param organizationIdPattern
     *            The organization identifier of the found organizations must
     *            match this pattern.
     * @param organizationRoleTypes
     *            The found organizations must have one of the given role. If
     *            the list is empty the found organizations must have the role
     *            CUSTOMER, SUPPLIER or TECHNOLOGY_PROVIDER
     * @return the list of organizations
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     */
    List<VOOrganization> getOrganizations(String organizationIdPattern,
            List<OrganizationRoleType> organizationRoleTypes)
            throws OrganizationAuthoritiesException;

    /**
     * Returns all organizations having the specified filter criteria.
     *
     * @param organizationIdPattern
     *            The organization identifier of the found organizations must
     *            match this pattern.
     * @param organizationRoleTypes
     *            The found organizations must have one of the given role. If
     *            the list is empty the found organizations must have the role
     *            CUSTOMER, SUPPLIER or TECHNOLOGY_PROVIDER
     * @param queryLimit
     *            Limits objects returned by the query
     * @return the list of organizations
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     */
    List<VOOrganization> getOrganizationsWithLimit(String organizationIdPattern,
            List<OrganizationRoleType> organizationRoleTypes,
            Integer queryLimit) throws OrganizationAuthoritiesException;

    /**
     * Returns all users having the specified filter criteria.
     * 
     * @return the list of users
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     */
    List<VOUserDetails> getUsers() throws OrganizationAuthoritiesException;

    /**
     *
     * Returns all users belonging to the user organization and with assigned
     * role "ORGANIZATION_ADMIN" or "SUBSCRIPTION_MANAGER" or
     * "UNIT_ADMINISTRATOR". It is used for finding all users available for
     * subscription owner assignment.
     *
     * @param organizationKey
     *            The organization to retrieve users from.
     *
     * @return the list of users available for subscription owner assignment.
     */
    List<VOUserDetails> getSubscriptionOwnersForAssignment(
            Long organizationKey);

    /**
     * Reads all currently unhandled billing result objects from the database
     * and performs the payment processing for those who are related to payment
     * information settings requiring a handling by the PSP.
     * 
     * @return <code>true</code>, if the operation was successful.
     * @throws OrganizationAuthoritiesException
     *             Thrown in case the caller is not authorized as platform
     *             operator.
     */
    boolean startPaymentProcessing() throws OrganizationAuthoritiesException;

    /**
     * Exports the user operation log for the specified operationIds.
     * 
     * @param operationIds
     *            The list of operationIds
     * @param fromDate
     *            Specifies the date from when the logs are listed.
     * @param toDate
     *            Specifies the date to when the logs are listed.
     * @return The user operation log data as byte array.
     * @throws ValidationException
     *             Thrown in case the conditions for outputting log is not
     *             correct.
     * @throws AuditLogTooManyRowsException
     *             Thrown in case the the audit log contains too many entries
     *             for the specified condition.
     */
    byte[] getUserOperationLog(List<String> operationIds, long fromDate,
            long toDate)
            throws ValidationException, AuditLogTooManyRowsException;

    /**
     * Returns a map containing all user operations, which are written to the
     * audit log file.
     * <p>
     * This returned instance is a key value map, where the values represent the
     * operation display names, and the keys represent the internal log type
     * names referring to the operations.
     */
    Map<String, String> getAvailableAuditLogOperations();

    /**
     * Returns a map containing all groups of operations, which are written to
     * the audit log file.
     */
    Map<String, String> getAvailableAuditLogOperationGroups();

    /**
     * Returns all existing PSPs defined in the BES installation.
     * 
     * @return The existing PSPs.
     */
    List<VOPSP> getPSPs();

    /**
     * Creates or updates the specified PSP including its settings.
     * 
     * @param psp
     *            The psp to create or update.
     * @return The value object representation of the created or updated PSP
     *         available in BES.
     * @throws ValidationException
     *             Thrown in case the PSP settings could not be validated.
     * @throws ConcurrentModificationException
     *             Thrown in case the PSP to be updated is not of the same
     *             version as the specified one.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case a PSP with the same identifier already exists.
     * @throws ObjectNotFoundException
     *             Thrown in case the PSP to be updated cannot be found.
     */
    VOPSP savePSP(VOPSP psp)
            throws ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException, ObjectNotFoundException;

    /**
     * Returns the PSP accounts that are registered for the specified
     * organization.
     * 
     * @param organization
     *            The organization to retrieve the PSP accounts for.
     * @return The PSP accounts.
     * @throws ObjectNotFoundException
     *             Thrown in case the organization could not be found.
     */
    List<VOPSPAccount> getPSPAccounts(VOOrganization organization)
            throws ObjectNotFoundException;

    /**
     * Creates or updates the PSP account for the organization as specified.
     * 
     * @param organization
     *            The organization the PSP account will be created for.
     * @param account
     *            The account information including the related PSP and the PSP
     *            identifier.
     * @return The value object representation of the created or updated PSP
     *         account.
     * @throws ObjectNotFoundException
     *             Thrown in case either the referenced organization, account or
     *             PSP could not be found.
     * @throws OrganizationAuthorityException
     *             Thrown in case the target organization is not a supplier.
     * @throws ValidationException
     *             Thrown in case the PSP account settings could not be
     *             validated.
     * @throws ConcurrentModificationException
     *             Thrown in case the PSP account has been modified in the
     *             meantime.
     */
    VOPSPAccount savePSPAccount(VOOrganization organization,
            VOPSPAccount account)
            throws ObjectNotFoundException, OrganizationAuthorityException,
            ConcurrentModificationException, ValidationException;

    /**
     * Returns the payment types currently registered for the specified PSP.
     * 
     * @param psp
     *            The PSP to find the payment types for.
     * @return The payment types for the PSP.
     * @throws ObjectNotFoundException
     *             Thrown in case the referenced PSP could not be found.
     */
    List<VOPaymentType> getPaymentTypes(VOPSP psp)
            throws ObjectNotFoundException;

    /**
     * Creates the payment type as specified.
     * 
     * @param psp
     *            The PSP to create the payment type.
     * @param paymentType
     *            The payment type to be created.
     * @return The value object representation of the created payment type.
     * @throws ObjectNotFoundException
     *             Thrown in case the PSP could not be found.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case a payment type with the same identifier
     *             already exists.
     * @throws ValidationException
     *             Thrown in case the payment type could not be validated.
     * @throws ConcurrentModificationException
     */
    VOPaymentType savePaymentType(VOPSP psp, VOPaymentType paymentType)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, ConcurrentModificationException;

    /**
     * Exports the revenue list of the suppliers for the specified time frame in
     * the saved csv format.
     * 
     * @param month
     *            Specifies the month for which the revenues of the suppliers
     *            are listed.
     * @return The revenue data csv as byte array.
     */
    byte[] getSupplierRevenueList(long month);

    /**
     * @param subscriptionKey
     * @param organizationKey
     * @return
     */
    List<VOUserDetails> getUnassignedUsersByOrg(Long subscriptionKey,
            Long organizationKey);

    /**
     * Delegate to reset the value of a setting to default by its key
     * 
     * @param key
     *            the technical key
     * @throws ObjectNotFoundException
     */
    void deleteConfigurationSetting(Long key) throws ObjectNotFoundException;

    /**
     * Delegate to get a setting by its key
     * 
     * @param key
     *            the technical key
     * @return the setting
     * @throws ObjectNotFoundException
     */
    VOConfigurationSetting getConfigurationSetting(Long key)
            throws ObjectNotFoundException;

    /**
     * Returns collection of subscriptions with usage data.
     * @return
     */
    Collection<VOSubscriptionUsageEntry> getSubscriptionUsageReport();
}
