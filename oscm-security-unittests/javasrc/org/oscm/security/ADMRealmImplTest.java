/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2015年3月30日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.directory.SearchResult;

import org.junit.Before;
import org.junit.Test;

/**
 * @author qiu
 * 
 */
public class ADMRealmImplTest {
    private ADMRealmImpl realmImpl;
    private Properties ldapProps;

    @Before
    public void setup() {
        Logger logger = Logger.getLogger(ADMRealmImplTest.class.toString());
        logger.setLevel(Level.FINEST);
        realmImpl = new ADMRealmImpl(logger);
        ldapProps = new Properties();
    }

    @Test
    public void retrieveName_relative() {
        // given
        SearchResult searchResult = new SearchResult(null, null, null, true);
        searchResult.setName("cn=ldap01");

        // when
        String name = realmImpl.retrieveName(ldapProps, searchResult);

        // then
        assertEquals("cn=ldap01", name);
    }

    @Test
    public void retrieveName_notRelative() {
        // given
        SearchResult searchResult = new SearchResult(null, null, null, false);
        searchResult.setNameInNamespace("cn=ldap01");
        searchResult
                .setName("ldap://estdevmail1.dev.est.fujitsu.com:389/cn=ldap01");
        ldapProps.put(Context.PROVIDER_URL, "");
        // when
        String name = realmImpl.retrieveName(ldapProps, searchResult);

        // then
        assertEquals("cn=ldap01", name);
        assertEquals("ldap://estdevmail1.dev.est.fujitsu.com:389",
                ldapProps.getProperty(Context.PROVIDER_URL));
    }

    @Test
    public void retrieveName_notRelative_Empty() {
        // given
        SearchResult searchResult = new SearchResult(null, null, null, false);
        searchResult.setNameInNamespace("cn=ldap01");
        searchResult.setName("");
        ldapProps.put(Context.PROVIDER_URL, "a");
        // when
        String name = realmImpl.retrieveName(ldapProps, searchResult);

        // then
        assertEquals("cn=ldap01", name);
        assertEquals("a", ldapProps.getProperty(Context.PROVIDER_URL));
    }
}
