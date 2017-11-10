/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 11, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
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

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.payloads.NotifyProvisioningServicePayload;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.provisioning.data.User;

/**
 * @author farmaki
 * 
 */
public class NotifyProvisioningServiceHandlerTest {

    DataService dataServiceMock;
    ApplicationServiceLocal applicationServiceMock;
    TaskQueueServiceLocal taskQueueServiceMock;
    LocalizerServiceLocal localizerServiceMock;
    ConfigurationServiceLocal configurationServiceMock;
    SubscriptionServiceLocal subscriptionServiceMock;
    CommunicationServiceLocal communicationServiceMock;

    NotifyProvisioningServiceHandler handler;

    Subscription subscription;
    private static final long SUB_KEY1 = 1;

    @Before
    public void setUp() throws Exception {
        handler = spy(new NotifyProvisioningServiceHandler(1));
        handler.setServiceFacade(createServiceFacade());
        handler.setPayload(new NotifyProvisioningServicePayload(SUB_KEY1, false));
    }

    private ServiceFacade createServiceFacade() throws Exception {
        ServiceFacade facade = new ServiceFacade();

        dataServiceMock = createDataServiceMock();
        applicationServiceMock = createApplicationServiceMock();
        configurationServiceMock = createConfigServiceMock();
        taskQueueServiceMock = createTaskQueueServiceMock();
        localizerServiceMock = createLocalizerServiceMock();
        subscriptionServiceMock = createSubscriptionServiceMock();
        communicationServiceMock = createCommunicationServiceMock();

        facade.setDataService(dataServiceMock);
        facade.setApplicationService(applicationServiceMock);
        facade.setConfigurationService(configurationServiceMock);
        facade.setTaskQueueService(taskQueueServiceMock);
        facade.setLocalizerService(localizerServiceMock);
        facade.setSubscriptionService(subscriptionServiceMock);
        facade.setCommunicationService(communicationServiceMock);

        return facade;
    }

    private DataService createDataServiceMock() throws Exception {
        createSubscription();
        DataService dataServiceMock = mock(DataService.class);
        when(dataServiceMock.getReference(Subscription.class, SUB_KEY1))
                .thenReturn(subscription);

        return dataServiceMock;
    }

    private void createSubscription() {
        subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setMarketplace(new Marketplace());

        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setKey(1L);
        techProduct.setAccessType(ServiceAccessType.USER);
        techProduct.setProvisioningType(ProvisioningType.ASYNCHRONOUS);
        techProduct.setOrganization(new Organization());
        techProduct.getOrganization().setOrganizationId("xyz");

        Product product = new Product();
        product.setTechnicalProduct(techProduct);
        product.setAutoAssignUserEnabled(Boolean.TRUE);

        subscription.setProduct(product);
        subscription.setAccessInfo("accessInfo");

        UsageLicense license = new UsageLicense();
        PlatformUser pUser = new PlatformUser();
        pUser.setUserId("user1");
        pUser.setOrganization(new Organization());
        pUser.getOrganization().setOrganizationId("abc");
        license.setUser(pUser);
        List<UsageLicense> usageLicenses = new ArrayList<UsageLicense>();
        usageLicenses.add(license);
        subscription.setUsageLicenses(usageLicenses);
        subscription.setOwner(pUser);
    }

    @SuppressWarnings("unchecked")
    private ApplicationServiceLocal createApplicationServiceMock()
            throws Exception {
        ApplicationServiceLocal applicationService = mock(ApplicationServiceLocal.class);

        doNothing().when(applicationService).deactivateInstance(
                any(Subscription.class));

        User user = new User();
        user.setApplicationUserId("applicationUserId");
        user.setUserId("user1");
        User[] users = new User[] { user };

        doReturn(users).when(applicationService).createUsers(eq(subscription),
                anyList());

        return applicationService;
    }

