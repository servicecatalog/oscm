/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.vo.VOTimerInfo;

public abstract class AbstractTimerCommand implements IOperatorCommand {

    protected void printTimerInormation(final List<VOTimerInfo> info,
            final PrintWriter out) {
        if (info.isEmpty()) {
            out.println("No timers are registered.");
        } else {
            out.println("Currently registered timers:");
            String message = "Timer '%s' expires at: %s";
            SimpleDateFormat sdf = new SimpleDateFormat();
            for (VOTimerInfo t : info) {
                out.println(String.format(message, t.getTimerType(), sdf
                        .format(t.getExpirationDate())));
            }
        }
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
