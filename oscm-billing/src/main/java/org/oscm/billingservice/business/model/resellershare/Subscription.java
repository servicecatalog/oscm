/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Subscription")
public class Subscription {

    @XmlElement(name = "Period", required = true)
    protected Period period;

    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "key", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger key;

    @XmlAttribute(name = "billingKey", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger billingKey;

    @XmlAttribute(name = "revenue", required = true)
    protected BigDecimal revenue;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public BigInteger getKey() {
        return key;
    }

    public void setKey(BigInteger value) {
        this.key = value;
    }

    public BigInteger getBillingKey() {
        return billingKey;
    }

    public void setBillingKey(BigInteger value) {
        this.billingKey = value;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal value) {
        this.revenue = value;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }

}
