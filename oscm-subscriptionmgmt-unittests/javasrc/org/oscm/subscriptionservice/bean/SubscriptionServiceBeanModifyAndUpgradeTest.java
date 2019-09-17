/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 9, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.oscm.accountservice.dataaccess.UdaAccess;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.domobjects.enums.ModifiedEntityType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.*;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.dao.ModifiedEntityDao;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.internal.types.exception.IllegalArgumentException;


/**
 * @author Zhou
 * 
 */
public class SubscriptionServiceBeanModifyAndUpgradeTest {
    private SubscriptionServiceBean bean;
    private ModifiedEntityDao modifiedEntityDao;
    private ApplicationServiceLocal as;
    private SubscriptionAuditLogCollector audit;
    private DataService ds;
    private SessionContext ctx;
    private LocalizerServiceLocal localizer;
    private TriggerQueueServiceLocal trigger;
    private CommunicationServiceLocal commService;
    private ManageSubscriptionBean manageBean;
    private ValidateSubscriptionStateBean stateValidator;
    private ModifyAndUpgradeSubscriptionBean modifyAndUpgradeBean;
    private PlatformUser user;
    private UdaAccess udaAccess;
    private List<VOParameter> voParameters;
    private List<VOUda> voUdas;
    private VOPaymentInfo voPaymentInfo;
    private VOBillingContact voBillingContact;
    private VOService voService;
    private Subscription subscription;
    private Organization technologyProvider;
    private Product targetProduct;
    private PaymentInfo paymentInfo;
    private BillingContact billingContact;
    private VOSubscription voSubscription;
    private TriggerProcess triggerProcessModify;
    private TriggerProcess triggerProcessUpgrade;
    private ProvisioningType provisioningType;
    private static final int PARAMETER_NUM = 3;
    private static final long SUBSCRIPTION_KEY = 1000L;
    private static final String SUBSCRIPTION_ID = "subId";
    private static final String NEW_SUBSCRIPTION_ID = "newSubId";
    private static final String ORGANIZATION_ID = "orgId";
    private OrganizationDao orgDao = mock(OrganizationDao.class);
    private List<PlatformUser> givenUsers = new ArrayList<>();

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        bean = spy(new SubscriptionServiceBean());
        ds = mock(DataService.class);
        as = mock(ApplicationServiceLocal.class);
        audit = mock(SubscriptionAuditLogCollector.class);
        ctx = mock(SessionContext.class);
        udaAccess = mock(UdaAccess.class);
        localizer = mock(LocalizerServiceLocal.class);
        modifiedEntityDao = mock(ModifiedEntityDao.class);
        trigger = mock(TriggerQueueServiceLocal.class);
        manageBean = spy(new ManageSubscriptionBean());
        manageBean.dataManager = ds;
        commService = mock(CommunicationServiceLocal.class);
        stateValidator = mock(ValidateSubscriptionStateBean.class);
        modifyAndUpgradeBean = spy(new ModifyAndUpgradeSubscriptionBean());
        modifyAndUpgradeBean.dataManager = ds;
        modifyAndUpgradeBean.commService = commService;
        bean.dataManager = ds;
        bean.appManager = as;
        bean.audit = audit;
        bean.sessionCtx = ctx;
        bean.localizer = localizer;
        bean.triggerQS = trigger;
        bean.manageBean = manageBean;
        bean.stateValidator = stateValidator;
        bean.modUpgBean = modifyAndUpgradeBean;
        doNothing().when(ctx).setRollbackOnly();

        technologyProvider = givenOrganization();
        user = new PlatformUser();
        user.setOrganization(technologyProvider);
        when(ds.getCurrentUser()).thenReturn(user);
        paymentInfo = mock(PaymentInfo.class);
        billingContact = mock(BillingContact.class);

        voParameters = givenVOParameters();
        voUdas = new ArrayList<>();
        voPaymentInfo = new VOPaymentInfo();
        voBillingContact = new VOBillingContact();
        voService = new VOService();
        voService.setKey(1L);

        doReturn(modifiedEntityDao).when(bean).getModifiedEntityDao();
        doReturn(Boolean.TRUE).when(bean).checkIfParametersAreModified(
                any(Subscription.class), any(Subscription.class),
                any(Product.class), any(Product.class),
                anyListOf(VOParameter.class), anyBoolean());
        doReturn(udaAccess).when(manageBean).getUdaAccess();
        doReturn(new ArrayList<Uda>()).when(udaAccess)
                .getExistingUdas(anyLong(), anyLong(), any(Organization.class));
        doReturn(paymentInfo).when(ds).getReference(eq(PaymentInfo.class),
                anyLong());
        doReturn(billingContact).when(ds).getReference(eq(BillingContact.class),
                anyLong());
        doNothing().when(bean).saveUdasForSubscription(anyListOf(VOUda.class),
                any(Subscription.class));
        doNothing().when(bean).validateSettingsForUpgrading(
                any(VOSubscription.class), any(VOService.class),
                any(VOPaymentInfo.class), any(VOBillingContact.class));
        doNothing().when(bean).copyProductAndModifyParametersForUpgrade(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        givenAsynchronousProvisiong();
    }

