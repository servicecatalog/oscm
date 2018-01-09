/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 08:41
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Authored by dawidch
 */
@Converter
public class UTTConverter implements AttributeConverter<UdaTargetType, String> {

    @Override
    public UdaTargetType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return UdaTargetType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(UdaTargetType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
