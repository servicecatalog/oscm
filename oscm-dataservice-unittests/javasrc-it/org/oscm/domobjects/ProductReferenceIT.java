/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 10.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.oscm.test.Numbers.TIMESTAMP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;

/**
 * Test of the product reference object.
 */
public class ProductReferenceIT extends DomainObjectTestBase {

    private Product prod1;
    private Product prod2;
    private String organizationId;

    private final List<ProductReference> references = new ArrayList<ProductReference>();

    @Override
    protected void dataSetup() throws Exception {
        Organization organization = Organizations.createOrganization(mgr);
        organizationId = organization.getOrganizationId();
        // create technical product
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TP_ID", false, ServiceAccessType.LOGIN);

        // create two products for it
        prod1 = new Product();
        prod1.setVendor(organization);
        prod1.setProductId("Product1");
        prod1.setTechnicalProduct(tProd);
        prod1.setProvisioningDate(TIMESTAMP);
        prod1.setStatus(ServiceStatus.ACTIVE);
        prod1.setType(ServiceType.TEMPLATE);
        PriceModel pi = new PriceModel();
        prod1.setPriceModel(pi);
        ParameterSet emptyPS1 = new ParameterSet();
        prod1.setParameterSet(emptyPS1);
        mgr.persist(prod1);

        prod2 = new Product();
        prod2.setVendor(organization);
        prod2.setProductId("Product2");
        prod2.setTechnicalProduct(tProd);
        prod2.setProvisioningDate(TIMESTAMP);
        prod2.setStatus(ServiceStatus.ACTIVE);
        prod2.setType(ServiceType.TEMPLATE);
        pi = new PriceModel();
        prod2.setPriceModel(pi);
        ParameterSet emptyPS2 = new ParameterSet();
        prod2.setParameterSet(emptyPS2);
        mgr.persist(prod2);
    }

    @Test
    public void testCreation() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestCheckCreation();
                return null;
            }
        });
    }

    @Test
    public void testDeletion() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestDelete();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doCheckDeletion();
                return null;
            }
        });
    }

    @Test
    public void testCreateWrongParameters() throws Exception {
        Product prod1 = new Product();
        Product prod2 = new Product();
        TechnicalProduct tProd1 = new TechnicalProduct();
        TechnicalProduct tProd2 = new TechnicalProduct();
        tProd1.setKey(1);
        tProd2.setKey(2);
        prod1.setTechnicalProduct(tProd1);
        prod2.setTechnicalProduct(tProd2);

        try {
            new ProductReference(prod1, prod2);
            Assert.fail("Products do not refer to same technical product, so creation must fail");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test
    public void testRetrieveFromProduct() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doRetrieveFromProduct();
                return null;
            }
        });
    }

    private void doTestAdd() throws Exception {
        ProductReference prodRef = new ProductReference(prod1, prod2);
        mgr.persist(prodRef);
        references.add(prodRef);
    }

    private void doTestCheckCreation() throws Exception {
        for (ProductReference ref : references) {
            DomainObject<?> savedRef = mgr.getReference(ProductReference.class,
                    ref.getKey());

            // now compare the objects themselves
            Assert.assertTrue(ReflectiveCompare.showDiffs(ref, savedRef),
                    ReflectiveCompare.compare(ref, savedRef));
        }
    }

    private void doTestDelete() {
        for (ProductReference ref : references) {
            DomainObject<?> savedRef = mgr.find(ProductReference.class,
                    ref.getKey());
            mgr.remove(savedRef);
        }
    }

    private void doCheckDeletion() throws Exception {
        for (ProductReference ref : references) {
            DomainObject<?> savedRef = mgr.find(ProductReference.class,
                    ref.getKey());
            Assert.assertNull("Object was deleted and must not be found",
                    savedRef);

        }
    }

    private void doRetrieveFromProduct() {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) mgr.find(organization);
        Product product = new Product();
        product.setVendor(organization);
        product.setProductId(prod1.getProductId());
        product = (Product) mgr.find(product);
        List<Product> compatibleProducts = product.getCompatibleProductsList();
        Assert.assertEquals("Wrong number of compatible products stored", 1,
                compatibleProducts.size());
        Product compatibleProduct = compatibleProducts.get(0);
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(prod2, compatibleProduct),
                ReflectiveCompare.compare(prod2, compatibleProduct));
    }
}
