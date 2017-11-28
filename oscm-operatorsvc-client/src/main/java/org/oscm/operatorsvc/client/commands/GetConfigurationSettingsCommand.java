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
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Command to retrieve the currently existing configuration settings.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class GetConfigurationSettingsCommand implements IOperatorCommand {

    public List<String> getArgumentNames() {
        return Arrays.asList();
    }

    public String getDescription() {
        return "Returns the currently existing configuration settings";
    }

    public String getName() {
        return "getconfigurationsettings";
    }

    public boolean run(CommandContext ctx) throws Exception {
        List<VOConfigurationSetting> result = ctx.getService()
                .getConfigurationSettings();
        ctx.out().println("Currently stored configuration settings are:");
        for (VOConfigurationSetting setting : result) {
            String lineContent = String.format("%s, '%s'", setting
                    .getInformationId().name(), setting.getValue());
            ctx.out().println(lineContent);
        }
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
