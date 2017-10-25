/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.trackingCode;

import java.io.Serializable;

import org.oscm.internal.base.BasePO;

public class POTrackingCode extends BasePO implements Serializable {

    private static final long serialVersionUID = -4560643646776054787L;

    private String marketplaceId;
    private String trackingCode;

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }
}
