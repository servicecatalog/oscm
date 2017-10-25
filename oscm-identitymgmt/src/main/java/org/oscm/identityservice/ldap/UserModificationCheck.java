/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import java.util.Set;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.PlatformUser;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.UnsupportedOperationException;

/**
 * Auxiliary class to ensure that no changes are applied to a platform user that
 * violate existing restrictions due to LDAP mappings.
 * 
 * @author jaeger
 * 
 */
public class UserModificationCheck {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(UserModificationCheck.class);

    private final Set<SettingType> mappedLdapSettings;

    public UserModificationCheck(Set<SettingType> mappedLdapSettings) {
        this.mappedLdapSettings = mappedLdapSettings;
    }

    /**
     * <p>
     * Checks whether the changes applied to the primitive type attributes of
     * the user violate the configuration of existing LDAP mappings. If so, an
     * exception of type UnsupportedOperationException is thrown. If any of the
     * parameters is <code>null</code>, the operation will abort without an
     * exception.
     * </p>
     * <p>
     * <b>Note: </b>This method does not perform any validation of the defined
     * values.
     * </p>
     * 
     * @param originalUser
     *            The unmodified user as e.g. currently stored in the database.
     * @param modifiedUser
     *            The user object containing the changes to be checked.
     */
    public void check(PlatformUser originalUser, PlatformUser modifiedUser) {
        if (originalUser == null || modifiedUser == null) {
            return;
        }

        boolean isAdditionalNameMapped = mappedLdapSettings
                .contains(SettingType.LDAP_ATTR_ADDITIONAL_NAME);
        if (isAdditionalNameMapped
                && !Strings.areStringsEqual(originalUser.getAdditionalName(),
                        modifiedUser.getAdditionalName())) {
            handleViolation("additionalName");
        }

        boolean isFirstNameMapped = mappedLdapSettings
                .contains(SettingType.LDAP_ATTR_FIRST_NAME);
        if (isFirstNameMapped
                && !Strings.areStringsEqual(originalUser.getFirstName(),
                        modifiedUser.getFirstName())) {
            handleViolation("firstName");
        }

        boolean isLastNameMapped = mappedLdapSettings
                .contains(SettingType.LDAP_ATTR_LAST_NAME);
        if (isLastNameMapped
                && !Strings.areStringsEqual(originalUser.getLastName(),
                        modifiedUser.getLastName())) {
            handleViolation("lastName");
        }

        boolean isEmailMapped = mappedLdapSettings
                .contains(SettingType.LDAP_ATTR_EMAIL);
        if (isEmailMapped
                && !Strings.areStringsEqual(originalUser.getEmail(),
                        modifiedUser.getEmail())) {
            handleViolation("email");
        }

        boolean isUserIdMapped = mappedLdapSettings
                .contains(SettingType.LDAP_ATTR_UID);
        if (isUserIdMapped
                && !Strings.areStringsEqual(originalUser.getUserId(),
                        modifiedUser.getUserId())) {
            handleViolation("userId");
        }

    }

    private void handleViolation(String attributeName) {
        UnsupportedOperationException uoe = new UnsupportedOperationException(
                String.format(
                        "Updating user attribute '%s' failed as it is mapped in LDAP system",
                        attributeName));
        logger.logError(LogMessageIdentifier.ERROR_USER_UPDATE_FAILED_ATTRIBUTE_LDAP_MAPPED);
        throw uoe;
    }

}
