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

import org.oscm.internal.types.enumtypes.UdaConfigurationType;

/**
 * Authored by dawidch
 */
@Converter
public class UCTConverter
        implements AttributeConverter<UdaConfigurationType, String> {

    @Override
    public UdaConfigurationType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return UdaConfigurationType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(UdaConfigurationType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
