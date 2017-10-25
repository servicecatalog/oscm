/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 09:06
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.types.enumtypes.FillinCriterion;

/**
 * Authored by dawidch
 */
@Converter
public class FCConverter
        implements AttributeConverter<FillinCriterion, String> {

    @Override
    public FillinCriterion convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return FillinCriterion.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(FillinCriterion enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
