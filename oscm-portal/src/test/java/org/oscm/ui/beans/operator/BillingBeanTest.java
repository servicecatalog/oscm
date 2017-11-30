/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                    
 *                                                                              
 *  Creation Date: 03.02.2011                                                      
 *                                                                              
 *  Completion Time: 08.02.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.test.stubs.OperatorServiceStub;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SelectOrganizationIncludeBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.validator.DateFromToValidator;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;

/**
 * @author weiser
 * 
 */
public class BillingBeanTest {

    private BillingBean bean;
    private OperatorSelectOrgBean orgBean;
    private FacesContext context;
    private UIComponent toValidate;
    private Object value;
    private DateFromToValidator validator;

    protected byte[] responseContent;
    protected String messageKey;
    protected boolean serviceCalled;
    protected boolean serviceResult;
    protected byte[] billingData = new byte[] { 1, 2, 3, 4, 5 };
    private final ApplicationBean appBean = mock(ApplicationBean.class);
    private final OperatorService operatorService = mock(OperatorService.class);

    private static final String ORG_ID = "A1B2C3D4";

    @Before
    public void setup() {
        final OperatorServiceStub stub = new OperatorServiceStub() {

            @Override
            public boolean startPaymentProcessing() {
                serviceCalled = true;
                return serviceResult;
            }

            @Override
            public boolean retryFailedPaymentProcesses() {
                serviceCalled = true;
                return serviceResult;
            }

            @Override
            public boolean startBillingRun() {
                serviceCalled = true;
                return serviceResult;
            }

            @Override
            public byte[] getOrganizationBillingData(long from, long to,
                    String organizationId) {
                serviceCalled = true;
                return billingData;
            }

        };

        bean = new BillingBean() {

            private static final long serialVersionUID = -3727626273904111506L;

            @Override
            protected OperatorService getOperatorService() {
                return stub;
            }

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

        };
        orgBean = new OperatorSelectOrgBean() {

            private static final long serialVersionUID = -9126265695343363133L;

            @Override
            protected OperatorService getOperatorService() {
                return operatorService;
            }
        };
        orgBean = spy(orgBean);
        orgBean.ui = mock(UiDelegate.class);
        when(orgBean.ui.findBean(eq(OperatorSelectOrgBean.APPLICATION_BEAN)))
                .thenReturn(appBean);
        when(orgBean.getApplicationBean()).thenReturn(appBean);
        orgBean.setSelectOrganizationIncludeBean(new SelectOrganizationIncludeBean());
        bean.setOperatorSelectOrgBean(orgBean);
        context = mock(FacesContext.class);
        toValidate = mock(UIComponent.class);
        value = mock(Object.class);
        validator = spy(new DateFromToValidator());
        bean.setValidator(validator);
    }

