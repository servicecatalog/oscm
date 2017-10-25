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

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author jaeger
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {
        "INFORMATION_ID", "CONTEXT_ID" }) })
@NamedQueries( {
        @NamedQuery(name = "ConfigurationSetting.findByInfoAndContext", query = "SELECT cs FROM ConfigurationSetting cs WHERE cs.dataContainer.informationId = :informationId AND cs.dataContainer.contextId = :contextId"),
        @NamedQuery(name = "ConfigurationSetting.getAll", query = "SELECT cs FROM ConfigurationSetting cs"),
        @NamedQuery(name = "ConfigurationSetting.getSettingsForNode", query = "SELECT cs FROM ConfigurationSetting cs WHERE cs.dataContainer.contextId IN (:context1, :globalContext)") })
public class ConfigurationSetting extends
        DomainObjectWithVersioning<ConfigurationSettingData> {

    private static final long serialVersionUID = 4236417344577567732L;

    public ConfigurationSetting(ConfigurationKey informationId,
            String contextId, String value) {
        super();
        dataContainer = new ConfigurationSettingData();
        dataContainer.setInformationId(informationId);
        dataContainer.setContextId(contextId);
        dataContainer.setValue(value);
    }

    public ConfigurationSetting() {
        super();
        dataContainer = new ConfigurationSettingData();
    }

    public String getContextId() {
        return dataContainer.getContextId();
    }

    public ConfigurationKey getInformationId() {
        return dataContainer.getInformationId();
    }

    /**
     * Returns the value of the configuration setting without leading or
     * trailing blanks.
     * 
     * @return The value of the configuration setting.
     */
    public String getValue() {
        return dataContainer.getValue();
    }

    public void setContextId(String contextId) {
        dataContainer.setContextId(contextId);
    }

    public void setInformationId(ConfigurationKey informationId) {
        dataContainer.setInformationId(informationId);
    }

    public void setValue(String value) {
        dataContainer.setValue(value);
    }

    public int getIntValue() {
        final String value = dataContainer.getValue();
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new SaaSSystemException(
                    "Invalid number format in configuration setting "
                            + getInformationId(), e);
        }
    }

    public long getLongValue() {
        final String value = dataContainer.getValue();
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new SaaSSystemException(
                    "Invalid number format in configuration setting "
                            + getInformationId(), e);
        }
    }

}
