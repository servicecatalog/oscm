/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 30.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;

public class PricedProductRoleAssemblerTest {

    private LocalizerFacade facade = new LocalizerFacade(
            new LocalizerServiceStub() {

                @Override
                public String getLocalizedTextFromDatabase(String localeString,
                        long objectKey, LocalizedObjectTypes objectType) {
                    return "";
                }
            }, "en");

    @Test
    public void toVOPricedProductRole() throws Exception {
        // given
        RoleDefinition rd = new RoleDefinition();
        rd.setKey(1);
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPricePerUser(BigDecimal.valueOf(15L));
        ppr.setRoleDefinition(rd);

        // when
        VOPricedRole vo = PricedProductRoleAssembler.toVOPricedProductRole(ppr,
                facade);

        // then
        Assert.assertEquals(BigDecimal.valueOf(15L), vo.getPricePerUser());
        Assert.assertEquals(1, vo.getRole().getKey());
    }

    @Test
    public void toVOPricedProductRoles() throws Exception {
        // given
        RoleDefinition rd = new RoleDefinition();
        rd.setKey(1);
        RoleDefinition rd2 = new RoleDefinition();
        rd2.setKey(2);
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPricePerUser(BigDecimal.valueOf(15L));
        ppr.setRoleDefinition(rd);
        PricedProductRole ppr2 = new PricedProductRole();
        ppr2.setPricePerUser(BigDecimal.valueOf(75L));
        ppr2.setRoleDefinition(rd2);
        List<PricedProductRole> roles = new ArrayList<PricedProductRole>();
        roles.add(ppr);
        roles.add(ppr2);

        // when
        List<VOPricedRole> voPricedProductRoles = PricedProductRoleAssembler
                .toVOPricedProductRoles(roles, facade);

        // then
        Assert.assertEquals(2, voPricedProductRoles.size());
        Assert.assertEquals(BigDecimal.valueOf(15L), voPricedProductRoles
                .get(0).getPricePerUser());
        Assert.assertEquals(1, voPricedProductRoles.get(0).getRole().getKey());
        Assert.assertEquals(BigDecimal.valueOf(75L), voPricedProductRoles
                .get(1).getPricePerUser());
        Assert.assertEquals(2, voPricedProductRoles.get(1).getRole().getKey());
    }

    @Test
    public void toPricedProductRole() throws Exception {
        // given
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(12L));

        // when
        PricedProductRole pricedProductRole = PricedProductRoleAssembler
                .toPricedProductRole(ppr);

        // then
        Assert.assertEquals(BigDecimal.valueOf(12L),
                pricedProductRole.getPricePerUser());
    }

    @Test
    public void updatePricedProductRole() throws Exception {
        // given
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(12L));
        PricedProductRole pricedProductRole = PricedProductRoleAssembler
                .toPricedProductRole(ppr);
        ppr.setPricePerUser(BigDecimal.valueOf(13L));

        // when
        PricedProductRoleAssembler.updatePricedProductRole(ppr,
                pricedProductRole);

        // then
        Assert.assertEquals(BigDecimal.valueOf(13L),
                pricedProductRole.getPricePerUser());
    }

    @Test
    public void updatePricedProductRole_scale() throws Exception {
        // given
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(12L));
        PricedProductRole pricedProductRole = PricedProductRoleAssembler
                .toPricedProductRole(ppr);
        ppr.setPricePerUser(BigDecimal.valueOf(13L).setScale(
                PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));

        // when
        try {
            PricedProductRoleAssembler.updatePricedProductRole(ppr,
                    pricedProductRole);
            Assert.fail();
        } catch (ValidationException e) {
            // then
            Assert.assertEquals(ReasonEnum.SCALE_TO_LONG, e.getReason());
        }
    }

    @Test(expected = ValidationException.class)
    public void toPricedProductRole_negativePrice() throws Exception {
        // given
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(-12L));

        // when
        PricedProductRole pricedProductRole = PricedProductRoleAssembler
                .toPricedProductRole(ppr);

        // then
        Assert.assertEquals(BigDecimal.valueOf(12L),
                pricedProductRole.getPricePerUser());
    }

    @Test(expected = ValidationException.class)
    public void validatePricedProductRoles_nonPersistedRole() throws Exception {
        // given
        VORoleDefinition vrd = new VORoleDefinition();
        vrd.setRoleId("roleId");
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(123L));
        ppr.setRole(vrd);

        // when
        PricedProductRoleAssembler.validatePricedProductRoles(Collections
                .singletonList(ppr));
    }

    @Test(expected = ValidationException.class)
    public void validatePricedProductRoles_nullRole() throws Exception {
        // given
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(123L));
        ppr.setRole(null);

        // when
        PricedProductRoleAssembler.validatePricedProductRoles(Collections
                .singletonList(ppr));
    }

    @Test(expected = ValidationException.class)
    public void validatePricedProductRoles_duplicateRole() throws Exception {
        // given
        VORoleDefinition vrd = new VORoleDefinition();
        vrd.setRoleId("roleId");
        vrd.setKey(1L);
        VOPricedRole ppr = new VOPricedRole();
        ppr.setPricePerUser(BigDecimal.valueOf(123L));
        ppr.setRole(vrd);
        VOPricedRole ppr2 = new VOPricedRole();
        ppr2.setPricePerUser(BigDecimal.valueOf(1234L));
        ppr2.setRole(vrd);
        List<VOPricedRole> pricedRoles = new ArrayList<VOPricedRole>();
        pricedRoles.add(ppr);
        pricedRoles.add(ppr2);

        // when
        PricedProductRoleAssembler.validatePricedProductRoles(pricedRoles);
    }
}
