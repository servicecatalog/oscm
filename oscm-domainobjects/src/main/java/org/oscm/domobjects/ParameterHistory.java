/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 29.06.2009                                                      
 *                                                                              
 *  Completion Time: 30.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * History-Object of Parameter, used for auditing. Will be automatically created
 * during persist, save or remove operations (if performed via DataManager)
 * 
 * @author Peter Pock
 * 
 */
@Entity
@NamedQuery(name = "ParameterHistory.findByObject", query = "select c from ParameterHistory c where c.objKey=:objKey order by objversion")
public class ParameterHistory extends DomainHistoryObject<ParameterData> {

    private static final long serialVersionUID = 1636263933673235041L;

    /**
     * Field to keep the old dependency to the parent object (referential
     * constraint). Although the parent object might be deleted, the former key
     * value must be stored to reconstruct the situation for any point in time
     * via history.
     */
    private long parameterSetObjKey;

    /**
     * Key for parameter definition.
     */
    private long parameterDefinitionObjKey;

    public ParameterHistory() {
        dataContainer = new ParameterData();
    }

    /**
     * Constructs ParameterHistory from a Parameter domain object
     * 
     * @param c
     *            The Parameter
     */
    public ParameterHistory(Parameter c) {
        super(c);
        if (c.getParameterSet() != null) {
            setParameterSetObjKey(c.getParameterSet().getKey());
        }
        if (c.getParameterDefinition() != null) {
            setParameterDefinitionObjKey(c.getParameterDefinition().getKey());
        }
    }

    /**
     * Getter of parameter definition.
     * 
     * @return
     */
    public long getParameterDefinitionObjKey() {
        return parameterDefinitionObjKey;
    }

    /**
     * Setter for parameter definition.
     * 
     * @param parameterDefinitionObjKey
     */
    public void setParameterDefinitionObjKey(long parameterDefinitionObjKey) {
        this.parameterDefinitionObjKey = parameterDefinitionObjKey;
    }

    /**
     * Getter for parameter set key.
     * 
     * @return Parameter set key.
     */
    public long getParameterSetObjKey() {
        return parameterSetObjKey;
    }

    /**
     * Setter for parameter set key.
     * 
     * @param parameterSetObjKey
     *            Parameter set key.
     */
    public void setParameterSetObjKey(long parameterSetObjKey) {
        this.parameterSetObjKey = parameterSetObjKey;
    }

    public String getValue() {
        return this.dataContainer.getValue();
    }
}
