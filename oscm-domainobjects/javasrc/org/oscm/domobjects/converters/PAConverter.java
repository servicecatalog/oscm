/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 26.08.16 15:09
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.domobjects.enums.PublishingAccess;

/**
 * Authored by dawidch
 */
@Converter
public class PAConverter
        implements AttributeConverter<PublishingAccess, String> {

    @Override
    public PublishingAccess convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return PublishingAccess.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(PublishingAccess enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
