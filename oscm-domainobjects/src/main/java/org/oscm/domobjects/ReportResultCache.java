/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * Holds all information used in the payment process of an subscription.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "cachekey" }))
@NamedQueries({
        @NamedQuery(name = "ReportResultCache.findByBusinessKey", query = "SELECT rc FROM ReportResultCache rc WHERE rc.cachekey=:cachekey"),
        @NamedQuery(name = "ReportResultCache.removeOldEntries", query = "DELETE FROM ReportResultCache rc WHERE rc.timestamp<:timestamp") })
@BusinessKey(attributes = { "cachekey" })
public class ReportResultCache extends
        DomainObjectWithVersioning<EmptyDataContainer> {

    private static final long serialVersionUID = -6694065053490507795L;

    @Column(insertable = true, updatable = false, nullable = false)
    private String cachekey;

    @Column(insertable = true, updatable = false, nullable = false)
    private Date timestamp;

    @Column(insertable = true, updatable = false, nullable = false)
    private byte[] report;

    public ReportResultCache() {
        super();
    }

    public void setCachekey(String cachekey) {
        this.cachekey = cachekey;
    }

    public String getCachekey() {
        return cachekey;
    }

    public void setReport(byte[] report) {
        this.report = report;
    }

    public byte[] getReport() {
        return report;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
