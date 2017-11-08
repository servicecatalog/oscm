/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-9-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * @author qiu
 */
public class UserLicenseDaoIT extends EJBTestBase {
    private DataService ds;
    private UserLicenseDao dao;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new UserLicenseDao());
        ds = container.get(DataService.class);
        dao = container.get(UserLicenseDao.class);
        prepareUsers();
    }

    @Test
    public void countRegisteredUsers() throws Exception {

        // when
        Long result = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return Long.valueOf(dao.countRegisteredUsers());
            }
        });

        // then
        assertEquals(2, result.longValue());
    }

    @Test
    public void getPlatformOperators() throws Exception {

        // when
        List<PlatformUser> result = runTX(new Callable<List<PlatformUser>>() {
            @Override
            public List<PlatformUser> call() throws Exception {
                return dao.getPlatformOperators();
            }
        });

        // then
        assertEquals(1, result.size());
    }

    private void prepareUsers() throws Exception {
        Organization admin = createOrg("admin",
                OrganizationRoleType.PLATFORM_OPERATOR);
        PlatformUsers.grantRoles(ds, createUser(admin, true, "user"),
                UserRoleType.PLATFORM_OPERATOR);
        createUser(admin, false, "user2");
    }

    private Organization createOrg(final String organizationId,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, organizationId,
                        roles);
            }
        });
    }

    private PlatformUser createUser(final Organization org,
            final boolean isAdmin, final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, org, isAdmin, userId);
            }
        });
    }
}
