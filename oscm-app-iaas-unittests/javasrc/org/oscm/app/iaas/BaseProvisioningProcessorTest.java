/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 26, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author farmaki
 * 
 */
public class BaseProvisioningProcessorTest {

    public static final String CONTROLLER_ID = "ess.ror";

    private APPlatformService platformService;

    private BaseProvisioningProcessor baseProvisioningProcessor;

    private PropertyHandler paramHandler;

    @Before
    public void setUp() throws Exception {
        platformService = mock(APPlatformService.class);
        doReturn(Boolean.FALSE).when(platformService).lockServiceInstance(
                eq(CONTROLLER_ID), anyString(),
                any(PasswordAuthentication.class));

        baseProvisioningProcessor = new BaseProvisioningProcessor() {
            @Override
            public void process(String controllerId, String instanceId,
                    PropertyHandler paramHandler) throws Exception {

            }
        };

        baseProvisioningProcessor.setPlatformService(platformService);

        ProvisioningSettings settings = new ProvisioningSettings(
                new HashMap<String, Setting>(), new HashMap<String, Setting>(),
                "en");
        settings.getConfigSettings().put(
                PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                new Setting(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                        "false"));
        paramHandler = new PropertyHandler(settings);
    }

    @Test
    public void checkNextStatus_ConflictingOperation() throws Exception {
        // given a conflicting operation

        // when
        boolean isNextStatusDefined = baseProvisioningProcessor
                .checkNextStatus(CONTROLLER_ID, "instanceId",
                        FlowState.VSERVER_CREATING, paramHandler);

        // then ask for exclusive processing
        assertFalse(isNextStatusDefined);
        verify(platformService, times(1)).lockServiceInstance(anyString(),
                anyString(), any(PasswordAuthentication.class));
    }

    @Test
    public void checkNextStatus_SafeOperation() throws Exception {
        // given a safe operation

        // when
        boolean isNextStatusDefined = baseProvisioningProcessor
                .checkNextStatus(CONTROLLER_ID, "instanceId",
                        FlowState.FINISHED, paramHandler);

        // then the exclusive token is released.
        assertTrue(isNextStatusDefined);
        verify(platformService, times(1)).unlockServiceInstance(anyString(),
                anyString(), any(PasswordAuthentication.class));
    }

    @Test
    public void disableExclusiveProcessing() throws Exception {
        // given

        // when
        baseProvisioningProcessor.disableExclusiveProcessing(CONTROLLER_ID,
                "instanceId", paramHandler);

        // then
        verify(platformService, times(1)).unlockServiceInstance(anyString(),
                anyString(), any(PasswordAuthentication.class));

    }
}
