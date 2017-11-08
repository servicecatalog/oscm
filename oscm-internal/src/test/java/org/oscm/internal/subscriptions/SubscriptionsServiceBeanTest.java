/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 14, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOUserSubscription;

/**
 * @author tokoda
 * 
 */
public class SubscriptionsServiceBeanTest {

    private static final int TECH_PRODUCT_KEY = 1;
    private static final int PRODUCT_KEY = 2;
    private static final int SUBSCRIPTION_KEY = 3;
    private static final String SUBSCRIPTION_ID = "subscriptionid";
    private static final String VENDER_ID = "venderid";
    private static final String VENDER_NAME = "vendername";
    private static String CUSTOMER_NAME = "customerExample";
    private static String CUSTOMER_ORGID = "customerExampleID";
    private static String PRODUCT_ID = "product_Id";
    private static long ACTIVATION_DATE = 1383844091182L;
    private static final String FACADE_RETURN_ACCESSINFO = "accessInfo";
    private static final String FACADE_RETURN_PROGRESS = "progress";

    private SubscriptionsServiceBean bean;
    private SubscriptionService subscrService;
    private SubscriptionServiceLocal subscriptionServiceLocal;

    @Before
    public void setup() {
        bean = new SubscriptionsServiceBean();
        bean.slService = mock(SubscriptionListServiceLocal.class);
        subscriptionServiceLocal = mock(SubscriptionServiceLocal.class);
        subscrService = mock(SubscriptionService.class);
        bean.subscriptionService = subscrService;
        bean.subscriptionServiceLocal = subscriptionServiceLocal;
    }

