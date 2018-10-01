/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by PLGrubskiM on 2017-03-27.
 */
public class PowerStateTest {

    @Test(expected = IllegalArgumentException.class)
    public void valueOfIgnoreCaseTest_invalidArgument() {
        PowerState.valueOfIgnoreCase("this does not exist");
    }

    @Test
    public void valueOfIgnoreCaseTest() {
        final PowerState deallocated = PowerState.valueOfIgnoreCase("deallocated");
        final PowerState deallocating = PowerState.valueOfIgnoreCase("DEALLOCATING");
        final PowerState running = PowerState.valueOfIgnoreCase("Running");
        final PowerState starting = PowerState.valueOfIgnoreCase("starting");
        final PowerState stopping = PowerState.valueOfIgnoreCase("STOPPING");
        final PowerState stopped = PowerState.valueOfIgnoreCase("STOPPED");
        final PowerState unknown = PowerState.valueOfIgnoreCase("unknown");

        Assert.assertTrue(deallocated.equals(PowerState.DEALLOCATED));
        Assert.assertTrue(deallocating.equals(PowerState.DEALLOCATING));
        Assert.assertTrue(running.equals(PowerState.RUNNING));
        Assert.assertTrue(starting.equals(PowerState.STARTING));
        Assert.assertTrue(stopping.equals(PowerState.STOPPING));
        Assert.assertTrue(stopped.equals(PowerState.STOPPED));
        Assert.assertTrue(unknown.equals(PowerState.UNKNOWN));
    }
}
