/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-9-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.trackingCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.trackingCode.POTrackingCode;

/**
 * @author Zou
 * 
 */
@ViewScoped
@ManagedBean(name="trackingCodeModel")
public class TrackingCodeModel implements Serializable {

    private static final long serialVersionUID = -9096301215403399277L;

    private boolean initialized = false;

    // chosen options
    private String selectedMarketplace;

    // Select options
    private List<SelectItem> marketplaces = new ArrayList<SelectItem>();

    // model
    POTrackingCode trackingCode;

    // getters and setters
    public void setMarketplaces(List<SelectItem> marketplaces) {
        this.marketplaces = marketplaces;
    }

    public List<SelectItem> getMarketplaces() {
        return marketplaces;
    }

    public POTrackingCode getTrackingCodeObject() {
        return trackingCode;
    }

    public void setTrackingCodeObject(POTrackingCode trackingCode) {
        this.trackingCode = trackingCode;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getSelectedMarketplace() {
        return selectedMarketplace;
    }

    public void setSelectedMarketplace(String selectedMarketplace) {
        this.selectedMarketplace = selectedMarketplace;
    }

    public String getMarketplaceId() {
        return trackingCode.getMarketplaceId();
    }

    public void setMarketplaceId(String marketplaceId) {
        trackingCode.setMarketplaceId(marketplaceId);
    }

    public String getTrackingCodeValue() {
        return trackingCode.getTrackingCode();
    }

    public void setTrackingCodeValue(String trackingCodeValue) {
        trackingCode.setTrackingCode(trackingCodeValue);
    }
}
