/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 14, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

/**
 * The model for creating a new marketplace. It only provides the fields that
 * are required for creating a new marketplace. The model for MVC pattern...
 * 
 * @author tang
 * 
 */
public class NewMarketplace {

    private String name;
    private String id;
    private String owningOrganizationId;
    private String tenantId;
    private boolean closed = false;
    private boolean taggingEnabled = true;
    private boolean reviewEnabled = true;
    private boolean categoriesEnabled = true;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the owningOrganizationId
     */
    public String getOwningOrganizationId() {
        return owningOrganizationId;
    }

    /**
     * @param owningOrganizationId
     *            the owningOrganizationId to set
     */
    public void setOwningOrganizationId(String owningOrganizationId) {
        this.owningOrganizationId = owningOrganizationId;
    }

    /**
     * @return the taggingEnabled
     */
    public boolean isTaggingEnabled() {
        return taggingEnabled;
    }

    /**
     * @param taggingEnabled
     *            the taggingEnabled to set
     */
    public void setTaggingEnabled(boolean taggingEnabled) {
        this.taggingEnabled = taggingEnabled;
    }

    /**
     * @return the reviewEnabled
     */
    public boolean isReviewEnabled() {
        return reviewEnabled;
    }

    /**
     * @param reviewEnabled
     *            the reviewEnabled to set
     */
    public void setReviewEnabled(boolean reviewEnabled) {
        this.reviewEnabled = reviewEnabled;
    }

    /**
     * @param closed
     *            the closed to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * set the flag indicating if category selection panel should be shown
     * 
     * @param categoriesEnabled
     */
    public void setCategoriesEnabled(boolean categoriesEnabled) {
        this.categoriesEnabled = categoriesEnabled;
    }

    /**
     * 
     * @return true if category selection panel should be shown
     */
    public boolean isCategoriesEnabled() {
        return categoriesEnabled;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
