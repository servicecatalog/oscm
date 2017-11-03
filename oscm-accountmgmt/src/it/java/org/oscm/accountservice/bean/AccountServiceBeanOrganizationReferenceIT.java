/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 06.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.oscm.test.data.Organizations.grantOrganizationRole;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.oscm.configurationservice.bean.ConfigurationServiceBean;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.UserRoles;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.triggerservice.bean.TriggerQueueServiceBean;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.constants.Configuration;

/**
 * @author barzu
 */
public class AccountServiceBeanOrganizationReferenceIT extends EJBTestBase {

    private AccountServiceBean asb;

    private AccountService as;
    private DataService ds;

    private final ConfigurationSetting invoiceAsDefault = new ConfigurationSetting(
            ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT,
            Configuration.GLOBAL_CONTEXT, "false");

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();

        asb = new AccountServiceBean();
        container.addBean(new ConfigurationServiceStub());
        asb.dm = mock(DataService.class);
    }

    private void setupContainer() throws Exception {
        container.addBean(new DataServiceBean());
        container.enableInterfaceMocking(true);
        container.addBean(new AccountServiceBean());

        as = container.get(AccountService.class);
        ds = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.setupSomeCountries(ds);
                UserRoles.createSetupRoles(ds);
                EJBTestBase.createOrganizationRoles(ds);
                createPaymentTypes(ds);
                return null;
            }
        });
    }

    private static void addRole(Organization org,
            OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private PlatformUser givenSupplier() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds, reseller,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.SERVICE_MANAGER);
                return user;
            }
        });
    }

    private PlatformUser givenBroker() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                PlatformUser user = Organizations.createUserForOrg(ds, reseller,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user, UserRoleType.BROKER_MANAGER);
                return user;
            }
        });
    }

    private PlatformUser givenReseller() throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                PlatformUser user = Organizations.createUserForOrg(ds, reseller,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.RESELLER_MANAGER);
                return user;
            }
        });
    }

    @Test(expected = SaaSSystemException.class)
    public void saveCustomerReference_noSellerRole() throws Exception {
        AccountServiceBean asb = new AccountServiceBean();
        asb.saveCustomerReference(Organizations.createOrganization("supplier"),
                Organizations.createOrganization("customer"));
    }

    @Test
    public void saveCustomerReference_forSupplier() throws Exception {
        Organization supplier = Organizations.createOrganization("supplier");
        addRole(supplier, OrganizationRoleType.SUPPLIER);

        OrganizationReference reference = asb.saveCustomerReference(supplier,
                Organizations.createOrganization("customer"));

        assertEquals(OrganizationReferenceType.SUPPLIER_TO_CUSTOMER,
                reference.getReferenceType());
    }

    @Test
    public void saveCustomerReference_forReseller() throws Exception {
        Organization supplier = Organizations.createOrganization("reseller");
        addRole(supplier, OrganizationRoleType.RESELLER);

        OrganizationReference reference = asb.saveCustomerReference(supplier,
                Organizations.createOrganization("customer"));

        assertEquals(OrganizationReferenceType.RESELLER_TO_CUSTOMER,
                reference.getReferenceType());
    }

    @Test
    public void saveCustomerReference_forBroker() throws Exception {
        Organization supplier = Organizations.createOrganization("broker");
        addRole(supplier, OrganizationRoleType.BROKER);

        OrganizationReference reference = asb.saveCustomerReference(supplier,
                Organizations.createOrganization("customer"));

        assertEquals(OrganizationReferenceType.BROKER_TO_CUSTOMER,
                reference.getReferenceType());
    }

    private VOOrganization createVOOrganization(String orgName) {
        VOOrganization org = new VOOrganization();
        org.setName(orgName);
        org.setAddress("Address of organization " + orgName);
        org.setEmail(orgName + "@organization.com");
        org.setPhone("012345/678" + orgName);
        org.setLocale("de");
        org.setUrl("http://www.organization.com");
        org.setDomicileCountry("DE");
        return org;
    }

    private VOUserDetails createVOUserDetails(String userId) {
        VOUserDetails user = new VOUserDetails();
        user.setLocale("de");
        user.setEMail("testuser@test.de");
        user.setUserId(userId);
        return user;
    }

    private Marketplace givenMarketplace(final Organization owner,
            final String mId) throws Exception {
        return runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws NonUniqueBusinessKeyException {
                return Marketplaces.createMarketplace(owner, mId, false, ds);
            }
        });
    }

    private void mockTriggerQueue() throws Exception {
        TriggerQueueServiceBean triggerQS = mock(TriggerQueueServiceBean.class);
        doReturn(Collections.singletonList(new TriggerProcessMessageData(
                new TriggerProcess(), new TriggerMessage()))).when(triggerQS)
                        .sendSuspendingMessages(
                                Matchers.anyListOf(TriggerMessage.class));
        container.addBean(triggerQS);
        // make it known to ASB
        container.addBean(new AccountServiceBean());
        as = container.get(AccountService.class);
    }

    private void mockMarketplaceService(Marketplace mpl) throws Exception {
        MarketplaceServiceLocal mplService = mock(
                MarketplaceServiceLocal.class);
        container.addBean(mplService);
        doReturn(mpl).when(mplService).getMarketplaceForId(anyString());
        container.addBean(new AccountServiceBean());
        as = container.get(AccountService.class);
    }

    private void mockNoInvoiceAsDefault() throws Exception {
        ConfigurationServiceBean csb = mock(ConfigurationServiceBean.class);
        doReturn(invoiceAsDefault).when(csb).getConfigurationSetting(
                eq(ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT),
                eq(Configuration.GLOBAL_CONTEXT));
        container.addBean(csb);
        // make it known to ASB
        container.addBean(new AccountServiceBean());
        as = container.get(AccountService.class);
    }

    private void validateOrganizationReference(final Organization source,
            long targetKey, final OrganizationReferenceType referenceType)
            throws Exception {
        final Organization target = new Organization();
        target.setKey(targetKey);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ds.getReferenceByBusinessKey(new OrganizationReference(source,
                        target, referenceType));
                return null;
            }
        });
    }

    @Test
    public void registerOrganization_asReseller() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenReseller();
        container.login(user.getKey(), UserRoleType.RESELLER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);

        // when
        VOOrganization customerVO = as.registerKnownCustomer(
                createVOOrganization("customer"), createVOUserDetails("admin"),
                null, marketplace.getMarketplaceId());

        // then
        validateOrganizationReference(user.getOrganization(),
                customerVO.getKey(),
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);
    }

    @Test
    public void registerOrganization_asBroker() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenBroker();
        container.login(user.getKey(), UserRoleType.BROKER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);

        // when
        VOOrganization customerVO = as.registerKnownCustomer(
                createVOOrganization("customer"), createVOUserDetails("admin"),
                null, marketplace.getMarketplaceId());

        // then
        validateOrganizationReference(user.getOrganization(),
                customerVO.getKey(),
                OrganizationReferenceType.BROKER_TO_CUSTOMER);
    }

    @Test
    public void getCustomersForSupplier_asReseller() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenReseller();
        container.login(user.getKey(), UserRoleType.RESELLER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        as.registerKnownCustomer(createVOOrganization("customerOfReseller"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        // when
        List<VOOrganization> customers = as.getMyCustomers();

        // then
        assertEquals(customers.size(), customers.size());
        VOOrganization customer = customers.get(0);
        assertEquals("customerOfReseller", customer.getName());
    }

    @Test
    public void getCustomersOptimizationForSupplier_asReseller()
            throws Exception {
        // given
        final int expected = 1;
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenReseller();
        container.login(user.getKey(), UserRoleType.RESELLER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        as.registerKnownCustomer(createVOOrganization("customerOfReseller"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        // when
        List<VOOrganization> customers = as.getMyCustomersOptimization();

        // then
        assertEquals(expected, customers.size());
        VOOrganization customer = customers.get(0);
        assertEquals("customerOfReseller", customer.getName());
    }

    @Test
    public void getCustomersForSupplier_asBroker() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenBroker();
        container.login(user.getKey(), UserRoleType.BROKER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        as.registerKnownCustomer(createVOOrganization("customerOfBroker"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        // when
        List<VOOrganization> customers = as.getMyCustomers();

        // then
        assertEquals(customers.size(), customers.size());
        VOOrganization customer = customers.get(0);
        assertEquals("customerOfBroker", customer.getName());
    }

    @Test
    public void getCustomersOptimizationForSupplier_asBroker()
            throws Exception {
        // given
        final int expected = 1;
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenBroker();
        container.login(user.getKey(), UserRoleType.BROKER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        as.registerKnownCustomer(createVOOrganization("customerOfBroker"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        // when
        List<VOOrganization> customers = as.getMyCustomersOptimization();

        // then
        assertEquals(expected, customers.size());
        VOOrganization customer = customers.get(0);
        assertEquals("customerOfBroker", customer.getName());
    }

    @Test
    public void getMyCustomerAsSupplier() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenSupplier();
        container.login(user.getKey(), UserRoleType.SERVICE_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        VOOrganization registeredCustomer = as.registerKnownCustomer(
                createVOOrganization("customerName"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());
        VODiscount voDiscount = new VODiscount();
        voDiscount.setValue(BigDecimal.TEN);
        voDiscount.setStartTime(Long.valueOf(12345L));
        voDiscount.setStartTime(Long.valueOf(45678L));
        registeredCustomer.setDiscount(voDiscount);
        as.updateCustomerDiscount(registeredCustomer);

        VOOrganization voCustomer = new VOOrganization();
        voCustomer.setKey(registeredCustomer.getKey());

        // when
        VOOrganization customer = as.getMyCustomer(voCustomer,
                Locale.ENGLISH.getLanguage());

        // then
        assertEquals("customerName", customer.getName());
        assertNotNull(customer.getDiscount());
        assertEquals(BigDecimal.TEN.byteValue(),
                customer.getDiscount().getValue().byteValue());
    }

    @Test
    public void getMyCustomerAsBroker() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenBroker();
        container.login(user.getKey(), UserRoleType.BROKER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        VOOrganization registeredCustomer = as.registerKnownCustomer(
                createVOOrganization("customerName"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        VOOrganization voCustomer = new VOOrganization();
        voCustomer.setKey(registeredCustomer.getKey());

        // when
        VOOrganization customer = as.getMyCustomer(voCustomer,
                Locale.ENGLISH.getLanguage());

        // then
        assertEquals("customerName", customer.getName());
    }

    @Test
    public void getMyCustomerAsReseller() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenReseller();
        container.login(user.getKey(), UserRoleType.RESELLER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        mockMarketplaceService(marketplace);
        VOOrganization registeredCustomer = as.registerKnownCustomer(
                createVOOrganization("customerName"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        VOOrganization voCustomer = new VOOrganization();
        voCustomer.setKey(registeredCustomer.getKey());

        // when
        VOOrganization customer = as.getMyCustomer(voCustomer,
                Locale.ENGLISH.getLanguage());

        // then
        assertEquals("customerName", customer.getName());
    }

    @Test
    public void registerKnownCustomer_asBroker() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenBroker();
        container.login(user.getKey(), UserRoleType.BROKER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        invoiceAsDefault.setValue("true");
        mockMarketplaceService(marketplace);

        // when
        VOOrganization org = as.registerKnownCustomer(
                createVOOrganization("customerOfBroker"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        // then
        assertEquals("customerOfBroker", org.getName());
        invoiceAsDefault.setValue("false");
    }

    @Test
    public void registerKnownCustomer_asReseller() throws Exception {
        // given
        setupContainer();
        mockTriggerQueue();
        mockNoInvoiceAsDefault();
        final PlatformUser user = givenBroker();
        container.login(user.getKey(), UserRoleType.RESELLER_MANAGER.name());
        Marketplace marketplace = givenMarketplace(user.getOrganization(),
                "MP");
        invoiceAsDefault.setValue("true");
        mockMarketplaceService(marketplace);

        // when
        VOOrganization org = as.registerKnownCustomer(
                createVOOrganization("customerOfReseller"),
                createVOUserDetails("admin"), null,
                marketplace.getMarketplaceId());

        // then
        assertEquals("customerOfReseller", org.getName());
        invoiceAsDefault.setValue("false");
    }

    private static Organization givenOrganization(
            OrganizationRoleType... roles) {
        Organization org = new Organization();
        org.setKey(123L);
        for (OrganizationRoleType role : roles) {
            grantOrganizationRole(org, role);
        }
        return org;
    }

    private static void setOrganizationReferences(Organization org,
            OrganizationReferenceType... types) {
        List<OrganizationReference> references = new ArrayList<>();
        for (OrganizationReferenceType type : types) {
            OrganizationReference reference = new OrganizationReference(org,
                    org, type);
            references.add(reference);
        }
        org.setSources(references);
    }

    @Test
    public void addSelfReferenceAsCustomer_asSupplier() throws Exception {
        // given
        Organization org = givenOrganization(OrganizationRoleType.SUPPLIER);
        ArgumentCaptor<OrganizationReference> argument = ArgumentCaptor
                .forClass(OrganizationReference.class);

        // when
        asb.addSelfReferenceAsCustomer(org);

        // then
        verify(asb.dm, times(1)).persist(argument.capture());
        assertEquals(OrganizationReferenceType.SUPPLIER_TO_CUSTOMER,
                argument.getValue().getReferenceType());
        assertEquals(org.getKey(), argument.getValue().getSource().getKey());
        assertEquals(org.getKey(), argument.getValue().getTarget().getKey());
    }

    @Test
    public void addSelfReferenceAsCustomer_asSupplier_existingReference()
            throws Exception {
        // given
        Organization org = givenOrganization(OrganizationRoleType.SUPPLIER);
        setOrganizationReferences(org,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        // when
        asb.addSelfReferenceAsCustomer(org);

        // then
        verify(asb.dm, times(0)).persist(any(OrganizationReference.class));
    }

    @Test
    public void addSelfReferenceAsCustomer_asBroker() throws Exception {
        // given
        Organization org = givenOrganization(OrganizationRoleType.BROKER);
        ArgumentCaptor<OrganizationReference> argument = ArgumentCaptor
                .forClass(OrganizationReference.class);

        // when
        asb.addSelfReferenceAsCustomer(org);

        // then
        verify(asb.dm, times(1)).persist(argument.capture());
        assertEquals(OrganizationReferenceType.BROKER_TO_CUSTOMER,
                argument.getValue().getReferenceType());
        assertEquals(org.getKey(), argument.getValue().getSource().getKey());
        assertEquals(org.getKey(), argument.getValue().getTarget().getKey());
    }

    @Test
    public void addSelfReferenceAsCustomer_asBroker_existingReference()
            throws Exception {
        // given
        Organization org = givenOrganization(OrganizationRoleType.BROKER);
        setOrganizationReferences(org,
                OrganizationReferenceType.BROKER_TO_CUSTOMER);

        // when
        asb.addSelfReferenceAsCustomer(org);

        // then
        verify(asb.dm, times(0)).persist(any(OrganizationReference.class));
    }

    @Test
    public void addSelfReferenceAsCustomer_asReseller() throws Exception {
        // given
        Organization org = givenOrganization(OrganizationRoleType.RESELLER);
        ArgumentCaptor<OrganizationReference> argument = ArgumentCaptor
                .forClass(OrganizationReference.class);

        // when
        asb.addSelfReferenceAsCustomer(org);

        // then
        verify(asb.dm, times(1)).persist(argument.capture());
        assertEquals(OrganizationReferenceType.RESELLER_TO_CUSTOMER,
                argument.getValue().getReferenceType());
        assertEquals(org.getKey(), argument.getValue().getSource().getKey());
        assertEquals(org.getKey(), argument.getValue().getTarget().getKey());
    }

    @Test
    public void addSelfReferenceAsCustomer_asReseller_existingReference()
            throws Exception {
        // given
        Organization org = givenOrganization(OrganizationRoleType.RESELLER);
        setOrganizationReferences(org,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);

        // when
        asb.addSelfReferenceAsCustomer(org);

        // then
        verify(asb.dm, times(0)).persist(any(OrganizationReference.class));
    }

}
