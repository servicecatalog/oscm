/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 9, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.mockito.Matchers.any;
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

import org.junit.Before;
import org.junit.Test;

import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.subscriptionservice.dao.SubscriptionDao;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * @author Ma
 * 
 */
public class SubscriptionServiceBeanAsyncSubscriptionTest extends
        SubscriptionServiceMockBase {
    private SubscriptionServiceBean bean;
    private DataService ds;
    private LocalizerServiceLocal localizer;
    private CommunicationServiceLocal commService;
    private SubscriptionDao subscriptionDao;
    private static final String SUBSCRIPTIIO_ID = "163";
    private static final String ORGANIZATION_ID = "126";
    private static final String INSTANCE_ID = "123";
    private VOInstanceInfo instanceInfo;
    private Subscription subscription;
    private List<PlatformUser> platformUsers;
    private ModifyAndUpgradeSubscriptionBean modifyAndUpgradeSubscriptionBean;
    private ManageSubscriptionBean manageSubscriptionBean;
    private ApplicationServiceLocal appManager;
    private static final String ja_text = "提供できませんでした";
    private static final String en_text = "test for english";
    private static final String orgId = "org1";

    @Before
    public void setup() throws Exception {

        subscriptionDao = mock(SubscriptionDao.class);
        bean = spy(new SubscriptionServiceBean());
        ds = mock(DataService.class);
        localizer = mock(LocalizerServiceLocal.class);
        commService = mock(CommunicationServiceLocal.class);
        modifyAndUpgradeSubscriptionBean = mock(ModifyAndUpgradeSubscriptionBean.class);
        bean.modUpgBean = modifyAndUpgradeSubscriptionBean;
        manageSubscriptionBean = mock(ManageSubscriptionBean.class);
        bean.manageBean = manageSubscriptionBean;
        bean.dataManager = ds;
        bean.localizer = localizer;
        bean.commService = commService;
        bean.modUpgBean.dataManager = ds;
        bean.manageBean.dataManager = ds;
        appManager = mock(ApplicationServiceLocal.class);
        bean.appManager = appManager;
        bean.tqs = mock(TaskQueueServiceLocal.class);

        instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId(INSTANCE_ID);
        instanceInfo.setAccessInfo("LOGIN");
        instanceInfo.setBaseUrl("http://localhost:8080");
        instanceInfo.setLoginPath("/login");
        subscription = prepareSubscription();
        platformUsers = new ArrayList<PlatformUser>();
        platformUsers.add(preparePlatformUser(1, "ja", "u1"));
        platformUsers.add(preparePlatformUser(2, "en", "u2"));

        doReturn(subscriptionDao).when(bean).getSubscriptionDao();
        doReturn(subscription).when(bean.manageBean).findSubscription(
                anyString(), anyString());
        doNothing().when(bean.manageBean).validateTechnoloyProvider(
                subscription);
        doReturn(Boolean.TRUE).when(bean.modUpgBean).isPaymentValidOrFree(
                subscription);
        doNothing().when(bean).activateSubscriptionFirstTime(subscription);
    }

    private Subscription prepareSubscription() {
        TechnicalProduct technicalProduct = new TechnicalProduct();
        Product product = new Product();
        PriceModel priceModel = new PriceModel();
        product.setPriceModel(priceModel);
        product.setTechnicalProduct(technicalProduct);
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setProduct(product);
        return subscription;
    }

    @Test
    public void completeAsyncSubscription_checkIfProductInstanceIdExists()
            throws Exception {

        // given
        doReturn(Boolean.FALSE).when(subscriptionDao)
                .checkIfProductInstanceIdExists(instanceInfo.getInstanceId(),
                        subscription.getProduct().getTechnicalProduct());

        // when
        bean.completeAsyncSubscription(SUBSCRIPTIIO_ID, ORGANIZATION_ID,
                instanceInfo);
        // then
        verify(subscriptionDao, times(1)).checkIfProductInstanceIdExists(
                instanceInfo.getInstanceId(),
                subscription.getProduct().getTechnicalProduct());
    }

    @Test
    public void completeAsyncSubscription_baseUrlAndLoginPathNull()
            throws Exception {

        // given
        instanceInfo.setBaseUrl(null);
        instanceInfo.setLoginPath(null);
        doReturn(Boolean.FALSE).when(subscriptionDao)
                .checkIfProductInstanceIdExists(instanceInfo.getInstanceId(),
                        subscription.getProduct().getTechnicalProduct());

        // when
        bean.completeAsyncSubscription(SUBSCRIPTIIO_ID, ORGANIZATION_ID,
                instanceInfo);
        // then
        verify(subscriptionDao, times(1)).checkIfProductInstanceIdExists(
                instanceInfo.getInstanceId(),
                subscription.getProduct().getTechnicalProduct());
    }

    @Test
    public void completeAsyncSubscription_baseUrlAndLoginPathEmpty()
            throws Exception {

        // given
        instanceInfo.setBaseUrl("");
        instanceInfo.setLoginPath("");
        doReturn(Boolean.FALSE).when(subscriptionDao)
                .checkIfProductInstanceIdExists(instanceInfo.getInstanceId(),
                        subscription.getProduct().getTechnicalProduct());

        // when
        bean.completeAsyncSubscription(SUBSCRIPTIIO_ID, ORGANIZATION_ID,
                instanceInfo);
        // then
        verify(subscriptionDao, times(1)).checkIfProductInstanceIdExists(
                instanceInfo.getInstanceId(),
                subscription.getProduct().getTechnicalProduct());
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void completeAsyncSubscription_baseUrlNull() throws Exception {

        // given
        instanceInfo.setBaseUrl("");
        doReturn(Boolean.FALSE).when(subscriptionDao)
                .checkIfProductInstanceIdExists(instanceInfo.getInstanceId(),
                        subscription.getProduct().getTechnicalProduct());

        // when
        bean.completeAsyncSubscription(SUBSCRIPTIIO_ID, ORGANIZATION_ID,
                instanceInfo);
        // then
        verify(subscriptionDao, times(1)).checkIfProductInstanceIdExists(
                instanceInfo.getInstanceId(),
                subscription.getProduct().getTechnicalProduct());
    }

    @Test(expected = TechnicalServiceOperationException.class)
    public void completeAsyncSubscription_baseUrlIllegal() throws Exception {

        // given
        instanceInfo.setBaseUrl("/");
        doReturn(Boolean.FALSE).when(subscriptionDao)
                .checkIfProductInstanceIdExists(instanceInfo.getInstanceId(),
                        subscription.getProduct().getTechnicalProduct());

        // when
        bean.completeAsyncSubscription(SUBSCRIPTIIO_ID, ORGANIZATION_ID,
                instanceInfo);
        // then
        verify(subscriptionDao, times(1)).checkIfProductInstanceIdExists(
                instanceInfo.getInstanceId(),
                subscription.getProduct().getTechnicalProduct());
    }

    @Test(expected = ValidationException.class)
    public void completeAsyncSubscription_validationException()
            throws Exception {

        // given
        doReturn(Boolean.TRUE).when(subscriptionDao)
                .checkIfProductInstanceIdExists(instanceInfo.getInstanceId(),
                        subscription.getProduct().getTechnicalProduct());
        // when
        bean.completeAsyncSubscription(SUBSCRIPTIIO_ID, ORGANIZATION_ID,
                instanceInfo);
    }

    @Test
    public void sendSubscriptionAbortEmail() throws Exception {
        // given
        when(ds.getCurrentUser()).thenReturn(platformUsers.get(0)).thenReturn(
                platformUsers.get(1));
        doReturn(ja_text).when(localizer).getLocalizedTextFromDatabase(
                eq("ja"), anyLong(), any(LocalizedObjectTypes.class));
        doReturn(en_text).when(localizer).getLocalizedTextFromDatabase(
                eq("en"), anyLong(), any(LocalizedObjectTypes.class));

        // when
        bean.sendSubscriptionAbortEmail(subscription.getSubscriptionId(),
                orgId, subscription, platformUsers);

        // then
        verify(bean.commService, times(1)).sendMail(
                eq(platformUsers.get(0)),
                eq(EmailType.SUBSCRIPTION_INVALIDATED),
                eq(new Object[] { subscription.getSubscriptionId(), orgId,
                        ja_text }), any(Marketplace.class));
        verify(bean.commService, times(1)).sendMail(
                eq(platformUsers.get(1)),
                eq(EmailType.SUBSCRIPTION_INVALIDATED),
                eq(new Object[] { subscription.getSubscriptionId(), orgId,
                        en_text }), any(Marketplace.class));
    }

    @Test
    public void sendAbortAsyncModifySubscriptionEmail() throws Exception {
        // given
        doReturn(platformUsers).when(bean.manageBean)
                .getCustomerAndTechnicalProductAdminForSubscription(
                        subscription);
        when(ds.getCurrentUser()).thenReturn(platformUsers.get(0)).thenReturn(
                platformUsers.get(1));
        doReturn(ja_text).when(localizer).getLocalizedTextFromDatabase(
                eq("ja"), anyLong(), any(LocalizedObjectTypes.class));
        doReturn(en_text).when(localizer).getLocalizedTextFromDatabase(
                eq("en"), anyLong(), any(LocalizedObjectTypes.class));

        // when
        bean.sendAbortAsyncModifySubscriptionEmail(subscription, orgId,
                new ArrayList<VOLocalizedText>());

        // then
        verify(bean.commService, times(1)).sendMail(
                eq(platformUsers.get(0)),
                eq(EmailType.SUBSCRIPTION_PARAMETER_MODIFY_ABORT),
                eq(new Object[] { subscription.getSubscriptionId(), orgId,
                        ja_text }), any(Marketplace.class));
        verify(bean.commService, times(1)).sendMail(
                eq(platformUsers.get(1)),
                eq(EmailType.SUBSCRIPTION_PARAMETER_MODIFY_ABORT),
                eq(new Object[] { subscription.getSubscriptionId(), orgId,
                        en_text }), any(Marketplace.class));
    }

    private PlatformUser preparePlatformUser(long key, String locale,
            String userId) {
        PlatformUser platformUser = new PlatformUser();
        platformUser.setKey(key);
        platformUser.setLocale(locale);
        platformUser.setUserId(userId);

        return platformUser;
    }
}
