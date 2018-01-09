/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *                                                                               
 *  Creation Date: 26.08.16 14:59
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Authored by dawidch
 */
@Converter
public class CKConverter
        implements AttributeConverter<ConfigurationKey, String> {

    @Override
    public ConfigurationKey convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return ConfigurationKey.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(ConfigurationKey configurationKey) {
        if (configurationKey == null) {
            return null;
        }
        return configurationKey.getKeyName();
    }
}
