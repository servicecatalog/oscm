/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                   
 *                                                                              
 *  Creation Date: 28.06.2010                                                      
 *                                                                              
 *  Completion Time: <date>                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.vo.VORoleDefinition;

/**
 * @author weiser
 * 
 */
public class RoleAssembler extends BaseAssembler {

    /**
     * Converts the provided list of {@link RoleDefinition} to a list of
     * {@link VORoleDefinition} setting name and description for the locale
     * configured for the provided {@link LocalizerFacade}.
     * 
     * @param roleDefinitions
     *            the {@link RoleDefinition}s to convert
     * @param facade
     *            the {@link LocalizerFacade} to get name and description
     * @return the list of {@link VORoleDefinition}s
     */
    public static List<VORoleDefinition> toVORoleDefinitions(
            List<RoleDefinition> roleDefinitions, LocalizerFacade facade) {
        if (roleDefinitions == null) {
            return new ArrayList<VORoleDefinition>();
        }
        List<VORoleDefinition> result = new ArrayList<VORoleDefinition>();
        for (RoleDefinition def : roleDefinitions) {
            result.add(toVORoleDefinition(def, facade));
        }
        return result;
    }

    /**
     * Converts the provided {@link RoleDefinition} to a
     * {@link VORoleDefinition} setting name and description for the locale
     * configured for the provided {@link LocalizerFacade}.
     * 
     * @param def
     *            the {@link RoleDefinition} to convert
     * @param facade
     *            the {@link LocalizerFacade} to get name and description
     * @return the {@link VORoleDefinition}
     */
    public static VORoleDefinition toVORoleDefinition(RoleDefinition def,
            LocalizerFacade facade) {
        if (def == null) {
            return null;
        }
        VORoleDefinition voDef = new VORoleDefinition();
        updateValueObject(voDef, def);
        voDef.setRoleId(def.getRoleId());
        voDef.setDescription(facade.getText(def.getKey(),
                LocalizedObjectTypes.ROLE_DEF_DESC));
        voDef.setName(facade.getText(def.getKey(),
                LocalizedObjectTypes.ROLE_DEF_NAME));
        return voDef;
    }

}
