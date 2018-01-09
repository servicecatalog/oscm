/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 27, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.app.iaas.data.DiskImage;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.IaasContext;
import org.oscm.app.iaas.data.Network;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.data.ResourceType;
import org.oscm.app.iaas.data.VServerConfiguration;
import org.oscm.app.iaas.data.VServerStatus;
import org.oscm.app.iaas.data.VSystemConfiguration;
import org.oscm.app.iaas.data.VSystemStatus;
import org.oscm.app.iaas.data.VSystemTemplateConfiguration;
import org.oscm.app.iaas.exceptions.MissingResourceException;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.iaas.intf.VServerCommunication;
import org.oscm.app.iaas.intf.VSystemCommunication;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author Zhou
 * 
 */
public class VSystemProcessorBeanTest {

    private VSystemProcessorBean vSystemProcessor;
    private VServerProcessorBean vServerProcessor;
    private APPlatformService platformService;
    @Captor
    private ArgumentCaptor<String> subject;
    @Captor
    private ArgumentCaptor<String> text;

    private HashMap<String, Setting> parameters;
    HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private PropertyHandler paramHandler;
    private HashMap<String, DiskImage> diskImages;
    private static final String NORMAL = "NORMAL";
    private static final String ERROR = "ERROR";
    private static final String CONTROLLER_ID = "controllerId";
    private static final String INSTANCE_ID = "instanceId";
    private static final String VSYS_ID = "vSysId";
    private static final String DISK_IMAGE_ID = "diskImageId";
    private static final String SERVER_ID = "serverId";
    private static final String REST_URL = "restURL";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        vSystemProcessor = spy(new VSystemProcessorBean());
        vSystemProcessor.vsysComm = mock(VSystemCommunication.class);
        vSystemProcessor.vserverComm = mock(VServerCommunication.class);
        platformService = mock(APPlatformService.class);
        vServerProcessor = spy(new VServerProcessorBean());
        vSystemProcessor.setDelegate(vServerProcessor);

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
        vSystemProcessor.setPlatformService(platformService);

        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        parameters.put("SYSTEM_TEMPLATE_ID", new Setting("SYSTEM_TEMPLATE_ID",
                "template"));
        settings = new ProvisioningSettings(parameters, configSettings, "en");
        settings.setSubscriptionId("subId");
        paramHandler = spy(new PropertyHandler(settings));
        // images to be validated against
        diskImages = new HashMap<>();
    }

    private void addDiskImageAnswer() throws Exception {
        Answer<DiskImage> answerDiskImage = new Answer<DiskImage>() {
            @Override
            public DiskImage answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments != null && arguments.length > 0) {
                    return diskImages.get(arguments[0]);
                }
                return null;
            }
        };
        Mockito.doAnswer(answerDiskImage).when(vServerProcessor)
                .isDiskImageIdValid(anyString(), any(PropertyHandler.class));
    }

    @Test
    public void setDelegate() {
        // given
        VServerProcessorBean delegate = mock(VServerProcessorBean.class);

        // when
        vSystemProcessor.setDelegate(delegate);
    }

    @Test
    public void setURLStreamHandler() {
        // given
        URLStreamHandler streamHandler = mock(URLStreamHandler.class);

        // when
        VSystemProcessorBean.setURLStreamHandler(streamHandler);
    }

    @Test
    public void validateParameters_NoState() throws Exception {
        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void validateParameters_VSYSTEM_CREATION_REQUESTED()
            throws Exception {
        // given
        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void validateParameters_VSYSTEM_MODIFICATION_REQUESTED()
            throws Exception {
        // given
        paramHandler.setState(FlowState.VSYSTEM_MODIFICATION_REQUESTED);

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void validateParameters_SuspendException() throws Exception {
        // given
        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.FALSE).when(vSystemProcessor).isAdminAgentReachable();

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void isAdminAgentReachable() throws Exception {
        // when
        boolean result = vSystemProcessor.isAdminAgentReachable();

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void validate_ClusterOK() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "2"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));
        doNothing().when(vSystemProcessor).validateClusterSize(
                eq(paramHandler), eq(true), eq(false),
                any(VSystemTemplateConfiguration.class));

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test(expected = AbortException.class)
    public void validate_Cluster_noMasterImg() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "2"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId() + "x"));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test(expected = AbortException.class)
    public void validate_Cluster_noSlaveImg() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();

        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "2"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, slaveImage.getDiskImageId()
                        + "x"));

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void validate_Cluster_maxCPU() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();

        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "4"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));

        doNothing().when(vSystemProcessor).validateClusterSize(
                any(PropertyHandler.class), anyBoolean(), anyBoolean(),
                any(VSystemTemplateConfiguration.class));

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void validate_Cluster_maxMasterCPUEmpty() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "4"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));

        doNothing().when(vSystemProcessor).validateClusterSize(
                any(PropertyHandler.class), anyBoolean(), anyBoolean(),
                any(VSystemTemplateConfiguration.class));
        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void validate_Cluster_maxSlaveCPUEmpty() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "4"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));

        doNothing().when(vSystemProcessor).validateClusterSize(
                any(PropertyHandler.class), anyBoolean(), anyBoolean(),
                any(VSystemTemplateConfiguration.class));
        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test(expected = AbortException.class)
    public void validate_Cluster_maxMasterCPU() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("2").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "4"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));

        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test(expected = AbortException.class)
    public void validate_Cluster_maxSlaveCPU() throws Exception {
        // given
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("2").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor).isAdminAgentReachable();
        addDiskImageAnswer();
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "4"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));
        // when
        vSystemProcessor.validateParameters(paramHandler);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_CREATION_REQUESTED()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_CREATION_REQUESTED);

        // then
        assertEquals(FlowState.VSYSTEM_CREATING, newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_CREATION_REQUESTED_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSYSTEM_CREATING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_CREATION_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_CREATING_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_CREATING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_CREATING_NextStatusNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STARTING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_CREATING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_CREATING() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm).startAllEFMs(
                paramHandler);
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_CREATING);

        // then the state is not modified.
        assertEquals(FlowState.VSERVERS_STARTING, newState);
    }

    @Test
    public void manageModificationProcess_VSERVERS_STARTING_CombinedVServerStateNotNormal()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSERVERS_STARTING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVERS_STARTING_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VServerStatus.RUNNING);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID,
                FlowState.VSYSTEM_SCALING_COMPLETED, paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSERVERS_STARTING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSERVERS_STARTING() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VServerStatus.RUNNING);

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSERVERS_STARTING);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED, newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_MODIFICATION_REQUESTED_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID,
                FlowState.VSYSTEM_SCALING_COMPLETED, paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_MODIFICATION_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_MODIFICATION_REQUESTED()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_MODIFICATION_REQUESTED);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED, newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_RETRIEVEGUEST_MailNotNull()
            throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_RETRIEVEGUEST);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_RETRIEVEGUEST_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_RETRIEVEGUEST);

        // then
        assertNull(newState);
    }

    @Test
    public void manageModificationProcess_VSYSTEM_RETRIEVEGUEST()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_RETRIEVEGUEST);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageModificationProcess_OtherState() throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageModificationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_SCALE_UP);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSYSTEM_DELETION_REQUESTED_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STOPPING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_DELETION_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSYSTEM_DELETION_REQUESTED()
            throws Exception {
        // given
        IaasContext ctx = mock(IaasContext.class);
        doReturn(VSystemStatus.NORMAL).when(ctx).getVSystemStatus();
        doReturn(ctx).when(paramHandler).getIaasContext();

        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_DELETION_REQUESTED);

        // then
        assertEquals(FlowState.VNET_DELETING, newState);
    }

    @Test
    public void manageDeletionProcess_VSERVERS_STOPPING_CombinedVServerStateNotNormal()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSERVERS_STOPPING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSERVERS_STOPPING_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VServerStatus.STOPPED);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSYSTEM_DELETING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSERVERS_STOPPING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSERVERS_STOPPING() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VServerStatus.STOPPED);

        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSERVERS_STOPPING);

        // then
        assertEquals(FlowState.VSYSTEM_DELETING, newState);
    }

    @Test
    public void manageDeletionProcess_VSYSTEM_DELETING_MailNotNull()
            throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@email.com"));
        paramHandler.setOperation(Operation.VSYSTEM_DELETION);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSYSTEM_DELETING,
                paramHandler);
        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_DELETING);

        // then
        assertTrue(subject.getValue().contains("subId"));
        assertTrue(text.getValue().contains("subId"));
        assertEquals(FlowState.DESTROYED, newState);
    }

    @Test
    public void manageDeletionProcess_VSYSTEM_DELETING_NextStatusNotNormal()
            throws Exception {
        // given
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.DESTROYED, paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_DELETING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeletionProcess_VSYSTEM_DELETING() throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_DELETING);

        // then
        assertEquals(FlowState.DESTROYED, newState);
    }

    @Test
    public void manageDeletionProcess_OtherState() throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageDeletionProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_SCALE_UP);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSYSTEM_START_REQUESTED_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_START_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSYSTEM_START_REQUESTED_NextStatusNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STARTING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_START_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSYSTEM_START_REQUESTED() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm).startAllEFMs(
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_START_REQUESTED);

        // then
        assertEquals(FlowState.VSERVERS_STARTING, newState);
    }

    @Test
    public void manageOperations_VSERVERS_STARTING_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STARTING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSERVERS_STARTING_NextStatusNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STARTING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSERVERS_STARTING_CombinedVServerStateNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STARTING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSERVERS_STARTING() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VServerStatus.RUNNING);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STARTING);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageOperations_VSYSTEM_STOP_REQUESTED_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_STOP_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSYSTEM_STOP_REQUESTED_NextStatusNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STOPPING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_STOP_REQUESTED);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSYSTEM_STOP_REQUESTED() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_STOP_REQUESTED);

        // then
        assertEquals(FlowState.VSERVERS_STOPPING, newState);
    }

    @Test
    public void manageOperations_VSERVERS_STOPPING_VSysNotInNormalState()
            throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STOPPING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSERVERS_STOPPING_NextStatusNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STOPPING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSERVERS_STOPPING_CombinedVServerStateNotNormal()
            throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STOPPING);

        // then
        assertNull(newState);
    }

    @Test
    public void manageOperations_VSERVERS_STOPPING() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VServerStatus.STOPPED);

        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSERVERS_STOPPING);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageOperations_OtherState() throws Exception {
        // when
        FlowState newState = vSystemProcessor.manageOperations(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_CREATING);

        // then
        assertNull(newState);
    }

    @Test
    public void dispatch_OperationUnknown() throws Exception {
        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.FAILED, paramHandler.getState());
    }

    @Test
    public void manageActivationProcess_VSYSTEM_ACTIVATION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSYSTEM_ACTIVATION_REQUESTED;
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STARTING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertEquals(FlowState.FINISHED, newState);
        List<Object> serversToBeStarted = Arrays.asList(paramHandler
                .getVserversToBeStarted().toArray());
        assertTrue(serversToBeStarted.isEmpty());
    }

    @Test
    public void manageActivationProcess_VSYSTEM_ACTIVATION_REQUESTED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSYSTEM_ACTIVATION_REQUESTED;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.DEPLOYING);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STARTING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageActivationProcess_VSYSTEM_ACTIVATION_REQUESTED_CheckStatusNotOK()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSYSTEM_ACTIVATION_REQUESTED;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STARTING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeactivationProcess_VSYSTEM_DEACTIVATION_REQUESTED()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSYSTEM_DEACTIVATION_REQUESTED;
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STOPPING,
                paramHandler);
        List<String> stoppedServers = Arrays.asList("s1", "s2", "s3");
        doReturn(stoppedServers).when(vSystemProcessor.vsysComm)
                .stopAllVServers(paramHandler);
        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertEquals(FlowState.VSERVERS_STOPPING, newState);
        List<Object> serversToBeStarted = Arrays.asList(paramHandler
                .getVserversToBeStarted().toArray());
        assertTrue(serversToBeStarted.size() == 3);
        assertTrue(serversToBeStarted.containsAll(stoppedServers));
    }

    @Test
    public void manageDeactivationProcess_VSYSTEM_DEACTIVATION_REQUESTED_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSYSTEM_DEACTIVATION_REQUESTED;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.DEPLOYING);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STOPPING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageDeactivationProcess_VSYSTEM_DEACTIVATION_REQUESTED_CheckStatusNotOK()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSYSTEM_DEACTIVATION_REQUESTED;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.VSERVERS_STOPPING,
                paramHandler);

        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageActivationProcess_VSERVERS_STOPPING_Normal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVERS_STOPPING;
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VSystemStatus.STOPPED);
        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertEquals(FlowState.FINISHED, newState);
    }

    @Test
    public void manageActivationProcess_VSERVERS_STOPPING_NotNormal()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVERS_STOPPING;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.DEPLOYING);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VSystemStatus.STOPPED);
        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageActivationProcess_VSERVERS_STOPPING_CombinedStateNotOK()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVERS_STOPPING;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.NORMAL);
        doReturn(Boolean.TRUE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);
        doReturn(Boolean.FALSE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VSystemStatus.STOPPED);
        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void manageActivationProcess_VSERVERS_STOPPING_CheckNextStatusNotOK()
            throws Exception {
        // given
        FlowState flowState = FlowState.VSERVERS_STOPPING;
        paramHandler.getIaasContext().setVSystemStatus(VSystemStatus.NORMAL);
        doReturn(Boolean.FALSE).when(vSystemProcessor).checkNextStatus(
                CONTROLLER_ID, INSTANCE_ID, FlowState.FINISHED, paramHandler);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm)
                .getCombinedVServerState(paramHandler, VSystemStatus.STOPPED);
        // when
        FlowState newState = vSystemProcessor.manageActivationProcess(
                CONTROLLER_ID, INSTANCE_ID, paramHandler, flowState);

        // then
        assertNull(newState);
    }

    @Test
    public void dispatch_VSysIdBlank() throws Exception {
        // given
        paramHandler.setVsysId("");

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.FAILED, paramHandler.getState());
    }

    @Test
    public void dispatch_VSysStatusNormal() throws Exception {
        // given
        paramHandler.setVsysId(VSYS_ID);
        doReturn(NORMAL).when(vSystemProcessor.vsysComm).getVSystemState(
                paramHandler);

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.FAILED, paramHandler.getState());
    }

    @Test
    public void dispatch_VSysStatusError() throws Exception {
        // given
        paramHandler.setVsysId(VSYS_ID);
        doReturn(ERROR).when(vSystemProcessor.vsysComm).getVSystemState(
                paramHandler);

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.FAILED, paramHandler.getState());
    }

    @Test
    public void dispatch_VSYSTEM_CREATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_CREATION);
        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.VSYSTEM_CREATING, paramHandler.getState());
    }

    @Test
    public void dispatch_VSYSTEM_MODIFICATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_MODIFICATION);
        paramHandler.setState(FlowState.VSYSTEM_MODIFICATION_REQUESTED);

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED,
                paramHandler.getState());
    }

    @Test
    public void dispatch_VSYSTEM_DELETION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_DELETION);
        paramHandler.setState(FlowState.VSYSTEM_DELETION_REQUESTED);

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.VNET_DELETING, paramHandler.getState());
    }

    @Test
    public void dispatch_VSYSTEM_OPERATION() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_OPERATION);
        paramHandler.setState(FlowState.VSYSTEM_START_REQUESTED);
        doReturn(Boolean.TRUE).when(vSystemProcessor.vsysComm).startAllEFMs(
                paramHandler);

        // when
        vSystemProcessor.dispatch(CONTROLLER_ID, INSTANCE_ID, paramHandler);

        // then
        assertEquals(FlowState.VSERVERS_STARTING, paramHandler.getState());
    }

    @Test
    public void determineScalingAndSizing_ClusterNotDefined() throws Exception {
        // when
        FlowState newState = vSystemProcessor
                .determineScalingAndSizing(paramHandler);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED, newState);
    }

    @Test
    public void determineScalingAndSizing_VSYSTEM_SCALE_UP() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(paramHandler).isClusterDefined();
        prepareSlaveClusterSizeAndServerIds(THREE, TWO);

        // when
        FlowState newState = vSystemProcessor
                .determineScalingAndSizing(paramHandler);

        // then
        assertEquals(FlowState.VSYSTEM_SCALE_UP, newState);
    }

    @Test
    public void determineScalingAndSizing_VSYSTEM_SCALE_DOWN() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(paramHandler).isClusterDefined();
        prepareSlaveClusterSizeAndServerIds(ONE, TWO);

        // when
        FlowState newState = vSystemProcessor
                .determineScalingAndSizing(paramHandler);

        // then
        assertEquals(FlowState.VSYSTEM_SCALE_DOWN, newState);
    }

    @Test
    public void determineScalingAndSizing_VSYSTEM_RESIZE_VSERVERS()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(paramHandler).isClusterDefined();
        prepareSlaveClusterSizeAndServerIds(TWO, TWO);
        doReturn("serverId").when(vSystemProcessor).isResizingRequired(
                paramHandler);

        // when
        FlowState newState = vSystemProcessor
                .determineScalingAndSizing(paramHandler);

        // then
        assertEquals(FlowState.VSYSTEM_RESIZE_VSERVERS, newState);
    }

    @Test
    public void isResizingRequired_NotNumber() throws Exception {
        // given
        paramHandler.setCountCPU("a1");

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertNull(result);
    }

    @Test
    public void isResizingRequired_VSystemConfigurationNull() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        VServerConfiguration server = givenVServerConfiguration(null, null);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        doReturn(configuration).when(vSystemProcessor.vsysComm)
                .getConfiguration(paramHandler);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertNull(result);
    }

    @Test
    public void isResizingRequired_ExistingCPUNull() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        VServerConfiguration server = givenVServerConfiguration(null, null);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        paramHandler.getIaasContext().add(configuration);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertNull(result);
    }

    @Test
    public void isResizingRequired_ExistingCPUEqualsCountCPU() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        VServerConfiguration server = givenVServerConfiguration(
                Integer.toString(ONE), null);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        paramHandler.getIaasContext().add(configuration);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertNull(result);
    }

    @Test
    public void isResizingRequired_DiskImageIdNull() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        VServerConfiguration server = givenVServerConfiguration(
                Integer.toString(TWO), null);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        paramHandler.getIaasContext().add(configuration);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertNull(result);
    }

    @Test
    public void isResizingRequired_DiskImageIdNotNull() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        VServerConfiguration server = givenVServerConfiguration(
                Integer.toString(TWO), DISK_IMAGE_ID);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        paramHandler.getIaasContext().add(configuration);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertNull(result);
    }

    @Test
    public void isResizingRequired_MasterTemplateId() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        parameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, DISK_IMAGE_ID));
        VServerConfiguration server = givenVServerConfiguration(
                Integer.toString(TWO), DISK_IMAGE_ID);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        paramHandler.getIaasContext().add(configuration);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertEquals(SERVER_ID, result);
        assertEquals(FlowState.VSYSTEM_RESIZE_VSERVERS, paramHandler.getState());
        assertEquals(FlowState.VSERVER_MODIFICATION_REQUESTED, paramHandler
                .getTemporaryVserver(server).getState());
        assertEquals(Operation.VSERVER_MODIFICATION, paramHandler
                .getTemporaryVserver(server).getOperation());
    }

    @Test
    public void isResizingRequired_SlaveTemplateId() throws Exception {
        // given
        paramHandler.setCountCPU(Integer.toString(ONE));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, DISK_IMAGE_ID));
        VServerConfiguration server = givenVServerConfiguration(
                Integer.toString(TWO), DISK_IMAGE_ID);
        VSystemConfiguration configuration = givenVSystemConfiguration(server);
        paramHandler.getIaasContext().add(configuration);

        // when
        String result = vSystemProcessor.isResizingRequired(paramHandler);

        // then
        assertEquals(SERVER_ID, result);
        assertEquals(FlowState.VSYSTEM_RESIZE_VSERVERS, paramHandler.getState());
        assertEquals(FlowState.VSERVER_MODIFICATION_REQUESTED, paramHandler
                .getTemporaryVserver(server).getState());
        assertEquals(Operation.VSERVER_MODIFICATION, paramHandler
                .getTemporaryVserver(server).getOperation());
    }

    @Test
    public void getSlaveClusterSize_SlaveTemplateIdNull() throws Exception {
        // given
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, Integer.toString(THREE)));

        // when
        int size = vSystemProcessor.getSlaveClusterSize(paramHandler);

        // then
        assertEquals(TWO, size);
    }

    @Test
    public void getSlaveClusterSize_SlaveTemplateIdNotNull() throws Exception {
        // given
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, Integer.toString(THREE)));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, DISK_IMAGE_ID));

        // when
        int size = vSystemProcessor.getSlaveClusterSize(paramHandler);

        // then
        assertEquals(TWO, size);
    }

    @Test
    public void getSlaveClusterSize() throws Exception {
        // given
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, Integer.toString(THREE)));
        parameters.put(PropertyHandler.SLAVE_TEMPLATE_ID, new Setting(
                PropertyHandler.SLAVE_TEMPLATE_ID, DISK_IMAGE_ID));
        parameters.put(PropertyHandler.MASTER_TEMPLATE_ID, new Setting(
                PropertyHandler.MASTER_TEMPLATE_ID, DISK_IMAGE_ID));

        // when
        int size = vSystemProcessor.getSlaveClusterSize(paramHandler);

        // then
        assertEquals(THREE, size);
    }

    @Test(expected = Exception.class)
    public void scaleUp_Exception() throws Exception {
        // given
        doThrow(new Exception()).when(vSystemProcessor).getSlaveClusterSize(
                eq(paramHandler));

        // when
        vSystemProcessor.scaleUp(paramHandler, FlowState.VSYSTEM_CREATING,
                DISK_IMAGE_ID, DISK_IMAGE_ID, true);
    }

    @Test
    public void scaleUp_SlaveClusterSizeEqualsServerSize() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(TWO, TWO);

        // when
        FlowState newState = vSystemProcessor.scaleUp(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED, newState);
    }

    @Test
    public void scaleUp_VSysNotInNormalState() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(THREE, TWO);
        FlowState flowState = FlowState.VSYSTEM_CREATING;

        // when
        FlowState newState = vSystemProcessor.scaleUp(paramHandler, flowState,
                DISK_IMAGE_ID, DISK_IMAGE_ID, false);

        // then
        assertEquals(flowState, newState);
    }

    @Test
    public void scaleUp_RestUrlNull() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(THREE, TWO);

        // when
        FlowState newState = vSystemProcessor.scaleUp(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_SCALE_UP, newState);
    }

    @Test
    public void scaleUp() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(THREE, TWO);
        parameters.put(PropertyHandler.ADMIN_REST_URL, new Setting(
                PropertyHandler.ADMIN_REST_URL, REST_URL));

        // when
        FlowState newState = vSystemProcessor.scaleUp(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_SCALE_UP_WAIT_BEFORE_NOTIFICATION,
                newState);
    }

    @Test
    public void scaleDown_SlaveClusterSizeBiggerThanServerSize()
            throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(THREE, TWO);

        // when
        FlowState newState = vSystemProcessor.scaleDown(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_CREATING, newState);
    }

    @Test
    public void scaleDown_SlaveClusterSizeEqualsServerSize() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(TWO, TWO);

        // when
        FlowState newState = vSystemProcessor.scaleDown(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED, newState);
    }

    @Test
    public void scaleDown_ServerSizeZero() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(ONE, ZERO);

        // when
        FlowState newState = vSystemProcessor.scaleDown(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_CREATING, newState);
    }

    @Test
    public void scaleDown_VSysNotInNormalState() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(ONE, TWO);

        // when
        FlowState newState = vSystemProcessor.scaleDown(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, false);

        // then
        assertEquals(FlowState.VSYSTEM_CREATING, newState);
    }

    @Test
    public void scaleDown() throws Exception {
        // given
        prepareSlaveClusterSizeAndServerIds(ONE, TWO);

        // when
        FlowState newState = vSystemProcessor.scaleDown(paramHandler,
                FlowState.VSYSTEM_CREATING, DISK_IMAGE_ID, true);

        // then
        assertEquals(FlowState.VSYSTEM_SCALE_DOWN_STOP_SERVER, newState);
    }

    @Test
    public void validateClusterSize() throws Exception {
        // given
        VSystemTemplateConfiguration templateConfiguration = mock(VSystemTemplateConfiguration.class);
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        Network network = new Network("network", "networkCategory",
                "networkId", 4);
        ArrayList<Network> networkList = new ArrayList<>();
        networkList.add(network);
        doReturn(networkList).when(templateConfiguration).getNetworks();

        VServerConfiguration vserverMaster = mock(VServerConfiguration.class);
        doReturn("networkId").when(vserverMaster).getNetworkId();
        doReturn(masterImage.getDiskImageId()).when(vserverMaster)
                .getDiskImageId();
        VServerConfiguration vserverSlave = mock(VServerConfiguration.class);
        doReturn("networkId").when(vserverSlave).getNetworkId();
        doReturn(slaveImage.getDiskImageId()).when(vserverSlave)
                .getDiskImageId();

        ArrayList<VServerConfiguration> vServerList = new ArrayList<>();
        vServerList.add(vserverMaster);
        vServerList.add(vserverSlave);
        doReturn(vServerList).when(templateConfiguration).getVServers();

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "2"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));
        // when
        vSystemProcessor.validateClusterSize(paramHandler, true, false,
                templateConfiguration);
    }

    @Test(expected = AbortException.class)
    public void validateClusterSize_CreateLimitReached() throws Exception {
        // given
        VSystemTemplateConfiguration templateConfiguration = mock(VSystemTemplateConfiguration.class);
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        Network network = new Network("network", "networkCategory",
                "networkId", 1);
        ArrayList<Network> networkList = new ArrayList<>();
        networkList.add(network);
        doReturn(networkList).when(templateConfiguration).getNetworks();
        VServerConfiguration vserverMaster = mock(VServerConfiguration.class);
        doReturn("networkId").when(vserverMaster).getNetworkId();
        doReturn(masterImage.getDiskImageId()).when(vserverMaster)
                .getDiskImageId();
        VServerConfiguration vserverSlave = mock(VServerConfiguration.class);
        doReturn("networkId").when(vserverSlave).getNetworkId();
        doReturn(slaveImage.getDiskImageId()).when(vserverSlave)
                .getDiskImageId();

        ArrayList<VServerConfiguration> vServerList = new ArrayList<>();
        vServerList.add(vserverMaster);
        vServerList.add(vserverSlave);
        doReturn(vServerList).when(templateConfiguration).getVServers();

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "2"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));
        // when
        vSystemProcessor.validateClusterSize(paramHandler, true, false,
                templateConfiguration);
    }

    @Test(expected = AbortException.class)
    public void validateClusterSize_ModifyLimitReached() throws Exception {
        // given
        VSystemConfiguration vSystemConfiguration = mock(VSystemConfiguration.class);
        doReturn(vSystemConfiguration).when(vSystemProcessor)
                .getVSystemConfiguration(paramHandler);
        DiskImage masterImage = mock(DiskImage.class);
        doReturn("masterId").when(masterImage).getDiskImageId();
        doReturn("master").when(masterImage).getDiskImageName();
        doReturn("4").when(masterImage).getMaxCpuCount();
        diskImages.put(masterImage.getDiskImageId(), masterImage);

        DiskImage slaveImage = mock(DiskImage.class);
        doReturn("slaveId").when(slaveImage).getDiskImageId();
        doReturn("slave").when(slaveImage).getDiskImageName();
        doReturn("4").when(slaveImage).getMaxCpuCount();
        diskImages.put(slaveImage.getDiskImageId(), slaveImage);

        Network network = new Network("network", "networkCategory",
                "networkId", 1);
        ArrayList<Network> networkList = new ArrayList<>();
        networkList.add(network);
        doReturn(networkList).when(vSystemConfiguration).getNetworks();

        VServerConfiguration vserverMaster = mock(VServerConfiguration.class);
        doReturn("networkId").when(vserverMaster).getNetworkId();
        doReturn(masterImage.getDiskImageId()).when(vserverMaster)
                .getDiskImageId();
        VServerConfiguration vserverSlave = mock(VServerConfiguration.class);
        doReturn("networkId").when(vserverSlave).getNetworkId();
        doReturn(slaveImage.getDiskImageId()).when(vserverSlave)
                .getDiskImageId();

        ArrayList<VServerConfiguration> vServerList = new ArrayList<>();
        vServerList.add(vserverMaster);
        vServerList.add(vserverSlave);
        doReturn(vServerList).when(vSystemConfiguration).getVServers();

        paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
        parameters.put(PropertyHandler.COUNT_CPU, new Setting(
                PropertyHandler.COUNT_CPU, "2"));
        parameters.put(PropertyHandler.CLUSTER_SIZE, new Setting(
                PropertyHandler.CLUSTER_SIZE, "2"));
        parameters.put(
                PropertyHandler.MASTER_TEMPLATE_ID,
                new Setting(PropertyHandler.MASTER_TEMPLATE_ID, masterImage
                        .getDiskImageId()));
        parameters
                .put(PropertyHandler.SLAVE_TEMPLATE_ID,
                        new Setting(PropertyHandler.SLAVE_TEMPLATE_ID,
                                slaveImage.getDiskImageId()));
        // when
        vSystemProcessor.validateClusterSize(paramHandler, false, true, null);
    }

    @Test
    public void manageModificationProcess_stop() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(VServerStatus.STOPPED).when(this.vSystemProcessor.vserverComm)
                .getNonErrorVServerStatus(paramHandler);
        FlowState newflowState = null;

        // when
        FlowState result = vSystemProcessor.manageScaling(CONTROLLER_ID,
                INSTANCE_ID, paramHandler,
                FlowState.VSYSTEM_SCALE_DOWN_DESTROY_SERVER, newflowState);

        // then
        assertEquals(FlowState.VSYSTEM_SCALE_DOWN, result);

    }

    @Test
    public void manageModificationProcess_completed() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        doReturn(VServerStatus.STOPPED).when(this.vSystemProcessor.vserverComm)
                .getNonErrorVServerStatus(paramHandler);
        FlowState newflowState = null;

        // when
        FlowState result = vSystemProcessor.manageScaling(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_SCALING_COMPLETED,
                newflowState);

        // then
        assertEquals(FlowState.VSYSTEM_RETRIEVEGUEST, result);

    }

    @Test
    public void manageModificationProcess_notCompleted() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus("");
        doReturn(VServerStatus.STOPPED).when(this.vSystemProcessor.vserverComm)
                .getNonErrorVServerStatus(paramHandler);
        FlowState newflowState = FlowState.VSYSTEM_SCALING_COMPLETED;

        // when
        FlowState result = vSystemProcessor.manageScaling(CONTROLLER_ID,
                INSTANCE_ID, paramHandler, FlowState.VSYSTEM_SCALING_COMPLETED,
                newflowState);

        // then
        assertEquals(FlowState.VSYSTEM_SCALING_COMPLETED, result);

    }

    @Test(expected = SuspendException.class)
    public void manageModificationProcess_stopFailed() throws Exception {
        // given
        paramHandler.getIaasContext().setVSystemStatus(NORMAL);
        paramHandler.setVserverId(SERVER_ID);
        doReturn(INSTANCE_ID).when(paramHandler).getInstanceName();
        doReturn(VServerStatus.STOP_ERROR).when(
                this.vSystemProcessor.vserverComm).getVServerStatus(
                paramHandler);
        doThrow(
                new SuspendException(Messages.getAll(
                        "error_failed_to_stop_vserver", new Object[] {
                                SERVER_ID, INSTANCE_ID }))).when(
                this.vSystemProcessor.vserverComm).getNonErrorVServerStatus(
                paramHandler);

        FlowState newflowState = null;

        // when
        try {
            vSystemProcessor.manageScaling(CONTROLLER_ID, INSTANCE_ID,
                    paramHandler, FlowState.VSYSTEM_SCALE_DOWN_DESTROY_SERVER,
                    newflowState);

        }
        // then
        catch (SuspendException se) {
            assertEquals(Messages.get("en", "error_failed_to_stop_vserver",
                    new Object[] { SERVER_ID, INSTANCE_ID }),
                    se.getLocalizedMessage("en"));
            assertEquals(
                    Messages.getAll("error_failed_to_stop_vserver",
                            new Object[] { SERVER_ID, INSTANCE_ID }).size(), se
                            .getLocalizedMessages().size());
            throw se;

        }

    }

    @Test
    public void process_CatchMissingResourceException() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_DELETION);
        paramHandler.setVsysId("systemId");

        doThrow(
                new MissingResourceException("test", ResourceType.VSYSTEM,
                        "systemId")).when(vSystemProcessor).validateParameters(
                paramHandler);

        // when
        vSystemProcessor.process("controllerId", "instanceId", paramHandler);

        // then
        assertEquals(FlowState.DESTROYED, paramHandler.getState());
        assertEquals("", paramHandler.getVsysId());
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_ResourceTypeFalse()
            throws Exception {
        // given
        doThrow(
                new MissingResourceException("test", ResourceType.UNKNOWN,
                        "systemId")).when(vSystemProcessor).validateParameters(
                paramHandler);

        // when
        vSystemProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_NoResourceType() throws Exception {
        // given
        doThrow(
                new MissingResourceException("test", ResourceType.VSYSTEM,
                        "systemId")).when(vSystemProcessor).validateParameters(
                paramHandler);

        // when
        vSystemProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_NoServerId() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_DELETION);
        paramHandler.setVsysId(null);
        doThrow(
                new MissingResourceException("test", ResourceType.VSYSTEM,
                        "systemId")).when(vSystemProcessor).validateParameters(
                paramHandler);

        // when
        vSystemProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void process_ThrowSuspendException_ServerIdFalse() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_DELETION);
        paramHandler.setVsysId("serverIdTest");
        doThrow(
                new MissingResourceException("test", ResourceType.VSYSTEM,
                        "systemId")).when(vSystemProcessor).validateParameters(
                paramHandler);

        // when
        vSystemProcessor.process("controllerId", "instanceId", paramHandler);
    }

    @Test(expected = InstanceNotAliveException.class)
    public void process_ThrowInstanceNotAliveException() throws Exception {
        // given
        paramHandler.setOperation(Operation.VSYSTEM_OPERATION);
        paramHandler.setVsysId("serverIdTest");
        doThrow(
                new MissingResourceException("test", ResourceType.VSYSTEM,
                        "systemId")).when(vSystemProcessor).validateParameters(
                paramHandler);

        // when
        vSystemProcessor.process("controllerId", "instanceId", paramHandler);
    }

    private void prepareSlaveClusterSizeAndServerIds(int slaveClusterSize,
            int slaveServersPresent) throws Exception {
        doReturn(Integer.valueOf(slaveClusterSize)).when(vSystemProcessor)
                .getSlaveClusterSize(paramHandler);
        prepareServerIds(slaveServersPresent);
    }

    private void prepareServerIds(int slaveServersPresent) throws Exception {
        List<String> serverIds = new ArrayList<>();
        for (int i = 0; i < slaveServersPresent; i++) {
            serverIds.add(SERVER_ID);
        }
        when(
                vSystemProcessor.vsysComm.getVServersForTemplate(anyString(),
                        eq(paramHandler))).thenReturn(serverIds);
    }

    private VSystemConfiguration givenVSystemConfiguration(
            VServerConfiguration server) throws Exception {
        VSystemConfiguration configuration = mock(VSystemConfiguration.class);
        List<VServerConfiguration> servers = new ArrayList<>();
        servers.add(server);
        doReturn(servers).when(configuration).getVServers();
        return configuration;
    }

    private VServerConfiguration givenVServerConfiguration(String existingCPU,
            String diskImageId) {
        VServerConfiguration server = mock(VServerConfiguration.class);
        doReturn(SERVER_ID).when(server).getServerId();
        doReturn(existingCPU).when(server).getNumOfCPU();
        doReturn(diskImageId).when(server).getDiskImageId();
        return server;
    }

}
