/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                   
 *                                                                              
 *  Creation Date: 12.07.2011                                                      
 *                                                                              
 *  Completion Time: 12.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.bean;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.accountservice.assembler.PaymentInfoAssembler;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Tests for payment info services.
 * 
 * @author weiser
 * 
 */
public class AccountServicePaymentInfoTest {

    private AccountServiceBean accountServiceBean;

    private DataService dataServiceMock;
    private SessionContext sessionContextMock;
    private PaymentServiceLocal paymentServiceMock;
    private ApplicationServiceLocal applicationServiceMock;
    private LocalizerServiceLocal localizer;

    private Organization customer;
    private Organization supplier;
    private PaymentInfo paymentInfo;
    private PaymentType creditCard;
    private PaymentType directDebit;
    private PaymentType invoice;

    private VOPaymentInfo pi;

    @Before
    public void setup() throws Exception {
        accountServiceBean = new AccountServiceBean();

        dataServiceMock = Mockito.mock(DataService.class);
        sessionContextMock = Mockito.mock(SessionContext.class);
        paymentServiceMock = Mockito.mock(PaymentServiceLocal.class);
        applicationServiceMock = Mockito.mock(ApplicationServiceLocal.class);
        localizer = Mockito.mock(LocalizerServiceLocal.class);

        PlatformUser user = new PlatformUser();
        customer = new Organization();
        customer.setOrganizationId("customer");
        user.setOrganization(customer);

        supplier = new Organization();
        supplier.setKey(9);
        supplier.setOrganizationId("supplier");

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        supplier.setGrantedRoles(roles);

        creditCard = new PaymentType();
        creditCard
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        creditCard.setKey(123);
        creditCard.setPaymentTypeId(PaymentType.CREDIT_CARD);

        directDebit = new PaymentType();
        directDebit
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        directDebit.setKey(234);
        directDebit.setPaymentTypeId(PaymentType.DIRECT_DEBIT);

        invoice = new PaymentType();
        invoice.setCollectionType(PaymentCollectionType.ORGANIZATION);
        invoice.setKey(345);
        invoice.setPaymentTypeId(PaymentType.INVOICE);

        paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentType(creditCard);
        paymentInfo.setKey(1234);
        paymentInfo.setOrganization(customer);
        paymentInfo.setPaymentInfoId("paymentInfoId");
        paymentInfo.setExternalIdentifier("identifier");

        customer.getPaymentInfos().add(paymentInfo);

        // the logged in user
        Mockito.when(dataServiceMock.getCurrentUser()).thenReturn(user);

        // the payment info to find
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(PaymentInfo.class),
                        Matchers.eq(paymentInfo.getKey()))).thenReturn(
                paymentInfo);
        setFindResult(paymentInfo);

        // the payment type to find
        setReferenceByBusinessKeyResult(creditCard);

        accountServiceBean.dm = dataServiceMock;
        accountServiceBean.sessionCtx = sessionContextMock;
        accountServiceBean.paymentService = paymentServiceMock;
        accountServiceBean.appManager = applicationServiceMock;
        accountServiceBean.localizer = localizer;

        pi = PaymentInfoAssembler.toVOPaymentInfo(paymentInfo,
                new LocalizerFacade(localizer, user.getLocale()));
    }

    @Test
    public void delete() throws Exception {
        accountServiceBean.deletePaymentInfo(pi);
        // check what's passed for removal
        Mockito.verify(dataServiceMock, Mockito.times(1)).remove(
                Matchers.eq(paymentInfo));
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
    }

    @Test(expected = PaymentDeregistrationException.class)
    public void delete_PaymentDeregistrationException() throws Exception {
        // ensure that exception is not thrown
        Mockito.doThrow(new PaymentDeregistrationException())
                .when(paymentServiceMock)
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        try {
            accountServiceBean.deletePaymentInfo(pi);
        } finally {
            // check what's passed for removal
            Mockito.verify(dataServiceMock, Mockito.never()).remove(
                    Matchers.eq(paymentInfo));
            Mockito.verify(paymentServiceMock, Mockito.times(1))
                    .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        }
    }

    @Test
    public void delete_NotPSPHandled() throws Exception {
        creditCard.setCollectionType(PaymentCollectionType.ORGANIZATION);
        accountServiceBean.deletePaymentInfo(pi);
        // check what's passed for removal
        Mockito.verify(dataServiceMock, Mockito.times(1)).remove(
                Matchers.eq(paymentInfo));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void delete_NotFound() throws Exception {
        // throw object not found
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(PaymentInfo.class),
                        Matchers.anyLong())).thenThrow(
                new ObjectNotFoundException());
        accountServiceBean.deletePaymentInfo(pi);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void delete_ConcurrentlyChanged() throws Exception {
        // as we cannot increase the version of the domain object, decrease the
        // one of the value object
        pi.setVersion(pi.getVersion() - 1);
        accountServiceBean.deletePaymentInfo(pi);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void delete_NotOwner() throws Exception {
        paymentInfo.setOrganization(new Organization());
        accountServiceBean.deletePaymentInfo(pi);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void delete_Invoice() throws Exception {
        paymentInfo.setPaymentType(invoice);
        accountServiceBean.deletePaymentInfo(pi);
    }

    @Test
    public void delete_SubscriptionsFreeActive() throws Exception {
        // given
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        paymentInfo.setSubscriptions(subs);

        // when
        accountServiceBean.deletePaymentInfo(pi);

        // then
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsChargeableActive() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsChargeableActive_TechnicalServiceNotAliveException()
            throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        // ensure that exception is not thrown
        Mockito.doThrow(new TechnicalServiceNotAliveException())
                .when(applicationServiceMock)
                .deactivateInstance(Matchers.eq(sub));
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsChargeableActive_TechnicalServiceOperationException()
            throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        // ensure that exception is not thrown
        Mockito.doThrow(new TechnicalServiceOperationException())
                .when(applicationServiceMock)
                .deactivateInstance(Matchers.eq(sub));
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsPending() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.PENDING);
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsSuspended() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.SUSPENDED);
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsDeactivated() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.DEACTIVATED);
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsExpired() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.EXPIRED);
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.EXPIRED, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsInvalid() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.INVALID);
        paymentInfo.setSubscriptions(subs);
        accountServiceBean.deletePaymentInfo(pi);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getPaymentInfo());
        Assert.assertEquals(SubscriptionStatus.INVALID, sub.getStatus());
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void getPaymentInfos() {
        List<VOPaymentInfo> list = accountServiceBean.getPaymentInfos();
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        VOPaymentInfo vopi = list.get(0);
        Assert.assertEquals(paymentInfo.getKey(), vopi.getKey());
        Assert.assertEquals(paymentInfo.getPaymentInfoId(), vopi.getId());
        Assert.assertEquals(paymentInfo.getVersion(), vopi.getVersion());
        Assert.assertEquals(creditCard.getPaymentTypeId(), vopi
                .getPaymentType().getPaymentTypeId());
        Assert.assertEquals(creditCard.getCollectionType(), vopi
                .getPaymentType().getCollectionType());
    }

    @Test
    public void getAvailablePaymentTypes() {
        Query query = Mockito.mock(Query.class);
        Mockito.when(query.getResultList()).thenReturn(
                Collections.singletonList(creditCard));
        Mockito.when(
                dataServiceMock.createNamedQuery(Matchers
                        .eq("PaymentType.getAllExceptInvoice"))).thenReturn(
                query);
        Set<VOPaymentType> types = accountServiceBean
                .getAvailablePaymentTypes();

        Assert.assertNotNull(types);
        Assert.assertEquals(1, types.size());
        VOPaymentType pt = types.iterator().next();
        Assert.assertEquals(creditCard.getPaymentTypeId(),
                pt.getPaymentTypeId());
        Assert.assertEquals(creditCard.getCollectionType(),
                pt.getCollectionType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void savePaymentInfo_Null() throws Exception {
        accountServiceBean.savePaymentInfo(null);
    }

    @Test(expected = PaymentDataException.class)
    public void savePaymentInfo_PaymentTypeNull() throws Exception {
        pi.setPaymentType(null);
        accountServiceBean.savePaymentInfo(pi);
    }

    @Test(expected = PaymentDataException.class)
    public void savePaymentInfo_PaymentTypeIdEmpty() throws Exception {
        pi.getPaymentType().setPaymentTypeId("   ");
        accountServiceBean.savePaymentInfo(pi);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void savePaymentInfo_NotOwned() throws Exception {
        paymentInfo.setOrganization(new Organization());
        accountServiceBean.savePaymentInfo(pi);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void savePaymentInfo_ConcurrentlyChanged() throws Exception {
        pi.setVersion(pi.getVersion() - 1);
        accountServiceBean.savePaymentInfo(pi);
    }

    @Test(expected = ValidationException.class)
    public void savePaymentInfo_InvalidId() throws Exception {
        pi.setId("   ");
        accountServiceBean.savePaymentInfo(pi);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void savePaymentInfo_NonUniqueId() throws Exception {
        Mockito.doThrow(new NonUniqueBusinessKeyException())
                .when(dataServiceMock)
                .validateBusinessKeyUniqueness(Matchers.eq(paymentInfo));
        accountServiceBean.savePaymentInfo(pi);
    }

    @Test
    public void savePaymentInfo() throws Exception {
        pi.setId("anotherid");
        VOPaymentInfo returnedPi = accountServiceBean.savePaymentInfo(pi);
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(pi.getId(), paymentInfo.getPaymentInfoId());
        Assert.assertEquals(pi.getId(), returnedPi.getId());
        Assert.assertEquals(paymentInfo.getAccountNumber(),
                returnedPi.getAccountNumber());
        Assert.assertEquals(paymentInfo.getProviderName(),
                returnedPi.getProviderName());
    }

    @Test
    public void savePaymentInfo_TypeChangeNotSupported() throws Exception {
        // change from invoice to credit card
        paymentInfo.setPaymentType(invoice);
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        accountServiceBean.savePaymentInfo(pi);
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .deactivateInstance(Matchers.eq(sub));
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Assert.assertEquals(creditCard, paymentInfo.getPaymentType());
    }

    @Test
    public void savePaymentInfo_TypeChangeSupported() throws Exception {
        // change from invoice to credit card
        paymentInfo.setPaymentType(invoice);
        enablePaymentType(creditCard);
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE,
                creditCard);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        accountServiceBean.savePaymentInfo(pi);
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        Assert.assertEquals(creditCard, paymentInfo.getPaymentType());
    }

    @Test
    public void savePaymentInfo_TypeChangeNotSupportedWithDeregistration()
            throws Exception {
        // change from credit card to invoice
        setReferenceByBusinessKeyResult(invoice);
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        accountServiceBean.savePaymentInfo(pi);
        // service instance has to be deactivated
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .deactivateInstance(Matchers.eq(sub));
        // payment has to be deregistered
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Assert.assertEquals(invoice, paymentInfo.getPaymentType());
        Assert.assertNull(paymentInfo.getExternalIdentifier());
    }

    @Test
    public void savePaymentInfo_TypeChangeSupportedWithDeregistration()
            throws Exception {
        // change from credit card to direct debit
        setReferenceByBusinessKeyResult(invoice);
        enablePaymentType(invoice);
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE, invoice);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        accountServiceBean.savePaymentInfo(pi);
        // service instance must not be deactivated
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
        // payment has to be deregistered
        Mockito.verify(paymentServiceMock, Mockito.times(1))
                .deregisterPaymentInPSPSystem(Matchers.eq(paymentInfo));
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        Assert.assertEquals(invoice, paymentInfo.getPaymentType());
        Assert.assertNull(paymentInfo.getExternalIdentifier());
    }

    @Test
    public void savePaymentInfo_TypeChangeSupportedCCToDD() throws Exception {
        // change from credit card to direct debit
        setReferenceByBusinessKeyResult(directDebit);
        enablePaymentType(directDebit);
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE,
                directDebit);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        accountServiceBean.savePaymentInfo(pi);
        // service instance must not be deactivated
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
        // payment must not be deregistered
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        Assert.assertEquals(directDebit, paymentInfo.getPaymentType());
        Assert.assertNotNull(paymentInfo.getExternalIdentifier());
    }

    @Test
    public void savePaymentInfo_TypeChangeSupportedNoExtId() throws Exception {
        paymentInfo.setExternalIdentifier(null);
        // change from credit card to invoice
        setReferenceByBusinessKeyResult(invoice);
        enablePaymentType(invoice);
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE, invoice);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        accountServiceBean.savePaymentInfo(pi);
        // service instance must not be deactivated
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
        // payment must not be deregistered
        Mockito.verifyNoMoreInteractions(paymentServiceMock);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Assert.assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        Assert.assertEquals(invoice, paymentInfo.getPaymentType());
        Assert.assertNull(paymentInfo.getExternalIdentifier());
    }

    @Test(expected = ValidationException.class)
    public void savePaymentInfo_Create_InvalidId() throws Exception {
        setFindResult(null);
        pi.setId("   ");
        accountServiceBean.savePaymentInfo(pi);
    }

    private Set<Subscription> createSubs(SubscriptionStatus state,
            PaymentType... types) {
        Subscription sub = new Subscription();
        sub.setStatus(state);
        sub.setPaymentInfo(paymentInfo);
        Product product = new Product();
        product.setVendor(supplier);
        product.setType(ServiceType.SUBSCRIPTION);
        product.setPriceModel(new PriceModel());
        Product template = new Product();
        for (PaymentType pt : types) {
            ProductToPaymentType ptpt = new ProductToPaymentType(template, pt);
            template.getPaymentTypes().add(ptpt);
        }
        product.setTemplate(template);
        template.setVendor(supplier);
        sub.bindToProduct(product);
        Set<Subscription> subs = Collections.singleton(sub);
        paymentInfo.setSubscriptions(subs);
        return subs;
    }

    private void enablePaymentType(PaymentType toEnable) {
        OrganizationReference ref = new OrganizationReference(supplier,
                customer, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        customer.getSources().add(ref);

        // enable credit card for the customer
        OrganizationRefToPaymentType refToPt = new OrganizationRefToPaymentType();
        refToPt.setOrganizationReference(ref);
        refToPt.setOrganizationRole(new OrganizationRole(
                OrganizationRoleType.CUSTOMER));
        refToPt.setPaymentType(toEnable);
        ref.getPaymentTypes().add(refToPt);
    }

    @SuppressWarnings("unchecked")
    private void setReferenceByBusinessKeyResult(PaymentType result)
            throws ObjectNotFoundException {
        // the payment type to find
        Mockito.when(
                dataServiceMock.getReferenceByBusinessKey(Matchers
                        .any(DomainObject.class))).thenReturn(
                DomainObject.class.cast(result));
    }

    private void setFindResult(PaymentInfo result) {
        Mockito.when(
                dataServiceMock.find(Matchers.eq(PaymentInfo.class),
                        Matchers.eq(paymentInfo.getKey()))).thenReturn(result);
    }

    private OrganizationToRole createOrgToRole(OrganizationRoleType role) {
        OrganizationToRole orgToRole = new OrganizationToRole();
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(role);
        orgToRole.setOrganizationRole(orgRole);
        return orgToRole;
    }

}
