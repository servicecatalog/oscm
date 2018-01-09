/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 29.08.16 09:03
 *    
 ******************************************************************************/

package org.oscm.app.converters;

import org.oscm.app.domain.ProvisioningStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Authored by dawidch
 */
@Converter
public class PSConverter
        implements AttributeConverter<ProvisioningStatus, String> {

    @Override
    public ProvisioningStatus convertToEntityAttribute(String s) {
        return ProvisioningStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ProvisioningStatus enumik) {
        return enumik.name();
    }
}
