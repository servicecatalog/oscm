/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 27, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oscm.app.iaas.data.DiskImage;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.data.ResourceType;
import org.oscm.app.iaas.data.VServerStatus;
import org.oscm.app.iaas.data.VSystemStatus;
import org.oscm.app.iaas.exceptions.MissingResourceException;
import org.oscm.app.iaas.intf.FWCommunication;
import org.oscm.app.iaas.intf.VDiskCommunication;
import org.oscm.app.iaas.intf.VServerCommunication;
import org.oscm.app.iaas.intf.VSystemCommunication;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author farmaki
 * 
 */
public class VServerProcessorBeanTest {

    private VServerProcessorBean vServerProcessor;
    private APPlatformService platformService;

    private HashMap<String, Setting> parameters;
    HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private PropertyHandler paramHandler;
    private final String DISKIDORNAME1 = "DISKIDORNAME1";
    private final String DISKIDORNAME2 = "DISKIDORNAME2";
    private final String DISKIDORNAME3 = "DISKIDORNAME3";
    private final String DISKIDORNAME4 = "DISKIDORNAME4";
    @Captor
    private ArgumentCaptor<String> subject;
    @Captor
    private ArgumentCaptor<String> text;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        vServerProcessor = spy(new VServerProcessorBean());
        vServerProcessor.vserverComm = mock(VServerCommunication.class);
        vServerProcessor.vsysComm = mock(VSystemCommunication.class);
        vServerProcessor.fwComm = mock(FWCommunication.class);
        vServerProcessor.vdiskInfo = mock(VDiskCommunication.class);
        platformService = mock(APPlatformService.class);
        doReturn("RUNNING").when(vServerProcessor.fwComm).getFirewallStatus(
                any(PropertyHandler.class));
        doReturn(Boolean.TRUE).when(platformService).lockServiceInstance(
                anyString(), anyString(), any(PasswordAuthentication.class));
        doNothing().when(platformService).unlockServiceInstance(anyString(),
                anyString(), any(PasswordAuthentication.class));
        doNothing().when(platformService).sendMail(anyListOf(String.class),
                subject.capture(), text.capture());
        User user = new User();
        user.setLocale("de");
        doReturn(user).when(platformService).authenticate(anyString(),
                any(PasswordAuthentication.class));
        vServerProcessor.setPlatformService(platformService);

        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings, "en");
        settings.setSubscriptionId("subId");
        paramHandler = new PropertyHandler(settings);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATION_REQUESTED_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATION_REQUESTED, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATION_REQUESTED()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATION_REQUESTED, null);

        // then
        assertEquals(FlowState.VSERVER_CREATING, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATING_VServerIsDeploying()
            throws Exception {
        // given
        doReturn(VServerStatus.DEPLOYING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATING, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATING() throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATING, null);
        // then
        assertEquals(FlowState.VSERVER_CREATED, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATED_additionalDiskSelected()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isAdditionalDiskSelected(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATED, null);

        // then
        assertEquals(FlowState.VSDISK_CREATION_REQUESTED, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATED_VServerIsRunning()
            throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATED, null);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATED_VServerIsStopped()
            throws Exception {
        // given
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATED, null);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_CREATED_VServerIsStarting()
            throws Exception {
        // given
        doReturn(VServerStatus.STARTING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_CREATED, null);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_STARTING() throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_STARTING, null);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_STARTED() throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_STARTED, null);

        // then
        assertEquals(FlowState.VSERVER_RETRIEVEGUEST, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_RETRIEVEGUEST() throws Exception {
        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_RETRIEVEGUEST, null);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageCreationProcess_VSERVER_RETRIEVEGUEST_manualOperation()
            throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSERVER_RETRIEVEGUEST, null);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_CREATION_REQUESTED()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_CREATION_REQUESTED, null);

        // then
        assertEquals(FlowState.VSDISK_CREATING, newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_CREATING_VDiskIsNotDeployed()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_CREATING, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_CREATING() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isVDiskDeployed(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_CREATING, null);

        // then
        assertEquals(FlowState.VSDISK_CREATED, newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_CREATED_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_CREATED, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_CREATED() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_CREATED, null);

        // then
        assertEquals(FlowState.VSDISK_ATTACHING, newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_ATTACHING_DiskIsNotAttached()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_ATTACHING, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_ATTACHING() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isVDiskAttached(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_ATTACHING, null);

        // then
        assertEquals(FlowState.VSDISK_ATTACHED, newState);
    }

    @Test
    public void manageCreationProcess_VSDISK_ATTACHED() throws Exception {
        // when
        FlowState newState = vServerProcessor.manageCreationProcess(null, null,
                paramHandler, FlowState.VSDISK_ATTACHED, null);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_MODIFICATION_REQUESTED_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_MODIFICATION_REQUESTED,
                null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_MODIFICATION_REQUESTED()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(FlowState.VSERVER_STOPPING_FOR_MODIFICATION).when(
                vServerProcessor.vserverComm).modifyVServerAttributes(
                any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_MODIFICATION_REQUESTED,
                null);

        // then
        assertEquals(FlowState.VSERVER_STOPPING_FOR_MODIFICATION, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STOPPING_FOR_MODIFICATION_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler,
                FlowState.VSERVER_STOPPING_FOR_MODIFICATION, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STOPPING_FOR_MODIFICATION_VServerIsRunning()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler,
                FlowState.VSERVER_STOPPING_FOR_MODIFICATION, null);

        // then the state is not modified.
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STOPPING_FOR_MODIFICATION()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getNonErrorVServerStatus(any(PropertyHandler.class));

        doReturn(FlowState.VSERVER_UPDATING).when(vServerProcessor.vserverComm)
                .modifyVServerAttributes(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler,
                FlowState.VSERVER_STOPPING_FOR_MODIFICATION, null);

        // then
        assertEquals(FlowState.VSERVER_UPDATING, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATING_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATING, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATING_emptyVServerIds()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        paramHandler.setVserverId("vServerId");

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATING, null);

        // then
        assertEquals(FlowState.VSERVER_RETRIEVEGUEST, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATING() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        String vServerId = "vServerId";
        paramHandler.setVserverId(vServerId);
        paramHandler.addVserverToBeStarted(vServerId);

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATING, null);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATING_startVServer()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        String vServerId = "vServerId";
        paramHandler.setVserverId(vServerId);
        paramHandler.addVserverToBeStarted(vServerId);

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATING, null);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATED_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATED, null);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATED_VServerIsRunning()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATED, null);

        // then state is not modified.
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_UPDATED() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_UPDATED, null);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STARTING_VServerIsStopped()
            throws Exception {
        // given
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_STARTING, null);

        // then state is not modified.
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STARTING() throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_STARTING, null);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STARTED_VServerIsStopped()
            throws Exception {
        // given
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_STARTED, null);

        // then state is not modified
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_STARTED() throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_STARTED, null);

        // then
        assertEquals(FlowState.VSERVER_RETRIEVEGUEST, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_RETRIEVEGUEST()
            throws Exception {
        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_RETRIEVEGUEST, null);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationProcess_VSERVER_RETRIEVEGUEST_manualOperation()
            throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));

        // when
        FlowState newState = vServerProcessor.manageModificationProcess(null,
                null, paramHandler, FlowState.VSERVER_RETRIEVEGUEST, null);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_MODIFICATION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_MODIFICATION_REQUESTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_MODIFICATION_REQUESTED_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_MODIFICATION_REQUESTED;
        FlowState newState = null;

        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
        verify(vServerProcessor.vserverComm, times(1)).stopVServer(
                eq(paramHandler));
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_MODIFICATION_REQUESTED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_MODIFICATION_REQUESTED;
        FlowState newState = null;

        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STOPPED_FOR_MODIFICATION, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STOPPED_FOR_MODIFICATION_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STOPPED_FOR_MODIFICATION()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
        FlowState newState = null;

        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_UPDATING, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STOPPED_FOR_MODIFICATION_additionalDiskSelected()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isAdditionalDiskSelected(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_CREATION_REQUESTED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_CREATION_REQUESTED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_CREATION_REQUESTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);
        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_CREATION_REQUESTED()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        FlowState flowState = FlowState.VSDISK_CREATION_REQUESTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_CREATING, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_CREATING()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_CREATING;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_CREATING_VDiskIsDeployed()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_CREATING;
        FlowState newState = null;

        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isVDiskDeployed(any(PropertyHandler.class));
        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_CREATED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_CREATED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_CREATED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_CREATED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_CREATED;
        FlowState newState = null;

        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_ATTACHING, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_ATTACHING()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_ATTACHING;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_ATTACHING_VDiskIsAttached()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_ATTACHING;
        FlowState newState = null;

        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isVDiskAttached(any(PropertyHandler.class));
        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_ATTACHED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSDISK_ATTACHED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_ATTACHED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_UPDATING, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_UPDATING_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATING;
        FlowState newState = null;

        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_UPDATING_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATING;
        FlowState newState = null;

        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_UPDATED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_UPDATED_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATED;
        FlowState newState = null;

        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_UPDATED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATED;
        FlowState newState = null;

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STARTING_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTING;
        FlowState newState = null;

        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STARTING_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTING;
        FlowState newState = null;

        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STARTED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTED;
        FlowState newState = null;

        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_STARTED_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTED;
        FlowState newState = null;

        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_RETRIEVEGUEST, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_RETRIEVEGUEST()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_RETRIEVEGUEST;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationVDiskCreation_VSERVER_RETRIEVEGUEST_mailForCompletion()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_RETRIEVEGUEST;
        FlowState newState = null;

        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));

        // when
        newState = vServerProcessor
                .manageModificationVDiskCreation("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void validateParameters_NoState() throws Exception {
        // given a virtual server with no state (FlowState.FAILED)

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then do not validate the parameters of the virtual server
    }

    @Test
    public void validateParameters_NonValidableState() throws Exception {
        // given a non-validable provisioning state
        // (other then request for creation, modification or deletion)
        paramHandler.setState(FlowState.VSERVER_CREATED);

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then do not validate
    }

    @Test(expected = SuspendException.class)
    public void validateParameters_VSystemNotRunning() throws Exception {
        // given a validable state and a VSystem which is not running
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
        paramHandler.getIaasContext().setVSystemStatus("ERROR");

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then a SuspendException is thrown.
    }

    @Test(expected = SuspendException.class)
    public void validateParameters_NetworkIdInvalid() throws Exception {
        // given a validable state and an invalid network id
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
        doReturn(Boolean.FALSE).when(vServerProcessor.vserverComm)
                .isNetworkIdValid(any(PropertyHandler.class));

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then a SuspendException is thrown.
    }

    @Test
    public void validateParameters_VServerDeletionRequested() throws Exception {
        // given a provisioning state for a deletion request
        paramHandler.setState(FlowState.VSERVER_DELETION_REQUESTED);

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isNetworkIdValid(any(PropertyHandler.class));

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then do not validate against the backend API
    }

    @Test(expected = SuspendException.class)
    public void validateParameters_VSystemIdInvalid() throws Exception {
        paramHandler.setState(FlowState.VSERVER_MODIFICATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isNetworkIdValid(any(PropertyHandler.class));

        // given an invalid Virtual System id
        doReturn(Boolean.FALSE).when(vServerProcessor.vserverComm)
                .isVSysIdValid(any(PropertyHandler.class));

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then a SuspendException is thrown.
    }

    @Test(expected = SuspendException.class)
    public void validateParameters_DiskImageInvalid() throws Exception {
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isNetworkIdValid(any(PropertyHandler.class));

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isVSysIdValid(any(PropertyHandler.class));

        // given an invalid disk image
        paramHandler.setDiskImageId("invalidDiskImgId");

        doReturn(givenDiskImages()).when(vServerProcessor.vsysComm)
                .getDiskImages(any(PropertyHandler.class));

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then a SuspendException is thrown.
    }

    @Test(expected = SuspendException.class)
    public void validateParameters_VServerTypeInvalid() throws Exception {
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isNetworkIdValid(any(PropertyHandler.class));

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isVSysIdValid(any(PropertyHandler.class));

        doReturn(givenDiskImages()).when(vServerProcessor.vsysComm)
                .getDiskImages(any(PropertyHandler.class));
        paramHandler.setDiskImageId("diskimgid");

        // given an invalid VServer type
        doReturn(Boolean.FALSE).when(vServerProcessor.vserverComm)
                .isServerTypeValid(any(PropertyHandler.class));
        paramHandler.setVserverType("invalidVServerType");

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then a SuspendException is thrown.
    }

    @Test
    public void validateParameters() throws Exception {
        // given everything valid.
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isNetworkIdValid(any(PropertyHandler.class));

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isVSysIdValid(any(PropertyHandler.class));

        doReturn(givenDiskImages()).when(vServerProcessor.vsysComm)
                .getDiskImages(any(PropertyHandler.class));
        paramHandler.setDiskImageId("diskimgid");

        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isServerTypeValid(any(PropertyHandler.class));

        // when
        vServerProcessor.validateParameters(paramHandler);

        // then the validation of the parameters of the VServer is successful.
    }

    @Test
    public void isDiskImageIdValid_ValidDiskImageID() throws Exception {
        // given
        paramHandler.setDiskImageId("diskimgid");
        doReturn(givenDiskImages()).when(vServerProcessor.vsysComm)
                .getDiskImages(any(PropertyHandler.class));

        // when
        DiskImage validDiskImage = vServerProcessor.isDiskImageIdValid(null,
                paramHandler);

        // then
        assertNotNull(validDiskImage);
    }

    @Test
    public void isDiskImageIdValid_UniqueDiskImageName() throws Exception {
        // given
        paramHandler.setDiskImageId(DISKIDORNAME3);
        doReturn(givenDiskImagesUniqueDiskname()).when(
                vServerProcessor.vsysComm).getDiskImages(
                any(PropertyHandler.class));

        // when
        DiskImage validDiskImage = vServerProcessor.isDiskImageIdValid(null,
                paramHandler);

        // then
        assertNotNull(validDiskImage);

    }

    @Test
    public void isDiskImageIdValid_DiskNonUniqueImageName() throws Exception {
        // given
        paramHandler.setDiskImageId(DISKIDORNAME3);
        doReturn(givenDiskImagesNonUniqueDiskname()).when(
                vServerProcessor.vsysComm).getDiskImages(
                any(PropertyHandler.class));

        // when
        DiskImage validDiskImage = vServerProcessor.isDiskImageIdValid(null,
                paramHandler);

        // then
        assertNull(validDiskImage);

    }

    @Test
    public void isDiskImageIdValid_InvalidDiskImages() throws Exception {
        // given
        paramHandler.setDiskImageId("diskId");
        doReturn(givenDiskImagesUniqueDiskname()).when(
                vServerProcessor.vsysComm).getDiskImages(
                any(PropertyHandler.class));

        // when
        DiskImage validDiskImage = vServerProcessor.isDiskImageIdValid(null,
                paramHandler);

        // then
        assertNull(validDiskImage);
    }

    @Test
    public void manageDeletionProcess_VSERVER_DELETION_REQUESTED_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_DELETION_REQUESTED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));
        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        verify(vServerProcessor.vserverComm, times(1))
                .stopVServer(paramHandler);
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSERVER_DELETION_REQUESTED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_DELETION_REQUESTED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));
        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STOPPED_FOR_DELETION, newState);
    }

    @Test
    public void manageDeletionProcess_VSERVER_STOPPED_FOR_DELETION_AttachedVDisks()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_DELETION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isAttachedVDisksFound(paramHandler);

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DETACHING, newState);
        verify(vServerProcessor.vdiskInfo, times(1)).detachVDisks(paramHandler);
    }

    @Test
    public void manageDeletionProcess_VSERVER_STOPPED_FOR_DELETION_AttachedVDisks_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_DELETION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("");
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isAttachedVDisksFound(paramHandler);

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSERVER_STOPPED_FOR_DELETION_NoAttachedVDisks_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_DELETION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .isAttachedVDisksFound(paramHandler);
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));
        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_DELETING, newState);
        verify(vServerProcessor.vserverComm, times(1)).destroyVServer(
                paramHandler);
    }

    @Test
    public void manageDeletionProcess_VSERVER_STOPPED_FOR_DELETION_NoAttachedVDisks_NotStopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_DELETION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .isAttachedVDisksFound(paramHandler);
        doReturn(VServerStatus.FAILOVER).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));
        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSERVER_STOPPED_FOR_DELETION_NoAttachedVDisks_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_DELETION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("");

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSERVER_DELETING_Mail() throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));
        FlowState flowState = FlowState.VSERVER_DELETING;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isVServerDestroyed(any(PropertyHandler.class));

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.DESTROYED, newState);
        verify(platformService, times(1)).sendMail(
                Collections.singletonList(any(String.class)),
                any(String.class), any(String.class));
    }

    @Test
    public void manageDeletionProcess_VSERVER_DELETING_NoMail()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_DELETING;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm)
                .isVServerDestroyed(any(PropertyHandler.class));

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.DESTROYED, newState);
        verify(platformService, times(0)).sendMail(
                Collections.singletonList(any(String.class)),
                any(String.class), any(String.class));
    }

    @Test
    public void manageDeletionProcess_VSDISK_DETACHING() throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHING;
        FlowState newState = null;
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .areVDisksDetached(paramHandler);

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);
        // then
        assertEquals(FlowState.VSDISK_DETACHED, newState);

    }

    @Test
    public void manageDeletionProcess_VSDISK_DETACHING_VDisksNotDetached()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHING;
        FlowState newState = null;
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .areVDisksDetached(paramHandler);

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);

    }

    @Test
    public void manageDeletionProcess_VSDISK_DETACHED() throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DELETING, newState);
    }

    @Test
    public void manageDeletionProcess_VSDISK_DETACHED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHED;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSDISK_DELETING_VDisksDestroyed()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DELETING;
        FlowState newState = null;
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .areVDisksDestroyed(paramHandler);

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DESTROYED, newState);
    }

    @Test
    public void manageDeletionProcess_VSDISK_DELETING_VDisksNotDestroyed()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DELETING;
        FlowState newState = null;
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .areVDisksDestroyed(paramHandler);

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSDISK_DESTROYED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DESTROYED;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);

    }

    @Test
    public void manageDeletionProcess_VSDISK_DESTROYED() throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DESTROYED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        newState = vServerProcessor.manageDeletionProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_DELETING, newState);

    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_MODIFICATION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_MODIFICATION_REQUESTED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.STOPPING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_MODIFICATION_REQUESTED_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_MODIFICATION_REQUESTED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        verify(vServerProcessor.vserverComm, times(1))
                .stopVServer(paramHandler);
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_MODIFICATION_REQUESTED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_MODIFICATION_REQUESTED;
        FlowState newState = null;
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STOPPED_FOR_MODIFICATION, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STOPPED_FOR_MODIFICATION_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);

    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STOPPED_FOR_MODIFICATION_VDiskAttached()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .isAttachedVDisksFound(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DELETION_REQUESTED, newState);
        verify(vServerProcessor.vserverComm, times(1)).modifyVServerAttributes(
                paramHandler);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STOPPED_FOR_MODIFICATION_VDiskNotAttached()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .isAttachedVDisksFound(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_UPDATING, newState);

    }

    @Test
    public void manageModificationVDiskDeletion_VSDISK_DELETION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DELETION_REQUESTED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DETACHING, newState);
        verify(vServerProcessor.vdiskInfo, times(1)).detachVDisks(
                eq(paramHandler));

    }

    @Test
    public void manageModificationVDiskDeletion_VSDISK_DELETION_REQUESTED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DELETION_REQUESTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);

    }

    @Test
    public void manageModificationVDiskDeletion_VSDISK_DETACHING_VDisksDetached()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHING;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .areVDisksDetached(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DETACHED, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSDISK_DETACHING_VDisksNotDetached()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHING;
        FlowState newState = null;
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .areVDisksDetached(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSDISK_DETACHED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHED;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSDISK_DELETING, newState);
        verify(vServerProcessor.vdiskInfo, times(1)).destroyVDisks(
                eq(paramHandler));
    }

    @Test
    public void manageModificationVDiskDeletion_VSDISK_DETACHED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DETACHED;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSDISKDELETING_VDisksDestroyed()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DELETING;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.TRUE).when(vServerProcessor.vdiskInfo)
                .areVDisksDestroyed(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_UPDATING, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSDISKDELETING_VDisksNotDestroyed()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSDISK_DELETING;
        FlowState newState = null;
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");
        doReturn(Boolean.FALSE).when(vServerProcessor.vdiskInfo)
                .areVDisksDestroyed(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_UPDATING_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATING;
        FlowState newState = null;
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_UPDATED, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_UPDATING_NotStopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATING;
        FlowState newState = null;
        doReturn(VServerStatus.STARTING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_UPDATED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATED;
        FlowState newState = null;
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
        verify(vServerProcessor.vserverComm, times(1)).startVServer(
                eq(paramHandler));
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_UPDATED_NotStopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_UPDATED;
        FlowState newState = null;
        doReturn(VServerStatus.STARTING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STARTING_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTING;
        FlowState newState = null;
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STARTING_NotRunning()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTING;
        FlowState newState = null;
        doReturn(VServerStatus.STARTING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STARTED_Running_MailForCompletion()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTED;
        FlowState newState = null;
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_RETRIEVEGUEST, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STARTED_Runnning_NoMailForCompletion()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTED;
        FlowState newState = null;
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_STARTED_NotRunning()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTED;
        FlowState newState = null;
        doReturn(VServerStatus.STARTING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_RETRIEVEGUEST_MailForCompletion()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_RETRIEVEGUEST;
        FlowState newState = null;
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationVDiskDeletion_VSERVER_RETRIEVEGUEST_NoMailForCompletion()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_RETRIEVEGUEST;
        FlowState newState = null;

        // when
        newState = vServerProcessor
                .manageModificationVDiskDeletion("controllerId", "instanceId",
                        paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_ACTIVATION_REQUESTED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_ACTIVATION_REQUESTED;
        FlowState newState = null;
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STARTING, newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_ACTIVATION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_ACTIVATION_REQUESTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);

    }

    @Test
    public void manageActivationProcess_VSERVER_STARTING_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTING;
        FlowState newState = null;
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STARTED, newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_STARTING() throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTING;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_STARTED() throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STARTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_DEACTIVATION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_DEACTIVATION_REQUESTED;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_DEACTIVATION_REQUESTED_Running()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_DEACTIVATION_REQUESTED;
        FlowState newState = null;
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertNull(newState);
        verify(vServerProcessor.vserverComm, times(1)).stopVServer(
                eq(paramHandler));
    }

    @Test
    public void manageActivationProcess_VSERVER_DEACTIVATION_REQUESTED_Stopped()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_DEACTIVATION_REQUESTED;
        FlowState newState = null;
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.VSERVER_STOP_FOR_DEACTIVATION, newState);
    }

    @Test
    public void manageActivationProcess_VSERVER_STOP_FOR_DEACTIVATION()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVER_STOP_FOR_DEACTIVATION;
        FlowState newState = null;

        // when
        newState = vServerProcessor.manageActivationProcess("controllerId",
                "instanceId", paramHandler, flowState, newState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void dispatchVServerManualOperation_VSERVER_CREATION()
            throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_CREATION);
        FlowState newState = null;
        setParameters();
        doReturn("").when(platformService).getEventServiceUrl();

        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertEquals(FlowState.MANUAL, newState);

    }

    @Test
    public void dispatchVServerManualOperation_VSERVER_MODIFICATION()
            throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_MODIFICATION);
        setParameters();

        FlowState newState = null;
        doReturn("").when(platformService).getEventServiceUrl();

        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void dispatchVServerManualOperation_VSERVER_MODIFICATION_VDISK_CREATION()
            throws Exception {
        // given
        paramHandler
                .setOperation(Operation.VSERVER_MODIFICATION_VDISK_CREATION);
        setParameters();
        FlowState newState = null;
        doReturn("").when(platformService).getEventServiceUrl();

        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void dispatchVServerManualOperation_VSERVER_MODIFICATION_VDISK_DELETION()
            throws Exception {
        // given
        paramHandler
                .setOperation(Operation.VSERVER_MODIFICATION_VDISK_DELETION);
        setParameters();
        FlowState newState = null;
        doReturn("").when(platformService).getEventServiceUrl();

        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void dispatchVServerManualOperation_FINISHED() throws Exception {
        // given
        paramHandler.setVserverType("VSERVER_TYPE");
        setParameters();
        FlowState newState = null;
        doReturn("").when(platformService).getEventServiceUrl();

        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void dispatchVServerManualOperation() throws Exception {
        // given
        paramHandler.setVserverType("VSERVER_TYPE");
        FlowState newState = null;
        setParameters();
        doReturn(Boolean.FALSE).when(vServerProcessor).checkNextStatus(
                "controllerId", "instanceId", FlowState.FINISHED, paramHandler);

        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertNull(newState);
    }

    @Test
    public void dispatchVServerManualOperatio_sendMail() throws Exception {
        // given
        paramHandler.setVserverType("VSERVER_TYPE");
        FlowState newState = null;
        setParameters();
        paramHandler.setOperation(Operation.VSERVER_CREATION);
        doReturn(Boolean.TRUE).when(vServerProcessor).checkNextStatus(
                "controllerId", "instanceId", FlowState.MANUAL, paramHandler);
        doReturn("http://mockUrl").when(platformService).getEventServiceUrl();
        // when
        newState = vServerProcessor.dispatchVServerManualOperation(
                "controllerId", "instanceId", paramHandler, "mail");

        // then
        assertTrue(subject.getValue().contains("subId"));
        assertTrue(text.getValue().contains("subId"));
        assertEquals(FlowState.MANUAL, newState);
    }

    @Test
    public void dispatch_OperationUnknown() throws Exception {
        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.FAILED, paramHandler.getState());
    }

    @Test
    public void dispatch_VSysStatusError() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("ERROR");

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.FAILED, paramHandler.getState());
    }

    @Test
    public void dispatch_VSERVER_CREATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_CREATION);
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSERVER_CREATING, paramHandler.getState());
    }

    @Test
    public void dispatch_VSERVER_MODIFICATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_MODIFICATION);
        paramHandler.setState(FlowState.VSERVER_MODIFICATION_REQUESTED);
        doReturn(FlowState.VSERVER_STOPPING_FOR_MODIFICATION).when(
                vServerProcessor.vserverComm).modifyVServerAttributes(
                any(PropertyHandler.class));
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSERVER_STOPPING_FOR_MODIFICATION,
                paramHandler.getState());
    }

    @Test
    public void dispatch_VSERVER_MODIFICATION_VDISK_CREATION() throws Exception {
        // given
        paramHandler
                .setOperation(Operation.VSERVER_MODIFICATION_VDISK_CREATION);
        paramHandler.setState(FlowState.VSDISK_CREATION_REQUESTED);
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSDISK_CREATING, paramHandler.getState());
    }

    @Test
    public void dispatch_VSERVER_MODIFICATION_VDISK_DELETION() throws Exception {
        // given
        paramHandler
                .setOperation(Operation.VSERVER_MODIFICATION_VDISK_DELETION);
        paramHandler.setState(FlowState.VSDISK_DELETION_REQUESTED);
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSDISK_DETACHING, paramHandler.getState());
    }

    @Test
    public void dispatch_VSERVER_MODIFICATION_VSERVER_DELETION()
            throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_DELETION);
        paramHandler.setState(FlowState.VSDISK_DETACHED);
        paramHandler.getIaasContext().setVSystemStatus("NORMAL");

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSDISK_DELETING, paramHandler.getState());
    }

    @Test
    public void dispatch_VSERVER_ACTIVATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_ACTIVATION);
        paramHandler.setState(FlowState.VSERVER_ACTIVATION_REQUESTED);
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(any(PropertyHandler.class));
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);

        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSERVER_STARTING, paramHandler.getState());
    }

    @Test(expected = RuntimeException.class)
    public void process_ThrowRuntimeException() throws Exception {
        // given
        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "template"));

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test
    public void process_NormalState() throws Exception {

        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.NORMAL);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);

        // then
        verify(vServerProcessor, times(1)).validateParameters(paramHandler);
    }

    @Test
    public void process_SetToNormalState() throws Exception {
        // given
        doReturn(VSystemStatus.NORMAL).when(vServerProcessor.vsysComm)
                .getVSystemState(paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(VSystemStatus.NORMAL,
                vServerProcessor.vsysComm.getVSystemState(paramHandler));

    }

    @Test
    public void process_SetNotToNormalState() throws Exception {
        // given
        doReturn(VSystemStatus.DEPLOYING).when(vServerProcessor.vsysComm)
                .getVSystemState(paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);

        // then
        assertFalse(VSystemStatus.NORMAL.equals(vServerProcessor.vsysComm
                .getVSystemState(paramHandler)));
    }

    @Test
    public void process_CatchMissingResourceException() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_DELETION);
        paramHandler.setVserverId("serverId");

        doThrow(
                new MissingResourceException("test", ResourceType.VSERVER,
                        "serverId")).when(vServerProcessor).validateParameters(
                paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.VSERVER_DELETING, paramHandler.getState());
        assertEquals("", paramHandler.getVserverId());
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_ResourceTypeFalse()
            throws Exception {
        // given
        doThrow(
                new MissingResourceException("test", ResourceType.UNKNOWN,
                        "serverId")).when(vServerProcessor).validateParameters(
                paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_NoResourceType() throws Exception {
        // given
        doThrow(
                new MissingResourceException("test", ResourceType.VSERVER,
                        "serverId")).when(vServerProcessor).validateParameters(
                paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_NoServerId() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_DELETION);
        paramHandler.setVserverId(null);
        doThrow(
                new MissingResourceException("test", ResourceType.VSERVER,
                        "serverId")).when(vServerProcessor).validateParameters(
                paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_ServerIdFalse() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_DELETION);
        paramHandler.setVserverId("serverIdTest");
        doThrow(
                new MissingResourceException("test", ResourceType.VSERVER,
                        "serverId")).when(vServerProcessor).validateParameters(
                paramHandler);

        // when
        vServerProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test
    public void dispatch_VSERVER_OPERATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSERVER_OPERATION);
        paramHandler.setState(FlowState.VSERVER_START_REQUESTED);
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);
        // when
        vServerProcessor.dispatch("controllerId", "instanceId", paramHandler);
        // then
        assertEquals(FlowState.VSERVER_STARTING, paramHandler.getState());
    }

    @Test
    public void manageOperationsProcess_VSERVER_START_REQUESTED()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vServerProcessor.vserverComm).startVServer(
                paramHandler);
        // when
        FlowState result = vServerProcessor.manageOperationsProcess(
                "controllerId", "instanceId", paramHandler,
                FlowState.VSERVER_START_REQUESTED, null);

        // then
        assertEquals(FlowState.VSERVER_STARTING, result);
    }

    @Test
    public void manageOperationsProcess_VSERVER_STARTING() throws Exception {
        // given
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);
        // when
        FlowState result = vServerProcessor.manageOperationsProcess(
                "controllerId", "instanceId", paramHandler,
                FlowState.VSERVER_STARTING, null);

        // then
        assertEquals(FlowState.VSERVER_STARTED, result);
    }

    @Test
    public void manageOperationsProcess_VSERVER_STARTED() throws Exception {
        // when
        FlowState result = vServerProcessor.manageOperationsProcess(
                "controllerId", "instanceId", paramHandler,
                FlowState.VSERVER_STARTED, null);
        // then
        assertEquals(FlowState.FINISHED, result);
    }

    @Test
    public void manageOperationsProcess_VSERVER_STOP_REQUESTED()
            throws Exception {
        // given
        doNothing().when(vServerProcessor.vserverComm)
                .stopVServer(paramHandler);
        doReturn(VServerStatus.RUNNING).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);
        // when
        FlowState result = vServerProcessor.manageOperationsProcess(
                "controllerId", "instanceId", paramHandler,
                FlowState.VSERVER_STOP_REQUESTED, null);

        // then
        assertEquals(FlowState.VSERVER_STOPPING, result);
    }

    @Test
    public void manageOperationsProcess_VSERVER_STOPPING() throws Exception {
        // given
        doReturn(VServerStatus.STOPPED).when(vServerProcessor.vserverComm)
                .getVServerStatus(paramHandler);
        // when
        FlowState result = vServerProcessor.manageOperationsProcess(
                "controllerId", "instanceId", paramHandler,
                FlowState.VSERVER_STOPPING, null);

        // then
        assertEquals(FlowState.VSERVER_STOPPED, result);
    }

    @Test
    public void manageOperationsProcess_VSERVER_STOPPED() throws Exception {
        // when
        FlowState result = vServerProcessor.manageOperationsProcess(
                "controllerId", "instanceId", paramHandler,
                FlowState.VSERVER_STOPPED, null);

        // then
        assertEquals(FlowState.FINISHED, result);
    }

    private void setParameters() {
        paramHandler.setVserverType("VSERVER_TYPE");
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "instance"));
        parameters.put(PropertyHandler.DISKIMG_ID, new Setting(
                PropertyHandler.DISKIMG_ID, "diskimg"));
    }

    private List<DiskImage> givenDiskImages() {
        List<DiskImage> diskImages = new ArrayList<>();
        DiskImage diskImage = mock(DiskImage.class);

        doReturn("diskimgid").when(diskImage).getDiskImageId();
        doReturn("diskimgname").when(diskImage).getDiskImageName();
        diskImages.add(diskImage);
        return diskImages;
    }

    private List<DiskImage> givenDiskImagesNonUniqueDiskname() {
        List<DiskImage> diskImages = new ArrayList<>();
        DiskImage diskImage1 = mock(DiskImage.class);
        doReturn(DISKIDORNAME1).when(diskImage1).getDiskImageId();
        doReturn(DISKIDORNAME3).when(diskImage1).getDiskImageName();
        diskImages.add(diskImage1);
        DiskImage diskImage2 = mock(DiskImage.class);
        doReturn(DISKIDORNAME2).when(diskImage2).getDiskImageId();
        doReturn(DISKIDORNAME3).when(diskImage2).getDiskImageName();
        diskImages.add(diskImage2);
        return diskImages;
    }

    private List<DiskImage> givenDiskImagesUniqueDiskname() {
        List<DiskImage> diskImages = new ArrayList<>();
        DiskImage diskImage1 = mock(DiskImage.class);
        doReturn(DISKIDORNAME1).when(diskImage1).getDiskImageId();
        doReturn(DISKIDORNAME3).when(diskImage1).getDiskImageName();
        diskImages.add(diskImage1);
        DiskImage diskImage2 = mock(DiskImage.class);
        doReturn(DISKIDORNAME2).when(diskImage2).getDiskImageId();
        doReturn(DISKIDORNAME4).when(diskImage2).getDiskImageName();
        diskImages.add(diskImage2);
        return diskImages;
    }
}
