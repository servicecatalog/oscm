/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of PricedEvent, used for auditing. Will be automatically
 * created during persist, save or remove operations (if performed via
 * DataManager)
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = "PricedEventHistory.findByObject", query = "select c from PricedEventHistory c where c.objKey=:objKey order by objversion"),       
        @NamedQuery(name = "PricedEventHistory.findEventsByPriceModelKeyBeforePeriodEnd", query = "SELECT c, evt.dataContainer.eventIdentifier FROM PricedEventHistory c, EventHistory evt WHERE c.priceModelObjKey=:priceModelKey AND c.eventObjKey = evt.objKey AND  c.modDate < :modDate ORDER BY c.objKey ASC, c.objVersion DESC, c.modDate DESC")})
public class PricedEventHistory extends
        DomainHistoryObject<PricedEventData> {

    private static final long serialVersionUID = 2625039778267658586L;

    /**
     * Field to keep the old dependency to the parent object (referential
     * constraint). Although the parent object might be deleted, the former key
     * value must be stored to reconstruct the situation for any point in time
     * via history.
     */
    private long priceModelObjKey;

    /**
     * Field to keep dependency to parent object, just like given in
     * {@link #priceModelObjKey}.
     */
    private long eventObjKey;

    public PricedEventHistory() {
        dataContainer = new PricedEventData();
    }

    /**
     * Constructs PricedEventHistory from a PricedEvent domain object
     * 
     * @param c
     *            The PricedEvent
     */
    public PricedEventHistory(PricedEvent c) {
        super(c);
        if (c.getPriceModel() != null) {
            setPriceModelObjKey(c.getPriceModel().getKey());
        }
        if (c.getEvent() != null) {
            setEventObjKey(c.getEvent().getKey());
        }
    }

    public long getPriceModelObjKey() {
        return priceModelObjKey;
    }

    public void setPriceModelObjKey(long priceModelObjKey) {
        this.priceModelObjKey = priceModelObjKey;
    }

    public long getEventObjKey() {
        return eventObjKey;
    }

    public void setEventObjKey(long eventObjKey) {
        this.eventObjKey = eventObjKey;
    }

    /**
     * Refer to {@link PricedEventData#eventPrice}
     */
    public BigDecimal getEventPrice() {
        return dataContainer.getEventPrice();
    }

    /**
     * Refer to {@link PricedEventData#eventPrice}
     */
    public void setEventPrice(BigDecimal eventPrice) {
        dataContainer.setEventPrice(eventPrice);
    }

}
