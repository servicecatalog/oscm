/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 29.07.2010                                                      
 *                                                                              
 *  Completion Time: 29.07.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.oscm.test.Numbers.L1;
import static org.oscm.test.Numbers.L2;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.billingservice.business.calculation.revenue.model.UsageDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UsageDetails.UsagePeriod;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModelData;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * 
 * Tests for the retrieval of user assignment related period and factor
 * information.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class CostCalculatorUserAssignmentFactorIT extends EJBTestBase {

    private long userKey1;
    private String userId1 = "user1";
    private long userKey2;
    private String userId2 = "user2";
    private long userKey3;
    private String userId3 = "user3";

    private DataService mgr;
    private BillingDataRetrievalServiceLocal bdrs;
    private PriceModelHistory pmHist;
    private int objKeyCount = 1;
    private int objVersionCount = 1;

    private static final Long ROLE1 = L1;
    private static final Long ROLE2 = L2;

    private static final long DAY_DURATION = 3600000 * 24;
    private static final long HOUR_DURATION = 3600000;

    // Java double has a precision of 15 significant digits.
    private final static double ASSERT_DOUBLE_DELTA = 0.000000000000009D;

    private UsageLicenseHistory ulh;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());

        mgr = container.get(DataService.class);
        bdrs = container.get(BillingDataRetrievalServiceLocal.class);

        createPlatformUsers();
    }

    private void createPlatformUsers() throws Exception {
        PlatformUser user1 = PlatformUsers.createUser(mgr, "user1");
        userKey1 = user1.getKey();
        userId1 = user1.getUserId();
        PlatformUser user2 = PlatformUsers.createUser(mgr, "user2");
        userKey2 = user2.getKey();
        userId2 = user2.getUserId();
        PlatformUser user3 = PlatformUsers.createUser(mgr, "user3");
        userKey3 = user3.getKey();
        userId3 = user3.getUserId();
    }

    /**
     * Initializes a price model history object.
     * 
     * @param isChargeable
     *            Indicates whether the price model is chargeable or not.
     * @param period
     *            The pricing period.
     */
    private void initPriceModelHistory(boolean isChargeable,
            PricingPeriod period) {
        pmHist = new PriceModelHistory();
        PriceModelData dataContainer = pmHist.getDataContainer();
        if (isChargeable) {
            dataContainer.setType(PriceModelType.PRO_RATA);
        } else {
            dataContainer.setType(PriceModelType.FREE_OF_CHARGE);
        }
        dataContainer.setPeriod(period);
    }

    /**
     * Creates a usage license history object.
     * 
     * @param entryCreationTime
     *            The modification date for the history entry.
     * @param entryModType
     *            The modification type for the history entry.
     * @param subscriptionKey
     *            The key of the subscription the user belongs to.
     * @param userKey
     *            The key of the user.
     * @param roleObjKey
     *            The role the user was assigned to.
     * @throws Exception
     */
    private void createUsageLicenseHistory(final long entryCreationTime,
            final ModificationType entryModType, final long subscriptionKey,
            final long userKey, final Long roleObjKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (entryModType == ModificationType.ADD) {
                    objKeyCount++;
                }
                ulh = new UsageLicenseHistory();
                Date date = new Date(entryCreationTime);
                ulh.setModdate(date);
                ulh.setInvocationDate(date);
                ulh.setModtype(entryModType);
                ulh.setModuser("anyUser");
                ulh.setSubscriptionObjKey(subscriptionKey);
                ulh.setUserObjKey(userKey);
                ulh.setObjKey(objKeyCount);
                ulh.setObjVersion(++objVersionCount);
                ulh.setRoleDefinitionObjKey(roleObjKey);
                if (entryModType == ModificationType.DELETE) {
                    objKeyCount++;
                }
                mgr.persist(ulh);
                return null;
            }
        });
    }

    /**
     * Checks that the specified user details object has the required period and
     * factor settings.
     */
    private void assertDetailsForUser(String userId,
            UserAssignmentDetails details, double expectedFactor,
            UsagePeriod... expectedUsagePeriod) {
        assertNotNull(details);
        assertEquals(userId, details.getUserId());

        UsageDetails usageDetails = details.getUsageDetails();
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();

        assertEquals("Wrong number of usage periods",
                expectedUsagePeriod.length, usagePeriods.size());
        for (int i = 0; i < usagePeriods.size(); i++) {
            assertEquals("Wrong usage period", expectedUsagePeriod[i],
                    usagePeriods.get(i));
        }

        assertEquals("Wrong factor", expectedFactor, usageDetails.getFactor(),
                ASSERT_DOUBLE_DELTA);
    }

    private double simpleFactor(long duration, long timeUnitDuration) {
        return (double) duration / (double) timeUnitDuration;
    }

    @Test(expected = IllegalArgumentException.class)
    public void determineUserAssignmentsFactors_NonChargeablePriceModel()
            throws Exception {
        // given
        initPriceModelHistory(false, PricingPeriod.DAY);

        // when
        runTX(new Callable<UserAssignmentFactors>() {
            @Override
            public UserAssignmentFactors call() throws Exception {
                final List<UsageLicenseHistory> ulHistList = bdrs
                        .loadUsageLicenses(1L, 1000, 2000);
                return CostCalculator.get(pmHist).computeUserAssignmentsFactors(
                        ulHistList, pmHist,
                        BillingInputFactory.newBillingInput(0, 0), 1000, 2000);
            }
        });

        // then
    }

    @Test
    public void determineUserAssignmentsFactors_NoUsers() throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 1000, 2000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        1000, 2000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(0, result.getUserKeys().size());
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedAfterBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 1000, 2000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        1000, 2000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(0, result.getUserKeys().size());
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedAndRevokedAfterBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 1000, 2000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        1000, 2000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(0, result.getUserKeys().size());
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedAndRevokedBeforeBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 100000, 120000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        100000, 120000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(0, result.getUserKeys().size());
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedInBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 1000, 12000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        1000, 12000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(7000, DAY_DURATION),
                new UsagePeriod(5000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedInRevokedAfterBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        final UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 1000, 12000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        1000, 12000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(7000, DAY_DURATION),
                new UsagePeriod(5000, 12000));
    }

    @Test
    public void determineUserAssignmentFactors_assignAndDeassign()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.HOUR);
        createUsageLicenseHistory(50000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(850000, ModificationType.ADD, 1, userKey2,
                null);
        createUsageLicenseHistory(900000, ModificationType.DELETE, 1, userKey2,
                null);
        createUsageLicenseHistory(950000, ModificationType.ADD, 1, userKey3,
                null);

        // when
        final UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 3550000, 3650000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        3550000, 3650000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(2, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details,
                simpleFactor(100000, HOUR_DURATION),
                new UsagePeriod(3550000, 3650000));
        assertNull(result.getUserAssignmentDetails(Long.valueOf(userKey2)));
        details = result.getUserAssignmentDetails(Long.valueOf(userKey3));
        assertDetailsForUser(userId3, details,
                simpleFactor(100000, HOUR_DURATION),
                new UsagePeriod(3550000, 3650000));
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedBeforeRevokedInBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        final UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(5000, DAY_DURATION),
                new UsagePeriod(10000, 15000));
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedBeforeBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(5000, ModificationType.ADD, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details,
                simpleFactor(10000, DAY_DURATION),
                new UsagePeriod(10000, 20000));
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedAndRevokedInBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(12000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(3000, DAY_DURATION),
                new UsagePeriod(12000, 15000));
    }

    @Test
    public void determineUserAssignmentsFactors_AssignedBeforeRevokedAfterBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(2000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(25000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details,
                simpleFactor(10000, DAY_DURATION),
                new UsagePeriod(10000, 20000));
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleAssignsInBillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(10000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(13000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(17000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(18000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(5000, DAY_DURATION),
                new UsagePeriod(17000, 18000), new UsagePeriod(13000, 15000),
                new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleAssigns1BillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(9000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(13000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(4000, DAY_DURATION),
                new UsagePeriod(13000, 15000), new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleAssigns2BillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(11000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(18000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(22000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(3000, DAY_DURATION),
                new UsagePeriod(18000, 20000), new UsagePeriod(11000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleAssigns3BillingPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(9000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(14000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(17000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(18000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(22000, ModificationType.DELETE, 1, userKey1,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(7000, DAY_DURATION),
                new UsagePeriod(18000, 20000), new UsagePeriod(14000, 17000),
                new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_MultipleUsers()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(9000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(14000, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(17000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(13000, ModificationType.ADD, 1, userKey2,
                null);
        createUsageLicenseHistory(16000, ModificationType.DELETE, 1, userKey2,
                null);
        createUsageLicenseHistory(19000, ModificationType.ADD, 1, userKey2,
                null);
        createUsageLicenseHistory(22000, ModificationType.DELETE, 1, userKey2,
                null);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(2, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertDetailsForUser(userId1, details, simpleFactor(5000, DAY_DURATION),
                new UsagePeriod(14000, 17000), new UsagePeriod(10000, 12000));

        details = result.getUserAssignmentDetails(Long.valueOf(userKey2));
        assertDetailsForUser(userId2, details, simpleFactor(4000, DAY_DURATION),
                new UsagePeriod(19000, 20000), new UsagePeriod(13000, 16000));
    }

    // *** tests considering roles ***

    /**
     * Assert the number of the role assignment details
     */
    private void assertRoleCount(UserAssignmentDetails details,
            int expectedRoleCount) {
        assertEquals(expectedRoleCount, details.getRoleKeys().size());
    }

    /**
     * Asserts the user assignment details related to roles.
     */
    private void assertRoleDetails(UserAssignmentDetails details, Long roleKey,
            double expectedFactor, UsagePeriod... expectedUsagePeriod) {
        assertNotNull(details);

        UsageDetails usageDetails = details.getUsageDetails(roleKey);
        List<UsagePeriod> usagePeriods = usageDetails.getUsagePeriods();

        assertEquals("Wrong number of usage periods",
                expectedUsagePeriod.length, usagePeriods.size());
        for (int i = 0; i < usagePeriods.size(); i++) {
            assertEquals("Wrong usage period", expectedUsagePeriod[i],
                    usagePeriods.get(i));
        }

        assertEquals("Wrong factor", expectedFactor, usageDetails.getFactor(),
                ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserRole1BeforeRole2AfterPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(7000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(24000, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(27000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(0, result.getUserKeys().size());
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserRole1PartialRole2PartialPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(17000, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(21000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));

        assertRoleCount(details, 2);
        assertRoleDetails(details, ROLE1, simpleFactor(2000, DAY_DURATION),
                new UsagePeriod(10000, 12000));
        assertRoleDetails(details, ROLE2, simpleFactor(3000, DAY_DURATION),
                new UsagePeriod(17000, 20000));
        assertDetailsForUser(userId1, details, simpleFactor(5000, DAY_DURATION),
                new UsagePeriod(17000, 20000), new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserRole1PartialRole2StillActivePeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(16000, ModificationType.ADD, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));

        assertRoleCount(details, 2);
        assertRoleDetails(details, ROLE1, simpleFactor(2000, DAY_DURATION),
                new UsagePeriod(10000, 12000));
        assertRoleDetails(details, ROLE2, simpleFactor(4000, DAY_DURATION),
                new UsagePeriod(16000, 20000));

        assertDetailsForUser(userId1, details, simpleFactor(6000, DAY_DURATION),
                new UsagePeriod(16000, 20000), new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserRole1EntireRole2AfterPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(22000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(26000, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(36000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));

        assertRoleCount(details, 1);
        assertRoleDetails(details, ROLE1, simpleFactor(10000, DAY_DURATION),
                new UsagePeriod(10000, 20000));

        assertDetailsForUser(userId1, details,
                simpleFactor(10000, DAY_DURATION),
                new UsagePeriod(10000, 20000));
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserRole1MultiplePartsPeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(16000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(26000, ModificationType.DELETE, 1, userKey1,
                ROLE1);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));

        assertRoleCount(details, 1);
        assertRoleDetails(details, ROLE1, simpleFactor(6000, DAY_DURATION),
                new UsagePeriod(16000, 20000), new UsagePeriod(10000, 12000));

        assertDetailsForUser(userId1, details, simpleFactor(6000, DAY_DURATION),
                new UsagePeriod(16000, 20000), new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserRole1AndRole2MultiplePeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12500, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(14000, ModificationType.DELETE, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(16000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(16500, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(19500, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(21000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));

        assertRoleCount(details, 2);
        assertRoleDetails(details, ROLE1, simpleFactor(2500, DAY_DURATION),
                new UsagePeriod(16000, 16500), new UsagePeriod(10000, 12000));
        assertRoleDetails(details, ROLE2, simpleFactor(2000, DAY_DURATION),
                new UsagePeriod(19500, 20000), new UsagePeriod(12500, 14000));

        assertDetailsForUser(userId1, details, simpleFactor(4500, DAY_DURATION),
                new UsagePeriod(19500, 20000), new UsagePeriod(16000, 16500),
                new UsagePeriod(12500, 14000), new UsagePeriod(10000, 12000));
    }

    @Test
    public void determineUserAssignmentsFactors_TwoUsersRole1AndRole2MultiplePeriod()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                ROLE1);

        createUsageLicenseHistory(6900, ModificationType.ADD, 1, userKey2,
                ROLE1);
        createUsageLicenseHistory(11500, ModificationType.DELETE, 1, userKey2,
                ROLE1);

        createUsageLicenseHistory(12500, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(14000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        createUsageLicenseHistory(14500, ModificationType.ADD, 1, userKey2,
                ROLE2);
        createUsageLicenseHistory(15000, ModificationType.DELETE, 1, userKey2,
                ROLE2);

        createUsageLicenseHistory(16000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(16500, ModificationType.DELETE, 1, userKey1,
                ROLE1);

        createUsageLicenseHistory(17000, ModificationType.ADD, 1, userKey2,
                ROLE2);
        createUsageLicenseHistory(25000, ModificationType.DELETE, 1, userKey2,
                ROLE2);

        createUsageLicenseHistory(19500, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(21000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(2, result.getUserKeys().size());

        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));
        assertRoleCount(details, 2);
        assertRoleDetails(details, ROLE1, simpleFactor(2500, DAY_DURATION),
                new UsagePeriod(16000, 16500), new UsagePeriod(10000, 12000));
        assertRoleDetails(details, ROLE2, simpleFactor(2000, DAY_DURATION),
                new UsagePeriod(19500, 20000), new UsagePeriod(12500, 14000));
        assertDetailsForUser(userId1, details, simpleFactor(4500, DAY_DURATION),
                new UsagePeriod(19500, 20000), new UsagePeriod(16000, 16500),
                new UsagePeriod(12500, 14000), new UsagePeriod(10000, 12000));

        details = result.getUserAssignmentDetails(Long.valueOf(userKey2));

        assertRoleCount(details, 2);
        assertRoleDetails(details, ROLE1, simpleFactor(1500, DAY_DURATION),
                new UsagePeriod(10000, 11500));
        assertRoleDetails(details, ROLE2, simpleFactor(3500, DAY_DURATION),
                new UsagePeriod(17000, 20000), new UsagePeriod(14500, 15000));
        assertDetailsForUser(userId2, details, simpleFactor(5000, DAY_DURATION),
                new UsagePeriod(17000, 20000), new UsagePeriod(14500, 15000),
                new UsagePeriod(10000, 11500));

        Map<Long, Double> roleFactors = result.getRoleFactors();
        assertEquals(2, roleFactors.keySet().size());
        assertEquals(4.62962962962963E-5, roleFactors.get(ROLE1).doubleValue(),
                ASSERT_DOUBLE_DELTA);
        assertEquals(6.36574074074074E-5, roleFactors.get(ROLE2).doubleValue(),
                ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void determineUserAssignmentsFactors_OneUserMixedMode()
            throws Exception {
        // given
        initPriceModelHistory(true, PricingPeriod.DAY);
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12000, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(12500, ModificationType.ADD, 1, userKey1,
                null);
        createUsageLicenseHistory(14000, ModificationType.DELETE, 1, userKey1,
                null);
        createUsageLicenseHistory(16000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(16500, ModificationType.DELETE, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(19500, ModificationType.ADD, 1, userKey1,
                ROLE2);
        createUsageLicenseHistory(21000, ModificationType.DELETE, 1, userKey1,
                ROLE2);

        // when
        UserAssignmentFactors result = runTX(
                new Callable<UserAssignmentFactors>() {
                    @Override
                    public UserAssignmentFactors call() throws Exception {
                        final List<UsageLicenseHistory> ulHistList = bdrs
                                .loadUsageLicenses(1L, 10000, 20000);
                        return CostCalculator.get(pmHist)
                                .computeUserAssignmentsFactors(ulHistList,
                                        pmHist, BillingInputFactory
                                                .newBillingInput(0, 0),
                                        10000, 20000);
                    }
                });

        // then
        assertNotNull(result);
        assertEquals(1, result.getUserKeys().size());
        UserAssignmentDetails details = result
                .getUserAssignmentDetails(Long.valueOf(userKey1));

        assertRoleCount(details, 2);
        assertRoleDetails(details, ROLE1, simpleFactor(2500, DAY_DURATION),
                new UsagePeriod(16000, 16500), new UsagePeriod(10000, 12000));
        assertRoleDetails(details, ROLE2, simpleFactor(500, DAY_DURATION),
                new UsagePeriod(19500, 20000));

        assertDetailsForUser(userId1, details, simpleFactor(4500, DAY_DURATION),
                new UsagePeriod(19500, 20000), new UsagePeriod(16000, 16500),
                new UsagePeriod(12500, 14000), new UsagePeriod(10000, 12000));
    }

    @Test
    public void getUsageLicenses_order() throws Exception {
        // given
        objVersionCount = 0;
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey2,
                ROLE2);
        createUsageLicenseHistory(4000, ModificationType.MODIFY, 1, userKey2,
                ROLE2);
        objVersionCount = 0;
        createUsageLicenseHistory(4000, ModificationType.ADD, 1, userKey1,
                ROLE1);
        createUsageLicenseHistory(4000, ModificationType.MODIFY, 1, userKey1,
                ROLE1);

        // when
        List<UsageLicenseHistory> result = runTX(
                new Callable<List<UsageLicenseHistory>>() {
                    @Override
                    public List<UsageLicenseHistory> call() throws Exception {
                        return bdrs.loadUsageLicenses(1L, 3000, 5000);
                    }
                });

        // then
        assertEquals(4, result.size());
        assertEquals(ModificationType.MODIFY, result.get(0).getModtype());
        assertEquals(userKey1, result.get(0).getUserObjKey());
        assertEquals(ModificationType.ADD, result.get(1).getModtype());
        assertEquals(userKey1, result.get(1).getUserObjKey());
        assertEquals(ModificationType.MODIFY, result.get(2).getModtype());
        assertEquals(userKey2, result.get(2).getUserObjKey());
        assertEquals(ModificationType.ADD, result.get(3).getModtype());
        assertEquals(userKey2, result.get(3).getUserObjKey());
    }
}
