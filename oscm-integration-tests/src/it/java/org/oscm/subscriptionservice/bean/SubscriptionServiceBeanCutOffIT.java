/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                  
 *                                                                              
 *  Creation Date: 04.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;

public class SubscriptionServiceBeanCutOffIT extends EJBTestBase {
    DataService mgr;
    SubscriptionService subMgmt;
    LocalizerServiceLocal localizer;

    boolean isTriggerQueueService_sendSuspendingMessageCalled = false;
    boolean isTriggerQueueService_sendAllNonSuspendingMessageCalled = false;
    List<TriggerType> usedTriggersTypes = new LinkedList<>();
    TriggerDefinition td;

    // supplier
    Organization supplier;
    String supplierId = "supplierId";
    PlatformUser supplierAdmin;

    // technology provider
    Organization techProv;
    String techProvId = "techProvId";

    // reseller
    Organization reseller;
    String resellerId = "resellerId";
    PlatformUser resellerAdmin;

    // broker
    Organization broker;
    String brokerId = "brokerId";
    PlatformUser brokerAdmin;

    // technical products
    TechnicalProduct technicalProductAsync;
    TechnicalProduct technicalProductSync;

    // products
    Product productSync;
    Product productAsync;
    Product productBrokerCopy;
    Product productResellerCopy;

