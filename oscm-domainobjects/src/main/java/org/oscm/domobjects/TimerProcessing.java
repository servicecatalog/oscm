/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 16.06.2009                                                      
 *                                                                              
 *  Completion Time: 04.08.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.types.enumtypes.TimerType;

/**
 * JPA managed entity to represent information on the processing of timers.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "timerType",
        "startTimeMutex" }))
@NamedQuery(name = "TimerProcessing.findDataForTimeAndTimeframe", query = "SELECT tpd FROM TimerProcessing tpd WHERE tpd.dataContainer.timerType = :timerType AND tpd.dataContainer.startTime > :lowerTimeBound")
public class TimerProcessing extends
        DomainObjectWithVersioning<TimerProcessingData> {

    private static final long serialVersionUID = 5168534678346314905L;

    public TimerProcessing() {
        super();
        dataContainer = new TimerProcessingData();
    }

    public long getDuration() {
        return dataContainer.getDuration();
    }

    public String getNodeName() {
        return dataContainer.getNodeName();
    }

    public long getStartTime() {
        return dataContainer.getStartTime();
    }

    public long getStartTimeMutex() {
        return dataContainer.getStartTimeMutex();
    }

    public TimerType getTimerType() {
        return dataContainer.getTimerType();
    }

    public boolean isSuccess() {
        return dataContainer.isSuccess();
    }

    public void setDuration(long duration) {
        dataContainer.setDuration(duration);
    }

    public void setNodeName(String nodeName) {
        dataContainer.setNodeName(nodeName);
    }

    public void setStartTime(long startTime) {
        dataContainer.setStartTime(startTime);
    }

    public void setStartTimeMutex(long startTimeMutex) {
        dataContainer.setStartTimeMutex(startTimeMutex);
    }

    public void setSuccess(boolean success) {
        dataContainer.setSuccess(success);
    }

    public void setTimerType(TimerType type) {
        dataContainer.setTimerType(type);
    }

}
