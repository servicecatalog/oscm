/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Collections;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

public class StartPaymentProcessingCommand implements IOperatorCommand {

    public String getName() {
        return "startpaymentprocessing";
    }

    public String getDescription() {
        return "Determines unhandled billing results and starts the payment processing for them.";
    }

    public List<String> getArgumentNames() {
        return Collections.emptyList();
    }

    public boolean run(CommandContext ctx) throws Exception {
        boolean result = ctx.getService().startPaymentProcessing();
        if (result) {
            ctx.out().print("Payment processing completed successfully.");
        } else {
            ctx.err().print("Payment processing failed.");
        }
        return result;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }
}
