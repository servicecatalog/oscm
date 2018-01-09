/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Oct 14, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.interceptor.DateFactory;
import org.oscm.operationslog.SubscriptionPriceQuery;
import org.oscm.operationslog.UserOperationLogQuery;
import org.oscm.test.TestDateFactory;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author barzu
 */
public class PriceModelHistoryIT extends UserOperationLogQueryTestBase {

    @Override
    protected UserOperationLogQuery getQuery() {
        return new SubscriptionPriceQuery();
    }

    @Override
    public void dataSetup() throws Exception {
        super.dataSetup();
        DateFactory.setInstance(new TestDateFactory(new Date()));
    }

    @After
    public void cleanUp() {
        TestDateFactory.restoreDefault();
    }

    @Test
    public void testLog() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                SupportedCurrencies.createOneSupportedCurrency(mgr);
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                assertTrue(result.get(0) instanceof Object[]);
                Object[] lastPriceModel = (Object[]) result.get(0);
                assertEquals("Wrong number of fields in the query result",
                        new SubscriptionPriceQuery().getFieldNames().length,
                        lastPriceModel.length);

                assertTrue(lastPriceModel[0] instanceof Date);
                assertEquals(ModificationType.MODIFY.name(), lastPriceModel[1]);
                assertEquals(user.getUserId(), lastPriceModel[2]);
                assertEquals(BigInteger.valueOf(product.getPriceModel()
                        .getVersion()), lastPriceModel[3]);
                assertEquals(subscription.getSubscriptionId(),
                        lastPriceModel[4]);
                assertEquals(supplier.getName(), lastPriceModel[5]);
                assertEquals(supplier.getOrganizationId(), lastPriceModel[6]);
                assertEquals(Boolean.FALSE, lastPriceModel[7]);
                assertEquals(product.getPriceModel().getOneTimeFee(),
                        lastPriceModel[8]);
                assertEquals(product.getPriceModel().getPeriod().name(),
                        lastPriceModel[9]);
                assertEquals(product.getPriceModel().getPricePerPeriod(),
                        lastPriceModel[10]);
                assertEquals(product.getPriceModel()
                        .getPricePerUserAssignment(), lastPriceModel[11]);
                assertEquals(product.getPriceModel().getCurrency()
                        .getCurrencyISOCode(), lastPriceModel[12]);

