/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import org.oscm.converter.api.Converter;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.vo.VOPricedRole;
import org.oscm.vo.VORoleDefinition;

public class ToExtPricedProductRoleStrategy extends AbstractConversionStrategy implements ConversionStrategy<PricedProductRole, VOPricedRole> {

    @Override
    public VOPricedRole convert(PricedProductRole pricedProductRole) {
        if (pricedProductRole == null) {
            return null;
        }

        VOPricedRole voPricedRole = new VOPricedRole();

        voPricedRole.setKey(pricedProductRole.getKey());
        voPricedRole.setVersion(pricedProductRole.getVersion());
        voPricedRole.setPricePerUser(pricedProductRole.getPricePerUser());
        RoleDefinition roleDefinition = pricedProductRole.getRoleDefinition();
        VORoleDefinition voRoleDefinition = Converter.convert(roleDefinition, RoleDefinition.class, VORoleDefinition.class, getDataService());
        voPricedRole.setRole(voRoleDefinition);


        return voPricedRole;
    }

}
