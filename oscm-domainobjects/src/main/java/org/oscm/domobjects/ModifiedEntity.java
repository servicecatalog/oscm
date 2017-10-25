/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.ModifiedEntityType;

/**
 * ModifiedEntity stores information which has been asynchronous updated but not
 * complete.
 * 
 * @author Qiu
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "ModifiedEntity.findByBusinessKey", query = "SELECT m FROM ModifiedEntity m WHERE m.dataContainer.targetObjectKey = :targetObjectKey AND m.dataContainer.targetObjectType = :targetObjectType"),
        @NamedQuery(name = "ModifiedEntity.countSubscriptionWithOrgIdAndSubId", query = "SELECT COUNT (m) FROM ModifiedEntity m WHERE m.dataContainer.targetObjectKey IN (select me.dataContainer.targetObjectKey from ModifiedEntity me where me.dataContainer.targetObjectType = :subOrgIdType and me.dataContainer.value = :organizationId)"
                + " AND m.dataContainer.targetObjectType = :subIdType AND m.dataContainer.value = :subscriptionId"),
        @NamedQuery(name = "ModifiedEntity.findSubscriptionKeyByOrgIdAndSubId", query = "SELECT m.dataContainer.targetObjectKey FROM ModifiedEntity m WHERE m.dataContainer.targetObjectKey IN (select me.dataContainer.targetObjectKey from ModifiedEntity me where me.dataContainer.targetObjectType = :subOrgIdType and me.dataContainer.value = :organizationId)"
                + " AND m.dataContainer.targetObjectType = :subIdType AND m.dataContainer.value = :subscriptionId"),
        @NamedQuery(name = "ModifiedEntity.findByObjectAndTypes", query = "SELECT m FROM ModifiedEntity m WHERE m.dataContainer.targetObjectKey = :targetObjectKey AND m.dataContainer.targetObjectType IN (:targetObjectTypes)"),
        @NamedQuery(name = "ModifiedEntity.deleteByObjectAndTypes", query = "DELETE FROM ModifiedEntity m WHERE m.dataContainer.targetObjectKey = :targetObjectKey AND m.dataContainer.targetObjectType IN (:targetObjectTypes)") })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "targetObjectKey",
        "targetObjectType" }))
@BusinessKey(attributes = { "targetObjectKey", "targetObjectType" })
public class ModifiedEntity extends
        DomainObjectWithVersioning<ModifiedEntityData> {

    private static final long serialVersionUID = -1844757495950320851L;

    public ModifiedEntity() {
        super();
        dataContainer = new ModifiedEntityData();
    }

    public ModifiedEntity(long targetObjectKey,
            ModifiedEntityType targetObjectType) {
        super();
        setTargetObjectKey(targetObjectKey);
        setTargetObjectType(targetObjectType);
    }

    public long getTargetObjectKey() {
        return dataContainer.getTargetObjectKey();
    }

    public void setTargetObjectKey(long targetObjectKey) {
        dataContainer.setTargetObjectKey(targetObjectKey);
    }

    public String getValue() {
        return dataContainer.getValue();
    }

    public void setValue(String value) {
        dataContainer.setValue(value);
    }

    public ModifiedEntityType getTargetObjectType() {
        return dataContainer.getTargetObjectType();
    }

    public void setTargetObjectType(ModifiedEntityType targetObjectType) {
        dataContainer.setTargetObjectType(targetObjectType);
    }

}
