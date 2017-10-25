/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import java.util.List;

import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.vo.VORoleDefinition;

public class ToExtRoleDefinitionStrategy extends AbstractConversionStrategy implements ConversionStrategy<RoleDefinition, VORoleDefinition> {

    @Override
    public VORoleDefinition convert(RoleDefinition roleDefinition) {
        if (roleDefinition == null) {
            return null;
        }

        VORoleDefinition voPricedRoleDefinition = new VORoleDefinition();

        voPricedRoleDefinition.setKey(roleDefinition.getKey());
        voPricedRoleDefinition.setVersion(roleDefinition.getVersion());
        voPricedRoleDefinition.setRoleId(roleDefinition.getRoleId());
        final List<LocalizedObjectTypes> localizedObjectTypes = roleDefinition.getLocalizedObjectTypes();
        final String locale = getDataService().getCurrentUser().getLocale();
        final List<LocalizedResource> localizedResources = getLocalizedResource(localizedObjectTypes, Long.valueOf(roleDefinition.getKey()), locale);

        for (LocalizedResource resource : localizedResources) {
            if (resource.getObjectType().equals(LocalizedObjectTypes.ROLE_DEF_DESC)){
                voPricedRoleDefinition.setDescription(resource.getValue());
            }
            else if (resource.getObjectType().equals(LocalizedObjectTypes.ROLE_DEF_NAME)) {
                voPricedRoleDefinition.setName(resource.getValue());
            }

        }
        return voPricedRoleDefinition;
    }

}
