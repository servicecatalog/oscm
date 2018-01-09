/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Nov 29, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.data;


/**
 * Build a JSON structure for sending a request to the Heat API to create a
 * stack.
 */
public class CreateStackRequest extends AbstractStackRequest {

    /**
     * 
     * @param stackName
     *            the name of the stack that will be created
     */
    public CreateStackRequest(String stackName) {
        if (stackName == null || stackName.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "The stack name cannot be null or empty.");
        }
        put("stack_name", stackName);
    }
}
