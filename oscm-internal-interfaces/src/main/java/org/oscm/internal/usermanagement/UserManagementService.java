/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 23.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.Properties;
import java.util.Set;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Service providing the functionality to manage integration of users maintained
 * in an (external) user management system.
 * 
 * @author groch
 * 
 */
@Remote
public interface UserManagementService {

    /**
     * Sets properties (e.g. remote LDAP properties) defined on platform level.
     * Incremental update of existing properties is not supported, i.e. this
     * method overrides all existing platform properties. For a list of
     * supported properties keys, please refer to {@link SettingType} <br>
     * Semantics:
     * <ul>
     * <li>A property given by (supported) key and value adds a platform
     * property with given key and value.</li>
     * <li>A property given only by (supported) key (with <i>value</i>==
     * <code>null</code>) or with an unsupported key is ignored.</li>
     * </ul>
     * 
     * @param platformProperties
     *            additional optional platform properties (e.g. remote LDAP
     *            properties) that are to be stored in the
     *            <code>PlatformSetting</code> table
     * @throws ValidationException
     *             if a platform property is given by key but has no value
     */
    public void setPlatformSettings(Properties platformProperties)
            throws ValidationException;

    /**
     * Returns all properties (e.g. remote LDAP properties) defined on platform
     * level. Since platform properties cannot be linked to other properties
     * (they rather may serve as default themselves), all returned properties
     * have a key and non-empty value. Furthermore all settings are marked to
     * represent the platform default.
     * 
     * @return the properties defined on platform level
     */
    public Set<POLdapSetting> getPlatformSettings();

    /**
     * Sets properties (e.g. remote LDAP properties) defined for a specific
     * organization. Incremental update of existing properties is not supported,
     * i.e. this method overrides all existing organization properties for the
     * given organization. For a list of supported properties keys, please refer
     * to {@link SettingType} <br>
     * Semantics:
     * <ul>
     * <li>A property given by (supported) key and value adds an
     * organization-specific property with given key and value for the
     * organization given by the orgId.</li>
     * <li>A property given only by (supported) key (with an empty String as
     * <i>value</i>) links the property to the corresponding platform property.
     * It's actual value is determined at run-time (see
     * {@link #getOrganizationSettingsResolved(orgId)})</li>
     * <li>A property with unsupported key is ignored.</li>
     * </ul>
     * 
     * @param orgId
     *            the organization id of the organization the settings are to be
     *            updated for; <br>
     *            if <code>null</code>, refer to platform-wide properties,
     *            otherwise refer to organization-specific properties
     * @param organizationProperties
     *            additional optional platform properties (e.g. remote LDAP
     *            properties) that are to be stored in the
     *            <code>OrganizationSetting</code> table
     * @throws ObjectNotFoundException
     *             if the organization given by <code>orgId</code> does not (or
     *             no longer) exist
     */
    public void setOrganizationSettings(String orgId,
            Properties organizationProperties) throws ObjectNotFoundException;

    /**
     * Performs the operation of
     * {@link #setOrganizationSettings(String, Properties)} using the identifier
     * of the organization the currently logged in user belongs to.
     */
    public void setOrganizationSettings(Properties organizationProperties);

    /**
     * Returns all properties (e.g. remote LDAP properties) defined for a
     * specific organization. The properties are returned as defined, i.e.
     * potential properties linked to their platform counterpart (i.e. no value
     * defined on organization level) are not resolved but returned with
     * <i>value</i>==<code>""</code>.
     * 
     * @param orgId
     *            the organization id of the organization the settings are to be
     *            retrieved for (must not be <code>null</code>)
     * @throws ObjectNotFoundException
     *             if the organization given by <code>orgId</code> does not (or
     *             no longer) exist
     * @return the list of (unresolved) properties defined for the given
     *         organization
     * 
     */
    public Properties getOrganizationSettings(String orgId)
            throws ObjectNotFoundException;

    /**
     * Deletes all organization-specific properties (e.g. remote LDAP
     * properties), copies all properties (i.e. their keys) defined on
     * platform-level for the organization given by the orgId and links them to
     * the defined platform properties (by setting their values to an empty
     * String). This method makes only sense on organization level.
     * 
     * @param orgId
     *            the organization id of the organization the settings are to be
     *            updated for (must not be <code>null</code>)
     * @param organizationProperties
     *            additional optional platform properties (e.g. remote LDAP
     *            properties) ();<br>
     *            if <code>null</code>, given properties hold platform-wide and
     *            are to be stored in the <code>PlatformSetting</code> table,
     *            otherwise properties are organization-specific and are to be
     *            stored in the <code>OrganizationSetting</code> table
     * @throws ObjectNotFoundException
     *             if the organization given by <code>orgId</code> does not (or
     *             no longer) exist
     */
    public void resetOrganizationSettings(String orgId)
            throws ObjectNotFoundException;

