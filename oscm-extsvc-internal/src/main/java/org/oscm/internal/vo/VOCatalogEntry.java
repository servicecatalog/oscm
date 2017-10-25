/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-01-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;

/**
 * Represents an entry for a service in a catalog, for example, on a
 * marketplace.
 * 
 */
public class VOCatalogEntry extends BaseVO {

    private static final long serialVersionUID = 4317886617023906055L;

    private VOMarketplace marketplace;
    private boolean anonymousVisible;
    private List<VOCategory> categories = new ArrayList<VOCategory>();

    /**
     * Indicates if the entry is visible in the catalog. Default value is
     * <code>true</code>
     */
    private boolean visibleInCatalog = true;

    private VOService service;

    /**
     * Retrieves the service which is represented by this entry in the catalog.
     * 
     * @return the service
     */
    public VOService getService() {
        return service;
    }

    /**
     * Sets the service to be represented by this entry in the catalog.
     * 
     * @param service
     *            the service
     */
    public void setService(VOService service) {
        this.service = service;
    }

    /**
     * Sets the marketplace (and thus the catalog) this entry belongs to.
     * 
     * @param marketplace
     *            the marketplace
     */
    public void setMarketplace(VOMarketplace marketplace) {
        this.marketplace = marketplace;
    }

    /**
     * Retrieves the marketplace (and thus the catalog) this entry belongs to.
     * 
     * @return the marketplace
     */
    public VOMarketplace getMarketplace() {
        return marketplace;
    }

    /**
     * Specifies whether the service represented by this entry is to be visible
     * in the catalog to users who are not logged in.
     * 
     * @param anonymousVisible
     *            <code>true</code> if the service is to be visible to anonymous
     *            users, <code>false</code> otherwise
     */
    public void setAnonymousVisible(boolean anonymousVisible) {
        this.anonymousVisible = anonymousVisible;
    }

    /**
     * Returns whether the service represented by this entry is visible in the
     * catalog to users who are not logged in.
     * 
     * @return <code>true</code> if the service is visible to anonymous users,
     *         <code>false</code> otherwise
     */
    public boolean isAnonymousVisible() {
        return anonymousVisible;
    }

    /**
     * Returns whether this entry is shown in the catalog. The default value is
     * <code>true</code>.
     * 
     * @return <code>true</code> if the entry is visible in the catalog,
     *         <code>false</code> otherwise
     */
    public boolean isVisibleInCatalog() {
        return visibleInCatalog;
    }

    /**
     * Specifies whether this entry is shown in the catalog. The default value
     * is <code>true</code>.
     * 
     * @param visibleInCatalog
     *            <code>true</code> if the entry is to be visible in the
     *            catalog, <code>false</code> otherwise
     */
    public void setVisibleInCatalog(boolean visibleInCatalog) {
        this.visibleInCatalog = visibleInCatalog;
    }

    /**
     * Returns the categories which are assigned to the service represented by
     * this catalog entry.
     * 
     * @return the list of categories
     */
    public List<VOCategory> getCategories() {
        return categories;
    }

    /**
     * Assigns the given categories to the service represented by this catalog
     * entry.
     * 
     * @param categories
     *            the list of categories
     */
    public void setCategories(List<VOCategory> categories) {
        this.categories = categories;
    }
}
