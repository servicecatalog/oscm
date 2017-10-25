/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:16
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.Salutation;

/**
 * Authored by dawidch
 */
@Converter
public class SalutaionConverter
        implements AttributeConverter<Salutation, String> {

    @Override
    public Salutation convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        if (s == null) {
            return null;
        }
        return Salutation.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(Salutation enumik) {
        if (enumik == null) {
            return null;
        }
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
