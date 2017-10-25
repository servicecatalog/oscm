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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * A priced event is the concrete representation of a event defined by a
 * technical product and considered in the price model for a concrete marketing
 * product. It is used to have a concrete mapping of an event id (which is
 * specified by the technical product) and the pricing (which has to be set at
 * the time of the service provisioning).
 * 
 * <p>
 * Priced events can be reused for several price models as long as those belong
 * to marketing products that origin from the same technical product (as the
 * technical product defines the set of supported events).
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
public class PricedEvent extends DomainObjectWithHistory<PricedEventData> {

    private static final long serialVersionUID = -8058350641846433064L;

    @Column(name = "priceModelKey", insertable = false, updatable = false, nullable = false)
    private long priceModelKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priceModelKey")
    private PriceModel priceModel;

    @Column(name = "eventKey", insertable = false, updatable = false, nullable = false)
    private long eventKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventKey")
    private Event event;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pricedEvent", fetch = FetchType.LAZY)
    @OrderBy
    private List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();

    public PricedEvent() {
        super();
        dataContainer = new PricedEventData();
    }

    public long getPriceModelKey() {
        return priceModelKey;
    }

    public void setPriceModelKey(long priceModelKey) {
        this.priceModelKey = priceModelKey;
    }

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public long getEventKey() {
        return eventKey;
    }

    public Event getEvent() {
        return event;
    }

    public void setEventKey(long eventKey) {
        this.eventKey = eventKey;
    }

    public void setEvent(Event event) {
        this.event = event;
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

    /**
     * Creates a copy of this priced event and returns it.
     * 
     * @param owningPriceModel
     *            The price model the created copy will belong to.
     * 
     * @return A copy of this object.
     */
    public PricedEvent copy(PriceModel owningPriceModel) {
        PricedEvent copy = new PricedEvent();
        copy.setDataContainer(new PricedEventData());
        copy.setEventPrice(getEventPrice());
        copy.setEvent(this.getEvent());
        copy.setPriceModel(owningPriceModel);
        // copy stepped prices
        List<SteppedPrice> copiedSteppedPrices = new ArrayList<SteppedPrice>();
        for (SteppedPrice sp : getSteppedPrices()) {
            SteppedPrice spCopy = sp.copy();
            spCopy.setPricedEvent(copy);
            copiedSteppedPrices.add(spCopy);
        }
        copy.setSteppedPrices(copiedSteppedPrices);

        return copy;
    }

    public void setSteppedPrices(List<SteppedPrice> steppedPrices) {
        this.steppedPrices = steppedPrices;
    }

    public List<SteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }
}
