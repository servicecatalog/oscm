/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:24
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.SettingType;

/**
 * Authored by dawidch
 */
@Converter
public class SettingTypeConverter
        implements AttributeConverter<SettingType, String> {

    @Override
    public SettingType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return SettingType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(SettingType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
