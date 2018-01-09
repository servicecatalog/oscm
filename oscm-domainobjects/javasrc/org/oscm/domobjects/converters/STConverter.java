/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 09:11
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ServiceType;

/**
 * Authored by dawidch
 */
@Converter
public class STConverter implements AttributeConverter<ServiceType, String> {

    @Override
    public ServiceType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ServiceType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ServiceType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
