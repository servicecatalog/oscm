/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 14:59
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * Authored by dawidch
 */
@Converter
public class LOTConverter
        implements AttributeConverter<LocalizedObjectTypes, String> {

    @Override
    public LocalizedObjectTypes convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return LocalizedObjectTypes.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(LocalizedObjectTypes enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
