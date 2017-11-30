/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 13.02.15 11:25
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.wizards;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.*;
import org.oscm.json.*;
import org.oscm.ui.beans.*;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.serviceDetails.ServiceDetailsModel;
import org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionWizardConversation;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionWizardConversationModel;
import org.oscm.ui.dialog.mp.userGroups.SubscriptionUnitCtrl;
import org.oscm.ui.dialog.mp.userGroups.SubscriptionUnitModel;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;

import javax.enterprise.context.Conversation;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_TO_PROCEED_SELECT_UNIT;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.SUBSCRIPTION_NAME_ALREADY_EXISTS;

public class SubscriptionWizardConversationTest {

    public static final String CONFIG_RESPONSE_ERROR = "{\"messageType\":\"CONFIG_RESPONSE\",\"responseCode\":\"CONFIGURATION_FINISHED\""
            + ",\"parameters\":[{\"id\":\"ABoolean\",\"value\":\"abcd\"}]}";
    private SubscriptionWizardConversationModel model;
    private SubscriptionUnitCtrl unitCtrl;
    private SubscriptionUnitModel unitModel;
    private SubscriptionWizardConversation bean;
    private ServiceDetailsModel sdm;
    private Conversation conversation;
    private UiDelegate ui;
    private SubscriptionService subscriptionService;
    private UserGroupService userGroupService;
    private MenuBean menuBean;
    private SessionBean sessionBean;
    private UserBean userBean;

    private static String CONFIG_RESPONSE = "{\"messageType\":\"CONFIG_RESPONSE\",\"responseCode\":\"CONFIGURATION_FINISHED\""
            + ",\"parameters\":[{\"id\":\"ABoolean\",\"value\":\"true\"}]}";
    private static final String SUBSCRIPTIONADD_VIEWID = "/marketplace/subscriptions/creation/add.xhtml";
    private static String DEFAULT_VALUE = "value";
    private SubscriptionServiceInternal subscriptionServiceInternal;
    private JsonConverter jsonConverter;
    private PaymentAndBillingVisibleBean pabv;
    private AccountService accountService;

    @Before
    public void setup() {
        model = spy(new SubscriptionWizardConversationModel());

        sdm = mock(ServiceDetailsModel.class);
        conversation = mock(Conversation.class);
        ui = mock(UiDelegate.class);
        subscriptionService = mock(SubscriptionService.class);
        userGroupService = mock(UserGroupService.class);
        menuBean = mock(MenuBean.class);
        sessionBean = mock(SessionBean.class);
        subscriptionServiceInternal = mock(SubscriptionServiceInternal.class);
        jsonConverter = spy(new JsonConverter());
        jsonConverter.setUiDelegate(ui);
        userBean = spy(new UserBean());
        pabv = mock(PaymentAndBillingVisibleBean.class);
        accountService = mock(AccountService.class);
        unitCtrl = new SubscriptionUnitCtrl();
        unitModel = new SubscriptionUnitModel();
        unitCtrl.setModel(unitModel);

        bean = spy(new SubscriptionWizardConversation());
        decorateBean();

    }

    public void decorateBean() {
        doReturn(sdm).when(bean).getServiceDetailsModel();
        doNothing().when(bean).addMessage(any(FacesMessage.Severity.class),
                anyString());
        when(ui.getRequest()).thenReturn(new HttpServletRequestStub());
        when(ui.getViewLocale()).thenReturn(Locale.ENGLISH);
        when(jsonConverter.getServiceParametersAsJsonString(anyList(), anyBoolean(), anyBoolean())).thenReturn("someJsonString");
        model.setService(new Service(new VOService()));

        bean.setModel(model);
        bean.setConversation(conversation);
        bean.setUi(ui);
        bean.setUserGroupService(userGroupService);
        bean.setSubscriptionService(subscriptionService);
        bean.setMenuBean(menuBean);
        bean.setSessionBean(sessionBean);
        bean.setSubscriptionServiceInternal(subscriptionServiceInternal);
        bean.setJsonConverter(jsonConverter);
        bean.setUserBean(userBean);
        bean.setAccountingService(accountService);
        bean.setPaymentAndBillingVisibleBean(pabv);
        bean.setSubscriptionUnitCtrl(unitCtrl);
        List<UdaRow> udaRows = new ArrayList<>();
        model.setSubscriptionUdaRows(udaRows);
    }

    @Test
    public void testStartSubscriptionExceptions() throws Exception {
        // given
        bean = new SubscriptionWizardConversation() {
            private static final long serialVersionUID = 5838904781636078095L;

            @Override
            protected String initializeService(Service selectedService)
                    throws ServiceStateException, ObjectNotFoundException,
                    OrganizationAuthoritiesException,
                    OperationNotPermittedException, ValidationException {
                throw new ServiceStateException();
            }
        };
        bean = spy(bean);
        decorateBean();
        doNothing().when(ui).handleException(any(ServiceStateException.class));

        // when
        bean.startSubscription();

        // then
        verify(ui, times(1)).handleException(any(ServiceStateException.class));
    }

    @Test
    public void testIsPaymentVisible() throws Exception {
        //given

        //when
        bean.isPaymentInfoVisible();
        bean.isBillingContactVisible();

        //then
        verify(model, atLeastOnce()).isAnyPaymentAvailable();
        verify(pabv, atLeastOnce()).isBillingContactVisible();
    }

