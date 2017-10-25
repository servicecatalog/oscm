/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: May 12, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container for the domain object <code>ProductFeedback</code>.
 * 
 * @author barzu
 */
@Embeddable
public class ProductFeedbackData extends DomainDataContainer {

    private static final long serialVersionUID = -4591822578066881809L;

    /**
     * The average rating for a <code>Product</code>. Must be <code>null</code>
     * if the product is a customer specific product.
     */
    @Column(nullable = true)
    private BigDecimal averageRating;

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

}
