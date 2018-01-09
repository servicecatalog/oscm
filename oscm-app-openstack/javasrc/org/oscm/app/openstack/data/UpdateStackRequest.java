/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Nov 29, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.data;


public class UpdateStackRequest extends AbstractStackRequest {

    private String stackName;

    public UpdateStackRequest(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Stack name is not optional");
        }
        stackName = name;
    }

    public String getStackName() {
        return stackName;
    }
}
