/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: May 16, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.*;

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
 * Tests for the domain object representing the sum of all user reviews for a
 * <code>Product</code>.
 * 
 * @author barzu
 */
public class ProductFeedbackIT extends DomainObjectTestBase {

    private static Random random = new Random();

    private Organization supplier;
    private TechnicalProduct technicalProduct;
    private PlatformUser platformUser;
    private ProductReview review;
    private ProductFeedback feedback;
    private Product product;

    /**
     * <b>Test case:</b> Add a new product feedback entry<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The product feedback entry can be retrieved from DB and is identical
     * to the provided object</li>
     * <li>The back reference to the collection of product reviews correctly
     * created</li>
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
                    createProductFeedback();
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

    private void createProductFeedback() throws Exception {
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

        feedback = new ProductFeedback();
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
        ProductFeedback savedFeedback = loadProductFeedback(true);

        // check the values
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedFeedback, feedback),
                ReflectiveCompare.compare(savedFeedback, feedback));

        // Check cascaded objects
        Product savedProduct = savedFeedback.getProduct();
        Product orgProduct = feedback.getProduct();
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedProduct, orgProduct),
                ReflectiveCompare.compare(savedProduct, orgProduct));

        List<ProductReview> savedReviews = savedFeedback.getProductReviews();
        Assert.assertEquals(
                "Only one ProductReview expected for ProductFeedback "
                        + feedback.getKey(), 1, savedReviews.size());
        // check back reference feedback.getProductReviews()
        ProductReview savedReview = savedReviews.get(0);
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedReview, review),
                ReflectiveCompare.compare(savedReview, review));
        Assert.assertTrue(ReflectiveCompare.showDiffs(
                savedReview.getProductFeedback(), feedback), ReflectiveCompare
                .compare(savedReview.getProductFeedback(), feedback));

        // Load cascaded history objects and check
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(savedProduct);
        Assert.assertNotNull("History entry 'null' for Product of ProductFeedback "
                + feedback.getKey());
        // no new entry for Product in history when creating ProductFeedback
        Assert.assertEquals(
                "Only one history entry expected for Product of ProductFeedback "
                        + feedback.getKey(), 1, histObjs.size());
    }

    private ProductFeedback loadProductFeedback(boolean mandatory) {
        assertNotNull("Cannot find the original ProductReview", feedback);

        // load the previously persisted ProductFeedback
        ProductFeedback savedFeedback = mgr.find(ProductFeedback.class,
                Long.valueOf(feedback.getKey()));
        if (mandatory) {
            assertNotNull("Cannot find ProductFeedback '" + feedback.getKey()
                    + "' in DB", savedFeedback);
        }
        return savedFeedback;
    }

    /**
     * <b>Testcase:</b> Modify an existing product feedback object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>Product unchanged</li>
     * <li>No new history object for Product</li>
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
                    createProductFeedback();
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
        ProductFeedback savedFeedback = loadProductFeedback(true);
        savedFeedback.setAverageRating(BigDecimal.valueOf(1.15));
        mgr.flush();
        feedback = savedFeedback;
        product = feedback.getProduct();
        load(product);
    }

    private void doTestModifyCheck() throws Exception {
        ProductFeedback savedFeedback = loadProductFeedback(true);

        // check the values
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedFeedback, feedback),
                ReflectiveCompare.compare(savedFeedback, feedback));

        // Check cascaded objects
        // Product should not be changed
        Product savedProduct = savedFeedback.getProduct();
        Product orgProduct = product;
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedProduct, orgProduct),
                ReflectiveCompare.compare(savedProduct, orgProduct));

        // Load cascaded history objects and check
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(savedProduct);
        Assert.assertNotNull("History entry 'null' for Product of ProductFeedback "
                + feedback.getKey());
        // Product should not have a new history entry when modifying
        // ProductFeedback
        Assert.assertEquals(
                "Only one history entry expected for Product of ProductFeedback "
                        + feedback.getKey(), 1, histObjs.size());
    }

    /**
     * <b>Testcase:</b> Delete an existing ProductFeedback object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>ProductFeedback marked as deleted in the DB</li>
     * <li>All corresponding ProductReviews marked as deleted</li>
     * <li>History objects created for all corresponding ProductReviews</li>
     * <li>Product was not deleted</li>
     * <li>No new history object created for Product</li>
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
                    createProductFeedback();
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
        ProductFeedback savedFeedback = loadProductFeedback(true);
        mgr.remove(savedFeedback);
        mgr.flush();
        feedback = savedFeedback;
        feedback.setProduct(unproxyEntity(feedback.getProduct()));
    }

    private void doTestDeleteCheck() throws Exception {
        ProductFeedback savedFeedback = loadProductFeedback(false);

        // check ProductFeedback deletion
        Assert.assertNull("Deleted ProductFeedback '" + feedback.getKey()
                + "' can still be accessed via DataManager.find", savedFeedback);

        // check deletion of corresponding ProductReviews
        List<ProductReview> savedReviews = feedback.getProductReviews();
        Assert.assertEquals(
                "Only one ProductReview expected for ProductFeedback "
                        + feedback.getKey(), 1, savedReviews.size());
        ProductReview review = savedReviews.get(0);
        assertNotNull("Cannot find the original ProductReview", review);
        ProductReview savedReview = mgr.find(ProductReview.class,
                Long.valueOf(review.getKey()));
        Assert.assertNull("Deleted ProductReview '" + feedback.getKey()
                + "' can still be accessed via DataManager.find", savedReview);

        // check that the Product was not deleted
        Product product = feedback.getProduct();
        assertNotNull(
                "Product not found for ProductFeedback " + feedback.getKey(),
                product);
        Product savedProduct = mgr.find(Product.class,
                Long.valueOf(product.getKey()));
        assertNotNull("Cannot find Product '" + product.getKey() + "' in DB",
                savedProduct);

        // check that the Product does not have a new history entry
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(savedProduct);
        Assert.assertNotNull(
                "History entry 'null' for Product " + product.getKey(),
                histObjs);
        Assert.assertEquals("Only one history entry expected for Product "
                + savedProduct.getKey(), 1, histObjs.size());
    }

    /**
     * Call update average rating without any reviews.
     */
    @Test
    public void updateAverageRating_empty() {
        ProductFeedback feedback = new ProductFeedback();
        feedback.updateAverageRating();
        assertEquals(new BigDecimal(0), feedback.getAverageRating());
    }

