/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.components;

import org.oscm.internal.base.BasePO;

/**
 * Represents a presentation object for marketplace selectors (displayName and
 * marketplaceId)
 */
public class POMarketplace extends BasePO {

    private static final long serialVersionUID = -2545041626147506691L;

    String marketplaceId;
    String displayName;

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public POMarketplace(String mId, String displayName) {
        super();
        this.displayName = displayName;
        this.marketplaceId = mId;
    }

}
