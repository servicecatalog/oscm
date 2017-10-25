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

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * History-Object of ParameterDefinition, used for auditing.
 * 
 * @author pock
 * 
 */
@Entity
@NamedQuery(name = "ParameterDefinitionHistory.findByObject", query = "select c from ParameterDefinitionHistory c where c.objKey=:objKey order by objversion")
public class ParameterDefinitionHistory extends
        DomainHistoryObject<ParameterDefinitionData> {

    private static final long serialVersionUID = 2615827431500996357L;

    private Long technicalProductObjKey;

    public ParameterDefinitionHistory() {
        this.dataContainer = new ParameterDefinitionData();
    }

    /**
     * Constructs ParameterDefinitionHistory from an ParameterDefinition domain
     * object
     * 
     * @param c
     *            The ParameterDefinition
     */
    public ParameterDefinitionHistory(ParameterDefinition c) {
        super(c);
        if (c.getTechnicalProduct() != null) {
            this.technicalProductObjKey = Long.valueOf(c.getTechnicalProduct()
                    .getKey());
        }
    }

    public Long getTechnicalProductObjKey() {
        return technicalProductObjKey;
    }

    public void setTechnicalProductObjKey(Long technicalProductObjKey) {
        this.technicalProductObjKey = technicalProductObjKey;
    }

    public ParameterModificationType getModificationType(){
        return dataContainer.getModificationType();
    }
    
    public ParameterType getParameterType() {
        return dataContainer.getParameterType();
    }

    public ParameterValueType getValueType() {
        return dataContainer.getValueType();
    }

    public String getParameterId() {
        return dataContainer.getParameterId();
    }

}