    /**
     * Performs the operation of {@link #resetOrganizationSettings(String)}
     * using the identifier of the organization the currently logged in user
     * belongs to.
     */
    public void resetOrganizationSettings();

    /**
     * Deletes all platform properties (by key and value). This method must only
     * be invoked by an platform operator.
     */
    public void clearPlatformSettings();

    /**
     * Returns the resolved list of properties (e.g. remote LDAP properties) for
     * a specific organization, taking into account platform properties. For
     * properties that are linked to their platform counterpart (i.e. no value
     * defined on organization level), the value of the platform property with
     * the same key is returned (if existing); if a corresponding platform
     * property is not defined, the property is not returned at all.
     * 
     * <p>
     * The credentials for the LDAP user, if defined, are not returned in plain
     * text but as '********'.
     * </p>
     * 
     * @param organization
     *            the organization the resolved settings are to be retrieved for
     *            (must not be <code>null</code>)
     * @throws ObjectNotFoundException
     *             if the organization given by <code>orgId</code> does not (or
     *             no longer) exist
     * @return the set of resolved properties defined for the given organization
     */
    public Set<POLdapSetting> getOrganizationSettingsResolved(String orgId)
            throws ObjectNotFoundException;

    /**
     * Performs the operation of
     * {@link #getOrganizationSettingsResolved(String)} using the identifier of
     * the organization the currently logged in user belongs to.
     */
    public Set<POLdapSetting> getOrganizationSettingsResolved();

    /**
     * This 'ping method' (or 'health check') determines whether all mandatory
     * properties are provided and the user management system can be reached
     * using the resolved list of organization properties (e.g. remote LDAP
     * properties) given by its orgId. If <code>null</code> is passed as
     * parameter, this method tries to connect using only the defined platform
     * properties.
     * 
     * @param organization
     *            the organization whose settings to be used (may be
     *            <code>null</code>)
     * @throws ObjectNotFoundException
     *             if the organization given by <code>orgId</code> does not (or
     *             no longer) exist
     * @throws ValidationException
     *             if not all mandatory LDAP properties are available
     * @return <code>true</code> if a connection using the defined organization
     *         and/or platform properties, and <code>false</code> otherwise.
     */
    public boolean canConnect(String orgId) throws ObjectNotFoundException,
            ValidationException;

    /**
     * Performs the operation of {@link #canConnect(String)} using the
     * identifier of the organization the currently logged in user belongs to.
     */
    public boolean canConnect() throws ValidationException;

    /**
     * Returns the list of LDAP properties which are mapped for the organization
     * of the calling user, i.e. for which a organization setting is defined and
     * a value can be resolved. This method can be used to determine the
     * attributes which are retrieved from an external user management system
     * (e.g. LDAP).
     * 
     * @return the list of mapped attributes
     */
    public Set<SettingType> getMappedAttributes();

    /**
     * Determines if the currently logged in user has the role
     * UserRoleType.PLATFORM_OPERATOR.
     * 
     * @return <code>true</code> in case the user is platform operator,
     *         <code>false</code> otherwise.
     */
    public boolean isPlatformOperator();

    /**
     * Retrieves all LDAP managed organizations in the system.
     * 
     * @return The LDAP managed organizations.
     */
    public Set<POLdapOrganization> getLdapManagedOrganizations();

    /**
     * Checks whether the organization specified by its identifier is LDAP
     * managed or not.
     * 
     * @param organizationIdentifier
     *            The identifier of the organization to check for.
     * @return <code>true</code> in case the organization is LDAP managed,
     *         <code>false</code> otherwise.
     * @throws ObjectNotFoundException
     *             Thrown in case an organization with the specified identifier
     *             does not exist.
     */
    public boolean isOrganizationLDAPManaged(String orgId)
            throws ObjectNotFoundException;

    /**
     * Performs the operation of {@link #isOrganizationLDAPManaged(String)}
     * using the identifier of the organization the currently logged in user
     * belongs to.
     */
    public boolean isOrganizationLDAPManaged();

}
