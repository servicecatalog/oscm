/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.vmware.business.statemachine.api.StateMachineAction;

/**
 * @author kulle
 *
 */
public class StateMachineTest {

    private States loadStates(String filename) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader
                .getResourceAsStream("statemachines/" + filename);) {
            JAXBContext jaxbContext = JAXBContext.newInstance(States.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (States) jaxbUnmarshaller.unmarshal(stream);
        }
    }

    /**
     * For all states except final states is checked that the action method
     * exists on the state class. The ERROR state for example is ignored because
     * it is a final state and the error handling is done by the controller and
     * not the action class.
     */
    private void ensureActionMethods(States states) throws Exception {
        String clazz = states.getActionClass();
        for (State s : states.getStates()) {
            if (isFinal(s)) {
                continue;
            }
            Method method = loadMethod(clazz, s);
            assertNotNull(method);
            assertNotNull(
                    method.getName()
                            + " is missing @StateMachineAction annotation",
                    method.getAnnotation(StateMachineAction.class));
        }
    }

    private boolean isFinal(State state) {
        return state.getAction() == null;
    }

    private Method loadMethod(String clazz, State state) throws Exception {
        Class<?> c = Class.forName(clazz);

        Class<?>[] paramTypes = new Class[3];
        paramTypes[0] = String.class;
        paramTypes[1] = ProvisioningSettings.class;
        paramTypes[2] = InstanceStatus.class;

        String methodName = state.getAction();
        try {
            return c.getMethod(methodName, paramTypes);
        } catch (@SuppressWarnings("unused") NoSuchMethodException e) {
            return c.getSuperclass().getMethod(methodName, paramTypes);
        }
    }

    /**
     * Checks for each transition the existence of the next state.
     */
    private void checkForBrokenTransitions(States states) {
        List<State> allStates = states.getStates();
        for (State s : allStates) {
            if (s.getEvents() == null) {
                // final state, no outgoing transitions
                continue;
            }

            for (Event e : s.getEvents()) {
                findStateById(e.getState(), states);
            }
        }

    }

    private State findStateById(String stateId, States states) {
        for (State state : states.getStates()) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        throw new IllegalStateException("State '" + stateId + "' not found");
    }

    private void checkStateUniqueness(States states) {
        List<String> foundStates = new ArrayList<String>();
        for (State s : states.getStates()) {
            if (foundStates.contains(s.getId())) {
                throw new IllegalStateException(
                        "State " + s.getId() + " is defined multiple times.");
            }
            foundStates.add(s.getId());
        }
    }

    @Test
    public void activateVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("activate_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void activateVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("activate_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void activateVm() throws Exception {
        // given
        States states = loadStates("activate_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void createVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("create_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void createVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("create_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void createVm() throws Exception {
        // given
        States states = loadStates("create_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void deactivateVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("deactivate_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void deactivateVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("deactivate_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void deactivateVm() throws Exception {
        // given
        States states = loadStates("deactivate_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void deleteVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("delete_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void deleteVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("delete_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void deleteVm() throws Exception {
        // given
        States states = loadStates("delete_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void modifyVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("modify_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void modifyVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("modify_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void modifyVm() throws Exception {
        // given
        States states = loadStates("modify_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void restartVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("restart_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void restartVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("restart_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void restartVm() throws Exception {
        // given
        States states = loadStates("restart_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void restoreVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("restore_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void restoreVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("restore_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void restoreVm() throws Exception {
        // given
        States states = loadStates("restore_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void snapshotVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("snapshot_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void snapshotVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("snapshot_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void snapshotVm() throws Exception {
        // given
        States states = loadStates("snapshot_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void startVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("start_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void startVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("start_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void startVm() throws Exception {
        // given
        States states = loadStates("start_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

    @Test
    public void stopVm_checkStateUniqueness() throws Exception {
        // given
        States states = loadStates("stop_vm.xml");

        // when
        checkStateUniqueness(states);

        // then no exception expected
    }

    @Test
    public void stopVm_checkForBrokenTransitions() throws Exception {
        // given
        States states = loadStates("stop_vm.xml");

        // when
        checkForBrokenTransitions(states);

        // then no exception expected
    }

    @Test
    public void stopVm() throws Exception {
        // given
        States states = loadStates("stop_vm.xml");

        // when
        ensureActionMethods(states);

        // then no exception expected
    }

}
