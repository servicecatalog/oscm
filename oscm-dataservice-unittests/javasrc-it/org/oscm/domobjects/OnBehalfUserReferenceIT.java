/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                
 *                                                                              
 *  Creation Date: 26.05.2011                                                     
 *                                                                              
 *  Completion Time: 26.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.PlatformUsers;

/**
 * Tests for the domain object representing an relation entity for on behalf
 * user.
 * 
 * @author tokoda
 */
public class OnBehalfUserReferenceIT extends DomainObjectTestBase {

    private static Random random = new Random();

    PlatformUser masterUser;
    PlatformUser slaveUser;
    private OnBehalfUserReference onBehalfUserReference;

    /**
     * <b>Test case:</b> Add a new reference object for on behalf user <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The relation entity can be retrieved from DB and is identical to the
     * provided object</li>
     * <li>A history object is created for the reference entry</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testAdd() throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOnBehalfUserReference();
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
    }

    private void createOnBehalfUserReference() throws Exception {
        masterUser = PlatformUsers.createUser(mgr,
                "masterUser_" + random.nextInt());

        slaveUser = PlatformUsers.createUser(mgr,
                "slaveUser_" + random.nextInt());

        onBehalfUserReference = new OnBehalfUserReference();
        onBehalfUserReference.setLastAccessTime(new Date().getTime());
        onBehalfUserReference.setMasterUser(masterUser);
        onBehalfUserReference.setSlaveUser(slaveUser);
        mgr.persist(onBehalfUserReference);
    }

    private void doTestAddCheck() {
        OnBehalfUserReference savedReference = loadOnBehalfUserReference();

        // check the values
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedReference,
                onBehalfUserReference), ReflectiveCompare.compare(
                savedReference, onBehalfUserReference));

        // check cascaded objects
        PlatformUser savedMasterUser = savedReference.getMasterUser();
        PlatformUser orgMasterUser = onBehalfUserReference.getMasterUser();
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedMasterUser, orgMasterUser),
                ReflectiveCompare.compare(savedMasterUser, orgMasterUser));

        PlatformUser savedSlaveUser = savedReference.getSlaveUser();
        PlatformUser orgSlaveUser = onBehalfUserReference.getSlaveUser();
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedSlaveUser, orgSlaveUser),
                ReflectiveCompare.compare(savedSlaveUser, orgSlaveUser));
    }

    private OnBehalfUserReference loadOnBehalfUserReference() {
        // load the previously persisted UserToOnBehalfUser
        OnBehalfUserReference savedReference = mgr.find(
                OnBehalfUserReference.class,
                Long.valueOf(onBehalfUserReference.getKey()));
        return savedReference;
    }

    /**
     * <b>Testcase:</b> Modify a reference object for on behalf user <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>Modification is saved to the DB</li>
     * <li>History object created for the product review</li>
     * <li>MasterUser unchanged</li>
     * <li>No new history object for PlatformUser</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testModify() throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOnBehalfUserReference();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                onBehalfUserReference = loadOnBehalfUserReference();
                onBehalfUserReference.setLastAccessTime(newAccessTime());
                masterUser = onBehalfUserReference.getMasterUser();
                slaveUser = onBehalfUserReference.getSlaveUser();
                load(masterUser);
                load(slaveUser);
                return null;
            }

        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestModifyCheck();
                return null;
            }
        });

    }

    private void doTestModifyCheck() throws Exception {
        OnBehalfUserReference savedReference = loadOnBehalfUserReference();

        // check the values
        Assert.assertTrue(ReflectiveCompare.showDiffs(savedReference,
                onBehalfUserReference), ReflectiveCompare.compare(
                savedReference, onBehalfUserReference));

        // Check cascaded objects
        PlatformUser savedMasterUser = savedReference.getMasterUser();
        PlatformUser orgMasterUser = masterUser;
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedMasterUser, orgMasterUser),
                ReflectiveCompare.compare(savedMasterUser, orgMasterUser));

        PlatformUser savedSlaveUser = savedReference.getSlaveUser();
        PlatformUser orgSlaveUser = slaveUser;
        Assert.assertTrue(
                ReflectiveCompare.showDiffs(savedSlaveUser, orgSlaveUser),
                ReflectiveCompare.compare(savedSlaveUser, orgSlaveUser));

    }

    /**
     * We do not need the correct system time for this test. Just change the
     * value. Windows system time is not very precise. Fix value is more secure
     * in this case.
     */
    private long newAccessTime() {
        return 10;
    }

