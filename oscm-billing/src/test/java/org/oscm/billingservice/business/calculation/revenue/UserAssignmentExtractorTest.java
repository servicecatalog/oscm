/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 24, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.UserAssignmentExtractor;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.DateTimeHandling;

/**
 * @author tokoda
 * 
 */
public class UserAssignmentExtractorTest {

    private static final long USER_KEY = 10000L;
    private static final String USER_ID = "userA";

    private static final long USER_KEY2 = 20000L;
    private static final String USER_ID2 = "userB";

    private static final long ROLE_KEY_1 = 11111;
    private static final long ROLE_KEY_2 = 22222;
    private static final long ROLE_KEY_3 = 33333;

    List<UsageLicenseHistory> histories;

    @Before
    public void setup() {
        histories = new ArrayList<UsageLicenseHistory>();
    }

    @Test
    public void extract_AddBeforeUsageStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_AddAfterUsageStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_AddAfterUsageEndWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(0, userKeySet.size());
    }

    @Test
    public void extract_ModifyBeforeUsageStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_ModifyAfterUsageStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_DeleteBeforeUsageStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(0, userKeySet.size());
    }

    @Test
    public void extract_DeleteAfterUsageStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_DeleteAfterUsageEndWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-05-01 00:00:01"),
                USER_KEY, USER_ID, null);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-03-31 00:00:00"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_MultipleUsersWithoutRole() {
        // given

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:05"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY2, USER_ID2, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(2, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY2)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());

        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());

        List<UserAssignment> userAssignmentList2 = extractor
                .getUserAssignments(Long.valueOf(USER_KEY2));
        assertEquals(1, userAssignmentList2.size());

        UserAssignment userAssignment2 = userAssignmentList2.get(0);
        assertEquals(USER_KEY2, userAssignment2.getUserKey());
        assertEquals(USER_ID2, userAssignment2.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment2.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment2.getUsageEndTime());
        assertEquals(0, userAssignment2.getRoleAssignments().size());
    }

    @Test
    public void extract_MultipleUserAssignmentsForOneUserWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(2, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:02"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());

        UserAssignment userAssignment2 = userAssignmentList.get(1);
        assertEquals(USER_KEY, userAssignment2.getUserKey());
        assertEquals(USER_ID, userAssignment2.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment2.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment2.getUsageEndTime());
        assertEquals(0, userAssignment2.getRoleAssignments().size());
    }

    @Test
    public void extract_MultipleHistoriesWithoutRole() {
        // given

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-04-01 00:00:03"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:03"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_HistoriesBeforePeriodStartWithoutRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:00"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-03-31 23:59:58"),
                USER_KEY, USER_ID, null);

        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-03-31 23:59:57"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());
        assertEquals(0, userAssignment.getRoleAssignments().size());
    }

    @Test
    public void extract_AddBeforeUsageStartWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_AddAfterUsageStartWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_AddAfterUsageEndWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(0, userKeySet.size());
    }

    @Test
    public void extract_ModifyBeforeUsageStartWithSameRole() {
        // given
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_ModifyBeforeUsageStartWithDifferntRole() {
        // given
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(2, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertFalse(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());

        assertEquals(ROLE_KEY_2, roles.get(1).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(1).getEndTime());
        assertTrue(roles.get(1).isFirstRoleAssignment());
        assertFalse(roles.get(1).isLastRoleAssignment());
    }

    @Test
    public void extract_ModifyAfterUsageStartWithSameRole() {
        // given
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-01-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_ModifyAfterUsageStartWithDifferntRole() {
        // given
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-01-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(2, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertFalse(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());

        assertEquals(ROLE_KEY_2, roles.get(1).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(1).getEndTime());
        assertTrue(roles.get(1).isFirstRoleAssignment());
        assertFalse(roles.get(1).isLastRoleAssignment());
    }

    @Test
    public void extract_DeleteBeforeUsageStartWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(0, userKeySet.size());
    }

    @Test
    public void extract_DeleteAfterUsageStartWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-01-01 00:00:00"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_DeleteAfterUsageEndWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-05-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-01-01 00:00:00"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_MultipleHistoriesWithRole() {
        // given

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-04-01 00:00:05"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_3));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:04"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_3));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:03"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:05"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(3, roles.size());

        UserRoleAssignment role1 = roles.get(0);
        assertEquals(ROLE_KEY_3, role1.getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:04"),
                role1.getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:05"),
                role1.getEndTime());
        assertFalse(role1.isFirstRoleAssignment());
        assertTrue(role1.isLastRoleAssignment());

        UserRoleAssignment role2 = roles.get(1);
        assertEquals(ROLE_KEY_2, role2.getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:03"),
                role2.getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:04"),
                role2.getEndTime());
        assertFalse(role2.isFirstRoleAssignment());
        assertFalse(role2.isLastRoleAssignment());

        UserRoleAssignment role3 = roles.get(2);
        assertEquals(ROLE_KEY_1, role3.getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                role3.getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:03"),
                role3.getEndTime());
        assertTrue(role3.isFirstRoleAssignment());
        assertFalse(role3.isLastRoleAssignment());
    }

    @Test
    public void extract_AddAndModifyAtSameTime() {
        // given

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:01"),
                USER_KEY, USER_ID, null);

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:02"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:02"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_MultipleUsersWithRole() {
        // given

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:03"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY2, USER_ID2, Long.valueOf(ROLE_KEY_2));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(2, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY2)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());

        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_2, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());

        List<UserAssignment> userAssignmentList2 = extractor
                .getUserAssignments(Long.valueOf(USER_KEY2));
        assertEquals(1, userAssignmentList2.size());

        UserAssignment userAssignment2 = userAssignmentList2.get(0);
        assertEquals(USER_KEY2, userAssignment2.getUserKey());
        assertEquals(USER_ID2, userAssignment2.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment2.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment2.getUsageEndTime());

        List<UserRoleAssignment> roles2 = userAssignment2.getRoleAssignments();
        assertEquals(1, roles2.size());
        assertEquals(ROLE_KEY_2, roles2.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles2.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles2.get(0).getEndTime());
        assertTrue(roles2.get(0).isFirstRoleAssignment());
        assertTrue(roles2.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_MultipleUsersWithMultipleRoles() {
        // given

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:05"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:04"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:03"),
                USER_KEY2, USER_ID2, Long.valueOf(ROLE_KEY_2));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-01 00:00:02"),
                USER_KEY2, USER_ID2, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(2, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY2)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());

        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(2, roles.size());
        assertEquals(ROLE_KEY_2, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:05"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertFalse(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
        assertEquals(ROLE_KEY_1, roles.get(1).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:05"),
                roles.get(1).getEndTime());
        assertTrue(roles.get(1).isFirstRoleAssignment());
        assertFalse(roles.get(1).isLastRoleAssignment());

        List<UserAssignment> userAssignmentList2 = extractor
                .getUserAssignments(Long.valueOf(USER_KEY2));
        assertEquals(1, userAssignmentList2.size());

        UserAssignment userAssignment2 = userAssignmentList2.get(0);
        assertEquals(USER_KEY2, userAssignment2.getUserKey());
        assertEquals(USER_ID2, userAssignment2.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment2.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment2.getUsageEndTime());

        List<UserRoleAssignment> roles2 = userAssignment2.getRoleAssignments();
        assertEquals(2, roles2.size());
        assertEquals(ROLE_KEY_2, roles2.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:03"),
                roles2.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles2.get(0).getEndTime());
        assertFalse(roles2.get(0).isFirstRoleAssignment());
        assertTrue(roles2.get(0).isLastRoleAssignment());
        assertEquals(ROLE_KEY_1, roles2.get(1).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles2.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:03"),
                roles2.get(1).getEndTime());
        assertTrue(roles2.get(1).isFirstRoleAssignment());
        assertFalse(roles2.get(1).isLastRoleAssignment());
    }

    @Test
    public void extract_MultipleUserAssignmentsForOneUserWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2012-12-05 00:00:00"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2012-12-03 00:00:00"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2012-11-15 12:00:00"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(2, userAssignmentList.size());

        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-05 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_2, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-05 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());

        UserAssignment userAssignment2 = userAssignmentList.get(1);
        assertEquals(USER_KEY, userAssignment2.getUserKey());
        assertEquals(USER_ID, userAssignment2.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                userAssignment2.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-03 00:00:00"),
                userAssignment2.getUsageEndTime());

        List<UserRoleAssignment> roles2 = userAssignment2.getRoleAssignments();
        assertEquals(1, roles2.size());
        assertEquals(ROLE_KEY_1, roles2.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                roles2.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-03 00:00:00"),
                roles2.get(0).getEndTime());
        assertTrue(roles2.get(0).isFirstRoleAssignment());
        assertTrue(roles2.get(0).isLastRoleAssignment());
    }

    @Test
    public void extract_HistoriesBeforePeriodStartWithRole() {
        // given
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-01 00:00:00"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-03-31 23:59:59"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-03-31 23:59:58"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_1));

        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-03-31 23:59:57"),
                USER_KEY, USER_ID, Long.valueOf(ROLE_KEY_2));

        // when
        UserAssignmentExtractor extractor = new UserAssignmentExtractor(
                histories,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        extractor.extract();

        // then
        Set<Long> userKeySet = extractor.getUserKeys();
        assertEquals(1, userKeySet.size());
        assertTrue(userKeySet.contains(Long.valueOf(USER_KEY)));

        List<UserAssignment> userAssignmentList = extractor
                .getUserAssignments(Long.valueOf(USER_KEY));
        assertEquals(1, userAssignmentList.size());
        UserAssignment userAssignment = userAssignmentList.get(0);
        assertEquals(USER_KEY, userAssignment.getUserKey());
        assertEquals(USER_ID, userAssignment.getUserId());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                userAssignment.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                userAssignment.getUsageEndTime());

        List<UserRoleAssignment> roles = userAssignment.getRoleAssignments();
        assertEquals(1, roles.size());
        assertEquals(ROLE_KEY_1, roles.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                roles.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                roles.get(0).getEndTime());
        assertTrue(roles.get(0).isFirstRoleAssignment());
        assertTrue(roles.get(0).isLastRoleAssignment());
    }

    private void createUsageLicenseHistory(ModificationType modtype,
            Date moddate, long userKey, String userId, Long roleKey) {
        UsageLicenseHistory history = new UsageLicenseHistory();
        history.setModtype(modtype);
        history.setModdate(moddate);
        history.setUserObjKey(userKey);
        history.getDataContainer().setApplicationUserId(userId);
        history.setRoleDefinitionObjKey(roleKey);
        histories.add(history);
    }
}
