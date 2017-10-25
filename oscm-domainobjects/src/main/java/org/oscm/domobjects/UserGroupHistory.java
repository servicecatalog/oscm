/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-6-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * This object represents a user group history.
 * 
 * @author stavreva
 */
@Entity
@NamedQueries({ @NamedQuery(name = "UserGroupHistory.findLastValidForEndPeriod", query = "SELECT obj FROM UserGroupHistory obj WHERE obj.objKey = :objKey AND obj.modDate < :endDate ORDER BY obj.objVersion DESC, obj.modDate DESC"), })
public class UserGroupHistory extends DomainHistoryObject<UserGroupData> {

    private static final long serialVersionUID = -1916686962781487872L;

    public UserGroupHistory() {
        super();
        dataContainer = new UserGroupData();
    }

    /**
     * Constructs UserGroupHistory from a UserGroup domain object
     * 
     * @param c
     *            - the UserGroup
     */
    public UserGroupHistory(UserGroup c) {
        super(c);
        if (c.getOrganization() != null) {
            setOrganizationObjKey(Long.valueOf(c.getOrganization().getKey()));
        }
    }

    @Column(nullable = false)
    private Long organizationObjKey;

    public String getName() {
        return dataContainer.getName();
    }

    public void setName(String name) {
        this.dataContainer.setName(name);
    }

    public String getDescription() {
        return dataContainer.getDescription();
    }

    public void setDescription(String description) {
        this.dataContainer.setDescription(description);
    }

    public boolean isDefault() {
        return dataContainer.isDefault();
    }

    public void setIsDefault(boolean isDefault) {
        this.dataContainer.setIsDefault(isDefault);
    }

    public Long getOrganizationObjkey() {
        return organizationObjKey;
    }

    public void setOrganizationObjKey(Long organizationObjKey) {
        this.organizationObjKey = organizationObjKey;
    }

    public String getReferenceId() {
        return this.dataContainer.getReferenceId();
    }

    public void setReferenceId(String referenceId) {
        this.dataContainer.setReferenceId(referenceId);
    }

}
