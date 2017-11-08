/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 17.11.2010                                                      
 *                                                                              
 *  Completion Time: 18.11.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.data;

import java.math.BigDecimal;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.SupportedCountry;
import org.oscm.domobjects.VatRate;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author pock
 * 
 */
public class VatRates {

    public static VatRate createVatRate(DataService mgr,
            Organization owningOrganization, BigDecimal rate,
            final String targetcountryCode,
            final Organization targetOrganization)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {

        VatRate vatRate = new VatRate();
        vatRate.setOwningOrganization(owningOrganization);
        vatRate.setRate(rate);
        if (targetcountryCode != null) {
            SupportedCountry sc = (SupportedCountry) mgr
                    .getReferenceByBusinessKey(new SupportedCountry(
                            targetcountryCode));
            vatRate.setTargetCountry(sc);
        }
        if (targetOrganization != null) {
            vatRate.setTargetOrganization(targetOrganization);
        }

        mgr.persist(vatRate);
        return vatRate;
    }

}
