/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: 26.05.2011                                                      
 *                                                                              
 *  Completion Time: 26.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

/**
 * Relation entity between the master user and the slave user. The master user
 * can create a new user called slave user that belongs to one of his users.
 * 
 * @author tokoda
 * 
 */
@Entity
@NamedQuery(name = "OnBehalfUserReference.findInactiveBeforePeriod", query = "SELECT u FROM OnBehalfUserReference u WHERE u.dataContainer.lastAccessTime < :leastPermittedTime")
public class OnBehalfUserReference extends
        DomainObjectWithVersioning<OnBehalfUserReferenceData> {

    private static final long serialVersionUID = 6961787812028179143L;

    public OnBehalfUserReference() {
        setDataContainer(new OnBehalfUserReferenceData());
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PlatformUser masterUser;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
    private PlatformUser slaveUser;

    public PlatformUser getMasterUser() {
        return masterUser;
    }

    public void setMasterUser(PlatformUser masterUser) {
        this.masterUser = masterUser;
    }

    public PlatformUser getSlaveUser() {
        return slaveUser;
    }

    public void setSlaveUser(PlatformUser slaveUser) {
        this.slaveUser = slaveUser;
    }

    /**
     * Refer to {@link OnBehalfUserReferenceData#lastAccessTime}
     */
    public long getLastAccessTime() {
        return dataContainer.getLastAccessTime();
    }

    /**
     * Refer to {@link OnBehalfUserReferenceData#lastAccessTime}
     */
    public void setLastAccessTime(long lastAccessTime) {
        dataContainer.setLastAccessTime(lastAccessTime);
    }

}
