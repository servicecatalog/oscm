/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 15:05
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.EventType;

/**
 * Authored by dawidch
 */
@Converter
public class ETConverter implements AttributeConverter<EventType, String> {

    @Override
    public EventType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return EventType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(EventType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
