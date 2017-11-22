/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VODiscount;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Discounts;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

public class DiscountServiceBeanIT extends EJBTestBase {

    protected DataService mgr;
    protected DiscountService discountService;

    private Product product;
    private List<String> customerUserKeys = new ArrayList<>();
    List<Organization> customers;
    private String supplierUserKey;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new DiscountServiceBean());

        mgr = container.get(DataService.class);
        discountService = container.get(DiscountService.class);

        container.login("setup", ROLE_ORGANIZATION_ADMIN);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Create a supplier
                Organization supplier = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                PlatformUser supplierUser = Organizations.createUserForOrg(mgr,
                        supplier, false, "supplierUser");
                supplierUserKey = String.valueOf(supplierUser.getKey());

                // Create a marketplace
                Marketplace marketplace = Marketplaces.ensureMarketplace(
                        supplier, supplier.getOrganizationId(), mgr);

                // Create a technical service
                TechnicalProduct techProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplier, "techProdId",
                                false, ServiceAccessType.LOGIN);

                // Create a product in the defined marketplace
                product = Products.createProduct(supplier, techProd, true,
                        "productId", "priceModelId", marketplace, mgr);

                // Create two customers for the same supplier
                customers = new ArrayList<>();
                customers.add(Organizations.createCustomer(mgr, supplier));
                customers.add(Organizations.createCustomer(mgr, supplier));
                // Create a customer who has no association to a supplier
                customers.add(Organizations.createOrganization(mgr));

                // Define a discount for the first customer only
                final BigDecimal discountValue = new BigDecimal("2.00");
                Discounts.createDiscount(mgr,
                        customers.get(0).getSources().get(0), discountValue);

                // Create platform users for all the three customers
                PlatformUser firstUser = Organizations.createUserForOrg(mgr,
                        customers.get(0), false, "firstUser");
                customerUserKeys.add(String.valueOf(firstUser.getKey()));

                PlatformUser secondUser = Organizations.createUserForOrg(mgr,
                        customers.get(1), false, "secondUser");
                customerUserKeys.add(String.valueOf(secondUser.getKey()));

                PlatformUser thirdUser = Organizations.createUserForOrg(mgr,
                        customers.get(2), false, "thirdUse");

                customerUserKeys.add(String.valueOf(thirdUser.getKey()));

                mgr.refresh(supplier);
                return null;
            }
        });

        // Log-in as the first customer
        container.login(customerUserKeys.get(0));

    }

    @Test
    public void testGetDiscountForService() throws Exception {
        VODiscount voDiscount = discountService
                .getDiscountForService(product.getKey());
        Assert.assertEquals(voDiscount.getValue(), new BigDecimal("2.00"));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetCustomerDiscountServiceNotFound() throws Exception {
        long invalidProductKey = 0;
        discountService.getDiscountForService(invalidProductKey);
    }

    public void testGetDiscountNotDefined() throws Exception {
        // Log-in as the customer for whom no discount has
        // been specified by the supplier
        container.login(customerUserKeys.get(1));
        Assert.assertNull(
                discountService.getDiscountForService(product.getKey()));
    }

    @Test
    public void testGetDiscountAssociationToSupplierNotFound()
            throws Exception {
        // Log-in as the customer with no association to the supplier.
        container.login(customerUserKeys.get(2));
        Assert.assertNull(
                discountService.getDiscountForService(product.getKey()));
    }

    @Test
    public void testGetDiscountForCustomer() throws Exception {
        // Log-in as a supplier and fetch the discount of a customer.
        container.login(supplierUserKey);
        VODiscount voDiscount = discountService
                .getDiscountForCustomer(customers.get(0).getOrganizationId());
        Assert.assertEquals(voDiscount.getValue(), new BigDecimal("2.00"));
    }

    /**
     * This situation can happen for example if the user accesses the public
     * catalog without first logging-in.
     */
    @Test
    public void testGetDiscountNoLoggedInUser() throws Exception {
        container.logout();
        Assert.assertNull(
                discountService.getDiscountForService(product.getKey()));
    }

}
