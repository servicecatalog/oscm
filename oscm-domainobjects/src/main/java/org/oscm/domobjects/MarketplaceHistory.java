/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History object for marketplace
 * 
 * @author pock
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "MarketplaceHistory.findByObject", query = "select c from MarketplaceHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "MarketplaceHistory.findBySubscriptionKey", query = "SELECT mp FROM SubscriptionHistory sub, MarketplaceHistory mp WHERE sub.objKey = :subscriptionKey AND sub.marketplaceObjKey = mp.objKey AND mp.modDate < :modDate ORDER BY mp.modDate DESC"),
        @NamedQuery(name = "MarketplaceHistory.findMarketplaceKeys", query = "SELECT mp.objKey FROM MarketplaceHistory mp  WHERE mp.modDate < :modDate AND mp.organizationObjKey = :mpOwnerKey GROUP BY mp.objKey"),
        @NamedQuery(name = "MarketplaceHistory.findWithinPeriod", query = "SELECT mp FROM MarketplaceHistory mp WHERE mp.objKey=:mpKey AND mp.modDate<:modDate ORDER BY mp.objVersion DESC") })
public class MarketplaceHistory extends DomainHistoryObject<MarketplaceData> {

    @Column(nullable = false)
    private long organizationObjKey;

    @Column(nullable = false)
    private long priceModelObjKey;

    @Column(nullable = false)
    private long brokerPriceModelObjKey;

    @Column(nullable = false)
    private long resellerPriceModelObjKey;

    private static final long serialVersionUID = 7047108191422227284L;

    public MarketplaceHistory() {
        dataContainer = new MarketplaceData();
    }

    public MarketplaceHistory(Marketplace domObj) {
        super(domObj);
        if (domObj.getOrganization() != null) {
            setOrganizationObjKey(domObj.getOrganization().getKey());
        }

        // Set the price models tkeys.
        if (domObj.getPriceModel() != null) {
            setPriceModelObjKey(domObj.getPriceModel().getKey());
        }

        if (domObj.getBrokerPriceModel() != null) {
            setBrokerPriceModelObjKey(domObj.getBrokerPriceModel().getKey());
        }

        if (domObj.getResellerPriceModel() != null) {
            setResellerPriceModelObjKey(domObj.getResellerPriceModel().getKey());
        }
    }

    public long getPriceModelObjKey() {
        return priceModelObjKey;
    }

    public void setPriceModelObjKey(long priceModelObjKey) {
        this.priceModelObjKey = priceModelObjKey;
    }

    public long getBrokerPriceModelObjKey() {
        return brokerPriceModelObjKey;
    }

    public void setBrokerPriceModelObjKey(long brokerPriceModelObjKey) {
        this.brokerPriceModelObjKey = brokerPriceModelObjKey;
    }

    public long getResellerPriceModelObjKey() {
        return resellerPriceModelObjKey;
    }

    public void setResellerPriceModelObjKey(long resellerPriceModelObjKey) {
        this.resellerPriceModelObjKey = resellerPriceModelObjKey;
    }

    public void setOrganizationObjKey(long organizationObjKey) {
        this.organizationObjKey = organizationObjKey;
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

    public boolean isTaggingEnabled() {
        return dataContainer.isTaggingEnabled();
    }

    public boolean isReviewEnabled() {
        return dataContainer.isReviewEnabled();
    }

    public boolean isSocialBookmarkEnabled() {
        return dataContainer.isSocialBookmarkEnabled();
    }

    public boolean isCategoriesEnabled() {
        return dataContainer.isCategoriesEnabled();
    }

    public void setCategoriesEnabled(boolean categoriesEnabled) {
        dataContainer.setCategoriesEnabled(categoriesEnabled);
    }

    public String getBrandingUrl() {
        return dataContainer.getBrandingUrl();
    }

    public void setBrandingUrl(String brandingUrl) {
        dataContainer.setBrandingUrl(brandingUrl);
    }

    public void setTrackingCode(String trackingCode) {
        dataContainer.setTrackingCode(trackingCode);
    }

    public String getTrackingCode() {
        return dataContainer.getTrackingCode();
    }
}
