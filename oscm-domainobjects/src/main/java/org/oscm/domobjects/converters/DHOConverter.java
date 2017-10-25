/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 15:02
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.domobjects.enums.ModificationType;

/**
 * Authored by dawidch
 */
@Converter
public class DHOConverter
        implements AttributeConverter<ModificationType, String> {

    @Override
    public ModificationType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ModificationType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ModificationType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