    /**
     * <b>Testcase:</b> Delete an existing OnBehalfUserReference object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>OnBehalfUserReference marked as deleted in the DB</li>
     * <li>PlatformUser as slave user marked as deleted in the DB</li>
     * <li>History object created for the deleted OnBehalfUserReference</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteReferenceCascade() throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOnBehalfUserReference();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                onBehalfUserReference = loadOnBehalfUserReference();
                unproxyEntity(unproxyEntity(onBehalfUserReference).getMasterUser());
                mgr.remove(onBehalfUserReference);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestDeleteOnBehalfUserReferenceCheck();
                doTestMasterUserNotDeletedCheck();
                return null;
            }
        });

    }

    private void doTestDeleteOnBehalfUserReferenceCheck() throws Exception {
        OnBehalfUserReference savedReference = loadOnBehalfUserReference();

        // check ProductReview deletion
        Assert.assertNull("Deleted OnBehalfUserReference '"
                + onBehalfUserReference.getKey()
                + "' can still be accessed via DataManager.find",
                savedReference);

        PlatformUser savedUser = loadPlatformUser(false, slaveUser);

        // check the PlatformUser of slaveUser deletion
        Assert.assertNull(
                "Deleted PlatformUser of slaveUser '" + slaveUser.getKey()
                        + "' can still be accessed via DataManager.find",
                savedUser);
    }

    private void doTestMasterUserNotDeletedCheck() throws Exception {
        // check that the MasterUser was not deleted
        PlatformUser orgMasterUser = onBehalfUserReference.getMasterUser();
        assertNotNull(
                "The PlatformUser of masterUser not found for OnBehalfUserReference "
                        + onBehalfUserReference.getKey(), orgMasterUser);
        PlatformUser savedMasterUser = mgr.find(PlatformUser.class,
                Long.valueOf(orgMasterUser.getKey()));
        assertNotNull("Cannot find PlatformUser of masterUser '"
                + orgMasterUser.getKey() + "' in DB", savedMasterUser);

    }

    private PlatformUser loadPlatformUser(boolean mandatory, PlatformUser user) {
        assertNotNull("Cannot find the original PlatformUser", user);

        // load the previously persisted PlatformUser
        PlatformUser savedUser = mgr.find(PlatformUser.class,
                Long.valueOf(user.getKey()));
        if (mandatory) {
            assertNotNull("Cannot find PlatformUser '" + user.getKey()
                    + "' in DB", savedUser);
        }
        return savedUser;
    }

    /**
     * <b>Testcase:</b> Delete an existing master user object <br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The corresponding OnBehalfUserReference objects to master user marked
     * as deleted in the DB</li>
     * <li>The corresponding slave users marked as deleted in the DB</li>
     * <li>History object created for the deleted OnBehalfUserReference</li>
     * </ul>
     * 
     * @throws Throwable
     */
    @Test
    public void testDeleteMasterUserCascade() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOnBehalfUserReference();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                masterUser = mgr.getReference(PlatformUser.class,
                        masterUser.getKey());
                mgr.remove(masterUser);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestDeleteOnBehalfUserReferenceCheck();
                return null;
            }
        });
    }

    /**
     * Test the query to find inactive on behalf off users. In this test case
     * the period until a user is considered inactive is set to 0 ms. Therefore
     * all users are inactive.
     * 
     * @throws Exception
     */
    @Test
    public void testFindInactiveBeforePeriod_allUsersInactive()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOnBehalfUserReference();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long period = 20;
                Long lowerPeriodBound = Long.valueOf(System.currentTimeMillis()
                        + period);
                List<OnBehalfUserReference> inactiveUsers = executeQuery(lowerPeriodBound);
                assertEquals(1, inactiveUsers.size());
                return null;
            }
        });
    }

    /**
     * Test the query to find inactive on behalf off users. In this test case
     * the period until a user is considered inactive is set to 10000 ms.
     * Therefore all users are active.
     * 
     * @throws Exception
     */
    @Test
    public void testFindInactiveBeforePeriod_noUserInactive() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOnBehalfUserReference();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                long period = 10000;
                Long lowerPeriodBound = Long.valueOf(System.currentTimeMillis()
                        - period);
                List<OnBehalfUserReference> inactiveUsers = executeQuery(lowerPeriodBound);
                assertEquals(0, inactiveUsers.size());
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<OnBehalfUserReference> executeQuery(Long lowerPeriodBound) {
        Query query = mgr
                .createNamedQuery("OnBehalfUserReference.findInactiveBeforePeriod");
        query.setParameter("leastPermittedTime", lowerPeriodBound);
        return query.getResultList();
    }

}
