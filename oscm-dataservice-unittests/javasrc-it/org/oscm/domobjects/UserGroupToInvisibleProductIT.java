/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-6-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;

/**
 * Tests for the domain object relationship user group to product
 * 
 * @author Fang
 * 
 */
public class UserGroupToInvisibleProductIT extends DomainObjectTestBase {

    /**
     * <b>Test case:</b> Add user group to product<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The dependent object can be retrieved from DB via userGroup and via
     * product</li>
     * <li>Cascading delete works for deletion of userGroup</li>
     * <li>Cascading delete works for deletion of product</li>
     * </ul>
     * 
     * @throws Exception
     */

    private Organization org;
    private UserGroup userGroup;
    private Product p;
    private Product oldproduct;
    private UserGroupToInvisibleProduct oldUserGroupToInvisibleProduct;

    @Before
    public void setupData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setupUserGroup();
                setupProduct();
                return null;
            }
        });
    }

    @Test
    public void testAdd() throws Exception {
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
    }

    private void setupUserGroup() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        org = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        assertNotNull("organization expected", org);

        userGroup = new UserGroup();
        userGroup.setName("group1");
        userGroup.setDescription("group1 description");
        userGroup.setReferenceId("group1 reference Id");
        userGroup.setIsDefault(true);
        userGroup.setOrganization(org);

        mgr.persist(userGroup);
    }

    private void setupProduct() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        p = Products.createProduct("organizationId", "productId",
                "techProductId", mgr);
        assertNotNull("product expected", p);

        mgr.persist(p);
    }

    private void doTestAdd() throws Exception {
        userGroup = mgr.find(UserGroup.class, userGroup.getKey());
        p = mgr.find(Product.class, p.getKey());

        UserGroupToInvisibleProduct uip = new UserGroupToInvisibleProduct();
        uip.setUserGroup(userGroup);
        uip.setProduct(p);
        uip.setForallusers(true);
        mgr.persist(uip);
    }

    private void doTestAddCheck() {
        Product product = mgr.find(Product.class, p.getKey());
        assertNotNull("Product expected", product);

        assertNotNull(product.getUserGroupToInvisibleProducts());
        List<UserGroupToInvisibleProduct> uguList = product
                .getUserGroupToInvisibleProducts();
        Assert.assertEquals(1, uguList.size());

        UserGroupToInvisibleProduct ugu = uguList.get(0);
        Assert.assertEquals(userGroup, ugu.getUserGroup());
        Assert.assertEquals(p, ugu.getProduct());
    }

    @Test
    public void testDeleteByProduct() throws Throwable {
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
                    doTestDeleteByProduct();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheckByProduct();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteByProduct() {
        oldproduct = mgr.find(Product.class, p.getKey());
        assertNotNull("Old Product expected", oldproduct);

        List<UserGroupToInvisibleProduct> list = oldproduct
                .getUserGroupToInvisibleProducts();
        assertNotNull(list);
        Assert.assertEquals(1, list.size());

        oldUserGroupToInvisibleProduct = mgr.find(
                UserGroupToInvisibleProduct.class, list.get(0).getKey());
        assertNotNull(oldUserGroupToInvisibleProduct);

        mgr.remove(oldproduct);
    }

    private void doTestDeleteCheckByProduct() {
        // Product must be deleted
        Product pu = mgr.find(Product.class, oldproduct.getKey());
        Assert.assertNull("Product still available", pu);

        // UserGroupToInvisibleProduct must be deleted
        UserGroupToInvisibleProduct userGroupToInvisibleProduct = mgr.find(
                UserGroupToInvisibleProduct.class,
                oldUserGroupToInvisibleProduct.getKey());
        Assert.assertNull("UserGroupToInvisibleProduct still available",
                userGroupToInvisibleProduct);
    }

    @Test
    public void testDeleteByUserGroup() throws Throwable {
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
                    doTestDeleteByUserGroup();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteCheckByUserGroup();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteByUserGroup() {
        userGroup = mgr.find(UserGroup.class, userGroup.getKey());
        assertNotNull("UserGroup expected", userGroup);

        List<UserGroupToInvisibleProduct> list = userGroup
                .getUserGroupToInvisibleProducts();
        assertNotNull(list);
        Assert.assertEquals(1, list.size());

        oldUserGroupToInvisibleProduct = mgr.find(
                UserGroupToInvisibleProduct.class, list.get(0).getKey());
        assertNotNull(oldUserGroupToInvisibleProduct);

        mgr.remove(userGroup);
    }

    private void doTestDeleteCheckByUserGroup() {
        // UserGroup must be deleted
        UserGroup ug = mgr.find(UserGroup.class, userGroup.getKey());
        Assert.assertNull("UserGroup still available", ug);

        // UserGroupToInvisibleProduct must be deleted
        UserGroupToInvisibleProduct userGroupToInvisibleProduct = mgr.find(
                UserGroupToInvisibleProduct.class,
                oldUserGroupToInvisibleProduct.getKey());
        Assert.assertNull("UserGroupToInvisibleProduct still available",
                userGroupToInvisibleProduct);
    }
}
