/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.usesubscriptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptions.OperationModel;
import org.oscm.internal.subscriptions.OperationParameterModel;
import org.oscm.internal.subscriptions.POSubscription;
import org.oscm.internal.subscriptions.SubscriptionsService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OperationParameterType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.UiDelegate;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

public class MySubscriptionsCtrlTest {

    private static final String BASE_URL_HTTP = "http://localhost:8080/oscm-portal";
    private static final String BASE_URL_HTTPS = "https://localhost:8080/oscm-portal";

    MySubscriptionsCtrl ctrl;
    MySubscriptionsLazyDataModel model;
    ApplicationBean appBean = mock(ApplicationBean.class);

    private SubscriptionService subSvc;
    private SubscriptionsService subsSvc;
    private ConfigurationService configSvc;

    UiDelegate ui;

    @Before
    public void setup() {
        ctrl = new MySubscriptionsCtrl();
        ctrl.setApplicationBean(appBean);

        ExternalContext extContext = mock(ExternalContext.class);
        when(extContext.getRequestContextPath()).thenReturn("/oscm-portal");

        ui = mock(UiDelegate.class);
        when(ui.getExternalContext()).thenReturn(extContext);
        when(ui.getViewLocale()).thenReturn(Locale.ENGLISH);

        ctrl.ui = ui;
        ctrl.selectId = "componentid";
        model = spy(new MySubscriptionsLazyDataModel());
        ctrl.setModel(model);

        subsSvc = mock(SubscriptionsService.class);
        ctrl.setSubscriptionsService(subsSvc);

        when(ctrl.applicationBean.getServerBaseUrlHttps())
                .thenReturn(BASE_URL_HTTPS);
        when(ctrl.applicationBean.getServerBaseUrl()).thenReturn(BASE_URL_HTTP);

        subSvc = mock(SubscriptionService.class);
        ctrl.setSubscriptionService(subSvc);

        configSvc = mock(ConfigurationService.class);
        ctrl.config = configSvc;
    }

    @Test
    public void executeOperation_NoSubscription() throws Exception {
        ctrl.executeOperation();
    }

    @Test
    public void executeOperation_NoOperation() throws Exception {
        initSubscription(model);

        ctrl.executeOperation();
    }