    @Test
    public void testGotoConfiguration() {
        // when
        bean.gotoConfiguration();

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isReadOnlyParams()));
    }

    @Test
    public void testStartSubscription() {
        // given
        bean = new SubscriptionWizardConversation() {
            private static final long serialVersionUID = 4321969571075963008L;

            @Override
            protected String initializeService(Service selectedService)
                    throws ServiceStateException, ObjectNotFoundException,
                    OrganizationAuthoritiesException,
                    OperationNotPermittedException, ValidationException {
                setPaymentAndBillingVisibleBean(pabv);
                return SubscriptionDetailsCtrlConstants.OUTCOME_SHOW_DETAILS_4_CREATION;
            }
        };
        bean = spy(bean);
        decorateBean();
        doReturn(Collections.emptyList()).when(bean).getEnabledPaymentTypes();
        doReturn(Collections.emptyList()).when(bean).getPaymentInfosForSubscription();

        // when
        bean.startSubscription();
    }

    @Test
    public void selectService() {
        // given
        initDataForSelectService();
        doNothing().when(bean).addMessage(any(FacesMessage.Severity.class),
                anyString());
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
        
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setKey(1000L);
        Set<UserRoleType> userRoles = new HashSet<>();
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        voUserDetails.setUserRoles(userRoles);
        doReturn(voUserDetails).when(ui).getUserFromSessionWithoutException();
        VOSubscriptionDetails subscription = new VOSubscriptionDetails();
        bean.getModel().setSubscription(subscription);
        
        // when
        String result = bean.selectService();
        // then
        assertEquals("success", result);
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
    public void selectServiceByUnitAdministrator_WithoutUnit() {
        // given
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.UNIT_ADMINISTRATOR);
        prepareDataForTestUnitSelection(userRoles, false);

        // when
        String result = bean.selectService();

        // then
        verify(bean, times(1)).addMessage(FacesMessage.SEVERITY_ERROR, ERROR_TO_PROCEED_SELECT_UNIT);
        assertEquals("", result);
    }

    @Test
    public void selectServiceByUnitAdministrator_WithUnit() {
        // given
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.UNIT_ADMINISTRATOR);
        prepareDataForTestUnitSelection(userRoles, true);

        // when
        String result = bean.selectService();

        // then
        verify(bean, times(0)).addMessage(any(Severity.class), any(String.class));
        assertEquals("success", result);
    }
    
    @Test
    public void testPreviousFromPayment() {
        // given
        
        // when
        bean.previousFromPayment();
        // then
        
        verify(model, times(1)).setReadOnlyParams(false);
    }

    @Test
    public void selectServiceByUnitAdminSubMan_WithoutUnit() {
        // given
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.UNIT_ADMINISTRATOR);
        userRoles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        prepareDataForTestUnitSelection(userRoles, false);

        // when
        String result = bean.selectService();

        // then
        verify(bean, times(0)).addMessage(any(Severity.class), any(String.class));
        assertEquals("success", result);
    }

    @Test
    public void selectServiceByUnitAdminOrgUnit_WithoutUnit() {
        // given
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.UNIT_ADMINISTRATOR);
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        prepareDataForTestUnitSelection(userRoles, false);
        // when
        String result = bean.selectService();

        // then
        verify(bean, times(0)).addMessage(any(Severity.class), any(String.class));
        assertEquals("success", result);
    }

    @Test
    public void testValidateSubscriptionId() throws Exception {

        // given
        SubscriptionServiceInternal ssi = mock(SubscriptionServiceInternal.class);
        doReturn(true).when(ssi).validateSubscriptionIdForOrganization(
                anyString());
        doReturn(ssi).when(bean).getSubscriptionServiceInternal();

        UIComponent uiInputMock = mock(UIInput.class);
        FacesContext contextMock = mock(FacesContext.class);

        // when
        bean.validateSubscriptionId(contextMock, uiInputMock, "value");

        // then
        verify(ui, times(1)).handleError(anyString(),
                eq(SUBSCRIPTION_NAME_ALREADY_EXISTS), anyObject());
    }

    @Test
    public void validateSubscriptionId_ShowExternalConfiguratorIsTrue()
            throws Exception {
        // given

        model.setShowExternalConfigurator(true);

        FacesContext context = mock(FacesContext.class);

        // when
        UIComponent toValidate = mock(UIComponent.class);
        doReturn("").when(toValidate).getClientId();
        bean.validateSubscriptionId(context, toValidate,
                new String());

        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isShowExternalConfigurator()));
    }

    @Test
    public void validateSubscriptionId_ShowExternalConfiguratorIsFalse()
            throws Exception {
        // given

        model.setShowExternalConfigurator(false);

        UIViewRoot viewRoot = mock(UIViewRoot.class);
        viewRoot.setViewId(SUBSCRIPTIONADD_VIEWID);

        FacesContext context = mock(FacesContext.class);
        context.setViewRoot(viewRoot);

        // when
        UIComponent toValidate = mock(UIComponent.class);
        doReturn("").when(toValidate).getClientId();
        bean.validateSubscriptionId(context, toValidate,
                new String());

        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.isShowExternalConfigurator()));
    }

    @Test
    public void validateSubscriptionId_noneSubIdExist() throws Exception {
        // given

        UIComponent toValidate = mock(UIComponent.class);
        doReturn("").when(toValidate).getClientId();

        when(
                Boolean.valueOf(subscriptionServiceInternal
                        .validateSubscriptionIdForOrganization(anyString())))
                .thenReturn(Boolean.FALSE);

        // when
        bean.validateSubscriptionId(mock(FacesContext.class), toValidate, new String());

        // then
        verify(subscriptionServiceInternal, times(1))
                .validateSubscriptionIdForOrganization(anyString());
    }

    @Test
    public void subscribe_SYNC_OK() throws Exception {
        // given
        prepareServiceAccessible(true);
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setStatus(SubscriptionStatus.ACTIVE);
        voSubscription.setSubscriptionId("test");
        voSubscription.setSuccessInfo("success mesage");
        doReturn(voSubscription).when(subscriptionService).subscribeToService(
                any(VOSubscriptionDetails.class), any(VOService.class),
                anyListOf(VOUsageLicense.class), any(VOPaymentInfo.class),
                any(VOBillingContact.class), anyListOf(VOUda.class));

        // when
        String result = bean.subscribe();

        // then
        verify(ui, times(1)).handle(
                SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_CREATED,
                "test", "success mesage");
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, result);
    }

    @Test
    public void subscribe_ASYNC_OK() throws Exception {
        // given
        prepareServiceAccessible(true);
        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setSubscriptionId("test");
        voSubscription.setStatus(SubscriptionStatus.PENDING);
        voSubscription.setSuccessInfo("success mesage");
        doReturn(voSubscription).when(subscriptionService).subscribeToService(
                any(VOSubscriptionDetails.class), any(VOService.class),
                anyListOf(VOUsageLicense.class), any(VOPaymentInfo.class),
                any(VOBillingContact.class), anyListOf(VOUda.class));

        // when
        String result = bean.subscribe();

        // then
        verify(ui, times(1))
                .handle(SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_CREATED,
                        "test", "success mesage");
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS, result);
    }

    @Test
    public void subscribe_ObjectNotFoundException_Service() throws Exception {
        // given
        prepareServiceAccessible(true);
        doThrow(
                new ObjectNotFoundException(
                        DomainObjectException.ClassEnum.SERVICE, "productId"))
                .when(subscriptionService).subscribeToService(
                        any(VOSubscriptionDetails.class), any(VOService.class),
                        anyListOf(VOUsageLicense.class),
                        any(VOPaymentInfo.class), any(VOBillingContact.class),
                        anyListOf(VOUda.class));

        // when
        String result = bean.subscribe();

        // then
        verify(ui, times(1)).handleError(null,
                SubscriptionDetailsCtrlConstants.ERROR_SERVICE_INACCESSIBLE);
        assertEquals(null, result);
    }

    @Test
    public void subscribe_serviceNotAccessible() throws Exception {
        // given
        prepareServiceAccessible(false);
        new FacesContextStub(Locale.ENGLISH);
        // when
        String result = bean.subscribe();

        // then
        verify(subscriptionService, never()).subscribeToService(
                any(VOSubscription.class), any(VOService.class),
                anyListOf(VOUsageLicense.class), any(VOPaymentInfo.class),
                any(VOBillingContact.class), anyListOf(VOUda.class));
        assertEquals(BaseBean.MARKETPLACE_ACCESS_DENY_PAGE, result);
    }

    @Test
    public void subscribe_serviceAccessible() throws Exception {
        // given
        prepareServiceAccessible(true);

        // when
        String result = bean.subscribe();

        // then
        verify(subscriptionService, times(1)).subscribeToService(
                any(VOSubscription.class), any(VOService.class),
                anyListOf(VOUsageLicense.class), any(VOPaymentInfo.class),
                any(VOBillingContact.class), anyListOf(VOUda.class));
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_PROCESS, result);
    }

    @Test
    public void subscribe_ObjectNotFoundException_BillingContact()
            throws Exception {
        // given
        prepareServiceAccessible(true);
        new FacesContextStub(Locale.ENGLISH);
        doThrow(
                new ObjectNotFoundException(
                        DomainObjectException.ClassEnum.BILLING_CONTACT, "id"))
                .when(subscriptionService).subscribeToService(
                        any(VOSubscriptionDetails.class), any(VOService.class),
                        anyListOf(VOUsageLicense.class),
                        any(VOPaymentInfo.class), any(VOBillingContact.class),
                        anyListOf(VOUda.class));

        // when
        String result = bean.subscribe();

        // then
        verify(ui, never()).handleError(null,
                SubscriptionDetailsCtrlConstants.ERROR_SERVICE_INACCESSIBLE);
        assertEquals(null, result);
    }

    @Test
    public void actionLoadIframe_setShowExternalConfigurator() throws Exception {
        // given
        // when
        bean.actionLoadIframe();

        // then
        assertTrue(model.isShowExternalConfigurator());
    }

    @Test
    public void actionLoadIframe_success() throws Exception {
        // given

        // when
        String result = bean.actionLoadIframe();

        // then
        assertNull(result);
        assertTrue(model.getLoadIframe());
        assertFalse(model.isHideExternalConfigurator());
        assertEquals("someJsonString", model.getServiceParametersAsJSONString());
    }

    @Test
    public void actionLoadIframe_jsonError() throws Exception {
        // given
        when(
                jsonConverter.getServiceParametersAsJsonString(anyList(),
                        anyBoolean(), anyBoolean())).thenReturn(null);
        // when
        String result = bean.actionLoadIframe();

        // then
        assertEquals(SubscriptionDetailsCtrlConstants.OUTCOME_ERROR, result);
        assertFalse(model.getLoadIframe());
        assertTrue(model.isHideExternalConfigurator());
        assertNull(model.getServiceParametersAsJSONString());
    }

    @Test
    public void copyResponseParameters_multipleParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        JsonObject configRequest = givenConfigRequest();
        addJsonParameter(configRequest, "id1", "3", false);
        addJsonParameter(configRequest, "id2", "false", false);
        addJsonParameter(configRequest, "id3", "option1", false);
        addJsonParameter(configRequest, "id4", "8", false);

        JsonObject configResponse = givenParameters();
        addJsonParameter(configResponse, "id1", "123", true);
        addJsonParameter(configResponse, "id2", "true", false);
        addJsonParameter(configResponse, "id3", "option3", false);
        addJsonParameter(configResponse, "id4", "4781111111", true);

        // when
        JsonUtils.copyResponseParameters(configRequest, configResponse);

        // then
        assertEquals("123", configRequest.getParameter("id1").getValue());
        assertTrue(configRequest.getParameter("id1").isValueError());
        assertEquals("true", configRequest.getParameter("id2").getValue());
        assertFalse(configRequest.getParameter("id2").isValueError());
        assertEquals("option3", configRequest.getParameter("id3").getValue());
        assertFalse(configRequest.getParameter("id3").isValueError());
        assertEquals("4781111111", configRequest.getParameter("id4").getValue());
        assertTrue(configRequest.getParameter("id4").isValueError());
    }

    @Test
    public void copyResponseParameters_wrongResponseParameters()
            throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        JsonObject configRequest = givenConfigRequest();
        addJsonParameter(configRequest, "id1", "3", false);
        addJsonParameter(configRequest, "id2", "false", false);
        addJsonParameter(configRequest, "id3", "option1", false);
        addJsonParameter(configRequest, "id4", "8", false);

        JsonObject configResponse = givenParameters();
        addJsonParameter(configResponse, "idx1", "123", true);
        addJsonParameter(configResponse, "id2", "true", false);
        addJsonParameter(configResponse, "idd3", "option3", false);
        addJsonParameter(configResponse, "id4", "4781111111", true);

        // when
        JsonUtils.copyResponseParameters(configRequest, configResponse);

        // then
        assertEquals("3", configRequest.getParameter("id1").getValue());
        assertFalse(configRequest.getParameter("id1").isValueError());
        assertEquals("true", configRequest.getParameter("id2").getValue());
        assertFalse(configRequest.getParameter("id2").isValueError());
        assertEquals("option1", configRequest.getParameter("id3").getValue());
        assertFalse(configRequest.getParameter("id3").isValueError());
        assertEquals("4781111111", configRequest.getParameter("id4").getValue());
        assertTrue(configRequest.getParameter("id4").isValueError());
    }

    @Test
    public void copyResponseParameters_nullResponse() throws Exception {
        // given
        JsonObject configRequest = givenConfigRequest();
        addJsonParameter(configRequest, "id1", "3", false);
        addJsonParameter(configRequest, "id2", "false", false);
        addJsonParameter(configRequest, "id3", "option1", false);
        addJsonParameter(configRequest, "id4", "8", false);

        // when
        JsonUtils.copyResponseParameters(configRequest, null);

        // then
        // ... no exception expected ...
    }

    @Test
    public void validateConfiguredParameters_success() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());
        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        model.setParameterConfigResponse(CONFIG_RESPONSE);

        // when
        String outcome = bean.getJsonValidator().validateConfiguredParameters(
                bean.getModel());

        // then
        assertNull("Outcome null expected: Stay on page", outcome);
        assertFalse("No error expected", model.getParameterValidationResult()
                .getValidationError());
        assertNull("No config request expected in validation result", model
                .getParameterValidationResult().getConfigRequest());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateConfiguredParameters_validationError() throws Exception {
        // given
        JsonConverter converter = spy(new JsonConverter());
        JsonParameterValidator validator = new JsonParameterValidator(converter);
        bean.setJsonValidator(validator);
        bean.setJsonConverter(converter);
        prepareServiceAccessible(true);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());

        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        model.setParameterConfigResponse(CONFIG_RESPONSE_ERROR);
        JsonObject configRequest = givenConfigRequest();
        addJsonParameter(configRequest, "ABoolean", "false", false);

        doReturn(configRequest).when(converter)
                .getServiceParametersAsJsonObject(Matchers.any(List.class),
                        Matchers.anyBoolean(), Matchers.anyBoolean());

        // when
        String outcome = bean.getJsonValidator().validateConfiguredParameters(
                bean.getModel());

        // then
        assertEquals("Validation error expected: Stay on page",
                SubscriptionDetailsCtrlConstants.VALIDATION_ERROR, outcome);
        assertTrue("Error expected", model.getParameterValidationResult()
                .getValidationError());
        String configRequestResult = model.getParameterValidationResult()
                .getConfigRequest();
        assertNotNull("Config request in validation result expected",
                configRequestResult);
        assertTrue("Wrong value should be in config request",
                configRequestResult.contains("\"value\":\"abcd\""));
        assertTrue("Validation error expected",
                configRequestResult.contains("\"valueError\":true"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateConfiguredParameters_parseError() throws Exception {
        // given
        JsonConverter converter = spy(new JsonConverter());
        JsonParameterValidator validator = new JsonParameterValidator(converter);
        bean.setJsonValidator(validator);
        bean.setJsonConverter(converter);
        prepareServiceAccessible(true);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());

        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        model.setParameterConfigResponse(CONFIG_RESPONSE_ERROR);
        JsonObject configRequest = givenConfigRequest();
        addJsonParameter(configRequest, "ABoolean", "false", false);

        doThrow(new IOException("Es ist ein Fehler aufgetreten.")).when(
                converter).parseJsonString(anyString());
        doReturn(configRequest).when(converter)
                .getServiceParametersAsJsonObject(Matchers.any(List.class),
                        Matchers.anyBoolean(), Matchers.anyBoolean());

        // when
        String outcome = bean.getJsonValidator().validateConfiguredParameters(
                bean.getModel());

        // then
        assertEquals("Validation error expected: Stay on page",
                SubscriptionDetailsCtrlConstants.VALIDATION_ERROR, outcome);
        assertTrue("Error expected in validation result", model
                .getParameterValidationResult().getValidationError());
        String configRequestResult = model.getParameterValidationResult()
                .getConfigRequest();
        assertNotNull("Config request in validation result expected",
                configRequestResult);
        assertTrue("Value should be unchanged in config request",
                configRequestResult.contains("\"value\":\"false\""));
        assertTrue("No validation, thus no value error in config request",
                configRequestResult.contains("\"valueError\":false"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void validateConfiguredParameters_createJsonError() throws Exception {
        // given
        prepareServiceAccessible(true);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());
        bean.setJsonValidator(new JsonParameterValidator(bean
                .getJsonConverter()));
        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        model.setParameterConfigResponse(CONFIG_RESPONSE_ERROR);
        JsonObject configRequest = givenConfigRequest();
        addJsonParameter(configRequest, "ABoolean", "false", false);

        doReturn(configRequest).when(jsonConverter)
                .getServiceParametersAsJsonObject(Matchers.any(List.class),
                        Matchers.anyBoolean(), Matchers.anyBoolean());
        doThrow(new JsonMappingException("Error in json mapping")).when(
                jsonConverter).createJsonString(Matchers.any(JsonObject.class));

        // when
        String outcome = bean.getJsonValidator().validateConfiguredParameters(
                bean.getModel());

        // then
        assertEquals("Outcome error expected: Rerender parent page",
                SubscriptionDetailsCtrlConstants.OUTCOME_ERROR, outcome);
        assertTrue("Error expected in validation result", model
                .getParameterValidationResult().getValidationError());
        assertNull("No config request expected in validation result", model
                .getParameterValidationResult().getConfigRequest());
    }

    /**
     * If the parameter configuration was cancelled the subscription model must
     * not be updated.
     */
    @Test
    public void updateValueObjects_nullJsonObject() throws Exception {
        // given
        JsonObject parameters = null;
        int originalHash = model.hashCode();

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(originalHash, model.hashCode());
    }

    @Test
    public void updateValueObjects_emptyParameters() throws Exception {
        // given
        JsonConverter jc = new JsonConverter();
        bean.setJsonConverter(spy(jc));
        JsonObject parameters = givenParameters();
        int originalHash = model.hashCode();

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(originalHash, model.hashCode());
        verify(bean.getJsonConverter(), never()).logParameterNotFound(
                anyString());
    }

    @Test
    public void updateValueObjects_oneParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "id", ParameterValueType.STRING, "123");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals("123",
                JsonUtils.findParameterById("id", model.getService())
                        .getValue());
    }

    @Test
    public void updateValueObjects_NullParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "id", ParameterValueType.STRING, null);

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(null, JsonUtils
                .findParameterById("id", model.getService()).getValue());
    }

    @Test
    public void updateValueObjects_durationParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "PERIOD", ParameterValueType.DURATION, "60");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals("5184000000",
                JsonUtils.findParameterById("PERIOD", model.getService())
                        .getValue());
    }

    @Test
    public void updateValueObjects_emptyDurationParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "PERIOD", ParameterValueType.DURATION, "");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals("",
                JsonUtils.findParameterById("PERIOD", model.getService())
                        .getValue());
    }

    @Test
    public void updateValueObjects_booleanParameter() throws Exception {
        // given
        prepareServiceAccessible(true);
        bean.setJsonConverter(new JsonConverter());
        JsonObject parameters = givenParameters();
        addParameter(parameters, "AFlag", ParameterValueType.BOOLEAN, "tRuE");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals("true",
                JsonUtils.findParameterById("AFlag", model.getService())
                        .getValue());
    }

    @Test
    public void updateValueObjects_multipleParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "id1", ParameterValueType.STRING, "123");
        addParameter(parameters, "id2", ParameterValueType.STRING, "true");
        addParameter(parameters, "id3", ParameterValueType.STRING, "option1");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals("123",
                JsonUtils.findParameterById("id1", model.getService())
                        .getValue());
        assertEquals("true",
                JsonUtils.findParameterById("id2", model.getService())
                        .getValue());
        assertEquals("option1",
                JsonUtils.findParameterById("id3", model.getService())
                        .getValue());
        assertEquals(3, model.getService().getVO().getParameters().size());
    }

    /**
     * The JSON parameter string contains only one parameter, whose parameter id
     * is unknown to BES. But the local value object contains a parameter, the
     * value of this parameter must not be changed. A warning must be logged.
     */
    @Test
    public void updateValueObjects_unknownParameter() throws Exception {
        // given
        JsonConverter jc = new JsonConverter();
        bean.setJsonConverter(spy(jc));
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addJsonParameter(parameters, "unknownId", "123");
        addVoParameter("id");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(DEFAULT_VALUE,
                JsonUtils.findParameterById("id", model.getService())
                        .getValue());
        assertEquals(1, model.getService().getVO().getParameters().size());
        verify(bean.getJsonConverter(), times(1)).logParameterNotFound(
                anyString());
    }

    /**
     * The JSON parameter string contains one unknown and one known parameter.
     * The knwon parameter must be updated correctly and a warning must be
     * logged.
     */
    @Test
    public void updateValueObjects_oneParameterAndOneUnknownParameter()
            throws Exception {
        // given
        JsonConverter jc = new JsonConverter();
        bean.setJsonConverter(spy(jc));
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "knownParameter", ParameterValueType.STRING,
                "known");
        addJsonParameter(parameters, "unknownId", "123");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(
                "known",
                JsonUtils.findParameterById("knownParameter",
                        model.getService()).getValue());
        assertEquals(1, model.getService().getVO().getParameters().size());
        verify(bean.getJsonConverter(), times(1)).logParameterNotFound(
                anyString());
    }

    @Test
    public void updateValueObjects_sameParameterTwice() throws Exception {
        // given
        JsonConverter jc = new JsonConverter();
        bean.setJsonConverter(spy(jc));
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        addParameter(parameters, "knownParameter", ParameterValueType.STRING,
                "known");
        addJsonParameter(parameters, "knownParameter", "known123");

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(
                "known123",
                JsonUtils.findParameterById("knownParameter",
                        model.getService()).getValue());
        assertEquals(1, model.getService().getVO().getParameters().size());
        verify(bean.getJsonConverter(), never()).logParameterNotFound(
                anyString());
    }

    @Test
    public void updateValueObjects_sameParameterDurationTwice_Bug10833()
            throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        long durationInMs = 410 * DurationValidation.MILLISECONDS_PER_DAY;

        addParameter(parameters, "knownParameter", ParameterValueType.DURATION,
                "410", Long.toString(durationInMs));

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(
                Long.toString(durationInMs),
                JsonUtils.findParameterById("knownParameter",
                        model.getService()).getValue());
    }

    @Test
    public void updateValueObjects_changeParameterDurationFromNull_Bug10833()
            throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        long durationInMs = 410 * DurationValidation.MILLISECONDS_PER_DAY;

        addParameter(parameters, "knownParameter", ParameterValueType.DURATION,
                "410", null);

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(
                Long.toString(durationInMs),
                JsonUtils.findParameterById("knownParameter",
                        model.getService()).getValue());
    }

    @Test
    public void updateValueObjects__changeParameterDurationToNull_Bug10833()
            throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        JsonObject parameters = givenParameters();
        long durationInMs = 410 * DurationValidation.MILLISECONDS_PER_DAY;

        addParameter(parameters, "knownParameter", ParameterValueType.DURATION,
                "", Long.toString(durationInMs));

        // when
        bean.getJsonConverter().updateValueObjects(parameters,
                bean.getModel().getService());

        // then
        assertEquals(
                "",
                JsonUtils.findParameterById("knownParameter",
                        model.getService()).getValue());
    }

    @Test
    public void validateParameters_multipleParameters() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        addPricedParameterRow("Number1", ParameterValueType.INTEGER, true, 10l,
                50l);
        addPricedParameterRow("Long1", ParameterValueType.LONG, false, 100000l,
                null);
        addPricedParameterRow("Enum1", ParameterValueType.ENUMERATION, true,
                "Option1", "Option2", "Option3", "Option4");
        addPricedParameterRow("PERIOD", ParameterValueType.DURATION, true, 0l,
                null);

        JsonObject configResponse = givenParameters();
        addJsonParameter(configResponse, "PERIOD", "106751991168");
        addJsonParameter(configResponse, "Enum1", "Option3");
        addJsonParameter(configResponse, "ABoolean", "xyz");
        addJsonParameter(configResponse, "Number1", "20");
        addJsonParameter(configResponse, "Long1", "8888888888");

        // when
        boolean validationError = bean.getJsonValidator().validateParameters(
                configResponse, new FacesContextStub(Locale.JAPAN),
                model.getServiceParameters());

        // then
        assertTrue("Some parameters were not valid", validationError);
        assertTrue("Parameter value was invalid",
                configResponse.getParameter("PERIOD").isValueError());
        assertFalse("Parameter value was valid",
                configResponse.getParameter("Enum1").isValueError());
        assertTrue("Parameter value was invalid",
                configResponse.getParameter("ABoolean").isValueError());
        assertFalse("Parameter value was valid",
                configResponse.getParameter("Number1").isValueError());
        assertFalse("Parameter value was valid",
                configResponse.getParameter("Long1").isValueError());
    }

    @Test
    public void validateParameters_emptyParameters() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        addPricedParameterRow("Number1", ParameterValueType.INTEGER, true, 10l,
                50l);
        addPricedParameterRow("Long1", ParameterValueType.LONG, false, 100000l,
                null);
        addPricedParameterRow("Enum1", ParameterValueType.ENUMERATION, true,
                "Option1", "Option2", "Option3", "Option4");
        addPricedParameterRow("PERIOD", ParameterValueType.DURATION, true, 0l,
                null);

        JsonObject configResponse = givenParameters();
        addJsonParameter(configResponse, "PERIOD", "");
        addJsonParameter(configResponse, "Enum1", "");
        addJsonParameter(configResponse, "ABoolean", "");
        addJsonParameter(configResponse, "Number1", "");
        addJsonParameter(configResponse, "Long1", "");

        // when
        boolean validationError = bean.getJsonValidator().validateParameters(
                configResponse, new FacesContextStub(Locale.JAPAN),
                model.getServiceParameters());

        // then
        assertTrue("Some parameters were not valid", validationError);
        assertTrue("Value of mandatory parameter was empty", configResponse
                .getParameter("PERIOD").isValueError());
        assertTrue("Value of mandatory parameter was empty", configResponse
                .getParameter("Enum1").isValueError());
        assertFalse("Empty value allowed because parameter is not mandatory",
                configResponse.getParameter("ABoolean").isValueError());
        assertTrue("Value of mandatory parameter was empty", configResponse
                .getParameter("Number1").isValueError());
        assertFalse("Empty value allowed because parameter is not mandatory",
                configResponse.getParameter("Long1").isValueError());
    }

    @Test
    public void validateParameters_unknownParameter() throws Exception {
        // given
        bean.setJsonConverter(new JsonConverter());
        prepareServiceAccessible(true);
        addPricedParameterRow("ABoolean", ParameterValueType.BOOLEAN, false);
        addPricedParameterRow("Number1", ParameterValueType.INTEGER, true, 10l,
                50l);
        addPricedParameterRow("Long1", ParameterValueType.LONG, false, null,
                1000000l);

        JsonObject configResponse = givenParameters();
        addJsonParameter(configResponse, "ABoolean", "true");
        addJsonParameter(configResponse, "UnknownNumber", "4711");
        addJsonParameter(configResponse, "Long1", "8888");

        // when
        boolean validationError = bean.getJsonValidator().validateParameters(
                configResponse, new FacesContextStub(Locale.JAPAN),
                model.getServiceParameters());

        // then
        assertTrue("Validation Error expected", validationError);
        assertFalse("Parameter value was valid",
                configResponse.getParameter("ABoolean").isValueError());
        assertFalse("Parameter value was valid",
                configResponse.getParameter("Long1").isValueError());
    }

    /**
     * Helper methods.
     */

    private void prepareServiceAccessible(boolean isAccessible)
            throws Exception {
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setKey(1000L);
        VOService voService = new VOService();
        voService.setKey(1001L);
        Service service = new Service(voService);
        model.setService(service);
        doReturn(voUserDetails).when(ui).getUserFromSessionWithoutException();
        VOSubscriptionDetails subsDetails = new VOSubscriptionDetails();
        subsDetails.setSubscribedService(voService);
        subsDetails.setSubscriptionId("test");
        model.setSubscription(subsDetails);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());
        List<Long> invisibleServiceKeys = new ArrayList<>();
        if (!isAccessible) {
            invisibleServiceKeys.add(service.getKey());
        }
        invisibleServiceKeys.add(1002L);
        doReturn(invisibleServiceKeys).when(userGroupService)
                .getInvisibleProductKeysForUser(voUserDetails.getKey());
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
        bean.getModel().setService(s);
        List<PricedParameterRow> pricedParameterRows = new ArrayList<>();
        PricedParameterRow pricedParameterRow = new PricedParameterRow();
        pricedParameterRows.add(pricedParameterRow);
        bean.getModel().setServiceParameters(pricedParameterRows);
        bean.getModel().getUseExternalConfigurator();
        bean.getModel().setHideExternalConfigurator(false);
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

    private void addJsonParameter(JsonObject jsonObject, String id,
            String value, boolean valueError) {
        addJsonParameter(jsonObject, id, value, null, valueError);
    }

    private void addJsonParameter(JsonObject jsonObject, String id,
            String value, ParameterValueType valueType, boolean valueError) {
        JsonParameter jsonParameter = new JsonParameter();
        jsonParameter.setId(id);
        jsonParameter.setValue(value);
        if (valueType != null) {
            jsonParameter.setValueType(valueType.name());
        }
        jsonParameter.setValueError(valueError);
        jsonObject.getParameters().add(jsonParameter);
    }

    private JsonObject givenConfigRequest() {
        JsonObject configRequest = new JsonObject();
        configRequest.setMessageType(MessageType.CONFIG_REQUEST);
        configRequest.setLocale("en");
        configRequest.setParameters(new ArrayList<JsonParameter>());
        return configRequest;
    }

    private JsonObject givenParameters() {
        JsonObject parameters = new JsonObject();
        parameters.setLocale("en");
        parameters.setMessageType(MessageType.CONFIG_RESPONSE);
        parameters.setResponseCode(ResponseCode.CONFIGURATION_FINISHED);
        parameters.setParameters(new ArrayList<JsonParameter>());
        return parameters;
    }

    private void addPricedParameterRow(VOParameter parameter) {
        PricedParameterRow pricedParRow = new PricedParameterRow(parameter,
                null, null, null, false);
        model.getServiceParameters().add(pricedParRow);
    }

    private void addPricedParameterRow(String id, ParameterValueType valueType,
            boolean mandatory, String... optionIds) {
        VOParameter parameter = addVoParameter(createParDefinition(id,
                valueType, mandatory, null, null, optionIds));
        addPricedParameterRow(parameter);
    }

    private VOParameter addVoParameter(String id, ParameterValueType valueType) {
        VOParameterDefinition voParameterDef = new VOParameterDefinition();
        voParameterDef.setParameterId(id);
        if (valueType != null) {
            voParameterDef.setValueType(valueType);
        }
        VOParameter voParameter = new VOParameter(voParameterDef);
        voParameter.setValue(DEFAULT_VALUE);
        model.getService().getVO().getParameters().add(voParameter);
        return voParameter;
    }

    private VOParameter addVoParameter(VOParameterDefinition voParameterDef) {
        VOParameter voParameter = new VOParameter(voParameterDef);
        voParameter.setValue(DEFAULT_VALUE);
        model.getService().getVO().getParameters().add(voParameter);
        return voParameter;
    }

    private VOParameterDefinition createParDefinition(String id,
            ParameterValueType valueType, boolean mandatory, Long minValue,
            Long maxValue, String... optionIds) {
        VOParameterDefinition parDefinition = new VOParameterDefinition();
        parDefinition.setParameterId(id);
        parDefinition.setMandatory(mandatory);
        parDefinition.setValueType(valueType);

        parDefinition.setMinValue(minValue);
        parDefinition.setMaxValue(maxValue);

        if (optionIds.length > 0) {
            List<VOParameterOption> options = new ArrayList<>();
            for (String optionId : optionIds) {
                options.add(new VOParameterOption(optionId, optionId
                        + "Description", id));
            }
            parDefinition.setParameterOptions(options);
        }

        return parDefinition;
    }

    private VOParameter byId(String id) {
        return JsonUtils.findParameterById(id, model.getService());
    }

    private void addParameter(JsonObject parameters, String id,
            ParameterValueType valueType, String value) {
        addJsonParameter(parameters, id, value, valueType, false);
        addVoParameter(id, valueType);
    }

    private void addParameter(JsonObject parameters, String id,
            ParameterValueType valueType, String value, String voValue) {
        addJsonParameter(parameters, id, value, valueType, false);
        addVoParameter(id, valueType, voValue);
    }

    private VOParameter addVoParameter(String id) {
        return addVoParameter(id, null);
    }

    private VOParameter addVoParameter(String id, ParameterValueType valueType,
            String value) {
        VOParameterDefinition voParameterDef = new VOParameterDefinition();
        voParameterDef.setParameterId(id);
        if (valueType != null) {
            voParameterDef.setValueType(valueType);
        }
        VOParameter voParameter = new VOParameter(voParameterDef);
        voParameter.setValue(value);
        model.getService().getVO().getParameters().add(voParameter);
        return voParameter;
    }

    private void addJsonParameter(JsonObject parameters, String id, String value) {
        addJsonParameter(parameters, id, value, false);
    }

    private void addPricedParameterRow(String id, ParameterValueType valueType,
            boolean mandatory, Long minValue, Long maxValue) {
        VOParameter parameter = addVoParameter(createParDefinition(id,
                valueType, mandatory, minValue, maxValue));
        addPricedParameterRow(parameter);
    }

    @Test
    public void isServiceForSubscription_NotSubscriptionManager() {
        // given
        Service service = new Service(new VOService());
        service.setSubscribable(true);
        VOUserDetails emptyRoleUser = mockUserDetailsWithRoles();
        doReturn(emptyRoleUser).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = bean.isServiceReadyForSubscription(service);

        // then
        assertFalse(result);
    }

    @Test
    public void isServiceForSubscription_SubscriptionManager() {
        // given
        Service service = new Service(new VOService());
        service.setSubscribable(true);
        VOUserDetails subscriptionManager = mockUserDetailsWithRoles(UserRoleType.SUBSCRIPTION_MANAGER);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = bean.isServiceReadyForSubscription(service);

        // then
        assertTrue(result);
    }

    @Test
    public void isServiceForSubscription_UnitAdmin() {
        // given
        Service service = new Service(new VOService());
        service.setSubscribable(true);
        VOUserDetails unitAdministrator = mockUserDetailsWithRoles(UserRoleType.UNIT_ADMINISTRATOR);
        doReturn(unitAdministrator).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = bean.isServiceReadyForSubscription(service);

        // then
        assertTrue(result);
    }

    private VOUserDetails mockUserDetailsWithRoles(UserRoleType... roles) {
        VOUserDetails userDetails = new VOUserDetails();

        userDetails.setUserRoles(Sets.newHashSet(roles));

        return userDetails;
    }

    private void prepareDataForTestUnitSelection(Set<UserRoleType> userRoles, boolean isUnitSelected) {
        VOUserDetails voUserDetails = new VOUserDetails();
        voUserDetails.setKey(1001L);
        voUserDetails.setUserRoles(userRoles);
        doReturn(voUserDetails).when(ui).getUserFromSessionWithoutException();
        mock(JSFUtils.class);
        if (isUnitSelected) {
            unitCtrl.getModel().setSelectedUnitId(1000L);
        } else {
            unitCtrl.getModel().setSelectedUnitId(0L);
        }
        bean.getModel().getService().setPriceModel(new PriceModel(new VOPriceModel()));
        doNothing().when(bean).updateSelectedUnit();
    }
}
