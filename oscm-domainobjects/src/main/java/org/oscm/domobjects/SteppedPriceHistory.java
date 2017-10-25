/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 13.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * The history object for a stepped price. Finders
 * SteppedPriceHistory.getForPMKeyAndEndDate,
 * SteppedPriceHistory.getForEventKeyAndEndDate, return only last version of
 * object in asked period.
 * 
 * @author weiser
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "SteppedPriceHistory.findByObject", query = "select c from SteppedPriceHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "SteppedPriceHistory.getForPMKeyAndEndDate", query = "SELECT sph FROM SteppedPriceHistory sph WHERE sph.priceModelObjKey = :pmObjKey AND sph.modDate <= :modDate AND sph.objVersion IN (SELECT MAX(sphSub.objVersion) FROM SteppedPriceHistory sphSub WHERE sph.objKey = sphSub.objKey AND sphSub.modDate <= :modDate) ORDER BY sph.dataContainer.limit ASC NULLS LAST"),
        @NamedQuery(name = "SteppedPriceHistory.getForEventKeyAndEndDate", query = "SELECT sph FROM SteppedPriceHistory sph WHERE sph.pricedEventObjKey = :evntObjKey AND sph.modDate <= :modDate AND sph.objVersion IN (SELECT MAX(sphSub.objVersion) FROM SteppedPriceHistory sphSub WHERE sph.objKey = sphSub.objKey AND sphSub.modDate <= :modDate) ORDER BY sph.dataContainer.limit ASC NULLS LAST"),
        @NamedQuery(name = "SteppedPriceHistory.getForParameterKeyAndEndDate", query = "SELECT sph FROM SteppedPriceHistory sph WHERE sph.pricedParameterObjKey = :prmtrObjKey AND sph.objVersion = (SELECT MAX(sphSub.objVersion) FROM SteppedPriceHistory sphSub WHERE sph.objKey = sphSub.objKey AND sphSub.modDate <= :modDate) ORDER BY sph.dataContainer.limit ASC NULLS LAST") })
public class SteppedPriceHistory extends DomainHistoryObject<SteppedPriceData> {

    private static final long serialVersionUID = -5700832199522804276L;

    private Long priceModelObjKey;

    private Long pricedEventObjKey;

    private Long pricedParameterObjKey;

    public SteppedPriceHistory() {
        super();
        setDataContainer(new SteppedPriceData());
    }

    public SteppedPriceHistory(SteppedPrice domobj) {
        super(domobj);
        if (domobj.getPricedEvent() != null) {
            setPricedEventObjKey(Long.valueOf(domobj.getPricedEvent().getKey()));
        }
        if (domobj.getPricedParameter() != null) {
            setPricedParameterObjKey(Long.valueOf(domobj.getPricedParameter()
                    .getKey()));
        }
        if (domobj.getPriceModel() != null) {
            setPriceModelObjKey(Long.valueOf(domobj.getPriceModel().getKey()));
        }
    }

    public Long getPriceModelObjKey() {
        return priceModelObjKey;
    }

    public void setPriceModelObjKey(Long priceModelObjKey) {
        this.priceModelObjKey = priceModelObjKey;
    }

    public Long getPricedEventObjKey() {
        return pricedEventObjKey;
    }

    public void setPricedEventObjKey(Long pricedEventObjKey) {
        this.pricedEventObjKey = pricedEventObjKey;
    }

    public Long getPricedParameterObjKey() {
        return pricedParameterObjKey;
    }

    public void setPricedParameterObjKey(Long pricedParameterValueObjKey) {
        this.pricedParameterObjKey = pricedParameterValueObjKey;
    }

    public Long getLimit() {
        return dataContainer.getLimit();
    }

    public BigDecimal getPrice() {
        return dataContainer.getPrice();
    }

    public BigDecimal getAdditionalPrice() {
        return dataContainer.getAdditionalPrice();
    }

    public long getFreeEntityCount() {
        return dataContainer.getFreeEntityCount();
    }
}
