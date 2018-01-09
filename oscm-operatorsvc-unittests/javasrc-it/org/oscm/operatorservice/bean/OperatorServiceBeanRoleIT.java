/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.bean.AccountServiceBean;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOperatorOrganization;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOTimerInfo;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.BillingServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.DataServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.LdapAccessServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.QueryStub;
import org.oscm.test.stubs.SearchServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.timerservice.bean.TimerServiceBean;

public class OperatorServiceBeanRoleIT extends EJBTestBase {

    private static final String ORGANIZATION_DESCRIPTION = "Enabling Software Technology";
    private static final String ORGANIZATION_NAME = "Fujitsu EST";

    private OperatorService operatorService;

    protected AccountService accountMgmt;
    protected AccountServiceLocal accountMgmtLocal;

    private Set<OrganizationRoleType> callerRolles = EnumSet
            .noneOf(OrganizationRoleType.class);

    // The following fields store the parameters and results of stub services:

    private Stack<DomainObject<?>> dataManager_getReferenceByBusinessKey_return = new Stack<>();
    private Stack<DomainObject<?>> dataManager_getReference_return = new Stack<>();
    private final List<DomainObject<?>> dataManager_persist_objects_orgreftopt = new ArrayList<>();
    private final List<DomainObject<?>> dataManager_persist_objects_triggerdef = new ArrayList<>();
    private final List<DomainObject<?>> dataManager_persist_objects_currencies = new ArrayList<>();
    private final List<DomainObject<?>> dataManager_persist_objects_organizations = new ArrayList<>();
    private final List<DomainObject<?>> dataManager_persist_objects_orgToRoles = new ArrayList<>();
    private final List<DomainObject<?>> dataManager_removed_objects = new ArrayList<>();
    private boolean dataManager_throwSaasNonUniqueBusinessKeyException = false;

    private VOUserDetails identityService_createdOrgAdmin = null;

    protected boolean userStatusChanged = false;
    private Organization organization = new Organization();
    private PlatformUser platformUser = new PlatformUser();
    private List<?> query_getResultList = Collections.emptyList();
    private Map<String, Object> queryParameters = new HashMap<>();
    protected boolean passwordReset;
    private List<BillingResult> billingResultList = new ArrayList<>();
    private OrganizationReference dataManaber_findOrgRef = null;

    private List<ConfigurationSetting> configSettings = new ArrayList<>();
    protected boolean configSettingSaved;
    private List<ConfigurationSetting> storedConfigSettings;
    protected boolean paymentProcessingStarted;
    private boolean paymentProcessingResult = true;

    protected ConfigurationServiceStub cs;