    private LocalizerServiceLocal createLocalizerServiceMock() {
        LocalizerServiceLocal localizerService = mock(LocalizerServiceLocal.class);
        doReturn("techProductLocalizedText").when(localizerService)
                .getLocalizedTextFromDatabase(anyString(), eq(1L),
                        eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));
        return localizerService;
    }

    private SubscriptionServiceLocal createSubscriptionServiceMock() {
        SubscriptionServiceLocal subscriptionService = mock(SubscriptionServiceLocal.class);
        return subscriptionService;
    }

    private CommunicationServiceLocal createCommunicationServiceMock() {
        CommunicationServiceLocal communicationService = mock(CommunicationServiceLocal.class);
        return communicationService;
    }

    @SuppressWarnings("unchecked")
    private TaskQueueServiceLocal createTaskQueueServiceMock() {
        TaskQueueServiceLocal taskQueueService = mock(TaskQueueServiceLocal.class);
        doNothing().when(taskQueueService).sendAllMessages(any(List.class));
        return taskQueueService;
    }

    private ConfigurationServiceLocal createConfigServiceMock() {
        ConfigurationServiceLocal configService = mock(ConfigurationServiceLocal.class);
        doReturn("http://www.baseUrl.com").when(configService).getBaseURL();

        ConfigurationSetting setting = new ConfigurationSetting();
        setting.setValue("https://www.baseUrl.com");
        doReturn(setting).when(configService).getConfigurationSetting(
                eq(ConfigurationKey.BASE_URL_HTTPS),
                eq(Configuration.GLOBAL_CONTEXT));
        return configService;

    }

    @Test
    public void execute_deactivateInstance() throws Exception {
        // given
        handler.setPayload(new NotifyProvisioningServicePayload(SUB_KEY1, true));

        // when
        handler.execute();

        // then
        verify(applicationServiceMock).deactivateInstance(subscription);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_noCreatedUsers() throws Exception {
        // given
        doReturn(null).when(applicationServiceMock).createUsers(
                eq(subscription), anyList());

        // when
        handler.execute();

        // then no application user id is mapped to usage license.
        assertNull(subscription.getUsageLicenses().get(0)
                .getApplicationUserId());
        verify(taskQueueServiceMock).sendAllMessages(any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_SubscriptionNotActive() throws Exception {
        // given
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);

        // when
        handler.execute();

        // then no e-mail is sent.
        assertEquals("applicationUserId", subscription.getUsageLicenses()
                .get(0).getApplicationUserId());
        verify(taskQueueServiceMock, times(0)).sendAllMessages(any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute() throws Exception {
        // when
        handler.execute();

        // then the application user id is mapped to usage license and an e-mail
        // is sent.
        assertEquals("applicationUserId", subscription.getUsageLicenses()
                .get(0).getApplicationUserId());
        verify(taskQueueServiceMock).sendAllMessages(any(List.class));
    }

    @Test
    public void handleError_TechnicalServiceNotAliveException()
            throws Exception {
        // when
        handler.handleError(new TechnicalServiceNotAliveException());

        // then
        verify(communicationServiceMock, times(1)).sendMail(
                any(PlatformUser.class),
                eq(EmailType.NOTIFY_PROVISIONING_SERVICE_FAILED),
                any(Object[].class), any(Marketplace.class));
        verify(communicationServiceMock, times(1)).sendMail(
                any(Organization.class),
                eq(EmailType.NOTIFY_PROVISIONING_SERVICE_FAILED),
                any(Object[].class), any(Marketplace.class));
    }

    @Test
    public void handleError_TechnicalServiceNotAliveException_OwnerIsTP()
            throws Exception {
        // given
        subscription.getOwner().getOrganization().setOrganizationId("xyz");

        // when
        handler.handleError(new TechnicalServiceNotAliveException());

        // then
        verify(communicationServiceMock, times(1)).sendMail(
                any(PlatformUser.class),
                eq(EmailType.NOTIFY_PROVISIONING_SERVICE_FAILED),
                any(Object[].class), any(Marketplace.class));
        verify(communicationServiceMock, times(0)).sendMail(
                any(Organization.class),
                eq(EmailType.NOTIFY_PROVISIONING_SERVICE_FAILED),
                any(Object[].class), any(Marketplace.class));
    }

    @Test
    public void execute_DIRECT_AccessInfoStartWithPublicDNS() throws Exception {
        // given
        subscription.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.DIRECT);
        subscription.setAccessInfo(getAccessInfo());
        // when
        handler.execute();

        // then
        verify(handler, times(2)).getIPAddress(anyString());
        verify(handler, times(2)).getPublicDNS(anyString());
        verify(handler, times(2)).getKeyPairName(anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_DIRECT() throws Exception {
        // given
        subscription.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.DIRECT);

        // when
        handler.execute();

        // then the application user id is mapped to usage license and an e-mail
        // is sent.
        assertEquals("applicationUserId", subscription.getUsageLicenses()
                .get(0).getApplicationUserId());
        verify(taskQueueServiceMock).sendAllMessages(any(List.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_LOGIN() throws Exception {
        // given
        subscription.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.LOGIN);

        // when
        handler.execute();

        // then the application user id is mapped to usage license and an e-mail
        // is sent.
        assertEquals("applicationUserId", subscription.getUsageLicenses()
                .get(0).getApplicationUserId());
        verify(taskQueueServiceMock).sendAllMessages(any(List.class));
    }

    @Test
    public void getAccessInfo_fromSubscription() throws Exception {
        // when
        String accessInfo = handler.getAccessInfo(subscription, subscription
                .getUsageLicenses().get(0).getUser());

        // then
        assertEquals("accessInfo", accessInfo);
    }

    @Test
    public void getAccessInfo_fromTechService() throws Exception {
        // given
        subscription.setAccessInfo(null);

        // when
        String accessInfo = handler.getAccessInfo(subscription, subscription
                .getUsageLicenses().get(0).getUser());

        // then
        assertEquals("techProductLocalizedText", accessInfo);
    }

    @Test
    public void getAccessInfo_nullTechServiceText() throws Exception {
        // given
        subscription.setAccessInfo(null);
        doReturn(null).when(localizerServiceMock).getLocalizedTextFromDatabase(
                anyString(), eq(1L),
                eq(LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC));

        // when
        String accessInfo = handler.getAccessInfo(subscription, subscription
                .getUsageLicenses().get(0).getUser());

        // then
        assertEquals("", accessInfo);
    }

    @Test
    public void getAccessInfo_LOGIN() throws Exception {
        // given
        subscription.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.LOGIN);

        // when
        String accessInfo = handler.getAccessInfo(subscription, subscription
                .getUsageLicenses().get(0).getUser());

        // then
        assertEquals("http://www.baseUrl.com/opt/0/", accessInfo);
    }

    @Test
    public void getAccessInfo_LOGIN_httpsScheme() throws Exception {
        // given
        subscription.getProduct().getTechnicalProduct()
                .setAccessType(ServiceAccessType.LOGIN);
        subscription.setAccessInfo(null);
        subscription.getProduct().getTechnicalProduct()
                .setBaseURL("https://url.com");
        doReturn(null).when(localizerServiceMock).getLocalizedTextFromDatabase(
                anyString(), eq(1L), any(LocalizedObjectTypes.class));

        // when
        String accessInfo = handler.getAccessInfo(subscription, subscription
                .getUsageLicenses().get(0).getUser());

        // then
        assertEquals("https://www.baseUrl.com/opt/0/", accessInfo);
    }

    @Test
    public void getPublicDNS_NonEmpty() {
        // given
        String accessInfo = getAccessInfo();
        // when
        String result = handler.getPublicDNS(accessInfo);
        // then
        assertEquals(
                "Public DNS for EC2 instance: ec2-54-174-206-114.compute-1.amazonaws.com  ",
                result);
    }

    @Test
    public void getIPAddress_NonEmpty() {
        // given
        String accessInfo = getAccessInfo();
        // when
        String result = handler.getIPAddress(accessInfo);
        // then
        assertEquals("54.174.206.114", result);
    }

    @Test
    public void getKeyPairName_NonEmpty() {
        // given
        String accessInfo = getAccessInfo();
        // when
        String result = handler.getKeyPairName(accessInfo);
        // then
        assertEquals("Key pair name: us-east-1", result);
    }

    @Test
    public void getPublicDNS_Empty() {
        // given
        String accessInfo = getUnableAccessInfo();
        // when
        String result = handler.getPublicDNS(accessInfo);
        // then
        assertEquals("", result);
    }

    @Test
    public void getIPAddress_Empty() {
        // given
        String accessInfo = getUnableAccessInfo();
        // when
        String result = handler.getIPAddress(accessInfo);
        // then
        assertEquals("", result);
    }

    @Test
    public void getKeyPairName_Empty() {
        // given
        String accessInfo = getUnableAccessInfo();
        // when
        String result = handler.getKeyPairName(accessInfo);
        // then
        assertEquals("", result);
    }

    @Test
    public void hasNullAccessInfo_False() {
        // given
        String accessInfo = getAccessInfo();
        // when
        boolean result = handler.hasNullAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void hasNullAccessInfo_True() {
        // given
        String accessInfo = getUnableAccessInfo();
        // when
        boolean result = handler.hasNullAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isUsableAWSAccessInfo_False() {
        // given
        String accessInfo = getUnableAccessInfo();
        // when
        boolean result = handler.isUsableAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isUsableAWSAccessInfo_True() {
        // given
        String accessInfo = getAccessInfo();
        // when
        boolean result = handler.isUsableAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isUsableAWSAccessInfo_NullAccessInfo() {
        // given
        String accessInfo = "";
        // when
        boolean result = handler.isUsableAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }
    
    @Test
    public void isAWSAccessInfo_False() {
        // given
        String accessInfo = "Key pair name besdev3";
        // when
        boolean result = handler.isAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isAWSAccessInfo_True() {
        // given
        String accessInfo = getAccessInfo();
        // when
        boolean result = handler.isAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    private String getAccessInfo() {
        String accessInfo = "Public DNS for EC2 instance: ec2-54-174-206-114.compute-1.amazonaws.com  Key pair name: us-east-1";
        return accessInfo;
    }

    private String getUnableAccessInfo() {
        String accessInfo = "amazonaws.com";
        return accessInfo;
    }
}
