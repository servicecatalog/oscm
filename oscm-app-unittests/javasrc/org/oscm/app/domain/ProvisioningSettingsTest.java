/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Sep 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;

/**
 * @author Dirk Bernsau
 * 
 */
public class ProvisioningSettingsTest {

    @Test
    public void testSetters() {

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key", new Setting("key", "value"));
        HashMap<String, Setting> configSettings = new HashMap<>();
        configSettings.put("key2", new Setting("key2", "value2"));
        configSettings.put("key2a", new Setting("key2a", "value2a"));

        HashMap<String, Setting> attributes = new HashMap<>();
        parameters.put("key3", new Setting("key3", "value3"));
        HashMap<String, Setting> customAttributes = new HashMap<>();
        parameters.put("key4", new Setting("key4", "value4"));

        ProvisioningSettings pSettings = new ProvisioningSettings(parameters,
                configSettings, "en");

        pSettings = new ProvisioningSettings(parameters, attributes,
                customAttributes, configSettings, "en");

        assertSame(parameters, pSettings.getParameters());
        assertSame(attributes, pSettings.getAttributes());
        assertSame(customAttributes, pSettings.getCustomAttributes());
        assertSame(configSettings, pSettings.getConfigSettings());

        parameters = new HashMap<>();
        parameters.put("key5", new Setting("key5", "value5"));
        attributes = new HashMap<>();
        attributes.put("key6", new Setting("key6", "value6"));
        customAttributes = new HashMap<>();
        customAttributes.put("key7", new Setting("key7", "value7"));
        configSettings = new HashMap<>();
        configSettings.put("key8", new Setting("key8", "value8"));
        pSettings.setParameters(parameters);
        pSettings.setAttributes(attributes);
        pSettings.setCustomAttributes(customAttributes);
        pSettings.setConfigSettings(configSettings);

        assertSame(parameters, pSettings.getParameters());
        assertSame(configSettings, pSettings.getConfigSettings());

        pSettings = new ProvisioningSettings(parameters, attributes,
                customAttributes, configSettings, "de");
        pSettings.setOrganizationId("orgId");
        pSettings.setOrganizationName("orgName");
        pSettings.setSubscriptionId("subId");
        pSettings.setReferenceId("refId");
        pSettings.setBesLoginUrl("besUrl");

        assertEquals("de", pSettings.getLocale());
        assertEquals("orgId", pSettings.getOrganizationId());
        assertEquals("orgName", pSettings.getOrganizationName());
        assertEquals("subId", pSettings.getSubscriptionId());
        assertEquals("refId", pSettings.getReferenceId());
        assertEquals("besUrl", pSettings.getBesLoginURL());
    }

    private void assertSame(Map<String, Setting> one, Map<String, Setting> two) {
        assertNotNull(one);
        assertNotNull(two);
        assertEquals(one.size(), two.size());
        Set<String> keySet = one.keySet();
        keySet.removeAll(two.keySet());
        assertTrue(keySet.isEmpty());
        for (String key : two.keySet()) {
            assertEquals(one.get(key).getValue(), two.get(key).getValue());
        }
    }

    @Test
    public void testOverwrite() {

        String controllerId = "controllerId";

        HashMap<String, Setting> configSettings = new HashMap<>();
        configSettings.put("key1", new Setting("key1", "value1s"));
        configSettings.put("key2", new Setting("key2", "value2s"));
        configSettings.put("key3", new Setting("key3", "value3s"));
        configSettings.put("key4", new Setting("key4", "value4s", true));

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key2", new Setting("key2", "value2p"));
        parameters.put("key3", new Setting("key3", "value3p"));
        parameters.put("key4", new Setting("key4", "value4p"));

        HashMap<String, Setting> customAttributes = new HashMap<>();
        customAttributes.put("key3", new Setting("key3", "value3c", false,
                controllerId));
        customAttributes.put("key4", new Setting("key4", "value4c", true,
                controllerId));

        HashMap<String, Setting> attributes = new HashMap<>();
        attributes.put("key4", new Setting("key4", "value4a", false,
                controllerId));

        ProvisioningSettings pSettings = new ProvisioningSettings(parameters,
                attributes, customAttributes, configSettings, "en");

        pSettings.overwriteProperties(controllerId);

        assertEquals("value1s", configSettings.get("key1").getValue());
        assertEquals("value2p", configSettings.get("key2").getValue());
        assertEquals("value3c", configSettings.get("key3").getValue());
        assertEquals("value4a", configSettings.get("key4").getValue());

        assertEquals("value3c", parameters.get("key3").getValue());
        assertEquals("value4a", parameters.get("key4").getValue());

        assertFalse(configSettings.get("key4").isEncrypted());
        assertFalse(parameters.get("key4").isEncrypted());
        assertTrue(customAttributes.get("key4").isEncrypted());
        assertFalse(attributes.get("key4").isEncrypted());
    }

