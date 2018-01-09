/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                             
 *                                                                              
 *  Creation Date: 14.06.2010                                                      
 *                                                                              
 *  Completion Time: 15.06.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.TPINConverter;
import org.oscm.domobjects.converters.TTConverter;
import org.oscm.domobjects.converters.TTTConverter;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * JPA managed entity representing the trigger definition data.
 * 
 * @author pock
 * 
 */
@Embeddable
public class TriggerDefinitionData extends DomainDataContainer {

    private static final long serialVersionUID = 8446424776529785581L;

    /**
     * The name of the action (e.g. SUBSCRIBE_TO_SERVICE) for which the trigger
     * is defined.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerType type;

    /**
     * If the type of the trigger is WEB_SERVICE this is the URL of WSDL.
     */
    private String target;

    /**
     * The type of the trigger (e.g. WEB_SERVICE or MAIL).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerTargetType targetType;

    /**
     * The identifier of the platform user related to the session.
     */
    @Column(nullable = false)
    private boolean suspendProcess;

    @Column(nullable = false)
    private String name;

    public TriggerType getType() {
        return type;
    }

    public void setType(TriggerType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public TriggerTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TriggerTargetType targetType) {
        this.targetType = targetType;
    }

    public boolean isSuspendProcess() {
        return suspendProcess;
    }

    public void setSuspendProcess(boolean suspendProcess) {
        this.suspendProcess = suspendProcess;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
