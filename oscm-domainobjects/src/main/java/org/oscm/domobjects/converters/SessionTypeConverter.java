/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:36
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.SessionType;

/**
 * Authored by dawidch
 */
@Converter
public class SessionTypeConverter
        implements AttributeConverter<SessionType, String> {

    @Override
    public SessionType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return SessionType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(SessionType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
