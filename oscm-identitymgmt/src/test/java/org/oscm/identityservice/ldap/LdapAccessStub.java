/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapAccessServiceLocal;

public class LdapAccessStub implements LdapAccessServiceLocal {

    private int platformUserCount = 0;

    private int organizationUnitCount = 0;

    private boolean throwNamingException = false;

    public int getPlatformUserCount() {
        return platformUserCount;
    }

    public int getOrganizationUnitCount() {
        return organizationUnitCount;
    }

    public void setThrowNamingException(boolean flag) {
        throwNamingException = flag;
    }

    public <T> List<T> search(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        if (throwNamingException) {
            throw new NamingException();
        }

        List<T> list = new ArrayList<T>();
        if (filter.endsWith("=")) {
            return list;
        }

        String[] values;
        String[] attrIds = mapper.getAttributes();
        values = new String[attrIds.length];
        for (int i = 0; i < attrIds.length; i++) {
            if (attrIds[i].equals("uid")) {
                values[i] = "pock";
            }
            if (attrIds[i].equals("scalixEmailAddress")) {
                values[i] = "peter.pock@est.fujitsu.com";
            }
        }
        list.add(mapper.map(values));
        if (filter.length() < 8) {
            values = new String[attrIds.length];
            for (int i = 0; i < attrIds.length; i++) {
                if (attrIds[i].equals("uid")) {
                    values[i] = "poppel";
                }
                if (attrIds[i].equals("scalixEmailAddress")) {
                    values[i] = "gerti.poppel@est.fujitsu.com";
                }
            }
            list.add(mapper.map(values));
        }
        return list;
    }

    public String dnSearch(Properties ldapProps, String baseDN, String filter)
            throws NamingException {
        return baseDN;
    }

    public <T> boolean searchOverLimit(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        if (throwNamingException) {
            throw new NamingException();
        }

        return false;
    }

}
