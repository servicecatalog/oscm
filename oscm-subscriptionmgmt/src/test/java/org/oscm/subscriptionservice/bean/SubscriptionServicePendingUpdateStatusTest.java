/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * @author Qiu
 * 
 */
public class SubscriptionServicePendingUpdateStatusTest
        extends SubscriptionServiceMockBase {

    private SubscriptionServiceBean bean;

    private TriggerProcess tp;
    private Subscription sub;
    private Product product;
    private VOSubscription voSubscription;
    private List<VOParameter> voParameters;
    private List<VOUda> voUdas;
    private final List<VOUsageLicense> usersToBeAdded = new ArrayList<>();
    private final List<VOUser> usersToBeRevoked = new ArrayList<>();

    private static final long SUBSCRIPTION_KEY = 1000L;
    private static final String SUBSCRIPTION_ID = "subId";
    private DataService ds;
    private ConfigurationServiceLocal cfgService;

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        bean = createMocksAndSpys();

        cfgService = mock(ConfigurationServiceLocal.class);
        bean.cfgService = cfgService;

        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        user.setOrganization(org);
        user.setLocale("en");

        product = givenProduct(new ParameterSet(), ServiceStatus.ACTIVE);
        sub = givenSubscription(user, SUBSCRIPTION_ID);
        voSubscription = givenVOSubscription(SUBSCRIPTION_ID);
        voParameters = givenVOParameters();
        voUdas = new ArrayList<>();
        tp = givenTriggerProcess();

        ds = mock(DataService.class);
        bean.dataManager = ds;
        bean.modUpgBean.dataManager = ds;
        bean.manageBean.dataManager = ds;
        bean.terminateBean.dataManager = ds;
        bean.terminateBean.commService = mock(CommunicationServiceLocal.class);
        Query query = mock(Query.class);
        doReturn(query).when(ds).createNamedQuery(anyString());
        doReturn("rter").when(bean.terminateBean.localizer)
                .getLocalizedTextFromBundle(
                        eq(LocalizedObjectTypes.MAIL_CONTENT),
                        (Marketplace) any(), eq(user.getLocale()),
                        eq("SUBSCRIPTION_TERMINATED_BY_SUPPLIER_REASON"));

        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        when(bean.dataManager.getReference(eq(Subscription.class), anyLong()))
                .thenReturn(sub);
        doReturn(sub).when(bean.dataManager)
                .getReferenceByBusinessKey(any(Subscription.class));
        when(bean.prodSessionMgmt
                .getProductSessionsForSubscriptionTKey(anyLong()))
                        .thenReturn(new ArrayList<Session>());
        doReturn(true).when(cfgService).isPaymentInfoAvailable();
    }

    private SubscriptionServiceBean createMocksAndSpys() throws Exception {
        bean = new SubscriptionServiceBean();
        spyInjected(bean, givenSpyClasses());
        mockEJBs(bean);
        mockResources(bean);
        copyMocks(bean, givenMocks());
        return spy(bean);
    }

    private List<Class<?>> givenSpyClasses() {
        return new ArrayList<>();
    }

    private List<Object> givenMocks() {
        List<Object> mocks = new ArrayList<>();
        mocks.add(bean.dataManager);
        mocks.add(bean.appManager);
        return mocks;
    }

    @Test
    public void modifySubscriptionInt_PendingUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.PENDING_UPD);
        // when
        try {
            bean.modifySubscriptionInt(tp);
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.PENDING_UPD);
        }
    }

    @Test
    public void modifySubscriptionInt_SuspendedUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.SUSPENDED_UPD);
        // when
        try {
            bean.modifySubscriptionInt(tp);
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.SUSPENDED_UPD);
        }
    }

    @Test
    public void modifySubscriptionInt_Expired() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.EXPIRED);
        // when
        try {
            bean.modifySubscriptionInt(tp);
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.EXPIRED);
        }
    }

    @Test
    public void terminateSubscription_PendingUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.PENDING_UPD);
        // when
        bean.terminateSubscription(voSubscription, "Terminate");
        // then
        assertEquals(ServiceStatus.DELETED, product.getStatus());
        assertEquals(SubscriptionStatus.DEACTIVATED, sub.getStatus());
        verify(bean.appManager, times(1)).deleteInstance(eq(sub));
    }

    @Test
    public void terminateSubscription_SuspendedUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.SUSPENDED_UPD);
        // when
        bean.terminateSubscription(voSubscription, "Terminate");
        // then
        assertEquals(ServiceStatus.DELETED, product.getStatus());
        assertEquals(SubscriptionStatus.DEACTIVATED, sub.getStatus());
        verify(bean.appManager, times(1)).deleteInstance(eq(sub));
    }

    @Test
    public void addRevokeUser_PendingUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.PENDING_UPD);
        mockTriggerMessageForAddRevokeUser();

        // when
        try {
            bean.addRevokeUser(SUBSCRIPTION_ID, usersToBeAdded,
                    usersToBeRevoked);
            fail();
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.PENDING_UPD);
        }

    }

    @Test
    public void addRevokeUser_Pending() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.PENDING);
        mockTriggerMessageForAddRevokeUser();

        // when
        bean.addRevokeUser(SUBSCRIPTION_ID, usersToBeAdded, usersToBeRevoked);
    }

    @Test
    public void addRevokeUser_SuspendedUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.SUSPENDED_UPD);
        mockTriggerMessageForAddRevokeUser();
        // when
        try {
            bean.addRevokeUser(SUBSCRIPTION_ID, usersToBeAdded,
                    usersToBeRevoked);
            fail();
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.SUSPENDED_UPD);
        }
    }

    @Test
    public void addRevokeUser_Suspended() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.SUSPENDED);
        mockTriggerMessageForAddRevokeUser();

        // when
        try {
            bean.addRevokeUser(SUBSCRIPTION_ID, usersToBeAdded,
                    usersToBeRevoked);
            fail();
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.SUSPENDED);
        }
    }

    @Test
    public void unsubscribeFromService_INVALID() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.INVALID);

        // when
        try {
            bean.unsubscribeFromService(sub.getSubscriptionId());
            fail();
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.INVALID);
        }
    }

    @Test
    public void upgradeSubscription_PendingUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.PENDING_UPD);
        // when
        try {
            bean.upgradeSubscription(voSubscription, new VOService(),
                    new VOPaymentInfo(), new VOBillingContact(),
                    new ArrayList<VOUda>());
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.PENDING_UPD);
        }

    }

    @Test
    public void upgradeSubscription_SuspendedUpd() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.SUSPENDED_UPD);

        // when
        try {
            bean.upgradeSubscription(voSubscription, new VOService(),
                    new VOPaymentInfo(), new VOBillingContact(),
                    new ArrayList<VOUda>());
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.SUSPENDED_UPD);
        }
    }

    @Test
    public void executeServiceOperation_Pending_B10754() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.PENDING);
        VOTechnicalServiceOperation techOp = mock(
                VOTechnicalServiceOperation.class);
        // when
        try {
            bean.executeServiceOperation(voSubscription, techOp);
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.PENDING);
        }
    }

    @Test
    public void executeServiceOperation_Invalid_B10754() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.INVALID);
        VOTechnicalServiceOperation techOp = mock(
                VOTechnicalServiceOperation.class);
        // when
        try {
            bean.executeServiceOperation(voSubscription, techOp);
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.INVALID);
        }
    }

    @Test
    public void executeServiceOperation_Deactive_B10754() throws Exception {
        // given
        sub.setStatus(SubscriptionStatus.DEACTIVATED);
        VOTechnicalServiceOperation techOp = mock(
                VOTechnicalServiceOperation.class);
        // when
        try {
            bean.executeServiceOperation(voSubscription, techOp);
            fail("call must cause an exception");
        } catch (SubscriptionStateException e) {
            // then
            assertInvalidStateException(e, SubscriptionStatus.DEACTIVATED);
        }
    }

    private List<VOParameter> givenVOParameters() {
        List<VOParameter> voParameters = new ArrayList<>();
        VOParameter voParameter = new VOParameter();
        VOParameterDefinition vodefinition = new VOParameterDefinition();
        vodefinition.setParameterId("PARAMETER_ID");
        voParameter.setParameterDefinition(vodefinition);
        voParameter.setValue("VALUE");
        voParameters.add(voParameter);
        return voParameters;
    }

    private TriggerProcess givenTriggerProcess() {
        TriggerProcess triggerProcess = new TriggerProcess();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, voSubscription);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PARAMETERS, voParameters);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.UDAS, voUdas);
        return triggerProcess;
    }

    private Product givenProduct(ParameterSet paraSet, ServiceStatus status) {
        Product product = new Product();
        product.setParameterSet(paraSet);
        product.setStatus(status);
        product.setVendor(new Organization());
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setProvisioningType(ProvisioningType.ASYNCHRONOUS);
        product.setTechnicalProduct(techProduct);
        return product;
    }

    private Subscription givenSubscription(PlatformUser user,
            String subscriptionId) {
        Subscription subscription = new Subscription();
        subscription.setKey(SUBSCRIPTION_KEY);
        subscription.setSubscriptionId(subscriptionId);
        subscription.setOwner(user);
        subscription.setOrganization(user.getOrganization());
        subscription.setProduct(product);
        return subscription;
    }

    private VOSubscription givenVOSubscription(String subscriptionId) {
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setKey(SUBSCRIPTION_KEY);
        voSubscription.setSubscriptionId(subscriptionId);
        return voSubscription;
    }

    private void mockTriggerMessageForAddRevokeUser() throws Exception {
        TriggerMessage message = new TriggerMessage(
                TriggerType.ADD_REVOKE_USER);
        List<TriggerProcessMessageData> list = new ArrayList<>();
        TriggerProcess proc = new TriggerProcess();
        TriggerProcessMessageData ProcMessage = new TriggerProcessMessageData(
                proc, message);
        list.add(ProcMessage);
        doReturn(list).when(bean.triggerQS)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

        doNothing().when(bean).validateTriggerProcessForSubscription(sub);
    }

    private void assertInvalidStateException(SubscriptionStateException e,
            SubscriptionStatus s) {
        assertEquals("ex.SubscriptionStateException.SUBSCRIPTION_INVALID_STATE",
                e.getMessageKey());
        assertEquals("enum.SubscriptionStatus." + s.name(),
                e.getMessageParams()[0]);
    }
}
