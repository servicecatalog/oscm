/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.data;

/**
 * Provides application instance data returned to the platform upon calls to the
 * provisioning service of an application.
 */
public class InstanceResult extends BaseResult {

    /**
     * The application instance that belongs to a subscription.
     * 
     */
    private InstanceInfo instanceInfo;

    /**
     * Retrieves the application instance that belongs to a subscription. An
     * instance is the set of items that an application provisions for a
     * specific subscription.
     * 
     * @return an <code>InstanceInfo</code> object representing the application
     *         instance
     */
    public InstanceInfo getInstance() {
        return instanceInfo;
    }

    /**
     * Sets the application instance that belongs to a subscription. An instance
     * is the set of items that an application provisions for a specific
     * subscription.
     * 
     * @param instance
     *            an <code>InstanceInfo</code> object representing the
     *            application instance
     */
    public void setInstance(InstanceInfo instance) {
        this.instanceInfo = instance;
    }

}
