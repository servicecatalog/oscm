/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-06-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Represents the definition of a trigger for external process control.
 * 
 */
public class VOTriggerDefinition extends BaseVO {

    private static final long serialVersionUID = -8230251340174891037L;

    private transient VOOrganization organization;

    /**
     * The action that activates the trigger.
     */
    private TriggerType type;

    /**
     * The type of the trigger target (e.g. <code>WEB_SERVICE</code>).
     */
    private TriggerTargetType targetType;

    private String target;

    private boolean suspendProcess;

    /**
     * The display name of the trigger definition.
     */
    private String name;

    /**
     * If there are trigger processes exist for current trigger definition.
     */
    private boolean hasTriggerProcess;

    public void setId(Long id) {
        if (id != null) {
            setKey(id.longValue());
        } else {
            setKey(0L);
        }
    }

    /**
     * Returns the action that activates the trigger.
     * 
     * @return the trigger type
     */
    public TriggerType getType() {
        return type;
    }

    /**
     * Sets the action that activates the trigger.
     * 
     * @param type
     *            the trigger type
     */
    public void setType(TriggerType type) {
        this.type = type;
    }

    /**
     * Returns the type of the trigger target, for example,
     * <code>WEB_SERVICE</code>.
     * 
     * @return the target type
     */
    public TriggerTargetType getTargetType() {
        return targetType;
    }

    /**
     * Sets the type of the trigger target, for example,
     * <code>WEB_SERVICE</code>.
     * 
     * @param targetType
     *            the target type
     */
    public void setTargetType(TriggerTargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * Returns the target of the trigger. If the target type is
     * <code>WEB_SERVICE</code>, this is the URL of the WSDL document of the Web
     * service to be called.
     * 
     * @return the trigger target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target of the trigger. If the target type is
     * <code>WEB_SERVICE</code>, this is the URL of the WSDL document of the Web
     * service to be called.
     * 
     * @param target
     *            the trigger target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Returns whether the action that activates the trigger is to be suspended.
     * 
     * @return <code>true</code> if the action is to be suspended,
     *         <code>false</code> otherwise
     */
    public boolean isSuspendProcess() {
        return suspendProcess;
    }

    /**
     * Specifies whether the action that activates the trigger is to be
     * suspended.
     * 
     * @param suspendProcess
     *            <code>true</code> if the action is to be suspended,
     *            <code>false</code> otherwise
     */
    public void setSuspendProcess(boolean suspendProcess) {
        this.suspendProcess = suspendProcess;
    }

    /**
     * Returns the name of the trigger definition.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the trigger definition.
     * 
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the hasTriggerProcess
     */
    public boolean isHasTriggerProcess() {
        return hasTriggerProcess;
    }

    /**
     * @param hasTriggerProcess
     *            the hasTriggerProcess to set
     */
    public void setHasTriggerProcess(boolean hasTriggerProcess) {
        this.hasTriggerProcess = hasTriggerProcess;
    }

    public VOOrganization getOrganization() {
        return organization;
    }

    public void setOrganization(VOOrganization organization) {
        this.organization = organization;
    }
}
