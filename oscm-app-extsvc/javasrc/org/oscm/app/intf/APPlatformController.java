/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-08-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.intf;

import java.util.List;
import java.util.Properties;

import javax.ejb.Remote;

import org.oscm.app.data.ControllerSettings;
import org.oscm.app.data.InstanceDescription;
import org.oscm.app.data.InstanceStatus;
import org.oscm.app.data.InstanceStatusUsers;
import org.oscm.app.data.LocalizedText;
import org.oscm.app.data.OperationParameter;
import org.oscm.app.data.ProvisioningSettings;
import org.oscm.app.data.ServiceUser;
import org.oscm.app.exceptions.APPlatformException;

/**
 * Interface abstracting the access to an application which is to be integrated
 * with the platform's subscription management. For each application, a specific
 * implementation of the methods defined here is required. Such an
 * implementation is referred to as a service controller.
 * <p>
 * It is recommended to divide long-running provisioning operations into several
 * steps using the polling feature of APP. For example, when creating an
 * application instance, the controller could immediately return the instance
 * ID, report the instance status as "not ready", and request APP to poll the
 * status at regular intervals. Only after the instance has actually been set up
 * at the application side, the controller would report its status as "ready"
 * upon the next polling by APP.
 * <p>
 * The polling feature can be used with every provisioning operation (methods
 * which return an <code>InstanceStatus</code> object or an extension thereof).
 * The polling as such takes place by means of the
 * <code>getInstanceStatus</code> and <code>getPublicIp</code> methods. These
 * are called at regular intervals by APP to obtain the instance status and IP
 * address.
 * <p>
 * Polling by APP should be switched off for processing steps which actively
 * report their completion via the APP notification handler. The controller is
 * responsible for providing the correct URL of the handler in this case. The
 * URL has the following format:<br>
 * <code><i>base_url</i>?sid=<i>instance_id</i>&cid=<i>controller_id</i>[&<i>options</i>]</code>
 * <br>
 * <code><i>base_url</i></code> is the basic URL of the APP notification handler
 * as provided by the <code>getEventServiceUrl</code> method of
 * <code>APPlatformService</code>.<br>
 * <code><i>instance_id</i></code> is the ID of the relevant application
 * instance.<br>
 * <code><i>controller_id</i></code> is the ID of the service controller.<br>
 * <code><i>options</i></code> are optional commands or parameters to be passed
 * to the controller.<br>
 * Example:<br>
 * <code>127.0.0.1:8080/oscm-app/notify?sid=vm2041&cid=ess.vmware&command=finish</code>
 * <p>
 * In addition to provisioning operations for subscriptions, a service
 * controller can execute service operations. Service operations access the
 * resources of an application and perform administrative tasks without actually
 * opening the application.
 */
@Remote
public interface APPlatformController {

    /**
     * The JNDI prefix used together with the controller ID for addressing the
     * controller in the container.
     */
    public static final String JNDI_PREFIX = "bss/app/controller/";

    /**
     * The key of the property used for passing the ID of a service operation.
     */
    public static final String KEY_OPERATION_ID = "_OPERATION_ID";

    /**
     * The key of the property used for passing the ID of a user who triggered a
     * service operation.
     */
    public static final String KEY_OPERATION_USER_ID = "_OPERATION_USER_ID";

    /**
     * The key of the property used for passing the transaction ID for a service
     * operation.
     */
    public static final String KEY_OPERATION_TX_ID = "_OPERATION_TX_ID";

    /**
     * This method is called when a customer subscribes to a service. It
     * triggers the application to provide an instance for the new subscription.
     * <p>
     * The application is supposed to perform whatever is required for the new
     * subscription. The actions to be performed and the items to be created, if
     * any, depend on the concepts and functionality of the application. For
     * example, an application that stores data for a customer may create a
     * separate workspace in a data container or a separate database instance.
     * In an IaaS environment, a new virtual machine could by provided.
     * 
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings for the
     *            provisioning operation
     * @return an <code>InstanceDescription</code> instance describing the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceDescription createInstance(ProvisioningSettings settings)
            throws APPlatformException;

    /**
     * This method is called when a customer upgrades or downgrades a
     * subscription.
     * <p>
     * It is up to you to decide which operations to perform in the application.
     * The platform passes all parameters that are specified in the service
     * definition. The application can evaluate the parameters and perform the
     * corresponding actions, for example, activate or deactivate a specific
     * feature, or allocate additional memory to a virtual machine.
     * <p>
     * Particularly with downgrade operations, the application should check
     * carefully whether the given parameter values do not cause
     * inconsistencies. For example, the number of files already created by a
     * customer could exceed the maximum number defined by the service offering
     * to which the customer wants to downgrade. In this case, the downgrade
     * would be rejected with a corresponding error.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription being upgraded or downgraded
     * @param currentSettings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            current service parameters and configuration settings
     * @param newSettings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            modified service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus modifyInstance(String instanceId,
            ProvisioningSettings currentSettings,
            ProvisioningSettings newSettings) throws APPlatformException;

    /**
     * This method is called when a subscription to a service is deleted
     * permanently.
     * <p>
     * It is up to you to decide which operations to perform in the application
     * and how to handle customer data that has been created for the
     * subscription. For example, you may delete the customer data or remove a
     * workspace or virtual machine created for the subscription.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription being deleted
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus deleteInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException;

    /**
     * Checks if the specified application instance is available. The remote
     * interface of the instance must be starting up or running so that
     * additional calls from APP or the controller can be executed.
     * 
     * @param instanceId
     *            the ID of the application instance to be checked
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus getInstanceStatus(String instanceId,
            ProvisioningSettings settings) throws APPlatformException;

    /**
     * Notifies the given application instance of one or more
     * controller-specific events.
     * <p>
     * The individual events and their purpose are defined by the controller
     * itself. For example, an event could indicate the approval of a step in
     * the provisioning process.
     * <p>
     * <b>Note: </b>APP itself raises specific events, which are passed to the
     * controller. The keys of these events start with an underscore (_). For
     * this reason, you must not use an underscore as the first character in the
     * keys of controller-specific events.
     * 
     * @param instanceId
     *            the ID of the application instance to receive the notification
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param properties
     *            the events as properties consisting of a key and a value each
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus notifyInstance(String instanceId,
            ProvisioningSettings settings, Properties properties)
            throws APPlatformException;

    /**
     * This method is called when a subscription becomes usable, for example,
     * after entering valid payment information if the service is not free of
     * charge.
     * <p>
     * It is up to you to decide which actions to perform in the application.
     * Typically, you activate the relevant application instance.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription whose status is changed
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus activateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException;

    /**
     * This method is called when a subscription becomes unusable, for example,
     * if the customer is no longer allowed to use the applicable payment type.
     * <p>
     * It is up to you to decide which actions to perform in the application.
     * Typically, you deactivate the relevant application instance.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription whose status is changed
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus deactivateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException;

    /**
     * This method is called when users are assigned to a subscription. It is
     * only called when the subscription's underlying service is configured with
     * one of the following access types: <code>LOGIN</code> or
     * <code>USER</code>.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * For example, the application might create the corresponding user accounts
     * in its own user management system. It can pass its own user IDs back to
     * the platform, which will use them in future calls to the application
     * instead of the platform user IDs, when required.
     * <p>
     * If supported by the application and technical service, users can be
     * assigned different service roles for a subscription in the platform. The
     * service roles are passed to the application together with the user IDs
     * and can be evaluated to take corresponding actions. For example, the
     * application may assign specific privileges or access rights to the users
     * depending on the service roles.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription to which the users are to be assigned
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param users
     *            the list of users to be assigned to the subscription
     * @return an <code>InstanceStatusUsers</code> instance with the status of
     *         the application instance
     * @throws APPlatformException
     */
    public InstanceStatusUsers createUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException;

