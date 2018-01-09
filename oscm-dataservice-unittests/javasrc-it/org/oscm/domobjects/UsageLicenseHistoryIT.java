/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: barzu                                            
 *                                                                              
 *  Creation Date: Oct 14, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 14, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.operationslog.SubscriptionUserQuery;
import org.oscm.operationslog.UserOperationLogQuery;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author barzu
 */
public class UsageLicenseHistoryIT extends UserOperationLogQueryTestBase {

    @Override
    protected UserOperationLogQuery getQuery() {
        return new SubscriptionUserQuery();
    }

    @Test
    public void testLog() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicenseWithRoleDefinition();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                assertTrue(result.get(0) instanceof Object[]);
                Object[] row0 = (Object[]) result.get(0);
                assertEquals("Wrong number of fields in the query result",
                        new SubscriptionUserQuery().getFieldNames().length,
                        row0.length);

                assertTrue(row0[0] instanceof Date);
                assertEquals(ModificationType.MODIFY.name(), row0[1]);
                assertEquals(user.getUserId(), row0[2]);
                assertEquals(BigInteger.valueOf(usageLicense.getVersion()),
                        row0[3]);
                assertEquals(subscription.getSubscriptionId(), row0[4]);
                assertEquals(supplier.getName(), row0[5]);
                assertEquals(supplier.getOrganizationId(), row0[6]);
                assertEquals(user.getUserId(), row0[7]);
                assertEquals(user.getFirstName(), row0[8]);
                assertEquals(user.getLastName(), row0[9]);
                assertEquals(user.getEmail(), row0[10]);
                assertEquals(usageLicense.getApplicationUserId(), row0[11]);
                assertEquals(usageLicense.getRoleDefinition().getRoleId(),
                        row0[12]);

                Object[] row1 = (Object[]) result.get(1);
                assertEquals(ModificationType.ADD.name(), row1[1]);
            }
        }.run();
    }

    @Test
    public void testLog_MissingNullableFields() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicense();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                // RoleDefinition
                assertEquals(null, ((Object[]) result.get(0))[12]);
                assertEquals(null, ((Object[]) result.get(1))[12]);
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionOrganization() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                supplier.setName("OrgWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicense();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong Organization (with greater objVersion) joined.",
                        2, result.size());
                assertEquals(ORG_NAME, ((Object[]) result.get(0))[5]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateSubscription() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicense();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                subscription = (Subscription) mgr.find(subscription);
                subscription.setSubscriptionId("TheModifiedSubscriptionId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                for (Object object : result) {
                    Object[] usageLicense = (Object[]) object;
                    assertEquals("Wrong SubscriptionHistory entry",
                            SUBSCRIPTION_ID, usageLicense[4]);
                }
                subscription = (Subscription) mgr.find(subscription);
                assertEquals("TheModifiedSubscriptionId",
                        subscription.getSubscriptionId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionSubscription() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                // increase version of Subscription
                subscription.setSubscriptionId("SubscrWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicense();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong Subscription (with greater objVersion) joined.",
                        2, result.size());
                assertEquals(SUBSCRIPTION_ID, ((Object[]) result.get(0))[4]);
                assertEquals(SUBSCRIPTION_ID, ((Object[]) result.get(1))[4]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdatePlatformUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicense();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                user = (PlatformUser) mgr.find(user);
                user.setFirstName("Lady");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                for (Object object : result) {
                    Object[] usageLicense = (Object[]) object;
                    assertEquals("Wrong PlatformUserHistory entry",
                            USER_FIRST_NAME, usageLicense[8]);
                }
                user = (PlatformUser) mgr.find(user);
                assertEquals("Lady", user.getFirstName());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionPlatformUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser("user" + Math.random());
                // increase the version of the PlatformUser
                user.setFirstName("PlatformUserWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addAndLoginAsUser();
                addProduct();
                addSubscription();
                addUsageLicense();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(
                        "Wrong PlatformUser (with greater objVersion) joined.",
                        2, result.size());
                assertEquals(USER_FIRST_NAME, ((Object[]) result.get(0))[8]);
                assertEquals(USER_FIRST_NAME, ((Object[]) result.get(1))[8]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateRoleDefinition() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicenseWithRoleDefinition();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                roleDefinition = mgr.getReference(RoleDefinition.class,
                        roleDefinition.getKey());
                roleDefinition.setRoleId("PETTY_CHIEF");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result)
                    throws ObjectNotFoundException {
                assertEquals(2, result.size());
                for (Object object : result) {
                    Object[] usageLicense = (Object[]) object;
                    assertEquals("Wrong RoleDefninitionHistory entry",
                            ROLE_DEFINITION_ID, usageLicense[12]);
                }
                roleDefinition = mgr.getReference(RoleDefinition.class,
                        roleDefinition.getKey());
                assertEquals("PETTY_CHIEF", roleDefinition.getRoleId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionRoleDefinition() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicenseWithRoleDefinition();
                // increase the version of the RoleDefinition
                roleDefinition.setRoleId("PETTY_CHIEF");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser("user" + Math.random());
                addProduct();
                addSubscription();
                addUsageLicenseWithRoleDefinition();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "Wrong RoleDefinition (with greater objVersion) joined.";
                assertEquals(msg, 4, result.size());
                assertEquals(msg, ROLE_DEFINITION_ID,
                        ((Object[]) result.get(0))[12]);
                assertEquals(msg, ROLE_DEFINITION_ID,
                        ((Object[]) result.get(1))[12]);
                assertEquals(msg, "PETTY_CHIEF", ((Object[]) result.get(2))[12]);
                assertEquals(msg, "PETTY_CHIEF", ((Object[]) result.get(3))[12]);
            }
        }.run();
    }

    @Test
    public void testLog_UserIdAsModUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addUser();
                // login with the userId instead of userKey,
                // so the userKey is written as history modDate
                container.login(user.getUserId());
                addProduct();
                addSubscription();
                addUsageLicense();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "The userId is expected when written into history moduser instead of the user key.";
                assertEquals(msg, 2, result.size());
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(0))[2]);
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(1))[2]);
            }
        }.run();
    }

    @Test
    public void testLog_ModdateModUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addSubscription(false, false);
                addUsageLicense();
                return null;
            }
        });
        final String initialUserId = user.getUserId();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                user = (PlatformUser) mgr.find(user);
                user.setUserId("NewUserId");
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                assertEquals(2, result.size());
                for (Object object : result) {
                    Object[] usageLicense = (Object[]) object;
                    assertEquals(
                            "Wrong PlatformUserHistory entry (for moduser)",
                            initialUserId, usageLicense[2]);
                }
                user = (PlatformUser) mgr.find(user);
                assertEquals("NewUserId", user.getUserId());
            }
        }.run();
    }

    @Test
    public void testLog_ObjversionModUser() throws Throwable {
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addOrganization();
                addAndLoginAsUser("user" + Math.random());
                // increase the version of the PlatformUser for moduser
                user.setUserId("PlatformUserWithGreaterObjversion");
                return null;
            }
        });
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                addAndLoginAsUser();
                addProduct();
                addSubscription();
                addUsageLicense();
                return null;
            }
        });
        new LogQueryRunner() {

            @Override
            protected void assertResult(List<?> result) {
                String msg = "Wrong PlatformUser (with greater objVersion) joined for moduser.";
                assertEquals(msg, 2, result.size());
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(0))[2]);
                assertEquals(msg, user.getUserId(),
                        ((Object[]) result.get(1))[2]);
            }
        }.run();
    }

}
