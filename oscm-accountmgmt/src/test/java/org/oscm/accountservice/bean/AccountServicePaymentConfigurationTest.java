/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 12.10.2011                                                      
 *                                                                              
 *  Completion Time: 12.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.accountservice.assembler.PaymentTypeAssembler;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * @author weiser
 * 
 */
public class AccountServicePaymentConfigurationTest {

    private DataService ds;
    private AccountServiceBean asb;

    private Organization supplier;
    private PlatformUser user;
    private final List<Organization> customers = new ArrayList<>();
    private final List<PaymentType> paymentTypes = new ArrayList<>();
    private OrganizationReference ref;
    private Query query;
    private ApplicationServiceLocal asl;
    private LocalizerServiceLocal localizer;

    private TriggerQueueServiceLocal tqsl;
    private Answer<List<TriggerProcessMessageData>> answerMock;

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        Organization po = new Organization();
        po.setOrganizationId(OrganizationRoleType.PLATFORM_OPERATOR.name());

        supplier = new Organization() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.util.Set<OrganizationRoleType> getGrantedRoleTypes() {
                return Collections.singleton(OrganizationRoleType.SUPPLIER);
            };
        };
        supplier.setOrganizationId("supplier");

        ref = new OrganizationReference(po, supplier,
                OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
        po.getTargets().add(ref);
        supplier.getSources().add(ref);

        // create and enable all payment types for the supplier
        List<String> pts = Arrays.asList(new String[] { PaymentType.CREDIT_CARD,
                PaymentType.DIRECT_DEBIT, PaymentType.INVOICE });
        for (int i = 0; i < pts.size(); i++) {
            PaymentType pt = new PaymentType();
            pt.setKey(i);
            pt.setPaymentTypeId(pts.get(i));
            paymentTypes.add(pt);

            OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
            ortpt.setOrganizationReference(ref);
            ortpt.setPaymentType(pt);
            ortpt.setUsedAsDefault(true);
            ortpt.setUsedAsServiceDefault(true);
            ortpt.setOrganizationRole(
                    new OrganizationRole(OrganizationRoleType.SUPPLIER));
            ref.getPaymentTypes().add(ortpt);
        }

        user = new PlatformUser();
        user.setOrganization(supplier);
        localizer = mock(LocalizerServiceLocal.class);

        ds = mock(DataService.class);
        doReturn(user).when(ds).getCurrentUser();

        query = mock(Query.class);
        doReturn(new ArrayList<Subscription>()).when(query).getResultList();
        doReturn(Long.valueOf(0)).when(query).getSingleResult();
        doReturn(query).when(ds).createNamedQuery(anyString());

        for (int i = 0; i < 5; i++) {
            Organization cust = new Organization();
            cust.setKey(i);
            cust.setOrganizationId("cust" + i);
            OrganizationReference r = new OrganizationReference(supplier, cust,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            cust.getSources().add(r);
            supplier.getTargets().add(r);
            customers.add(cust);

            Product p = new Product();
            p.setType(ServiceType.TEMPLATE);
            p.setKey(i);
            p.setVendor(supplier);
            supplier.getProducts().add(p);

            doReturn(p).when(ds).getReference(Product.class, i);
        }

        when(ds.getReferenceByBusinessKey(any(DomainObject.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation)
                            throws Throwable {
                        Object obj = invocation.getArguments()[0];
                        if (obj instanceof Organization) {
                            Organization org = (Organization) obj;
                            for (Organization cust : customers) {
                                if (cust.getOrganizationId()
                                        .equals(org.getOrganizationId())) {
                                    return cust;
                                }
                            }
                            throw new ObjectNotFoundException(
                                    ClassEnum.ORGANIZATION,
                                    org.getOrganizationId());
                        }
                        throw new ObjectNotFoundException(
                                obj.getClass().getName());
                    }
                });

        tqsl = mock(TriggerQueueServiceLocal.class);
        answerMock = new Answer<List<TriggerProcessMessageData>>() {

            @Override
            public List<TriggerProcessMessageData> answer(
                    InvocationOnMock invocation) throws Throwable {

                List<TriggerProcessMessageData> result = new ArrayList<>();
                for (TriggerMessage m : ParameterizedTypes.list(
                        (List<?>) invocation.getArguments()[0],
                        TriggerMessage.class)) {
                    TriggerProcessMessageData data = new TriggerProcessMessageData(
                            new TriggerProcess(), m);
                    result.add(data);
                }
                return result;
            }
        };

        doAnswer(answerMock).when(tqsl)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

        asb = spy(new AccountServiceBean());
        asb.dm = ds;
        asb.triggerQS = tqsl;
        asb.sessionCtx = mock(SessionContext.class);
        asb.localizer = localizer;

        asl = mock(ApplicationServiceLocal.class);
        asb.appManager = asl;
    }

    /**
     * Checks if the values are correctly passed to trigger processing
     */
    @Test
    public void savePaymentConfiguration_OnlyDefaults() throws Exception {
        Set<VOPaymentType> svcDef = new HashSet<>();
        ArgumentCaptor<TriggerProcess> c = spySavePaymentConfigurationInt(asb);
        Set<VOPaymentType> custDef = new HashSet<>();
        asb.savePaymentConfiguration(custDef, null, svcDef, null);

        List<TriggerProcess> values = c.getAllValues();
        // one call for each default configuration
        assertEquals(2, values.size());

        TriggerProcess tp = values.get(0);
        TriggerProcessParameter p = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION);
        assertEquals(custDef, p.getValue(Set.class));

        tp = values.get(1);
        p = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION);
        assertEquals(svcDef, p.getValue(Set.class));
    }

    /**
     * Checks if the values are correctly passed to trigger processing
     */
    @Test
    public void savePaymentConfiguration() throws Exception {
        Mockito.when(tqsl.sendSuspendingMessages(
                Matchers.anyListOf(TriggerMessage.class)))
                .thenAnswer(answerMock);

        ArgumentCaptor<TriggerProcess> c = spySavePaymentConfigurationInt(asb);
        Set<VOPaymentType> def = new HashSet<>();
        List<VOOrganizationPaymentConfiguration> custConf = getCustomerPaymentConfiguration(
                customers, def);
        List<VOServicePaymentConfiguration> svcConf = getServicePaymentConfiguration(
                supplier.getProducts(), def);
        asb.savePaymentConfiguration(def, custConf, def, svcConf);

        List<TriggerProcess> values = c.getAllValues();
        // 1 call for each default configuration; customer and service specific
        // ones are ignored as they are not changed
        assertEquals(2, values.size());

        // customer default
        TriggerProcess tp = values.get(0);
        assertEquals(1, tp.getTriggerProcessParameters().size());
        TriggerProcessParameter p = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION);
        assertEquals(def, p.getValue(Set.class));

        // service default
        tp = values.get(1);
        assertEquals(1, tp.getTriggerProcessParameters().size());
        p = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION);
        assertEquals(def, p.getValue(Set.class));
    }

    /**
     * Checks if the values are correctly passed to trigger processing
     */
    @Test
    public void savePaymentConfiguration_ChangeAll() throws Exception {
        Mockito.when(tqsl.sendSuspendingMessages(
                Matchers.anyListOf(TriggerMessage.class)))
                .thenAnswer(answerMock);

        ArgumentCaptor<TriggerProcess> c = spySavePaymentConfigurationInt(asb);
        Set<VOPaymentType> specific = getPaymentTypes(paymentTypes);
        Set<VOPaymentType> def = new HashSet<>();
        List<VOOrganizationPaymentConfiguration> custConf = getCustomerPaymentConfiguration(
                customers, specific);
        List<VOServicePaymentConfiguration> svcConf = getServicePaymentConfiguration(
                supplier.getProducts(), specific);
        asb.savePaymentConfiguration(def, custConf, def, svcConf);

        List<TriggerProcess> values = c.getAllValues();
        // 1 call for each default configuration + 5 customers + 5 products
        assertEquals(12, values.size());

        // customer default
        TriggerProcess tp = values.get(0);
        assertEquals(1, tp.getTriggerProcessParameters().size());
        TriggerProcessParameter p = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION);
        assertEquals(def, p.getValue(Set.class));

        // customer specific
        for (int i = 1; i < 6; i++) {
            tp = values.get(i);
            assertEquals(1, tp.getTriggerProcessParameters().size());

            p = tp.getParamValueForName(
                    TriggerProcessParameterName.CUSTOMER_CONFIGURATION);
            VOOrganizationPaymentConfiguration actual = p
                    .getValue(VOOrganizationPaymentConfiguration.class);
            VOOrganizationPaymentConfiguration expected = custConf.get(i - 1);

            assertEquals(String.valueOf(i), expected.getOrganization().getKey(),
                    actual.getOrganization().getKey());
            assertEquals(String.valueOf(i), specific,
                    actual.getEnabledPaymentTypes());
        }
        // service default
        tp = values.get(6);
        assertEquals(1, tp.getTriggerProcessParameters().size());
        p = tp.getParamValueForName(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION);
        assertEquals(def, p.getValue(Set.class));

        // service specific
        for (int i = 7; i < 12; i++) {
            tp = values.get(i);
            assertEquals(1, tp.getTriggerProcessParameters().size());

            p = tp.getParamValueForName(
                    TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION);
            VOServicePaymentConfiguration actual = p
                    .getValue(VOServicePaymentConfiguration.class);
            VOServicePaymentConfiguration expected = svcConf.get(i - 7);

            assertEquals(String.valueOf(i), expected.getService().getKey(),
                    actual.getService().getKey());
            assertEquals(String.valueOf(i), specific,
                    actual.getEnabledPaymentTypes());
        }
    }

    @Test
    public void savePaymentConfigurationInt_ServiceDefault_Disable()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION,
                new HashSet<VOPaymentType>());
        asb.savePaymentConfigurationInt(tp);

        List<OrganizationRefToPaymentType> list = ref.getPaymentTypes();
        assertEquals(paymentTypes.size(), list.size());
        for (OrganizationRefToPaymentType ref : list) {
            assertFalse(ref.isUsedAsServiceDefault());
            assertTrue(ref.isUsedAsDefault());
        }
        verifyZeroInteractions(asl);
    }

    @Test
    public void savePaymentConfigurationInt_CustomerDefault_Disable()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION,
                new HashSet<VOPaymentType>());
        asb.savePaymentConfigurationInt(tp);

        List<OrganizationRefToPaymentType> list = ref.getPaymentTypes();
        assertEquals(paymentTypes.size(), list.size());
        for (OrganizationRefToPaymentType ref : list) {
            assertFalse(ref.isUsedAsDefault());
            assertTrue(ref.isUsedAsServiceDefault());
        }
        verifyZeroInteractions(asl);
    }

    @Test
    public void savePaymentConfigurationInt_ServiceDefault_Enable()
            throws Exception {
        turnOffDefaults(ref);
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_SERVICE_PAYMENT_CONFIGURATION,
                getPaymentTypes(paymentTypes));
        asb.savePaymentConfigurationInt(tp);

        List<OrganizationRefToPaymentType> list = ref.getPaymentTypes();
        assertEquals(paymentTypes.size(), list.size());
        for (OrganizationRefToPaymentType ref : list) {
            assertFalse(ref.isUsedAsDefault());
            assertTrue(ref.isUsedAsServiceDefault());
        }
        verifyZeroInteractions(asl);
    }

    @Test
    public void savePaymentConfigurationInt_CustomerDefault_Enable()
            throws Exception {
        turnOffDefaults(ref);
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.DEFAULT_CONFIGURATION,
                getPaymentTypes(paymentTypes));
        asb.savePaymentConfigurationInt(tp);

        List<OrganizationRefToPaymentType> list = ref.getPaymentTypes();
        assertEquals(paymentTypes.size(), list.size());
        for (OrganizationRefToPaymentType ref : list) {
            assertFalse(ref.isUsedAsServiceDefault());
            assertTrue(ref.isUsedAsDefault());
        }
        verifyZeroInteractions(asl);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePaymentConfigurationInt_ServiceSpecific_ProductNotOwned()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        p.setVendor(new Organization());
        // try to enable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(getPaymentTypes(paymentTypes),
                        p));
        asb.savePaymentConfigurationInt(tp);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePaymentConfigurationInt_ServiceSpecific_ProductNoTemplate()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        p.setTemplate(new Product());
        p.setType(ServiceType.CUSTOMER_TEMPLATE);
        // try to enable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(getPaymentTypes(paymentTypes),
                        p));
        asb.savePaymentConfigurationInt(tp);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePaymentConfigurationInt_ServiceSpecific_PaymentNotEnabled()
            throws Exception {
        ref.setPaymentTypes(new ArrayList<OrganizationRefToPaymentType>());
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        // try to enable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(getPaymentTypes(paymentTypes),
                        p));
        asb.savePaymentConfigurationInt(tp);
    }

    @Test
    public void savePaymentConfigurationInt_ServiceSpecific_Enable()
            throws Exception {
        turnOffDefaults(ref);
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        // try to enable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(getPaymentTypes(paymentTypes),
                        p));
        asb.savePaymentConfigurationInt(tp);

        List<ProductToPaymentType> types = p.getPaymentTypes();
        assertEquals(paymentTypes.size(), types.size());
        verify(ds, times(3)).persist(any(ProductToPaymentType.class));

        Set<String> ids = new HashSet<>(
                Arrays.asList(new String[] { PaymentType.CREDIT_CARD,
                        PaymentType.DIRECT_DEBIT, PaymentType.INVOICE }));
        for (ProductToPaymentType ref : types) {
            assertTrue(ids.remove(ref.getPaymentType().getPaymentTypeId()));
        }
        verifyZeroInteractions(asl);
    }

    @Test
    public void savePaymentConfigurationInt_ServiceSpecific_Disable()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        // enable all payment types
        for (PaymentType pt : paymentTypes) {
            ProductToPaymentType ref = new ProductToPaymentType();
            ref.setPaymentType(pt);
            ref.setProduct(p);
            p.getPaymentTypes().add(ref);
        }
        // try to disable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(new HashSet<VOPaymentType>(),
                        p));
        asb.savePaymentConfigurationInt(tp);

        List<ProductToPaymentType> types = p.getPaymentTypes();
        assertTrue(types.isEmpty());
        verify(ds, times(3)).remove(any(ProductToPaymentType.class));
        verifyZeroInteractions(asl);
    }

    /**
     * The suspended subscription will be activated because the customer is
     * allowed to use the payment type.
     */
    @Test
    public void savePaymentConfigurationInt_ServiceSpecific_EnableWithActivateSub()
            throws Exception {
        turnOffDefaults(ref);
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        // enable the payment for the customer so that the enablement for the
        // service will activate the subscription
        Organization c = customers.get(0);
        OrganizationRefToPaymentType refToPt = new OrganizationRefToPaymentType();
        OrganizationReference orgRef = c.getSources().get(0);
        refToPt.setOrganizationReference(orgRef);
        refToPt.setOrganizationRole(
                new OrganizationRole(OrganizationRoleType.CUSTOMER));
        refToPt.setPaymentType(paymentTypes.get(0));
        orgRef.getPaymentTypes().add(refToPt);

        List<Subscription> subs = givenSubscriptions(p, query, c);
        // try to enable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(getPaymentTypes(paymentTypes),
                        p));
        asb.savePaymentConfigurationInt(tp);

        List<ProductToPaymentType> types = p.getPaymentTypes();
        assertEquals(paymentTypes.size(), types.size());
        verify(ds, times(3)).persist(any(ProductToPaymentType.class));

        Set<String> ids = new HashSet<>(
                Arrays.asList(new String[] { PaymentType.CREDIT_CARD,
                        PaymentType.DIRECT_DEBIT, PaymentType.INVOICE }));
        for (ProductToPaymentType ref : types) {
            assertTrue(ids.remove(ref.getPaymentType().getPaymentTypeId()));
        }
        Subscription sub = subs.get(1);
        verify(asl, times(1)).activateInstance(eq(sub));
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertEquals(SubscriptionStatus.ACTIVE, subs.get(0).getStatus());

        sub = subs.get(3);
        verify(asl, times(1)).activateInstance(eq(sub));
        assertEquals(SubscriptionStatus.PENDING_UPD, sub.getStatus());
        assertEquals(SubscriptionStatus.PENDING_UPD, subs.get(2).getStatus());
    }

    /**
     * The suspended subscription will not be activated because the customer is
     * not allowed to use the payment type.
     */
    @Test
    public void savePaymentConfigurationInt_ServiceSpecific_EnableWithoutActivateSub()
            throws Exception {
        turnOffDefaults(ref);
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        List<Subscription> subs = givenSubscriptions(p, query,
                customers.get(0));
        // try to enable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(getPaymentTypes(paymentTypes),
                        p));
        asb.savePaymentConfigurationInt(tp);

        List<ProductToPaymentType> types = p.getPaymentTypes();
        assertEquals(paymentTypes.size(), types.size());
        verify(ds, times(3)).persist(any(ProductToPaymentType.class));

        Set<String> ids = new HashSet<>(
                Arrays.asList(new String[] { PaymentType.CREDIT_CARD,
                        PaymentType.DIRECT_DEBIT, PaymentType.INVOICE }));
        for (ProductToPaymentType ref : types) {
            assertTrue(ids.remove(ref.getPaymentType().getPaymentTypeId()));
        }
        Subscription sub = subs.get(1);
        verifyZeroInteractions(asl);
        assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());

        sub = subs.get(3);
        verifyZeroInteractions(asl);
        assertEquals(SubscriptionStatus.SUSPENDED_UPD, sub.getStatus());
    }

    @Test
    public void savePaymentConfigurationInt_ServiceSpecific_DisableWithDeactivateSub()
            throws Exception {
        TriggerProcess tp = new TriggerProcess();
        Product p = supplier.getProducts().get(0);
        List<Subscription> subs = givenSubscriptions(p, query,
                customers.get(0));
        // enable all payment types
        for (PaymentType pt : paymentTypes) {
            ProductToPaymentType ref = new ProductToPaymentType();
            ref.setPaymentType(pt);
            ref.setProduct(p);
            p.getPaymentTypes().add(ref);
        }
        // try to disable all payment types for the product
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.SERVICE_PAYMENT_CONFIGURATION,
                getServicePaymentConfiguration(new HashSet<VOPaymentType>(),
                        p));
        asb.savePaymentConfigurationInt(tp);

        List<ProductToPaymentType> types = p.getPaymentTypes();
        assertTrue(types.isEmpty());
        verify(ds, times(3)).remove(any(ProductToPaymentType.class));
        Subscription sub = subs.get(0);
        verify(asl, times(1)).deactivateInstance(eq(sub));
        assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        assertEquals(SubscriptionStatus.SUSPENDED, subs.get(1).getStatus());

        sub = subs.get(2);
        verify(asl, times(1)).deactivateInstance(eq(sub));
        assertEquals(SubscriptionStatus.SUSPENDED_UPD, sub.getStatus());
        assertEquals(SubscriptionStatus.SUSPENDED_UPD, subs.get(3).getStatus());
    }

    private List<Subscription> givenSubscriptions(Product p, Query q,
            Organization c) {
        List<Subscription> result = new ArrayList<>();

        result.add(givenSubscription(p, c, SubscriptionStatus.ACTIVE));
        result.add(givenSubscription(p, c, SubscriptionStatus.SUSPENDED));
        result.add(givenSubscription(p, c, SubscriptionStatus.PENDING_UPD));
        result.add(givenSubscription(p, c, SubscriptionStatus.SUSPENDED_UPD));

        doReturn(result).when(q).getResultList();
        return result;
    }

    /**
     * @param p
     * @param c
     * @param result
     */
    private Subscription givenSubscription(Product p, Organization c,
            SubscriptionStatus state) {

        Subscription s = new Subscription();
        Product copy = p.copyForSubscription(c, s);
        copy.setPriceModel(new PriceModel());
        copy.getPriceModel().setType(PriceModelType.PRO_RATA);
        copy.setType(ServiceType.SUBSCRIPTION);
        s.bindToProduct(copy);
        s.setStatus(state);
        s.setOrganization(c);
        s.setBillingContact(new BillingContact());
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentType(paymentTypes.get(0));
        s.setPaymentInfo(paymentInfo);
        return s;
    }

    private ArgumentCaptor<TriggerProcess> spySavePaymentConfigurationInt(
            AccountServiceBean asb) throws Exception {
        ArgumentCaptor<TriggerProcess> tpCaptor = ArgumentCaptor
                .forClass(TriggerProcess.class);
        doNothing().when(asb).savePaymentConfigurationInt(tpCaptor.capture());
        return tpCaptor;
    }

    private List<VOServicePaymentConfiguration> getServicePaymentConfiguration(
            List<Product> products, Set<VOPaymentType> def) {
        List<VOServicePaymentConfiguration> result = new ArrayList<>();
        for (Product p : products) {
            result.add(getServicePaymentConfiguration(def, p));
        }
        return result;
    }

    private VOServicePaymentConfiguration getServicePaymentConfiguration(
            Set<VOPaymentType> def, Product p) {
        VOService vo = new VOService();
        vo.setKey(p.getKey());
        VOServicePaymentConfiguration conf = new VOServicePaymentConfiguration();
        conf.setEnabledPaymentTypes(def);
        conf.setService(vo);
        return conf;
    }

    private List<VOOrganizationPaymentConfiguration> getCustomerPaymentConfiguration(
            List<Organization> customers, Set<VOPaymentType> def) {
        List<VOOrganizationPaymentConfiguration> result = new ArrayList<>();
        for (Organization o : customers) {
            VOOrganization vo = new VOOrganization();
            vo.setKey(o.getKey());
            vo.setOrganizationId(o.getOrganizationId());
            VOOrganizationPaymentConfiguration conf = new VOOrganizationPaymentConfiguration();
            conf.setEnabledPaymentTypes(def);
            conf.setOrganization(vo);
            result.add(conf);
        }
        return result;
    }

    private Set<VOPaymentType> getPaymentTypes(List<PaymentType> types) {
        Set<VOPaymentType> result = new HashSet<>();
        for (PaymentType pt : types) {
            result.add(PaymentTypeAssembler.toVOPaymentType(pt,
                    new LocalizerFacade(localizer, user.getLocale())));
        }
        return result;
    }

    private void turnOffDefaults(OrganizationReference ref) {
        List<OrganizationRefToPaymentType> types = ref.getPaymentTypes();
        for (OrganizationRefToPaymentType t : types) {
            t.setUsedAsDefault(false);
            t.setUsedAsServiceDefault(false);
        }
    }

}
