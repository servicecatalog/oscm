/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 09:14
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.PriceModelType;

/**
 * Authored by dawidch
 */
@Converter
public class PriceModelTConverter
        implements AttributeConverter<PriceModelType, String> {

    @Override
    public PriceModelType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return PriceModelType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(PriceModelType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