    @Test
    public void modifySubscriptionInt_Sync() throws Exception {
        // given
        givenSynchronousProvisiong();
        prepareSubscriptionForModify(SUBSCRIPTION_ID);

        // when
        bean.modifySubscriptionInt(triggerProcessModify);

        // then
        verify(bean, times(1)).copyProductAndModifyParametersForUpdate(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        verify(bean, times(1)).saveUdasForSubscription(eq(voUdas),
                eq(subscription));
        verify(bean.modUpgBean, never()).storeModifiedEntity(anyLong(),
                any(ModifiedEntityType.class), anyString());
    }

    @Test
    public void modifySubscriptionInt_Async() throws Exception {
        // given
        givenAsynchronousProvisiong();
        prepareSubscriptionForModify(SUBSCRIPTION_ID);

        // when
        bean.modifySubscriptionInt(triggerProcessModify);

        // then
        verify(bean, times(1)).copyProductAndModifyParametersForUpdate(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        verify(bean.modUpgBean, times(5)).storeModifiedEntity(anyLong(),
                any(ModifiedEntityType.class), anyString());
        verify(bean, never()).saveUdasForSubscription(anyListOf(VOUda.class),
                any(Subscription.class));
    }

    @Test
    public void modifySubscriptionInt_Async_IdAndParameterNotModified()
            throws Exception {
        // given
        givenAsynchronousProvisiong();
        prepareSubscriptionForModify(SUBSCRIPTION_ID);
        doReturn(Boolean.FALSE).when(bean).checkIfParametersAreModified(
                any(Subscription.class), any(Subscription.class),
                any(Product.class), any(Product.class),
                anyListOf(VOParameter.class), anyBoolean());

        // when
        bean.modifySubscriptionInt(triggerProcessModify);

        // then
        verify(bean, never()).copyProductAndModifyParametersForUpdate(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        verify(bean.modUpgBean, never()).storeModifiedEntity(anyLong(),
                any(ModifiedEntityType.class), anyString());
        verify(bean, times(1)).saveUdasForSubscription(eq(voUdas),
                eq(subscription));
    }

    @Test
    public void modifySubscriptionInt_Async_IdChanged() throws Exception {
        // given
        givenAsynchronousProvisiong();
        prepareSubscriptionForModify(NEW_SUBSCRIPTION_ID);
        doReturn(Long.valueOf(0)).when(modifiedEntityDao)
                .countSubscriptionOfOrganizationAndSubscription(
                        any(Subscription.class), anyString());
        TriggerService ts = mock(TriggerService.class);
        List<VOTriggerProcess> triggerProcesses = new ArrayList<>();
        bean.triggerService = ts;
        when(ts.getAllActionsForOrganizationRelatedSubscription()).thenReturn(triggerProcesses);
        // when
        bean.modifySubscriptionInt(triggerProcessModify);

        // then
        verify(bean, times(1)).copyProductAndModifyParametersForUpdate(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        verify(bean.modUpgBean, times(5)).storeModifiedEntity(anyLong(),
                any(ModifiedEntityType.class), anyString());
        verify(bean, never()).saveUdasForSubscription(anyListOf(VOUda.class),
                any(Subscription.class));
    }

    @Test
    public void upgradeSubscriptionInt_Sync() throws Exception {
        // given
        givenSynchronousProvisiong();
        prepareSubscriptionForUpgrade();
        doReturn(orgDao).when(bean.modUpgBean).getOrganizationDao();
        doReturn(givenUsers).when(orgDao).getOrganizationAdmins(anyLong());

        // when
        bean.upgradeSubscriptionInt(triggerProcessUpgrade);

        // then
        verify(bean, times(1)).copyProductAndModifyParametersForUpgrade(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        verify(bean, times(1)).saveUdasForSubscription(eq(voUdas),
                eq(subscription));
    }

    @Test
    public void upgradeSubscriptionInt_Async() throws Exception {
        // given
        givenAsynchronousProvisiong();
        prepareSubscriptionForUpgrade();

        // when
        bean.upgradeSubscriptionInt(triggerProcessUpgrade);

        // then
        verify(bean, times(1)).copyProductAndModifyParametersForUpgrade(
                eq(subscription), eq(targetProduct), eq(user),
                anyListOf(VOParameter.class));
        verify(bean.modUpgBean, times(2)).storeModifiedEntity(anyLong(),
                any(ModifiedEntityType.class), anyString());
        verify(bean, never()).saveUdasForSubscription(anyListOf(VOUda.class),
                any(Subscription.class));
    }

    @Test
    public void updateAccessInfo() throws Exception {
        // given
        prepareSubscription(SUBSCRIPTION_ID);
        VOInstanceInfo info = givenInstanceInfo();

        // when
        bean.updateAccessInformation(SUBSCRIPTION_ID, ORGANIZATION_ID, info);

        // then
        assertInstanceInfoSet(info);

    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateAccessInfo_OtherUser() throws Exception {
        // given
        givenOtherUserCalling();
        prepareSubscription(SUBSCRIPTION_ID);
        VOInstanceInfo info = givenInstanceInfo();

        // when
        bean.updateAccessInformation(SUBSCRIPTION_ID, ORGANIZATION_ID, info);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateAccessInfo_Null() throws Exception {
        // given
        VOInstanceInfo info = null;

        // when
        bean.updateAccessInformation(SUBSCRIPTION_ID, ORGANIZATION_ID, info);
    }

    @Test(expected = ValidationException.class)
    public void updateAccessInfo_BaseURL() throws Exception {
        // given
        prepareSubscription(SUBSCRIPTION_ID);
        VOInstanceInfo info = givenInstanceInfo();
        info.setBaseUrl("INVALID - NO URL");

        // when
        bean.updateAccessInformation(SUBSCRIPTION_ID, ORGANIZATION_ID, info);
    }

    @Test
    public void saveUdasForAsyncModifyOrUpgradeSubscription() throws Exception {
        // given
        prepareSubscription(SUBSCRIPTION_ID);
        List<VOUda> udas = givenVoUdas();
        Uda uda = new Uda();
        uda.setKey(1000L);
        uda.setUdaValue("value1");
        uda.setUdaDefinition(new UdaDefinition());
        doReturn(uda).when(ds).getReferenceByBusinessKey(any(Uda.class));

        // when
        bean.saveUdasForAsyncModifyOrUpgradeSubscription(udas, subscription);

        // then
        verify(udaAccess, times(1)).saveUdas(anyListOf(VOUda.class),
                any(Organization.class));
        verify(bean.modUpgBean, times(1)).storeModifiedUda(eq(1000L),
                eq(ModifiedEntityType.UDA_VALUE), eq("value1"),
                eq(SUBSCRIPTION_KEY), eq(false));
    }

    @Test
    public void removeSubscriptionOwner() throws ObjectNotFoundException {
        // given
        prepareSubscription(SUBSCRIPTION_ID);
        // when
        bean.removeSubscriptionOwner(subscription);
        // then
        assertEquals(subscription.getOwner(), null);
    }

    private List<VOUda> givenVoUdas() {
        List<VOUda> udas = new ArrayList<>();
        VOUda uda1 = new VOUda();
        uda1.setTargetObjectKey(1L);
        uda1.setUdaDefinition(new VOUdaDefinition());
        uda1.setUdaValue("value1");
        udas.add(uda1);
        return udas;
    }

    private void assertInstanceInfoSet(VOInstanceInfo info) {
        assertEquals(info.getAccessInfo(), subscription.getAccessInfo());
        assertEquals(info.getBaseUrl(), subscription.getBaseURL());
        assertEquals(info.getInstanceId(), subscription.getProductInstanceId());
        assertEquals(info.getLoginPath(), subscription.getLoginPath());
    }

    private VOInstanceInfo givenInstanceInfo() {
        VOInstanceInfo info = new VOInstanceInfo();
        info.setAccessInfo("info");
        info.setBaseUrl("http://www.basUrl.com");
        info.setInstanceId("instanceId");
        info.setLoginPath("loginPath");
        return info;
    }

    private List<Parameter> givenParameters() {
        List<Parameter> parameters = new ArrayList<>();
        for (int i = 1; i <= PARAMETER_NUM; i++) {
            Parameter parameter = new Parameter();
            ParameterDefinition definition = new ParameterDefinition();
            definition.setParameterId("PARAMETER_ID" + String.valueOf(i));
            parameter.setParameterDefinition(definition);
            parameter.setKey(i);
            parameter.setValue("PARAMETER_VALUE" + String.valueOf(i));
            parameters.add(parameter);
        }
        return parameters;
    }

    private ParameterSet givenParameterSet(List<Parameter> parameters) {
        ParameterSet parameterSet = new ParameterSet();
        parameterSet.setKey(1L);
        parameterSet.setParameters(parameters);
        return parameterSet;
    }

    private List<VOParameter> givenVOParameters() {
        List<VOParameter> voParameters = new ArrayList<>();
        for (int i = 1; i <= PARAMETER_NUM; i++) {
            VOParameter voParameter = new VOParameter();
            VOParameterDefinition vodefinition = new VOParameterDefinition();
            vodefinition.setParameterId("PARAMETER_ID" + String.valueOf(i));
            voParameter.setParameterDefinition(vodefinition);
            voParameter.setKey(i);
            voParameter.setValue("VALUE" + String.valueOf(i));
            voParameters.add(voParameter);
        }
        return voParameters;
    }

    private Product givenProduct(Organization technologyProvider) {
        Product product = new Product();
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setProvisioningType(provisioningType);
        product.setKey(1L);
        product.setTechnicalProduct(techProduct);
        techProduct.setOrganization(technologyProvider);
        product.setParameterSet(givenParameterSet(givenParameters()));
        product.setAutoAssignUserEnabled(Boolean.FALSE);
        product.setType(ServiceType.TEMPLATE);
        List<TechnicalProduct> tps = new ArrayList<>();
        tps.add(techProduct);
        technologyProvider.setTechnicalProducts(tps);
        PriceModel pm = new PriceModel();
        pm.setType(PriceModelType.FREE_OF_CHARGE);
        product.setPriceModel(pm);
        product.setVendor(new Organization());
        return product;
    }

    void givenAsynchronousProvisiong() {
        provisioningType = ProvisioningType.ASYNCHRONOUS;
    }

    void givenSynchronousProvisiong() {
        provisioningType = ProvisioningType.SYNCHRONOUS;
    }

    private Organization givenOrganization() {
        Organization organization = new Organization();
        organization.setOrganizationId(ORGANIZATION_ID);
        return organization;
    }

    private Organization otherOrganization() {
        Organization organization = new Organization();
        organization.setOrganizationId("Other_ID");
        return organization;
    }

    private Subscription givenSubscription(Product product) {
        Subscription subscription = new Subscription();
        subscription.setKey(SUBSCRIPTION_KEY);
        subscription.setSubscriptionId(SUBSCRIPTION_ID);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setProduct(product);
        subscription.setOrganization(technologyProvider);
        subscription.setOwner(user);

        return subscription;
    }

    private VOSubscription givenVOSubscription(String subscriptionId) {
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setKey(SUBSCRIPTION_KEY);
        voSubscription.setSubscriptionId(subscriptionId);
        return voSubscription;
    }

    private TriggerProcess givenTriggerProcessForModifySubscription() {
        TriggerProcess triggerProcess = new TriggerProcess();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, voSubscription);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PARAMETERS, voParameters);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.UDAS, voUdas);
        return triggerProcess;
    }

    private TriggerProcess givenTriggerProcessForUpgradeSubscription() {
        TriggerProcess triggerProcess = new TriggerProcess();
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.SUBSCRIPTION, voSubscription);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PRODUCT, voService);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.PAYMENTINFO, voPaymentInfo);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, voBillingContact);
        triggerProcess.addTriggerProcessParameter(
                TriggerProcessParameterName.UDAS, voUdas);
        return triggerProcess;
    }

