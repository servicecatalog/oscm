/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 14, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.math.BigDecimal;

import org.oscm.string.Strings;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.vo.VOMarketplace;

/**
 * @author tang
 * 
 */
public class Marketplace extends NewMarketplace {

    private boolean organizationSelectVisible = false;
    private boolean tenantSelectVisible = false;
    private boolean editDisabled = true;
    private boolean propertiesDisabled = true;
    private boolean open;
    private String marketplaceId;
    private String name;
    private String originalOrgId;
    private long key;
    private int version;
    private boolean revenueSharesReadOnly = true;
    private PORevenueShare marketplaceRevenueShare;
    private PORevenueShare resellerRevenueShare;
    private PORevenueShare brokerRevenueShare;

    public Marketplace() {
    }

    public Marketplace(VOMarketplace mp) {
        marketplaceId = mp.getMarketplaceId();
        key = mp.getKey();
        version = mp.getVersion();
        name = mp.getName();
        open = mp.isOpen();
    }

    public boolean isOrganizationSelectVisible() {
        return organizationSelectVisible;
    }

    public void setOrganizationSelectVisible(boolean organizationSelectVisible) {
        this.organizationSelectVisible = organizationSelectVisible;
    }

    public boolean isEditDisabled() {
        return editDisabled;
    }

    public void setEditDisabled(boolean editDisabled) {
        this.editDisabled = editDisabled;
    }

    public boolean isPropertiesDisabled() {
        return propertiesDisabled;
    }

    public void setPropertiesDisabled(boolean propertiesDisabled) {
        this.propertiesDisabled = propertiesDisabled;
    }

    public String getMarketplaceId() {
        return marketplaceId;
    }

    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setOriginalOrgId(String originalOrgId) {
        this.originalOrgId = originalOrgId;
    }

    public String getOriginalOrgId() {
        return originalOrgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Checks if the assigned organization has been changed by comparing
     * {@link #getOwningOrganizationId()} and {@link #getOriginalOrgId()}.
     * 
     * @return <code>true</code> if the assigned organization has been changed,
     *         <code>false</code> otherwise
     */
    public boolean assignedOrgChanged() {
        if (Strings.isEmpty(originalOrgId)) {
            return !Strings.isEmpty(getOwningOrganizationId());
        }
        return !originalOrgId.equals(getOwningOrganizationId());
    }

    public String getDisplayName() {
        return getName() + "(" + getMarketplaceId() + ")";
    }

    public boolean isRevenueSharesReadOnly() {
        return revenueSharesReadOnly;
    }

    public void setRevenueSharesReadOnly(boolean revenueSharesReadOnly) {
        this.revenueSharesReadOnly = revenueSharesReadOnly;
    }

    public BigDecimal getMarketplaceRevenueShare() {
        if (marketplaceRevenueShare == null) {
            return null;
        }
        return marketplaceRevenueShare.getRevenueShare();
    }

    public void setMarketplaceRevenueShare(BigDecimal revenueShare) {
        if (marketplaceRevenueShare == null) {
            setMarketplaceRevenueShareObject(new PORevenueShare());
        }
        marketplaceRevenueShare.setRevenueShare(revenueShare);
    }

    public PORevenueShare getMarketplaceRevenueShareObject() {
        return marketplaceRevenueShare;
    }

    public void setMarketplaceRevenueShareObject(PORevenueShare revenueShare) {
        marketplaceRevenueShare = revenueShare;
    }

    public boolean isMarketplaceRevenueShareVisible() {
        return isRevenueSharesReadOnly() && marketplaceRevenueShare != null;
    }

    public BigDecimal getResellerRevenueShare() {
        if (resellerRevenueShare == null) {
            return null;
        }
        return resellerRevenueShare.getRevenueShare();
    }

    public void setResellerRevenueShare(BigDecimal revenueShare) {
        if (resellerRevenueShare == null) {
            setResellerRevenueShareObject(new PORevenueShare());
        }
        resellerRevenueShare.setRevenueShare(revenueShare);
    }

    public PORevenueShare getResellerRevenueShareObject() {
        return resellerRevenueShare;
    }

    public void setResellerRevenueShareObject(PORevenueShare revenueShare) {
        resellerRevenueShare = revenueShare;
    }

    public boolean isResellerRevenueShareVisible() {
        return isRevenueSharesReadOnly() && resellerRevenueShare != null;
    }

    public BigDecimal getBrokerRevenueShare() {
        if (brokerRevenueShare == null) {
            return null;
        }
        return brokerRevenueShare.getRevenueShare();
    }

    public void setBrokerRevenueShare(BigDecimal revenueShare) {
        if (brokerRevenueShare == null) {
            setBrokerRevenueShareObject(new PORevenueShare());
        }
        brokerRevenueShare.setRevenueShare(revenueShare);
    }

    public PORevenueShare getBrokerRevenueShareObject() {
        return brokerRevenueShare;
    }

    public void setBrokerRevenueShareObject(PORevenueShare revenueShare) {
        brokerRevenueShare = revenueShare;
    }

    public boolean isBrokerRevenueShareVisible() {
        return isRevenueSharesReadOnly() && brokerRevenueShare != null;
    }

    public boolean isTenantSelectVisible() {
        return tenantSelectVisible;
    }

    public void setTenantSelectVisible(boolean tenantSelectVisible) {
        this.tenantSelectVisible = tenantSelectVisible;
    }

}
