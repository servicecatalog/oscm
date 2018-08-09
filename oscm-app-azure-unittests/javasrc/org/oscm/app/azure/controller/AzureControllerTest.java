/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/

package org.oscm.app.azure.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.azure.data.FlowState;
import org.oscm.app.v1_0.data.InstanceDescription;
import org.oscm.app.v1_0.data.InstanceStatus;
import org.oscm.app.v1_0.data.InstanceStatusUsers;
import org.oscm.app.v1_0.data.LocalizedText;
import org.oscm.app.v1_0.data.OperationParameter;
import org.oscm.app.v1_0.data.ProvisioningSettings;
import org.oscm.app.v1_0.data.ServiceUser;
import org.oscm.app.v1_0.exceptions.APPlatformException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oscm.app.azure.controller.PropertyHandler.API_USER_NAME;
import static org.oscm.app.azure.controller.PropertyHandler.CLIENT_ID;
import static org.oscm.app.azure.controller.PropertyHandler.CLIENT_SECRET;
import static org.oscm.app.azure.controller.PropertyHandler.FLOW_STATUS;
import static org.oscm.app.azure.controller.PropertyHandler.REGION;
import static org.oscm.app.azure.controller.PropertyHandler.RESOURCE_GROUP_NAME;
import static org.oscm.app.azure.controller.PropertyHandler.TENANT_ID;

public class AzureControllerTest {

    private static final String INSTANCE_ID = "instanceId";
    private static final String USER_ID = "userId";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String OPERATION_ID_START = "START_VIRTUAL_SYSTEM";
    private static final String OPERATION_ID_STOP = "STOP_VIRTUAL_SYSTEM";
    private static final String OPERATION_ID_INVALID = "operationId";

    private AzureController ctrl;
    private ProvisioningSettings provSettingsMock;
    private AzureCommunication azureCommMock;
    @Before
    public void setup() {
        azureCommMock = mock(AzureCommunication.class);
        ctrl = new AzureController(){
            @Override
            public AzureCommunication getAzureCommunication(PropertyHandler ph) {
                return azureCommMock;
            }
        };
        provSettingsMock = mock(ProvisioningSettings.class);
        final HashMap<String, String> parameters = fillParameters("1");
        doReturn(parameters).when(provSettingsMock).getParameters();

        ArrayList regionsList = new ArrayList();
        regionsList.add("region1");
        regionsList.add("region2");

        when(azureCommMock.getAvailableRegions()).thenReturn(regionsList);
    }

    @Test
    public void createInstanceTest() throws APPlatformException {
        // given
        // when
        final InstanceDescription instance = ctrl.createInstance(provSettingsMock);
        // then
        assertTrue(instance != null);
        assertTrue(instance.getInstanceId().length() > 0);
        assertTrue(instance.getInstanceId().startsWith("azure"));
    }

    @Test
    public void deleteInstanceTest() throws APPlatformException {
        // given
        // when
        final InstanceStatus instanceStatus = ctrl.deleteInstance(INSTANCE_ID, provSettingsMock);
        // then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.DELETION_REQUESTED.toString()));
    }

    @Test
    public void modifyInstanceTest() throws APPlatformException {
        // given
        final ProvisioningSettings parametersMockWithFlowState = getParametersMockWithFlowState("2", FlowState.FINISHED);
        // when
        final InstanceStatus instanceStatus = ctrl.modifyInstance(INSTANCE_ID, provSettingsMock, parametersMockWithFlowState);
        // then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.MODIFICATION_REQUESTED.toString()));
    }

    @Test
    public void getInstanceStatusTest() throws APPlatformException {
        // given
        // when
        final InstanceStatus instanceStatus = ctrl.getInstanceStatus(INSTANCE_ID, provSettingsMock);
        // then
        assertTrue(instanceStatus != null);

    }

    @Test
    public void notifyInstanceTest() throws APPlatformException {
        //given
        Properties propertiesMock = mock(Properties.class);
        when(propertiesMock.get(any(String.class))).thenReturn("notfinish");
        //when
        final InstanceStatus instanceStatus = ctrl.notifyInstance(INSTANCE_ID, provSettingsMock, propertiesMock);
        //then
        assertTrue(instanceStatus == null);

    }

    @Test
    public void notifyInstanceTest_finish() throws APPlatformException {
        //given
        Properties propertiesMock = mock(Properties.class);
        when(propertiesMock.get(any(String.class))).thenReturn("finish");
        final ProvisioningSettings parametersMockWithFlowState = getParametersMockWithFlowState("2", FlowState.MANUAL);
        //when
        final InstanceStatus instanceStatus = ctrl.notifyInstance(INSTANCE_ID, parametersMockWithFlowState, propertiesMock);
        //then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.FINISHED.toString()));

    }

    @Test(expected = APPlatformException.class)
    public void notifyInstanceTest_exception() throws APPlatformException {
        //given
        Properties propertiesMock = mock(Properties.class);
        when(propertiesMock.get(any(String.class))).thenReturn("finish");
        // invalid flow state
        final ProvisioningSettings parametersMockWithFlowState = getParametersMockWithFlowState("2", FlowState.CREATING);
        //when
        final InstanceStatus instanceStatus = ctrl.notifyInstance(INSTANCE_ID, parametersMockWithFlowState, propertiesMock);
        //then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.FINISHED.toString()));

    }

    @Test
    public void activateInstanceTest() throws APPlatformException {
        //given
        //when
        final InstanceStatus instanceStatus = ctrl.activateInstance(INSTANCE_ID, provSettingsMock);
        //then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.ACTIVATION_REQUESTED.toString()));
    }

