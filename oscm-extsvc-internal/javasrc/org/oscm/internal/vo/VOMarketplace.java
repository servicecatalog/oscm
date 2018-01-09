/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2011-03-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

/**
 * Represents a marketplace with a catalog of services provided by one or more
 * suppliers, brokers, or resellers.
 * 
 */
public class VOMarketplace extends BaseVO  {

    private static final long serialVersionUID = -4732615853529027987L;

    private String marketplaceId;

    private String name;

    private boolean open;

    private String owningOrganizationName;

    private String owningOrganizationId;
    
    private String tenantId;

    private boolean taggingEnabled = true;
    private boolean reviewEnabled = true;
    private boolean socialBookmarkEnabled = true;
    private boolean categoriesEnabled = true;
    private boolean restricted = false;
    private boolean hasPublicLandingPage;

    /**
     * Default constructor.
     */
    public VOMarketplace() {

    }

    /**
     * Sets the unique identifier of the marketplace.
     *
     * @param marketplaceId
     *            the marketplace ID
     */
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    /**
     * Retrieves the unique identifier of the marketplace.
     *
     * @return the marketplace ID
     */
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * Sets the name of the marketplace.
     *
     * @param name
     *            the marketplace name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of the marketplace.
     *
     * @return the marketplace name
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves information on whether the marketplace is open for any
     * supplier, broker, and reseller organization to publish services, or
     * whether it is restricted to specific suppliers, brokers, and resellers
     * admitted by the marketplace owner.
     *
     * @return <code>true</code> for an open marketplace, <code>false</code>
     *         otherwise
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Specifies whether the marketplace is open for any supplier, broker, and
     * reseller organization to publish services, or whether it is restricted to
     * specific suppliers, brokers, and resellers admitted by the marketplace
     * owner.
     *
     * @param open
     *            <code>true</code> for an open marketplace, <code>false</code>
     *            otherwise
     */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Retrieves the ID of the marketplace owner organization.
     *
     * @return the organization ID
     */
    public String getOwningOrganizationId() {
        return owningOrganizationId;
    }

    /**
     * Returns whether the tags of the tag cloud are displayed on the
     * marketplace.
     *
     * @return <code>true</code> if the tag cloud is enabled, <code>false</code>
     *         otherwise
     */
    public boolean isTaggingEnabled() {
        return taggingEnabled;
    }

    /**
     * Specifies whether the tags of the tag cloud are displayed on the
     * marketplace.
     *
     * @param taggingEnabled
     *            <code>true</code> if the tag cloud is to be enabled,
     *            <code>false</code> otherwise
     */
    public void setTaggingEnabled(boolean taggingEnabled) {
        this.taggingEnabled = taggingEnabled;
    }

    /**
     * Returns whether reviews and ratings can be made and are shown on the
     * marketplace.
     *
     * @return <code>true</code> if reviews are enabled, <code>false</code>
     *         otherwise
     */
    public boolean isReviewEnabled() {
        return reviewEnabled;
    }

    /**
     * Specifies whether reviews and ratings can be made and are shown on the
     * marketplace.
     *
     * @param reviewEnabled
     *            <code>true</code> if reviews are to be enabled,
     *            <code>false</code> otherwise
     */
    public void setReviewEnabled(boolean reviewEnabled) {
        this.reviewEnabled = reviewEnabled;
    }

    /**
     * Returns whether social bookmarks are shown on the marketplace.
     *
     * @return <code>true</code> if social bookmarks are enabled,
     *         <code>false</code> otherwise
     */
    public boolean isSocialBookmarkEnabled() {
        return socialBookmarkEnabled;
    }

    /**
     * Specifies whether social bookmarks are shown on the marketplace.
     *
     * @param socialBookmarkEnabled
     *            <code>true</code> if social bookmarks are to be enabled,
     *            <code>false</code> otherwise
     */
    public void setSocialBookmarkEnabled(boolean socialBookmarkEnabled) {
        this.socialBookmarkEnabled = socialBookmarkEnabled;
    }

    /**
     * Sets the ID of the marketplace owner organization.
     *
     * @param id
     *            the organization ID
     */
    public void setOwningOrganizationId(String id) {
        owningOrganizationId = id;
    }

    /**
     * Retrieves the name of the marketplace owner organization.
     *
     * @return the organization name
     */
    public String getOwningOrganizationName() {
        return owningOrganizationName;
    }

    /**
     * Sets the name of the marketplace owner organization.
     *
     * @param name
     *            the organization name
     */
    public void setOwningOrganizationName(String name) {
        owningOrganizationName = name;
    }

    /**
     * Specifies whether categories are available for searching and browsing on
     * the marketplace.
     *
     * @param categoriesEnabled
     *            <code>true</code> if categories are to be enabled,
     *            <code>false</code> otherwise
     */
    public void setCategoriesEnabled(boolean categoriesEnabled) {
        this.categoriesEnabled = categoriesEnabled;
    }

    /**
     * Returns whether categories are available for searching and browsing on
     * the marketplace.
     *
     * @return <code>true</code> if categories are enabled, <code>false</code>
     *         otherwise
     */
    public boolean isCategoriesEnabled() {
        return categoriesEnabled;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return getMarketplaceId() == null ? 0 : getMarketplaceId().hashCode();
    }

    /**
     * Indicates whether the given object is equal to this one.
     *
     * @param obj
     *            the reference object with which to compare
     * @return <code>true</code> if this object is the same as the
     *         <code>obj</code> argument; <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // returns also false if obj is null
        if (!(obj instanceof VOMarketplace)) {
            return false;
        }

        VOMarketplace marketplace = (VOMarketplace) obj;
        if (getMarketplaceId() != null) {
            return getMarketplaceId().equals(marketplace.getMarketplaceId());
        } else {
            return false;
        }
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }


    public boolean isHasPublicLandingPage() {
        return hasPublicLandingPage;
    }

    public void setHasPublicLandingPage(boolean hasPublicLandingPage) {
        this.hasPublicLandingPage = hasPublicLandingPage;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}
