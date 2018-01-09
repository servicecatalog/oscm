/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2012-02-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

/**
 * Represents a category for one or more services.
 * 
 */
public class VOCategory extends BaseVO {

    private static final long serialVersionUID = 8409610805879957673L;

    /**
     * The short name that uniquely identifies the category on the marketplace
     * (business key).
     */
    private String categoryId;

    /**
     * The short name that uniquely identifies the marketplace in the platform
     * (business key).
     */
    private String marketplaceId;

    /**
     * The localized category name.
     */
    private String name;

    /**
     * Returns the unique identifier of the category.
     *
     * @return the identifier
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the identifier of the category, which must be unique on the
     * marketplace.
     *
     * @param categoryId
     *            the identifier
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Returns the identifier of the marketplace the category belongs to.
     *
     * @return the marketplace ID
     */
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * Sets the identifier of the marketplace the category belongs to.
     *
     * @param marketplaceId
     *            the marketplace ID
     */
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    /**
     * Returns the localized name of the category for the language specified by
     * the caller.
     *
     * @return the category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the localized name of the category for the language specified by the
     * caller.
     *
     * @param name
     *            the category name
     */
    public void setName(String name) {
        this.name = name;
    }

}
