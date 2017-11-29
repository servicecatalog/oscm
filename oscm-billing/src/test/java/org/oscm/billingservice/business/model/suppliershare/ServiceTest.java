/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.model.suppliershare.Broker;
import org.oscm.billingservice.business.model.suppliershare.Reseller;
import org.oscm.billingservice.business.model.suppliershare.RevenueShareDetails;
import org.oscm.billingservice.business.model.suppliershare.Seller;
import org.oscm.billingservice.business.model.suppliershare.Service;
import org.oscm.billingservice.business.model.suppliershare.Subscription;
import org.oscm.converter.PriceConverter;

public class ServiceTest {

    final BigDecimal BD_ZERO_NORMALIZED = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_500_NORMALIZED = new BigDecimal(500)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_300_NORMALIZED = new BigDecimal(300)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_200_NORMALIZED = new BigDecimal(200)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    Service service;
    RevenueShareDetails revenueShareDetails;

    @Before
    public void setup() {
        revenueShareDetails = mock(RevenueShareDetails.class);
        service = spy(new Service());
        service.setRevenueShareDetails(revenueShareDetails);
    }

    @Test
    public void calculate_supplier() {
        // given
        doReturn(null).when(service).getBroker();
        doReturn(null).when(service).getReseller();

        // when
        service.calculate();

        // then
        verify(revenueShareDetails, times(1)).calculate(Seller.SUPPLIER);
    }

    @Test
    public void calculate_broker() {
        // given
        doReturn(new Broker()).when(service).getBroker();

        // when
        service.calculate();

        // then
        verify(revenueShareDetails, times(1)).calculate(Seller.BROKER);
    }

    @Test
    public void calculate_reseller() {
        // given
        doReturn(null).when(service).getBroker();
        doReturn(new Reseller()).when(service).getReseller();

        // when
        service.calculate();

        // then
        verify(revenueShareDetails, times(1)).calculate(Seller.RESELLER);
    }

    @Test
    public void calculate_serviceRevenue() {
        // given
        service.addSubscription(newSubscription(BD_300_NORMALIZED));
        service.addSubscription(newSubscription(BD_200_NORMALIZED));

        // when
        service.calculate();

        // then
        verify(revenueShareDetails).setServiceRevenue(BD_500_NORMALIZED);
    }

    private Subscription newSubscription(BigDecimal revenue) {
        Subscription s = new Subscription();
        s.setRevenue(revenue);
        return s;
    }

}
