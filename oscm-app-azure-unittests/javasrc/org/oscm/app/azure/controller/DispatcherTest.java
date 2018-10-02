/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import com.microsoft.azure.storage.table.Ignore;
import com.microsoft.windowsazure.exception.ServiceException;
import org.junit.Before;
import org.junit.Test;
import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.azure.data.AccessInfo;
import org.oscm.app.azure.data.AzureState;
import org.oscm.app.azure.data.FlowState;
import org.oscm.app.azure.exception.AzureClientException;
import org.oscm.app.azure.exception.AzureServiceException;
import org.oscm.app.v2_0.data.*;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


/**
 * Created by PLGrubskiM on 2017-03-21.
 */
public class DispatcherTest {

    private static final String INSTANCE_ID = "instanceId";
    private static final String TENANT_ID = "tenantId";
    private static final String CLENT_ID = "clientId";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String MAIL_FOR_COMPLETION = "mail";
    private static final String RESOURCE_GROUP_NAME = "resgroupname";
    private static final String TEMPLATE_BASE_USERNAME = "http://template.test";
    private static final String TEMPLATE_NAME = "templatename";
    private static final String REGION = "region";

    private Dispatcher dispatcher;
    private APPlatformService mockPlatformService;
    private PropertyHandler ph;
    private AzureCommunication mockAzureComm;
    private ProvisioningSettings settings;

