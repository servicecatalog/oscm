/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *                                                                               
 *  Creation Date: 29.08.16 08:59
 *    
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * Authored by dawidch
 */
@Converter
public class SSConverter
        implements AttributeConverter<SubscriptionStatus, String> {

    @Override
    public SubscriptionStatus convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return SubscriptionStatus.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(SubscriptionStatus enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
