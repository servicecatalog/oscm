/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.03.2014                                                      
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.iaas.data.AccessInformation;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.exceptions.CommunicationException;
import org.oscm.app.iaas.exceptions.IaasException;
import org.oscm.app.iaas.intf.VServerCommunication;
import org.oscm.app.iaas.intf.VSystemCommunication;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author iversen
 * 
 */
public class ProcessManagerBeanTest {
    private ProcessManagerBean processManagerBean;
    private HashMap<String, Setting> parameters;
    private PropertyHandler paramHandler;
    private final String INSTANCEID = "INSTANCEID";
    private final String PRIVATEIP = "123.123.123.123";
    private final String IPPASSWORD = "IPPASSWORD";

    @Before
    public void setUp() throws Exception {
        processManagerBean = new ProcessManagerBean();
        processManagerBean.vsystemComm = mock(VSystemCommunication.class);
        processManagerBean.vserverComm = mock(VServerCommunication.class);

        processManagerBean.vServerProcessor = mock(VServerProcessorBean.class);
        processManagerBean.vSysProcessor = mock(VSystemProcessorBean.class);

        processManagerBean.platformService = mock(APPlatformService.class);

        doNothing().when(processManagerBean.platformService).sendMail(
                anyListOf(String.class), anyString(), anyString());

        parameters = new HashMap<>();
        HashMap<String, Setting> configSettings = new HashMap<>();
        ProvisioningSettings settings = new ProvisioningSettings(parameters,
                configSettings, "en");
        paramHandler = new PropertyHandler(settings);
    }