    @Before
    public void setUp() {
        mockAzureComm = mock(AzureCommunication.class);
        mockPlatformService = mock(APPlatformService.class);
        HashMap<String, Setting> configSettings = null;
        settings = new ProvisioningSettings(createParameters(), configSettings, "en");
        ph = new PropertyHandler(settings);
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph) {
            @Override
            public AzureCommunication getAzureCommunication() {
                return mockAzureComm;
            }
        };

    }

    @Test
    public void dispatchTest_provisioning_creation_requested() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(),
                FlowState.CREATION_REQUESTED.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo(FlowState.STARTING.toString())).thenReturn(output);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.CREATING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.CREATING.toString()));
    }

    @Test
    public void dispatchTest_provisioning_creating() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATING.toString(),
                FlowState.CREATING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.FINISHED.toString()));
    }

    @Test
    @Ignore
    public void dispatchTest_provisioning_creating_2() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(MAIL_FOR_COMPLETION, MAIL_FOR_COMPLETION));
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATING.toString(), FlowState.CREATING.toString()));
        HashMap<String, Setting> configSettingsMap = new HashMap<>();
        configSettingsMap.put("API_USER_NAME", new Setting("username", "username"));
        settings.setConfigSettings(configSettingsMap);
        ph.getSettings().setSubscriptionId("subId");
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        User user = mock(User.class);
        when(user.getLocale()).thenReturn("en");
        when(mockPlatformService.getEventServiceUrl()).thenReturn("http://someurl.test");
        when(mockPlatformService.authenticate(any(String.class), any(PasswordAuthentication.class))).thenReturn(user);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.MANUAL.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.MANUAL.toString()));
    }

    @Test
    public void dispatchTest_provisioning_creating_3() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(MAIL_FOR_COMPLETION, MAIL_FOR_COMPLETION));
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATING.toString(), FlowState.CREATING.toString()));
        HashMap<String, Setting> configSettingsMap = new HashMap<>();
        configSettingsMap.put("API_USER_NAME", new Setting("username", "username"));
        settings.setConfigSettings(configSettingsMap);
        ph.getSettings().setSubscriptionId("subId");
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(false);
        when(azureState.isFailed()).thenReturn(false);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        User user = mock(User.class);
        when(user.getLocale()).thenReturn("en");
        when(mockPlatformService.getEventServiceUrl()).thenReturn("http://someurl.test");
        when(mockPlatformService.authenticate(any(String.class), any(PasswordAuthentication.class))).thenReturn(user);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.CREATING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.CREATING.toString()));
    }

    @Test(expected = APPlatformException.class)
    public void dispatchTest_provisioning_creating2() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATING.toString(), FlowState.CREATING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(false);
        when(azureState.isFailed()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        verify(mockPlatformService, times(1)).sendMail(any(List.class), any(String.class), any(String.class));
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS)
                .equals(FlowState.FINISHED.toString()));
    }

    @Test
    public void dispatchTest_modification_requested() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.MODIFICATION_REQUESTED.toString(), FlowState.MODIFICATION_REQUESTED.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(false);
        when(azureState.isFailed()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.UPDATING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.UPDATING.toString()));
    }

    @Test(expected = APPlatformException.class)
    public void dispatchTest_updating_fail() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.UPDATING.toString(), FlowState.UPDATING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(false);
        when(azureState.isFailed()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.UPDATING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS)
                .equals(FlowState.UPDATING.toString()));
    }

    @Test
    public void dispatchTest_updating() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.UPDATING.toString(), FlowState.UPDATING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.FINISHED.toString()));
    }

    @Test
    public void dispatchTest_updating2() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.UPDATING.toString(), FlowState.UPDATING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(false);
        when(azureState.isFailed()).thenReturn(false);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.UPDATING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.UPDATING.toString()));
    }

    @Test
    @Ignore
    public void dispatchTest_updating3() throws APPlatformException {
        // given
        HashMap<String, Setting> configSettingsMap = new HashMap<>();
        configSettingsMap.put("API_USER_NAME", new Setting("username", "username"));
        settings.setConfigSettings(configSettingsMap);
        ph.getSettings().setSubscriptionId("subId");
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.UPDATING.toString(), FlowState.UPDATING.toString()));
        settings.getParameters().put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(MAIL_FOR_COMPLETION, MAIL_FOR_COMPLETION));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        User user = mock(User.class);
        when(user.getLocale()).thenReturn("en");
        when(mockPlatformService.authenticate(any(String.class), any(PasswordAuthentication.class))).thenReturn(user);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.FINISHED.toString()));
    }

    @Test
    public void dispatchTest_deletion_requested() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.DELETION_REQUESTED.toString(),
                FlowState.DELETION_REQUESTED.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.DELETING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.DELETING.toString()));
    }

    @Test
    public void dispatchTest_deleting() throws APPlatformException {
        // given
        ph.getSettings().setSubscriptionId("subId");
        settings.getParameters().put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(MAIL_FOR_COMPLETION, MAIL_FOR_COMPLETION));
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.DELETING.toString(), FlowState.DELETING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isDeleted()).thenReturn(true);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeletingState()).thenReturn(azureState);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        User user = mock(User.class);
        when(user.getLocale()).thenReturn("en");
        when(mockPlatformService.authenticate(any(String.class), any(PasswordAuthentication.class))).thenReturn(user);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        verify(mockPlatformService, times(1)).sendMail(any(List.class), any(String.class), any(String.class));
        assertTrue(ph.getFlowState().toString().equals(FlowState.DESTROYED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.DESTROYED.toString()));
    }

    @Test
    public void dispatchTest_deleting_2() throws APPlatformException {
        // given
        ph.getSettings().setSubscriptionId("subId");
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.DELETING.toString(), FlowState.DELETING.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isFailed()).thenReturn(false);
        when(azureState.isSucceeded()).thenReturn(false);
        when(mockAzureComm.getDeletingState()).thenReturn(azureState);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        User user = mock(User.class);
        when(user.getLocale()).thenReturn("en");
        when(mockPlatformService.authenticate(any(String.class), any(PasswordAuthentication.class))).thenReturn(user);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.DELETING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.DELETING.toString()));
    }

    @Test
    public void dispatchTest_finished() throws APPlatformException {
        // given
        ph.getSettings().setSubscriptionId("subId");
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.FINISHED.toString(), FlowState.FINISHED.toString()));
        AccessInfo accessInfo = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(accessInfo);
        AzureState azureState = mock(AzureState.class);
        when(azureState.isDeleted()).thenReturn(true);
        when(azureState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getDeletingState()).thenReturn(azureState);
        when(mockAzureComm.getDeploymentState()).thenReturn(azureState);
        User user = mock(User.class);
        when(user.getLocale()).thenReturn("en");
        when(mockPlatformService.authenticate(any(String.class), any(PasswordAuthentication.class))).thenReturn(user);
        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.FINISHED.toString()));
    }

    @Test(expected = APPlatformException.class)
    public void dispatchTest_provisioning_exception1() throws APPlatformException {
        // given
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph){
            @Override
            protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                            InstanceStatus status) throws APPlatformException {
                throw new APPlatformException("ex");
            }

        };
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(), FlowState.CREATION_REQUESTED.toString()));

        // when
        dispatcher.dispatch();

        // then
    }

    @Test(expected = SuspendException.class)
    public void dispatchTest_provisioning_exception2() throws APPlatformException {
        // given
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph){
            @Override
            protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                            InstanceStatus status) throws APPlatformException {
                throw new AzureServiceException("ex");
            }

        };
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(), FlowState.CREATION_REQUESTED.toString()));

        // when
        dispatcher.dispatch();

        // then
    }

    @Test(expected = InstanceNotAliveException.class)
    public void dispatchTest_provisioning_exception3() throws APPlatformException {
        // given
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph){
            @Override
            protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                            InstanceStatus status) throws APPlatformException {
                ServiceException azureEx = new ServiceException();
                azureEx.getError().setCode("ResourceGroupNotFound");
                AzureServiceException e = new AzureServiceException(azureEx);
                throw e;
            }

        };
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(),
                FlowState.CREATION_REQUESTED.toString()));

        // when
        dispatcher.dispatch();

        // then
    }

    @Test(expected = SuspendException.class)
    public void dispatchTest_provisioning_exception4() throws APPlatformException {
        // given
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph){
            @Override
            protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                            InstanceStatus status) throws APPlatformException {
                throw new AzureClientException("ex");
            }
        };
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(),
                FlowState.CREATION_REQUESTED.toString()));

        // when
        dispatcher.dispatch();

        // then
    }

    @Test(expected = InstanceNotAliveException.class)
    public void dispatchTest_provisioning_exception5() throws APPlatformException {
        // given
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph){
            @Override
            protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                            InstanceStatus status) throws APPlatformException {
                ServiceException azureEx = new ServiceException();
                azureEx.getError().setCode("ResourceGroupNotFound");
                AzureServiceException e = new AzureServiceException(azureEx);
                throw e;
            }

        };
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(),
                FlowState.CREATION_REQUESTED.toString()));

        // when
        dispatcher.dispatch();

        // then
    }

    @Test(expected = Exception.class)
    public void dispatchTest_provisioning_exception6() throws APPlatformException {
        // given
        dispatcher = new Dispatcher(mockPlatformService, INSTANCE_ID, ph){
            @Override
            protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                            InstanceStatus status) throws APPlatformException {
                throw new NullPointerException("ex");
            }

        };
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.CREATION_REQUESTED.toString(),
                FlowState.CREATION_REQUESTED.toString()));

        // when
        dispatcher.dispatch();

        // then
    }

    @Test
    public void dispatchTest_operation_startRequested() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.START_REQUESTED.toString(),
                FlowState.START_REQUESTED.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo(FlowState.STARTING.toString())).thenReturn(output);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.STARTING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.STARTING.toString()));
    }

    @Test
    public void dispatchTest_operation_starting() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.STARTING.toString(),
                FlowState.STARTING.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(output);
        AzureState mockState = mock(AzureState.class);
        when(mockState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getStartingState()).thenReturn(mockState);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.FINISHED.toString()));
    }

    @Test
    public void dispatchTest_operation_starting2() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.STARTING.toString(),
                FlowState.STARTING.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("RUNNING")).thenReturn(output);
        AzureState mockState = mock(AzureState.class);
        when(mockState.isSucceeded()).thenReturn(false);
        when(mockAzureComm.getStartingState()).thenReturn(mockState);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.STARTING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.STARTING.toString()));
    }

    @Test
    public void dispatchTest_operation_stopRequested() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.STOP_REQUESTED.toString(),
                FlowState.STOP_REQUESTED.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo(FlowState.STOPPING.toString())).thenReturn(output);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.STOPPING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.STOPPING.toString()));
    }

    @Test
    public void dispatchTest_operation_stopping() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.STOPPING.toString(),
                FlowState.STOPPING.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("STOPPED")).thenReturn(output);
        AzureState mockState = mock(AzureState.class);
        when(mockState.isSucceeded()).thenReturn(true);
        when(mockAzureComm.getStoppingState()).thenReturn(mockState);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FINISHED.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.FINISHED.toString()));
    }

    @Test
    public void dispatchTest_operation_stopping_2() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.STOPPING.toString(),
                FlowState.STOPPING.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo("STOPPED")).thenReturn(output);
        AzureState mockState = mock(AzureState.class);
        when(mockState.isSucceeded()).thenReturn(false);
        when(mockAzureComm.getStoppingState()).thenReturn(mockState);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.STOPPING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.STOPPING.toString()));
    }

    @Test
    public void dispatchTest_activation() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.ACTIVATION_REQUESTED.toString(),
                FlowState.ACTIVATION_REQUESTED.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo(FlowState.STARTING.toString())).thenReturn(output);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.STARTING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.STARTING.toString()));
    }

    @Test
    public void dispatchTest_activation2() throws APPlatformException {
        // given
        settings.getParameters().put(PropertyHandler.FLOW_STATUS, new Setting(FlowState.DEACTIVATION_REQUESTED.toString(),
                FlowState.DEACTIVATION_REQUESTED.toString()));

        AccessInfo output = mock(AccessInfo.class);
        when(mockAzureComm.getAccessInfo(FlowState.STOPPING.toString())).thenReturn(output);

        // when
        final InstanceStatus instanceStatus = dispatcher.dispatch();

        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.STOPPING.toString()));
        assertTrue(instanceStatus.getChangedParameters().get(PropertyHandler.FLOW_STATUS).getValue()
                .equals(FlowState.STOPPING.toString()));
    }

    @Test
    @org.junit.Ignore
    public void testTest() throws Exception {
        // given
        // when
        dispatcher.test(ph);
        // then
        assertTrue(ph.getFlowState().toString().equals(FlowState.FAILED.toString()));
    }

    private HashMap<String, Setting> createParameters() {
        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put(PropertyHandler.CLIENT_ID, new Setting(CLENT_ID, CLENT_ID));
        parameters.put(PropertyHandler.TENANT_ID, new Setting(TENANT_ID, TENANT_ID));
        parameters.put(PropertyHandler.API_USER_NAME, new Setting(USERNAME, USERNAME));
        parameters.put(PropertyHandler.API_USER_PWD, new Setting(PASSWORD, PASSWORD));
        parameters.put(PropertyHandler.RESOURCE_GROUP_NAME, new Setting(RESOURCE_GROUP_NAME, RESOURCE_GROUP_NAME));
        parameters.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(TEMPLATE_BASE_USERNAME, TEMPLATE_BASE_USERNAME));
        parameters.put(PropertyHandler.TEMPLATE_NAME, new Setting(TEMPLATE_NAME, TEMPLATE_NAME));
        parameters.put(PropertyHandler.REGION, new Setting(REGION, REGION));
        return parameters;
    }
}
