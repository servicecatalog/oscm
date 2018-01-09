/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.statemachine.api.StateMachineException;
import org.oscm.app.vmware.business.statemachine.api.StateMachineProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMachine {

    private static final Logger logger = LoggerFactory
            .getLogger(StateMachine.class);

    private States states;

    private String stateId;

    private String machine;

    private String history;

    public StateMachine(ProvisioningSettings settings)
            throws StateMachineException {
        machine = settings.getParameters()
                .get(StateMachineProperties.SM_STATE_MACHINE).getValue();
        states = loadStateMachine(machine);
        history = settings.getParameters()
                .get(StateMachineProperties.SM_STATE_HISTORY).getValue();
        stateId = settings.getParameters().get(StateMachineProperties.SM_STATE)
                .getValue();
    }

    private States loadStateMachine(String filename)
            throws StateMachineException {
        logger.debug("filename: " + filename);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream("statemachines/"
                + filename);) {
            JAXBContext jaxbContext = JAXBContext.newInstance(States.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (States) jaxbUnmarshaller.unmarshal(stream);
        } catch (Exception e) {
            throw new StateMachineException(
                    "Failed to load state machine definition file: " + filename,
                    e);
        }
    }

    public String getStateId() {
        return stateId;
    }

    public String getHistory() {
        return history;
    }

    public static void initializeProvisioningSettings(
            ProvisioningSettings settings, String stateMachine) {
        settings.getParameters().put(StateMachineProperties.SM_STATE_HISTORY,
                new Setting(StateMachineProperties.SM_STATE_HISTORY, ""));
        settings.getParameters().put(
                StateMachineProperties.SM_STATE_MACHINE,
                new Setting(StateMachineProperties.SM_STATE_MACHINE,
                        stateMachine));
        settings.getParameters().put(StateMachineProperties.SM_STATE,
                new Setting(StateMachineProperties.SM_STATE, "BEGIN"));
    }

    public String executeAction(ProvisioningSettings settings,
            String instanceId, InstanceStatus result)
            throws AuthenticationException, ConfigurationException,
            APPlatformException {

        State currentState = getState(stateId);
        String eventId = states.invokeAction(currentState, instanceId,
                settings, result);
        history = appendStateToHistory(stateId, history);
        stateId = getNextState(currentState, eventId);

        State nextState = getState(stateId);
        if (hasTimeout(nextState)) {
            VMPropertyHandler config = new VMPropertyHandler(settings);

            if (sameState(currentState, nextState)) {
                if ("suspended"
                        .equals(config
                                .getServiceSetting(VMPropertyHandler.GUEST_READY_TIMEOUT_REF))) {
                    logger.debug("Reinitialize timeout reference after an occured timeout.");
                    setReferenceForTimeout(config);
                } else {
                    String timeoutInMs = getReadyTimeout(nextState, config);
                    if (exceededTimeout(config, timeoutInMs)) {
                        config.setSetting(
                                VMPropertyHandler.GUEST_READY_TIMEOUT_REF,
                                "suspended");
                        storeSettings(instanceId, config);
                        logger.debug("Aborted execution of state '"
                                + nextState.getId() + "' due to timeout of "
                                + timeoutInMs + " ms");
                        throw new SuspendException("Task not finished after "
                                + timeoutInMs + " ms.");
                    }
                }
            } else {
                setReferenceForTimeout(config);
            }
        }

        return stateId;
    }

    private void setReferenceForTimeout(VMPropertyHandler config) {
        config.setSetting(VMPropertyHandler.GUEST_READY_TIMEOUT_REF,
                String.valueOf(System.currentTimeMillis()));
    }

    private void storeSettings(String instanceId, VMPropertyHandler config)
            throws AuthenticationException, ConfigurationException,
            APPlatformException {

        PasswordAuthentication credentials = config
                .getTechnologyProviderCredentials();
        APPlatformServiceFactory.getInstance().storeServiceInstanceDetails(
                Controller.ID, instanceId, config.getProvisioningSettings(),
                credentials);
    }

    private boolean hasTimeout(State state) {
        return state.getTimeout() != null
                && state.getTimeout().trim().length() > 0;
    }

    private boolean sameState(State oldState, State nextState) {
        return oldState.getId().equals(nextState.getId());
    }

    private String getReadyTimeout(State nextState, VMPropertyHandler config) {
        String timeoutInMs = nextState.getTimeout();
        if (timeoutInMs.startsWith("$")) {
            String timeoutVar = timeoutInMs.substring(2,
                    timeoutInMs.length() - 1);
            timeoutInMs = config.getGuestReadyTimeout(timeoutVar);
        }
        return timeoutInMs;
    }

    private boolean exceededTimeout(VMPropertyHandler config, String timeoutInMs) {

        if (timeoutInMs == null || timeoutInMs.trim().length() == 0) {
            logger.warn("Action timeout is not set and therefore ignored!");
            return false;
        }

        try {
            return System.currentTimeMillis()
                    - Long.valueOf(
                            config.getServiceSetting(VMPropertyHandler.GUEST_READY_TIMEOUT_REF))
                            .longValue() > Long.valueOf(timeoutInMs)
                    .longValue();
        } catch (NumberFormatException e) {
            logger.warn("The action timeout '" + timeoutInMs
                    + " 'is not a number and therefore ignored.");
            return false;
        }

    }

    private State getState(String stateId) throws StateMachineException {
        for (State state : states.getStates()) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        throw new StateMachineException("State " + stateId + " not found");
    }

    private String getNextState(State state, String eventId)
            throws StateMachineException {

        for (Event event : state.getEvents()) {
            if (event.getId().equals(eventId)) {
                logger.debug("Transition from current state '" + state.getId()
                        + "' with event '" + eventId + "' into state '"
                        + event.getState() + "'");
                return event.getState();
            }
        }

        throw new StateMachineException(
                "No next state defined for current state: " + state.getId()
                        + " event: " + eventId);
    }

    String appendStateToHistory(String state, String stateHistory) {
        if (stateHistory == null || stateHistory.trim().length() == 0) {
            return state;
        } else if (!stateHistory.endsWith(state)) {
            return stateHistory.concat(",").concat(state);
        }
        return stateHistory;
    }

    public String loadPreviousStateFromHistory(ProvisioningSettings settings)
            throws StateMachineException {

        String currentState = settings.getParameters()
                .get(StateMachineProperties.SM_STATE).getValue();
        String stateHistory = settings.getParameters()
                .get(StateMachineProperties.SM_STATE_HISTORY).getValue();
        String[] states = stateHistory.split(",");
        for (int i = states.length - 1; i >= 0; i--) {
            if (!states[i].equals(currentState)) {
                return states[i];
            }
        }

        throw new StateMachineException(
                "Couldn't find a previous state for statemachine " + machine
                        + " and current state " + stateId);
    }
}
