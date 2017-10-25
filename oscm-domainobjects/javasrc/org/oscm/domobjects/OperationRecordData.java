/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014年9月17日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Date;

import javax.persistence.*;

import org.oscm.domobjects.converters.OSConverter;
import org.oscm.internal.types.enumtypes.OperationStatus;

/**
 * @author yuyin
 * 
 */
@Embeddable
public class OperationRecordData extends DomainDataContainer {

    private static final long serialVersionUID = -4895792027394033780L;

    /**
     * The state of the Operation (RUNNING, FINISHED, ERROR).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    /**
     * The unique id for transaction between CTMG and APP.
     */
    @Column(nullable = false)
    private String transactionid;

    /**
     * Date of execution of operation
     */
    @Column(nullable = false)
    private long executiondate;

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }

    public Date getExecutiondate() {
        if (executiondate != 0) {
            return new Date(executiondate);
        } else {
            return null;
        }
    }

    public void setExecutiondate(Date executiondate) {
        this.executiondate = executiondate.getTime();
    }

}
