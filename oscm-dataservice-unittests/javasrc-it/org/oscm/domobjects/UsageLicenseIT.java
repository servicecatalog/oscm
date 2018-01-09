/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 01.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;

import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author weiser
 * 
 */
public class UsageLicenseIT extends DomainObjectTestBase {

    @Test
    public void getForUser() throws Exception {
        final Long userKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        false, "getForUser");

                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, org, "tp", false, ServiceAccessType.LOGIN);

                TechnicalProduct tp_roles = TechnicalProducts
                        .createTechnicalProduct(mgr, org, "tp_roles", false,
                                ServiceAccessType.LOGIN);
                RoleDefinition role = TechnicalProducts.addRoleDefinition(
                        "role1", tp_roles, mgr);
                TechnicalProducts.addRoleDefinition("role2", tp_roles, mgr);

                Product p = Products.createProduct(org, tp, false, "p1", "pm1",
                        mgr);
                Product p_roles = Products.createProduct(org, tp_roles, false,
                        "p3", "pm3", mgr);

                Subscription sub = Subscriptions.createSubscription(mgr,
                        org.getOrganizationId(), p.getProductId(), "s1", org);
                Subscriptions.createUsageLicense(mgr, user, sub);

                Subscription sub_roles = Subscriptions.createSubscription(mgr,
                        org.getOrganizationId(), p_roles.getProductId(),
                        "s4_roles", org);
                Subscriptions.createUsageLicense(mgr, user, sub_roles, role);

                Subscriptions.createSubscription(mgr, org.getOrganizationId(),
                        p_roles.getProductId(), "notAssigned", org);
                return Long.valueOf(user.getKey());
            }
        });

        List<UsageLicense> list = runTX(new Callable<List<UsageLicense>>() {

            @Override
            public List<UsageLicense> call() throws Exception {
                Query q = mgr.createNamedQuery("UsageLicense.getForUser");
                q.setParameter("status",
                        EnumSet.allOf(SubscriptionStatus.class));
                q.setParameter("userKey", userKey);
                @SuppressWarnings("unchecked")
                List<UsageLicense> list = q.getResultList();
                return list;
            }
        });

        assertEquals(2, list.size());

        UsageLicense lic = list.get(0);
        assertEquals("s1", lic.getSubscription().getSubscriptionId());
        assertNull(lic.getRoleDefinition());

        lic = list.get(1);
        assertEquals("s4_roles", lic.getSubscription().getSubscriptionId());
        assertEquals("role1", lic.getRoleDefinition().getRoleId());
    }

}
