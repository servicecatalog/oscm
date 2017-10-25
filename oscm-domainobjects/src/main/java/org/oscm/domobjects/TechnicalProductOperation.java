/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

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

/**
 * @author weiser
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalProduct_tkey", "operationId" }))
@NamedQueries({ @NamedQuery(name = "TechnicalProductOperation.findByBusinessKey", query = "select c from TechnicalProductOperation c where c.dataContainer.operationId=:operationId AND c.technicalProduct_tkey=:technicalProduct_tkey") })
@BusinessKey(attributes = { "technicalProduct_tkey", "operationId" })
public class TechnicalProductOperation extends
        DomainObjectWithVersioning<TechnicalProductOperationData> {

    private static final long serialVersionUID = 2022025062839133740L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays
                    .asList(LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION,
                            LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME));

    @Column(name = "technicalProduct_tkey", insertable = false, updatable = false, nullable = false)
    private long technicalProduct_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "technicalProduct_tkey")
    private TechnicalProduct technicalProduct;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "technicalProductOperation", fetch = FetchType.LAZY)
    @OrderBy
    private List<OperationParameter> parameters = new ArrayList<OperationParameter>();

    @OneToMany(mappedBy = "technicalProductOperation", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy
    private List<OperationRecord> operationRecord = new ArrayList<OperationRecord>();

    public TechnicalProductOperation() {
        super();
        setDataContainer(new TechnicalProductOperationData());
    }

    public long getTechnicalProduct_tkey() {
        return technicalProduct_tkey;
    }

    public void setTechnicalProduct_tkey(long technicalProduct_tkey) {
        this.technicalProduct_tkey = technicalProduct_tkey;
    }

    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
        if (null != technicalProduct) {
            setTechnicalProduct_tkey(technicalProduct.getKey());
        }
    }

    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    public void setOperationId(String operationId) {
        dataContainer.setOperationId(operationId);
    }

    public String getOperationId() {
        return dataContainer.getOperationId();
    }

    public String getActionUrl() {
        return dataContainer.getActionUrl();
    }

    public void setActionUrl(String actionUrl) {
        dataContainer.setActionUrl(actionUrl);
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

    public List<OperationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OperationParameter> parameters) {
        this.parameters = parameters;
    }

    public List<OperationRecord> getOperationRecord() {
        return operationRecord;
    }

    public void setOperationRecord(List<OperationRecord> operationRecord) {
        this.operationRecord = operationRecord;
    }

    public boolean isRequestParameterValuesRequired() {
        for (OperationParameter op : getParameters()) {
            if (op.getType().isRequestValues()) {
                return true;
            }
        }
        return false;
    }

    public OperationParameter findParameter(String id) {
        for (OperationParameter op : getParameters()) {
            if (op.getId().equals(id)) {
                return op;
            }
        }
        return null;
    }
}
