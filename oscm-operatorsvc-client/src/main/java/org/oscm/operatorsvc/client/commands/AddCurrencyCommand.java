/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Ronny Weiser                                                      
 *                                                                              
 *  Creation Date: 28.05.2010                                                      
 *                                                                              
 *  Completion Time: <date>                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * Command to invoke the operator service functionality to add a new currency to
 * the system.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class AddCurrencyCommand implements IOperatorCommand {

    private static final String ARG_CURRENCYISOCODE = "currencyISOCode";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_CURRENCYISOCODE);
    }

    public String getDescription() {
        return "Adds a new currency to the system.";
    }

    public String getName() {
        return "addcurrency";
    }

    public boolean run(CommandContext ctx) throws Exception {
        final String currency = ctx.getString(ARG_CURRENCYISOCODE);
        ctx.getService().addCurrency(currency);
        ctx.out().printf("Currency '%s' is now available.", currency);
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
