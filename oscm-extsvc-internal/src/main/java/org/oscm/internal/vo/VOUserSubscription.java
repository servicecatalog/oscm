/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;

import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;

/**
 * Represents a subscription to a service, enhanced by the assignment of a
 * specific user.
 * 
 */
public class VOUserSubscription extends VOSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    private VOUsageLicense license;

    /**
     * Retrieves the usage license for the subscription, i.e. the assigned user
     * with his service role.
     * 
     * @return the usage license
     */
    public VOUsageLicense getLicense() {
        return license;
    }

    /**
     * Sets the usage license for the subscription, i.e. the assigned user with
     * his service role.
     * 
     * @param license
     *            the usage license
     */
    public void setLicense(VOUsageLicense license) {
        this.license = license;
    }

}
