/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 08:47
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.TriggerProcessIdentifierName;

/**
 * Authored by dawidch
 */
@Converter
public class TPINConverter
        implements AttributeConverter<TriggerProcessIdentifierName, String> {

    @Override
    public TriggerProcessIdentifierName convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return TriggerProcessIdentifierName.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(TriggerProcessIdentifierName enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
