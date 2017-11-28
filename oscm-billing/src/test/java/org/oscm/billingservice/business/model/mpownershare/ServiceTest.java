/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 03, 2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.business.model.mpownershare;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

public class ServiceTest {

    private Service service;
    private RevenueShareDetails revenueShareDetails;

    @Before
    public void setup() {
        service = new Service();
        revenueShareDetails = service.getRevenueShareDetails();
        revenueShareDetails.setServiceRevenue(Util.createBigDecimal(500));
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_direct_mpRevenueSharePercentageLessThan0() {
        // given
        service.setModel(OfferingType.DIRECT);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(-1));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate_direct_mpRevenueSharePercentageGreaterThan100() {
        // given
        service.setModel(OfferingType.DIRECT);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(500));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then exception
    }

    @Test
    public void calculate_direct_mpRevenueSharePercentage0() {
        // given
        service.setModel(OfferingType.DIRECT);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(500),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_direct_mpRevenueSharePercentage15() {
        // given
        service.setModel(OfferingType.DIRECT);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(75),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(425),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_direct_mpRevenueSharePercentage33() {
        // given
        service.setModel(OfferingType.DIRECT);
        revenueShareDetails.setServiceRevenue(Util.createBigDecimal(100));
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(33.3333));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(33.33),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(66.67),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_direct_mpRevenueSharePercentage100() {
        // given
        service.setModel(OfferingType.DIRECT);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(100));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(500),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_reseller_RevenueSharePercentage0() {
        // given
        service.setModel(OfferingType.RESELLER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setResellerRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(75),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getResellerRevenue());
        assertEquals(Util.createBigDecimal(425),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_reseller_RevenueSharePercentage5() {
        // given
        service.setModel(OfferingType.RESELLER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setResellerRevenueSharePercentage(Util
                .createBigDecimal(5));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(75),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(25),
                revenueShareDetails.getResellerRevenue());
        assertEquals(Util.createBigDecimal(400),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_reseller_RevenueSharePercentage33() {
        // given
        service.setModel(OfferingType.RESELLER);
        revenueShareDetails.setServiceRevenue(Util.createBigDecimal(100));
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(33.3333));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setResellerRevenueSharePercentage(BigDecimal
                .valueOf(33.3333));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(33.33),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(33.33),
                revenueShareDetails.getResellerRevenue());
        assertEquals(Util.createBigDecimal(33.34),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_reseller_RevenueSharePercentage100() {
        // given
        service.setModel(OfferingType.RESELLER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setResellerRevenueSharePercentage(Util
                .createBigDecimal(100));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(500),
                revenueShareDetails.getResellerRevenue());
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_reseller_RevenueSharePercentagePlusMpRSPGreaterThan100() {
        // given
        service.setModel(OfferingType.RESELLER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(5));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setResellerRevenueSharePercentage(Util
                .createBigDecimal(100));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(25),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(500),
                revenueShareDetails.getResellerRevenue());
        assertEquals(Util.createBigDecimal(-25),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_broker_RevenueSharePercentage0() {
        // given
        service.setModel(OfferingType.BROKER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setBrokerRevenueSharePercentage(Util
                .createBigDecimal(0));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(75),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getBrokerRevenue());
        assertEquals(Util.createBigDecimal(425),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_broker_RevenueSharePercentage5() {
        // given
        service.setModel(OfferingType.BROKER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setBrokerRevenueSharePercentage(Util
                .createBigDecimal(5));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(75),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(25),
                revenueShareDetails.getBrokerRevenue());
        assertEquals(Util.createBigDecimal(400),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_broker_RevenueSharePercentage33() {
        // given
        service.setModel(OfferingType.BROKER);
        revenueShareDetails.setServiceRevenue(Util.createBigDecimal(100));
        revenueShareDetails.setMarketplaceRevenueSharePercentage(BigDecimal
                .valueOf(33.3333));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setBrokerRevenueSharePercentage(BigDecimal
                .valueOf(33.3333));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(33.33),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(33.33),
                revenueShareDetails.getBrokerRevenue());
        assertEquals(Util.createBigDecimal(33.34),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_broker_RevenueSharePercentage100() {
        // given
        service.setModel(OfferingType.BROKER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setBrokerRevenueSharePercentage(Util
                .createBigDecimal(100));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(500),
                revenueShareDetails.getBrokerRevenue());
        assertEquals(Util.createBigDecimal(0),
                revenueShareDetails.getAmountForSupplier());
    }

    @Test
    public void calculate_broker_RevenueSharePercentagePlusMpRSPGreaterThan100() {
        // given
        service.setModel(OfferingType.BROKER);
        revenueShareDetails.setMarketplaceRevenueSharePercentage(Util
                .createBigDecimal(5));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails.setBrokerRevenueSharePercentage(Util
                .createBigDecimal(100));

        // when
        service.calculate();

        // then
        assertEquals(Util.createBigDecimal(25),
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(Util.createBigDecimal(500),
                revenueShareDetails.getBrokerRevenue());
        assertEquals(Util.createBigDecimal(-25),
                revenueShareDetails.getAmountForSupplier());
    }

}
