/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client.commands;

import org.junit.Test;
import org.oscm.internal.vo.VOTimerInfo;
import org.oscm.operatorsvc.client.IOperatorCommand;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RetrieveTimerExpirationsCommandTest extends CommandTestBase {

    @Override
    protected IOperatorCommand createCommand() {
        return new RetrieveTimerExpirationsCommand();
    }

    @Test
    public void testGetName() {
        assertEquals("retrievetimerexpirations", command.getName());
    }

    @Test
    public void testGetArgumentNames() {
        assertEquals(Arrays.asList(), command.getArgumentNames());
    }

    @Test
    public void testNoRegisteredTimers() throws Exception {
        stubCallReturn = Collections.emptyList();
        assertTrue(command.run(ctx));
        assertEquals("getTimerExpirationInformation", stubMethodName);
        assertOut("No timers are registered.%n");
        assertErr("");
    }

    @Test
    public void testWithRegisteredTimers() throws Exception {
        Date date = new Date(System.currentTimeMillis());
        String format = new SimpleDateFormat().format(date);

        VOTimerInfo ti1 = new VOTimerInfo();
        ti1.setTimerType("T1");
        ti1.setExpirationDate(date);

        VOTimerInfo ti2 = new VOTimerInfo();
        ti2.setTimerType("T2");
        ti2.setExpirationDate(date);

        VOTimerInfo ti3 = new VOTimerInfo();
        ti3.setTimerType("T3");
        ti3.setExpirationDate(date);

        stubCallReturn = Arrays.asList(ti1, ti2, ti3);
        String message = "Timer '%s' expires at: %s";
        String expectedOut = "Currently registered timers:%n%s%n%s%n%s%n";
        expectedOut = String.format(expectedOut, String.format(message, "T1",
                format), String.format(message, "T2", format), String.format(
                message, "T3", format));
        assertTrue(command.run(ctx));
        assertEquals(stubMethodName, "getTimerExpirationInformation");
        assertOut(expectedOut);
        assertErr("");
    }

}
