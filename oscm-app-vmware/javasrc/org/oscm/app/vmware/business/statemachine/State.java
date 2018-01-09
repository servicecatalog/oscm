/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class State {

    private String id;
    private String action;
    private String timeout;
    private List<Event> events;

    @XmlElement(name = "event")
    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public String getAction() {
        return action;
    }

    @XmlAttribute
    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(String id) {
        this.id = id;
    }

    public String getTimeout() {
        return timeout;
    }

    @XmlAttribute(required = false)
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

}