    @Test
    public void executeOperation() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);

        ctrl.executeOperation();

        verify(subSvc).executeServiceOperation(same(sub.getVOSubscription()),
                same(om.getOperation()));
        verify(ctrl.ui).handle(eq(MySubscriptionsCtrl.INFO_OPERATION_EXECUTED),
                eq(om.getOperation().getOperationName()));
    }

    @Test
    public void executeOperation_ConcurrentlyChanged() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        doThrow(new ConcurrentModificationException()).when(subSvc)
                .executeServiceOperation(any(VOSubscription.class),
                        any(VOTechnicalServiceOperation.class));

        ctrl.executeOperation();

        verify(subSvc).executeServiceOperation(same(sub.getVOSubscription()),
                same(om.getOperation()));
        verify(ctrl.ui).handleError(anyString(),
                eq(MySubscriptionsCtrl.ERROR_SUBSCRIPTION_CONCURRENTMODIFY));
    }

    @Test
    public void operationChanged_Empty() throws Exception {
        POSubscription sub = initSubscription(model);
        initOperation(sub);
        sub.setSelectedOperationId("");
        ctrl.operationChanged();

        assertTrue(sub.isExecuteDisabled());
        assertNull(sub.getSelectedOperation());
        assertNull(sub.getSelectedOperationId());
        verify(subSvc, never()).getServiceOperationParameterValues(
                any(VOSubscription.class),
                any(VOTechnicalServiceOperation.class));
    }

    @Test
    public void operationChanged_NoParameter() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        ctrl.operationChanged();

        assertFalse(sub.isExecuteDisabled());
        assertSame(om.getOperation(),
                sub.getSelectedOperation().getOperation());
        verify(subSvc, never()).getServiceOperationParameterValues(
                any(VOSubscription.class),
                any(VOTechnicalServiceOperation.class));
    }

    @Test
    public void operationChanged_ParameterNoRequest() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        initOperationParameter(om, OperationParameterType.INPUT_STRING);
        ctrl.operationChanged();

        assertFalse(sub.isExecuteDisabled());
        assertSame(om.getOperation(),
                sub.getSelectedOperation().getOperation());
        verify(subSvc, never()).getServiceOperationParameterValues(
                any(VOSubscription.class),
                any(VOTechnicalServiceOperation.class));
    }

    @Test
    public void operationChanged_ParameterRequest() throws Exception {
        POSubscription sub = initSubscription(model);
        OperationModel om = initOperation(sub);
        initOperationParameter(om, OperationParameterType.REQUEST_SELECT);
        List<String> list = initValues(subSvc, sub, om);
        ctrl.operationChanged();

        assertFalse(sub.isExecuteDisabled());
        assertSame(om.getOperation(),
                sub.getSelectedOperation().getOperation());
        verify(subSvc).getServiceOperationParameterValues(
                eq(sub.getVOSubscription()), eq(om.getOperation()));
        List<SelectItem> values = model.getMySubscriptions().get(0)
                .getSelectedOperation().getParameters().get(0).getValues();
        assertEquals(list.size(), values.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), values.get(i).getValue());
            assertEquals(list.get(i), values.get(i).getLabel());
        }
    }

    @Test
    public void checkSubscription() {

        // given
        initSubscription(model);

        // when
        ctrl.checkSelectedSubscription();

        // then
        verify(subsSvc, times(1)).getMySubscriptionDetails(anyLong());
    }

    @Test
    public void checkSubscription_concurrentlyRemoved() {

        // given
        initSubscription(model);
        assertNotNull("Selected subscription was not selected yet",
                model.getSelectedSubscription());

        // when
        ctrl.checkSelectedSubscription();
        doReturn(null).when(subsSvc).getMySubscriptionDetails(anyLong());

        // then
        assertNull(model.getSelectedSubscription());
    }

    @Test
    public void testCustomerTabURL() throws Exception {
        CertAndKeyGen gen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
        gen.generate(1024);
        X509Certificate cert = gen.getSelfCertificate(new X500Name("CN=ROOT"),
                new Date(), 10000000);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        String alias = "temp123";
        String loc = "./temp.jks";
        String password = "changeit";
        ks.load(null, password.toCharArray());

        ks.setKeyEntry(alias, gen.getPrivateKey(), password.toCharArray(),
                new Certificate[] { cert });

        FileOutputStream fos = new FileOutputStream(loc);
        ks.store(fos, password.toCharArray());
        fos.close();

        VOConfigurationSetting settingLoc = new VOConfigurationSetting();
        settingLoc.setValue(loc);
        Mockito.when(configSvc.getVOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEYSTORE,
                Configuration.GLOBAL_CONTEXT)).thenReturn(settingLoc);

        VOConfigurationSetting settingPwd = new VOConfigurationSetting();
        settingPwd.setValue(password);
        Mockito.when(configSvc.getVOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS,
                Configuration.GLOBAL_CONTEXT)).thenReturn(settingPwd);

        VOConfigurationSetting settingAlias = new VOConfigurationSetting();
        settingAlias.setValue(alias);
        Mockito.when(configSvc.getVOConfigurationSetting(
                ConfigurationKey.SSO_SIGNING_KEY_ALIAS,
                Configuration.GLOBAL_CONTEXT)).thenReturn(settingAlias);

        String instId = "instance";
        String orgId = "organization";
        String subId = "subscription";
        String path = "http://abc.de/context";

        VOSubscription sub = new VOSubscription();
        sub.setServiceInstanceId(instId);
        sub.setOrganizationId(orgId);
        sub.setSubscriptionId(subId);
        sub.setCustomTabUrl(path);

        model.setSelectedSubscription(new POSubscription(sub));

        String urlStr = ctrl.getCustomTabUrlWithParameters();

        assertTrue(urlStr.length() > 0);
        assertTrue(urlStr.contains(Locale.ENGLISH.toLanguageTag()));
        assertTrue(urlStr
                .contains(Base64.encodeBase64URLSafeString(instId.getBytes())));
        assertTrue(urlStr
                .contains(Base64.encodeBase64URLSafeString(orgId.getBytes())));
        assertTrue(urlStr
                .contains(Base64.encodeBase64URLSafeString(subId.getBytes())));

        Files.delete(new File(loc).toPath());
    }

    private static final List<String> initValues(SubscriptionService subSvc,
            POSubscription sub, OperationModel om) throws Exception {

        VOServiceOperationParameterValues value = new VOServiceOperationParameterValues();
        value.setParameterId(om.getOperation().getOperationParameters().get(0)
                .getParameterId());
        List<String> list = Arrays.asList("1", "2", "3");
        value.setValues(list);
        when(subSvc.getServiceOperationParameterValues(
                eq(sub.getVOSubscription()), eq(om.getOperation())))
                        .thenReturn(Arrays.asList(value));
        return list;
    }

    private static final OperationParameterModel initOperationParameter(
            OperationModel om, OperationParameterType type) {
        VOServiceOperationParameter sop = new VOServiceOperationParameter();
        sop.setMandatory(true);
        sop.setParameterId("TEST");
        sop.setParameterName("Test");
        sop.setType(type);
        OperationParameterModel opm = new OperationParameterModel();
        opm.setParameter(sop);
        om.setParameters(Arrays.asList(opm));
        om.getOperation().setOperationParameters(Arrays.asList(sop));
        return opm;
    }

    private static final OperationModel initOperation(POSubscription sub) {
        VOTechnicalServiceOperation tso = new VOTechnicalServiceOperation();
        tso.setOperationId("operationId");
        tso.setOperationName("operationName");
        OperationModel om = new OperationModel();
        om.setOperation(tso);
        sub.setTechnicalServiceOperations(Arrays.asList(tso));
        sub.setSelectedOperationId(tso.getOperationId());
        sub.setSelectedOperation(om);
        return om;
    }

    private static final POSubscription initSubscription(
            MySubscriptionsLazyDataModel model) {
        VOSubscription vo = new VOSubscription();
        vo.setSubscriptionId("subscriptionId");
        vo.setStatus(SubscriptionStatus.ACTIVE);
        vo.setKey(11001);
        POSubscription subscription = new POSubscription(vo);
        when(model.getMySubscriptions())
                .thenReturn(Arrays.asList(subscription));
        when(model.getMySubscriptions())
                .thenReturn(Arrays.asList(subscription));
        model.setSubscriptionIdForOperation(vo.getSubscriptionId());
        model.setSelectedSubscription(subscription);
        model.setSelectedSubscriptionId(Long.toString(subscription.getKey()));
        return subscription;
    }
}
