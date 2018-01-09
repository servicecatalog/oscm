/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 08:26
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ParameterType;

/**
 * Authored by dawidch
 */
@Converter
public class PTConverter implements AttributeConverter<ParameterType, String> {

    @Override
    public ParameterType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ParameterType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ParameterType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
