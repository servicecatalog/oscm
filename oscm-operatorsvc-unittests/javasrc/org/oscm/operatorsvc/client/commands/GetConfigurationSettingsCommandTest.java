/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 12.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Tests for the getconfigurationsettings comman.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class GetConfigurationSettingsCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new GetConfigurationSettingsCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("getconfigurationsettings", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList(), command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        List<VOConfigurationSetting> inputList = new ArrayList<VOConfigurationSetting>();
        inputList.add(new VOConfigurationSetting(ConfigurationKey.BASE_URL,
                "global2", "value"));
        stubCallReturn = inputList;
        assertTrue(command.run(ctx));
        assertEquals("getConfigurationSettings", stubMethodName);
        assertOut("Currently stored configuration settings are:%nBASE_URL, 'value'%n");
        assertErr("");
    }
}
