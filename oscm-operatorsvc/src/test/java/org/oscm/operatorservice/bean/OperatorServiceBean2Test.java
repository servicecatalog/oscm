/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DuplicateTenantIdException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

public class OperatorServiceBean2Test {

    private OperatorServiceBean operatorServiceBean;
    private SessionContext sessionCtxMock;
    private AccountServiceLocal accountServiceMock;
    private DataService ds;
    private ConfigurationServiceLocal configurationServiceLocal;

    @Before
    public void setUp() throws Exception {
        operatorServiceBean = new OperatorServiceBean();
        sessionCtxMock = Mockito.mock(SessionContext.class);
        operatorServiceBean.sessionCtx = sessionCtxMock;
        accountServiceMock = Mockito.mock(AccountServiceLocal.class);
        operatorServiceBean.accMgmt = accountServiceMock;
        ds = mock(DataService.class);
        operatorServiceBean.dm = ds;
        configurationServiceLocal = mock(ConfigurationServiceLocal.class);
        operatorServiceBean.configService = configurationServiceLocal;
    }

    /**
     * BE07787
     */
    @Test
    public void registerOrganization_mailServerUnavailable() throws Exception {
        final VOOrganization organization = new VOOrganization();
        organization.setOrganizationId("MyOrg");
        organization.setName("MyOrganization");
        organization.setEmail("asm-ue-test@est.fujitsu.com");
        organization.setPhone("+49894711");
        organization.setUrl("http://www.fujitsu.com");
        organization.setAddress("Schwanthaler Str. 75a  Munich");
        organization.setLocale("en");
        organization.setOperatorRevenueShare(BigDecimal.valueOf(15));

        final VOUserDetails userDetails = new VOUserDetails();
        userDetails.setFirstName("Hans");
        userDetails.setLastName("Meier");
        userDetails.setEMail("asm-ue-test@est.fujitsu.com");
        userDetails.setUserId("admin");
        userDetails.setSalutation(Salutation.MR);
        userDetails.setPhone("(089) 123 456 78");
        userDetails.setLocale("de");

        when(
                accountServiceMock.registerOrganization(
                        any(Organization.class),
                        any(ImageResource.class),
                        any(VOUserDetails.class),
                        any(Properties.class),
                        any(String.class), any(String.class),
                        any(String.class),
                        any(OrganizationRoleType.class))).thenThrow(
                new MailOperationException("Mail cannot be sent"));

        try {
            operatorServiceBean.registerOrganization(organization, null,
                    userDetails, null, null, OrganizationRoleType.SUPPLIER);

            fail("MailOperationException expected");
        } catch (MailOperationException e) {
            // There must be a transaction rollback if the
            // mail server is unreachable
            verify(sessionCtxMock, times(1)).setRollbackOnly();
        }
    }

    // Issue #380
    @Test(expected = DuplicateTenantIdException.class)
    public void saveConfigurationSettingTest_duplicateTenant()
            throws OrganizationAuthoritiesException, DuplicateTenantIdException,
            ConcurrentModificationException, ValidationException {
        // given
        VOConfigurationSetting voSetting = mock(VOConfigurationSetting.class);
        when(voSetting.getInformationId()).thenReturn(ConfigurationKey.SSO_DEFAULT_TENANT_ID);
        Query mockQuery = mock(Query.class);
        List<Object[]> resultList = new ArrayList<>();

        // list not empty so duplicate tenantId found
        resultList.add(new Object[0]);
        when(mockQuery.getResultList()).thenReturn(resultList);
        when(ds.createNamedQuery("Tenant.findByBusinessKey")).thenReturn(mockQuery);
        // when
        operatorServiceBean.saveConfigurationSetting(voSetting);
    }

    // Issue #380
    @Test
    public void saveConfigurationSettingTest_Ok()
            throws OrganizationAuthoritiesException, DuplicateTenantIdException,
            ConcurrentModificationException, ValidationException {
        // given
        VOConfigurationSetting voSetting = mock(VOConfigurationSetting.class);
        when(voSetting.getContextId()).thenReturn("contextID");
        when(voSetting.getInformationId()).thenReturn(ConfigurationKey.SSO_DEFAULT_TENANT_ID);
        Query mockQuery = mock(Query.class);

        // list empty so duplicate tenantID not found
        List<Object[]> resultList = new ArrayList<>();
        when(mockQuery.getResultList()).thenReturn(resultList);
        when(ds.createNamedQuery("Tenant.findByBusinessKey")).thenReturn(mockQuery);

        ConfigurationSetting dbSetting = new ConfigurationSetting();
        ConfigurationKey mockConfigurationKey = ConfigurationKey.SSO_DEFAULT_TENANT_ID;
        dbSetting.setInformationId(mockConfigurationKey);
        when(configurationServiceLocal.getConfigurationSetting(
                any(ConfigurationKey.class), any(String.class)))
                        .thenReturn(dbSetting);
        // when
        operatorServiceBean.saveConfigurationSetting(voSetting);
    }
}
