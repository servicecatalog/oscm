/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 9, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.types.enumtypes.EmailType;

/**
 * @author Zhao
 * 
 */
public class SubscriptionServiceAsyncModifySubscriptionTest extends
        SubscriptionServiceMockBase {
    private SubscriptionServiceBean bean;

    private PlatformUser user;

    private Subscription subscription;
    private List<VOLocalizedText> reason;

    private static final String SUBSCRIPTION_ID = "subId";
    private static final String ORGANIZATION_ID = "orgId";
    private static final int PARAMETER_NUM = 3;
    private static final String LOCALE = "en";
    private static final String INIT_VALUE = "INIT_VALUE";
    private static final String NEW_VALUE = "NEW_VALUE";
    private DataService ds;
    private CommunicationServiceLocal commService;

    @Before
    public void setup() throws Exception {
        bean = initMocksAndSpys();

        user = givenCurrentUser();
        ds = mock(DataService.class);
        bean.dataManager = ds;
        bean.modUpgBean.dataManager = ds;
        bean.manageBean.dataManager = ds;

        commService = mock(CommunicationServiceLocal.class);
        bean.modUpgBean.commService = commService;
        bean.commService = commService;
        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        when(bean.modUpgBean.dataManager.getCurrentUser()).thenReturn(user);
        when(bean.dataManager.getCurrentUser()).thenReturn(user);

        reason = new ArrayList<VOLocalizedText>();

        List<PlatformUser> receivers = givenPlatformUsers();
        doReturn(receivers).when(bean.manageBean)
                .getCustomerAdminsAndSubscriptionOwner(any(Subscription.class));
        doReturn(receivers).when(bean.modUpgBean)
                .getCustomerAdminsAndSubscriptionOwner(any(Subscription.class));
        doReturn(null).when(bean.modUpgBean)
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        doReturn(null).when(bean.modUpgBean)
                .updateSubscriptionAttributesForAsyncUpdate(
                        any(Subscription.class));
        doNothing().when(bean.modUpgBean).deleteModifiedEntityForSubscription(
                any(Subscription.class));
    }

    private SubscriptionServiceBean initMocksAndSpys() throws Exception {
        bean = new SubscriptionServiceBean();
        spyInjected(bean, givenSpyClasses());
        mockEJBs(bean);
        mockResources(bean);
        return spy(bean);
    }

    private List<Class<?>> givenSpyClasses() {
        List<Class<?>> spys = new ArrayList<Class<?>>();
        spys.add(TerminateSubscriptionBean.class);
        spys.add(ManageSubscriptionBean.class);
        spys.add(ValidateSubscriptionStateBean.class);
        spys.add(ModifyAndUpgradeSubscriptionBean.class);
        spys.add(SubscriptionUtilBean.class);
        return spys;
    }

    @Test
    public void abortAsyncModifySubscription_Success() throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(true);

        // when
        bean.abortAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                reason);
        // then
        assertNull(subscription.getAsyncTempProduct());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        verify(bean.modUpgBean, times(1)).deleteModifiedEntityForSubscription(
                any(Subscription.class));
        verify(bean.localizer, times(1)).removeLocalizedValues(
                eq(subscription.getKey()),
                eq(LocalizedObjectTypes.SUBSCRIPTION_MODIFICATION_REASON));
        verify(bean.localizer, times(1)).storeLocalizedResources(
                eq(subscription.getKey()),
                eq(LocalizedObjectTypes.SUBSCRIPTION_MODIFICATION_REASON),
                eq(reason));
        verify(bean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_PARAMETER_MODIFY_ABORT),
                any(Object[].class), any(Marketplace.class));
    }

    @Test
    public void abortAsyncModifySubscription_SubscriptionStateException()
            throws Exception {
        // given
        subscription = givenFreeSubscription(SubscriptionStatus.ACTIVE, true);
        // when
        try {
            bean.abortAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                    reason);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals(
                    "ex.SubscriptionStateException.SUBSCRIPTION_INVALID_STATE",
                    e.getMessageKey());
        }
    }

    @Test
    public void abortAsyncModifySubscription_OperationNotPermittedException()
            throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(true);
        subscription.getProduct().getTechnicalProduct().setKey(2L);
        // when
        try {
            bean.abortAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                    reason);
        } catch (OperationNotPermittedException e) {
            // then
            assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
        }
    }

    @Test
    public void abortAsyncModifySubscription_NotInUpdatingProcess()
            throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(false);
        // when
        try {
            bean.abortAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                    reason);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals("ex.SubscriptionStateException.NOT_IN_UPDATING",
                    e.getMessageKey());
        }
    }

    @Test
    public void abortAsyncModifySubscription() throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(true);
        // when
        bean.abortAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                reason);
        // then
        verify(bean.modUpgBean, times(1)).findSubscriptionForAsyncCallBack(
                eq(SUBSCRIPTION_ID), eq(ORGANIZATION_ID));
        verify(bean, times(1)).abortAsyncUpgradeOrModifySubscription(
                eq(subscription), eq(ORGANIZATION_ID), eq(reason), eq(true));
    }

    @Test
    public void abortAsyncUpgradeSubscription_PENDING_UPD() throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(false);
        // when
        bean.abortAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                reason);
        // then
        verify(bean, times(1)).abortAsyncUpgradeOrModifySubscription(
                eq(subscription), eq(ORGANIZATION_ID), eq(reason), eq(true));
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }

    @Test
    public void abortAsyncUpgradeSubscription_NotInUpgradingProcess()
            throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(true);
        // when
        try {
            bean.abortAsyncUpgradeSubscription(SUBSCRIPTION_ID,
                    ORGANIZATION_ID, reason);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals("ex.SubscriptionStateException.NOT_IN_UPGRADING",
                    e.getMessageKey());
        }
    }

    @Test
    public void abortAsyncUpgradeSubscription_SUSPENDED_UPD() throws Exception {
        // given
        subscription = givenFreeSubscription(SubscriptionStatus.SUSPENDED_UPD,
                false);

        // when
        bean.abortAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                reason);
        // then
        verify(bean, times(1)).abortAsyncUpgradeOrModifySubscription(
                eq(subscription), eq(ORGANIZATION_ID), eq(reason), eq(true));

        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());
    }

    @Test
    public void completeAsyncModifySubscription_PENDING_UPD() throws Exception {

        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpdate();
        subscription = givenPendingUpdateSubscriptionFree(true);
        // when
        bean.completeAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1)).findSubscriptionForAsyncCallBack(
                eq(SUBSCRIPTION_ID), eq(ORGANIZATION_ID));
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpdate(
                        any(Subscription.class));
        verify(bean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_PARAMETER_MODIFIED),
                any(Object[].class), any(Marketplace.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncModifySubscription_SUSPENDED_UPD()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpdate();
        subscription = givenFreeSubscription(SubscriptionStatus.SUSPENDED_UPD,
                true);

        // when
        bean.completeAsyncModifySubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());
        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1)).findSubscriptionForAsyncCallBack(
                eq(SUBSCRIPTION_ID), eq(ORGANIZATION_ID));
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpdate(
                        any(Subscription.class));
        verify(bean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_PARAMETER_MODIFIED),
                any(Object[].class), any(Marketplace.class));

        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncModifySubscription_SubscriptionStateException()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpdate();
        subscription = givenFreeSubscription(SubscriptionStatus.EXPIRED, true);
        // when
        try {
            bean.completeAsyncModifySubscription(SUBSCRIPTION_ID,
                    ORGANIZATION_ID, voInstance);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals("ex.SubscriptionStateException.ONLY_UPD",
                    e.getMessageKey());
        }
        assertEquals("INIT_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
    }

    @Test
    public void completeAsyncModifySubscription_NotInUpdatingProcess()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpdate();
        subscription = givenPendingUpdateSubscriptionFree(false);
        // when
        try {
            bean.completeAsyncModifySubscription(SUBSCRIPTION_ID,
                    ORGANIZATION_ID, voInstance);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals("ex.SubscriptionStateException.NOT_IN_UPDATING",
                    e.getMessageKey());
        }
    }

    @Test
    public void completeAsyncUpgradeSubscription_PENDING_UPD_Free()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();

        subscription = givenPendingUpdateSubscriptionFree(false);

        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_MIGRATED), any(Object[].class),
                any(Marketplace.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_PENDING_UPD_Charged_Success()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        subscription = givenPendingUpdateSubscriptionCharged(false);
        doReturn(subscription).when(bean.manageBean).findSubscription(
                anyString(), anyString());

        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_MIGRATED), any(Object[].class),
                any(Marketplace.class));
        verify(bean.manageBean, never()).suspend(any(Subscription.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_PENDING_UPD_Charged_NoPayment()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        subscription = givenPendingUpdateSubscriptionCharged_NoPaymentInfo(false);
        doReturn(subscription).when(bean.manageBean).findSubscription(
                anyString(), anyString());

        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean).sendConfirmUpgradationEmail(
                any(Subscription.class), anyString(), anyString(), anyString());
        verify(bean.manageBean, times(1)).suspend(any(Subscription.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_PENDING_UPD_Charged_NoBillingContact()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        subscription = givenPendingUpdateSubscriptionCharged_NoBillingContact(false);
        doReturn(subscription).when(bean.manageBean).findSubscription(
                anyString(), anyString());

        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_MIGRATED), any(Object[].class),
                any(Marketplace.class));
        verify(bean.manageBean, times(1)).suspend(any(Subscription.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_PENDING_UPD_Charged_InvalidPaymentData()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        subscription = givenPendingUpdateSubscriptionCharged_InvalidPaymentData(false);
        doReturn(subscription).when(bean.manageBean).findSubscription(
                anyString(), anyString());

        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_MIGRATED), any(Object[].class),
                any(Marketplace.class));
        verify(bean.manageBean, times(1)).suspend(any(Subscription.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_SUSPENDED_UPD_Success()
            throws Exception {
        // given
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        subscription = givenFreeSubscription(SubscriptionStatus.SUSPENDED_UPD,
                false);
        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());

        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_MIGRATED), any(Object[].class),
                any(Marketplace.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_EXPIRED() throws Exception {
        // given
        subscription = givenFreeSubscription(SubscriptionStatus.EXPIRED, false);
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        // when
        bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID, ORGANIZATION_ID,
                voInstance);
        // then
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals("NEW_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
        verify(bean.modUpgBean, times(1))
                .updateSubscriptionAttributesForAsyncUpgrade(
                        any(Subscription.class));
        verify(bean.modUpgBean.commService).sendMail(any(PlatformUser.class),
                eq(EmailType.SUBSCRIPTION_MIGRATED), any(Object[].class),
                any(Marketplace.class));
        assertInstanceInfo(voInstance);
    }

    @Test
    public void completeAsyncUpgradeSubscription_SubscriptionStateException()
            throws Exception {
        // given
        subscription = givenFreeSubscription(SubscriptionStatus.PENDING, false);
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        // when
        try {
            bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID,
                    ORGANIZATION_ID, voInstance);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals("ex.SubscriptionStateException.ONLY_UPD",
                    e.getMessageKey());
        }
        assertEquals("INIT_VALUE1", subscription.getParameterSet()
                .getParameters().get(0).getValue());
    }

    @Test
    public void completeAsyncUpgradeSubscription_NotInUpgradingProcess()
            throws Exception {
        // given
        subscription = givenPendingUpdateSubscriptionFree(true);
        VOInstanceInfo voInstance = givenInstanceInfoForUpgrade();
        // when
        try {
            bean.completeAsyncUpgradeSubscription(SUBSCRIPTION_ID,
                    ORGANIZATION_ID, voInstance);
        } catch (SubscriptionStateException e) {
            // then
            assertEquals("ex.SubscriptionStateException.NOT_IN_UPGRADING",
                    e.getMessageKey());
        }
    }

    private List<Parameter> givenParameters(String parameterValue) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (int i = 1; i <= PARAMETER_NUM; i++) {
            Parameter parameter = new Parameter();
            ParameterDefinition definition = new ParameterDefinition();
            definition.setParameterId("PARAMETER_ID" + String.valueOf(i));
            parameter.setParameterDefinition(definition);
            parameter.setKey(i);
            parameter.setValue(parameterValue + String.valueOf(i));
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

    private PlatformUser givenCurrentUser() {
        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        org.setKey(2L);
        List<TechnicalProduct> technicalProducts = new ArrayList<TechnicalProduct>();
        technicalProducts.add(givenTechProduct());
        org.setTechnicalProducts(technicalProducts);
        user.setOrganization(org);
        user.setLocale(LOCALE);
        return user;
    }

    private TechnicalProduct givenTechProduct() {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setKey(1L);
        techProduct.setAccessType(ServiceAccessType.DIRECT);
        Organization organization = new Organization();
        organization.setPlatformUsers(givenPlatformUsers());
        techProduct.setOrganization(organization);
        return techProduct;
    }

    private Product givenProduct(ParameterSet paraSet,
            TechnicalProduct techProduct, PriceModel priceModel) {
        Product product = new Product();
        product.setTechnicalProduct(techProduct);
        product.setParameterSet(paraSet);
        product.setPriceModel(priceModel);
        return product;
    }

    private Product givenFreeProduct(ParameterSet paraSet,
            TechnicalProduct techProduct, boolean provisioningCompleted) {
        PriceModel priceModel = new PriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModel.setProvisioningCompleted(provisioningCompleted);
        return givenProduct(paraSet, techProduct, priceModel);
    }

    private Product givenChargedProduct(ParameterSet paraSet,
            TechnicalProduct techProduct, boolean provisioningCompleted) {
        PriceModel priceModel = new PriceModel();
        priceModel.setType(PriceModelType.PER_UNIT);
        priceModel.setProvisioningCompleted(provisioningCompleted);
        return givenProduct(paraSet, techProduct, priceModel);
    }

    private Subscription givenFreeSubscription(SubscriptionStatus status,
            boolean provisioningCompleted) throws ObjectNotFoundException {

        Product product = givenFreeProduct(
                givenParameterSet(givenParameters(INIT_VALUE)),
                givenTechProduct(), true);
        Product asyncTempProduct = givenFreeProduct(
                givenParameterSet(givenParameters(NEW_VALUE)),
                givenTechProduct(), provisioningCompleted);
        Subscription subscription = givenSubscription(product, asyncTempProduct);
        doReturn(subscription).when(bean.modUpgBean).findSubscription(
                eq(SUBSCRIPTION_ID), eq(ORGANIZATION_ID));
        doReturn(subscription).when(bean.manageBean).findSubscription(
                eq(SUBSCRIPTION_ID), eq(ORGANIZATION_ID));

        subscription.setStatus(status);
        return subscription;
    }

    private Subscription givenChargedSubscription(SubscriptionStatus status,
            boolean provisioningCompleted) {

        Product product = givenChargedProduct(
                givenParameterSet(givenParameters(INIT_VALUE)),
                givenTechProduct(), true);
        Product asyncTempProduct = givenChargedProduct(
                givenParameterSet(givenParameters(NEW_VALUE)),
                givenTechProduct(), provisioningCompleted);
        Subscription subscription = givenSubscription(product, asyncTempProduct);
        Organization customer = new Organization();
        subscription.setOrganization(customer);
        PaymentInfo paymentInfo = new PaymentInfo();
        subscription.setPaymentInfo(paymentInfo);
        subscription.setBillingContact(new BillingContact());
        subscription.setStatus(status);

        try {
            doNothing().when(bean.modUpgBean).validatePaymentInfo(
                    eq(subscription), eq(paymentInfo), eq(customer));
        } catch (PaymentInformationException e) {

        }
        return subscription;
    }

    private Subscription givenSubscription(Product product, Product tempProduct) {
        Subscription subscription = new Subscription();
        subscription.setKey(1000L);
        subscription.setProduct(product);
        subscription.setAsyncTempProduct(tempProduct);
        Organization org = new Organization();
        subscription.setOrganization(org);
        return subscription;
    }

    private VOInstanceInfo givenInstanceInfoForUpdate() {
        VOInstanceInfo voInstanceInfo = new VOInstanceInfo();
        voInstanceInfo.setAccessInfo("access info Update");
        voInstanceInfo
                .setBaseUrl("http://locahost:8080/example-updated-service");
        voInstanceInfo.setInstanceId("Example_with_roles 2.00");
        voInstanceInfo.setLoginPath("/login2");
        return voInstanceInfo;
    }

    private VOInstanceInfo givenInstanceInfoForUpgrade() {
        VOInstanceInfo voInstanceInfo = new VOInstanceInfo();
        voInstanceInfo.setAccessInfo("access info Upgrade");
        voInstanceInfo
                .setBaseUrl("http://locahost:8080/example-upraded-service");
        voInstanceInfo.setInstanceId("Example_with_roles 3.00");
        voInstanceInfo.setLoginPath("/login3");
        return voInstanceInfo;
    }

    private void assertInstanceInfo(VOInstanceInfo voInstance) {
        assertEquals(voInstance.getAccessInfo(), subscription.getAccessInfo());
        assertEquals(voInstance.getBaseUrl(), subscription.getBaseURL());
        assertEquals(voInstance.getInstanceId(),
                subscription.getProductInstanceId());
        assertEquals(voInstance.getLoginPath(), subscription.getLoginPath());
    }

    private Subscription givenPendingUpdateSubscriptionCharged(
            boolean provisioningCompleted) {
        return givenChargedSubscription(SubscriptionStatus.PENDING_UPD,
                provisioningCompleted);
    }

    private Subscription givenPendingUpdateSubscriptionCharged_NoPaymentInfo(
            boolean provisioningCompleted) {
        Subscription subscription = givenChargedSubscription(
                SubscriptionStatus.PENDING_UPD, provisioningCompleted);
        subscription.setPaymentInfo(null);
        return subscription;
    }

    private Subscription givenPendingUpdateSubscriptionCharged_NoBillingContact(
            boolean provisioningCompleted) {
        Subscription subscription = givenChargedSubscription(
                SubscriptionStatus.PENDING_UPD, provisioningCompleted);
        subscription.setBillingContact(null);
        return subscription;
    }

    private Subscription givenPendingUpdateSubscriptionCharged_InvalidPaymentData(
            boolean provisioningCompleted) throws Exception {
        Subscription subscription = givenChargedSubscription(
                SubscriptionStatus.PENDING_UPD, provisioningCompleted);

        doThrow(new PaymentInformationException()).when(bean.modUpgBean)
                .validatePaymentInfo(eq(subscription), any(PaymentInfo.class),
                        eq(subscription.getOrganization()));

        return subscription;
    }

    private Subscription givenPendingUpdateSubscriptionFree(
            boolean provisioningCompleted) throws ObjectNotFoundException {
        return givenFreeSubscription(SubscriptionStatus.PENDING_UPD,
                provisioningCompleted);
    }

    private List<PlatformUser> givenPlatformUsers() {
        List<PlatformUser> receivers = new ArrayList<PlatformUser>();
        PlatformUser user = new PlatformUser();
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
        Set<RoleAssignment> roleAssignSet = new HashSet<RoleAssignment>();
        roleAssignSet.add(roleAssignment);
        user.setAssignedRoles(roleAssignSet);
        receivers.add(new PlatformUser());
        return receivers;
    }

}
