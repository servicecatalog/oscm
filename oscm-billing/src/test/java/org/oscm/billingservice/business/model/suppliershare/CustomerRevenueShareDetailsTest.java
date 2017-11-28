/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 15, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.model.suppliershare.CustomerRevenueShareDetails;
import org.oscm.converter.PriceConverter;

/**
 * @author tokoda
 * 
 */
public class CustomerRevenueShareDetailsTest {

    private static final BigDecimal BD_100_NORMALIZED = new BigDecimal(100)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_75_NORMALIZED = new BigDecimal(75)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_50_NORMALIZED = new BigDecimal(50)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_20_NORMALIZED = new BigDecimal(20)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_10_NORMALIZED = new BigDecimal(10)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_ZERO_NORMALIZED = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_N5_NORMALIZED = new BigDecimal(-5)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_N25_NORMALIZED = new BigDecimal(-25)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    private CustomerRevenueShareDetails customerRS;

    @Before
    public void setup() {
        customerRS = new CustomerRevenueShareDetails();
        customerRS.setServiceRevenue(BigDecimal.valueOf(100));
    }

    @Test
    public void calculate_ZeroRevenueSharePercentages() {
        // given
        // when
        customerRS.calculate(BigDecimal.ZERO, BigDecimal.ZERO, null, null);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getOperatorRevenue());
        assertNull(customerRS.getBrokerRevenue());
        assertNull(customerRS.getResellerRevenue());
        assertEquals(BD_100_NORMALIZED, customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_RevenueShareOverOneHandredPercentage() {
        // given
        // when
        customerRS.calculate(BigDecimal.valueOf(50), BigDecimal.valueOf(75),
                null, null);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BD_50_NORMALIZED, customerRS.getMarketplaceRevenue());
        assertEquals(BD_75_NORMALIZED, customerRS.getOperatorRevenue());
        assertNull(customerRS.getBrokerRevenue());
        assertNull(customerRS.getResellerRevenue());
        assertEquals(BD_N25_NORMALIZED, customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_FractionalRevenueShare() {
        // given
        // when
        customerRS.calculate(BigDecimal.valueOf(1.11),
                BigDecimal.valueOf(2.22), null, null);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BigDecimal.valueOf(1.11),
                customerRS.getMarketplaceRevenue());
        assertEquals(BigDecimal.valueOf(2.22), customerRS.getOperatorRevenue());
        assertNull(customerRS.getBrokerRevenue());
        assertNull(customerRS.getResellerRevenue());
        assertEquals(BigDecimal.valueOf(96.67),
                customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_BrokerZeroRevenueSharePercentages() {
        // given
        // when
        customerRS.calculate(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getOperatorRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getBrokerRevenue());
        assertNull(customerRS.getResellerRevenue());
        assertEquals(BD_100_NORMALIZED, customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_BrokerRevenueShareOverOneHandredPercentage() {
        // given
        // when
        customerRS.calculate(BigDecimal.valueOf(10), BigDecimal.valueOf(20),
                BigDecimal.valueOf(75), null);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BD_10_NORMALIZED, customerRS.getMarketplaceRevenue());
        assertEquals(BD_20_NORMALIZED, customerRS.getOperatorRevenue());
        assertEquals(BD_75_NORMALIZED, customerRS.getBrokerRevenue());
        assertNull(customerRS.getResellerRevenue());
        assertEquals(BD_N5_NORMALIZED, customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_BrokerFractionalRevenueShare() {
        // given
        // when
        customerRS.calculate(BigDecimal.valueOf(1.11),
                BigDecimal.valueOf(2.22), BigDecimal.valueOf(3.33), null);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BigDecimal.valueOf(1.11),
                customerRS.getMarketplaceRevenue());
        assertEquals(BigDecimal.valueOf(2.22), customerRS.getOperatorRevenue());
        assertEquals(BigDecimal.valueOf(3.33), customerRS.brokerRevenue);
        assertNull(customerRS.getResellerRevenue());
        assertEquals(BigDecimal.valueOf(93.34),
                customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_ResellerZeroRevenueSharePercentages() {
        // given
        // when
        customerRS.calculate(BigDecimal.ZERO, BigDecimal.ZERO, null,
                BigDecimal.ZERO);

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getOperatorRevenue());
        assertNull(customerRS.getBrokerRevenue());
        assertEquals(BD_ZERO_NORMALIZED, customerRS.getResellerRevenue());
        assertEquals(BD_100_NORMALIZED, customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_ResellerRevenueShareOverOneHandredPercentage() {
        // given
        // when
        customerRS.calculate(BigDecimal.valueOf(10), BigDecimal.valueOf(20),
                null, BigDecimal.valueOf(75));

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BD_10_NORMALIZED, customerRS.getMarketplaceRevenue());
        assertEquals(BD_20_NORMALIZED, customerRS.getOperatorRevenue());
        assertNull(customerRS.getBrokerRevenue());
        assertEquals(BD_75_NORMALIZED, customerRS.getResellerRevenue());
        assertEquals(BD_N5_NORMALIZED, customerRS.getAmountForSupplier());
    }

    @Test
    public void calculate_ResellerFractionalRevenueShare() {
        // given
        // when
        customerRS.calculate(BigDecimal.valueOf(1.11),
                BigDecimal.valueOf(2.22), null, BigDecimal.valueOf(3.33));

        // then
        assertEquals(BD_100_NORMALIZED, customerRS.getServiceRevenue());
        assertEquals(BigDecimal.valueOf(1.11),
                customerRS.getMarketplaceRevenue());
        assertEquals(BigDecimal.valueOf(2.22), customerRS.getOperatorRevenue());
        assertNull(customerRS.brokerRevenue);
        assertEquals(BigDecimal.valueOf(3.33), customerRS.getResellerRevenue());
        assertEquals(BigDecimal.valueOf(93.34),
                customerRS.getAmountForSupplier());
    }

}
