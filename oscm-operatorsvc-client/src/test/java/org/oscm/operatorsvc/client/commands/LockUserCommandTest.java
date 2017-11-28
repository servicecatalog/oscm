/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOUser;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LockUserCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new LockUserCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("lockuser", command.getName());
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
        assertEquals(UserAccountStatus.LOCKED, stubCallArgs[1]);
        assertOut("Status of user 'jaeger' has been successfully changed to LOCKED.%n");
        assertErr("");
    }

}
