/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                             
 *                                                                              
 *  Creation Date: 26.08.2010                                                      
 *                                                                              
 *  Completion Time: 26.08.2010                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Command to save or update a configuration settings.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SaveConfigurationSettingCommand implements IOperatorCommand {

    private static final String ARG_CONFIG_SETTING_KEY = "settingKey";
    private static final String ARG_CONFIG_SETTING_VALUE = "settingValue";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_CONFIG_SETTING_KEY, ARG_CONFIG_SETTING_VALUE);
    }

    public String getDescription() {
        return "Saves the specified configuration setting.";
    }

    public String getName() {
        return "saveconfigurationsetting";
    }

    public boolean run(CommandContext ctx) throws Exception {
        final Set<ConfigurationKey> configurationKeys = new HashSet<ConfigurationKey>(
                Arrays.asList(ConfigurationKey.values()));
        final OperatorService os = ctx.getService();

        final List<VOConfigurationSetting> list = os.getConfigurationSettings();
        final VOConfigurationSetting setting = updateOrCreateSetting(list,
                ctx.getEnum(ARG_CONFIG_SETTING_KEY, configurationKeys),
                ctx.getString(ARG_CONFIG_SETTING_VALUE));

        os.saveConfigurationSetting(setting);
        String outputText = String
                .format("Configuration setting '%s' with value '%s' was successfully stored.",
                        setting.getInformationId(), setting.getValue());
        ctx.out().println(outputText);
        return true;
    }

    /**
     * Updates an existing (if contained in the passed list) or creates a new
     * {@link VOConfigurationSetting} for the passed key and value.
     * 
     * @param list
     *            the list of existing {@link VOConfigurationSetting}s
     * @param key
     *            the {@link ConfigurationKey} to update
     * @param value
     *            the new value
     * @return an existing {@link VOConfigurationSetting} with its key and
     *         version and the updated value or if no matching setting was
     *         found, a new {@link VOConfigurationSetting} with key and value
     *         set to 0.
     */
    private VOConfigurationSetting updateOrCreateSetting(
            List<VOConfigurationSetting> list, ConfigurationKey key,
            String value) {
        for (VOConfigurationSetting cs : list) {
            if (cs.getInformationId() == key) {
                cs.setValue(value);
                return cs;
            }
        }
        return new VOConfigurationSetting(key, Configuration.GLOBAL_CONTEXT,
                value);
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
