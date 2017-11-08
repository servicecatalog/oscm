/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                     
 *                                                                              
 *  Creation Date: 01.02.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NamingException;

import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapAccessServiceLocal;

/**
 * Test stub for an LDAP access.
 * 
 */
@Stateless
@Local(LdapAccessServiceLocal.class)
public class LdapAccessStub implements LdapAccessServiceLocal {

    public static final String[] ids = new String[] { "uid",
            "scalixEmailAddress", "givenName", "addName", "sn", "locale" };

    public static final String[][] allValues = new String[][] {
            { "jaeger", "mike.jaeger@est.fujitsu.com", "Mike", "", "Jaeger",
                    "de" },
            { "pock", "peter.pock@est.fujitsu.com", "Peter", "", "Pock", "en" },
            { "weiser", "ronny.weiser@est.fujitsu.com", "Ronny", "W", "Weiser",
                    "" } };

    /**
     * Execute an LDAP search and return the result.
     * 
     * @return the search result.
     */
    public <T> List<T> search(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {

        if ("invalid".equals(properties.getProperty(Context.PROVIDER_URL))) {
            throw new NamingException("Invalid URL.");
        }

        List<T> list = new ArrayList<T>();
        T t;

        String[] attrIds = mapper.getAttributes();

        if (filter.endsWith("*")) {
            t = mapper.map(getValues(attrIds, 0));
            list.add(t);
            t = mapper.map(getValues(attrIds, 1));
            list.add(t);
            t = mapper.map(getValues(attrIds, 2));
            list.add(t);
        } else if (filter.endsWith(allValues[0][0])) {
            t = mapper.map(getValues(attrIds, 0));
            list.add(t);
        } else if (filter.endsWith(allValues[1][0])) {
            t = mapper.map(getValues(attrIds, 1));
            list.add(t);
        } else if (filter.endsWith(allValues[2][0])) {
            t = mapper.map(getValues(attrIds, 2));
            list.add(t);
        }

        return list;
    }

    private String[] getValues(String[] attrIds, int idx) {
        String[] values = new String[attrIds.length];
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < ids.length; j++) {
                if (ids[j].equals(attrIds[i])) {
                    values[i] = allValues[idx][j];
                    break;
                }
            }
        }
        return values;
    }

    public String dnSearch(Properties ldapProps, String baseDN, String filter)
            throws NamingException {
        return baseDN;
    }

    public <T> boolean searchOverLimit(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException {
        return false;
    }

}
