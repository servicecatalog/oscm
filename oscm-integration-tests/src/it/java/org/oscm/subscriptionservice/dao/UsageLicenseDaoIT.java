/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-5                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceStub2;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUser;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;

/**
 * Unit tests for {@link UsageLicenseDao} using the test EJB container.
 * 
 * @author Mao
 */
public class UsageLicenseDaoIT extends EJBTestBase {

    protected SubscriptionService subMgmt;
    protected LocalizerServiceLocal localizer;
    private Product testProduct = new Product();
    private Organization testOrganization = new Organization();
    private final Map<Organization, ArrayList<PlatformUser>> testUsers = new HashMap<>();
    private TriggerDefinition td;
    private Organization tpAndSupplier;
    private String customerUserKey;
    private org.oscm.domobjects.Marketplace mp;

    private DataService ds;
    private UsageLicenseDao dao;
    Set<SubscriptionStatus> states = Collections
            .unmodifiableSet(EnumSet.of(SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED));

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        final Organization org = new Organization();
        org.setKey(0);
        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", org);
            }
        };
        container.addBean(new ConfigurationServiceStub());
        container.addBean(ds);
        dao = new UsageLicenseDao(ds);
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new SessionManagementStub2());
        container.addBean(new IdManagementStub());
        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new LocalizerServiceStub2());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TaskQueueServiceStub() {
            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
            }
        });

        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(td);
                tp.setUser(testUsers.get(testOrganization).get(0));
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

        });

        container.addBean(new AccountServiceStub());

        ds = container.get(DataService.class);
        Long userKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return initMasterData();
            }
        });
        customerUserKey = String.valueOf(userKey);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        subMgmt = container.get(SubscriptionService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        testProduct.getTechnicalProduct()
                .setAccessType(ServiceAccessType.LOGIN);
    }

    @Test
    public void getSubscriptionAssignments() throws Exception {
        // when
        List<UsageLicense> result = runTX(new Callable<List<UsageLicense>>() {
            @Override
            public List<UsageLicense> call() throws Exception {
                return dao.getSubscriptionAssignments(ds.getCurrentUser(),
                        states);
            }
        });

        // then
        assertEquals(0, result.size());
    }

    @Test
    public void getUserforSubscription() throws Exception {
        // given
        final String subscriptionId = "testSubscribeToProduct";
        createVOSubscription(subscriptionId);

        // when
        List<UsageLicense> result = runTX(new Callable<List<UsageLicense>>() {
            @Override
            public List<UsageLicense> call() throws Exception {
                return dao.getUsersforSubscription(
                        getSubscription(subscriptionId));
            }
        });

        // then
        assertEquals(3, result.size());
    }

    private void createVOSubscription(final String subscriptionId)
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VOService product = getProductToSubscribe(testProduct.getKey());
                VOUser[] users = new VOUser[2];
                VOUser[] admins = new VOUser[1];
                admins[0] = UserDataAssembler
                        .toVOUser(testUsers.get(testOrganization).get(0));
                users[0] = UserDataAssembler
                        .toVOUser(testUsers.get(testOrganization).get(1));
                users[1] = UserDataAssembler
                        .toVOUser(testUsers.get(testOrganization).get(2));
                subMgmt.subscribeToService(
                        Subscriptions.createVOSubscription(subscriptionId),
                        product, getUsersToAdd(admins, users), null, null,
                        new ArrayList<VOUda>());
                return null;
            }
        });
    }

    private Subscription getSubscription(final String subscriptionId)
            throws Exception {

        final Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Subscription qryObj = new Subscription();
                qryObj.setOrganizationKey(testOrganization.getKey());
                qryObj.setSubscriptionId(subscriptionId);
                Subscription subscription = (Subscription) ds.find(qryObj);
                return subscription;
            }
        });

        return sub;
    }

    private PlatformUser givenUserAdmin(long key, String id, Organization org) {
        return givenUser(key, id, org, UserRoleType.ORGANIZATION_ADMIN);
    }

    private PlatformUser givenUser(long key, String id, Organization org,
            UserRoleType roleType) {
        PlatformUser user = new PlatformUser();
        user.setKey(key);
        user.setUserId(id);
        user.setOrganization(org);
        RoleAssignment roleAssign = new RoleAssignment();
        roleAssign.setUser(user);
        roleAssign.setRole(new UserRole(roleType));
        user.getAssignedRoles().add(roleAssign);
        return user;
    }

    private Long initMasterData()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        tpAndSupplier = Organizations.createOrganization(ds,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        mp = Marketplaces.ensureMarketplace(tpAndSupplier,
                tpAndSupplier.getOrganizationId(), ds);
        Marketplaces.grantPublishing(tpAndSupplier, mp, ds, false);
        createProduct();
        createTestOrganization();
        Long initialCustomerAdminKey = addUsersforTestOrganization();
        return initialCustomerAdminKey;
    }

    private void createProduct() throws NonUniqueBusinessKeyException {
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(ds,
                tpAndSupplier, "TP_ID", false, ServiceAccessType.LOGIN);
        testProduct = Products.createProduct(tpAndSupplier, tProd, false,
                "Product", null, mp, ds);

    }

    private void createTestOrganization()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        testOrganization = Organizations.createOrganization(ds,
                OrganizationRoleType.CUSTOMER);
        OrganizationReference ref = new OrganizationReference(tpAndSupplier,
                testOrganization,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        ds.persist(ref);

    }

    private Long addUsersforTestOrganization()
            throws NonUniqueBusinessKeyException {

        ArrayList<PlatformUser> userlist = new ArrayList<>();
        testUsers.put(testOrganization, userlist);
        PlatformUser admin = Organizations.createUserForOrg(ds,
                testOrganization, true, "admin");
        Long initialCustomerAdminKey = Long.valueOf(admin.getKey());
        userlist.add(admin);
        for (int j = 1; j <= 2; j++) {
            PlatformUser user = Organizations.createUserForOrg(ds,
                    testOrganization, false, "user" + j);
            userlist.add((PlatformUser) ReflectiveClone.clone(user));
        }
        return initialCustomerAdminKey;
    }

    private VOService getProductToSubscribe(final long key) throws Exception {
        return runTX(new Callable<VOService>() {

            @Override
            public VOService call() throws Exception {
                Product product = ds.getReference(Product.class, key);
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }
}
