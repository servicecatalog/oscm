/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 26.08.16 14:59
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * Authored by dawidch
 */
@Converter
public class BSRDConverter
        implements AttributeConverter<BillingSharesResultType, String> {

    @Override
    public BillingSharesResultType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return BillingSharesResultType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(BillingSharesResultType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
