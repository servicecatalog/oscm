/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                   
 *                                                                              
 *  Creation Date: 22.01.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.configurationservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * Local interface providing all functionality to retrieve and manipulate
 * configuration settings.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface ConfigurationServiceLocal {

    /**
     * Retrieves one particular configuration setting. If it is not available
     * for the given context, the setting for the context
     * {@link Configuration#GLOBAL_CONTEXT} will be returned. If this value
     * cannot be found either, the fallback value will be returned..
     * 
     * @param informationId
     *            The identifier for the setting to be retrieved.
     * @param contextId
     *            The context identifier for the setting to be retrieved.
     * @return The setting for the given information and context identifier
     *         pair, if there is any; <code>null</code> otherwise.
     */
    public ConfigurationSetting getConfigurationSetting(
            ConfigurationKey informationId, String contextId);

    /**
     * Creates, updates or deletes a configuration setting. Mandatory
     * configuration settings must not have an empty value - in this case an
     * {@link IllegalArgumentException} is thrown. If an optional configuration
     * setting is empty, it will be ignored when not persisted and deleted if
     * persisted.
     * 
     * @param configSetting
     *            The configuration setting to be stored or removed.
     */
    public void setConfigurationSetting(ConfigurationSetting configSetting);

    /**
     * Get the given configuration setting as parsed long value.
     * 
     * @param informationId
     *            The identifier for the setting to be retrieved.
     * @param contextId
     *            The context identifier for the setting to be retrieved.
     * @return the parsed long value or 0 if the informationId is null or if the
     *         configuration setting is not defined.
     */
    public long getLongConfigurationSetting(ConfigurationKey informationId,
            String contextId);

    /**
     * Returns the name of the cluster node. Returns <code>null</code> in case
     * the node name is not set.
     * 
     * @return The node name.
     */
    public String getNodeName();

    /**
     * Returns all currently stored configuration settings.
     * 
     * @return The configuration settings.
     */
    public List<ConfigurationSetting> getAllConfigurationSettings();

    /**
     * Checks if customer self registration is enabled or not.
     * 
     * @return <code>true</code> if enabled otherwise <code>false</code>
     * @see ConfigurationKey#CUSTOMER_SELF_REGISTRATION_ENABLED
     */
    public boolean isCustomerSelfRegistrationEnabled();

    /**
     * Checks if CT_MG acts as service provider.
     * 
     * @return <code>true</code> if CT_MG acts as service provider, otherwise
     *         <code>false</code>
     * @see ConfigurationKey#AUTH_MODE
     */
    public boolean isServiceProvider();

    /**
     * Get the part of the TIMER_INTERVAL_BILLING_OFFSET configuration setting
     * in milliseconds, which reflects an integer number of days.
     * 
     * @see ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET
     */
    public long getBillingRunOffsetInMs();

    /**
     * Get the part of the TIMER_INTERVAL_BILLING_OFFSET configuration setting
     * in milliseconds, which reflects a fraction of a day.
     * 
     * @see ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET
     */
    public long getBillingRunStartTimeInMs();

    /**
     * get BASE_URL, if BASE_URL is blank, return BASE_URL_HTTPS
     * 
     * @return base url
     */
    public String getBaseURL();

    public String getBaseUrlHttps();
    
    /**
     * checks if payment information is available
     * 
     * @return <code>true</code> if information is available (default value), otherwise
     *         <code>false</code>
     * @see ConfigurationKey#HIDDEN_UI_ELEMENTS
     */
    public boolean isPaymentInfoAvailable();
    
    /**
     * checks if kafka server is set
     * 
     * @return <code>true</code> if information is available (default value), otherwise
     *         <code>false</code>
     * @see ConfigurationKey#KAFKA_BOOTSTRAP_SERVERS
     */
    public boolean isKafkaEnabled();
}
