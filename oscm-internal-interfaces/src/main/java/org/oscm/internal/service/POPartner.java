/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.service;

import java.io.Serializable;

import org.oscm.internal.pricing.PORevenueShare;

public class POPartner implements Serializable {

    private static final long serialVersionUID = 5826074251310310969L;

    private String organizationId;
    private String name;
    private long key;
    private PORevenueShare revenueShare;
    private boolean selected;

    public POPartner() {
    }

    public POPartner(long key, String organizationId, String name,
            PORevenueShare revenueShare, boolean selected) {
        this.key = key;
        this.organizationId = organizationId;
        this.name = name;
        this.revenueShare = revenueShare;
        this.selected = selected;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public PORevenueShare getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(PORevenueShare revenueShare) {
        this.revenueShare = revenueShare;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
