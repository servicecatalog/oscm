/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.OperationServiceBean;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;

public class OperationServiceBeanTest {

    private OperationServiceBean opService = spy(new OperationServiceBean());
    private EntityManager em = mock(EntityManager.class);

    private OperationDAO opDAO = mock(OperationDAO.class);
    private ServiceInstanceDAO instanceDAO = mock(ServiceInstanceDAO.class);
    private APPlatformController controller = mock(APPlatformController.class);
    private APPConfigurationServiceBean configSevice = mock(APPConfigurationServiceBean.class);
    private static String USER_ID = "userId";
    private static String OP_ID = "opId";
    private static String TRACT_ID = "transactionId";
    private static long OP_KEY = 100L;
    private static String INSTANCE_ID = "instanceId";

    @Before
    public void setup() throws APPlatformException, BadResultException {
        opService.em = em;

        opService.operationDAO = opDAO;
        opService.instanceDAO = instanceDAO;
        opService.configService = configSevice;
        doNothing().when(em).persist(anyObject());
        doReturn(controller).when(opService).getController(
                any(ServiceInstance.class));
        doReturn(null).when(configSevice).getProvisioningSettings(
                any(ServiceInstance.class), any(ServiceUser.class));
    }

    @Test
    public void createProperties_nullParams() {
        // when
        Properties prop = opService.createProperties(USER_ID, OP_ID, null);

        // then
        assertEquals(2, prop.size());
        verifyMandatoryProperties(prop);
    }

    void verifyMandatoryProperties(Properties prop) {
        assertEquals(USER_ID,
                prop.get(APPlatformController.KEY_OPERATION_USER_ID));
        assertEquals(OP_ID, prop.get(APPlatformController.KEY_OPERATION_ID));
    }

    void verifyMandatoryNotInParam(Properties prop) {
        assertFalse(prop.containsKey(APPlatformController.KEY_OPERATION_ID));
        assertFalse(prop
                .containsKey(APPlatformController.KEY_OPERATION_USER_ID));
    }

    @Test
    public void createProperties() {
        // when
        Properties prop = opService.createProperties(USER_ID, OP_ID,
                Arrays.asList(opParam("p1", "v1"), opParam("p2", "v2")));

        // then
        assertEquals(4, prop.size());
        assertEquals("v1", prop.get("p1"));
        assertEquals("v2", prop.get("p2"));
        verifyMandatoryProperties(prop);
    }

    OperationParameter opParam(String name, String value) {
        OperationParameter opParam = new OperationParameter();
        opParam.setName(name);
        opParam.setValue(value);
        return opParam;
    }

    @Test
    public void isOperationFromQueue_positiv() {
        assertTrue(opService.isOperationFromQueue(10000L));
    }

    @Test
    public void isOperationFromQueue_negativ() {
        assertFalse(opService.isOperationFromQueue(0L));
    }

    @Test
    public void executeOperationFromQueue_emptyQueue() {
        // given
        doReturn(null).when(opDAO).getOperationFromQueue(anyString());
        doReturn(null).when(opService).execute(anyString(), anyString(),
                anyString(), anyString(), any(Properties.class), anyLong());

        // when
        opService.executeServiceOperationFromQueue("anyId");

        // then
        verify(opService, times(0)).execute(anyString(), anyString(),
                anyString(), anyString(), any(Properties.class), anyLong());
    }

