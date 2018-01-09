/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 16.06.2009                                                      
 *                                                                              
 *  Completion Time: 04.08.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.TTTConverter;
import org.oscm.domobjects.converters.TimerTConverter;
import org.oscm.types.enumtypes.TimerType;

/**
 * JPA managed entity to represent information on the processing of timers.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class TimerProcessingData extends DomainDataContainer {

    private static final long serialVersionUID = -3431823317877481679L;

    /**
     * The name of the cluster node that processed the timer.
     */
    @Column(nullable = false)
    private String nodeName;

    /**
     * The timer type.
     */
    @Column(nullable = false, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private TimerType timerType;

    /**
     * The time the timer processing was started.
     */
    @Column(nullable = false)
    private long startTime;

    /**
     * The time the processing took.
     */
    private long duration;

    /**
     * Flag to indicate the result of the timer processing.
     */
    @Column(nullable = false)
    private boolean success;

    /**
     * Value that is startTime divided by a certain value, used to prevent
     * several nodes from starting the handling of tasks for the same timer
     * type. The divisor should be chosen reasonably.
     */
    @Column(nullable = false)
    private long startTimeMutex;

    public String getNodeName() {
        return nodeName;
    }

    public TimerType getTimerType() {
        return timerType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setTimerType(TimerType type) {
        this.timerType = type;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getStartTimeMutex() {
        return startTimeMutex;
    }

    public void setStartTimeMutex(long startTimeMutex) {
        this.startTimeMutex = startTimeMutex;
    }

}
