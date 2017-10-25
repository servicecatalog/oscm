/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 22.01.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Remote interface providing the functionality to retrieve and manipulate
 * configuration settings.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Remote
public interface ConfigurationService {

    /**
     * Retrieves one particular configuration setting. If it is not available
     * for the given context, the setting for the context 'global' will be
     * returned. If this value cannot be found either, <code>null</code> will be
     * returned.
     * 
     * @param informationId
     *            the identifier of the setting to be retrieved.
     * @param contextId
     *            the context identifier for the setting to be retrieved.
     * @return the setting for the given information and context identifier
     *         pair, if there is any; <code>null</code> otherwise.
     */

    public VOConfigurationSetting getVOConfigurationSetting(
            ConfigurationKey informationId, String contextId);

    /**
     * Creates, updates, or deletes a configuration setting. Mandatory
     * configuration settings must not have an empty value - in this case an
     * {@link IllegalArgumentException} is thrown. If an optional configuration
     * setting is empty, it will be ignored if not persisted and deleted if
     * persisted.
     * 
     * @param informationId
     *            The information ID for which the value will be changed.
     * 
     * @param value
     *            The value to be set for the information ID.
     * 
     */
    public void setConfigurationSetting(String informationId, String value);

    /**
     * Checks if OSCM acts as service provider.
     * 
     * @return <code>true</code> if OSCM acts as service provider, otherwise
     *         <code>false</code>
     * @see ConfigurationKey#AUTH_MODE
     */
    public boolean isServiceProvider();
    
    /**
     * checks if payment information is available
     * 
     * @return <code>true</code> if information is available (default value), otherwise
     *         <code>false</code>
     * @see ConfigurationKey#HIDDEN_UI_ELEMENTS
     */
    public boolean isPaymentInfoAvailable();

}
