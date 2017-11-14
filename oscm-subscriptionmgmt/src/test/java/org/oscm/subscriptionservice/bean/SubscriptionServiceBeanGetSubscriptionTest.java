/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.dao.SubscriptionDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.vo.VOSubscriptionDetails;

public class SubscriptionServiceBeanGetSubscriptionTest {
    private static final String CUSTOMER_ORGID = "CustomerOrgId";
    private static final String SUPPLIER_ORGID = "SupplierOrgId";
    private static final String BROKER_ORGID = "MyBrokerId";
    private static final String RESELLER_ORGID = "MyResellerId";
    private static final String PRODUCT_ID = "MyProduct";
    private static final String SUBSCRIPTION_ID = "ASubscription";
    private static final Set<SubscriptionStatus> states = EnumSet.of(
            SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING);

    private PlatformUser currentUser;
    private Subscription subscription;
    private Product product;
    private Organization customerOrg;

    private DataService dsMock;
    private SubscriptionServiceBean subscriptionServiceBean;
    private SubscriptionDao subscriptionDao;
    private Pagination pagination;

    @Before
    public void setup() throws Exception {
        pagination = new Pagination();
        pagination.setLimit(10);
        pagination.setOffset(20);
        subscriptionDao = mock(SubscriptionDao.class);
        subscriptionServiceBean = spy(new SubscriptionServiceBean());
        dsMock = mock(DataService.class);
        subscriptionServiceBean.audit = mock(SubscriptionAuditLogCollector.class);
        subscriptionServiceBean.dataManager = dsMock;
        subscriptionServiceBean.localizer = mock(LocalizerServiceLocal.class);
        doReturn(subscriptionDao).when(subscriptionServiceBean)
                .getSubscriptionDao();
        when(
                subscriptionDao
                        .getSubscriptionsForMyBrokerCustomers(any(Organization.class)))
                .thenReturn(new ArrayList<Subscription>());
        when(
                subscriptionDao.getSubscriptionsForMyCustomers(
                        any(Organization.class),
                        anySetOf(SubscriptionStatus.class))).thenReturn(
                new ArrayList<Subscription>());

        doAnswer(new Answer<DomainObject<?>>() {
            @Override
            public DomainObject<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                DomainObject<?> arg = (DomainObject<?>) invocation
                        .getArguments()[0];
                if (arg instanceof Organization) {
                    Organization org = (Organization) arg;
                    if (org.getOrganizationId().equals(CUSTOMER_ORGID)) {
                        return customerOrg;
                    }
                } else if (arg instanceof Subscription) {
                    Subscription sub = (Subscription) arg;
                    if (sub.getSubscriptionId().equals(SUBSCRIPTION_ID)
                            && sub.getOrganizationKey() == customerOrg.getKey()) {
                        return subscription;
                    }
                }

                return null;
            }
        }).when(dsMock).getReferenceByBusinessKey(any(DomainObject.class));

