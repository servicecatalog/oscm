/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2010-06-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the actions that can initiate a trigger.
 * 
 */
public enum TriggerType {

    /**
     * Activating a service.
     */
    ACTIVATE_SERVICE(true),

    /**
     * Changing the user assignments of a subscription or the user/role
     * assignments in a subscription context.
     */
    ADD_REVOKE_USER(true),

    /**
     * Deactivating a service.
     */
    DEACTIVATE_SERVICE(true),

    /**
     * Changing a subscription (action and notification for subscriber).
     */
    MODIFY_SUBSCRIPTION(true),

    /**
     * Registering a new customer for a supplier.
     */
    REGISTER_CUSTOMER_FOR_SUPPLIER(true),

    /**
     * Registering a user in one's organization.
     */
    REGISTER_OWN_USER(true),

    /**
     * Configuring payment types.
     */
    SAVE_PAYMENT_CONFIGURATION(true),

    /**
     * Starting a billing run.
     */
    START_BILLING_RUN(false),

    /**
     * Subscribing to a service (action and notification for subscriber).
     */
    SUBSCRIBE_TO_SERVICE(true),

    /**
     * Terminating a subscription (action and notification for subscriber).
     */
    UNSUBSCRIBE_FROM_SERVICE(true),

    /**
     * Upgrading or downgrading a subscription to a different service.
     */
    UPGRADE_SUBSCRIPTION(true),

    /**
     * Subscribing to a service (action and notification for service supplier).
     */
    SUBSCRIPTION_CREATION(false),

    /**
     * Changing a subscription (action and notification for service supplier).
     */
    SUBSCRIPTION_MODIFICATION(false),

    /**
     * Terminating a subscription (action and notification for service
     * supplier).
     */
    SUBSCRIPTION_TERMINATION(false);

    /**
     * The action (transaction) which activates the trigger can only be
     * suspended if this flag is true;
     */
    private boolean suspendProcess;

    private TriggerType(boolean suspendProcess) {
        this.suspendProcess = suspendProcess;
    }

    /**
     * Checks whether the action (transaction) which activates the trigger can
     * be suspended.
     * 
     * @return <code>true</code> if the trigger activation can be suspended,
     *         <code>false</code> otherwise
     */
    public boolean isSuspendProcess() {
        return suspendProcess;
    }

}
