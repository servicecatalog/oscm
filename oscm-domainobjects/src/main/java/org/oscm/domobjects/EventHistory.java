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

import javax.persistence.Entity;
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
@NamedQuery(name = "EventHistory.findByObject", query = "select c from EventHistory c where c.objKey=:objKey order by objversion")
public class EventHistory extends DomainHistoryObject<EventData> {

    private static final long serialVersionUID = -1264572952008443314L;

    private Long technicalProductObjKey;

    public EventHistory() {
        dataContainer = new EventData();
    }

    /**
     * Constructs EventHistory from an Event domain object
     * 
     * @param c
     *            The Event
     */
    public EventHistory(Event c) {
        super(c);
        if (c.getTechnicalProduct() != null) {
            this.technicalProductObjKey = Long.valueOf(c.getTechnicalProduct()
                    .getKey());
        }
    }

    public Long getTechnicalProductObjKey() {
        return technicalProductObjKey;
    }

    public void setTechnicalProductObjKey(Long technicalProductObjKey) {
        this.technicalProductObjKey = technicalProductObjKey;
    }

}
