/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 26.03.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LdapTest {

    ADMRealmImpl realmImpl;

    private static final String LDAP_CTX_FACT = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String LDAP_URL = "ldap://estdevmail1.dev.est.fujitsu.com:389";
    private static final String LDAP_BASE_DN = "ou=people,dc=dev,dc=est,dc=fujitsu,dc=de";
    private static final String LDAP_USER = "cn=4711@dev.est.fujitsu.com BES Test User,ou=people,dc=dev,dc=est,dc=fujitsu,dc=de";
    private static final String LDAP_USER_PWD = "secret";

    Properties ldapProperties;

    @Before
    public void setup() throws Exception {
        Logger logger = Logger.getLogger(LdapTest.class.toString());
        logger.setLevel(Level.FINEST);
        realmImpl = new ADMRealmImpl(logger);

        ldapProperties = new Properties();
        ldapProperties.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CTX_FACT);
        ldapProperties.put(Context.PROVIDER_URL, LDAP_URL);
    }

    @Test
    public void testUserSearch() throws Exception {
        String filter = "cn=4711@dev.est.fujitsu.com BES Test User";

        String dn = realmImpl.userSearch(ldapProperties, LDAP_BASE_DN, filter);

        Assert.assertEquals(filter + "," + LDAP_BASE_DN, dn);
    }

    @Test
    public void testBindAsUser() throws Exception {
        Assert.assertTrue(realmImpl.bindAsUser(ldapProperties, LDAP_USER,
                LDAP_USER_PWD));
    }

    @Test
    public void testBindAsUserFail() throws Exception {
        ldapProperties.remove(Context.PROVIDER_URL);
        Assert.assertFalse(realmImpl.bindAsUser(ldapProperties, LDAP_USER, ""));
    }
}
