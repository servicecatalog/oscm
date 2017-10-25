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
 * Data container to hold the information on each PricedParameter.
 * 
 * @author Ravi
 * 
 */

@Embeddable
public class PricedParameterData extends DomainDataContainer {

    private static final long serialVersionUID = 6566613141530508961L;

    private BigDecimal pricePerUser = BigDecimal.ZERO;

    private BigDecimal pricePerSubscription = BigDecimal.ZERO;

    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    public BigDecimal getPricePerSubscription() {
        return pricePerSubscription;
    }

    public void setPricePerSubscription(BigDecimal pricePerSubscription) {
        this.pricePerSubscription = pricePerSubscription;
    }
}
