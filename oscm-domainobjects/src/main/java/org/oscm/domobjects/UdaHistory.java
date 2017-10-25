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

/**
 * @author weiser
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "UdaHistory.findByObject", query = "SELECT h FROM UdaHistory h WHERE h.objKey=:objKey ORDER BY objversion"),
        @NamedQuery(name = "UdaHistory.findForOrg", query = "SELECT uh, udh.dataContainer.udaId FROM UdaHistory uh, UdaDefinitionHistory udh WHERE udh.organizationObjKey = :organizationObjKey AND uh.udaDefinitionObjKey = udh.objKey AND uh.objVersion = (SELECT max(subUh.objVersion) FROM UdaHistory subUh WHERE subUh.objKey = uh.objKey) AND uh.modType != :ignoredModType AND uh.dataContainer.targetObjectKey = :targetObjectKey AND udh.dataContainer.targetType = :udaTargetType AND udh.objVersion = (SELECT max(subUdh.objVersion) FROM UdaDefinitionHistory subUdh WHERE udh.objKey = subUdh.objKey) ORDER BY uh.objKey ASC"),
        @NamedQuery(name = "UdaHistory.findForSub", query = "SELECT uh, udh.dataContainer.udaId FROM UdaHistory uh, UdaDefinitionHistory udh, SubscriptionHistory sh WHERE udh.organizationObjKey = :organizationObjKey AND uh.udaDefinitionObjKey = udh.objKey AND sh.objKey = uh.dataContainer.targetObjectKey AND uh.dataContainer.targetObjectKey = :targetObjectKey AND udh.dataContainer.targetType = :udaTargetType AND uh.modType != :ignoredModType AND udh.objVersion = (SELECT max(subUdh.objVersion) FROM UdaDefinitionHistory subUdh WHERE udh.objKey = subUdh.objKey) AND ( (uh.objVersion = (SELECT max(subUh.objVersion) FROM UdaHistory subUh WHERE subUh.objKey = uh.objKey) AND sh.objVersion = (SELECT max(shInt.objVersion) FROM SubscriptionHistory shInt WHERE shInt.objKey = sh.objKey)) OR (sh.objVersion = (SELECT max(shInt.objVersion) FROM SubscriptionHistory shInt WHERE shInt.objKey = sh.objKey AND shInt.modType = :modTypeDeleted) AND uh.objVersion = (SELECT max(subUh.objVersion) FROM UdaHistory subUh WHERE subUh.objKey = uh.objKey AND subUh.modDate < sh.modDate) )) ORDER BY uh.objKey ASC") })
public class UdaHistory extends DomainHistoryObject<UdaData> {

    private static final long serialVersionUID = -5127696392991000907L;

    private long udaDefinitionObjKey;

    public UdaHistory() {
        setDataContainer(new UdaData());
    }

    public UdaHistory(Uda uda) {
        super(uda);
        setUdaDefinitionObjKey(uda.getUdaDefinition().getKey());
    }

    public void setUdaDefinitionObjKey(long udaDefinitionObjKey) {
        this.udaDefinitionObjKey = udaDefinitionObjKey;
    }

    public long getUdaDefinitionObjKey() {
        return udaDefinitionObjKey;
    }
}
