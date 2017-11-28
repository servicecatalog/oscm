/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Collections;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

public class StartBillingRunCommand implements IOperatorCommand {

    public String getName() {
        return "startbillingrun";
    }

    public String getDescription() {
        return "Explicitly starts the billing process for the last billing period.";
    }

    public List<String> getArgumentNames() {
        final List<String> none = Collections.emptyList();
        return none;
    }

    public boolean run(CommandContext ctx) throws Exception {
        boolean success = ctx.getService().startBillingRun();
        if (success) {
            final String msg = "The manually triggered timer run was completed successfully.";
            ctx.out().println(msg);
        } else {
            final String msg = "A problem occurred during the billing run execution. Please refer to the log files.";
            ctx.err().println(msg);
        }
        return success;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
