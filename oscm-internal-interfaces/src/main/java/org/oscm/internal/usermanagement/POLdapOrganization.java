/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import org.oscm.internal.base.BasePO;

/**
 * Represents the display data for organizations that are LDAP managed.
 * 
 * @author jaeger
 * 
 */
public class POLdapOrganization extends BasePO {

    private static final long serialVersionUID = 1L;

    private String name;
    private String identifier;

    public POLdapOrganization(long key, int version, String organizationName,
            String organizationIdentifier) {
        this.key = key;
        this.version = version;
        this.name = organizationName;
        this.identifier = organizationIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean equals(Object other) {
        if (!(other instanceof POLdapOrganization)) {
            return false;
        }
        POLdapOrganization otherOrg = (POLdapOrganization) other;
        return getKey() == otherOrg.getKey();
    }

    public int hashCode() {
        final long k = getKey();
        return (int) (k ^ (k >>> 32));
    }

}
