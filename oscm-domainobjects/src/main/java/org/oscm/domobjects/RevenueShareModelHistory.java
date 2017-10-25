/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: farmaki                                                     
 *                                                                              
 *  Creation Date: 16.07.2012                                                    
 *                                                                              
 *  Completion Time: 16.07.2012                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The history object for the revenue share model domain object.
 * 
 * @author farmaki
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "RevenueShareModelHistory.findByObject", query = "select c from RevenueShareModelHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "RevenueShareModelHistory.findBrokerRevenueSharePercentage", query = "SELECT rsmh FROM RevenueShareModelHistory rsmh, CatalogEntryHistory ceh WHERE rsmh.objKey = ceh.brokerPriceModelObjKey AND ceh.marketplaceObjKey IS NOT NULL AND ceh.productObjKey= :productObjKey AND rsmh.modDate <= :modDate ORDER BY rsmh.objVersion DESC"),
        @NamedQuery(name = "RevenueShareModelHistory.findResellerRevenueSharePercentage", query = "SELECT rsmh FROM RevenueShareModelHistory rsmh, CatalogEntryHistory ceh WHERE rsmh.objKey = ceh.resellerPriceModelObjKey AND ceh.marketplaceObjKey IS NOT NULL AND ceh.productObjKey= :productObjKey AND rsmh.modDate <= :modDate ORDER BY rsmh.objVersion DESC"),
        @NamedQuery(name = "RevenueShareModelHistory.findOperatorRevenueSharePercentage", query = "SELECT rsmh FROM RevenueShareModelHistory rsmh, CatalogEntryHistory ceh WHERE rsmh.objKey = ceh.operatorPriceModelObjKey AND ceh.productObjKey= :productObjKey AND rsmh.modDate <= :modDate ORDER BY rsmh.objVersion DESC"),
        @NamedQuery(name = "RevenueShareModelHistory.findMarketplaceRevenueSharePercentage", query = "SELECT rsmh FROM RevenueShareModelHistory rsmh, MarketplaceHistory mph WHERE rsmh.objKey = mph.priceModelObjKey AND mph.objKey = :mpKey AND rsmh.modDate <= :modDate ORDER BY rsmh.objVersion DESC") })
public class RevenueShareModelHistory extends
        DomainHistoryObject<RevenueShareModelData> {

    private static final long serialVersionUID = -1054217923469772155L;

    public RevenueShareModelHistory() {
        dataContainer = new RevenueShareModelData();
    }

    public RevenueShareModelHistory(RevenueShareModel revenueShareModel) {
        super(revenueShareModel);
    }
}
