/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.UserRole;
import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.sessionservice.bean.SessionServiceBean;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * Unit tests for {@link SessionDao} using the test EJB container.
 * 
 * @author Mao
 */
public class SessionDaoIT extends EJBTestBase {

    protected static final String GLOBAL_MP_ID = "GLOBAL_MP";
    private SessionService sessionMgmt;
    private DataService ds;
    private SessionDao dao;

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
        ds = new DataServiceBean() {
            @Override
            public PlatformUser getCurrentUser() {
                return givenUserAdmin(1, "userId", org);
            }
        };
        container.enableInterfaceMocking(true);
        container.addBean(ds);
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(new UserGroupAuditLogCollector());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new UserGroupServiceLocalBean());
        container.addBean(Mockito.mock(SubscriptionServiceLocal.class));
        container.addBean(new EventServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new SessionServiceBean());
        container.addBean(Mockito.mock(IdentityService.class));
        sessionMgmt = container.get(SessionService.class);
        dao = new SessionDao(ds);
        container.login("1", ROLE_ORGANIZATION_ADMIN);
    }

    @Test
    public void getActiveSessionsForUser() throws Exception {
        // given
        sessionMgmt.createPlatformSession("sessionId");

        // when
        List<Session> result = runTX(new Callable<List<Session>>() {
            @Override
            public List<Session> call() throws Exception {
                return dao.getActiveSessionsForUser(ds.getCurrentUser());
            }
        });

        // then
        assertEquals(1, result.size());
    }
}
