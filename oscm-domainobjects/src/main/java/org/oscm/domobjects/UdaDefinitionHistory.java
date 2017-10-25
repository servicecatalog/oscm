/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 13.10.2010                                                      
 *                                                                              
 *  Completion Time: 13.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.internal.types.enumtypes.UdaConfigurationType;

/**
 * @author weiser
 * 
 */
@Entity
@NamedQueries({ @NamedQuery(name = "UdaDefinitionHistory.findByObject", query = "SELECT h FROM UdaDefinitionHistory h WHERE h.objKey=:objKey ORDER BY objversion") })
public class UdaDefinitionHistory extends
        DomainHistoryObject<UdaDefinitionData> {

    private static final long serialVersionUID = 2555734272121012990L;

    private long organizationObjKey;

    public UdaDefinitionHistory() {
        setDataContainer(new UdaDefinitionData());
    }

    public UdaDefinitionHistory(UdaDefinition udaDefinition) {
        super(udaDefinition);
        setOrganizationObjKey(udaDefinition.getOrganizationKey());
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

    public void setOrganizationObjKey(long organizationObjKey) {
        this.organizationObjKey = organizationObjKey;
    }

    public void setConfigurationType(UdaConfigurationType configurationType) {
        dataContainer.setConfigurationType(configurationType);
    }

    public UdaConfigurationType getConfigurationType() {
        return dataContainer.getConfigurationType();
    }
}
