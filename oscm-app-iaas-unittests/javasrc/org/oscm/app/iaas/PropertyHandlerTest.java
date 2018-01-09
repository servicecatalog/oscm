/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-17                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.iaas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.ConfigurationException;

/**
 * Tests for the property handling.
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
    public void testGetInstanceName() throws Exception {
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, "ess"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "tstdemo"));
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
        parameters.put(PropertyHandler.API_STATUS, new Setting(
                PropertyHandler.API_STATUS, "VSERVER_CREATING"));
        propertyHandler = new PropertyHandler(settings);
        FlowState status = propertyHandler.getState();
        assertEquals(FlowState.VSERVER_CREATING, status);
    }

    @Test()
    public void testGetOperation_NullValue() throws Exception {
        Operation operation = propertyHandler.getOperation();
        assertEquals(Operation.UNKNOWN, operation);
    }

    @Test()
    public void testGetOperation() throws Exception {
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, "VSERVER_MODIFICATION"));
        propertyHandler = new PropertyHandler(settings);
        Operation operation = propertyHandler.getOperation();
        assertEquals(Operation.VSERVER_MODIFICATION, operation);
    }

    /*
     * @Test(expected = RuntimeException.class) public void
     * testGetLocale_NullValue() { //propertyHandler.getLocale(); }
     */
    @Test()
    public void testGetLocale() throws Exception {
        configSettings.put(PropertyHandler.IAAS_API_LOCALE, new Setting(
                PropertyHandler.IAAS_API_LOCALE, "EN"));
        propertyHandler = new PropertyHandler(settings);
        String locale = propertyHandler.getAPILocale();
        assertEquals("EN", locale);
    }

    @Test()
    public void testGetURL() throws Exception {
        configSettings.put(PropertyHandler.IAAS_API_URI, new Setting(
                PropertyHandler.IAAS_API_URI, "https://..."));
        propertyHandler = new PropertyHandler(settings);
        String url = propertyHandler.getURL();
        assertEquals("https://...", url);
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

    @Test()
    public void testGetVDiskSize() throws Exception {
        parameters.put(PropertyHandler.VDISK_SIZE, new Setting(
                PropertyHandler.VDISK_SIZE, "10"));
        propertyHandler = new PropertyHandler(settings);
        String size = propertyHandler.getVDiskSize();
        assertEquals("10", size);
    }

    @Test()
    public void testGetVDiskName() throws Exception {
        parameters.put(PropertyHandler.VDISK_NAME, new Setting(
                PropertyHandler.VDISK_SIZE, "vdisk"));
        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, "ess"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "tstdemo"));
        propertyHandler = new PropertyHandler(settings);
        String name = propertyHandler.getVDiskName();
        assertEquals("esststdemovdisk", name);
    }

    @Test()
    public void testGetVDiskId() throws Exception {
        parameters.put(PropertyHandler.VDISK_ID, new Setting(
                PropertyHandler.VDISK_ID, "vdiskid"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getVDiskId();
        assertEquals("vdiskid", id);
    }

    @Test()
    public void testGetVsysId() throws Exception {
        parameters.put(PropertyHandler.VSYS_ID, new Setting(
                PropertyHandler.VSYS_ID, "vsyid"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getVsysId();
        assertEquals("vsyid", id);
    }

    @Test()
    public void testGetVserverId() throws Exception {
        parameters.put(PropertyHandler.VSERVER_ID, new Setting(
                PropertyHandler.VSERVER_ID, "vserverid"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getVserverId();
        assertEquals("vserverid", id);
    }

    @Test()
    public void testGetDiskImageId() throws Exception {
        parameters.put(PropertyHandler.DISKIMG_ID, new Setting(
                PropertyHandler.DISKIMG_ID, "diskimgid"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getDiskImageId();
        assertEquals("diskimgid", id);
    }

    @Test()
    public void testGetVserverType() throws Exception {
        parameters.put(PropertyHandler.VSERVER_TYPE, new Setting(
                PropertyHandler.VSERVER_TYPE, "type"));
        propertyHandler = new PropertyHandler(settings);
        String type = propertyHandler.getVserverType();
        assertEquals("type", type);
    }

    @Test()
    public void testGetNetworkId() throws Exception {
        parameters.put(PropertyHandler.NETWORK_ID, new Setting(
                PropertyHandler.NETWORK_ID, "networkid"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getNetworkId();
        assertEquals("networkid", id);
    }

    @Test()
    public void testGetInstanceNamePattern() throws Exception {
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, "estess([a-z0-9]){6,8}"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getInstanceNamePattern();
        assertEquals("estess([a-z0-9]){6,8}", id);
    }

    @Test()
    public void testSetVDiskName() throws Exception {

        parameters.put(PropertyHandler.INSTANCENAME_PREFIX, new Setting(
                PropertyHandler.INSTANCENAME_PREFIX, "ess"));
        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "tstdemo"));
        propertyHandler = new PropertyHandler(settings);
        propertyHandler.setVDiskName("vdiskname");
        String vdiskname = propertyHandler.getVDiskName();
        assertEquals("esststdemovdiskname", vdiskname);
    }

    @Test()
    public void testSetVDiskId() throws Exception {
        propertyHandler = new PropertyHandler(settings);
        propertyHandler.setVDiskId("vdiskid");
        String vdiskid = propertyHandler.getVDiskId();
        assertEquals("vdiskid", vdiskid);
    }

    @Test()
    public void testGetUser() throws Exception {
        configSettings.put(PropertyHandler.IAAS_API_USER, new Setting(
                PropertyHandler.IAAS_API_USER, "user"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getUser();
        assertEquals("user", id);
    }

    @Test()
    public void testGetPassword() throws Exception {
        configSettings.put(PropertyHandler.IAAS_API_PWD, new Setting(
                PropertyHandler.IAAS_API_PWD, "pwd"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getPassword();
        assertEquals("pwd", id);
    }

    @Test()
    public void testGetTenantId() throws Exception {
        configSettings.put(PropertyHandler.IAAS_API_TENANT, new Setting(
                PropertyHandler.IAAS_API_TENANT, "tenantid"));
        propertyHandler = new PropertyHandler(settings);
        String id = propertyHandler.getTenantId();
        assertEquals("tenantid", id);
    }

    @Test()
    public void testGetVServerList() throws Exception {
        String prefixVServer1 = "VSERVER_1";
        String prefixVServer2 = "VSERVER_2";

        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "tstdemo"));
        parameters.put(PropertyHandler.VDISK_NAME, new Setting(
                PropertyHandler.VDISK_NAME, "vdisk"));

        parameters.put(prefixVServer1, new Setting(prefixVServer1, "true"));
        parameters.put(prefixVServer1 + "_"
                + PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                prefixVServer1 + "_" + PropertyHandler.INSTANCENAME_CUSTOM,
                "tstdemo1"));
        parameters.put(prefixVServer1 + "_" + PropertyHandler.VDISK_NAME,
                new Setting(prefixVServer1 + "_" + PropertyHandler.VDISK_NAME,
                        "vdisk1"));

        parameters.put(prefixVServer2, new Setting(prefixVServer2, "true"));
        parameters.put(prefixVServer2 + "_"
                + PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                prefixVServer2 + "_" + PropertyHandler.INSTANCENAME_CUSTOM,
                "tstdemo2"));
        parameters.put(prefixVServer2 + "_" + PropertyHandler.VDISK_NAME,
                new Setting(prefixVServer2 + "_"
                        + PropertyHandler.INSTANCENAME_CUSTOM, "vdisk2"));

        propertyHandler = new PropertyHandler(settings);
        PropertyHandler[] list = propertyHandler.getVserverList();
        assertEquals(2, list.length);

        assertEquals("tstdemo", propertyHandler.getInstanceNameCustom());
        assertEquals("vdisk", propertyHandler.getVDiskNameCustom());

        assertEquals("tstdemo1", list[0].getInstanceNameCustom());
        assertEquals("vdisk1", list[0].getVDiskNameCustom());

        assertEquals("tstdemo2", list[1].getInstanceNameCustom());
        assertEquals("vdisk2", list[1].getVDiskNameCustom());

    }

    @Test()
    public void testSetVServerList() throws Exception {
        String prefixVServer1 = "VSERVER_1";
        String prefixVServer2 = "VSERVER_2";

        parameters.put(PropertyHandler.VDISK_NAME, new Setting(
                PropertyHandler.VDISK_NAME, "vdisk"));
        parameters.put(prefixVServer1, new Setting(prefixVServer1, "true"));
        parameters.put(prefixVServer1 + "_" + PropertyHandler.VDISK_NAME,
                new Setting(prefixVServer1 + "_" + PropertyHandler.VDISK_NAME,
                        "vdisk1"));
        parameters.put(prefixVServer2, new Setting(prefixVServer2, "true"));
        parameters.put(prefixVServer2 + "_" + PropertyHandler.VDISK_NAME,
                new Setting(prefixVServer2 + "_" + PropertyHandler.VDISK_NAME,
                        "vdisk2"));

        propertyHandler = new PropertyHandler(settings);
        PropertyHandler[] list = propertyHandler.getVserverList();
        assertEquals(2, list.length);

        assertEquals("vdisk", propertyHandler.getVDiskNameCustom());
        assertEquals("vdisk1", list[0].getVDiskNameCustom());
        assertEquals("vdisk2", list[1].getVDiskNameCustom());

        list[1].setVDiskName("vdisk2_new");

        list = propertyHandler.getVserverList();
        assertEquals(2, list.length);

        assertEquals("vdisk", propertyHandler.getVDiskNameCustom());
        assertEquals("vdisk1", list[0].getVDiskNameCustom());
        assertEquals("vdisk2_new", list[1].getVDiskNameCustom());

    }

    @Test()
    public void testGetVServerListWithConfig() throws Exception {
        String prefixVServer1 = "VSERVER_1";
        String prefixVServer2 = "VSERVER_2";

        parameters.put(PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                PropertyHandler.INSTANCENAME_CUSTOM, "tstdemo"));
        parameters.put(PropertyHandler.VDISK_NAME, new Setting(
                PropertyHandler.VDISK_NAME, "vdisk"));

        parameters.put(prefixVServer1, new Setting(prefixVServer1, "true"));
        parameters.put(prefixVServer1 + "_"
                + PropertyHandler.INSTANCENAME_CUSTOM, new Setting(
                prefixVServer1 + "_" + PropertyHandler.INSTANCENAME_CUSTOM,
                "tstdemo1"));
        parameters.put(prefixVServer1 + "_CONFIG", new Setting(prefixVServer1
                + "_CONFIG", PropertyHandler.VDISK_NAME + "=vdisk1"));

        parameters.put(prefixVServer2, new Setting(prefixVServer2, "true"));
        parameters.put(prefixVServer2 + "_CONFIG", new Setting(prefixVServer2
                + "_CONFIG", PropertyHandler.VDISK_NAME + "=vdisk2;"
                + PropertyHandler.INSTANCENAME_CUSTOM + "=tstdemo2"));

        propertyHandler = new PropertyHandler(settings);
        PropertyHandler[] list = propertyHandler.getVserverList();
        assertEquals(2, list.length);

        assertEquals("tstdemo", propertyHandler.getInstanceNameCustom());
        assertEquals("vdisk", propertyHandler.getVDiskNameCustom());

        assertEquals("tstdemo1", list[0].getInstanceNameCustom());
        assertEquals("vdisk1", list[0].getVDiskNameCustom());

        assertEquals("tstdemo2", list[1].getInstanceNameCustom());
        assertEquals("vdisk2", list[1].getVDiskNameCustom());

    }

    @Test()
    public void testSetVserverListFirstDisabled() throws Exception {
        String prefixVServer1 = "VSERVER_1";
        String prefixVServer2 = "VSERVER_2";

        parameters.put(PropertyHandler.VDISK_NAME, new Setting(
                PropertyHandler.VDISK_NAME, "vdisk"));
        parameters.put(prefixVServer1, new Setting(prefixVServer1, "false"));
        parameters.put(prefixVServer1 + "_" + PropertyHandler.VDISK_NAME,
                new Setting(prefixVServer1 + "_" + PropertyHandler.VDISK_NAME,
                        "vdisk1"));
        parameters.put(prefixVServer2, new Setting(prefixVServer2, "true"));
        parameters.put(prefixVServer2 + "_" + PropertyHandler.VDISK_NAME,
                new Setting(prefixVServer2 + "_" + PropertyHandler.VDISK_NAME,
                        "vdisk2"));

        propertyHandler = new PropertyHandler(settings);
        SubPropertyHandler[] list = propertyHandler.getVserverList();
        assertEquals(2, list.length);

        assertFalse(list[0].isEnabled());
        assertTrue(list[1].isEnabled());

        assertEquals("vdisk", propertyHandler.getVDiskNameCustom());
        assertEquals("vdisk1", list[0].getVDiskNameCustom());
        assertEquals("vdisk2", list[1].getVDiskNameCustom());
    }

    @Test()
    public void testSetInvalidNULLValue() throws Exception {
        parameters.put(PropertyHandler.VDISK_NAME, new Setting(
                PropertyHandler.VDISK_NAME, "abc"));
        propertyHandler = new PropertyHandler(settings);
        assertEquals("abc", propertyHandler.getVDiskNameCustom());

        propertyHandler.setVDiskName(null); // must result in empty value!
        assertEquals("", propertyHandler.getVDiskNameCustom());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidKey() throws Exception {
        PropertyReader reader = new PropertyReader(
                new HashMap<String, Setting>(), "prefix");
        reader.getValidatedProperty("nonExisiting");
    }

    @Test
    public void testGetControllerWaitTime() throws Exception {
        configSettings.put(PropertyHandler.CONTROLLER_WAIT_TIME, new Setting(
                PropertyHandler.CONTROLLER_WAIT_TIME, "60000"));
        propertyHandler = new PropertyHandler(settings);
        long waitTime = propertyHandler.getControllerWaitTime();
        assertEquals(60000, waitTime);
    }

    @Test
    public void testGetControllerWaitTime_invalid() throws Exception {
        configSettings.put(PropertyHandler.CONTROLLER_WAIT_TIME, new Setting(
                PropertyHandler.CONTROLLER_WAIT_TIME, "abs"));
        propertyHandler = new PropertyHandler(settings);
        long waitTime = propertyHandler.getControllerWaitTime();
        assertEquals(0, waitTime);
    }

    @Test
    public void testGetControllerWaitTime_notSet() throws Exception {
        long waitTime = propertyHandler.getControllerWaitTime();
        assertEquals(0, waitTime);
    }

    @Test
    public void isParallelProvisioning_True() throws ConfigurationException {
        // given
        configSettings.put(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                new Setting(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                        "true"));
        propertyHandler = new PropertyHandler(settings);

        // then
        assertTrue(propertyHandler.isParallelProvisioningEnabled());
    }

    @Test
    public void isParallelProvisioning_False() throws ConfigurationException {
        // given
        configSettings.put(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                new Setting(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                        "false"));
        propertyHandler = new PropertyHandler(settings);

        // then
        assertFalse(propertyHandler.isParallelProvisioningEnabled());
    }

    @Test
    public void isParallelProvisioning_WrongValue()
            throws ConfigurationException {
        // given
        configSettings.put(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                new Setting(PropertyHandler.ENABLE_PARALLEL_PROVISIONING,
                        "wrong"));
        propertyHandler = new PropertyHandler(settings);

        // then
        assertFalse(propertyHandler.isParallelProvisioningEnabled());
    }

    @Test
    public void isParallelProvisioning_NotSet() throws ConfigurationException {
        // given
        configSettings = new HashMap<>();
        propertyHandler = new PropertyHandler(settings);

        // then
        assertTrue(propertyHandler.isParallelProvisioningEnabled());
    }

}
