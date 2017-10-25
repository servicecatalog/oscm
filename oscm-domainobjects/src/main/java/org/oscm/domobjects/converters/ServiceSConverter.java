/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:10
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ServiceStatus;

/**
 * Authored by dawidch
 */
@Converter
public class ServiceSConverter
        implements AttributeConverter<ServiceStatus, String> {

    @Override
    public ServiceStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ServiceStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ServiceStatus enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
