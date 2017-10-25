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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * @author pock
 */
@Entity
@NamedQuery(name = "TriggerProcessParameter.getParam", query = "SELECT o from TriggerProcessParameter o WHERE o.triggerProcess.key = :actionKey and o.dataContainer.name = :paramName")
public class TriggerProcessParameter extends
        DomainObjectWithVersioning<TriggerProcessParameterData> {

    private static final long serialVersionUID = 4970321447922528586L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TriggerProcess triggerProcess;

    public TriggerProcessParameter() {
        super();
        dataContainer = new TriggerProcessParameterData();
    }

    public TriggerProcess getTriggerProcess() {
        return triggerProcess;
    }

    public void setTriggerProcess(TriggerProcess triggerProcess) {
        this.triggerProcess = triggerProcess;
    }

    // -----------------------
    // dataContainer delegates

    public TriggerProcessParameterName getName() {
        return dataContainer.getName();
    }

    public void setName(TriggerProcessParameterName name) {
        dataContainer.setName(name);
    }

    public <T> T getValue(Class<T> clazz) {
        return dataContainer.getValue(clazz);
    }

    public void setValue(Object value) {
        dataContainer.setValue(value);
    }

    public void setSerializedValue(String serializedValue) {
        dataContainer.setSerializedValue(serializedValue);
    }
    
    public String getSerializedValue() {
        return dataContainer.getSerializedValue();
    }
}
