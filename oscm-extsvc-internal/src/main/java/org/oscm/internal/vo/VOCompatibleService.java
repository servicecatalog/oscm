/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2001-12-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

/**
 * Represents a marketable service which is already included in or can be added
 * to the list of services to which a subscription to another given marketable
 * service can be upgraded or downgraded.
 * 
 */
public class VOCompatibleService extends VOService {

    private static final long serialVersionUID = 4129437139885483424L;

    /**
     * Set to <code>true</code> if the service is already included in the list
     * of services to which a subscription to another service can be upgraded or
     * downgraded. Otherwise, the service can be added to this list, because it
     * is based on the same technical service and published on the same
     * marketplace as the other service.
     */
    private boolean compatible;

    /**
     * Checks whether the service is already included in the list of services to
     * which a subscription to another service can be upgraded or downgraded.
     * 
     * @return <code>true</code> if the service is already included in the list,
     *         <code>false</code> if the service is not yet included in the list
     *         but can be added to it because it is based on the same technical
     *         service and published on the same marketplace as the other
     *         service
     */
    public boolean isCompatible() {
        return compatible;
    }

    /**
     * Specifies that the service is included in the list of services to which a
     * subscription to another service can be upgraded or downgraded.
     * 
     * @param compatible
     *            <code>true</code> if the service is included in the list,
     *            <code>false</code> if the service is not yet included in the
     *            list but can be added to it because it is based on the same
     *            technical service and published on the same marketplace as the
     *            other service
     */
    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }
}
