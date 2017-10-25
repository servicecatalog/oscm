/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Organization")
public class Organization {

    @XmlAttribute(name = "identifier", required = true)
    protected String identifier;

    @XmlAttribute(name = "name")
    protected String name;

    @XmlAttribute(name = "amount", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amount = BigDecimal.ZERO;

    @XmlAttribute(name = "marketplaceRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenue = BigDecimal.ZERO;

    @XmlAttribute(name = "totalAmount")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal totalAmount = BigDecimal.ZERO;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(BigDecimal marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Organization() {
    }

    public Organization(String organizationId) {
        setIdentifier(organizationId);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String value) {
        this.identifier = value;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }
}
