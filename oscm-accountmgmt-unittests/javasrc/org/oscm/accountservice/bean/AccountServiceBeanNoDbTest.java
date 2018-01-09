/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 16.11.2011                                                      
 *                                                                              
 *  Completion Time: 16.11.2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author Mike J&auml;ger
 * 
 */
public class AccountServiceBeanNoDbTest {

    private AccountServiceBean ab;
    private SessionContext sessionMock;
    private IdentityServiceLocal idMock;
    private ConfigurationServiceLocal cfs;
    private DataService dataServiceMock;

    @Before
    public void setup() throws Exception {
        ab = spy(new AccountServiceBean());
        sessionMock = mock(SessionContext.class);
        ab.sessionCtx = sessionMock;
        dataServiceMock = mock(DataService.class);
        ab.dm = dataServiceMock;
        idMock = mock(IdentityServiceLocal.class);
        ab.im = idMock;
        ab.imgSrv = mock(ImageResourceServiceLocal.class);
        ab.localizer = mock(LocalizerServiceLocal.class);
        cfs = mock(ConfigurationServiceLocal.class);
        ab.configService = cfs;

        when(Boolean.valueOf(cfs.isCustomerSelfRegistrationEnabled()))
                .thenReturn(Boolean.TRUE);
        doAnswer(new Answer<DomainObject<?>>() {

            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                return (DomainObject<?>) invocation.getArguments()[0];
            }
        }).when(dataServiceMock).find(Matchers.any(DomainObject.class));
        doAnswer(new Answer<DomainObject<?>>() {

            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                return (DomainObject<?>) invocation.getArguments()[0];
            }
        }).when(dataServiceMock)
                .getReferenceByBusinessKey(Matchers.any(DomainObject.class));
    }

    @Test
    public void registerCustomer_MailOperationException() throws Exception {
        Marketplace mp = new Marketplace();
        mp.setTenant(null);
        when(ab.getMarketplace(Mockito.anyString())).thenReturn(mp);

        Mockito.doThrow(new MailOperationException("Test")).when(idMock)
                .createOrganizationAdmin(Matchers.any(VOUserDetails.class),
                        Matchers.any(Organization.class), Matchers.anyString(),
                        Matchers.any(Long.class),
                        Matchers.any(Marketplace.class));
        try {
            VOOrganization org = new VOOrganization();
            org.setLocale("en");
            ab.registerCustomer(org, new VOUserDetails(), "secret", null,
                    "12345", null);
            fail("Mail processing must have failed");
        } catch (MailOperationException e) {
            verify(sessionMock, times(1)).setRollbackOnly();
        }
    }

    @Test(expected = RegistrationException.class)
    public void registerCustomer_DisableSelfRegistration() throws Exception {
        // when
        VOOrganization org = new VOOrganization();
        org.setLocale("en");
        when(Boolean.valueOf(cfs.isCustomerSelfRegistrationEnabled()))
                .thenReturn(Boolean.FALSE);
        // given
        try {
            ab.registerCustomer(org, new VOUserDetails(), "123456", null, null,
                    null);
        } catch (RegistrationException e) {
            assertEquals(
                    RegistrationException.Reason.SELFREGISTRATION_NOT_ALLOWED,
                    e.getReason());
            throw e;

        }
    }

    @Test
    public void registerCustomer_EnableSelfRegistration() throws Exception {
        // when
        VOOrganization org = new VOOrganization();
        org.setLocale("en");
        Marketplace mp = new Marketplace();
        mp.setTenant(null);
        when(ab.getMarketplace(Mockito.anyString())).thenReturn(mp);
        // given
        VOOrganization result = ab.registerCustomer(org, new VOUserDetails(),
                "123456", null, null, null);
        // then
        assertEquals("en", result.getLocale());
        verify(dataServiceMock, atLeast(1)).persist(any(Organization.class));
    }

    @Test
    public void getOrganizationDataFallback_En() throws Exception {
        // when
        VOOrganization org = new VOOrganization();
        org.setLocale("en");
        org.setKey(1);
        org.setDescription("en");
        doReturn(org).when(ab).getOrganizationData();
        // given
        VOOrganization result = ab.getOrganizationDataFallback();
        //
        assertEquals("en", result.getLocale());
        assertEquals("en", result.getDescription());
    }

    @Test
    public void getOrganizationDataFallback_DE() throws Exception {
        // when
        VOOrganization voOrg = new VOOrganization();
        voOrg.setLocale("de");
        voOrg.setKey(1);
        voOrg.setDescription("");
        doReturn(voOrg).when(ab).getOrganizationData();
        PlatformUser user = new PlatformUser();
        user.setLocale("de");
        user.setKey(1L);
        Organization org = new Organization();
        org.setKey(1L);
        user.setOrganization(org);
        doReturn(user).when(ab.dm).getCurrentUser();
        doReturn("123").when(ab).getEnDescription(eq(1L));
        // given
        VOOrganization result = ab.getOrganizationDataFallback();
        //
        assertEquals("de", result.getLocale());
        assertEquals("123", result.getDescription());
    }

    @Test
    public void createDefaultUserGroup() throws Exception {
        // given
        Organization org = new Organization();
        org.setKey(1L);

        // when
        UserGroup result = ab.createDefaultUserGroup(org);

        // then
        assertEquals(1, result.getOrganization_tkey());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.isDefault()));
        assertEquals("default", result.getName());
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_Supplier()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_TechnologyProvider()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_PlatformOperator()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(
                OrganizationRoleType.PLATFORM_OPERATOR,
                OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_Reseller()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(OrganizationRoleType.RESELLER,
                OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_Broker()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(OrganizationRoleType.BROKER,
                OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_MarketplaceOwner()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(
                OrganizationRoleType.MARKETPLACE_OWNER,
                OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    @Test
    public void deregisterOrganization_Customer()
            throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {
        // given
        Organization org = createOrganization(OrganizationRoleType.CUSTOMER);

        doReturn(org).when(ab).getOrganization();

        // when
        ab.deregisterOrganization();
    }

    private Organization createOrganization(OrganizationRoleType... roles) {
        Organization org = new Organization();
        org.setOrganizationId("id");
        for (OrganizationRoleType role : roles) {
            OrganizationRole orgRole = new OrganizationRole();
            orgRole.setRoleName(role);
            OrganizationToRole orgToRole = new OrganizationToRole();
            orgToRole.setOrganization(org);
            orgToRole.setOrganizationRole(orgRole);
            org.getGrantedRoles().add(orgToRole);
        }
        return org;
    }
}
