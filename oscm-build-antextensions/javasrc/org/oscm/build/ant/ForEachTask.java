/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.build.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * Executes a wrapped sequence of tasks for a list of tokens.
 * 
 * @author hoffmann
 */
public class ForEachTask extends Task implements TaskContainer {

    private static final String SEPERATORS = " \n\r\t,;:";

    private String property = "item";

    private final List<Task> tasks = new ArrayList<Task>();

    private final List<String> tokens = new ArrayList<String>();

    public void setTokens(String tokens) {
        final StringTokenizer tokenizer = new StringTokenizer(tokens,
                SEPERATORS);
        while (tokenizer.hasMoreTokens()) {
            this.tokens.add(tokenizer.nextToken());
        }
    }

    /**
     * Sets the name of the property each item is assigned within the loop.
     * 
     * @param property
     *            name of the item property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Adds the child tasks to execute in the loop.
     */
    public void addTask(final Task task) {
        tasks.add(task);
    }

    @Override
    public void execute() throws BuildException {
        final String bak = getProject().getProperty(property);
        for (String token : tokens) {
            getProject().setProperty(property, token);
            for (Task task : tasks) {
                task.maybeConfigure();
                task.execute();
            }
        }
        if (bak != null) {
            getProject().setProperty(property, bak);
        }
    }

}
