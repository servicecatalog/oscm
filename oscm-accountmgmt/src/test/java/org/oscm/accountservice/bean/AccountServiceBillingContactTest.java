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
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.accountservice.assembler.BillingContactAssembler;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOBillingContact;

/**
 * Tests for billing contact services.
 * 
 * @author weiser
 * 
 */
public class AccountServiceBillingContactTest {

    private AccountServiceBean accountServiceBean;

    private DataService dataServiceMock;
    private SessionContext sessionContextMock;
    private ApplicationServiceLocal applicationServiceMock;

    private Organization organization;
    private BillingContact billingContact;

    private VOBillingContact bc;

    @Before
    public void setup() throws Exception {
        accountServiceBean = new AccountServiceBean();

        dataServiceMock = Mockito.mock(DataService.class);
        sessionContextMock = Mockito.mock(SessionContext.class);
        applicationServiceMock = Mockito.mock(ApplicationServiceLocal.class);

        PlatformUser user = new PlatformUser();
        organization = new Organization();
        user.setOrganization(organization);

        billingContact = new BillingContact();
        billingContact.setKey(0);
        billingContact.setOrganization(organization);
        billingContact.setAddress("companyAddress");
        billingContact.setBillingContactId("billingContactId");
        billingContact.setCompanyName("companyName");
        billingContact.setEmail("mail@test.de");

        organization.getBillingContacts().add(billingContact);

        // the logged in user
        Mockito.when(dataServiceMock.getCurrentUser()).thenReturn(user);

        // the billing contact to find
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(BillingContact.class),
                        Matchers.eq(billingContact.getKey()))).thenReturn(
                billingContact);
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.eq(billingContact.getKey()))).thenReturn(
                billingContact);

        accountServiceBean.dm = dataServiceMock;
        accountServiceBean.sessionCtx = sessionContextMock;
        accountServiceBean.appManager = applicationServiceMock;

        bc = BillingContactAssembler.toVOBillingContact(billingContact);

    }

    @Test
    public void delete() throws Exception {
        accountServiceBean.deleteBillingContact(bc);
        // check what's passed for removal
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Assert.assertEquals(1, args.length);
                Assert.assertNotNull(args[0]);
                Assert.assertSame(billingContact, args[0]);
                return null;
            }
        }).when(dataServiceMock).remove(Matchers.any(BillingContact.class));
        Mockito.verify(dataServiceMock, Mockito.times(1)).remove(
                Matchers.any(BillingContact.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void delete_Null() throws Exception {
        accountServiceBean.deleteBillingContact(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void delete_NotFound() throws Exception {
        // throw object not found
        Mockito.when(
                dataServiceMock.getReference(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenThrow(
                new ObjectNotFoundException());
        accountServiceBean.deleteBillingContact(bc);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void delete_ConcurrentlyChanged() throws Exception {
        // as we cannot increase the version of the domain object, decrease the
        // one of the value object
        bc.setVersion(bc.getVersion() - 1);
        accountServiceBean.deleteBillingContact(bc);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void delete_NotOwner() throws Exception {
        billingContact.setOrganization(new Organization());
        accountServiceBean.deleteBillingContact(bc);
    }

    @Test
    public void delete_SubscriptionsChargeableActive() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        sub.getPriceModel().setType(PriceModelType.PRO_RATA);
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
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
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
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
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
        Mockito.verify(applicationServiceMock, Mockito.times(1))
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsFreeActive() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.ACTIVE);
        Subscription sub = subs.iterator().next();
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsPending() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.PENDING);
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.PENDING, sub.getStatus());
    }

    @Test
    public void delete_SubscriptionsSuspended() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.SUSPENDED);
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());
    }

    @Test
    public void delete_SubscriptionsDeactivated() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.DEACTIVATED);
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.DEACTIVATED, sub.getStatus());
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsExpired() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.EXPIRED);
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.EXPIRED, sub.getStatus());
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void delete_SubscriptionsInvalid() throws Exception {
        Set<Subscription> subs = createSubs(SubscriptionStatus.INVALID);
        billingContact.setSubscriptions(subs);
        accountServiceBean.deleteBillingContact(bc);
        // verify that setRollbackOnly has been called never
        Mockito.verify(sessionContextMock, Mockito.never()).setRollbackOnly();
        Subscription sub = subs.iterator().next();
        Assert.assertNull(sub.getBillingContact());
        Assert.assertEquals(SubscriptionStatus.INVALID, sub.getStatus());
        Mockito.verify(applicationServiceMock, Mockito.never())
                .deactivateInstance(Matchers.eq(sub));
    }

    @Test
    public void get() {
        List<VOBillingContact> list = accountServiceBean.getBillingContacts();
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        VOBillingContact vobc = list.get(0);
        Assert.assertEquals(billingContact.getAddress(), vobc.getAddress());
        Assert.assertEquals(billingContact.getBillingContactId(), vobc.getId());
        Assert.assertEquals(billingContact.getCompanyName(),
                vobc.getCompanyName());
        Assert.assertEquals(billingContact.getEmail(), vobc.getEmail());
        Assert.assertEquals(billingContact.getKey(), vobc.getKey());
        Assert.assertEquals(billingContact.getVersion(), vobc.getVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void save_Null() throws Exception {
        accountServiceBean.saveBillingContact(null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void save_NotOwner() throws Exception {
        billingContact.setOrganization(new Organization());
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void save_ConcurrentlyChanged() throws Exception {
        bc.setVersion(bc.getVersion() - 1);
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_InvalidId() throws Exception {
        bc.setId(null);
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_InvalidEmail() throws Exception {
        bc.setEmail("invalidemail");
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_InvalidCompanyName() throws Exception {
        bc.setCompanyName("   ");
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_InvalidAddress() throws Exception {
        bc.setAddress(null);
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void save_NonUniqueBusinessKey() throws Exception {
        billingContact.setKey(123);
        bc.setKey(123);
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(billingContact);

        Mockito.doThrow(new NonUniqueBusinessKeyException())
                .when(dataServiceMock)
                .validateBusinessKeyUniqueness(Matchers.eq(billingContact));
        try {
            accountServiceBean.saveBillingContact(bc);
        } finally {
            Mockito.verify(sessionContextMock, Mockito.times(1))
                    .setRollbackOnly();
        }
    }

    @Test
    public void save_Update() throws Exception {
        bc.setAddress("testaddress");
        bc.setCompanyName("testcompanyname");
        bc.setEmail("test@mail.de");
        bc.setId("testid");

        // capture the argument passed to persist
        ArgumentCaptor<BillingContact> ac = ArgumentCaptor
                .forClass(BillingContact.class);

        VOBillingContact newBC = accountServiceBean.saveBillingContact(bc);
        Assert.assertEquals(bc.getAddress(), billingContact.getAddress());
        Assert.assertEquals(bc.getCompanyName(),
                billingContact.getCompanyName());
        Assert.assertEquals(bc.getEmail(), billingContact.getEmail());
        Assert.assertEquals(bc.getId(), billingContact.getBillingContactId());
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();

        // validate that a new billing contact with same id and organization has
        // been passed for business key uniqueness validation - but must not be
        // the same as the read one.
        Mockito.verify(dataServiceMock, Mockito.times(1))
                .validateBusinessKeyUniqueness(ac.capture());
        Assert.assertEquals(0, ac.getValue().getKey());
        Assert.assertEquals(bc.getId(), ac.getValue().getBillingContactId());
        Assert.assertEquals(organization, ac.getValue().getOrganization());
        Assert.assertNotSame(billingContact, ac.getValue());

        Assert.assertEquals(bc.getAddress(), newBC.getAddress());
        Assert.assertEquals(bc.getCompanyName(), newBC.getCompanyName());
        Assert.assertEquals(bc.getEmail(), newBC.getEmail());
        Assert.assertEquals(bc.getId(), newBC.getId());
    }

    @Test
    public void save_Create() throws Exception {
        // don't find the billing contact
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(null);
        // capture the argument passed to persist
        ArgumentCaptor<BillingContact> ac = ArgumentCaptor
                .forClass(BillingContact.class);

        bc.setAddress("testaddress");
        bc.setCompanyName("testcompanyname");
        bc.setEmail("test@mail.de");
        bc.setId("testid");

        VOBillingContact newBC = accountServiceBean.saveBillingContact(bc);
        Mockito.verify(dataServiceMock, Mockito.times(1)).flush();

        Mockito.verify(dataServiceMock).persist(ac.capture());

        Assert.assertEquals(bc.getAddress(), newBC.getAddress());
        Assert.assertEquals(bc.getCompanyName(), newBC.getCompanyName());
        Assert.assertEquals(bc.getEmail(), newBC.getEmail());
        Assert.assertEquals(bc.getId(), newBC.getId());

        BillingContact persisted = ac.getValue();
        Assert.assertEquals(bc.getAddress(), persisted.getAddress());
        Assert.assertEquals(bc.getCompanyName(), persisted.getCompanyName());
        Assert.assertEquals(bc.getEmail(), persisted.getEmail());
        Assert.assertEquals(bc.getId(), persisted.getBillingContactId());
    }

    @Test(expected = ValidationException.class)
    public void save_Create_InvalidId() throws Exception {
        // don't find the billing contact
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(null);
        bc.setId(null);
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_Create_InvalidEmail() throws Exception {
        // don't find the billing contact
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(null);
        bc.setEmail("invalidemail");
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_Create_InvalidCompanyName() throws Exception {
        // don't find the billing contact
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(null);
        bc.setCompanyName("   ");
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ValidationException.class)
    public void save_Create_InvalidAddress() throws Exception {
        // don't find the billing contact
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(null);
        bc.setAddress(null);
        accountServiceBean.saveBillingContact(bc);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void save_Create_ConcurrentDeletion() throws Exception {
        // don't find the billing contact
        bc.setKey(123);
        Mockito.when(
                dataServiceMock.find(Matchers.eq(BillingContact.class),
                        Matchers.anyLong())).thenReturn(null);
        accountServiceBean.saveBillingContact(bc);
    }

    private Set<Subscription> createSubs(SubscriptionStatus state) {
        Subscription sub = new Subscription();
        sub.setStatus(state);
        sub.setBillingContact(billingContact);
        Product product = new Product();
        product.setPriceModel(new PriceModel());
        sub.bindToProduct(product);
        Set<Subscription> subs = Collections.singleton(sub);
        return subs;
    }
}