    @Test
    public void testGetBillingData_AllNull() throws Exception {
        String result = bean.getBillingData();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_EXPORT_BILLING_DATA, messageKey);
        assertFalse(serviceCalled);
        assertFalse(bean.isBillingDataAvailable());
    }

    @Test
    public void testGetBillingData_OrgIdNull() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(date);
        bean.setToDate(date);
        String result = bean.getBillingData();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_EXPORT_BILLING_DATA, messageKey);
        assertFalse(serviceCalled);
        assertFalse(bean.isBillingDataAvailable());
    }

    @Test
    public void testGetBillingData_OrgIdEmpty() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(date);
        bean.setToDate(date);
        orgBean.setOrganizationId("   ");
        String result = bean.getBillingData();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_EXPORT_BILLING_DATA, messageKey);
        assertFalse(serviceCalled);
        assertFalse(bean.isBillingDataAvailable());
    }

    @Test
    public void testGetBillingData_NullReturned() throws Exception {
        billingData = null;
        String result = getBillingData();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_SHOW_BILLING_DATA, messageKey);
        assertTrue(serviceCalled);
        assertFalse(bean.isBillingDataAvailable());
    }

    @Test
    public void testGetBillingData() throws Exception {
        String result = getBillingData();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(null, messageKey);
        assertTrue(serviceCalled);
        assertTrue(bean.isBillingDataAvailable());
    }

    @Test
    public void testShowBillingData_BillingDataNull() throws Exception {
        String result = bean.showBillingData();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_SHOW_BILLING_DATA, messageKey);
    }

    @Test
    public void testShowBillingData() throws Exception {
        String result = getBillingData();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        result = bean.showBillingData();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(null, messageKey);
        assertEquals(billingData, responseContent);
    }

    @Test
    public void testStartBillingRun_Negative() throws Exception {
        String result = bean.startBillingRun();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_TASK_EXECUTION, messageKey);
        assertTrue(serviceCalled);
    }

    @Test
    public void testStartBillingRun() throws Exception {
        serviceResult = true;
        String result = bean.startBillingRun();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(BaseOperatorBean.INFO_TASK_SUCCESSFUL, messageKey);
        assertTrue(serviceCalled);
    }

    @Test
    public void testStartPaymentProcessing_Negative() throws Exception {
        String result = bean.startPaymentProcessing();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_TASK_EXECUTION, messageKey);
        assertTrue(serviceCalled);
    }


    @Test
    public void testStartPaymentProcessing() throws Exception {
        serviceResult = true;
        String result = bean.startPaymentProcessing();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(BaseOperatorBean.INFO_TASK_SUCCESSFUL, messageKey);
        assertTrue(serviceCalled);
    }

    @Test
    public void testRetryFailedPaymentProcesses_Negative() throws Exception {
        String result = bean.retryFailedPaymentProcesses();
        assertEquals(BaseBean.OUTCOME_ERROR, result);
        assertEquals(BaseOperatorBean.ERROR_TASK_EXECUTION, messageKey);
        assertTrue(serviceCalled);
    }

    @Test
    public void testRetryFailedPaymentProcesses() throws Exception {
        serviceResult = true;
        String result = bean.retryFailedPaymentProcesses();
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        assertEquals(BaseOperatorBean.INFO_TASK_SUCCESSFUL, messageKey);
        assertTrue(serviceCalled);
    }

    @Test
    public void testGetOperatorSelectOrgBean() throws Exception {
        assertEquals(orgBean, bean.getOperatorSelectOrgBean());
    }

    @Test
    public void testGetFromDate() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(date);
        assertEquals(date, bean.getFromDate());
    }

    @Test
    public void testGetToDate() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        bean.setToDate(date);
        assertEquals(date, bean.getToDate());
    }

    private String getBillingData() throws ObjectNotFoundException,
            OrganizationAuthoritiesException {
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(date);
        bean.setToDate(date);
        orgBean.setOrganizationId(ORG_ID);
        String result = bean.getBillingData();
        return result;
    }

    @Test
    public void validateFromAndToDate() {
        // given
        context = mock(FacesContext.class);
        toValidate = mock(UIComponent.class);
        when(toValidate.getClientId(context)).thenReturn("clientId");
        value = mock(Object.class);
        // when
        bean.validateFromAndToDate(context, toValidate, value);
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
        bean.validateFromAndToDate(context, toValidate, value);
        // then
        verify(context, times(1)).addMessage(anyString(),
                any(FacesMessage.class));
    }

    @Test
    public void isBillingDataButtonDisabled() {
        // given
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(date);
        bean.setToDate(date);
        // when
        boolean result = bean.isBillingDataButtonDisabled();
        // then
        assertFalse(result);
    }

    @Test
    public void isBillingDataButtonDisabled_FromDateIsNull() {
        // given
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(null);
        bean.setToDate(date);
        // when
        boolean result = bean.isBillingDataButtonDisabled();
        // then
        assertTrue(result);
    }

    @Test
    public void isBillingDataButtonDisabled_ToDateIsNull() {
        // given
        Date date = new Date(System.currentTimeMillis());
        bean.setFromDate(date);
        bean.setToDate(null);
        // when
        boolean result = bean.isBillingDataButtonDisabled();
        // then
        assertTrue(result);
    }

    @Test
    public void isBillingDataButtonDisabled_Null() {
        bean.setFromDate(null);
        bean.setToDate(null);
        // when
        boolean result = bean.isBillingDataButtonDisabled();
        // then
        assertTrue(result);
    }

    @Test
    public void isBillingDataButtonDisabled_FromDateAfterToDate() {
        // given
        Date fromDate = new Date(System.currentTimeMillis() + 100);
        Date toDate = new Date(System.currentTimeMillis());
        bean.setFromDate(fromDate);
        bean.setToDate(toDate);
        // when
        boolean result = bean.isBillingDataButtonDisabled();
        // then
        assertTrue(result);
    }
}
