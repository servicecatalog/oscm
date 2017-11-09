/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 22.06.2010                                                     
                                          
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.model;

import java.util.Date;
import java.util.List;

import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.vo.VOTriggerDefinition;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUser;

/**
 * Wrapper Class for VOTriggerProcess which holds additional view attributes.
 * 
 */
public class TriggerProcess {

    private static final String KEY_BASE = TriggerType.class.getSimpleName()
            + ".";

    private boolean selected;
    private VOTriggerProcess voTriggerProcess;

    public TriggerProcess(VOTriggerProcess voTriggerProcess) {
        this.voTriggerProcess = voTriggerProcess;
    }

    public VOTriggerProcess getVOTriggerProcess() {
        return voTriggerProcess;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /*
     * Delegate Methods
     */

    public long getKey() {
        return voTriggerProcess.getKey();
    }

    public Date getActivationDate() {
        return new Date(voTriggerProcess.getActivationDate());
    }

    public List<String> getTargetNames() {
        return voTriggerProcess.getTargetNames();
    }

    public String getReason() {
        return voTriggerProcess.getReason();
    }

    public TriggerProcessStatus getStatus() {
        return voTriggerProcess.getStatus();
    }

    public VOTriggerDefinition getTriggerDefinition() {
        return voTriggerProcess.getTriggerDefinition();
    }

    public VOUser getUser() {
        return voTriggerProcess.getUser();
    }

    public int getVersion() {
        return voTriggerProcess.getVersion();
    }

    public void setActivationDate(Date activationDate) {
        voTriggerProcess.setActivationDate(activationDate.getTime());
    }

    public void setReason(String reason) {
        voTriggerProcess.setReason(reason);
    }

    public void setStatus(TriggerProcessStatus status) {
        voTriggerProcess.setStatus(status);
    }

    public void setTriggerDefinition(VOTriggerDefinition triggerDefinition) {
        voTriggerProcess.setTriggerDefinition(triggerDefinition);
    }

    public void setUser(VOUser user) {
        voTriggerProcess.setUser(user);
    }

    public String toString() {
        return voTriggerProcess.toString();
    }

    public String getMessageKey() {
        String key = KEY_BASE + getTriggerDefinition().getType().name();
        String param = getVOTriggerProcess().getParameter();
        if (param != null) {
            key += ("." + param);
        }
        return key;
    }
}
