/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:33
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * Authored by dawidch
 */
@Converter
public class ServiceAccessTypeConverter
        implements AttributeConverter<ServiceAccessType, String> {

    @Override
    public ServiceAccessType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ServiceAccessType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ServiceAccessType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
