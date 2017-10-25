/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Represents the supported timer types.
 * 
 * @author Mike J&auml;ger
 * 
 */
public enum TimerType {

    /**
     * Indicates a timer that is used to control checks to remove organization
     * accounts that have not been confirmed by a login of the initial
     * administrator within a certain period of time.
     */
    ORGANIZATION_UNCONFIRMED(ConfigurationKey.TIMER_INTERVAL_ORGANIZATION,
            ConfigurationKey.TIMER_INTERVAL_ORGANIZATION_OFFSET),
    /**
     * Indicates a timer that is used to ensure that subscriptions can only be
     * used for the time specified in the product related parameters. If this
     * period is exceeded, the timer related operations must ensure that the
     * subscription cannot be used anymore, unless the price model is changed.
     */
    RESTRICTED_SUBSCRIPTION_USAGE_PERIOD(
            ConfigurationKey.TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION,
            ConfigurationKey.TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION_OFFSET),

    /**
     * Indicates a timer that check pending subscriptions for a reached timeout
     * time. If this time is reached, an e-mail is sent to the technical
     * products organization administrators informing about the timeout.
     */
    TENANT_PROVISIONING_TIMEOUT(
            ConfigurationKey.TIMER_INTERVAL_TENANT_PROVISIONING_TIMEOUT,
            ConfigurationKey.TIMER_INTERVAL_TENANT_PROVISIONING_TIMEOUT_OFFSET),

    /**
     * Indicates a timer that runs the billing service every day and also causes
     * the debit operations to be run against the PSP interface. The timer does
     * not have a cyclic expiration time given in ms but as a period setting.
     */
    BILLING_INVOCATION(Period.DAY,
            ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET),

    /**
     * Indicates a timer that runs the check of discount to end coming every
     * day. The timer does not have a cyclic expiration time given in ms but as
     * a period setting.
     */
    DISCOUNT_END_CHECK(Period.DAY,
            ConfigurationKey.TIMER_INTERVAL_DISCOUNT_END_NOTIFICATION_OFFSET),

    /**
     * Indicates a timer that will scan for inactive on-behalf users regularly
     * and remove them, if they are inactive for a longer time than specified in
     * the configuration setting
     * {@link ConfigurationKey#PERMITTED_PERIOD_INACTIVE_ON_BEHALF_USERS}.
     */
    INACTIVE_ON_BEHALF_USERS_REMOVAL(
            ConfigurationKey.TIMER_INTERVAL_INACTIVE_ON_BEHALF_USERS,
            ConfigurationKey.TIMER_INTERVAL_INACTIVE_ON_BEHALF_USERS_OFFSET),
    /**
     * Indicates a timer that runs the check if the amount of users currently
     * registered has exceeded the amount of users allowed for the platform.No
     * offset is necessary for this timer.
     * 
     */
    USER_NUM_CHECK(ConfigurationKey.TIMER_INTERVAL_USER_COUNT, null);

    private ConfigurationKey keyForIntervalTime;
    private ConfigurationKey keyForIntervalOffset;
    private Period periodSetting;
    private boolean isBasedOnFixPeriod;

    /**
     * Creates a cyclic timer recurring every keyIntervalTime ms using an offset
     * of keyIntervalOffset ms.
     * 
     * @param keyIntervalTime
     *            The time between the cyclic expirations of the timer.
     * @param keyIntervalOffset
     *            The offset to be used to configure this timer.
     */
    private TimerType(ConfigurationKey keyIntervalTime,
            ConfigurationKey keyIntervalOffset) {
        this.keyForIntervalTime = keyIntervalTime;
        this.keyForIntervalOffset = keyIntervalOffset;
        this.isBasedOnFixPeriod = true;
    }

    private TimerType(Period periodSetting, ConfigurationKey keyIntervalOffset) {
        this.periodSetting = periodSetting;
        this.keyForIntervalOffset = keyIntervalOffset;
        this.isBasedOnFixPeriod = false;
    }

    public ConfigurationKey getKeyForIntervalTime() {
        return keyForIntervalTime;
    }

    public ConfigurationKey getKeyForIntervalOffset() {
        return keyForIntervalOffset;
    }

    public Period getPeriodSetting() {
        return periodSetting;
    }

    /**
     * Indicates whether the timer is based on an absolutely fix base period
     * given in milliseconds (what would return <code>true</code>) or if it is
     * based on a period like a month that is flexible.
     * 
     * @return <code>true</code> in case the timer is based on a fix amount of
     *         ms, <code>false</code> otherwise.
     */
    public boolean isBasedOnFixPeriod() {
        return isBasedOnFixPeriod;
    }

}
