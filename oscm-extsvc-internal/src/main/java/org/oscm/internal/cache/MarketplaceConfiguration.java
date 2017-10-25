/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 22, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.cache;

import java.io.Serializable;
import java.util.Set;

/**
 * UI model for Marketplace configurations(tagging, review, social bookmark)
 */
public class MarketplaceConfiguration implements Serializable {

    private static final long serialVersionUID = -7649326629529571515L;

    private boolean taggingEnabled = true;

    private boolean reviewEnabled = true;

    private boolean socialBookmarkEnabled = true;

    private boolean categoriesEnabled;

    private boolean restricted;

    private boolean landingPage;

    private Set<String> allowedOrganizations = null;
    private String tenantId;
    private String tenantTkey;

    /**
     * Indicating if the tag cloud is shown.
     * 
     * @return the taggingEnabled
     */
    public boolean isTaggingEnabled() {
        return taggingEnabled;
    }

    /**
     * Set if the tag cloud should be shown.
     * 
     * @param taggingEnabled
     *            the taggingEnabled to set
     */
    public void setTaggingEnabled(boolean taggingEnabled) {
        this.taggingEnabled = taggingEnabled;
    }

    /**
     * Indicating if the tag review/rating is shown.
     * 
     * @return the reviewEnabled
     */
    public boolean isReviewEnabled() {
        return reviewEnabled;
    }

    /**
     * Set if the tag review/rating should be shown.
     * 
     * @param reviewEnabled
     *            the reviewEnabled to set
     */
    public void setReviewEnabled(boolean reviewEnabled) {
        this.reviewEnabled = reviewEnabled;
    }

    /**
     * Indicating if the social bookmarks are shown.
     * 
     * @return the socialBookmarkEnabled
     */
    public boolean isSocialBookmarkEnabled() {
        return socialBookmarkEnabled;
    }

    /**
     * Set if the social bookmarks should be shown.
     * 
     * @param socialBookmarkEnabled
     *            the socialBookmarkEnabled to set
     */
    public void setSocialBookmarkEnabled(boolean socialBookmarkEnabled) {
        this.socialBookmarkEnabled = socialBookmarkEnabled;
    }

    /**
     * @param categoriesEnabled
     *            the categoriesEnabled to set
     */
    public void setCategoriesEnabled(boolean categoriesEnabled) {
        this.categoriesEnabled = categoriesEnabled;
    }

    /**
     * @return the categoriesEnabled
     */
    public boolean isCategoriesEnabled() {
        return categoriesEnabled;
    }

    /**
     * Returns true if the marketplace is restricted
     * 
     * @return true if restricted
     */
    public boolean isRestricted() {
        return restricted;
    }

    /**
     * Sets if the marketplace is restricted
     * 
     * @param restricted
     *            true if restricted
     */
    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    /**
     * Returns true if the marketplace has a landingpage
     * 
     * @return true if it has a landingPage
     */
    public boolean hasLandingPage() {
        return landingPage;
    }

    /**
     * Sets if the marketplace has a landingpage
     * 
     * @param landingPage
     *            true if it has a landingpage
     */
    public void setLandingPage(boolean landingPage) {
        this.landingPage = landingPage;
    }

    /**
     * Returns the ids of all allowed organizations for the marketplace
     * 
     * @return list of organization ids
     */
    public Set<String> getAllowedOrganizations() {
        return allowedOrganizations;
    }

    /**
     * Sets the ids of all allowed organizations for the marketplace
     * 
     * @param allowedOrganizations
     *            the list of organization ids
     */
    public void setAllowedOrganizations(Set<String> allowedOrganizations) {
        this.allowedOrganizations = allowedOrganizations;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantTkey(String tenantTkey) {
        this.tenantTkey = tenantTkey;
    }

    public String getTenantTkey() {
        return tenantTkey;
    }
}
