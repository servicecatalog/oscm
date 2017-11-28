/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.model.resellershare.Currency;
import org.oscm.billingservice.business.model.resellershare.ResellerRevenueShareResult;
import org.oscm.billingservice.business.model.resellershare.Service;
import org.oscm.billingservice.business.model.resellershare.ServiceCustomerRevenue;
import org.oscm.billingservice.business.model.resellershare.ServiceRevenue;
import org.oscm.billingservice.business.model.resellershare.Subscription;
import org.oscm.billingservice.business.model.resellershare.Supplier;

public class ResellerShareCalculationTest {

    @Test
    public void calculate_ResellerRevenueShareResult() {
        // given
        ResellerRevenueShareResult result = new ResellerRevenueShareResult();
        addCurrency_EUR(result);
        addCurrency_USD(result);

        // when
        result.calculateAllShares();

        // then
        assertNotNull(result.getCurrency());
        assertEquals(2, result.getCurrency().size());

        // verify EUR
        Currency currencyEUR = result.getCurrency().get(0);
        assertNotNull(currencyEUR.getResellerRevenue());
        assertEquals(
                0,
                currencyEUR.getResellerRevenue().getAmount()
                        .compareTo(BigDecimal.valueOf(3.86)));

        // verify USD
        Currency currencyUSD = result.getCurrency().get(1);
        assertNotNull(currencyUSD.getResellerRevenue());
        System.out.println(currencyUSD.getResellerRevenue().getAmount());
        assertEquals(
                0,
                currencyUSD.getResellerRevenue().getAmount()
                        .compareTo(BigDecimal.valueOf(0.23)));
    }

    private void addCurrency_EUR(ResellerRevenueShareResult result) {
        Currency currency = new Currency("EUR");
        result.getCurrency().add(currency);

        // add supplier
        Supplier supplier = new Supplier();
        currency.addSupplier(supplier);
        BigDecimal[] subscriptionRevenues = new BigDecimal[] {
                BigDecimal.valueOf(30), BigDecimal.valueOf(2.55),
                BigDecimal.valueOf(2.045) };
        // total revenue : 30 + 2.55 + 2.05 = 34.6
        addService(supplier, subscriptionRevenues);

        BigDecimal[] subscriptionRevenues2 = new BigDecimal[] { BigDecimal
                .valueOf(4) };
        // total revenue : 4
        addService(supplier, subscriptionRevenues2);
    }

    private void addCurrency_USD(ResellerRevenueShareResult result) {
        Currency currency = new Currency("USD");
        result.getCurrency().add(currency);

        Supplier supplier = new Supplier();
        currency.addSupplier(supplier);
        BigDecimal[] subscriptionRevenues = new BigDecimal[] { BigDecimal
                .valueOf(2.342) };

        addService(supplier, subscriptionRevenues);
    }

    private void addService(Supplier supplier,
            BigDecimal... subscriptionRevenues) {
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        for (BigDecimal revenue : subscriptionRevenues) {
            totalAmount = totalAmount.add(revenue);
        }
        ServiceCustomerRevenue customerRevenue = new ServiceCustomerRevenue();
        customerRevenue.setTotalAmount(totalAmount);
        customerRevenue.setResellerRevenueSharePercentage(BigDecimal.TEN);

        Service service = new Service();
        service.getServiceRevenue().setResellerRevenueSharePercentage(
                BigDecimal.TEN);
        service.getServiceRevenue().getServiceCustomerRevenues()
                .add(customerRevenue);
        service.getServiceRevenue().setTotalAmount(BigDecimal.valueOf(29.736));
        addSubscriptions(service, subscriptionRevenues);
        supplier.getService().add(service);
    }

    private void addSubscriptions(Service service,
            BigDecimal... subscriptionRevenues) {
        for (BigDecimal revenue : subscriptionRevenues) {
            Subscription subscription = new Subscription();
            subscription.setRevenue(revenue);
            service.getSubscription().add(subscription);
        }
    }

