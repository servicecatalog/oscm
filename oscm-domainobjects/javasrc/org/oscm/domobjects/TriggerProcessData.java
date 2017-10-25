/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                             
 *                                                                              
 *  Creation Date: 14.06.2010                                                      
 *                                                                              
 *  Completion Time: 15.06.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.TPPNConverter;
import org.oscm.domobjects.converters.TPSConverter;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;

/**
 * JPA managed entity representing the trigger process data.
 * 
 * @author pock
 * 
 */
@Embeddable
public class TriggerProcessData extends DomainDataContainer {

    private static final long serialVersionUID = 8595544701217446596L;

    /**
     * The state of the process (ACTIVE, CANCELLED, FAILED, APPROVED, ERROR,
     * REJECTED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerProcessStatus status;

    /**
     * Trigger process activation (creation) date
     */
    @Column(nullable = false)
    private long activationDate;

    public TriggerProcessStatus getStatus() {
        return status;
    }

    public void setState(TriggerProcessStatus status) {
        this.status = status;
    }

    public long getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(long activationDate) {
        this.activationDate = activationDate;
    }
}
