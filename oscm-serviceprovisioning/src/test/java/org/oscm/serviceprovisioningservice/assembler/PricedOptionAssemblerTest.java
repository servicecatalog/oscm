/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 27, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.PricedOption;
import org.oscm.internal.types.exception.ValidationException;

/**
 * @author kulle
 * 
 */
public class PricedOptionAssemblerTest {

    @Test(expected = ValidationException.class)
    public void validatePricedOption_scaleTooLong() throws Exception {
        // given
        PricedOption pricedOption = givenVoPricedOption(
                BigDecimal.TEN
                        .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1),
                BigDecimal.ONE);

        // when
        PricedOptionAssembler.validatePricedOption(pricedOption);
    }

    private PricedOption givenVoPricedOption(BigDecimal pricePerUser,
            BigDecimal pricePerSubscription) {
        PricedOption po = new PricedOption();
        po.setPricePerUser(pricePerUser);
        po.setPricePerSubscription(pricePerSubscription);
        return po;
    }
}
