/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOUser;

public class UnlockUserCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new UnlockUserCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("unlockuser", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList("userid"), command.getArgumentNames());
    }

    @Test
    public void testLockUser() throws Exception {
        args.put("userid", "jaeger");
        assertTrue(command.run(ctx));
        assertEquals("setUserAccountStatus", stubMethodName);
        VOUser user = (VOUser) stubCallArgs[0];
        assertEquals("jaeger", user.getUserId());
        assertEquals(UserAccountStatus.ACTIVE, stubCallArgs[1]);
        assertOut("Status of user 'jaeger' has been successfully changed to ACTIVE.%n");
        assertErr("");
    }

}
