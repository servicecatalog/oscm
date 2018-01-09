/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                
 *                                                                              
 *  Creation Date: Oct 10, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 10, 2011                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.operationslog.SubscriptionQuery;
import org.oscm.operationslog.UserOperationLogQuery;

/**
 * @author barzu
 */
@SuppressWarnings("boxing")
public class SubscriptionHistoryIT extends UserOperationLogQueryTestBase {

    @Override
    protected UserOperationLogQuery getQuery() {
        return new SubscriptionQuery();
    }

    @Test
    public void testLog() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(true, true);
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertTrue(result.get(0) instanceof Object[]);
                Object[] row = (Object[]) result.get(0);
                assertEquals("Wrong number of fields in the query result",
                        new SubscriptionQuery().getFieldNames().length,
                        row.length);

                assertTrue(row[0] instanceof Date);
                assertEquals(ModificationType.ADD.name(), row[1]);
                assertEquals(user.getUserId(), row[2]);
                assertEquals(BigInteger.valueOf(subscription.getVersion()),
                        row[3]);
                assertEquals(subscription.getSubscriptionId(), row[4]);
                assertEquals(supplier.getName(), row[5]);
                assertEquals(supplier.getOrganizationId(), row[6]);
                assertEquals(product.getProductId(), row[7]);
                BigInteger activationDate = subscription.getActivationDate() == null ? null
                        : BigInteger.valueOf(subscription.getActivationDate()
                                .longValue());
                assertEquals(activationDate, row[8]);
                assertEquals(subscription.getStatus().name(), row[9]);
                BigInteger deactivationDate = subscription
                        .getDeactivationDate() == null ? null
                        : BigInteger.valueOf(subscription.getDeactivationDate()
                                .longValue());
                assertEquals(deactivationDate, row[10]);
                assertEquals(GLOBAL_MARKETPLACE_NAME, row[11]);
                assertEquals(PaymentType.INVOICE, row[12]);
                assertEquals(BILLING_CONTACT_ID, row[13]);