    /**
     * This method is called when users are deassigned from a subscription. It
     * is only called when the subscription's underlying service is configured
     * with one of the following access types: <code>LOGIN</code> or
     * <code>USER</code>.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * For example, the application may remove the corresponding user accounts
     * from its own user management system.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription from which the users are to be deassigned
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param users
     *            the list of users to be deassigned from the subscription
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus deleteUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException;

    /**
     * This method is called when the profiles of users assigned to a
     * subscription are updated by the users or an administrator. The method is
     * only called when the subscription's underlying service is configured with
     * one of the following access types: <code>LOGIN</code> or
     * <code>USER</code>.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * For example, if the service role assigned to a user for the subscription
     * is changed in the platform, the application may update the user's
     * privileges or access rights accordingly.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription to which the users affected by the profile update
     *            are assigned
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param users
     *            the list of users assigned to the subscription, including the
     *            updated profile information
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus updateUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException;

    /**
     * Returns the current status of the service controller.
     * <p>
     * If the controller is working as intended, the method should return one or
     * more localized messages describing the current status and/or settings. If
     * there are problems, the method should throw a corresponding exception.
     * 
     * @param settings
     *            a <code>ControllerSettings</code> object specifying the
     *            controller configuration settings
     * @return the status messages
     * @throws APPlatformException
     */
    public List<LocalizedText> getControllerStatus(ControllerSettings settings)
            throws APPlatformException;

    /**
     * Requests the given application instance to return the possible values for
     * all parameters with a predefined set of values for the specified service
     * operation on behalf of the given user.
     * 
     * @param userId
     *            the ID of the user who requests the operation parameter
     *            values. If user IDs of the platform are mapped to user IDs of
     *            the application, the application user ID is passed. Otherwise,
     *            the platform user ID is passed.
     * @param instanceId
     *            the ID of the application instance for which to get the
     *            operation parameter values
     * @param operationId
     *            the ID of the operation for which to get the parameter values
     *            as specified in the technical service definition
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return the possible values for all parameters with a predefined set of
     *         values for the specified service operation and application
     *         instance
     * @throws APPlatformException
     */
    public List<OperationParameter> getOperationParameters(String userId,
            String instanceId, String operationId, ProvisioningSettings settings)
            throws APPlatformException;

    /**
     * Executes the service operation identified by its ID on behalf of the
     * given user for the specified application instance.
     * 
     * @param userId
     *            the ID of the user who triggered the service operation. If
     *            user IDs of the platform are mapped to user IDs of the
     *            application, the application user ID is passed. Otherwise, the
     *            platform user ID is passed.
     * @param instanceId
     *            the ID of the application instance for which the operation is
     *            to be executed
     * @param transactionId
     *            the transaction ID of the service operation
     * @param operationId
     *            the ID of the operation to be executed as specified in the
     *            technical service definition
     * @param parameters
     *            the parameters of the operation with their values
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the status of the
     *         application instance
     * @throws APPlatformException
     */
    public InstanceStatus executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters, ProvisioningSettings settings)
            throws APPlatformException;

    /**
     * Retrieves the current controller configuration settings from APP. 
     * This method is called by the APP core when the settings have been 
     * requested by the 
     * <code>APPlatformService.requestControllerSettings()</code> method or
     * after the settings have been modified by the 
     * <code>APPlatformService.stroreControllerSettings()</code> method.
     * 
     * @param settings
     *            a <code>ControllerSettings</code> object specifying the
     *            controller configuration settings
     */
    public void setControllerSettings(ControllerSettings settings);
}
