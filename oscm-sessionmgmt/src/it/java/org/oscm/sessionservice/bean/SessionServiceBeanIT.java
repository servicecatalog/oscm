/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.sessionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UserRole;
import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.types.constants.Configuration;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SessionType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * @author Mike J&auml;ger
 * 
 */
public class SessionServiceBeanIT extends EJBTestBase {

    private SessionService sessionMgmt;
    private SessionServiceLocal sessionMgmtLocal;
    private ServiceAccessType serviceAccessType = ServiceAccessType.DIRECT;
    private GatheredEvent lastEvent;
    private Parameter param;
    private Long paramValue;
    private String nodeName = "nodeName";
    protected DataService mgr;

    private PlatformUser givenUserAdmin(long key, String id, Organization org) {
        return givenUser(key, id, org, UserRoleType.ORGANIZATION_ADMIN);
    }

    private PlatformUser givenUser(long key, String id, Organization org,
            UserRoleType roleType) {
        PlatformUser user = new PlatformUser();
        user.setKey(key);
        user.setUserId(id);
        user.setOrganization(org);
        RoleAssignment roleAssign = new RoleAssignment();
        roleAssign.setUser(user);
        roleAssign.setRole(new UserRole(roleType));
        user.getAssignedRoles().add(roleAssign);
        return user;
    }

