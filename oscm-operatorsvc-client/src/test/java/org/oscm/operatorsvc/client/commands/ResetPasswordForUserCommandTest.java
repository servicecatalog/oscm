/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 26.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.operatorsvc.client.IOperatorCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResetPasswordForUserCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new ResetPasswordForUserCommand();
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("resetpasswordforuser", command.getName());
    }

    @Test
    public void testGetArgumentNames() throws Exception {
        assertEquals(1, command.getArgumentNames().size());
        assertEquals("userid", command.getArgumentNames().get(0));
    }

    @Test
    public void testRun() throws Exception {
        args.put("userid", "userId");
        assertTrue(command.run(ctx));
        assertEquals("resetPasswordForUser", stubMethodName);
        assertEquals(1, stubCallArgs.length);
        assertEquals("userId", stubCallArgs[0]);
        assertOut("Successfully reset the password for user 'userId'.%n");
    }
}
