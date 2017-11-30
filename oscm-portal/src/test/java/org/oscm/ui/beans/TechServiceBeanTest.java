/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage.Severity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.beans.marketplace.TagCloudBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.OperationRow;
import org.oscm.ui.model.TechnicalService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class TechServiceBeanTest {

    private TechServiceBean bean;

    private TagCloudBean tagCloudBean;
    private ServiceProvisioningService sps;

    private VOTechnicalService ts;
    private final byte[] data = new byte[] { 1, 2, 3, 4 };

    private final static String DEFAULT_BILLING_ADAPTER = "NATIVE_BILLING";

    @Before
    public void setup() throws Exception {
        sps = mock(ServiceProvisioningService.class);

        ts = new VOTechnicalService();
        ts.setKey(1234);
        ts.setTechnicalServiceId("1234");
        when(sps.getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER))).thenReturn(
                        Arrays.asList(new VOTechnicalService[] { ts }));
        when(sps.createTechnicalService(any(VOTechnicalService.class)))
                .thenAnswer(new Answer<VOTechnicalService>() {

                    @Override
                    public VOTechnicalService answer(
                            InvocationOnMock invocation) throws Throwable {
                        // return the passed technical service on create calls
                        return (VOTechnicalService) invocation
                                .getArguments()[0];
                    }
                });
        when(sps.exportTechnicalServices(anyListOf(VOTechnicalService.class)))
                .thenReturn(data);

        bean = spy(new TechServiceBean());

        doReturn(sps).when(bean).getProvisioningService();
        doReturn(DEFAULT_BILLING_ADAPTER).when(bean)
                .getDefaultBillingIdentifier();
        doNothing().when(bean).addMessage(anyString(), any(Severity.class),
                anyString(), (Object[]) any());
        bean.setSessionBean(new SessionBean());
        bean.setMenuBean(new MenuBean());

        tagCloudBean = mock(TagCloudBean.class);
        UiDelegate ui = mock(UiDelegate.class);
        when(ui.findTagCloudBean()).thenReturn(tagCloudBean);
        bean.ui = ui;
    }

    @Test
    public void setSelectedTechnicalServiceKey() throws Exception {
        List<VOTechnicalService> list = bean.getTechnicalServices();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(ts, list.get(0));
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));

        bean.setSelectedTechnicalServiceKey(ts.getKey());
        // setting the selected key must not cause a list reset
        verifyNoMoreInteractions(sps);
        TechnicalService service = bean.getSelectedTechnicalService();
        // getting the selected service must not cause a list reset
        verifyNoMoreInteractions(sps);
        assertNotNull(service);
        assertEquals(ts, service.getVo());
    }

    @Test
    public void setSelectedTechnicalServiceKey_ListNotInitialied()
            throws Exception {
        assertNull(bean.getSelectedTechnicalService());
        verifyNoMoreInteractions(sps);

        bean.setSelectedTechnicalServiceKey(ts.getKey());
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        TechnicalService service = bean.getSelectedTechnicalService();
        assertNotNull(service);
        assertEquals(ts, service.getVo());
        verifyNoMoreInteractions(sps);
    }

    @Test
    public void setSelectedTechnicalServiceKeyWithExceptionAndRefresh_TechServiceNotFound()
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException {
        // given
        doNothing().when(bean).setSelectedTechnicalServiceKey(anyLong());
        bean.selectedTechnicalService = new TechnicalService(ts);
        doThrow(new ObjectNotFoundException()).when(sps)
                .validateTechnicalServiceCommunication(ts);

        // when
        bean.setSelectedTechnicalServiceKeyWithExceptionAndRefresh(ts.getKey());

        // then
        verify(bean.ui, times(1))
                .handleException(any(SaaSApplicationException.class), eq(true));
        assertNull(bean.selectedTechnicalService);
        assertNull(bean.technicalServices);
        assertEquals(new Long(0), new Long(
                bean.getSessionBean().getSelectedTechnicalServiceKey()));

    }

    @Test
    public void setSelectedTechnicalServiceKeyWithExceptionAndRefresh_TechServiceFound()
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException {
        // given
        doNothing().when(bean).setSelectedTechnicalServiceKey(anyLong());
        bean.selectedTechnicalService = new TechnicalService(ts);
        doNothing().when(sps).validateTechnicalServiceCommunication(ts);

        // when
        bean.setSelectedTechnicalServiceKeyWithExceptionAndRefresh(ts.getKey());

        // then
        verify(bean.ui, never())
                .handleException(any(SaaSApplicationException.class));
    }

    @Test
    public void getNewTechnicalService_VerifyNoDefaultAccessType() {
        assertNotNull(bean.getNewTechnicalService());
        assertNull(bean.getNewTechnicalService().getAccessType());
    }

    @Test
    public void getSelectedTechnicalServiceKey_SelectedKeyFromSession()
            throws Exception {
        bean.getSessionBean().setSelectedTechnicalServiceKey(ts.getKey());

        TechnicalService service = bean.getSelectedTechnicalService();
        // as the list has not bean initialized yet, a service call must have
        // been done
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        assertNotNull(service);
        assertEquals(ts, service.getVo());
    }

    @Test
    public void create() throws Exception {
        TechnicalService newTS = bean.getNewTechnicalService();
        
        bean.create();
        verify(sps).createTechnicalService(eq(newTS.getVo()));

        bean.getTechnicalServices();
        // save must cause a reset of the list
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
    }

    @Test
    public void save() throws Exception {
        // given
        bean.setSelectedTechnicalServiceKey(ts.getKey());
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        reset(sps);

        // when
        bean.save();

        // then
        verify(sps).saveTechnicalServiceLocalization(eq(ts));
        // save must cause a reset of the list
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        verify(tagCloudBean, times(1)).resetTagsForMarketplace();
    }

    @Test
    public void delete() throws Exception {
        // given
        bean.setSelectedTechnicalServiceKey(ts.getKey());
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        reset(sps);
        String token = bean.getToken();
        bean.setToken(token);
        // when
        bean.delete();
        // then
        verify(sps).deleteTechnicalService(eq(ts));

        bean.getTechnicalServices();
        // save must cause a reset of the list
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
    }

    @Test
    public void delete_noValidToken() throws Exception {
        // given
        bean.setSelectedTechnicalServiceKey(ts.getKey());
        verify(sps).getTechnicalServices(
                eq(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        reset(sps);
        // when
        bean.delete();
        // then
        verify(sps, times(0)).deleteTechnicalService(eq(ts));
    }

    @Test
    public void isExportEnabled_ServiceSelected() throws Exception {
        // given
        List<TechnicalService> services = givenServicesSelected(1);
        doReturn(services).when(bean).getSelectableTechnicalServices();

        // when
        boolean isExportEnabled = bean.isExportEnabled();

        // then
        assertTrue(isExportEnabled);
    }

    @Test
    public void isExportEnabled_NoServiceSelected() throws Exception {
        // given
        List<TechnicalService> services = givenServicesNotSelected(1);
        doReturn(services).when(bean).getSelectableTechnicalServices();

        // when
        boolean isExportEnabled = bean.isExportEnabled();

        // then
        assertFalse(isExportEnabled);
    }

    @Test
    public void isAllServicesSelected_None() {
        bean.allServicesSelected = false;

        boolean actual = bean.isAllServicesSelected();

        assertEquals(false, actual);
    }

    @Test
    public void isAllServicesSelected_All() {
        bean.allServicesSelected = true;

        boolean actual = bean.isAllServicesSelected();

        assertEquals(true, actual);
    }

    @Test
    public void setAllServicesSelected_None() {
        bean.allServicesSelected = true;
        bean.selectableTechnicalServices = givenServicesSelected(5);

        bean.setAllServicesSelected(false);

        assertEquals(false, bean.allServicesSelected);
        for (TechnicalService ts : bean.getSelectableTechnicalServices()) {
            assertEquals(false, ts.isSelected());
        }
    }

    @Test
    public void setAllServicesSelected_All() {
        bean.allServicesSelected = false;
        bean.selectableTechnicalServices = givenServicesNotSelected(5);

        bean.setAllServicesSelected(true);

        assertEquals(true, bean.allServicesSelected);
        for (TechnicalService ts : bean.getSelectableTechnicalServices()) {
            assertEquals(true, ts.isSelected());
        }
    }

    @Test
    public void isDataAvailable() {
        bean.buf = data;

        assertTrue(bean.isDataAvailable());
    }

    @Test
    public void isDataAvailable_Negative() {
        bean.buf = null;

        assertFalse(bean.isDataAvailable());
    }

    @Test
    public void exportTechnicalServices() throws Exception {
        List<TechnicalService> list = givenServicesSelected(3);
        bean.selectableTechnicalServices = list;

        String outcome = bean.exportTechnicalServices();

        assertSame(data, bean.buf);
        assertSame(list, bean.selectableTechnicalServices);
        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void exportTechnicalServices_Exception() throws Exception {
        when(sps.exportTechnicalServices(anyListOf(VOTechnicalService.class)))
                .thenThrow(new ObjectNotFoundException());
        bean.selectableTechnicalServices = givenServicesSelected(3);
        bean.allServicesSelected = true;

        try {
            bean.exportTechnicalServices();
        } finally {
            assertNull(bean.buf);
            assertNull(bean.selectableTechnicalServices);
            assertFalse(bean.allServicesSelected);
        }
    }

    @Test
    public void getBaseUrlVisible() {

        TechServiceBean bean = new TechServiceBean();
        TechnicalService newTechnicalService = getNewTechnicalService();
        bean.setNewTechnicalService(newTechnicalService);

        assertFalse(bean.getBaseUrlVisible()); // accessType == null

        newTechnicalService.setAccessType(ServiceAccessType.EXTERNAL);
        assertTrue(bean.getBaseUrlVisible()); // accessType == EXTERNAL

        newTechnicalService.setAccessType(ServiceAccessType.DIRECT);
        assertFalse(bean.getBaseUrlVisible()); // accessType == DIRECT

        newTechnicalService.setAccessType(ServiceAccessType.LOGIN);
        assertTrue(bean.getBaseUrlVisible()); // accessType == LOGIN

        newTechnicalService.setAccessType(ServiceAccessType.USER);
        assertFalse(bean.getBaseUrlVisible()); // accessType == USER
    }

    @Test
    public void getProvisioningUrlVisible() {

        TechServiceBean bean = new TechServiceBean();
        TechnicalService newTechnicalService = getNewTechnicalService();
        bean.setNewTechnicalService(newTechnicalService);

        assertFalse(bean.getProvisioningUrlVisible()); // accessType == null

        newTechnicalService.setAccessType(ServiceAccessType.EXTERNAL);
        assertFalse(bean.getProvisioningUrlVisible()); // accessType == EXTERNAL

        newTechnicalService.setAccessType(ServiceAccessType.DIRECT);
        assertTrue(bean.getProvisioningUrlVisible()); // accessType == DIRECT

        newTechnicalService.setAccessType(ServiceAccessType.LOGIN);
        assertTrue(bean.getProvisioningUrlVisible()); // accessType == LOGIN

        newTechnicalService.setAccessType(ServiceAccessType.USER);
        assertTrue(bean.getProvisioningUrlVisible()); // accessType == USER
    }

    @Test
    public void getLoginPathVisible() {
        
        TechServiceBean bean = new TechServiceBean();
        TechnicalService newTechnicalService = getNewTechnicalService();
        bean.setNewTechnicalService(newTechnicalService);
        
        assertFalse(bean.getLoginPathVisible()); // accessType == null

        newTechnicalService.setAccessType(ServiceAccessType.EXTERNAL);
        assertFalse(bean.getLoginPathVisible()); // accessType == EXTERNAL

        newTechnicalService.setAccessType(ServiceAccessType.DIRECT);
        assertFalse(bean.getLoginPathVisible()); // accessType == DIRECT

        newTechnicalService.setAccessType(ServiceAccessType.LOGIN);
        assertTrue(bean.getLoginPathVisible()); // accessType == LOGIN

        newTechnicalService.setAccessType(ServiceAccessType.USER);
        assertFalse(bean.getLoginPathVisible()); // accessType == USER
    }

    @Test
    public void getAccessInfoVisible() {
        
        TechServiceBean bean = new TechServiceBean();
        TechnicalService newTechnicalService = getNewTechnicalService();
        bean.setNewTechnicalService(newTechnicalService);
        
        assertFalse(bean.getAccessInfoVisible()); // accessType == null

        newTechnicalService.setAccessType(ServiceAccessType.EXTERNAL);
        assertFalse(bean.getAccessInfoVisible()); // accessType == EXTERNAL

        newTechnicalService.setAccessType(ServiceAccessType.DIRECT);
        assertTrue(bean.getAccessInfoVisible()); // accessType == DIRECT

        newTechnicalService.setAccessType(ServiceAccessType.LOGIN);
        assertFalse(bean.getAccessInfoVisible()); // accessType == LOGIN

        newTechnicalService.setAccessType(ServiceAccessType.USER);
        assertTrue(bean.getAccessInfoVisible()); // accessType == USER
    }

    @Test
    public void isCheckingAccessInfoEmpty_nullSelectedTechnicalService() {
        assertFalse(bean.isCheckingAccessInfoEmpty());
    }

    @Test
    @Ignore
    public void isCheckingAccessInfoEmpty_en() {
        bean.getTechnicalServices();
        bean.setSelectedTechnicalServiceKey(ts.getKey());
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == null

        ts.setAccessType(ServiceAccessType.EXTERNAL);
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == EXTERNAL

        ts.setAccessType(ServiceAccessType.DIRECT);
        assertTrue(bean.isCheckingAccessInfoEmpty());// accessType == DIRECT

        ts.setAccessType(ServiceAccessType.LOGIN);
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == LOGIN

        ts.setAccessType(ServiceAccessType.USER);
        assertTrue(bean.isCheckingAccessInfoEmpty());// accessType == USER
    }

    @Test
    public void isCheckingAccessInfoEmpty_de() {
        bean.getTechnicalServices();
        doReturn("de").when(bean).getUserLanguage();
        bean.setSelectedTechnicalServiceKey(ts.getKey());
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == null

        ts.setAccessType(ServiceAccessType.EXTERNAL);
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == EXTERNAL

        ts.setAccessType(ServiceAccessType.DIRECT);
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == DIRECT

        ts.setAccessType(ServiceAccessType.LOGIN);
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == LOGIN

        ts.setAccessType(ServiceAccessType.USER);
        assertFalse(bean.isCheckingAccessInfoEmpty());// accessType == USER
    }

    @Test
    public void getOperations_SelectedServiceIsNull() {
        // given
        doReturn(null).when(bean).getSelectedTechnicalService();

        // when
        List<OperationRow> result = bean.getOperations();

        // then
        assertEquals(result.size(), 0);
    }

    @Test
    public void getOperations_OperationParameterIsNull() {
        // given
        TechnicalService services = givenServiceSelectedWithOperation(true);
        doReturn(services).when(bean).getSelectedTechnicalService();

        // when
        List<OperationRow> result = bean.getOperations();

        // then
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getVOOperation().getOperationId(),
                "operationId");
        assertEquals(result.get(0).getVOOperation().getOperationName(),
                "operationName");
        assertEquals(result.get(0).getVOOperation().getOperationDescription(),
                "operationDescription");
        assertNull(result.get(0).getVOOperation().getOperationParameters());
    }

    @Test
    public void getOperations() {
        // given
        TechnicalService services = givenServiceSelectedWithOperation(false);
        doReturn(services).when(bean).getSelectedTechnicalService();

        // when
        List<OperationRow> result = bean.getOperations();

        // then
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getVOOperation().getOperationId(),
                "operationId");
        assertEquals(result.get(0).getVOOperation().getOperationName(),
                "operationName");
        assertEquals(result.get(0).getVOOperation().getOperationDescription(),
                "operationDescription");
        assertEquals(result.get(1).getVOOperation().getOperationId(),
                "operationId");
        assertEquals(result.get(1).getVOOperation().getOperationName(),
                "operationName");
        assertEquals(result.get(1).getVOOperation().getOperationDescription(),
                "operationDescription");
        assertEquals(result.get(1).getVOOperation().getOperationParameters()
                .get(0).getParameterId(), "ParameterId1");
        assertEquals(result.get(1).getVOOperation().getOperationParameters()
                .get(0).getParameterName(), "ParameterName1");

    }

    private List<TechnicalService> givenServicesSelected(int numOfServices) {
        List<TechnicalService> services = new ArrayList<TechnicalService>();
        for (int i = 0; i < numOfServices; i++) {
            TechnicalService ts = new TechnicalService(
                    new VOTechnicalService());
            ts.setSelected(true);
            services.add(ts);
        }
        return services;
    }

    private List<TechnicalService> givenServicesNotSelected(int numOfServices) {
        List<TechnicalService> services = new ArrayList<TechnicalService>();
        for (int i = 0; i < numOfServices; i++) {
            TechnicalService ts = new TechnicalService(
                    new VOTechnicalService());
            ts.setSelected(false);
            services.add(ts);
        }
        return services;
    }

    private TechnicalService givenServiceSelectedWithOperation(
            boolean isParameterNull) {
        List<VOServiceOperationParameter> operationParameters = new ArrayList<VOServiceOperationParameter>();
        VOServiceOperationParameter operationParameter1 = new VOServiceOperationParameter();
        operationParameter1.setParameterId("ParameterId1");
        operationParameter1.setParameterName("ParameterName1");
        operationParameters.add(operationParameter1);

        List<VOTechnicalServiceOperation> operations = new ArrayList<VOTechnicalServiceOperation>();
        VOTechnicalServiceOperation operation = new VOTechnicalServiceOperation();
        operation.setOperationId("operationId");
        operation.setOperationName("operationName");
        operation.setOperationDescription("operationDescription");
        if (isParameterNull) {
            operation.setOperationParameters(null);
        } else {
            operation.setOperationParameters(operationParameters);
        }
        operations.add(operation);

        TechnicalService ts = new TechnicalService(new VOTechnicalService());
        ts.setTechnicalServiceOperations(operations);
        ts.setSelected(true);
        return ts;
    }

    private TechnicalService getNewTechnicalService() {
        VOTechnicalService voTechService = new VOTechnicalService();
        voTechService.setBillingIdentifier(DEFAULT_BILLING_ADAPTER);
        return new TechnicalService(voTechService);
    }
}
