/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.intf;

import static javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;

/**
 * Interface defining the Web services which organizations must provide in order
 * to integrate their applications with the platform's subscription management.
 * The provisioning service of an application must be specified in the
 * definition of the technical service created for the application.
 * <p>
 * A provisioning service is called by the platform through SOAP messages over
 * HTTP/HTTPS. The service's interface must conform to the specification
 * provided in the <code>ProvisioningService.wsdl</code> document.
 */
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface ProvisioningService {

    /**
     * This method is called when a customer subscribes to a service whose
     * underlying technical service defines synchronous provisioning. It
     * retrieves an application instance for the subscription.
     * <p>
     * The application is supposed to perform whatever is required for the new
     * subscription and to return an identifier for the new instance to the
     * platform for future reference. The actions to be performed and the items
     * to be created, if any, depend on the concepts and functionality of the
     * application. For example, an application that stores data for a customer
     * may create a separate workspace in a data container or a separate
     * database instance.
     * 
     * @param request
     *            an <code>InstanceRequest</code> object specifying the
     *            subscription for which an instance is to be provisioned
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return an <code>InstanceResult</code> object representing the
     *         application instance for the subscription
     */
    @WebMethod(action = "urn:createInstance")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public InstanceResult createInstance(
            @WebParam(name = "request") InstanceRequest request,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a customer subscribes to a service whose
     * underlying technical service defines asynchronous provisioning. It
     * retrieves an application instance for the subscription.
     * <p>
     * The application is supposed to perform whatever is required for the new
     * subscription. The actions to be performed and the items to be created, if
     * any, depend on the concepts and functionality of the application. For
     * example, an application that stores data for a customer may create a
     * separate workspace in a data container or a separate database instance.
     * <p>
     * When the asynchronous provisioning of the instance is complete, the
     * application must notify the platform accordingly. To do this, it must
     * call the <code>completeAsyncSubscription</code> method of the
     * <code>SubscriptionService</code>, which is one of the platform services.
     * 
     * @param request
     *            an <code>InstanceRequest</code> object specifying the
     *            subscription for which an instance is to be provisioned
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error. If you want to report a message in the
     *         default language set for the customer organization owning the
     *         subscription, call
     *         <code>BaseResult.setDesc(java.lang.String text)</code> with a
     *         text for the locale returned by
     *         <code>InstanceRequest.getDefaultLocale()</code>.
     */
    @WebMethod(action = "urn:asyncCreateInstance")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult asyncCreateInstance(
            @WebParam(name = "request") InstanceRequest request,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a subscription to a service is deleted
     * permanently. It retrieves the status message and return code of the
     * corresponding operation in the application.
     * <p>
     * It is up to you to decide which operations to perform in your application
     * and how to handle customer data that has been created for the
     * subscription. For example, you may delete the customer data or remove a
     * workspace created for the subscription.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription being deleted
     * @param organizationId
     *            the ID of the customer organization associated with the
     *            subscription being deleted. This is required when deleting a
     *            subscription which is pending because no application instance
     *            has been created yet.
     * @param subscriptionId
     *            the ID of the subscription being deleted. This is required
     *            when deleting a subscription which is pending because no
     *            application instance has been created yet.
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:deleteInstance")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult deleteInstance(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "organizationId") String organizationId,
            @WebParam(name = "subscriptionId") String subscriptionId,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a customer renames a subscription or changes
     * parameter values or options for a service whose underlying technical
     * service defines synchronous provisioning. It retrieves the status message
     * and return code of the corresponding operation in the application.
     * Implementation must not perform any callback to Catalog Manager, which
     * modifies the subscription, e.g. then storing the instance information,
     * otherwise it'll cause the whole transaction to fail.
     * <p>
     * It is up to you to decide which operations to perform in your
     * application. The platform passes all parameters that are specified in the
     * service definition. The application can evaluate the parameters and
     * perform the corresponding actions, for example, activate or deactivate a
     * specific feature.
     * <p>
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription being modified
     * @param subscriptionId
     *            the identifier of the subscription as specified when the
     *            subscription was created or as changed by the customer for the
     *            current operation
     * @param parameterValues
     *            the new values for the service parameters. The parameters with
     *            their original values are passed to the application when
     *            <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:modifySubscription")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult modifySubscription(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "subscriptionId") String subscriptionId,
            @WebParam(name = "referenceId") String referenceId,
            @WebParam(name = "parameterValues") List<ServiceParameter> parameterValues,
            @WebParam(name = "attributeValues") List<ServiceAttribute> attributeValues,
            @WebParam(name = "requestingUser") User requestingUser);

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
     * @param users
     *            the list of users to be assigned to the subscription
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>UserResult</code> object, including a list of platform
     *         user IDs which are to be mapped to the corresponding application
     *         user IDs in the future
     */
    @WebMethod(action = "urn:createUsers")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public UserResult createUsers(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "users") List<User> users,
            @WebParam(name = "requestingUser") User requestingUser);

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
     * @param users
     *            the list of users to be deassigned from the subscription
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:deleteUsers")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult deleteUsers(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "users") List<User> users,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when the profiles of users assigned to a
     * subscription are updated by the users or an administrator. It retrieves
     * the status message and return code of the corresponding operation in the
     * application. The method is only called when the subscription's underlying
     * service is configured with one of the following access types:
     * <code>LOGIN</code> or <code>USER</code>.
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
     * @param users
     *            the list of users assigned to the subscription, including the
     *            updated profile information
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:updateUsers")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult updateUsers(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "users") List<User> users,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method can be used for testing purposes, for example, to check
     * whether the provisioning service is running and responding to requests.
     * 
     * @param arg
     *            some suitable string
     * @return some suitable string. For example, to indicate that the
     *         provisioning service is running and responding properly, the
     *         operation could return the string passed as <code>arg</code>.
     */
    @WebMethod(action = "urn:sendPing")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public String sendPing(@WebParam(name = "arg") String arg);

    /**
     * This method is called when a subscription becomes usable, for example,
     * after entering valid payment information for a subscription whose service
     * is not free of charge. The method retrieves the status message and return
     * code of the corresponding operation in the application.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription whose status is changed
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:activateInstance")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult activateInstance(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a subscription becomes unusable, for example,
     * if the customer is no longer allowed to use the applicable payment type.
     * The method retrieves the status message and return code of the
     * corresponding operation in the application.
     * <p>
     * It is up to you to decide which actions to perform in your application.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription whose status is changed
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:deactivateInstance")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult deactivateInstance(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a customer renames a subscription or changes
     * parameter values or options for a service whose underlying technical
     * service defines asynchronous provisioning. It retrieves the status
     * message and return code of the corresponding operation in the
     * application.
     * <p>
     * It is up to you to decide which operations to perform in your
     * application. The platform passes all parameters that are specified in the
     * service definition. The application can evaluate the parameters and
     * perform the corresponding actions, for example, activate or deactivate a
     * specific feature.
     * <p>
     * When the asynchronous modification of the instance is complete, the
     * application must notify the platform accordingly. To do this, it must
     * call the <code>completeAsyncModifySubscription</code> method of the
     * <code>SubscriptionService</code>, which is one of the platform services.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription being modified
     * @param subscriptionId
     *            the identifier of the subscription as specified when the
     *            subscription was created or as changed by the customer for the
     *            current operation
     * @param referenceId
     *            the ID specified when the subscription was created or changed
     *            by the customer to refer to it.
     * @param parameterValues
     *            the new values for the service parameters. The parameters with
     *            their original values are passed to the application when
     *            <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param attributeValues
     *            the new values for the service attributes as specified for the
     *            subscription attributes by the customer during the creation or
     *            update of the subscription.
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:asyncModifySubscription")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult asyncModifySubscription(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "subscriptionId") String subscriptionId,
            @WebParam(name = "referenceId") String referenceId,
            @WebParam(name = "parameterValues") List<ServiceParameter> parameterValues,
            @WebParam(name = "attributeValues") List<ServiceAttribute> attributeValues,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a customer upgrades or downgrades a
     * subscription to a service whose underlying technical service defines
     * asynchronous provisioning. It retrieves the status message and return
     * code of the corresponding operation in the application.
     * <p>
     * It is up to you to decide which operations to perform in your
     * application. The platform passes all parameters that are specified in the
     * service definition. The application can evaluate the parameters and
     * perform the corresponding actions, for example, activate or deactivate a
     * specific feature.
     * <p>
     * Particularly with downgrade operations, the application should check
     * carefully whether the given parameter values do not cause
     * inconsistencies. For example, the number of files already created by a
     * customer could exceed the maximum number defined by the service offering
     * to which the customer wants to downgrade. In this case, the downgrade
     * would be rejected with a corresponding error.
     * <p>
     * When the asynchronous modification of the instance is complete, the
     * application must notify the platform accordingly. To do this, it must
     * call the <code>completeAsyncUpgradeSubscription</code> method of the
     * <code>SubscriptionService</code>, which is one of the platform services.
     * 
     * @param instanceId
     *            the ID of the application instance belonging to the
     *            subscription being upgraded or downgraded
     * @param subscriptionId
     *            the identifier of the subscription as specified when the
     *            subscription was created or as changed by the customer for the
     *            current operation
     * @param referenceId
     *            the ID specified when the subscription was created or updated
     *            by the customer to refer to it.
     * @param parameterValues
     *            the new values for the service parameters. The parameters with
     *            their original values are passed to the application when
     *            <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param attributeValues
     *            the new values for the service attributes as specified for the
     *            subscription attributes by the customer during the creation or
     *            update of the subscription.
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:asyncUpgradeSubscription")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult asyncUpgradeSubscription(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "subscriptionId") String subscriptionId,
            @WebParam(name = "referenceId") String referenceId,
            @WebParam(name = "parameterValues") List<ServiceParameter> parameterValues,
            @WebParam(name = "attributeValues") List<ServiceAttribute> attributeValues,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a customer upgrades or downgrades a
     * subscription to a service whose underlying technical service defines
     * synchronous provisioning. It retrieves the status message and return code
     * of the corresponding operation in the application.
     * <p>
     * It is up to you to decide which operations to perform in your
     * application. The platform passes all parameters that are specified in the
     * service definition. The application can evaluate the parameters and
     * perform the corresponding actions, for example, activate or deactivate a
     * specific feature.
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
     * @param subscriptionId
     *            the identifier of the subscription as specified when the
     *            subscription was created or as changed by the customer for the
     *            current operation
     * @param referenceId
     *            the id specified when the subscription was created or changed
     *            by the customer to refer to it.
     * @param parameterValues
     *            the new values for the service parameters. The parameters with
     *            their original values are passed to the application when
     *            <code>createInstance</code> or
     *            <code>asynchCreateInstance</code> is called.
     * @param attributeValues
     *            the new values for the service attributes as specified for the
     *            subscription attributes by the customer during the creation or
     *            update of the subscription.
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation
     * @return a <code>BaseResult</code> object with a status message and the
     *         return code of the operation in the application. A value of 0
     *         indicates that the operation was successful. A value greater than
     *         0 indicates an error.
     */
    @WebMethod(action = "urn:upgradeSubscription")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult upgradeSubscription(
            @WebParam(name = "instanceId") String instanceId,
            @WebParam(name = "subscriptionId") String subscriptionId,
            @WebParam(name = "referenceId") String referenceId,
            @WebParam(name = "parameterValues") List<ServiceParameter> parameterValues,
            @WebParam(name = "attributeValues") List<ServiceAttribute> attributeValues,
            @WebParam(name = "requestingUser") User requestingUser);

    /**
     * This method is called when a customer sets values for customer
     * attributes. The attributes are saved within the APP and used, for
     * example, to overwrite the configured controller credentials.
     * 
     * @param organizationId
     *            the id of the organization the customer belongs to.
     * @param attributeValues
     *            the new values for the service attributes as specified for the
     *            customer attributes by the customer through the account.
     * @param requestingUser
     *            a <code>User</code> object specifying the platform user who
     *            requests the instance operation.
     * @return
     */
    @WebMethod(action = "urn:saveAttributes")
    @WebResult(name = "return")
    @SOAPBinding(parameterStyle = WRAPPED)
    public BaseResult saveAttributes(
            @WebParam(name = "organizationId") String organizationId,
            @WebParam(name = "attributeValues") List<ServiceAttribute> attributeValues,
            @WebParam(name = "requestingUser") User requestingUser);

}
