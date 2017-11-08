/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class PricingServiceBeanTest {

    private static final String MARKETPLACE_ID = "mId";

    private PricingServiceBean bean;
    private Marketplace mp;

    @Before
    public void setup() throws ObjectNotFoundException {
        DataService mockDs = mock(DataService.class);
        mp = new Marketplace(MARKETPLACE_ID);
        doReturn(mp).when(mockDs).getReferenceByBusinessKey(
                any(Marketplace.class));

        bean = spy(new PricingServiceBean());
        bean.dm = mockDs;
        bean.mpServiceLocal = mock(MarketplaceServiceLocal.class);
        bean.spPartnerServiceLocal = mock(ServiceProvisioningPartnerServiceLocal.class);
    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type, BigDecimal value) {
        RevenueShareModel m = new RevenueShareModel();
        m.setRevenueShare(value);
        m.setRevenueShareModelType(type);
        return m;
    }

    @Test(expected = NullPointerException.class)
    public void getPartnerRevenueSharesForMarketplace_NullBrokerPriceMode()
            throws Exception {
        // given a marketplace with no broker revenue share model
        // when
        bean.getPartnerRevenueSharesForMarketplace(MARKETPLACE_ID);
    }

    @Test(expected = NullPointerException.class)
    public void getPartnerRevenueSharesForMarketplace_NullResellerPriceMode()
            throws Exception {
        // given a marketplace with no reseller revenue share model
        RevenueShareModel brokerPriceModel = createRevenueModel(
                RevenueShareModelType.BROKER_REVENUE_SHARE, BigDecimal.ZERO);
        mp.setBrokerPriceModel(brokerPriceModel);

        // when
        bean.getPartnerRevenueSharesForMarketplace(MARKETPLACE_ID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPartnerRevenueSharesForMarketplace_marketplaceNotFound()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.mpServiceLocal)
                .getMarketplace(eq("unknownMarketplaceId"));

        // when
        bean.getPartnerRevenueSharesForMarketplace("unknownMarketplaceId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMarketplaceRevenueShares_NullPriceModel() throws Exception {
        // given a marketplace with no revenue share model
        // when
        bean.getMarketplaceRevenueShares(MARKETPLACE_ID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getMarketplaceRevenueShares_ObjectNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.mpServiceLocal)
                .loadMarketplaceRevenueShare(eq(MARKETPLACE_ID));
        // when
        bean.getMarketplaceRevenueShares(MARKETPLACE_ID);
    }

    @Test
    public void getOperatorRevenueShare() throws Exception {
        // given
        RevenueShareModel operatorRS = createRevenueModel(
                RevenueShareModelType.OPERATOR_REVENUE_SHARE, BigDecimal.TEN);
        doReturn(operatorRS).when(bean.spPartnerServiceLocal)
                .getOperatorRevenueShare(eq(101L));
        RevenueShareModel defaultOperatorRS = createRevenueModel(
                RevenueShareModelType.OPERATOR_REVENUE_SHARE,
                BigDecimal.valueOf(11));
        doReturn(defaultOperatorRS).when(bean.spPartnerServiceLocal)
                .getDefaultOperatorRevenueShare(eq(101L));

        // when
        Response response = bean.getOperatorRevenueShare(101L);

        // then
        POOperatorPriceModel pm = response
                .getResult(POOperatorPriceModel.class);
        assertNotNull(pm);
        assertNotNull(pm.getRevenueShare());
        assertEquals(BigDecimal.TEN, pm.getRevenueShare().getRevenueShare());
        assertNotNull(pm.getDefaultRevenueShare());
        assertEquals(BigDecimal.valueOf(11), pm.getDefaultRevenueShare()
                .getRevenueShare());
    }

    @Test
    public void getOperatorRevenueShare_Null() throws Exception {
        // given
        doReturn(null).when(bean.spPartnerServiceLocal)
                .getOperatorRevenueShare(eq(101L));
        doReturn(null).when(bean.spPartnerServiceLocal)
                .getDefaultOperatorRevenueShare(eq(101L));

        // when
        Response response = bean.getOperatorRevenueShare(101L);

        // then
        POOperatorPriceModel pm = response
                .getResult(POOperatorPriceModel.class);
        assertNotNull(pm);
        assertNull(pm.getRevenueShare());
        assertNull(pm.getDefaultRevenueShare());
    }

    @Test
    public void saveOperatorRevenueShare() throws Exception {
        PORevenueShare po = new PORevenueShare();
        po.setKey(101L);
        po.setRevenueShare(BigDecimal.TEN);
        po.setVersion(2);
        ArgumentCaptor<RevenueShareModel> captor = ArgumentCaptor
                .forClass(RevenueShareModel.class);

        // when
        Response response = bean.saveOperatorRevenueShare(1L, po);

        // then
        assertTrue(response.getResults().isEmpty());
        assertTrue(response.getWarnings().isEmpty());
        assertTrue(response.getReturnCodes().isEmpty());
        verify(bean.spPartnerServiceLocal, times(1)).saveOperatorRevenueShare(
                eq(1L), captor.capture(), eq(2));
        assertEquals(BigDecimal.TEN, captor.getValue().getRevenueShare());
        assertEquals(RevenueShareModelType.OPERATOR_REVENUE_SHARE, captor
                .getValue().getRevenueShareModelType());
        assertEquals(101L, captor.getValue().getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveOperatorRevenueShare_NullParameter() throws Exception {
        bean.saveOperatorRevenueShare(1L, null);
    }
}
