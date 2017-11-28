/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Collections;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.internal.vo.VOTimerInfo;

public class ReinitTimersCommand extends AbstractTimerCommand {

    public String getName() {
        return "reinittimers";
    }

    public String getDescription() {
        return "Re-initializes the timers registered at the active cluster "
                + "node and returns the detail information on them including "
                + "when they will expire next.";
    }

    public List<String> getArgumentNames() {
        final List<String> none = Collections.emptyList();
        return none;
    }

    public boolean run(CommandContext ctx) throws Exception {
        final List<VOTimerInfo> info = ctx.getService().reInitTimers();
        printTimerInormation(info, ctx.out());
        return true;
    }

}
