/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.applicationservice.local;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.operation.data.OperationResult;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.User;

@Local
public interface ApplicationServiceLocal {

    /**
     * Creates and starts the product instance that belongs to the given
     * subscription.
     * 
     * @param subscription
     *            The subscription for which a product instance is created.
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public InstanceResult createInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Deletes the product instance that belongs to the given subscription.
     * 
     * @param subscription
     *            The subscription for which the product instance is deleted.
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void deleteInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Update the parameter set of the product instance that belongs to the
     * given subscription.
     * 
     * @param subscription
     *            The subscription for to which the product instance belongs and
     *            which contains the new parameter set.
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void modifySubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Create a new user in the product instance that belongs to the given
     * subscription.
     * 
     * @param subscription
     *            The subscription to which the product instance belongs.
     * @param usageLicenses
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public User[] createUsers(Subscription subscription,
            List<UsageLicense> usageLicenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Delete the user in the product instance that belongs to the given
     * subscription.
     * 
     * @param subscription
     *            The subscription to which the product instance belongs.
     * @param licenses
     *            The user to delete.
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void deleteUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Update the user data in the product instance that belongs to the given
     * subscription.
     * 
     * @param subscription
     *            The subscription to which the product instance belongs.
     * @param licenses
     *            The user to update.
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void updateUsers(Subscription subscription,
            List<UsageLicense> licenses)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Validates that the correct version of the provisioning service provided
     * by the technical product is running.
     * 
     * @param techProduct
     *            The technical product.
     * @return true if the technical product is running. Indicates that the
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the provisioning service of the technical
     *             product does not respond correctly.
     */
    public void validateCommunication(TechnicalProduct techProduct)
            throws TechnicalServiceNotAliveException;

    /**
     * Triggers the creation of a product instance for the given subscription.
     * Because it may take some time to get the product instance created, the
     * method just returns to avoid unnecessary waiting time or timeouts. The
     * application or some operator has to notify the system when the instance
     * is created.
     * 
     * @param subscription
     *            The subscription for which a product instance will be created.
     * @return
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public BaseResult asyncCreateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Calls the application to add all users licensed for the subscription to
     * its product instance. This method should be called when setting the state
     * of the subscription to {@link SubscriptionStatus#ACTIVE} after the
     * asynchronous tenant provisioning process.
     * 
     * @param subscription
     *            The subscription whose users shall be added to its product
     *            instance.
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public User[] createUsersForSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Calls the operation specified by the provided
     * {@link TechnicalProductOperation}. and passes the required attributes.
     * The passed parameters already must have been validated for completeness.
     * 
     * @param userId
     *            the user id of the user that triggered the service operation
     * @param subscription
     *            the subscription with the information about the service
     *            instance id
     * @param transactionId
     *            the transaction id
     * @param operation
     *            the service operation to execute
     * @param parameters
     *            a map of service operation parameters mapping the parameter id
     *            to its value
     */
    OperationResult executeServiceOperation(String userId,
            Subscription subscription, String transactionId,
            TechnicalProductOperation operation, Map<String, String> parameters)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * This method will be called whenever a subscription gets into state
     * {@link SubscriptionStatus#ACTIVE}.
     * 
     * @param subscription
     *            the subscription to activate the instance for
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void activateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * This method will be called whenever a subscription gets into state
     * {@link SubscriptionStatus#PENDING}.
     * 
     * @param subscription
     *            the subscription to deactivate the instance for
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void deactivateInstance(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Update the parameter set of the product instance that belongs to the
     * given subscription asynchronously.
     * 
     * @param subscription
     *            The subscription for to which the product instance belongs and
     *            which contains the new parameter set.
     * @param Product
     *            The product which contains new parameters
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void asyncModifySubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Upgrade the data in the given subscription asynchronously.
     * 
     * @param subscription
     *            The subscription to which the product instance belongs.
     * @param product
     *            The product which contains new parameters
     * 
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void asyncUpgradeSubscription(Subscription subscription,
            Product product) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Upgrade the data in the given subscription
     * 
     * @param subscription
     *            The subscription to which the product instance belongs.
     * 
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void upgradeSubscription(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Requests the available values for operation parameters that get their
     * values from the external service. If there is no such operation
     * parameter, an empty map will be returned without doing a call to the
     * external system.
     * 
     * @param userId
     *            the calling users id
     * @param operation
     *            the operation to get the parameter values for
     * @param subscription
     *            the subscription to get the instance id from
     * @return the parameter ids mapped to the available values
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public Map<String, List<String>> getOperationParameterValues(String userId,
            TechnicalProductOperation operation, Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Saves the user defined attributes for the subscribing organization in the
     * APP
     * 
     * @param subscription
     *            the subscription to get the UDAs and the APP credentials
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     */
    public void saveAttributes(Subscription subscription)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

}