    @Test
    public void getSubscriptionsForOrg_NoSubscription() {

        LocalizerFacade facade = getLocalizerFacadeMock();
        bean = spy(bean);
        doReturn(facade).when(bean).getLocalizerFacade();
        doReturn(new POSubscriptionForList()).when(bean)
                .toPOSubscriptionForList(any(Subscription.class),
                        any(LocalizerFacade.class));

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        when(
                bean.slService
                        .getSubscriptionsForOrganization(anySetOf(SubscriptionStatus.class)))
                .thenReturn(subscriptions);
        // when
        Response response = bean.getSubscriptionsForOrg(null);

        // then
        List<POSubscriptionForList> result = response
                .getResultList(POSubscriptionForList.class);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getSubscriptionsForOrg() {
        // given
        LocalizerFacade facade = getLocalizerFacadeMock();
        bean = spy(bean);
        doReturn(facade).when(bean).getLocalizerFacade();
        doReturn(new POSubscriptionForList()).when(bean)
                .toPOSubscriptionForList(any(Subscription.class),
                        any(LocalizerFacade.class));

        List<Subscription> subscriptions = new ArrayList<Subscription>();
        subscriptions.add(new Subscription());
        subscriptions.add(new Subscription());

        when(
                bean.slService
                        .getSubscriptionsForOrganization(anySetOf(SubscriptionStatus.class)))
                .thenReturn(subscriptions);

        // when
        Response response = bean.getSubscriptionsForOrg(null);

        // then
        List<POSubscriptionForList> result = response
                .getResultList(POSubscriptionForList.class);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void toPOSubscriptionForList() {
        // given
        Subscription subscription = getSubscription(SubscriptionStatus.ACTIVE);
        LocalizerFacade facade = getLocalizerFacadeMock();

        // when
        POSubscriptionForList po = bean.toPOSubscriptionForList(subscription,
                facade);

        // then
        assertEquals(SUBSCRIPTION_ID, po.getSubscriptionId());
        assertEquals(PRODUCT_KEY, po.getServiceKey());
        assertEquals(VENDER_NAME, po.getSupplierName());
        assertEquals(VENDER_ID, po.getSupplierOrganizationId());
        assertEquals(null, po.getAccessUrl());
        assertTrue(po.isAccessViaAccessInfo());
        assertEquals(SubscriptionStatus.class.getSimpleName() + "."
                + SubscriptionStatus.ACTIVE.name(), po.getStatusTextKey());
        assertTrue(po.isStatusActive());
        assertFalse(po.isStatusPending());
        assertEquals(FACADE_RETURN_ACCESSINFO, po.getServiceAccessInfo());
        assertEquals(2, po.getNumberOfAssignedUsers());
        assertEquals(FACADE_RETURN_PROGRESS, po.getProvisioningProgress());
        assertFalse(po.isProvisioningProgressRendered());
    }

    @Test
    public void toPOSubscriptionForList_PendingUpdate() {
        // given
        Subscription subscription = getSubscription(SubscriptionStatus.PENDING_UPD);
        LocalizerFacade facade = getLocalizerFacadeMock();

        // when
        POSubscriptionForList po = bean.toPOSubscriptionForList(subscription,
                facade);
        // then
        assertEquals(SUBSCRIPTION_ID, po.getSubscriptionId());
        assertEquals(SubscriptionStatus.class.getSimpleName() + "."
                + SubscriptionStatus.PENDING_UPD.name(), po.getStatusTextKey());
        assertTrue(po.isStatusPendingUpd());
        assertFalse(po.isStatusActive());
        assertFalse(po.isStatusPending());
    }

    @Test
    public void toPOSubscriptionForList_ProvisioningProgressRendered() {
        // given
        Subscription subscription = getSubscription(SubscriptionStatus.ACTIVE);
        subscription.setStatus(SubscriptionStatus.PENDING);
        LocalizerFacade facade = getLocalizerFacadeMock();

        // when
        POSubscriptionForList po = bean.toPOSubscriptionForList(subscription,
                facade);

        // then
        assertEquals(SubscriptionStatus.class.getSimpleName() + "."
                + SubscriptionStatus.PENDING.name(), po.getStatusTextKey());
        assertFalse(po.isStatusActive());
        assertTrue(po.isStatusPending());
        assertTrue(po.isProvisioningProgressRendered());
    }

    private LocalizerFacade getLocalizerFacadeMock() {
        LocalizerFacade facade = mock(LocalizerFacade.class);
        when(
                facade.getText(TECH_PRODUCT_KEY,
                        LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC))
                .thenReturn(FACADE_RETURN_ACCESSINFO);
        when(
                facade.getText(SUBSCRIPTION_KEY,
                        LocalizedObjectTypes.SUBSCRIPTION_PROVISIONING_PROGRESS))
                .thenReturn(FACADE_RETURN_PROGRESS);
        return facade;
    }

    private Subscription getSubscription(SubscriptionStatus status) {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setAccessType(ServiceAccessType.DIRECT);
        techProduct.setKey(TECH_PRODUCT_KEY);

        Organization vender = new Organization();
        vender.setOrganizationId(VENDER_ID);
        vender.setName(VENDER_NAME);

        Product template = new Product();
        template.setVendor(vender);

        Product product = new Product();
        product.setKey(PRODUCT_KEY);
        product.setTechnicalProduct(techProduct);
        product.setTemplate(template);

        List<UsageLicense> users = new ArrayList<UsageLicense>();
        UsageLicense user1 = new UsageLicense();
        users.add(user1);
        UsageLicense user2 = new UsageLicense();
        users.add(user2);

        Subscription subscription = new Subscription();
        subscription.setKey(SUBSCRIPTION_KEY);
        subscription.setSubscriptionId(SUBSCRIPTION_ID);
        subscription.setStatus(status);
        subscription.setProduct(product);
        subscription.setUsageLicenses(users);

        return subscription;
    }

    @Test
    public void testGetMySubscriptions() {
        // given
        List<VOUserSubscription> expectedSubscriptionList = new ArrayList<VOUserSubscription>();
        VOUserSubscription sub = new VOUserSubscription();
        sub.setKey(1L);
        sub.setServiceId("MyService");

        when(subscrService.getSubscriptionsForCurrentUser()).thenReturn(
                expectedSubscriptionList);
        // when
        Response response = bean.getMySubscriptions();
        // then
        verify(bean.subscriptionService, times(1))
                .getSubscriptionsForCurrentUser();
        assertNotNull(response.getResultList(VOUserSubscription.class));
        assertEquals(expectedSubscriptionList,
                response.getResultList(VOUserSubscription.class));
    }

    @Test
    public void getSubscriptionsAndCustomersForManagers() throws Exception {
        // given
        List<Subscription> expectedSubscriptions = new ArrayList<Subscription>();
        expectedSubscriptions.addAll(givenSubscriptions());
        when(subscriptionServiceLocal.getSubscriptionsForManagers())
                .thenReturn(expectedSubscriptions);

        // when
        Response response = bean.getSubscriptionsAndCustomersForManagers();
        List<POSubscriptionAndCustomer> result = response
                .getResultList(POSubscriptionAndCustomer.class);

        // then
        verify(bean.subscriptionServiceLocal, times(1))
                .getSubscriptionsForManagers();
        assertNotNull(result);
        assertEquals(Long.valueOf(ACTIVATION_DATE),
                Long.valueOf(result.get(0).getActivation()));
        assertEquals(CUSTOMER_NAME, result.get(0).getCustomerName());
        assertEquals(PRODUCT_ID, result.get(1).getServiceId());
        assertEquals(SUBSCRIPTION_ID, result.get(1).getSubscriptionId());
    }

    @Test
    public void getSubscriptionsAndCustomersForManagersWithPagination()
            throws Exception {
        bean = spy(bean);
        LocalizerFacade facade = getLocalizerFacadeMock();
        doReturn(facade).when(bean).getLocalizerFacade();
        // given
        Pagination pagination = new Pagination();
        pagination.setOffset(20);
        pagination.setLimit(10);
        List<Subscription> expectedSubscriptions = new ArrayList<Subscription>();
        expectedSubscriptions.addAll(givenSubscriptions());
        when(subscriptionServiceLocal.getSubscriptionsForManagers(pagination))
                .thenReturn(expectedSubscriptions);
        
        // when
        Response response = bean
                .getSubscriptionsAndCustomersForManagers(pagination);
        List<POSubscriptionAndCustomer> result = response
                .getResultList(POSubscriptionAndCustomer.class);

        // then
        verify(bean.subscriptionServiceLocal, times(1))
                .getSubscriptionsForManagers(eq(pagination));
        assertNotNull(result);
        assertEquals(Long.valueOf(ACTIVATION_DATE),
                Long.valueOf(result.get(0).getActivation()));
        assertEquals(CUSTOMER_NAME, result.get(0).getCustomerName());
        assertEquals(PRODUCT_ID, result.get(1).getServiceId());
        assertEquals(SUBSCRIPTION_ID, result.get(1).getSubscriptionId());
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getSubscriptionsAndCustomersForManagers_OrganizationNotAuthorzied()
            throws Exception {
        // given
        when(subscriptionServiceLocal.getSubscriptionsForManagers()).thenThrow(
                new OrganizationAuthoritiesException());

        // when
        bean.getSubscriptionsAndCustomersForManagers();

        // then
        verify(bean.subscriptionServiceLocal, times(1))
                .getSubscriptionsForManagers();
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void getSubscriptionsAndCustomersForManagersWithPaginationOrganizationNotAuthorzied()
            throws Exception {
        // given
        Pagination pagination = new Pagination();
        pagination.setOffset(20);
        pagination.setLimit(10);
        when(subscriptionServiceLocal.getSubscriptionsForManagers(pagination))
                .thenThrow(new OrganizationAuthoritiesException());

        // when
        bean.getSubscriptionsAndCustomersForManagers(pagination);

        // then
        verify(bean.subscriptionServiceLocal, times(1))
                .getSubscriptionsForManagers(pagination);
    }

    private List<Subscription> givenSubscriptions() {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptionList.add(createSubscription());
        subscriptionList.add(createSubscription());
        return subscriptionList;
    }

    private Subscription createSubscription() {
        Subscription subscription1 = new Subscription();
        Organization customer = new Organization();
        Product product = new Product();
        product.setTechnicalProduct(new TechnicalProduct());
        customer.setOrganizationId(CUSTOMER_ORGID);
        customer.setName(CUSTOMER_NAME);
        product.setProductId(PRODUCT_ID);
        subscription1.setSubscriptionId(SUBSCRIPTION_ID);
        subscription1.setOrganization(customer);
        subscription1.setProduct(product);
        subscription1.setActivationDate(Long.valueOf(ACTIVATION_DATE));
        subscription1.setStatus(SubscriptionStatus.ACTIVE);
        return subscription1;
    }

    @Test
    public void getMySubscriptionDetailsTest() {
        bean = spy(bean);
        LocalizerFacade facade = getLocalizerFacadeMock();
        doReturn(facade).when(bean).getLocalizerFacade();

        Subscription sub = createSubscription();
        sub = spy(sub);
        when(subscriptionServiceLocal.getMySubscriptionDetails(1L)).thenReturn(sub);
        bean.dm = mock(DataService.class);
        when(bean.dm.getCurrentUser()).thenReturn(new PlatformUser());

        POSubscription mySubscriptionDetails = bean.getMySubscriptionDetails(1L);

        assertNotNull(mySubscriptionDetails);
    }
}
