/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 26.08.16 15:20
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.OperationStatus;

/**
 * Authored by dawidch
 */
@Converter
public class OSConverter
        implements AttributeConverter<OperationStatus, String> {

    @Override
    public OperationStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return OperationStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(OperationStatus enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
