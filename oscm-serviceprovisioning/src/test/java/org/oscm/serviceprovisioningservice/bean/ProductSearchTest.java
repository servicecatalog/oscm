/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.test.data.UserRoles;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.ListCriteria;

/**
 * @author weiser
 * 
 */
public class ProductSearchTest {

    private static final String RESTRICTION_SUSPENDED_OR_ACTIVE = " (p.status = 'SUSPENDED' OR p.status = 'ACTIVE')";
    private static final String RESTRICTION_ACTIVE = " (p.status = 'ACTIVE')";
    private static final String RESTRICTION_CATEGORY = " AND EXISTS ( SELECT p.tkey   FROM category c, categorytocatalogentry ctce, catalogentry ce  WHERE c.categoryid = :categoryId    AND c.marketplacekey = :marketplaceKey    AND c.tkey = ctce.category_tkey    AND ctce.catalogentry_tkey = ce.tkey    AND (ce.product_tkey = p.tkey OR (ce.product_tkey = p.template_tkey AND p.type = 'CUSTOMER_TEMPLATE')) )";

    private ProductSearch ps;

    private DataService ds;

    private PlatformUser user;
    private Organization org;
    private Marketplace mp;

    @Before
    public void setup() throws Exception {
        org = new Organization();

        user = new PlatformUser();
        user.setOrganization(org);
        user.setAssignedRoles(UserRoles.createRoleAssignments(user,
                UserRoleType.MARKETPLACE_OWNER));

        mp = new Marketplace();
        mp.setMarketplaceId("marketplaceId");
        mp.setOrganization(org);

        ds = mock(DataService.class);

        // required as constructor calls this method
        when(ds.getCurrentUserIfPresent()).thenReturn(user);
        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setCategoryId("categoryId");
        ps = new ProductSearch(ds, mp.getMarketplaceId(), listCriteria, "en",
                "en", null);
        ps.currentUsersOrg = org;
        ps.marketplace = mp;
        ps.user = user;
    }

    @Test
    public void prepareProductStatusRestriction() {
        String actual = ps.prepareProductStatusRestriction();

        assertEquals(RESTRICTION_SUSPENDED_OR_ACTIVE, actual);
    }

    @Test
    public void prepareProductStatusRestriction_NotOwner() {
        mp.setOrganization(new Organization());

        String actual = ps.prepareProductStatusRestriction();

        assertEquals(RESTRICTION_ACTIVE, actual);
    }

    @Test
    public void prepareProductStatusRestriction_Anonymous() {
        ps.user = null;

        String actual = ps.prepareProductStatusRestriction();

        assertEquals(RESTRICTION_ACTIVE, actual);
    }

    @Test
    public void prepareProductStatusRestriction_DifferentRole() {
        user.setAssignedRoles(UserRoles.createRoleAssignments(user,
                UserRoleType.ORGANIZATION_ADMIN));

        String actual = ps.prepareProductStatusRestriction();

        assertEquals(RESTRICTION_ACTIVE, actual);
    }

    // bug 10055
    @Test
    public void prepareCategoryRestriction() throws Exception {
        String actual = ps.prepareCategoryRestriction();
        assertEquals(RESTRICTION_CATEGORY, actual);
    }
}
