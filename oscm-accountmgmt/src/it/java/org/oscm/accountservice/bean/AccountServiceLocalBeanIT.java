/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

public class AccountServiceLocalBeanIT extends EJBTestBase {

    private AccountServiceBean asl;
    private VOUserDetails user;
    private Organization orgToRegister;

    @Captor
    ArgumentCaptor<DomainObject<?>> storedValues;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(spy(new AccountServiceBean()));

        addMocks();

        asl = container.get(AccountServiceBean.class);
        doReturn(Boolean.FALSE).when(asl)
                .checkIfPlatformUserInGivenTenantExists(anyLong(), anyString());

        user = new VOUserDetails();
        user.setUserId("user1");

        orgToRegister = new Organization();
    }

    @Test
    public void registerOrganization_NoLdapUsed() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                asl.registerOrganization(orgToRegister, null, user, null, "DE",
                        "mId", null, OrganizationRoleType.CUSTOMER);
                return null;
            }
        });
        Organization org = validateStoredOrganization();
        assertFalse(org.isRemoteLdapActive());
    }

    @Test
    public void registerOrganization_LdapUsed() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                asl.registerOrganization(orgToRegister, null, user,
                        new Properties(), "DE", "mId", null,
                        OrganizationRoleType.CUSTOMER);
                return null;
            }
        });
        Organization org = validateStoredOrganization();
        assertTrue(org.isRemoteLdapActive());
    }

    private Properties getLdapProperties() {
        Properties properties = new Properties();
        properties.setProperty(SettingType.LDAP_URL.name(),
                "ldap://somehost:389");
        properties.setProperty(SettingType.LDAP_BASE_DN.name(),
                "uid=user,o=company");
        properties.setProperty(SettingType.LDAP_CONTEXT_FACTORY.name(),
                "defaultContextFactory");
        properties.setProperty(SettingType.LDAP_ATTR_UID.name(), "uid");
        return properties;
    }

    @SuppressWarnings("unchecked")
    private void addMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        LocalizerServiceLocal localizerMock = mock(LocalizerServiceLocal.class);
        doReturn("").when(localizerMock).getLocalizedTextFromDatabase(
                anyString(), anyLong(), any(LocalizedObjectTypes.class));

        DataService ds = mock(DataService.class);
        doAnswer(new Answer<DomainObject<?>>() {
            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                DomainObject<?> arg = (DomainObject<?>) invocation
                        .getArguments()[0];
                if (arg instanceof PaymentType) {
                    return new PaymentType();
                } else if (arg instanceof Marketplace) {
                    return new Marketplace();
                }
                return null;
            }
        }).when(ds).getReferenceByBusinessKey(any(DomainObject.class));
        doAnswer(new Answer<DomainObject<?>>() {
            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                DomainObject<?> arg = (DomainObject<?>) invocation
                        .getArguments()[0];
                if (arg instanceof OrganizationRole) {
                    return new OrganizationRole();
                } else if (arg instanceof SupportedCountry) {
                    return new SupportedCountry();
                }
                return null;
            }
        }).when(ds).find(any(DomainObject.class));
        doNothing().when(ds).persist(storedValues.capture());

        LdapAccessServiceLocal ldapAccess = mock(LdapAccessServiceLocal.class);
        doReturn(Collections.singletonList(new VOUserDetails())).when(
                ldapAccess).search(any(Properties.class), anyString(),
                anyString(), any(ILdapResultMapper.class), anyBoolean());
        doReturn("user1").when(ldapAccess).dnSearch(any(Properties.class),
                anyString(), anyString());

        LdapSettingsManagementServiceLocal ldapSettingsMgmt = mock(LdapSettingsManagementServiceLocal.class);
        doReturn(getLdapProperties()).when(ldapSettingsMgmt)
                .getOrganizationSettingsResolved(anyString());
        when(ldapSettingsMgmt.getDefaultValueForSetting(any(SettingType.class)))
                .thenReturn("someDefault");

        MarketplaceServiceLocal mplService = mock(MarketplaceServiceLocal.class);
        doReturn(getMarketplace("TestMpl")).when(mplService)
                .getMarketplaceForId(anyString());

        container.addBean(ldapSettingsMgmt);
        container.addBean(localizerMock);
        container.addBean(ds);
        container.addBean(ldapAccess);
        container.addBean(mplService);
    }

    private Organization validateStoredOrganization() {
        Organization org = null;
        int orgCount = 0;
        for (DomainObject<?> entry : storedValues.getAllValues()) {
            if (entry instanceof Organization) {
                orgCount++;
                org = (Organization) entry;
            }
        }
        assertEquals(1, orgCount);
        return org;
    }

    private Marketplace getMarketplace(String mplId) {
        return new Marketplace(mplId);
    }
}
