/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TriggerDefinitions;
import org.oscm.test.data.TriggerProcesses;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

public class AccountServiceBeanTriggerIdIT extends EJBTestBase {

    private AccountService accountService;
    private VOOrganization org;
    private VOUserDetails user;
    private DataService ds;
    private Organization supplier;
    private PlatformUser supplierUser;
    private Organization supplier2;
    private PlatformUser supplier2User;
    private TriggerProcessMessageData triggerProcessData;
    private TriggerProcess tp;
    private Set<VOPaymentType> types = Collections.emptySet();
    private List<VOOrganizationPaymentConfiguration> orgPayments = Collections
            .emptyList();
    private List<VOServicePaymentConfiguration> servPayments = Collections
            .emptyList();
    private String marketplaceId = "FUJITSU";

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();

        container.enableInterfaceMocking(true);
        TriggerQueueServiceLocal triggerQueueServiceLocal = mock(
                TriggerQueueServiceLocal.class);
        container.addBean(triggerQueueServiceLocal);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        AccountServiceBean accountServiceBean = spy(new AccountServiceBean());
        container.addBean(accountServiceBean);

        accountService = container.get(AccountService.class);
        ds = container.get(DataService.class);
        doAnswer(new Answer<List<TriggerProcessMessageData>>() {
            @Override
            public List<TriggerProcessMessageData> answer(
                    InvocationOnMock invocation) throws Throwable {
                return Collections.singletonList(triggerProcessData);
            }
        }).when(triggerQueueServiceLocal)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));
        doReturn(null).when(accountServiceBean)
                .registerKnownCustomerInt(any(TriggerProcess.class));
        doNothing().when(accountServiceBean)
                .savePaymentConfigurationInt(any(TriggerProcess.class));
    }

    @Test
    public void registerCustomerForSupplier_NonConflicting() throws Exception {
        initData(false, false, TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        tp.setTriggerDefinition(null);
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        accountService.registerKnownCustomer(org, user, null, marketplaceId);
        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertTrue(processIdentifiers.isEmpty());
    }

    @Test
    public void registerCustomerForSupplier_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initData(false, false, TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        accountService.registerKnownCustomer(org, user, null, marketplaceId);
        // assert existence of identifiers
        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertEquals(3, processIdentifiers.size());
    }

    @Test
    public void registerCustomerForSupplier_Conflicting() throws Exception {
        initData(true, false, TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        try {
            accountService.registerKnownCustomer(org, user, null,
                    marketplaceId);
            fail();
        } catch (OperationPendingException e) {
            assertEquals(
                    "ex.OperationPendingException.REGISTER_CUSTOMER_FOR_SUPPLIER",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(2, messageParams.length);
            assertEquals(user.getUserId(), messageParams[0]);
            assertEquals(user.getEMail(), messageParams[1]);
        }
    }

    @Test
    public void savePaymentConfiguration_NonConflicting() throws Exception {
        initData(false, false, TriggerType.SAVE_PAYMENT_CONFIGURATION);
        tp.setTriggerDefinition(null);
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        accountService.savePaymentConfiguration(types, orgPayments, types,
                servPayments);
        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertTrue(processIdentifiers.isEmpty());
    }

    @Test
    public void savePaymentConfiguration_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initData(false, false, TriggerType.SAVE_PAYMENT_CONFIGURATION);
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        accountService.savePaymentConfiguration(types, orgPayments, types,
                servPayments);
        List<TriggerProcessIdentifier> processIdentifiers = getProcessIdentifiers();
        assertNotNull(processIdentifiers);
        assertEquals(1, processIdentifiers.size());
    }

    @Test
    public void savePaymentConfiguration_Conflicting() throws Exception {
        initData(true, false, TriggerType.SAVE_PAYMENT_CONFIGURATION);
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        try {
            accountService.savePaymentConfiguration(types, orgPayments, types,
                    servPayments);
            fail();
        } catch (OperationPendingException e) {
            assertEquals(
                    "ex.OperationPendingException.SAVE_PAYMENT_CONFIGURATION",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(0, messageParams.length);
        }
    }

    /**
     * Initializes the test setup, creates the org roles, currencies, countries,
     * a supplier organization and, if required, some trigger data.
     * 
     * @param createTriggerProcessIds
     *            Indicates whether the trigger process identifier should be
     *            generated or not.
     * @param assignTDToSecondSupplier
     *            Indicates whether the trigger belongs to supplier 2.
     * @param triggerType
     *            The type of the trigger
     * @throws Exception
     */
    private void initData(final boolean createTriggerProcessIds,
            final boolean assignTDToSecondSupplier,
            final TriggerType triggerType) throws Exception {
        org = new VOOrganization();
        org.setName("testorg");
        org.setLocale("en");
        org.setDomicileCountry("DE");
        user = new VOUserDetails();
        user.setUserId("testUser");
        user.setEMail("user@server.com");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(ds);
                SupportedCountries.createSomeSupportedCountries(ds);
                createPaymentTypes(ds);
                supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                supplierUser = Organizations.createUserForOrg(ds, supplier,
                        true, "supplierAdmin");
                supplier2 = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                supplier2User = Organizations.createUserForOrg(ds, supplier2,
                        true, "supplier2Admin");
                Marketplaces.createMarketplace(supplier, marketplaceId, true,
                        ds);
                createTriggerData(createTriggerProcessIds,
                        assignTDToSecondSupplier, triggerType);
                return null;
            }

        });
    }

    /**
     * Creates trigger definition and process.
     * 
     * @param createTriggerProcessIds
     *            Indicates if trigger process identifiers have to be set.
     * @param assignTDToSecondSupplier
     *            Indicates if the trigger definition is created for the second
     *            supplier.
     * @param triggerType
     *            The type of the trigger to be created.
     * @throws Exception
     */
    private void createTriggerData(final boolean createTriggerProcessIds,
            final boolean assignTDToSecondSupplier,
            final TriggerType triggerType) throws Exception {
        PlatformUser triggerUser = supplierUser;
        Organization triggerOrg = supplier;
        if (assignTDToSecondSupplier) {
            triggerUser = supplier2User;
            triggerOrg = supplier2;
        }
        TriggerDefinition td = TriggerDefinitions
                .createSuspendingTriggerDefinition(ds, triggerOrg, triggerType);
        tp = TriggerProcesses.createPendingTriggerProcess(ds, triggerUser, td);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USER, user);
        triggerProcessData = new TriggerProcessMessageData(tp,
                new TriggerMessage());
        if (createTriggerProcessIds) {
            createTriggerIds(tp);
        }
    }

    /**
     * Creates the trigger process identifiers for the registration of a
     * customer organization for a supplier and initializes the trigger process
     * meta-data accordingly.
     * 
     * @param tp
     *            The trigger process the identifiers belong to.
     */
    private void createTriggerIds(TriggerProcess tp) {
        List<TriggerProcessParameter> params = Collections.emptyList();
        TriggerType triggerType = tp.getTriggerDefinition().getType();
        TriggerMessage tm = new TriggerMessage(triggerType, params,
                Collections.singletonList(supplier));
        TriggerProcessIdentifier tpi1 = new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(tp.getUser().getOrganization().getKey()));
        tpi1.setTriggerProcess(tp);
        tp.setTriggerProcessIdentifiers(Arrays.asList(tpi1));
        if (triggerType == TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER) {
            TriggerProcessIdentifier tpi2 = new TriggerProcessIdentifier(
                    TriggerProcessIdentifierName.USER_ID, user.getUserId());
            tpi2.setTriggerProcess(tp);
            TriggerProcessIdentifier tpi3 = new TriggerProcessIdentifier(
                    TriggerProcessIdentifierName.USER_EMAIL, user.getEMail());
            tpi3.setTriggerProcess(tp);
            tp.setTriggerProcessIdentifiers(Arrays.asList(tpi1, tpi2, tpi3));
        }
        triggerProcessData = new TriggerProcessMessageData(tp, tm);
    }

    private List<TriggerProcessIdentifier> getProcessIdentifiers(
            final Long... tpKeys) throws Exception {
        return runTX(new Callable<List<TriggerProcessIdentifier>>() {
            @Override
            public List<TriggerProcessIdentifier> call() throws Exception {
                return TriggerProcesses.getProcessIdentifiers(ds, tpKeys);
            }

        });
    }

}
