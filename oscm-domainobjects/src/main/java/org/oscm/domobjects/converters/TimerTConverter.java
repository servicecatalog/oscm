/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:57
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.TimerType;

/**
 * Authored by dawidch
 */
@Converter
public class TimerTConverter implements AttributeConverter<TimerType, String> {

    @Override
    public TimerType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return TimerType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(TimerType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
