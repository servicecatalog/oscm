/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine.api;

import org.oscm.app.v2_0.exceptions.APPlatformException;

/**
 * @author kulle
 * 
 */
public class StateMachineException extends APPlatformException {

    private static final long serialVersionUID = -5619220710463876008L;

    private String instanceId;

    private String clazz;

    private String method;

    public StateMachineException(String msg) {
        super(msg);
    }

    public StateMachineException(String msg, Throwable e) {
        super(msg, e);
    }

    public StateMachineException(String msg, Throwable e, String instanceId,
            String clazz, String method) {

        super(msg, e);
        this.instanceId = instanceId;
        this.clazz = clazz;
        this.method = method;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getClazz() {
        return clazz;
    }

    public String getMethod() {
        return method;
    }

}
