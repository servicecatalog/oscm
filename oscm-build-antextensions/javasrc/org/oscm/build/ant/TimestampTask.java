/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Stores the current timestamp in milliseconds within the given property.
 * <p>
 * This works even if the property already contains any value (in contrast to
 * the ANT concepts).
 * 
 * @author soehnges
 */
public class TimestampTask extends Task {
    private String name = null;

    /**
     * Set the name of the property.
     * 
     * @param name
     *            the name of the property.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Execute this task.
     * 
     * @exception BuildException
     *                Description of the Exception
     */
    @Override
    public void execute() throws BuildException {
        if (name == null || name.equals("")) {
            throw new BuildException(
                    "The 'name' attribute is required within 'timestamp'.");
        }

        getProject().setProperty(name,
                Long.toString(System.currentTimeMillis()));
    }
}
