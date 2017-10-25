/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PricedProductRoleData extends DomainDataContainer {

    private static final long serialVersionUID = -1539526090831265120L;

    /**
     * Value of discount in percents.
     */
    @Column(nullable = false)
    private BigDecimal pricePerUser = BigDecimal.ZERO;

    /**
     * Setter for price.
     * 
     * @param pricePerUser
     */
    public void setPricePerUser(BigDecimal pricePerUser) {
        this.pricePerUser = pricePerUser;
    }

    /**
     * Getter for price.
     * 
     * @return
     */
    public BigDecimal getPricePerUser() {
        return pricePerUser;
    }

}
