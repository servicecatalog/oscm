/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: May 12, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container for the domain object <code>ProductReview</code>.
 * 
 * @author barzu
 */
@Embeddable
public class ProductReviewData extends DomainDataContainer {

    private static final long serialVersionUID = -534064256551599759L;

    /**
     * The value of the rating from 1 to 5.
     */
    @Column(nullable = false)
    private int rating;

    /**
     * A title the user must provide.
     */
    @Column(nullable = false)
    private String title;

    /**
     * The explanation of the <code>rating</code> value.
     */
    @Column(nullable = false)
    private String comment;

    /**
     * The last modification date (milliseconds since Epoch).
     */
    @Column(nullable = false)
    private long modificationDate;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(long modificationDate) {
        this.modificationDate = modificationDate;
    }

}
