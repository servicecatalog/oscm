/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.aws.data.FlowState;
import org.oscm.app.aws.data.Operation;
import org.oscm.app.aws.i18n.Messages;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;

/**
 * Unit test of property handler
 */
public class PropertyHandlerTest {
    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private PropertyHandler propertyHandler;

    @Before
    public void setUp() throws Exception {
        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings, "en");
        propertyHandler = new PropertyHandler(settings);
    }

    @Test()
    public void testGetSettings() throws Exception {
        assertEquals(settings, new PropertyHandler(settings).getSettings());
    }

    @Test()
    public void testGetInstanceName() throws Exception {
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "tstdemo"));
        propertyHandler = new PropertyHandler(settings);
        String instanceName = propertyHandler.getInstanceName();
        assertEquals("tstdemo", instanceName);
    }

    @Test()
    public void testGetInstanceNameWithPrefix() throws Exception {
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, "ess"));
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "tstdemo"));
        propertyHandler = new PropertyHandler(settings);
        String instanceName = propertyHandler.getInstanceName();
        assertEquals("esststdemo", instanceName);
    }

    @Test()
    public void testGetState_NullValue() throws Exception {
        FlowState status = propertyHandler.getState();
        assertEquals(FlowState.FAILED, status);
    }

    @Test()
    public void testGetState() throws Exception {
        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.ACTIVATION_REQUESTED.name()));
        propertyHandler = new PropertyHandler(settings);
        FlowState state = propertyHandler.getState();
        assertEquals(FlowState.ACTIVATION_REQUESTED, state);
    }

    @Test()
    public void testGetOperation_NullValue() throws Exception {
        Operation operation = propertyHandler.getOperation();
        assertEquals(Operation.UNKNOWN, operation);
    }

    @Test()
    public void testGetOperation() throws Exception {
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_ACTIVATION.name()));
        propertyHandler = new PropertyHandler(settings);
        Operation operation = propertyHandler.getOperation();
        assertEquals(Operation.EC2_ACTIVATION, operation);
    }

    @Test()
    public void testGetSecretKey() throws Exception {
        configSettings.put(PropertyHandler.SECRET_KEY_PWD, new Setting(
                PropertyHandler.SECRET_KEY_PWD, "key"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("key", propertyHandler.getSecretKey());
    }

    @Test()
    public void testGetAccessKeyId() throws Exception {
        configSettings.put(PropertyHandler.ACCESS_KEY_ID_PWD, new Setting(
                PropertyHandler.ACCESS_KEY_ID_PWD, "akid"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("akid", propertyHandler.getAccessKeyId());
    }

    @Test()
    public void testGetRegion() throws Exception {
        parameters.put(PropertyHandler.REGION, new Setting(
                PropertyHandler.REGION, "east-1"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("east-1", propertyHandler.getRegion());
    }

    @Test()
    public void testGetKeyPairName() throws Exception {
        parameters.put(PropertyHandler.KEY_PAIR_NAME, new Setting(
                PropertyHandler.KEY_PAIR_NAME, "kpn1"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("kpn1", propertyHandler.getKeyPairName());
    }

    @Test()
    public void testGetInstanceType() throws Exception {
        parameters.put(PropertyHandler.INSTANCE_TYPE, new Setting(
                PropertyHandler.INSTANCE_TYPE, "type"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("type", propertyHandler.getInstanceType());
    }

    @Test()
    public void testAWS_InstanceID() throws Exception {
        propertyHandler = new PropertyHandler(settings);
        propertyHandler.setAWSInstanceId("test1");
        assertEquals("test1", propertyHandler.getAWSInstanceId());
    }

    @Test()
    public void testGetImageName() throws Exception {
        parameters.put(PropertyHandler.IMAGE_NAME, new Setting(
                PropertyHandler.IMAGE_NAME, "Image1"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("Image1", propertyHandler.getImageName());
    }

    @Test
    public void testMissingResource() throws Exception {
        String message = Messages.get("de", "key");
        assertEquals("!key!", message);
    }

    @Test
    public void testLocalizedResources() throws Exception {
        String message1 = Messages.get("de", "status_INSTANCE_OVERALL");
        String message2 = Messages.get("en", "status_INSTANCE_OVERALL");
        String message3 = Messages.get("fr", "status_INSTANCE_OVERALL");
        assertFalse(message1.equals(message2));
        assertTrue(message2.equals(message3));
    }

    @Test
    public void testGetAll() throws Exception {
        List<LocalizedText> all = Messages.getAll("error_invalid_image",
                "123456789");
        assertNotNull(all);
        assertTrue(all.size() > 0);
        assertTrue(all.get(0).getText().contains("123456789"));
    }

    @Test
    public void testGetAll2() throws Exception {
        List<LocalizedText> all = Messages.getAll("error_invalid_image");
        assertNotNull(all);
        assertTrue(all.size() > 0);
        assertTrue(all.get(0).getText().contains("{0}"));
    }
}
