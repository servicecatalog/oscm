/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 26.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;

/**
 * Command implementation for the reset of a user password.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ResetPasswordForUserCommand implements IOperatorCommand {

    private static final String ARG_USERID = "userid";

    public List<String> getArgumentNames() {
        return Arrays.asList(ARG_USERID);
    }

    public String getDescription() {
        return "Resets the password of a specified user and sends an email with the new password to him. Furthermore the account will be unlocked, if it was not especially locked by an operator.";
    }

    public String getName() {
        return "resetpasswordforuser";
    }

    public boolean run(CommandContext ctx) throws Exception {
        final String userid = ctx.getString(ARG_USERID);
        ctx.getService().resetPasswordForUser(userid);

        ctx.out().printf("Successfully reset the password for user '%s'.%n",
                userid);
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
