/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.intf.AccountService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.SubscriptionService;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.types.enumtypes.UdaConfigurationType;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.RegistrationException;
import org.oscm.types.exceptions.RegistrationException.Reason;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUdaDefinition;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/**
 * @author barzu
 */
@Ignore
public class AccountServiceWSTest {

    private WebserviceTestSetup setup;
    private static VOFactory factory = new VOFactory();
    private static AccountService accountService_Supplier;
    private static AccountService accountService_Customer;
    private static VOOrganization supplier1;
    private static VOOrganization supplier2;
    private static List<VOUdaDefinition> voUdaDefinitions;
    private static SubscriptionService subscrServiceForCustomer;
    private static VOService freeService;
    private static List<VOUsageLicense> usageLicences;
    private static VOUserDetails customerUser;
    private static VOOrganization customerOrg;
    private static VOSubscription createdSubscription = null;

    @Before
    public void setUp() throws Exception {
        // clean the mails
        WebserviceTestBase.getMailReader().deleteMails();

        // add currencies
        WebserviceTestBase.getOperator().addCurrency("EUR");

        setup = new WebserviceTestSetup();

        // Create two suppliers
        supplier2 = setup.createSupplier("Supplier2");
        supplier1 = setup.createSupplier("Supplier1");
        setup.createTechnicalService();

        // Retrieve AccountService of Supplier
        accountService_Supplier = ServiceFactory.getDefault()
                .getAccountService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // Create customer
        customerOrg = setup.createCustomer("Customer");
        accountService_Customer = setup.getAccountServiceForCustomer();
        customerUser = setup.getCustomerUser();

        accountService_Customer = ServiceFactory.getDefault()
                .getAccountService(String.valueOf(customerUser.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        // Create UdaDefinitions using supplier
        voUdaDefinitions = createUdaDefinitions();
    }

    @Test(expected = OperationPendingException.class)
    public void activateService_TriggerProcessPending() throws Exception {
        // create a trigger definition
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        setup.createTriggerDefinition(triggerDef);

        try {
            // register a customer
            setup.registerCustomerForSupplier("triggerAdmin");

            // try to register a customer with the same initial administrator
            // email
            setup.registerCustomerForSupplier("triggerAdmin");
        } finally {
            setup.deleteTriggersForUser();
        }
    }

    @Ignore
    @Test(expected = OperationPendingException.class)
    public void savePaymentConfiguration_TriggerProcessPending()
            throws Exception {
        // create a trigger definition
        VOTriggerDefinition triggerDef = WebserviceTestBase
                .createTriggerDefinition();
        triggerDef.setType(TriggerType.SAVE_PAYMENT_CONFIGURATION);
        setup.createTriggerDefinition(triggerDef);

        AccountService as = setup.getAccountServiceAsSupplier();
        try {
            Set<VOPaymentType> defaultPaymentTypes = as
                    .getDefaultServicePaymentConfiguration();
            // save the payment configuration - we need a real change, so for
            // customer an empty set is saved meaning the removal of invoice as
            // default
            as.savePaymentConfiguration(new HashSet<VOPaymentType>(), null,
                    defaultPaymentTypes, null);

            // try to save the payment configuration for the supplier again
            as.savePaymentConfiguration(new HashSet<VOPaymentType>(), null,
                    defaultPaymentTypes, null);
        } finally {
            setup.deleteTriggersForUser();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getSupplier_InvalidOrgId() throws Exception {
        setup.getAccountServiceAsSupplier()
                .getSeller(
                        "1001 ALARM: A REALLY BAD HACKER has successfully logged in using this method call. Could not find supplier with business key 1001",
                        "en");
    }

    @Test
    public void getUdaDefinitionsForCustomer_OK() throws Exception {
        VOUdaDefinition def1 = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "NEWUDAID1", "newdefaultValue1",
                UdaConfigurationType.USER_OPTION_MANDATORY);
        accountService_Supplier.saveUdaDefinitions(Arrays.asList(def1),
                new ArrayList<VOUdaDefinition>());

        List<VOUdaDefinition> voUdadefinitions = accountService_Customer
                .getUdaDefinitionsForCustomer(supplier1.getOrganizationId());

        assertEquals(1, voUdadefinitions.size());
        VOUdaDefinition voUdaDefinition = voUdadefinitions.get(0);
        assertEquals(UdaConfigurationType.USER_OPTION_MANDATORY,
                voUdaDefinition.getConfigurationType());
        assertEquals("NEWUDAID1", voUdaDefinition.getUdaId());
        assertEquals(UdaTargetType.CUSTOMER.name(),
                voUdaDefinition.getTargetType());
        assertEquals("newdefaultValue1", voUdaDefinition.getDefaultValue());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdaDefinitionsForCustomer_SupplierNotFound()
            throws Exception {
        accountService_Supplier.getUdaDefinitionsForCustomer("123");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdaDefinitionsForCustomer_InvalidSupplierId()
            throws Exception {
        accountService_Customer.getUdaDefinitionsForCustomer("Fake-Supplier");
    }

    @Ignore
    @Test
    public void getUdasForCustomer_OK() throws Exception {
        // Given - Create UdaDefinitions
        VOUdaDefinition def1 = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "NEWUDAID1", "newdefaultValue2",
                UdaConfigurationType.USER_OPTION_MANDATORY);
        accountService_Supplier.saveUdaDefinitions(Arrays.asList(def1),
                new ArrayList<VOUdaDefinition>());
        List<VOUdaDefinition> voUdadefinitions = accountService_Supplier
                .getUdaDefinitions();
        // create Udas
        createUdas(voUdadefinitions);
        // get Udas
        List<VOUda> VOUdas = accountService_Customer.getUdasForCustomer(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey(),
                supplier1.getOrganizationId());
        // verify results
        assertEquals(1, VOUdas.size());
        VOUda voUda = VOUdas.get(0);
        assertEquals("newdefaultValue2", voUda.getUdaValue());
        assertNotNull(voUda.getUdaDefinition());
        assertEquals(customerOrg.getKey(), voUda.getTargetObjectKey());
    }

    @Test(expected = ValidationException.class)
    public void getUdasForCustomer_EmptyTargetType() throws Exception {
        accountService_Customer.getUdasForCustomer("  ", customerOrg.getKey(),
                supplier1.getOrganizationId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdasForCustomer_NoTargetObject() throws Exception {
        // send a invalid object key
        accountService_Customer
                .getUdasForCustomer(UdaTargetType.CUSTOMER.name(), 3,
                        supplier1.getOrganizationId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdasForCustomer_SupplierNotFound() throws Exception {
        accountService_Customer.getUdasForCustomer(
                UdaTargetType.CUSTOMER.toString(), customerOrg.getKey(), "123");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdasForCustomer_InvalidSupplier() throws Exception {
        accountService_Customer.getUdasForCustomer(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey(),
                customerOrg.getOrganizationId());
    }

    @Test(expected = ValidationException.class)
    public void getUdasForCustomer_ValidationException() throws Exception {
        accountService_Customer.getUdasForCustomer("Invalid-Uda-Target-Type",
                customerOrg.getKey(), supplier1.getOrganizationId());
    }

    @Test
    public void getUdasForCustomer_NoneExisting() throws Exception {
        List<VOUda> udasForCustomer = accountService_Customer
                .getUdasForCustomer(UdaTargetType.CUSTOMER.name(),
                        customerOrg.getKey(), supplier2.getOrganizationId());

        assertTrue(udasForCustomer.isEmpty());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdasForCustomer_ObjectNotFoundException() throws Exception {
        accountService_Customer.getUdasForCustomer(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey(),
                "Fake-Supplier");
    }

    @Test
    public void getUdaDefinitions_Ok() throws Exception {
        List<VOUdaDefinition> defs = accountService_Supplier
                .getUdaDefinitions();
        assertEquals(defs.size(), 2);
        verifyDefinition(voUdaDefinitions.get(0), defs.get(0));
        verifyDefinition(voUdaDefinitions.get(1), defs.get(1));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getUdas_DifferentSupplier() throws Exception {
        AccountService accountService_Supplier3 = getNewSupplierService();
        accountService_Supplier3.getUdas(UdaTargetType.CUSTOMER.name(),
                customerOrg.getKey());
    }

    private AccountService getNewSupplierService() throws Exception {
        setup.createSupplier("Supplier3");
        AccountService accountService_Supplier3 = ServiceFactory.getDefault()
                .getAccountService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        return accountService_Supplier3;
    }

    @Test(expected = ValidationException.class)
    public void getUdas_EmptyTargetType() throws Exception {
        accountService_Supplier.getUdas("   ", customerOrg.getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUdas_NoTargetObject() throws Exception {
        accountService_Supplier.getUdas(UdaTargetType.CUSTOMER.name(), 5);
    }

    @Test(expected = ValidationException.class)
    public void getUdas_InvalidTargetType() throws Exception {
        accountService_Supplier.getUdas("TEST", customerOrg.getKey());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdaDefinitions_ConcurrentDelete() throws Exception {
        VOUdaDefinition def = voUdaDefinitions.get(0);
        def.setDefaultValue("defaultValue");
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def }),
                new ArrayList<VOUdaDefinition>());
        accountService_Supplier.saveUdaDefinitions(
                new ArrayList<VOUdaDefinition>(),
                Arrays.asList(new VOUdaDefinition[] { def }));
    }

    @Test
    public void saveUdaDefinitions_Create() throws Exception {
        VOUdaDefinition def = voUdaDefinitions.get(0);
        def.setDefaultValue("defaultValue");
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def }),
                new ArrayList<VOUdaDefinition>());
        List<VOUdaDefinition> results = accountService_Supplier
                .getUdaDefinitions();
        assertEquals(def.getDefaultValue(), results.get(0).getDefaultValue());
    }

    @Test
    public void saveUdaDefinitions_Delete() throws Exception {
        accountService_Supplier.saveUdaDefinitions(
                new ArrayList<VOUdaDefinition>(), voUdaDefinitions);
        List<VOUdaDefinition> results = accountService_Supplier
                .getUdaDefinitions();
        assertEquals(results.size(), 0);
    }

    @Test
    public void saveUdaDefinitions_DeleteWithInstances() throws Exception {
        createAndVerifyUdas();
        List<VOUdaDefinition> definitions = accountService_Supplier
                .getUdaDefinitions();
        assertNotNull(definitions);
        assertEquals(2, definitions.size());
        accountService_Supplier.saveUdaDefinitions(
                new ArrayList<VOUdaDefinition>(), voUdaDefinitions);
        List<VOUda> udas = accountService_Supplier.getUdas(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey());
        definitions = accountService_Supplier.getUdaDefinitions();
        assertEquals(0, definitions.size());
        assertEquals(0, udas.size());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdaDefinitions_ConcurrentUpdate() throws Exception {
        VOUdaDefinition def = voUdaDefinitions.get(0);
        def.setDefaultValue("defaultValue");
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def }),
                new ArrayList<VOUdaDefinition>());
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def }),
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void saveUdaDefinitions_CreateDuplicate() throws Exception {
        final VOUdaDefinition def1 = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA", null,
                UdaConfigurationType.SUPPLIER);
        final VOUdaDefinition def2 = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA", null,
                UdaConfigurationType.SUPPLIER);
        List<VOUdaDefinition> list = new ArrayList<VOUdaDefinition>();
        list.add(def1);
        list.add(def2);
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdaDefinitions_DifferentOwner() throws Exception {
        VOUdaDefinition def1 = voUdaDefinitions.get(0);
        setup.createSupplier("Supplier3");
        AccountService accountService_Supplier3 = getNewSupplierService();
        accountService_Supplier3.saveUdaDefinitions(
                Collections.singletonList(def1),
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_EmptyId() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition(
                        UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), "   ",
                        null, UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_EmptyTargetType() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition("   ", "UDA", null,
                        UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_InvalidTargetType() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition("invalid", "UDA", null,
                        UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_NullId() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition(
                        UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), null, null,
                        UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_NullTargetType() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition(null, "UDA", null,
                        UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveUdaDefinitions_UpdateDeleted() throws Exception {
        VOUdaDefinition def = voUdaDefinitions.get(0);
        def.setDefaultValue("defaultValue");
        accountService_Supplier.saveUdaDefinitions(
                new ArrayList<VOUdaDefinition>(),
                Arrays.asList(new VOUdaDefinition[] { def }));
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def }),
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_ToLongDefaultValue() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition(
                        UdaTargetType.CUSTOMER.name(), "UDA",
                        BaseAdmUmTest.TOO_LONG_DESCRIPTION,
                        UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = ValidationException.class)
    public void saveUdaDefinitions_ToLongId() throws Exception {
        List<VOUdaDefinition> list = Collections
                .singletonList(createVOUdaDefinition(
                        UdaTargetType.CUSTOMER_SUBSCRIPTION.name(),
                        BaseAdmUmTest.TOO_LONG_ID, null,
                        UdaConfigurationType.SUPPLIER));
        accountService_Supplier.saveUdaDefinitions(list,
                new ArrayList<VOUdaDefinition>());
    }

    @Test
    public void saveUdaDefinitions_Update() throws Exception {
        final VOUdaDefinition defChanged = voUdaDefinitions.get(0);
        defChanged.setDefaultValue("42");
        defChanged.setUdaId("UDAchanged");
        accountService_Supplier.saveUdaDefinitions(voUdaDefinitions,
                new ArrayList<VOUdaDefinition>());
        voUdaDefinitions = accountService_Supplier.getUdaDefinitions();
        assertEquals(defChanged.getDefaultValue(), voUdaDefinitions.get(0)
                .getDefaultValue());
        assertEquals(defChanged.getUdaId(), voUdaDefinitions.get(0).getUdaId());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void saveUdaDefinitions_UpdateDuplicate() throws Exception {
        VOUdaDefinition def1 = voUdaDefinitions.get(0);
        def1.setUdaId(voUdaDefinitions.get(1).getUdaId());
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def1 }),
                new ArrayList<VOUdaDefinition>());
    }

    @Test
    public void saveUdas_Create() throws Exception {
        createAndVerifyUdas();
    }

    @Test
    public void saveUdas_Update() throws Exception {
        List<VOUda> udas = createAndVerifyUdas();
        udas.get(0).setUdaValue("new " + udas.get(0).getUdaValue());
        udas.get(1).setUdaValue("new " + udas.get(1).getUdaValue());
        saveAndVerifyUdas(udas.get(0), udas.get(1), udas, true);
    }

    @Test
    public void saveUdas_Delete() throws Exception {
        List<VOUda> udas = createAndVerifyUdas();
        udas.get(0).setUdaValue(null);
        udas.get(1).setUdaValue(null);
        accountService_Supplier.saveUdas(udas);
        udas = accountService_Supplier.getUdas(UdaTargetType.CUSTOMER.name(),
                customerOrg.getKey());
        assertNotNull(udas);
        assertEquals(0, udas.size());
    }

    @Test(expected = ValidationException.class)
    public void saveUdas_NullDefinition() throws Exception {
        VOUda uda1 = createVOUda(null, "value1", customerOrg.getKey());
        accountService_Supplier.saveUdas(Collections.singletonList(uda1));
    }

    @Test(expected = ValidationException.class)
    public void saveUdas_ToLongValue() throws Exception {
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0),
                BaseAdmUmTest.TOO_LONG_DESCRIPTION, customerOrg.getKey());
        accountService_Supplier.saveUdas(Collections.singletonList(uda1));
    }

    @Test(expected = ValidationException.class)
    public void saveUdas_InvalidTargetKey() throws Exception {
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value", 0);
        accountService_Supplier.saveUdas(Collections.singletonList(uda1));
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void saveUdas_CreateDuplicate() throws Exception {
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value1",
                customerOrg.getKey());
        VOUda uda2 = createVOUda(voUdaDefinitions.get(0), "value2",
                customerOrg.getKey());
        accountService_Supplier.saveUdas(Arrays
                .asList(new VOUda[] { uda1, uda2 }));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdas_DefinitionNotFound() throws Exception {
        VOUdaDefinition def = voUdaDefinitions.get(0);
        def.setKey(def.getKey() + 1000);
        VOUda uda1 = createVOUda(def, "value1", customerOrg.getKey());
        VOUda uda2 = createVOUda(def, "value2", customerOrg.getKey());
        accountService_Supplier.saveUdas(Arrays
                .asList(new VOUda[] { uda1, uda2 }));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveUdas_UdaNotFound() throws Exception {
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value1",
                customerOrg.getKey());
        VOUda uda2 = createVOUda(voUdaDefinitions.get(0), "value2",
                secondCustomer.getKey());
        accountService_Supplier.saveUdas(Arrays
                .asList(new VOUda[] { uda1, uda2 }));
        List<VOUda> udas = accountService_Supplier.getUdas(
                UdaTargetType.CUSTOMER.name(), secondCustomer.getKey());
        udas.get(0).setKey(udas.get(0).getKey() + 100);
        accountService_Supplier.saveUdas(udas);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdas_ConcurrentDefinitionChanged() throws Exception {
        VOUdaDefinition def = voUdaDefinitions.get(0);
        def.setDefaultValue("somevalue");
        accountService_Supplier.saveUdaDefinitions(
                Arrays.asList(new VOUdaDefinition[] { def }),
                new ArrayList<VOUdaDefinition>());
        VOUda uda1 = createVOUda(def, "value1", customerOrg.getKey());
        // create Uda
        accountService_Supplier.saveUdas(Arrays.asList(uda1));
        uda1.setUdaValue("value2");
        // update Uda value
        accountService_Supplier.saveUdas(Arrays.asList(uda1));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdas_ConcurrentUdaUpdate() throws Exception {
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value1",
                customerOrg.getKey());
        VOUda uda2 = createVOUda(voUdaDefinitions.get(0), "value2",
                secondCustomer.getKey());
        accountService_Supplier.saveUdas(Arrays
                .asList(new VOUda[] { uda1, uda2 }));
        List<VOUda> udas = accountService_Supplier.getUdas(
                UdaTargetType.CUSTOMER.name(), secondCustomer.getKey());
        VOUda voUda = udas.get(0);
        voUda.setUdaValue("udaValue");
        accountService_Supplier.saveUdas(udas);
        accountService_Supplier.saveUdas(udas);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void saveUdas_ConcurrentUdaDelete() throws Exception {
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value1",
                customerOrg.getKey());
        VOUda uda2 = createVOUda(voUdaDefinitions.get(0), "value2",
                secondCustomer.getKey());
        accountService_Supplier.saveUdas(Arrays
                .asList(new VOUda[] { uda1, uda2 }));
        List<VOUda> udas = accountService_Supplier.getUdas(
                UdaTargetType.CUSTOMER.name(), secondCustomer.getKey());
        VOUda voUda = udas.get(0);
        voUda.setUdaValue("udaValue");
        accountService_Supplier.saveUdas(udas);
        voUda.setUdaValue(null);
        accountService_Supplier.saveUdas(udas);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdas_DefinitionOfDifferentOwner() throws Exception {
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value1",
                customerOrg.getKey());
        VOUda uda2 = createVOUda(voUdaDefinitions.get(0), "value2",
                secondCustomer.getKey());
        setup.createSupplier("Supplier3");
        AccountService accountService_Supplier3 = getNewSupplierService();
        accountService_Supplier3.saveUdas(Arrays.asList(new VOUda[] { uda1,
                uda2 }));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdas_DifferentDefinition() throws Exception {
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda1 = createVOUda(voUdaDefinitions.get(0), "value1",
                customerOrg.getKey());
        VOUda uda2 = createVOUda(voUdaDefinitions.get(0), "value2",
                secondCustomer.getKey());
        accountService_Supplier.saveUdas(Arrays
                .asList(new VOUda[] { uda1, uda2 }));
        VOUda uda = accountService_Supplier.getUdas(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey()).get(0);
        uda.setUdaDefinition(voUdaDefinitions.get(1));
        accountService_Supplier.saveUdas(Arrays.asList(new VOUda[] { uda }));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdas_UdaOfDifferentOwner() throws Exception {
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda = createVOUda(voUdaDefinitions.get(0), "value1",
                secondCustomer.getKey());
        accountService_Supplier.saveUdas(Arrays.asList(new VOUda[] { uda }));
        uda = accountService_Supplier.getUdas(UdaTargetType.CUSTOMER.name(),
                secondCustomer.getKey()).get(0);
        AccountService as = getNewSupplierService();
        uda.setUdaValue("udaValue");
        uda.setTargetObjectKey(customerOrg.getKey());
        uda.setUdaDefinition(voUdaDefinitions.get(0));
        as.saveUdas(Arrays.asList(new VOUda[] { uda }));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdas_ForeignTarget() throws Exception {
        AccountService accountService_Supplier3 = getNewSupplierService();
        VOOrganization secondCustomer = setup.createCustomer("Customer2");
        VOUda uda = createVOUda(voUdaDefinitions.get(0), "value1",
                secondCustomer.getKey());
        accountService_Supplier3.saveUdas(Arrays.asList(new VOUda[] { uda }));
        uda = accountService_Supplier3.getUdas(UdaTargetType.CUSTOMER.name(),
                customerOrg.getKey()).get(0);
        uda.setTargetObjectKey(customerOrg.getKey());
        accountService_Supplier3.saveUdas(Arrays.asList(new VOUda[] { uda }));
    }

    @Test
    public void registerCustomer_Disabled() throws Exception {
        WebserviceTestBase.storeGlobalConfigurationSetting(
                ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED, false);
        try {
            accountService_Supplier.registerCustomer(new VOOrganization(),
                    new VOUserDetails(), "password", null,
                    setup.getGlobalMarketplaceId(), null);
            fail("registerCustomer() succeeded with self registration disabled");
        } catch (RegistrationException e) {
            assertEquals(Reason.SELFREGISTRATION_NOT_ALLOWED, e.getReason());
        } finally {
            WebserviceTestBase.storeGlobalConfigurationSetting(
                    ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED, true);
        }
    }

    private List<VOUda> createAndVerifyUdas() throws Exception {
        VOUdaDefinition def1 = voUdaDefinitions.get(0);
        VOUdaDefinition def2 = voUdaDefinitions.get(1);
        VOUda uda1 = createVOUda(def1, "value1", customerOrg.getKey());
        VOUda uda2 = createVOUda(def2, "value2", customerOrg.getKey());
        List<VOUda> udaList = Arrays.asList(new VOUda[] { uda1, uda2 });
        return saveAndVerifyUdas(uda1, uda2, udaList, false);
    }

    private List<VOUda> saveAndVerifyUdas(VOUda uda1, VOUda uda2,
            List<VOUda> udaList, boolean checkKeys) throws Exception {
        accountService_Supplier.saveUdas(udaList);
        List<VOUda> udas = accountService_Supplier.getUdas(
                UdaTargetType.CUSTOMER.name(), customerOrg.getKey());
        assertNotNull(udas);
        assertEquals(2, udas.size());
        verify(uda1, udas.get(0), checkKeys);
        verify(uda2, udas.get(1), checkKeys);
        return udas;
    }

    private void verify(VOUda expected, VOUda read, boolean checkKeys) {
        assertEquals(expected.getUdaValue(), read.getUdaValue());
        assertEquals(expected.getTargetObjectKey(), read.getTargetObjectKey());
        if (checkKeys) {
            assertEquals(expected.getKey(), read.getKey());
        }
    }

    private void verifyDefinition(VOUdaDefinition def, VOUdaDefinition voDef) {
        assertEquals(def.getDefaultValue(), voDef.getDefaultValue());
        assertEquals(def.getTargetType(), voDef.getTargetType());
        assertEquals(def.getUdaId(), voDef.getUdaId());
    }

    private VOUdaDefinition createVOUdaDefinition(String targetType,
            String udaId, String defaultValue,
            UdaConfigurationType configurationType) {
        VOUdaDefinition def = new VOUdaDefinition();
        def.setDefaultValue(defaultValue);
        def.setTargetType(targetType);
        def.setUdaId(udaId);
        def.setConfigurationType(configurationType);
        return def;
    }

    private VOUda createVOUda(VOUdaDefinition def, String value,
            long targetObjectKey) {
        VOUda uda = new VOUda();
        uda.setTargetObjectKey(targetObjectKey);
        uda.setUdaDefinition(def);
        uda.setUdaValue(value);
        return uda;
    }

    private VOSubscription createSubscription() {
        // Subscribe to service
        VOSubscription subscription = new VOSubscription();
        String subscriptionId = Long.toHexString(System.currentTimeMillis());
        subscription.setSubscriptionId(subscriptionId);
        return subscription;
    }

    private List<VOUdaDefinition> createUdaDefinitions()
            throws org.oscm.types.exceptions.NonUniqueBusinessKeyException,
            org.oscm.types.exceptions.ObjectNotFoundException,
            org.oscm.types.exceptions.ValidationException,
            OrganizationAuthoritiesException,
            org.oscm.types.exceptions.ConcurrentModificationException,
            org.oscm.types.exceptions.OperationNotPermittedException {
        final VOUdaDefinition def1 = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDAID1", "defaultValue1",
                UdaConfigurationType.SUPPLIER);
        final VOUdaDefinition def2 = createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDAID2", "defaultValue2",
                UdaConfigurationType.SUPPLIER);
        List<VOUdaDefinition> voUdaDefinitions = new ArrayList<VOUdaDefinition>();
        voUdaDefinitions.add(def1);
        voUdaDefinitions.add(def2);
        accountService_Supplier.saveUdaDefinitions(voUdaDefinitions,
                new ArrayList<VOUdaDefinition>());
        voUdaDefinitions = accountService_Supplier.getUdaDefinitions();
        return voUdaDefinitions;
    }

    private void createUdas(List<VOUdaDefinition> voUdaDefinitions)
            throws Exception {
        // Customer subscribes service
        MarketplaceService mpSrvOperator = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());
        VOMarketplace mpLocal = mpSrvOperator.createMarketplace(factory
                .createMarketplaceVO(supplier1.getOrganizationId(), false,
                        "Local Marketplace"));
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);

        freeService = setup.createAndActivateService("Service", mpLocal,
                priceModel);
        usageLicences = new ArrayList<VOUsageLicense>();
        usageLicences.add(factory.createUsageLicenceVO(customerUser));
        subscrServiceForCustomer = ServiceFactory.getDefault()
                .getSubscriptionService(String.valueOf(customerUser.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // Create Udas using different roles
        List<VOUda> voUdas_Supplier = new ArrayList<VOUda>();
        List<VOUda> voUdas_Customer = new ArrayList<VOUda>();
        for (VOUdaDefinition vUD : voUdaDefinitions) {
            VOUda uda = this.createVOUda(vUD, vUD.getDefaultValue(),
                    customerOrg.getKey());
            if (uda.getUdaDefinition().getConfigurationType()
                    .equals(UdaConfigurationType.SUPPLIER)) {
                voUdas_Supplier.add(uda);
            } else if (uda.getUdaDefinition().getConfigurationType()
                    .equals(UdaConfigurationType.USER_OPTION_MANDATORY)) {
                voUdas_Customer.add(uda);
            }
        }
        accountService_Supplier.saveUdas(voUdas_Supplier);
        createdSubscription = createSubscription();
        createdSubscription = subscrServiceForCustomer.subscribeToService(
                createdSubscription, freeService, usageLicences, null, null,
                voUdas_Customer);
    }

    @Test
    public void deregisterOrganization_Customer() throws Exception {

        Set<VOPaymentType> defaultPaymentTypes = accountService_Supplier
                .getDefaultPaymentConfiguration();
        Set<VOPaymentType> defaultServicePaymentTypes = accountService_Supplier
                .getDefaultServicePaymentConfiguration();
        List<VOOrganizationPaymentConfiguration> customerConfigList = accountService_Supplier
                .getCustomerPaymentConfiguration();
        List<VOServicePaymentConfiguration> serviceConfigList = accountService_Supplier
                .getServicePaymentConfiguration();

        // set payment type for a customer organization (Bugzilla #12615)
        for (VOOrganizationPaymentConfiguration customerConfig : customerConfigList) {
            if (customerConfig.getOrganization().getName()
                    .contains(customerOrg.getName())) {
                customerConfig.getEnabledPaymentTypes().addAll(
                        defaultPaymentTypes);
                break;
            }
        }

        accountService_Supplier.savePaymentConfiguration(defaultPaymentTypes,
                customerConfigList, defaultServicePaymentTypes,
                serviceConfigList);

        List<VOOrganization> customersBeforeDeregister = accountService_Supplier
                .getMyCustomers();

        accountService_Customer.deregisterOrganization();

        List<VOOrganization> customersAfterDeregister = accountService_Supplier
                .getMyCustomers();

        assertEquals(customersBeforeDeregister.size() - 1, customersAfterDeregister
                .size());
    }

    @Test(expected = DeletionConstraintException.class)
    public void deregisterOrganization_Supplier() throws Exception {
        accountService_Supplier.deregisterOrganization();
    }

}