    private void prepareSubscription(String subscriptionId)
            throws ObjectNotFoundException {
        prepareSubscription(subscriptionId, technologyProvider);
    }

    private void prepareSubscription(String subscriptionId,
            Organization technologyProvider) throws ObjectNotFoundException {
        voSubscription = givenVOSubscription(subscriptionId);
        targetProduct = givenProduct(technologyProvider);
        subscription = givenSubscription(targetProduct);
        doReturn(targetProduct).when(ds).getReference(Product.class,
                voService.getKey());
        doReturn(subscription).when(ds).getReference(Subscription.class,
                subscription.getKey());
        doReturn(subscription).when(manageBean).loadSubscription(anyString(),
                anyLong());
        doReturn(subscription).when(manageBean).findSubscription(anyString(),
                anyString());

    }

    private void prepareSubscriptionForModify(String subscriptionId)
            throws ObjectNotFoundException {
        prepareSubscription(subscriptionId);
        triggerProcessModify = givenTriggerProcessForModifySubscription();
    }

    private void prepareSubscriptionForUpgrade()
            throws ObjectNotFoundException {
        prepareSubscription(SUBSCRIPTION_ID);
        triggerProcessUpgrade = givenTriggerProcessForUpgradeSubscription();
    }

    private void givenOtherUserCalling() {
        Organization organization = otherOrganization();
        user = new PlatformUser();
        user.setOrganization(organization);
        when(ds.getCurrentUser()).thenReturn(user);
        organization.setTechnicalProducts(new ArrayList<TechnicalProduct>());
    }

}
