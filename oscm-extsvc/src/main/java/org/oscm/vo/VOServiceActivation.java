/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents information on the activation status and visibility of a
 * marketable service.
 * 
 */
public class VOServiceActivation implements Serializable {

    private static final long serialVersionUID = -3618895152168081871L;

    private VOService service;
    private List<VOCatalogEntry> catalogEntries = new ArrayList<VOCatalogEntry>();
    private boolean active;

    /**
     * Retrieves the service whose activation status or visibility is to be
     * updated.
     * 
     * @return the service
     */
    public VOService getService() {
        return service;
    }

    /**
     * Specifies the service whose activation status or visibility is to be
     * updated.
     * 
     * @param service
     *            the service
     */
    public void setService(VOService service) {
        this.service = service;
    }

    /**
     * Returns a list of all catalog entries, which defines the visibility of
     * the service on each marketplace.
     * 
     * @return the list of catalog entries
     */
    public List<VOCatalogEntry> getCatalogEntries() {
        return catalogEntries;
    }

    /**
     * Sets a list of catalog entries, which defines the visibility of the
     * service on each marketplace.
     * 
     * @param catalogEntries
     *            the list of catalog entries
     */
    public void setCatalogEntries(List<VOCatalogEntry> catalogEntries) {
        this.catalogEntries = catalogEntries;
    }

    /**
     * Returns whether the service was activated.
     * 
     * @return <code>true</code> if the service is active, <code>false</code>
     *         otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Specifies whether the service was activated.
     * 
     * @param active
     *            <code>true</code> if the service is active, <code>false</code>
     *            otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

}
