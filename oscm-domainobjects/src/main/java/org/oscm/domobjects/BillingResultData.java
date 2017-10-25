/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 30.07.2009                                                      
 *                                                                              
 *  Completion Time: 04.08.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.IOException;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.oscm.converter.XMLConverter;

/**
 * Data Container for the BillingResult domain object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class BillingResultData extends DomainDataContainer {

    private static final long serialVersionUID = -7081672655363845549L;

    /**
     * The result of the billing run as XML string.
     */
    @Column(nullable = false)
    private String resultXML;

    /**
     * The time when the billing result was generated.
     */
    @Column(nullable = false)
    private long creationTime;

    /**
     * The technical key of the organization this billing result belongs to. The
     * reference is not a foreign key constraint, but just an additional
     * information (organization might already have been deleted).
     */
    @Column(nullable = false)
    private long organizationTKey;

    /**
     * The technical key of the charging organization. The reference is not a
     * foreign key constraint, but just an additional information (organization
     * might already have been deleted).
     */
    @Column(nullable = false)
    private long chargingOrgKey;

    /**
     * The technical key of the vendor organization. The reference is not a
     * foreign key constraint, but just an additional information (organization
     * might already have been deleted).
     */
    @Column(nullable = false)
    private long vendorKey;

    /**
     * The time the billing period started at.
     */
    @Column(nullable = false)
    private long periodStartTime;

    /**
     * The time the billing period ended at.
     */
    @Column(nullable = false)
    private long periodEndTime;

    /**
     * The subscription for this billing.
     */
    private Long subscriptionKey;

    /**
     * Reference to the unit assigned to subscription.
     */
    private Long usergroup_tkey;

    /**
     * The gross amount for this billing.
     */
    @Column(nullable = false)
    private BigDecimal grossAmount;

    /**
     * The net amount for this billing.
     */
    @Column(nullable = false)
    private BigDecimal netAmount;

    /**
     * Holds the billing result document, only transient! Is set to null if
     * billing result xml will be updated.
     */
    @Transient
    Document document;

    /**
     * Holds the hash of the last read billing result xml
     */
    @Transient
    int xmlHash;

    public String getResultXML() {
        return resultXML;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setResultXML(String billingResult) {
        this.resultXML = billingResult;
        document = null;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getOrganizationTKey() {
        return organizationTKey;
    }

    public void setOrganizationTKey(long organizationTKey) {
        this.organizationTKey = organizationTKey;
    }

    public long getPeriodStartTime() {
        return periodStartTime;
    }

    public long getPeriodEndTime() {
        return periodEndTime;
    }

    public void setPeriodStartTime(long periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    public void setPeriodEndTime(long periodEndTime) {
        this.periodEndTime = periodEndTime;
    }

    public void setChargingOrgKey(long chargingOrgKey) {
        this.chargingOrgKey = chargingOrgKey;
    }

    public long getChargingOrgKey() {
        return chargingOrgKey;
    }

    public void setVendorKey(long vendorKey) {
        this.vendorKey = vendorKey;
    }

    public long getVendorKey() {
        return vendorKey;
    }

    public void setSubscriptionKey(Long subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public Long getSubscriptionKey() {
        return subscriptionKey;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    Document getDocument() throws ParserConfigurationException, SAXException,
            IOException {
        String resultXml = getResultXML();
        if (resultXml == null) {
            return null;
        }
        if (xmlHash != resultXml.hashCode() || document == null) {
            document = XMLConverter.convertToDocument(resultXml, true);
            xmlHash = resultXml.hashCode();
        }

        return document;
    }

    public Long getUsergroup_tkey() {
        return usergroup_tkey;
    }

    public void setUsergroup_tkey(Long usergroup_tkey) {
        this.usergroup_tkey = usergroup_tkey;
    }
}