    @Test
    public void updateAverageRating_one() {
        ProductFeedback feedback = new ProductFeedback();
        createReview(feedback, 2);
        feedback.updateAverageRating();
        assertEquals(new BigDecimal("2.00"), feedback.getAverageRating());
    }

    @Test
    public void updateAverageRating_two() {
        ProductFeedback feedback = new ProductFeedback();
        createReview(feedback, 2);
        createReview(feedback, 1);
        feedback.updateAverageRating();
        assertEquals(new BigDecimal("1.50"), feedback.getAverageRating());
    }

    /**
     * Given three ratings. The value (divided by three) must be rounded to two
     * digests.
     */
    @Test
    public void updateAverageRating_round() {
        ProductFeedback feedback = new ProductFeedback();
        createReview(feedback, 2);
        createReview(feedback, 1);
        createReview(feedback, 1);
        feedback.updateAverageRating();
        assertEquals(new BigDecimal("1.33"), feedback.getAverageRating());
    }

    @Test
    public void updateAverageRating_zero() {
        ProductFeedback feedback = new ProductFeedback();
        createReview(feedback, 0);
        feedback.updateAverageRating();
        assertEquals(new BigDecimal("0.00"), feedback.getAverageRating());
    }

    private void createReview(ProductFeedback feedback, int rating) {
        ProductReview review = new ProductReview();
        review.setRating(rating);
        feedback.getProductReviews().add(review);
    }

    /**
     * Check if user has created review
     */
    @Test
    public void hasReview() {

        // given a feedback with one review
        ProductFeedback feedback = new ProductFeedback();
        PlatformUser user1 = new PlatformUser();
        createReview(feedback, user1);

        // when checking if user has a review
        boolean hasReviewed = feedback.hasReview(user1);

        // then result must be true
        assertTrue(hasReviewed);
    }

    /**
     * Check if user has created a review on an empty feedback object.
     */
    @Test
    public void hasReview_empty() {
        ProductFeedback feedback = new ProductFeedback();
        PlatformUser user1 = new PlatformUser();
        assertFalse(feedback.hasReview(user1));
    }

    private void createReview(ProductFeedback feedback, PlatformUser user) {
        ProductReview review = new ProductReview();
        review.setPlatformUser(user);
        feedback.getProductReviews().add(review);
    }

    /**
     * Create a review with a different user and check if user has created it
     */
    @Test
    public void hasReview_differentUser() {
        ProductFeedback feedback = new ProductFeedback();
        PlatformUser user1 = new PlatformUser();
        PlatformUser user2 = new PlatformUser();
        createReview(feedback, user1);
        assertFalse(feedback.hasReview(user2));
    }
}
