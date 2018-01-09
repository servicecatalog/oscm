/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: weiser                                                   
 *                                                                              
 *  Creation Date: 13.12.2011                                                      
 *                                                                              
 *  Completion Time: 13.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.VOFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;
import org.oscm.intf.IdentityService;
import org.oscm.intf.MarketplaceService;
import org.oscm.intf.ReviewService;
import org.oscm.intf.SubscriptionService;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.DomainObjectException.ClassEnum;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.types.exceptions.ValidationException.ReasonEnum;
import org.oscm.vo.VOBillingContact;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOPaymentInfo;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceReview;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOUda;
import org.oscm.vo.VOUserDetails;

/**
 * Tests for {@link ReviewService} web service.
 * 
 * @author groch
 * 
 */
public class ReviewServiceWSTest {

    private static WebserviceTestSetup setup;
    private static VOFactory factory = new VOFactory();
    private static VOMarketplace mpLocal;
    private static VOOrganization supplier;
    private static VOService chargeableService;
    private static VOUserDetails customerUser;
    private static VOUserDetails customerUser2;
    private static VOPaymentInfo customerPaymentInfo;
    private static VOBillingContact customerBillingContact;
    private static SubscriptionService subSvc;
    private static ReviewService revSvcMpOwner;
    private static ReviewService revSvcCustomer;
    private static ReviewService revSvcCustomer2;
    private static IdentityService is;
    private static VOServiceReview firstReviewCustomer;

    @BeforeClass
    public static void setUpOnce() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        WebserviceTestBase.getOperator().addCurrency("EUR");

        setup = new WebserviceTestSetup();
        supplier = setup.createSupplier("Supplier");
        setup.createTechnicalService();

        is = ServiceFactory.getDefault()
                .getIdentityService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        MarketplaceService mpSrvOperator = ServiceFactory.getDefault()
                .getMarketPlaceService(
                        WebserviceTestBase.getPlatformOperatorKey(),
                        WebserviceTestBase.getPlatformOperatorPassword());

        mpLocal = mpSrvOperator.createMarketplace(factory.createMarketplaceVO(
                supplier.getOrganizationId(), false, "Local Marketplace"));

        chargeableService = setup.createAndActivateService("Service", mpLocal);

        revSvcMpOwner = ServiceFactory.getDefault()
                .getReviewService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        VOUserDetails mpOwner = is.getCurrentUserDetails();

        is.grantUserRoles(mpOwner,
                Arrays.asList(UserRoleType.MARKETPLACE_OWNER));

        // create customer

        setup.createCustomer("Customer1");
        customerUser = setup.getCustomerUser();
        customerPaymentInfo = setup.getCustomerPaymentInfo();
        customerBillingContact = setup.getCustomerBillingContact();

        // subscribe customer to chargeable service
        subSvc = ServiceFactory.getDefault().getSubscriptionService(
                String.valueOf(customerUser.getKey()),
                WebserviceTestBase.DEFAULT_PASSWORD);
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        subscription = subSvc.subscribeToService(subscription,
                chargeableService, null, customerPaymentInfo,
                customerBillingContact, new ArrayList<VOUda>());

        // get reviewService for customer (used to test all but initial create)
        revSvcCustomer = ServiceFactory.getDefault().getReviewService(
                String.valueOf(customerUser.getKey()),
                WebserviceTestBase.DEFAULT_PASSWORD);

        // create second customer
        setup.createCustomer("Customer2");
        customerUser2 = setup.getCustomerUser();
        VOPaymentInfo customerPaymentInfo2 = setup.getCustomerPaymentInfo();
        VOBillingContact customerBillingContact2 = setup
                .getCustomerBillingContact();

