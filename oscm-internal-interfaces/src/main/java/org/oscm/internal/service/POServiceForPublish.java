/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: afschar
 *                                                                                                                                                            
 *******************************************************************************/

package org.oscm.internal.service;

import java.io.Serializable;

import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author afschar
 * 
 */
public class POServiceForPublish implements Serializable {

    private static final long serialVersionUID = 7967519382425802915L;

    private VOServiceDetails service = new VOServiceDetails();
    private VOCatalogEntry catalogEntry = new VOCatalogEntry();
    private boolean partOfUpgradePath;

    public VOServiceDetails getService() {
        return service;
    }

    public void setService(VOServiceDetails service) {
        this.service = service == null ? new VOServiceDetails() : service;
    }

    public VOCatalogEntry getCatalogEntry() {
        return catalogEntry;
    }

    public void setCatalogEntry(VOCatalogEntry catalogEntry) {
        this.catalogEntry = catalogEntry == null ? new VOCatalogEntry()
                : catalogEntry;
    }

    public String getMarketplaceId() {
        if (catalogEntry.getMarketplace() == null) {
            return null;
        }
        return catalogEntry.getMarketplace().getMarketplaceId();
    }

    public void setMarketplaceId(String marketplaceId) {
        if (catalogEntry.getMarketplace() == null) {
            catalogEntry.setMarketplace(new VOMarketplace());
        }
        catalogEntry.getMarketplace().setMarketplaceId(marketplaceId);
    }

    public void setPartOfUpgradePath(boolean partOfUpgradePath) {
        this.partOfUpgradePath = partOfUpgradePath;
    }

    public boolean isPartOfUpgradePath() {
        return partOfUpgradePath;
    }
}
