/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 13.07.2011                                                      
 *                                                                              
 *  Completion Time: 13.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.ejb.SessionContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.accountservice.assembler.BillingContactAssembler;
import org.oscm.accountservice.assembler.PaymentInfoAssembler;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
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
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * Tests for billing contact services.
 * 
 * @author weiser
 * 
 */
public class SubscriptionServicePaymentTest {

    private SubscriptionServiceBean subscriptionServiceBean;
    private ManageSubscriptionBean manageBean;

    private DataService dataServiceMock;
    private ApplicationServiceLocal applicationServiceMock;
    private SessionContext sessionContextMock;
    private SubscriptionAuditLogCollector auditlogCollectorMock;

    private Organization supplier;
    private Organization customer;
    private BillingContact billingContact;
    private PaymentInfo paymentInfoCreditCard;
    private PaymentInfo paymentInfoInvoice;
    private PaymentType creditCard;
    private PaymentType invoice;
    private Subscription subscription;

    /**
     * enabled
     */
    private VOPaymentInfo piCreditCard;
    /**
     * not enabled
     */
    private VOPaymentInfo piInvoice;
    private VOBillingContact bc;
    private VOSubscription sub;

    @Before
    public void setup() throws Exception {

        subscriptionServiceBean = new SubscriptionServiceBean();

        dataServiceMock = Mockito.mock(DataService.class);
        LocalizerServiceLocal localizerMock = Mockito
                .mock(LocalizerServiceLocal.class);
        applicationServiceMock = Mockito.mock(ApplicationServiceLocal.class);
        sessionContextMock = Mockito.mock(SessionContext.class);
        auditlogCollectorMock = Mockito
                .mock(SubscriptionAuditLogCollector.class);

        PlatformUser user = new PlatformUser();
        customer = new Organization();
        customer.setKey(8);
        customer.setOrganizationId("customer");
        user.setOrganization(customer);
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.ORGANIZATION_ADMIN));
        customer.getPlatformUsers().add(user);

        supplier = new Organization();
        supplier.setKey(9);
        supplier.setOrganizationId("supplier");

        Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
        roles.add(createOrgToRole(OrganizationRoleType.SUPPLIER));
        supplier.setGrantedRoles(roles);

        billingContact = new BillingContact();
        billingContact.setKey(3456);
        billingContact.setOrganization(customer);
        billingContact.setAddress("companyAddress");
        billingContact.setBillingContactId("billingContactId");
        billingContact.setCompanyName("companyName");
        billingContact.setEmail("email");
        customer.getBillingContacts().add(billingContact);

        creditCard = new PaymentType();
        creditCard
                .setCollectionType(PaymentCollectionType.PAYMENT_SERVICE_PROVIDER);
        creditCard.setPaymentTypeId(PaymentType.CREDIT_CARD);

        invoice = new PaymentType();
        invoice.setCollectionType(PaymentCollectionType.ORGANIZATION);
        invoice.setPaymentTypeId(PaymentType.INVOICE);

        paymentInfoCreditCard = new PaymentInfo();
        paymentInfoCreditCard.setPaymentType(creditCard);
        paymentInfoCreditCard.setKey(1234);
        paymentInfoCreditCard.setOrganization(customer);
        paymentInfoCreditCard.setPaymentInfoId("CREDIT_CARD");
        paymentInfoCreditCard.setExternalIdentifier("identifier");
        customer.getPaymentInfos().add(paymentInfoCreditCard);

        paymentInfoInvoice = new PaymentInfo();
        paymentInfoInvoice.setPaymentType(invoice);
        paymentInfoInvoice.setKey(2345);
        paymentInfoInvoice.setOrganization(customer);
        paymentInfoInvoice.setPaymentInfoId("INVOICE");
        customer.getPaymentInfos().add(paymentInfoInvoice);

        OrganizationReference ref = new OrganizationReference(supplier,
                customer, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        supplier.getTargets().add(ref);
        customer.getSources().add(ref);

        // enable credit card for the customer
        OrganizationRefToPaymentType refToPt = new OrganizationRefToPaymentType();
        refToPt.setOrganizationReference(ref);
        refToPt.setOrganizationRole(new OrganizationRole(
                OrganizationRoleType.CUSTOMER));
        refToPt.setPaymentType(creditCard);
        ref.getPaymentTypes().add(refToPt);

        SupportedCurrency currency = new SupportedCurrency();
        currency.setCurrency(Currency.getInstance(Locale.GERMANY));

        PriceModel priceModel = new PriceModel();
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setCurrency(currency);

        Product product = new Product();
        product.setVendor(supplier);
        product.setPriceModel(priceModel);
        product.setTechnicalProduct(new TechnicalProduct());
        product.setAutoAssignUserEnabled(Boolean.FALSE);

        subscription = new Subscription();
        subscription.bindToProduct(product);
        subscription.setKey(4321);
        subscription.setOrganization(customer);
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        customer.getSubscriptions().add(subscription);

        // the logged in user
        Mockito.when(dataServiceMock.getCurrentUser()).thenReturn(user);

        // the billing contact to find
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(BillingContact.class),
                        Matchers.eq(billingContact.getKey()))).thenReturn(
                billingContact);

        // the payment information to find
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(PaymentInfo.class),
                        Matchers.eq(paymentInfoCreditCard.getKey())))
                .thenReturn(paymentInfoCreditCard);
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(PaymentInfo.class),
                        Matchers.eq(paymentInfoInvoice.getKey()))).thenReturn(
                paymentInfoInvoice);

        // the subscription to find
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(Subscription.class),
                        Matchers.eq(subscription.getKey()))).thenReturn(
                subscription);

        // the localized texts to return
        Mockito.when(
                localizerMock.getLocalizedTextFromDatabase(
                        Matchers.anyString(), Matchers.anyLong(),
                        Matchers.any(LocalizedObjectTypes.class))).thenReturn(
                "doesn't matter");

        subscriptionServiceBean.dataManager = dataServiceMock;
        subscriptionServiceBean.localizer = localizerMock;
        subscriptionServiceBean.appManager = applicationServiceMock;
        subscriptionServiceBean.sessionCtx = sessionContextMock;
        subscriptionServiceBean.audit = auditlogCollectorMock;

        manageBean = spy(new ManageSubscriptionBean());
        manageBean.dataManager = subscriptionServiceBean.dataManager;
        manageBean.audit = subscriptionServiceBean.audit;
        subscriptionServiceBean.manageBean = manageBean;

        bc = BillingContactAssembler.toVOBillingContact(billingContact);
        piCreditCard = PaymentInfoAssembler.toVOPaymentInfo(
                paymentInfoCreditCard,
                new LocalizerFacade(localizerMock, user.getLocale()));
        piInvoice = PaymentInfoAssembler.toVOPaymentInfo(paymentInfoInvoice,
                new LocalizerFacade(localizerMock, user.getLocale()));

        sub = SubscriptionAssembler.toVOSubscription(subscription,
                new LocalizerFacade(localizerMock, "en"));

        manageBean.userGroupService = mock(UserGroupServiceLocalBean.class);
        doReturn(Collections.EMPTY_LIST).when(manageBean.userGroupService)
                .getUserGroupsForUserWithRole(anyLong(), anyLong());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void modifySubscriptionPaymentData_SubscriptionNotFound()
            throws Exception {
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(Subscription.class),
                        Matchers.anyLong())).thenThrow(
                new ObjectNotFoundException());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscriptionPaymentData_SubscriptionNotOwned()
            throws Exception {
        subscription.setOrganization(new Organization());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void modifySubscriptionPaymentData_SubscriptionConcurrentlyChanged()
            throws Exception {
        sub.setVersion(sub.getVersion() - 1);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void modifySubscriptionPaymentData_BillingContactNull()
            throws Exception {
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, null,
                piCreditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void modifySubscriptionPaymentData_NoPSPIdentifier()
            throws Exception {
        paymentInfoCreditCard.setExternalIdentifier(null);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void modifySubscriptionPaymentData_PaymentInfoNull()
            throws Exception {
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void modifySubscriptionPaymentData_BillingContactNotFound()
            throws Exception {
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenThrow(
                new ObjectNotFoundException());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void modifySubscriptionPaymentData_PaymentInfoNotFound()
            throws Exception {
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(PaymentInfo.class),
                        Matchers.anyLong())).thenThrow(
                new ObjectNotFoundException());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscriptionPaymentData_BillingContactNotOwned()
            throws Exception {
        billingContact.setOrganization(new Organization());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscriptionPaymentData_PaymentInfoNotOwned()
            throws Exception {
        paymentInfoCreditCard.setOrganization(new Organization());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = PaymentInformationException.class)
    public void modifySubscriptionPaymentData_PaymentNotEnabled()
            throws Exception {
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piInvoice);
    }

    @Test(expected = PaymentInformationException.class)
    public void modifySubscriptionPaymentData_PaymentNotEnabledForProduct()
            throws Exception {
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test
    public void modifySubscriptionPaymentData_PaymentEnabledForProduct()
            throws Exception {

        setPaymentTypesForProduct(subscription.getProduct(),
                paymentInfoCreditCard.getPaymentType());
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void modifySubscriptionPaymentData_PaymentInfoConcurrentlyChanged()
            throws Exception {
        piCreditCard.setVersion(piCreditCard.getVersion() - 1);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void modifySubscriptionPaymentData_BillingContactConcurrentlyChanged()
            throws Exception {
        bc.setVersion(bc.getVersion() - 1);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test
    public void modifySubscriptionPaymentData_SuspendedToActive()
            throws Exception {
        setPaymentTypesForProduct(subscription.getProduct(),
                paymentInfoCreditCard.getPaymentType());
        VOSubscriptionDetails details = subscriptionServiceBean
                .modifySubscriptionPaymentData(sub, bc, piCreditCard);
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .activateInstance(Matchers.eq(subscription));
        validateResult(SubscriptionStatus.ACTIVE, details);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void modifySubscriptionPaymentData_SuspendedTechnicalServiceNotAliveException()
            throws Exception {
        Mockito.doThrow(new TechnicalServiceNotAliveException())
                .when(applicationServiceMock)
                .activateInstance(Matchers.eq(subscription));
        try {
            setPaymentTypesForProduct(subscription.getProduct(),
                    paymentInfoCreditCard.getPaymentType());
            subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                    piCreditCard);
        } finally {
            Mockito.verify(applicationServiceMock, Mockito.times(1))
                    .activateInstance(Matchers.eq(subscription));
            Assert.assertEquals(SubscriptionStatus.SUSPENDED,
                    subscription.getStatus());
            Mockito.verify(sessionContextMock, Mockito.times(1))
                    .setRollbackOnly();
        }
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void modifySubscriptionPaymentData_SuspendedTechnicalServiceOperationException()
            throws Exception {
        Mockito.doThrow(new TechnicalServiceOperationException())
                .when(applicationServiceMock)
                .activateInstance(Matchers.eq(subscription));
        try {
            setPaymentTypesForProduct(subscription.getProduct(),
                    paymentInfoCreditCard.getPaymentType());
            subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                    piCreditCard);
        } finally {
            Mockito.verify(applicationServiceMock, Mockito.times(1))
                    .activateInstance(Matchers.eq(subscription));
            Assert.assertEquals(SubscriptionStatus.SUSPENDED,
                    subscription.getStatus());
            Mockito.verify(sessionContextMock, Mockito.times(1))
                    .setRollbackOnly();
        }
    }

    @Test
    public void modifySubscriptionPaymentData_KeepPending() throws Exception {
        subscription.setStatus(SubscriptionStatus.PENDING);
        setPaymentTypesForProduct(subscription.getProduct(),
                paymentInfoCreditCard.getPaymentType());
        VOSubscriptionDetails details = subscriptionServiceBean
                .modifySubscriptionPaymentData(sub, bc, piCreditCard);
        validateResult(SubscriptionStatus.PENDING, details);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
    }

    @Test
    public void modifySubscriptionPaymentData_KeepActive() throws Exception {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        setPaymentTypesForProduct(subscription.getProduct(),
                paymentInfoCreditCard.getPaymentType());
        VOSubscriptionDetails details = subscriptionServiceBean
                .modifySubscriptionPaymentData(sub, bc, piCreditCard);
        validateResult(SubscriptionStatus.ACTIVE, details);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
        Mockito.verifyNoMoreInteractions(applicationServiceMock);
    }

    @Test(expected = SubscriptionStateException.class)
    public void modifySubscriptionPaymentData_SubscriptionDeactivated()
            throws Exception {
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = SubscriptionStateException.class)
    public void modifySubscriptionPaymentData_SubscriptionExpired()
            throws Exception {
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = SubscriptionStateException.class)
    public void modifySubscriptionPaymentData_SubscriptionInvalid()
            throws Exception {
        subscription.setStatus(SubscriptionStatus.INVALID);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc,
                piCreditCard);
    }

    @Test(expected = PaymentDataException.class)
    public void modifySubscriptionPaymentData_FreePriceModelBillingContactNotNull()
            throws Exception {
        subscription.getPriceModel().setType(PriceModelType.FREE_OF_CHARGE);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, bc, null);
    }

    @Test(expected = PaymentDataException.class)
    public void modifySubscriptionPaymentData_FreePriceModelPaymentInfoNotNull()
            throws Exception {
        subscription.getPriceModel().setType(PriceModelType.FREE_OF_CHARGE);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, null,
                piCreditCard);
    }

    @Test
    public void modifySubscriptionPaymentData_FreePriceModel() throws Exception {
        subscription.getPriceModel().setType(PriceModelType.FREE_OF_CHARGE);
        subscription.setBillingContact(billingContact);
        subscription.setPaymentInfo(paymentInfoCreditCard);
        subscriptionServiceBean.modifySubscriptionPaymentData(sub, null, null);
        Assert.assertNull(subscription.getBillingContact());
        Assert.assertNull(subscription.getPaymentInfo());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();
    }

    private void validateResult(SubscriptionStatus expectedStatus,
            VOSubscriptionDetails details) {
        Assert.assertSame(billingContact, subscription.getBillingContact());
        Assert.assertSame(paymentInfoCreditCard, subscription.getPaymentInfo());
        Assert.assertEquals(expectedStatus, subscription.getStatus());

        Assert.assertNotNull(details);
        Assert.assertEquals(subscription.getKey(), details.getKey());
        Assert.assertEquals(expectedStatus, details.getStatus());
    }

    /**
     * Sets the list of allowed payment types for the given Product as
     * specified.
     * 
     * @param product
     *            the product to change
     * @param paymentTypes
     *            the available payment types
     */
    private void setPaymentTypesForProduct(Product product,
            PaymentType... paymentTypes) {
        for (int i = 0; i < paymentTypes.length; i++) {
            ProductToPaymentType ptpt = new ProductToPaymentType(product,
                    paymentTypes[i]);
            subscription.getProduct().setPaymentTypes(
                    Collections.singletonList(ptpt));
        }
    }

    private OrganizationToRole createOrgToRole(OrganizationRoleType role) {
        OrganizationToRole orgToRole = new OrganizationToRole();
        OrganizationRole orgRole = new OrganizationRole();
        orgRole.setRoleName(role);
        orgToRole.setOrganizationRole(orgRole);
        return orgToRole;
    }

    private Set<RoleAssignment> newRoleAssignment(PlatformUser user,
            UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }
}