                Object[] firstPriceModel = (Object[]) result.get(1);
                assertEquals(ModificationType.ADD.name(), firstPriceModel[1]);
            }
        }.run();
    }

    @Test
    public void testLog_MissingNullableFields() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                // SupportedCurrency
                assertEquals(null, ((Object[]) result.get(0))[12]);
                assertEquals(null, ((Object[]) result.get(1))[12]);
            }
        }.run();
    }

    @Test
    public void testLog_ProductWithoutSubscription() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        DateFactory.setInstance(new TestDateFactory(new Date(DateFactory
                .getInstance().getTransactionDate().getTime() + 1)));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addChargeableProduct("prod0");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "Expected one product with subscription and one without subscription.";
                assertEquals(msg, 4, result.size());

                // check subscriptionId
                assertEquals(msg, null, ((Object[]) result.get(0))[4]);
                // check objVersion
                assertEquals(msg, BigInteger.valueOf(1),
                        ((Object[]) result.get(0))[3]);

                assertEquals(msg, null, ((Object[]) result.get(1))[4]);
                assertEquals(msg, BigInteger.valueOf(0),
                        ((Object[]) result.get(1))[3]);

                assertEquals(msg, SUBSCRIPTION_ID,
                        ((Object[]) result.get(2))[4]);
                assertEquals(msg, BigInteger.valueOf(1),
                        ((Object[]) result.get(2))[3]);

                assertEquals(msg, SUBSCRIPTION_ID,
                        ((Object[]) result.get(3))[4]);
                assertEquals(msg, BigInteger.valueOf(0),
                        ((Object[]) result.get(3))[3]);
            }
        }.run();
    }

    @Test
    public void testLog_ProductWithout2Subscriptions() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                addSubscription("SecondSubscription");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals("PriceModels * Subscriptions results expected.",
                        4, result.size());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionOrganization() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                supplier.setName("OrgWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong Organization (with greater objVersion) joined.",
                        2, result.size());
                assertEquals(ORG_NAME, ((Object[]) result.get(0))[5]);
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionProduct() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addProduct();
                // increase Product version
                product.setProductId("ProdWithGreaterObjhistory");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals("Wrong Product (with greater objVersion) joined.",
                        2, result.size());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionSubscription() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                // increase Subscription version
                subscription.setSubscriptionId("SubscrWithGreaterObjversion");
                return null;
            }
        });
        DateFactory.setInstance(new TestDateFactory(new Date(DateFactory
                .getInstance().getTransactionDate().getTime() + 1)));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addChargeableProduct("prod2");
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "Wrong Subscription (with greater objVersion) joined.";
                assertEquals(msg, 4, result.size());
                assertEquals(msg, SUBSCRIPTION_ID,
                        ((Object[]) result.get(0))[4]);
                assertEquals(msg, SUBSCRIPTION_ID,
                        ((Object[]) result.get(1))[4]);
                assertEquals(msg, "SubscrWithGreaterObjversion",
                        ((Object[]) result.get(2))[4]);
                assertEquals(msg, "SubscrWithGreaterObjversion",
                        ((Object[]) result.get(3))[4]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateSubscription() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        DateFactory.setInstance(new TestDateFactory(new Date(DateFactory
                .getInstance().getTransactionDate().getTime() + 1)));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription = (Subscription) mgr.find(subscription);
                subscription.setSubscriptionId("TheModifiedSubscriptionId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                for (Object object : result) {
                    Object[] priceModel = (Object[]) object;
                    assertEquals(
                            "The initial SubscriptionHistory entry expected",
                            SUBSCRIPTION_ID, priceModel[4]);
                }
                subscription = (Subscription) mgr.find(subscription);
                assertEquals("The last SubscriptionHistory entry expected",
                        "TheModifiedSubscriptionId",
                        subscription.getSubscriptionId());
            }
        }.run();
    }

    @Test
    public void testLog_ModdateSubscription2() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        DateFactory.setInstance(new TestDateFactory(new Date(DateFactory
                .getInstance().getTransactionDate().getTime() + 1)));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscription = (Subscription) mgr.find(subscription);
                // change the Subscription
                subscription.setSubscriptionId("TheModifiedSubscriptionId");
                // change the PriceModel
                subscription.getPriceModel().setPeriod(PricingPeriod.WEEK);
                // but do not change the Product
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(3, result.size());
                assertTrue(result.get(0) instanceof Object[]);
                Object[] newPriceModel = (Object[]) result.get(0);
                assertEquals("The last SubscriptionHistory entry expected",
                        "TheModifiedSubscriptionId", newPriceModel[4]);

                assertTrue(result.get(1) instanceof Object[]);
                Object[] oldPriceModel = (Object[]) result.get(1);
                assertEquals("The initial SubscriptionHistory entry expected",
                        SUBSCRIPTION_ID, oldPriceModel[4]);
            }
        }.run();
    }

    @Test
    public void testLog_UserIdAsModUser() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addUser();
                // login with the userId instead of userKey,
                // so the userKey is written as history modDate
                container.login(user.getUserId());
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "The userId is expected when written into history moduser instead of the user key.";
                assertEquals(msg, 2, result.size());
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(0))[2]);
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(1))[2]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateModUser() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        final String initialUserId = user.getUserId();
        DateFactory.setInstance(new TestDateFactory(new Date(DateFactory
                .getInstance().getTransactionDate().getTime() + 1)));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                user = (PlatformUser) mgr.find(user);
                user.setUserId("NewUserId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                for (Object object : result) {
                    Object[] usageLicense = (Object[]) object;
                    assertEquals(
                            "Wrong PlatformUserHistory entry (for moduser)",
                            initialUserId, usageLicense[2]);
                }
                user = (PlatformUser) mgr.find(user);
                assertEquals("NewUserId", user.getUserId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionModUser() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser("user");
                // increase the version of the PlatformUser for moduser
                user.setUserId("PlatformUserWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                addAndLoginAsUser();
                addChargeableProduct();
                addSubscription();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "Wrong PlatformUser (with greater objVersion) joined for moduser.";
                assertEquals(msg, 2, result.size());
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(0))[2]);
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(1))[2]);
            }
        }.run();
    }

}
