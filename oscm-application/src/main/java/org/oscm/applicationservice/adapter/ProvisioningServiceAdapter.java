/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.adapter;

import java.net.URL;
import java.util.List;

import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;

/**
 * Interface to represent the version independent functionality of the
 * provisioning service.
 * 
 * @author Mike J&auml;ger
 */
public interface ProvisioningServiceAdapter {

    /**
     * Returns the URL to the local WSDL file, depending on the version of the
     * related provisioning service.
     * 
     * @return The URL to the local ProvisioningService WSDL
     */
    public URL getLocalWSDL();

    /**
     * Sets the reference to the web service provided by the technical service,
     * that implements the version-appropriate ProvisioningService.
     * 
     * @param provServ
     *            The reference to the provisioning service.
     */
    public void setProvisioningService(Object provServ);

    /**
     * Retrieves an instance belonging to a subscription while synchronous
     * provisioning is configured in the service definition. This method is
     * called when a customer subscribes to a service.
     * <p>
     * The underlying application is supposed to perform whatever is required
     * for the subscription and return an identifier to BES for future
     * reference. The actions to be performed and the items to be created, if
     * any, entirely depend on the concepts and functionality of your
     * application. For example, if a customer creates and stores data when
     * using your application, your application may create a separate workspace
     * in a data container or a separate database instance.
     * 
     * @param request
     *            Subscription for which an instance is to be provisioned.
     * @return Object of type <code>InstanceResult</code> representing an
     *         instance that - on the application side - belongs to a
     *         subscription.
     */
    public InstanceResult createInstance(InstanceRequest request,
            User requestingUser);

    /**
     * Retrieves an instance belonging to a subscription while asynchronous
     * provisioning is configured in the service definition. This method is
     * called when a customer subscribes to a service.
     * <p>
     * The underlying application is supposed to perform whatever is required
     * for the subscription. The actions to be performed and the items to be
     * created, if any, entirely depend on the concepts and functionality of
     * your application. For example, if a customer creates and stores data when
     * using your application, your application may create a separate workspace
     * in a data container or a separate database instance.
     * <p>
     * When asynchronous provisioning of an instance is complete, BES must be
     * notified using the <code>completeAsyncSubscription</code> method of the
     * BES public Web Service API.
     * 
     * @param request
     *            Subscription for which an instance is to be provisioned.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult asyncCreateInstance(InstanceRequest request,
            User requestingUser);

    /**
     * Retrieves the status message and the return code of a delete operation.
     * This method is called when a subscription of a service is irrevocably
     * deleted in BES.
     * <p>
     * It is up to you to decide which operations to perform in your application
     * and how to handle customer data that has been created during
     * subscription.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param organizationId
     *            ID of the organization of the instance that on the application
     *            side belongs to a subscription.
     * @param subscriptionId
     *            ID of the subscription of the instance that on the application
     *            side belongs to a subscription.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult deleteInstance(String instanceId, String organizationId,
            String subscriptionId, User requestingUser);

    /**
     * Retrieves the status message and the return code of an update operation.
     * This method is called when a customer update the subscription of a
     * service.
     * <p>
     * The operation passes all parameters that are declared in the service
     * definition. Your application could evaluate the parameter set and perform
     * any required actions, for example, activate or deactivate a feature of
     * your application.
     * <p>
     * To avoid inconsistent settings, it is recommended that you call
     * <code>checkParameterSet</code> before performing any actions in your
     * application.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param subscriptionId
     *            the subscription's id, can be changed by a user
     * @param parameterValues
     *            New values for parameters. The parameters are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult modifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser);

    /**
     * Retrieves the status message and the return code of an update operation.
     * This method is called when a customer update the subscription of a
     * service which provisioning type is "ASYNCHRONOUS".
     * <p>
     * The operation passes all parameters that are declared in the service
     * definition. Your application could evaluate the parameter set and perform
     * any required actions, for example, activate or deactivate a feature of
     * your application.
     * <p>
     * To avoid inconsistent settings, it is recommended that you call
     * <code>checkParameterSet</code> before performing any actions in your
     * application.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param subscriptionId
     *            the subscription's id, can be changed by a user
     * @param referenceId
     *            the subscription's reference, can be changed by a user
     * @param parameterValues
     *            New values for parameters. The parameters are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param attributesValues
     *            New values for attributes. The attributes are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult asyncModifySubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser);

    /**
     * Retrieves the status message and the return code of an upgrade or
     * downgrade operation. This method is called when a customer upgrades or
     * downgrades the subscription of a service which provisioning type is
     * "ASYNCHRONOUS".
     * <p>
     * The operation passes all parameters that are declared in the service
     * definition. Your application could evaluate the parameter set and perform
     * any required actions, for example, activate or deactivate a feature of
     * your application.
     * <p>
     * To avoid inconsistent settings, it is recommended that you call
     * <code>checkParameterSet</code> before performing any actions in your
     * application.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param subscriptionId
     *            the subscription's id, can be changed by a user
     * @param referenceId
     *            the subscription's reference, can be changed by a user
     * @param parameterValues
     *            New values for parameters. The parameters are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param attributesValues
     *            New values for attributes. The attributes are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult asyncUpgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser);

    /**
     * Retrieves the status message and the return code of an upgrade or
     * downgrade operation. This method is called when a customer upgrades or
     * downgrades the subscription of a service.
     * <p>
     * The operation passes all parameters that are declared in the service
     * definition. Your application could evaluate the parameter set and perform
     * any required actions, for example, activate or deactivate a feature of
     * your application.
     * <p>
     * To avoid inconsistent settings, it is recommended that you call
     * <code>checkParameterSet</code> before performing any actions in your
     * application.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param subscriptionId
     *            the subscription's id, can be changed by a user
     * @param referenceId
     *            the subscription's reference, can be changed by a user
     * @param parameterValues
     *            New values for parameters. The parameters are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param attributesValues
     *            New values for attributes. The attributes are passed to your
     *            application when <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult upgradeSubscription(String instanceId,
            String subscriptionId, String referenceId,
            List<ServiceParameter> parameterValues,
            List<ServiceAttribute> attributeValues, User requestingUser);

    /**
     * Assigns new users to the subscription of a service. The operation is only
     * called when login access or platform access is configured in the service
     * definition.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * The application might create, for example, corresponding user accounts in
     * its own user management system.
     * <p>
     * In BES, a user can be a standard user or an administrator. This
     * information is passed to your application and can be used to take
     * corresponding actions, for example, assign the user a corresponding role
     * in the application.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param users
     *            List of users that have been assigned to a subscription
     * @return Object of type <code>UserResult</code>
     */
    public UserResult createUsers(String instanceId, List<User> users,
            User requestingUser);

