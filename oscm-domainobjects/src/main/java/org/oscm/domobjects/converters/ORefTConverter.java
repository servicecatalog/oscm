/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 29.08.16 08:22
 *
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;

import org.oscm.domobjects.enums.OrganizationReferenceType;

/**
 * Authored by dawidch
 */
public class ORefTConverter
        implements AttributeConverter<OrganizationReferenceType, String> {

    @Override
    public OrganizationReferenceType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return OrganizationReferenceType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(OrganizationReferenceType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