    @Test
    public void calculate_Supplier() {
        // given
        Supplier supplier = new Supplier();
        BigDecimal[] subscriptionRevenues = new BigDecimal[] {
                BigDecimal.valueOf(2.564), BigDecimal.valueOf(2.565) };
        addService(supplier, subscriptionRevenues);

        // when
        supplier.calculate();

        // then
        assertNotNull(supplier.getResellerRevenuePerSupplier());
        assertEquals(0, supplier.getResellerRevenuePerSupplier().getAmount()
                .compareTo(BigDecimal.valueOf(0.51)));
    }

    @Test
    public void calculate_Service() {
        // given
        ServiceCustomerRevenue customerRevenue = new ServiceCustomerRevenue();
        customerRevenue.setTotalAmount(BigDecimal.valueOf(29.736));
        customerRevenue.setResellerRevenueSharePercentage(BigDecimal.TEN);

        Service service = new Service();
        service.getServiceRevenue().getServiceCustomerRevenues()
                .add(customerRevenue);
        service.getServiceRevenue().setTotalAmount(BigDecimal.valueOf(29.736));
        service.getServiceRevenue().setResellerRevenueSharePercentage(
                BigDecimal.TEN);

        addSubscriptions(service, BigDecimal.valueOf(12.55),
                BigDecimal.valueOf(13), BigDecimal.valueOf(2.091),
                BigDecimal.valueOf(2.095));

        // when
        service.calculate();

        // then
        assertNotNull(service.getServiceRevenue());
        assertEquals(0, service.getServiceRevenue().getResellerRevenue()
                .compareTo(BigDecimal.valueOf(2.97)));
    }

    @Test
    public void calculate_Service_Rounding() {
        // given
        ServiceCustomerRevenue customerRevenue = new ServiceCustomerRevenue();
        customerRevenue.setTotalAmount(BigDecimal.valueOf(29.746));
        customerRevenue.setResellerRevenueSharePercentage(BigDecimal.TEN);

        Service service = new Service();
        service.getServiceRevenue().getServiceCustomerRevenues()
                .add(customerRevenue);
        service.getServiceRevenue().setResellerRevenueSharePercentage(
                BigDecimal.TEN); // 10%
        service.getServiceRevenue().setTotalAmount(BigDecimal.valueOf(29.746));

        addSubscriptions(service, BigDecimal.valueOf(12.56),
                BigDecimal.valueOf(13), BigDecimal.valueOf(2.091),
                BigDecimal.valueOf(2.095));

        // when
        service.calculate();

        // then
        assertNotNull(service.getServiceRevenue());
        assertEquals(0, service.getServiceRevenue().getResellerRevenue()
                .compareTo(BigDecimal.valueOf(2.98)));
    }

    @Test
    public void calculate_ServiceRevenue_rounding() {
        calculate_ServiceRevenueInt(BigDecimal.valueOf(0.99),
                BigDecimal.valueOf(0.1));
    }

    private void calculate_ServiceRevenueInt(BigDecimal baseRevenue,
            BigDecimal expectedServiceRevenueShare) {
        // given
        ServiceRevenue revenue = new ServiceRevenue();
        revenue.setTotalAmount(baseRevenue);
        revenue.setResellerRevenueSharePercentage(BigDecimal.valueOf(10));

        ServiceCustomerRevenue revenueCustomer = new ServiceCustomerRevenue();
        revenueCustomer.setTotalAmount(baseRevenue);
        revenueCustomer.setResellerRevenueSharePercentage(BigDecimal
                .valueOf(10));

        revenue.getServiceCustomerRevenues().add(revenueCustomer);

        // when
        revenue.calculate();

        // then
        assertNotNull(revenue.getResellerRevenue());
        assertEquals(
                0,
                revenue.getResellerRevenue().compareTo(
                        expectedServiceRevenueShare));
    }

    @Test
    public void calculate_ServiceRevenue() {
        calculate_ServiceRevenueInt(BigDecimal.valueOf(0.9),
                BigDecimal.valueOf(0.09));
    }
}
