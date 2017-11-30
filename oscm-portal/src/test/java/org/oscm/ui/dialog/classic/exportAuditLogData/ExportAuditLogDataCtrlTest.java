/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-4-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportAuditLogData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import org.oscm.converter.DateConverter;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.operator.BaseOperatorBean;
import org.oscm.ui.model.AuditLogOperation;
import org.oscm.ui.validator.DateFromToValidator;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author Qiu
 * 
 */
public class ExportAuditLogDataCtrlTest extends BaseOperatorBean {

    private static final long serialVersionUID = 5397180433490282313L;
    private ExportAuditLogDataCtrl ctrl;
    private ExportAuditLogDataModel model;
    private FacesContext context;
    private UIComponent toValidate;
    private Object value;
    private DateFromToValidator validator;
    private ArgumentCaptor<Long> fromDate = ArgumentCaptor
            .forClass(Long.class);
    private ArgumentCaptor<Long> toDate = ArgumentCaptor
            .forClass(Long.class);
    protected byte[] responseContent;
    protected String messageKey;

    protected boolean serviceResult;
    protected byte[] auditLogData = new byte[] { 1, 2, 3, 4, 5 };
    protected List<String> availableAuditLogOperationGroups = new ArrayList<String>();
    protected byte[] anEmptyAuditLog = "".getBytes();
    protected Map<String, String> availableOperations = new HashMap<String, String>();
    protected Map<String, String> operationGroups = new HashMap<String, String>();

    private static final String SUBSCRIBE_SERVICE = "Subscribe to service";
    private static final String OPERATIONS_ALL = "OPERATIONS_ALL";
    private OperatorService operatorService;

    private static GivenPeriod period;

