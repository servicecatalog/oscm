/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 20.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.math.BigDecimal;
import java.util.Arrays;

import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.marketplace.MarketplaceServiceManagePartner;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POMarketplacePricing;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POOrganization;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.POServicePricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;

/**
 * @author kulle
 * 
 */
public class TestSharesSetup {

    private final PricingService pricingService;
    private final MarketplaceServiceManagePartner mpManagePartner;
    private final MarketplaceService mpService;

    public TestSharesSetup(TestContainer container) {
        pricingService = container.get(PricingService.class);
        mpManagePartner = container.get(MarketplaceServiceManagePartner.class);
        mpService = container.get(MarketplaceService.class);
    }

    public void updateOperatorRevenueShare(BigDecimal share, long serviceKey)
            throws Exception {
        Response response = pricingService.getOperatorRevenueShare(serviceKey);
        POOperatorPriceModel result = response
                .getResult(POOperatorPriceModel.class);
        PORevenueShare revenueShare = result.getRevenueShare();
        revenueShare.setRevenueShare(share);
        pricingService.saveOperatorRevenueShare(serviceKey, revenueShare);
    }

    public void updateMarketplaceShare(String marketplaceId, BigDecimal share)
            throws Exception {

        VOMarketplace mp = mpService.getMarketplaceById(marketplaceId);
        Response response = pricingService
                .getPricingForMarketplace(marketplaceId);
        POMarketplacePricing pricing = response
                .getResult(POMarketplacePricing.class);
        POMarketplacePriceModel marketplacePriceModel = pricing
                .getMarketplacePriceModel();
        POPartnerPriceModel partnerPriceModel = pricing.getPartnerPriceModel();

        marketplacePriceModel.getRevenueShare().setRevenueShare(share);
        mpManagePartner.updateMarketplace(mp, marketplacePriceModel,
                partnerPriceModel);
    }

    public void updatePartnerRevenueShares(BigDecimal brokerShare,
            BigDecimal resellerShare, VOService service) throws Exception {
        POServiceForPricing serviceForPricing = newPOServiceForPricing(service);

        POPartnerPriceModel partnerPriceModel = getPartnerRevenueShareForService(serviceForPricing);
        setSharesInPOPartnerPriceModel(partnerPriceModel, brokerShare,
                resellerShare);

        POServicePricing servicePricing = newPOServicePricing(
                serviceForPricing, partnerPriceModel);

        pricingService.savePartnerRevenueSharesForServices(Arrays
                .asList(new POServicePricing[] { servicePricing }));
    }

    public POPartnerPriceModel getPartnerRevenueShareForService(
            VOService service) throws Exception {
        return getPartnerRevenueShareForService(newPOServiceForPricing(service));
    }

    private POPartnerPriceModel getPartnerRevenueShareForService(
            POServiceForPricing serviceForPricing) throws Exception {
        Response response = pricingService
                .getPartnerRevenueShareForService(serviceForPricing);
        return response.getResult(POPartnerPriceModel.class);
    }

    private POServicePricing newPOServicePricing(
            POServiceForPricing serviceForPricing,
            POPartnerPriceModel partnerPriceModel) {
        POServicePricing servicePricing = new POServicePricing();
        servicePricing.setServiceForPricing(serviceForPricing);
        servicePricing.setPartnerPriceModel(partnerPriceModel);
        return servicePricing;
    }

    private POServiceForPricing newPOServiceForPricing(VOService service) {
        POServiceForPricing serviceForPricing = new POServiceForPricing(
                service.getKey(), service.getVersion());
        serviceForPricing.setVendor(newPOOrganization(service.getSellerId(),
                service.getSellerName()));
        return serviceForPricing;
    }

    private POOrganization newPOOrganization(String organizationId,
            String organizationName) {
        POOrganization poOrg = new POOrganization();
        poOrg.setOrganizationId(organizationId);
        poOrg.setOrganizationName(organizationName);
        return poOrg;
    }

    private void setSharesInPOPartnerPriceModel(
            POPartnerPriceModel poPartnerPM, BigDecimal brokerShare,
            BigDecimal resellerShare) {
        PORevenueShare poBrokerRevenueShare = poPartnerPM
                .getRevenueShareBrokerModel();
        if (poBrokerRevenueShare != null) {
            poBrokerRevenueShare.setRevenueShare(brokerShare);
        } else {
            poPartnerPM.setRevenueShareBrokerModel(new PORevenueShare(
                    brokerShare));
        }

        PORevenueShare poResellerRevenueShare = poPartnerPM
                .getRevenueShareResellerModel();
        if (poResellerRevenueShare != null) {
            poResellerRevenueShare.setRevenueShare(resellerShare);
        } else {
            poPartnerPM.setRevenueShareResellerModel(new PORevenueShare(
                    resellerShare));
        }
    }

}
