/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-9-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.PriceModelException;
import org.oscm.internal.vo.VOPriceModel;

/**
 * @author zhaohang
 * 
 */
public class ServiceProvisioningServiceBeanValidatePriceModelTest {

    private ServiceProvisioningServiceBean bean;
    private final String CURRENCYISOCODE_USD = "USD";
    private final String CURRENCYISOCODE_EUR = "EUR";
    private final PricingPeriod Period_WEEK = PricingPeriod.WEEK;
    private final PricingPeriod Period_DAY = PricingPeriod.DAY;
    private PriceModel existingPriceModel;

    @Before
    public void before() {
        bean = spy(new ServiceProvisioningServiceBean());
        existingPriceModel = givenPriceModel(CURRENCYISOCODE_USD, Period_WEEK);
    }

    @Test(expected = PriceModelException.class)
    public void validateCurrency_CurrencyChanged() throws Exception {
        // given
        VOPriceModel inputPriceModel = givenVOPriceModel(CURRENCYISOCODE_EUR,
                Period_WEEK);
        String errorMessage = "ex.PriceModelException.UNMODIFIABLE_CURRENCY";

        // when
        try {
            bean.validateCurrency(existingPriceModel, inputPriceModel);
        } catch (PriceModelException e) {
            assertEquals(errorMessage, e.getMessageKey());

            throw e;
        }
    }

    @Test(expected = PriceModelException.class)
    public void validateTimeUnit_TimeUnitChanged() throws Exception {
        // given
        VOPriceModel inputPriceModel = givenVOPriceModel(CURRENCYISOCODE_USD,
                Period_DAY);
        String errorMessage = "ex.PriceModelException.UNMODIFIABLE_TIMEUNIT";

        // when
        try {
            bean.validateTimeUnit(existingPriceModel, inputPriceModel);
        } catch (PriceModelException e) {
            assertEquals(errorMessage, e.getMessageKey());

            throw e;
        }
    }

    @Test(expected = PriceModelException.class)
    public void validatePriceModelType_TypeChanged() throws Exception {
        // given
        VOPriceModel inputPriceModel = givenVOPriceModel(CURRENCYISOCODE_USD,
                Period_WEEK);
        inputPriceModel.setType(PriceModelType.PRO_RATA);
        String errorMessage = "ex.PriceModelException.UNMODIFIABLE_TYPE";

        // when
        try {
            bean.validatePriceModelType(existingPriceModel, inputPriceModel);
        } catch (PriceModelException e) {
            assertEquals(errorMessage, e.getMessageKey());

            throw e;
        }
    }

    private PriceModel givenPriceModel(String currencyISOCode,
            PricingPeriod pricingPeriod) {
        PriceModel model = new PriceModel();
        SupportedCurrency currency = new SupportedCurrency(currencyISOCode);
        model.setType(PriceModelType.PER_UNIT);
        model.setCurrency(currency);
        model.setPeriod(pricingPeriod);
        return model;
    }

    private VOPriceModel givenVOPriceModel(String currencyISOCode,
            PricingPeriod pricingPeriod) {
        VOPriceModel model = new VOPriceModel();
        model.setType(PriceModelType.PER_UNIT);
        model.setCurrencyISOCode(currencyISOCode);
        model.setPeriod(pricingPeriod);
        return model;
    }
}
