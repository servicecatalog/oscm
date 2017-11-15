/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 9, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
import java.util.List;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.SubscriptionMigrationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;

/**
 * @author Zhou
 * 
 */
public class SubscriptionServiceBeanCopyProductAndModifyParametersTest {
    private SubscriptionServiceBean bean;
    private ApplicationServiceLocal as;
    private LocalizerServiceLocal ls;
    private SubscriptionAuditLogCollector audit;
    private DataService ds;
    private SessionContext ctx;
    private PlatformUser user;
    private List<VOParameter> voParameters;
    private ParameterSet parameterSet;
    private List<Parameter> initialParameters;
    private Product initialProduct;
    private Product targetProduct;

    private static final int PARAMETER_NUM = 3;

    @Before
    public void setup() throws Exception {
        bean = spy(new SubscriptionServiceBean());
        ds = mock(DataService.class);
        as = mock(ApplicationServiceLocal.class);
        ls = mock(LocalizerServiceLocal.class);
        audit = mock(SubscriptionAuditLogCollector.class);

        ctx = mock(SessionContext.class);
        bean.dataManager = ds;
        bean.appManager = as;
        bean.localizer = ls;

        bean.audit = audit;
        bean.sessionCtx = ctx;

        doNothing().when(ctx).setRollbackOnly();

        user = new PlatformUser();
        Organization org = new Organization();
        org.setKey(2L);
        user.setOrganization(org);
        when(ds.getCurrentUser()).thenReturn(user);
        doReturn(user).when(ds).getCurrentUser();
        voParameters = givenVOParameters();
        parameterSet = givenParameterSet(givenParameters());
        targetProduct = givenProduct(parameterSet,
                ProvisioningType.ASYNCHRONOUS);
        initialParameters = givenInitialParameters();
        initialProduct = givenProduct(givenParameterSet(initialParameters),
                ProvisioningType.ASYNCHRONOUS);
    }

