/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 26.08.16 15:18
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.OperationParameterType;

/**
 * Authored by dawidch
 */
@Converter
public class OPConverter
        implements AttributeConverter<OperationParameterType, String> {

    @Override
    public OperationParameterType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return OperationParameterType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(OperationParameterType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
