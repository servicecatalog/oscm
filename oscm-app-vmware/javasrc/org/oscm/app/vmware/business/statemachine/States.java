/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.vmware.business.statemachine.api.StateMachineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "states")
public class States {

    private static final Logger logger = LoggerFactory.getLogger(States.class);

    private List<State> states;
    private String clazz;

    @XmlAttribute
    public void setClass(String clazz) {
        this.clazz = clazz;
    }

    public String getActionClass() {
        return clazz;
    }

    @XmlElement(name = "state")
    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> aStates) {
        this.states = aStates;
    }

    public String invokeAction(State state, String instanceId,
            ProvisioningSettings settings, InstanceStatus status)
            throws StateMachineException {

        logger.info("Invoking action '" + state.getAction() + "' of state '"
                + state.getId() + "' for instance '" + instanceId + "'");

        try {
            Class<?> c = Class.forName(clazz);
            Object o = c.newInstance();

            Class<?>[] paramTypes = new Class[3];
            paramTypes[0] = String.class;
            paramTypes[1] = ProvisioningSettings.class;
            paramTypes[2] = InstanceStatus.class;

            String methodName = state.getAction();
            Method m;
            try {
                m = c.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                m = c.getSuperclass().getMethod(methodName, paramTypes);
            }
            return (String) m.invoke(o, instanceId, settings, status);
        } catch (InvocationTargetException e) {
            throw new StateMachineException(e.getCause().getMessage(),
                    e.getCause(), instanceId, clazz, state.getAction());
        } catch (Exception e) {
            logger.error("Failed to call action method '" + state.getAction()
                    + "' of state '" + state.getId() + "' for class '" + clazz
                    + "' and instance " + instanceId, e);
            throw new StateMachineException("Runtime error in action method: "
                    + e.getMessage(), e, instanceId, clazz, state.getAction());
        }
    }
}