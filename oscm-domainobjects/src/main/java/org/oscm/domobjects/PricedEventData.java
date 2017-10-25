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

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container to hold the information on each priced event.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class PricedEventData extends DomainDataContainer {

    private static final long serialVersionUID = 5573413296309147470L;

    /**
     * The price that has to be charged whenever this event is received.
     */
    @Column(nullable = false)
    private BigDecimal eventPrice = BigDecimal.ZERO;

    public BigDecimal getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(BigDecimal eventPrice) {
        this.eventPrice = eventPrice;
    }

}
