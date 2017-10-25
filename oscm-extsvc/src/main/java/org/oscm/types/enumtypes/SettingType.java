/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-04-08                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

/**
 * Specifies the LDAP settings for an organization that uses its own user
 * authentication system based on an LDAP directory server, or the platform
 * defaults for such settings.
 */
public enum SettingType {

    /**
     * The base DN of the LDAP system (default: "").
     */
    LDAP_BASE_DN(String.class, ""),

    /**
     * The context factory class for the LDAP system (default:
     * <code>com.sun.jndi.ldap.LdapCtxFactory</code>).
     */
    LDAP_CONTEXT_FACTORY(String.class, "com.sun.jndi.ldap.LdapCtxFactory",
            Context.INITIAL_CONTEXT_FACTORY),

    /**
     * The password for a search access to the LDAP system (optional).
     */
    LDAP_CREDENTIALS(String.class, null, Context.SECURITY_CREDENTIALS),

    /**
     * The security principal for a search access to the LDAP system (optional).
     */
    LDAP_PRINCIPAL(String.class, null, Context.SECURITY_PRINCIPAL),

    /**
     * The URL of the LDAP system (default: <code>localhost:389</code>).
     */
    LDAP_URL(String.class, null, Context.PROVIDER_URL),

    /**
     * The LDAP attribute containing the additional name of a user (optional).
     */
    LDAP_ATTR_ADDITIONAL_NAME(String.class),

    /**
     * The LDAP attribute containing the email address of a user (optional).
     */
    LDAP_ATTR_EMAIL(String.class),

    /**
     * The LDAP attribute containing the first name of a user (optional).
     */
    LDAP_ATTR_FIRST_NAME(String.class),

    /**
     * The LDAP attribute containing the last name of a user (optional).
     */
    LDAP_ATTR_LAST_NAME(String.class),

    /**
     * The LDAP attribute containing the locale of a user (optional).
     */
    LDAP_ATTR_LOCALE(String.class),

    /**
     * The LDAP attribute containing the ID of a user (default: <code>uid</code>
     * ).
     */
    LDAP_ATTR_UID(String.class, "uid"),

    /**
     * The LDAP attribute containing how referrals encountered by the service
     * provider are to be processed (default: <code>ignore</code> ).
     */
    LDAP_ATTR_REFERRAL(String.class, "ignore", Context.REFERRAL);

    /**
     * A collection of the mandatory LDAP settings and attributes.
     */
    public final static List<String> LDAP_ATTRIBUTES_MANDATORY = Arrays
            .asList(new String[] { LDAP_URL.name(), LDAP_BASE_DN.name(),
                    LDAP_CONTEXT_FACTORY.name(), LDAP_ATTR_UID.name() });

    /**
     * A collection of LDAP attributes with the defaults set for the platform.
     */
    public final static List<SettingType> LDAP_ATTRIBUTES = Arrays
            .asList(new SettingType[] { LDAP_ATTR_ADDITIONAL_NAME,
                    LDAP_ATTR_EMAIL, LDAP_ATTR_FIRST_NAME, LDAP_ATTR_LAST_NAME,
                    LDAP_ATTR_LOCALE, LDAP_ATTR_UID, LDAP_ATTR_REFERRAL });

    private Class<?> typeClass;

    private String defaultValue;

    private String dirContextKey;

    private SettingType(Class<?> clazz) {
        typeClass = clazz;
    }

    private SettingType(Class<?> clazz, String defaultValue) {
        this.typeClass = clazz;
        this.defaultValue = defaultValue;
    }

    private SettingType(Class<?> clazz, String defaultValue,
            String dirContextKey) {
        this.typeClass = clazz;
        this.defaultValue = defaultValue;
        this.dirContextKey = dirContextKey;
    }

    /**
     * Returns the base DN class of the LDAP system.
     * 
     * @return the type class
     */
    public Class<?> getTypeClass() {
        return typeClass;
    }

    /**
     * Returns the default value used by the LDAP system.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the context factory key for the LDAP system (default:
     * <code>com.sun.jndi.ldap.LdapCtxFactory</code>).
     * 
     * @return the context factory key
     */
    public String getDirContextKey() {
        return dirContextKey;
    }

    /**
     * Returns whether the given string is included in the LDAP settings.
     * 
     * @param enumKey
     *            the string to check for
     * @return <code>true</code> if the string is an LDAP setting,
     *         <code>false</code> otherwise
     */
    public static boolean contains(String enumKey) {
        Set<String> enumKeys = new HashSet<String>();
        for (SettingType type : SettingType.values()) {
            enumKeys.add(type.name());
        }
        return enumKeys.contains(enumKey);
    }

}
