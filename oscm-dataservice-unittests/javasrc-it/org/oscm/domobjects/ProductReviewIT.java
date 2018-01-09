/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: May 16, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;

/**
 * Tests for the domain object representing an user review for a
 * <code>Product</code>.
 * 
 * @author barzu
 */
public class ProductReviewIT extends DomainObjectTestBase {

    private static Random random = new Random();

    private Organization supplier;
    private TechnicalProduct technicalProduct;
    private PlatformUser platformUser;
    private ProductReview review;

    /**
     * <b>Test case:</b> Add a new product review entry<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The product review entry can be retrieved from DB and is identical to
     * the provided object</li>
     * <li>A history object is created for the product review entry</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createProductReview();
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
            throw e.getCause();
        }
    }

    private void createProductReview() throws Exception {
        if (supplier == null) {
            supplier = Organizations.createOrganization(mgr,
                    OrganizationRoleType.TECHNOLOGY_PROVIDER,
                    OrganizationRoleType.SUPPLIER);
        }
        if (technicalProduct == null) {
            technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
                    supplier, "tec_pro", false, ServiceAccessType.DIRECT);
        }
        if (platformUser == null) {
            platformUser = PlatformUsers.createUser(mgr, "usr");
        }

        Product product = Products.createProduct(supplier, technicalProduct,
                true, "testProduct4Review_" + random.nextInt(), null, mgr);

        ProductFeedback feedback = new ProductFeedback();
        BigDecimal averageRating = BigDecimal.valueOf(2.08);
        feedback.setAverageRating(averageRating);
        feedback.setProduct(product);
        mgr.persist(feedback);

        review = new ProductReview();
        review.setRating(3);
        review.setTitle("Reviewed by buffalo soldier");
        review.setComment("Fine!");
        review.setModificationDate(new Date().getTime());
        review.setProductFeedback(feedback);
        review.setPlatformUser(platformUser);
        mgr.persist(review);

        mgr.flush();
    }

    private void doTestAddCheck() {
        ProductReview savedReview = loadProductReview(true);

        // check the values
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedReview, review),
                ReflectiveCompare.compare(savedReview, review));

        // check cascaded objects
        PlatformUser savedUser = savedReview.getPlatformUser();
        PlatformUser orgUser = review.getPlatformUser();
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedUser, orgUser),
                ReflectiveCompare.compare(savedUser, orgUser));

        // Load cascaded history objects and check
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(savedUser);
        Assert.assertNotNull("History entry 'null' for PlatformUser of ProductReview "
                + review.getKey());
        Assert.assertEquals(
                "Only one history entry expected for PlatformUser of ProductReview "
                        + review.getKey(), 1, histObjs.size());
    }

    private ProductReview loadProductReview(boolean mandatory) {
        assertNotNull("Cannot find the original ProductReview", review);

        // load the previously persisted ProductReview
        ProductReview savedReview = mgr.find(ProductReview.class,
                Long.valueOf(review.getKey()));
        if (mandatory) {
            assertNotNull("Cannot find ProductReview '" + review.getKey()
                    + "' in DB", savedReview);
        }
        return savedReview;
    }

    /**
     * <b>Testcase:</b> Modify an existing product review object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the product review</li>
     * <li>PlatformUser unchanged</li>
     * <li>No new history object for PlatformUser</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createProductReview();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModify();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModify() throws Exception {
        ProductReview savedReview = loadProductReview(true);
        savedReview.setRating(2);
        savedReview.setComment("Actually not so fine!");
        mgr.flush();
        review = savedReview;
        platformUser = review.getPlatformUser();
        load(platformUser);
    }

    private void doTestModifyCheck() throws Exception {
        ProductReview savedReview = loadProductReview(true);

        // check the values
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedReview, review),
                ReflectiveCompare.compare(savedReview, review));

        // Check cascaded objects
        PlatformUser savedUser = savedReview.getPlatformUser();
        PlatformUser orgUser = platformUser;
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedUser, orgUser),
                ReflectiveCompare.compare(savedUser, orgUser));

        // Load cascaded history objects and check
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(savedUser);
        Assert.assertNotNull("History entry 'null' for PlatformUser of ProductReview "
                + review.getKey());
        Assert.assertEquals(
                "Only one history entry expected for PlatformUser of ProductReview "
                        + review.getKey(), 1, histObjs.size());
    }

    /**
     * <b>Testcase:</b> Delete an existing ProductReview object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>ProductReview marked as deleted in the DB</li>
     * <li>History object created for the deleted ProductReview</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDelete() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createProductReview();
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
                    doTestDeleteCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDelete() throws Exception {
        ProductReview savedReview = loadProductReview(true);
        mgr.remove(savedReview);
        mgr.flush();
        review = savedReview;
        review.setPlatformUser(unproxyEntity(review.getPlatformUser()));
    }

    private void doTestDeleteCheck() throws Exception {
        ProductReview savedReview = loadProductReview(false);

        // check ProductReview deletion
        Assert.assertNull("Deleted ProductReview '" + review.getKey()
                + "' can still be accessed via DataManager.find", savedReview);

        // check that the PlatformUser was not deleted
        PlatformUser user = review.getPlatformUser();
        assertNotNull(
                "PlatformUser not found for ProductFeedback " + review.getKey(),
                user);
        PlatformUser savedUser = mgr.find(PlatformUser.class,
                Long.valueOf(user.getKey()));
        assertNotNull("Cannot find PlatformUser '" + user.getKey() + "' in DB",
                savedUser);

        // check that the PlatformUser does not have a new history entry
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(savedUser);
        Assert.assertNotNull(
                "History entry 'null' for PlatformUser " + user.getKey(),
                histObjs);
        Assert.assertEquals("Only one history entry expected for PlatformUser "
                + savedUser.getKey(), 1, histObjs.size());
    }

}
