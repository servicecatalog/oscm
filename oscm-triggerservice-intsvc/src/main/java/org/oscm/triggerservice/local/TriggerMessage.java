/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerservice.local;

import java.util.Collections;
import java.util.List;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Wrapper object for sending a trigger of a certain type with a list of
 * parameters and receiver organizations.
 * 
 * @author weiser
 * 
 */
public class TriggerMessage {

    private int key = 0;
    private static int keyCounter = 0;
    private TriggerType triggerType;
    private List<TriggerProcessParameter> params;
    private List<Organization> receiverOrgs;
    private TriggerProcessParameterName parameterName;

    public TriggerMessage() {
        this(null, null);
    }

    public TriggerMessage(TriggerType triggerType) {
        this(triggerType, null);
    }

    public TriggerMessage(TriggerType triggerType,
            TriggerProcessParameterName parameterName) {
        key = keyCounter++;
        this.triggerType = triggerType;
        this.parameterName = parameterName;
    }

    public TriggerMessage(TriggerType type,
            List<TriggerProcessParameter> params,
            List<Organization> receiverOrgs) {
        setParams(params);
        setReceiverOrgs(receiverOrgs);
        setTriggerType(type);
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public List<TriggerProcessParameter> getParams() {
        return params;
    }

    public void setParams(List<TriggerProcessParameter> params) {
        this.params = params;
    }

    public List<Organization> getReceiverOrgs() {
        return receiverOrgs;
    }

    public void setReceiverOrgs(List<Organization> receiverOrgs) {
        this.receiverOrgs = receiverOrgs;
    }

    public TriggerProcessParameterName getParameterName() {
        return parameterName;
    }

    public int getKey() {
        return key;
    }

    @Override
    public boolean equals(Object that) {

        if (this == that)
            return true;

        if (!(that instanceof TriggerMessage)) {
            return false;
        }

        TriggerMessage compareTo = (TriggerMessage) that;
        return getKey() == compareTo.getKey();
    }

    @Override
    public int hashCode() {
        int hash = 59;
        hash = key * hash;

        return hash;
    }

    /**
     * Convenience method to constructs a new trigger message list based on the
     * given parameters.
     */
    public static List<TriggerMessage> create(TriggerType triggerType,
            List<TriggerProcessParameter> params, Organization receiver) {
        List<TriggerMessage> messages = Collections
                .singletonList((new TriggerMessage(triggerType, params,
                        Collections.singletonList(receiver))));
        return messages;
    }
}
