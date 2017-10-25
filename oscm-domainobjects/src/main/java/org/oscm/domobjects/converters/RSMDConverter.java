/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:05
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.domobjects.enums.RevenueShareModelType;

/**
 * Authored by dawidch
 */
@Converter
public class RSMDConverter
        implements AttributeConverter<RevenueShareModelType, String> {

    @Override
    public RevenueShareModelType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return RevenueShareModelType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(RevenueShareModelType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
