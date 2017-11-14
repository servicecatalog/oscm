/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.subscriptionservice.dao.ProductDao;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

public class SubscriptionServiceBeanSubscribeToPartnerServiceTest {
    private static final long PRODUCT_TEMPLATE_KEY = 4711L;
    private static final String PRODUCT_TEMPLATE_ID = "TheProduct";
    private static final long PARTNER_TEMPLATE_KEY = 8888L;
    private static final String SERVICE_BASE_URL = "http://myservice.org";
    private static final long SUBSCRIPTION_KEY = 333L;
    private static final String SUBSCRIPTION_ID = "ASubscription";
    private static final String CUSTOMER_ORGID = "CustomerOrgId";
    private static final String SUPPLIER_ORGID = "MySupplierId";
    private static final String SUPPLIER_NAME = "MySupplier";
    private static final String RESELLER_ORGID = "MyResellerId";
    private static final String RESELLER_NAME = "MyReseller";

    private Organization supplierOrg;
    private Organization resellerOrg;
    private Organization customerOrg;
    private PriceModel templatePriceModel;
    private TechnicalProduct technicalProduct;
    private Product productTemplate;
    private Product partnerTemplate;
    private VOService service;
    private VOSubscription subscription;

    private DataService dsMock;
    private Query queryMock;
    private TriggerQueueServiceLocal triggerQueueServiceMock;
    private TenantProvisioningServiceBean tenantProvisioningMock;

    private SubscriptionServiceBean subscriptionServiceBean;
    private TerminateSubscriptionBean terminateBean;
    private ManageSubscriptionBean manageBean;
    private ProductDao productDao;
    private String queryName;
    private UserGroupServiceLocalBean userGroupService;
    private final OrganizationDao orgDao = mock(OrganizationDao.class);
    private final List<PlatformUser> givenUsers = new ArrayList<>();
    private ConfigurationServiceLocal cfgService;

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        createDomainObjects();

        subscriptionServiceBean = spy(new SubscriptionServiceBean() {
            @Override
            List<VOUda> getUdasForCustomer(String targetType,
                    long targetObjectKey, Organization supplier)
                    throws ValidationException, ObjectNotFoundException,
                    OperationNotPermittedException {
                return new ArrayList<>();
            }
        });
        subscriptionServiceBean.prodSessionMgmt = mock(
                SessionServiceLocal.class);
        subscriptionServiceBean.appManager = mock(
                ApplicationServiceLocal.class);
        subscriptionServiceBean.localizer = mock(LocalizerServiceLocal.class);
        subscriptionServiceBean.commService = mock(
                CommunicationServiceLocal.class);
        subscriptionServiceBean.idManager = mock(IdentityServiceLocal.class);
        subscriptionServiceBean.tqs = mock(TaskQueueServiceLocal.class);
        subscriptionServiceBean.audit = mock(
                SubscriptionAuditLogCollector.class);
        subscriptionServiceBean.audit = mock(
                SubscriptionAuditLogCollector.class);

        productDao = mock(ProductDao.class);
        doReturn(productDao).when(subscriptionServiceBean).getProductDao();
        terminateBean = spy(new TerminateSubscriptionBean());
        terminateBean.prodSessionMgmt = subscriptionServiceBean.prodSessionMgmt;
        terminateBean.appManager = subscriptionServiceBean.appManager;
        terminateBean.commService = subscriptionServiceBean.commService;
        terminateBean.tqs = subscriptionServiceBean.tqs;
        terminateBean.audit = subscriptionServiceBean.audit;

        subscriptionServiceBean.terminateBean = terminateBean;

        manageBean = spy(new ManageSubscriptionBean());
        manageBean.dataManager = subscriptionServiceBean.dataManager;
        manageBean.audit = subscriptionServiceBean.audit;
        subscriptionServiceBean.manageBean = manageBean;

