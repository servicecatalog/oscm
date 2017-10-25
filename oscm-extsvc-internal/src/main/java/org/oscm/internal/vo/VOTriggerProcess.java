/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-06-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.types.enumtypes.TriggerProcessStatus;

/**
 * Represents a trigger process.
 * 
 */
public class VOTriggerProcess extends BaseVO {

    private static final long serialVersionUID = -8525435700457729053L;

    /**
     * The activation date and time of the trigger process.
     */
    private long activationDate;

    /**
     * The reason for the last status change.
     */
    private String reason;

    /**
     * The current status of the trigger process.
     */
    private TriggerProcessStatus status;

    /**
     * The trigger definition of this trigger process.
     */
    private VOTriggerDefinition triggerDefinition;

    /**
     * The user who initiated the trigger process.
     */
    private VOUser user;

    /**
     * The IDs or names of the target objects of the trigger process, depending
     * on the action that started the process.
     */
    private List<String> targetNames = new ArrayList<String>();

    /**
     * The name of the affected trigger process parameter, if the trigger
     * process can have different parameters.
     */
    private String parameter;

    /**
     * The subscription which initiated the trigger process.
     */
    private VOSubscription subscription;

    /**
     * The subscribed marketable service
     */
    private VOService service;

    /**
     * Trigger process parameter udas
     */
    private List<VOUda> udas;

    /**
     * Returns the activation date and time of the trigger process.
     * 
     * @return the activation date and time
     */
    public long getActivationDate() {
        return activationDate;
    }

    /**
     * Sets the activation date and time for the trigger process.
     * 
     * @param activationDate
     *            the activation date and time
     */
    public void setActivationDate(long activationDate) {
        this.activationDate = activationDate;
    }

    /**
     * Returns the reason for the last status change of the trigger process.
     * 
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason for the last status change of the trigger process.
     * 
     * @param reason
     *            the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the current status of the trigger process.
     * 
     * @return the status
     */
    public TriggerProcessStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the trigger process.
     * 
     * @param status
     *            the status
     */
    public void setStatus(TriggerProcessStatus status) {
        this.status = status;
    }

    /**
     * Returns the trigger definition the trigger process is based on.
     * 
     * @return the trigger definition
     */
    public VOTriggerDefinition getTriggerDefinition() {
        return triggerDefinition;
    }

    /**
     * Sets the trigger definition the trigger process is to be based on.
     * 
     * @param triggerDefinition
     *            the trigger definition
     */
    public void setTriggerDefinition(VOTriggerDefinition triggerDefinition) {
        this.triggerDefinition = triggerDefinition;
    }

    /**
     * Returns the user who initiated the action that started the trigger
     * process.
     * 
     * @return the user
     */
    public VOUser getUser() {
        return user;
    }

    /**
     * Sets the user who initiated the action that started the trigger process.
     * 
     * @param user
     *            the user
     */
    public void setUser(VOUser user) {
        this.user = user;
    }

    /**
     * Returns the IDs or names of the target objects of the trigger process,
     * depending on the action that started the process.
     * 
     * @return the object IDs or names
     */
    public List<String> getTargetNames() {
        return targetNames;
    }

    /**
     * Sets the IDs or names of the target objects of the trigger process,
     * depending on the action that started the process.
     * 
     * @param targetNames
     *            the object IDs or names
     */
    public void setTargetNames(List<String> targetNames) {
        this.targetNames = targetNames;
    }

    /**
     * Sets the given parameter for the trigger process.
     * 
     * @param parameter
     *            the parameter
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * Returns the parameter defined for the trigger process.
     * 
     * @return the parameter
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * Returns the subscription which initiated the trigger process
     * 
     * @return the subscription
     */
    public VOSubscription getSubscription() {
        return subscription;
    }

    /**
     * Sets the subscription which initiated the trigger process
     * 
     * @param subscription
     *            the subscription to set
     */
    public void setSubscription(VOSubscription subscription) {
        this.subscription = subscription;
    }

    /**
     * Returns the subscribed service
     * 
     * @return the service
     */
    public VOService getService() {
        return service;
    }

    /**
     * Sets the subscribed service
     * 
     * @param service
     *            the service to set
     */
    public void setService(VOService service) {
        this.service = service;
    }

    public List<VOUda> getUdas() {
        return udas;
    }

    public void setUdas(List<VOUda> udas) {
        this.udas = udas;
    }
}
