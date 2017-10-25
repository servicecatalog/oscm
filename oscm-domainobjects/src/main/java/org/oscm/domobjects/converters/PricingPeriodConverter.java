/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:14
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * Authored by dawidch
 */
@Converter
public class PricingPeriodConverter
        implements AttributeConverter<PricingPeriod, String> {

    @Override
    public PricingPeriod convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return PricingPeriod.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(PricingPeriod enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
