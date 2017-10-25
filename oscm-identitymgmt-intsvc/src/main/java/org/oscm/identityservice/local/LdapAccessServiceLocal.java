/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                            
 *                                                                              
 *  Creation Date: 01.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.local;

import java.util.List;
import java.util.Properties;

import javax.ejb.Local;
import javax.naming.NamingException;

@Local
public interface LdapAccessServiceLocal {

    /**
     * Performs an LDAP search with the properties.
     * 
     * @param properties
     *            Properties for the LDAP access
     * @param baseDN
     *            base data name for the search
     * @param filter
     *            search filter
     * @param mapper
     *            LDAP result mapper
     * @param checkAttribute
     *            if <code>true</code> an exception will be thrown if the
     *            required attribute wasn't returned by the search.
     * @throws NamingException
     */
    public <T> List<T> search(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException;

    /**
     * Search for users by "dn" attribute
     * 
     * @param ldapProps
     *            the LDAP properties
     * @param baseDN
     *            the base data name
     * @param filter
     *            the search filter
     * 
     * @return the found data name of the user
     * @throws NamingException
     */
    public String dnSearch(Properties ldapProps, String baseDN, String filter)
            throws NamingException;

    /**
     * Judge if search results more than search limit or not.
     * 
     * @param properties
     *            Properties for the LDAP access
     * @param baseDN
     *            base data name for the search
     * @param filter
     *            search filter
     * @param mapper
     *            LDAP result mapper
     * @param checkAttribute
     *            if <code>true</code> an exception will be thrown if the
     *            required attribute wasn't returned by the search.
     * @return search results more than search limit or not
     * @throws NamingException
     */
    public <T> boolean searchOverLimit(Properties properties, String baseDN,
            String filter, ILdapResultMapper<T> mapper, boolean checkAttribute)
            throws NamingException;

}
