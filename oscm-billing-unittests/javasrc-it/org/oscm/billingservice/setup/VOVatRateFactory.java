/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 22.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.math.BigDecimal;

import org.oscm.internal.vo.VOCountryVatRate;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationVatRate;
import org.oscm.internal.vo.VOVatRate;

/**
 * @author baumann
 */
public class VOVatRateFactory {

    public static VOVatRate newVOVatRate(BigDecimal rate) {
        VOVatRate vatRate = new VOVatRate();
        vatRate.setRate(rate);
        return vatRate;
    }

    public static VOCountryVatRate newVOCountryVatRate(BigDecimal rate,
            String countryCode) {
        VOCountryVatRate countryVatRate = new VOCountryVatRate();
        countryVatRate.setRate(rate);
        countryVatRate.setCountry(countryCode);
        return countryVatRate;
    }

    public static VOOrganizationVatRate newVOOrganizationVatRate(
            BigDecimal rate, VOOrganization organization) {
        VOOrganizationVatRate orgVatRate = new VOOrganizationVatRate();
        orgVatRate.setRate(rate);
        orgVatRate.setOrganization(organization);
        return orgVatRate;
    }

}