        doAnswer(new Answer<PlatformUser>() {
            @Override
            public PlatformUser answer(InvocationOnMock invocation)
                    throws Throwable {
                return currentUser;
            }
        }).when(dsMock).getCurrentUser();
    }

    private Organization createCustomer() {
        Organization customerOrg = new Organization();
        customerOrg.setOrganizationId(CUSTOMER_ORGID);
        return customerOrg;
    }

    private PlatformUser createSupplier(Organization custOrg) {
        PlatformUser supplierAdmin = new PlatformUser();

        Organization supplierOrg = new Organization();
        supplierOrg.setOrganizationId(SUPPLIER_ORGID);
        addRole(supplierOrg, OrganizationRoleType.SUPPLIER);

        createOrgReference(supplierOrg, custOrg,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        supplierAdmin.setOrganization(supplierOrg);
        return supplierAdmin;
    }

    private PlatformUser createBroker(Organization custOrg) {
        PlatformUser brokerAdmin = new PlatformUser();

        Organization brokerOrg = new Organization();
        brokerOrg.setOrganizationId(BROKER_ORGID);
        addRole(brokerOrg, OrganizationRoleType.BROKER);

        createOrgReference(brokerOrg, custOrg,
                OrganizationReferenceType.BROKER_TO_CUSTOMER);

        brokerAdmin.setOrganization(brokerOrg);
        return brokerAdmin;
    }

    private PlatformUser createReseller(Organization custOrg) {
        PlatformUser resellerAdmin = new PlatformUser();

        Organization resellerOrg = new Organization();
        resellerOrg.setOrganizationId(RESELLER_ORGID);
        addRole(resellerOrg, OrganizationRoleType.RESELLER);

        createOrgReference(resellerOrg, custOrg,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);

        resellerAdmin.setOrganization(resellerOrg);
        return resellerAdmin;
    }

    private void createOrgReference(Organization source, Organization target,
            OrganizationReferenceType orgRefType) {
        OrganizationReference orgRef = new OrganizationReference(source,
                target, orgRefType);
        source.setTargets(Arrays.asList(new OrganizationReference[] { orgRef }));
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private Product createProduct() {
        Product prod = new Product();
        prod.setProductId(PRODUCT_ID);
        prod.setTechnicalProduct(new TechnicalProduct());
        prod.setAutoAssignUserEnabled(Boolean.FALSE);
        return prod;
    }

    private Subscription createSubscription(Product prod, Organization custOrg) {
        Subscription sub = new Subscription();
        sub.setSubscriptionId(SUBSCRIPTION_ID);
        sub.setProduct(prod);
        sub.setOrganization(custOrg);
        return sub;
    }

    @Test
    public void getSubscriptionForCustomer_Supplier() throws Exception {
        // given
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);
        product = createProduct();
        subscription = createSubscription(product, customerOrg);

        // when
        VOSubscriptionDetails voSubscr = subscriptionServiceBean
                .getSubscriptionForCustomer(CUSTOMER_ORGID, SUBSCRIPTION_ID);

        // then
        assertEquals("Wrong subscription ID", SUBSCRIPTION_ID,
                voSubscr.getSubscriptionId());
        assertEquals("Wrong product ID", PRODUCT_ID, voSubscr.getServiceId());
    }

    @Test
    public void getSubscriptionForCustomer_Broker() throws Exception {
        // given
        customerOrg = createCustomer();
        currentUser = createBroker(customerOrg);
        product = createProduct();
        subscription = createSubscription(product, customerOrg);

        // when
        VOSubscriptionDetails voSubscr = subscriptionServiceBean
                .getSubscriptionForCustomer(CUSTOMER_ORGID, SUBSCRIPTION_ID);

        // then
        assertEquals("Wrong subscription ID", SUBSCRIPTION_ID,
                voSubscr.getSubscriptionId());
        assertEquals("Wrong product ID", PRODUCT_ID, voSubscr.getServiceId());
    }

    @Test
    public void getSubscriptionForCustomer_Reseller() throws Exception {
        // given
        customerOrg = createCustomer();
        currentUser = createReseller(customerOrg);
        product = createProduct();
        subscription = createSubscription(product, customerOrg);

        // when
        VOSubscriptionDetails voSubscr = subscriptionServiceBean
                .getSubscriptionForCustomer(CUSTOMER_ORGID, SUBSCRIPTION_ID);

        // then
        assertEquals("Wrong subscription ID", SUBSCRIPTION_ID,
                voSubscr.getSubscriptionId());
        assertEquals("Wrong product ID", PRODUCT_ID, voSubscr.getServiceId());
    }

    @Test
    public void getSubcsriptionsForManagers() throws Exception {
        // given
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.PENDING_UPD, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD);
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);
        // when
        List<Subscription> list = subscriptionServiceBean
                .getSubscriptionsForManagers();

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsForMyBrokerCustomers(
                eq(currentUser.getOrganization()));
        verify(subscriptionDao, times(1)).getSubscriptionsForMyCustomers(
                eq(currentUser.getOrganization()), eq(states));
        assertNotNull(list);
    }

    @Test
    public void getSubcsriptionsForManagersWithPagination() throws Exception {
        // given
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.PENDING_UPD, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD);
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);
        // when
        List<Subscription> list = subscriptionServiceBean
                .getSubscriptionsForManagers(pagination);

        // then
		verify(subscriptionDao, times(1)).getSubscriptionsForMyBrokerCustomers(
				eq(currentUser), eq(states), eq(pagination));

        assertNotNull(list);
    }

    @Test
    public void executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers()
            throws Exception {
        // given
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);
        // when
        List<Subscription> list = subscriptionServiceBean
                .executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers();

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsForMyBrokerCustomers(
                eq(currentUser.getOrganization()));
        assertNotNull(list);

    }

    @Test
    public void executeQueryLoadSubIdsAndOrgsForMyBrokerCustomersWithPagination()
            throws Exception {
        // given
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);
        // when
        List<Subscription> list = subscriptionServiceBean
                .executeQueryLoadSubIdsAndOrgsForMyBrokerCustomers(states,
                        pagination);

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsForMyBrokerCustomers(
                eq(currentUser), eq(states), eq(pagination));
        assertNotNull(list);

    }

    @Test
    public void executeQueryLoadSubIdsAndOrgsForMyCustomers() throws Exception {
        // given
        Set<SubscriptionStatus> states = EnumSet.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PENDING);
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);

        // when
        List<Subscription> list = subscriptionServiceBean
                .executeQueryLoadSubIdsAndOrgsForMyCustomers(states);

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsForMyCustomers(
                any(Organization.class), eq(states));

        assertNotNull(list);
    }

    @Test
    public void executeQueryLoadSubIdsAndOrgsForMyCustomersWithPagination()
            throws Exception {
        // given
        customerOrg = createCustomer();
        currentUser = createSupplier(customerOrg);

        // when
        List<Subscription> list = subscriptionServiceBean
                .executeQueryLoadSubIdsAndOrgsForMyCustomers(states, pagination);

        // then
        verify(subscriptionDao, times(1)).getSubscriptionsForMyCustomers(
                any(PlatformUser.class), eq(states), eq(pagination));

        assertNotNull(list);
    }
}
