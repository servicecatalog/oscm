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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.domobjects.UserRole;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.subscriptionservice.dao.ProductDao;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

public class SubscriptionServiceBeanTriggerIdTest
        extends SubscriptionServiceMockBase {

    private SubscriptionServiceBean subscriptionServiceBean;
    private final List<VOUsageLicense> usersToAdd = new ArrayList<>();
    private final List<VOUser> usersToRevoke = new ArrayList<>();
    private final List<TriggerProcessMessageData> triggerMessageData = new ArrayList<>();
    private final List<VOSubscription> subscriptions = new ArrayList<>();
    private Organization callingOrg;
    private TriggerProcess storedTriggerProcess;
    private VOSubscription subscription;
    private VOService service;
    private VOService compatibleService;
    private DataService dsMock;
    private Query queryMock;
    private TriggerQueueServiceLocal triggerQueueServiceMock;
    private Product prod;
    private String queryName;
    private Subscription sub;
    private VOUser user1;
    private VOUser user2;
    private PlatformUser platformUserForUser1;
    private PlatformUser platformUserForUser2;
    private PlatformUser orgAdmin;
    private CommunicationServiceLocal commService;
    private ModifyAndUpgradeSubscriptionBean modifyAndUpgradeBean;
    private ProductDao productDao;
    private UserGroupServiceLocalBean userGroupService;
    private ConfigurationServiceLocal cfgService;

    @SuppressWarnings("boxing")
    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        subscriptionServiceBean = initMocksAndSpys();
        productDao = mock(ProductDao.class);
        commService = mock(CommunicationServiceLocal.class);
        cfgService = mock(ConfigurationServiceLocal.class);
        subscriptionServiceBean.cfgService = cfgService;
        modifyAndUpgradeBean = spy(new ModifyAndUpgradeSubscriptionBean());
        modifyAndUpgradeBean.commService = commService;
        subscriptionServiceBean.modUpgBean = modifyAndUpgradeBean;
        user1 = new VOUser();
        user1.setKey(1);
        platformUserForUser1 = new PlatformUser();
        platformUserForUser1.setUserId("user1");
        platformUserForUser1.setKey(1);
        user2 = new VOUser();
        user2.setKey(2);
        platformUserForUser2 = new PlatformUser();
        platformUserForUser2.setUserId("user2");
        platformUserForUser2.setKey(2);
        service = new VOService();
        compatibleService = new VOService();
        compatibleService.setKey(11);
        subscription = new VOSubscription();
        subscription.setSubscriptionId("subId");
        subscription.setKey(23);

        subscriptions.add(subscription);

        doReturn(productDao).when(subscriptionServiceBean).getProductDao();
        doReturn(false).when(subscriptionServiceBean)
                .isActivationAllowed(any(Subscription.class), eq(true));

        callingOrg = new Organization();
        callingOrg.setKey(15L);
        prod = new Product();
        TechnicalProduct technicalProduct = new TechnicalProduct();
        technicalProduct.setOrganization(new Organization());
        prod.setTechnicalProduct(technicalProduct);
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setPriceModel(new PriceModel());
        prod.setKey(11);
        prod.setType(ServiceType.TEMPLATE);
        prod.setAutoAssignUserEnabled(Boolean.FALSE);
        Product targetProduct = new Product();
        targetProduct.setKey(compatibleService.getKey());
        targetProduct.setTechnicalProduct(technicalProduct);
        targetProduct.setStatus(ServiceStatus.ACTIVE);
        prod.getCompatibleProducts()
                .add(new ProductReference(prod, targetProduct));
        sub = new Subscription();
        sub.setSubscriptionId("subId");
        sub.bindToProduct(prod);
        sub.setOrganization(callingOrg);
        sub.setKey(23);
        sub.setStatus(SubscriptionStatus.ACTIVE);

        prod.setVendor(new Organization());

        VOUsageLicense license = new VOUsageLicense();
        license.setUser(user1);

        usersToAdd.add(license);
        usersToRevoke.add(user2);

        dsMock = subscriptionServiceBean.dataManager;
        triggerQueueServiceMock = subscriptionServiceBean.triggerQS;
        queryMock = mock(Query.class);

        when(dsMock.getReference(PlatformUser.class, user1.getKey()))
                .thenReturn(platformUserForUser1);
        when(dsMock.getReference(PlatformUser.class, user2.getKey()))
                .thenReturn(platformUserForUser2);

        orgAdmin = new PlatformUser();
        orgAdmin.setAssignedRoles(
                newRoleAssignment(orgAdmin, UserRoleType.ORGANIZATION_ADMIN));
        orgAdmin.setOrganization(callingOrg);
        orgAdmin.setAssignedRoles(
                newRoleAssignment(orgAdmin, UserRoleType.ORGANIZATION_ADMIN));
        callingOrg.getPlatformUsers().add(orgAdmin);
        subscriptionServiceBean.manageBean.dataManager = dsMock;
        subscriptionServiceBean.modUpgBean.dataManager = dsMock;

        userGroupService = mock(UserGroupServiceLocalBean.class);
        subscriptionServiceBean.userGroupService = userGroupService;
        prepareInvisibleProducts();

        defineMockBehavior();
        when(dsMock.getCurrentUser()).thenReturn(orgAdmin);
        doReturn(true).when(cfgService).isPaymentInfoAvailable();
    }

    private void prepareInvisibleProducts() throws Exception {
        List<Long> invisibleProductKeys = new ArrayList<>();
        invisibleProductKeys.add(Long.valueOf(1l));
        doReturn(invisibleProductKeys).when(userGroupService)
                .getInvisibleProductKeysForUser(1l);
    }

    private SubscriptionServiceBean initMocksAndSpys() throws Exception {
        SubscriptionServiceBean sb = new SubscriptionServiceBean();
        spyInjected(sb, givenSpyClasses());
        mockEJBs(sb);
        mockResources(sb);
        return spy(sb);
    }

    private List<Class<?>> givenSpyClasses() {
        List<Class<?>> spys = new ArrayList<>();
        spys.add(TerminateSubscriptionBean.class);
        spys.add(ManageSubscriptionBean.class);
        spys.add(ModifyAndUpgradeSubscriptionBean.class);
        spys.add(SubscriptionUtilBean.class);
        return spys;
    }

    @Test
    public void addRevokeUser_NonConflicting() throws Exception {
        initMessageData(TriggerType.ADD_REVOKE_USER, callingOrg);
        subscriptionServiceBean.addRevokeUser("subId", usersToAdd,
                usersToRevoke);
        assertTrue(
                storedTriggerProcess.getTriggerProcessIdentifiers().isEmpty());
    }

    @Test
    public void addRevokeUser_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initMessageData(TriggerType.ADD_REVOKE_USER, callingOrg);
        storedTriggerProcess.setTriggerDefinition(
                createTriggerDefinition(TriggerType.ADD_REVOKE_USER));
        subscriptionServiceBean.addRevokeUser("subId", usersToAdd,
                usersToRevoke);
        List<TriggerProcessIdentifier> ids = storedTriggerProcess
                .getTriggerProcessIdentifiers();
        assertEquals(4, ids.size());
        assertEquals(TriggerProcessIdentifierName.ORGANIZATION_KEY,
                ids.get(0).getName());
        assertEquals(String.valueOf(callingOrg.getKey()),
                ids.get(0).getValue());
        assertEquals(TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                ids.get(1).getName());
        assertEquals("subId", ids.get(1).getValue());
        assertEquals(TriggerProcessIdentifierName.USER_TO_ADD,
                ids.get(2).getName());
        assertEquals("user1", ids.get(2).getValue());
        assertEquals(TriggerProcessIdentifierName.USER_TO_REVOKE,
                ids.get(3).getName());
        assertEquals("user2", ids.get(3).getValue());
        verify(dsMock, times(1)).merge(any());
    }

    @Test
    public void addRevokeUser_Conflicting() throws Exception {
        initMessageData(TriggerType.ADD_REVOKE_USER, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.USER_TO_REVOKE, "user2");
        try {
            subscriptionServiceBean.addRevokeUser("subId", usersToAdd,
                    usersToRevoke);
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            assertEquals("ex.OperationPendingException.ADD_REVOKE_USER",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(2, messageParams.length);
            assertEquals("subId", messageParams[0]);
            assertEquals("user2", messageParams[1]);
        }
    }

    @Test
    public void addRevokeUser_modifySubscriptionConflicting() throws Exception {
        initMessageData(TriggerType.MODIFY_SUBSCRIPTION, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY, "23");
        prepareForModifyAndUpgradeSubscriptionConflict();

        try {
            subscriptionServiceBean.addRevokeUser("subId", usersToAdd,
                    usersToRevoke);
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", String.valueOf(messageParams[0]));
        }
    }

    @Test
    public void addRevokeUser_upgradeSubscriptionConflicting()
            throws Exception {
        initMessageData(TriggerType.UPGRADE_SUBSCRIPTION, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY, "23");
        prepareForModifyAndUpgradeSubscriptionConflict();

        try {
            subscriptionServiceBean.addRevokeUser("subId", usersToAdd,
                    usersToRevoke);
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", String.valueOf(messageParams[0]));
        }
    }

    @Test
    public void addRevokeUser_unSubscribeFormProductConflicting()
            throws Exception {
        initMessageData(TriggerType.UNSUBSCRIBE_FROM_SERVICE, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        prepareForTerminateSubscriptionConflict();

        try {
            subscriptionServiceBean.addRevokeUser("subId", usersToAdd,
                    usersToRevoke);
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", messageParams[0]);
        }
    }

    @Test
    public void subscribeToService_NonConflicting() throws Exception {
        // given
        initMessageData(TriggerType.SUBSCRIBE_TO_SERVICE, callingOrg);
        doReturn(sub).when(subscriptionServiceBean)
                .subscribeToServiceInt(any(TriggerProcess.class));

        prod.setAutoAssignUserEnabled(Boolean.TRUE);

        // when
        subscriptionServiceBean.subscribeToService(subscription, service, null,
                null, null, new ArrayList<VOUda>());

        // then
        verify(subscriptionServiceBean, times(1))
                .addRevokeUserInt(any(TriggerProcess.class));
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void subscribeToService_WithDuplicateSubName() throws Exception {
        // given
        initMessageData(TriggerType.SUBSCRIBE_TO_SERVICE, callingOrg);
        doReturn(sub).when(subscriptionServiceBean)
                .subscribeToServiceInt(any(TriggerProcess.class));
        doThrow(new NonUniqueBusinessKeyException()).when(dsMock)
                .validateBusinessKeyUniqueness(any(Subscription.class));
        // when
        subscriptionServiceBean.subscribeToService(subscription, service, null,
                null, null, new ArrayList<VOUda>());
    }

    @Test
    public void subscribeToService_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initMessageData(TriggerType.SUBSCRIBE_TO_SERVICE, callingOrg);
        storedTriggerProcess.setTriggerDefinition(
                createTriggerDefinition(TriggerType.SUBSCRIBE_TO_SERVICE));
        subscriptionServiceBean.subscribeToService(subscription, service, null,
                null, null, new ArrayList<VOUda>());
        List<TriggerProcessIdentifier> ids = storedTriggerProcess
                .getTriggerProcessIdentifiers();
        assertEquals(2, ids.size());
        assertEquals(TriggerProcessIdentifierName.ORGANIZATION_KEY,
                ids.get(0).getName());
        assertEquals(String.valueOf(callingOrg.getKey()),
                ids.get(0).getValue());
        assertEquals(TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                ids.get(1).getName());
        assertEquals("subId", ids.get(1).getValue());
        verify(dsMock, times(1)).merge(any());
    }

    @Test
    public void subscribeToService_Conflicting() throws Exception {
        initMessageData(TriggerType.SUBSCRIBE_TO_SERVICE, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        try {
            subscriptionServiceBean.subscribeToService(subscription, service,
                    null, null, null, new ArrayList<VOUda>());
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            assertEquals("ex.OperationPendingException.SUBSCRIBE_TO_SERVICE",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", messageParams[0]);
        }
        verify(subscriptionServiceBean, never())
                .addRevokeUserInt(any(TriggerProcess.class));
    }

    @Test
    public void unsubscribeFromService_NonConflicting() throws Exception {
        initMessageData(TriggerType.UNSUBSCRIBE_FROM_SERVICE, callingOrg);
        subscriptionServiceBean
                .unsubscribeFromService(subscription.getSubscriptionId());
        assertTrue(
                storedTriggerProcess.getTriggerProcessIdentifiers().isEmpty());
    }

    @Test
    public void unsubscribeFromService_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initMessageData(TriggerType.UNSUBSCRIBE_FROM_SERVICE, callingOrg);
        storedTriggerProcess.setTriggerDefinition(
                createTriggerDefinition(TriggerType.UNSUBSCRIBE_FROM_SERVICE));
        subscriptionServiceBean
                .unsubscribeFromService(subscription.getSubscriptionId());
        List<TriggerProcessIdentifier> ids = storedTriggerProcess
                .getTriggerProcessIdentifiers();
        assertEquals(2, ids.size());
        assertEquals(TriggerProcessIdentifierName.ORGANIZATION_KEY,
                ids.get(0).getName());
        assertEquals(String.valueOf(callingOrg.getKey()),
                ids.get(0).getValue());
        assertEquals(TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                ids.get(1).getName());
        assertEquals("subId", ids.get(1).getValue());
        verify(dsMock, times(1)).merge(any());
    }

    @Test
    public void unsubscribeFromService_Conflicting() throws Exception {
        initMessageData(TriggerType.UNSUBSCRIBE_FROM_SERVICE, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_ID, "subId");
        try {
            subscriptionServiceBean
                    .unsubscribeFromService(subscription.getSubscriptionId());
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            assertEquals(
                    "ex.OperationPendingException.UNSUBSCRIBE_FROM_SERVICE",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", messageParams[0]);
        }
    }

    @Test
    public void modifySubscription_NonConflicting() throws Exception {
        initMessageData(TriggerType.MODIFY_SUBSCRIPTION, callingOrg);
        subscriptionServiceBean.modifySubscription(subscription, null,
                new ArrayList<VOUda>());
        assertTrue(
                storedTriggerProcess.getTriggerProcessIdentifiers().isEmpty());
    }

    @Test
    public void modifySubscription_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initMessageData(TriggerType.MODIFY_SUBSCRIPTION, callingOrg);
        storedTriggerProcess.setTriggerDefinition(
                createTriggerDefinition(TriggerType.MODIFY_SUBSCRIPTION));
        subscriptionServiceBean.modifySubscription(subscription, null,
                new ArrayList<VOUda>());
        List<TriggerProcessIdentifier> ids = storedTriggerProcess
                .getTriggerProcessIdentifiers();
        assertEquals(2, ids.size());
        assertEquals(TriggerProcessIdentifierName.ORGANIZATION_KEY,
                ids.get(0).getName());
        assertEquals(String.valueOf(callingOrg.getKey()),
                ids.get(0).getValue());
        assertEquals(TriggerProcessIdentifierName.SUBSCRIPTION_KEY,
                ids.get(1).getName());
        assertEquals("23", ids.get(1).getValue());
        verify(dsMock, times(1)).merge(any());
    }

    @Test
    public void modifySubscription_Conflicting() throws Exception {
        initMessageData(TriggerType.MODIFY_SUBSCRIPTION, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY, "23");
        try {
            subscriptionServiceBean.modifySubscription(subscription, null,
                    new ArrayList<VOUda>());
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            assertEquals("ex.OperationPendingException.MODIFY_SUBSCRIPTION",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", messageParams[0]);
        }
    }

    @Test
    public void upgradeSubscription_NonConflicting() throws Exception {
        initMessageData(TriggerType.UPGRADE_SUBSCRIPTION, callingOrg);
        subscriptionServiceBean.upgradeSubscription(subscription, service, null,
                null, new ArrayList<VOUda>());
        assertTrue(
                storedTriggerProcess.getTriggerProcessIdentifiers().isEmpty());
    }

    @Test
    public void upgradeSubscription_NonConflictingValidateIdentifierGeneration()
            throws Exception {
        initMessageData(TriggerType.UPGRADE_SUBSCRIPTION, callingOrg);
        storedTriggerProcess.setTriggerDefinition(
                createTriggerDefinition(TriggerType.UPGRADE_SUBSCRIPTION));
        subscriptionServiceBean.upgradeSubscription(subscription, service, null,
                null, new ArrayList<VOUda>());
        List<TriggerProcessIdentifier> ids = storedTriggerProcess
                .getTriggerProcessIdentifiers();
        assertEquals(2, ids.size());
        assertEquals(TriggerProcessIdentifierName.ORGANIZATION_KEY,
                ids.get(0).getName());
        assertEquals(String.valueOf(callingOrg.getKey()),
                ids.get(0).getValue());
        assertEquals(TriggerProcessIdentifierName.SUBSCRIPTION_KEY,
                ids.get(1).getName());
        assertEquals("23", ids.get(1).getValue());
        verify(dsMock, times(1)).merge(any());
    }

    @Test
    public void upgradeSubscription_Conflicting() throws Exception {
        initMessageData(TriggerType.UPGRADE_SUBSCRIPTION, callingOrg);
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(callingOrg));
        storedTriggerProcess.addTriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY, "23");
        try {
            subscriptionServiceBean.upgradeSubscription(subscription, service,
                    null, null, new ArrayList<VOUda>());
            fail("call must cause an exception");
        } catch (OperationPendingException e) {
            assertEquals("ex.OperationPendingException.UPGRADE_SUBSCRIPTION",
                    e.getMessageKey());
            String[] messageParams = e.getMessageParams();
            assertEquals(1, messageParams.length);
            assertEquals("subId", messageParams[0]);
        }
    }

    /**
     * Initializes a trigger message for the specified parameters.
     * 
     * @param triggerType
     *            The trigger type to set.
     * @param org
     *            The organization to retrieve the trigger data
     */
    private void initMessageData(TriggerType triggerType, Organization org) {
        storedTriggerProcess = new TriggerProcess();
        TriggerMessage messageData = new TriggerMessage(triggerType, null,
                Collections.singletonList(org));
        triggerMessageData.add(new TriggerProcessMessageData(
                storedTriggerProcess, messageData));
    }

    /**
     * Creates a new instance of a suspending trigger definition with the
     * specified type.
     * 
     * @param type
     *            The trigger type to set.
     * @return The created trigger definition.
     */
    private TriggerDefinition createTriggerDefinition(TriggerType type) {
        TriggerDefinition td = new TriggerDefinition();
        td.setType(type);
        td.setSuspendProcess(true);
        return td;
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
                }
                return storedTriggerProcess.getTriggerProcessIdentifiers();
            }
        }).when(queryMock).getResultList();

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if ("TriggerProcessIdentifier.isSubscribeOrUnsubscribeServicePending"
                        .equals(queryName)
                        || "TriggerProcessIdentifier.isModifyOrUpgradeSubscriptionPending"
                                .equals(queryName)) {
                    return Long.valueOf(storedTriggerProcess
                            .getTriggerProcessIdentifiers().size());
                }
                if ("Subscription.hasSubscriptionsBasedOnOnBehalfServicesForTp"
                        .equals(queryName)) {
                    return Long.valueOf(1);
                }
                return Long.valueOf(0);
            }
        }).when(queryMock).getSingleResult();

        doReturn(prod).when(dsMock).getReference(eq(Product.class), anyLong());
        doReturn(sub).when(dsMock).getReference(eq(Subscription.class),
                anyLong());

        doAnswer(new Answer<PlatformUser>() {
            @Override
            public PlatformUser answer(InvocationOnMock invocation)
                    throws Throwable {
                PlatformUser user = new PlatformUser();
                user.setOrganization(callingOrg);
                return user;
            }
        }).when(dsMock).getCurrentUser();

        // getReferenceByBusinessKey for type subscription must return a
        // subscription
        doAnswer(new Answer<Subscription>() {
            @Override
            public Subscription answer(InvocationOnMock invocation)
                    throws Throwable {
                return sub;
            }
        }).when(dsMock).getReferenceByBusinessKey(any(Subscription.class));

        doAnswer(new Answer<List<TriggerProcessMessageData>>() {
            @Override
            public List<TriggerProcessMessageData> answer(
                    InvocationOnMock invocation) throws Throwable {
                return triggerMessageData;
            }
        }).when(triggerQueueServiceMock)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

        // ignore internal trigger related methods
        doNothing().when(subscriptionServiceBean)
                .addRevokeUserInt(any(TriggerProcess.class));
        doReturn(null).when(subscriptionServiceBean)
                .subscribeToServiceInt(any(TriggerProcess.class));
    }

    private Set<RoleAssignment> newRoleAssignment(PlatformUser user,
            UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }

    private void prepareForModifyAndUpgradeSubscriptionConflict()
            throws Exception {
        doAnswer(new Answer<List<?>>() {
            @Override
            public List<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                return new ArrayList<TriggerProcessIdentifier>();
            }
        }).when(queryMock).getResultList();

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if ("TriggerProcessIdentifier.isModifyOrUpgradeSubscriptionPending"
                        .equals(queryName)) {
                    return Long.valueOf(storedTriggerProcess
                            .getTriggerProcessIdentifiers().size());
                }

                return Long.valueOf(0);
            }
        }).when(queryMock).getSingleResult();
    }

    private void prepareForTerminateSubscriptionConflict() throws Exception {
        doAnswer(new Answer<List<?>>() {
            @Override
            public List<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                return new ArrayList<TriggerProcessIdentifier>();
            }
        }).when(queryMock).getResultList();

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if ("TriggerProcessIdentifier.isSubscribeOrUnsubscribeServicePending"
                        .equals(queryName)) {
                    return Long.valueOf(storedTriggerProcess
                            .getTriggerProcessIdentifiers().size());
                }

                return Long.valueOf(0);
            }
        }).when(queryMock).getSingleResult();

    }
}
