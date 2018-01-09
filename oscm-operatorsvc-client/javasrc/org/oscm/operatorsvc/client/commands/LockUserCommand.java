/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.oscm.internal.types.enumtypes.UserAccountStatus;

public class LockUserCommand extends AbstractUserStatusCommand {

    public LockUserCommand() {
        super(UserAccountStatus.LOCKED);
    }

    public String getName() {
        return "lockuser";
    }

    public String getDescription() {
        return "Changes the status of the specified user to LOCKED.";
    }

}
