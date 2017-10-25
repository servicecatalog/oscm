/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 3, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import java.math.BigDecimal;
import java.util.List;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.business.calculation.BigDecimals;

/**
 * @author farmaki
 * 
 */
public class DiscountCalculator {

    public static BigDecimal calculateServiceRevenue(XmlSearch xmlSearch,
            Long pmKey) {
        BigDecimal netAmountBeforeDiscount = BigDecimal.ZERO;

        List<BigDecimal> amounts = xmlSearch.retrieveNetAmounts(pmKey);
        BigDecimal percent = xmlSearch.retrieveDiscountPercent();

        for (int i = 0; i < amounts.size(); i++) {
            netAmountBeforeDiscount = netAmountBeforeDiscount.add(amounts
                    .get(i));

        }
        if (percent == null) {
            return netAmountBeforeDiscount;
        }

        else {
            BigDecimal discountValue = BigDecimals.calculatePercent(percent,
                    netAmountBeforeDiscount);

            BigDecimal netAmountAfterDiscount = netAmountBeforeDiscount
                    .subtract(discountValue);
            return netAmountAfterDiscount;
        }
    }
}
