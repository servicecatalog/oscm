/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.assembler;

import java.util.List;

import org.apache.commons.validator.GenericValidator;

import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Tenant;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Assembler to convert user related value objects to the according domain
 * objects and vice versa.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UserDataAssembler extends BaseAssembler {

    public static final String FIELD_NAME_ORGANIZATION_ID = "organizationId";

    public static final String FIELD_NAME_USER_ID = "userId";

    public static final String FIELD_NAME_ADDRESS = "address";

    public static final String FIELD_NAME_ADDITIONAL_NAME = "additionalName";

    public static final String FIELD_NAME_EMAIL = "email";

    public static final String FIELD_NAME_FIRST_NAME = "firstName";

    public static final String FIELD_NAME_LAST_NAME = "lastName";

    public static final String FIELD_NAME_LOCALE = "locale";

    public static final String FIELD_NAME_PHONE = "phone";

    private static final int LOCAL_LENGTH = 2;

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @param platformUser
     *            The domain object containing the values to be set.
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOUser toVOUser(PlatformUser platformUser) {
        if (platformUser == null) {
            return null;
        }
        VOUser voUser = new VOUser();
        updateVoUser(platformUser, voUser);
        return voUser;
    }

    static void updateVoUser(PlatformUser platformUser, VOUser voUser) {
        voUser.setOrganizationId(platformUser.getOrganization()
                .getOrganizationId());
        voUser.setOrganizationName(platformUser.getOrganization().getName());
        voUser.setUserId(platformUser.getUserId());
        voUser.setStatus(platformUser.getStatus());
        final Tenant tenant = platformUser.getOrganization().getTenant();
        if (tenant != null) {
            voUser.setTenantKey(String.valueOf(tenant.getKey()));
            voUser.setTenantId(String.valueOf(tenant.getTenantId()));
        }
        updateOrganizationRoles(platformUser, voUser);
        updateUserRoles(platformUser, voUser);
        updateValueObject(voUser, platformUser);
    }

    /**
     * Converts the given domain object to a value object containing all
     * attributes.
     * 
     * @param platformUser
     *            The domain object containing the values to be set.
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOUserDetails toVOUserDetails(PlatformUser platformUser) {
        if (platformUser == null) {
            return null;
        }
        VOUserDetails userDetails = new VOUserDetails(platformUser.getKey(),
                platformUser.getVersion());
        updateVoUser(platformUser, userDetails);
        updateVoUserDetails(platformUser, userDetails);
        return userDetails;
    }

    static void updateVoUserDetails(PlatformUser platformUser,
            VOUserDetails userDetails) {
        userDetails.setEMail(platformUser.getEmail());
        userDetails.setRealmUserId(platformUser.getRealmUserId());
        userDetails.setFirstName(platformUser.getFirstName());
        userDetails.setAdditionalName(platformUser.getAdditionalName());
        userDetails.setLastName(platformUser.getLastName());
        userDetails.setPhone(platformUser.getPhone());
        userDetails.setAddress(platformUser.getAddress());
        userDetails.setLocale(platformUser.getLocale());
        userDetails.setSalutation(platformUser.getSalutation());
        userDetails.setRemoteLdapActive(platformUser.getOrganization()
                .isRemoteLdapActive());
        userDetails.setRemoteLdapAttributes(platformUser.getOrganization()
                .getLdapUserAttributes());
        Tenant tenant = platformUser.getOrganization().getTenant();
        if(tenant!=null){
            userDetails.setTenantId(tenant.getTenantId());
        }
        
    }

    /**
     * Updates the organization role settings of the user's value object.
     * 
     * @param platformUser
     *            The domain object serving as template.
     * @param voUser
     *            The value object to be updated.
     */
    private static void updateOrganizationRoles(PlatformUser platformUser,
            VOUser voUser) {
        for (OrganizationToRole orgToRole : platformUser.getOrganization()
                .getGrantedRoles()) {
            voUser.getOrganizationRoles().add(
                    orgToRole.getOrganizationRole().getRoleName());
        }
    }

    /**
     * Updates the user role settings of the user's value object.
     * 
     * @param platformUser
     *            The domain object serving as template.
     * @param voUser
     *            The value object to be updated.
     */
    private static void updateUserRoles(PlatformUser platformUser, VOUser voUser) {
        for (RoleAssignment roleAssignment : platformUser.getAssignedRoles()) {
            voUser.addUserRole(roleAssignment.getRole().getRoleName());
        }
    }

    /**
     * Updates all non-business-key attributes in a given PlatformUser object
     * according to the values specified in the value object.
     * 
     * <p>
     * The organization, status and business key (fullLoginName) information
     * will not be set. Neither will the logical id or the version information.
     * This method should only be called when creating a new platform user.
     * </p>
     * 
     * @param userDetails
     *            The value object containing the values to be set.
     * @return The modified platform user.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    public static PlatformUser toPlatformUser(VOUserDetails userDetails)
            throws ValidationException {
        validate(userDetails);
        PlatformUser platformUser = new PlatformUser();
        copyAttributes(userDetails, platformUser);
        return platformUser;
    }

    /**
     * Updates all non-key fields in the platform user object to reflect the
     * changes performed in the value object. This method considers the version
     * settings in the value object and stores them in the domain object.
     * 
     * @param userDetails
     *            The updated details for the user
     * @param userToBeUpdated
     *            The platform user object to be updated.
     * @return The updated platform user object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public static PlatformUser updatePlatformUser(VOUserDetails userDetails,
            PlatformUser userToBeUpdated) throws ValidationException,
            ConcurrentModificationException {
        validate(userDetails);
        verifyVersionAndKey(userToBeUpdated, userDetails);
        copyAttributes(userDetails, userToBeUpdated);
        return userToBeUpdated;
    }

    public static void copyAttributes(VOUserDetails userDetails,
            PlatformUser userToBeUpdated) {
        userToBeUpdated.setUserId(userDetails.getUserId().trim());
        userToBeUpdated.setFirstName(userDetails.getFirstName());
        userToBeUpdated.setAdditionalName(userDetails.getAdditionalName());
        userToBeUpdated.setLastName(userDetails.getLastName());
        userToBeUpdated.setEmail(userDetails.getEMail());
        userToBeUpdated.setAddress(userDetails.getAddress());
        userToBeUpdated.setPhone(userDetails.getPhone());
        userToBeUpdated.setLocale(userDetails.getLocale());
        userToBeUpdated.setSalutation(userDetails.getSalutation());
        userToBeUpdated.setTenantId(userDetails.getTenantId());
        // don't modify credentials!
    }

    /**
     * Update a user value object with the attributes read from a remote LDAP
     * system.
     * 
     * @param values
     *            The values of the read LDAP attributes (the order off the
     *            values must be the same as the order of the settings).
     * @param settingList
     *            The list with the configured LDAP attributes.
     * @param userDetails
     *            The updated details for the user
     * 
     * @return The updated value object
     */
    public static VOUserDetails updateVOUserDetails(String[] values,
            List<SettingType> settingList, VOUserDetails userDetails) {
        for (int i = 0; i < settingList.size(); i++) {
            final String value = values[i];
            if (value == null || value.length() == 0) {
                continue;
            }
            if (settingList.get(i) == SettingType.LDAP_ATTR_ADDITIONAL_NAME) {
                userDetails.setAdditionalName(value);
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_EMAIL) {
                userDetails.setEMail(value);
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_FIRST_NAME) {
                userDetails.setFirstName(value);
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_LAST_NAME) {
                userDetails.setLastName(value);
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_LOCALE) {
                if (value.length() > LOCAL_LENGTH) {
                    userDetails.setLocale(value.substring(0, LOCAL_LENGTH));
                } else {
                    userDetails.setLocale(value);
                }
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_UID) {
                // Usually the user ID which is used in the LDAP is the same as
                // used in BES. It might differ if the ID is not unique in the
                // context of BES.
                userDetails.setRealmUserId(value);
            }
        }
        return userDetails;
    }

    /**
     * Updates all fields in the platform user object for which a LDAP attribute
     * is configured. <br>
     * The filed "userid", will not be updated since it's possible that the new
     * LDAP user id is not unique in BES. For this reason the field
     * "realmuserid" will be updated to be in sync with LDAP.
     * 
     * @param userDetails
     *            The value object containing the values to be set.
     * @param settingList
     *            The list with the configured LDAP attributes.
     * @param userToBeUpdated
     *            The platform user object to be updated.
     * @return The updated platform user object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    public static PlatformUser updatePlatformUser(VOUserDetails userDetails,
            List<SettingType> settingList, PlatformUser userToBeUpdated) {
        for (int i = 0; i < settingList.size(); i++) {
            if (settingList.get(i) == SettingType.LDAP_ATTR_ADDITIONAL_NAME) {
                userToBeUpdated.setAdditionalName(userDetails
                        .getAdditionalName());
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_EMAIL) {
                if (!GenericValidator.isBlankOrNull(userDetails.getEMail())) {
                    userToBeUpdated.setEmail(userDetails.getEMail());
                }
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_FIRST_NAME) {
                userToBeUpdated.setFirstName(userDetails.getFirstName());
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_LAST_NAME) {
                userToBeUpdated.setLastName(userDetails.getLastName());
            } else if (settingList.get(i) == SettingType.LDAP_ATTR_UID) {
                // Do not update the userid here because a update can violate
                // the unique constraint in BES.
                userToBeUpdated.setRealmUserId((userDetails.getRealmUserId()));
            }
        }
        return userToBeUpdated;
    }

    /**
     * Validate a user details value object.
     * 
     * @param userDetails
     *            the value object to validate
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     */
    private static void validate(VOUserDetails userDetails)
            throws ValidationException {
        BLValidator.isName(FIELD_NAME_ADDITIONAL_NAME,
                userDetails.getAdditionalName(), false);
        BLValidator.isDescription(FIELD_NAME_ADDRESS, userDetails.getAddress(),
                false);
        BLValidator.isId(FIELD_NAME_ORGANIZATION_ID,
                userDetails.getOrganizationId(), true);
        BLValidator.isEmail(FIELD_NAME_EMAIL, userDetails.getEMail(), true);
        BLValidator.isName(FIELD_NAME_FIRST_NAME, userDetails.getFirstName(),
                false);
        BLValidator.isName(FIELD_NAME_LAST_NAME, userDetails.getLastName(),
                false);
        BLValidator.isLocale(FIELD_NAME_LOCALE, userDetails.getLocale(), true);
        BLValidator.isUserId(FIELD_NAME_USER_ID, userDetails.getUserId(), true);
        BLValidator.isName(FIELD_NAME_PHONE, userDetails.getPhone(), false);
    }

    /**
     * Copies all user detailed attributes from one platform user to another
     * platform user.
     * 
     * @param platformUser
     *            source instance
     * @return Platform user copy
     */
    public static PlatformUser copyPlatformUser(PlatformUser platformUser) {
        PlatformUser u = new PlatformUser();
        u.setUserId(platformUser.getUserId());
        u.setStatus(platformUser.getStatus());
        u.setFirstName(platformUser.getFirstName());
        u.setAdditionalName(platformUser.getAdditionalName());
        u.setLastName(platformUser.getLastName());
        u.setEmail(platformUser.getEmail());
        u.setAddress(platformUser.getAddress());
        u.setPhone(platformUser.getPhone());
        u.setLocale(platformUser.getLocale());
        u.setSalutation(platformUser.getSalutation());
        u.setKey(platformUser.getKey());
        u.setTenantId(platformUser.getTenantId());
        return u;
    }
}
