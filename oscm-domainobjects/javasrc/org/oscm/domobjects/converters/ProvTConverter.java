/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 08:58
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.ProvisioningType;

/**
 * Authored by dawidch
 */
@Converter
public class ProvTConverter
        implements AttributeConverter<ProvisioningType, String> {

    @Override
    public ProvisioningType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ProvisioningType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ProvisioningType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
