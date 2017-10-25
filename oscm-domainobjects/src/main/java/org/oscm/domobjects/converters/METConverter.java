/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 15:16
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.domobjects.enums.ModifiedEntityType;

/**
 * Authored by dawidch
 */
@Converter
public class METConverter
        implements AttributeConverter<ModifiedEntityType, String> {

    @Override
    public ModifiedEntityType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ModifiedEntityType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ModifiedEntityType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
