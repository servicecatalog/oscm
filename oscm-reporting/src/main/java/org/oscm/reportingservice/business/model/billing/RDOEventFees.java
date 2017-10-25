/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import java.util.List;

import org.oscm.reportingservice.business.model.RDO;

/**
 * A report data object for the event fees.
 * 
 * @author farmaki
 */
public class RDOEventFees extends RDO {

    private static final long serialVersionUID = 5733059573353033942L;

    private List<RDOEvent> events;
    private String subtotalAmount;

    /** hide, if no rows at all are present */
    private boolean hideEventFees = false;

    public String getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(String subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public List<RDOEvent> getEvents() {
        return events;
    }

    public void setEvents(List<RDOEvent> events) {
        this.events = events;
    }

    public RDOEvent getEvent(String eventId) {
        RDOEvent result = null;
        for (RDOEvent e : getEvents()) {
            if (eventId.equals(e.getId())) {
                result = e;
                break;
            }
        }
        return result;
    }

    public boolean isHideEventFees() {
        return hideEventFees;
    }

    public void setHideEventFees(boolean hideEventFees) {
        this.hideEventFees = hideEventFees;
    }

}
