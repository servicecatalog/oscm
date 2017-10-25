/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:23
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.PaymentProcessingStatus;

/**
 * Authored by dawidch
 */
@Converter
public class PaymentProcessingStatusConverter
        implements AttributeConverter<PaymentProcessingStatus, String> {

    @Override
    public PaymentProcessingStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return PaymentProcessingStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(PaymentProcessingStatus enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
