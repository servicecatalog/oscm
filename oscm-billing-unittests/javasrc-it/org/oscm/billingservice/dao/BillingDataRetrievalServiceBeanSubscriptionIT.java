/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Oct 22, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.oscm.test.matchers.BesMatchers.haveVersions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.billingservice.service.model.CustomerData;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.PriceModels;
import org.oscm.test.data.Products;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author kulle
 * 
 */
public class BillingDataRetrievalServiceBeanSubscriptionIT extends EJBTestBase {

    private static final String SUBPRX = "subscriptionid";
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final long CUSTOMER_ORGANIZTION_KEY = 1000;
    private BillingDataRetrievalServiceLocal bdr;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);

        createSubscriptionHistory(0L, 1001L, "2012-09-01 08:00:00", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE, 0, false);
    }

    private SubscriptionHistory createSubscriptionHistory(
            final long subscriptionObjKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            final SubscriptionStatus subscriptionStatus) throws Exception {
        return createSubscriptionHistory(subscriptionObjKey,
                CUSTOMER_ORGANIZTION_KEY, modificationDate, version,
                modificationType, subscriptionStatus, 0, false);
    }

    private SubscriptionHistory createSubscriptionHistory(
            final long subscriptionObjKey, final long customerOrganizationKey,
            final String modificationDate, final int version,
            final ModificationType modificationType,
            final SubscriptionStatus subscriptionStatus,
            final long productObjKey, final boolean external) throws Exception {
        return runTX(new Callable<SubscriptionHistory>() {
            @Override
            public SubscriptionHistory call() throws Exception {
                SubscriptionHistory subHist = new SubscriptionHistory();

                subHist.setInvocationDate(new Date());
                subHist.setObjKey(subscriptionObjKey);
                subHist.setObjVersion(version);
                subHist.setModdate(new SimpleDateFormat(DATE_PATTERN)
                        .parse(modificationDate));
                subHist.setModtype(modificationType);
                subHist.setModuser("moduser");

                subHist.getDataContainer().setCreationDate(
                        Long.valueOf(System.currentTimeMillis()));
                subHist.getDataContainer().setActivationDate(
                        Long.valueOf(System.currentTimeMillis()));
                subHist.getDataContainer().setStatus(subscriptionStatus);
                subHist.getDataContainer()
                        .setSubscriptionId(SUBPRX + subscriptionObjKey);
                subHist.getDataContainer().setTimeoutMailSent(false);
                subHist.setOrganizationObjKey(customerOrganizationKey);
                subHist.setCutOffDay(1);
                subHist.setProductObjKey(productObjKey);
                ds.persist(subHist);
                return subHist;
            }

        });
    }

    /**
     * One subscription only, created in current billing period.
     */
    @Test
    public void getSubscriptionsForCustomeroneSubscription() throws Exception {
        // given
        createSubscriptionHistory(10L, "2012-10-01 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {

            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        CUSTOMER_ORGANIZTION_KEY,
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-10-01 00:00:00").getTime(),
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-11-01 00:00:00").getTime(),
                        -1));
            }
        });

        // then
        assertEquals(1, billingInput.getSubscriptionKeys().size());
        long subscriptionKey = billingInput.getSubscriptionKeys().get(0)
                .longValue();
        assertEquals(1, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey).size());
        assertEquals(SUBPRX + "10",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(0).getDataContainer().getSubscriptionId());
    }

    /**
     * One subscription only, created in current billing period. Also updated in
     * the same period.
     */
    @Test
    public void getSubscriptionsForCustomeroneSubscriptionmodified()
            throws Exception {
        // given
        createSubscriptionHistory(10L, "2012-10-01 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(10L, "2012-10-10 08:00:00", 1,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {

            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        CUSTOMER_ORGANIZTION_KEY,
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-10-01 00:00:00").getTime(),
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-11-01 00:00:00").getTime(),
                        -1));
            }
        });

        // then
        assertEquals(1, billingInput.getSubscriptionKeys().size());
        long subscriptionKey = billingInput.getSubscriptionKeys().get(0)
                .longValue();
        assertEquals(2, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey).size());
        assertEquals(1,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(0).getObjVersion());
        assertEquals(0,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(1).getObjVersion());
    }

    @Test
    public void testGetHistoriesForSubscriptionsAndBillingPeriodHistoryDelivered()
            throws Exception {
        // given
        // when
        final List<Long> list = new ArrayList<>();
        list.add(Long.valueOf(0));
        final List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscriptionHistories.addAll(
                        bdr.loadSubscriptionHistoriesForBillingPeriod(list,
                                new SimpleDateFormat(DATE_PATTERN)
                                        .parse("2012-08-01 08:00:00").getTime(),
                                new SimpleDateFormat(DATE_PATTERN)
                                        .parse("2012-10-01 08:00:00")
                                        .getTime()));
                return null;
            }
        });

        // then
        assertEquals(1, subscriptionHistories.size());
    }

    @Test
    public void testGetHistoriesForSubscriptionsAndBillingPeriodNoHistoryDelivered()
            throws Exception {
        // given
        // when
        final List<Long> list = new ArrayList<>();
        list.add(Long.valueOf(13061984L));
        createSubscriptionHistory(13061984L, 1001L, "2011-12-02 08:00:00", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE, 0, false);
        createSubscriptionHistory(13061984L, 1001L, "2011-12-02 09:00:00", 0,
                ModificationType.MODIFY, SubscriptionStatus.DEACTIVATED, 0,
                false);

        final List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscriptionHistories.addAll(
                        bdr.loadSubscriptionHistoriesForBillingPeriod(list,
                                new SimpleDateFormat(DATE_PATTERN)
                                        .parse("2011-10-01 08:00:00").getTime(),
                                new SimpleDateFormat(DATE_PATTERN)
                                        .parse("2011-12-01 08:00:00")
                                        .getTime()));
                return null;
            }
        });

        // then
        assertEquals(0, subscriptionHistories.size());
    }

    @Test
    public void testGetHistoriesForSubscriptionsAndBillingPeriodOlTerminatedHistory()
            throws Exception {
        // given
        createSubscriptionHistory(0L, "2012-09-10 08:00:00", 1,
                ModificationType.MODIFY, SubscriptionStatus.DEACTIVATED);
        // when
        final List<Long> list = new ArrayList<>();
        list.add(Long.valueOf(0));
        final List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subscriptionHistories.addAll(
                        bdr.loadSubscriptionHistoriesForBillingPeriod(list,
                                new SimpleDateFormat(DATE_PATTERN)
                                        .parse("2013-10-01 08:00:00").getTime(),
                                new SimpleDateFormat(DATE_PATTERN)
                                        .parse("2013-11-01 08:00:00")
                                        .getTime()));
                return null;
            }
        });

        // then
        assertEquals(1, subscriptionHistories.size());
    }

    /**
     * One subscription only, created in last billing period.
     */
    @Test
    public void getSubscriptionsForCustomeroneSubscriptionlastPeriod()
            throws Exception {
        // given
        createSubscriptionHistory(10L, "2012-09-01 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {

            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        CUSTOMER_ORGANIZTION_KEY,
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-10-01 00:00:00").getTime(),
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-11-01 00:00:00").getTime(),
                        -1));
            }
        });

        // then
        assertEquals(1, billingInput.getSubscriptionKeys().size());
        long subscriptionKey = billingInput.getSubscriptionKeys().get(0)
                .longValue();
        assertEquals(1, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey).size());
        assertEquals(SUBPRX + "10",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(0).getDataContainer().getSubscriptionId());
    }

    /**
     * One subscription only, created in last billing period. Updated in last
     * and current period.
     */
    @Test
    public void getSubscriptionsForCustomeroneSubscriptionlastPeriodmodified()
            throws Exception {
        // given
        createSubscriptionHistory(11L, "2012-09-01 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(11L, "2012-09-11 10:10:02", 1,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(11L, "2012-10-02 09:10:00", 2,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(11L, "2012-10-03 12:41:00", 3,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {

            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        CUSTOMER_ORGANIZTION_KEY,
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-10-01 00:00:00").getTime(),
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-11-01 00:00:00").getTime(),
                        -1));
            }
        });

        // then
        assertEquals(1, billingInput.getSubscriptionKeys().size());
        long subscriptionKey = billingInput.getSubscriptionKeys().get(0)
                .longValue();
        assertEquals(4, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey).size());
        assertEquals(SUBPRX + "11",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(0).getDataContainer().getSubscriptionId());
        assertEquals(3,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(0).getObjVersion());
        assertEquals(2,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(1).getObjVersion());
        assertEquals(1,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(2).getObjVersion());
    }

    /**
     * <ul>
     * <li>one subscription deactivated in last period</li>
     * <li>one created and modified in last period</li>
     * <li>one created in last period and modified in current period</li>
     * <li>one created and modified in current period</li>
     * </ul>
     */
    @Test
    public void getSubscriptionsForCustomercomplex() throws Exception {
        // given
        givenDeactivatedSubsriptionInLastPeriod();
        givenSubscriptionModifiedInLastPeriod();
        givenModifiedSubscriptionFromLastPeriod();
        givenModifiedSubscriptionFromCurrentPeriod();

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {

            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        CUSTOMER_ORGANIZTION_KEY,
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-10-01 00:00:00").getTime(),
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-11-01 00:00:00").getTime(),
                        -1));
            }
        });

        // then
        assertEquals(4, billingInput.getSubscriptionKeys().size());
        long subscriptionKey0 = billingInput.getSubscriptionKeys().get(0)
                .longValue();
        assertEquals(SUBPRX + "11",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey0)
                        .get(0).getDataContainer().getSubscriptionId());
        assertEquals(1,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey0)
                        .get(0).getObjVersion());

        // givenSubscriptionModifiedInLastPeriod
        long subscriptionKey1 = billingInput.getSubscriptionKeys().get(1)
                .longValue();
        assertEquals(3, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey1).size());
        assertEquals(SUBPRX + "12",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey1)
                        .get(0).getDataContainer().getSubscriptionId());
        assertEquals(2,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey1)
                        .get(0).getObjVersion());

        // givenModifiedSubscriptionFromLastPeriod
        long subscriptionKey2 = billingInput.getSubscriptionKeys().get(2)
                .longValue();
        assertEquals(3, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey2).size());
        assertEquals(SUBPRX + "13",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey2)
                        .get(0).getDataContainer().getSubscriptionId());
        assertEquals(2,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey2)
                        .get(0).getObjVersion());
        assertEquals(1,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey2)
                        .get(1).getObjVersion());

        // givenModifiedSubscriptionFromCurrentPeriod
        long subscriptionKey3 = billingInput.getSubscriptionKeys().get(3)
                .longValue();
        assertEquals(2, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey3).size());
        assertEquals(SUBPRX + "14",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey3)
                        .get(0).getDataContainer().getSubscriptionId());
        assertEquals(1,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey3)
                        .get(0).getObjVersion());
        assertEquals(0,
                billingInput.getSubscriptionHistoryEntries(subscriptionKey3)
                        .get(1).getObjVersion());

    }

    /**
     * Two subscriptions, one of them with external billing system
     */
    @Test
    public void getSubscriptionsForCustomeroneSubscriptionWithExtBillingIncluded()
            throws Exception {
        // given
        createSubscriptionHistory(10L, "2012-09-01 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);

        createSubscriptionHistory(11L, 1001L, "2012-09-02 12:00:00", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE, 0, true);

        createSubscriptionHistory(12L, 1001L, "2012-09-03 12:00:00", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE, 0, true);

        // when
        CustomerData billingInput = runTX(new Callable<CustomerData>() {

            @Override
            public CustomerData call() throws Exception {
                return new CustomerData(bdr.loadSubscriptionsForCustomer(
                        CUSTOMER_ORGANIZTION_KEY,
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-10-01 00:00:00").getTime(),
                        new SimpleDateFormat(DATE_PATTERN)
                                .parse("2012-11-01 00:00:00").getTime(),
                        -1));
            }
        });

        // then
        assertEquals(1, billingInput.getSubscriptionKeys().size());
        long subscriptionKey = billingInput.getSubscriptionKeys().get(0)
                .longValue();
        assertEquals(1, billingInput
                .getSubscriptionHistoryEntries(subscriptionKey).size());
        assertEquals(SUBPRX + "10",
                billingInput.getSubscriptionHistoryEntries(subscriptionKey)
                        .get(0).getDataContainer().getSubscriptionId());
    }

    private void givenModifiedSubscriptionFromCurrentPeriod() throws Exception {
        createSubscriptionHistory(14L, "2012-10-15 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(14L, "2012-10-17 20:10:00", 1,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);
    }

    private void givenModifiedSubscriptionFromLastPeriod() throws Exception {
        createSubscriptionHistory(13L, "2012-09-18 09:00:22", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(13L, "2012-09-20 10:00:00", 1,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(13L, "2012-10-18 09:05:00", 2,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);
    }

    private void givenSubscriptionModifiedInLastPeriod() throws Exception {
        createSubscriptionHistory(12L, "2012-09-05 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(12L, "2012-09-15 15:10:42", 1,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(12L, "2012-09-15 15:11:50", 2,
                ModificationType.MODIFY, SubscriptionStatus.ACTIVE);

    }

    private void givenDeactivatedSubsriptionInLastPeriod() throws Exception {
        createSubscriptionHistory(11L, "2012-09-01 15:10:42", 0,
                ModificationType.ADD, SubscriptionStatus.ACTIVE);
        createSubscriptionHistory(11L, "2012-09-05 10:11:11", 1,
                ModificationType.MODIFY, SubscriptionStatus.DEACTIVATED);
    }

    @Test
    public void getPricemodelHistoriesForSubscriptionHistory()
            throws Exception {
        // given
        long endPeriod = 1325372400000L;
        SubscriptionHistory subscriptionHistory = setupForGetPricemodelForSubscriptionHistory(
                endPeriod);

        // when
        List<PriceModelHistory> priceModelHistories = getPricemodelHistoriesForSubscriptionHistory(
                subscriptionHistory.getObjKey(), endPeriod);

        // then
        assertThat(priceModelHistories, haveVersions(new long[] { 1, 2, 3 }));
    }

    @Test
    public void getLastPriceModelHistoryForSubscriptionHistorygetLastVersion()
            throws Exception {
        // given
        long baseTime = 1325372400000L;
        SubscriptionHistory subscriptionHistory = setupForGetPricemodelForSubscriptionHistory(
                baseTime);

        // when
        PriceModelHistory priceModelHistory = getLastPriceModelHistoryForSubscriptionHistory(
                subscriptionHistory);

        // then
        assertEquals(3, priceModelHistory.getObjVersion());
    }

    @Test
    public void getLastPriceModelHistoryForSubscriptionHistorygetLastTime()
            throws Exception {
        // given
        long baseTime = 1325372400000L;
        SubscriptionHistory subscriptionHistory = setupForPricemodelsWithDifferentTimeForSubscriptionHistory(
                baseTime);

        // when
        PriceModelHistory priceModelHistory = getLastPriceModelHistoryForSubscriptionHistory(
                subscriptionHistory);

        // then
        assertEquals(1, priceModelHistory.getObjVersion());
    }

    private SubscriptionHistory setupForGetPricemodelForSubscriptionHistory(
            final long endPeriod) throws Exception {
        return runTX(new Callable<SubscriptionHistory>() {
            @Override
            public SubscriptionHistory call() throws Exception {
                long productObjKey = 1;
                long priceModelObjKey = 2;
                long subscriptionObjKey = 3;
                String endDate = new SimpleDateFormat(PriceModels.DATE_PATTERN)
                        .format(new Date(endPeriod));

                Products.createProductHistory(ds, 0, productObjKey,
                        priceModelObjKey, 0, endDate, 0, ModificationType.ADD);

                PriceModels.createPriceModelHistory(ds, priceModelObjKey,
                        endDate, 1, ModificationType.ADD,
                        PriceModelType.FREE_OF_CHARGE, productObjKey);

                PriceModels.createPriceModelHistory(ds, priceModelObjKey,
                        endDate, 2, ModificationType.MODIFY,
                        PriceModelType.PER_UNIT, productObjKey);

                PriceModels.createPriceModelHistory(ds, priceModelObjKey,
                        endDate, 3, ModificationType.MODIFY,
                        PriceModelType.PRO_RATA, productObjKey);

                SubscriptionHistory subscriptionHistory = createSubscriptionHistory(
                        subscriptionObjKey, 4, endDate, 0, ModificationType.ADD,
                        SubscriptionStatus.ACTIVE, productObjKey, false);
                ds.flush();

                return subscriptionHistory;
            }
        });
    }

    private SubscriptionHistory setupForPricemodelsWithDifferentTimeForSubscriptionHistory(
            final long baseTime) throws Exception {
        return runTX(new Callable<SubscriptionHistory>() {
            @Override
            public SubscriptionHistory call() throws Exception {
                long productObjKey = 1;
                long priceModelObjKey = 2;
                long subscriptionObjKey = 3;

                Products.createProductHistory(ds, 0, productObjKey,
                        priceModelObjKey, 0,
                        new SimpleDateFormat(PriceModels.DATE_PATTERN)
                                .format(new Date(baseTime - 1000)),
                        0, ModificationType.ADD);

                PriceModels.createPriceModelHistory(ds, priceModelObjKey,
                        new SimpleDateFormat(PriceModels.DATE_PATTERN)
                                .format(new Date(baseTime - 1000)),
                        0, ModificationType.ADD, PriceModelType.FREE_OF_CHARGE,
                        productObjKey);

                PriceModels.createPriceModelHistory(ds, priceModelObjKey,
                        new SimpleDateFormat(PriceModels.DATE_PATTERN).format(
                                new Date(baseTime)),
                        1, ModificationType.MODIFY, PriceModelType.PER_UNIT,
                        productObjKey);

                PriceModels.createPriceModelHistory(ds, priceModelObjKey,
                        new SimpleDateFormat(PriceModels.DATE_PATTERN)
                                .format(new Date(baseTime + 1000)),
                        2, ModificationType.MODIFY, PriceModelType.PRO_RATA,
                        productObjKey);

                SubscriptionHistory subscriptionHistory = createSubscriptionHistory(
                        subscriptionObjKey, 4,
                        new SimpleDateFormat(PriceModels.DATE_PATTERN).format(
                                new Date(baseTime)),
                        0, ModificationType.MODIFY, SubscriptionStatus.ACTIVE,
                        productObjKey, false);
                ds.flush();

                return subscriptionHistory;
            }
        });
    }

    private List<PriceModelHistory> getPricemodelHistoriesForSubscriptionHistory(
            final long subscriptionKey, final long periodEnd) throws Exception {
        return runTX(new Callable<List<PriceModelHistory>>() {
            @Override
            public List<PriceModelHistory> call() {
                return bdr.loadPricemodelHistoriesForSubscriptionHistory(
                        subscriptionKey, periodEnd);
            }
        });
    }

    private PriceModelHistory getLastPriceModelHistoryForSubscriptionHistory(
            final SubscriptionHistory history) throws Exception {
        return runTX(new Callable<PriceModelHistory>() {
            @Override
            public PriceModelHistory call() throws Exception {
                return bdr.loadLatestPriceModelHistory(history);
            }
        });
    }
}
