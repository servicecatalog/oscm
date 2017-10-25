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

public class ToPricedRoleStrategy extends AbstractConversionStrategy
        implements ConversionStrategy<PricedProductRole, VOPricedRole> {
    @Override
    public VOPricedRole convert(PricedProductRole pricedProductRole) {
        if (pricedProductRole == null) {
            return null;
        }
        VOPricedRole voPricedRole = new VOPricedRole();

        voPricedRole.setRole(Converter.convert(pricedProductRole.getRoleDefinition(), RoleDefinition.class, VORoleDefinition.class));
        voPricedRole.setPricePerUser(pricedProductRole.getPricePerUser());
        voPricedRole.setKey(pricedProductRole.getKey());
        voPricedRole.setVersion(pricedProductRole.getVersion());

        return voPricedRole;
    }
}
