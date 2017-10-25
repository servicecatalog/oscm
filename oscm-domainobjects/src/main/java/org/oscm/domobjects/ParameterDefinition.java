/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * A parameter definition is the abstract definition of a technical product
 * setting which can be instantiated by a (market) product.
 * 
 * @author pock
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalProduct_tkey", "parameterId", "parameterType" }))
@NamedQueries({
        @NamedQuery(name = "ParameterDefinition.getPlatformParameterDefinition", query = "SELECT c FROM ParameterDefinition c WHERE c.technicalProduct IS NULL AND c.dataContainer.parameterType=:parameterType AND c.dataContainer.parameterId=:parameterId ORDER BY c.key ASC"),
        @NamedQuery(name = "ParameterDefinition.getAllPlatformParameterDefinitions", query = "SELECT c FROM ParameterDefinition c WHERE c.technicalProduct IS NULL AND c.dataContainer.parameterType=:parameterType ORDER BY c.key ASC"),
        @NamedQuery(name = "ParameterDefinition.findByBusinessKey", query = "select c from ParameterDefinition c where c.dataContainer.parameterId=:parameterId AND c.dataContainer.parameterType=:parameterType AND c.technicalProduct_tkey=:technicalProduct_tkey") })
@BusinessKey(attributes = { "technicalProduct_tkey", "parameterId",
        "parameterType" })
public class ParameterDefinition
        extends DomainObjectWithHistory<ParameterDefinitionData> {

    private static final long serialVersionUID = -2321303407897558512L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(
                    Arrays.asList(LocalizedObjectTypes.PARAMETER_DEF_DESC));

    @Column(name = "technicalProduct_tkey", insertable = false, updatable = false, nullable = true)
    private Long technicalProduct_tkey;

    /**
     * product events belong to a technical product (platform events do not)
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "technicalProduct_tkey")
    private TechnicalProduct technicalProduct;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parameterDefinition", fetch = FetchType.LAZY)
    @OrderBy
    private List<ParameterOption> optionList = new ArrayList<>();

    @OneToMany(cascade = {}, mappedBy = "parameterDefinition", fetch = FetchType.LAZY)
    @OrderBy
    private List<Parameter> parameters = new ArrayList<>();

    public ParameterDefinition() {
        super();
        dataContainer = new ParameterDefinitionData();
    }

    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
        if (null != technicalProduct) {
            setTechnicalProduct_tkey(Long.valueOf(technicalProduct.getKey()));
        }
    }

    /*
     * Delegate Methods
     */

    public String getParameterId() {
        return dataContainer.getParameterId();
    }

    public Long getTechnicalProduct_tkey() {
        return technicalProduct_tkey;
    }

    public void setTechnicalProduct_tkey(Long technicalProduct_tkey) {
        this.technicalProduct_tkey = technicalProduct_tkey;
    }

    public ParameterType getParameterType() {
        return dataContainer.getParameterType();
    }

    public ParameterValueType getValueType() {
        return dataContainer.getValueType();
    }

    public void setParameterId(String parameterId) {
        dataContainer.setParameterId(parameterId);
    }

    public void setParameterType(ParameterType parameterType) {
        dataContainer.setParameterType(parameterType);
    }

    public void setValueType(ParameterValueType valueType) {

        String value = getDefaultValue();

        dataContainer.setValueType(valueType);

        setDefaultValue(value);
    }

    /**
     * @return ModificationType
     */
    public ParameterModificationType getModificationType() {
        return dataContainer.getModificationType();
    }

    /**
     * @param parameterModificationType
     *            the parameterModificationType to set
     */
    public void setModificationType(
            ParameterModificationType modificationType) {
        dataContainer.setModificationType(modificationType);
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        if (getValueType() == ParameterValueType.PWD
                && dataContainer.getDefaultValue() != null) {
            try {
                return AESEncrypter.decrypt(dataContainer.getDefaultValue());
            } catch (GeneralSecurityException e) {
                return null;
            }
        } else {
            return dataContainer.getDefaultValue();
        }
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        if (getValueType() == ParameterValueType.PWD && defaultValue != null) {
            try {
                dataContainer
                        .setDefaultValue(AESEncrypter.encrypt(defaultValue));
            } catch (GeneralSecurityException e) {
                // ignore
            }
        } else {
            dataContainer.setDefaultValue(defaultValue);
        }
    }

    /**
     * @return the minimumValue
     */
    public Long getMinimumValue() {
        return dataContainer.getMinimumValue();
    }

    /**
     * @param minimumValue
     *            the minimumValue to set
     */
    public void setMinimumValue(Long minimumValue) {
        dataContainer.setMinimumValue(minimumValue);
    }

    /**
     * @return the maximumValue
     */
    public Long getMaximumValue() {
        return dataContainer.getMaximumValue();
    }

    /**
     * @param maximumValue
     *            the maximumValue to set
     */
    public void setMaximumValue(Long maximumValue) {
        dataContainer.setMaximumValue(maximumValue);
    }

    /**
     * @return the configurable
     */
    public boolean isConfigurable() {
        return dataContainer.isConfigurable();
    }

    /**
     * @param configurable
     *            the configurable to set
     */
    public void setConfigurable(boolean configurable) {
        dataContainer.setConfigurable(configurable);
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return dataContainer.isMandatory();
    }

    /**
     * @param mandatory
     *            the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        dataContainer.setMandatory(mandatory);
    }

    /**
     * @return the optionList
     */
    public List<ParameterOption> getOptionList() {
        return optionList;
    }

    /**
     * @param optionList
     *            the optionList to set
     */
    public void setOptionList(List<ParameterOption> optionList) {
        this.optionList = optionList;
    }

    /**
     * @return the parameterList
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param parameters
     *            the parameters to set
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    String toStringAttributes() {
        return String.format(", parameterId='%s', parameterOptions='%s'",
                getParameterId(), getOptionList());
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public boolean definesParametersOfUndeletedProduct() {
        for (Parameter param : parameters) {
            if (!param.getParameterSet().getProduct().isDeleted()) {
                return true;
            }
        }
        return false;
    }

}
