/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 08:45
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.TriggerProcessStatus;

/**
 * Authored by dawidch
 */
@Converter
public class TPSConverter
        implements AttributeConverter<TriggerProcessStatus, String> {

    @Override
    public TriggerProcessStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return TriggerProcessStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(TriggerProcessStatus enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
