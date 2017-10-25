/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 03.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * JPA managed entity representing the marketplace data.
 * 
 * @author pock
 * 
 */
@Embeddable
public class MarketplaceData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 226658413689731229L;

    /**
     * Date of marketplace creation
     */
    private long creationDate;

    /**
     * For purpose of tracking the access to a catalog managed by OSCM
     */
    private String trackingCode;

    /**
     * The marketplace id
     */
    @Column(nullable = false)
    private String marketplaceId;

    /**
     * Flag indicating if this is an open marketplace.
     */
    @Column(nullable = false)
    private boolean open;

    /**
     * Flag indicating if the tag cloud is shown.
     */
    @Column(nullable = false)
    private boolean taggingEnabled;

    /**
     * Flag indicating if the tag review/rating is shown.
     */
    @Column(nullable = false)
    private boolean reviewEnabled;

    /**
     * Flag indicating if the social bookmarks are shown.
     */
    @Column(nullable = false)
    private boolean socialBookmarkEnabled;

    /**
     * Flag indicating if the categories are shown.
     */
    @Column(nullable = false)
    private boolean categoriesEnabled;
    
    /**
     * Flag indicating if the marketplace has restricted access.
     */
    @Column(nullable = false)
    private boolean restricted;
    
    /**
     * The URL to the CSS file for branding this marketplace.
     */
    private String brandingUrl;

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public void setTaggingEnabled(boolean taggingEnabled) {
        this.taggingEnabled = taggingEnabled;
    }

    public boolean isTaggingEnabled() {
        return taggingEnabled;
    }

    public void setReviewEnabled(boolean reviewEnabled) {
        this.reviewEnabled = reviewEnabled;
    }

    public boolean isReviewEnabled() {
        return reviewEnabled;
    }

    public void setSocialBookmarkEnabled(boolean socialBookmarkEnabled) {
        this.socialBookmarkEnabled = socialBookmarkEnabled;
    }

    public boolean isSocialBookmarkEnabled() {
        return socialBookmarkEnabled;
    }

    public String getBrandingUrl() {
        return brandingUrl;
    }

    public void setBrandingUrl(String brandingUrl) {
        this.brandingUrl = brandingUrl;
    }

    public boolean isCategoriesEnabled() {
        return categoriesEnabled;
    }

    public void setCategoriesEnabled(boolean categoriesEnabled) {
        this.categoriesEnabled = categoriesEnabled;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

}
