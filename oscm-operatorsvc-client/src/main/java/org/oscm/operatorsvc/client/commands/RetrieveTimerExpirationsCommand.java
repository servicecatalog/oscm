/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Collections;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.internal.vo.VOTimerInfo;

public class RetrieveTimerExpirationsCommand extends AbstractTimerCommand {

    public String getName() {
        return "retrievetimerexpirations";
    }

    public String getDescription() {
        return "Retrieves the settings of the currently registered timers.";
    }

    public List<String> getArgumentNames() {
        final List<String> none = Collections.emptyList();
        return none;
    }

    public boolean run(CommandContext ctx) throws Exception {
        final List<VOTimerInfo> info = ctx.getService()
                .getTimerExpirationInformation();
        printTimerInormation(info, ctx.out());
        return true;
    }
}