    @Test
    public void handleAsyncUpdateSubscrption_Active() throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);

        // when
        bean.handleAsyncUpdateSubscription(subscription, targetProduct);

        // then
        verify(as, times(1)).asyncModifySubscription(eq(subscription),
                eq(targetProduct));
        assertEquals(SubscriptionStatus.PENDING_UPD, subscription.getStatus());
    }

    @Test
    public void handleAsyncUpdateSubscrption_Suspended() throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.SUSPENDED, initialProduct);

        // when
        bean.handleAsyncUpdateSubscription(subscription, targetProduct);

        // then
        verify(as, times(1)).asyncModifySubscription(eq(subscription),
                eq(targetProduct));
        assertEquals(SubscriptionStatus.SUSPENDED_UPD, subscription.getStatus());
    }

    @Test
    public void handleAsyncUpdateSubscription_Pending() throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.PENDING, initialProduct);

        // when
        bean.handleAsyncUpdateSubscription(subscription, targetProduct);

        // then
        verify(as, never()).asyncModifySubscription(any(Subscription.class),
                any(Product.class));
        assertEquals(SubscriptionStatus.PENDING, subscription.getStatus());
    }

    @Test
    public void handleAsyncUpgradeSubscrption_Active() throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);

        // when
        bean.handleAsyncUpgradeSubscription(subscription, targetProduct);

        // then
        verify(as, times(1)).asyncUpgradeSubscription(eq(subscription),
                eq(targetProduct));
        assertEquals(SubscriptionStatus.PENDING_UPD, subscription.getStatus());
    }

    @Test
    public void handleAsyncUpgradeSubscrption_Suspended_B10967_1()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.SUSPENDED, initialProduct);

        // when
        bean.handleAsyncUpgradeSubscription(subscription, targetProduct);

        // then
        verify(as, times(1)).asyncUpgradeSubscription(eq(subscription),
                eq(targetProduct));
        assertEquals(SubscriptionStatus.SUSPENDED_UPD, subscription.getStatus());
    }

    @Test
    public void handleAsyncUpgradeSubscrption_Expired() throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.EXPIRED, initialProduct);

        // when
        bean.handleAsyncUpgradeSubscription(subscription, targetProduct);

        // then
        verify(as, times(1)).asyncUpgradeSubscription(eq(subscription),
                eq(targetProduct));
        assertEquals(SubscriptionStatus.EXPIRED, subscription.getStatus());
    }

    @Test
    public void handleAsyncUpgradeSubscrption_Pending() throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.PENDING, initialProduct);

        // when
        bean.handleAsyncUpgradeSubscription(subscription, targetProduct);

        // then
        verify(as, never()).asyncUpgradeSubscription(any(Subscription.class),
                any(Product.class));
        assertEquals(SubscriptionStatus.PENDING, subscription.getStatus());
    }

    @Test
    public void copyProductAndModifyParametersForUpdate_Synchronous()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        targetProduct.getTechnicalProduct().setProvisioningType(
                ProvisioningType.SYNCHRONOUS);

        // when
        bean.copyProductAndModifyParametersForUpdate(subscription,
                targetProduct, user, voParameters);

        // then
        verify(bean, times(1)).updateConfiguredParameterValues(
                any(Product.class), eq(voParameters), eq(subscription));
        verify(as, times(1)).modifySubscription(eq(subscription));
    }

    @Test
    public void copyProductAndModifyParametersForUpdate_Asynchronous()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);

        // when
        bean.copyProductAndModifyParametersForUpdate(subscription,
                targetProduct, user, voParameters);

        // then
        verify(bean, times(1)).updateConfiguredParameterValues(
                any(Product.class), eq(voParameters), eq(subscription));
        verify(bean, times(1)).handleAsyncUpdateSubscription(
                any(Subscription.class), any(Product.class));
        assertEquals(Boolean.TRUE, new Boolean(subscription
                .getAsyncTempProduct().getPriceModel()
                .isProvisioningCompleted()));
        assertEquals(SubscriptionStatus.PENDING_UPD, subscription.getStatus());
    }

    @Test
    public void copyProductAndModifyParametersForUpgrade_Synchronous()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        targetProduct.getTechnicalProduct().setProvisioningType(
                ProvisioningType.SYNCHRONOUS);

        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);

        // then
        verify(bean, times(1)).updateConfiguredParameterValues(
                any(Product.class), eq(voParameters), eq(subscription));
        verify(as, times(1)).upgradeSubscription(any(Subscription.class));
    }

    @Test
    public void copyProductAndModifyParametersForUpgrade_Synchronous_Expired()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.EXPIRED, initialProduct);
        targetProduct.getTechnicalProduct().setProvisioningType(
                ProvisioningType.SYNCHRONOUS);

        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);

        // then
        verify(bean, times(1)).updateConfiguredParameterValues(
                any(Product.class), eq(voParameters), eq(subscription));
        verify(as, times(1)).activateInstance(eq(subscription));
        verify(as, times(1)).upgradeSubscription(eq(subscription));
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
    }

    @Test
    public void copyProductAndModifyParametersForUpgrade_Asynchronous()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);

        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);

        // then
        assertEquals(Boolean.FALSE, new Boolean(subscription
                .getAsyncTempProduct().getPriceModel()
                .isProvisioningCompleted()));
        verify(bean, times(1)).updateConfiguredParameterValues(
                any(Product.class), eq(voParameters), eq(subscription));
        verify(bean, times(1)).handleAsyncUpgradeSubscription(eq(subscription),
                any(Product.class));
        assertEquals(SubscriptionStatus.PENDING_UPD, subscription.getStatus());
    }

    @Test
    public void copyProductAndModifyParametersForUpgrade_Asynchronous_Expired()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.EXPIRED, initialProduct);

        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);

        // then
        verify(bean, times(1)).updateConfiguredParameterValues(
                any(Product.class), eq(voParameters), eq(subscription));
        verify(bean, times(1)).handleAsyncUpgradeSubscription(eq(subscription),
                any(Product.class));
        assertEquals(SubscriptionStatus.EXPIRED, subscription.getStatus());
    }

    @Test(expected = SaaSSystemException.class)
    public void copyProductAndModifyParametersForUpdate_SaaSSystemException()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        doThrow(new NonUniqueBusinessKeyException()).when(ds).persist(
                any(DomainObject.class));
        // when
        bean.copyProductAndModifyParametersForUpdate(subscription,
                targetProduct, user, voParameters);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void copyProductAndModifyParametersForUpdate_TechnicalServiceNotAliveException()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        doThrow(new TechnicalServiceNotAliveException()).when(as)
                .asyncModifySubscription(any(Subscription.class),
                        any(Product.class));
        // when
        bean.copyProductAndModifyParametersForUpdate(subscription,
                targetProduct, user, voParameters);
    }

    @Test(expected = SubscriptionMigrationException.class)
    public void copyProductAndModifyParametersForUpdate_SubscriptionMigrationException()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        doThrow(new TechnicalServiceOperationException()).when(as)
                .asyncModifySubscription(any(Subscription.class),
                        any(Product.class));
        // when
        bean.copyProductAndModifyParametersForUpdate(subscription,
                targetProduct, user, voParameters);
    }

    @Test(expected = SaaSSystemException.class)
    public void copyProductAndModifyParametersForUpgrade_SaaSSystemException()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        doThrow(new NonUniqueBusinessKeyException()).when(ds).persist(
                any(DomainObject.class));
        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void copyProductAndModifyParametersForUpgrade_TechnicalServiceNotAliveException()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        doThrow(new TechnicalServiceNotAliveException()).when(as)
                .asyncUpgradeSubscription(any(Subscription.class),
                        any(Product.class));
        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);
    }

    @Test(expected = SubscriptionMigrationException.class)
    public void copyProductAndModifyParametersForUpgrade_SubscriptionMigrationException()
            throws Exception {
        // given
        Subscription subscription = givenSubscription(
                SubscriptionStatus.ACTIVE, initialProduct);
        doThrow(new TechnicalServiceOperationException()).when(as)
                .asyncUpgradeSubscription(any(Subscription.class),
                        any(Product.class));
        // when
        bean.copyProductAndModifyParametersForUpgrade(subscription,
                targetProduct, user, voParameters);
    }

    private List<Parameter> givenParameters() {
        List<Parameter> parameters = new ArrayList<Parameter>();
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

    private List<Parameter> givenInitialParameters() {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (int i = 1; i <= PARAMETER_NUM; i++) {
            Parameter parameter = new Parameter();
            ParameterDefinition definition = new ParameterDefinition();
            definition.setParameterId("PARAMETER_ID" + String.valueOf(i));
            parameter.setParameterDefinition(definition);
            parameter.setKey(i);
            parameter.setValue("PARAMETER_INITIALVALUE" + String.valueOf(i));
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
        List<VOParameter> voParameters = new ArrayList<VOParameter>();
        for (int i = 1; i <= PARAMETER_NUM; i++) {
            VOParameter voParameter = new VOParameter();
            VOParameterDefinition vodefinition = new VOParameterDefinition();
            vodefinition.setParameterId("PARAMETER_ID" + String.valueOf(i));
            voParameter.setParameterDefinition(vodefinition);
            voParameter.setValue("VALUE" + String.valueOf(i));
            voParameters.add(voParameter);
        }
        return voParameters;
    }

    private Product givenProduct(ParameterSet paraSet,
            ProvisioningType provisioningType) {
        Product product = new Product();
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setProvisioningType(provisioningType);
        product.setTechnicalProduct(techProduct);
        product.setParameterSet(paraSet);
        product.setType(ServiceType.TEMPLATE);
        product.setPriceModel(new PriceModel());
        product.setVendor(new Organization());
        return product;
    }

    private Subscription givenSubscription(SubscriptionStatus status,
            Product product) {
        Subscription subscription = new Subscription();
        subscription.setStatus(status);
        subscription.setProduct(product);
        return subscription;
    }
}
