/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Sample controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2012-09-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.sample.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.oscm.app.sample.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher for triggering the next step in a provisioning operation depending
 * on the current internal status.
 * <p>
 * The controller methods for creating, updating, and deleting instances set
 * their own, internal status. This status is evaluated and handled by the
 * dispatcher, which is invoked at regular intervals by APP through the
 * <code>getInstanceStatus</code> method of the <code>SampleController</code>
 * class. The dispatcher sets the next internal status and returns the
 * corresponding overall instance status to APP through the controller.
 * <p>
 * In this sample implementation, the dispatcher simply moves from one step to
 * the next and sends emails at specific status transitions. The recipient and
 * contents of the emails are set as service parameters in the technical service
 * definition of the sample.
 * 
 */
public class Dispatcher {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Dispatcher.class);

    // The ID of the application instance
    private String instanceId;

    // A property handler of the sample implementation
    private PropertyHandler paramHandler;

    // An APPlatformService instance which provides for email communication
    private APPlatformService platformService;

    /**
     * Constructs a new dispatcher.
     * 
     * @param platformService
     *            an <code>APPlatformService</code> instance which provides
     *            helper methods for accessing common APP utilities, for
     *            example, send emails
     * @param instanceId
     *            the ID of the application instance in question
     * @param paramHandler
     *            a property handler for reading and writing service parameters
     *            and controller configuration settings
     */
    public Dispatcher(APPlatformService platformService, String instanceId,
            PropertyHandler paramHandler) {
        this.platformService = platformService;
        this.instanceId = instanceId;
        this.paramHandler = paramHandler;

    }

    /**
     * Triggers the next step of a provisioning operation depending on the
     * operation's current internal status, and returns the overall instance
     * status. The internal status is set by the service controller or the
     * dispatcher itself. At specific status transitions, emails are sent to the
     * recipient specified as a service parameter in the technical service
     * definition.
     * <p>
     * In real-life scenarios, the dispatcher might also check the current
     * status or the result of operations triggered from the outside.
     * 
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    public InstanceStatus dispatch() throws APPlatformException {
        // Get and trace current internal status of the operation
        Status currentState = paramHandler.getState();
        LOGGER.debug("  currentState=" + currentState.toString());

        Status newStatus = null;

        // Dispatch next step depending on current internal status
        switch (currentState) {
        case CREATION_REQUESTED:
            newStatus = Status.CREATION_STEP1;
            break;

        case MODIFICATION_REQUESTED:
            newStatus = Status.UPDATING;
            break;

        case DELETION_REQUESTED:
            newStatus = Status.DELETING;
            break;

        case ACTIVATION_REQUESTED:
            newStatus = Status.FINISHED;
            break;

        case DEACTIVATION_REQUESTED:
            newStatus = Status.FINISHED;
            break;

        case CREATION_STEP1:
            platformService.lockServiceInstance("ess.sample", instanceId,
                    paramHandler.getTPAuthentication());
            newStatus = Status.CREATION_STEP2;
            break;

        case CREATION_STEP2:
            platformService.unlockServiceInstance("ess.sample", instanceId,
                    paramHandler.getTPAuthentication());
            newStatus = Status.FINISHED;
            sendMail(instanceId, currentState);
            break;

        case UPDATING:
            newStatus = Status.FINISHED;
            sendMail(instanceId, currentState);
            break;

        case DELETING:
            newStatus = Status.DESTROYED;
            break;

        default:
        }

        if (newStatus != null) {
            // Set the next internal status for the provisioning operation.
            // The status is stored as a controller configuration setting.
            LOGGER.debug("  newState=" + newStatus.toString());
            paramHandler.setState(newStatus);
        }

        InstanceStatus result = new InstanceStatus();

        // Set the overall status of the application instance.
        // The instance is ready if the internal status of the
        // provisioning operation is FINISHED or DESTROYED. If this
        // is the case, APP stops polling for the instance status.
        result.setIsReady(paramHandler.getState() == Status.FINISHED
                || paramHandler.getState() == Status.DESTROYED);

        // Update the description of the instance status.
        // This description is displayed to users for a pending
        // subscription.
        List<LocalizedText> messages = Messages
                .getAll("status_" + paramHandler.getState());
        result.setDescription(messages);

        // Return the current parameters and settings to APP.
        // They are stored in the APP database.
        result.setChangedParameters(paramHandler.getSettings().getParameters());

        // When provisioning is done, provide access information that can be
        // shown to the subscriber.
        if (result.isReady()) {
            result.setAccessInfo(
                    "Access information for instance " + instanceId);
        }

        return result;
    }

    /**
     * Sends an email with the contents and to the recipient specified in the
     * technical service definition and includes all parameters and attributes.
     * 
     * @param instanceId
     *            the ID of the application instance in question
     * @param currentState
     *            the current internal status of the provisioning operation
     * @throws APPlatformException
     */
    private void sendMail(String instanceId, Status currentState)
            throws APPlatformException {
        // Create mail subject and contents
        String subject = Messages.get(Messages.DEFAULT_LOCALE, "mail.subject",
                new Object[] { instanceId });
        String text = Messages.get(Messages.DEFAULT_LOCALE, "mail.text",
                new Object[] { instanceId, paramHandler.getMessage(),
                        currentState.toString() });
        ProvisioningSettings ps = paramHandler.getSettings();

        StringBuffer sb = new StringBuffer(text);

        // include service parameters,
        sb.append("\n\nParameters\n");
        logSettings(sb, ps.getParameters());

        // service attributes,
        sb.append("\n\nAttributes\n");
        logSettings(sb, ps.getAttributes());

        // and customer attributes (UDAs)
        sb.append("\n\nCustomAttributes\n");
        logSettings(sb, ps.getCustomAttributes());

        // Send mail via APPlatformService
        platformService.sendMail(
                Collections.singletonList(paramHandler.getEMail()), subject,
                sb.toString());
    }

    private void logSettings(StringBuffer sb, HashMap<String, Setting> map) {
        for (Entry<String, Setting> item : map.entrySet()) {
            final String name = item.getKey();
            final Setting setting = item.getValue();
            String key = setting.getKey();
            String value = setting.getValue();
            sb.append(String.format("[%s] %s=%s", name, key, value));
        }
    }

}
