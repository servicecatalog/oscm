/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-08-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;

/**
 * Provides information on an application instance. An instance is the set of
 * items that an application provisions for a subscription.
 * 
 */
public class InstanceDescription extends InstanceStatus implements Serializable {

    private static final long serialVersionUID = 1250743005647854513L;

    private String instanceId;

    /**
     * Retrieves the identifier of the application instance as defined when the
     * instance is created.
     * 
     * @return the instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the identifier of the application instance as defined when the
     * instance is created.
     * <p>
     * <b>Important note:</b>
     * <p>
     * The instance ID must be unique within all instances which are handled by
     * APP, even if they are related to different controllers. We strongly
     * recommend you prepend the ID of the respective controller in order to
     * avoid conflicts at runtime.
     * <p>
     * Example: <code><i>controllerID</i>.instance_12345</code>
     * 
     * @param instanceId
     *            the instance ID
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