                assertEquals(subscription.getPurchaseOrderNumber(), row[14]);
                assertEquals(subscription.getAccessInfo(), row[15]);
                assertEquals(subscription.getBaseURL(), row[16]);
                assertEquals(subscription.getLoginPath(), row[17]);
                boolean isTimeoutMailSent = row[17] == null ? false
                        : ((Boolean) row[18]).booleanValue();
                assertEquals(subscription.isTimeoutMailSent(),
                        isTimeoutMailSent);
            }
        }.run();
    }

    @Test
    public void testLog_MissingNullableFields() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                // Marketplace
                assertEquals(null, ((Object[]) result.get(0))[11]);
                // PaymentInfo
                assertEquals(null, ((Object[]) result.get(0))[12]);
                // BillingContact
                assertEquals(null, ((Object[]) result.get(0))[13]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateOrganization() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                supplier = (Organization) mgr.find(supplier);
                supplier.setName("TheModifiedName");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertEquals(ORG_NAME, ((Object[]) result.get(0))[5]);

                supplier = (Organization) mgr.find(supplier);
                assertEquals("TheModifiedName", supplier.getName());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionOrganization() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                supplier.setName("OrgWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong Organization (with greater objVersion) joined.",
                        1, result.size());
                assertEquals(ORG_NAME, ((Object[]) result.get(0))[5]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateProduct() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                product = (Product) mgr.find(product);
                product.setProductId("TheModifiedProductId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertEquals(PRODUCT_ID, ((Object[]) result.get(0))[7]);

                product = (Product) mgr.find(product);
                assertEquals("TheModifiedProductId", product.getProductId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionProduct() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addProduct();
                // incerease the version of the product
                product.setProductId("ProdWithGreaterObjhistory");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals("Wrong Product (with greater objVersion) joined.",
                        1, result.size());
                assertEquals(PRODUCT_ID, ((Object[]) result.get(0))[7]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateMarketplace() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(true, false);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                marketplace = (Marketplace) mgr.find(marketplace);
                marketplace.setMarketplaceId("TheModifiedMarketplaceId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertEquals(GLOBAL_MARKETPLACE_NAME,
                        ((Object[]) result.get(0))[11]);

                marketplace = (Marketplace) mgr.find(marketplace);
                assertEquals("TheModifiedMarketplaceId",
                        marketplace.getMarketplaceId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionMarketplace() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addProduct();
                addMarketplace();
                // increase the version of marketplace
                marketplace
                        .setMarketplaceId("MarketplaceWithGreaterObjhistory");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addMarketplace();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong Marketplace (with greater objVersion) joined.",
                        1, result.size());
                assertEquals(GLOBAL_MARKETPLACE_NAME,
                        ((Object[]) result.get(0))[11]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdatePaymentInfo() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, true);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // Change the PaymentType from INVOICE -> CREDIT_CARD
                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentTypeId(PaymentType.CREDIT_CARD);
                paymentType = (PaymentType) mgr.find(paymentType);
                PaymentInfo paymentInfo = (PaymentInfo) mgr.find(subscription
                        .getPaymentInfo());
                paymentInfo.setPaymentType(paymentType);
                paymentInfo
                        .setPaymentInfoId("Changed from INVOICE to CREDIT_CARD");
                subscription.setPaymentInfo(paymentInfo);
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                // the PaymentType was changed AFTER the subscription creation
                // so the SubscriptionHistory entry should still have INVOICE
                assertEquals(1, result.size());
                assertEquals(PaymentType.INVOICE,
                        ((Object[]) result.get(0))[12]);

                // verify that the new PaymentType of the Subscription
                // was indeed changed to CREDIT_CARD
                PaymentInfo paymentInfo = (PaymentInfo) mgr.find(subscription
                        .getPaymentInfo());
                assertEquals(PaymentType.CREDIT_CARD, paymentInfo
                        .getPaymentType().getPaymentTypeId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionPaymentInfo() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addProduct();
                addPaymentInfo();
                // Change the PaymentType from INVOICE -> CREDIT_CARD
                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentTypeId(PaymentType.CREDIT_CARD);
                paymentType = (PaymentType) mgr.find(paymentType);
                paymentInfo.setPaymentType(paymentType);
                // increase the version of the PaymentInfo
                paymentInfo
                        .setPaymentInfoId("Changed from INVOICE to CREDIT_CARD");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addPaymentInfo();
                addBillingContact();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong PaymentInfo (with greater objVersion) joined.",
                        1, result.size());
                assertEquals(PaymentType.INVOICE,
                        ((Object[]) result.get(0))[12]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdatePaymentType() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, true);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // change the PaymentType
                paymentType = (PaymentType) mgr.find(paymentType);
                paymentType.setPaymentTypeId("FAST_INVOICE");
                // change the Subscription
                subscription = (Subscription) mgr.find(subscription);
                subscription.setAccessInfo("back door");
                // but do not change the PaymentInfo
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                assertTrue(result.get(0) instanceof Object[]);
                Object[] newSubscription = (Object[]) result.get(0);
                assertEquals("The last PaymentTypeHistory entry expected",
                        "FAST_INVOICE", newSubscription[12]);

                assertTrue(result.get(1) instanceof Object[]);
                Object[] oldSubscription = (Object[]) result.get(1);
                assertEquals("The initial PaymentTypeHistory entry expected",
                        PaymentType.INVOICE, oldSubscription[12]);

            }
        }.run();
    }

    @Test
    public void testLog_ObjversionPaymentType() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addProduct();
                addPaymentInfo();
                // Change the PaymentType from INVOICE -> CREDIT_CARD
                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentTypeId(PaymentType.CREDIT_CARD);
                paymentType = (PaymentType) mgr.find(paymentType);
                paymentInfo.setPaymentType(paymentType);
                paymentInfo
                        .setPaymentInfoId("Changed from INVOICE to CREDIT_CARD");
                // modify the CREDIT_CARD payment type
                // so it has a greater objVersion than INVOICE
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addPaymentInfo();
                addBillingContact();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong PaymentType (with greater objVersion) joined.",
                        1, result.size());
                assertEquals(PaymentType.INVOICE,
                        ((Object[]) result.get(0))[12]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateBillingContact() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, true);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                BillingContact billingContact = (BillingContact) mgr
                        .find(subscription.getBillingContact());
                billingContact
                        .setBillingContactId("TheModifiedBillingContactId");
                subscription.setBillingContact(billingContact);
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertEquals(BILLING_CONTACT_ID, ((Object[]) result.get(0))[13]);

                BillingContact billingContact = (BillingContact) mgr
                        .find(subscription.getBillingContact());
                assertEquals("TheModifiedBillingContactId",
                        billingContact.getBillingContactId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionBillingContact() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addProduct();
                addPaymentInfo();
                addBillingContact();
                // increase the version of the BillingContact
                billingContact
                        .setBillingContactId("BillingContactWithGreaterObjhistory");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                paymentInfo = (PaymentInfo) mgr.find(paymentInfo);
                addBillingContact();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong BillingContact (with greater objVersion) joined.",
                        1, result.size());
                assertEquals(BILLING_CONTACT_ID, ((Object[]) result.get(0))[13]);
            }
        }.run();
    }

    @Test
    public void testLog_UserIdAsModUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addUser();
                // login with the userId instead of userKey,
                // so the userKey is written as history modDate
                container.login(user.getUserId());
                addProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertEquals(
                        "The userId is expected when written into history moduser instead of the user key.",
                        user.getUserId(), ((Object[]) result.get(0))[2]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdatePlatformUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                return null;
            }
        });
        final String initialUserId = user.getUserId();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                user = (PlatformUser) mgr.find(user);
                user.setUserId("NewUserId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(1, result.size());
                assertEquals("Wrong PlatformUserHistory entry", initialUserId,
                        ((Object[]) result.get(0))[2]);
                user = (PlatformUser) mgr.find(user);
                assertEquals("NewUserId", user.getUserId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionPlatformUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser("user" + Math.random());
                // increase the version of the PlatformUser
                user.setUserId("PlatformUserWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addAndLoginAsUser();
                addProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "Wrong PlatformUser (with greater objVersion) joined.";
                assertEquals(msg, 1, result.size());
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(0))[2]);
            }
        }.run();
    }

}
