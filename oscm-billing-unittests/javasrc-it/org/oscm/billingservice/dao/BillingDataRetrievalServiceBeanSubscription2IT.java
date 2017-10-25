/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.05.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.billingservice.dao.model.BillingSubscriptionData;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingSubscriptionStatus;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingSubscriptionStates;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author baumann
 * 
 */
public class BillingDataRetrievalServiceBeanSubscription2IT
        extends EJBTestBase {

    private BillingDataRetrievalServiceLocal bdr;
    private DataService ds;
    private List<Subscription> subscriptions = new ArrayList<>();
    private Organization supplier;
    private TechnicalProduct techProd;
    private Product product;
    private Product productFree;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);

        supplier = Organizations.createOrganization(ds,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);

        techProd = TechnicalProducts.createTechnicalProduct(ds, supplier,
                "prodId", false, ServiceAccessType.LOGIN);

        product = Products.createProduct(supplier, techProd, true, "Product",
                null, null, ds);
        productFree = Products.createProduct(supplier, techProd, true,
                "Product free", null, null, ds);
    }

    /**
     * Three subscriptions to a chargeable service. One was not charged at all,
     * for the other two the last billing period was not charged yet.
     */
    @Test
    public void getSubscriptionsForBilling() throws Exception {
        final Long subActivationDate = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));
        final long endOfLastBilledPeriod = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long effectiveBillingEndDate = DateTimeHandling
                .calculateMillis("2014-09-01 00:00:00");
        final long cutoffBillingEndDate = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long cutoffDeactivationDate = DateTimeHandling
                .calculateMillis("2014-06-28 00:00:00");

        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                createSubscription(product, "subscription1", supplier,
                        subActivationDate, subActivationDate, 1);
                createSubscription(product, "subscription2", supplier,
                        subActivationDate, subActivationDate, 2);
                createSubscription(product, "subscription3", supplier,
                        subActivationDate, subActivationDate, 3);

                BillingSubscriptionStates.createBillingSubscriptionStatus(ds,
                        subscriptions.get(0).getKey(), endOfLastBilledPeriod);
                BillingSubscriptionStates.createBillingSubscriptionStatus(ds,
                        subscriptions.get(2).getKey(), endOfLastBilledPeriod);

                return null;
            }
        });

        // when
        List<BillingSubscriptionData> subscriptionData = runTX(
                new Callable<List<BillingSubscriptionData>>() {
                    @Override
                    public List<BillingSubscriptionData> call()
                            throws Exception {
                        return bdr.getSubscriptionsForBilling(
                                effectiveBillingEndDate, cutoffBillingEndDate,
                                cutoffDeactivationDate);
                    }
                });

        // then
        assertNotNull(subscriptionData);
        assertEquals("Wrong number of subscriptions", 3,
                subscriptionData.size());
        assertSubscriptionData(subscriptionData, 0, 0,
                Long.valueOf(endOfLastBilledPeriod));
        assertSubscriptionData(subscriptionData, 1, 1, null);
        assertSubscriptionData(subscriptionData, 2, 2,
                Long.valueOf(endOfLastBilledPeriod));
    }

    /**
     * Subscription 1 was deactivated on the cutoff deactivation date (-> must
     * be billed), Subscription 2 was also deactivated on the cutoff
     * deactivation date, but was already charged in the last relevant billing
     * period (-> no billing), Subscription 3 was not deactivated (-> must be
     * billed).
     */
    @Test
    public void getSubscriptionsForBilling2() throws Exception {
        final Long subActivationDate = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));
        final long endOfLastBilledPeriod = DateTimeHandling
                .calculateMillis("2014-07-15 00:00:00");
        final long endOfLastBilledPeriod2 = DateTimeHandling
                .calculateMillis("2014-08-15 23:59:59");
        final long effectiveBillingEndDate = DateTimeHandling
                .calculateMillis("2014-09-01 00:00:00");
        final long cutoffBillingEndDate = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long cutoffDeactivationDate = DateTimeHandling
                .calculateMillis("2014-06-28 00:00:00");

        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                createSubscription(product, "subscription1", supplier,
                        subActivationDate, subActivationDate, 1);
                deactivateSubscription("subscription1", supplier,
                        Long.valueOf(cutoffDeactivationDate));
                createSubscription(product, "subscription2", supplier,
                        subActivationDate, subActivationDate, 2);
                deactivateSubscription("subscription2", supplier,
                        Long.valueOf(cutoffDeactivationDate));
                createSubscription(product, "subscription3", supplier,
                        subActivationDate, subActivationDate, 3);

                BillingSubscriptionStates.createBillingSubscriptionStatus(ds,
                        subscriptions.get(0).getKey(), endOfLastBilledPeriod);
                BillingSubscriptionStates.createBillingSubscriptionStatus(ds,
                        subscriptions.get(1).getKey(), endOfLastBilledPeriod2);

                return null;
            }
        });

        // when
        List<BillingSubscriptionData> subscriptionData = runTX(
                new Callable<List<BillingSubscriptionData>>() {
                    @Override
                    public List<BillingSubscriptionData> call()
                            throws Exception {
                        return bdr.getSubscriptionsForBilling(
                                effectiveBillingEndDate, cutoffBillingEndDate,
                                cutoffDeactivationDate);
                    }
                });

        // then
        assertNotNull(subscriptionData);
        assertEquals("Wrong number of subscriptions", 2,
                subscriptionData.size());
        assertSubscriptionData(subscriptionData, 0, 0,
                Long.valueOf(endOfLastBilledPeriod));
        assertSubscriptionData(subscriptionData, 2, 1, null);
    }

    /**
     * Subscription 1 was not activated (-> no billing), Subscription 2 was
     * deactivated after the cutoff deactivation date (-> must be billed),
     * Subscription 3 was deactivated before the cutoff deactivation date and
     * has a billing subscription status (-> no billing, because it must have
     * been completely billed), Subscription 4 was deactivated before the cutoff
     * deactivation date but has no billing subscription status (-> must be
     * billed).
     */
    @Test
    public void getSubscriptionsForBilling3() throws Exception {
        final Long subCreationDate = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));
        final Long subActivationDate = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:10"));
        final Long subDeactivationDate1 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-07-30 00:00:00"));
        final Long subDeactivationDate2 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-06-15 00:00:00"));
        final long endOfLastBilledPeriod = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long endOfLastBilledPeriod2 = DateTimeHandling
                .calculateMillis("2014-07-28 00:00:00");
        final long effectiveBillingEndDate = DateTimeHandling
                .calculateMillis("2014-09-01 00:00:00");
        final long cutoffBillingEndDate = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long cutoffDeactivationDate = DateTimeHandling
                .calculateMillis("2014-06-28 00:00:00");

        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                createSubscription(product, "subscription1", supplier,
                        subCreationDate, null, 1);
                createSubscription(product, "subscription2", supplier,
                        subCreationDate, subActivationDate, 2);
                deactivateSubscription("subscription2", supplier,
                        subDeactivationDate1);
                createSubscription(product, "subscription3", supplier,
                        subCreationDate, subActivationDate, 3);
                deactivateSubscription("subscription3", supplier,
                        subDeactivationDate2);
                createSubscription(product, "subscription4", supplier,
                        subCreationDate, subActivationDate, 4);
                deactivateSubscription("subscription4", supplier,
                        subDeactivationDate2);

                BillingSubscriptionStates.createBillingSubscriptionStatus(ds,
                        subscriptions.get(1).getKey(), endOfLastBilledPeriod);
                BillingSubscriptionStates.createBillingSubscriptionStatus(ds,
                        subscriptions.get(2).getKey(), endOfLastBilledPeriod2);

                return null;
            }
        });

        // when
        List<BillingSubscriptionData> subscriptionData = runTX(
                new Callable<List<BillingSubscriptionData>>() {
                    @Override
                    public List<BillingSubscriptionData> call()
                            throws Exception {
                        return bdr.getSubscriptionsForBilling(
                                effectiveBillingEndDate, cutoffBillingEndDate,
                                cutoffDeactivationDate);
                    }
                });

        // then
        assertNotNull(subscriptionData);
        assertEquals("Wrong number of subscriptions", 2,
                subscriptionData.size());
        assertSubscriptionData(subscriptionData, 1, 0,
                Long.valueOf(endOfLastBilledPeriod));
        assertSubscriptionData(subscriptionData, 3, 1, null);
    }

    /**
     * Four subscriptions to a chargeable service. For subscription1 the last
     * billing period was not charged yet. (-> must be billed). Subscription2
     * was not charged at all and has an activation date after the effective
     * billing end date (-> no billing). Subscription3 was not charged at all
     * and has an activation date equal to the effective billing end date (->
     * may be billed, depends on cutoff day). Subscription4 was not charged at
     * all and has an activation date before the effective billing end date (->
     * may be billed, depends on cutoff day).
     */
    @Test
    public void getSubscriptionsForBilling4() throws Exception {
        final Long subActivationDate1 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));
        final Long subActivationDate2 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-09-08 00:00:00"));
        final Long subActivationDate3 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-09-01 00:00:00"));
        final Long subActivationDate4 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-08-20 00:00:00"));
        final long endOfLastBilledPeriod = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long effectiveBillingEndDate = DateTimeHandling
                .calculateMillis("2014-09-01 00:00:00");
        final long cutoffBillingEndDate = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long cutoffDeactivationDate = DateTimeHandling
                .calculateMillis("2014-06-28 00:00:00");

        // given
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                create4SubsAndBillingSubscriptionStatus(subActivationDate4,
                        subActivationDate2, subActivationDate3,
                        subActivationDate1, endOfLastBilledPeriod);

                return null;
            }
        });

        // when
        List<BillingSubscriptionData> subscriptionData = runTX(
                new Callable<List<BillingSubscriptionData>>() {
                    @Override
                    public List<BillingSubscriptionData> call()
                            throws Exception {
                        return bdr.getSubscriptionsForBilling(
                                effectiveBillingEndDate, cutoffBillingEndDate,
                                cutoffDeactivationDate);
                    }
                });

        // then
        assertNotNull(subscriptionData);
        assertEquals("Wrong number of subscriptions", 3,
                subscriptionData.size());
        assertSubscriptionData(subscriptionData, 0, 0,
                Long.valueOf(endOfLastBilledPeriod));
        assertSubscriptionData(subscriptionData, 2, 1, null);
        assertSubscriptionData(subscriptionData, 3, 2, null);
    }

    BillingSubscriptionStatus create4SubsAndBillingSubscriptionStatus(
            Long subActivationDate4, Long subActivationDate2,
            Long subActivationDate3, Long subActivationDate1,
            long endOfLastBilledPeriod) throws NonUniqueBusinessKeyException {
        createSubscription(product, "subscription4", supplier,
                subActivationDate4, subActivationDate4, 4);
        createSubscription(product, "subscription2", supplier,
                subActivationDate2, subActivationDate2, 2);
        createSubscription(product, "subscription3", supplier,
                subActivationDate3, subActivationDate3, 3);
        createSubscription(product, "subscription1", supplier,
                subActivationDate1, subActivationDate1, 1);

        BillingSubscriptionStatus billingSubscriptionStatus = BillingSubscriptionStates
                .createBillingSubscriptionStatus(ds,
                        subscriptions.get(0).getKey(), endOfLastBilledPeriod);
        return billingSubscriptionStatus;
    }

    @Test
    public void testUpdateBillingSubscriptionStatus_update() throws Exception {
        // given
        final Long subActivationDate1 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));
        final Long subActivationDate2 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-09-08 00:00:00"));
        final Long subActivationDate3 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-09-01 00:00:00"));
        final Long subActivationDate4 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-08-20 00:00:00"));
        final long endOfLastBilledPeriod = DateTimeHandling
                .calculateMillis("2014-08-01 00:00:00");
        final long endOfLastBilledPeriod2 = DateTimeHandling
                .calculateMillis("2014-07-01 00:00:00");

        final BillingSubscriptionStatus status = runTX(
                new Callable<BillingSubscriptionStatus>() {
                    @Override
                    public BillingSubscriptionStatus call() throws Exception {
                        return create4SubsAndBillingSubscriptionStatus(
                                subActivationDate4, subActivationDate2,
                                subActivationDate3, subActivationDate1,
                                endOfLastBilledPeriod);
                    }
                });
        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bdr.updateBillingSubscriptionStatus(status.getSubscriptionKey(),
                        endOfLastBilledPeriod2);
                return null;
            }
        });

        // then
        BillingSubscriptionStatus status2 = runTX(
                new Callable<BillingSubscriptionStatus>() {
                    @Override
                    public BillingSubscriptionStatus call() throws Exception {
                        BillingSubscriptionStatus billingSubStatus = new BillingSubscriptionStatus();
                        billingSubStatus.setSubscriptionKey(
                                status.getSubscriptionKey());
                        return (BillingSubscriptionStatus) ds
                                .find(billingSubStatus);
                    }
                });
        assertEquals(status.getEndOfLastBilledPeriod(),
                status2.getEndOfLastBilledPeriod());
    }

    @Test
    public void testUpdateBillingSubscriptionStatus_createNew()
            throws Exception {
        // given
        final Long subActivationDate4 = Long.valueOf(
                DateTimeHandling.calculateMillis("2014-08-20 00:00:00"));
        final long endOfLastBilledPeriod2 = DateTimeHandling
                .calculateMillis("2014-07-01 00:00:00");

        final Subscription createdSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return createSubscription(productFree, "subscription4",
                        supplier, subActivationDate4, subActivationDate4, 1);
            }
        });
        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bdr.updateBillingSubscriptionStatus(createdSub.getKey(),
                        endOfLastBilledPeriod2);
                return null;
            }
        });

        // then
        BillingSubscriptionStatus status2 = runTX(
                new Callable<BillingSubscriptionStatus>() {
                    @Override
                    public BillingSubscriptionStatus call() throws Exception {
                        BillingSubscriptionStatus billingSubStatus = new BillingSubscriptionStatus();
                        billingSubStatus
                                .setSubscriptionKey(createdSub.getKey());
                        return (BillingSubscriptionStatus) ds
                                .find(billingSubStatus);
                    }
                });
        assertEquals(endOfLastBilledPeriod2,
                status2.getEndOfLastBilledPeriod());
    }

    private void assertSubscriptionData(
            List<BillingSubscriptionData> subscriptionData, int subIndex,
            int subDataIndex, Long endOfLastBilledPeriod) {
        assertEquals("Wrong subscription key",
                subscriptions.get(subIndex).getKey(),
                subscriptionData.get(subDataIndex).getSubscriptionKey());
        assertEquals("Wrong subscription activation date",
                subscriptions.get(subIndex).getActivationDate().longValue(),
                subscriptionData.get(subDataIndex).getActivationDate());
        assertEquals("Wrong subscription cutoff day",
                subscriptions.get(subIndex).getCutOffDay(),
                subscriptionData.get(subDataIndex).getCutOffDay());
        assertEquals("Wrong end of last billed period", endOfLastBilledPeriod,
                subscriptionData.get(subDataIndex).getEndOfLastBilledPeriod());
    }

    private Subscription createSubscription(Product prodTemplate,
            String subscriptionId, Organization org, Long creationDate,
            Long activationDate, int cutoffDay)
            throws NonUniqueBusinessKeyException {
        Product subProduct = Products.createProduct(supplier, techProd,
                prodTemplate.getPriceModel().isChargeable(),
                prodTemplate.getProductId() + "#" + UUID.randomUUID(), null,
                null, ds, false, true);
        subProduct.setTemplate(prodTemplate);

        Subscription sub = createSubscription(subProduct, subscriptionId, org,
                null, creationDate, activationDate, cutoffDay);
        subscriptions.add(sub);
        return sub;
    }

    private Subscription createSubscription(Product prod, String subscriptionId,
            Organization org, Marketplace marketplace, Long creationDate,
            Long activationDate, int cutoffDay)
            throws NonUniqueBusinessKeyException {
        Subscription sub = new Subscription();
        sub.setCreationDate(creationDate);
        sub.setActivationDate(activationDate);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setSubscriptionId(subscriptionId);
        sub.setOrganization(org);
        sub.bindToProduct(prod);
        sub.setMarketplace(marketplace);
        sub.setCutOffDay(cutoffDay);
        sub.setUuid(UUID.randomUUID());
        ds.persist(sub);
        return sub;
    }

    private Subscription deactivateSubscription(String subscriptionId,
            Organization org, Long deactivationDate) {
        Subscription subTemplate = new Subscription();
        subTemplate.setSubscriptionId(subscriptionId);
        subTemplate.setOrganization(org);

        Subscription sub = (Subscription) ds.find(subTemplate);
        if (sub != null) {
            sub.setSubscriptionId(String.valueOf(System.currentTimeMillis()));
            sub.setDeactivationDate(deactivationDate);
            sub.setStatus(SubscriptionStatus.DEACTIVATED);
        }

        return sub;
    }
}
