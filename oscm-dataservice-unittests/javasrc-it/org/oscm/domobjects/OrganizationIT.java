/********************************************************************************
 *                                                                             
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: schmid
 *                                                                              
 *  Creation Date: 20.01.2009                                                      
 *                                                                             
 *  Completion Time:                           
 *                                                                           
 ********************************************************************************/
package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.OrganizationReferences;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Udas;
import org.oscm.types.enumtypes.UdaTargetType;

/**
 * Tests of the organization-related domain objects (incl. auditing
 * functionality)
 * 
 * @author schmid
 * 
 */
public class OrganizationIT extends DomainObjectTestBase {

    private List<DomainObjectWithHistory<?>> domObjects = new ArrayList<DomainObjectWithHistory<?>>();
    private Organization orgWithoutSubs;
    private Organization orgWithOneSub;
    private Organization orgWithTwoSubs;
    private Organization orgWithTwoStatesSubs;

    @Before
    public void setUp() throws Exception {
        initOrgWithSubs();
    }

    /**
     * <b>Testcase:</b> Add new Organization objects <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>All objects can be retrieved from DB and are identical to provided
     * Organization objects</li>
     * <li>Cascaded objects (i.e. PaymentInfo) is also stored</li>
     * <li>A history object is created for each organization stored</li>
     * <li>History objects are created for CascadeAudit-annotated associated
     * objects</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestAdd() throws NonUniqueBusinessKeyException {
        // Enter new Organizations
        domObjects.clear();
        Organization cust;
        for (int i = 1; i < 10; i++) {
            cust = new Organization();
            cust.setOrganizationId("Organization" + i);
            cust.setName("The " + i + ". organization");
            cust.setAddress("my address is a very long string, which is stored in the database \n with line delimiters\n.");
            cust.setEmail("organization@organization" + i + ".com");
            cust.setPhone("012345/678" + i + i + i);
            cust.setTechnicalProducts(new ArrayList<TechnicalProduct>());
            cust.setProducts(new ArrayList<Product>());
            cust.setSubscriptions(new ArrayList<Subscription>());
            cust.setPlatformUsers(new ArrayList<PlatformUser>());
            cust.setSupportEmail("Support" + i + "@Mail.com");
            cust.setCutOffDay(1);

            mgr.persist(cust);
            Marketplaces.createMarketplace(cust, cust.getOrganizationId(),
                    false, mgr);
            domObjects.add((Organization) ReflectiveClone.clone(cust));
        }
    }

    private void doTestAddCheck() {
        Organization saved = null;
        Organization qry = new Organization();
        for (DomainObjectWithHistory<?> org : domObjects) {
            // Load organization and check values
            Organization orgOrganization = (Organization) org;
            qry.setOrganizationId(orgOrganization.getOrganizationId());
            saved = (Organization) mgr.find(qry);
            Assert.assertNotNull(
                    "Cannot find '" + orgOrganization.getOrganizationId()
                            + "' in DB", saved);
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(saved, orgOrganization),
                    ReflectiveCompare.compare(saved, orgOrganization));
            Assert.assertEquals(orgOrganization.getPlatformUsers().size(),
                    saved.getPlatformUsers().size());
            Assert.assertEquals(orgOrganization.getProducts().size(), saved
                    .getProducts().size());
            Assert.assertEquals(orgOrganization.getSubscriptions().size(),
                    saved.getSubscriptions().size());
            Assert.assertEquals(orgOrganization.getTechnicalProducts().size(),
                    saved.getTechnicalProducts().size());

            // Load history objects and check them
            List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
            Assert.assertNotNull("History entry 'null' for organization "
                    + orgOrganization.getOrganizationId());
            Assert.assertFalse("History entry empty for organization "
                    + orgOrganization.getOrganizationId(), histObjs.isEmpty());
            Assert.assertEquals("One history entry expected for organization "
                    + orgOrganization.getOrganizationId(), 1, histObjs.size());
            DomainHistoryObject<?> hist = histObjs.get(0);
            Assert.assertEquals(ModificationType.ADD, hist.getModtype());
            Assert.assertEquals("modUser", "guest", hist.getModuser());
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(orgOrganization, hist),
                    ReflectiveCompare.compare(orgOrganization, hist));
            Assert.assertEquals("OBJID in history different",
                    orgOrganization.getKey(), hist.getObjKey());
        }

    }

    /**
     * <b>Testcase:</b> Modify an existing organization object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the organization</li>
     * <li>usedPayment unchanged</li>
     * <li>No new history object for PaymentInfo</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifyOrganization() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModifyOrganizationPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModifyOrganization();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModifyOrganizationCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifyOrganizationPrepare()
            throws NonUniqueBusinessKeyException {
        domObjects.clear();
        // insert new organization
        Organization cust = new Organization();
        cust.setOrganizationId("TestModifyOrganization");
        cust.setName("The TestModifyOrganization");
        cust.setAddress("Adress");
        cust.setEmail("organization@TestModifyOrganization.com");
        cust.setPhone("012345/67890");
        cust.setSupportEmail("Support@Mail.com");
        cust.setCutOffDay(1);

        mgr.persist(cust);
    }

    private void doTestModifyOrganization() {
        // Change only organization data
        Organization cust = new Organization();
        cust.setOrganizationId("TestModifyOrganization");
        Organization cust2 = (Organization) mgr.find(cust);
        cust2.setName("another one");
        domObjects.clear();
        domObjects.add((Organization) ReflectiveClone.clone(cust2));
    }

    private void doTestModifyOrganizationCheck() {
        // Load modified
        Organization orgOrganization = (Organization) domObjects.get(0);
        Organization cid = new Organization();
        cid.setOrganizationId("TestModifyOrganization");
        Organization saved = (Organization) mgr.find(cid);
        // Check organization data
        Assert.assertNotNull(
                "Cannot find '" + orgOrganization.getOrganizationId()
                        + "' in DB", saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgOrganization),
                ReflectiveCompare.compare(saved, orgOrganization));
        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History entry 'null' for organization "
                + orgOrganization.getOrganizationId(), histObjs);
        Assert.assertFalse("History entry empty for organization "
                + orgOrganization.getOrganizationId(), histObjs.isEmpty());
        Assert.assertEquals(
                "Exactly 2 history entries expected for organization "
                        + orgOrganization.getOrganizationId(), 2,
                histObjs.size());
        // load modified history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.MODIFY, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgOrganization, hist),
                ReflectiveCompare.compare(orgOrganization, hist));
        Assert.assertEquals("OBJID in history different",
                orgOrganization.getKey(), hist.getObjKey());

        Assert.assertEquals("SupportEmial in history different",
                orgOrganization.getSupportEmail(),
                ((OrganizationHistory) hist).getSupportEmail());
    }

    /**
     * <b>Testcase:</b> Delete an existing organization object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Organization marked as deleted in the DB</li>
     * <li>History object created for the deleted organization</li>
     * <li>PaymentInfo (usedPayment) marked as deleted in the DB</li>
     * <li>History object created for the deleted PaymentInfo</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteOrganization() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDeleteOrganizationPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDeleteOrganization();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDeleteOrganizationCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteOrganizationPrepare()
            throws NonUniqueBusinessKeyException {
        domObjects.clear();
        // insert new organization
        Organization cust = new Organization();
        cust.setOrganizationId("TestDeleteOrganization");
        cust.setName("The TestDeleteOrganization");
        cust.setAddress("Adress");
        cust.setEmail("organization@TestModifyOrganization.com");
        cust.setPhone("012345/67890");
        cust.setSupportEmail("Support@mail.com");
        cust.setCutOffDay(1);

        mgr.persist(cust);
    }

    private void doTestDeleteOrganization() {
        // delete the organization
        Organization cust = new Organization();
        cust.setOrganizationId("TestDeleteOrganization");
        Organization cust2 = (Organization) mgr.find(cust);
        domObjects.clear();
        mgr.remove(cust2);
        domObjects.add((Organization) ReflectiveClone.clone(cust2));
    }

    private void doTestDeleteOrganizationCheck() {
        // Try to load deleted
        Organization orgOrganization = (Organization) domObjects.get(0);
        Organization cid = new Organization();
        cid.setOrganizationId("TestDeleteOrganization");
        Organization saved = (Organization) mgr.find(cid);
        // Check organization data
        Assert.assertNull(
                "Deleted Organization '" + orgOrganization.getOrganizationId()
                        + "' can still be accessed via DataManager.find", saved);
        // Load Organization history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr
                .findHistory(orgOrganization);
        Assert.assertNotNull("History entry 'null' for organization "
                + orgOrganization.getOrganizationId(), histObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for organization "
                        + orgOrganization.getOrganizationId(), 2,
                histObjs.size());
        Assert.assertEquals("OBJID in history different",
                orgOrganization.getKey(), histObjs.get(1).getObjKey());

        // deleted history object
        DomainHistoryObject<?> hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
    }

    /**
     * <b>Testcase:</b> Try to insert two organizations with the same
     * organizationId<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraint() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestViolateUniqueConstraint();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestViolateUniqueConstraintPrepare()
            throws NonUniqueBusinessKeyException {
        domObjects.clear();
        // insert new organization
        Organization cust = new Organization();
        cust.setOrganizationId("TestUniqueOrganization");
        cust.setName("The TestUniqueOrganization");
        cust.setAddress("Adress");
        cust.setEmail("organization@TestUniqueOrganization.com");
        cust.setPhone("012345/67890");
        cust.setSupportEmail("Support@mail.com");
        cust.setCutOffDay(1);

        mgr.persist(cust);
    }

    private void doTestViolateUniqueConstraint()
            throws NonUniqueBusinessKeyException {
        // Change only organization data
        Organization cust = new Organization();
        cust.setOrganizationId("TestUniqueOrganization");
        cust.setName("A completely different name");
        cust.setAddress("A completely different address");
        cust.setEmail("ACompletelyDifferentEMail@TestUniqueOrganization.com");
        cust.setPhone("2385787/5289475");
        cust.setSupportEmail("ANewSupport@mail.com");
        cust.setCutOffDay(1);

        mgr.persist(cust);
    }

    @Test
    public void testGetTriggerDefinitionsNoTD() throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                return org;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());

                List<TriggerDefinition> triggerDefinitions = storedOrg
                        .getTriggerDefinitions();
                Assert.assertEquals(0, triggerDefinitions.size());
                return null;
            }
        });
    }

    @Test
    public void testGetTriggerDefinitionsOneTD() throws Exception {
        final Organization org = createOrgAndSetTriggerDefinition(false, true);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());

                List<TriggerDefinition> triggerDefinitions = storedOrg
                        .getTriggerDefinitions();
                Assert.assertEquals(1, triggerDefinitions.size());
                return null;
            }
        });
    }

    @Test
    public void testSetTriggerDefinitionsNoCascade() throws Exception {
        final Organization org = createOrgAndSetTriggerDefinition(false, false);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());

                List<TriggerDefinition> triggerDefinitions = storedOrg
                        .getTriggerDefinitions();
                Assert.assertEquals(0, triggerDefinitions.size());
                return null;
            }
        });
    }

    @Test
    public void testGetSuspendingTriggerDefinitionNoHit() throws Exception {
        final Organization org = createOrgAndSetTriggerDefinition(false, false);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());

                TriggerDefinition suspendingTriggerDefinition = storedOrg
                        .getSuspendingTriggerDefinition(TriggerType.ACTIVATE_SERVICE);
                Assert.assertNull(suspendingTriggerDefinition);
                return null;
            }
        });
    }

    @Test
    public void testGetSuspendingTriggerDefinitionOneHit() throws Exception {
        final Organization org = createOrgAndSetTriggerDefinition(true, true);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());

                TriggerDefinition suspendingTriggerDefinition = storedOrg
                        .getSuspendingTriggerDefinition(TriggerType.ACTIVATE_SERVICE);
                Assert.assertNotNull(suspendingTriggerDefinition);
                return null;
            }
        });
    }

    @Test(expected = SaaSSystemException.class)
    public void testGetSuspendingTriggerDefinitionMultipleHits()
            throws Exception {
        final Organization org = createOrgAndSetTriggerDefinition(true, true);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerDefinition td = new TriggerDefinition();
                td.setOrganization(org);
                td.setTarget("bla");
                td.setTargetType(TriggerTargetType.WEB_SERVICE);
                td.setType(TriggerType.ACTIVATE_SERVICE);
                td.setSuspendProcess(true);
                td.setName("testTrigger");
                mgr.persist(td);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());
                storedOrg
                        .getSuspendingTriggerDefinition(TriggerType.ACTIVATE_SERVICE);
                return null;
            }
        });
    }

    @Test()
    public void testGetSuspendingTriggerDefinitionMultipleTDs()
            throws Exception {
        final Organization org = createOrgAndSetTriggerDefinition(true, true);
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerDefinition td = new TriggerDefinition();
                td.setOrganization(org);
                td.setTarget("bla");
                td.setTargetType(TriggerTargetType.WEB_SERVICE);
                td.setType(TriggerType.ADD_REVOKE_USER);
                td.setSuspendProcess(true);
                td.setName("testTrigger");
                mgr.persist(td);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization storedOrg = mgr.getReference(Organization.class,
                        org.getKey());
                TriggerDefinition suspendingTriggerDefinition = storedOrg
                        .getSuspendingTriggerDefinition(TriggerType.ACTIVATE_SERVICE);
                Assert.assertNotNull(suspendingTriggerDefinition);
                return null;
            }
        });
    }

    /**
     * Creates an organization and sets a trigger definition for it.
     * 
     * @param isTriggerSuspending
     *            Specifies whether the trigger definition should be configured
     *            to suspend the processing.
     * @param persistTriggerDefinition
     *            Indicates whether the trigger definition should be persisted
     *            or not.
     * @return The created organization.
     * @throws Exception
     */
    private Organization createOrgAndSetTriggerDefinition(
            final boolean isTriggerSuspending,
            final boolean persistTriggerDefinition) throws Exception {
        final Organization org = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                TriggerDefinition td = new TriggerDefinition();
                td.setOrganization(org);
                td.setTarget("bla");
                td.setTargetType(TriggerTargetType.WEB_SERVICE);
                td.setType(TriggerType.ACTIVATE_SERVICE);
                td.setSuspendProcess(isTriggerSuspending);
                td.setName("testTrigger");
                if (persistTriggerDefinition) {
                    mgr.persist(td);
                }
                org.setTriggerDefinitions(Collections.singletonList(td));
                return org;
            }
        });
        return org;
    }

    /**
     * Negative test cases to add and remove supported countries.
     */
    @Test
    public void testSetSupportedCountries_negative() {
        Organization org = new Organization();

        // add country twice. Only one entry is added
        org.setSupportedCountry(new SupportedCountry("DE"));
        org.setSupportedCountry(new SupportedCountry("DE"));
        assertEquals(1, org.getSupportedCountryCodes().size());
    }

    @Test
    public void testGetSubscriptionsForStateAndPaymentType_NoHit()
            throws Exception {
        List<Subscription> result = orgWithoutSubs
                .getSubscriptionsForStateAndPaymentType(
                        EnumSet.of(SubscriptionStatus.ACTIVE),
                        PaymentInfoType.CREDIT_CARD.name());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSubscriptionsForStateAndPaymentType_SubWithoutPaymentInfo()
            throws Exception {
        List<Subscription> result = orgWithoutSubs
                .getSubscriptionsForStateAndPaymentType(
                        EnumSet.of(SubscriptionStatus.SUSPENDED),
                        PaymentInfoType.CREDIT_CARD.name());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSubscriptionsForStateAndPaymentType_SubWithPaymentInfo()
            throws Exception {
        List<Subscription> result = orgWithOneSub
                .getSubscriptionsForStateAndPaymentType(
                        EnumSet.of(SubscriptionStatus.SUSPENDED),
                        PaymentInfoType.CREDIT_CARD.name());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getKey());
    }

    @Test
    public void testGetSubscriptionsForStateAndPaymentType_SubWithPaymentInfoFiltered()
            throws Exception {
        List<Subscription> result = orgWithTwoSubs
                .getSubscriptionsForStateAndPaymentType(
                        EnumSet.of(SubscriptionStatus.SUSPENDED),
                        PaymentInfoType.CREDIT_CARD.name());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getKey());
        assertEquals(3, result.get(1).getKey());
    }

    @Test
    public void testGetSubscriptionsForStateAndPaymentType_SubsWithTwoStates()
            throws Exception {
        List<Subscription> result = orgWithTwoStatesSubs
                .getSubscriptionsForStateAndPaymentType(EnumSet.of(
                        SubscriptionStatus.SUSPENDED,
                        SubscriptionStatus.SUSPENDED_UPD),
                        PaymentInfoType.CREDIT_CARD.name());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getKey());
        assertEquals(5, result.get(1).getKey());
    }

    private void initOrgWithSubs() {
        Subscription sub1 = new Subscription();
        sub1.setKey(1);
        sub1.setStatus(SubscriptionStatus.SUSPENDED);

        Subscription sub2 = new Subscription();
        sub2.setKey(2);
        sub2.setStatus(SubscriptionStatus.SUSPENDED);

        Subscription sub3 = new Subscription();
        sub3.setKey(3);
        sub3.setStatus(SubscriptionStatus.SUSPENDED);

        Subscription sub4 = new Subscription();
        sub4.setKey(4);
        sub4.setStatus(SubscriptionStatus.ACTIVE);

        Subscription sub5 = new Subscription();
        sub5.setKey(5);
        sub5.setStatus(SubscriptionStatus.SUSPENDED_UPD);

        PaymentType paymentTypeCC = new PaymentType();
        paymentTypeCC.setPaymentTypeId(PaymentInfoType.CREDIT_CARD.name());

        PaymentType paymentTypeDD = new PaymentType();
        paymentTypeDD.setPaymentTypeId(PaymentInfoType.DIRECT_DEBIT.name());

        PaymentInfo paymentInfoCC1 = new PaymentInfo();
        paymentInfoCC1.setPaymentType(paymentTypeCC);

        PaymentInfo paymentInfoCC2 = new PaymentInfo();
        paymentInfoCC2.setPaymentType(paymentTypeCC);

        PaymentInfo paymentInfoDD1 = new PaymentInfo();
        paymentInfoDD1.setPaymentType(paymentTypeDD);

        sub2.setPaymentInfo(paymentInfoCC1);
        sub5.setPaymentInfo(paymentInfoCC1);
        sub3.setPaymentInfo(paymentInfoDD1);
        sub3.setPaymentInfo(paymentInfoCC2);

        orgWithoutSubs = new Organization();
        orgWithOneSub = new Organization();
        orgWithOneSub.getSubscriptions().add(sub1);
        orgWithOneSub.getSubscriptions().add(sub2);

        orgWithTwoSubs = new Organization();
        orgWithTwoSubs.getSubscriptions().add(sub1);
        orgWithTwoSubs.getSubscriptions().add(sub2);
        orgWithTwoSubs.getSubscriptions().add(sub3);
        orgWithTwoSubs.getSubscriptions().add(sub4);

        orgWithTwoStatesSubs = new Organization();
        orgWithTwoStatesSubs.getSubscriptions().add(sub2);
        orgWithTwoStatesSubs.getSubscriptions().add(sub5);
    }

    /**
     * Given an organization with role supplier, then getGrantedRoleTypes() must
     * return the role type supplier
     */
    @Test
    public void getGrantedRoleTypes() {
        Organization givenOrg = createOrganizationWithRole(OrganizationRoleType.SUPPLIER);
        assertTrue(givenOrg.getGrantedRoleTypes().contains(
                OrganizationRoleType.SUPPLIER));
    }

    private Organization createOrganizationWithRole(
            OrganizationRoleType roleType) {
        Organization org = new Organization();
        OrganizationRole role = new OrganizationRole(roleType);
        OrganizationToRole orgToRole = new OrganizationToRole();
        orgToRole.setOrganizationRole(role);
        org.getGrantedRoles().add(orgToRole);
        return org;
    }

    /**
     * Several organizations can act on behalf of an other organization, in case
     * they have a reference of type ON_BEHALF_ACTING
     */
    @Test
    public void isActingOnBehalf() {

        // given two organizations that reference another organization with type
        // ON_BEHALF_ACTING
        Organization master1 = new Organization();
        Organization master2 = new Organization();
        Organization slave = new Organization();
        OrganizationReference ref = new OrganizationReference(master1, slave,
                OrganizationReferenceType.ON_BEHALF_ACTING);
        slave.getSources().add(ref);
        OrganizationReference ref2 = new OrganizationReference(master2, slave,
                OrganizationReferenceType.ON_BEHALF_ACTING);
        slave.getSources().add(ref2);

        // then both can act on behalf of customer
        assertTrue(master1.isActingOnBehalf(slave));
        assertTrue(master2.isActingOnBehalf(slave));
    }

    /**
     * Organization requires a reference of type ON_BEHALF_ACTING in order to
     * act on behalf.
     */
    @Test
    public void isActingOnBehalf_negative() {

        // given a organizations that does not reference another organization
        // with type ON_BEHALF_ACTING
        Organization master = new Organization();
        Organization slave = new Organization();
        OrganizationReference ref = new OrganizationReference(master, slave,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        slave.getSources().add(ref);

        // then this organization can not act on behalf of customer
        assertFalse(master.isActingOnBehalf(slave));
    }

    @Test
    public void testGetAdministratorsPositive() throws Exception {

        // create test data

        final Organization org = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                mgr.persist(org);
                org.addPlatformUser(PlatformUsers.createAdmin(mgr, "admin1",
                        org));
                org.addPlatformUser(PlatformUsers.createAdmin(mgr, "admin2",
                        org));
                org.addPlatformUser(PlatformUsers.createAdmin(mgr, "admin3",
                        org));
                PlatformUser user = PlatformUsers.createUser(mgr, "user1", org);
                org.addPlatformUser(user);
                mgr.persist(org);
                mgr.flush();
                // execute query
                Query query = mgr
                        .createNamedQuery("Organization.getAdministrators");
                query.setParameter("orgkey", new Long(org.getKey()));
                // assert
                assertEquals(3, query.getResultList().size());
                assertFalse(query.getResultList().contains(user));
                return org;
            }
        });

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization org1 = (Organization) mgr.find(org);
                List<PlatformUser> organizationAdmins = org1
                        .getOrganizationAdmins();
                PlatformUser user = new PlatformUser();
                user.setUserId("user1_" + org.getOrganizationId());
                user.setStatus(UserAccountStatus.ACTIVE);
                user.setOrganization(org);
                // assert
                assertEquals(3, organizationAdmins.size());
                assertFalse(organizationAdmins.contains(user));
                return null;
            }
        });
    }
    
    @Test
    public void findOrganizationsByName_customTenant() throws Exception {

        // Given
        givenOrgTenant("Supplier1", "tenant1");
        givenOrgTenant("Supplier2", "tenant3");
        givenOrgTenant("Supplier1", "tenant2");
                
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // When
                Query query = mgr
                        .createNamedQuery("Organization.findOrganizationsByName");
                query.setParameter("name", "Supplier1");
                query.setParameter("tenantId", "tenant1");
                
                // Then
                assertEquals(1, query.getResultList().size());
                return null;
            }
        });
    }

    
    @Test
    public void findOrganizationsByName_differentName_defaultTenant() throws Exception {

        // Given
        givenOrgDefaultTenant("Supplier1");
        givenOrgDefaultTenant("Supplier2");
        
                
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // When
                Query query = mgr
                        .createNamedQuery("Organization.findOrganizationsByName");
                query.setParameter("name", "Supplier2");
                query.setParameter("tenantId", null);
                
                // Then
                assertEquals(1, query.getResultList().size());
                return null;
            }
        });
    }
    
    @Test
    public void findOrganizationsByName_differentName_customTenant() throws Exception {

        // Given
        Organization org = givenOrgTenant("Supplier1", "tenant1");
        givenOrgExistingTenant("Supplier2", org.getTenant());
        
                
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // When
                Query query = mgr
                        .createNamedQuery("Organization.findOrganizationsByName");
                query.setParameter("name", "Supplier2");
                query.setParameter("tenantId", "tenant1");
                
                // Then
                assertEquals(1, query.getResultList().size());
                return null;
            }
        });
    }
    
    @Test
    public void findOrganizationsByName_sameName_bothDefaultTenant() throws Exception {

        // Given
        givenOrgDefaultTenant("Supplier2");
        givenOrgDefaultTenant("Supplier2");
        
                
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // When
                Query query = mgr
                        .createNamedQuery("Organization.findOrganizationsByName");
                query.setParameter("name", "Supplier2");
                query.setParameter("tenantId", null);
                
                // Then
                assertEquals(2, query.getResultList().size());
                return null;
            }
        });
    }
    
    
    @Test
    public void findOrganizationsByName_sameName_differentTenants_queryDefault() throws Exception {

        // Given
        givenOrgDefaultTenant("Supplier2");
        givenOrgTenant("Supplier2", "tenant1");
        
                
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // When
                Query query = mgr
                        .createNamedQuery("Organization.findOrganizationsByName");
                query.setParameter("name", "Supplier2");
                query.setParameter("tenantId", null);
                
                // Then
                assertEquals(1, query.getResultList().size());
                return null;
            }
        });
    }
    
    @Test
    public void findOrganizationsByName_sameName_differentTenants_queryCustom() throws Exception {

        // Given
        givenOrgDefaultTenant("Supplier2");
        givenOrgTenant("Supplier2", "tenant1");
        
                
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // When
                Query query = mgr
                        .createNamedQuery("Organization.findOrganizationsByName");
                query.setParameter("name", "Supplier2");
                query.setParameter("tenantId", "tenant1");
                
                // Then
                assertEquals(1, query.getResultList().size());
                return null;
            }
        });
    }


    @Test
    public void testGetVisiblePlatformUsers() throws Exception {

        // create test data

        final Organization org = runTX(new Callable<Organization>() {
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                mgr.persist(org);
                org.addPlatformUser(PlatformUsers.createAdmin(mgr, "admin1",
                        org));
                org.addPlatformUser(PlatformUsers.createAdmin(mgr, "admin2",
                        org));
                org.addPlatformUser(PlatformUsers.createAdmin(mgr, "admin3",
                        org));
                PlatformUser user = PlatformUsers.createUser(mgr, "user1", org);
                org.addPlatformUser(user);
                mgr.persist(org);
                mgr.flush();
                return org;
            }
        });

        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                Organization org1 = (Organization) mgr.find(org);
                org1.getVisiblePlatformUsers();
                return null;
            }
        });
    }

    @Test
    public void testGetAdministratorsNegative() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // create test data
                Organization org = Organizations.createOrganization(mgr);
                mgr.persist(org);
                org.addPlatformUser(PlatformUsers.createUser(mgr, "user1", org));

                // execute query
                Query query = mgr
                        .createNamedQuery("Organization.getAdministrators");
                query.setParameter("orgkey", new Long(org.getKey()));

                // assert
                assertEquals(0, query.getResultList().size());
                return null;
            }
        });
    }

    @Test
    public void testGetOnbehalfUsers() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization provider = Organizations.createOrganization(mgr);
                mgr.persist(provider);
                PlatformUser muser1 = PlatformUsers.createUser(mgr, "muser1",
                        provider);
                PlatformUser muser2 = PlatformUsers.createUser(mgr, "muser2",
                        provider);
                provider.addPlatformUser(muser1);
                provider.addPlatformUser(muser2);
                provider.addPlatformUser(PlatformUsers.createUser(mgr, "user1",
                        provider));

                // setup customer organization
                Organization customer = Organizations.createOrganization(mgr);
                mgr.persist(customer);
                PlatformUser suser1 = PlatformUsers.createUser(mgr, "suser1",
                        customer);
                PlatformUser suser2 = PlatformUsers.createUser(mgr, "suser2",
                        customer);
                customer.addPlatformUser(suser1);
                customer.addPlatformUser(suser2);
                customer.addPlatformUser(PlatformUsers.createUser(mgr, "user2",
                        provider));

                // setup another organization
                Organization someOrganization = Organizations
                        .createOrganization(mgr);
                mgr.persist(someOrganization);
                PlatformUser anotherTempUser = PlatformUsers.createUser(mgr,
                        "suser2_1", someOrganization);
                someOrganization.addPlatformUser(anotherTempUser);

                // setup user references
                OnBehalfUserReference onBehalfReference1 = new OnBehalfUserReference();
                onBehalfReference1.setMasterUser(muser1);
                onBehalfReference1.setSlaveUser(suser1);
                mgr.persist(onBehalfReference1);
                suser1.setMaster(onBehalfReference1);

                OnBehalfUserReference onBehalfReference2 = new OnBehalfUserReference();
                onBehalfReference2.setMasterUser(muser2);
                onBehalfReference2.setSlaveUser(suser2);
                mgr.persist(onBehalfReference2);
                suser2.setMaster(onBehalfReference2);

                OnBehalfUserReference onBehalfReference3 = new OnBehalfUserReference();
                onBehalfReference3.setMasterUser(muser1);
                onBehalfReference3.setSlaveUser(anotherTempUser);
                mgr.persist(onBehalfReference3);
                anotherTempUser.setMaster(onBehalfReference3);

                // assert
                List<PlatformUser> onbehalfUsers = provider
                        .getOnBehalfUsersFor(customer);
                assertEquals(2, onbehalfUsers.size());
                assertTrue(onbehalfUsers.contains(suser1));
                assertTrue(onbehalfUsers.contains(suser2));
                return null;
            }
        });

    }

    @Test
    public void testCallMethods() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.getLdapUserAttributes();
                org.getPaymentTypes(OrganizationRoleType.PLATFORM_OPERATOR
                        .name());
                org.getUsableSubscriptionsForProduct(new Product());

                return null;
            }
        });

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCallMethods_copy() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.getLdapUserAttributes();
                org.getPaymentTypes(OrganizationRoleType.PLATFORM_OPERATOR
                        .name());
                Product product = new Product();
                product.setType(ServiceType.TEMPLATE);
                product.setVendor(org);
                Product copy = product.copyForSubscription(orgWithOneSub, new Subscription());
                org.getUsableSubscriptionsForProduct(copy);

                return null;
            }
        });

    }

    @Test
    public void testSetUdaDefinition() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.setUdaDefinitions(null);
                return null;
            }
        });

    }

    @Test
    public void testSetDefineVatRates() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.setDefinedVatRates(null);
                return null;
            }
        });

    }

    @Test
    public void testSetMarketPlaces() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.setMarketplaces(null);
                return null;
            }
        });

    }

    @Test
    public void testSetBillingContacts() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.setBillingContacts(null);
                return null;
            }
        });

    }

    @Test
    public void testSetMarketplaceToOrganizations() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.setMarketplaceToOrganizations(null);
                return null;
            }
        });

    }

    @Test
    public void testSetPspAccounts() throws Exception {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                // setup technology provider
                Organization org = Organizations.createOrganization(mgr);
                org.setPspAccounts(null);
                return null;
            }
        });

    }

    private Organization prepareUdaDefinitions() throws Exception {
        Organization org = runTX(new Callable<Organization>() {

            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                List<UdaDefinition> definitions = new ArrayList<UdaDefinition>();
                UdaDefinition uda1 = Udas.createUdaDefinition(mgr, org,
                        UdaTargetType.CUSTOMER, "UdaDefCust1",
                        "UdaDefCust1_Default", UdaConfigurationType.SUPPLIER);
                UdaDefinition uda2 = Udas.createUdaDefinition(mgr, org,
                        UdaTargetType.CUSTOMER, "UdaDefCust2",
                        "UdaDefCust2_Default",
                        UdaConfigurationType.USER_OPTION_MANDATORY);
                UdaDefinition uda3 = Udas.createUdaDefinition(mgr, org,
                        UdaTargetType.CUSTOMER, "UdaDefCust3",
                        "UdaDefCust3_Default",
                        UdaConfigurationType.USER_OPTION_OPTIONAL);
                definitions.add(uda1);
                definitions.add(uda2);
                definitions.add(uda3);
                org.setUdaDefinitions(definitions);
                return org;
            }
        });
        return org;
    }

    @Test
    public void getReadableUdaDefinitions_MARKETPLACE_OWNER() throws Exception {
        testGetReadableUdaDefinitionsForNoneResult(OrganizationRoleType.MARKETPLACE_OWNER);
    }

    @Test
    public void getReadableUdaDefinitions_PLATFORM_OPERATOR() throws Exception {
        testGetReadableUdaDefinitionsForNoneResult(OrganizationRoleType.PLATFORM_OPERATOR);
    }

    @Test
    public void getReadableUdaDefinitions_TECHNOLOGY_PROVIDER()
            throws Exception {
        testGetReadableUdaDefinitionsForNoneResult(OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    @Test
    public void getReadableUdaDefinitions_CUSTOMER() throws Exception {
        Organization org = prepareUdaDefinitions();
        List<UdaDefinition> udaDefinitions = org.getUdaDefinitions();
        List<UdaDefinition> results = org
                .getReadableUdaDefinitions(OrganizationRoleType.CUSTOMER);
        assertNotNull(results);
        // customer can not get the UdaDefinition with
        // UdaConfigurationType.SUPPLIER
        assertEquals(results.size(), 2);
        assertSame(udaDefinitions.get(1), results.get(0));
        assertSame(udaDefinitions.get(2), results.get(1));
    }

    @Test
    public void getReadableUdaDefinitions_SUPPLIER() throws Exception {
        Organization org = prepareUdaDefinitions();
        List<UdaDefinition> udaDefinitions = org.getUdaDefinitions();
        List<UdaDefinition> results = org
                .getReadableUdaDefinitions(OrganizationRoleType.SUPPLIER);
        assertNotNull(results);
        assertEquals(results.size(), 3);
        assertSame(udaDefinitions.get(0), results.get(0));
        assertSame(udaDefinitions.get(1), results.get(1));
        assertSame(udaDefinitions.get(2), results.get(2));
    }

    private void testGetReadableUdaDefinitionsForNoneResult(
            OrganizationRoleType type) throws Exception {
        Organization org = prepareUdaDefinitions();
        List<UdaDefinition> results = org.getReadableUdaDefinitions(type);
        assertNotNull(results);
        assertEquals(results.size(), 0);
    }

    @Test
    public void getVendorRoleForPaymentConfiguration_Supplier() {
        Organization org = new Organization();
        Organizations.grantOrganizationRole(org, OrganizationRoleType.SUPPLIER);

        assertEquals(OrganizationRoleType.SUPPLIER,
                org.getVendorRoleForPaymentConfiguration());
    }

    @Test
    public void getVendorRoleForPaymentConfiguration_Reseller() {
        Organization org = new Organization();
        Organizations.grantOrganizationRole(org, OrganizationRoleType.RESELLER);

        assertEquals(OrganizationRoleType.RESELLER,
                org.getVendorRoleForPaymentConfiguration());
    }

    @Test
    public void getVendorRoleForPaymentConfiguration_None() {
        Organization org = new Organization();
        Organizations.grantOrganizationRole(org, OrganizationRoleType.CUSTOMER);
        Organizations.grantOrganizationRole(org, OrganizationRoleType.BROKER);
        Organizations.grantOrganizationRole(org,
                OrganizationRoleType.PLATFORM_OPERATOR);

        assertNull(org.getVendorRoleForPaymentConfiguration());
    }

    @Test
    public void getCustomersOfReseller_Empty() {
        Organization org = new Organization();

        assertTrue(org.getCustomersOfReseller().isEmpty());
    }

    @Test
    public void getCustomersOfReseller_SupplierRelation() {
        Organization org = new Organization();
        OrganizationReferences.addReference(org, new Organization(),
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

        assertTrue(org.getCustomersOfReseller().isEmpty());
    }

    @Test
    public void getCustomersOfReseller() {
        Organization customer = new Organization();
        Organization reseller = new Organization();
        OrganizationReferences.addReference(reseller, customer,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER);

        List<Organization> customersOfReseller = reseller
                .getCustomersOfReseller();

        assertEquals(1, customersOfReseller.size());
        assertTrue(customersOfReseller.contains(customer));
    }

    @Test
    public void getCustomerReference_Empty() {
        Organization customer = new Organization();
        Organization seller = new Organization();

        assertNull(seller.getCustomerReference(customer,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER));
        assertNull(seller.getCustomerReference(customer,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER));
    }

    @Test
    public void getCustomerReference_Supplier() {
        Organization customer = new Organization();
        Organization seller = new Organization();
        OrganizationReference ref = OrganizationReferences
                .addSupplierReference(seller, customer);

        assertEquals(ref, seller.getCustomerReference(customer,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER));
        assertNull(seller.getCustomerReference(customer,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER));
    }

    @Test
    public void getCustomerReference_Reseller() {
        Organization customer = new Organization();
        Organization seller = new Organization();
        OrganizationReference ref = OrganizationReferences.addReference(seller,
                customer, OrganizationReferenceType.RESELLER_TO_CUSTOMER);

        assertNull(seller.getCustomerReference(customer,
                OrganizationReferenceType.SUPPLIER_TO_CUSTOMER));
        assertEquals(ref, seller.getCustomerReference(customer,
                OrganizationReferenceType.RESELLER_TO_CUSTOMER));
    }

    @Test
    public void getUsableSubscriptionsForProduct_subscription() {
        Organization org = new Organization();
        Product product = createProductTemplate();

        Subscription subscription = createSubscription(product, org,
                ServiceType.SUBSCRIPTION);
        Subscription anotherSubscription = createAnotherSubscription();

        org.setSubscriptions(Arrays.asList(new Subscription[] { subscription,
                anotherSubscription }));

        // when
        List<Subscription> subscrList = org
                .getUsableSubscriptionsForProduct(product);

        // then
        assertEquals("Only the subscription refers to the template", 1,
                subscrList.size());
        assertSame("Wrong subscription delivered", subscription,
                subscrList.get(0));
    }

    @Test
    public void getUsableSubscriptionsForProduct_partnerSubscription() {
        // given
        Organization org = new Organization();
        Product product = createProductTemplate();
        Product partnerTemplate = product.copyForResale(new Organization());

        Subscription partnerSubscription = createSubscription(partnerTemplate,
                org, ServiceType.PARTNER_SUBSCRIPTION);
        Subscription anotherSubscription = createAnotherSubscription();
        org.setSubscriptions(Arrays.asList(new Subscription[] {
                partnerSubscription, anotherSubscription }));

        // when
        List<Subscription> subscrList = org
                .getUsableSubscriptionsForProduct(product);

        // then
        assertEquals("Only the partner subscription refers to the template", 1,
                subscrList.size());
        assertSame("Wrong subscription delivered", partnerSubscription,
                subscrList.get(0));
    }

    private Product createProductTemplate() {
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setTemplate(null);
        return product;
    }

    private Subscription createSubscription(Product product, Organization org,
            ServiceType productCopyType) {
        Subscription sub = new Subscription();
        Product subscriptionCopy = product.copyForSubscription(org, sub);
        subscriptionCopy.setType(productCopyType);

        sub.setProduct(subscriptionCopy);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        return sub;
    }

    private Subscription createAnotherSubscription() {
        Subscription anotherSubscription = new Subscription();
        anotherSubscription.setProduct(new Product());
        anotherSubscription.setStatus(SubscriptionStatus.ACTIVE);
        return anotherSubscription;
    }

    /**
     * Create a supplier with an operator revenue share and read it again. Check
     * if also an OrganizationHistory object is created referencing the same
     * operator revenue share.
     * 
     * @throws Throwable
     */
    @Test
    public void addSupplierWithOperatorPriceModel() throws Throwable {
        // Given, When
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                createSupplierWithOperatorPriceModel();
                return null;
            }
        });

        // Then
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                checkSupplierWithOperatorPriceModel();
                return null;
            }
        });
    }

    private void createSupplierWithOperatorPriceModel()
            throws NonUniqueBusinessKeyException {
        domObjects.clear();

        // Create new Organization withg operator price model
        RevenueShareModel operatorPriceModel = new RevenueShareModel();
        operatorPriceModel.setRevenueShare(BigDecimal.ZERO);
        operatorPriceModel
                .setRevenueShareModelType(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        mgr.persist(operatorPriceModel);

        Organization suppl = new Organization();
        suppl.setOrganizationId("Supplier");
        suppl.setCutOffDay(1);
        suppl.setOperatorPriceModel(operatorPriceModel);
        mgr.persist(suppl);

        domObjects.add((Organization) ReflectiveClone.clone(suppl));
    }

    private void checkSupplierWithOperatorPriceModel() {
        Organization original = (Organization) domObjects.get(0);

        Organization saved = new Organization();
        saved.setOrganizationId(original.getOrganizationId());
        saved = (Organization) mgr.find(saved);
        Assert.assertNotNull("Cannot find '" + original.getOrganizationId()
                + "' in DB", saved);

        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, original),
                ReflectiveCompare.compare(saved, original));

        Assert.assertNotNull("operator price model missing",
                saved.getOperatorPriceModel());
        Assert.assertEquals(
                "Wrong operator revenue share",
                0,
                saved.getOperatorPriceModel()
                        .getRevenueShare()
                        .compareTo(
                                original.getOperatorPriceModel()
                                        .getRevenueShare()));

        // Load history object and check if it also has the operator price model
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertFalse(
                "History entry empty for organization "
                        + saved.getOrganizationId(), histObjs.isEmpty());
        Assert.assertEquals("One history entry expected for organization "
                + saved.getOrganizationId(), 1, histObjs.size());

        OrganizationHistory orgHistory = (OrganizationHistory) histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, orgHistory.getModtype());
        Assert.assertEquals("modUser wrong", USER_GUEST,
                orgHistory.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgHistory),
                ReflectiveCompare.compare(saved, orgHistory));
        Assert.assertEquals("Wrong object key in history", saved.getKey(),
                orgHistory.getObjKey());
        Assert.assertEquals("Operator price model key mismatch", orgHistory
                .getOperatorPriceModelObjKey().longValue(), saved
                .getOperatorPriceModel().getKey());
    }

    @Test
    public void deleteSupplierWithOperatorPriceModel() throws Throwable {
        // Given
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                createSupplierWithOperatorPriceModel();
                return null;
            }
        });

        // When
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                deleteOrganization();
                return null;
            }
        });

        // Then
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                checkDeletedOrganizationWithOperatorPriceModel();
                return null;
            }
        });
    }

    private void deleteOrganization() {
        Organization org = new Organization();
        org.setOrganizationId(((Organization) domObjects.get(0))
                .getOrganizationId());
        org = (Organization) mgr.find(org);
        domObjects.clear();
        domObjects.add((Organization) ReflectiveClone.clone(org));
        mgr.remove(org);
    }

    private void checkDeletedOrganizationWithOperatorPriceModel() {
        Organization deletedOrg = (Organization) domObjects.get(0);

        // Try to load the deleted organization
        Organization saved = new Organization();
        saved.setOrganizationId(deletedOrg.getOrganizationId());
        saved = (Organization) mgr.find(saved);
        Assert.assertNull(
                "Deleted Organization '" + deletedOrg.getOrganizationId()
                        + "' can still be accessed via DataManager.find", saved);

        // Try to load the deleted operator price model
        RevenueShareModel savedOpPriceModel = mgr.find(RevenueShareModel.class,
                deletedOrg.getOperatorPriceModel().getKey());
        Assert.assertNull("Deleted operator price model of organization '"
                + deletedOrg.getOrganizationId()
                + "' can still be accessed via DataManager.find",
                savedOpPriceModel);

        // Load Organization history objects and check them
        List<DomainHistoryObject<?>> orgHistObjs = mgr.findHistory(deletedOrg);
        Assert.assertNotNull("History entry 'null' for organization "
                + deletedOrg.getOrganizationId(), orgHistObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for organization "
                        + deletedOrg.getOrganizationId(), 2, orgHistObjs.size());
        Assert.assertEquals("Wrong object key in history", deletedOrg.getKey(),
                orgHistObjs.get(1).getObjKey());
        Assert.assertEquals(ModificationType.DELETE, orgHistObjs.get(1)
                .getModtype());

        // Load revenue share model history objects and check them
        List<DomainHistoryObject<?>> revShmHistObjs = mgr
                .findHistory(deletedOrg.getOperatorPriceModel());
        Assert.assertNotNull("History entry 'null' for operator price model ",
                revShmHistObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for operator price model ",
                2, revShmHistObjs.size());
        Assert.assertEquals("Wrong object key in history", deletedOrg
                .getOperatorPriceModel().getKey(), revShmHistObjs.get(1)
                .getObjKey());
        Assert.assertEquals(ModificationType.DELETE, revShmHistObjs.get(1)
                .getModtype());

    }

    private Organization givenOrgDefaultTenant(final String name) throws Exception {
        return givenOrgTenant(name, null);   
    }
        
    private Organization givenOrgTenant(final String name, final String tenantId)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganizationWithTenant(
                        UUID.randomUUID().toString(), name, tenantId, mgr);
                mgr.persist(org);
                return org;
            }
        });
    }

    private Organization givenOrgExistingTenant(final String name, final Tenant tenant)
            throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganizationWithTenant(
                        UUID.randomUUID().toString(), name, tenant, mgr);
                mgr.persist(org);
                return org;
            }
        });
    }

}
