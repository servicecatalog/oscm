/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import java.util.Arrays;
import java.util.List;

import org.oscm.operatorsvc.client.CommandContext;
import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOUser;

public abstract class AbstractUserStatusCommand implements IOperatorCommand {

    private static final String ARG_USERID = "userid";

    private final UserAccountStatus status;

    protected AbstractUserStatusCommand(UserAccountStatus status) {
        this.status = status;
    }

    public final List<String> getArgumentNames() {
        return Arrays.asList(ARG_USERID);
    }

    public boolean run(final CommandContext ctx) throws Exception {

        final String userid = ctx.getString(ARG_USERID);

        final VOUser user = new VOUser();
        user.setUserId(userid);
        ctx.getService().setUserAccountStatus(user, status);

        ctx.out().printf(
                "Status of user '%s' has been successfully changed to %s.%n",
                userid, status);
        return true;
    }

    public boolean replaceGreateAndLessThan() {
        return false;
    }

}
