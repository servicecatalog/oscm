/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 24.09.2012                                                                                                                                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * Utility class for the creation of BillingSharesResults
 * 
 * @author stavreva
 */
public class BillingSharesResults {

    public static BillingSharesResult createBillingSharesResult(DataService ds,
            BillingSharesResultType resultType, long orgKey, long periodStart,
            long periodEnd) throws Exception {
        BillingSharesResult bsr = new BillingSharesResult();
        bsr.setPeriodStartTime(periodStart);
        bsr.setPeriodEndTime(periodEnd);
        bsr.setResultType(resultType);
        bsr.setOrganizationTKey(orgKey);
        bsr.setResultXML("");
        ds.persist(bsr);
        return bsr;
    }
}
