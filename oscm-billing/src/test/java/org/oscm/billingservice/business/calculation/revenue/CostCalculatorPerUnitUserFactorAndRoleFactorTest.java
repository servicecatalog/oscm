/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.CostCalculatorPerUnit;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.UsageLicenseData;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitUserFactorAndRoleFactorTest {

    private static final String USER_ID_1 = "user1";
    private static final long USER_KEY_1 = 1;
    private static final String USER_ID_2 = "user2";
    private static final long USER_KEY_2 = 2;

    private static final PriceModelHistory PM_MONTH = createPriceModelHistory(PricingPeriod.MONTH);
    private static final PriceModelHistory PM_WEEK = createPriceModelHistory(PricingPeriod.WEEK);
    private static final PriceModelHistory PM_DAY = createPriceModelHistory(PricingPeriod.DAY);

    private static final Long ROLE_KEY_1 = Long.valueOf(91);
    private static final Long ROLE_KEY_2 = Long.valueOf(92);
    private static final Long ROLE_KEY_3 = Long.valueOf(93);

    private CostCalculatorPerUnit calculator;
    private List<UsageLicenseHistory> ulHistories;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
        ulHistories = new ArrayList<UsageLicenseHistory>();
    }

    @Test
    public void determineUserAssignmentsFactors_NullHistory() {
        // given

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        BillingInputFactory.newBillingInput(0, 0), 0, 0);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_EmptyHistory() {
        // given

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        BillingInputFactory.newBillingInput(0, 0), 0, 0);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_HistoryAfterBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-03-02 00:00:00"), null));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_HistoryAfterBillingPeriodForSecondUser() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.ADD));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_2, USER_ID_2,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-03-02 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserAddedBeforeBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserAddedInBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserAddedInLastWeek() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-02-28 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserModifiedBeforeBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserModifiedInBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserDeletedInFirstWeek() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserDeletedBeforeFirstWeek() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-28 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserDeletedBeforeFirstWeekAndMultipleUser() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-28 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_2, USER_ID_2, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_2));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserAssignedTwoTimesInOneUnit() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-02-29 00:00:00"),
                ModificationType.ADD));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-02-02 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(USER_ID_1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUserId());
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleUsers() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-02-02 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_2, USER_ID_2, null,
                DateTimeHandling.calculateDate("2012-02-02 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_2, USER_ID_2, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(2, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        result.getUserKeys().contains(Long.valueOf(USER_KEY_2));
        assertEquals(USER_ID_1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUserId());
        assertEquals(USER_ID_2,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_2))
                        .getUserId());
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleAddedBeforeBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleAddedInBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleAddedInLastWeek() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-28 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleModifiedBeforeBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleModifiedInBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);

        assertEquals(2, result.getRoleFactors().size());
        // From 15.02.2012 00:00 to 01.03.2012 00:00
        assertEquals(0.51724137, result.getRoleFactors().get(ROLE_KEY_1)
                .doubleValue(), 0.0000001);
        assertEquals(0.51724137,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);

        // From 01.02.2012 00:00 to 15.02.2012 00:00
        assertEquals(0.48275862, result.getRoleFactors().get(ROLE_KEY_2)
                .doubleValue(), 0.0000001);
        assertEquals(0.48275862,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleModifiedAndDeletedInBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-16 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-14 23:59:50"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-14 23:59:50"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);

        assertEquals(2, result.getRoleFactors().size());
        // From 15.02.2012 00:00 to 01.03.2012 00:00
        assertEquals(0.51724137, result.getRoleFactors().get(ROLE_KEY_1)
                .doubleValue(), 0.0000001);
        assertEquals(0.51724137,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);

        // From 01.02.2012 00:00 to 15.02.2012 00:00
        assertEquals(0.48275862, result.getRoleFactors().get(ROLE_KEY_2)
                .doubleValue(), 0.0000001);
        assertEquals(0.48275862,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleDeletedInFirstWeek() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleDeletedBeforeBillingPeriod() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-28 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0);
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleDeletedInBillingPeriod_MONTH() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    /**
     * Bug #10125
     */
    @Test
    public void determineUserAssignmentsFactors_UserRoleDeletedInBillingPeriod_1WEEK() {
        // given
        // the user with role ROLE_KEY_1 is assigned on 19.03 and de-assigned on
        // 21.03.2013
        // the UsageLicenseHistory entries are expected in descending order:
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-03-21 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-03-19 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-03-19 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(
                        ulHistories,
                        PM_WEEK,
                        BillingInputFactory.newBillingInput(
                                "2013-03-01 00:00:00", "2013-04-01 00:00:00"),
                        DateTimeHandling.calculateMillis("2013-03-18 00:00:00"),
                        DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        // then
        // one WEEK time unit must be calculated for user roles for
        // 18.03-24.03.2013
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    /**
     * Bug #10125
     */
    @Test
    public void determineUserAssignmentsFactors_UserRoleDeletedInBillingPeriod_3WEEKs() {
        // given
        // the user with role ROLE_KEY_1 is assigned on 11.04 and de-assigned on
        // 25.04.2013
        // the UsageLicenseHistory entries are expected in descending order:
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-04-25 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-04-11 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-04-11 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(
                        ulHistories,
                        PM_WEEK,
                        BillingInputFactory.newBillingInput(
                                "2013-04-01 00:00:00", "2013-05-01 00:00:00"),
                        DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                        DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));

        // then
        // 3 WEEK time units must be calculated for user roles for
        // 11.04-25.04.2013
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(3, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(3, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(3,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    /**
     * Bug #10125
     */
    @Test
    public void determineUserAssignmentsFactors_UserRoleDeletedInBillingPeriod_3WEEKs_onlyADD() {
        // given
        // the user with role ROLE_KEY_1 is assigned on 11.04 and de-assigned on
        // 25.04.2013
        // the UsageLicenseHistory entries are expected in descending order:
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2013-04-11 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(
                        ulHistories,
                        PM_WEEK,
                        BillingInputFactory.newBillingInput(
                                "2013-04-01 00:00:00", "2013-05-01 00:00:00"),
                        DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                        DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));

        // then
        // 3 WEEK time units must be calculated for user roles for
        // 11.04-25.04.2013
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(3, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(3, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(3,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleModifiedOnTimeUnitStart() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-06 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(4, result.getBasicFactor(), 0);
        assertEquals(2, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
        assertEquals(3, result.getRoleFactors().get(ROLE_KEY_2).doubleValue(),
                0);
        assertEquals(3,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleModifiedMultipleTimes() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-12 23:59:59"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_3,
                DateTimeHandling.calculateDate("2012-02-06 00:00:01"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-05 23:59:59"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(4, result.getBasicFactor(), 0);

        assertEquals(3, result.getRoleFactors().size());
        assertEquals(3, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0.0000001);
        assertEquals(3,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);

        assertEquals(0.00000330, result.getRoleFactors().get(ROLE_KEY_2)
                .doubleValue(), 0.0000001);
        assertEquals(0.00000330,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);

        assertEquals(0.99999669, result.getRoleFactors().get(ROLE_KEY_3)
                .doubleValue(), 0.0000001);
        assertEquals(0.99999669,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_3).getFactor(), 0.0000001);
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleRolesAndMultipleUsers() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_2, USER_ID_2,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_2, USER_ID_2,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(2, result.getNumberOfUsers());
        assertEquals(8, result.getBasicFactor(), 0);

        assertEquals(2, result.getRoleFactors().size());
        assertEquals(4.57142857, result.getRoleFactors().get(ROLE_KEY_1)
                .doubleValue(), 0.0000001);
        assertEquals(2.28571428,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);
        assertEquals(2.28571428,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_2))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);

        assertEquals(3.42857142, result.getRoleFactors().get(ROLE_KEY_2)
                .doubleValue(), 0.0000001);
        assertEquals(1.71428571,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);
        assertEquals(1.71428571,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_2))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);
    }

    @Test
    public void determineUserAssignmentsFactors_ForDayUnit() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_DAY,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(29, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(29, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(29,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserAssignedTwoTimesInOneUnitWithRole() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-29 00:00:00"),
                ModificationType.ADD));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-02 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(USER_ID_1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUserId());
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_StartTimeAdjustedForWeekPricing() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        List<UsageLicenseHistory> ulHistoryForWeek = new ArrayList<UsageLicenseHistory>();
        ulHistoryForWeek.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                null, DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));
        ulHistoryForWeek.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                null, DateTimeHandling.calculateDate("2012-01-30 00:00:01"),
                ModificationType.DELETE));
        ulHistoryForWeek.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                null, DateTimeHandling.calculateDate("2012-01-28 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistoryForWeek, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(4, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
    }

    @Test
    public void determineUserAssignmentsFactors_StartTimeAdjustedForMonthPricing() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        List<UsageLicenseHistory> ulHistoryForMonth = new ArrayList<UsageLicenseHistory>();
        ulHistoryForMonth.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                null, DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistoryForMonth, PM_MONTH,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
    }

    @Test
    public void determineUserAssignmentsFactors_PeriodStartInBillingPeriodAndUserAdded() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-15 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(2, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(2,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_PeriodStartInBillingPeriodAndUserModified() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-15 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-16 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0);

        assertEquals(2, result.getRoleFactors().size());
        assertEquals(0.42857142, result.getRoleFactors().get(ROLE_KEY_1)
                .doubleValue(), 0.0000001);
        assertEquals(0.42857142,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);

        assertEquals(1.57142857, result.getRoleFactors().get(ROLE_KEY_2)
                .doubleValue(), 0.0000001);
        assertEquals(1.57142857,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);
    }

    @Test
    public void determineUserAssignmentsFactors_PeriodStartInBillingPeriodAndUserDeleted() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-15 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-16 00:00:00"),
                ModificationType.DELETE));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_PeriodEndInBillingPeriodAndUserAdded() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-02-16 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(1,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_PeriodEndInBillingPeriodAndUserModified() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-02-16 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_2,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.MODIFY));
        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.MODIFY));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(3, result.getBasicFactor(), 0);
        assertEquals(2, result.getRoleFactors().size());
        assertEquals(2.28571428, result.getRoleFactors().get(ROLE_KEY_1)
                .doubleValue(), 0.0000001);
        assertEquals(2.28571428,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0.0000001);
        assertEquals(0.71428571, result.getRoleFactors().get(ROLE_KEY_2)
                .doubleValue(), 0.0000001);
        assertEquals(0.71428571,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_2).getFactor(), 0.0000001);
    }

    @Test
    public void determineUserAssignmentsFactors_PeriodEndInBillingPeriodAndUserDeleted() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-01-30 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-02-16 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-02-15 00:00:00"),
                ModificationType.DELETE));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(3, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(3, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(3,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    @Test
    public void determineUserAssignmentsFactors_UserAddedUsageStartTimeBeforePeriodStart() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1, null,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(4, result.getBasicFactor(), 0);
        result.getUserKeys().contains(Long.valueOf(USER_KEY_1));
        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void determineUserAssignmentsFactors_UserRoleAddedUsageStartTimebeBeforePeriodStart() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");
        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        ulHistories.add(createUsageLicenseHistory(USER_KEY_1, USER_ID_1,
                ROLE_KEY_1,
                DateTimeHandling.calculateDate("2012-01-31 00:00:00"),
                ModificationType.ADD));

        // when
        UserAssignmentFactors result = calculator
                .computeUserAssignmentsFactors(ulHistories, PM_WEEK,
                        billingInput, periodStart, periodEnd);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(4, result.getBasicFactor(), 0);
        assertEquals(1, result.getRoleFactors().size());
        assertEquals(4, result.getRoleFactors().get(ROLE_KEY_1).doubleValue(),
                0);
        assertEquals(4,
                result.getUserAssignmentDetails(Long.valueOf(USER_KEY_1))
                        .getUsageDetails(ROLE_KEY_1).getFactor(), 0);
    }

    private UsageLicenseHistory createUsageLicenseHistory(long userObjKey,
            String userId, Long roleDefinitionObjKey, Date modDate,
            ModificationType modType) {
        UsageLicenseHistory usageLicenseHistory = new UsageLicenseHistory();
        usageLicenseHistory.setUserObjKey(userObjKey);
        usageLicenseHistory.setRoleDefinitionObjKey(roleDefinitionObjKey);
        usageLicenseHistory.setModdate(modDate);
        usageLicenseHistory.setModtype(modType);
        UsageLicenseData dataContainer = new UsageLicenseData();
        dataContainer.setApplicationUserId(userId);
        usageLicenseHistory.setDataContainer(dataContainer);
        return usageLicenseHistory;
    }

    private static PriceModelHistory createPriceModelHistory(
            PricingPeriod period) {
        PriceModelHistory priceModelHistory = new PriceModelHistory();
        priceModelHistory.setPeriod(period);
        return priceModelHistory;
    }

}
