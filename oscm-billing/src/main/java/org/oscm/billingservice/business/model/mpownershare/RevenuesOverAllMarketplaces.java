/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "brokers", "resellers", "suppliers",
        "marketplaceOwner" })
@XmlRootElement(name = "RevenuesOverAllMarketplaces")
public class RevenuesOverAllMarketplaces {
    @XmlElement(name = "Brokers", required = true)
    protected Brokers brokers = new Brokers();

    @XmlElement(name = "Resellers", required = true)
    protected Resellers resellers = new Resellers();

    @XmlElement(name = "Suppliers", required = true)
    protected Suppliers suppliers = new Suppliers();

    @XmlElement(name = "MarketplaceOwner", required = true)
    protected MarketplaceOwner marketplaceOwner = new MarketplaceOwner();

    public Brokers getBrokers() {
        return brokers;
    }

    public void setBrokers(Brokers brokers) {
        this.brokers = brokers;
    }

    public void addBroker(Organization organization) {
        boolean isEmptyOrNew = true;
        for (Organization org : brokers.getOrganization()) {
            if (org.getIdentifier().equals(organization.getIdentifier())) {
                isEmptyOrNew = false;
                Organization brokerOrg = brokers.getOrganization(org
                        .getIdentifier());
                BigDecimal amount = brokerOrg.getAmount().add(
                        organization.getAmount());
                BigDecimal marketplaceRevenue = brokerOrg
                        .getMarketplaceRevenue().add(
                                organization.getMarketplaceRevenue());
                BigDecimal totalAmount = brokerOrg.getTotalAmount().add(
                        organization.getTotalAmount());
                brokerOrg.setAmount(amount);
                brokerOrg.setMarketplaceRevenue(marketplaceRevenue);
                brokerOrg.setTotalAmount(totalAmount);
            }
        }
        if (isEmptyOrNew) {
            brokers.addBroker(constructOrganization(organization));
        }
    }

    public Resellers getResellers() {
        return resellers;
    }

    public void setResellers(Resellers resellers) {
        this.resellers = resellers;
    }

    public void addReseller(Organization organization) {
        boolean isEmptyOrNew = true;
        for (Organization org : resellers.getOrganization()) {
            if (org.getIdentifier().equals(organization.getIdentifier())) {
                isEmptyOrNew = false;
                Organization resellerOrg = resellers.getOrganization(org
                        .getIdentifier());
                BigDecimal amount = resellerOrg.getAmount().add(
                        organization.getAmount());
                BigDecimal marketplaceRevenue = resellerOrg
                        .getMarketplaceRevenue().add(
                                organization.getMarketplaceRevenue());
                BigDecimal totalAmount = resellerOrg.getTotalAmount().add(
                        organization.getTotalAmount());
                resellerOrg.setAmount(amount);
                resellerOrg.setMarketplaceRevenue(marketplaceRevenue);
                resellerOrg.setTotalAmount(totalAmount);
            }
        }
        if (isEmptyOrNew) {
            resellers.addReseller(constructOrganization(organization));
        }
    }

    public Suppliers getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Suppliers suppliers) {
        this.suppliers = suppliers;
    }

    public void addOrUpdateSupplier(Organization organization) {
        boolean isEmptyOrNew = true;
        for (Organization org : suppliers.getOrganization()) {
            if (org.getIdentifier().equals(organization.getIdentifier())) {
                isEmptyOrNew = false;
                Organization supplierOrg = suppliers.getOrganization(org
                        .getIdentifier());

                BigDecimal amount = supplierOrg.getAmount().add(
                        organization.getAmount());
                supplierOrg.setAmount(amount);

                BigDecimal marketplaceRevenue = supplierOrg
                        .getMarketplaceRevenue().add(
                                organization.getMarketplaceRevenue());
                supplierOrg.setMarketplaceRevenue(marketplaceRevenue);

                BigDecimal totalAmount = supplierOrg.getTotalAmount().add(
                        organization.getTotalAmount());
                supplierOrg.setTotalAmount(totalAmount);
            }
        }
        if (isEmptyOrNew) {
            suppliers.addSupplier(constructOrganization(organization));
        }
    }

    public MarketplaceOwner getMarketplaceOwner() {
        return marketplaceOwner;
    }

    public void setMarketplaceOwner(MarketplaceOwner marketplaceOwner) {
        this.marketplaceOwner = marketplaceOwner;
    }

    public Organization constructOrganization(Organization org) {
        Organization organization = new Organization();
        organization.setAmount(org.getAmount());
        organization.setIdentifier(org.getIdentifier());
        organization.setMarketplaceRevenue(org.getMarketplaceRevenue());
        organization.setName(org.getName());
        organization.setTotalAmount(org.getTotalAmount());
        return organization;
    }

    public void calculate() {
        BigDecimal mpAmount = BigDecimal.ZERO;
        brokers.calculate();
        resellers.calculate();
        suppliers.calculate();
        mpAmount = mpAmount.add(brokers.getMarketplaceRevenue());
        mpAmount = mpAmount.add(resellers.getMarketplaceRevenue());
        mpAmount = mpAmount.add(suppliers.getMarketplaceRevenue());
        marketplaceOwner.setAmount(mpAmount);
    }

}
