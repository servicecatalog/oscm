/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:54
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Authored by dawidch
 */
@Converter
public class TTConverter implements AttributeConverter<TriggerType, String> {

    @Override
    public TriggerType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return TriggerType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(TriggerType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
