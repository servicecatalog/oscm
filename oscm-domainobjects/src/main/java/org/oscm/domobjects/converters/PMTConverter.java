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

import org.oscm.internal.types.enumtypes.ParameterModificationType;

/**
 * Authored by dawidch
 */
@Converter
public class PMTConverter
        implements AttributeConverter<ParameterModificationType, String> {

    @Override
    public ParameterModificationType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ParameterModificationType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ParameterModificationType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
