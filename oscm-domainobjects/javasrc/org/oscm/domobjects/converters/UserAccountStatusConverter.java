/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 09:17
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.UserAccountStatus;

/**
 * Authored by dawidch
 */
@Converter
public class UserAccountStatusConverter
        implements AttributeConverter<UserAccountStatus, String> {

    @Override
    public UserAccountStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return UserAccountStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(UserAccountStatus enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