    @Before
    public void setup() throws Exception {

        operatorService = mockOperatorService();
        ctrl = new ExportAuditLogDataCtrl() {

            private static final long serialVersionUID = 4851581978644817692L;

            @Override
            protected void writeContentToResponse(byte[] content,
                    String filename, String contentType) throws IOException {
                responseContent = content;
            }

            @Override
            protected void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key) {
                messageKey = key;
            }

            @Override
            protected OperatorService getOperatorService() {
                return operatorService;
            }
        };
        model = new ExportAuditLogDataModel();
        ctrl.setModel(model);
        context = mock(FacesContext.class);
        toValidate = mock(UIComponent.class);
        value = mock(Object.class);
        validator = spy(new DateFromToValidator());
        ctrl.setValidator(validator);
    }

    @SuppressWarnings("boxing")
    private OperatorService mockOperatorService() throws Exception {
        OperatorService operatorService = mock(OperatorService.class);
        when(operatorService.getAvailableAuditLogOperations()).thenReturn(
                availableOperations);

        doReturn(auditLogData).when(operatorService).getUserOperationLog(
                Matchers.anyListOf(String.class), fromDate.capture(),
                toDate.capture());

        doReturn(operationGroups).when(operatorService)
                .getAvailableAuditLogOperationGroups();
        return operatorService;
    }

    private long capturedFromDate() {
        return fromDate.getValue().longValue();
    }

    private long capturedToDate() {
        return toDate.getValue().longValue();
    }

    class GivenPeriod {
        Date start;
        Date end;
    }

    @Test
    public void getAuditLogData() throws Exception {
        // given
        givenAnyPeriodAndType();

        // when
        String result = ctrl.getAuditLogData();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(null, messageKey);
        assertTrue(ctrl.isAuditLogDataAvailable());
    }

    @Test
    public void getAuditLogData_NoPeriod() throws Exception {
        // given
        givenNoPeriodSelected();

        // when
        String result = ctrl.getAuditLogData();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_EXPORT_AUDIT_LOG_DATA, messageKey);
        assertFalse(ctrl.isAuditLogDataAvailable());
    }

    @Test
    public void getAuditLogData_NoTypeSelected() throws Exception {
        // given
        givenNoTypeSelected();

        // when
        String result = ctrl.getAuditLogData();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_EXPORT_AUDIT_LOG_DATA, messageKey);
        assertFalse(ctrl.isAuditLogDataAvailable());
    }

    @Test
    public void getAuditLogData_Empty() throws Exception {
        // given
        givenAnyPeriodAndType();

        doReturn(anEmptyAuditLog).when(operatorService).getUserOperationLog(
                Matchers.anyListOf(String.class), Matchers.anyLong(),
                Matchers.anyLong());

        // when
        String result = ctrl.getAuditLogData();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_SHOW_AUDIT_LOG_DATA, messageKey);
        assertFalse(ctrl.isAuditLogDataAvailable());
    }

    @Test
    public void showAuditLogData_AuditLogNotAvailable() throws Exception {
        // given
        givenAuditLogDataNull();

        // when
        String result = ctrl.showAuditLogData();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_SHOW_AUDIT_LOG_DATA, messageKey);
    }

    @Test
    public void showAuditLogData() throws Exception {
        // given
        givenAnyPeriodAndType();

        // when
        String result = ctrl.getAuditLogData();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);

        result = ctrl.showAuditLogData();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        Assert.assertEquals(null, messageKey);
        Assert.assertEquals(auditLogData, responseContent);
    }

    @Test
    public void testFromToDateEqual() throws Exception {
        // given
        period = givenEqualToAndFromTime();

        // when
        model.setAvailableOperations(generateAvailableOperations());

        ctrl.getAuditLogData();

        // then
        assertLogFilePeriodAtLeastOneDay();
    }

    @Test
    public void testFromToDateDifferent() throws Exception {
        // given
        period = givenDifferentFromAndToTime();

        // when
        model.setAvailableOperations(generateAvailableOperations());

        ctrl.getAuditLogData();

        // then
        assertLogFilePeriodAtLeastOneDay();
    }

    /**
     * Test if the export button is disabled when no item is selected
     */
    @Test
    public void setSelectedItem_null() throws Exception {
        // given
        givenAnyPeriodAndType();

        // when
        model.setAvailableOperations(null);
        boolean isEnabled = ctrl.isButtonEnabled();

        // then
        assertFalse(isEnabled);

    }

    @Test
    public void processValueChange() {
        // given
        givenGroupedOperations();
        model.setSelectedGroup(null);
        model.setOperationGroups(operationGroups);
        model.setOperations(availableOperations);

        ValueChangeEvent mockedEvent = mock(ValueChangeEvent.class);
        doReturn("OPERATIONS_ORGANIZATION_ADMIN").when(mockedEvent)
                .getNewValue();

        // when
        ctrl.processValueChange(mockedEvent);

        // then
        assertNotNull(model.getSelectedGroup());
        assertEquals(operationGroups.get("OPERATIONS_ORGANIZATION_ADMIN")
                .split(",").length, model.getAvailableOperations().size());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.isAllServicesSelected()));

    }

    @Test
    public void processValueChange_SelectAll() {
        // given
        availableOperations.clear();
        operationGroups.clear();
        givenGroupedOperations();
        model.setSelectedGroup(null);
        model.setOperationGroups(operationGroups);
        model.setOperations(availableOperations);
        ValueChangeEvent mockedEvent = mock(ValueChangeEvent.class);
        doReturn(OPERATIONS_ALL).when(mockedEvent).getNewValue();

        // when
        ctrl.processValueChange(mockedEvent);

        // then
        assertEquals(16, model.getAvailableOperations().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void processValueChange_NullOperationGroupsAndOperations() {
        // given
        availableOperations.clear();
        operationGroups.clear();
        model.setSelectedGroup(null);
        ValueChangeEvent mockedEvent = mock(ValueChangeEvent.class);
        doReturn(OPERATIONS_ALL).when(mockedEvent).getNewValue();

        // when
        ctrl.processValueChange(mockedEvent);
    }

    @Test
    public void processValueChange_EmptyOperationsOfOperationGroups() {
        // given
        availableOperations.clear();
        operationGroups.put(SUBSCRIBE_SERVICE, "");
        model.setOperationGroups(operationGroups);
        model.setOperations(availableOperations);
        model.setSelectedGroup(null);
        ValueChangeEvent mockedEvent = mock(ValueChangeEvent.class);
        doReturn(OPERATIONS_ALL).when(mockedEvent).getNewValue();

        // when
        ctrl.processValueChange(mockedEvent);

        // given
        model.setOperationGroups(operationGroups);

        // when
        ctrl.processValueChange(mockedEvent);

        // then
        assertEquals(0, model.getAvailableOperations().size());
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.isAllServicesSelected()));
    }

    @Test
    public void showOperationData_RoleNull() {
        // given
        model.setSelectedGroup(null);
        // when
        ctrl.showOperationData();
        // then
        assertEquals(0, model.getAvailableOperations().size());
    }

    @Test
    public void setAllServicesSelected() {
        model.setAvailableOperations(generateAvailableOperations());

        ctrl.setAllServicesSelected(true);

        // then
        for (AuditLogOperation o : model.getAvailableOperations()) {
            assertTrue(o.isSelected());
        }

    }

    public void resetAllServicesSelected() {

        // given
        model.setAvailableOperations(generateAvailableOperations());

        // when
        model.getAvailableOperations().get(0).setSelected(false);
        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.isAllServicesSelected()));
    }

    @Test
    public void showOperationData_AvailableOperationsSorted() {
        // given
        availableOperations.clear();
        operationGroups.clear();
        givenGroupedOperations();
        model.setSelectedGroup(null);
        model.setOperationGroups(operationGroups);
        model.setOperations(availableOperations);
        ValueChangeEvent mockedEvent = mock(ValueChangeEvent.class);
        doReturn(OPERATIONS_ALL).when(mockedEvent).getNewValue();

        // when
        ctrl.processValueChange(mockedEvent);

        // then
        assertEquals(
                Boolean.TRUE,
                Boolean.valueOf(model
                        .getAvailableOperations()
                        .get(0)
                        .getOperationName()
                        .compareTo(
                                model.getAvailableOperations().get(1)
                                        .getOperationName()) < 0));

    }

    @Test
    public void validateFromAndToDate() {
        // given
        context = mock(FacesContext.class);
        toValidate = mock(UIComponent.class);
        when(toValidate.getClientId(context)).thenReturn("clientId");
        value = mock(Object.class);
        // when
        ctrl.validateFromAndToDate(context, toValidate, value);
        // then
        verify(validator, times(1)).validate(eq(context), eq(toValidate),
                eq(value));
    }

    @Test
    public void validateFromAndToDate_ValidatorException() {
        // given
        context = mock(FacesContext.class);
        toValidate = mock(UIComponent.class);
        value = mock(Object.class);
        ValidatorException ex = mock(ValidatorException.class);
        doThrow(ex).when(validator).validate(any(FacesContext.class),
                any(UIComponent.class), any(Object.class));
        // when
        ctrl.validateFromAndToDate(context, toValidate, value);
        // then
        verify(context, times(1)).addMessage(anyString(),
                any(FacesMessage.class));
    }

    /**
     * Test if the export button is enabled when an item is selected
     */
    @Test
    public void isButtonEnabled_Succeed() throws Exception {
        // given
        givenAnyPeriodAndType();

        // when
        model.setAvailableOperations(generateAvailableOperations());

        boolean isEnabled = ctrl.isButtonEnabled();

        // then
        assertTrue(isEnabled);
    }

    @Test
    public void isButtonEnabled_AvailableOperationsNull() throws Exception {
        // given
        givenAnyPeriodAndType();

        // when
        model.setAvailableOperations(null);

        boolean isEnabled = ctrl.isButtonEnabled();

        // then
        assertFalse(isEnabled);
    }

    @Test
    public void isButtonEnabled_FromDateIsNull() throws Exception {

        // given
        givenAnyPeriodAndType();
        model.setFromDate(null);
        model.setToDate(new Date(System.currentTimeMillis()));

        // when
        model.setAvailableOperations(generateAvailableOperations());
        boolean result = ctrl.isButtonEnabled();
        // then
        assertFalse(result);
    }

    @Test
    public void isButtonEnabled_ToDateIsNull() throws Exception {
        // given
        givenAnyPeriodAndType();
        model.setFromDate(new Date(System.currentTimeMillis()));
        model.setToDate(null);

        // when
        model.setAvailableOperations(generateAvailableOperations());
        boolean result = ctrl.isButtonEnabled();
        // then
        assertFalse(result);
    }

    @Test
    public void isButtonEnabled_Null() throws Exception {
        // given
        givenAnyPeriodAndType();
        model.setFromDate(null);
        model.setToDate(null);
        // when
        boolean result = ctrl.isButtonEnabled();
        // then
        assertFalse(result);
    }

    @Test
    public void isButtonEnabled_FromDateAfterToDate() throws Exception {
        // given
        givenAnyPeriodAndType();
        Date fromDate = new Date(System.currentTimeMillis() + 100);
        Date toDate = new Date(System.currentTimeMillis());
        model.setFromDate(fromDate);
        model.setToDate(toDate);
        // when
        boolean result = ctrl.isButtonEnabled();
        // then
        assertFalse(result);
    }

    /**
     * Check captured "to" and "from" date:<br>
     * - from: must be the begin of the day <br>
     * - to: must be the end of the day <br>
     * - the difference must be at least one day minus one millisecond <br>
     */
    private void assertLogFilePeriodAtLeastOneDay() {
        final long dayBegin = DateConverter
                .getBeginningOfDayInCurrentTimeZone(period.start.getTime());

        assertEquals(dayBegin, capturedFromDate());

        final long dayEnd = DateConverter
                .getEndOfDayInCurrentTimeZone(period.end.getTime());

        assertEquals(dayEnd, capturedToDate());

        final long ONE_DAY = 60 * 60 * 24 * 1000 - 1;

        assertTrue("period must be at least one day", capturedToDate()
                - capturedFromDate() >= ONE_DAY);

    }

    private void givenAnyPeriodAndType() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        model.setFromDate(date);
        model.setToDate(date);
        model.setAvailableOperations(generateAvailableOperations());
    }

    private void givenNoTypeSelected() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        model.setFromDate(date);
        model.setToDate(date);
        model.setAvailableOperations(null);

    }

    private void givenNoPeriodSelected() throws Exception {
        model.setFromDate(null);
        model.setToDate(null);
        model.setAvailableOperations(null);
    }

    private GivenPeriod givenDifferentFromAndToTime() throws Exception {
        Calendar calFrom = Calendar.getInstance();
        calFrom.set(2011, 10, 10, 10, 10, 10);

        GivenPeriod period = new GivenPeriod();
        period.start = new Date(calFrom.getTimeInMillis());

        Calendar calTo = Calendar.getInstance();
        calTo.set(2011, 11, 11, 11, 11, 11);
        period.end = new Date(calTo.getTimeInMillis());

        model.setFromDate(period.start);
        model.setToDate(period.end);
        return period;
    }

    private GivenPeriod givenEqualToAndFromTime() throws Exception {
        Calendar calFrom = Calendar.getInstance();
        calFrom.set(2011, 10, 10, 10, 10, 10);

        GivenPeriod period = new GivenPeriod();
        period.end = period.start = new Date(calFrom.getTimeInMillis());
        model.setFromDate(period.start);
        model.setToDate(period.end);
        return period;
    }

    private void givenAuditLogDataNull() {
        model.setAuditLogData(null);
    }

    private List<AuditLogOperation> generateAvailableOperations() {
        List<AuditLogOperation> operations = new ArrayList<AuditLogOperation>();
        AuditLogOperation operation = new AuditLogOperation(
                "SUBSCRIBE_SERVICE", SUBSCRIBE_SERVICE);
        operation.setSelected(true);
        operations.add(operation);
        // when
        return operations;
    }

    private void givenGroupedOperations() {
        operationGroups
                .put("OPERATIONS_ORGANIZATION_ADMIN",
                        "30000,30001,30002,30003,30004,30005,30006,30007,30008,30009,30010,30011");
        operationGroups
                .put("OPERATIONS_ORGANIZATION_USER",
                        "30000,30001,30002,30003,30004,30005,30006,30007,30008,30009,30010,30011");
        operationGroups
                .put("OPERATIONS_SERVICE_MANAGER",
                        "30000,30001,30002,30003,30004,30005,30006,30007,30008,30009,30010,30011,30012,30013,30014,30015");
        operationGroups
                .put("OPERATIONS_TECHNOLOGY_MANAGER",
                        "30000,30001,30002,30003,30004,30005,30006,30007,30008,30009,30010,30011");
        operationGroups
                .put("OPERATIONS_MARKETPLACE_OWNER",
                        "30000,30001,30002,30003,30004,30005,30006,30007,30008,30009,30010,30011");
        operationGroups
                .put("OPERATIONS_PLATFORM_OPERATOR",
                        "30000,30001,30002,30003,30004,30005,30006,30007,30008,30009,30010,30011");
        availableOperations.put("30000", "Subscribe to service");
        availableOperations.put("30001", "Assign user to subscription");
        availableOperations.put("30002", "Deassign user from subscription");
        availableOperations.put("30003", "Assign user role for service");
        availableOperations.put("30004", "Deassign user role for service");
        availableOperations.put("30005",
                "Edit subscription parameter configuration");
        availableOperations.put("30006", "Edit subscription billing address");
        availableOperations.put("30007", "Edit subscription payment type");
        availableOperations.put("30008", "Up/downgrade supscription");
        availableOperations.put("30009", "Execute service operation");
        availableOperations.put("30010", "Terminate subscription");
        availableOperations.put("30011", "Subscription report issue");
        availableOperations.put("30012", "View subscription");
        availableOperations.put("30013",
                "Localize price model for subscription");
        availableOperations.put("30014", "Edit subscription attribute");
        availableOperations.put("30015", "Unsubscribe from service");

    }
}
