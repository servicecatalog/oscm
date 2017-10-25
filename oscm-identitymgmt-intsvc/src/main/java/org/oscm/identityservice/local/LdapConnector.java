/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 30.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author groch
 * 
 */
public class LdapConnector {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LdapConnector.class);
    private final LdapAccessServiceLocal ldapAccess;
    private Set<String> missingMandatoryLdapProps;
    private Properties dirProperties;
    private String baseDN;
    private Map<SettingType, String> attrMap;

    private static final int LOCAL_LENGTH = 2;

    /**
     * @param ldapAccess
     */
    public LdapConnector(final LdapAccessServiceLocal ldapAccess,
            final Properties allProps) {
        super();
        this.ldapAccess = ldapAccess;
        init(allProps);
    }

    /**
     * Sets the dirProperties and attrMap
     */
    public void init(final Properties props) {
        this.dirProperties = new Properties();
        this.attrMap = new HashMap<SettingType, String>();

        // extract different types of properties + check if all mandatory
        // settings are available
        this.missingMandatoryLdapProps = new HashSet<String>(
                SettingType.LDAP_ATTRIBUTES_MANDATORY);

        if (props != null) {
            for (Object propKey : props.keySet()) {
                String key = String.valueOf(propKey);
                SettingType settingType = SettingType.valueOf(key);
                String value = props.getProperty(key);
                if (settingType.getDirContextKey() != null) {
                    this.dirProperties.put(settingType.getDirContextKey(),
                            value);
                } else if (SettingType.LDAP_ATTRIBUTES.contains(settingType)) {
                    this.attrMap.put(settingType, value);
                } else if (SettingType.LDAP_BASE_DN.equals(settingType)) {
                    this.baseDN = value;
                }
                this.missingMandatoryLdapProps.remove(key);
            }
        }
    }

    /**
     * Validate that a value entered by a user is empty or equal to the value
     * from the remote LDAP system.
     * 
     * @param attrMap
     *            the map with all configured LDAP attributes
     * @param setting
     *            the LDAP attribute the check
     * @param userValue
     *            the value entered by a the user
     * @param ldapValue
     *            value from the LDAP system
     * @throws ValidationException
     */
    private void validateLdapPropertyValue(Map<SettingType, String> attrMap,
            SettingType setting, String userValue, String ldapValue)
            throws ValidationException {
        if (attrMap.containsKey(setting) && userValue != null
                && userValue.length() != 0 && !userValue.equals(ldapValue)) {
            // sessionCtx.setRollbackOnly();
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_VALUE_MISMATCH, null, new Object[] {
                            ldapValue, setting.toString(), userValue });
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    vf,
                    LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                    "User Value");
            throw vf;
        }
    }

    /**
     * Validate the LDAP properties by performing a search request.
     * 
     * @param baseDN
     *            the baseDN
     * @return the read attribute
     * @throws ValidationException
     *             Thrown in case the LDAP access failed or no record was found
     */
    public VOUserDetails validateLdapProperties(VOUserDetails user)
            throws ValidationException {
        
        LdapVOUserDetailsMapper mapper = new LdapVOUserDetailsMapper(user,
                this.attrMap);
        VOUserDetails tmpUser = new VOUserDetails();
        tmpUser.setAdditionalName(user.getAdditionalName());
        tmpUser.setEMail(user.getEMail());
        tmpUser.setFirstName(user.getFirstName());
        tmpUser.setLastName(user.getLastName());
        tmpUser.setLocale(user.getLocale());

        try {

            String dnName = ldapAccess.dnSearch(this.dirProperties,
                    this.baseDN, this.attrMap.get(SettingType.LDAP_ATTR_UID)
                            + "=" + user.getUserId());

            if (dnName == null) {
                // sessionCtx.setRollbackOnly();
                ValidationException vf = new ValidationException(
                        ReasonEnum.LDAP_USER_NOT_FOUND, null,
                        new Object[] { user.getUserId() });
                logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                        LogMessageIdentifier.ERROR_LDAP_SEARCH_OF_USER_FAILED,
                        user.getUserId());
                throw vf;
            }

            List<VOUserDetails> result = ldapAccess.search(this.dirProperties,
                    this.baseDN, this.attrMap.get(SettingType.LDAP_ATTR_UID)
                            + "=" + user.getUserId(), mapper, true);
            int size = result.size();
            if (size == 1) {
                user = result.get(0);
                if (user.getLocale() != null && !user.getLocale().isEmpty()
                        && user.getLocale().length() > LOCAL_LENGTH) {
                    user.setLocale(user.getLocale().substring(0, LOCAL_LENGTH));
                }
                validateLdapPropertyValue(this.attrMap,
                        SettingType.LDAP_ATTR_ADDITIONAL_NAME,
                        tmpUser.getAdditionalName(), user.getAdditionalName());
                validateLdapPropertyValue(this.attrMap,
                        SettingType.LDAP_ATTR_EMAIL, tmpUser.getEMail(),
                        user.getEMail());
                validateLdapPropertyValue(this.attrMap,
                        SettingType.LDAP_ATTR_FIRST_NAME,
                        tmpUser.getFirstName(), user.getFirstName());
                validateLdapPropertyValue(this.attrMap,
                        SettingType.LDAP_ATTR_LAST_NAME, tmpUser.getLastName(),
                        user.getLastName());
                validateLdapPropertyValue(this.attrMap,
                        SettingType.LDAP_ATTR_LOCALE, tmpUser.getLocale(),
                        user.getLocale());

                
                return result.get(0);
            } else if (size == 0) {
                // sessionCtx.setRollbackOnly();
                ValidationException vf = new ValidationException(
                        ReasonEnum.LDAP_USER_NOT_FOUND, null,
                        new Object[] { user.getUserId() });
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        vf,
                        LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                        "LDAP User");
                throw vf;
            } else {
                // sessionCtx.setRollbackOnly();
                ValidationException vf = new ValidationException(
                        ReasonEnum.LDAP_USER_NOT_UNIQUE, null,
                        new Object[] { user.getUserId() });
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        vf,
                        LogMessageIdentifier.ERROR_VALIDATION_PARAMETER_LDAP_FOUND_ERROR,
                        "LDAP User");
                throw vf;
            }
        } catch (NameNotFoundException nnfe) {
            // sessionCtx.setRollbackOnly();
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_BASE_DN_INVALID, null,
                    new Object[] { this.baseDN });
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.ERROR_LDAP_ACCESS_FAILED,
                    nnfe.getMessage());
            throw vf;
        } catch (NamingException e1) {
            // sessionCtx.setRollbackOnly();
            Object[] params = new Object[] {
                    dirProperties.get(Context.PROVIDER_URL), e1.getMessage() };
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_CONNECTION_REFUSED, null, params);
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.ERROR_LDAP_SYSTEM_CONNECTION_REFUSED,
                    "LDAPuser");
            throw vf;
        }

    }

    public void ensureAllMandatoryLdapPropertiesPresent()
            throws ValidationException {
        if (!missingMandatoryLdapProps.isEmpty()) {
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_MANDATORY_PROPERTY_MISSING, null,
                    new Object[] { this.missingMandatoryLdapProps.toString() });
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    vf,
                    LogMessageIdentifier.ERROR_MANDATORY_LDAP_PARAMETER_MISSING,
                    this.missingMandatoryLdapProps.toString());
            throw vf;
        }
    }

    public boolean canConnect() throws ValidationException {

        try {
            // now try to connect
            String dnName = ldapAccess.dnSearch(this.dirProperties,
                    this.baseDN, this.attrMap.get(SettingType.LDAP_ATTR_UID)
                            + "=*");

            if (dnName == null) {
                
                return false;
            }

            return true;

        } catch (NameNotFoundException nnfe) {
            // sessionCtx.setRollbackOnly();
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_BASE_DN_INVALID, null,
                    new Object[] { this.baseDN });
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.ERROR_LDAP_ACCESS_FAILED,
                    nnfe.getMessage());
            throw vf;
        } catch (NamingException e1) {
            // sessionCtx.setRollbackOnly();
            Object[] params = new Object[] {
                    this.dirProperties.get(Context.PROVIDER_URL),
                    e1.getMessage() };
            ValidationException vf = new ValidationException(
                    ReasonEnum.LDAP_CONNECTION_REFUSED, null, params);
            logger.logError(Log4jLogger.SYSTEM_LOG, vf,
                    LogMessageIdentifier.ERROR_LDAP_SYSTEM_CONNECTION_REFUSED,
                    "LDAPuser");
            throw vf;
        }
    }

    /**
     * @return the dirProperties
     */
    public Properties getDirProperties() {
        return dirProperties;
    }

    /**
     * @return the baseDN
     */
    public String getBaseDN() {
        return baseDN;
    }

    /**
     * @return the attrMap
     */
    public Map<SettingType, String> getAttrMap() {
        return attrMap;
    }

}
