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

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * History object for PriceModels
 * 
 * @author schmid
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "PriceModelHistory.findByObject", query = "SELECT c FROM PriceModelHistory c WHERE c.objKey=:objKey ORDER BY objversion"),
        @NamedQuery(name = "PriceModelHistory.findByObjectAndModType", query = "SELECT c FROM PriceModelHistory c WHERE c.objKey=:objKey AND c.modType = :modType ORDER BY objversion"),
        @NamedQuery(name = "PriceModelHistory.findByObjectAndProvisioningCompleted", query = "SELECT c FROM PriceModelHistory c WHERE c.objKey=:objKey AND c.dataContainer.provisioningCompleted = :provisioningCompleted ORDER BY objVersion"),
        @NamedQuery(name = "PriceModelHistory.findChargebleByKeyDescVersion", query = "SELECT c FROM PriceModelHistory c WHERE c.objKey=:objKey AND c.dataContainer.type <> :invalidChargeType AND c.modDate < :modDate ORDER BY objVersion DESC, modDate DESC"),
        @NamedQuery(name = "PriceModelHistory.findByKeyDescVersion", query = "SELECT c FROM PriceModelHistory c WHERE c.objKey=:objKey AND c.modDate < :modDate ORDER BY objVersion DESC, modDate DESC"),
        @NamedQuery(name = "PriceModelHistory.findBySubscriptionHistory", query = "SELECT pmh FROM PriceModelHistory pmh WHERE pmh.modDate <= :modDate AND pmh.productObjKey IN (SELECT DISTINCT sh.productObjKey FROM SubscriptionHistory sh WHERE sh.objKey = :subcriptionObjKey AND sh.modDate <= :modDate) ORDER BY objVersion DESC, modDate DESC"),
        @NamedQuery(name = "PriceModelHistory.findLatestBySubscriptionHistory", query = "SELECT pmh FROM PriceModelHistory pmh WHERE pmh.modDate <= :modDate AND pmh.productObjKey = :prdObjKey AND pmh.objVersion = (SELECT MAX(tmp.objVersion) FROM PriceModelHistory tmp WHERE tmp.objKey=pmh.objKey and tmp.modDate <= :modDate)"),
        @NamedQuery(name = "PriceModelHistory.getPriceModelForOrganizationKey", query = "SELECT COUNT(p) FROM SubscriptionHistory subHist, PriceModelHistory p, ProductHistory prodHist WHERE subHist.dataContainer.activationDate IS NOT NULL AND subHist.organizationObjKey=:organizationKey AND subHist.productObjKey = prodHist.objKey AND prodHist.priceModelObjKey = p.objKey AND p.dataContainer.type <> org.oscm.internal.types.enumtypes.PriceModelType.FREE_OF_CHARGE") })
public class PriceModelHistory extends DomainHistoryObject<PriceModelData> {

    private static final long serialVersionUID = 1L;

    private long productObjKey;

    private Long currencyObjKey;

    public PriceModelHistory() {
        dataContainer = new PriceModelData();
    }

    public PriceModelHistory(PriceModel domObj) {
        super(domObj);
        if (domObj.getProduct() != null) {
            setProductObjKey(domObj.getProduct().getKey());
        }
        if (domObj.getCurrency() != null) {
            setCurrencyObjKey(domObj.getCurrency().getKey());
        }
    }

    @Override
    public PriceModelData getDataContainer() {
        return dataContainer;
    }

    public long getProductObjKey() {
        return productObjKey;
    }

    public void setProductObjKey(long productObjKey) {
        this.productObjKey = productObjKey;
    }

    public PriceModelType getType() {
        return dataContainer.getType();
    }

    public void setType(PriceModelType type) {
        dataContainer.setType(type);
    }

    public boolean isChargeable() {
        return dataContainer.getType() != PriceModelType.FREE_OF_CHARGE &&
                dataContainer.getType() != PriceModelType.UNKNOWN;
    }

    public PricingPeriod getPeriod() {
        return dataContainer.getPeriod();
    }

    public BigDecimal getPricePerPeriod() {
        return dataContainer.getPricePerPeriod();
    }

    public BigDecimal getPricePerUserAssignment() {
        return dataContainer.getPricePerUserAssignment();
    }

    public void setPeriod(PricingPeriod period) {
        dataContainer.setPeriod(period);
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        dataContainer.setPricePerPeriod(pricePerPeriod);
    }

    public void setPricePerUserAssignment(BigDecimal pricePerUser) {
        dataContainer.setPricePerUserAssignment(pricePerUser);
    }

    public Long getCurrencyObjKey() {
        return currencyObjKey;
    }

    public void setCurrencyObjKey(Long currencyObjKey) {
        this.currencyObjKey = currencyObjKey;
    }

    public BigDecimal getOneTimeFee() {
        return dataContainer.getOneTimeFee();
    }

    public void setOneTimeFee(BigDecimal oneTimeFee) {
        dataContainer.setOneTimeFee(oneTimeFee);
    }

    public int getFreePeriod() {
        return dataContainer.getFreePeriod();
    }

    public void setFreePeriod(int freePeriod) {
        dataContainer.setFreePeriod(freePeriod);
    }

    public boolean isProvisioningCompleted() {
        return dataContainer.isProvisioningCompleted();
    }

    public void setProvisioningCompleted(boolean completed) {
        dataContainer.setProvisioningCompleted(completed);
    }
}
