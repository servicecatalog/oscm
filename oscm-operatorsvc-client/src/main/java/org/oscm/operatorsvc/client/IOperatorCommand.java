/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorsvc.client;

import java.util.List;

/**
 * Interface for all operator commands.
 * 
 * @author hoffmann
 */
public interface IOperatorCommand {

    /**
     * Returns the name of this command.
     */
    public String getName();

    /**
     * Returns the description for this command.
     */
    public String getDescription();

    /**
     * Returns the set of arguments that is processed by this operation.
     */
    public List<String> getArgumentNames();

    /**
     * Executes the command with the given named arguments. All messages should
     * be written to the provided writers.
     * 
     * @return <code>true</code>, if the command was successful
     */
    public boolean run(CommandContext ctx) throws Exception;

    /**
     * Indicates if &gt; or &lt; shall be replaced by &amp;gt; or &amp;lt;.
     * 
     * @return <code>true</code> if replace otherwise <code>false</code>
     */
    public boolean replaceGreateAndLessThan();
}
