/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;

/**
 * Represents a marketable service, enhanced by information on whether the
 * maximum number of subscriptions allowed by the technical service is reached.
 * 
 */
public class VOServiceEntry extends VOService implements Serializable {

    private static final long serialVersionUID = 4530724324736743515L;

    private boolean subscriptionLimitReached = false;

    /**
     * Checks whether a subscription already exists for the marketable service
     * whose underlying technical service allows for one subscription only.
     * 
     * @return <code>true</code> if the subscription limit is reached,
     *         <code>false</code> otherwise
     */
    public boolean isSubscriptionLimitReached() {
        return subscriptionLimitReached;
    }

    /**
     * Specifies whether a subscription already exists for the marketable
     * service whose underlying technical service allows for one subscription
     * only.
     * 
     * @param subscriptionLimitReached
     *            <code>true</code> if the subscription limit is reached,
     *            <code>false</code> otherwise. The default is
     *            <code>false</code>.
     */
    public void setSubscriptionLimitReached(boolean subscriptionLimitReached) {
        this.subscriptionLimitReached = subscriptionLimitReached;
    }

}
