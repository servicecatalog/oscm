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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.operationslog.UserOperationLogQuery;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Tests of the organization-related domain objects (incl. auditing
 * functionality)
 * 
 * @author schmid
 * 
 */
public class PlatformUserIT extends UserOperationLogQueryTestBase {

    private List<PlatformUser> users = new ArrayList<PlatformUser>();
    private List<Organization> organizations = new ArrayList<Organization>();

    /**
     * <b>Testcase:</b> Add new Platform user objects <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>All objects can be retrieved from DB and are identical to provided
     * Platform user objects</li>
     * <li>Relations to the organization are set (bidirectional)</li>
     * <li>A history object is created for each PlatformUser stored</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestAdd() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        users.clear();
        organizations.clear();
        // first add 2 organizations
        int CUST_COUNT = 2;
        Organization[] cust = new Organization[CUST_COUNT + 1];
        for (int i = 1; i <= CUST_COUNT; i++) {
            cust[i] = Organizations.createOrganization(mgr);
            cust[i].setOrganizationId("PUTestAdd" + i);
            cust[i].setName("PUTest Organization" + i);
            cust[i].setAddress("Address " + i);
            cust[i].setEmail("EMail " + i);
            cust[i].setPhone("012345/678" + i + i + i);
            mgr.persist(cust[i]);
        }
        // now add 1 platform user to first organization
        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("AddName1");
        usr1.setAddress("Address1");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail1");
        usr1.setFirstName("Arnold");
        usr1.setLastName("Schwarzenegger");
        usr1.setUserId("usr1");
        usr1.setPhone("111111/111111");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setOrganization(cust[1]);
        usr1.setLocale("en");
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        cust[1].addPlatformUser(usr1);
        mgr.persist(usr1);
        users.add((PlatformUser) ReflectiveClone.clone(usr1));
        organizations.add((Organization) ReflectiveClone.clone(cust[1]));
        // and two more users for organization 2
        PlatformUser usr2 = new PlatformUser();
        usr2.setAdditionalName("AddName2");
        usr2.setAddress("Address2");
        usr2.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr2.setEmail("EMail2");
        usr2.setFirstName("Barack");
        usr2.setLastName("Obama");
        usr2.setUserId("usr2");
        usr2.setPhone("222222/222222");
        usr2.setStatus(UserAccountStatus.ACTIVE);
        usr2.setLocale("en");
        usr2.setOrganization(cust[2]);
        usr2.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr2);
        users.add((PlatformUser) ReflectiveClone.clone(usr2));
        organizations.add((Organization) ReflectiveClone.clone(cust[2]));
        PlatformUser usr3 = new PlatformUser();
        usr3.setAdditionalName("AddName3");
        usr3.setAddress("Address3");
        usr3.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr3.setEmail("EMail3");
        usr3.setFirstName("Hillary");
        usr3.setLastName("Clinton");
        usr3.setUserId("usr3");
        usr3.setPhone("333333/333333");
        usr3.setStatus(UserAccountStatus.ACTIVE);
        usr3.setLocale("en");
        usr3.setOrganization(cust[2]);
        usr3.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr3);
        users.add((PlatformUser) ReflectiveClone.clone(usr3));
        organizations.add((Organization) ReflectiveClone.clone(cust[2]));
    }

    private void doTestAddCheck() {
        // Find via direct select
        PlatformUser qry = new PlatformUser();
        PlatformUser saved;
        for (int i = 0; i < users.size(); i++) {
            // Load user and check values
            PlatformUser orgPlatformUser = users.get(i);
            Organization orgOrganization = organizations.get(i);
            qry.setUserId(orgPlatformUser.getUserId());
            // qry.setFullLoginName(orgPlatformUser.getUserId());
            saved = (PlatformUser) mgr.find(qry);
            // Assert.assertNotNull("Cannot find '"
            // + orgPlatformUser.getUserId() + "' in DB", saved);
            Assert.assertNotNull("Cannot find '" + orgPlatformUser.getUserId()
                    + "' in DB", saved);
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(saved, orgPlatformUser),
                    ReflectiveCompare.compare(saved, orgPlatformUser));
            // Check relation to organization
            Assert.assertTrue(ReflectiveCompare.showDiffs(
                    saved.getOrganization(), orgOrganization),
                    ReflectiveCompare.compare(saved.getOrganization(),
                            orgOrganization));
            // Load history objects and check them
            List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
            Assert.assertNotNull("History entry 'null' for platformUser "
                    + orgPlatformUser.getUserId());
            Assert.assertFalse("History entry empty for platformUser "
                    + orgPlatformUser.getUserId(), histObjs.isEmpty());
            Assert.assertTrue(
                    "Only one history entry expected for platformUser "
                            + orgPlatformUser.getUserId(), histObjs.size() == 1);
            DomainHistoryObject<?> hist = histObjs.get(0);
            Assert.assertEquals(ModificationType.ADD, hist.getModtype());
            Assert.assertEquals("modUser", "guest", hist.getModuser());
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(orgPlatformUser, hist),
                    ReflectiveCompare.compare(orgPlatformUser, hist));
            Assert.assertEquals("OBJID in history different",
                    orgPlatformUser.getKey(), hist.getObjKey());
        }
        // Check availability via Organization
        Organization qryCust = new Organization();
        qryCust.setOrganizationId("PUTestAdd1");
        Organization savedCust = (Organization) mgr.find(qryCust);

        Assert.assertNotNull(
                "Cannot find Organization '" + qryCust.getOrganizationId()
                        + "' in DB", savedCust);
        List<PlatformUser> userlist = savedCust.getPlatformUsers();
        // Organization 1 should have exactly 1 PlatformUser
        Assert.assertTrue("Organization 1 should contain exactly 1 user",
                userlist.size() == 1);
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(userlist.get(0), users.get(0)),
                ReflectiveCompare.compare(userlist.get(0), users.get(0)));
        // Check Organization2
        qryCust.setOrganizationId("PUTestAdd2");
        savedCust = (Organization) mgr.find(qryCust);

        Assert.assertNotNull(
                "Cannot find Organization '" + qryCust.getOrganizationId()
                        + "' in DB", savedCust);
        userlist = savedCust.getPlatformUsers();
        // Organization 2 should have exactly 2 PlatformUsers
        Assert.assertTrue("Organization 2 should contain exactly 2 user",
                userlist.size() == 2);
        // As we do not know the sequence, in which we retrieve the related
        // users from the database, we have to lookup the whole list
        if (userlist.get(0).getUserId().equals("usr2")) {
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(userlist.get(0), users.get(1)),
                    ReflectiveCompare.compare(userlist.get(0), users.get(1)));
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(userlist.get(1), users.get(2)),
                    ReflectiveCompare.compare(userlist.get(1), users.get(2)));
        } else {
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(userlist.get(0), users.get(2)),
                    ReflectiveCompare.compare(userlist.get(0), users.get(2)));
            Assert.assertTrue(
                    ReflectiveCompare.showDiffs(userlist.get(1), users.get(1)),
                    ReflectiveCompare.compare(userlist.get(1), users.get(1)));
        }
    }

    /**
     * <b>Testcase:</b> Modify an existing PlatformUser object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the platformUser</li>
     * <li>No new history object for Organization</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModifyUser() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyUserPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyUser();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyUserCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestModifyUserPrepare() throws NonUniqueBusinessKeyException {
        users.clear();
        organizations.clear();
        Organization cust = new Organization();
        cust.setOrganizationId("PUTestModify");
        cust.setName("PUTest User");
        cust.setAddress("Address");
        cust.setEmail("EMail");
        cust.setPhone("012345/67813264364");
        cust.setCutOffDay(1);
        mgr.persist(cust);
        organizations.add((Organization) ReflectiveClone.clone(cust));
        // now add 1 platform user to first organization
        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("AddName1");
        usr1.setAddress("Address1");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail1");
        usr1.setFirstName("George");
        usr1.setLastName("Washington");
        usr1.setUserId("usr1");
        usr1.setPhone("111111/111111");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setLocale("en");
        usr1.setOrganization(cust);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);
    }

    private void doTestModifyUser() {
        // Change only platformUser data
        PlatformUser qryUser = new PlatformUser();
        qryUser.setUserId("usr1");
        PlatformUser usr = (PlatformUser) mgr.find(qryUser);
        usr.setAdditionalName("another one");
        usr.setEmail("mynewemail@whitehouse.gov");
        users.clear();
        users.add(usr);
        load(usr.getOrganization());
    }

    private void doTestModifyUserCheck() {
        // Load modified
        PlatformUser qryUser = new PlatformUser();
        qryUser.setUserId("usr1");
        PlatformUser saved = (PlatformUser) mgr.find(qryUser);
        PlatformUser orgUser = users.get(0);
        // Check platformUser data
        Assert.assertNotNull("Cannot find '" + orgUser.getUserId() + "' in DB",
                saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgUser),
                ReflectiveCompare.compare(saved, orgUser));
        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull(
                "History entry 'null' for platformUser " + orgUser.getUserId(),
                histObjs);
        Assert.assertFalse(
                "History entry empty for platformUser " + orgUser.getUserId(),
                histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 2 history entries expected for platformUser "
                        + orgUser.getUserId(), histObjs.size() == 2);
        // load modified history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.MODIFY, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgUser, hist),
                ReflectiveCompare.compare(orgUser, hist));
        Assert.assertEquals("OBJID in history different", orgUser.getKey(),
                hist.getObjKey());
        // Check related organization (not cascaded !)
        Organization orgOrganization = orgUser.getOrganization();
        Organization savedCust = saved.getOrganization();
        // should be unchanged ...
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(orgOrganization, savedCust),
                ReflectiveCompare.compare(orgOrganization, savedCust));
        // ... and therefore not contain a new history entry
        histObjs = mgr.findHistory(orgOrganization);
        Assert.assertNotNull("History entry 'null' for Organization "
                + orgOrganization.getOrganizationId(), histObjs);
        Assert.assertFalse("History entry empty for Organization "
                + orgOrganization.getOrganizationId(), histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 1 history entries expected for Organization "
                        + orgOrganization.getOrganizationId(),
                histObjs.size() == 1);
    }

    /**
     * <b>Testcase:</b> Delete an existing PlatformUser object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>User is marked as deleted in DB</li>
     * <li>History object created for the platformUser</li>
     * <li>No new history object for Organization</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteUser() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteUserPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteUser();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeleteUserCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestDeleteUserPrepare() throws NonUniqueBusinessKeyException {
        users.clear();
        organizations.clear();
        Organization cust = new Organization();
        cust.setOrganizationId("PUTestDelete");
        cust.setName("PUTest User");
        cust.setAddress("Address");
        cust.setEmail("EMail");
        cust.setPhone("012345/9999999");
        cust.setCutOffDay(1);
        mgr.persist(cust);
        organizations.add((Organization) ReflectiveClone.clone(cust));
        // now add 2 platform users to first organization
        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("DeleteName1");
        usr1.setAddress("Address1");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail1");
        usr1.setFirstName("Ronald");
        usr1.setLastName("Reagan");
        usr1.setUserId("ronni");
        usr1.setPhone("111111/111111");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setLocale("en");
        usr1.setOrganization(cust);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);
        users.add((PlatformUser) ReflectiveClone.clone(usr1));
        PlatformUser usr2 = new PlatformUser();
        usr2.setAdditionalName("DeleteName2");
        usr2.setAddress("Address2");
        usr2.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr2.setEmail("EMail2");
        usr2.setFirstName("Bill");
        usr2.setLastName("Clinton");
        usr2.setUserId("billy");
        usr2.setPhone("222222/222222");
        usr2.setStatus(UserAccountStatus.ACTIVE);
        usr2.setLocale("en");
        usr2.setOrganization(cust);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr2);
        users.add((PlatformUser) ReflectiveClone.clone(usr2));
    }

    private void doTestDeleteUser() {
        // Change only platformUser data
        PlatformUser qryUser = new PlatformUser();
        qryUser.setUserId("ronni");
        PlatformUser usr = (PlatformUser) mgr.find(qryUser);
        mgr.remove(usr);
    }

    private void doTestDeleteUserCheck() {
        // Try to load deleted
        PlatformUser orgUser = users.get(0);
        PlatformUser qryUser = new PlatformUser();
        qryUser.setUserId("ronni");
        PlatformUser saved = (PlatformUser) mgr.find(qryUser);
        // Check organization data
        Assert.assertNull("Deleted PlatformUser '" + orgUser.getUserId()
                + "' can still be accessed via DataManager.find", saved);
        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(orgUser);
        Assert.assertNotNull(
                "History entry 'null' for platformUser " + orgUser.getUserId(),
                histObjs);
        Assert.assertFalse(
                "History entry empty for platformUser " + orgUser.getUserId(),
                histObjs.isEmpty());
        Assert.assertTrue(
                "Exactly 2 history entries expected for platformUser "
                        + orgUser.getUserId(), histObjs.size() == 2);
        // load modified history object (should be second)
        DomainHistoryObject<?> hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());
        Assert.assertTrue(ReflectiveCompare.showDiffs(orgUser, hist),
                ReflectiveCompare.compare(orgUser, hist));
        Assert.assertEquals("OBJID in history different", orgUser.getKey(),
                hist.getObjKey());
        // Organization should be unchanged, i.e. it can be found
        Organization qryOrganization = new Organization();
        qryOrganization.setOrganizationId("PUTestDelete");
        Organization orgOrganization = organizations.get(0);
        Organization savedCust = (Organization) mgr.find(qryOrganization);
        // Check organization data
        Assert.assertNotNull(
                "Cannot find '" + orgOrganization.getOrganizationId()
                        + "' in DB", savedCust);
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedCust, orgOrganization),
                ReflectiveCompare.compare(savedCust, orgOrganization));
        // and has exactly one history object
        histObjs = mgr.findHistory(savedCust);
        Assert.assertNotNull("History entry 'null' for organization "
                + orgOrganization.getOrganizationId(), histObjs);
        Assert.assertFalse("History entry empty for organization "
                + orgOrganization.getOrganizationId(), histObjs.isEmpty());
        Assert.assertTrue("Exactly 1 history entry expected for organization "
                + orgOrganization.getOrganizationId(), histObjs.size() == 1);
        // And the same check for the second PlatformUser
        qryUser = new PlatformUser();
        qryUser.setUserId("billy");
        saved = (PlatformUser) mgr.find(qryUser);
        orgUser = users.get(1);
        // Check platformUser data
        Assert.assertNotNull("Cannot find '" + orgUser.getUserId() + "' in DB",
                saved);
        Assert.assertTrue(ReflectiveCompare.showDiffs(saved, orgUser),
                ReflectiveCompare.compare(saved, orgUser));
        // Load history objects and check them
        histObjs = mgr.findHistory(saved);
        Assert.assertNotNull(
                "History entry 'null' for platformUser " + orgUser.getUserId(),
                histObjs);
        Assert.assertFalse(
                "History entry empty for platformUser " + orgUser.getUserId(),
                histObjs.isEmpty());
        Assert.assertTrue("Exactly 1 history entry expected for platformUser "
                + orgUser.getUserId(), histObjs.size() == 1);
    }

    /**
     * <b>Testcase:</b> Try to insert two Users with the same login for the same
     * organization<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraintSameOrganization() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraint(0);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestViolateUniqueConstraintPrepare()
            throws NonUniqueBusinessKeyException {
        organizations.clear();

        // Organization 1
        Organization cust1 = new Organization();
        cust1.setOrganizationId("PUTestViolateUniqueConstraintPrepare");
        cust1.setName("PUTest User");
        cust1.setAddress("Address");
        cust1.setEmail("EMail");
        cust1.setPhone("012345/67813264364");
        cust1.setCutOffDay(1);
        organizations.add((Organization) ReflectiveClone.clone(cust1));
        mgr.persist(cust1);

        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("sen.");
        usr1.setAddress("Address1");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail1");
        usr1.setFirstName("George");
        usr1.setLastName("Bush");
        usr1.setUserId("usr1");
        usr1.setPhone("111111/111111");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setLocale("en");
        usr1.setOrganization(cust1);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);

        // Organization 2
        Organization cust2 = new Organization();
        cust2.setOrganizationId("PUTestViolateUniqueConstraintPrepare2");
        cust2.setName("PUTest User2");
        cust2.setAddress("Address");
        cust2.setEmail("EMail");
        cust2.setPhone("012345/67813264364");
        cust2.setCutOffDay(1);
        organizations.add((Organization) ReflectiveClone.clone(cust2));
        mgr.persist(cust2);

        PlatformUser usr2 = new PlatformUser();
        usr2.setAdditionalName("sen.");
        usr2.setAddress("Address1");
        usr2.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr2.setEmail("EMail1");
        usr2.setFirstName("Barack");
        usr2.setLastName("Obama");
        usr2.setUserId("usr2");
        usr2.setPhone("111111/111111");
        usr2.setStatus(UserAccountStatus.ACTIVE);
        usr2.setLocale("en");
        usr2.setOrganization(cust2);
        usr2.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr2);
    }

    private void doTestViolateUniqueConstraint(int organizationIndex)
            throws NonUniqueBusinessKeyException {
        Organization saved = (Organization) mgr.find(organizations
                .get(organizationIndex));
        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("jun.");
        usr1.setAddress("Address2");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail2");
        usr1.setFirstName("George W.");
        usr1.setLastName("Bush");
        usr1.setUserId("usr1");
        usr1.setPhone("222222/2222222");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setOrganization(saved);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);
    }

    /**
     * <b>Testcase:</b> Try to insert two Users with the same login for the same
     * organization<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraintSameOrganizationInOneTX()
            throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintInOneTX();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestViolateUniqueConstraintInOneTX()
            throws NonUniqueBusinessKeyException {
        doTestViolateUniqueConstraintPrepare();

        Organization cust = organizations.get(0);

        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("jun.");
        usr1.setAddress("Address2");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail2");
        usr1.setFirstName("George W.");
        usr1.setLastName("Bush");
        usr1.setUserId("usr1");
        usr1.setPhone("222222/2222222");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setLocale("en");
        usr1.setOrganization(cust);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);
    }

    private void doTestViolateUniqueConstraintDifferentOrganizationInOneTX()
            throws NonUniqueBusinessKeyException {
        doTestViolateUniqueConstraintPrepare();

        Organization cust = organizations.get(1);

        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("jun.");
        usr1.setAddress("Address2");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail2");
        usr1.setFirstName("George W.");
        usr1.setLastName("Bush");
        usr1.setUserId("usr1");
        usr1.setPhone("222222/2222222");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setLocale("en");
        usr1.setOrganization(cust);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);
    }

    /**
     * <b>Testcase:</b> Try to insert User without a related Organization<br>
     * <b>ExpectedResult:</b> PersistenceException
     * 
     * @throws Throwable
     */
    @Test(expected = EJBTransactionRolledbackException.class)
    public void testUserWithoutOrganization() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestUserWithoutOrganization();
                return null;
            }
        });
    }

    private void doTestUserWithoutOrganization()
            throws NonUniqueBusinessKeyException {
        PlatformUser usr1 = new PlatformUser();
        usr1.setAddress("Address1");
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail1");
        usr1.setFirstName("Angela");
        usr1.setLastName("Merkel");
        usr1.setUserId("angela");
        usr1.setPhone("111111/111111");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setPasswordRecoveryStartDate(1360046301085L);
        mgr.persist(usr1);
    }

    /**
     * Load platform users with query.
     */
    @Test
    public void testListByEmail() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestListByEmailPrepare();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestListByEmail();
                return null;
            }
        });
    }

    private void doTestListByEmailPrepare() throws Exception {
        Organization org = Organizations.createOrganization(mgr);
        Organizations.createUserForOrg(mgr, org, true, "user1");
        Organizations.createUserForOrg(mgr, org, true, "user2");
    }

    private void doTestListByEmail() throws Exception {
        Query query = mgr.createNamedQuery("PlatformUser.listByEmail");
        query.setParameter("email", "admin@organization.com");
        assertEquals(2, query.getResultList().size());
    }

    /**
     * <b>Testcase:</b> Try to insert two Users with the same login for the same
     * organization<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraintDifferentOrganization()
            throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintPrepare();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraint(1);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /**
     * <b>Testcase:</b> Try to insert two Users with the same login for
     * different organizations<br>
     * <b>ExpectedResult:</b> SaasNonUniqueBusinessKeyException
     * 
     * @throws Throwable
     */
    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testViolateUniqueConstraintDifferentOrganizationInOneTX()
            throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestViolateUniqueConstraintDifferentOrganizationInOneTX();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    /**
     * Bug re-test. Platform user has roles as well as organizations have roles.
     * When loading platform user eager the resulting SQL query contains the
     * Cartesian product of user roles and organization roles. This may lead to
     * duplicate list entries. This test ensures that no duplicate entries
     * exist.
     * 
     * @throws Exception
     */
    @Test
    public void testEagerLoading() throws Exception {
        // create user with two roles and organization with two roles
        final Long key = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                PlatformUser admin = PlatformUsers.createAdmin(mgr, "user");
                PlatformUsers.grantRoles(mgr, admin,
                        UserRoleType.PLATFORM_OPERATOR);
                Organization org = admin.getOrganization();
                OrganizationRole techProv = new OrganizationRole(
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                mgr.persist(techProv);
                Organizations.addOrganizationToRole(mgr, org,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return Long.valueOf(admin.getKey());
            }
        });

        // when loading the user the organization must have only two roles in
        // the grantedRoles list
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser reloadedAdmin = mgr.find(PlatformUser.class, key);
                assertEquals(2, reloadedAdmin.getOrganization()
                        .getGrantedRoles().size());
                return null;
            }
        });
    }

    /**
     * Try loading a user twice in the same transaction. The same object must be
     * returned. But when loading it within two transactions, two objects must
     * be returned.
     * 
     * @throws Exception
     */
    @Test
    public void testLoadCurrentUserTwiceAndWithinDifferntTransactions()
            throws Exception {
        // create user, login, and get the current user
        final PlatformUser user1 = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                addOrganization();
                addUser();
                // login with the userId instead of userKey,
                // so the userKey is written as history modDate
                container.login(user.getKey());
                final PlatformUser user = mgr.getCurrentUser();
                // users must be the same object
                assertTrue(user == mgr.getCurrentUser());
                return user;
            }
        });

        // login again and get the current user
        final PlatformUser user2 = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                container.login(user.getKey());
                final PlatformUser user = mgr.getCurrentUser();
                // here again, users must be the same object
                assertTrue(user == mgr.getCurrentUser());
                return user;
            }
        });

        // users must be different
        assertTrue(user1 != user2);
    }

    @Override
    protected UserOperationLogQuery getQuery() {
        return null;
    }

}