    @Test
    public void deactivateInstanceTest() throws APPlatformException {
        //given
        //when
        final InstanceStatus instanceStatus = ctrl.deactivateInstance(INSTANCE_ID, provSettingsMock);
        //then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.DEACTIVATION_REQUESTED.toString()));
    }

    @Test
    public void executeServiceOperation_start() throws APPlatformException {
        //given
        List<OperationParameter> opParametersMock = null;
        
        //when
        final InstanceStatus instanceStatus = ctrl.executeServiceOperation(USER_ID,
                INSTANCE_ID,
                TRANSACTION_ID,
                OPERATION_ID_START,
                opParametersMock,
                provSettingsMock);
        //then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.START_REQUESTED.toString()));
    }

    @Test
    public void executeServiceOperation_stop() throws APPlatformException {
        //given
        List<OperationParameter> opParametersMock = null;

        //when
        final InstanceStatus instanceStatus = ctrl.executeServiceOperation(USER_ID,
                INSTANCE_ID,
                TRANSACTION_ID,
                OPERATION_ID_STOP,
                opParametersMock,
                provSettingsMock);
        //then
        assertTrue(instanceStatus != null);
        assertTrue(instanceStatus.getChangedParameters().get(FLOW_STATUS)
                .equals(FlowState.STOP_REQUESTED.toString()));
    }

    @Test
    public void executeServiceOperation_invalidOperation() throws APPlatformException {
        //given
        List<OperationParameter> opParametersMock = null;

        //when
        final InstanceStatus instanceStatus = ctrl.executeServiceOperation(USER_ID,
                INSTANCE_ID,
                TRANSACTION_ID,
                OPERATION_ID_INVALID,
                opParametersMock,
                provSettingsMock);
        //then
        assertTrue(instanceStatus == null);
    }

    @Test
    public void executeServiceOperation_invalidArguments() throws APPlatformException {
        //given
        List<OperationParameter> opParametersMock = null;
        //when
        final InstanceStatus instanceStatus1 = ctrl.executeServiceOperation(USER_ID,
                INSTANCE_ID,
                TRANSACTION_ID,
                OPERATION_ID_START,
                opParametersMock,
                null);
        //then
        assertTrue(instanceStatus1 == null);
        //when
        final InstanceStatus instanceStatus2 = ctrl.executeServiceOperation(USER_ID,
                null,
                TRANSACTION_ID,
                OPERATION_ID_START,
                opParametersMock,
                provSettingsMock);
        //then
        assertTrue(instanceStatus2 == null);
        //when
        final InstanceStatus instanceStatus3 = ctrl.executeServiceOperation(USER_ID,
                INSTANCE_ID,
                TRANSACTION_ID,
                null,
                opParametersMock,
                provSettingsMock);
        //then
        assertTrue(instanceStatus3 == null);
    }

    @Test
    public void createUsers() throws APPlatformException {
        //given
        List<ServiceUser> usersList = null;
        //when
        final InstanceStatusUsers users = ctrl.createUsers(INSTANCE_ID, provSettingsMock, usersList);
        //then
        // not implemented
        assertTrue(users == null);
    }

    @Test
    public void deleteUsers() throws APPlatformException {
        //given
        List<ServiceUser> usersList = null;
        //when
        final InstanceStatus instanceStatus = ctrl.deleteUsers(INSTANCE_ID, provSettingsMock, usersList);
        //then
        // not implemented
        assertTrue(instanceStatus == null);
    }

    @Test
    public void updateUsers() throws APPlatformException {
        //given
        List<ServiceUser> userList = null;
        //when
        final InstanceStatus instanceStatus = ctrl.updateUsers(INSTANCE_ID, provSettingsMock, userList);
        //then
        // not implemented
        assertTrue(instanceStatus == null);
    }

    @Test
    public void getControllerStatus() throws APPlatformException {
        //given
        //when
        final List<LocalizedText> controllerStatus = ctrl.getControllerStatus(provSettingsMock);
        //then
        // not implemented
        assertTrue(controllerStatus == null);
    }

    @Test
    public void getOperationParameters() throws APPlatformException {
        //given
        //when
        final List<OperationParameter> operationParameters = ctrl.getOperationParameters(USER_ID, INSTANCE_ID, OPERATION_ID_START, provSettingsMock);
        //then
        // not implemented
        assertTrue(operationParameters == null);
    }

    @Test
    public void setControllerSettings() {
        //given
        //when
        ctrl.setControllerSettings(provSettingsMock);
        //then
        // not implemented
    }

    private HashMap<String, String> fillParameters(String modifier) {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put(RESOURCE_GROUP_NAME, "res" + modifier);
        parameters.put(TENANT_ID, "tenant1" + modifier);
        parameters.put(CLIENT_ID, "client1" + modifier);
        parameters.put(API_USER_NAME, "client1" + modifier);
        parameters.put(CLIENT_SECRET, "secret1" + modifier);
        parameters.put(REGION, "region" + modifier);

        return parameters;
    }

    private ProvisioningSettings getParametersMockWithFlowState(String modifier, FlowState flowState) {
        ProvisioningSettings provSettingsMock = mock(ProvisioningSettings.class);
        final HashMap<String, String> mockParameters = fillParameters(modifier);
        mockParameters.put(FLOW_STATUS, flowState.toString());
        doReturn(mockParameters).when(provSettingsMock).getParameters();
        return provSettingsMock;

    }
}
