/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the statuses a subscription can take on.
 * 
 */
public enum SubscriptionStatus {
    /**
     * The subscription is ready to handle user requests. Users can work with
     * the underlying application(s).
     */
    ACTIVE,

    /**
     * The subscription has been created but is still waiting for the tenant ID
     * of the associated application instance.
     */
    PENDING,

    /**
     * The subscription was used but has been deactivated explicitly. User
     * requests are not handled.
     */
    DEACTIVATED,

    /**
     * The subscription was active for a defined period but has been deactivated
     * automatically.
     */
    EXPIRED,

    /**
     * The subscription cannot be used because there are problems in fulfilling
     * its conditions and constraints (e.g. the application instance cannot be
     * provided).
     */
    INVALID,

    /**
     * The subscription cannot be used because the supplier or reseller has
     * disabled the payment type for which the customer specified his payment
     * information. The subscription keeps this status until valid payment
     * information is provided.
     */
    SUSPENDED,

    /**
     * The subscription has been modified but is still waiting for the
     * provisioning service to confirm the modification.
     */
    PENDING_UPD,

    /**
     * The subscription is suspended and at the same time the subscription has
     * been modified and response from the provisioning service is still
     * pending.
     */
    SUSPENDED_UPD;

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#SUSPENDED} or
     * {@link SubscriptionStatus#SUSPENDED_UPD}
     */
    public boolean isSuspendedOrSuspendedUpd() {
        return this == SUSPENDED || this == SUSPENDED_UPD;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#ACTIVE} or
     * {@link SubscriptionStatus#PENDING_UPD}
     */
    public boolean isActiveOrPendingUpd() {
        return this == ACTIVE || this == PENDING_UPD;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#ACTIVE}
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#EXPIRED}
     */
    public boolean isExpired() {
        return this == EXPIRED;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#ACTIVE} or
     * {@link SubscriptionStatus#PENDING}
     */
    public boolean isActiveOrPending() {
        return this == ACTIVE || this == PENDING;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#PENDING}
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#PENDING_UPD} or
     * {@link SubscriptionStatus#SUSPENDED_UPD}
     */
    public boolean isPendingUpdOrSuspendedUpd() {
        return this == PENDING_UPD || this == SUSPENDED_UPD;
    }

    /**
     * Convenience method returning <code>true</code> if the subscription is
     * either {@link SubscriptionStatus#INVALID} or
     * {@link SubscriptionStatus#DEACTIVATED}
     */
    public boolean isInvalidOrDeactive() {
        return this == INVALID || this == DEACTIVATED;
    }

    public SubscriptionStatus getNextForPaymentTypeRevoked() {
        if (this == SUSPENDED)
            return ACTIVE;
        if (this == SUSPENDED_UPD)
            return PENDING_UPD;
        return this;
    }

    public SubscriptionStatus getNextForPaymentTypeRemoved() {
        if (this == ACTIVE)
            return SUSPENDED;
        if (this == PENDING_UPD)
            return SUSPENDED_UPD;
        return this;
    }

    public SubscriptionStatus getNextForCompleteModify() {
        if (this == PENDING_UPD)
            return ACTIVE;
        if (this == SUSPENDED_UPD)
            return SUSPENDED;
        return this;
    }

    public SubscriptionStatus getNextForCompleteUpgrade(boolean validPayments) {
        if (validPayments)
            return ACTIVE;
        return SUSPENDED;
    }

    public SubscriptionStatus getNextForAbort() {
        if (this == PENDING_UPD)
            return ACTIVE;
        if (this == SUSPENDED_UPD)
            return SUSPENDED;
        return this;
    }

    public boolean canCompleteUpgrade() {
        switch (this) {
        case PENDING_UPD:
        case SUSPENDED_UPD:
        case EXPIRED:
            return true;
        default:
            return false;
        }
    }

    public boolean canCompleteModify() {
        switch (this) {
        case PENDING_UPD:
        case SUSPENDED_UPD:
            return true;
        default:
            return false;
        }
    }

}