    /**
     * Deassigns users from the subscription of a service. The operation is only
     * called when login access or platform access is configured in the service
     * definition.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * For example, the application might remove the corresponding user accounts
     * from its own user management system.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param users
     *            List of users that have been assigned to a subscription
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult deleteUsers(String instanceId, List<User> users,
            User requestingUser);

    /**
     * Retrieves the status message and the return code of a profile update
     * operation. Called when user profiles are updated by the user or by an
     * administrator. The operation is only called when login access or platform
     * access is configured in the service definition.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * For example, if administrative rights have been revoked from a user in
     * BES, the application might assign the user a different role.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @param users
     *            List of users that have been assigned to a subscription
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult updateUsers(String instanceId, List<User> users,
            User requestingUser);

    /**
     * Can be used for testing purposes, for example, to check whether the
     * provisioning service is up and running and responding to requests.
     * 
     * @param arg
     *            Some suitable string.
     * @return Some suitable string. For example, to indicate that the
     *         provisioning service is up and running and properly responding,
     *         the operation could return the string passed as <code>arg</code>.
     */
    public String sendPing(String arg);

    /**
     * This method will be called whenever a subscription gets into state
     * {@link SubscriptionStatus#ACTIVE} after being in state
     * {@link SubscriptionStatus#PENDING}.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult activateInstance(String instanceId, User requestingUser);

    /**
     * This method will be called whenever a subscription gets into state
     * {@link SubscriptionStatus#PENDING}.
     * 
     * @param instanceId
     *            ID of the instance that on the application side belongs to a
     *            subscription.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult deactivateInstance(String instanceId,
            User requestingUser);

    /**
     * This method will be called after customer UDAs have been saved.
     * 
     * @param organizationId
     *            ID of the organization that set the attributes
     * @param attributeValues
     *            New values for the attributes.
     * @return Object of type <code>BaseResult</code> containing a status
     *         message and the return code of the operation. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    public BaseResult saveAttributes(String organizationId,
            List<ServiceAttribute> attributeValues, User requestingUser);

}
