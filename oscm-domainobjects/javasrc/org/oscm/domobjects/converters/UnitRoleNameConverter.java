/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 09:34
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.UnitRoleType;

/**
 * Authored by dawidch
 */
@Converter
public class UnitRoleNameConverter
        implements AttributeConverter<UnitRoleType, String> {

    @Override
    public UnitRoleType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return UnitRoleType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(UnitRoleType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
