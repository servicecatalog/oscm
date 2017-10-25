/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014年9月17日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.OperationStatus;

/**
 * @author yuyin
 * 
 */
@BusinessKey(attributes = { "transactionid" })
@NamedQueries({
        @NamedQuery(name = "OperationRecord.findByOrgKey", query = "select o from OperationRecord o where o.subscription.organizationKey = :organization_tkey ORDER BY o.dataContainer.executiondate"),
        @NamedQuery(name = "OperationRecord.findByBusinessKey", query = "select o from OperationRecord o where o.dataContainer.transactionid = :transactionid"),
        @NamedQuery(name = "OperationRecord.findBySubOwnerKey", query = "select o from OperationRecord o where o.subscription.owner.key = :subscriptionOwner_tkey OR o.user.key = :subscriptionOwner_tkey ORDER BY o.dataContainer.executiondate"),
        @NamedQuery(name = "OperationRecord.findByUserKey", query = "select o from OperationRecord o where o.user.key = :platformUser_tkey ORDER BY o.dataContainer.executiondate"),
        @NamedQuery(name = "OperationRecord.removeBySubscriptionKey", query = "delete from OperationRecord o where o.subscription.key = :subscription_tkey") })
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "transactionid" }))
public class OperationRecord extends
        DomainObjectWithVersioning<OperationRecordData> {

    private static final long serialVersionUID = -4895792027394033780L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PlatformUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private TechnicalProductOperation technicalProductOperation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Subscription subscription;

    public OperationRecord() {
        super();
        dataContainer = new OperationRecordData();
    }

    /**
     * Fill creation date (if not already set)
     */
    @PrePersist
    public void fillCreationDate() {
        if (getExecutiondate() == null) {
            setExecutiondate(GregorianCalendar.getInstance().getTime());
        }
    }

    public TechnicalProductOperation getTechnicalProductOperation() {
        return technicalProductOperation;
    }

    public void setTechnicalProductOperation(
            TechnicalProductOperation technicalProductOperation) {
        this.technicalProductOperation = technicalProductOperation;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void setUser(PlatformUser user) {
        this.user = user;
    }

    public PlatformUser getUser() {
        return user;
    }

    // -----------------------
    // dataContainer delegates
    public OperationStatus getStatus() {
        return dataContainer.getStatus();
    }

    public void setStatus(OperationStatus status) {
        dataContainer.setStatus(status);
    }

    /**
     * Refer to {@link PlatformUserData#creationDate}
     */
    public Date getExecutiondate() {
        return dataContainer.getExecutiondate();
    }

    /**
     * Refer to {@link PlatformUserData#creationDate}
     */
    public void setExecutiondate(Date creationDate) {
        dataContainer.setExecutiondate(creationDate);
    }

    public String getTransactionid() {
        return dataContainer.getTransactionid();
    }

    public void setTransactionid(String transactionid) {
        dataContainer.setTransactionid(transactionid);
    }
}
