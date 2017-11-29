/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.model.brokershare.Currency;
import org.oscm.billingservice.business.model.brokershare.Service;
import org.oscm.billingservice.business.model.brokershare.ServiceCustomerRevenue;
import org.oscm.billingservice.business.model.brokershare.ServiceRevenue;
import org.oscm.billingservice.business.model.brokershare.Supplier;

public class BrokerShareCalculationTest {
    @Test
    public void calculate_Currency() {
        // given
        Currency currency = new Currency();
        addSupplier1(currency);
        addSupplier2(currency);

        // when
        currency.calculate();

        System.out.println(currency.getBrokerRevenue().getAmount());
        // then
        assertNotNull(currency.getBrokerRevenue().getAmount());
        // share revenues supplier1 : 1.51, 0.15, 0.16, 0.2
        // share revenues supplier2 : 100
        assertEquals(
                0,
                currency.getBrokerRevenue().getAmount()
                        .compareTo(BigDecimal.valueOf(102.02)));
    }

    private void addSupplier1(Currency currency) {
        Supplier supplier = new Supplier();
        addService(supplier, BigDecimal.valueOf(15.1));
        addService(supplier, BigDecimal.valueOf(1.51));
        addService(supplier, BigDecimal.valueOf(1.55));
        addService(supplier, BigDecimal.valueOf(1.9941));
        currency.addSupplier(supplier);
    }

    private void addSupplier2(Currency currency) {
        Supplier supplier = new Supplier();
        addService(supplier, BigDecimal.valueOf(1000));
        currency.addSupplier(supplier);
    }

    private void addService(Supplier supplier, BigDecimal baseServiceRevenue) {
        Service service = new Service();
        ServiceCustomerRevenue customerRevenue = new ServiceCustomerRevenue();
        customerRevenue.setTotalAmount(baseServiceRevenue);
        customerRevenue.setBrokerRevenueSharePercentage(BigDecimal.TEN);
        ServiceRevenue serviceRevenue = new ServiceRevenue();
        serviceRevenue.getServiceCustomerRevenues().add(customerRevenue);
        serviceRevenue.setBrokerRevenueSharePercentage(BigDecimal.TEN); // 10%
        serviceRevenue.setTotalAmount(baseServiceRevenue);
        service.setServiceRevenue(serviceRevenue);
        supplier.getService().add(service);
    }

    @Test
    public void calculate_Supplier() {
        // given
        Supplier supplier = new Supplier();
        addService(supplier, BigDecimal.valueOf(30));
        addService(supplier, BigDecimal.valueOf(2.55));
        addService(supplier, BigDecimal.valueOf(2.045));
        // when
        supplier.calculate();

        // then
        assertNotNull(supplier.getBrokerRevenuePerSupplier());
        // share revenues : 3, 0.26, 0.21 = 3.47
        assertEquals(0, supplier.getBrokerRevenuePerSupplier().getAmount()
                .compareTo(BigDecimal.valueOf(3.47)));
    }

    @Test
    public void calculate_ServiceRevenue_rounding() {
        calculate_ServiceRevenueInt(BigDecimal.valueOf(0.99),
                BigDecimal.valueOf(0.1));
    }

    @Test
    public void calculate_ServiceRevenue() {
        calculate_ServiceRevenueInt(BigDecimal.valueOf(0.9),
                BigDecimal.valueOf(0.09));
    }

    private void calculate_ServiceRevenueInt(BigDecimal baseRevenue,
            BigDecimal expectedServiceRevenueShare) {
        // given
        ServiceRevenue revenue = new ServiceRevenue();
        revenue.setTotalAmount(baseRevenue);
        revenue.setBrokerRevenueSharePercentage(BigDecimal.valueOf(10));

        ServiceCustomerRevenue revenueCustomer = new ServiceCustomerRevenue();
        revenueCustomer.setTotalAmount(baseRevenue);
        revenueCustomer.setBrokerRevenueSharePercentage(BigDecimal.valueOf(10));

        revenue.getServiceCustomerRevenues().add(revenueCustomer);

        // when
        revenue.calculate();

        // then
        assertNotNull(revenue.getBrokerRevenue());
        assertEquals(
                0,
                revenue.getBrokerRevenue().compareTo(
                        expectedServiceRevenueShare));
    }

    @Test
    public void calculate_BrokerRevenueShareResult() {
    }
}
