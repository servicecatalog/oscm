/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.mp.subscriptionDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.triggerprocess.TriggerProcessesService;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SubscriptionMigrationException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.json.JsonConverter;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.BillingContactBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.OrganizationBean;
import org.oscm.ui.beans.PaymentAndBillingVisibleBean;
import org.oscm.ui.beans.PaymentInfoBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionsHelper;
import org.oscm.ui.dialog.mp.userGroups.SubscriptionUnitCtrl;
import org.oscm.ui.dialog.mp.userGroups.SubscriptionUnitModel;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.model.User;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;

import com.google.common.collect.Sets;

public class ManageSubscriptionCtrlTest {

    private static final long ANY_PARAMETER_KEY = 234523;
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_SUBSCRIPTION_NOT_AVAILABLE = "subscriptionNotAccessible";
    private static final String OUTCOME_DEASSIGNED_USER_OR_ERROR = "deassignedUserOrError";
    private static final String JSON_STRING = "someJsonString";
    private static final String JSON_STRING_WITH_QUOTATION = "someJson'String";
    private static final String BACK = "back";
    private static final String OUTCOME_SUBSCRIPTION_NEED_APPROVAL = "subscriptionNeedApproval";
    private final boolean SUBSCRIPTION_FREE = true;
    private ManageSubscriptionCtrl ctrl;
    SubscriptionService subscriptionService;
    SubscriptionServiceInternal subscriptionServiceInternal;
    private BillingContactBean billingContactBean;
    private PaymentInfoBean paymentInfoBean;
    private HttpServletRequest httpRequest;
    private VOSubscriptionDetails sub;
    private List<VOTriggerProcess> waitingForApprovalTriggerProcesses;
    private SubscriptionDetailsService subscriptionDetailsService;
    private ManageSubscriptionModel model;
    private TriggerProcessesService triggerProcessService;
    private UserBean userBean;
    private SubscriptionsHelper subscriptionsHelper;
    private JsonConverter jsonConverter;
    private SubscriptionUnitCtrl subscriptionUnitCtrl;
    private SubscriptionUnitModel subscriptionUnitModel;
    private UserGroupService userGroupService;
    private OperatorService operatorService;
    private PaymentAndBillingVisibleBean paymentAndBillingVisibleBean;

