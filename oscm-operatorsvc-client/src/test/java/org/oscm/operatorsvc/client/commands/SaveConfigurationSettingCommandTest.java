/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.types.constants.Configuration;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests for the saveconfigurationsetting command.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SaveConfigurationSettingCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new SaveConfigurationSettingCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("saveconfigurationsetting", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("settingKey", "settingValue"),
                command.getArgumentNames());
    }

    @Test
    public void testSuccess_New() throws Exception {
        stubCallReturn = Arrays.asList(new VOConfigurationSetting(
                ConfigurationKey.BASE_URL, "", "value"));
        final String value = "value";
        final ConfigurationKey key = ConfigurationKey.HTTP_PROXY;

        args.put("settingKey", key.name());
        args.put("settingValue", value);

        assertTrue(command.run(ctx));

        assertEquals("saveConfigurationSetting", stubMethodName);
        assertTrue(stubCallArgs[0] instanceof VOConfigurationSetting);
        VOConfigurationSetting cs = (VOConfigurationSetting) stubCallArgs[0];
        assertEquals(value, cs.getValue());
        assertEquals(key, cs.getInformationId());
        assertEquals(0, cs.getKey());
        assertEquals(0, cs.getVersion());
        assertOut("Configuration setting 'HTTP_PROXY' with value 'value' was successfully stored.%n");
        assertErr("");
    }

    /**
     * Bug 9211 - version and key of existing setting must be used.
     */
    @Test
    public void testSuccess_UpdateExisting() throws Exception {
        final String value = "value";
        final ConfigurationKey key = ConfigurationKey.HTTP_PROXY;
        VOConfigurationSetting cs = new VOConfigurationSetting(key, "context",
                "oldvalue");
        cs.setKey(1234);
        cs.setVersion(5);
        stubCallReturn = Arrays.asList(cs);

        args.put("settingKey", key.name());
        args.put("settingValue", value);

        assertTrue(command.run(ctx));

        assertEquals("saveConfigurationSetting", stubMethodName);
        assertSame(cs, stubCallArgs[0]);
        assertEquals(key, cs.getInformationId());
        assertEquals(1234, cs.getKey());
        assertEquals(5, cs.getVersion());
        assertEquals(value, cs.getValue());
        assertOut("Configuration setting 'HTTP_PROXY' with value 'value' was successfully stored.%n");
        assertErr("");
    }

    @Test
    public void testFail() throws Exception {
        args.put("settingKey", "noKEY");
        args.put("settingValue", "value");
        try {
            command.run(ctx);
            fail("Execution must fail");
        } catch (IllegalArgumentException e) {
            assertOut("");
            assertErr("");
            final String msg = e.getMessage();
            assertTrue(
                    "Unexpected message: " + msg,
                    msg.startsWith("Invalid parameter value 'noKEY' for settingKey."));
        }
    }

    @Test
    public void updateOrCreateSetting_bug10273_10274() throws Exception {
        final String value = "operator.manageLdapSettings";
        final ConfigurationKey key = ConfigurationKey.HIDDEN_UI_ELEMENTS;
        stubCallReturn = Arrays.asList(new VOConfigurationSetting());

        args.put("settingKey", key.name());
        args.put("settingValue", value);

        assertTrue(command.run(ctx));

        assertEquals("saveConfigurationSetting", stubMethodName);
        assertTrue(stubCallArgs[0] instanceof VOConfigurationSetting);
        VOConfigurationSetting cs = (VOConfigurationSetting) stubCallArgs[0];
        assertEquals(value, cs.getValue());
        assertEquals(key, cs.getInformationId());
        assertEquals(Configuration.GLOBAL_CONTEXT, cs.getContextId());
        assertEquals(0, cs.getKey());
        assertEquals(0, cs.getVersion());
    }
}
