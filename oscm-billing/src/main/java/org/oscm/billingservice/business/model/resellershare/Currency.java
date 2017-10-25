/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplier", "resellerRevenue" })
@XmlRootElement(name = "Currency")
public class Currency {

    @XmlElement(name = "Supplier", required = true)
    protected List<Supplier> supplier;

    @XmlElement(name = "ResellerRevenue", required = true)
    protected ResellerRevenue resellerRevenue = new ResellerRevenue();

    @XmlAttribute(name = "id", required = true)
    protected String id;

    public Currency() {
    }

    public Currency(String id) {
        setId(id);
    }

    public List<Supplier> getSupplier() {
        if (supplier == null) {
            supplier = new ArrayList<Supplier>();
        }
        return this.supplier;
    }

    public Supplier getSupplierByKey(long supplierKey) {
        for (Supplier supplier : getSupplier()) {
            if (supplier.getOrganizationData().getKey().longValue() == supplierKey) {
                return supplier;
            }
        }
        return null;

    }

    public void addSupplier(Supplier supplier) {
        getSupplier().add(supplier);
    }

    public ResellerRevenue getResellerRevenue() {
        return resellerRevenue;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public void calculate() {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Supplier supplier : getSupplier()) {
            supplier.calculate();
            amount = amount.add(supplier
                    .getResellerRevenuePerSupplier().getAmount());
            totalAmount = totalAmount.add(supplier
                    .getResellerRevenuePerSupplier().getTotalAmount());
        }

        amount = amount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        totalAmount = totalAmount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        resellerRevenue.setTotalAmount(totalAmount);
        resellerRevenue.setAmount(amount);
        resellerRevenue.setPurchasePrice(totalAmount.subtract(
                amount).setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE));
    }
}