    private void addBeansToContainer(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new ModifyAndUpgradeSubscriptionBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());

        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new ApplicationServiceStub());
        container.addBean(new IdentityServiceStub() {

            @Override
            public PlatformUser getPlatformUser(String userId,
                    boolean validateOrganization) {
                try {
                    supplierAdmin = (PlatformUser) mgr
                            .getReferenceByBusinessKey(supplierAdmin);
                } catch (ObjectNotFoundException e) {
                    // do nothing if user does not exist in DB.
                }
                return supplierAdmin;
            }
        });

        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                for (TriggerMessage msg : messageData) {
                    usedTriggersTypes.add(msg.getTriggerType());
                }
                isTriggerQueueService_sendSuspendingMessageCalled = true;
                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(td);
                tp.setUser(resellerAdmin);
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }
        });
    }

    private void createOrganizations() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                techProv = Organizations.createOrganization(mgr, techProvId,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.CUSTOMER);

                supplier = Organizations.createOrganization(mgr, supplierId,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.CUSTOMER);

                reseller = Organizations.createOrganization(mgr, resellerId,
                        OrganizationRoleType.RESELLER,
                        OrganizationRoleType.CUSTOMER);

                broker = Organizations.createOrganization(mgr, brokerId,
                        OrganizationRoleType.BROKER,
                        OrganizationRoleType.CUSTOMER);

                return null;
            }
        });
    }

    private void createUsers() throws NonUniqueBusinessKeyException {
        supplierAdmin = Organizations.createUserForOrg(mgr, supplier, true,
                "supplierAdminId");

        brokerAdmin = Organizations.createUserForOrg(mgr, broker, true,
                "brokerAdminId");

        resellerAdmin = Organizations.createUserForOrg(mgr, reseller, true,
                "resellerAdminId");
    }

    private void createTechnicalProducts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                technicalProductAsync = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier,
                                "technicalProductAsyncId", /* asyncProvisioning */
                                true, ServiceAccessType.LOGIN);

                technicalProductSync = TechnicalProducts.createTechnicalProduct(
                        mgr, supplier,
                        "technicalProductSyncId", /* asyncProvisioning */
                        false, ServiceAccessType.LOGIN);
                return null;
            }
        });
    }

    private void createProducts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                productSync = Products.createProduct(supplier,
                        technicalProductSync, /* chargeable */false,
                        "productSyncId", "productSyncPriceModelId", mgr);
                productSync = Products.setStatusForProduct(mgr, productSync,
                        ServiceStatus.ACTIVE);

                productAsync = Products.createProduct(supplier,
                        technicalProductAsync, /* chargeable */false,
                        "productAsyncId", "productAsyncPriceModelId", mgr);
                productAsync = Products.setStatusForProduct(mgr, productAsync,
                        ServiceStatus.ACTIVE);

                productBrokerCopy = Products
                        .createProductResaleCopy(productSync, broker, mgr);
                productBrokerCopy = Products.setStatusForProduct(mgr,
                        productBrokerCopy, ServiceStatus.ACTIVE);

                productResellerCopy = Products
                        .createProductResaleCopy(productSync, reseller, mgr);
                productResellerCopy = Products.setStatusForProduct(mgr,
                        productResellerCopy, ServiceStatus.ACTIVE);
                return null;
            }
        });
    }

    private Organization setCutOffDayForOrganization(final long orgKey,
            final int cutOffDay) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = mgr.find(Organization.class,
                        orgKey);
                organization.setCutOffDay(cutOffDay);
                mgr.persist(organization);
                return organization;
            }
        });
    }

    private int getCutOffDayFromSubscription(final long subscriptionKey)
            throws Exception {
        Integer result = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        subscriptionKey);
                return Integer.valueOf(subscription.getCutOffDay());
            }
        });
        return result.intValue();
    }

    private VOService getProductAndAssemble(final long productKey)
            throws Exception {
        return runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                Product product = mgr.getReference(Product.class, productKey);
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    private List<VOUsageLicense> createVOUsageLicenses(PlatformUser user) {
        VOUser admin = UserDataAssembler.toVOUser(user);
        VOUsageLicense voUsageLicense = new VOUsageLicense();
        voUsageLicense.setUser(admin);
        List<VOUsageLicense> users = new ArrayList<>();
        users = Arrays.asList(voUsageLicense);
        return users;
    }

    private VOInstanceInfo createVOInstanceInfo() {
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        instanceInfo.setAccessInfo(null);
        return instanceInfo;
    }

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        addBeansToContainer(container);

        mgr = container.get(DataService.class);
        subMgmt = container.get(SubscriptionService.class);
        localizer = container.get(LocalizerServiceLocal.class);

        createOrganizations();
        createUsers();

        createTechnicalProducts();
        createProducts();
    }

    @Test
    public void subscribeToService_setCutOffDaySync() throws Exception {
        // given
        int cutOffDay = 5;
        setCutOffDayForOrganization(supplier.getKey(), cutOffDay);

        // when
        container.login(String.valueOf(supplierAdmin.getKey()),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscription voSubscription = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("subscriptionSyncId"),
                getProductAndAssemble(productSync.getKey()),
                createVOUsageLicenses(supplierAdmin), null, null,
                new ArrayList<VOUda>());

        // then
        assertEquals(cutOffDay,
                getCutOffDayFromSubscription(voSubscription.getKey()));
    }

    @Test
    public void completeAsyncSubscription_setCutOffDayAsync() throws Exception {
        // given
        int cutOffDay = 6;
        String subscriptionAsyncId = "subscriptionAsyncId";
        setCutOffDayForOrganization(supplier.getKey(), cutOffDay);

        container.login(String.valueOf(supplierAdmin.getKey()),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscription voSubscription = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionAsyncId),
                getProductAndAssemble(productAsync.getKey()),
                createVOUsageLicenses(supplierAdmin), null, null,
                new ArrayList<VOUda>());

        // when
        container.login(String.valueOf(supplierAdmin.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.completeAsyncSubscription(subscriptionAsyncId, supplierId,
                createVOInstanceInfo());

        // then
        assertEquals(cutOffDay,
                getCutOffDayFromSubscription(voSubscription.getKey()));
    }

    @Test
    public void subscribeToService_setCutOffDayBroker() throws Exception {
        // given
        int cutOffDay = 7;
        setCutOffDayForOrganization(supplier.getKey(), cutOffDay);

        // when
        container.login(String.valueOf(brokerAdmin.getKey()),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscription voSubscription = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("subscriptionBrokerId"),
                getProductAndAssemble(productBrokerCopy.getKey()),
                createVOUsageLicenses(brokerAdmin), null, null,
                new ArrayList<VOUda>());

        // then
        assertEquals(cutOffDay,
                getCutOffDayFromSubscription(voSubscription.getKey()));
    }

    @Test
    public void subscribeToService_setCutOffDayReseller() throws Exception {
        // given
        int cutOffDay = 8;
        setCutOffDayForOrganization(reseller.getKey(), cutOffDay);

        // when
        container.login(String.valueOf(resellerAdmin.getKey()),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscription voSubscription = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("subscriptionResellerId"),
                getProductAndAssemble(productResellerCopy.getKey()),
                createVOUsageLicenses(resellerAdmin), null, null,
                new ArrayList<VOUda>());

        // then
        assertEquals(cutOffDay,
                getCutOffDayFromSubscription(voSubscription.getKey()));
    }
}
