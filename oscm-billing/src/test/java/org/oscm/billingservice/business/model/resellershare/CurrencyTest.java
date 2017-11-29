/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.model.resellershare.Currency;
import org.oscm.billingservice.business.model.resellershare.Service;
import org.oscm.billingservice.business.model.resellershare.ServiceCustomerRevenue;
import org.oscm.billingservice.business.model.resellershare.Subscription;
import org.oscm.billingservice.business.model.resellershare.Supplier;
import org.oscm.test.BigDecimalAsserts;

/**
 * @author kulle
 * 
 */
public class CurrencyTest {

    private Service newService(BigDecimal resellerShare,
            BigDecimal... subscriptionRevenues) {

        BigDecimal totalAmount = BigDecimal.valueOf(0);
        for (BigDecimal revenue : subscriptionRevenues) {
            totalAmount = totalAmount.add(revenue);
        }

        ServiceCustomerRevenue customerRevenue = new ServiceCustomerRevenue();
        customerRevenue.setTotalAmount(totalAmount);
        customerRevenue.setResellerRevenueSharePercentage(resellerShare);

        Service service = new Service();
        service.getServiceRevenue().setResellerRevenueSharePercentage(
                resellerShare);
        service.getServiceRevenue().getServiceCustomerRevenues()
                .add(customerRevenue);
        service.getServiceRevenue().setTotalAmount(BigDecimal.valueOf(29.736));
        addSubscriptions(service, subscriptionRevenues);

        return service;
    }

    private void addSubscriptions(Service service,
            BigDecimal... subscriptionRevenues) {
        for (BigDecimal revenue : subscriptionRevenues) {
            Subscription subscription = new Subscription();
            subscription.setRevenue(revenue);
            service.getSubscription().add(subscription);
        }
    }

    private Supplier givenSupplier(Currency currency) {
        Supplier supplier = new Supplier();
        currency.addSupplier(supplier);
        return supplier;
    }

    @Test
    public void calculate_resellerRevenue() {
        // given
        Currency currency = new Currency();
        Supplier supplier = givenSupplier(currency);
        supplier.getService().add(
                newService(BigDecimal.valueOf(10), BigDecimal.valueOf(100)));

        // when
        currency.calculate();

        // then
        BigDecimalAsserts.checkEquals(new BigDecimal("10"), currency
                .getResellerRevenue().getAmount());
    }

    @Test
    public void calculate_purchasePrice() {
        // given
        Currency currency = new Currency();
        Supplier supplier = givenSupplier(currency);
        supplier.getService().add(
                newService(BigDecimal.valueOf(10), BigDecimal.valueOf(100)));

        // when
        currency.calculate();

        // then
        BigDecimalAsserts.checkEquals(new BigDecimal("100"), currency
                .getResellerRevenue().getTotalAmount());
        BigDecimalAsserts.checkEquals(new BigDecimal("10"), currency
                .getResellerRevenue().getAmount());
        BigDecimalAsserts.checkEquals(new BigDecimal("90"), currency
                .getResellerRevenue().getPurchasePrice());
    }

}
