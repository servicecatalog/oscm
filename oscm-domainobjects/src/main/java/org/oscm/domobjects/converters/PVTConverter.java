/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:30
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ParameterValueType;

/**
 * Authored by dawidch
 */
@Converter
public class PVTConverter
        implements AttributeConverter<ParameterValueType, String> {

    @Override
    public ParameterValueType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ParameterValueType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ParameterValueType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
