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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.calculation.revenue.model.SubscriptionInput;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.ProductHistory;
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
public class PriceModelEvaluatorTest {

    private static final long PRODUCT_KEY_FREE_OF_CHARGE = 4711L;
    private static final long PRODUCT_KEY_PRO_RATA = 4712L;
    private static final long PRODUCT_KEY_PER_UNIT = 4713L;
    private static final long PRODUCT_KEY_PER_UNIT2 = 4714L;
    private static final long PRICEMODEL_KEY_FREE_OF_CHARGE = 13L;
    private static final long PRICEMODEL_KEY_PRO_RATA = 14L;
    private static final long PRICEMODEL_KEY_PER_UNIT = 15L;
    private static final long PRICEMODEL_KEY_PER_UNIT2 = 16L;
    private static final String PRODUCT_ID = "MyService";

    private BillingDataRetrievalServiceLocal bdr;

    private PriceModelHistory pmhFreeOfCharge;
    private PriceModelHistory pmhProRata;
    private PriceModelHistory pmhPerUnit;
    private PriceModelHistory pmhPerUnit2;

    @SuppressWarnings("boxing")
    @Before
    public void setup() throws Exception {
        bdr = mock(BillingDataRetrievalServiceBean.class);

        pmhFreeOfCharge = newPriceModelHistory(PriceModelType.FREE_OF_CHARGE);
        when(
                bdr.loadOldestPriceModelHistory(
                        eq(PRICEMODEL_KEY_FREE_OF_CHARGE), anyLong()))
                .thenReturn(pmhFreeOfCharge);
        when(
                bdr.loadPriceModelKeyForSubscriptionHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_FREE_OF_CHARGE;
                    }
                }))).thenReturn(PRICEMODEL_KEY_FREE_OF_CHARGE);

        pmhProRata = newPriceModelHistory(PriceModelType.PRO_RATA);
        when(
                bdr.loadOldestPriceModelHistory(eq(PRICEMODEL_KEY_PRO_RATA),
                        anyLong())).thenReturn(pmhProRata);
        when(
                bdr.loadPriceModelKeyForSubscriptionHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PRO_RATA;
                    }
                }))).thenReturn(PRICEMODEL_KEY_PRO_RATA);

        pmhPerUnit = newPriceModelHistory(PriceModelType.PER_UNIT);
        when(
                bdr.loadOldestPriceModelHistory(eq(PRICEMODEL_KEY_PER_UNIT),
                        anyLong())).thenReturn(pmhPerUnit);
        when(
                bdr.loadPriceModelKeyForSubscriptionHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PER_UNIT;
                    }
                }))).thenReturn(PRICEMODEL_KEY_PER_UNIT);

        pmhPerUnit2 = newPriceModelHistory(PriceModelType.PER_UNIT);
        when(
                bdr.loadOldestPriceModelHistory(eq(PRICEMODEL_KEY_PER_UNIT2),
                        anyLong())).thenReturn(pmhPerUnit2);
        when(
                bdr.loadPriceModelKeyForSubscriptionHistory(argThat(new ArgumentMatcher<SubscriptionHistory>() {

                    @Override
                    public boolean matches(Object argument) {
                        SubscriptionHistory sh = (SubscriptionHistory) argument;
                        return sh != null
                                && sh.getProductObjKey() == PRODUCT_KEY_PER_UNIT2;
                    }
                }))).thenReturn(PRICEMODEL_KEY_PER_UNIT2);

        when(bdr.loadPriceModelStartDate(anyLong())).thenReturn(new Date(0));

        ProductHistory ph = new ProductHistory();
        ph.getDataContainer().setProductId(PRODUCT_ID);
        when(
                bdr.loadProductTemplateHistoryForSubscriptionHistory(
                        any(SubscriptionHistory.class), anyLong())).thenReturn(
                ph);
    }

    @Test
    public void evaluatePriceModels_Suspended() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_OnlySuspended() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertFalse("No relevant price models expected",
                pmEvaluator.subscriptionHasRelevantPriceModels());
    }

    @Test
    public void evaluatePriceModels_SuspendedAndUpgraded() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_SuspendedUpd_ProRata() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_SuspendedUpd_PerUnit() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 3L, SubscriptionStatus.SUSPENDED_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-20 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_Expired() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 3L, SubscriptionStatus.EXPIRED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.ADD, "SubId",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-03 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_ResumedUpgrades() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 9L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
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

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("Three price models expected", 3, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertFalse(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_FREE_OF_CHARGE,
                priceModels.get(1).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                priceModels.get(1).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00"),
                priceModels.get(1).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(1).isResumedPriceModel());
        assertFalse(priceModels.get(1).isYoungestPriceModelOfPeriod());
        assertFalse(priceModels.get(1).isOldestPriceModelOfPeriod());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(2).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-07 18:00:00"),
                priceModels.get(2).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                priceModels.get(2).getPriceModelPeriodEnd());
        assertTrue(priceModels.get(2).isResumedPriceModel());
        assertTrue(priceModels.get(2).isYoungestPriceModelOfPeriod());
        assertFalse(priceModels.get(2).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_NoRelevantPriceModel() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 9L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "1364193900000",
                DateTimeHandling.calculateMillis("2013-03-25 07:45:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
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

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertFalse("No price model expected",
                pmEvaluator.subscriptionHasRelevantPriceModels());
    }

    @Test
    public void evaluatePriceModels_Active() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-27 23:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
        assertEquals("Wrong product ID", PRODUCT_ID, priceModels.get(0)
                .getProductId());
    }

    @Test
    public void evaluatePriceModels_PendingUpd() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-27 23:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_ResumedPendingUpd() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-15 23:00:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-15 23:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertTrue(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_PendingUpdDeact() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 6L, SubscriptionStatus.DEACTIVATED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:10:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 5L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());
        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-10 10:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-10 10:10:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_Upgrade_PerUnit() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-27 12:10:00"),
                PRODUCT_KEY_PER_UNIT2);
        addSubscriptionHistory(histories, 2L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-20 10:00:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("Two price models expected", 2, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT2,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-02-27 12:10:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertFalse(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(1).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                priceModels.get(1).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-02-27 12:10:00"),
                priceModels.get(1).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(1).isResumedPriceModel());
        assertTrue(priceModels.get(1).isYoungestPriceModelOfPeriod());
        assertFalse(priceModels.get(1).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_ResumedAsyncUpgrade() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-01 00:12:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-28 23:59:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("Two price models expected", 2, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-01 00:12:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertFalse(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(1).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                priceModels.get(1).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-01 00:12:00"),
                priceModels.get(1).getPriceModelPeriodEnd());
        assertTrue(priceModels.get(1).isResumedPriceModel());
        assertTrue(priceModels.get(1).isYoungestPriceModelOfPeriod());
        assertFalse(priceModels.get(1).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_ResumedAsyncUpgrade_PerUnit()
            throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 8L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-22 00:12:00"),
                PRODUCT_KEY_PER_UNIT2);
        addSubscriptionHistory(histories, 6L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-20 23:59:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT2,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertFalse(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_AsyncUpgradeSuspend() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-13 10:12:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-10 22:59:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model history", pmhProRata, priceModels
                .get(0).getPriceModelHistory());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-13 10:12:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_AsyncUpgradeSuspend2() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-13 10:12:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.ACTIVE,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-10 22:59:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, false, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model history", pmhPerUnit, priceModels
                .get(0).getPriceModelHistory());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-13 10:12:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertFalse(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_AsyncUpgradeSuspend3() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-03-13 10:12:00"),
                PRODUCT_KEY_PER_UNIT);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-10 22:59:00"),
                PRODUCT_KEY_PRO_RATA);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PRO_RATA,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model history", pmhProRata, priceModels
                .get(0).getPriceModelHistory());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-03-13 10:12:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertTrue(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_AsyncUpgradeSuspend4() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-27 10:12:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-10 22:59:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertTrue(pmEvaluator.subscriptionHasRelevantPriceModels());
        List<PriceModelInput> priceModels = pmEvaluator.getPriceModels();
        assertEquals("One price model expected", 1, priceModels.size());

        assertEquals("Wrong price model key", PRICEMODEL_KEY_PER_UNIT,
                priceModels.get(0).getPriceModelKey());
        assertEquals("Wrong price model history", pmhPerUnit, priceModels
                .get(0).getPriceModelHistory());
        assertEquals("Wrong price model start time",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                priceModels.get(0).getPriceModelPeriodStart());
        assertEquals("Wrong price model end time",
                DateTimeHandling.calculateMillis("2013-02-27 10:12:00"),
                priceModels.get(0).getPriceModelPeriodEnd());
        assertTrue(priceModels.get(0).isResumedPriceModel());
        assertTrue(priceModels.get(0).isYoungestPriceModelOfPeriod());
        assertTrue(priceModels.get(0).isOldestPriceModelOfPeriod());
    }

    @Test
    public void evaluatePriceModels_AsyncUpgradeSuspend5() throws Exception {
        // given
        List<SubscriptionHistory> histories = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(histories, 5L, SubscriptionStatus.SUSPENDED,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-24 23:12:00"),
                PRODUCT_KEY_PRO_RATA);
        addSubscriptionHistory(histories, 1L, SubscriptionStatus.PENDING_UPD,
                ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2013-02-10 22:59:00"),
                PRODUCT_KEY_PER_UNIT);

        SubscriptionInput subscriptionInput = newSubscriptionInput("SubId",
                histories, true, false);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                subscriptionInput,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        pmEvaluator.evaluatePriceModels();

        // then
        assertFalse("No relevant price models expected",
                pmEvaluator.subscriptionHasRelevantPriceModels());
    }

    @Test
    public void determineFreePeriodEnd() throws Exception {

        // given
        when(bdr.loadPriceModelStartDate(anyLong())).thenReturn(
                DateTimeHandling.calculateDate("2013-04-01 00:00:00"));

        PriceModelHistory pmHistory = newPriceModelHistory(
                PriceModelType.PRO_RATA, 7);

        // when
        long freePeriodEnd = newPriceModelEvaluator().determineFreePeriodEnd(
                pmHistory);

        assertEquals("Wrong free period end",
                DateTimeHandling.calculateDate("2013-04-08 00:00:00"),
                new Date(freePeriodEnd));
    }

    @Test
    public void determineFreePeriodEnd_SummerTimeChange() throws Exception {

        // given
        when(bdr.loadPriceModelStartDate(anyLong())).thenReturn(
                DateTimeHandling.calculateDate("2013-03-25 00:00:00"));

        PriceModelHistory pmHistory = newPriceModelHistory(
                PriceModelType.PRO_RATA, 10);

        // when
        long freePeriodEnd = newPriceModelEvaluator().determineFreePeriodEnd(
                pmHistory);

        assertEquals("Wrong free period end",
                DateTimeHandling.calculateDate("2013-04-04 00:00:00"),
                new Date(freePeriodEnd));
    }

    @Test
    public void determineFreePeriodEnd_WinterTimeChange() throws Exception {

        // given
        when(bdr.loadPriceModelStartDate(anyLong())).thenReturn(
                DateTimeHandling.calculateDate("2013-10-25 00:00:00"));

        PriceModelHistory pmHistory = newPriceModelHistory(
                PriceModelType.PRO_RATA, 10);

        // when
        long freePeriodEnd = newPriceModelEvaluator().determineFreePeriodEnd(
                pmHistory);

        assertEquals("Wrong free period end",
                DateTimeHandling.calculateDate("2013-11-04 00:00:00"),
                new Date(freePeriodEnd));
    }

    @Test
    public void adjustBillingPeriodStart_Free() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.FREE_OF_CHARGE, null);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));

        // when
        long adjustedBPStart = pmEvaluator.adjustBillingPeriodStart(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period start",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                adjustedBPStart);
    }

    @Test
    public void adjustBillingPeriodStart_ProRataWeek() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.PRO_RATA, PricingPeriod.WEEK);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));

        // when
        long adjustedBPStart = pmEvaluator.adjustBillingPeriodStart(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period start",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                adjustedBPStart);
    }

    @Test
    public void adjustBillingPeriodStart_PUWeek() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.PER_UNIT, PricingPeriod.WEEK);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));

        // when
        long adjustedBPStart = pmEvaluator.adjustBillingPeriodStart(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period start",
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"),
                adjustedBPStart);
    }

    @Test
    public void adjustBillingPeriodStart_PUMonth() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.PER_UNIT, PricingPeriod.MONTH);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // when
        long adjustedBPStart = pmEvaluator.adjustBillingPeriodStart(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period start",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                adjustedBPStart);
    }

    @Test
    public void adjustBillingPeriodEnd_Free() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.FREE_OF_CHARGE, null);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));

        // when
        long adjustedBPEnd = pmEvaluator.adjustBillingPeriodEnd(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period end",
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"),
                adjustedBPEnd);
    }

    @Test
    public void adjustBillingPeriodEnd_ProRataWeek() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.PRO_RATA, PricingPeriod.WEEK);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));

        // when
        long adjustedBPEnd = pmEvaluator.adjustBillingPeriodEnd(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period end",
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"),
                adjustedBPEnd);
    }

    @Test
    public void adjustBillingPeriodEnd_PUWeek() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.PER_UNIT, PricingPeriod.WEEK);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));

        // when
        long adjustedBPEnd = pmEvaluator.adjustBillingPeriodEnd(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period end",
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"),
                adjustedBPEnd);
    }

    @Test
    public void adjustBillingPeriodEnd_PUMonth() throws Exception {
        // given
        PriceModelHistory pmhPerUnit = newPriceModelHistory(
                PriceModelType.PER_UNIT, PricingPeriod.MONTH);

        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));

        // when
        long adjustedBPEnd = pmEvaluator.adjustBillingPeriodEnd(pmhPerUnit);

        // then
        assertEquals("Wrong adjusted billing period end",
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"),
                adjustedBPEnd);
    }

    @Test
    public void priceModelIsFree_notFree() throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));

        // when
        boolean priceModelIsFree = pmEvaluator.priceModelIsFree(
                DateTimeHandling.calculateMillis("2013-07-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 13:00:00"));

        // then
        assertFalse("Price model should not be free", priceModelIsFree);
    }

    @Test
    public void priceModelIsFree_freepEndsAfterBP() throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));

        // when
        boolean priceModelIsFree = pmEvaluator.priceModelIsFree(
                DateTimeHandling.calculateMillis("2013-09-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-15 13:00:00"));

        // then
        assertTrue("Price model should be free", priceModelIsFree);
    }

    @Test
    public void priceModelIsFree_freepEndsAfterPMEnd() throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));

        // when
        boolean priceModelIsFree = pmEvaluator.priceModelIsFree(
                DateTimeHandling.calculateMillis("2013-08-15 01:30:00"),
                DateTimeHandling.calculateMillis("2013-08-25 08:08:08"));

        // then
        assertTrue("Price model should be free", priceModelIsFree);
    }

    @Test
    public void priceModelIsFree_freepEndsAfterBPAndPMEnd() throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(null,
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));

        // when
        boolean priceModelIsFree = pmEvaluator.priceModelIsFree(
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-15 13:00:00"));

        // then
        assertTrue("Price model should be free", priceModelIsFree);
    }

    @Test
    public void adjustPriceModelStartTimeToFreePeriod_freePriceModel()
            throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator();

        // when
        long priceModelStart = DateTimeHandling
                .calculateMillis("2013-08-02 01:30:00");
        long priceModelEnd = DateTimeHandling
                .calculateMillis("2013-08-15 00:00:00");
        long freePeriodEnd = DateTimeHandling
                .calculateMillis("2013-08-22 01:30:00");
        long pmStartAdjustedToFreePeriod = pmEvaluator
                .adjustPriceModelStartTimeToFreePeriod(priceModelStart,
                        priceModelEnd, freePeriodEnd);

        // then
        assertEquals(
                "Price model is free, so adjusted start should be price model end",
                priceModelEnd, pmStartAdjustedToFreePeriod);
    }

    @Test
    public void adjustPriceModelStartTimeToFreePeriod_freePeriodEndsAfterPMStart()
            throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator();

        // when
        long priceModelStart = DateTimeHandling
                .calculateMillis("2013-08-02 01:30:00");
        long priceModelEnd = DateTimeHandling
                .calculateMillis("2013-08-15 00:00:00");
        long freePeriodEnd = DateTimeHandling
                .calculateMillis("2013-08-13 05:55:55");
        long pmStartAdjustedToFreePeriod = pmEvaluator
                .adjustPriceModelStartTimeToFreePeriod(priceModelStart,
                        priceModelEnd, freePeriodEnd);

        // then
        assertEquals(
                "Adjusted price model should be the end of the free period",
                freePeriodEnd, pmStartAdjustedToFreePeriod);
    }

    @Test
    public void adjustPriceModelStartTimeToFreePeriod_freePeriodEndsBeforePMStart()
            throws Exception {
        // given
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator();

        // when
        long priceModelStart = DateTimeHandling
                .calculateMillis("2013-08-12 01:30:00");
        long priceModelEnd = DateTimeHandling
                .calculateMillis("2013-08-25 00:00:00");
        long freePeriodEnd = DateTimeHandling
                .calculateMillis("2013-08-03 08:08:13");
        long pmStartAdjustedToFreePeriod = pmEvaluator
                .adjustPriceModelStartTimeToFreePeriod(priceModelStart,
                        priceModelEnd, freePeriodEnd);

        // then
        assertEquals("Adjusted price model should be price model start",
                priceModelStart, pmStartAdjustedToFreePeriod);
    }

    @Test
    public void isOneTimeFeeCharged_free() throws Exception {
        // given
        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, true,
                DateTimeHandling.calculateMillis("2014-02-02 08:00:00"),
                DateTimeHandling.calculateMillis("2014-02-28 01:00:00"),
                DateTimeHandling.calculateMillis("2014-02-13 08:08:08"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_noFP() throws Exception {
        // given
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                null);

        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-15 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, false,
                DateTimeHandling.calculateMillis("2014-02-15 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-15 13:00:00"));

        // then
        assertTrue("One time fee must be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_noFP_async() throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(3L,
                SubscriptionStatus.PENDING, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 10:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 10:20:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-10 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-10 10:20:00"));

        // then
        assertTrue("One time fee must be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_fpEndsBetweenSusRes() throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(3L,
                SubscriptionStatus.SUSPENDED, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 23:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                previousHistory);

        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-15 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, false,
                DateTimeHandling.calculateMillis("2014-02-02 08:00:00"),
                DateTimeHandling.calculateMillis("2014-02-28 01:00:00"),
                DateTimeHandling.calculateMillis("2014-02-13 08:08:08"));

        // then
        assertTrue("One time fee must be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_fpEndsBetweenSusRes_noActDate()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(3L,
                SubscriptionStatus.SUSPENDED, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 23:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                previousHistory);

        // for coverage...
        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                null, PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, false,
                DateTimeHandling.calculateMillis("2014-02-02 08:00:00"),
                DateTimeHandling.calculateMillis("2014-02-28 01:00:00"),
                DateTimeHandling.calculateMillis("2014-02-13 08:08:08"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_fpEndsBetweenSusRes_ResAfterBP()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(3L,
                SubscriptionStatus.SUSPENDED, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 23:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                previousHistory);

        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-03-15 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, false,
                DateTimeHandling.calculateMillis("2014-02-02 08:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-13 08:08:08"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_fpEndsBetweenSusRes_overlap()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(3L,
                SubscriptionStatus.SUSPENDED_UPD, ModificationType.MODIFY,
                "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 23:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-25 13:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-25 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-13 08:08:08"));

        // then
        assertTrue("One time fee must be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_fpEndsBetweenSusRes_overlap_nextBP()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(3L,
                SubscriptionStatus.SUSPENDED_UPD, ModificationType.MODIFY,
                "SubId",
                DateTimeHandling.calculateMillis("2014-02-10 23:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        SubscriptionHistory nextActHistory = newSubscriptionHistory(4L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-25 13:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadNextActiveSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                nextActHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-04-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-25 13:00:00"),
                DateTimeHandling.calculateMillis("2014-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-13 08:08:08"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_FPEndsAfterDeactivation() throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(5L,
                SubscriptionStatus.DEACTIVATED, ModificationType.MODIFY,
                "SubId",
                DateTimeHandling.calculateMillis("2014-02-21 10:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        // this is not realistic because the price model is completely free;
        // however, the else case in the code should be tested for better
        // coverage...
        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-13 08:13:13"),
                DateTimeHandling.calculateMillis("2014-02-21 10:00:00"),
                DateTimeHandling.calculateMillis("2014-02-23 08:13:13"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_activeWhenfpEnds() throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(2L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-08 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                previousHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, false,
                DateTimeHandling.calculateMillis("2014-02-08 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-12 13:00:00"));

        // then
        assertTrue("One time fee must be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_active_fpEndsInNextBP() throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(2L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-25 13:00:00"),
                PRODUCT_KEY_PRO_RATA);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PRO_RATA), anyLong())).thenReturn(
                previousHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PRO_RATA, false,
                DateTimeHandling.calculateMillis("2014-02-25 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-07 13:00:00"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_activeWhenfpEnds_resumedPm()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(2L,
                SubscriptionStatus.PENDING_UPD, ModificationType.MODIFY,
                "SubId",
                DateTimeHandling.calculateMillis("2014-02-12 12:55:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-23 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-12 13:00:00"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_activeWhenfpEnds_overlapping()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(2L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-24 13:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-24 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-27 13:00:00"));

        // then
        assertTrue("One time fee must be charged", chargeOneTimeFee);
    }

    @Test
    public void isOneTimeFeeCharged_activeWhenfpEnds_overlapping_nextBP()
            throws Exception {
        // given
        SubscriptionHistory previousHistory = newSubscriptionHistory(2L,
                SubscriptionStatus.ACTIVE, ModificationType.MODIFY, "SubId",
                DateTimeHandling.calculateMillis("2014-02-24 13:00:00"),
                PRODUCT_KEY_PER_UNIT);
        when(
                bdr.loadPreviousSubscriptionHistoryForPriceModel(
                        eq(PRICEMODEL_KEY_PER_UNIT), anyLong())).thenReturn(
                previousHistory);

        // when
        PriceModelEvaluator pmEvaluator = newPriceModelEvaluator(
                DateTimeHandling.calculateMillis("2014-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2014-04-01 00:00:00"));

        boolean chargeOneTimeFee = pmEvaluator.isOneTimeFeeCharged(
                PRICEMODEL_KEY_PER_UNIT, false,
                DateTimeHandling.calculateMillis("2014-02-24 13:00:00"),
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2014-02-27 13:00:00"));

        // then
        assertFalse("One time fee must not be charged", chargeOneTimeFee);
    }

    private PriceModelEvaluator newPriceModelEvaluator(
            SubscriptionInput subscriptionInput, long billingPeriodStart,
            long billingPeriodEnd) {
        return new PriceModelEvaluator(newBillingInput(billingPeriodStart,
                billingPeriodEnd), bdr, subscriptionInput);
    }

    private PriceModelEvaluator newPriceModelEvaluator(long billingPeriodStart,
            long billingPeriodEnd) {
        return new PriceModelEvaluator(newBillingInput(billingPeriodStart,
                billingPeriodEnd), bdr, null);
    }

    private PriceModelEvaluator newPriceModelEvaluator() {
        return new PriceModelEvaluator(null, bdr, null);
    }

    private SubscriptionInput newSubscriptionInput(String subscriptionId,
            List<SubscriptionHistory> histories, boolean isResumedSubscription,
            boolean isUpgradedAfterExpiryOrSuspend) {
        SubscriptionInput.Builder subscrBuilder = new SubscriptionInput.Builder();
        subscrBuilder
                .setSubscriptionId(subscriptionId)
                .setHistories(histories)
                .setResumedSubscription(isResumedSubscription)
                .setUpgradedAfterExpiryOrSuspend(isUpgradedAfterExpiryOrSuspend);
        return subscrBuilder.build();
    }

    private BillingInput newBillingInput(long billingPeriodStart,
            long billingPeriodEnd) {
        BillingInput.Builder billingInputBuilder = new BillingInput.Builder();
        billingInputBuilder.setBillingPeriodStart(billingPeriodStart)
                .setBillingPeriodEnd(billingPeriodEnd);
        return billingInputBuilder.build();
    }

    private SubscriptionHistory newSubscriptionHistory(long historyKey,
            SubscriptionStatus status, Date modDate) {
        SubscriptionHistory sh = new SubscriptionHistory();
        sh.setKey(historyKey);
        sh.getDataContainer().setStatus(status);
        sh.setModdate(modDate);
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
            Date modDate, long productObjKey) {
        SubscriptionHistory sh = newSubscriptionHistory(historyKey, status,
                modDate);
        sh.setModtype(modType);
        sh.getDataContainer().setSubscriptionId(subId);
        sh.setProductObjKey(productObjKey);
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

    private PriceModelHistory newPriceModelHistory(PriceModelType type) {
        PriceModelHistory priceModelHistory = new PriceModelHistory();
        priceModelHistory.setObjKey(1);
        priceModelHistory.getDataContainer().setType(type);
        if (type != PriceModelType.FREE_OF_CHARGE) {
            priceModelHistory.getDataContainer().setPeriod(PricingPeriod.WEEK);
        }
        return priceModelHistory;
    }

    private PriceModelHistory newPriceModelHistory(PriceModelType type,
            PricingPeriod pricingPeriod) {
        PriceModelHistory priceModelHistory = new PriceModelHistory();
        priceModelHistory.setObjKey(1);
        priceModelHistory.getDataContainer().setType(type);
        if (type != PriceModelType.FREE_OF_CHARGE) {
            priceModelHistory.getDataContainer().setPeriod(pricingPeriod);
        }
        return priceModelHistory;
    }

    private PriceModelHistory newPriceModelHistory(PriceModelType type,
            int freePeriod) {
        PriceModelHistory priceModelHistory = newPriceModelHistory(type);
        priceModelHistory.setFreePeriod(freePeriod);
        return priceModelHistory;
    }
}
