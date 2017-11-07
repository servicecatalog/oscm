/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;

import javax.ejb.SessionContext;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationSetting;
import org.oscm.domobjects.PlatformUser;
import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author weiser
 * 
 */
public class IdentityServiceBeanLdapTest {

    private IdentityServiceBean bean;
    private PlatformUser user;
    private UserGroupServiceLocalBean userGroupServiceLocalBean;

    @Before
    public void setup() throws Exception {
        bean = spy(new IdentityServiceBean());

        user = new PlatformUser();
        Organization ldapManagedOrg = new Organization();
        ldapManagedOrg.setRemoteLdapActive(true);
        user.setOrganization(ldapManagedOrg);

        DataService ds = mock(DataService.class);
        when(ds.getCurrentUser()).thenReturn(user);
        bean.dm = ds;
        bean.ldapAccess = mock(LdapAccessServiceLocal.class);
        bean.cm = mock(CommunicationServiceLocal.class);
        bean.sessionCtx = mock(SessionContext.class);
        bean.ldapSettingsMS = mock(LdapSettingsManagementServiceLocal.class);
        ConfigurationServiceLocal cs = mock(ConfigurationServiceLocal.class);

        doReturn(
                new ConfigurationSetting(ConfigurationKey.AUTH_MODE,
                        Configuration.GLOBAL_CONTEXT, "INTERNAL")).when(cs)
                .getConfigurationSetting(any(ConfigurationKey.class),
                        anyString());

        bean.cs = cs;

        doNothing().when(bean).syncUserWithLdap(any(PlatformUser.class));

        userGroupServiceLocalBean = mock(UserGroupServiceLocalBean.class);
        bean.userGroupService = userGroupServiceLocalBean;
    }

    @Test
    public void refreshLdapUser_NoLdap() throws Exception {
        // disable LDAP managed organization setting
        user.getOrganization().setRemoteLdapActive(false);

        // when
        bean.refreshLdapUser();

        // then
        verify(bean, never()).syncUserWithLdap(any(PlatformUser.class));
    }

    @Test
    public void refreshLdapUser() throws Exception {
        // given - organization with LDAP integration active
        OrganizationSetting os = new OrganizationSetting();
        os.setSettingType(SettingType.LDAP_URL);
        user.getOrganization().getOrganizationSettings().add(os);

        // when
        bean.refreshLdapUser();

        // then
        verify(bean, times(1)).syncUserWithLdap(any(PlatformUser.class));
    }

    /**
     * Bug 9190 - MailOperationException must not be converted to
     * SaasSystemException
     */
    @Test(expected = ValidationException.class)
    public void importLdapUsers_notAllMandatoryAttributesGiven()
            throws Exception {
        // given
        VOUserDetails u = createUser();
        userFoundSendMailFailed(u);

        // when
        bean.importLdapUsers(Arrays.asList(u), "marketplaceId");
    }

    /**
     * Bug 9190 - MailOperationException must not be converted to
     * SaasSystemException
     */
    @Test(expected = MailOperationException.class)
    public void importLdapUsers_MailOperationException() throws Exception {
        // given
        VOUserDetails u = createUser();
        userFoundSendMailFailed(u);

        // when
        LdapConnector connectorMock = mock(LdapConnector.class);
        doReturn(connectorMock).when(bean).getLdapConnector(
                any(Properties.class));
        try {
            bean.importLdapUsers(Arrays.asList(u), "marketplaceId");
            fail();
        } catch (MailOperationException ex) {
            verify(connectorMock, times(1))
                    .ensureAllMandatoryLdapPropertiesPresent();
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private void userFoundSendMailFailed(VOUserDetails u)
            throws NamingException, MailOperationException {
        // let LDAP search return the user passed in
        when(
                bean.ldapAccess
                        .search(any(Properties.class), anyString(),
                                anyString(), any(ILdapResultMapper.class),
                                anyBoolean())).thenReturn(Arrays.asList(u));
        // then throw an exception when trying to send a mail
        doThrow(new MailOperationException()).when(bean.cm).sendMail(
                any(PlatformUser.class), any(EmailType.class),
                any(Object[].class), any(Marketplace.class));
    }

    private VOUserDetails createUser() {
        VOUserDetails u = new VOUserDetails();
        u.setEMail("mail@mail.de");
        u.setLocale("en");
        u.setOrganizationId("organizationId");
        u.setUserId("userId");
        return u;
    }
}
