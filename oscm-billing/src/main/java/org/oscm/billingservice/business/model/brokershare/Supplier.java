/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.oscm.converter.PriceConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "organizationData", "service",
        "brokerRevenuePerSupplier" })
@XmlRootElement(name = "Supplier")
public class Supplier {

    @XmlElement(name = "OrganizationData", required = true)
    protected OrganizationData organizationData;

    @XmlElement(name = "Service", required = true)
    protected List<Service> service;

    @XmlElement(name = "BrokerRevenuePerSupplier", required = true)
    protected BrokerRevenuePerSupplier brokerRevenuePerSupplier = new BrokerRevenuePerSupplier();

    public OrganizationData getOrganizationData() {
        return organizationData;
    }

    public void setOrganizationData(OrganizationData value) {
        this.organizationData = value;
    }

    public List<Service> getService() {
        if (service == null) {
            service = new ArrayList<Service>();
        }
        return this.service;
    }

    public Service getServiceByKey(long productKey) {
        for (Service service : getService()) {
            if (service.getKey().longValue() == productKey) {
                return service;
            }
        }
        return null;
    }

    public void addService(Service service) {
        getService().add(service);
    }

    public BrokerRevenuePerSupplier getBrokerRevenuePerSupplier() {
        return brokerRevenuePerSupplier;
    }

    public void calculate() {
        BigDecimal supplierRevenueAmount = BigDecimal.ZERO;
        BigDecimal supplierRevenueTotalAmount = BigDecimal.ZERO;

        for (Service service : getService()) {
            service.getServiceRevenue().calculate();
            supplierRevenueAmount = supplierRevenueAmount.add(service
                    .getServiceRevenue().getBrokerRevenue());
            supplierRevenueTotalAmount = supplierRevenueTotalAmount.add(service
                    .getServiceRevenue().getTotalAmount());
        }
        supplierRevenueAmount = supplierRevenueAmount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        brokerRevenuePerSupplier.setAmount(supplierRevenueAmount);
        supplierRevenueTotalAmount = supplierRevenueTotalAmount.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        brokerRevenuePerSupplier.setTotalAmount(supplierRevenueTotalAmount);
    }
}
