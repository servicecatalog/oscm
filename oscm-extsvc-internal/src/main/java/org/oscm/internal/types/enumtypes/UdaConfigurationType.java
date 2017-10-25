/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-06-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Specifies the possible configuration types of custom attributes. The
 * configuration type determines to whom a custom attribute is visible and
 * whether a value is mandatory or optional.
 */

public enum UdaConfigurationType {

    /**
     * The custom attribute is not presented to customers on a marketplace, and
     * a value can be entered by a supplier only.
     */
    SUPPLIER(Collections.singleton(OrganizationRoleType.SUPPLIER), Collections
            .singleton(OrganizationRoleType.SUPPLIER)),

    /**
     * The custom attribute is displayed when a customer subscribes to a
     * service, and a value must be entered by the customer.
     */
    USER_OPTION_MANDATORY(Collections.unmodifiableSet(EnumSet.of(
            OrganizationRoleType.SUPPLIER, OrganizationRoleType.CUSTOMER)),
            Collections.singleton(OrganizationRoleType.CUSTOMER), true),

    /**
     * The custom attribute is displayed when a customer subscribes to a
     * service, and a value can be entered by the customer.
     */
    USER_OPTION_OPTIONAL(Collections.unmodifiableSet(EnumSet.of(
            OrganizationRoleType.SUPPLIER, OrganizationRoleType.CUSTOMER)),
            Collections.singleton(OrganizationRoleType.CUSTOMER));

    /**
     * Organization roles that have the authority to read this UDA.
     */
    private Set<OrganizationRoleType> rolesForReading;

    /**
     * Organization roles that have the authority to write/change this UDA.
     */
    private Set<OrganizationRoleType> rolesForWriting;

    /**
     * Specifies whether a value for the custom attribute is mandatory.
     */
    private boolean mandatory;

    /**
     * Constructs an <code>UdaConfigurationType</code> with the given
     * organization roles.
     * 
     * @param forReading
     *            roles that have read authority
     * @param forWriting
     *            roles that have write authority
     */
    private UdaConfigurationType(Set<OrganizationRoleType> forReading,
            Set<OrganizationRoleType> forWriting) {
        this(forReading, forWriting, false);
    }

    private UdaConfigurationType(Set<OrganizationRoleType> forReading,
            Set<OrganizationRoleType> forWriting, boolean required) {
        rolesForReading = forReading;
        rolesForWriting = forWriting;
        mandatory = required;
    }

    /**
     * Returns whether an organization with the specified role is allowed to
     * read custom attributes with the given configuration type.
     * 
     * @param type
     *            the organization role
     * @return <code>true</code> if the organization role has read rights,
     *         <code>false</code> otherwise
     */
    public boolean canRead(OrganizationRoleType type) {
        return rolesForReading.contains(type);
    }

    /**
     * Returns whether an organization with the specified role is allowed to
     * update custom attributes with the given configuration type.
     * 
     * @param type
     *            the organization role
     * @return <code>true</code> if the organization role has write rights,
     *         <code>false</code> otherwise
     */
    public boolean canWrite(OrganizationRoleType type) {
        return rolesForWriting.contains(type);
    }

    /**
     * Returns whether a value must be specified for custom attributes with the
     * given configuration type.
     * 
     * @return <code>true</code> if a value is mandatory, <code>false</code>
     *         otherwise
     */
    public boolean isMandatory() {
        return mandatory;
    }
}
