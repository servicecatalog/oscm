/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 05.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.OperationParameter;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;

/**
 * @author stavreva
 * 
 */
public class IaasControllerTest {

    private HashMap<String, Setting> parameters;
    private ProvisioningSettings settings;
    private APPlatformController controller = Mockito.spy(new IaasController() {

        @Override
        public void validateDiskName(PropertyHandler paramHandler)
                throws APPlatformException {
        }

        @Override
        protected String getControllerID() {
            return null;
        }
    });

    @Before
    public void setUp() throws Exception {
        parameters = new HashMap<>();
        HashMap<String, Setting> configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings, "en");

    }

    @Test
    public void activateInstance_System() throws APPlatformException {
        parameters.put("SYSTEM_TEMPLATE_ID", new Setting("SYSTEM_TEMPLATE_ID",
                "some value"));
        InstanceStatus result = controller.activateInstance("instanceId",
                settings);
        HashMap<String, Setting> params = result.getChangedParameters();
        assertEquals(Operation.VSYSTEM_ACTIVATION.name(),
                params.get("OPERATION").getValue());
        assertEquals(FlowState.VSYSTEM_ACTIVATION_REQUESTED.name(),
                params.get("API_STATUS").getValue());
    }

    @Test
    public void activateInstance_Server() throws APPlatformException {
        // given
        parameters = new HashMap<>();

        // when
        InstanceStatus result = controller.activateInstance("instanceId",
                settings);

        // then
        HashMap<String, Setting> params = result.getChangedParameters();
        assertEquals(Operation.VSERVER_ACTIVATION.name(),
                params.get("OPERATION").getValue());
        assertEquals(FlowState.VSERVER_ACTIVATION_REQUESTED.name(),
                params.get("API_STATUS").getValue());
    }

    @Test
    public void deactivateInstance_System() throws APPlatformException {
        // given
        parameters.put("SYSTEM_TEMPLATE_ID", new Setting("SYSTEM_TEMPLATE_ID",
                "some value"));

        // when
        InstanceStatus result = controller.deactivateInstance("instanceId",
                settings);

        // then
        HashMap<String, Setting> params = result.getChangedParameters();
        assertEquals(Operation.VSYSTEM_ACTIVATION.name(),
                params.get("OPERATION").getValue());
        assertEquals(FlowState.VSYSTEM_DEACTIVATION_REQUESTED.name(), params
                .get("API_STATUS").getValue());
    }

    @Test
    public void deactivateInstance_Server() throws APPlatformException {
        // given
        parameters = new HashMap<>();

        // when
        InstanceStatus result = controller.deactivateInstance("instanceId",
                settings);

        // then
        HashMap<String, Setting> params = result.getChangedParameters();
        assertEquals(Operation.VSERVER_ACTIVATION.name(),
                params.get("OPERATION").getValue());
        assertEquals(FlowState.VSERVER_DEACTIVATION_REQUESTED.name(), params
                .get("API_STATUS").getValue());
    }

    @Test
    public void executeServiceOperation_VSERVER_START_REQUESTED()
            throws APPlatformException {
        // given
        List<OperationParameter> parameters = new ArrayList<>();
        // when
        InstanceStatus result = controller.executeServiceOperation("userId",
                "instanceId", "transactionId", "START_VIRTUAL_SERVER",
                parameters, settings);
        // then
        HashMap<String, Setting> params = result.getChangedParameters();
        assertEquals(Operation.VSERVER_OPERATION.name(), params
                .get("OPERATION").getValue());
        assertEquals(FlowState.VSERVER_START_REQUESTED.name(),
                params.get("API_STATUS").getValue());
    }

    @Test
    public void executeServiceOperation_STOP_VIRTUAL_SERVER()
            throws APPlatformException {
        // given
        List<OperationParameter> parameters = new ArrayList<>();
        // when
        InstanceStatus result = controller.executeServiceOperation("userId",
                "instanceId", "transactionId", "STOP_VIRTUAL_SERVER",
                parameters, settings);
        // then
        HashMap<String, Setting> params = result.getChangedParameters();
        assertEquals(Operation.VSERVER_OPERATION.name(), params
                .get("OPERATION").getValue());
        assertEquals(FlowState.VSERVER_STOP_REQUESTED.name(),
                params.get("API_STATUS").getValue());
    }

}
