/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

/**
 * Represents a review for a service that is published on a marketplace. A
 * review consists of a comment and a rating.
 * 
 */
public class VOServiceReview extends BaseVO {

    private static final long serialVersionUID = 1528028649102993193L;

    private String title;

    private String comment;

    private int rating;

    private long modificationDate;

    private long productKey;

    private String userId;

    private String userName;

    /**
     * Default constructor.
     */
    public VOServiceReview() {
    }

    /**
     * Constructs a review with the given title, rating, and comment for the
     * specified user and service.
     * 
     * @param title
     *            the title of the review
     * @param rating
     *            the rating; specify a number between 1 (lowest) and 5
     *            (highest)
     * @param comment
     *            the review comment
     * @param userId
     *            the ID of the user writing the review
     * @param productKey
     *            the numeric key of the service that is subject to the review
     */
    public VOServiceReview(String title, int rating, String comment,
            String userId, long productKey) {
        this.title = title;
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.productKey = productKey;
    }

    /**
     * Returns the title of the review.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the review.
     * 
     * @param title
     *            the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the comment written in this review.
     * 
     * @return the review comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment of this review.
     * 
     * @param comment
     *            the review comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retrieves the rating given in this review. The rating is a value between
     * 1 (lowest) and 5 (highest). At a user interface, it is typically
     * represented by stars.
     * 
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating of this review. The rating is a value between 1 (lowest)
     * and 5 (highest). At a user interface, it is typically represented by
     * stars.
     * 
     * @param rating
     *            the rating
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Sets the last modification date and time of this review.
     * 
     * @param modificationDate
     *            the modification date and time
     */
    public void setModificationDate(long modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * Returns the last modification date and time of this review.
     * 
     * @return the modification date and time
     */
    public long getModificationDate() {
        return modificationDate;
    }

    /**
     * Sets the numeric key of the service which is subject to this review.
     * 
     * @param productKey
     *            the service key
     */
    public void setProductKey(long productKey) {
        this.productKey = productKey;
    }

    /**
     * Returns the numeric key of the service which is subject to this review.
     * 
     * @return the service key
     */
    public long getProductKey() {
        return productKey;
    }

    /**
     * Returns the unique ID of the user who wrote this review.
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique ID of the user writing this review.
     * 
     * @param userId
     *            the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the first and/or last name of the user who wrote this review,
     * depending on which information is available from the user's profile.
     * 
     * @return the first and/or last name, separated by a space, or
     *         <code>null</code> if none of the names is available
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the first and/or last name of the user writing this review,
     * depending on which information is available from the user's profile.
     * 
     * @param userName
     *            the user name as obtained with {@link #getUserName()}
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}
