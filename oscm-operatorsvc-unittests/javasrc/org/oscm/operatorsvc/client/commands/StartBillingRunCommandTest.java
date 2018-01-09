/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import org.oscm.operatorsvc.client.IOperatorCommand;

public class StartBillingRunCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new StartBillingRunCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("startbillingrun", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList(), command.getArgumentNames());
    }

    @Test
    public void testSuccess() throws Exception {
        stubCallReturn = Boolean.TRUE;
        assertTrue(command.run(ctx));
        assertEquals("startBillingRun", stubMethodName);
        assertOut("The manually triggered timer run was completed successfully.%n");
        assertErr("");
    }

    @Test
    public void testFailure() throws Exception {
        stubCallReturn = Boolean.FALSE;
        assertFalse(command.run(ctx));
        assertEquals("startBillingRun", stubMethodName);
        assertOut("");
        assertErr("A problem occurred during the billing run execution. Please refer to the log files.%n");
    }

}