        // subscribe customer2 to same chargeable service
        SubscriptionService subSvc2 = ServiceFactory.getDefault()
                .getSubscriptionService(String.valueOf(customerUser2.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
        VOSubscription subscription2 = new VOSubscription();
        subscription2.setSubscriptionId(Long.toHexString(System
                .currentTimeMillis()));
        subscription2 = subSvc2.subscribeToService(subscription2,
                chargeableService, null, customerPaymentInfo2,
                customerBillingContact2, new ArrayList<VOUda>());

        // get reviewService for customer 2 (used to test initial create)
        revSvcCustomer2 = ServiceFactory.getDefault().getReviewService(
                String.valueOf(customerUser2.getKey()),
                WebserviceTestBase.DEFAULT_PASSWORD);
    }

    @Before
    public void setUp() throws Exception {
        firstReviewCustomer = revSvcCustomer.writeReview(createReview(
                chargeableService, "myTitle", "awesome", 3));
    }

    @After
    public void cleanUp() throws Exception {
        try {
            revSvcCustomer.deleteReview(firstReviewCustomer);
        } catch (ObjectNotFoundException e) {
            // ignore, object has been deleted before
        }
    }

    @Test(expected = SOAPFaultException.class)
    public void writeReview_create_nullArgument() throws Throwable {
        ReviewService revSvc = ServiceFactory.getDefault().getReviewService();
        try {
            revSvc.writeReview(null);
        } catch (SOAPFaultException e) {
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void writeReview_create_notSubscribed() throws Exception {
        VOService svc = setup.createAndActivateService("Service", mpLocal);
        VOServiceReview review = createReview(svc, "myTitle", "awesome", 3);
        try {
            revSvcCustomer2.writeReview(review);
        } catch (OperationNotPermittedException e) {
            validateException(e);
            throw e;
        }
    }

    @Test(expected = ValidationException.class)
    public void writeReview_create_subscribed_missingRequiredFields()
            throws Exception {
        VOServiceReview review = new VOServiceReview();
        review.setProductKey(chargeableService.getKey());

        try {
            revSvcCustomer2.writeReview(review);
        } catch (ValidationException e) {
            validateException(e, "title");
            throw e;
        }
    }

    @Test
    public void writeReview_create_goodCase() throws Exception {
        VOServiceReview rev = createReview(chargeableService, "myTitle",
                "awesome", 3);
        assertEquals(0, rev.getKey());
        try {
            rev = revSvcCustomer2.writeReview(rev);
            assertTrue(rev.getKey() != 0);
            assertEquals(0, rev.getVersion());
            assertEquals("myTitle", rev.getTitle());
            assertEquals("awesome", rev.getComment());
            assertEquals(3, rev.getRating());
        } finally {
            revSvcCustomer2.deleteReview(rev);
        }
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void writeReview_create_2ndReviewBySameUser() throws Exception {
        try {
            VOServiceReview review2 = createReview(chargeableService,
                    "anotherTitle", "fantastic", 5);
            revSvcCustomer.writeReview(review2);
        } catch (NonUniqueBusinessKeyException e) {
            validateException(ClassEnum.PRODUCT_REVIEW,
                    Arrays.asList(String.valueOf(customerUser.getKey())), e);
            throw e;
        }
    }

    @Test
    public void writeReview_update_goodCase() throws Exception {
        // now change existing rating and comment
        firstReviewCustomer.setComment("amazing, extremely helpful");
        firstReviewCustomer.setRating(5);
        assertEquals(0, firstReviewCustomer.getVersion());
        VOServiceReview rev = revSvcCustomer.writeReview(firstReviewCustomer);
        assertEquals(1, rev.getVersion());
        assertEquals("amazing, extremely helpful", rev.getComment());
        assertEquals(5, rev.getRating());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void writeReview_update_otherUser() throws Exception {
        try {
            // now change rating and comment
            firstReviewCustomer.setComment("amazing, extremely helpful");
            firstReviewCustomer.setRating(5);
            ReviewService revSvc = ServiceFactory.getDefault()
                    .getReviewService(
                            WebserviceTestBase.getPlatformOperatorKey(),
                            WebserviceTestBase.getPlatformOperatorPassword());
            firstReviewCustomer = revSvc.writeReview(firstReviewCustomer);
        } catch (OperationNotPermittedException e) {
            validateException(e);
            throw e;
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void writeReview_update_concurrentModification() throws Exception {
        firstReviewCustomer.setRating(4);
        revSvcCustomer.writeReview(firstReviewCustomer);
        firstReviewCustomer.setRating(5);
        try {
            // now change rating and comment
            // update the review without updating the VO
            revSvcCustomer.writeReview(firstReviewCustomer);
        } catch (ConcurrentModificationException e) {
            validateException(e);
            throw e;
        }
    }

    @Test(expected = SOAPFaultException.class)
    public void deleteReview_nullArgument() throws Throwable {
        try {
            revSvcCustomer.deleteReview(null);
        } catch (SOAPFaultException e) {
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteReview_subscribed_missingRequiredField() throws Exception {
        VOServiceReview reviewToDelete = new VOServiceReview();
        try {
            revSvcCustomer.deleteReview(reviewToDelete);
        } catch (ObjectNotFoundException e) {
            validateException("0", e);
            throw e;
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteReview_notExisting() throws Exception {
        VOServiceReview reviewToDelete = new VOServiceReview();
        reviewToDelete.setKey(1000);
        try {
            revSvcCustomer.deleteReview(reviewToDelete);
        } catch (ObjectNotFoundException e) {
            validateException("1000", e);
            throw e;
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteReview_notReviewOwner() throws Exception {
        VOServiceReview reviewToDelete = new VOServiceReview();
        reviewToDelete.setKey(firstReviewCustomer.getKey());
        try {
            revSvcCustomer2.deleteReview(reviewToDelete);
        } catch (OperationNotPermittedException e) {
            validateException(e);
            throw e;
        }
    }

    @Test
    public void deleteReview_goodCase() throws Exception {
        VOServiceReview reviewTodelete = new VOServiceReview();
        reviewTodelete.setKey(firstReviewCustomer.getKey());
        assertEquals(
                1,
                revSvcCustomer
                        .getServiceFeedback(firstReviewCustomer.getProductKey())
                        .getReviews().size());
        revSvcCustomer.deleteReview(reviewTodelete);
        assertEquals(
                0,
                revSvcCustomer
                        .getServiceFeedback(firstReviewCustomer.getProductKey())
                        .getReviews().size());
    }

    @Test(expected = SOAPFaultException.class)
    public void deleteReviewByMarketplaceOwner_nullReview() throws Throwable {
        revSvcMpOwner.deleteReviewByMarketplaceOwner(null, new String());
    }

    @Test(expected = SOAPFaultException.class)
    public void deleteReviewByMarketplaceOwner_nullReason() throws Throwable {
        revSvcMpOwner.deleteReviewByMarketplaceOwner(new VOServiceReview(),
                null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteReviewByMarketplaceOwner_missingRequiredField()
            throws Exception {
        VOServiceReview reviewToDelete = new VOServiceReview();
        try {
            revSvcMpOwner
                    .deleteReviewByMarketplaceOwner(reviewToDelete, "test");
        } catch (ObjectNotFoundException e) {
            validateException("0", e);
            throw e;
        }
    }

    @Test(expected = SOAPFaultException.class)
    public void deleteReviewByMarketplaceOwner_notMarketplaceOwner()
            throws Exception {
        VOServiceReview reviewToDelete = new VOServiceReview();
        reviewToDelete.setKey(firstReviewCustomer.getKey());
        revSvcCustomer.deleteReviewByMarketplaceOwner(reviewToDelete, "test");
    }

    @Test
    public void deleteReviewByMarketplaceOwner_goodCase() throws Exception {
        VOServiceReview reviewTodelete = new VOServiceReview();
        reviewTodelete.setKey(firstReviewCustomer.getKey());
        assertEquals(
                1,
                revSvcCustomer
                        .getServiceFeedback(firstReviewCustomer.getProductKey())
                        .getReviews().size());
        revSvcMpOwner.deleteReviewByMarketplaceOwner(reviewTodelete, "test");
        assertEquals(
                0,
                revSvcCustomer
                        .getServiceFeedback(firstReviewCustomer.getProductKey())
                        .getReviews().size());
    }

    private static VOServiceReview createReview(VOService svc, String title,
            String comment, int rating) {
        VOServiceReview review = new VOServiceReview();
        review.setProductKey(svc.getKey());
        review.setTitle(title);
        review.setComment(comment);
        review.setRating(rating);
        return review;
    }

    protected static void validateException(OperationNotPermittedException e) {
        assertEquals("ex.OperationNotPermittedException", e.getMessageKey());
    }

    protected static void validateException(ConcurrentModificationException e) {
        assertEquals("ex.ConcurrentModificationException", e.getMessageKey());
    }

    protected void validateException(String param, ObjectNotFoundException e) {
        assertEquals(ClassEnum.PRODUCT_REVIEW, e.getDomainObjectClassEnum());
        String[] params = e.getFaultInfo().getMessageParams();
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(param, params[0]);
        assertEquals("ex.ObjectNotFoundException.PRODUCT_REVIEW",
                e.getMessageKey());
    }

    protected static void validateException(ValidationException e, String member) {
        assertEquals(ReasonEnum.REQUIRED, e.getFaultInfo().getReason());
        assertEquals(member, e.getFaultInfo().getMember());
        String[] params = e.getFaultInfo().getMessageParams();
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(member, params[0]);
    }

    protected static void validateException(ClassEnum type,
            List<String> objIds, NonUniqueBusinessKeyException e) {
        assertEquals(type, e.getDomainObjectClassEnum());
        String[] params = e.getFaultInfo().getMessageParams();
        assertNotNull(params);
        assertEquals(1, params.length);
        for (String objId : objIds) {
            assertTrue(params[0].contains(objId));
        }
        assertEquals("ex.NonUniqueBusinessKeyException." + type.name(),
                e.getMessageKey());
    }

}
