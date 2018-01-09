/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-03-01                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.controller;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.openstack.i18n.Messages;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProvisioningValidator {

    private static final Logger logger = LoggerFactory
            .getLogger(ProvisioningValidator.class);
    private static final String TIMEOUT = "Timeout";
    private static final List<FlowState> TIMEOUT_OPERATION = Arrays.asList(
            FlowState.START_REQUESTED, FlowState.STARTING,
            FlowState.STOP_REQUESTED, FlowState.STOPPING);

    public void validateStackName(PropertyHandler paramHandler)
            throws APPlatformException {
        String stackName = paramHandler.getStackName();
        if (isNullOrEmpty(stackName)) {
            throw new APPlatformException(Messages.getAll("error_invalid_name",
                    new Object[] { stackName }));
        }

        String regex = "([A-Za-z][A-Za-z0-9_.-]*){1,30}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(stackName);
        if (!m.matches()) {
            logger.error("Validation error on stack name: [" + stackName + "/"
                    + regex + "]");
            throw new APPlatformException(Messages.getAll("error_invalid_name",
                    new Object[] { stackName }));
        }

        String stackNamePattern = paramHandler.getStackNamePattern();
        if (!isNullOrEmpty(stackNamePattern)) {

            try {
                p = Pattern.compile(stackNamePattern);
            } catch (PatternSyntaxException e) {
                logger.error("Compile error on stack name pattern: ["
                        + stackNamePattern + "]");
                throw new APPlatformException(Messages.getAll(
                        "error_invalid_pattern",
                        new Object[] { stackNamePattern }));
            }

            m = p.matcher(stackName);
            if (!m.matches()) {
                logger.error("Validation error on stack name: [" + stackName
                        + "/" + stackNamePattern + "]");
                throw new APPlatformException(Messages.getAll(
                        "error_name_match", new Object[] { stackName,
                                stackNamePattern }));
            }
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Check timeout for each operation. If operation is not finished during
     * several times, APPlatformException will occur.
     * 
     * @param instanceId
     * @param ph
     * @throws APPlatformException
     * @throws ConfigurationException
     * @throws AuthenticationException
     */
    public static void validateTimeout(String instanceId, PropertyHandler ph,
            APPlatformService APPService) throws AuthenticationException,
            ConfigurationException, APPlatformException {
        long readyTimeout = ph.getReadyTimeout();
        String startTimeStr = ph.getStartTime();
        if (readyTimeout != 0 && startTimeStr != null
                && TIMEOUT_OPERATION.contains(ph.getState())) {
            if (startTimeStr.equals(TIMEOUT)) {
                logger.warn("This request already timeout. This should not occur.");
                throw new APPlatformException(Messages.getAll(
                        "error_operation_timeout", Long.valueOf(readyTimeout)));
            }
            try {

                long startTime = Long.parseLong(startTimeStr);
                long currentTime = System.currentTimeMillis();
                long timePast = currentTime - startTime;
                logger.debug("ExecutionTime: " + timePast + "ms (StartTime: "
                        + startTime + "ms, CurrentTime: " + currentTime
                        + "ms), ReadyTimeout: " + readyTimeout + "ms");

                if (timePast > readyTimeout) {
                    logger.debug("Request timeout: over " + timePast + "ms");
                    ph.setStartTime(TIMEOUT);
                    APPService.storeServiceInstanceDetails(
                            OpenStackController.ID, instanceId,
                            ph.getSettings(), ph.getTPAuthentication());

                    if (ph.getState() == FlowState.START_REQUESTED
                            || ph.getState() == FlowState.STARTING) {
                        throw new APPlatformException(Messages.getAll(
                                "error_starting_operation_timeout",
                                Long.valueOf(readyTimeout)));
                    } else if (ph.getState() == FlowState.STOP_REQUESTED
                            || ph.getState() == FlowState.STOPPING) {
                        throw new APPlatformException(Messages.getAll(
                                "error_stopping_operation_timeout",
                                Long.valueOf(readyTimeout)));
                    }
                }
            } catch (NumberFormatException ex) {
                logger.warn("The action timeout is not a number and therefore ignored.");
            }
        }

    }
}
