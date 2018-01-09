/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 19.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share.setup;

import java.math.BigDecimal;

import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.internal.vo.VOService;

/**
 * @author baumann
 */
public class SharesSetup extends IntegrationTestSetup {

    protected void updateOperatorRevenueShare(double share, long key)
            throws Exception {
        container.login(basicSetup.getPlatformOperatorUserKey(),
                "PLATFORM_OPERATOR");
        sharesSetup.updateOperatorRevenueShare(BigDecimal.valueOf(share), key);
    }

    protected void updateMarketplaceRevenueShare(double share,
            String marketplaceId) throws Exception {
        container.login(basicSetup.getPlatformOperatorUserKey(),
                "PLATFORM_OPERATOR");
        sharesSetup.updateMarketplaceShare(marketplaceId,
                BigDecimal.valueOf(share));
    }

    protected void updatePartnerRevenueShares(double brokerShare,
            double resellerShare, VOService service) throws Exception {
        container.login(basicSetup.getPlatformOperatorUserKey(),
                "PLATFORM_OPERATOR");
        sharesSetup.updatePartnerRevenueShares(BigDecimal.valueOf(brokerShare),
                BigDecimal.valueOf(resellerShare), service);
    }

}