        dsMock = mock(DataService.class);
        subscriptionServiceBean.dataManager = dsMock;
        triggerQueueServiceMock = mock(TriggerQueueServiceLocal.class);
        subscriptionServiceBean.triggerQS = triggerQueueServiceMock;
        tenantProvisioningMock = mock(TenantProvisioningServiceBean.class);
        subscriptionServiceBean.tenantProvisioning = tenantProvisioningMock;
        userGroupService = mock(UserGroupServiceLocalBean.class);
        subscriptionServiceBean.userGroupService = userGroupService;
        cfgService = mock(ConfigurationServiceLocal.class);
        subscriptionServiceBean.cfgService = cfgService;
        queryMock = mock(Query.class);
    }

    private void createDomainObjects() {
        customerOrg = new Organization();
        customerOrg.setOrganizationId(CUSTOMER_ORGID);

        supplierOrg = new Organization();
        supplierOrg.setOrganizationId(SUPPLIER_ORGID);
        supplierOrg.setName(SUPPLIER_NAME);

        resellerOrg = new Organization();
        resellerOrg.setOrganizationId(RESELLER_ORGID);
        resellerOrg.setName(RESELLER_NAME);
        addRole(resellerOrg, OrganizationRoleType.RESELLER);

        technicalProduct = new TechnicalProduct();
        technicalProduct.setOrganization(new Organization());
        technicalProduct.setBaseURL(SERVICE_BASE_URL);

        productTemplate = new Product();
        productTemplate.setTechnicalProduct(technicalProduct);
        productTemplate.setKey(PRODUCT_TEMPLATE_KEY);
        productTemplate.setProductId(PRODUCT_TEMPLATE_ID);
        productTemplate.setStatus(ServiceStatus.ACTIVE);
        productTemplate.setVendor(supplierOrg);
        templatePriceModel = new PriceModel();
        productTemplate.setPriceModel(templatePriceModel);

        partnerTemplate = productTemplate.copyForResale(resellerOrg);
        partnerTemplate.setKey(PARTNER_TEMPLATE_KEY);
        partnerTemplate.setPriceModel(templatePriceModel);

        service = new VOService();
        service.setKey(PARTNER_TEMPLATE_KEY);
        service.setServiceId("serviceId");

        subscription = new VOSubscription();
        subscription.setSubscriptionId(SUBSCRIPTION_ID);
        subscription.setKey(SUBSCRIPTION_KEY);
    }

    private void addRole(Organization org, OrganizationRoleType roleType) {
        OrganizationRole role = new OrganizationRole();
        role.setRoleName(roleType);
        OrganizationToRole otr = new OrganizationToRole();
        otr.setOrganizationRole(role);
        org.setGrantedRoles(Collections.singleton(otr));
    }

    private void defineMockBehavior() throws Exception {
        doAnswer(new Answer<Query>() {
            @Override
            public Query answer(InvocationOnMock invocation) throws Throwable {
                queryName = (String) invocation.getArguments()[0];
                return queryMock;
            }
        }).when(dsMock).createNamedQuery(anyString());

        doAnswer(new Answer<List<?>>() {
            @Override
            public List<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                if ("Product.getCopyForCustomer".equals(queryName)
                        || "Product.getForCustomerOnly".equals(queryName)) {
                    return new ArrayList<Product>();
                } else {
                    return null;
                }
            }
        }).when(queryMock).getResultList();

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if ("TriggerProcessIdentifier.isSubscribeOrUnsubscribeServicePending"
                        .equals(queryName)
                        || "TriggerProcessIdentifier.isModifyOrUpgradeSubscriptionPending"
                                .equals(queryName)) {
                    return Long.valueOf(0);
                } else {
                    return null;
                }
            }
        }).when(queryMock).getSingleResult();

        doAnswer(new Answer<PlatformUser>() {
            @Override
            public PlatformUser answer(InvocationOnMock invocation)
                    throws Throwable {
                PlatformUser user = new PlatformUser();
                user.setKey(1l);
                user.setOrganization(customerOrg);
                return user;
            }
        }).when(dsMock).getCurrentUser();

        doAnswer(new Answer<List<TriggerProcessMessageData>>() {
            @Override
            public List<TriggerProcessMessageData> answer(
                    InvocationOnMock invocation) throws Throwable {
                PlatformUser user = new PlatformUser();
                user.setOrganization(customerOrg);
                TriggerProcess tp = new TriggerProcess();
                tp.setUser(user);
                return Collections.singletonList(new TriggerProcessMessageData(
                        tp,
                        new TriggerMessage(TriggerType.SUBSCRIBE_TO_SERVICE)));
            }
        }).when(triggerQueueServiceMock)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

        doReturn(partnerTemplate).when(dsMock).getReference(Product.class,
                PARTNER_TEMPLATE_KEY);

        TenantProvisioningResult provisioningResult = new TenantProvisioningResult();
        provisioningResult.setBaseUrl(SERVICE_BASE_URL);
        doReturn(provisioningResult).when(tenantProvisioningMock)
                .createProductInstance(any(Subscription.class));

        doNothing().when(subscriptionServiceBean).informProductAboutNewUsers(
                any(Subscription.class), anyListOf(PlatformUser.class));
    }

    @Test
    public void subscribeToPartnerService() throws Exception {
        // given
        defineMockBehavior();
        doReturn(orgDao).when(subscriptionServiceBean.manageBean)
                .getOrganizationDao();
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());
        doReturn(true).when(cfgService).isPaymentInfoAvailable();

        // when
        VOSubscription voSub = subscriptionServiceBean.subscribeToService(
                subscription, service, null, null, null,
                new ArrayList<VOUda>());

        // then
        assertEquals("Wrong service key in subscription", PARTNER_TEMPLATE_KEY,
                voSub.getServiceKey());

        assertEquals("Wrong service id in subscription",
                partnerTemplate.getTemplate().getProductId(),
                voSub.getServiceId());

        assertEquals("Wrong vendor name in subscription", RESELLER_NAME,
                voSub.getSellerName());

        assertEquals("Wrong service base url in subscription", SERVICE_BASE_URL,
                voSub.getServiceBaseURL());

    }

    @Test(expected = OperationNotPermittedException.class)
    public void subscribeToService_serviceNotAvalible() throws Exception {
        // given
        defineMockBehavior();
        List<Long> invisibleProductKeys = new ArrayList<>();
        invisibleProductKeys.add(Long.valueOf(PARTNER_TEMPLATE_KEY));
        doReturn(invisibleProductKeys).when(userGroupService)
                .getInvisibleProductKeysForUser(1l);
        // when
        try {
            subscriptionServiceBean.subscribeToService(subscription, service,
                    null, null, null, new ArrayList<VOUda>());
            // then
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("is not avalible."));
            throw e;
        }

    }
}
