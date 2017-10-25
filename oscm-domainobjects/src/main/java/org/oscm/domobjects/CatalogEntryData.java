/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: 26.01.2011                                                      
 *                                                                              
 *  Completion Time: 27.01.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CatalogEntryData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = -5500241188618267764L;

    @Column(nullable = false)
    private boolean anonymousVisible;

    /**
     * Defines if the entry is visible in the catalog.
     */
    @Column(nullable = false)
    private boolean visibleInCatalog;

    public void setAnonymousVisible(boolean anonymousVisible) {
        this.anonymousVisible = anonymousVisible;
    }

    public boolean isAnonymousVisible() {
        return anonymousVisible;
    }

    /**
     * Checks if the entry is visible in the catalog.
     * 
     * @return <code>true</code> if the entry is visible in the catalog,
     *         otherwise <code>false</code>.
     */
    public boolean isVisibleInCatalog() {
        return visibleInCatalog;
    }

    /**
     * Sets the visibility of the entry in the catalog.
     * 
     * @param catalogVisibility
     *            <code>true</code> if the entry should be visible in the
     *            catalog, otherwise <code>false</code>.
     */
    public void setVisibleInCatalog(boolean visibleInCatalog) {
        this.visibleInCatalog = visibleInCatalog;
    }
}
