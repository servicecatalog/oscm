/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the customer feedback on a service that is published on a
 * marketplace. A feedback is basically a list of reviews.
 * 
 */
public class VOServiceFeedback implements Serializable {

    private static final long serialVersionUID = 9044682410586578827L;

    private List<VOServiceReview> reviews = new ArrayList<VOServiceReview>();

    private BigDecimal averageRating;

    private boolean allowedToWriteReview;

    private long serviceKey;

    /**
     * Sets the service reviews that make up this feedback. A review consists of
     * a comment and a rating.
     * 
     * @param ratings
     *            the list of reviews
     */
    public void setReviews(List<VOServiceReview> ratings) {
        this.reviews = ratings;
    }

    /**
     * Retrieves the service reviews that make up this feedback. A review
     * consists of a comment and a rating.
     * 
     * @return the list of reviews
     */
    public List<VOServiceReview> getReviews() {
        return reviews;
    }

    /**
     * Returns the average rating of all reviews that make up this feedback.
     * 
     * @return the average rating
     */
    public BigDecimal getAverageRating() {
        return averageRating;
    }

    /**
     * Sets the average rating of all reviews that make up this feedback.
     * 
     * @param averageRating
     *            the average rating; specify a value between 1 and 5
     */
    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    /**
     * Returns the numeric key of the service this feedback belongs to.
     * 
     * @return the key
     */
    public long getServiceKey() {
        return serviceKey;
    }

    /**
     * Sets the numeric key of the service this feedback belongs to.
     * 
     * @param serviceKey
     *            the key
     */
    public void setServiceKey(long serviceKey) {
        this.serviceKey = serviceKey;
    }

    /**
     * Specifies whether the current user is allowed to write a review for the
     * service this feedback belongs to.
     * 
     * @param allowedToWriteReview
     *            <code>true</code> if the user is allowed to write a review,
     *            <code>false</code> otherwise
     */
    public void setAllowedToWriteReview(boolean allowedToWriteReview) {
        this.allowedToWriteReview = allowedToWriteReview;
    }

    /**
     * Checks whether the current user is allowed to write a review for the
     * service this feedback belongs to.
     * <p>
     * A user is allowed to write a review for a service in the following cases:
     * <ul>
     * <li>
     * The user's organization has at least one subscription to the service, and
     * the user is assigned to such a subscription or is an administrator of the
     * organization.</li>
     * <li>
     * The user has written a review for the service before.</li>
     * </ul>
     * 
     * @return <code>true</code> if the user is allowed to write a review,
     *         <code>false</code> otherwise
     */
    public boolean isAllowedToWriteReview() {
        return allowedToWriteReview;
    }

}
