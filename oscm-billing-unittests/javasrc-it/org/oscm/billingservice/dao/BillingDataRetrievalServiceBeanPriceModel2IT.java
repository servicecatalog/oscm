/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 16, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.PriceModels;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author baumann
 */
public class BillingDataRetrievalServiceBeanPriceModel2IT extends EJBTestBase {

    private static final long SUBSCRIPTION_KEY = 50000;
    private static final long CUSTOMER_ORG_KEY = 1000;
    private static final long PRODUCT1_KEY = 10000;
    private static final long PRODUCT2_KEY = 20000;
    private static final long PRICEMODEL1_KEY = 11000;
    private static final long PRICEMODEL2_KEY = 21000;

    private BillingDataRetrievalServiceLocal bdr;
    private DataService ds;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        bdr = container.get(BillingDataRetrievalServiceLocal.class);
    }

    private void createSubscriptionHistory(final long subscriptionObjKey,
            final long customerOrganizationKey, final String modificationDate,
            final int version, final ModificationType modificationType,
            final SubscriptionStatus subscriptionStatus,
            final long productObjKey) throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscriptions.createSubscriptionHistory(ds, subscriptionObjKey,
                        customerOrganizationKey, modificationDate, version,
                        modificationType, subscriptionStatus, productObjKey, 1);
                return null;
            }
        });
    }

    private void createPriceModelHistory(final DataService ds,
            final long objKey, final String modificationDate, final int version,
            final ModificationType modificationType,
            final PriceModelType priceModelType, final long productObjKey,
            final boolean provisioningCompleted) throws Exception {

        runTX(new Callable<PriceModelHistory>() {
            @Override
            public PriceModelHistory call() throws Exception {
                return PriceModels.createPriceModelHistory(ds, objKey,
                        modificationDate + ",000", version, modificationType,
                        priceModelType, productObjKey, provisioningCompleted);
            }
        });
    }

    @Test
    public void loadPreviousSubscriptionHistoryForPriceModel_noResult()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr.loadPreviousSubscriptionHistoryForPriceModel(
                                PRICEMODEL1_KEY,
                                dateToMillis("2012-10-01 08:00:00"));
                    }
                });

        // then
        assertNull(subHistory);
    }

    @Test
    public void loadPreviousSubscriptionHistoryForPriceModel_noFreePeriod()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr.loadPreviousSubscriptionHistoryForPriceModel(
                                PRICEMODEL1_KEY,
                                dateToMillis("2012-10-01 08:20:00"));
                    }
                });

        // then
        assertEquals(1, subHistory.getObjVersion());
    }

    @Test
    public void loadPreviousSubscriptionHistoryForPriceModel_FreePeriod()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr.loadPreviousSubscriptionHistoryForPriceModel(
                                PRICEMODEL1_KEY,
                                dateToMillis("2012-10-03 10:10:00"));
                    }
                });

        // then
        assertEquals(5, subHistory.getObjVersion());
    }

    @Test
    public void loadNextActiveSubscriptionHistoryForPriceModel_noFreePeriod()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr
                                .loadNextActiveSubscriptionHistoryForPriceModel(
                                        PRICEMODEL1_KEY,
                                        dateToMillis("2012-10-01 08:20:00"));
                    }
                });

        // then
        assertEquals(2, subHistory.getObjVersion());
    }

    @Test
    public void loadNextActiveSubscriptionHistoryForPriceModel_FpEndsAtUpgradeTime()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr
                                .loadNextActiveSubscriptionHistoryForPriceModel(
                                        PRICEMODEL1_KEY,
                                        dateToMillis("2012-10-03 10:20:00"));
                    }
                });

        // then
        assertNull(subHistory);
    }

    @Test
    public void loadNextActiveSubscriptionHistoryForPriceModel_upgradedProductNoFp()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr
                                .loadNextActiveSubscriptionHistoryForPriceModel(
                                        PRICEMODEL2_KEY,
                                        dateToMillis("2012-10-03 10:20:00"));
                    }
                });

        // then
        assertEquals(7, subHistory.getObjVersion());
    }

    @Test
    public void loadNextActiveSubscriptionHistoryForPriceModel_upgradedProductFpEndsAtActivation()
            throws Exception {
        // given
        setupSubAndPmHistoriesForAsyncUpgrade();

        // when
        SubscriptionHistory subHistory = runTX(
                new Callable<SubscriptionHistory>() {
                    @Override
                    public SubscriptionHistory call() throws Exception {
                        return bdr
                                .loadNextActiveSubscriptionHistoryForPriceModel(
                                        PRICEMODEL2_KEY,
                                        dateToMillis("2012-10-05 11:00:00"));
                    }
                });

        // then
        assertEquals(7, subHistory.getObjVersion());
    }

    private void setupSubAndPmHistoriesForAsyncUpgrade() throws Exception {
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-01 08:00:00", 0, ModificationType.ADD,
                SubscriptionStatus.PENDING, PRODUCT1_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-01 08:00:00", 1, ModificationType.MODIFY,
                SubscriptionStatus.PENDING, PRODUCT1_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-01 08:20:00", 2, ModificationType.MODIFY,
                SubscriptionStatus.ACTIVE, PRODUCT1_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-03 10:00:00", 3, ModificationType.MODIFY,
                SubscriptionStatus.ACTIVE, PRODUCT1_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-03 10:00:00", 4, ModificationType.MODIFY,
                SubscriptionStatus.PENDING_UPD, PRODUCT1_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-03 10:00:00", 5, ModificationType.MODIFY,
                SubscriptionStatus.PENDING_UPD, PRODUCT1_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-03 10:20:00", 6, ModificationType.MODIFY,
                SubscriptionStatus.SUSPENDED, PRODUCT2_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-05 11:00:00", 7, ModificationType.MODIFY,
                SubscriptionStatus.ACTIVE, PRODUCT2_KEY);
        createSubscriptionHistory(SUBSCRIPTION_KEY, CUSTOMER_ORG_KEY,
                "2012-10-25 13:00:00", 8, ModificationType.MODIFY,
                SubscriptionStatus.DEACTIVATED, PRODUCT2_KEY);

        createPriceModelHistory(ds, PRICEMODEL1_KEY, "2012-10-01 08:00:00", 0,
                ModificationType.ADD, PriceModelType.PER_UNIT, PRODUCT1_KEY,
                false);
        createPriceModelHistory(ds, PRICEMODEL1_KEY, "2012-10-01 08:20:00", 1,
                ModificationType.MODIFY, PriceModelType.PER_UNIT, PRODUCT1_KEY,
                true);
        createPriceModelHistory(ds, PRICEMODEL2_KEY, "2012-10-03 10:00:00", 1,
                ModificationType.ADD, PriceModelType.PRO_RATA, PRODUCT2_KEY,
                false);
        createPriceModelHistory(ds, PRICEMODEL1_KEY, "2012-10-03 10:20:00", 2,
                ModificationType.DELETE, PriceModelType.PER_UNIT, PRODUCT1_KEY,
                true);
        createPriceModelHistory(ds, PRICEMODEL2_KEY, "2012-10-03 10:20:00", 2,
                ModificationType.MODIFY, PriceModelType.PRO_RATA, PRODUCT2_KEY,
                true);
    }

    private long dateToMillis(String date) {
        return DateTimeHandling.calculateMillis(date);
    }

}
