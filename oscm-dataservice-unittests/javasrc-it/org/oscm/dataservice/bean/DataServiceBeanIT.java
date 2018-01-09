/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 31.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dataservice.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.exceptions.InvalidUserSession;

/**
 * Unit tests for the credential management.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class DataServiceBeanIT extends EJBTestBase {

    private DataService mgr;
    private Organization org;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());

        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                return null;
            }
        });
    }

    @Test
    public void testGetCurrentUserNonExisting() throws Exception {
        container.login("1000");
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mgr.getCurrentUser();
                    return null;
                }
            });
            Assert.fail(
                    "Call must fail, as no dataset is present in the database for this user!");
        } catch (EJBException e) {
            Assert.assertTrue(
                    e.getCausedByException() instanceof InvalidUserSession);
            Assert.assertNotNull(e.getCausedByException().getCause());
        }
    }

    @Test
    public void testGetCurrentUserNonExistingClientCert() throws Exception {
        String dn = "dn=1";
        createOrgForWS(dn, OrganizationRoleType.SUPPLIER);
        container.login(dn);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mgr.getCurrentUser();
                    return null;
                }
            });
            Assert.fail(
                    "Call must fail, as no user is present in the database for this organization!");
        } catch (EJBException e) {
            Assert.assertTrue(
                    e.getCausedByException() instanceof InvalidUserSession);
        }
    }

    @Test
    public void testGetCurrentUserNonExistingOrganizationClientCert()
            throws Exception {
        container.login("dn=1");
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertNull("No valid user object expected", user);

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mgr.getCurrentUser();
                    return null;
                }
            });
            Assert.fail(
                    "Call must fail, as no dataset is present in the database for this user!");
        } catch (EJBException e) {
            Assert.assertTrue(
                    e.getCausedByException() instanceof InvalidUserSession);
        }
    }

    @Test
    public void testGetCurrentUserExisting() throws Exception {
        PlatformUser user = createOrgAndUser();
        PlatformUser currentUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUser();
            }
        });
        Assert.assertEquals(user.getOrganization().getOrganizationId(),
                currentUser.getOrganization().getOrganizationId());
        Assert.assertEquals(user.getUserId(), currentUser.getUserId());
    }

    @Test
    public void testGetCurrentAsyncUserExisting() throws Exception {
        final PlatformUser user = createOrgAndUser();
        createOrgAndUser();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.setCurrentUserKey(Long.valueOf(user.getKey()));
                return null;
            }
        });
        PlatformUser currentUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUser();
            }
        });
        Assert.assertEquals(user.getOrganization().getOrganizationId(),
                currentUser.getOrganization().getOrganizationId());
        Assert.assertEquals(user.getUserId(), currentUser.getUserId());
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.setCurrentUserKey(null);
                return null;
            }
        });
    }

    @Test
    public void testGetCurrentAsyncUserNonExisting() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mgr.setCurrentUserKey(Long.valueOf(666));
                return null;
            }
        });
        try {
            runTX(new Callable<PlatformUser>() {
                @Override
                public PlatformUser call() throws Exception {
                    return mgr.getCurrentUser();
                }
            });
            Assert.fail(
                    "Call must fail, as no dataset is present in the database for this user!");
        } catch (EJBException e) {
            Assert.assertTrue(
                    e.getCausedByException() instanceof InvalidUserSession);
            Assert.assertNotNull(e.getCausedByException().getCause());
        } finally {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mgr.setCurrentUserKey(null);
                    return null;
                }
            });
        }
    }

    @Test
    public void testGetCurrentUserExistingButLoggedOut() throws Exception {
        PlatformUser user = createOrgAndUser();
        PlatformUser currentUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertEquals(user.getOrganization().getOrganizationId(),
                currentUser.getOrganization().getOrganizationId());
        Assert.assertEquals(user.getUserId(), currentUser.getUserId());
        container.logout();
        currentUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertNull(currentUser);
    }

    @Test
    public void testGetCurrentUserExistingClientCert() throws Exception {
        String dn = "dn=1";
        PlatformUser user = createOrgAndUserForWS(dn, true,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(dn);
        PlatformUser currentUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUser();
            }
        });
        Assert.assertEquals(user.getOrganization().getOrganizationId(),
                currentUser.getOrganization().getOrganizationId());
        Assert.assertEquals(user.getUserId(), currentUser.getUserId());
        currentUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertEquals(user.getOrganization().getOrganizationId(),
                currentUser.getOrganization().getOrganizationId());
        Assert.assertEquals(user.getUserId(), currentUser.getUserId());
    }

    @Test(expected = InvalidUserSession.class)
    public void testGetCurrentUserExistingButNoAdminClientCert()
            throws Exception {
        String dn = "dn=1";
        createOrgAndUserForWS(dn, false,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(dn);
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertNull("No valid user object expected", user);
        try {
            runTX(new Callable<PlatformUser>() {
                @Override
                public PlatformUser call() throws Exception {
                    return mgr.getCurrentUser();
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = InvalidUserSession.class)
    public void testGetCurrentUserExpired() throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "admin");
                user.getOrganization().setDeregistrationDate(
                        Long.valueOf(System.currentTimeMillis()));
                return user;
            }
        });
        container.login(String.valueOf(user.getKey()));
        user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertNull("No valid user object expected", user);
        try {
            runTX(new Callable<PlatformUser>() {
                @Override
                public PlatformUser call() throws Exception {
                    return mgr.getCurrentUser();
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = InvalidUserSession.class)
    public void testGetCurrentUserExpiredClientCert() throws Exception {
        final String dn = "dn=1";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.createOrganization(mgr);
                org.setDistinguishedName(dn);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "admin");
                user.getOrganization().setDeregistrationDate(
                        Long.valueOf(System.currentTimeMillis()));
                return null;
            }
        });
        container.login(dn);
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getCurrentUserIfPresent();
            }
        });
        Assert.assertNull("No valid user object expected", user);
        try {
            runTX(new Callable<PlatformUser>() {
                @Override
                public PlatformUser call() throws Exception {
                    return mgr.getCurrentUser();
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void testRemovePersistentEntity() throws Exception {

        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Event obj = new Event();
                obj.setEventIdentifier("id");
                obj.setEventType(EventType.PLATFORM_EVENT);
                mgr.persist(obj);
                assertTrue(mgr.contains(obj));
                mgr.remove(obj);
                assertFalse(mgr.contains(obj));
                return null;
            }
        });
    }

    @Test
    public void testRemoveNonPersistentEntity() throws Exception {

        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Event obj = new Event();
                obj.setEventIdentifier("id");
                obj.setEventType(EventType.PLATFORM_EVENT);
                assertFalse(mgr.contains(obj));
                mgr.remove(obj);
                assertFalse(mgr.contains(obj));
                return null;
            }
        });
    }

    private PlatformUser createOrgAndUserForWS(final String dn,
            final boolean admin, final OrganizationRoleType... roleType)
            throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                org = Organizations.createOrganization(mgr, roleType);
                org.setDistinguishedName(dn);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        admin, "admin");
                return user;
            }
        });
        return user;
    }

    private PlatformUser createOrgAndUser(
            final OrganizationRoleType... roleType) throws Exception {
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                org = Organizations.createOrganization(mgr, roleType);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "admin");
                return user;
            }
        });
        container.login(String.valueOf(user.getKey()));
        return user;
    }

    private void createOrgForWS(final String dn,
            final OrganizationRoleType... roleType) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                org = Organizations.createOrganization(mgr, roleType);
                org.setDistinguishedName(dn);
                return null;
            }
        });
    }
}
