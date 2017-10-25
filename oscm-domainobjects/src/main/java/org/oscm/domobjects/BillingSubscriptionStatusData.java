/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.03.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container for BillingSubscriptionStatus domain object
 * 
 * @author baumann
 */
@Embeddable
public class BillingSubscriptionStatusData extends DomainDataContainer
        implements Serializable {

    private static final long serialVersionUID = -2219241770144734866L;

    /**
     * The technical key of the corresponding subscription. There is association
     * with the Subscription domain object defined because of concurrency cases
     * (subscription modification and billing run at the same time).
     */
    @Column(nullable = false)
    private long subscriptionKey;

    /**
     * The end of the last period that was billed for the subscription
     */
    @Column(nullable = false)
    private long endOfLastBilledPeriod;

    public long getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(long subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public long getEndOfLastBilledPeriod() {
        return endOfLastBilledPeriod;
    }

    public void setEndOfLastBilledPeriod(long endOfLastBilledPeriod) {
        this.endOfLastBilledPeriod = endOfLastBilledPeriod;
    }

}
