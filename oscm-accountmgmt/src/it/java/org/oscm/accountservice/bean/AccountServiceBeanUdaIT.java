/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 12.10.2010                                                      
 *                                                                              
 *  Completion Time: 14.10.2010                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.identityservice.bean.LdapAccessStub;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.data.Udas;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ApplicationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.test.stubs.PaymentServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class AccountServiceBeanUdaIT extends EJBTestBase {

    private DataService mgr;
    private AccountService accountMgmt;

    private Organization provider;
    private Organization supplier;
    private Organization customer;

    private PlatformUser providerUser;
    private PlatformUser supplierUser;
    private PlatformUser customerUser;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(mock(TriggerQueueServiceLocal.class));
        container.addBean(mock(ReviewServiceLocalBean.class));
        container.addBean(new LocalizerServiceStub());
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new TaskQueueServiceStub());
        container.addBean(new IdentityServiceBean());
        container.addBean(new PaymentServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(new AccountServiceBean());

        mgr = container.get(DataService.class);
        accountMgmt = container.get(AccountService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                createPaymentTypes(mgr);
                SupportedCountries.createOneSupportedCountry(mgr);
                return null;
            }
        });

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                provider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                providerUser = Organizations.createUserForOrg(mgr, provider,
                        true, "admin");

                supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                Organizations.createSupplierToTechnologyProviderReference(mgr,
                        provider, supplier);
                supplierUser = Organizations.createUserForOrg(mgr, supplier,
                        true, "admin");
                Organization secondSupplier = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                Organizations.createSupplierToTechnologyProviderReference(mgr,
                        provider, secondSupplier);
                customer = Organizations.createCustomer(mgr, supplier);
                customerUser = Organizations.createUserForOrg(mgr, customer,
                        true, "admin");

                return null;
            }
        });
    }

    @Test
    public void testGetUdaTargetTypes_Admin() throws Exception {
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        Set<String> types = accountMgmt.getUdaTargetTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        assertTrue(types.contains(UdaTargetType.CUSTOMER.name()));
        assertTrue(types.contains(UdaTargetType.CUSTOMER_SUBSCRIPTION.name()));
    }

    @Test
    public void testGetUdaTargetTypes_NonAdmin() throws Exception {
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        Set<String> types = accountMgmt.getUdaTargetTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        assertTrue(types.contains(UdaTargetType.CUSTOMER.name()));
        assertTrue(types.contains(UdaTargetType.CUSTOMER_SUBSCRIPTION.name()));
    }

    @Test
    public void testGetUdaTargetTypes_Customer() throws Exception {
        container.login(customerUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        Set<String> types = accountMgmt.getUdaTargetTypes();
        assertNotNull(types);
        assertEquals(0, types.size());
    }

    @Test
    public void testGetUdaTargetTypes_Provider() throws Exception {
        container.login(providerUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        Set<String> types = accountMgmt.getUdaTargetTypes();
        assertNotNull(types);
        assertEquals(0, types.size());
    }

    @Test
    public void testGetUdaDefinitions_Admin() throws Exception {
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<VOUdaDefinition> defs = accountMgmt.getUdaDefinitions();
        assertNotNull(defs);
        assertTrue(defs.isEmpty());
    }

    @Test
    public void testGetUdaDefinitions_NonAdmin() throws Exception {
        container.login(supplierUser.getKey());
        List<VOUdaDefinition> defs = accountMgmt.getUdaDefinitions();
        assertNotNull(defs);
        assertTrue(defs.isEmpty());
    }

    @Test
    public void testGetUdaDefinitions_CustomerAdmin() throws Exception {
        container.login(customerUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<VOUdaDefinition> defs = accountMgmt.getUdaDefinitions();
        assertNotNull(defs);
        assertTrue(defs.isEmpty());
    }

    @Test
    public void testGetUdaDefinitions_ProviderNonAdmin() throws Exception {
        container.login(providerUser.getKey());
        List<VOUdaDefinition> defs = accountMgmt.getUdaDefinitions();
        assertNotNull(defs);
        assertTrue(defs.isEmpty());
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void testSaveUdaDefinitions_Customer() throws Exception {
        container.login(customerUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<VOUdaDefinition> list = Collections.singletonList(Udas
                .createVOUdaDefinition(UdaTargetType.CUSTOMER.name(), "UDA",
                        null, UdaConfigurationType.SUPPLIER));
        accountMgmt.saveUdaDefinitions(list, new ArrayList<VOUdaDefinition>());
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void testSaveUdaDefinitions_Provider() throws Exception {
        container.login(providerUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<VOUdaDefinition> list = Collections.singletonList(Udas
                .createVOUdaDefinition(
                        UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), "UDA",
                        null, UdaConfigurationType.SUPPLIER));
        accountMgmt.saveUdaDefinitions(list, new ArrayList<VOUdaDefinition>());
    }

    @Test
    public void testGetUdas_SupplierAdmin() throws Exception {
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        prepareUdaOnSubscription("UDA1", "UDA2", "UDA3");
        List<Uda> udas = prepareUdaOnCustomer("UDA1", "UDA2");
        List<VOUda> list = accountMgmt.getUdas(UdaTargetType.CUSTOMER.name(),
                customer.getKey(), false);
        assertNotNull(list);
        assertEquals(2, list.size());
        verify(udas.get(0), list.get(0));
        verify(udas.get(1), list.get(1));
    }

    @Test
    public void testGetUdas_Supplier() throws Exception {
        container.login(supplierUser.getKey());
        prepareUdaOnCustomer("UDA");
        List<Uda> udas = prepareUdaOnSubscription("UDA1", "UDA2", "UDA3");
        List<VOUda> list = accountMgmt.getUdas(
                UdaTargetType.CUSTOMER_SUBSCRIPTION.name(), udas.get(0)
                        .getTargetObjectKey(), false);
        assertNotNull(list);
        assertEquals(udas.size(), list.size());
        verify(udas.get(0), list.get(0));
        verify(udas.get(1), list.get(1));
        verify(udas.get(2), list.get(2));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void saveUdas_OperationNotPermittedException() throws Exception {
        container.login(supplierUser.getKey(), ROLE_ORGANIZATION_ADMIN);
        List<VOUdaDefinition> list = prepareDefinitions(UdaConfigurationType.USER_OPTION_MANDATORY);
        createAndVerifyUdas(list);
    }

    /*
     * ------------------------- helper methods ---------------------
     */

    private List<VOUda> createAndVerifyUdas(List<VOUdaDefinition> list)
            throws Exception {
        VOUdaDefinition def1 = list.get(0);
        VOUdaDefinition def2 = list.get(1);
        VOUda uda1 = Udas.createVOUda(def1, "value1", customer.getKey());
        VOUda uda2 = Udas.createVOUda(def2, "value2", customer.getKey());
        List<VOUda> udaList = Arrays.asList(new VOUda[] { uda1, uda2 });
        return saveAndVerifyUdas(uda1, uda2, udaList, false);
    }

    private List<VOUda> saveAndVerifyUdas(VOUda uda1, VOUda uda2,
            List<VOUda> udaList, boolean checkKeys) throws Exception {
        accountMgmt.saveUdas(udaList);
        List<VOUda> udas = accountMgmt.getUdas(UdaTargetType.CUSTOMER.name(),
                customer.getKey(), false);
        assertNotNull(udas);
        assertEquals(2, udas.size());
        verify(uda1, udas.get(0), checkKeys);
        verify(uda2, udas.get(1), checkKeys);
        return udas;
    }

    private List<VOUdaDefinition> prepareDefinitions(UdaConfigurationType type)
            throws ValidationException, OrganizationAuthoritiesException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException {
        VOUdaDefinition def1 = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA1", null, type);
        VOUdaDefinition def2 = Udas.createVOUdaDefinition(
                UdaTargetType.CUSTOMER.name(), "UDA2", null, type);
        List<VOUdaDefinition> list = new ArrayList<VOUdaDefinition>();
        list.add(def1);
        list.add(def2);
        accountMgmt.saveUdaDefinitions(list, new ArrayList<VOUdaDefinition>());
        return accountMgmt.getUdaDefinitions();
    }

    private static void verify(Uda uda, VOUda voUda) {
        assertEquals(uda.getKey(), voUda.getKey());
        assertEquals(uda.getVersion(), voUda.getVersion());
        assertEquals(uda.getUdaValue(), voUda.getUdaValue());
        assertEquals(uda.getTargetObjectKey(), voUda.getTargetObjectKey());
        verifyDefinition(uda.getUdaDefinition(), voUda.getUdaDefinition());
    }

    private static void verify(VOUda expected, VOUda read, boolean checkKeys) {
        assertEquals(expected.getUdaValue(), read.getUdaValue());
        assertEquals(expected.getTargetObjectKey(), read.getTargetObjectKey());
        if (checkKeys) {
            assertEquals(expected.getKey(), read.getKey());
        }
    }

    private static void verifyDefinition(UdaDefinition def,
            VOUdaDefinition voDef) {
        assertEquals(def.getDefaultValue(), voDef.getDefaultValue());
        assertEquals(def.getTargetType().name(), voDef.getTargetType());
        assertEquals(def.getUdaId(), voDef.getUdaId());
    }

    private List<Uda> prepareUdaOnCustomer(final String... udaId)
            throws Exception {
        List<Uda> result = runTX(new Callable<List<Uda>>() {

            @Override
            public List<Uda> call() throws Exception {
                List<Uda> result = new ArrayList<Uda>();
                for (String id : udaId) {
                    UdaDefinition def = Udas.createUdaDefinition(mgr, supplier,
                            UdaTargetType.CUSTOMER, id, null,
                            UdaConfigurationType.USER_OPTION_MANDATORY);
                    Uda uda = Udas.createUda(mgr, customer, def, "42");
                    result.add(uda);
                }
                return result;
            }
        });
        return result;
    }

    private List<Uda> prepareUdaOnSubscription(final String... udaId)
            throws Exception {
        List<Uda> result = runTX(new Callable<List<Uda>>() {

            @Override
            public List<Uda> call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, provider, "TPID", false, ServiceAccessType.LOGIN);
                Product prod = Products.createProduct(supplier, tp, false,
                        "PRODID", null, mgr);
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), prod.getProductId(),
                        "SUBID", supplier);
                List<Uda> result = new ArrayList<Uda>();
                for (String id : udaId) {
                    UdaDefinition def = Udas.createUdaDefinition(mgr, supplier,
                            UdaTargetType.CUSTOMER_SUBSCRIPTION, id, null,
                            UdaConfigurationType.USER_OPTION_MANDATORY);
                    Uda uda = Udas.createUda(mgr, sub, def, "42");
                    result.add(uda);
                }
                return result;
            }
        });
        return result;
    }

}
