/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 24, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningPartnerServiceLocalBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author tokoda
 * 
 */
public class PricingServiceBeanSavePartnerRevenueShareIT extends EJBTestBase {

    private PricingServiceBean bean;

    private long poUserKey;

    @Override
    protected void setup(final TestContainer container) {
        bean = new PricingServiceBean();
        bean.dm = mock(DataService.class);
        bean.spPartnerServiceLocal = mock(ServiceProvisioningPartnerServiceLocalBean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void savePartnerRevenueSharesForServices_NullPricingParameter()
            throws Exception {
        // given
        // when
        bean.savePartnerRevenueSharesForServices(null);
    }

    @Test
    public void savePartnerRevenueSharesForServices_EmptyPricing()
            throws Exception {
        // given
        List<POServicePricing> pricings = new ArrayList<POServicePricing>();

        // when
        Response response = bean.savePartnerRevenueSharesForServices(pricings);

        // then
        List<POServicePricing> result = response
                .getResultList(POServicePricing.class);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void savePartnerRevenueSharesForServices_NullInputServiceForPricing()
            throws Exception {
        // given
        POPartnerPriceModel priceModel = new POPartnerPriceModel();

        POServicePricing servicePricing = new POServicePricing();
        servicePricing.setPartnerPriceModel(priceModel);

        List<POServicePricing> pricings = new ArrayList<POServicePricing>();
        pricings.add(servicePricing);

        // when
        bean.savePartnerRevenueSharesForServices(pricings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void savePartnerRevenueSharesForServices_NullInputPriceModel()
            throws Exception {
        // given
        POServicePricing servicePricing = new POServicePricing();
        servicePricing.setServiceForPricing(new POServiceForPricing());

        List<POServicePricing> pricings = new ArrayList<POServicePricing>();
        pricings.add(servicePricing);

        // when
        bean.savePartnerRevenueSharesForServices(pricings);
    }

    @Test
    public void savePartnerRevenueSharesForServices_NullInputRevenueShareBroker()
            throws Exception {
        // given
        POServiceForPricing serviceForPricing = new POServiceForPricing();
        serviceForPricing.setKey(1);
        POPartnerPriceModel priceModel = new POPartnerPriceModel();
        priceModel.setRevenueShareBrokerModel(null);
        PORevenueShare resellerRevenueShare = new PORevenueShare();
        resellerRevenueShare.setKey(2);
        resellerRevenueShare.setRevenueShare(BigDecimal.ONE);
        priceModel.setRevenueShareResellerModel(resellerRevenueShare);

        List<POServicePricing> pricings = createServicePricings(
                serviceForPricing, priceModel);

        // when
        bean.savePartnerRevenueSharesForServices(pricings);

        // then
        RevenueShareModel revenueShareToCompare = new RevenueShareModel();
        revenueShareToCompare.setKey(2);
        revenueShareToCompare.setRevenueShare(BigDecimal.ONE);
        verify(bean.spPartnerServiceLocal, times(1))
                .saveRevenueShareModelsForProduct(1, null,
                        revenueShareToCompare, 0, 0);
    }

    @Test
    public void savePartnerRevenueSharesForServices_NullInputRevenueShareReseller()
            throws Exception {
        // given
        POServiceForPricing serviceForPricing = new POServiceForPricing();
        serviceForPricing.setKey(1);
        POPartnerPriceModel priceModel = new POPartnerPriceModel();
        PORevenueShare brokerRevenueShare = new PORevenueShare();
        brokerRevenueShare.setKey(2);
        brokerRevenueShare.setRevenueShare(BigDecimal.ONE);
        priceModel.setRevenueShareBrokerModel(brokerRevenueShare);
        priceModel.setRevenueShareResellerModel(null);

        List<POServicePricing> pricings = createServicePricings(
                serviceForPricing, priceModel);

        // when
        bean.savePartnerRevenueSharesForServices(pricings);

        // then
        RevenueShareModel revenueShareToCompare = new RevenueShareModel();
        revenueShareToCompare.setKey(2);
        revenueShareToCompare.setRevenueShare(BigDecimal.ONE);
        verify(bean.spPartnerServiceLocal, times(1))
                .saveRevenueShareModelsForProduct(1, revenueShareToCompare,
                        null, 0, 0);
    }

    @Test
    public void savePartnerRevenueSharesForServices_MultipleServices()
            throws Exception {
        // given
        POPartnerPriceModel priceModel = createPriceModelForInput(1,
                BigDecimal.ONE, 2, BigDecimal.TEN);

        POServiceForPricing serviceForPricing = new POServiceForPricing();
        serviceForPricing.setKey(3);
        List<POServicePricing> pricings = createServicePricings(
                serviceForPricing, priceModel);

        POServiceForPricing secondServiceForPricing = new POServiceForPricing();
        secondServiceForPricing.setKey(4);
        pricings.add(createServicePricing(secondServiceForPricing, priceModel));

        // when
        container.login(poUserKey, ROLE_PLATFORM_OPERATOR);
        bean.savePartnerRevenueSharesForServices(pricings);

        // then validate the results list
        RevenueShareModel brokerRevenueShareToCompare = new RevenueShareModel();
        brokerRevenueShareToCompare.setKey(1);
        brokerRevenueShareToCompare.setRevenueShare(BigDecimal.ONE);
        RevenueShareModel resellerRevenueShareToCompare = new RevenueShareModel();
        resellerRevenueShareToCompare.setKey(2);
        resellerRevenueShareToCompare.setRevenueShare(BigDecimal.TEN);
        verify(bean.spPartnerServiceLocal, times(1))
                .saveRevenueShareModelsForProduct(3,
                        brokerRevenueShareToCompare,
                        resellerRevenueShareToCompare, 0, 0);
        verify(bean.spPartnerServiceLocal, times(1))
                .saveRevenueShareModelsForProduct(4,
                        brokerRevenueShareToCompare,
                        resellerRevenueShareToCompare, 0, 0);

    }

    @Test(expected = ConcurrentModificationException.class)
    public void savePartnerRevenueSharesForServices_concurrentExecution()
            throws Exception {
        // given
        POServiceForPricing serviceForPricing = new POServiceForPricing();
        POPartnerPriceModel priceModel = createPriceModelForInput(1,
                BigDecimal.valueOf(100.00), 1, BigDecimal.valueOf(100.00));
        List<POServicePricing> pricings = createServicePricings(
                serviceForPricing, priceModel);

        when(
                bean.spPartnerServiceLocal.saveRevenueShareModelsForProduct(
                        anyLong(), any(RevenueShareModel.class),
                        any(RevenueShareModel.class), anyInt(), anyInt()))
                .thenThrow(new ConcurrentModificationException());

        // when
        bean.savePartnerRevenueSharesForServices(pricings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toRevenueShareModel_NullRevenueShare() {
        // given
        // when
        PricingServiceBean.toRevenueShareModel(null);
    }

    @Test
    public void toRevenueShareModel() {
        // given
        PORevenueShare poRevenueShare = new PORevenueShare();
        poRevenueShare.setKey(1);
        poRevenueShare.setRevenueShare(BigDecimal.ONE);

        // when
        RevenueShareModel revenueShare = PricingServiceBean
                .toRevenueShareModel(poRevenueShare);

        // then
        assertEquals(1, revenueShare.getKey());
        assertEquals(BigDecimal.ONE, revenueShare.getRevenueShare());
    }

    private POServicePricing createServicePricing(
            POServiceForPricing serviceForPricing,
            POPartnerPriceModel priceModel) {
        POServicePricing servicePricing = new POServicePricing();
        servicePricing.setPartnerPriceModel(priceModel);
        servicePricing.setServiceForPricing(serviceForPricing);

        return servicePricing;
    }

    private List<POServicePricing> createServicePricings(
            POServiceForPricing serviceForPricing,
            POPartnerPriceModel priceModel) {
        List<POServicePricing> pricings = new ArrayList<POServicePricing>();
        POServicePricing servicePricing = createServicePricing(
                serviceForPricing, priceModel);
        pricings.add(servicePricing);
        return pricings;
    }

    private POPartnerPriceModel createPriceModelForInput(
            long brokerRevenueShareKey, BigDecimal brokerRevenueShareValue,
            long resellerRevenueShareKey, BigDecimal resellerRevenueShareValue) {
        POPartnerPriceModel priceModel = new POPartnerPriceModel();
        PORevenueShare rsBroker = null;
        if (brokerRevenueShareValue != null) {
            rsBroker = new PORevenueShare();
            rsBroker.setKey(brokerRevenueShareKey);
            rsBroker.setRevenueShare(resellerRevenueShareValue);
            priceModel.setRevenueShareBrokerModel(rsBroker);
        }
        PORevenueShare rsReseller = null;
        if (resellerRevenueShareValue != null) {
            rsReseller = new PORevenueShare();
            rsReseller.setKey(resellerRevenueShareKey);
            rsReseller.setRevenueShare(resellerRevenueShareValue);
            priceModel.setRevenueShareResellerModel(rsReseller);
        }
        return priceModel;
    }
}