    @SuppressWarnings("boxing")
    @Test
    public void execute_instanceNotAvailable_newOperation()
            throws ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        doReturn(false).when(si).isAvailable();
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        Properties properties = new Properties();
        properties.put("key", "value");
        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                properties, 0L);

        // then
        assertNull(result.getErrorMessage());

        verify(opDAO, times(1)).addOperationForQueue(eq(si),
                any(Properties.class), anyString());
    }

    @SuppressWarnings("boxing")
    @Test
    public void execute_instanceNotAvailable_operationFromQueue()
            throws ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        doReturn(false).when(si).isAvailable();
        doReturn(si).when(instanceDAO).getInstanceById(anyString());

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",

        null, 1000L);

        // then
        assertNull(result.getErrorMessage());
        verify(opDAO, times(0)).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

    }

    @Test
    public void execute_instanceAvailable_newOperation_noStatus()
            throws APPlatformException, ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        doReturn(null).when(controller).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                new Properties(), 0L);

        // then
        assertNull(result.getErrorMessage());
        verify(controller, times(1)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opDAO, times(0)).removeOperation(anyLong());
        assertEquals(ProvisioningStatus.COMPLETED, si.getProvisioningStatus());
    }

    @Test
    public void execute_instanceAvailable_newOperation_status()
            throws APPlatformException, ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        doReturn(new InstanceStatus())
                .when(controller)
                .executeServiceOperation(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                        any(ProvisioningSettings.class));
        Properties properties = new Properties();
        properties.put("key", "value");

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                properties, 0L);

        // then
        assertNull(result.getErrorMessage());
        verify(controller, times(1)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opDAO, times(0)).removeOperation(anyLong());
        assertEquals(ProvisioningStatus.WAITING_FOR_SYSTEM_OPERATION,
                si.getProvisioningStatus());
    }

    @Test
    public void execute_instanceAvailable_newOperation_statusReady()
            throws APPlatformException, ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        InstanceStatus status = new InstanceStatus();
        status.setIsReady(true);
        doReturn(status).when(controller).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                new Properties(), 0L);

        // then
        assertNull(result.getErrorMessage());
        verify(controller, times(1)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opDAO, times(0)).removeOperation(anyLong());
        verify(si).setRequestTime(anyLong());
        assertEquals(ProvisioningStatus.COMPLETED, si.getProvisioningStatus());
    }

    @Test
    public void execute_instanceAvailable_operationFromQueue_noStatus()
            throws APPlatformException, ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        doReturn(null).when(controller).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                new Properties(), 1000L);

        // then
        assertNull(result.getErrorMessage());
        verify(controller, times(1)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opDAO, times(0)).removeOperation(anyLong());
        assertEquals(ProvisioningStatus.COMPLETED, si.getProvisioningStatus());
    }

    @Test
    public void execute_instanceAvailable_operationFromQueue_status()
            throws APPlatformException, ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        doReturn(new InstanceStatus())
                .when(controller)
                .executeServiceOperation(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                        any(ProvisioningSettings.class));
        doReturn(getOperation()).when(em).getReference(eq(Operation.class),
                anyString());

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                new Properties(), 1000L);

        // then
        assertNull(result.getErrorMessage());
        verify(controller, times(1)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opService, times(1)).saveOperation(anyLong(),
                any(Properties.class), any(ServiceInstance.class), anyString());
        assertEquals(ProvisioningStatus.WAITING_FOR_SYSTEM_OPERATION,
                si.getProvisioningStatus());
    }

    @Test
    public void execute_instanceAvailable_exception()
            throws APPlatformException, ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doReturn(si).when(instanceDAO).getInstanceById(anyString());
        doThrow(new APPlatformException("error_message"))
                .when(controller)
                .executeServiceOperation(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                        any(ProvisioningSettings.class));

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                null, 0L);

        // then
        assertEquals("error_message", result.getErrorMessage());
        verify(controller, times(1)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opDAO, times(1)).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

        assertEquals(ProvisioningStatus.COMPLETED, si.getProvisioningStatus());
    }

    @Test
    public void executeOperationFromQueue() {
        // given
        Operation op = getOperation();
        doReturn(op).when(opDAO).getOperationFromQueue(anyString());
        doReturn(null).when(opService).execute(anyString(), anyString(),
                anyString(), anyString(), any(Properties.class), anyLong());

        // when
        opService.executeServiceOperationFromQueue("anyId");

        // then

        verify(opService, times(1)).execute(USER_ID, INSTANCE_ID, TRACT_ID,
                OP_ID, new Properties(), OP_KEY);
    }

    @Test
    public void execute_noInstance() throws APPlatformException,
            ServiceInstanceNotFoundException {
        // given
        ServiceInstance si = spy(new ServiceInstance());
        si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        doThrow(
                new ServiceInstanceNotFoundException("error_message",
                        new Throwable())).when(instanceDAO).getInstanceById(
                anyString());

        // when
        OperationResult result = opService.execute("any", "any", "any", "any",
                new Properties(), 1000L);

        // then
        assertEquals("error_message", result.getErrorMessage());
        verify(controller, times(0)).executeServiceOperation(anyString(),
                anyString(), anyString(), anyString(),
                anyListOf(org.oscm.app.v2_0.data.OperationParameter.class),
                any(ProvisioningSettings.class));
        verify(opDAO, times(0)).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

        assertEquals(ProvisioningStatus.COMPLETED, si.getProvisioningStatus());
    }

    @Test
    public void addOperationToQueue_newOperation() {
        // given
        doReturn(null).when(opDAO).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

        // when
        opService.addOperationToQueue(0L, new Properties(),
                new ServiceInstance(), new String());

        // then
        verify(opDAO, times(1)).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

    }

    @Test
    public void addOperationToQueue_existingOperation() {
        // given
        doReturn(null).when(opDAO).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

        // when
        opService.addOperationToQueue(1000L, new Properties(),
                new ServiceInstance(), new String());

        // then
        verify(opDAO, times(0)).addOperationForQueue(
                any(ServiceInstance.class), any(Properties.class), anyString());

    }

    @Test
    public void removeOperationFromQueue_nonexistingOperation() {
        // given
        doNothing().when(opDAO).removeOperation(anyLong());

        // when
        opService.removeOperationFromQueue(0L);

        // then
        verify(opDAO, times(0)).removeOperation(anyLong());
    }

    @Test
    public void removeOperationFromQueue_existingOperation() {
        // given
        doNothing().when(opDAO).removeOperation(anyLong());

        // when
        opService.removeOperationFromQueue(10000L);

        // then
        verify(opDAO, times(1)).removeOperation(anyLong());
    }

    private Operation getOperation() {
        ServiceInstance si = new ServiceInstance();
        si.setInstanceId(INSTANCE_ID);
        Operation op = new Operation();
        op.setOperationId(OP_ID);
        op.setUserId(USER_ID);
        op.setTransactionId(TRACT_ID);
        op.setServiceInstance(si);
        op.setTkey(OP_KEY);
        return op;
    }
}
