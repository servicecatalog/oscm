/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Unit testing of the CRUD operations of {@link MarketplaceServiceLocalBean}.
 * 
 * @author barzu
 */
public class MarketplaceServiceLocalBeanCRUDTest {

    private MarketplaceServiceLocalBean mpSrvLocal;

    @Before
    public void setup() throws Exception {
        mpSrvLocal = new MarketplaceServiceLocalBean();
        mpSrvLocal.ds = mock(DataService.class);
    }

    @Test
    public void createRevenueModels() throws Exception {
        Marketplace mp = new Marketplace();

        mpSrvLocal.createRevenueModels(mp, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO);

        assertEquals(RevenueShareModelType.BROKER_REVENUE_SHARE, mp
                .getBrokerPriceModel().getRevenueShareModelType());
        assertEquals(BigDecimal.ZERO, mp.getBrokerPriceModel()
                .getRevenueShare());
        verify(mpSrvLocal.ds, times(1))
                .persist(
                        argThat(isZeroRevenueShare(RevenueShareModelType.BROKER_REVENUE_SHARE)));

        assertEquals(RevenueShareModelType.RESELLER_REVENUE_SHARE, mp
                .getResellerPriceModel().getRevenueShareModelType());
        assertEquals(BigDecimal.ZERO, mp.getResellerPriceModel()
                .getRevenueShare());
        verify(mpSrvLocal.ds, times(1))
                .persist(
                        argThat(isZeroRevenueShare(RevenueShareModelType.RESELLER_REVENUE_SHARE)));

        assertEquals(RevenueShareModelType.MARKETPLACE_REVENUE_SHARE, mp
                .getPriceModel().getRevenueShareModelType());
        assertEquals(BigDecimal.ZERO, mp.getPriceModel().getRevenueShare());
        verify(mpSrvLocal.ds, times(1))
                .persist(
                        argThat(isZeroRevenueShare(RevenueShareModelType.MARKETPLACE_REVENUE_SHARE)));
    }

    @Test
    public void createRevenueModels_NonUniqueBusinessKeyException()
            throws Exception {
        NonUniqueBusinessKeyException e = new NonUniqueBusinessKeyException();
        doThrow(e).when(mpSrvLocal.ds).persist(any(RevenueShareModel.class));

        try {
            mpSrvLocal.createRevenueModels(new Marketplace(), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            fail();
        } catch (SaaSSystemException ex) {
            assertEquals(e, ex.getCause());
        }
    }

    private ArgumentMatcher<RevenueShareModel> isZeroRevenueShare(
            final RevenueShareModelType type) {
        return new ArgumentMatcher<RevenueShareModel>() {
            @Override
            public boolean matches(Object argument) {
                RevenueShareModel model = ((RevenueShareModel) argument);
                return type.equals(model.getRevenueShareModelType())
                        && BigDecimal.ZERO.equals(model.getRevenueShare());
            }
        };
    }

}
