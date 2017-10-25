/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.03.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * Represents the billing status for a specific subscription
 * 
 * @author baumann
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "subscriptionKey" }))
@NamedQueries({ @NamedQuery(name = "BillingSubscriptionStatus.findByBusinessKey", query = "select bss from BillingSubscriptionStatus bss where bss.dataContainer.subscriptionKey=:subscriptionKey") })
@BusinessKey(attributes = { "subscriptionKey" })
public class BillingSubscriptionStatus extends
        DomainObjectWithVersioning<BillingSubscriptionStatusData> {

    private static final long serialVersionUID = -2026574417879867283L;

    public BillingSubscriptionStatus() {
        super();
        dataContainer = new BillingSubscriptionStatusData();
    }

    /**
     * Refer to {@link SubscriptionData#subscriptionKey}
     */
    public long getSubscriptionKey() {
        return dataContainer.getSubscriptionKey();
    }

    /**
     * Refer to {@link SubscriptionData#subscriptionKey}
     */
    public void setSubscriptionKey(long subscriptionKey) {
        dataContainer.setSubscriptionKey(subscriptionKey);
    }

    /**
     * Refer to {@link SubscriptionData#endOfLastBilledPeriod}
     */
    public long getEndOfLastBilledPeriod() {
        return dataContainer.getEndOfLastBilledPeriod();
    }

    /**
     * Refer to {@link SubscriptionData#endOfLastBilledPeriod}
     */
    public void setEndOfLastBilledPeriod(long endOfLastBilledPeriod) {
        dataContainer.setEndOfLastBilledPeriod(endOfLastBilledPeriod);
    }
}
