/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * History-Object of Organization, used for auditing. Will be automatically
 * created during persist, save or remove operations (if performed via
 * DataManager)
 * 
 * @author schmid
 */
@Entity
@NamedQueries( {
        @NamedQuery(name = "PlatformUserHistory.findByObject", query = "select c from PlatformUserHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "PlatformUserHistory.findUnconfirmedUserForRemovedOrganization", query = "SELECT c FROM PlatformUserHistory c, OrganizationHistory cust WHERE c.organizationObjKey = cust.objKey AND c.dataContainer.status = :userStatus AND c.modType = :modType AND cust.modType = :modType AND c.dataContainer.userId = :userId AND cust.dataContainer.organizationId = :organizationId") })
public class PlatformUserHistory extends
        DomainHistoryObject<PlatformUserData> {

    private static final long serialVersionUID = 1L;

    public PlatformUserHistory() {
        dataContainer = new PlatformUserData();
    }

    /**
     * Constructs PlatformUserHistory from a PlatformUser domain object
     * 
     * @param c
     *            - the PlatformUser
     */
    public PlatformUserHistory(PlatformUser c) {
        super(c);
        setOrganizationObjKey(c.getOrganization().getKey());
    }

    /**
     * Reference to the Organization (only id)
     */
    @Column(nullable = false)
    private long organizationObjKey;

    public void setOrganizationObjKey(long organization_objid) {
        this.organizationObjKey = organization_objid;
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

}
