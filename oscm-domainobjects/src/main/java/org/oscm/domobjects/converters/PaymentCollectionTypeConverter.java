/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:20
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.PaymentCollectionType;

/**
 * Authored by dawidch
 */
@Converter
public class PaymentCollectionTypeConverter
        implements AttributeConverter<PaymentCollectionType, String> {

    @Override
    public PaymentCollectionType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return PaymentCollectionType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(PaymentCollectionType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