    @Override
    protected void setup(TestContainer container) throws Exception {
        userStatusChanged = false;
        container.enableInterfaceMocking(true);
        container.addBean(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                if (objectType == LocalizedObjectTypes.ORGANIZATION_DESCRIPTION) {
                    return ORGANIZATION_DESCRIPTION;
                }
                return "";
            }

            @Override
            public List<VOLocalizedText> getLocalizedValues(long objectKey,
                    LocalizedObjectTypes objectType) {
                return new ArrayList<>();
            }

            @Override
            public boolean storeLocalizedResource(String localeString,
                    long objectKey, LocalizedObjectTypes objectType,
                    String value) {
                return true;
            }

        });

        DataServiceStub dataServiceStub = new DataServiceStub() {

            @Override
            public void flush() {
            }

            @Override
            public PlatformUser getCurrentUser() {
                platformUser.setOrganization(organization);
                return platformUser;
            }

            @Override
            public DomainObject<?> find(DomainObject<?> idobj) {
                if (idobj instanceof OrganizationReference) {
                    OrganizationReference result = dataManaber_findOrgRef;
                    dataManaber_findOrgRef = (OrganizationReference) idobj;
                    return result;
                }
                return idobj;
            }

            @Override
            public DomainObject<?> getReferenceByBusinessKey(
                    DomainObject<?> findTemplate)
                    throws ObjectNotFoundException {
                if (dataManager_getReferenceByBusinessKey_return.isEmpty()) {
                    throw new ObjectNotFoundException(ClassEnum.ORGANIZATION,
                            "");
                }
                return dataManager_getReferenceByBusinessKey_return.pop();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends DomainObject<?>> T getReference(Class<T> objclass,
                    long key) {
                return (T) dataManager_getReference_return.pop();
            }

            @Override
            public void persist(DomainObject<?> obj)
                    throws NonUniqueBusinessKeyException {
                if (dataManager_throwSaasNonUniqueBusinessKeyException) {
                    throw new NonUniqueBusinessKeyException();
                }
                if (obj instanceof OrganizationRefToPaymentType) {
                    dataManager_persist_objects_orgreftopt.add(obj);
                }
                if (obj instanceof TriggerDefinition) {
                    dataManager_persist_objects_triggerdef.add(obj);
                }
                if (obj instanceof SupportedCurrency) {
                    dataManager_persist_objects_currencies.add(obj);
                }
                if (obj instanceof Organization) {
                    dataManager_persist_objects_organizations.add(obj);
                }
                if (obj instanceof OrganizationToRole) {
                    dataManager_persist_objects_orgToRoles.add(obj);
                    OrganizationToRole orgToRole = (OrganizationToRole) obj;

                    for (DomainObject<?> domObj : dataManager_persist_objects_organizations) {
                        Organization persistedOrg = (Organization) domObj;
                        if (persistedOrg.getOrganizationId().equals(orgToRole
                                .getOrganization().getOrganizationId())) {
                            persistedOrg.getGrantedRoles().add(orgToRole);
                        }
                    }
                }
            }

            @Override
            public Query createNamedQuery(String arg0) {
                return new QueryStub() {

                    @Override
                    public List<?> getResultList() {
                        return query_getResultList;
                    }

                    @Override
                    public Object getSingleResult() {
                        return query_getResultList.size() > 0
                                ? query_getResultList.get(0) : null;
                    }

                    @Override
                    public Query setMaxResults(int arg0) {
                        return null;
                    }

                    @Override
                    public Query setParameter(String arg0, Object arg1) {
                        queryParameters.put(arg0, arg1);
                        return this;
                    }

                };
            }

            @Override
            public Query createNativeQuery(String arg0) {
                return new QueryStub() {

                    @Override
                    public List<?> getResultList() {
                        return query_getResultList;
                    }

                    @Override
                    public Query setMaxResults(int arg0) {
                        return null;
                    }

                    @Override
                    public Query setParameter(String arg0, Object arg1) {
                        queryParameters.put(arg0, arg1);
                        return this;
                    }

                };
            }

            @Override
            public void remove(DomainObject<?> obj) {
                dataManager_removed_objects.add(obj);
            }

            @Override
            public void refresh(Object arg0) {
            }

        };
        dataServiceStub = spy(dataServiceStub);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass()
                        .equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub)
                .getReferenceByBusinessKey(any(DomainObject.class));
        container.addBean(dataServiceStub);

        container.addBean(new IdentityServiceStub() {
            @Override
            public void setUserAccountStatus(PlatformUser user,
                    UserAccountStatus newStatus) {
                userStatusChanged = true;
            }

            @Override
            public void resetPasswordForUser(PlatformUser user,
                    Marketplace marketplace) {
                passwordReset = true;
            }

            @Override
            public void createOrganizationAdmin(VOUserDetails userDetails,
                    Organization organization, String password, Long serviceKey,
                    Marketplace marketplace) {
                identityService_createdOrgAdmin = userDetails;
            }

        });

        container.addBean(new PaymentServiceStub() {
            @Override
            public boolean reinvokePaymentProcessing() {
                return true;
            }

            @Override
            public boolean chargeForOutstandingBills() {
                paymentProcessingStarted = true;
                return paymentProcessingResult;
            }
        });

        TimerServiceBean tsMock = mock(TimerServiceBean.class);
        when(tsMock.getCurrentTimerExpirationDates())
                .thenReturn(new ArrayList<VOTimerInfo>());
        container.addBean(tsMock);
        container.addBean(new BillingServiceStub() {
            @Override
            public boolean startBillingRun(long currentTime) {
                return true;
            }

            @Override
            public List<BillingResult> generateBillingForAnyPeriod(
                    long startOfPeriod, long endOfPeriod,
                    long organizationKey) {
                return billingResultList;
            }

        });

        container.addBean(cs = new ConfigurationServiceStub() {
            @Override
            public List<ConfigurationSetting> getAllConfigurationSettings() {
                return configSettings;
            }

            @Override
            public void setConfigurationSetting(ConfigurationSetting setting) {
                configSettingSaved = true;
                storedConfigSettings.add(setting);
            }

        });

        container.addBean(new SearchServiceStub());
        container.addBean(new ImageResourceServiceStub() {
            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
            }
        });

        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(mock(LdapSettingsManagementServiceLocal.class));

        MarketplaceServiceLocal marketplaceServiceLocal = mock(
                MarketplaceServiceLocal.class);
        when(marketplaceServiceLocal.getMarketplaceForId(anyString()))
                .thenReturn(new Marketplace("testMpl"));
        container.addBean(marketplaceServiceLocal);

        container.addBean(new AccountServiceBean());
        accountMgmt = container.get(AccountService.class);
        accountMgmtLocal = container.get(AccountServiceLocal.class);

        AuditLogServiceBean auditLogMock = mock(AuditLogServiceBean.class);
        when(auditLogMock.loadAuditLogs(Mockito.anyListOf(String.class),
                Mockito.anyLong(), Mockito.anyLong()))
                        .thenReturn(new String("").getBytes());
        container.addBean(auditLogMock);

        container.addBean(new OperatorServiceBean());

        operatorService = container.get(OperatorService.class);
        storedConfigSettings = new ArrayList<>();
    }

    // -------------------------------------------------------------
    // internal methods

    private VOUserDetails newVOUser() {
        return new VOUserDetails();
    }

    private VOOrganization newVOOrganization() {
        final VOOrganization org = new VOOrganization();
        fillVOOrganization(org);
        return org;
    }

    private VOOperatorOrganization newVOOperatorOrganization() {
        final VOOperatorOrganization org = new VOOperatorOrganization();
        fillVOOrganization(org);
        return org;
    }

    private void fillVOOrganization(final VOOrganization org) {
        org.setAddress("St.-Pauls-Viertel");
        org.setEmail("info@est.fujitsu.com");
        org.setLocale("de");
        org.setName(ORGANIZATION_NAME);
        org.setOrganizationId("est");
        org.setPhone("+49 89 360908-0");
        org.setDomicileCountry("DE");
        org.setUrl("http://est.fujitsu.com");
        org.setDescription(ORGANIZATION_DESCRIPTION);
    }

    @Test(expected = IncompatibleRolesException.class)
    public void registerOrganization_IncompatibleRoles() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        org.setOperatorRevenueShare(BigDecimal.valueOf(15));
        final VOUserDetails user = newVOUser();
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);

        runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                VOOrganization newOrg = operatorService.registerOrganization(
                        org, null, user, null, null,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.RESELLER);
                return newOrg;
            }
        });
    }

    /**
     * Update an Organization with the BROKER role.
     */
    @Test
    public void updateOrganization_Broker() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOperatorOrganization org = newVOOperatorOrganization();
        final VOUserDetails user = newVOUser();

        VOOrganization createdOrg = registerOrg(org, user, "Marketplace_1");

        org.setOrganizationId(createdOrg.getOrganizationId());
        List<OrganizationRoleType> roles = org.getOrganizationRoles();
        roles.add(OrganizationRoleType.BROKER);

        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));
        dataManager_getReferenceByBusinessKey_return
                .push(new SupportedCountry("en"));
        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));

        VOOperatorOrganization updatedOrg = runTX(
                new Callable<VOOperatorOrganization>() {
                    @Override
                    public VOOperatorOrganization call() throws Exception {
                        VOOperatorOrganization updOrg = operatorService
                                .updateOrganization(org, null);
                        return updOrg;
                    }
                });

        assertEquals("Wrong org ID in organization role",
                updatedOrg.getOrganizationId(),
                ((OrganizationToRole) dataManager_persist_objects_orgToRoles
                        .get(1)).getOrganization().getOrganizationId());

        assertEquals("Wrong organization role name",
                OrganizationRoleType.BROKER,
                ((OrganizationToRole) dataManager_persist_objects_orgToRoles
                        .get(1)).getOrganizationRole().getRoleName());
    }

    /**
     * Update an Organization with the RESELLER role.
     */
    @Test
    public void updateOrganization_Reseller() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOperatorOrganization org = newVOOperatorOrganization();
        final VOUserDetails user = newVOUser();

        VOOrganization createdOrg = registerOrg(org, user, "Marketplace_1");

        org.setOrganizationId(createdOrg.getOrganizationId());
        List<OrganizationRoleType> roles = org.getOrganizationRoles();
        roles.add(OrganizationRoleType.RESELLER);

        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));
        dataManager_getReferenceByBusinessKey_return
                .push(new SupportedCountry("en"));
        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));

        VOOperatorOrganization updatedOrg = runTX(
                new Callable<VOOperatorOrganization>() {
                    @Override
                    public VOOperatorOrganization call() throws Exception {
                        VOOperatorOrganization updOrg = operatorService
                                .updateOrganization(org, null);
                        return updOrg;
                    }
                });

        assertEquals("Wrong org ID in organization role",
                updatedOrg.getOrganizationId(),
                ((OrganizationToRole) dataManager_persist_objects_orgToRoles
                        .get(1)).getOrganization().getOrganizationId());

        assertEquals("Wrong organization role name",
                OrganizationRoleType.RESELLER,
                ((OrganizationToRole) dataManager_persist_objects_orgToRoles
                        .get(1)).getOrganizationRole().getRoleName());
    }

    /**
     * Update a SUPPLIER Organization with the RESELLER role.
     */
    @Test(expected = IncompatibleRolesException.class)
    public void updateOrganization_SupplierReseller() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOperatorOrganization org = newVOOperatorOrganization();
        org.setOperatorRevenueShare(BigDecimal.valueOf(15));
        final VOUserDetails user = newVOUser();

        VOOrganization createdOrg = registerOrg(org, user,
                OrganizationRoleType.SUPPLIER);

        org.setOrganizationId(createdOrg.getOrganizationId());
        List<OrganizationRoleType> roles = org.getOrganizationRoles();
        roles.add(OrganizationRoleType.RESELLER);

        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));
        dataManager_getReferenceByBusinessKey_return
                .push(new SupportedCountry("en"));
        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));

        runTX(new Callable<VOOperatorOrganization>() {
            @Override
            public VOOperatorOrganization call() throws Exception {
                VOOperatorOrganization updOrg = operatorService
                        .updateOrganization(org, null);
                return updOrg;
            }
        });
    }

    /**
     * Update a BROKER Organization with the RESELLER role.
     */
    @Test(expected = IncompatibleRolesException.class)
    public void updateOrganization_BrokerReseller() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOperatorOrganization org = newVOOperatorOrganization();
        final VOUserDetails user = newVOUser();

        VOOrganization createdOrg = registerOrg(org, user,
                OrganizationRoleType.BROKER);

        org.setOrganizationId(createdOrg.getOrganizationId());
        List<OrganizationRoleType> roles = org.getOrganizationRoles();
        roles.add(OrganizationRoleType.RESELLER);

        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));
        dataManager_getReferenceByBusinessKey_return
                .push(new SupportedCountry("en"));
        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));

        runTX(new Callable<VOOperatorOrganization>() {
            @Override
            public VOOperatorOrganization call() throws Exception {
                VOOperatorOrganization updOrg = operatorService
                        .updateOrganization(org, null);
                return updOrg;
            }
        });
    }

    /**
     * Update a BROKER Organization with the TECHNOLOGY_PROVIDER role.
     */
    @Test(expected = IncompatibleRolesException.class)
    public void updateOrganization_BrokerTechProvider() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOperatorOrganization org = newVOOperatorOrganization();
        final VOUserDetails user = newVOUser();

        VOOrganization createdOrg = registerOrg(org, user,
                OrganizationRoleType.BROKER);

        org.setOrganizationId(createdOrg.getOrganizationId());
        List<OrganizationRoleType> roles = org.getOrganizationRoles();
        roles.add(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));
        dataManager_getReferenceByBusinessKey_return
                .push(new SupportedCountry("en"));
        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));

        runTX(new Callable<VOOperatorOrganization>() {
            @Override
            public VOOperatorOrganization call() throws Exception {
                VOOperatorOrganization updOrg = operatorService
                        .updateOrganization(org, null);
                return updOrg;
            }
        });
    }

    /**
     * Add the supplier role to a broker organization
     */
    @Test(expected = IncompatibleRolesException.class)
    public void addOrganizationToRole_SupplierToBroker() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOperatorOrganization org = newVOOperatorOrganization();
        final VOUserDetails user = newVOUser();

        final VOOrganization createdOrg = registerOrg(org, user,
                OrganizationRoleType.BROKER);

        dataManager_getReferenceByBusinessKey_return
                .push(dataManager_persist_objects_organizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                operatorService.addOrganizationToRole(
                        createdOrg.getOrganizationId(),
                        OrganizationRoleType.SUPPLIER);
                return null;
            }
        });
    }

    private VOOrganization registerOrg(final VOOrganization org,
            final VOUserDetails user, OrganizationRoleType... roles)
            throws Exception {
        return registerOrg(org, user, null, roles);
    }

    private VOOrganization registerOrg(final VOOrganization org,
            final VOUserDetails user, final String marketplaceID,
            OrganizationRoleType... roles) throws Exception {
        final OrganizationRoleType[] orgRoles;
        if (roles == null) {
            orgRoles = new OrganizationRoleType[0];
        } else {
            orgRoles = roles;
        }

        dataManager_getReferenceByBusinessKey_return
                .add(new OrganizationRole(OrganizationRoleType.SUPPLIER));

        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);

        if (orgRoles.length == 0 && marketplaceID != null) {
            Marketplace marketplace = new Marketplace();
            dataManager_getReferenceByBusinessKey_return.add(marketplace);
        }

        dataManager_getReferenceByBusinessKey_return.add(pt);

        // set marketplace to pass the validation of marketplaceID
        if (orgRoles.length == 0 && marketplaceID != null) {
            Marketplace marketplace = new Marketplace();
            dataManager_getReferenceByBusinessKey_return.add(marketplace);
        }

        VOOrganization createdOrg = runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                VOOrganization newOrg = operatorService.registerOrganization(
                        org, null, user, null, marketplaceID, orgRoles);
                return newOrg;
            }
        });

        Organization persistedOrg = (Organization) dataManager_persist_objects_organizations
                .get(0);
        String persistedOrgId = persistedOrg.getOrganizationId();

        assertEquals("Wrong persisted organization ID",
                createdOrg.getOrganizationId(), persistedOrgId);
        assertEquals("Wrong persisted organization name", ORGANIZATION_NAME,
                persistedOrg.getName());

        List<OrganizationRoleType> persistedRoleTypes = new ArrayList<>();
        for (DomainObject<?> domObj : dataManager_persist_objects_orgToRoles) {
            OrganizationToRole orgToRole = (OrganizationToRole) domObj;

            assertEquals("Wrong org ID in persisted organization role",
                    persistedOrgId,
                    orgToRole.getOrganization().getOrganizationId());
            persistedRoleTypes
                    .add(orgToRole.getOrganizationRole().getRoleName());
        }

        assertTrue("Role CUSTOMER missing in persisted role types",
                persistedRoleTypes.contains(OrganizationRoleType.CUSTOMER));

        for (OrganizationRoleType role : orgRoles) {
            assertTrue(
                    "Role " + role.name() + " missing in persisted role types",
                    persistedRoleTypes.contains(role));
        }

        assertEquals("Wrong persisted organization admin",
                identityService_createdOrgAdmin.getOrganizationId(),
                persistedOrgId);

        return createdOrg;
    }

}
