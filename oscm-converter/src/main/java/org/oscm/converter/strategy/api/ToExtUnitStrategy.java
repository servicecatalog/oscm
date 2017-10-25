/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 21.07.15 16:07
 *
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import org.oscm.domobjects.UserGroup;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.vo.VOOrganizationalUnit;

public class ToExtUnitStrategy extends AbstractConversionStrategy implements
        ConversionStrategy<UserGroup, VOOrganizationalUnit> {

    @Override
    public VOOrganizationalUnit convert(UserGroup userGroup) {
        if (userGroup == null) {
            return null;
        }

        VOOrganizationalUnit unit = new VOOrganizationalUnit();

        // Base attributes
        unit.setKey(userGroup.getKey());
        unit.setVersion(userGroup.getVersion());

        unit.setName(userGroup.getName());
        unit.setDescription(userGroup.getDescription());
        unit.setReferenceId(userGroup.getReferenceId());
        unit.setDefaultGroup(userGroup.isDefault());
        unit.setOrganizationId(userGroup.getOrganization().getOrganizationId());

        return unit;
    }
}
