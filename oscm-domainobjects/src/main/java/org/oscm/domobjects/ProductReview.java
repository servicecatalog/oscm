/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: May 12, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * A review of a <code>Product</code> given by one user. It consists of an
 * integer rating value between 1 and 5, a title and a description.
 * 
 * @author barzu
 */
@Entity
@BusinessKey(attributes = { "platformUser", "productFeedback" })
@NamedQueries({
        @NamedQuery(name = "ProductReview.findByUser", query = "select r from ProductReview r where r.platformUser=:platformUser"),
        @NamedQuery(name = "ProductReview.findByBusinessKey", query = "select r from ProductReview r where r.platformUser=:platformUser and r.productFeedback=:productFeedback") })
public class ProductReview extends
        DomainObjectWithVersioning<ProductReviewData> {

    private static final long serialVersionUID = 4418368426157722708L;

    public ProductReview() {
        dataContainer = new ProductReviewData();
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PlatformUser platformUser;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ProductFeedback productFeedback;

    public PlatformUser getPlatformUser() {
        return platformUser;
    }

    public void setPlatformUser(PlatformUser platformUser) {
        this.platformUser = platformUser;
    }

    public ProductFeedback getProductFeedback() {
        return productFeedback;
    }

    public void setProductFeedback(ProductFeedback productFeedback) {
        this.productFeedback = productFeedback;
    }

    /**
     * Refer to {@link ProductReviewData#rating}
     */
    public int getRating() {
        return dataContainer.getRating();
    }

    /**
     * Refer to {@link ProductReviewData#rating}
     */
    public void setRating(int rating) {
        dataContainer.setRating(rating);
    }

    /**
     * Refer to {@link ProductReviewData#title}
     */
    public String getTitle() {
        return dataContainer.getTitle();
    }

    /**
     * Refer to {@link ProductReviewData#title}
     */
    public void setTitle(String title) {
        dataContainer.setTitle(title);
    }

    /**
     * Refer to {@link ProductReviewData#comment}
     */
    public String getComment() {
        return dataContainer.getComment();
    }

    /**
     * Refer to {@link ProductReviewData#comment}
     */
    public void setComment(String comment) {
        dataContainer.setComment(comment);
    }

    /**
     * Refer to {@link ProductReviewData#modificationDate}
     */
    public long getModificationDate() {
        return dataContainer.getModificationDate();
    }

    /**
     * Refer to {@link ProductReviewData#modificationDate}
     */
    public void setModificationDate(long modificationDate) {
        dataContainer.setModificationDate(modificationDate);
    }

    /**
     * The given user is allowed to update this review in case the user is the
     * owner of this review
     * 
     * @param user
     *            the user to be checked
     * @return boolean
     */
    public boolean isAllowedToModify(PlatformUser user) {
        return (user.equals(getPlatformUser()));
    }

    /**
     * The given user is allowed to delete this review if the user is
     * <ul>
     * <li>the creator of the review</li>
     * <li>a administrator of the organization which the creator belongs to</li>
     * </ul>
     * 
     * @param user
     *            the user to check.
     * @return true in case the user is allowed to delete the review, otherwise
     *         false.
     */
    public boolean isAllowedToDelete(PlatformUser user) {
        // Check if the current user is the creator of the review
        if (user.equals(getPlatformUser())) {
            return true;
        }

        // Check if the current user is an admin of the org which the creator of
        // the review belongs to
        Organization org = getPlatformUser().getOrganization();
        if (user.isOrganizationAdmin() && user.getOrganization().equals(org)) {
            return true;
        }
        return false;
    }

    /**
     * Determines all marketplaces on which the review was published. A review
     * is published on the same marketplaces as the product.
     * 
     * @return all marketplaces on which the review was published.
     */
    public Set<Marketplace> getPublishedMarketplaces() {
        Product ratedProduct = getProductFeedback().getProduct();
        List<CatalogEntry> catalogEntries = ratedProduct.getCatalogEntries();
        Set<Marketplace> marketplaces = new HashSet<Marketplace>();
        for (CatalogEntry catalogEntry : catalogEntries) {
            if (catalogEntry.getMarketplace() != null) {
                marketplaces.add(catalogEntry.getMarketplace());
            }
        }
        return marketplaces;
    }

    /**
     * Update the last modification time. Method is call by JPA.
     */
    @PrePersist
    @PreUpdate
    public void updateModificationDate() {
        setModificationDate(new Date().getTime());
    }

}
