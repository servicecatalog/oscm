/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                    
 *                                                                              
 *  Creation Date: 27.10.2011                                                      
 *                                                                              
 *  Completion Time: 27.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.local;

import org.oscm.domobjects.TriggerProcess;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * @author kulle
 * 
 */
public class TriggerProcessMessageData {

    private TriggerProcess trigger;
    private TriggerMessage messageData;

    public TriggerProcessMessageData(TriggerProcess trigger,
            TriggerMessage messageData) {
        this.trigger = trigger;
        this.messageData = messageData;
    }

    public TriggerProcess getTrigger() {
        return trigger;
    }

    public void setTrigger(TriggerProcess trigger) {
        this.trigger = trigger;
    }

    public TriggerType getTriggerType() {
        if (messageData == null) {
            return null;
        }
        return messageData.getTriggerType();
    }

    public TriggerProcessParameterName getParameterName() {
        if (messageData == null) {
            return null;
        }
        return messageData.getParameterName();
    }

    public TriggerMessage getMessageData() {
        return messageData;
    }

}
