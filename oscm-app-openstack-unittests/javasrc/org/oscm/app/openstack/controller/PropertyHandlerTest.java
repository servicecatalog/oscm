/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Unit test.
 *       
 *  Creation Date: 2013-11-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.openstack.i18n.Messages;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;

public class PropertyHandlerTest {

    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private PropertyHandler propertyHandler;

    @Before
    public void setUp() {
        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings, "en");
        propertyHandler = new PropertyHandler(settings);
    }

    @Test()
    public void testGetInstanceName() {
        parameters.put(PropertyHandler.STACK_NAME, new Setting(
                PropertyHandler.STACK_NAME, "db2"));
        propertyHandler = new PropertyHandler(settings);
        String instanceName = propertyHandler.getStackName();
        assertEquals("db2", instanceName);
    }

    @Test
    public void testGetInstanceNamePattern() {
        parameters.put(PropertyHandler.STACK_NAME_PATTERN, new Setting(
                PropertyHandler.STACK_NAME_PATTERN, "regex"));
        propertyHandler = new PropertyHandler(settings);
        String instanceNamePattern = propertyHandler.getStackNamePattern();
        assertEquals("regex", instanceNamePattern);
    }

    @Test()
    public void testGetTemplateURL() throws HeatException {

        // case 1
        parameters.put(PropertyHandler.TEMPLATE_NAME, new Setting(
                PropertyHandler.TEMPLATE_NAME, "template1.json"));
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL, "http://test.com"));
        propertyHandler = new PropertyHandler(settings);
        String url = propertyHandler.getTemplateUrl();
        assertEquals("http://test.com/template1.json", url);

        // case 2
        parameters.put(PropertyHandler.TEMPLATE_NAME, new Setting(
                PropertyHandler.TEMPLATE_NAME,
                "https://other.com/template1.json"));
        url = propertyHandler.getTemplateUrl();
        assertEquals("https://other.com/template1.json", url);

        // case 3
        configSettings
                .put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                        PropertyHandler.TEMPLATE_BASE_URL,
                        "http://test.com/templates"));
        parameters.put(PropertyHandler.TEMPLATE_NAME, new Setting(
                PropertyHandler.TEMPLATE_NAME, "templates/apache.json"));
        url = propertyHandler.getTemplateUrl();
        assertEquals("http://test.com/templates/apache.json", url);

        // case 4
        parameters.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL, ""));
        url = propertyHandler.getTemplateUrl();
        assertEquals("http://test.com/templates/apache.json", url);
    }

    @Test()
    public void testGetTemplateURL_onlyParam() throws HeatException {

        // case 1
        parameters.put(PropertyHandler.TEMPLATE_NAME, new Setting(
                PropertyHandler.TEMPLATE_NAME, "template1.json"));
        parameters.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL, "http://test.com"));
        propertyHandler = new PropertyHandler(settings);
        String url = propertyHandler.getTemplateUrl();
        assertEquals("http://test.com/template1.json", url);
    }

    @Test()
    public void testGetInstanceDomain() {
        parameters.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "12345"));
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "23455"));
        propertyHandler = new PropertyHandler(settings);
        String domainName = propertyHandler.getDomainName();
        assertEquals("12345", domainName);
    }

    @Test()
    public void testGetControllerDomain() {
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "23455"));
        propertyHandler = new PropertyHandler(settings);
        String domainName = propertyHandler.getDomainName();
        assertEquals("23455", domainName);
        parameters.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, ""));
        domainName = propertyHandler.getDomainName();
        assertEquals("23455", domainName);
    }

    @Test()
    public void testGetDefaultDomain() {
        // controller setting and property setting are empty
        propertyHandler = new PropertyHandler(settings);
        String domainNameWithNull = propertyHandler.getDomainName();
        assertEquals("default", domainNameWithNull);

        // controller setting is ""
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, ""));
        propertyHandler = new PropertyHandler(settings);
        String domainNameWithEmptyStr = propertyHandler.getDomainName();
        assertEquals("default", domainNameWithEmptyStr);
    }

    @Test()
    public void testGetKeystoneURL() {
        parameters.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL, "http://keystone/v3/auth"));
        configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL,
                "http://otherkeystone/v3/auth"));
        propertyHandler = new PropertyHandler(settings);
        String keystoneURL = propertyHandler.getKeystoneUrl();
        assertEquals("http://keystone/v3/auth", keystoneURL);
    }

    @Test()
    public void testGetControllerKeystoneURL() {
        configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL,
                "http://otherkeystone/v3/auth"));
        propertyHandler = new PropertyHandler(settings);
        String keystoneURL = propertyHandler.getKeystoneUrl();
        assertEquals("http://otherkeystone/v3/auth", keystoneURL);
        parameters.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL, ""));
        keystoneURL = propertyHandler.getKeystoneUrl();
        assertEquals("http://otherkeystone/v3/auth", keystoneURL);
    }

    @Test()
    public void testGetInstanceTenant() {
        parameters.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "12345"));
        configSettings.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "23455"));
        propertyHandler = new PropertyHandler(settings);
        String tenantId = propertyHandler.getTenantId();
        assertEquals("12345", tenantId);
    }

    @Test()
    public void testGetControllerTenant() {
        configSettings.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "23455"));
        propertyHandler = new PropertyHandler(settings);
        String tenantId = propertyHandler.getTenantId();
        assertEquals("23455", tenantId);
        parameters.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, ""));
        tenantId = propertyHandler.getTenantId();
        assertEquals("23455", tenantId);
    }

    @Test()
    public void testGetSettings() {
        propertyHandler = new PropertyHandler(settings);
        ProvisioningSettings testSettings = propertyHandler.getSettings();
        assertNotNull(testSettings);
        assertEquals(settings, testSettings);
    }

    @Test()
    public void testGetState_NullValue() {
        FlowState status = propertyHandler.getState();
        assertEquals(FlowState.FAILED, status);
    }

    @Test()
    public void testGetState() {
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, "CREATING_STACK"));
        propertyHandler = new PropertyHandler(settings);
        FlowState status = propertyHandler.getState();
        assertEquals(FlowState.CREATING_STACK, status);
    }

    @Test()
    public void testSetState() {
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, "CREATION_EXECUTING_WORKLOAD"));
        propertyHandler = new PropertyHandler(settings);
        propertyHandler.setState(FlowState.FINISHED);
        FlowState status = propertyHandler.getState();
        assertEquals(FlowState.FINISHED, status);
    }

    @Test
    public void testMissingResource() {
        String message = Messages.get("de", "key");
        assertEquals("!key!", message);
    }

    @Test
    public void testLocalizedResources() {
        String messageEn = Messages.get("en",
                "status_" + FlowState.UPDATING.name());
        String messageFr = Messages.get("fr",
                "status_" + FlowState.UPDATING.name());
        assertTrue(messageEn.equals(messageFr));
    }

    @Test
    public void testArgsResources() {
        String messageEn = Messages.get("en",
                "status_" + FlowState.UPDATING.name());
        String messageEnParam = Messages.get("en", "status_"
                + FlowState.UPDATING.name(), "param 1");
        assertTrue(messageEn.equals(messageEnParam));
    }

    @Test
    public void testGetAllResources() {
        List<LocalizedText> all = Messages.getAll("status_"
                + FlowState.UPDATING.name(), "testArg");
        String messageEn = Messages.get("en",
                "status_" + FlowState.UPDATING.name());
        String found = null;
        for (LocalizedText test : all) {
            if ("en".equals(test.getLocale())) {
                found = test.getText();
            }
        }
        assertEquals(messageEn, found);
    }

    @Test
    public void getMailForCompletion_null() {
        String mail = propertyHandler.getMailForCompletion();
        assertNull(mail);
    }

    @Test
    public void getMailForCompletion_empty() {
        String mailAddress = "";
        propertyHandler
                .getSettings()
                .getParameters()
                .put(PropertyHandler.MAIL_FOR_COMPLETION,
                        new Setting(PropertyHandler.MAIL_FOR_COMPLETION,
                                mailAddress));
        String mail = propertyHandler.getMailForCompletion();
        assertNull(mail);
    }

    @Test
    public void getMailForCompletion_notNull() {
        String mailAddress = "test@test.com";
        propertyHandler
                .getSettings()
                .getParameters()
                .put(PropertyHandler.MAIL_FOR_COMPLETION,
                        new Setting(PropertyHandler.MAIL_FOR_COMPLETION,
                                mailAddress));
        String mail = propertyHandler.getMailForCompletion();
        assertEquals(mailAddress, mail);
    }

    @Test
    public void getReadyTimeout() {
        // given
        configSettings.put(PropertyHandler.READY_TIMEOUT, new Setting(
                PropertyHandler.READY_TIMEOUT, "300000"));
        propertyHandler = new PropertyHandler(settings);

        // when
        long time = propertyHandler.getReadyTimeout();

        // then
        assertEquals(300000, time);
    }

    @Test
    public void getReadyTimeout_null() {
        // given
        propertyHandler = new PropertyHandler(settings);

        // when
        long time = propertyHandler.getReadyTimeout();

        // then
        assertEquals(0, time);
    }

    @Test
    public void getReadyTimeout_empty() {
        // given
        configSettings.put(PropertyHandler.READY_TIMEOUT, new Setting(
                PropertyHandler.READY_TIMEOUT, ""));
        propertyHandler = new PropertyHandler(settings);

        // when
        long time = propertyHandler.getReadyTimeout();

        // then
        assertEquals(0, time);
    }

    @Test
    public void getReadyTimeout_wringValue() {
        // given
        configSettings.put(PropertyHandler.READY_TIMEOUT, new Setting(
                PropertyHandler.READY_TIMEOUT, "foo"));
        propertyHandler = new PropertyHandler(settings);

        // when
        long time = propertyHandler.getReadyTimeout();

        // then
        assertEquals(0, time);
    }

    @Test
    public void getStartTime() {
        // given
        long time = System.currentTimeMillis();
        parameters.put(PropertyHandler.START_TIME, new Setting(
                PropertyHandler.START_TIME, String.valueOf(time)));
        propertyHandler = new PropertyHandler(settings);

        // when
        String timeStr = propertyHandler.getStartTime();

        // then
        assertEquals(timeStr, String.valueOf(time));

    }

    @Test
    public void getStartTime_useSetter() {
        // given
        long time = System.currentTimeMillis();
        propertyHandler = new PropertyHandler(settings);
        propertyHandler.setStartTime(String.valueOf(time));

        // when
        String timeStr = propertyHandler.getStartTime();

        // then
        assertEquals(timeStr, String.valueOf(time));

    }

    @Test
    public void getStartTime_null() {
        // given
        propertyHandler = new PropertyHandler(settings);

        // when
        String timeStr = propertyHandler.getStartTime();

        // then
        assertEquals(timeStr, null);

    }

    @Test
    public void TestMultipleSecurityGroup() {
        parameters.put("TP_ARRAY_" + "SecurityGroupName", new Setting(
                "TP_ARRAY_" + "SecurityGroupName", "testOne,testTwo"));
        propertyHandler = new PropertyHandler(settings);
        final String SECURITY_GROUPS = "SecurityGroupName";
        JSONObject parameters = propertyHandler.getTemplateParameters();
        assertNotNull(parameters);
        try {
            assertTrue(parameters.has(SECURITY_GROUPS));
            assertTrue(parameters.getJSONArray(SECURITY_GROUPS).getString(0)
                    .equals("testOne"));
            assertTrue(parameters.getJSONArray(SECURITY_GROUPS).getString(1)
                    .equals("testTwo"));
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    @Test
    public void TestSingleSecurityGroup() {
        final String SECURITY_GROUPS = "SG";
        parameters.put("TP_ARRAY_" + SECURITY_GROUPS, new Setting("TP_ARRAY_"
                + SECURITY_GROUPS, "testOne"));
        propertyHandler = new PropertyHandler(settings);
        JSONObject parameters = propertyHandler.getTemplateParameters();
        assertNotNull(parameters);
        try {
            assertTrue(parameters.has(SECURITY_GROUPS));
            assertTrue(parameters.getJSONArray(SECURITY_GROUPS).getString(0)
                    .equals("testOne"));

        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    @Test
    public void TestEmptySecurityGroup() {
        propertyHandler = new PropertyHandler(settings);
        final String SECURITY_GROUPS = "SG";
        JSONObject parameters = propertyHandler.getTemplateParameters();
        assertNotNull(parameters);
        assertTrue(!parameters.has(SECURITY_GROUPS));

    }
}
