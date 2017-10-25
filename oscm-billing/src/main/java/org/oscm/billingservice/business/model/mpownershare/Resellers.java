/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;
import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "organization" })
@XmlRootElement(name = "Resellers")
public class Resellers {

    @XmlAttribute(name = "amount")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amount;

    @XmlAttribute(name = "totalAmount")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal totalAmount;

    @XmlAttribute(name = "marketplaceRevenue")
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal marketplaceRevenue;

    @XmlElement(name = "Organization", required = true)
    protected List<Organization> organization = new LinkedList<Organization>();

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getMarketplaceRevenue() {
        return marketplaceRevenue;
    }

    public void setMarketplaceRevenue(BigDecimal marketplaceRevenue) {
        this.marketplaceRevenue = marketplaceRevenue;
    }

    public List<Organization> getOrganization() {
        return organization;
    }

    public void setOrganization(List<Organization> organization) {
        this.organization = organization;
    }

    public void addReseller(Organization org) {
        if (org != null) {
            getOrganization().add(org);
        }
    }

    public Organization getOrganization(String organizationId) {
        for (Organization organization : getOrganization()) {
            if (organization.identifier.equals(organizationId)) {
                return organization;
            }
        }
        return null;
    }

    public void calculate() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal marketplaceRevenue = BigDecimal.ZERO;

        for (Organization org : getOrganization()) {
            totalAmount = totalAmount.add(org.getTotalAmount());
            amount = amount.add(org.getAmount());
            marketplaceRevenue = marketplaceRevenue.add(org
                    .getMarketplaceRevenue());
        }

        this.totalAmount = totalAmount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        this.amount = amount.setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        this.marketplaceRevenue = marketplaceRevenue.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
    }
}
