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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;

@Entity
@NamedQuery(name = "CatalogEntryHistory.findByObject", query = "select c from CatalogEntryHistory c where c.objKey=:objKey order by objversion, modDate")
public class CatalogEntryHistory extends DomainHistoryObject<CatalogEntryData> {

    private static final long serialVersionUID = -1323706337608719480L;

    /**
     * Reference to the original Product domain object
     */
    private Long productObjKey;

    /**
     * Reference to the marketplace domain object
     */
    private Long marketplaceObjKey;

    @Column(nullable = true)
    private Long brokerPriceModelObjKey;

    @Column(nullable = true)
    private Long resellerPriceModelObjKey;

    @Column(nullable = true)
    private Long operatorPriceModelObjKey;

    public CatalogEntryHistory() {
        dataContainer = new CatalogEntryData();
    }

    public CatalogEntryHistory(CatalogEntry entry) {
        super(entry);
        if (entry.getProduct() != null) {
            setProductObjKey(Long.valueOf(entry.getProduct().getKey()));
        }
        if (entry.getMarketplace() != null) {
            setMarketplaceObjKey(Long.valueOf(entry.getMarketplace().getKey()));
        }

        if (entry.getBrokerPriceModel() != null) {
            setBrokerPriceModelObjKey(Long.valueOf(entry.getBrokerPriceModel()
                    .getKey()));
        }

        if (entry.getResellerPriceModel() != null) {
            setResellerPriceModelObjKey(Long.valueOf(entry
                    .getResellerPriceModel().getKey()));
        }

        if (entry.getOperatorPriceModel() != null) {
            setOperatorPriceModelObjKey(Long.valueOf(entry
                    .getOperatorPriceModel().getKey()));
        }
    }

    public void setProductObjKey(Long productObjKey) {
        this.productObjKey = productObjKey;
    }

    public Long getProductObjKey() {
        return productObjKey;
    }

    public void setMarketplaceObjKey(Long marketplaceObjKey) {
        this.marketplaceObjKey = marketplaceObjKey;
    }

    public Long getMarketplaceObjKey() {
        return marketplaceObjKey;
    }

    public Long getBrokerPriceModelObjKey() {
        return brokerPriceModelObjKey;
    }

    public void setBrokerPriceModelObjKey(Long brokerPriceModelObjKey) {
        this.brokerPriceModelObjKey = brokerPriceModelObjKey;
    }

    public Long getResellerPriceModelObjKey() {
        return resellerPriceModelObjKey;
    }

    public void setResellerPriceModelObjKey(Long resellerPriceModelObjKey) {
        this.resellerPriceModelObjKey = resellerPriceModelObjKey;
    }

    public Long getOperatorPriceModelObjKey() {
        return operatorPriceModelObjKey;
    }

    public void setOperatorPriceModelObjKey(Long operatorPriceModelObjKey) {
        this.operatorPriceModelObjKey = operatorPriceModelObjKey;
    }

}
