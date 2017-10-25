/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "directRevenue", "brokerRevenue",
        "resellerRevenue" })
@XmlRootElement(name = "SupplierRevenue")
public class SupplierRevenue {
    @XmlElement(name = "DirectRevenue")
    protected DirectRevenue directRevenue;

    @XmlElement(name = "BrokerRevenue")
    protected BrokerRevenue brokerRevenue;

    @XmlElement(name = "ResellerRevenue")
    protected ResellerRevenue resellerRevenue;

    @XmlAttribute(name = "amount", required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal amount = BigDecimal.ZERO;

    public BigDecimal getAmount() {
        return amount;
    }

    public void sumUp(BigDecimal valueToAdd) {
        amount = amount.add(valueToAdd);
    }

    public DirectRevenue getDirectRevenue() {
        return directRevenue;
    }

    public void setDirectRevenue(DirectRevenue directRevenue) {
        this.directRevenue = directRevenue;
    }

    public BrokerRevenue getBrokerRevenue() {
        return brokerRevenue;
    }

    public void setBrokerRevenue(BrokerRevenue brokerRevenue) {
        this.brokerRevenue = brokerRevenue;
    }

    public ResellerRevenue getResellerRevenue() {
        return resellerRevenue;
    }

    public void setResellerRevenue(ResellerRevenue resellerRevenue) {
        this.resellerRevenue = resellerRevenue;
    }

    /**
     * @param marketplace
     */
    public void calculate(List<Marketplace> marketplace) {
        for (Marketplace mp : marketplace) {
            List<Service> services = mp.getService();
            if (!services.isEmpty()) {
                for (Service service : services) {
                    sumUp(service.getRevenueShareDetails()
                            .getAmountForSupplier());
                    if (OfferingType.DIRECT.name().equals(service.getModel())) {
                        sumUpDirectRevenue(service);
                    } else if (OfferingType.RESELLER.name().equals(
                            service.getModel())) {
                        sumUpResellerRevenue(service);
                    } else if (OfferingType.BROKER.name().equals(
                            service.getModel())) {
                        sumUpBrokerRevenue(service);
                    }
                }
            }
        }
        if (resellerRevenue != null) {
            resellerRevenue.calculate();
        }
    }

    private void sumUpDirectRevenue(Service service) {
        if (directRevenue == null) {
            directRevenue = new DirectRevenue();
        }
        directRevenue.sumMarketplaceRevenue(service.getRevenueShareDetails()
                .getMarketplaceRevenue());
        directRevenue.sumOperatorRevenue(service.getRevenueShareDetails()
                .getOperatorRevenue());
        directRevenue.sumServiceRevenue(service.getRevenueShareDetails()
                .getServiceRevenue());
    }

    private void sumUpResellerRevenue(Service service) {
        if (resellerRevenue == null) {
            resellerRevenue = new ResellerRevenue();
        }
        resellerRevenue.sumMarketplaceRevenue(service.getRevenueShareDetails()
                .getMarketplaceRevenue());
        resellerRevenue.sumOperatorRevenue(service.getRevenueShareDetails()
                .getOperatorRevenue());
        resellerRevenue.sumServiceRevenue(service.getRevenueShareDetails()
                .getServiceRevenue());
        resellerRevenue.sumResellerRevenue(service.getRevenueShareDetails()
                .getResellerRevenue());
    }

    private void sumUpBrokerRevenue(Service service) {
        if (brokerRevenue == null) {
            brokerRevenue = new BrokerRevenue();
        }
        brokerRevenue.sumMarketplaceRevenue(service.getRevenueShareDetails()
                .getMarketplaceRevenue());
        brokerRevenue.sumOperatorRevenue(service.getRevenueShareDetails()
                .getOperatorRevenue());
        brokerRevenue.sumServiceRevenue(service.getRevenueShareDetails()
                .getServiceRevenue());
        brokerRevenue.sumBrokerRevenue(service.getRevenueShareDetails()
                .getBrokerRevenue());
    }

}
