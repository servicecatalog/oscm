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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * DataContainer for domain object Subscription
 * 
 * @see UsageLicense.java
 * 
 * @author schmid
 */
@Embeddable
public class UsageLicenseData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Date of assignment
     */
    @Column(nullable = false)
    private long assignmentDate;

    /**
     * The id, the user has in the subscribed service/application
     */
    private String applicationUserId;

    public long getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(long assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public void setApplicationUserId(String applicationUserId) {
        this.applicationUserId = applicationUserId;
    }

    public String getApplicationUserId() {
        return applicationUserId;
    }
}
