/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.oscm.internal.types.enumtypes.UserAccountStatus;

/**
 * Changes the status of the specified user to ACTIVE.
 * 
 * @author hoffmann
 */
public class UnlockUserCommand extends AbstractUserStatusCommand {

    public UnlockUserCommand() {
        super(UserAccountStatus.ACTIVE);
    }

    public String getName() {
        return "unlockuser";
    }

    public String getDescription() {
        return "Changes the status of the specified user to ACTIVE.";
    }

}
