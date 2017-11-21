/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.model.suppliershare.CustomerRevenueShareDetails;
import org.oscm.billingservice.business.model.suppliershare.RevenueShareDetails;
import org.oscm.billingservice.business.model.suppliershare.Seller;
import org.oscm.converter.PriceConverter;

public class RevenueShareDetailsTest {

    final BigDecimal BD_ZERO_NORMALIZED = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_500_NORMALIZED = new BigDecimal(500)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_475_NORMALIZED = new BigDecimal(475)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_425_NORMALIZED = new BigDecimal(425)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_400_NORMALIZED = new BigDecimal(400)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_375_NORMALIZED = new BigDecimal(375)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_350_NORMALIZED = new BigDecimal(350)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_250_NORMALIZED = new BigDecimal(250)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_150_NORMALIZED = new BigDecimal(150)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_125_NORMALIZED = new BigDecimal(125)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_75_NORMALIZED = new BigDecimal(75)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_50_NORMALIZED = new BigDecimal(50)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_30_NORMALIZED = new BigDecimal(30)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_25_NORMALIZED = new BigDecimal(25)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_15_NORMALIZED = new BigDecimal(15)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_10_NORMALIZED = new BigDecimal(10)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_5_NORMALIZED = new BigDecimal(5)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_N25_NORMALIZED = new BigDecimal(-25)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    final BigDecimal BD_N125_NORMALIZED = new BigDecimal(-125)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    RevenueShareDetails revenueDetails;
    CustomerRevenueShareDetails crs1, crs2;

    @Before
    public void setup() {
        revenueDetails = new RevenueShareDetails();
        crs1 = new CustomerRevenueShareDetails();
        crs1.setCustomerId("customerId1");
        crs2 = new CustomerRevenueShareDetails();
        crs2.setCustomerId("customerId2");
    }

    @Test
    public void calculate_supplier() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_15_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_5_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.SUPPLIER);

        // then
        assertEquals(BD_75_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_25_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_400_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_supplierZeroRevenueShare() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_ZERO_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_ZERO_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.SUPPLIER);

        // then
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_500_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_supplierZeroMarketplacePercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_ZERO_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_5_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.SUPPLIER);

        // then
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_25_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_475_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_supplierZeroOperatorPercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_5_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_ZERO_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.SUPPLIER);

        // then
        assertEquals(BD_25_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_475_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_supplierRevenueShareOverOneHundredPercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_50_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_75_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.SUPPLIER);

        // then
        assertEquals(BD_250_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_375_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_N125_NORMALIZED,
                revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_broker() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_15_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_10_NORMALIZED);
        revenueDetails.setBrokerRevenueSharePercentage(BD_5_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.BROKER);

        // then
        assertEquals(BD_75_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_50_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_25_NORMALIZED, revenueDetails.getBrokerRevenue());
        assertEquals(BD_350_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_brokerZeroRevenueShare() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_ZERO_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_ZERO_NORMALIZED);
        revenueDetails.setBrokerRevenueSharePercentage(BD_ZERO_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.BROKER);

        // then
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getBrokerRevenue());
        assertEquals(BD_500_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_brokerZeroBrokerPercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_15_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_5_NORMALIZED);
        revenueDetails.setBrokerRevenueSharePercentage(BD_ZERO_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.BROKER);

        // then
        assertEquals(BD_75_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_25_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getBrokerRevenue());
        assertEquals(BD_400_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_brokerRevenueShareOverOneHundredPercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_30_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_25_NORMALIZED);
        revenueDetails.setBrokerRevenueSharePercentage(BD_50_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.BROKER);

        // then
        assertEquals(BD_150_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_125_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_250_NORMALIZED, revenueDetails.getBrokerRevenue());
        assertEquals(BD_N25_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_reseller() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_15_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_10_NORMALIZED);
        revenueDetails.setResellerRevenueSharePercentage(BD_5_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.RESELLER);

        // then
        assertEquals(BD_75_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_50_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_25_NORMALIZED, revenueDetails.getResellerRevenue());
        assertEquals(BD_350_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_resellerZeroRevenueShare() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_ZERO_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_ZERO_NORMALIZED);
        revenueDetails.setResellerRevenueSharePercentage(BD_ZERO_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.RESELLER);

        // then
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getResellerRevenue());
        assertEquals(BD_500_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_resellerZeroResellerPercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_15_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_5_NORMALIZED);
        revenueDetails.setResellerRevenueSharePercentage(BD_ZERO_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.RESELLER);

        // then
        assertEquals(BD_75_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_25_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_ZERO_NORMALIZED, revenueDetails.getResellerRevenue());
        assertEquals(BD_400_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_resellerRevenueShareOverOneHundredPercentage() {
        // given
        revenueDetails.setServiceRevenue(BD_500_NORMALIZED);
        revenueDetails.setMarketplaceRevenueSharePercentage(BD_30_NORMALIZED);
        revenueDetails.setOperatorRevenueSharePercentage(BD_25_NORMALIZED);
        revenueDetails.setResellerRevenueSharePercentage(BD_50_NORMALIZED);

        // when
        revenueDetails.calculate(Seller.RESELLER);

        // then
        assertEquals(BD_150_NORMALIZED, revenueDetails.getMarketplaceRevenue());
        assertEquals(BD_125_NORMALIZED, revenueDetails.getOperatorRevenue());
        assertEquals(BD_250_NORMALIZED, revenueDetails.getResellerRevenue());
        assertEquals(BD_N25_NORMALIZED, revenueDetails.getAmountForSupplier());
    }

}
