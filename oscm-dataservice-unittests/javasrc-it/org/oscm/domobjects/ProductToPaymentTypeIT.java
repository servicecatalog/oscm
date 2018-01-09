/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: brandstetter                                                     
 *                                                                              
 *  Creation Date: 07.10.2011                                                      
 *                                                                              
 *  Completion Time: 07.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;

/**
 * @author brandstetter
 * 
 */
public class ProductToPaymentTypeIT extends DomainObjectTestBase {

    private Product prod;
    private ProductToPaymentType prodToPt;
    private PaymentType ptCreditCard;
    private PaymentType ptDirectDebit;

    @Override
    protected void dataSetup() throws Exception {

        // PaymentTypes are created in the DomainObjectTest::setup
        createOrganizationRoles(mgr);

        Organization techProv = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        Organization supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER);

        TechnicalProduct techProd = TechnicalProducts.createTechnicalProduct(
                mgr, techProv, "techProv_ID", false, ServiceAccessType.LOGIN);
        mgr.persist(techProd);

        prod = Products.createProductWithoutPriceModel(supplier, techProd,
                "product_ID");
        mgr.persist(prod);

        ptDirectDebit = findPaymentType(DIRECT_DEBIT, mgr);
        prodToPt = new ProductToPaymentType(prod, ptDirectDebit);
        mgr.persist(prodToPt);
    }

    @Test
    public void testAdd() throws Exception {
        try {
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
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testAdd_Duplicate() throws Exception {
        try {
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
                    doTestAdd();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testModify() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            final PaymentType newPt = runTX(new Callable<PaymentType>() {
                @Override
                public PaymentType call() throws Exception {
                    ProductToPaymentType ref = mgr.getReference(
                            ProductToPaymentType.class, prodToPt.getKey());
                    PaymentType pt = findPaymentType(INVOICE, mgr);
                    ref.setPaymentType(pt);
                    mgr.persist(ref);
                    return pt;
                }
            });

            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyCheck(newPt);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testDelete() throws Exception {
        try {
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
                    ProductToPaymentType ref = mgr.getReference(
                            ProductToPaymentType.class, prodToPt.getKey());
                    mgr.remove(ref);
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    protected void doTestDeleteCheck() throws ObjectNotFoundException {
        Assert.assertNull(mgr.find(ProductToPaymentType.class,
                prodToPt.getKey()));

        Product prodDb = mgr.getReference(Product.class, prod.getKey());

        List<ProductToPaymentType> ptList = prodDb.getPaymentTypes();
        Assert.assertNotNull(ptList);
        Assert.assertEquals(1, ptList.size());

    }

    protected void doTestAdd() throws Exception {
        prod = mgr.getReference(Product.class, prod.getKey());
        ptCreditCard = findPaymentType(CREDIT_CARD, mgr);

        prodToPt = new ProductToPaymentType(prod, ptCreditCard);
        mgr.persist(prodToPt);
    }

    protected void doTestAddCheck() throws Exception {
        ProductToPaymentType ref = mgr.getReference(ProductToPaymentType.class,
                prodToPt.getKey());
        Assert.assertEquals(prod.getKey(), ref.getProduct().getKey());
        Assert.assertEquals(ptCreditCard.getKey(), ref.getPaymentType()
                .getKey());
        Assert.assertEquals(0, ref.getVersion());

        Product prodDb = mgr.getReference(Product.class, prod.getKey());

        List<ProductToPaymentType> ptList = prodDb.getPaymentTypes();
        Assert.assertNotNull(ptList);
        Assert.assertEquals(2, ptList.size());
        Assert.assertTrue(ptList.contains(prodToPt));
    }

    protected void doTestModifyCheck(PaymentType newPt) throws Exception {
        ProductToPaymentType ref = mgr.getReference(ProductToPaymentType.class,
                prodToPt.getKey());
        Assert.assertEquals(newPt.getKey(), ref.getPaymentType().getKey());
        Assert.assertEquals(prod.getKey(), ref.getProduct().getKey());
        Assert.assertEquals(1, ref.getVersion());
        Assert.assertEquals("INVOICE", ref.getPaymentType().getPaymentTypeId());

        Product prodDb = mgr.getReference(Product.class, prod.getKey());

        List<ProductToPaymentType> ptList = prodDb.getPaymentTypes();
        Assert.assertNotNull(ptList);
        Assert.assertEquals(2, ptList.size());
        Assert.assertTrue(ptList.contains(prodToPt));
    }

}
