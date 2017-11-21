/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigInteger;

import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.business.model.brokershare.BrokerRevenueShareResult;
import org.oscm.billingservice.business.model.mpownershare.MarketplaceOwnerRevenueShareResult;
import org.oscm.billingservice.business.model.resellershare.ResellerRevenueShareResult;
import org.oscm.billingservice.business.model.suppliershare.SupplierRevenueShareResult;

/**
 * test marshaling of revenue share results
 */
public class SharesCalculatorBeanTest2 {
    private SharesCalculatorBean sharesCalculator;

    @Before
    public void setup() {
        sharesCalculator = spy(new SharesCalculatorBean());
    }

    @Test
    public void marshall_BrokerRevenueShareResult() throws Exception {
        // given
        BrokerRevenueShareResult brokerRevenueShareResult = createBrokerRevenueShareResult();

        // when
        byte[] serialized = sharesCalculator
                .marshallRevenueShareResults(brokerRevenueShareResult);

        // then
        assertNotNull(serialized);
    }

    private BrokerRevenueShareResult createBrokerRevenueShareResult() {
        BrokerRevenueShareResult brokerRevenue = new BrokerRevenueShareResult();
        brokerRevenue.setOrganizationId("organizationId");
        brokerRevenue.setOrganizationKey(BigInteger.valueOf(1000));
        return brokerRevenue;
    }

    @Test
    public void marshall_SupplierRevenueShareResult() throws Exception {
        // given
        SupplierRevenueShareResult supplierRevenueShareResult = createSupplierRevenueShareResult();

        // when
        byte[] serialized = sharesCalculator
                .marshallRevenueShareResults(supplierRevenueShareResult);

        // then
        assertNotNull(serialized);
    }

    private SupplierRevenueShareResult createSupplierRevenueShareResult() {
        SupplierRevenueShareResult supplierRevenue = new SupplierRevenueShareResult();
        supplierRevenue.setOrganizationId("organizationId");
        supplierRevenue.setOrganizationKey(BigInteger.valueOf(1000));
        return supplierRevenue;
    }

    @Test
    public void marshall_ResellerRevenueShareResult() throws Exception {
        // given
        ResellerRevenueShareResult resellerRevenueShareResult = createResellerRevenueShareResult();

        // when
        byte[] serialized = sharesCalculator
                .marshallRevenueShareResults(resellerRevenueShareResult);

        // then
        assertNotNull(serialized);
    }

    private ResellerRevenueShareResult createResellerRevenueShareResult() {
        ResellerRevenueShareResult resellerRevenue = new ResellerRevenueShareResult();
        resellerRevenue.setOrganizationId("organizationId");
        resellerRevenue.setOrganizationKey(BigInteger.valueOf(1000));
        return resellerRevenue;
    }

    @Test
    public void marshall_MPOwnerRevenueShareResult() throws Exception {
        // given
        MarketplaceOwnerRevenueShareResult mpOwnerRevenueShareResult = createMPOwnerRevenueShareResult();

        // when
        byte[] serialized = sharesCalculator
                .marshallRevenueShareResults(mpOwnerRevenueShareResult);

        // then
        assertNotNull(serialized);
    }

    private MarketplaceOwnerRevenueShareResult createMPOwnerRevenueShareResult() {
        MarketplaceOwnerRevenueShareResult resellerRevenue = new MarketplaceOwnerRevenueShareResult();
        resellerRevenue.setOrganizationId("organizationId");
        resellerRevenue.setOrganizationKey(BigInteger.valueOf(1000));
        return resellerRevenue;
    }

    @Test
    public void marshall_invalidSetSchemaCall() throws Exception {
        // given
        Marshaller marshaller = mock(Marshaller.class);
        doReturn(marshaller).when(sharesCalculator).createMarshaller(
                anyObject());

        // when
        sharesCalculator.marshallRevenueShareResults(anyObject());

        // then
        verify(marshaller, times(0)).setSchema((Schema) any());
    }
}
