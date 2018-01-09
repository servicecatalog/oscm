/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;

public class PricedProductRoleIT extends DomainObjectTestBase {

    /**
     * Test role definition creation.
     * 
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {

        final BigDecimal pricePerUser = BigDecimal.valueOf(40L);
        final String roleID = "roleID";

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(roleID, pricePerUser);

                PricedProductRole pricedRole = mgr.find(PricedProductRole.class,
                        key);

                Assert.assertEquals(key, pricedRole.getKey());
                Assert.assertEquals(pricePerUser, pricedRole.getPricePerUser());

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

        final String roleID = "roleID1";
        final BigDecimal price1 = BigDecimal.valueOf(40l);
        final BigDecimal price2 = BigDecimal.valueOf(60l);

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(roleID, price1);

                PricedProductRole pricedRole = mgr.find(PricedProductRole.class,
                        key);

                pricedRole.setPricePerUser(price2);
                pricedRole = mgr.find(PricedProductRole.class, key);

                Assert.assertEquals(key, pricedRole.getKey());
                Assert.assertEquals(price2, pricedRole.getPricePerUser());

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
    public void testDelete() throws Exception {

        final String roleID = "roleID1";
        final BigDecimal price1 = BigDecimal.valueOf(40l);

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                final long key = doCreate(roleID, price1);

                PricedProductRole pricedRole = mgr.find(PricedProductRole.class,
                        key);

                mgr.remove(pricedRole);

                pricedRole = mgr.find(PricedProductRole.class, key);

                Assert.assertEquals(null, pricedRole);

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
    private long doCreate(String roleID, BigDecimal pricePerUser)
            throws Exception {

        Organization organization = createOrganization();

        TechnicalProduct technicalProduct = createTechnicalProduct(
                organization);

        Product product = createProduct(organization, technicalProduct);

        PriceModel priceModel = createPriceModel(product);

        RoleDefinition roleDefinition = createRoleDefinition(roleID,
                technicalProduct);

        final long key = createPricedRole(priceModel, roleDefinition,
                pricePerUser);

        return key;
    }

    /**
     * Helper method for product creation.
     * 
     * @return
     * @throws Exception
     */
    private Product createProduct(Organization organization,
            TechnicalProduct technicalProduct) throws Exception {

        PriceModel priceModel = new PriceModel();
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);

        Product product = new Product();
        product.setVendor(organization);
        product.setProductId("productID");
        product.setTechnicalProduct(technicalProduct);
        product.setProvisioningDate(TIMESTAMP);
        product.setStatus(ServiceStatus.ACTIVE);
        product.setType(ServiceType.TEMPLATE);
        product.setPriceModel(priceModel);
        ParameterSet paramSet = new ParameterSet();
        product.setParameterSet(paramSet);
        mgr.persist(product);
        mgr.flush();

        return product;
    }

    /**
     * Helper method for creating technical product.
     * 
     * @return
     * @throws Exception
     */
    private Organization createOrganization() throws Exception {
        Organization organization = new Organization();

        organization.setOrganizationId("testOrg");
        organization.setRegistrationDate(123L);
        organization.setCutOffDay(1);
        mgr.persist(organization);
        mgr.flush();

        return organization;
    }

    /**
     * Helper method for creating technical product.
     * 
     * @return
     * @throws Exception
     */
    private TechnicalProduct createTechnicalProduct(Organization organization)
            throws Exception {
        TechnicalProduct technicalProduct = new TechnicalProduct();

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
     * Helper method for creating role definition.
     * 
     * @return
     * @throws Exception
     */
    private RoleDefinition createRoleDefinition(String roleID,
            TechnicalProduct technicalProduct) throws Exception {
        RoleDefinition roleDefinition = new RoleDefinition();

        roleDefinition.setTechnicalProduct(technicalProduct);
        roleDefinition.setRoleId(roleID);

        mgr.persist(roleDefinition);
        mgr.flush();

        return roleDefinition;
    }

    /**
     * Helper method for creating price model.
     * 
     * @return
     */
    private PriceModel createPriceModel(Product product) throws Exception {

        PriceModel priceModel = new PriceModel();

        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        priceModel.setProduct(product);

        mgr.persist(priceModel);
        mgr.flush();

        return priceModel;
    }

    /**
     * Helper method for discount creating.
     * 
     * @param value
     * @return
     * @throws Exception
     */
    private long createPricedRole(PriceModel priceModel,
            RoleDefinition roleDefinition, BigDecimal pricePerUser)
                    throws Exception {

        PricedProductRole pricedRole = new PricedProductRole();

        pricedRole.setPriceModel(priceModel);
        pricedRole.setRoleDefinition(roleDefinition);
        pricedRole.setPricePerUser(pricePerUser);

        mgr.persist(pricedRole);
        mgr.flush();

        return pricedRole.getKey();
    }

    @Test
    public void testCopyForPMDependency() {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("roleID");
        PriceModel pm = new PriceModel();
        pm.setKey(1);
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPricePerUser(new BigDecimal(1L));
        ppr.setPriceModel(pm);
        ppr.setRoleDefinition(rd);

        PriceModel pmCopy = new PriceModel();
        PricedProductRole copy = ppr.copy(pmCopy);

        Assert.assertEquals(ppr.getPricePerUser(), copy.getPricePerUser());
        Assert.assertEquals(ppr.getRoleDefinition(), copy.getRoleDefinition());
        Assert.assertEquals(pmCopy.getKey(), copy.getPriceModel().getKey());
        Assert.assertNull(copy.getPricedOption());
        Assert.assertNull(copy.getPricedParameter());
    }

    @Test
    public void testCopyForPricedParameterDependency() {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("roleID");
        PricedParameter pm = new PricedParameter();
        pm.setKey(1);
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPricePerUser(new BigDecimal(1L));
        ppr.setPricedParameter(pm);
        ppr.setRoleDefinition(rd);

        PricedParameter pmCopy = new PricedParameter();
        PricedProductRole copy = ppr.copy(pmCopy);

        Assert.assertEquals(ppr.getPricePerUser(), copy.getPricePerUser());
        Assert.assertEquals(ppr.getRoleDefinition(), copy.getRoleDefinition());
        Assert.assertEquals(pmCopy.getKey(),
                copy.getPricedParameter().getKey());
        Assert.assertNull(copy.getPriceModel());
        Assert.assertNull(copy.getPricedOption());
    }

    @Test
    public void testCopyForPricedOptionDependency() {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("roleID");
        PricedOption pm = new PricedOption();
        pm.setKey(1);
        PricedProductRole ppr = new PricedProductRole();
        ppr.setPricePerUser(new BigDecimal(1L));
        ppr.setPricedOption(pm);
        ppr.setRoleDefinition(rd);

        PricedOption pmCopy = new PricedOption();
        PricedProductRole copy = ppr.copy(pmCopy);

        Assert.assertEquals(ppr.getPricePerUser(), copy.getPricePerUser());
        Assert.assertEquals(ppr.getRoleDefinition(), copy.getRoleDefinition());
        Assert.assertEquals(pmCopy.getKey(), copy.getPricedOption().getKey());
        Assert.assertNull(copy.getPriceModel());
        Assert.assertNull(copy.getPricedParameter());
    }
}
