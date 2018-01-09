/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;

import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.domobjects.BillingResult;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

public class BillingServiceStub implements BillingService, BillingServiceLocal {

    @Override
    public List<BillingResult> generateBillingForAnyPeriod(long startOfPeriod,
            long endOfPeriod, long organizationKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startBillingRun(long currentTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getCustomerBillingData(Long from, Long to,
            List<String> customerIdList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getRevenueShareData(Long from, Long to,
            BillingSharesResultType resultType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BillingRun generatePaymentPreviewReport(long organizationKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BillingRun generatePaymentPreviewReport(long organizationKey,
            List<Long> unitKeys) throws BillingRunFailed {
        throw new UnsupportedOperationException();
    }

}
