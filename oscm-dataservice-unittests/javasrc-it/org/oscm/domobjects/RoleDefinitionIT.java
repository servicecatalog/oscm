/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.BillingAdapterIdentifier;

public class RoleDefinitionIT extends DomainObjectTestBase {

    /**
     * Test role definition creation.
     * 
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {

        final String roleID = "roleID";

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(roleID);

                RoleDefinition roleDefinition = mgr.find(RoleDefinition.class,
                        key);

                Assert.assertEquals(key, roleDefinition.getKey());
                Assert.assertEquals(roleID, roleDefinition.getRoleId());

                return null;
            }
        });
    }

    /**
     * Test role definition update.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {

        final String roleID1 = "roleID1";
        final String roleID2 = "roleID2";

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(roleID1);

                RoleDefinition roleDefinition = mgr.find(RoleDefinition.class,
                        key);

                roleDefinition.setRoleId(roleID2);
                roleDefinition = mgr.find(RoleDefinition.class, key);

                Assert.assertEquals(key, roleDefinition.getKey());
                Assert.assertEquals(roleID2, roleDefinition.getRoleId());

                return null;
            }
        });
    }

    /**
     * Test role definition creation.
     * 
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {

        final String roleID = "roleID";

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(roleID);

                RoleDefinition roleDefinition = mgr.find(RoleDefinition.class,
                        key);

                mgr.remove(roleDefinition);

                roleDefinition = mgr.find(RoleDefinition.class, key);

                Assert.assertEquals(null, roleDefinition);

                return null;
            }
        });
    }

    /**
     * Create role definition.
     * 
     * @param roleID
     *            Role ID.
     * @return Key of just created role definition.
     * 
     * @throws Exception
     */
    private long doCreate(String roleID) throws Exception {

        TechnicalProduct technicalProduct = createTechnicalProduct();

        final long key = createRoleDefinition(technicalProduct, roleID);

        return key;
    }

    /**
     * Create role definition.
     * 
     * @param value
     * @return
     * @throws Exception
     */
    private TechnicalProduct createTechnicalProduct() throws Exception {
        TechnicalProduct technicalProduct = new TechnicalProduct();

        Organization organization = new Organization();
        organization.setOrganizationId("testOrg");
        organization.setRegistrationDate(123L);
        organization.setCutOffDay(1);
        mgr.persist(organization);
        mgr.flush();

        technicalProduct.setProvisioningURL("http://");
        technicalProduct.setProvisioningVersion("1.0");
        technicalProduct.setTechnicalProductId("technicalProductId");
        technicalProduct.setOrganizationKey(100);
        technicalProduct.setOrganization(organization);
        technicalProduct.setBillingIdentifier(
                BillingAdapterIdentifier.NATIVE_BILLING.toString());

        mgr.persist(technicalProduct);
        mgr.flush();

        return technicalProduct;
    }

    /**
     * Helper method for discount creating.
     * 
     * @param value
     * @return
     * @throws Exception
     */
    private long createRoleDefinition(TechnicalProduct technicalProduct,
            String roleID) throws Exception {

        ArrayList<PricedProductRole> pricedRoles = new ArrayList<PricedProductRole>();

        RoleDefinition roleDefinition = new RoleDefinition();

        roleDefinition.setTechnicalProduct(technicalProduct);
        roleDefinition.setRoleId(roleID);
        roleDefinition.setPricedRoles(pricedRoles);

        mgr.persist(roleDefinition);
        mgr.flush();

        return roleDefinition.getKey();
    }

}