    @Test
    public void getConnectionData_NoPublicIP() throws Exception {
        // given
        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "template"));

        // when
        String result = processManagerBean.getConnectionData(INSTANCEID,
                paramHandler);

        // then
        assertEquals("ID: INSTANCEID (No IP information available)", result);

    }

    @Test
    public void getConnectionDatais_PublicIP() throws Exception {
        // given
        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "template"));
        List<AccessInformation> publicIps = new ArrayList<>();
        publicIps.add(new AccessInformation("1.2.3.4", "password"));
        publicIps.add(new AccessInformation("1.2.3.5", "secret"));
        doReturn(publicIps).when(processManagerBean.vsystemComm).getAccessInfo(
                paramHandler);

        // when
        String result = processManagerBean.getConnectionData(INSTANCEID,
                paramHandler);

        // then
        assertEquals("ID: INSTANCEID\r\n1.2.3.4\tpassword\r\n1.2.3.5\tsecret",
                result);

    }

    @Test
    public void getConnectionData_PrivateIp() throws Exception {
        // given
        doReturn(PRIVATEIP).when(processManagerBean.vserverComm).getInternalIp(
                paramHandler);
        doReturn(IPPASSWORD).when(processManagerBean.vserverComm)
                .getVServerInitialPassword(paramHandler);

        // when
        String result = processManagerBean.getConnectionData(INSTANCEID,
                paramHandler);

        // then
        assertEquals("INSTANCEID (" + PRIVATEIP + "), initial password ("
                + IPPASSWORD + ")", result);
    }

    @Test
    public void getConnectionData_NoPrivateIp() throws Exception {
        // given
        doReturn(null).when(processManagerBean.vserverComm).getInternalIp(
                paramHandler);
        doReturn(IPPASSWORD).when(processManagerBean.vserverComm)
                .getVServerInitialPassword(paramHandler);

        // when
        String result = processManagerBean.getConnectionData(INSTANCEID,
                paramHandler);

        // then
        assertNull(result);
    }

    @Test(expected = APPlatformException.class)
    public void getControllerInstanceStatus_NoState() throws Exception {
        // given a paramHandler with no state set (FlowState.FAILED)

        // when
        processManagerBean.getControllerInstanceStatus("controllerId",
                "instanceId", paramHandler);

        // then an APPlatformException is thrown
    }

    @Test
    public void getControllerInstanceStatus_SuspendedInstance_invalidSuspendTime()
            throws Exception {
        // given an invalid suspend time
        parameters.put(PropertyHandler.SUSPEND_UNTIL, new Setting(
                PropertyHandler.SUSPEND_UNTIL, "invalidSuspendUntil"));
        paramHandler.setState(FlowState.FINISHED);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then the normal instance status is returned
        assertNotNull(result);
        assertTrue(result.isReady());
        assertTrue(result.getRunWithTimer());
    }

    @Test
    public void getControllerInstanceStatus_SuspendedInstance()
            throws Exception {

        long suspendTime = System.currentTimeMillis() + 1000;
        parameters.put(PropertyHandler.SUSPEND_UNTIL, new Setting(
                PropertyHandler.SUSPEND_UNTIL, String.valueOf(suspendTime)));
        paramHandler.setState(FlowState.FINISHED);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then the instance status for a suspended instance is returned
        assertNotNull(result);
        assertFalse(result.isReady());
        assertTrue(result.getRunWithTimer());
    }

    @Test(expected = Exception.class)
    public void getControllerInstanceStatus_ProvisioningException()
            throws Exception {
        // given
        paramHandler.setState(FlowState.VSERVER_CREATED);

        IaasException mockIaasException = mock(IaasException.class);
        doThrow(mockIaasException).when(processManagerBean.vServerProcessor)
                .process(anyString(), anyString(), any(PropertyHandler.class));

        // when
        processManagerBean.getControllerInstanceStatus("controllerId",
                "instanceId", paramHandler);
    }

    @Test
    public void getControllerInstanceStatus_ProvException_isBusyMessage()
            throws Exception {
        // given
        paramHandler.setState(FlowState.VSERVER_CREATED);

        IaasException mockIaasException = mock(IaasException.class);
        doReturn(Boolean.TRUE).when(mockIaasException).isBusyMessage();

        doThrow(mockIaasException).when(processManagerBean.vServerProcessor)
                .process(anyString(), anyString(), any(PropertyHandler.class));

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then
        assertNotNull(result);
    }

    @Test
    public void getControllerInstanceStatus_VServerProvisioning()
            throws Exception {
        // given a VServer provisioning
        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, ""));
        paramHandler.setState(FlowState.VSERVER_CREATED);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then
        assertNotNull(result);
    }

    @Test
    public void getControllerInstanceStatus_VSystemProvisioning()
            throws Exception {
        // given a VSystem provisioning
        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "template"));
        paramHandler.setState(FlowState.VSERVER_CREATED);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then
        assertNotNull(result);
    }

    @Test
    public void getControllerInstanceStatus_FinishedState() throws Exception {
        // given a finished operation state
        paramHandler.setState(FlowState.FINISHED);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then
        assertNotNull(result);
        assertTrue(result.isReady());
        assertTrue(result.getRunWithTimer());
    }

    @Test
    public void getControllerInstanceStatus_DestroyedState() throws Exception {
        // given a destroyed operation state
        paramHandler.setState(FlowState.DESTROYED);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then
        assertNotNull(result);
        assertTrue(result.isReady());
        assertTrue(result.getRunWithTimer());
    }

    @Test(expected = SuspendException.class)
    public void getControllerInstanceStatus_NoConnection_ServerProcessor()
            throws Exception {
        // given no ROR connection
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
        doThrow(new CommunicationException("Connection failed", "hostname"))
                .when(processManagerBean.vServerProcessor).process(
                        "controllerId", "instanceId", paramHandler);

        // when
        processManagerBean.getControllerInstanceStatus("controllerId",
                "instanceId", paramHandler);
    }

    @Test(expected = SuspendException.class)
    public void getControllerInstanceStatus_NoConnection_SysProcessor()
            throws Exception {
        // given no ROR connection
        parameters.put(PropertyHandler.SYSTEM_TEMPLATE_ID, new Setting(
                PropertyHandler.SYSTEM_TEMPLATE_ID, "template"));
        paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
        doThrow(new CommunicationException("Connection failed", "hostname"))
                .when(processManagerBean.vSysProcessor).process("controllerId",
                        "instanceId", paramHandler);

        // when
        processManagerBean.getControllerInstanceStatus("controllerId",
                "instanceId", paramHandler);
    }

    @Test
    public void getControllerInstanceStatus_ManualState() throws Exception {
        // given a manual state
        paramHandler.setState(FlowState.MANUAL);

        // when
        InstanceStatus result = processManagerBean.getControllerInstanceStatus(
                "controllerId", "instanceId", paramHandler);

        // then
        assertNotNull(result);
        assertFalse(result.isReady());
        assertFalse(result.getRunWithTimer());
    }

    @Test
    public void getControllerInstanceStatus_Modification_NoMail()
            throws Exception {
        // given a modification operation but with no mail for notification set
        paramHandler.setOperation(Operation.VSERVER_MODIFICATION);
        paramHandler.setState(FlowState.FINISHED);
        // when
        InstanceStatus instanceStatus = processManagerBean
                .getControllerInstanceStatus("controllerId", "instanceId",
                        paramHandler);

        // then
        assertNotNull(instanceStatus);
        assertTrue(instanceStatus.isReady());
        // no notification email is sent
        verify(processManagerBean.platformService, times(0)).sendMail(
                anyListOf(String.class), anyString(), anyString());
    }

    @Test
    public void getControllerInstanceStatus_Modification_Mail()
            throws Exception {

        parameters.put(PropertyHandler.MAIL_FOR_NOTIFICATION, new Setting(
                PropertyHandler.MAIL_FOR_NOTIFICATION, "test@email.com"));
        paramHandler.setOperation(Operation.VSERVER_MODIFICATION);
        paramHandler.setState(FlowState.FINISHED);
        // when
        InstanceStatus instanceStatus = processManagerBean
                .getControllerInstanceStatus("controllerId", "instanceId",
                        paramHandler);

        // then
        assertNotNull(instanceStatus);
        assertTrue(instanceStatus.isReady());
        // no notification email is sent
        verify(processManagerBean.platformService, times(1)).sendMail(
                anyListOf(String.class), anyString(), anyString());
    }

    @Test
    public void getControllerInstanceStatus_Mail_NoModification()
            throws Exception {
        // given an operation which is not a modification and a mail for
        // notification set
        parameters.put(PropertyHandler.MAIL_FOR_NOTIFICATION, new Setting(
                PropertyHandler.MAIL_FOR_NOTIFICATION, "test@email.com"));
        paramHandler.setOperation(Operation.VSERVER_CREATION);
        paramHandler.setState(FlowState.FINISHED);
        // when
        InstanceStatus instanceStatus = processManagerBean
                .getControllerInstanceStatus("controllerId", "instanceId",
                        paramHandler);

        // then
        assertNotNull(instanceStatus);
        assertTrue(instanceStatus.isReady());
        // no notification email is sent.
        verify(processManagerBean.platformService, times(0)).sendMail(
                anyListOf(String.class), anyString(), anyString());

    }
}
