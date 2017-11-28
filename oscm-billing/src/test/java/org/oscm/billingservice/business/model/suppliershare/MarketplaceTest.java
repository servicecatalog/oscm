/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.model.suppliershare.Broker;
import org.oscm.billingservice.business.model.suppliershare.Marketplace;
import org.oscm.billingservice.business.model.suppliershare.Reseller;
import org.oscm.billingservice.business.model.suppliershare.RevenuePerMarketplace;
import org.oscm.billingservice.business.model.suppliershare.RevenueShareDetails;
import org.oscm.billingservice.business.model.suppliershare.Service;
import org.oscm.converter.PriceConverter;

public class MarketplaceTest {

    private final BigDecimal BD_ZERO = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_600 = new BigDecimal(600)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_410 = new BigDecimal(410)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_400 = new BigDecimal(400)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_280 = new BigDecimal(280)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_200 = new BigDecimal(200)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_150 = new BigDecimal(150)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_130 = new BigDecimal(130)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_100 = new BigDecimal(100)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_50 = new BigDecimal(50)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_20 = new BigDecimal(20)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private final BigDecimal BD_10 = new BigDecimal(10)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    private Marketplace mp;
    private RevenuePerMarketplace revenuePerMarketplace;
    private List<Service> services = new ArrayList<Service>();

    @Before
    public void setup() {
        revenuePerMarketplace = mock(RevenuePerMarketplace.class);
        mp = spy(new Marketplace());
        mp.setRevenuePerMarketplace(revenuePerMarketplace);
        doReturn(services).when(mp).getService();
    }

    @Test
    public void calculate_brokerService() {
        // given
        services.add(givenBrokerService(BD_10, BD_10, BD_200, BD_50, BD_10));

        // when
        mp.calculate();

        // then
        verify(services.get(0), times(1)).calculate();
        assertEquals(BD_200, mp.serviceRevenue);
        assertEquals(BD_50, mp.marketplaceRevenue);
        assertEquals(BD_10, mp.brokerRevenue);
        assertEquals(BD_ZERO, mp.resellerRevenue);
        verify(revenuePerMarketplace).setOverallRevenue(eq(BD_130));
    }

    @Test
    public void calculate_resellerService() {
        // given
        services.add(givenResellerService(BD_20, BD_10, BD_400, BD_100, BD_10));

        // when
        mp.calculate();

        // then
        verify(services.get(0), times(1)).calculate();
        assertEquals(BD_400, mp.serviceRevenue);
        assertEquals(BD_100, mp.marketplaceRevenue);
        assertEquals(BD_ZERO, mp.brokerRevenue);
        assertEquals(BD_10, mp.resellerRevenue);
        verify(revenuePerMarketplace).setOverallRevenue(eq(BD_280));
    }

    @Test
    public void calculate_brokerAndResellerService() {
        // given
        services.add(givenBrokerService(BD_10, BD_10, BD_200, BD_50, BD_10));
        services.add(givenResellerService(BD_20, BD_10, BD_400, BD_100, BD_10));

        // when
        mp.calculate();

        // then
        verify(services.get(0), times(1)).calculate();
        assertEquals(BD_600, mp.serviceRevenue);
        assertEquals(BD_150, mp.marketplaceRevenue);
        assertEquals(BD_10, mp.brokerRevenue);
        assertEquals(BD_10, mp.resellerRevenue);
        verify(revenuePerMarketplace).setOverallRevenue(eq(BD_410));
    }

    private Service givenResellerService(BigDecimal brokerRevenue,
            BigDecimal resellerRevenue, BigDecimal serviceRevenue,
            BigDecimal marketplaceRevenue, BigDecimal opRevenue) {
        Service s2 = givenMockedService(brokerRevenue, resellerRevenue,
                serviceRevenue, marketplaceRevenue, opRevenue);
        doReturn(new Reseller()).when(s2).getReseller();
        return s2;
    }

    private Service givenBrokerService(BigDecimal brokerRevenue,
            BigDecimal resellerRevenue, BigDecimal serviceRevenue,
            BigDecimal marketplaceRevenue, BigDecimal opRevenue) {
        Service s1 = givenMockedService(brokerRevenue, resellerRevenue,
                serviceRevenue, marketplaceRevenue, opRevenue);
        doReturn(new Broker()).when(s1).getBroker();
        return s1;
    }

    private Service givenMockedService(BigDecimal brokerRevenue,
            BigDecimal resellerRevenue, BigDecimal serviceRevenue,
            BigDecimal marketplaceRevenue, BigDecimal opRevenue) {

        RevenueShareDetails details = new RevenueShareDetails();
        details.setBrokerRevenue(brokerRevenue);
        details.setResellerRevenue(resellerRevenue);
        details.setServiceRevenue(serviceRevenue);
        details.setMarketplaceRevenue(marketplaceRevenue);
        details.setOperatorRevenue(opRevenue);

        Service s = mock(Service.class);

        doNothing().when(s).calculate();
        doReturn(details).when(s).getRevenueShareDetails();

        return s;
    }
}