    @Override
    public void setup(final TestContainer container) throws Exception {
        final Organization org = new Organization();
        org.setKey(0);

        container.login("1", ROLE_ORGANIZATION_ADMIN);
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", org);
            }
        });

        container.addBean(new CommunicationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(Mockito.mock(IdentityService.class));
        container.addBean(new UserGroupAuditLogCollector());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new UserGroupServiceLocalBean());

        SubscriptionServiceLocal subMock = Mockito
                .mock(SubscriptionServiceLocal.class);

        Mockito.doAnswer(new Answer<Subscription>() {
            @Override
            public Subscription answer(InvocationOnMock invocation) {
                long subscriptionKey = ((Long) invocation.getArguments()[0])
                        .longValue();
                Subscription sub = new Subscription();
                sub.setSubscriptionId("s1");
                sub.setKey(subscriptionKey);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setOrganization(org);
                sub.setOwner(givenUserAdmin(1, "userId", org));

                Product sampleProd = new Product();
                sampleProd.setProductId("p1");
                sampleProd.setParameterSet(new ParameterSet());
                param = new Parameter();
                ParameterDefinition paramDef = new ParameterDefinition();
                paramDef.setParameterType(ParameterType.PLATFORM_PARAMETER);
                paramDef.setParameterId(PlatformParameterIdentifiers.CONCURRENT_USER);
                param.setParameterDefinition(paramDef);
                if (paramValue != null) {
                    param.setValue(String.valueOf(paramValue));
                }
                sampleProd.getParameterSet().getParameters().add(param);
                sampleProd.setTechnicalProduct(new TechnicalProduct());
                sampleProd.getTechnicalProduct().setAccessType(
                        serviceAccessType);
                sub.bindToProduct(sampleProd);

                if (subscriptionKey == 10) {
                    Organization organization = new Organization();
                    organization.setKey(1l);
                    sub.setOrganization(organization);
                    sub.setOwner(givenUserAdmin(1, "userId", org));
                } else if (subscriptionKey == 20) {
                    sub.setStatus(SubscriptionStatus.EXPIRED);
                    sub.setOwner(givenUserAdmin(1, "userId", org));
                } else if (subscriptionKey == 30) {
                    sub.setStatus(SubscriptionStatus.DEACTIVATED);
                    sub.setOwner(givenUserAdmin(1, "userId", org));
                } else if (subscriptionKey == 40) {
                    sub.setStatus(SubscriptionStatus.PENDING_UPD);
                    sub.setOwner(givenUserAdmin(1, "userId", org));
                }
                return sub;
            }
        }).when(subMock).loadSubscription(Mockito.anyLong());

        container.addBean(subMock);

        container.addBean(new MarketplaceServiceStub() {
            @Override
            public VOMarketplace getMarketplaceForSubscription(
                    long subscriptionKey, String locale)
                    throws ObjectNotFoundException {
                VOMarketplace mpl = new VOMarketplace();

                switch ((int) subscriptionKey) {
                case 1:
                    mpl.setMarketplaceId("GLOBAL");
                    break;
                default:
                    mpl.setMarketplaceId("LOCAL");
                    break;
                }
                return mpl;
            }

        });

        container.addBean(new EventServiceBean() {
            @Override
            public void recordEvent(GatheredEvent event) {
                lastEvent = event;
            }
        });

        container.addBean(new ConfigurationServiceStub() {
            @Override
            public String getNodeName() {
                return nodeName;
            }
        });
        container.addBean(new SessionServiceBean());

        mgr = container.get(DataService.class);
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                "http://localhost"));

        // lookup bean references
        sessionMgmt = container.get(SessionService.class);
        sessionMgmtLocal = container.get(SessionServiceLocal.class);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                SupportedCountries.createOneSupportedCountry(mgr);
                return null;
            }
        });
    }

    @Test
    public void testProductSessionCreation() throws Exception {
        createProductSessionAndResolve(1L, "session1");

        // now check if an according even has been created
        assertNotNull("Event for session entry was not created", lastEvent);
        assertEquals("Wrong user for event", "userId", lastEvent.getActor());
        assertEquals("Wrong event type", EventType.PLATFORM_EVENT,
                lastEvent.getType());
        assertEquals("Stored information is wrong!",
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                lastEvent.getEventId());
    }

    @Test
    public void testGetProductSessionsForSessionId() throws Exception {
        createProductSessionAndResolve(1L, "session1");

        List<Long> result = sessionMgmt
                .getSubscriptionKeysForSessionId("session1");
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get(0));
    }

    @Test
    public void testGetProductSessionsForSubscriptionTKey() throws Exception {
        createProductSessionAndResolve(1L, "session1");

        List<Session> result = runTX(new Callable<List<Session>>() {
            @Override
            public List<Session> call() throws Exception {
                return sessionMgmtLocal
                        .getProductSessionsForSubscriptionTKey(1L);
            }
        });
        assertEquals(1, result.size());
        assertEquals("userId", result.get(0).getPlatformUserId());
    }

    @Test
    public void testGetNumberOfServiceSessions() throws Exception {
        createProductSessionAndResolve(1L, "session1");
        int num = sessionMgmt.getNumberOfServiceSessions(1L);
        assertEquals(1, num);
    }

    @Test
    public void testGetNumberOfServiceSessions_SubMgr() throws Exception {
        createProductSessionAndResolve(1L, "session1");
        container.login("1", ROLE_SUBSCRIPTION_MANAGER);
        int num = sessionMgmt.getNumberOfServiceSessions(1L);
        assertEquals(1, num);
    }

    @Test
    public void testGetNumberOfServiceSessions_none() throws Exception {
        int num = sessionMgmt.getNumberOfServiceSessions(1L);
        assertEquals(0, num);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetNumberOfServiceSessions_subOwnedByOtherOrg()
            throws Exception {
        sessionMgmt.getNumberOfServiceSessions(10L);
    }

    @Test
    public void testDeleteServiceSessionsForSubscription() throws Exception {
        createProductSessionAndResolve(1L, "session1");
        sessionMgmt.deleteServiceSessionsForSubscription(1L);
        int num = sessionMgmt.getNumberOfServiceSessions(1L);
        assertEquals(0, num);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDeleteServiceSessionsForSubscription_subOwnedByOtherOrg()
            throws Exception {
        sessionMgmt.deleteServiceSessionsForSubscription(10L);
    }

    @Test
    public void testDeleteServiceSessionsForSubscription_none()
            throws Exception {
        sessionMgmt.deleteServiceSessionsForSubscription(1L);
        int num = sessionMgmt.getNumberOfServiceSessions(1L);
        assertEquals(0, num);
    }

    @Test
    public void testGetSessionsForUserKey() throws Exception {
        createProductSessionAndResolve(1L, "session1");

        List<Session> result = runTX(new Callable<List<Session>>() {
            @Override
            public List<Session> call() throws Exception {
                return sessionMgmtLocal.getSessionsForUserKey(1L);
            }
        });
        assertEquals(1, result.size());
        assertEquals("userId", result.get(0).getPlatformUserId());
        assertEquals("session1", result.get(0).getSessionId());
    }

    @Test
    public void testHasTechnicalProductActiveSessions() throws Exception {
        final Subscription sub = createSubscription();

        createProductSessionAndResolve(sub.getKey(), "session1");

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(sessionMgmtLocal
                        .hasTechnicalProductActiveSessions(sub.getProduct()
                                .getTechnicalProduct().getKey()));
            }
        });
        assertTrue(result.booleanValue());
        result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(sessionMgmtLocal
                        .hasTechnicalProductActiveSessions(1000L));
            }
        });
        assertFalse(result.booleanValue());
    }

    @Test
    public void testProductSessionRemoval() throws Exception {
        createProductSessionAndResolve(0L, "session1");

        String forward = sessionMgmt.deleteServiceSession(0, "session1");

        // now check if an according even has been created
        assertNotNull("Event for session entry was not created", lastEvent);
        assertEquals("Wrong user for event", "userId", lastEvent.getActor());
        assertEquals("Wrong event type", EventType.PLATFORM_EVENT,
                lastEvent.getType());
        assertEquals("Stored information is wrong!",
                PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                lastEvent.getEventId());

        final String expected_BSS_Logout = "/logoutPage.jsf?subscriptionKey=";
        assertTrue("Wrong forwarding! BSS logout page was expected.",
                forward.indexOf(expected_BSS_Logout) > 0);
    }

    @Test
    public void testMPLProductSessionRemoval() throws Exception {
        createProductSessionAndResolve(1L, "session1");

        String forward = sessionMgmt.deleteServiceSession(1L, "session1");

        // now check if an according even has been created
        assertNotNull("Event for session entry was not created", lastEvent);
        assertEquals("Wrong user for event", "userId", lastEvent.getActor());
        assertEquals("Wrong event type", EventType.PLATFORM_EVENT,
                lastEvent.getType());
        assertEquals("Stored information is wrong!",
                PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                lastEvent.getEventId());
        final String expected_MPL_Logout = Marketplace.MARKETPLACE_ROOT
                + "/logoutPage.jsf?subscriptionKey=";
        assertTrue("Wrong forwarding! MPL logout page was expected.",
                forward.contains(expected_MPL_Logout));
    }

    @Test
    public void testProductSessionRemovalForSessionId() throws Exception {

        createProductSessionAndResolve(100L, "newSession");
        sessionMgmt.deleteSessionsForSessionId("newSession");

        // now check if an according even has been created
        assertNotNull("Event for session entry was not created", lastEvent);
        assertEquals("Wrong user for event", "userId", lastEvent.getActor());
        assertEquals("Wrong event type", EventType.PLATFORM_EVENT,
                lastEvent.getType());
        assertEquals("Stored information is wrong!",
                PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                lastEvent.getEventId());
    }

    @Test
    public void testProductSessionRemovalAllSessions() throws Exception {
        createProductSessionAndResolve(100L, "deleteAllSessions");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                sessionMgmtLocal.deleteAllSessions();
                return null;
            }
        });
        // now check if an according even has been created
        assertNotNull("Event for session entry was not created", lastEvent);
        assertEquals("Wrong user for event", "userId", lastEvent.getActor());
        assertEquals("Wrong event type", EventType.PLATFORM_EVENT,
                lastEvent.getType());
        assertEquals("Stored information is wrong!",
                PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                lastEvent.getEventId());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testProductSessionCreationWrongOrganization() throws Exception {
        createProductSessionAndResolve(10L, "session10");
    }

    @Test(expected = ServiceParameterException.class)
    public void testProductSessionCreationExpired() throws Exception {
        createProductSessionAndResolve(20L, "session20");
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testProductSessionCreationDeactivated() throws Exception {
        createProductSessionAndResolve(30L, "session30");
    }

    @Test
    public void createServiceSession_PENDING_UPD() throws Exception {
        createProductSessionAndResolve(40L, "session40");
    }

    @Test
    public void testPlatformSession() throws Exception {

        sessionMgmt.createPlatformSession("sessionId");
        Session session = runTX(new Callable<Session>() {
            @Override
            public Session call() throws Exception {
                return sessionMgmtLocal
                        .getPlatformSessionForSessionId("sessionId");
            }
        });
        assertEquals("userId", session.getPlatformUserId());
        assertEquals(1, session.getPlatformUserKey());
        assertEquals(SessionType.PLATFORM_SESSION, session.getSessionType());
    }

    @Test(expected = EJBException.class)
    public void testDeletePlatformSession() throws Exception {
        String sessionid = "sessionId";
        testPlatformSession();
        sessionMgmt.deletePlatformSession(sessionid);

        sessionMgmtLocal.getPlatformSessionForSessionId(sessionid);
    }

    @Test
    public void testVerifyParameterConcurrentUser() throws Exception {
        paramValue = Long.valueOf(1L);
        final Subscription sub = createSubscription();

        createProductSessionAndResolve(sub.getKey(), "session1");
        try {
            createProductSessionAndResolve(sub.getKey(), "session2");
            fail("Only one session allowed");
        } catch (ServiceParameterException e) {
            // expected
        }
        List<Session> result = runTX(new Callable<List<Session>>() {
            @Override
            public List<Session> call() throws Exception {
                return sessionMgmtLocal
                        .getProductSessionsForSubscriptionTKey(sub.getKey());
            }
        });
        assertEquals(1, result.size());
    }

    @Test
    public void deleteAllSessions_ForDifferentNodes() throws Exception {
        nodeName = "node1";
        sessionMgmt.createPlatformSession("session1");
        createProductSessionAndResolve(100L, "session1");
        nodeName = "node2";
        sessionMgmt.createPlatformSession("session2");
        createProductSessionAndResolve(100L, "session2");
        // now remove the sessions on cluster node 2, the sessions for session 1
        // must remain
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                sessionMgmtLocal.deleteAllSessions();
                Session session = sessionMgmtLocal
                        .getPlatformSessionForSessionId("session1");
                assertNotNull(session);
                List<Session> sessions = sessionMgmtLocal
                        .getProductSessionsForSubscriptionTKey(100L);
                assertNotNull(sessions);
                assertEquals(1, sessions.size());
                return null;
            }
        });
    }

    /**
     * Creates a product session with the given platformUserId, resolves it as
     * well to create an event entry implicitly.
     * 
     * @throws ServiceParameterException
     * @throws OperationNotPermittedException
     */
    private void createProductSessionAndResolve(long subscriptionTKey,
            String sessionId) throws Exception {
        sessionMgmt.createServiceSession(subscriptionTKey, sessionId,
                "someToken");

        sessionMgmt.resolveUserToken(subscriptionTKey, sessionId, "someToken");
    }

    private Subscription createSubscription() throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Organization supplierAndProvider = Organizations
                        .createOrganization(mgr, OrganizationRoleType.SUPPLIER,
                                OrganizationRoleType.TECHNOLOGY_PROVIDER);
                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, supplierAndProvider, "id",
                                false, ServiceAccessType.LOGIN);
                Product prod = Products.createProduct(supplierAndProvider,
                        tProd, false, "p1", null, mgr);
                Organization customer = Organizations.createCustomer(mgr,
                        supplierAndProvider);
                return Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), prod.getProductId(),
                        "s1", supplierAndProvider);
            }
        });
    }

}
