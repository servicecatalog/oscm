/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.PaymentDataException.Reason;
import org.oscm.internal.vo.*;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;
import org.oscm.timerservice.bean.TimerServiceBean;

import javax.ejb.EJBException;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Ignore
public class OperatorServiceBeanIT extends EJBTestBase {

    private static final String ORGANIZATION_DESCRIPTION = "Enabling Software Technology";
    private static final String TOO_LONG_DN = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890abc";
    private final List<DomainObject<?>> dataManager_persist_objects_orgreftopt = new ArrayList<DomainObject<?>>();
    private final List<DomainObject<?>> dataManager_persist_objects_triggerdef = new ArrayList<DomainObject<?>>();

    // The following fields store the parameters and results of stub services:
    private final List<DomainObject<?>> dataManager_persist_objects_currencies = new ArrayList<DomainObject<?>>();
    private final List<DomainObject<?>> dataManager_removed_objects = new ArrayList<DomainObject<?>>();
    private final List<String> operationList = new ArrayList<String>();
    public boolean queryLimit = false;
    private OperatorService operatorService;
    private AuditLogServiceBean auditLogMock;
    private Set<OrganizationRoleType> callerRolles = EnumSet
        .noneOf(OrganizationRoleType.class);
    private Stack<DomainObject<?>> dataManager_getReferenceByBusinessKey_return = new Stack<DomainObject<?>>();
    private Stack<DomainObject<?>> dataManager_getReference_return = new Stack<DomainObject<?>>();
    private OrganizationRoleType[] accountManagement_registerOrganization_roles;
    private Properties accountManagement_registerOrganization_properties;
    private VOUserDetails accountManagement_registerOrganization_user;
    private String accountManagement_addOrganizationToRole_organizationId;
    private OrganizationRoleType accountManagement_addOrganizationToRole_role;
    private String accountManagement_registerOrganization_description;
    private boolean userStatusChanged = false;
    private Organization organization = new Organization();
    private PlatformUser platformUser = new PlatformUser();
    private List<?> query_getResultList = Collections.emptyList();
    private boolean throwNonUniqueDistinguishedName;
    private Map<String, Object> queryParameters = new HashMap<String, Object>();
    private boolean passwordReset;
    private List<BillingResult> billingResultList = new ArrayList<BillingResult>();
    private OrganizationReference dataManaber_findOrgRef = null;
    private boolean dataManager_throwSaasNonUniqueBusinessKeyException = false;
    private List<ConfigurationSetting> configSettings = new ArrayList<ConfigurationSetting>();
    private boolean configSettingSaved;
    private List<ConfigurationSetting> storedConfigSettings;
    private boolean paymentProcessingStarted;
    private boolean paymentProcessingResult = true;
    private ConfigurationServiceStub cs;
    private DataServiceStub dataServiceStub;

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
        });

        dataServiceStub = new DataServiceStub() {
            @Override
            public Query createQuery(String jpql) {
                Query userLimitQuery = mock(Query.class);
                doReturn(query_getResultList).when(userLimitQuery).getResultList();
                return userLimitQuery;
            }

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
            public <T extends DomainObject<?>> T getReference(
                Class<T> objclass, long key) {
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
            }

            @Override
            public Query createNamedQuery(String arg0) {
                return new QueryStub() {

                    @Override
                    public List<?> getResultList() {
                        return query_getResultList;
                    }

                    @Override
                    public Query setMaxResults(int arg0) {
                        queryLimit = true;
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
        };
        dataServiceStub = spy(dataServiceStub);
        container.addBean(dataServiceStub);
        IdentityServiceLocal mock = mock(IdentityServiceLocal.class);
        container.addBean(mock);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                userStatusChanged = true;
                return null;
            }
        }).when(mock).setUserAccountStatus(any(PlatformUser.class), any(UserAccountStatus.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                passwordReset = true;
                return null;
            }
        }).when(mock).resetPasswordForUser(any(PlatformUser.class), any(Marketplace.class));

        container.addBean(new AccountServiceStub() {

            @Override
            public Organization registerOrganization(Organization organization,
                                                     ImageResource imageResource, VOUserDetails user,
                                                     Properties properties, String domicileCountry,
                                                     String marketplaceId, String description,
                                                     OrganizationRoleType... roles) {
                accountManagement_registerOrganization_user = user;
                accountManagement_registerOrganization_properties = properties;
                accountManagement_registerOrganization_roles = roles;
                accountManagement_registerOrganization_description = description;
                organization.setKey(12345);
                organization.setOrganizationId("est12345");
                platformUser.setOrganization(organization);
                for (OrganizationRoleType type : roles) {
                    OrganizationToRole orgToRole = new OrganizationToRole();
                    orgToRole.setOrganizationRole(new OrganizationRole(type));
                    organization.getGrantedRoles().add(orgToRole);
                }
                return organization;
            }

            @Override
            public Organization addOrganizationToRole(String organizationId,
                                                      OrganizationRoleType role) {
                accountManagement_addOrganizationToRole_organizationId = organizationId;
                accountManagement_addOrganizationToRole_role = role;
                return organization;
            }

            @Override
            public void checkDistinguishedName(Organization organization)
                throws DistinguishedNameException {
                if (throwNonUniqueDistinguishedName) {
                    throw new DistinguishedNameException();
                }
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
        when(tsMock.getCurrentTimerExpirationDates()).thenReturn(
            new ArrayList<VOTimerInfo>());
        container.addBean(tsMock);
        BillingServiceLocal billingService = mock(BillingServiceLocal.class);
        doReturn(true).when(billingService).startBillingRun(anyLong());
        doReturn(billingResultList).when(billingService).generateBillingForAnyPeriod(anyLong(), anyLong(), anyLong());

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
        auditLogMock = mock(AuditLogServiceBean.class);

        when(
            auditLogMock.loadAuditLogs(Mockito.anyListOf(String.class),
                Mockito.anyLong(), Mockito.anyLong())).thenReturn(
            new String("").getBytes());
        container.addBean(auditLogMock);
        container.addBean(new OperatorServiceBean());

        operatorService = container.get(OperatorService.class);
        storedConfigSettings = new ArrayList<ConfigurationSetting>();
    }

    // === registerOrganization ================================================

    /**
     * Successfully register an Organization.
     */
    @Test
    public void testRegisterOrganization() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass().equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub).getReferenceByBusinessKey(any(DomainObject.class));
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        org.setOperatorRevenueShare(BigDecimal.valueOf(15));
        final VOUserDetails user = newVOUser();
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);
        VOOrganization createdOrg = operatorService.registerOrganization(org,
            null, user, null, null, OrganizationRoleType.SUPPLIER,
            OrganizationRoleType.TECHNOLOGY_PROVIDER);

        assertSame(user, accountManagement_registerOrganization_user);
        assertNull(accountManagement_registerOrganization_properties);
        assertEquals(org.getDescription(),
            accountManagement_registerOrganization_description);
        assertEquals(Arrays.asList(OrganizationRoleType.SUPPLIER,
            OrganizationRoleType.TECHNOLOGY_PROVIDER),
            Arrays.asList(accountManagement_registerOrganization_roles));

        assertEquals(org.getAddress(), createdOrg.getAddress());
        assertEquals(org.getEmail(), createdOrg.getEmail());
        assertEquals(12345, createdOrg.getKey(), .0);
        assertEquals(org.getLocale(), createdOrg.getLocale());
        assertEquals(org.getName(), createdOrg.getName());
        assertEquals("est12345", createdOrg.getOrganizationId());
        assertEquals(org.getPhone(), createdOrg.getPhone());
        assertEquals(ORGANIZATION_DESCRIPTION, createdOrg.getDescription());
    }

    /**
     * Successfully register an Organization, make sure LDAP properties are
     * passed to account service to be invoked.
     */
    @Test
    public void testRegisterOrganizationUsingLdapProperties() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        dataManager_getReferenceByBusinessKey_return.add(new Marketplace());

        LdapProperties props = new LdapProperties();
        props.setProperty(SettingType.LDAP_BASE_DN.toString(),
            "ou=people,dc=est,dc=fujitsu,dc=de");
        props.setProperty(SettingType.LDAP_URL.toString(),
            "ldap://estinfra1.lan.est.fujitsu.de:389");
        props.setProperty(SettingType.LDAP_ATTR_UID.toString(), "uid");

        operatorService.registerOrganization(org, null, user, props,
            "marketplace_1", new OrganizationRoleType[]{});
        assertNotNull(accountManagement_registerOrganization_properties);
        assertEquals(props.asProperties(),
            accountManagement_registerOrganization_properties);
    }

    @Test
    public void testRegisterOrganizationSupplier_VerifyInvoice()
        throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass().equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub).getReferenceByBusinessKey(any(DomainObject.class));
        OrganizationRefToPaymentType cpt = verifyInvoice();
        assertFalse(cpt.isUsedAsDefault());
        assertFalse(cpt.isUsedAsServiceDefault());
    }

    @Test
    public void testRegisterOrganizationSupplier_VerifyInvoiceDefault()
        throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass().equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub).getReferenceByBusinessKey(any(DomainObject.class));
        cs.setConfigurationSetting(
            ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT, "true");
        OrganizationRefToPaymentType cpt = verifyInvoice();
        assertTrue(cpt.isUsedAsDefault());
        assertTrue(cpt.isUsedAsServiceDefault());
    }

    private OrganizationRefToPaymentType verifyInvoice()
        throws NonUniqueBusinessKeyException, ObjectNotFoundException,
        ValidationException, OrganizationAuthorityException,
        IncompatibleRolesException, MailOperationException,
        OrganizationAuthoritiesException, ImageException {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        org.setOperatorRevenueShare(BigDecimal.valueOf(15));
        final VOUserDetails user = newVOUser();
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        dataManager_getReferenceByBusinessKey_return.add(pt);
        operatorService.registerOrganization(org, null, user, null, null,
            OrganizationRoleType.SUPPLIER);

        assertEquals(1, dataManager_persist_objects_orgreftopt.size());
        DomainObject<?> domainObject = dataManager_persist_objects_orgreftopt
            .get(0);
        assertTrue(domainObject instanceof OrganizationRefToPaymentType);
        OrganizationRefToPaymentType cpt = (OrganizationRefToPaymentType) domainObject;
        assertEquals(OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
            cpt.getOrganizationReference().getReferenceType());
        assertEquals(PaymentType.INVOICE, cpt.getPaymentType()
            .getPaymentTypeId());
        return cpt;
    }

    @Test
    public void testRegisterOrganizationTechnologyProvider_VerifyInvoice()
        throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass().equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub).getReferenceByBusinessKey(any(DomainObject.class));
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        dataManager_getReferenceByBusinessKey_return.add(pt);
        operatorService.registerOrganization(org, null, user, null, null,
            OrganizationRoleType.TECHNOLOGY_PROVIDER);

        assertTrue(dataManager_persist_objects_orgreftopt.isEmpty());
    }

    /**
     * Tries to invoke the register organization functionality as a non platform
     * operator.
     */
    @Test(expected = EJBException.class)
    public void testRegisterOrganizationNotAuthorized() throws Exception {
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        operatorService.registerOrganization(org, null, user, null, null);
    }

    /**
     * Pass a null object as roles.
     */
    @Test
    public void testRegisterOrganizationNullRoles() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        final OrganizationRoleType[] roles = null;
        dataManager_getReferenceByBusinessKey_return.add(new Marketplace());
        operatorService.registerOrganization(org, null, user, null,
            "marketplace_1", roles);
    }

    /**
     * Pass an empty array as roles.
     */
    @Test
    public void testRegisterOrganizationEmptyRoles() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        final OrganizationRoleType[] roles = new OrganizationRoleType[0];
        dataManager_getReferenceByBusinessKey_return.add(new Marketplace());
        operatorService.registerOrganization(org, null, user, null,
            "marketplace_1", roles);
    }

    /**
     * Tries to register an organization as customer. The operation is expected
     * to fail.
     */
    @Test(expected = EJBException.class)
    public void testRegisterOrganizationNewCustomer() throws Exception {
        container.login("me");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        operatorService.registerOrganization(org, null, user, null, null,
            OrganizationRoleType.CUSTOMER);
    }

    /**
     * Tries to register an organization as platform operator. The operation is
     * expected to fail.
     */
    @Test(expected = EJBException.class)
    public void testRegisterOrganizationNewPlatformOperator() throws Exception {
        container.login("me");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        operatorService.registerOrganization(org, null, user, null, null,
            OrganizationRoleType.PLATFORM_OPERATOR);
    }

    /**
     * Tries to register an organization as a supplier and customer. The
     * operation is expected to fail.
     */
    @Test(expected = EJBException.class)
    public void testRegisterOrganizationNewSupplierAndCustomer()
        throws Exception {
        container.login("me");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        operatorService.registerOrganization(org, null, user, null, null,
            OrganizationRoleType.SUPPLIER, OrganizationRoleType.CUSTOMER);
    }

    /**
     * Successfully register an Organization as Broker
     */
    @Test
    public void registerOrganization_Broker() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass().equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub).getReferenceByBusinessKey(any(DomainObject.class));
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);
        VOOrganization createdOrg = operatorService.registerOrganization(org,
            null, user, null, null, OrganizationRoleType.BROKER);

        assertSame(user, accountManagement_registerOrganization_user);
        assertNull(accountManagement_registerOrganization_properties);
        assertEquals(org.getDescription(),
            accountManagement_registerOrganization_description);
        assertEquals(Arrays.asList(OrganizationRoleType.BROKER),
            Arrays.asList(accountManagement_registerOrganization_roles));

        assertEquals(org.getAddress(), createdOrg.getAddress());
        assertEquals(org.getEmail(), createdOrg.getEmail());
        assertEquals(12345, createdOrg.getKey(), .0);
        assertEquals(org.getLocale(), createdOrg.getLocale());
        assertEquals(org.getName(), createdOrg.getName());
        assertEquals("est12345", createdOrg.getOrganizationId());
        assertEquals(org.getPhone(), createdOrg.getPhone());
        assertEquals(ORGANIZATION_DESCRIPTION, createdOrg.getDescription());
    }

    /**
     * Successfully register an Organization as Reseller
     */
    @Test
    public void registerOrganization_Reseller() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getArguments()[0].getClass().equals(PlatformUser.class)) {
                    throw new ObjectNotFoundException();
                }
                return invocation.callRealMethod();
            }
        }).when(dataServiceStub).getReferenceByBusinessKey(any(DomainObject.class));
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        final VOOrganization org = newVOOrganization();
        final VOUserDetails user = newVOUser();
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);
        VOOrganization createdOrg = operatorService.registerOrganization(org,
            null, user, null, null, OrganizationRoleType.RESELLER);

        assertSame(user, accountManagement_registerOrganization_user);
        assertNull(accountManagement_registerOrganization_properties);
        assertEquals(org.getDescription(),
            accountManagement_registerOrganization_description);
        assertEquals(Arrays.asList(OrganizationRoleType.RESELLER),
            Arrays.asList(accountManagement_registerOrganization_roles));

        assertEquals(org.getAddress(), createdOrg.getAddress());
        assertEquals(org.getEmail(), createdOrg.getEmail());
        assertEquals(12345, createdOrg.getKey(), .0);
        assertEquals(org.getLocale(), createdOrg.getLocale());
        assertEquals(org.getName(), createdOrg.getName());
        assertEquals("est12345", createdOrg.getOrganizationId());
        assertEquals(org.getPhone(), createdOrg.getPhone());
        assertEquals(ORGANIZATION_DESCRIPTION, createdOrg.getDescription());
    }

    // === addOrganizationToRole ===============================================

    @Test
    public void testAddOrganizationToRole() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.SUPPLIER);
        assertEquals("est",
            accountManagement_addOrganizationToRole_organizationId);
        assertEquals(OrganizationRoleType.SUPPLIER,
            accountManagement_addOrganizationToRole_role);
    }

    @Test
    public void testAddOrganizationToRole_SupplierCheckInvoice()
        throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganizationRole(new OrganizationRole(
            OrganizationRoleType.SUPPLIER));
        organization.getGrantedRoles().add(orgToRole);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.SUPPLIER);

        assertEquals(1, dataManager_persist_objects_orgreftopt.size());
        DomainObject<?> domainObject = dataManager_persist_objects_orgreftopt
            .get(0);
        assertTrue(domainObject instanceof OrganizationRefToPaymentType);
        OrganizationRefToPaymentType cpt = (OrganizationRefToPaymentType) domainObject;
        assertEquals(OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER,
            cpt.getOrganizationReference().getReferenceType());
        assertEquals(PaymentType.INVOICE, cpt.getPaymentType()
            .getPaymentTypeId());
        assertFalse(cpt.isUsedAsDefault());
    }

    @Test
    public void testAddOrganizationToRole_TechnologyProviderCheckInvoice()
        throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return
            .add(new OrganizationRole());
        PaymentType pt = new PaymentType();
        pt.setPaymentTypeId(PaymentType.INVOICE);
        dataManager_getReferenceByBusinessKey_return.add(pt);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganizationRole(new OrganizationRole(
            OrganizationRoleType.TECHNOLOGY_PROVIDER));
        organization.getGrantedRoles().add(orgToRole);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.TECHNOLOGY_PROVIDER);

        assertTrue(dataManager_persist_objects_orgreftopt.isEmpty());
    }

    /**
     * Tries to add a role to an organization as a non-authorized user. The
     * operation is expected to fail.
     */
    @Test(expected = EJBException.class)
    public void testAddOrganizationToRoleNotAuthorized() throws Exception {
        container.login("me");
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.SUPPLIER);
    }

    /**
     * Tries to add null as a role to an organization. The operation is expected
     * to fail.
     */
    @Test(expected = OrganizationAuthorityException.class)
    public void addOrganizationToRole_NullRole() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addOrganizationToRole("est", null);
    }

    /**
     * Tries to add the Platform Operator role to an organization. The operation
     * is expected to fail.
     */
    @Test(expected = OrganizationAuthorityException.class)
    public void addOrganizationToRole_PlatformOperator() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.PLATFORM_OPERATOR);
    }

    /**
     * Tries to add the Customer role to an organization. The operation is
     * expected to fail.
     */
    @Test(expected = OrganizationAuthorityException.class)
    public void addOrganizationToRole_Customer() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.CUSTOMER);
    }

    @Test
    public void addOrganizationToRole_Broker() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.BROKER);
        assertEquals("est",
            accountManagement_addOrganizationToRole_organizationId);
        assertEquals(OrganizationRoleType.BROKER,
            accountManagement_addOrganizationToRole_role);
    }

    @Test
    public void addOrganizationToRole_Reseller() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addOrganizationToRole("est",
            OrganizationRoleType.RESELLER);
        assertEquals("est",
            accountManagement_addOrganizationToRole_organizationId);
        assertEquals(OrganizationRoleType.RESELLER,
            accountManagement_addOrganizationToRole_role);
    }

    // === addAvailablePaymentTypes ============================================

    @Test
    public void testAddEnabledPaymentTypesUnknownType() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOOrganization voorg = newVOOrganization();
        Organization supplier = newSupplierOrganization();
        // supplier.setPspIdentifier("12345");
        Set<String> types = Collections.singleton("not_existing");

        dataManager_getReferenceByBusinessKey_return.push(supplier);
        PaymentType paymentType = new PaymentType();
        paymentType.setPaymentTypeId("EXISTING");
        ArrayList<PaymentType> arrayList = new ArrayList<PaymentType>();
        arrayList.add(paymentType);
        query_getResultList = arrayList;
        try {
            operatorService.addAvailablePaymentTypes(voorg, types);
            Assert.fail("no exception cought");
        } catch (PaymentDataException e) {
            Assert.assertTrue(e.getMessage().indexOf(
                paymentType.getPaymentTypeId()) > 0);
            Assert.assertEquals("ex.PaymentDataException."
                + Reason.UNKNOWN_PAYMENT_TYPE.name(), e.getMessageKey());
        }
    }

    /**
     * Payment type invoice must be filtered.
     */
    @Test
    public void testAddEnabledPaymentTypes() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOOrganization voorg = newVOOrganization();
        Organization supplier = newSupplierOrganization();
        PSPAccount pspAccount = new PSPAccount();
        PSP psp = new PSP();
        pspAccount.setPsp(psp);
        supplier.getPspAccounts().add(pspAccount);
        supplier.getPspAccounts().get(0).setPspIdentifier("12345");
        Set<String> types = Collections.singleton(DIRECT_DEBIT);
        PaymentType paymentType = new PaymentType();
        paymentType
            .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        paymentType.setPaymentTypeId(DIRECT_DEBIT);
        paymentType.setPsp(psp);
        dataManager_getReferenceByBusinessKey_return.push(paymentType);

        dataManager_getReferenceByBusinessKey_return.push(supplier);
        operatorService.addAvailablePaymentTypes(voorg, types);

        assertEquals(1, dataManager_persist_objects_orgreftopt.size());

        OrganizationRefToPaymentType orgSetting = (OrganizationRefToPaymentType) dataManager_persist_objects_orgreftopt
            .get(0);
        assertSame(supplier, orgSetting.getAffectedOrganization());
        assertEquals(DIRECT_DEBIT, orgSetting.getPaymentType()
            .getPaymentTypeId());
    }

    /**
     * Existing payment types must be filtered.
     */
    @Test
    public void testAddEnabledPaymentTypesExistingFiltered() throws Exception {
        container.login("me", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOOrganization voorg = newVOOrganization();
        PaymentType paymentType = new PaymentType();
        paymentType
            .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        paymentType.setPaymentTypeId(BaseAdmUmTest.CREDIT_CARD);
        dataManager_getReferenceByBusinessKey_return.push(paymentType);

        Organization supplier = newSupplierOrganization();
        // supplier.setPspIdentifier("12345");
        OrganizationRefToPaymentType orgToPT = new OrganizationRefToPaymentType();
        organization = new Organization();
        organization.setOrganizationId("platform_operator");
        OrganizationReference ref = new OrganizationReference(organization,
            supplier,
            OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        orgToPT.setOrganizationReference(ref);
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(OrganizationRoleType.SUPPLIER);
        orgToPT.setOrganizationRole(role);
        orgToPT.setPaymentType(paymentType);
        orgToPT.setUsedAsDefault(false);
        supplier.getSources().add(ref);
        ref.getPaymentTypes().add(orgToPT);
        dataManager_getReferenceByBusinessKey_return.push(supplier);
        Set<String> types = Collections.singleton(BaseAdmUmTest.CREDIT_CARD);

        operatorService.addAvailablePaymentTypes(voorg, types);
        assertTrue(dataManager_persist_objects_orgreftopt.isEmpty());
    }

    @Test(expected = EJBException.class)
    public void testAddAvailablePaymentTypesNotAuthorized() throws Exception {
        VOOrganization org = newVOOrganization();
        Set<String> types = Collections.emptySet();
        operatorService.addAvailablePaymentTypes(org, types);
    }

    @Test(expected = OrganizationAuthorityException.class)
    public void testAddEnabledPaymentTypesToNonSupplier() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOOrganization voorg = newVOOrganization();
        Set<String> types = Collections.emptySet();
        dataManager_getReferenceByBusinessKey_return.push(new Organization());
        operatorService.addAvailablePaymentTypes(voorg, types);
    }

    @Test(expected = PSPIdentifierForSellerException.class)
    public void testAddEnabledPaymentTypesNoPSPIdentifier() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOOrganization voorg = newVOOrganization();
        Organization supplier = newSupplierOrganization();
        Set<String> types = Collections.singleton(DIRECT_DEBIT);
        PaymentType paymentType = new PaymentType();
        paymentType
            .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        paymentType.setPaymentTypeId(DIRECT_DEBIT);
        dataManager_getReferenceByBusinessKey_return.push(paymentType);

        dataManager_getReferenceByBusinessKey_return.push(supplier);
        operatorService.addAvailablePaymentTypes(voorg, types);
    }

    @Test
    public void testAddEnabledPaymentTypesINVOICENoPSPIdentifier()
        throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        VOOrganization voorg = newVOOrganization();
        Organization supplier = newSupplierOrganization();
        Set<String> types = Collections.singleton(INVOICE);
        PaymentType paymentType = new PaymentType();
        paymentType.setCollectionType(PaymentCollectionType.ORGANIZATION);
        paymentType.setPaymentTypeId(INVOICE);
        dataManager_getReferenceByBusinessKey_return.push(paymentType);

        dataManager_getReferenceByBusinessKey_return.push(supplier);
        operatorService.addAvailablePaymentTypes(voorg, types);
        OrganizationRefToPaymentType orgSetting = (OrganizationRefToPaymentType) dataManager_persist_objects_orgreftopt
            .get(0);
        assertSame(supplier, orgSetting.getAffectedOrganization());
        assertEquals(INVOICE, orgSetting.getPaymentType().getPaymentTypeId());
    }

    @Test
    public void testGetTimerExpirationinfomration() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        List<VOTimerInfo> info = operatorService
            .getTimerExpirationInformation();
        assertNotNull("Result must not be null", info);
    }

    @Test
    public void testSetUserAccountStatus() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReference_return.push(platformUser);
        VOUser user = new VOUser();
        user.setUserId("userId");
        user.setKey(123L);
        operatorService.setUserAccountStatus(user, UserAccountStatus.ACTIVE);
        assertTrue("User status was not changed", userStatusChanged);
    }

    @Test(expected = EJBException.class)
    public void testReInitTimersNoOperator() throws Exception {
        operatorService.reInitTimers();
    }

    @Test
    public void testReInitTimers() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        List<VOTimerInfo> reInitTimers = operatorService.reInitTimers();
        assertNotNull(reInitTimers);
    }

    @Test(expected = EJBException.class)
    public void testRetryFailedPaymentProcessNoOperator() throws Exception {
        operatorService.retryFailedPaymentProcesses();
    }

    @Test
    public void testRetryFailedPaymentProcess() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        boolean result = operatorService.retryFailedPaymentProcesses();
        assertTrue(result);
    }

    @Test(expected = EJBException.class)
    public void testSetDistinguishedNameNoOperator() throws Exception {
        operatorService.setDistinguishedName("1", "2");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSetDistinguishedNameOrganizationNotFound() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.setDistinguishedName("1", "2");
    }

    @Test
    public void testSetTechnicalProductDistinguishedName() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return.push(organization);
        operatorService.setDistinguishedName("1", "2");
        Assert.assertEquals("2", organization.getDistinguishedName());
    }

    @Test
    public void testSetTechnicalProductDistinguishedNameNull() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return.push(organization);
        operatorService.setDistinguishedName("1", null);
        Assert.assertEquals(null, organization.getDistinguishedName());
    }

    @Test(expected = ValidationException.class)
    public void testSetTechnicalProductDistinguishedNameTooLong()
        throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return.push(organization);
        operatorService.setDistinguishedName("1", TOO_LONG_DN);
    }

    @Test(expected = DistinguishedNameException.class)
    public void testSetTechnicalProductDistinguishedNameUsedByOtherOrg()
        throws Exception {
        throwNonUniqueDistinguishedName = true;
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return.push(organization);
        operatorService.setDistinguishedName("1", "2");
    }

    /**
     * Client organizations are not allowed to have images
     */
    @Test(expected = ImageException.class)
    public void testUpdateClientOrganizationWithImage() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        dataManager_getReferenceByBusinessKey_return.push(new Organization());

        operatorService.updateOrganization(newVOOperatorOrganization(),
            new VOImageResource());
    }

    @Test
    public void testGetOrganizationBillingData() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return.add(organization);
        BillingResult billingResult = new BillingResult();
        String xml = "<Parent><Child></Child></Parent>";
        billingResult.setResultXML(xml);
        billingResultList.add(billingResult);
        long to = 234567890;
        long from = 123456789;
        byte[] result = operatorService.getOrganizationBillingData(from, to,
            "id");
        final String expected = String
            .format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n"
                + "<Billingdata>%n"
                + "<Parent><Child></Child></Parent>%n"
                + "</Billingdata>%n");
        Assert.assertEquals(expected, new String(result, "UTF-8"));
    }

    @Test
    public void testGetOrganizationBillingData_MultipleBillingResults()
        throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        dataManager_getReferenceByBusinessKey_return.add(organization);

        // Create two billing results and add them to the list of billing
        // results.
        BillingResult billingResult1 = new BillingResult();
        String xml = "<Parent><Child></Child></Parent>";
        billingResult1.setResultXML(xml);
        billingResultList.add(billingResult1);

        BillingResult billingResult2 = new BillingResult();
        billingResult2.setResultXML(xml);
        billingResultList.add(billingResult2);

        long to = 234567890;
        long from = 123456789;
        byte[] result = operatorService.getOrganizationBillingData(from, to,
            "id");
        final String expected = String
            .format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n"
                + "<Billingdata>%n"
                + "<Parent><Child></Child></Parent>%n"
                + "<Parent><Child></Child></Parent>%n"
                + "</Billingdata>%n");

        Assert.assertEquals(expected, new String(result, "UTF-8"));
    }

    @Test(expected = EJBException.class)
    public void testGetOrganizationBillingData_OrgNotFound() throws Exception {
        container.login("1");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.getOrganizationBillingData(0, 0, "id");
    }

    @Test(expected = EJBException.class)
    public void testGetOrganizationBillingData_WithoutRole() throws Exception {
        operatorService.getOrganizationBillingData(0, 0, "id");
    }

    @Test(expected = EJBException.class)
    public void testResetPasswordForUserNoOperator() throws Exception {
        container.login("1", "OrganizationAdmin");
        operatorService.resetPasswordForUser("user1");
    }

    @Test(expected = EJBException.class)
    public void testResetPasswordForUserNoUser() throws Exception {
        container.login("1");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.resetPasswordForUser("user111");
    }

    @Test
    public void testResetPasswordForUser() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        PlatformUser user = new PlatformUser();
        user.setUserId("user");
        dataManager_getReferenceByBusinessKey_return.add(user);
        Organization org = new Organization();
        org.setOrganizationId("ordId");
        org.setPlatformUsers(Collections.singletonList(user));
        operatorService.resetPasswordForUser(org.getPlatformUsers().get(0)
            .getUserId());
        assertTrue(passwordReset);
    }

    @Test(expected = EJBException.class)
    public void testAddCurrency_NotAuthorized() throws Exception {
        operatorService.addCurrency(null);
    }

    @Test(expected = EJBException.class)
    public void testAddCurrency_NullInput() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addCurrency(null);
    }

    @Test(expected = ValidationException.class)
    public void testAddCurrency_InvalidISOCode() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addCurrency("EURO");
    }

    @Test
    public void testAddCurrency_ValidISOCode() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addCurrency("EUR");
        assertFalse(dataManager_persist_objects_currencies.isEmpty());
        SupportedCurrency sp = (SupportedCurrency) dataManager_persist_objects_currencies
            .get(0);
        assertEquals("EUR", sp.getCurrencyISOCode());
    }

    @Test
    public void testAddCurrency_DuplicateISOCode() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.addCurrency("EUR");
        dataManager_throwSaasNonUniqueBusinessKeyException = true;
        operatorService.addCurrency("EUR");
        assertFalse(dataManager_persist_objects_currencies.isEmpty());
    }

    @Test(expected = EJBException.class)
    public void testGetConfigurationSettings_NoPermission() throws Exception {
        operatorService.getConfigurationSettings();
    }

    @Test
    public void testGetConfigurationSettings_NoEntries() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        List<VOConfigurationSetting> result = operatorService
            .getConfigurationSettings();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetConfigurationSettings_OneEntry() throws Exception {
        configSettings.add(new ConfigurationSetting(ConfigurationKey.BASE_URL,
            "global", "value"));
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        List<VOConfigurationSetting> result = operatorService
            .getConfigurationSettings();
        assertNotNull(result);
        assertEquals(1, result.size());
        VOConfigurationSetting setting = result.get(0);
        assertEquals(ConfigurationKey.BASE_URL, setting.getInformationId());
        assertEquals("value", setting.getValue());
    }

    @Test
    public void testGetConfigurationSettings_MultipleEntries() throws Exception {
        configSettings.add(new ConfigurationSetting(ConfigurationKey.BASE_URL,
            "global", "value"));
        configSettings.add(new ConfigurationSetting(
            ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, "global", "24"));
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        List<VOConfigurationSetting> result = operatorService
            .getConfigurationSettings();
        assertNotNull(result);
        assertEquals(2, result.size());
        VOConfigurationSetting setting = result.get(0);
        assertEquals(ConfigurationKey.BASE_URL, setting.getInformationId());
        assertEquals("value", setting.getValue());
        setting = result.get(1);
        assertEquals(ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS,
            setting.getInformationId());
        assertEquals("24", setting.getValue());
    }

    @Test
    public void testSaveConfigurationSettings_Create() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        List<VOConfigurationSetting> list = new ArrayList<VOConfigurationSetting>();
        list.add(new VOConfigurationSetting(
            ConfigurationKey.HIDDEN_UI_ELEMENTS, "global",
            "HIDDEN_UI_ELEMENTS"));
        list.add(new VOConfigurationSetting(ConfigurationKey.BASE_URL, "local",
            "http://www.fujitsu.com"));
        operatorService.saveConfigurationSettings(list);

        assertTrue(configSettingSaved);

        ConfigurationSetting setting = storedConfigSettings.get(0);
        assertEquals(ConfigurationKey.HIDDEN_UI_ELEMENTS,
            setting.getInformationId());
        assertEquals("HIDDEN_UI_ELEMENTS", setting.getValue());
        assertEquals("global", setting.getContextId());

        setting = storedConfigSettings.get(1);
        assertEquals(ConfigurationKey.BASE_URL, setting.getInformationId());
        assertEquals("http://www.fujitsu.com", setting.getValue());
        assertEquals("local", setting.getContextId());
    }

    @Test(expected = EJBException.class)
    public void testSaveConfigurationSettings_NotPermitted() throws Exception {
        operatorService.saveConfigurationSettings(null);
    }

    @Test(expected = ValidationException.class)
    public void testSaveConfigurationSettings_Validation() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        List<VOConfigurationSetting> list = new ArrayList<VOConfigurationSetting>();
        list.add(new VOConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
            "bla", ""));
        operatorService.saveConfigurationSettings(list);
    }

    @Test
    public void testSaveConfigurationSetting_Save() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.saveConfigurationSetting(new VOConfigurationSetting(
            ConfigurationKey.HIDDEN_UI_ELEMENTS, "bla", "value1"));
        assertTrue(configSettingSaved);
        ConfigurationSetting setting = storedConfigSettings.get(0);
        assertEquals(ConfigurationKey.HIDDEN_UI_ELEMENTS,
            setting.getInformationId());
        assertEquals("value1", setting.getValue());
        assertEquals("bla", setting.getContextId());
    }

    @Test(expected = EJBException.class)
    public void testSaveConfigurationSetting_NotPermitted() throws Exception {
        operatorService.saveConfigurationSetting(null);
    }

    @Test(expected = ValidationException.class)
    public void testSaveConfigurationSetting_Validation() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operatorService.saveConfigurationSetting(new VOConfigurationSetting(
            ConfigurationKey.BASE_URL_HTTPS, "bla", ""));
    }

    @Test
    public void testGetOrganization() throws Exception {
        Organization supplier = newSupplierOrganization();
        supplier.setOrganizationId("organizationId");
        supplier.setName("name");
        dataManager_getReferenceByBusinessKey_return.push(supplier);

        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        VOOrganization vo = operatorService.getOrganization("");

        assertEquals(supplier.getOrganizationId(), vo.getOrganizationId());
        assertEquals(supplier.getName(), vo.getName());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetOrganization_ObjectNotFound() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        operatorService.getOrganization("");
    }

    @Test(expected = EJBException.class)
    public void testGetOrganization_NotPermitted() throws Exception {
        operatorService.getOrganization("");
    }

    @Test
    public void testGetOrganizations() throws Exception {
        List<Organization> list = new ArrayList<Organization>();
        Organization supplier;
        supplier = newSupplierOrganization();
        supplier.setOrganizationId("organizationId1");
        supplier.setName("name1");
        list.add(supplier);
        supplier = newSupplierOrganization();
        supplier.setOrganizationId("organizationId2");
        supplier.setName("name2");
        list.add(supplier);
        query_getResultList = list;

        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        List<VOOrganization> result = operatorService.getOrganizations("",
            new ArrayList<OrganizationRoleType>());

        assertEquals(list.size(), result.size());
        assertEquals(list.get(0).getName(), result.get(0).getName());
        assertEquals(list.get(1).getName(), result.get(1).getName());
    }

    @Test
    public void testGetOrganizationsWitLimit() throws Exception {
        List<Organization> list = new ArrayList<>();
        Organization supplier;
        supplier = newSupplierOrganization();
        supplier.setOrganizationId("organizationId1");
        supplier.setName("name1");
        list.add(supplier);
        query_getResultList = list;

        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        operatorService.getOrganizationsWithLimit("",
            new ArrayList<OrganizationRoleType>(), 1);

        assertTrue(queryLimit);
    }

    @Test
    public void testGetUsersWithLimit() throws Exception {
        List<Object[]> listOb = new ArrayList<>();
        Object[] user1Ob = new Object[]{"user1", "user1", "user1", "user1", UserAccountStatus.LOCKED, 123L};
        listOb.add(user1Ob);
        user1Ob = new Object[]{"user2", "user2", "user2", "user2", UserAccountStatus.LOCKED, 234L};
        listOb.add(user1Ob);
        query_getResultList = listOb;

        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);

        List<VOUserDetails> result = operatorService.getUsers();

        assertEquals(listOb.size(), result.size());
        assertEquals(listOb.get(0)[0], result.get(0).getUserId());
        assertEquals(listOb.get(1)[0], result.get(1).getUserId());
    }

    @Test(expected = EJBException.class)
    public void testGetOrganizations_NotPermitted() throws Exception {
        operatorService.getOrganizations("",
            new ArrayList<OrganizationRoleType>());
    }

    @Test(expected = EJBException.class)
    public void testStartPaymentProcessing_NotAuthorized() throws Exception {
        operatorService.startPaymentProcessing();
    }

    @Test
    public void testStartPaymentProcessing() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        boolean result = operatorService.startPaymentProcessing();
        assertTrue("Call was not delegated to the payment processing service",
            paymentProcessingStarted);
        assertTrue(result);
    }

    @Test
    public void testStartPaymentProcessing_NegativeResult() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        paymentProcessingResult = false;
        boolean result = operatorService.startPaymentProcessing();
        assertTrue("Call was not delegated to the payment processing service",
            paymentProcessingStarted);
        assertFalse(result);
    }

    @Test
    public void testGetUserOperationLog_Success() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        operationList.add("Add");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        byte[] result = operatorService.getUserOperationLog(operationList,
            100000, 200000);
        assertNotNull(result);
        verify(auditLogMock).loadAuditLogs(operationList, 100000, 200000);
    }

    @Test(expected = ValidationException.class)
    public void testGetUserOperationLog_StartBeforeEnd() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        operationList.add("Add");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        byte[] result = operatorService.getUserOperationLog(operationList,
            200000, 100000);
        assertNotNull(result);
        verify(auditLogMock).loadAuditLogs(operationList, 100000, 200000);
    }

    @Test(expected = AuditLogTooManyRowsException.class)
    public void testGetUserOperationLog_tooBig() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        operationList.add("Add");
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        Mockito.doThrow(new AuditLogTooManyRowsException())
            .when(auditLogMock)
            .loadAuditLogs(Mockito.anyListOf(String.class),
                Mockito.anyLong(), Mockito.anyLong());

        operatorService.getUserOperationLog(operationList, 100000, 200000);
    }

    @Test(expected = EJBException.class)
    public void testGetUserOperationLog_NotAuthorized() throws Exception {
        operationList.add("SUBSCR");
        operatorService.getUserOperationLog(operationList, 0, 0);
    }

    @Test(expected = EJBException.class)
    public void getAvailableAuditLogOperations_NotAuthorized() {
        operatorService.getAvailableAuditLogOperations();
    }

    @Test
    public void getAvailableAuditLogOperations() {
        container.login("1", ROLE_PLATFORM_OPERATOR);

        Map<String, String> operations = operatorService
            .getAvailableAuditLogOperations();
        Map<String, String> operationGroups = operatorService
            .getAvailableAuditLogOperationGroups();

        assertEquals(Boolean.TRUE, Boolean.valueOf(operations.size() > 0));
        assertEquals(Boolean.TRUE, Boolean.valueOf(operationGroups.size() > 0));
    }

    @Test
    public void testGetUserOperationLog_WrongEntityType() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        callerRolles.add(OrganizationRoleType.PLATFORM_OPERATOR);
        operationList.add("WRONG_TYPE");
        operatorService.getUserOperationLog(operationList, 0, 0);
    }

    @Test
    public void testGetSupplierRevenueList() throws Exception {
        container.login("1", ROLE_PLATFORM_OPERATOR);
        List<String[]> resultList = new ArrayList<String[]>();

        String[] result1 = new String[7];
        result1[0] = "1272664800000";
        result1[1] = "1273156744630";
        result1[2] = "supplieridA";
        result1[3] = "suppliernameA";
        result1[4] = "1231.11";
        result1[5] = "EUR";
        result1[6] = "MP1";
        resultList.add(result1);

        String[] result2 = new String[7];
        result2[0] = "1272764800000";
        result2[1] = "1273256744630";
        result2[2] = "supplierid\"B";
        result2[3] = "suppliername,B";
        result2[4] = "123.0012";
        result2[5] = "JPY";
        result2[6] = "MP2";
        resultList.add(result2);
        query_getResultList = resultList;

        long month = 1273156744630L;

        byte[] result = operatorService.getSupplierRevenueList(month);
        String header = "FROM,TO,ID,NAME,AMOUNT,CURRENCY,MARKETPLACE";
        String line1 = "1272664800000,1273156744630,supplieridA,suppliernameA,1231.11,EUR,MP1";
        String line2 = "1272764800000,1273256744630,\"supplierid\"\"B\",\"suppliername,B\",123.0012,JPY,MP2";
        final String expected = String.format("%s%n%s%n%s%n", header, line1,
            line2);
        Assert.assertEquals(expected, new String(result, "UTF-8"));
    }

    @Test
    public void testExportSubscriptionUsage() throws Exception {
        DataServiceBean moczek = mock(DataServiceBean.class);
        Query query = mock(Query.class);
        List<Object[]> list = new ArrayList<>();
        list.add(new Object[]{"a", "b", "c", "d", "e", "f", "g", "h", "i"});

        doReturn(query).when(moczek).createNativeQuery(anyString());
        doReturn(list).when(query).getResultList();

        OperatorServiceBean operatorServiceBean = container.get(OperatorServiceBean.class);
        operatorServiceBean.setDm(moczek);
        container.login("1", ROLE_PLATFORM_OPERATOR);


        Collection<VOSubscriptionUsageEntry> subscriptionUsageReport = new ArrayList<>();
        try {
            subscriptionUsageReport = operatorService.getSubscriptionUsageReport();
        } catch (Exception exc) {
            exc.printStackTrace();
            Assert.fail();
        }
        verify(moczek).createNativeQuery(anyString());
        verify(query, times(1)).getResultList();
        assertTrue(("b,a,g,c,d,f,e,h,i" + System.lineSeparator()).equals(toCSV(subscriptionUsageReport.iterator().next()).toString()));
        assertNotSame(0, subscriptionUsageReport.size());
    }

    // -------------------------------------------------------------
    // internal methods

    private StringBuffer toCSV(VOSubscriptionUsageEntry entry) throws IOException {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
        StringBuffer appendable = new StringBuffer();
        CSVPrinter csvFilePrinter = new CSVPrinter(appendable, csvFileFormat);
        List<String> columns = toList(entry);
        csvFilePrinter.printRecord(columns);
        csvFilePrinter.close();
        return appendable;
    }

    private List<String> toList(VOSubscriptionUsageEntry entry) {
        List<String> columns = new ArrayList<>();
        columns.add(entry.getCustomerOrgId());
        columns.add(entry.getCustomerOrgName());
        columns.add(entry.getSubscriptionName());
        columns.add(entry.getMarketableServiceName());
        columns.add(entry.getTechnicalServiceName());
        columns.add(entry.getSupplierOrganizationName());
        columns.add(entry.getSupplierOrganizationId());
        columns.add(entry.getNumberOfusers());
        columns.add(entry.getNumberOfVMs());
        return columns;
    }

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
        org.setName("Fujitsu EST");
        org.setOrganizationId("est");
        org.setPhone("+49 89 360908-0");
        org.setDomicileCountry("DE");
        org.setUrl("http://est.fujitsu.com");
        org.setDescription(ORGANIZATION_DESCRIPTION);

    }

    private Organization newSupplierOrganization() {
        final Organization org = new Organization();
        org.setOrganizationId("supplier");
        final OrganizationToRole orgTorole = new OrganizationToRole();
        final OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(OrganizationRoleType.SUPPLIER);
        orgTorole.setOrganizationRole(orgRole);
        org.setGrantedRoles(Collections.singleton(orgTorole));
        return org;
    }

}
