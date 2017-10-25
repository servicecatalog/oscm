/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                   
 *                                                                              
 *  Creation Date: 26.01.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;

import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.BaseVO;

/**
 * Value object that represents a particular configuration setting.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class VOConfigurationSetting extends BaseVO {

    private static final long serialVersionUID = 7420098797652288039L;

    private ConfigurationKey informationId;
    private String contextId;
    private String value;

    public VOConfigurationSetting() {

    }

    /**
     * Creates a configuration setting.
     * 
     * @param informationId
     *            The identifier for the configuration setting.
     * @param contextId
     *            The context identifier for the setting. The value must not be
     *            <code>null</code>.
     * @param value
     *            The actual value for the configuration setting.
     */
    public VOConfigurationSetting(ConfigurationKey informationId,
            String contextId, String value) {
        this.informationId = informationId;
        this.contextId = contextId;
        this.value = value;
    }

    /**
     * Retrieves the ID for the configuration setting.
     * 
     * @return configuration ID
     */
    public ConfigurationKey getInformationId() {
        return informationId;
    }

    /**
     * Sets the ID for the configuration setting.
     * 
     * @param informationId
     *            the configuration ID to be set
     */
    public void setInformationId(ConfigurationKey informationId) {
        this.informationId = informationId;
    }

    /**
     * Retrieves the context identifier for the configuration setting.
     * 
     * @return context ID
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Sets the context identifier for the configuration setting. The value must
     * not be <code>null</code>.
     * 
     * @param contextId
     *            the context id to be set
     */
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    /**
     * Retrieves the actual value for the configuration setting.
     * 
     * @return configuration setting value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the actual value for the configuration setting.
     * 
     * @param value
     *            the value to be set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
