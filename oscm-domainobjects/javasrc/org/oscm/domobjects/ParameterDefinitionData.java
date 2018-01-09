/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.PMTConverter;
import org.oscm.domobjects.converters.PTConverter;
import org.oscm.domobjects.converters.PVTConverter;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * Data container to hold the information on each ParameterDefinition.
 * 
 * @author pock
 * 
 */
@Embeddable
public class ParameterDefinitionData extends DomainDataContainer {

    private static final long serialVersionUID = 5497543689039983993L;

    /**
     * The type of the parameter.
     */
    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ParameterType parameterType;

    /**
     * The identifier of the parameter.
     */
    @Column(nullable = false, updatable = false)
    private String parameterId;

    /**
     * The type of the parameter value.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParameterValueType valueType;

    /**
     * The type of the parameter modification type.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ParameterModificationType modificationType = ParameterModificationType.STANDARD;

    private String defaultValue;

    private Long minimumValue;

    private Long maximumValue;

    private boolean configurable = true;

    private boolean mandatory;

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setParameterId(String parameterId) {
        this.parameterId = parameterId;
    }

    public ParameterValueType getValueType() {
        return valueType;
    }

    public void setValueType(ParameterValueType valueType) {
        this.valueType = valueType;
    }

    public ParameterModificationType getModificationType() {
        return modificationType;
    }

    public void setModificationType(
            ParameterModificationType modificationType) {
        this.modificationType = modificationType;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the minimumValue
     */
    public Long getMinimumValue() {
        return minimumValue;
    }

    /**
     * @param minimumValue
     *            the minimumValue to set
     */
    public void setMinimumValue(Long minimumValue) {
        this.minimumValue = minimumValue;
    }

    /**
     * @return the maximumValue
     */
    public Long getMaximumValue() {
        return maximumValue;
    }

    /**
     * @param maximumValue
     *            the maximumValue to set
     */
    public void setMaximumValue(Long maximumValue) {
        this.maximumValue = maximumValue;
    }

    /**
     * @return the configurable
     */
    public boolean isConfigurable() {
        return configurable;
    }

    /**
     * @param configurable
     *            the configurable to set
     */
    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @param mandatory
     *            the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

}
