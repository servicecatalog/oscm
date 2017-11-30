/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 16.02.15 08:13
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.Conversation;
import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.ui.beans.BillingContactBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.PaymentAndBillingVisibleBean;
import org.oscm.ui.beans.PaymentInfoBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants;
import org.oscm.ui.dialog.mp.subscriptionwizard.UpgradeWizardConversation;
import org.oscm.ui.dialog.mp.subscriptionwizard.UpgradeWizardModel;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.triggerprocess.TriggerProcessesService;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.model.UdaRow;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeWizardConversationTest {

    @Spy
    private UpgradeWizardConversation bean = new UpgradeWizardConversation();
    private UpgradeWizardModel model;

    @Mock
    private UserBean userBean;
    @Mock
    private TriggerProcessesService triggerProcessService;
    @Mock
    private SubscriptionDetailsService subscriptionDetailsService;
    @Mock
    private UiDelegate uiDelegate;
    @Mock
    private AccountService accountingService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private MenuBean menuBean;
    @Mock
    private SessionBean sessionBean;
    @Mock
    private Conversation conversation;
    @Mock
    private PaymentInfoBean paymentInfoBean;
    @Mock
    private BillingContactBean billingConactBean;

    @Before
    public void setUp() throws Exception {
        model = new UpgradeWizardModel();

        when(
                triggerProcessService
                        .getAllWaitingForApprovalTriggerProcessesBySubscriptionId(anyString()))
                .thenReturn(new Response(Collections.emptyList()));
        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.DEACTIVATED));

        when(uiDelegate.getViewLocale()).thenReturn(Locale.ENGLISH);

        bean.setUserBean(userBean);
        bean.setTriggerProcessService(triggerProcessService);
        bean.setSubscriptionDetailsService(subscriptionDetailsService);
        bean.setUi(uiDelegate);
        bean.setModel(model);
        bean.setAccountingService(accountingService);
        bean.setSubscriptionService(subscriptionService);
        bean.setMenuBean(menuBean);
        bean.setSessionBean(sessionBean);
        bean.setConversation(conversation);
        PaymentAndBillingVisibleBean paymentAndBillingVisibleBean = new PaymentAndBillingVisibleBean();
        paymentAndBillingVisibleBean.setUserBean(userBean);
        paymentAndBillingVisibleBean.setBillingContactBean(billingConactBean);
        bean.setPaymentAndBillingVisibleBean(paymentAndBillingVisibleBean);
        bean.setPaymentInfoBean(paymentInfoBean);
        List<UdaRow> udaRows = new ArrayList<>();
        model.setSubscriptionUdaRows(udaRows);
    }

    @Test
    public void selectService() {
        // given
        doNothing().when(bean).addMessage(any(FacesMessage.Severity.class),
                anyString());
        initDataForSelectService();
        // when
        String result = bean.selectService();
        // then
        assertEquals("", result);
    }

    @Test
    public void selectServiceHIDE_PAYMENT_INFO() {
        // given
        initDataForSelectServiceWithoutParams();
        doNothing().when(bean).addMessage(any(FacesMessage.Severity.class),
                anyString());
        doReturn(true).when(subscriptionService).isPaymentInfoHidden();
        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
        bean.getModel().setSubscription(subscription);

        // when
        String result = bean.selectService();
        // then
        assertEquals("success", result);
    }

    @org.junit.Test
    public void testIsPaymentInfoVisible() throws Exception {
        // given
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(
                Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndAllowedToSubscribe()))
                .thenReturn(Boolean.TRUE);
        List<VOPaymentInfo> paymentInfosForSubscription = new ArrayList<>();
        paymentInfosForSubscription.add(new VOPaymentInfo());
        doReturn(paymentInfosForSubscription).when(paymentInfoBean)
                .getPaymentInfosForSubscription(anyLong(),
                        eq(accountingService));
        Collection<VOPaymentType> paymentTypes = new ArrayList<>();
        paymentTypes.add(new VOPaymentType());
        doReturn(paymentTypes).when(bean).getEnabledPaymentTypes();
        initDataForSelectService();
        // when

        boolean paymentInfoVisible = bean.isPaymentInfoVisible();

        // then
        assertTrue(paymentInfoVisible);

        // given
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(
                Boolean.FALSE);
        paymentInfoVisible = bean.isPaymentInfoVisible();
        assertTrue(paymentInfoVisible);

    }

    @Test
    public void upgrade_SYNC_OK() throws Exception {
        // given
        VOSubscription voSubscription = new VOSubscription();

        voSubscription.setStatus(SubscriptionStatus.ACTIVE);
        voSubscription.setSubscriptionId("test");
        model.setSubscription(this.givenSubscription(true));
        model.setService(new Service(new VOService()));

        PricedParameterRow param = decorateWithPricedParameterRow();

        doReturn(voSubscription).when(subscriptionService).upgradeSubscription(
                any(VOSubscriptionDetails.class), any(VOService.class),
                any(VOPaymentInfo.class), any(VOBillingContact.class),
                anyListOf(VOUda.class));

        // when
        String result = bean.upgrade();

        // then
        verify(uiDelegate, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_UPGRADED,
                "test");
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, result);
        verify(param).rewriteEncryptedValues();
    }

    private PricedParameterRow decorateWithPricedParameterRow() {
        List<PricedParameterRow> paramList = new ArrayList<>();
        PricedParameterRow param = spy(new PricedParameterRow());
        doReturn(new VOParameter()).when(param).getParameter();
        VOParameterDefinition paramDef = new VOParameterDefinition();
        doReturn(paramDef).when(param).getParameterDefinition();
        paramDef.setParameterId("parameterId");
        paramDef.setValueType(ParameterValueType.PWD);
        paramList.add(param);
        model.setServiceParameters(paramList);
        return param;
    }

    @Test
    public void upgrade_ASYNC_OK() throws Exception {
        // given
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setSubscriptionId("test");
        voSubscription.setStatus(SubscriptionStatus.PENDING_UPD);

        model.setSubscription(this.givenSubscription(true));
        model.setService(new Service(new VOService()));
        PricedParameterRow param = decorateWithPricedParameterRow();

        doReturn(voSubscription).when(subscriptionService).upgradeSubscription(
                any(VOSubscriptionDetails.class), any(VOService.class),
                any(VOPaymentInfo.class), any(VOBillingContact.class),
                anyListOf(VOUda.class));

        // when
        String result = bean.upgrade();

        // then
        verify(uiDelegate, times(1))
                .handle(SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_UPGRADED,
                        "test");
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, result);
        verify(param).rewriteEncryptedValues();
    }

    /**
     * Bugfix 9921
     */
    @Test
    public void initializeSubscription() throws Exception {
        // given

        VOSubscriptionDetails subscription = givenSubscription(false);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(
                subscriptionDetailsService.getSubscriptionDetails(
                        eq("subscription_id"), anyString())).thenReturn(
                new Response(subscriptionDetails));
        when(bean.getUi().getViewLocale()).thenReturn(Locale.ENGLISH);
        when(
                accountingService.getAvailablePaymentTypesFromOrganization(Long
                        .valueOf(anyLong()))).thenReturn(
                new HashSet<VOPaymentType>());
        model.setSelectedSubscriptionId("subscription_id");

        // when
        bean.upgradeSubscription();

        // then
        assertNotNull(bean.getPaymentInfosForSubscription());
        assertTrue(bean.getPaymentInfosForSubscription().isEmpty());
        // verify no subscription owner is added
        assertEquals(0, model.getSubscriptionOwners().size());
    }

    @Test
    public void initializeSubscription_Bug10481_SubMgr() throws Exception {
        // given
        VOSubscriptionDetails subscription = givenSubscription(false);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(
                subscriptionDetailsService.getSubscriptionDetails(
                        eq("subscription_id"), anyString())).thenReturn(
                new Response(subscriptionDetails));
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin())).thenReturn(
                Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager()))
                .thenReturn(Boolean.TRUE);
        model.setSelectedSubscriptionId("subscription_id");

        // when
        bean.upgradeSubscription();

        // then
        assertEquals(Boolean.TRUE.booleanValue(), model.isReportIssueAllowed());
    }

    @Test
    public void testNext() {
        // given
        model.setReadOnlyParams(false);
        // when
        bean.next();
        // then
        assertTrue(model.isReadOnlyParams());
    }

    @Test
    public void initializeSubscription_Bug10481_Admin() throws Exception {
        // given

        VOSubscriptionDetails subscription = givenSubscription(false);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(
                subscriptionDetailsService.getSubscriptionDetails(
                        eq("subscription_id"), anyString())).thenReturn(
                new Response(subscriptionDetails));
        when(bean.getUi().getViewLocale()).thenReturn(Locale.ENGLISH);
        when(userBean.isLoggedInAndAdmin()).thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager()))
                .thenReturn(Boolean.FALSE);
        model.setSelectedSubscriptionId("subscription_id");

        // when
        bean.upgradeSubscription();

        // then
        assertEquals(Boolean.TRUE.booleanValue(), model.isReportIssueAllowed());
    }

    @Test
    public void initializeSubscription_addSubscriptionOwners_withStoredOwner()
            throws Exception {
        // given
        VOSubscriptionDetails subscription = givenSubscription(false);
        subscription.setOwnerId("owner");
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        List<VOUserDetails> userList = new ArrayList<>();
        userList.add(prepareVOUserDetails_SubMgr("owner", true));
        userList.add(prepareVOUserDetails_OrgAdmin("admin"));
        userList.add(prepareVOUserDetails_SubMgr("notowner", false));
        subscriptionDetails.setUsersForOrganization(userList);
        when(
                subscriptionDetailsService.getSubscriptionDetails(
                        eq("subscription_id"), anyString())).thenReturn(
                new Response(subscriptionDetails));
        when(bean.getUi().getViewLocale()).thenReturn(Locale.ENGLISH);
        model.setSelectedSubscriptionId("subscription_id");

        // when
        bean.upgradeSubscription();

        // then
        assertEquals(2, model.getSubscriptionOwners().size());
        assertEquals("owner", model.getSubscriptionOwners().get(0).getUserId());
        assertEquals("admin", model.getSubscriptionOwners().get(1).getUserId());
        assertEquals("owner", model.getSelectedOwner().getUserId());
        assertEquals("owner", model.getStoredOwner().getUserId());
    }

    @Test
    public void initializeSubscription_addSubscriptionOwners_withoutStoredOwner()
            throws Exception {
        // given
        VOSubscriptionDetails subscription = givenSubscription(false);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        List<VOUserDetails> userList = new ArrayList<>();
        userList.add(prepareVOUserDetails_SubMgr("owner", true));
        userList.add(prepareVOUserDetails_SubMgr("notowner", false));
        subscriptionDetails.setUsersForOrganization(userList);
        when(
                subscriptionDetailsService.getSubscriptionDetails(
                        eq("subscription_id"), anyString())).thenReturn(
                new Response(subscriptionDetails));
        when(bean.getUi().getViewLocale()).thenReturn(Locale.ENGLISH);
        model.setSelectedSubscriptionId("subscription_id");

        // when
        bean.upgradeSubscription();

        // then
        assertEquals(1, model.getSubscriptionOwners().size());
        assertEquals("owner", model.getSubscriptionOwners().get(0).getUserId());
        assertNull(model.getSelectedOwner());
        assertNull(model.getStoredOwner());
    }

    @Test
    public void initializeSubscription_NoPaymentInfoNoBillingContact()
            throws Exception {
        // given

        VOSubscriptionDetails subscription = givenSubscription(true);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(
                subscriptionDetailsService.getSubscriptionDetails(
                        eq("subscription_id"), anyString())).thenReturn(
                new Response(subscriptionDetails));
        when(bean.getUi().getViewLocale()).thenReturn(Locale.ENGLISH);
        model.setSelectedSubscriptionId("subscription_id");

        // when
        bean.upgradeSubscription();

        // then
        assertNotNull(bean.getPaymentInfosForSubscription());
        assertTrue(bean.getPaymentInfosForSubscription().isEmpty());
    }

    /**
     * Helper methods.
     */

    private VOSubscriptionDetails givenSubscription(boolean isFree) {

        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
        subscription.setSubscriptionId("test");
        subscription.setSubscribedService(new VOService());
        if (isFree) {
            // set price model to subscription
            VOPriceModel priceModel = new VOPriceModel();
            priceModel.setType(PriceModelType.FREE_OF_CHARGE);
            subscription.setPriceModel(priceModel);
        } else {
            // set price model to subscription
            VOPriceModel priceModel = new VOPriceModel();
            priceModel.setType(PriceModelType.PER_UNIT);
            subscription.setPriceModel(priceModel);

            prepareBillingContact(subscription, 10000);
            preparePaymentInfo(subscription, 10001);

        }

        return subscription;
    }

    private void preparePaymentInfo(VOSubscriptionDetails subscription, long key) {
        VOPaymentInfo paymentInfo = new VOPaymentInfo();
        paymentInfo.setKey(key);
        subscription.setPaymentInfo(paymentInfo);
    }

    private void prepareBillingContact(VOSubscriptionDetails subscription,
            long key) {
        VOBillingContact billingContact = new VOBillingContact();
        billingContact.setKey(key);
        subscription.setBillingContact(billingContact);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void upgradeSubscription_ObjectNotFoundException() throws Exception {

        // given
        when(
                subscriptionDetailsService.getSubscriptionDetails(anyString(),
                        anyString())).thenThrow(new ObjectNotFoundException());

        // when
        bean.upgradeSubscription();

        // then expect exception not to be caught
    }

    @Test(expected = OperationNotPermittedException.class)
    public void upgradeSubscription_OperationNotPermittedException()
            throws Exception {

        // given
        when(
                subscriptionDetailsService.getSubscriptionDetails(anyString(),
                        anyString())).thenThrow(
                new OperationNotPermittedException());

        // when
        bean.upgradeSubscription();

        // then expect exception not to be caught
    }

    @Test(expected = OrganizationAuthoritiesException.class)
    public void upgradeSubscription_OrganizationAuthoritiesException()
            throws Exception {

        // given
        when(
                subscriptionDetailsService.getSubscriptionDetails(anyString(),
                        anyString())).thenThrow(
                new OrganizationAuthoritiesException());

        // when
        bean.upgradeSubscription();

        // then expect exception not to be caught
    }

    private POSubscriptionDetails givenPOSubscriptionDetails() {
        List<VOUdaDefinition> udaDefinitions = new LinkedList<>();
        VOUdaDefinition subscriptionDefinition = new VOUdaDefinition();
        subscriptionDefinition.setTargetType(UdaBean.CUSTOMER_SUBSCRIPTION);
        udaDefinitions.add(subscriptionDefinition);

        VOUdaDefinition customerDefinition = new VOUdaDefinition();
        customerDefinition.setTargetType(UdaBean.CUSTOMER);
        udaDefinitions.add(customerDefinition);

        POSubscriptionDetails subscriptionDetails = new POSubscriptionDetails();
        subscriptionDetails.setUdasDefinitions(udaDefinitions);
        subscriptionDetails.setStatus(SubscriptionStatus.ACTIVE);
        return subscriptionDetails;
    }

    private VOUserDetails prepareVOUserDetails_SubMgr(String userId,
            boolean isSubMgr) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        if (isSubMgr) {
            user.addUserRole(UserRoleType.SUBSCRIPTION_MANAGER);
        }
        return user;
    }

    private VOUserDetails prepareVOUserDetails_OrgAdmin(String userId) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(userId);
        user.addUserRole(UserRoleType.ORGANIZATION_ADMIN);
        return user;
    }

    private void initDataForSelectService() {
        VOService vo = new VOService();
        vo.setConfiguratorUrl("http://");
        List<VOParameter> parameters = new ArrayList<>();
        VOParameter parameter = new VOParameter();
        VOParameterDefinition parameterDefinition = new VOParameterDefinition();
        parameterDefinition.setMandatory(true);
        parameterDefinition.setKey(100L);
        parameter.setParameterDefinition(parameterDefinition);
        parameters.add(parameter);
        vo.setParameters(parameters);
        vo.setKey(1000L);
        Service s = new Service(vo);
        model.setService(s);
        List<PricedParameterRow> pricedParameterRows = new ArrayList<>();
        PricedParameterRow pricedParameterRow = new PricedParameterRow();
        pricedParameterRows.add(pricedParameterRow);
        model.setServiceParameters(pricedParameterRows);
        model.getUseExternalConfigurator();
        model.setHideExternalConfigurator(false);
    }

    private void initDataForSelectServiceWithoutParams() {
        VOService vo = new VOService();
        vo.setConfiguratorUrl("http://");
        List<VOParameter> parameters = new ArrayList<>();
        vo.setParameters(parameters);
        vo.setKey(1000L);
        Service s = new Service(vo);
        bean.getModel().setService(s);
        List<PricedParameterRow> pricedParameterRows = new ArrayList<>();
        PricedParameterRow pricedParameterRow = new PricedParameterRow();
        pricedParameterRows.add(pricedParameterRow);
        bean.getModel().setServiceParameters(pricedParameterRows);
        bean.getModel().getUseExternalConfigurator();
        bean.getModel().setHideExternalConfigurator(false);
    }
}
