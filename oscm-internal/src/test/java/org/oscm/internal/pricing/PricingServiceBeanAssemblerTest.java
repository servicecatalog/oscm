/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.components.POMarketplace;

/**
 * Tests the assembler methods of the PricingServiceBean
 * 
 * @author barzu
 */
public class PricingServiceBeanAssemblerTest {

    private static final String MARKETPLACEID = "1234567";
    private static final String MARKETPLACE_DISPLAYNAME = "marketplace_display";

    @Test
    public void toPOMarketplace() {
        // given
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(1);
        marketplace.setMarketplaceId(MARKETPLACEID);
        LocalizerFacade localizerFacadeMock = mock(LocalizerFacade.class);
        when(
                localizerFacadeMock.getText(marketplace.getKey(),
                        LocalizedObjectTypes.MARKETPLACE_NAME)).thenReturn(
                MARKETPLACE_DISPLAYNAME);

        // when retrieving the POMarketplace
        POMarketplace poMarketplace = PricingServiceBean.toPOMarketplace(
                marketplace, localizerFacadeMock);

        // verify that the POMarketplace has the correct values.
        assertNotNull(poMarketplace);
        assertEquals(marketplace.getKey(), poMarketplace.getKey());
        assertEquals(marketplace.getVersion(), poMarketplace.getVersion());
        assertEquals(MARKETPLACEID, poMarketplace.getMarketplaceId());
        assertEquals(MARKETPLACE_DISPLAYNAME, poMarketplace.getDisplayName());
    }

    @Test
    public void toPOMarketplacePriceModel() {
        // given a revenue share model
        RevenueShareModel revenueShareModel = createRevenueModel(RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);

        // when retrieving the POMarketplacePriceModel for the revenue share
        // model
        POMarketplacePriceModel poPriceModel = PricingServiceBean
                .toPOMarketplacePriceModel(revenueShareModel);

        // verify that the PORevenueShare has the correct values.
        assertNotNull(poPriceModel);
        PORevenueShare poRevenueShare = poPriceModel.getRevenueShare();

        assertEquals(revenueShareModel.getRevenueShare(),
                poRevenueShare.getRevenueShare());
        assertEquals(revenueShareModel.getKey(), poRevenueShare.getKey());
        assertEquals(revenueShareModel.getVersion(),
                poRevenueShare.getVersion());
    }

    @Test
    public void toPOPartnerPriceModel() {
        // given a marketplace with a broker and a reseller revenue share model
        Marketplace mp = new Marketplace("mId");
        RevenueShareModel brokerPriceModel = createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE);

        mp.setBrokerPriceModel(brokerPriceModel);
        mp.setResellerPriceModel(resellerPriceModel);

        // when retrieving the POPartnerPriceModel for the marketplace
        POPartnerPriceModel poPartnerModel = PricingServiceBean
                .toPOPartnerPriceModel(mp);

        // verify that the PORevenueShares for broker and reseller
        // have the correct values.
        assertNotNull(poPartnerModel);
        PORevenueShare poBrokerRevenueShare = poPartnerModel
                .getRevenueShareBrokerModel();

        assertEquals(brokerPriceModel.getRevenueShare(),
                poBrokerRevenueShare.getRevenueShare());
        assertEquals(brokerPriceModel.getKey(), poBrokerRevenueShare.getKey());
        assertEquals(brokerPriceModel.getVersion(),
                poBrokerRevenueShare.getVersion());

        PORevenueShare poResellerRevenueShare = poPartnerModel
                .getRevenueShareResellerModel();

        assertEquals(resellerPriceModel.getRevenueShare(),
                poResellerRevenueShare.getRevenueShare());
        assertEquals(resellerPriceModel.getKey(),
                poResellerRevenueShare.getKey());
        assertEquals(resellerPriceModel.getVersion(),
                poResellerRevenueShare.getVersion());

    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type) {
        RevenueShareModel m = new RevenueShareModel();
        m.setRevenueShare(BigDecimal.ZERO);
        m.setRevenueShareModelType(type);
        return m;
    }

}
