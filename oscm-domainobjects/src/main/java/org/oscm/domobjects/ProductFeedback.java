/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: May 12, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

/**
 * The collection of all user reviews for a <code>Product</code>.
 * 
 * @author barzu
 */
@Entity
public class ProductFeedback extends
        DomainObjectWithVersioning<ProductFeedbackData> {

    private static final long serialVersionUID = -1778086741602708671L;

    public ProductFeedback() {
        dataContainer = new ProductFeedbackData();
    }

    /**
     * The product these reviews belong to (mandatory).
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Product product;

    /**
     * A collection of all reviews given by users to the referenced product.
     */
    @OneToMany(mappedBy = "productFeedback", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dataContainer.modificationDate DESC")
    private List<ProductReview> productReviews = new ArrayList<ProductReview>();

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<ProductReview> getProductReviews() {
        return productReviews;
    }

    public void setProductReviews(List<ProductReview> productReviews) {
        this.productReviews = productReviews;
    }

    /**
     * Returns the review of the given user.
     */
    public ProductReview getProductReview(PlatformUser user) {
        for (ProductReview review : getProductReviews()) {
            if (review.getPlatformUser().equals(user)) {
                return review;
            }
        }
        return null;
    }

    /**
     * Refer to {@link ProductFeedbackData#averageRating}
     */
    public BigDecimal getAverageRating() {
        return dataContainer.getAverageRating();
    }

    /**
     * Refer to {@link ProductFeedbackData#averageRating}
     */
    public void setAverageRating(BigDecimal averageRating) {
        dataContainer.setAverageRating(averageRating);
    }

    /**
     * Calculates the current average value of all reviews.
     */
    public void updateAverageRating() {
        BigDecimal result = new BigDecimal(0);
        for (ProductReview review : getProductReviews()) {
            result = result.add(new BigDecimal(review.getRating()));
        }
        if (getProductReviews().size() > 0) {
            result = result.divide(new BigDecimal(getProductReviews().size()),
                    2, BigDecimal.ROUND_HALF_UP);
        }
        setAverageRating(result);
    }

    /**
     * Returns true if the given user has created a review.
     * 
     * @param user
     *            the user to check
     * @return boolean
     */
    public boolean hasReview(PlatformUser user) {
        return (getProductReview(user) != null);
    }

}
