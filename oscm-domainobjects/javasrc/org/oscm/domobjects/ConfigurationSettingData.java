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

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.CKConverter;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Data container for the configuration setting domain object.
 * 
 * @author jaeger
 */
@Embeddable
public class ConfigurationSettingData extends DomainDataContainer {

    private static final long serialVersionUID = 4236417344577567732L;

    @Enumerated(EnumType.STRING)
    @Column(name = "INFORMATION_ID", nullable = false)
    private ConfigurationKey informationId;

    @Column(name = "CONTEXT_ID", nullable = false)
    private String contextId;

    @Column(name = "ENV_VALUE")
    private String value;

    public ConfigurationSettingData() {

    }

    public ConfigurationSettingData(ConfigurationKey informationId,
            String contextId, String value) {
        this.informationId = informationId;
        this.contextId = contextId;
        this.value = value;
    }

    public ConfigurationKey getInformationId() {
        return informationId;
    }

    public void setInformationId(ConfigurationKey informationId) {
        this.informationId = informationId;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getValue() {
        if (value != null) {
            return value.trim();
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
