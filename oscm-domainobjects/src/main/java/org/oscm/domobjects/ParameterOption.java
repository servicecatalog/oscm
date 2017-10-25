/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jan 7, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * It stores the all information regarding the parameter options.
 * 
 * @author PRavi
 * 
 */

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "optionId",
        "parameterdefinition_tkey" }))
@NamedQueries({ @NamedQuery(name = "ParameterOption.findByBusinessKey", query = "select c from ParameterOption c where c.dataContainer.optionId=:optionId AND c.parameterdefinition_tkey=:parameterdefinition_tkey") })
@BusinessKey(attributes = { "optionId", "parameterdefinition_tkey" })
public class ParameterOption extends
        DomainObjectWithHistory<ParameterOptionData> {

    private static final long serialVersionUID = -2317882051339338028L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays
                    .asList(LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC));

    @Column(name = "parameterdefinition_tkey", insertable = false, updatable = false, nullable = false)
    private long parameterdefinition_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parameterdefinition_tkey")
    private ParameterDefinition parameterDefinition;

    public ParameterOption() {
        super();
        dataContainer = new ParameterOptionData();
    }

    public String getOptionId() {
        return dataContainer.getOptionId();
    }

    public void setOptionId(String optionId) {
        dataContainer.setOptionId(optionId);
    }

    public ParameterDefinition getParameterDefinition() {
        return parameterDefinition;
    }

    public void setParameterDefinition(ParameterDefinition parameterDefinition) {
        this.parameterDefinition = parameterDefinition;
        if (null != parameterDefinition) {
            setParameterdefinition_tkey(parameterDefinition.getKey());
        }
    }

    public long getParameterdefinition_tkey() {
        return parameterdefinition_tkey;
    }

    public void setParameterdefinition_tkey(long parameterdefinition_tkey) {
        this.parameterdefinition_tkey = parameterdefinition_tkey;
    }

    @Override
    String toStringAttributes() {
        return String.format(", parameterOptionId='%s'", getOptionId());
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

}
