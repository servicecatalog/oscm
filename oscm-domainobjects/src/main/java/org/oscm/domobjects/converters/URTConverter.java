/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:33
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * Authored by dawidch
 */
@Converter
public class URTConverter implements AttributeConverter<UserRoleType, String> {

    @Override
    public UserRoleType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return UserRoleType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(UserRoleType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