    @Test
    public void testOverwriteNullValues() {

        String controllerId = "controllerId";

        HashMap<String, Setting> configSettings = new HashMap<>();
        configSettings.put("key1", new Setting("key1", "value1s"));
        configSettings.put("key2", new Setting("key2", "value2s"));
        configSettings.put("key3", new Setting("key3", "value3s"));
        configSettings.put("key4", new Setting("key4", "value4s", true));

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key2", new Setting("key2", "value2p"));
        parameters.put("key3", new Setting("key3", "value3p"));
        parameters.put("key4", new Setting("key4", "value4p"));

        HashMap<String, Setting> customAttributes = new HashMap<>();
        customAttributes.put("key3", new Setting("key3", "value3c", false,
                controllerId));
        customAttributes.put("key4", new Setting("key4", "value4c", true,
                controllerId));

        HashMap<String, Setting> attributes = new HashMap<>();
        attributes.put("key4", new Setting("key4", null, false, controllerId));

        ProvisioningSettings pSettings = new ProvisioningSettings(parameters,
                attributes, customAttributes, configSettings, "en");

        pSettings.overwriteProperties(controllerId);

        assertEquals("value1s", configSettings.get("key1").getValue());
        assertEquals("value2p", configSettings.get("key2").getValue());
        assertEquals("value3c", configSettings.get("key3").getValue());
        assertEquals("value4c", configSettings.get("key4").getValue());

        assertEquals("value3c", parameters.get("key3").getValue());
        assertEquals("value4c", parameters.get("key4").getValue());

        assertTrue(configSettings.get("key4").isEncrypted());
        assertTrue(parameters.get("key4").isEncrypted());
        assertTrue(customAttributes.get("key4").isEncrypted());
        assertFalse(attributes.get("key4").isEncrypted());
    }

    @Test
    public void testOverwriteEmptyValues() {

        String controllerId = "controllerId";

        HashMap<String, Setting> configSettings = new HashMap<>();
        configSettings.put("key1", new Setting("key1", "value1s"));
        configSettings.put("key2", new Setting("key2", "value2s"));
        configSettings.put("key3", new Setting("key3", "value3s"));
        configSettings.put("key4", new Setting("key4", "value4s", true));

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key2", new Setting("key2", "value2p"));
        parameters.put("key3", new Setting("key3", "value3p"));
        parameters.put("key4", new Setting("key4", "value4p"));

        HashMap<String, Setting> customAttributes = new HashMap<>();
        customAttributes.put("key3", new Setting("key3", "value3c", false,
                controllerId));
        customAttributes.put("key4", new Setting("key4", "value4c", true,
                controllerId));

        HashMap<String, Setting> attributes = new HashMap<>();
        attributes.put("key4", new Setting("key4", " ", false, controllerId));

        ProvisioningSettings pSettings = new ProvisioningSettings(parameters,
                attributes, customAttributes, configSettings, "en");

        pSettings.overwriteProperties(controllerId);

        assertEquals("value1s", configSettings.get("key1").getValue());
        assertEquals("value2p", configSettings.get("key2").getValue());
        assertEquals("value3c", configSettings.get("key3").getValue());
        assertEquals("value4c", configSettings.get("key4").getValue());

        assertEquals("value3c", parameters.get("key3").getValue());
        assertEquals("value4c", parameters.get("key4").getValue());

        assertTrue(configSettings.get("key4").isEncrypted());
        assertTrue(parameters.get("key4").isEncrypted());
        assertTrue(customAttributes.get("key4").isEncrypted());
        assertFalse(attributes.get("key4").isEncrypted());
    }
    
    @Ignore
    @Test
    public void testOverwriteCrossReference() {
        String controllerId = "controllerId";

        HashMap<String, Setting> configSettings = new HashMap<>();
        configSettings.put("key3", new Setting("key3", "value3s"));
        configSettings.put("key4", new Setting("key4", "value4s"));

        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put("key3", new Setting("key3", "value3p"));
        parameters.put("key4", new Setting("key4", "value4p"));

        HashMap<String, Setting> customAttributes = new HashMap<>();
        customAttributes.put("key3", new Setting("key3", "value3c", false,
                controllerId));
        customAttributes.put("key4", new Setting("key4", "value4c", false,
                controllerId));

        HashMap<String, Setting> attributes = new HashMap<>();
        attributes.put("key4", new Setting("key4", "${key3}", false, controllerId));

        ProvisioningSettings pSettings = new ProvisioningSettings(parameters,
                attributes, customAttributes, configSettings, "en");

        pSettings.overwriteProperties(controllerId);

        assertEquals("value3c", configSettings.get("key3").getValue());
        assertEquals("value3c", configSettings.get("key4").getValue());

        assertEquals("value3c", parameters.get("key3").getValue());
        assertEquals("value3c", parameters.get("key4").getValue());
        
        assertEquals("value4c", customAttributes.get("key4").getValue());
        

        assertFalse(configSettings.get("key4").isEncrypted());
        assertFalse(parameters.get("key4").isEncrypted());
        assertFalse(customAttributes.get("key4").isEncrypted());
        assertFalse(attributes.get("key4").isEncrypted());
    }
}
