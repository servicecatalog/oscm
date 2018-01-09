/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.08.16 08:17
 *
 ******************************************************************************/

package org.oscm.domobjects.converters;

import javax.persistence.AttributeConverter;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Authored by dawidch
 */
public class ORTConverter
        implements AttributeConverter<OrganizationRoleType, String> {

    @Override
    public OrganizationRoleType convertToEntityAttribute(String s) {
        if (s == null) {
            return null;
        }
        return OrganizationRoleType.valueOf(s);
    }

    @Override
    public String convertToDatabaseColumn(OrganizationRoleType enumik) {
        if (enumik == null) {
            return null;
        }
        return enumik.name();
    }
}
