/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 15:08
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.domobjects.enums.LocalizedBillingResourceType;

/**
 * Authored by dawidch
 */
@Converter
public class LBRTConverter
        implements AttributeConverter<LocalizedBillingResourceType, String> {

    @Override
    public LocalizedBillingResourceType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return LocalizedBillingResourceType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(LocalizedBillingResourceType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