    @Before
    public void setup() throws SaaSApplicationException {

        ctrl = spy(new ManageSubscriptionCtrl());

        SessionBean session = mock(SessionBean.class);
        triggerProcessService = mock(TriggerProcessesService.class);
        subscriptionServiceInternal = mock(SubscriptionServiceInternal.class);
        subscriptionService = mock(SubscriptionService.class);
        billingContactBean = mock(BillingContactBean.class);
        paymentAndBillingVisibleBean = mock(PaymentAndBillingVisibleBean.class);
        ctrl.ui = mock(UiDelegate.class);
        subscriptionsHelper = mock(SubscriptionsHelper.class);
        paymentInfoBean = mock(PaymentInfoBean.class);
        subscriptionDetailsService = mock(SubscriptionDetailsService.class);
        httpRequest = mock(HttpServletRequest.class);
        model = new ManageSubscriptionModel();
        userBean = mock(UserBean.class);
        subscriptionsHelper = mock(SubscriptionsHelper.class);
        jsonConverter = mock(JsonConverter.class);
        subscriptionUnitCtrl = mock(SubscriptionUnitCtrl.class);
        subscriptionUnitModel = mock(SubscriptionUnitModel.class);
        userGroupService = mock(UserGroupService.class);

        ctrl.setModel(model);
        ctrl.setTriggerProcessService(triggerProcessService);
        ctrl.setSubscriptionService(subscriptionService);
        ctrl.setMenuBean(mock(MenuBean.class));
        ctrl.setSessionBean(session);
        ctrl.setBillingContactBean(billingContactBean);
        ctrl.setPaymentInfoBean(paymentInfoBean);
        ctrl.setUserBean(userBean);
        ctrl.setSubscriptionDetailsService(subscriptionDetailsService);
        ctrl.setSubscriptionServiceInternal(subscriptionServiceInternal);
        ctrl.setJsonConverter(jsonConverter);
        ctrl.setSubscriptionsHelper(subscriptionsHelper);
        subscriptionUnitCtrl.setModel(subscriptionUnitModel);
        ctrl.setSubscriptionUnitCtrl(subscriptionUnitCtrl);
        ctrl.setUserGroupService(userGroupService);
        ctrl.setPaymentAndBillingVisibleBean(paymentAndBillingVisibleBean);

        waitingForApprovalTriggerProcesses = new ArrayList<>();
        when(triggerProcessService
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId(
                        anyString()))
                                .thenReturn(new Response(
                                        waitingForApprovalTriggerProcesses));

        when(session.getSelectedSubscriptionId()).thenReturn("subscriptionId");
        when(ctrl.ui.getRequest()).thenReturn(httpRequest);

        model.setSubscription(new VOSubscriptionDetails());
        model.setService(new Service(new VOService()));
        model.getSubscription().setSubscriptionId("test");

        VOSubscriptionDetails subscription = givenSubscription(
                SUBSCRIPTION_FREE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(subscriptionDetailsService.getSubscriptionDetails(anyString(),
                anyString())).thenReturn(new Response(subscriptionDetails));
        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.ACTIVE));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.GERMAN);
        doReturn(Boolean.FALSE).when(subscriptionDetailsService)
                .isUserAssignedToTheSubscription(anyLong(), anyLong());
        stubMessageBundles();
        List<UdaRow> udaRows = new ArrayList<>();
        model.setSubscriptionUdaRows(udaRows);
    }

    private VOSubscriptionDetails givenSubscription(boolean isFree) {

        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
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

    private VOSubscriptionDetails givenSubscriptionWithParameters(long key,
            String value) {
        // create new service parameter
        List<VOParameter> parameters = new LinkedList<>();
        VOParameterDefinition parameterDef = new VOParameterDefinition();
        parameterDef.setParameterId("paramId");
        parameterDef.setValueType(ParameterValueType.PWD);
        parameterDef.setModificationType(ParameterModificationType.STANDARD);
        VOParameter parameter = new VOParameter(parameterDef);
        // STANDARD parameters should be configurable
        parameter.setConfigurable(true);
        parameter.setKey(key);
        parameter.setValue(value);
        parameters.add(parameter);

        // create service
        VOService service = new VOService();
        service.setParameters(parameters);

        // set service to subscription
        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
        subscription.setSubscribedService(service);

        return subscription;
    }

    private void preparePaymentInfo(VOSubscriptionDetails subscription,
            long key) {
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

    private void stubMessageBundles() {
        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH);
        when(ctrl.ui.getFacesContext()).thenReturn(contextStub);

        ResourceBundleStub testBundle = new ResourceBundleStub();
        ((ApplicationStub) contextStub.getApplication())
                .setResourceBundleStub(testBundle);

        testBundle.addResource(
                SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                "Subscription state warning {0}");
        testBundle.addResource("SubscriptionStatus.SUSPENDED_UPD",
                "suspended update");
        testBundle.addResource("SubscriptionStatus.PENDING", "pending");
        testBundle.addResource("SubscriptionStatus.PENDING_UPD",
                "pending update");
        testBundle.addResource("SubscriptionStatus.SUSPENDED",
                "suspended - please update your payment information");
    }

    private void modify_assertRefreshModel(boolean refreshModellExpected)
            throws Exception {
        int numberExpectedRefreshMethodCall = refreshModellExpected ? 1 : 0;
        verify(ctrl, times(numberExpectedRefreshMethodCall))
                .refreshModel(any(VOSubscriptionDetails.class));
    }

    private void modify_assertUISuccessMessage(boolean successMsgExpected) {
        int numberExpectedSuccessMethodCall = successMsgExpected ? 1 : 0;
        verify(ctrl.ui, times(numberExpectedSuccessMethodCall)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_SAVED,
                model.getSubscription().getSubscriptionId());
    }

    private void modifySubscription(SubscriptionStatus status,
            long parameterKey, String value)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException, SubscriptionStateException,
            OrganizationAuthoritiesException,
            MandatoryCustomerUdaMissingException {
        sub = givenSubscriptionWithParameters(parameterKey, value);
        sub.setSubscriptionId("test");
        sub.setStatus(status);

        when(subscriptionService.modifySubscription(
                any(VOSubscriptionDetails.class), anyListOf(VOParameter.class),
                anyListOf(VOUda.class))).thenReturn(sub);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(ctrl).refreshOrgAndSubscriptionUdasInModel(anyString());
    }

    private User prepareSubOwner(String userId, boolean isSelected) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId(userId);
        User owner = new User(userDetails);
        owner.setFirstName("FirstName");
        owner.setLastName("LastName");
        owner.setOwnerSelected(isSelected);
        return owner;
    }

    private void assertSubscriptionUdaRows() {
        List<UdaRow> refreshedSubscriptionUdaRows = model
                .getSubscriptionUdaRows();
        assertEquals(1, refreshedSubscriptionUdaRows.size());
        assertEquals(UdaBean.CUSTOMER_SUBSCRIPTION, refreshedSubscriptionUdaRows
                .get(0).getUdaDefinition().getTargetType());
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

    private void setSubscriptionOwners(boolean isOwner1Select,
            boolean isOwner2Select) {
        List<User> subOwners = new ArrayList<>();
        subOwners.add(prepareSubOwner("owner1", isOwner1Select));
        subOwners.add(prepareSubOwner("owner2", isOwner2Select));
        model.setSubscriptionOwners(subOwners);
    }

    private void checkSubscriptionOwner(String ownerUserId) {
        if (ownerUserId != null)
            for (User user : model.getSubscriptionOwners()) {
                if (user.isOwnerSelected()) {
                    assertEquals(Boolean.TRUE, Boolean.valueOf(
                            user.getUserId().equalsIgnoreCase(ownerUserId)));
                }
            }
    }

    private ValueChangeEvent initChangeOwnerEvent(String userId) {
        ValueChangeEvent event = mock(ValueChangeEvent.class);
        when(event.getNewValue()).thenReturn("true");
        HtmlSelectOneRadio radio = mock(HtmlSelectOneRadio.class);
        when(event.getComponent()).thenReturn(radio);
        when(radio.getSubmittedValue()).thenReturn("true");
        HtmlInputHidden input = mock(HtmlInputHidden.class);
        when(input.getValue()).thenReturn(userId);
        when(input.getRendererType()).thenReturn("javax.faces.Hidden");
        List<UIComponent> componentList = new ArrayList<>();
        componentList.add(input);
        when(radio.getChildren()).thenReturn(componentList);
        return event;
    }

    private void assertParametersModified(long key, String value) {
        assertEquals(
                model.getServiceParameters().get(0).getParameter().getKey(),
                model.getSubscriptionParameters().get(0).getParameter()
                        .getKey());
        assertEquals(
                model.getServiceParameters().get(0).getParameter().getValue(),
                model.getSubscriptionParameters().get(0).getParameter()
                        .getValue());
        assertEquals(
                model.getServiceParameters().get(0).getParameter().getKey(),
                key);
        assertEquals(
                model.getServiceParameters().get(0).getParameter().getValue(),
                value);
        assertEquals(model.getService().getVO().getParameters().get(0).getKey(),
                model.getServiceParameters().get(0).getParameter().getKey());
        assertEquals(
                model.getService().getVO().getParameters().get(0).getValue(),
                model.getServiceParameters().get(0).getParameter().getValue());
    }

    @Test
    public void modify_subscriptionSuspended() throws Exception {
        // given
        when(subscriptionService.modifySubscription(
                any(VOSubscriptionDetails.class), anyListOf(VOParameter.class),
                anyListOf(VOUda.class))).thenReturn(null);
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        modify_assertRefreshModel(false);
        modify_assertUISuccessMessage(false);
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void modify_subscriptionModified_SYNC() throws Exception {
        // given
        modifySubscription(SubscriptionStatus.ACTIVE, ANY_PARAMETER_KEY,
                "ANYTHING");
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        verify(ctrl.ui, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_SAVED,
                "test");
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isConfigDirty()));
        modify_assertRefreshModel(true);
        modify_assertUISuccessMessage(true);
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void modify_subscriptionModified_ASYNC() throws Exception {
        // given
        modifySubscription(SubscriptionStatus.PENDING_UPD, ANY_PARAMETER_KEY,
                "ANYTHING");
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        verify(ctrl.ui, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_SAVED,
                "test");
        verify(ctrl.ui, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_SAVED,
                "test");
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isShowStateWarning()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isReadOnlyParams()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isAsyncModified()));
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        assertEquals(
                JSFUtils.getText(
                        SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                        new Object[] { "pending update" }),
                model.getStateWarning());
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void modify_ASYNC_bug10821() throws Exception {
        // given
        modifySubscription(SubscriptionStatus.PENDING_UPD, ANY_PARAMETER_KEY,
                "ANYTHING");
        model.setConfigurationChanged(true);
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        ctrl.modify();

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isConfigurationChanged()));
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void modify_NoSubscriptionOwnerSelected_withoutStoredOwner()
            throws Exception {
        // given
        modifySubscription(SubscriptionStatus.ACTIVE, ANY_PARAMETER_KEY,
                "ANYTHING");
        model.setSelectedOwner(null);
        model.setStoredOwner(null);
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        modify_assertRefreshModel(true);
        modify_assertUISuccessMessage(true);
        assertNull(sub.getOwnerId());
        assertNull(model.getStoredOwner());
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
        model.setSubscriptionParameters(paramList);
        return param;
    }

    @Test
    public void modify_NoSubscriptionOwnerSelected_withStoredOwner()
            throws Exception {
        // given
        modifySubscription(SubscriptionStatus.ACTIVE, ANY_PARAMETER_KEY,
                "ANYTHING");
        model.setSelectedOwner(null);
        User owner = prepareSubOwner("owner", true);
        model.setStoredOwner(owner);
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        modify_assertRefreshModel(true);
        verify(ctrl.ui, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_SAVED,
                "test");
        assertNull(sub.getOwnerId());
        assertNull(model.getStoredOwner());
        assertEquals(Boolean.FALSE, Boolean.valueOf(owner.isOwnerSelected()));
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void modify_SelectSubscriptionOwner_SYNC() throws Exception {
        // given
        modifySubscription(SubscriptionStatus.ACTIVE, ANY_PARAMETER_KEY,
                "ANYTHING");
        model.setStoredOwner(prepareSubOwner("owner1", false));
        model.setSelectedOwner(prepareSubOwner("owner2", true));
        VOSubscriptionDetails subTemp = new VOSubscriptionDetails();
        model.setSubscription(subTemp);
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        modify_assertRefreshModel(true);
        modify_assertUISuccessMessage(true);
        assertEquals("owner2", subTemp.getOwnerId());
        assertEquals("owner2", model.getStoredOwner().getUserId());
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getStoredOwner().isOwnerSelected()));
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void modify_SelectSubscriptionOwner_ASYNC() throws Exception {
        // given
        modifySubscription(SubscriptionStatus.PENDING_UPD, ANY_PARAMETER_KEY,
                "ANYTHING");
        model.setStoredOwner(prepareSubOwner("owner1", false));
        model.setSelectedOwner(prepareSubOwner("owner2", true));
        VOSubscriptionDetails subTemp = new VOSubscriptionDetails();
        subTemp.setSubscriptionId("test");
        model.setSubscription(subTemp);
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        String outcome = ctrl.modify();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, outcome);
        verify(ctrl.ui, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_SAVED,
                "test");
        assertEquals("owner2", subTemp.getOwnerId());
        assertEquals("owner1", model.getStoredOwner().getUserId());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getStoredOwner().isOwnerSelected()));
        verify(param).rewriteEncryptedValues();
    }

    @Test
    public void refreshModel() throws Exception {
        // given
        VOSubscriptionDetails subscription = mock(VOSubscriptionDetails.class);

        VOServiceEntry voService = mock(VOServiceEntry.class);
        doReturn(voService).when(subscription).getSubscribedService();

        when(subscriptionService.modifySubscription(
                any(VOSubscriptionDetails.class), anyListOf(VOParameter.class),
                anyListOf(VOUda.class))).thenReturn(subscription);

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(ctrl).refreshOrgAndSubscriptionUdasInModel(anyString());

        // when
        ctrl.refreshModel(subscription);

        // then
        verify(ctrl, times(1))
                .refreshSubscriptionParametersInModel(subscription);
        verify(ctrl, times(1))
                .refreshOrgAndSubscriptionUdasInModel(anyString());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isConfigurationChanged()));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isConfigDirty()));
    }

    @Test
    public void refreshSubscriptionParametersInModel() {
        // given
        VOSubscriptionDetails givenSubscription = givenSubscriptionWithParameters(
                ANY_PARAMETER_KEY, "ANY THING");

        // when
        ctrl.refreshSubscriptionParametersInModel(givenSubscription);

        // then
        List<VOParameter> givenSubscriptionParameters = givenSubscription
                .getSubscribedService().getParameters();
        List<PricedParameterRow> refreshedModelServiceParameters = ctrl
                .getModel().getServiceParameters();
        List<PricedParameterRow> refreshedModelSubscriptionParameters = ctrl
                .getModel().getSubscriptionParameters();
        assertEquals(givenSubscriptionParameters.size(),
                refreshedModelSubscriptionParameters.size());
        assertEquals(givenSubscriptionParameters.get(0).getKey(),
                refreshedModelSubscriptionParameters.get(0).getParameter()
                        .getKey());
        assertEquals(givenSubscriptionParameters.size(),
                refreshedModelServiceParameters.size());
        assertEquals(givenSubscriptionParameters.get(0).getKey(),
                refreshedModelServiceParameters.get(0).getParameter().getKey());
    }

    @Test
    public void updateRoles_userNotAssignedToTheSubscription()
            throws Exception {
        // given
        final String OUTCOME_MODIFICATION_ERROR = "concurrentModificationError";
        ManageSubscriptionModel model = new ManageSubscriptionModel();
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setKey(8988L);
        User user = new User(voUserDetails);
        List<User> assignedUsers = new ArrayList<>();
        assignedUsers.add(user);
        model.setAssignedUsers(assignedUsers);
        VOSubscriptionDetails sub = new VOSubscriptionDetails();
        sub.setKey(9988L);
        model.setSubscription(sub);
        ctrl.setModel(model);

        // when
        String result = ctrl.updateRoles();

        // then
        assertEquals(OUTCOME_MODIFICATION_ERROR, result);
    }

    @Test
    public void refreshOrgAndSubscriptionUdasInModel() throws Exception {
        // given
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        SubscriptionDetailsService subscriptionDetailService = mock(
                SubscriptionDetailsService.class);

        String subscriptionId = anyString();
        when(subscriptionDetailService.getSubscriptionDetails(subscriptionId,
                anyString()))
                        .thenReturn(new Response(givenPOSubscriptionDetails()));

        // when
        ctrl.refreshOrgAndSubscriptionUdasInModel(subscriptionId);

        // then
        assertSubscriptionUdaRows();
    }

    @Test
    public void initialize_succeed() throws Exception {
        // given
        doNothing().when(ctrl).initializeSubscription(anyString());

        // when
        ctrl.initialize();

        // then
        verify(ctrl.ui, never())
                .handleException(any(SaaSApplicationException.class));

        assertTrue(model.isSubscriptionExisting());
        assertFalse(model.isReadOnlyParams());
    }

    @Test
    public void getInitialize_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(ctrl)
                .initializeSubscription(anyInt());

        // when
        ctrl.initialize();

        // then
        verify(ctrl.ui, times(1))
                .handleException(any(ObjectNotFoundException.class));
    }

    /**
     * Bugfix 9921
     */
    @Test
    public void initializeSubscription() throws Exception {
        // given
        SubscriptionDetailsService ss = mock(SubscriptionDetailsService.class);
        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(ss.getSubscriptionDetails(eq("subscription_id"), anyString()))
                .thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        verify(ctrl, times(1)).initPaymentInfo();
        // verify no subscription owner is added
        assertEquals(0, model.getSubscriptionOwners().size());
    }

    @Test
    public void initializeSubscription_Bug10481_SubMgr() throws Exception {
        // given
        SubscriptionDetailsService ss = mock(SubscriptionDetailsService.class);

        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(ss.getSubscriptionDetails(eq("subscription_id"), anyString()))
                .thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin()))
                .thenReturn(Boolean.FALSE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager()))
                .thenReturn(Boolean.TRUE);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getIsReportIssueAllowed()));
    }

    @Test
    public void initializeSubscription_Bug10481_Admin() throws Exception {
        // given
        SubscriptionDetailsService ss = mock(SubscriptionDetailsService.class);

        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(ss.getSubscriptionDetails(eq("subscription_id"), anyString()))
                .thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(Boolean.valueOf(userBean.isLoggedInAndAdmin()))
                .thenReturn(Boolean.TRUE);
        when(Boolean.valueOf(userBean.isLoggedInAndSubscriptionManager()))
                .thenReturn(Boolean.FALSE);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getIsReportIssueAllowed()));
    }

    @Test
    public void initializeSubscription_Bug11075_Admin() throws Exception {
        // given
        SubscriptionDetailsService ss = mock(SubscriptionDetailsService.class);
        //
        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(ss.getSubscriptionDetails(eq("subscription_id"), anyString()))
                .thenReturn(new Response(subscriptionDetails));
        List<VOTriggerProcess> triggerProcessList = new ArrayList<>();
        VOTriggerProcess triggerProcess = new VOTriggerProcess();
        triggerProcessList.add(triggerProcess);
        Response response = mock(Response.class);
        when(triggerProcessService
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId(
                        any(String.class))).thenReturn(response);
        when(response.getResultList(VOTriggerProcess.class))
                .thenReturn(triggerProcessList);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        verify(ctrl.ui, times(1)).handleProgress();
    }

    @Test
    public void initializeSubscription_addSubscriptionOwners_withStoredOwner()
            throws Exception {
        // given
        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        subscription.setOwnerId("owner");
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        List<VOUserDetails> userList = new ArrayList<>();
        userList.add(prepareVOUserDetails_SubMgr("owner", true));
        userList.add(prepareVOUserDetails_OrgAdmin("admin"));
        userList.add(prepareVOUserDetails_SubMgr("notowner", false));
        subscriptionDetails.setUsersForOrganization(userList);
        when(subscriptionDetailsService
                .getSubscriptionDetails(eq("subscription_id"), anyString()))
                        .thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        assertEquals(2, model.getSubscriptionOwners().size());
        assertEquals("owner", model.getSubscriptionOwners().get(0).getUserId());
        assertEquals("admin", model.getSubscriptionOwners().get(1).getUserId());
        assertEquals("owner", model.getSelectedOwner().getUserId());
        assertEquals("owner", model.getStoredOwner().getUserId());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isNoSubscriptionOwner()));
    }

    @Test
    public void initializeSubscription_addSubscriptionOwners_withoutStoredOwner()
            throws Exception {
        // given
        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        List<VOUserDetails> userList = new ArrayList<>();
        userList.add(prepareVOUserDetails_SubMgr("owner", true));
        userList.add(prepareVOUserDetails_SubMgr("notowner", false));
        subscriptionDetails.setUsersForOrganization(userList);
        when(subscriptionDetailsService
                .getSubscriptionDetails(eq("subscription_id"), anyString()))
                        .thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        assertEquals(1, model.getSubscriptionOwners().size());
        assertEquals("owner", model.getSubscriptionOwners().get(0).getUserId());
        assertNull(model.getSelectedOwner());
        assertNull(model.getStoredOwner());
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isNoSubscriptionOwner()));
    }

    @Test
    public void initializeSubscription_NoPaymentInfoNoBillingContact()
            throws Exception {
        // given
        SubscriptionDetailsService ss = mock(SubscriptionDetailsService.class);

        VOSubscriptionDetails subscription = givenSubscription(
                SUBSCRIPTION_FREE);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        when(ss.getSubscriptionDetails(eq("subscription_id"), anyString()))
                .thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.initializeSubscription("subscription_id");

        // then
        verify(ctrl, times(1)).initPaymentInfo();
    }

    @Test
    public void initPaymentInfo() {
        // given
        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        model.setSubscription(subscription);
        // when
        ctrl.initPaymentInfo();

        // then
        verify(billingContactBean).getBillingContacts();
        verify(paymentInfoBean).getPaymentInfosForSubscription();
        verify(billingContactBean, times(1))
                .setSelectedBillingContactKey(eq(Long.valueOf(
                        model.getSubscription().getBillingContact().getKey())));
        verify(paymentInfoBean, times(1))
                .setSelectedPaymentInfoForSubscriptionKey(eq(Long.valueOf(
                        model.getSubscription().getPaymentInfo().getKey())));

    }

    @Test
    public void initPaymentInfo_NoPaymentInfoNoBillingContact() {
        // given
        VOSubscriptionDetails subscription = givenSubscription(
                SUBSCRIPTION_FREE);
        model.setSubscription(subscription);

        // when
        ctrl.initPaymentInfo();

        // then
        verify(billingContactBean).getBillingContacts();
        verify(paymentInfoBean).getPaymentInfosForSubscription();
        verifyNoMoreInteractions(billingContactBean, paymentInfoBean);
    }

    @Test
    public void leavePaymentTab_ok() {
        // given
        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        model.setSubscription(subscription);
        // when
        String result = ctrl.leavePaymentTab();
        // then
        verify(billingContactBean, times(1))
                .setSelectedBillingContactKey(eq(Long.valueOf(
                        model.getSubscription().getBillingContact().getKey())));
        boolean isDirty = model.isDirty();
        assertFalse(isDirty);
        assertNull(result);
    }

    @Test
    public void setPopupTargetSelectOwners_NoSubscriptionOwners() {
        // given
        model.setSubscriptionOwners(new ArrayList<User>());
        // when
        String result = ctrl.setPopupTargetSelectOwners();
        // then
        assertEquals("dontOpenModalDialog", result);
    }

    @Test
    public void setPopupTargetSelectOwners_NoSelectOwner() {
        // given
        model.setSelectedOwner(null);
        setSubscriptionOwners(true, false);

        // when
        String result = ctrl.setPopupTargetSelectOwners();

        // then
        for (User user : model.getSubscriptionOwners()) {
            assertEquals(Boolean.FALSE, Boolean.valueOf(user.isSelected()));
        }
        assertEquals(Boolean.FALSE, Boolean.valueOf(ctrl.isOwnerSelected()));
        assertNull(result);
    }

    @Test
    public void setPopupTargetSelectOwners_WithSubscriptionOwner() {
        // given
        model.setSelectedOwner(prepareSubOwner("owner1", true));
        setSubscriptionOwners(false, false);

        // when
        String result = ctrl.setPopupTargetSelectOwners();

        // then
        checkSubscriptionOwner("owner1");
        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isOwnerSelected()));
        assertNull(result);
    }

    @Test
    public void setPopupTargetSelectOwners_WithWrongSubscriptionOwner() {
        // given
        model.setSelectedOwner(prepareSubOwner("owner1", true));
        setSubscriptionOwners(false, true);

        // when
        String result = ctrl.setPopupTargetSelectOwners();

        // then
        checkSubscriptionOwner("owner1");
        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isOwnerSelected()));
        assertNull(result);
    }

    @Test
    public void refreshOwner_ClickNoOwner() {
        // given
        model.setNoSubscriptionOwner(true);
        model.setSelectedOwner(new User(new VOUserDetails()));
        // when
        ctrl.refreshOwner();
        // then
        assertNull(model.getSelectedOwner());
    }

    @Test
    public void refreshOwner_ClickOwner() {
        // given
        model.setNoSubscriptionOwner(false);
        model.setStoredOwner(prepareSubOwner("owner1", true));
        // when
        ctrl.refreshOwner();
        // then
        assertNotNull(model.getSelectedOwner());
        assertEquals("owner1", model.getSelectedOwner().getUserId());
    }

    @Test
    public void refreshSelectedOwnerName_NoUser() {
        // when
        ctrl.refreshSelectedOwnerName(null);

        // then
        assertEquals(
                JSFUtils.getText("subscription.noOwner", new Object[] { "" }),
                model.getSelectedOwnerName());
    }

    @Test
    public void refreshSelectedOwnerName() {
        // given
        doReturn(Boolean.FALSE).when(ctrl.ui).isNameSequenceReversed();

        // when
        ctrl.refreshSelectedOwnerName(prepareSubOwner("owner1", true));

        // then
        assertEquals("FirstName LastName(owner1)",
                model.getSelectedOwnerName());
    }

    @Test
    public void refreshSelectedOwnerName_ReverseNameSequence() {
        // given
        doReturn(Boolean.TRUE).when(ctrl.ui).isNameSequenceReversed();

        // when
        ctrl.refreshSelectedOwnerName(prepareSubOwner("owner1", true));

        // then
        assertEquals("LastName FirstName(owner1)",
                model.getSelectedOwnerName());
    }

    @Test
    public void updateSelectedOwner() {
        // given
        model.setSelectedOwner(prepareSubOwner("owner1", true));
        setSubscriptionOwners(false, true);

        // when
        ctrl.updateSelectedOwner();

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.getSelectedOwner()
                .getUserId().equalsIgnoreCase("owner2")));
    }

    @Test
    public void selectedOwnerChanged() {
        // given
        setSubscriptionOwners(false, true);
        ValueChangeEvent event = initChangeOwnerEvent("owner1");
        // when
        ctrl.selectedOwnerChanged(event);

        // then
        checkSubscriptionOwner("owner1");
        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isOwnerSelected()));
    }

    @Test
    public void selectedOwnerChangedToNoOwner() {
        // given
        setSubscriptionOwners(false, true);
        ValueChangeEvent event = initChangeOwnerEvent(" ");
        // when
        ctrl.selectedOwnerChanged(event);

        // then
        for (User user : model.getSubscriptionOwners()) {
            assertEquals(Boolean.FALSE,
                    Boolean.valueOf(user.isOwnerSelected()));
        }
        assertEquals(Boolean.FALSE, Boolean.valueOf(ctrl.isOwnerSelected()));
    }

    @Test
    public void validateSubscriptionAccessible_subscriptionInvalid()
            throws Exception {
        // given
        when(ctrl.getSubscriptionsHelper())
                .thenReturn(new SubscriptionsHelper());
        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.INVALID));

        // when
        boolean result = ctrl.getSubscriptionsHelper()
                .validateSubscriptionStatus(model.getSubscription(),
                        subscriptionDetailsService);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void validateSubscriptionAccessible_subscriptionDeactivated()
            throws Exception {
        // given
        when(ctrl.getSubscriptionsHelper())
                .thenReturn(new SubscriptionsHelper());
        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.DEACTIVATED));

        // when
        boolean result = ctrl.getSubscriptionsHelper()
                .validateSubscriptionStatus(model.getSubscription(),
                        subscriptionDetailsService);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void subscriptionDetailsOperations_subscriptionInvalid()
            throws Exception {
        // given
        model.getSubscription().setSubscriptionId("subscriptionId");
        ctrl.setSubscriptionsHelper(new SubscriptionsHelper());

        when(subscriptionDetailsService.loadSubscriptionStatus(anyLong()))
                .thenReturn(new Response(SubscriptionStatus.INVALID));

        // when
        String assignUsers = ctrl.assignUsers();
        String deassignUser = ctrl.deassignUser();
        String modify = ctrl.modify();
        String savePayment = ctrl.savePayment();
        String unsubscribe = ctrl.unsubscribe();
        String updateRoles = ctrl.updateRoles();

        // then
        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, assignUsers);
        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, deassignUser);
        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, modify);
        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, savePayment);
        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, unsubscribe);
        assertEquals(OUTCOME_SUBSCRIPTION_NOT_AVAILABLE, updateRoles);
    }

    @Test
    public void subscriptionDetailsOperations_subscriptionNeedApproval()
            throws Exception {
        // given
        model.getSubscription().setSubscriptionId("subscriptionId");
        when(Boolean.valueOf(
                subscriptionService.unsubscribeFromService(anyString())))
                        .thenReturn(Boolean.FALSE);

        SessionService sessionService = mock(SessionService.class);
        ctrl.setSessionService(sessionService);

        // when
        String unsubscribe = ctrl.unsubscribe();

        // then
        assertEquals(OUTCOME_SUBSCRIPTION_NEED_APPROVAL, unsubscribe);
    }

    @Test
    public void assignUsers_subscriptionAccessible() throws Exception {
        // when
        String result = ctrl.assignUsers();

        // then
        assertEquals(BACK, result);
        verify(subscriptionsHelper, times(1)).validateSubscriptionStatus(
                any(VOSubscriptionDetails.class),
                any(SubscriptionDetailsService.class));
    }

    @Test
    public void deassignUser_subscriptionAccessible() throws Exception {
        // given
        model.getSubscription().setSubscriptionId("subscriptionId");

        // when
        String result = ctrl.deassignUser();

        // then
        assertEquals(OUTCOME_DEASSIGNED_USER_OR_ERROR, result);
        verify(subscriptionsHelper, times(1)).validateSubscriptionStatus(
                model.getSubscription(), subscriptionDetailsService);
    }

    @Test
    public void savePayment_subscriptionAccessible() throws Exception {
        // given
        when(subscriptionService.modifySubscriptionPaymentData(
                any(VOSubscription.class), any(VOBillingContact.class),
                any(VOPaymentInfo.class)))
                        .thenReturn(new VOSubscriptionDetails());
        // when
        String result = ctrl.savePayment();

        // then
        assertEquals(OUTCOME_SUCCESS, result);
        verify(subscriptionsHelper, times(1)).validateSubscriptionStatus(
                any(VOSubscriptionDetails.class),
                any(SubscriptionDetailsService.class));
    }

    @Test
    public void updateRoles_subscriptionAccessible() throws Exception {
        // given
        List<User> users = new ArrayList<>();
        model.setAssignedUsers(users);

        // when
        String result = ctrl.updateRoles();

        // then
        assertEquals(BACK, result);
        assertEquals(null, model.getSubscription());
        verify(subscriptionsHelper, times(1)).validateSubscriptionStatus(
                any(VOSubscriptionDetails.class),
                any(SubscriptionDetailsService.class));
    }

    @Test
    public void actionLoadIframe_setShowExternalConfigurator()
            throws Exception {
        // given
        List<User> users = new ArrayList<>();
        model.setAssignedUsers(users);
        doReturn(JSON_STRING).when(jsonConverter)
                .getServiceParametersAsJsonString(
                        anyListOf(PricedParameterRow.class), anyBoolean(),
                        anyBoolean());
        // when
        ctrl.actionLoadIframe();

        // then
        assertTrue(model.getShowExternalConfigurator());
    }

    @Test
    public void actionLoadIframe_success() throws Exception {
        // given
        List<User> users = new ArrayList<>();
        model.setAssignedUsers(users);
        doReturn(JSON_STRING).when(jsonConverter)
                .getServiceParametersAsJsonString(
                        anyListOf(PricedParameterRow.class), anyBoolean(),
                        anyBoolean());
        // when
        String result = ctrl.actionLoadIframe();

        // then
        assertNull(result);
        assertTrue(model.isLoadIframe());
        assertFalse(model.getHideExternalConfigurator());
        assertEquals(JSON_STRING, model.getServiceParametersAsJSONString());
    }

    @Test
    public void actionLoadIframe_successWithSinleQuotation() throws Exception {
        // given
        List<User> users = new ArrayList<>();
        model.setAssignedUsers(users);
        doReturn(JSON_STRING_WITH_QUOTATION).when(jsonConverter)
                .getServiceParametersAsJsonString(
                        anyListOf(PricedParameterRow.class), anyBoolean(),
                        anyBoolean());
        // when
        ctrl.actionLoadIframe();

        // then
        assertEquals("someJson\\'String",
                model.getServiceParametersAsJSONString());
    }

    @Test
    public void actionLoadIframe_jsonError() throws Exception {
        // given
        List<User> users = new ArrayList<>();
        model.setAssignedUsers(users);
        doReturn(null).when(jsonConverter).getServiceParametersAsJsonString(
                anyListOf(PricedParameterRow.class), anyBoolean(),
                anyBoolean());

        // when
        String result = ctrl.actionLoadIframe();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_ERROR, result);
        assertFalse(model.isLoadIframe());
        assertTrue(model.getHideExternalConfigurator());
        assertNull(model.getServiceParametersAsJSONString());
    }

    @Test
    public void setStateWarningAndTabDisabled_ACTIVE() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        doReturn(Boolean.FALSE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertFalse(model.isUsersTabDisabled());
        assertFalse(model.isCfgTabDisabled());
        assertFalse(model.isPayTabDisabled());
        assertFalse(model.isUpgTabDisabled());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
    }

    @Test
    public void setStateWarningAndTabDisabled_EXPIRED() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.EXPIRED);
        doReturn(Boolean.FALSE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertTrue(model.isUsersTabDisabled());
        assertTrue(model.isCfgTabDisabled());
        assertTrue(model.isPayTabDisabled());
        assertFalse(model.isUpgTabDisabled());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
    }

    @Test
    public void setStateWarningAndTabDisabled_PENDING() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.PENDING);
        doReturn(Boolean.FALSE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertTrue(model.isShowStateWarning());
        assertTrue(model.isUsersTabDisabled());
        assertTrue(model.isCfgTabDisabled());
        assertFalse(model.isPayTabDisabled());
        assertTrue(model.isUpgTabDisabled());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        assertEquals(JSFUtils.getText(
                SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                new Object[] { "pending" }), model.getStateWarning());
    }

    @Test
    public void setStateWarningAndTabDisabled_PENDING_UPD() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.PENDING_UPD);
        doReturn(Boolean.FALSE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertTrue(model.isShowStateWarning());
        assertTrue(model.isUsersTabDisabled());
        assertTrue(model.isCfgTabDisabled());
        assertFalse(model.isPayTabDisabled());
        assertTrue(model.isUpgTabDisabled());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        assertEquals(
                JSFUtils.getText(
                        SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                        new Object[] { "pending update" }),
                model.getStateWarning());
    }

    @Test
    public void setStateWarningAndTabDisabled_SUSPENDED() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.SUSPENDED);
        doReturn(Boolean.FALSE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertTrue(model.isUsersTabDisabled());
        assertTrue(model.isCfgTabDisabled());
        assertFalse(model.isPayTabDisabled());
        assertFalse(model.isUpgTabDisabled());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
    }

    @Test
    public void setStateWarningAndTabDisabled_SUSPENDED_UPD() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.SUSPENDED_UPD);
        doReturn(Boolean.FALSE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertTrue(model.isShowStateWarning());
        assertTrue(model.isUsersTabDisabled());
        assertTrue(model.isCfgTabDisabled());
        assertFalse(model.isPayTabDisabled());
        assertTrue(model.isUpgTabDisabled());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        assertEquals(
                JSFUtils.getText(
                        SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                        new Object[] { "suspended update" }),
                model.getStateWarning());
    }

    @Test
    public void setStateWarningAndTabDisabled_waitingForReply_true_ACTIVE() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        doReturn(Boolean.TRUE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isPayTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
    }

    @Test
    public void setStateWarningAndTabDisabled_waitingForReply_true_EXPIRED() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.EXPIRED);
        doReturn(Boolean.TRUE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isPayTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
    }

    @Test
    public void setStateWarningAndTabDisabled_waitingForReply_true_PENDING() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.PENDING);
        doReturn(Boolean.TRUE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isShowStateWarning()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isPayTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        assertEquals(JSFUtils.getText(
                SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                new Object[] { "pending" }), model.getStateWarning());
    }

    @Test
    public void setStateWarningAndTabDisabled_waitingForReply_true_PENDING_UPD() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.PENDING_UPD);
        doReturn(Boolean.TRUE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isShowStateWarning()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isPayTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        assertEquals(
                JSFUtils.getText(
                        SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                        new Object[] { "pending update" }),
                model.getStateWarning());
    }

    @Test
    public void setStateWarningAndTabDisabled_waitingForReply_true_SUSPENDED() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.SUSPENDED);
        doReturn(Boolean.TRUE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isPayTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
    }

    @Test
    public void setStateWarningAndTabDisabled_waitingForReply_true_SUSPENDED_UPD() {
        // given
        POSubscriptionDetails subscriptionDetail = givenPOSubscriptionDetails();
        subscriptionDetail.setStatus(SubscriptionStatus.SUSPENDED_UPD);
        doReturn(Boolean.TRUE).when(ctrl).checkTriggerProcessForSubscription(
                subscriptionDetail.getSubscription());

        // when
        ctrl.setStateWarningAndTabDisabled(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isShowStateWarning()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUsersTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isCfgTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isPayTabDisabled()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isUpgTabDisabled()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.isUnsubscribeButtonDisabled()));
        assertEquals(
                JSFUtils.getText(
                        SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING,
                        new Object[] { "suspended update" }),
                model.getStateWarning());
    }

    @Test
    public void checkTriggerProcessForSubscription_true() {

        // given
        VOSubscriptionDetails subscriptionDetail = new VOSubscriptionDetails();
        VOTriggerProcess triggerProcess = new VOTriggerProcess();
        waitingForApprovalTriggerProcesses.add(triggerProcess);

        // when
        boolean result = ctrl
                .checkTriggerProcessForSubscription(subscriptionDetail);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void reload_bug10504() throws Exception {
        // given

        VOSubscriptionDetails subscription = givenSubscription(
                !SUBSCRIPTION_FREE);
        subscription.setOwnerId("owner1");
        subscription.setSubscriptionId("subscription_id");
        subscription.setKey(10L);
        POSubscriptionDetails subscriptionDetails = givenPOSubscriptionDetails();
        subscriptionDetails.setSubscription(subscription);
        List<VOUserDetails> userList = new ArrayList<>();
        userList.add(prepareVOUserDetails_SubMgr("owner1", true));
        subscriptionDetails.setUsersForOrganization(userList);
        when(subscriptionDetailsService.getSubscriptionDetails(eq(10L),
                anyString())).thenReturn(new Response(subscriptionDetails));
        when(ctrl.ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        setSubscriptionOwners(true, false);
        model.setSubscription(subscription);
        model.setNoSubscriptionOwner(true);
        when(ctrl.getSubscriptionUnitCtrl().getModel())
                .thenReturn(subscriptionUnitModel);
        // when
        ctrl.reload();
        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isNoSubscriptionOwner()));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isConfigDirty()));
    }

    @Test
    public void changeTwiceParameters_bug10833() throws Exception {
        // given
        modifySubscription(SubscriptionStatus.ACTIVE, 1, "10");
        PricedParameterRow param = decorateWithPricedParameterRow();

        // when
        ctrl.modify();

        // then
        assertParametersModified(1, "10");

        // given
        modifySubscription(SubscriptionStatus.ACTIVE, 2, "20");

        // when
        ctrl.modify();

        // then
        assertParametersModified(2, "20");
        verify(param).rewriteEncryptedValues();
    }

    @SuppressWarnings("boxing")
    @Test
    public void getNoPaymentTypeAvailableMSG_Admin() {
        // given
        Service service = new Service(new VOService());
        service.setSubscribable(true);
        model.setService(service);
        when(userBean.isLoggedInAndAdmin()).thenReturn(Boolean.TRUE);

        // when
        String result = ctrl.getNoPaymentTypeAvailableMSG();

        // then
        assertEquals(result,
                SubscriptionDetailsCtrlConstants.MESSAGE_NO_PAYMENT_TYPE_ENABLED);
    }

    @SuppressWarnings("boxing")
    @Test
    public void getNoPaymentTypeAvailableMSG_NonAdmin() {
        // given
        Service service = new Service(new VOService());
        service.setSubscribable(true);
        model.setService(service);
        when(userBean.isLoggedInAndAdmin()).thenReturn(Boolean.FALSE);

        // when
        String result = ctrl.getNoPaymentTypeAvailableMSG();

        // then
        assertEquals(result,
                SubscriptionDetailsCtrlConstants.MESSAGE_NO_PAYMENT_TYPE_AVAILABLE);
    }

    private void mockPriceModelWithRolePrices(
            VOSubscriptionDetails voSubscription) {
        VOPriceModel pm = mock(VOPriceModel.class);
        doReturn(pm).when(voSubscription).getPriceModel();
        mockRolePrices(pm);
    }

    private void mockRolePrices(VOPriceModel pm) {
        List<VOPricedRole> roles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            VOPricedRole role = new VOPricedRole();
            role.setRole(new VORoleDefinition());
            role.setPricePerUser(BigDecimal.ONE);
            roles.add(role);
        }
        doReturn(roles).when(pm).getRoleSpecificUserPrices();
    }

    @SuppressWarnings("boxing")
    @Test
    public void initializePriceModel_Subscription() {
        // given
        VOSubscriptionDetails voSubscription = mock(
                VOSubscriptionDetails.class);
        mockPriceModelWithRolePrices(voSubscription);

        // when
        ctrl.initializePriceModelForSubscription(voSubscription);

        // then
        assertNotNull(model.getRoleSpecificPrices());
        assertEquals(Boolean.FALSE, model.getRoleSpecificPrices().isEmpty());
    }

    @Test
    public void shouldOwnerWarningBeShownTest_UnitAdministrator() {
        // given
        User owner = prepareSubOwnerWithRole("owner", true,
                UserRoleType.UNIT_ADMINISTRATOR);
        model.setSelectedOwner(owner);
        model.setSelectedOwnerName("owner");
        // when
        boolean showOwnerWarning = ctrl.shouldOwnerWarningBeShown();
        // then
        assertTrue(showOwnerWarning);
        verify(ctrl).setOwnerWarningMessage(
                BaseBean.WARNING_UNIT_NOT_SELECTED_UNIT_ADMIN,
                new Object[] { "owner" });
    }

    @Test
    public void shouldOwnerWarningBeShownTest_SubscriptionManager() {
        // given
        User owner = prepareSubOwnerWithRole("owner", true,
                UserRoleType.SUBSCRIPTION_MANAGER);
        model.setSelectedOwner(owner);
        model.setSelectedOwnerName("owner");
        model.getSubscription().setUnitName("unit1");
        model.getSubscription().setUnitKey(1L);
        // when
        boolean showOwnerWarning = ctrl.shouldOwnerWarningBeShown();
        // then
        assertTrue(showOwnerWarning);
        verify(ctrl).setOwnerWarningMessage(BaseBean.WARNING_OWNER_IS_SUB_MAN,
                new Object[] { "unit1", "owner" });
    }

    @Test
    public void shouldOwnerWarningBeShownTest_OrgAdmin() {
        // given
        User owner = prepareSubOwnerWithRole("owner", true,
                UserRoleType.ORGANIZATION_ADMIN);
        model.setSelectedOwner(owner);
        // when
        boolean showOwnerWarning = ctrl.shouldOwnerWarningBeShown();
        // then
        assertFalse(showOwnerWarning);
    }

    @Test
    public void shouldOwnerWarningBeShownTest_UnitAdminOfSameUnit() {
        // given
        User owner = prepareSubOwnerWithRole("owner", true,
                UserRoleType.UNIT_ADMINISTRATOR);
        model.setSelectedOwner(owner);
        model.getSubscription().setUnitName("unit1");
        model.getSubscription().setUnitKey(1L);
        List<POUserGroup> usergroups = new ArrayList<>();
        usergroups.add(prepareUserGroup(1L, "unit1"));
        when(userGroupService.getUserGroupsForUserWithRole(anyLong(),
                anyLong())).thenReturn(usergroups);

        // when
        boolean showOwnerWarning = ctrl.shouldOwnerWarningBeShown();
        // then
        assertFalse(showOwnerWarning);
    }

    @Test
    public void shouldOwnerWarningBeShownTest_UnitAdminOfAnotherUnit() {
        // given
        User owner = prepareSubOwnerWithRole("owner", true,
                UserRoleType.UNIT_ADMINISTRATOR);
        model.setSelectedOwner(owner);
        model.setSelectedOwnerName("owner");
        model.getSubscription().setUnitName("unit2");
        model.getSubscription().setUnitKey(2L);
        List<POUserGroup> usergroups = new ArrayList<>();
        usergroups.add(prepareUserGroup(1L, "unit1"));
        when(userGroupService.getUserGroupsForUserWithRole(anyLong(),
                anyLong())).thenReturn(usergroups);

        // when
        boolean showOwnerWarning = ctrl.shouldOwnerWarningBeShown();
        // then
        assertTrue(showOwnerWarning);
        verify(ctrl).setOwnerWarningMessage(
                BaseBean.WARNING_OWNER_NOT_A_UNIT_ADMIN,
                new Object[] { "unit2", "owner" });
    }

    @Test
    public void testInitializeSubscriptionOwners() {
        // given
        UserBean userBean = new UserBean();
        userBean = spy(userBean);
        ctrl.setUserBean(userBean);

        OrganizationBean organizationBean = mock(OrganizationBean.class);
        VOOrganization org = new VOOrganization();
        org.setKey(1L);
        when(organizationBean.getOrganization()).thenReturn(org);
        userBean.setOrganizationBean(organizationBean);

        operatorService = mock(OperatorService.class);
        ctrl.setOperatorService(operatorService);

        List<User> users = new ArrayList<>();
        List<VOUserDetails> voUsers = new ArrayList<>();
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setUserRoles(
                Sets.newHashSet(UserRoleType.SUBSCRIPTION_MANAGER));
        voUsers.add(voUserDetails);
        User user = new User(voUserDetails);

        users.add(user);
        model = spy(model);
        ctrl.setModel(model);
        model.setSubscriptionOwners(new ArrayList<User>());
        when(ctrl.getOperatorService()
                .getSubscriptionOwnersForAssignment(anyLong()))
                        .thenReturn(voUsers);
        // when
        String result = ctrl.initializeSubscriptionOwners();
        // then
        assertTrue(users.contains(user));
        assertEquals(null, result);
    }

    @Test
    public void testInitializeSubscriptionOwnersNotAllowed() {
        // given
        UserBean userBean = new UserBean();
        userBean = spy(userBean);
        ctrl.setUserBean(userBean);

        OrganizationBean organizationBean = mock(OrganizationBean.class);
        VOOrganization org = new VOOrganization();
        org.setKey(1L);
        when(organizationBean.getOrganization()).thenReturn(org);
        userBean.setOrganizationBean(organizationBean);

        operatorService = mock(OperatorService.class);
        ctrl.setOperatorService(operatorService);

        List<User> users = new ArrayList<>();
        User user = mock(User.class);

        users.add(user);
        model.setSubscriptionOwners(users);
        // when
        String result = ctrl.initializeSubscriptionOwners();
        // then
        assertEquals("dontOpenModalDialog", result);
    }

    @Test
    public void testSetPopupTargetAssignUsers_initializeUnassignedUsers() {
        // given
        model = spy(model);
        VOUserDetails details = new VOUserDetails(123456L, 654321);
        VOUserDetails details2 = new VOUserDetails(123457L, 654322);
        List<VOUserDetails> voList = new ArrayList<>();
        voList.add(details);
        User user2 = new User(details2);
        List<User> list = new ArrayList<>();
        list.add(user2);
        ctrl.setModel(model);
        model.setUnassignedUsers(list);

        OrganizationBean organizationBean = mock(OrganizationBean.class);
        VOOrganization voOrgganisation = mock(VOOrganization.class);
        operatorService = mock(OperatorService.class);
        userBean.setOrganizationBean(organizationBean);
        ctrl.setOperatorService(operatorService);
        when(userBean.getOrganizationBean()).thenReturn(organizationBean);
        when(organizationBean.getOrganization()).thenReturn(voOrgganisation);
        when(voOrgganisation.getKey()).thenReturn(10000L);
        when(model.getCurrentSubscriptionKey()).thenReturn(20000L);
        when(operatorService.getUnassignedUsersByOrg(anyLong(), anyLong()))
                .thenReturn(voList);
        // when
        ctrl.setPopupTargetAssignUsers();
        // then

        boolean check = false;
        List<User> finalList = model.getUnassignedUsers();
        for (User finalUser : finalList) {
            if (finalUser.getVOUserDetails().equals(details)) {
                check = true;
            }
        }

        assertTrue(check);
        assertFalse(model.getUnassignedUsers().contains(user2));
    }

    private User prepareSubOwnerWithRole(String userId, boolean isSelected,
            UserRoleType userRole) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId(userId);
        Set<UserRoleType> userRoles = new HashSet<>();
        userRoles.add(userRole);
        userDetails.setUserRoles(userRoles);
        User owner = new User(userDetails);
        owner.setFirstName("FirstName");
        owner.setLastName("LastName");
        owner.setOwnerSelected(isSelected);
        return owner;
    }

    private POUserGroup prepareUserGroup(long groupKey, String groupName) {
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup.setKey(1L);
        poUserGroup.setGroupName("unit1");
        return poUserGroup;
    }

}
