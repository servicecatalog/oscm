/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jan 11, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Embeddable;

/**
 * Data container to hold the information on each PricedOption.
 * 
 * @author Ravi
 * 
 */
@Embeddable
public class PricedOptionData extends DomainDataContainer {

    private static final long serialVersionUID = -3363189933896188297L;

    private BigDecimal pricePerUser = BigDecimal.ZERO;

    private BigDecimal pricePerSubscription = BigDecimal.ZERO;

    /**
     * @return the pricePerUser
     */
    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

    /**
     * @param pricePerUser
     *            the pricePerUser to set
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    /**
     * @return the pricePerSubscription
     */
    public BigDecimal getPricePerSubscription() {
        return pricePerSubscription;
    }

    /**
     * @param pricePerSubscription
     *            the pricePerSubscription to set
     */
    public void setPricePerSubscription(BigDecimal pricePerSubscription) {
        this.pricePerSubscription = pricePerSubscription;
    }

}
