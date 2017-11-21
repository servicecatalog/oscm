/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 25, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.billingservice.business.calculation.revenue.model.SubscriptionInput;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author baumann
 * 
 */
public class SubscriptionHistoryEvaluatorTest {

    private static final long SUBSCRIPTION_KEY = 11111L;
    private static final long PRODUCT_KEY_FREE_OF_CHARGE = 4711L;
    private static final long PRODUCT_KEY_PRO_RATA = 4712L;
    private static final long PRODUCT_KEY_PRO_RATA2 = 4713L;
    private static final long PRODUCT_KEY_PER_UNIT = 4714L;
    private static final long PRODUCT_KEY_PER_UNIT2 = 4715L;

    private BillingDataRetrievalServiceLocal bdr;

    @Before
    public void setup() throws Exception {
        bdr = mock(BillingDataRetrievalServiceBean.class);

        PriceModelHistory pmhFreeOfCharge = newPriceModelHistory(PriceModelType.FREE_OF_CHARGE);
        when(
                bdr.loadLatestPriceModelHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_FREE_OF_CHARGE;
                    }
                }))).thenReturn(pmhFreeOfCharge);

        PriceModelHistory pmhProRata = newPriceModelHistory(PriceModelType.PRO_RATA);
        when(
                bdr.loadLatestPriceModelHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PRO_RATA;
                    }
                }))).thenReturn(pmhProRata);

        PriceModelHistory pmhProRata2 = newPriceModelHistory(PriceModelType.PRO_RATA);
        when(
                bdr.loadLatestPriceModelHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PRO_RATA2;
                    }
                }))).thenReturn(pmhProRata2);

        PriceModelHistory pmhPerUnit = newPriceModelHistory(PriceModelType.PER_UNIT);
        when(
                bdr.loadLatestPriceModelHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PER_UNIT;
                    }
                }))).thenReturn(pmhPerUnit);

        PriceModelHistory pmhPerUnit2 = newPriceModelHistory(PriceModelType.PER_UNIT);
        when(
                bdr.loadLatestPriceModelHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PER_UNIT2;
                    }
                }))).thenReturn(pmhPerUnit2);
    }

    @Test
    public void filterSuspendedSubscriptionHistories_EmptyList()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(0, results.size());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_OneSubscriptionHistory()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        histories.add(newSubscriptionHistory(1L, 1L, 1L, null, null));

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(1, results.size());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_FreeOfChargeSubscriptionSuspendedAndActivatedInOneTimeUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        histories.add(newSubscriptionHistory(2L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_FREE_OF_CHARGE, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-11 00:00:01")));
        histories.add(newSubscriptionHistory(1L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_FREE_OF_CHARGE, SubscriptionStatus.SUSPENDED,
                DateTimeHandling.calculateDate("2013-03-11 00:00:00")));

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getKey());
        assertEquals(1L, results.get(1).getKey());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_ProRataSubscriptionSuspendedAndActivatedInOneTimeUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        histories.add(newSubscriptionHistory(2L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PRO_RATA, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-11 00:00:01")));
        histories.add(newSubscriptionHistory(1L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PRO_RATA, SubscriptionStatus.SUSPENDED,
                DateTimeHandling.calculateDate("2013-03-11 00:00:00")));

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getKey());
        assertEquals(1L, results.get(1).getKey());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_PerUnitSubscriptionSuspendedAndActivatedInOneTimeUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        histories.add(newSubscriptionHistory(2L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-11 00:00:01")));
        histories.add(newSubscriptionHistory(1L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.SUSPENDED,
                DateTimeHandling.calculateDate("2013-03-11 00:00:00")));

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).getKey());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_PerUnitSubscriptionSuspendedAndUpgradedInOneTimeUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-08 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-05 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(4, results.size());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_PerUnitSubscriptionSuspendedAndActivatedInDifferentTimeUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        histories.add(newSubscriptionHistory(2L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-11 00:00:00")));
        histories.add(newSubscriptionHistory(1L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.SUSPENDED,
                DateTimeHandling.calculateDate("2013-03-10 23:59:59")));

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).getKey());
        assertEquals(1L, results.get(1).getKey());
    }

    @Test
    public void filterSuspendedSubscriptionHistories_MultipleActiveHistories()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();
        histories.add(newSubscriptionHistory(3L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-11 00:00:01")));
        histories.add(newSubscriptionHistory(2L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-11 00:00:00")));
        histories.add(newSubscriptionHistory(1L, SUBSCRIPTION_KEY,
                PRODUCT_KEY_PER_UNIT, SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateDate("2013-03-10 23:59:59")));

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterSuspendedSubscriptionHistories(histories);

        // then
        assertEquals(3, results.size());
        assertEquals(3L, results.get(0).getKey());
        assertEquals(2L, results.get(1).getKey());
        assertEquals(1L, results.get(2).getKey());
    }

    @Test
    public void filterIrrelevantSubscriptionHistories() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 10L, SubscriptionStatus.DEACTIVATED,
                ModificationType.DELETE, "1364193900000",
                DateTimeHandling.calculateMillis("2013-04-02 16:33:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 9L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-15 12:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-14 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 18:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterIrrelevantSubscriptionHistories(histories);

        // then
        assertEquals(6, results.size());
        assertEquals(9L, results.get(0).getKey());
        assertEquals(8L, results.get(1).getKey());
        assertEquals(5L, results.get(2).getKey());
        assertEquals(4L, results.get(3).getKey());
        assertEquals(3L, results.get(4).getKey());
        assertEquals(1L, results.get(5).getKey());
    }

    @Test
    public void filterIrrelevantSubscriptionHistories_Pending()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 18:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 03:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 03:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterIrrelevantSubscriptionHistories(histories);

        // then
        assertEquals(3, results.size());
        assertEquals(4L, results.get(0).getKey());
        assertEquals(3L, results.get(1).getKey());
        assertEquals(1L, results.get(2).getKey());
    }

    @Test
    public void filterIrrelevantSubscriptionHistories_Invalid()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 1L, SubscriptionStatus.INVALID,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-10 11:22:33"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterIrrelevantSubscriptionHistories(histories);

        // then
        assertEquals(0, results.size());
    }

    @Test
    public void filterIrrelevantSubscriptionHistories_OneHistory()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-14 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterIrrelevantSubscriptionHistories(histories);

        // then
        assertEquals(1, results.size());
        assertEquals(6L, results.get(0).getKey());
    }

    @Test
    public void filterIrrelevantSubscriptionHistories_EmptyList()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterIrrelevantSubscriptionHistories(histories);

        // then
        assertEquals(0, results.size());
    }

    @Test
    public void filterIrrelevantSubscriptionHistories_UpgradeSuspended()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-08 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-05 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator()
                .filterIrrelevantSubscriptionHistories(histories);

        // then
        assertEquals(4, results.size());
        assertEquals(5L, results.get(0).getKey());
        assertEquals(4L, results.get(1).getKey());
        assertEquals(3L, results.get(2).getKey());
        assertEquals(1L, results.get(3).getKey());
    }

    @Test
    public void filterSubscriptionHistories() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 10L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 9L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-15 12:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-14 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 18:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId1",
                DateTimeHandling.calculateMillis("2013-03-07 15:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        List<SubscriptionHistory> results = newSubHistoryEvaluator(histories)
                .filterSubscriptionHistories();

        // then
        assertEquals(4, results.size());
        assertEquals(10L, results.get(0).getKey());
        assertEquals(9L, results.get(1).getKey());
        assertEquals(6L, results.get(2).getKey());
        assertEquals(1L, results.get(3).getKey());
    }

    @Test
    public void testGetLastValidSubscriptionId_OneElementInput()
            throws Exception {
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(newSubscriptionHistory(
                1L, SubscriptionStatus.ACTIVE, ModificationType.MODIFY,
                "subId", 5));
        String result = subHistoryEvaluator.getLastValidSubscriptionId();
        assertEquals("subId", result);
    }

    @Test
    public void testGetLastValidSubscriptionId_ChangedWhenActive()
            throws Exception {
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(
                newSubscriptionHistory(2L, SubscriptionStatus.ACTIVE,
                        ModificationType.MODIFY, "subIdChanged", 15),
                newSubscriptionHistory(1L, SubscriptionStatus.ACTIVE,
                        ModificationType.MODIFY, "subId", 5));
        String result = subHistoryEvaluator.getLastValidSubscriptionId();
        assertEquals("subIdChanged", result);
    }

    @Test
    public void testGetLastValidSubscriptionId_Deactivated() throws Exception {
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(
                newSubscriptionHistory(2L, SubscriptionStatus.DEACTIVATED,
                        ModificationType.MODIFY, "1364193900000", 15),
                newSubscriptionHistory(1L, SubscriptionStatus.ACTIVE,
                        ModificationType.MODIFY, "subId", 5));
        String result = subHistoryEvaluator.getLastValidSubscriptionId();
        assertEquals("subId", result);
    }

    @Test
    public void testGetLastValidSubscriptionId_ExpiredDeactivated()
            throws Exception {
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(
                newSubscriptionHistory(2L, SubscriptionStatus.DEACTIVATED,
                        ModificationType.MODIFY, "1364193900000", 15),
                newSubscriptionHistory(1L, SubscriptionStatus.EXPIRED,
                        ModificationType.MODIFY, "subId", 5));
        String result = subHistoryEvaluator.getLastValidSubscriptionId();
        assertEquals("subId", result);
    }

    @Test
    public void hasOnlyDeactivatedHistory_ActDeact() throws Exception {
        // given
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(
                newSubscriptionHistory(2L, SubscriptionStatus.DEACTIVATED,
                        ModificationType.MODIFY, "1364193900000", 25),
                newSubscriptionHistory(1L, SubscriptionStatus.ACTIVE,
                        ModificationType.MODIFY, "subId", 5));

        // when, then
        assertFalse(subHistoryEvaluator.hasOnlyDeactivatedHistory());
    }

    @Test
    public void hasOnlyDeactivatedHistory_Act() throws Exception {
        // given
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(newSubscriptionHistory(
                1L, SubscriptionStatus.ACTIVE, ModificationType.MODIFY,
                "subId", 5));

        // when, then
        assertFalse(subHistoryEvaluator.hasOnlyDeactivatedHistory());
    }

    @Test
    public void hasOnlyDeactivatedHistory_Deact() throws Exception {
        // given
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(newSubscriptionHistory(
                1L, SubscriptionStatus.DEACTIVATED, ModificationType.MODIFY,
                "1364193900000", 5));

        // when, then
        assertTrue(subHistoryEvaluator.hasOnlyDeactivatedHistory());
    }

    @Test
    public void hasOnlyDeactivatedHistory_Expired() throws Exception {
        // given
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(newSubscriptionHistory(
                1L, SubscriptionStatus.EXPIRED, ModificationType.MODIFY,
                "subId", 5));

        // when, then
        assertTrue(subHistoryEvaluator.hasOnlyDeactivatedHistory());
    }

    @Test
    public void splitHistories() throws Exception {
        // given
        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator();
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 9L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 18:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        // when
        subHistoryEvaluator.splitHistories(histories);

        // then
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());
        assertTrue("First subscription must be the resumed part", subInputs
                .get(0).isResumedSubscription());
        assertEquals("Resumed part has 4 history entries", 4, subInputs.get(0)
                .getHistories().size());
        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isResumedSubscription());
        assertEquals("Suspended part has 2 history entries", 2, subInputs
                .get(1).getHistories().size());
    }

    @Test
    public void evaluateHistories() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 10L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 9L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-15 12:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-14 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 20:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 18:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId1",
                DateTimeHandling.calculateMillis("2013-03-07 15:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId2",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());
        assertTrue("First subscription must be the resumed part", subInputs
                .get(0).isResumedSubscription());
        assertEquals("Resumed part has 4 history entries", 4, subInputs.get(0)
                .getHistories().size());
        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isResumedSubscription());
        assertEquals("Suspended part has 2 history entries", 2, subInputs
                .get(1).getHistories().size());
    }

    @Test
    public void evaluateHistories_withPurchaseOrderNumber() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                "My Subscription 2 purchase order number",
                DateTimeHandling.calculateMillis("2013-03-15 12:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2", "My Subscription 2",
                DateTimeHandling.calculateMillis("2013-03-14 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId", "My Subscription",
                DateTimeHandling.calculateMillis("2013-03-11 20:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId", "My Subscription",
                DateTimeHandling.calculateMillis("2013-03-11 18:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId1", "My Subscription",
                DateTimeHandling.calculateMillis("2013-03-07 15:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId", "My Subscription",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId", "My Subscription",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId", "My Subscription",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        assertTrue("First subscription must be the resumed part", subInputs
                .get(0).isResumedSubscription());
        assertEquals("Wrong subscription ID", "SubId2", subInputs.get(0)
                .getSubscriptionId());
        assertEquals("Wrong purchase order number",
                "My Subscription 2 purchase order number", subInputs.get(0)
                        .getPurchaseOrderNumber());
        assertEquals("Resumed part has 4 history entries", 2, subInputs.get(0)
                .getHistories().size());

        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isResumedSubscription());
        assertEquals("Wrong subscription ID", "SubId2", subInputs.get(1)
                .getSubscriptionId());
        assertEquals("Wrong purchase order number",
                "My Subscription 2 purchase order number", subInputs.get(1)
                        .getPurchaseOrderNumber());
        assertEquals("Suspended part has 2 history entries", 2, subInputs
                .get(1).getHistories().size());
    }

    @Test
    public void evaluateHistories_expireSuspendedSubscription()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-08-30 15:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-08-13 10:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-08-12 13:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());
        assertFalse("First subscription must be the upgraded part", subInputs
                .get(0).isResumedSubscription());
        assertTrue("First subscription must be the upgraded part", subInputs
                .get(0).isUpgradedAfterExpiryOrSuspend());
        assertEquals("Upgraded part has 2 history entries", 2, subInputs.get(0)
                .getHistories().size());
        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isResumedSubscription());
        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isUpgradedAfterExpiryOrSuspend());
        assertEquals("Suspended part has 2 history entries", 2, subInputs
                .get(1).getHistories().size());
    }

    @Test
    public void evaluateHistories_upgradeSuspendedSubscription()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-08 00:00:00"),
                PRODUCT_KEY_FREE_OF_CHARGE);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-05 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());
        assertFalse("First subscription must be the upgraded part", subInputs
                .get(0).isResumedSubscription());
        assertTrue("First subscription must be the upgraded part", subInputs
                .get(0).isUpgradedAfterExpiryOrSuspend());
        assertEquals("Upgraded part has 2 history entries", 2, subInputs.get(0)
                .getHistories().size());
        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isResumedSubscription());
        assertFalse("Second subscription must be the suspended part", subInputs
                .get(1).isUpgradedAfterExpiryOrSuspend());
        assertEquals("Suspended part has 2 history entries", 2, subInputs
                .get(1).getHistories().size());
    }

    @Test
    public void evaluateHistories_upgradeSuspendedSubscription_PerUnitToPerUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-04-28 14:28:22"),
                PRODUCT_KEY_PER_UNIT2);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-04-28 14:27:56"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-04-28 14:25:56"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2014-04-28 14:25:56"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("First subscription must be the upgraded part",
                subInput.isResumedSubscription());
        assertTrue("First subscription must be the upgraded part",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertEquals("Upgraded part has 1 history entry", 1, subInput
                .getHistories().size());
        assertSubHistoryKeys(subInput, 4);

        subInput = subInputs.get(1);
        assertFalse("Second subscription must be the suspended part",
                subInput.isResumedSubscription());
        assertFalse("Second subscription must be the suspended part",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertEquals("Suspended part has 2 history entries", 2, subInput
                .getHistories().size());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_Pending_Upd() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());
        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 0);
    }

    @Test
    public void evaluateHistories_SuspendedActive() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 01:35:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:15:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2);
    }

    @Test
    public void evaluateHistories_PendingSuspendedActive() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 01:35:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:15:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:03:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 4);
    }

    @Test
    public void evaluateHistories_PendingSuspendedDeactivated()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 4L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-01 01:35:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:15:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:03:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertFalse("No relevant histories expected",
                subHistoryEvaluator.evaluateHistories());
    }

    @Test
    public void evaluateHistories_Suspended_Upd() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 0L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when, then
        assertFalse("No relevant histories expected",
                subHistoryEvaluator.evaluateHistories());
    }

    @Test
    public void evaluateHistories_PendingInvalid() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 2L, SubscriptionStatus.INVALID,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-01 00:15:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-01 00:03:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when, then
        assertFalse("No relevant histories expected",
                subHistoryEvaluator.evaluateHistories());
    }

    @Test
    public void evaluateHistories_AsyncUpdate() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:10:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());
        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateAndSuspend() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:03:45"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateAndSuspend_perUnit()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 00:18:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 00:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 00:02:45"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 00:01:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-03 23:58:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateSuspendUpdateDeact()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-10 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:03:45"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateAsyncSuspendFinishUpdate()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 13L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 15:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 12L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 14:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 11L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 10L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:28:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 9L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:24:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:20:45"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:03:45"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:01:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 02:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 01:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 11);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateAsyncSuspendFinishUpdate_perUnit()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 13L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-18 15:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 12L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-18 14:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 11L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 22:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 10L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:28:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 9L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:24:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:20:45"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:03:45"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:02:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:01:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 02:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 01:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateAsyncSuspendFinishUpdateDeact()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 11L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-10 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 10L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:28:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 9L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:24:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:20:45"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:03:45"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:01:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 02:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 01:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscriptions expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateDeact() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-10 10:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:10:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());
        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateSuspendDeact() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-10 10:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-02 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());
        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 4, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpgrade() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:10:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());
        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not suspended or expired",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5, 2);
    }

    @Test
    public void evaluateHistories_SuspendDuringAsyncUpgrade() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:10:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:06:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 8, 6);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 4, 1);
    }

    @Test
    public void evaluateHistories_SuspendDuringAsyncUpgrade_SusAfterUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_SuspendDuringAsyncUpgrade_SusAfterUpgrade2()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_SuspendDuringAsyncUpgrade_SusDeactAfterUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_SuspendWhenFinishingAsyncUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5, 1);
    }

    @Test
    public void evaluateHistories_SuspendWhenFinishingAsyncUpgradeToPerUnit()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5, 1);
    }

    @Test
    public void evaluateHistories_SuspendAndDeactWhenFinishingAsyncUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5, 1);
    }

    @Test
    public void evaluateHistories_SuspendWhenFinishingAsyncUpgrade_SusResDuringUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 01:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 00:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 00:01:30"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 23:52:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 23:51:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 23:50:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Three subscriptions expected", 3, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 5);

        subInput = subInputs.get(2);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_SuspendWhenFinishingAsyncUpgrade_SusResDuringUpgrade2()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:30"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 1);
    }

    @Test
    public void evaluateHistories_SuspendWhenFinishingAsyncUpgradeToPerUnit_SusResDuringUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:30"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Three subscriptions expected", 3, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7);

        subInput = subInputs.get(1);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 5);

        subInput = subInputs.get(2);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_SuspendAndDeactWhenFinishingAsyncUpgrade_SusResDuringUpgrade()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-09 08:25:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:30"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:04:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpdateSuspendUpgrade() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 08:03:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-11 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:10:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:03:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 8);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeAndSuspendResume()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 10:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 09:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:02:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 07:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-04 10:30:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId2",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 8);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeToPerUnitAndSuspendResume()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 10:25:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 09:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 07:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-04 10:30:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId2",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 8);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeAndSuspendDeact()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 10:25:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 7L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 09:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:02:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-09 07:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId2",
                DateTimeHandling.calculateMillis("2013-03-04 10:30:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId2",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 3, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpgradeSuspendFinishUpgradeToAct()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:02:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 4, 1);
    }

    @Test
    public void evaluateHistories_AsyncUpgradeSuspendFinishUpgradeToActDeact()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-19 07:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:13:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:02:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 4, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeFinishUpgradeToAct()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:23:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:15:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:10:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:01:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 6);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncExpiredUpgrade() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 4L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 14:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after expiration",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 4);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncExpiredDeact() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 4L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 14:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncExpiredSuspended() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 14:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after expiration",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncExpiredSuspended_PerUnit()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 14:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 13:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertTrue("Subscription was upgraded after expiration",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncExpiredSuspendedDeact() throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 14:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeResumeBeforeFinishUpgrade_RataRata()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PRO_RATA2);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:07:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:06:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:01:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeResumeBeforeFinishUpgrade_RataUnit()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:07:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:06:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:01:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("Two subscriptions expected", 2, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertTrue("Subscription was resumed", subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded after suspend",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7, 5);

        subInput = subInputs.get(1);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 2, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeResumeBeforeFinishUpgrade_UnitRata()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:07:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:06:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:01:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7, 1);
    }

    @Test
    public void evaluateHistories_AsyncSuspendUpgradeResumeBeforeFinishUpgrade_UnitUnit()
            throws Exception {

        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 7L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:14:00"),
                PRODUCT_KEY_PER_UNIT2);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:07:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:06:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 4L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:05:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:01:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-09 08:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 0L, SubscriptionStatus.PENDING,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-04 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionHistoryEvaluator subHistoryEvaluator = newSubHistoryEvaluator(histories);

        // when
        subHistoryEvaluator.evaluateHistories();

        // then
        assertEquals("Wrong subscription ID", "SubId",
                subHistoryEvaluator.getLastValidSubscriptionId());
        List<SubscriptionInput> subInputs = subHistoryEvaluator
                .getSubscriptions();
        assertEquals("One subscription expected", 1, subInputs.size());

        SubscriptionInput subInput = subInputs.get(0);
        assertFalse("Subscription was not resumed",
                subInput.isResumedSubscription());
        assertFalse("Subscription was not upgraded",
                subInput.isUpgradedAfterExpiryOrSuspend());
        assertSubHistoryKeys(subInput, 7, 1);
    }

    private SubscriptionHistoryEvaluator newSubHistoryEvaluator(
            List<SubscriptionHistory> subscriptionHistoryEntries) {
        return new SubscriptionHistoryEvaluator(
                BillingInputFactory.newBillingInput(subscriptionHistoryEntries),
                bdr);
    }

    private SubscriptionHistoryEvaluator newSubHistoryEvaluator(
            SubscriptionHistory... subscriptionHistories) {
        List<SubscriptionHistory> subscriptionHistoryEntries = new ArrayList<SubscriptionHistory>();
        for (SubscriptionHistory subHistory : subscriptionHistories) {
            subscriptionHistoryEntries.add(subHistory);
        }

        return new SubscriptionHistoryEvaluator(
                BillingInputFactory.newBillingInput(subscriptionHistoryEntries),
                bdr);
    }

    private SubscriptionHistoryEvaluator newSubHistoryEvaluator() {
        return new SubscriptionHistoryEvaluator(null, bdr);
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            long subObjKey, long productObjKey, SubscriptionStatus status,
            Date moddate) {
        return newSubscriptionHistory(historyKey, subObjKey, productObjKey,
                status, moddate, null);
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            SubscriptionStatus status, Date modDate) {
        SubscriptionHistory sh = new SubscriptionHistory();
        sh.setKey(historyKey);
        sh.getDataContainer().setStatus(status);
        sh.setModdate(modDate);
        sh.setUserGroupObjKey(Long.valueOf(100L));
        return sh;
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            long subObjKey, long productObjKey, SubscriptionStatus status,
            Date modDate, Date activationDate) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                modDate);
        sh.setObjKey(subObjKey);
        sh.setProductObjKey(productObjKey);
        sh.setModtype(ModificationType.MODIFY);
        if (activationDate != null)
            sh.getDataContainer().setActivationDate(
                    Long.valueOf(activationDate.getTime()));
        return sh;
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            SubscriptionStatus status, ModificationType modType, String subId,
            long modDate) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                new Date(modDate));
        sh.setModtype(modType);
        sh.getDataContainer().setSubscriptionId(subId);
        return sh;
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            SubscriptionStatus status, ModificationType modType, String subId,
            long modDate, long productObjKey) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                modType, subId, modDate);
        sh.setProductObjKey(productObjKey);
        return sh;
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            SubscriptionStatus status, ModificationType modType, String subId,
            String purchaseOrderNumber, long modDate, long productObjKey) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                modType, subId, modDate);
        sh.setProductObjKey(productObjKey);
        sh.getDataContainer().setPurchaseOrderNumber(purchaseOrderNumber);
        return sh;
    }

    private void addSubscriptionHistory(List<SubscriptionHistory> histories,
            long historyKey, SubscriptionStatus status,
            ModificationType modType, String subId, long modDate,
            long productObjKey) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                modType, subId, modDate, productObjKey);
        histories.add(sh);
    }

    private void addSubscriptionHistory(List<SubscriptionHistory> histories,
            long historyKey, SubscriptionStatus status,
            ModificationType modType, String subId, String purchaseOrderNumber,
            long modDate, long productObjKey) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                modType, subId, purchaseOrderNumber, modDate, productObjKey);
        histories.add(sh);
    }

    private PriceModelHistory newPriceModelHistory(PriceModelType type) {
        PriceModelHistory priceModelHistory = new PriceModelHistory();
        priceModelHistory.setObjKey(1);
        priceModelHistory.getDataContainer().setType(type);
        if (type != PriceModelType.FREE_OF_CHARGE) {
            priceModelHistory.getDataContainer().setPeriod(PricingPeriod.WEEK);
        }
        return priceModelHistory;
    }

    private void assertSubHistoryKeys(SubscriptionInput subInput, long... keys) {
        List<SubscriptionHistory> subHistories = subInput.getHistories();
        assertEquals("Wrong number of subscription histories", keys.length,
                subHistories.size());

        for (int i = 0; i < subHistories.size(); i++) {
            assertEquals("Wrong sub history key", keys[i], subHistories.get(i)
                    .getKey());
        }
    }

}
