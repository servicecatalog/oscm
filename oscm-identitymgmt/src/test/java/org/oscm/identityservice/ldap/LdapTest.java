/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 01.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.ldap;

import java.util.List;
import java.util.Properties;

import javax.naming.Context;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOUserDetails;

@Ignore
public class LdapTest {

    @Test
    public void testLdap() throws Exception {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL,
                "ldap://estinfra1.lan.est.fujitsu.de:389");

        ILdapResultMapper<VOUserDetails> mapper = new ILdapResultMapper<VOUserDetails>() {

            @Override
            public String[] getAttributes() {
                return new String[] { "uid", "sn", "givenName",
                        "scalixEmailAddress" };
            }

            @Override
            public VOUserDetails map(String[] values) {
                VOUserDetails user = new VOUserDetails();
                user.setUserId(values[0]);
                user.setLastName(values[1]);
                user.setFirstName(values[2]);
                user.setEMail(values[3]);
                return user;
            }

        };

        LdapAccessServiceBean ldapAccess = new LdapAccessServiceBean();
        ldapAccess.cs = new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                ConfigurationSetting setting = new ConfigurationSetting();
                setting.setInformationId(ConfigurationKey.LDAP_SEARCH_LIMIT);
                setting.setValue("1");
                return setting;
            }
        };
        List<VOUserDetails> result = ldapAccess.search(props,
                "ou=people,dc=est,dc=fujitsu,dc=de", "uid=p*", mapper, false);
        Assert.assertFalse(result.isEmpty());
    }
}
