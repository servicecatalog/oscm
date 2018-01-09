/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import static org.junit.Assert.assertNull;
import org.junit.Assert;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Test;

public class TimestampTaskTest {

    private Project project;
    private TimestampTask task;

    @Before
    public void setup() {
        project = new Project();
        task = new TimestampTask();
        task.setProject(project);
    }

    @Test(expected = BuildException.class)
    public void testTimestampNameNotSet() {
        task.execute();
    }

    @Test
    public void testTimestamp() {
        final String stampPropertyName = "timestamp";

        assertNull(project.getProperty(stampPropertyName));

        task.setName(stampPropertyName);
        task.execute();

        final String tstamp1 = project.getProperty(stampPropertyName);

        try {
            // Wait at least 1ms to get different timestamp
            Thread.sleep(1);
        } catch (Throwable t) {
            // Ignore
        }
        task.setName(stampPropertyName);
        task.execute();

        final String tstamp2 = project.getProperty(stampPropertyName);

        Assert.assertNotNull(tstamp1);
        Assert.assertNotNull(tstamp2);

        // Value must be different
        Assert.assertFalse(tstamp1.equals(tstamp2));
    }

}
