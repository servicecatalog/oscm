/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Collections;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

public class RetryFailedPaymentProcessesCommand implements IOperatorCommand {

    public String getName() {
        return "retryfailedpaymentprocesses";
    }

    public String getDescription() {
        return "Retries the payment processing for all payment attempts that "
                + "failed before and are marked to be retried.";
    }

    public List<String> getArgumentNames() {
        final List<String> none = Collections.emptyList();
        return none;
    }

    public boolean run(CommandContext ctx) throws Exception {
        final boolean result = ctx.getService().retryFailedPaymentProcesses();
        if (result) {
            final String msg = "All payment processes to be retried were handled successfully.";
            ctx.out().println(msg);
        } else {
            final String msg = "Retrying the payment processes failed for at least one entry.";
            ctx.err().println(msg);
        }
        return result;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
