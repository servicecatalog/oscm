/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "marketplace", "supplierRevenue" })
@XmlRootElement(name = "Currency")
public class Currency {

    @XmlElement(name = "Marketplace", required = true)
    protected List<Marketplace> marketplace;

    @XmlElement(name = "SupplierRevenue")
    private SupplierRevenue supplierRevenue;

    @XmlAttribute(name = "id", required = true)
    protected String id;

    public Currency() {
    }

    public Currency(String id) {
        setId(id);
    }

    public List<Marketplace> getMarketplace() {
        if (marketplace == null) {
            marketplace = new ArrayList<Marketplace>();
        }
        return this.marketplace;
    }

    public Marketplace getMarketplace(long marketplaceKey) {
        for (Marketplace marketplace : getMarketplace()) {
            if (marketplace.getKey().longValue() == marketplaceKey) {
                return marketplace;
            }
        }
        return null;

    }

    public void addMarketplace(Marketplace marketplace) {
        getMarketplace().add(marketplace);
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public void calculate() {
        for (Marketplace marketplace : getMarketplace()) {
            marketplace.calculate();
        }
        if (supplierRevenue == null) {
            supplierRevenue = new SupplierRevenue();
        }
        supplierRevenue.calculate(marketplace);
    }

    public void setSupplierRevenue(SupplierRevenue supplierRevenue) {
        this.supplierRevenue = supplierRevenue;
    }

    public SupplierRevenue getSupplierRevenue() {
        return supplierRevenue;
    }
}
