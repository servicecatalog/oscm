/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 31.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.naming.NameNotFoundException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
public class LdapConnectorTest {

    private LdapAccessServiceLocal ldapAccess;

    @Before
    public void setup() throws Exception {

        ldapAccess = mock(LdapAccessServiceLocal.class);

    }

    /**
     * Bug 9280 - in case the user was not found, an suitable exception must be
     * thrown
     */
    @Test(expected = ValidationException.class)
    public void validateLdapProperties_dnSearchReturnedNull() throws Exception {
        LdapConnector connector = new LdapConnector(ldapAccess,
                new Properties());
        when(
                ldapAccess.dnSearch(any(Properties.class), anyString(),
                        anyString())).thenReturn(null);
        VOUserDetails user = new VOUserDetails();
        user.setUserId("user1");

        try {
            connector.validateLdapProperties(user);
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.LDAP_USER_NOT_FOUND, e.getReason());
            assertEquals(user.getUserId(), e.getMessageParams()[0]);
            // verify(asb.sessionCtx, times(1)).setRollbackOnly();
            throw e;
        }
    }

    /**
     * Bug 9280 - in case a NameNotFoundException probably the base DN is wrong
     * so throw a suitable exception.
     */
    @Test(expected = ValidationException.class)
    public void validateLdapProperties_NameNotFoundException() throws Exception {
        final String baseDN = "baseDN";
        final Properties props = new Properties();
        props.put(SettingType.LDAP_BASE_DN.name(), baseDN);

        final LdapConnector connector = new LdapConnector(ldapAccess, props);

        final String extCause = "some external cause";
        when(
                ldapAccess.dnSearch(any(Properties.class), anyString(),
                        anyString())).thenThrow(
                new NameNotFoundException(extCause));
        try {
            connector.validateLdapProperties(new VOUserDetails());
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.LDAP_BASE_DN_INVALID, e.getReason());
            assertEquals(baseDN, e.getMessageParams()[0]);
            // verify(asb.sessionCtx, times(1)).setRollbackOnly();
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void ensureAllMandatoryLdapPropertiesPresent() throws Throwable {
        final LdapConnector connector = new LdapConnector(ldapAccess,
                new Properties());
        try {
            connector.ensureAllMandatoryLdapPropertiesPresent();
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.LDAP_MANDATORY_PROPERTY_MISSING,
                    e.getReason());
            assertNotNull(e.getMessageParams());
            assertEquals(1, e.getMessageParams().length);
            for (String missingProp : SettingType.LDAP_ATTRIBUTES_MANDATORY) {
                assertTrue(e.getMessageParams()[0].contains(missingProp));
            }
            throw e;
        }
    }

}
