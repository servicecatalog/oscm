/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 22.01.2014                                                      
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
import org.oscm.types.enumtypes.OperationParameterType;

/**
 * Domain object representing a service operation parameter.
 * 
 * @author weiser
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalproductoperation_tkey", "id" }))
@NamedQueries({ @NamedQuery(name = "OperationParameter.findByBusinessKey", query = "select c from OperationParameter c where c.dataContainer.id=:id AND c.technicalproductoperation_tkey=:technicalproductoperation_tkey") })
@BusinessKey(attributes = { "technicalproductoperation_tkey", "id" })
public class OperationParameter extends
        DomainObjectWithVersioning<OperationParameterData> {

    private static final long serialVersionUID = 6061895654935133124L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays
                    .asList(LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME));

    @Column(name = "technicalproductoperation_tkey", insertable = false, updatable = false, nullable = true)
    private Long technicalproductoperation_tkey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicalproductoperation_tkey")
    private TechnicalProductOperation technicalProductOperation;

    public OperationParameter() {
        dataContainer = new OperationParameterData();
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public TechnicalProductOperation getTechnicalProductOperation() {
        return technicalProductOperation;
    }

    public void setTechnicalProductOperation(
            TechnicalProductOperation technicalProductOperation) {
        this.technicalProductOperation = technicalProductOperation;
        if (null != technicalProductOperation) {
            setTechnicalproductoperation_tkey(Long
                    .valueOf(technicalProductOperation.getKey()));
        }
    }

    public Long getTechnicalproductoperation_tkey() {
        return technicalproductoperation_tkey;
    }

    public void setTechnicalproductoperation_tkey(
            Long technicalproductoperation_tkey) {
        this.technicalproductoperation_tkey = technicalproductoperation_tkey;
    }

    public String getId() {
        return dataContainer.getId();
    }

    public void setId(String id) {
        dataContainer.setId(id);
    }

    public OperationParameterType getType() {
        return dataContainer.getType();
    }

    public void setType(OperationParameterType type) {
        dataContainer.setType(type);
    }

    public boolean isMandatory() {
        return dataContainer.isMandatory();
    }

    public void setMandatory(boolean mandatory) {
        dataContainer.setMandatory(mandatory);
    }

}
