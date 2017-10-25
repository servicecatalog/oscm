/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Jan 7, 2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of ParameterOption, used for auditing. Will be automatically
 * created during persist, save or remove operations.
 * 
 * @author PRavi
 */
@NamedQueries({ @NamedQuery(name = "ParameterOptionHistory.findByObject", query = "select c from ParameterOptionHistory c where c.objKey=:objKey order by objversion") })
@Entity
public class ParameterOptionHistory extends
        DomainHistoryObject<ParameterOptionData> {

    /**
     * 
     */
    private static final long serialVersionUID = 3445090245075996818L;

    /**
     * 
     */
    public ParameterOptionHistory() {
        dataContainer = new ParameterOptionData();
    }

    /**
     * Constructs ParameterDefinitionHistory from an ParameterDefinition domain
     * object
     * 
     * @param c
     *            The ParameterDefinition
     */
    public ParameterOptionHistory(ParameterOption parameterOption) {
        super(parameterOption);
        if (parameterOption.getParameterDefinition() != null) {
            setParameterDefObjKey(parameterOption.getParameterDefinition()
                    .getKey());
        }
    }

    @Column(nullable = false)
    private long parameterDefObjKey;

    /**
     * @return the parameterDefObjKey
     */
    public long getParameterDefObjKey() {
        return parameterDefObjKey;
    }

    /**
     * @param parameterDefObjKey
     *            the parameterDefObjKey to set
     */
    public void setParameterDefObjKey(long parameterDefObjKey) {
        this.parameterDefObjKey = parameterDefObjKey;
    }

    public String getOptionId() {
        return dataContainer.getOptionId();
    }

}
