/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 15:07
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ImageType;

/**
 * Authored by dawidch
 */
@Converter
public class ITConverter implements AttributeConverter<ImageType, String> {

    @Override
    public ImageType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ImageType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ImageType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
