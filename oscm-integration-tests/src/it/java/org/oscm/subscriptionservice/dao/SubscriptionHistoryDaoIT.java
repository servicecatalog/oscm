/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
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
import org.oscm.i18nservice.bean.LocalizerServiceStub2;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUser;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.subscriptionservice.bean.ManageSubscriptionBean;
import org.oscm.subscriptionservice.bean.ModifyAndUpgradeSubscriptionBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.enumtypes.ProvisioningType;

public class SubscriptionHistoryDaoIT extends EJBTestBase {
    protected DataService mgr;
    protected ApplicationServiceStub appMgmtStub;
    protected SubscriptionService subMgmt;
    protected SubscriptionServiceLocal subMgmtLocal;
    protected IdentityService idMgmt;
    protected ServiceProvisioningService servProv;
    protected LocalizerServiceLocal localizer;

    private Product asyncTestProduct = new Product();
    private PlatformUser supplierUser;
    private TriggerDefinition td;
    private Organization tpAndSupplier;
    private String customerUserKey;
    private SubscriptionHistoryDao dao;

    private org.oscm.domobjects.Marketplace mp;

    @Override
    public void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(appMgmtStub = new ApplicationServiceStub());
        container.addBean(new SessionManagementStub2());
        container.addBean(new IdManagementStub());
        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new LocalizerServiceStub2());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new ManageSubscriptionBean());
        container.addBean(new ModifyAndUpgradeSubscriptionBean());
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
                tp.setUser(supplierUser);
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

        });

        mgr = container.get(DataService.class);
        dao = new SubscriptionHistoryDao(mgr);

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

    }

    private Long initMasterData()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        tpAndSupplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        mp = Marketplaces.ensureMarketplace(tpAndSupplier,
                tpAndSupplier.getOrganizationId(), mgr);
        Marketplaces.grantPublishing(tpAndSupplier, mp, mgr, false);

        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                tpAndSupplier, "TP_ID_ASYNC", true, ServiceAccessType.LOGIN);

        asyncTestProduct = Products.createProduct(tpAndSupplier, tProd, false,
                getProductId(tProd), null, mp, mgr);

        supplierUser = Organizations.createUserForOrg(mgr, tpAndSupplier, true,
                "admin");
        return Long.valueOf(supplierUser.getKey());
    }

    private String getProductId(TechnicalProduct tProd) {
        String result = "Product";
        if (tProd.getProvisioningType() == ProvisioningType.ASYNCHRONOUS) {
            result += tProd.getProvisioningType().name();
        }
        return result;
    }

    private void subscribeAsync(final String id) throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(asyncTestProduct.getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler.toVOUser(supplierUser);
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(id),
                product, getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
    }

    private VOService getProductToSubscribe(final long key) throws Exception {
        return runTX(new Callable<VOService>() {

            @Override
            public VOService call() throws Exception {
                Product product = mgr.getReference(Product.class, key);
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    @Test
    public void getAccessInfos() throws Throwable {

        // given
        final String id = "asyncSubscriptionId";
        subscribeAsync(id);
        String orgId = tpAndSupplier.getOrganizationId();
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        final VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        instanceInfo.setAccessInfo(
                "Public DNS for EC2 instance: ec2-66-66-66-66.compute-1.amazonaws.com");

        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);

        // when
        List<String> result = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                Subscription qryObj = new Subscription();
                qryObj.setOrganizationKey(tpAndSupplier.getKey());
                qryObj.setSubscriptionId(id);
                Subscription subscription = (Subscription) mgr.find(qryObj);
                return dao.getAccessInfos(subscription, instanceInfo);

            }
        });

        // then
        assertEquals(1, result.size());
    }
}
