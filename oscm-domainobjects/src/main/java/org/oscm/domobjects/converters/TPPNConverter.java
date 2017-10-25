/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:43
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * Authored by dawidch
 */
@Converter
public class TPPNConverter
        implements AttributeConverter<TriggerProcessParameterName, String> {

    @Override
    public TriggerProcessParameterName convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return TriggerProcessParameterName.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(TriggerProcessParameterName enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
