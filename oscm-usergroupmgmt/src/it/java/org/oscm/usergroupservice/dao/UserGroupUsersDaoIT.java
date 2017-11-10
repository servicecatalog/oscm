/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2015.10.22                                                   
 *                                                                              
 *******************************************************************************/
package org.oscm.usergroupservice.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.paginator.Filter;
import org.oscm.paginator.PaginationUsersInUnit;
import org.oscm.paginator.TableColumns;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.UserGroups;
import org.oscm.test.ejb.TestContainer;

public class UserGroupUsersDaoIT extends EJBTestBase {

    private DataService ds;
    private UserGroupUsersDao dao;
    private UserGroup userGroup;
    private Organization supplierOrg;
    private PlatformUser supplier;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);
        dao = new UserGroupUsersDao();
        dao.dm = ds;

        supplierOrg = createOrg("supplier", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        supplier = createOrgUser("supplier", supplierOrg, "en");

        userGroup = createUserGroup();
        ds.setCurrentUserKey(supplier.getKey());

        for (int i = 0; i < 12; i++) {
            createOrgUser("user" + i, supplierOrg, "en");
        }
    }

    private UserGroup createUserGroup() throws Exception {
        return runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                return UserGroups.createUserGroup(ds, "userGroupTest",
                        supplierOrg, false, "Description", "referenceID",
                        supplier);
            }
        });
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

    private PlatformUser createOrgUser(final String userId,
            final Organization organization, final String locale)
            throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createUserForOrg(ds, organization, true,
                        userId, locale);
            }
        });
    }

    @Test
    public void executeQueryGroupUsers() throws Exception {
        final PaginationUsersInUnit pagination = new PaginationUsersInUnit();
        pagination.setOffset(0);
        pagination.setLimit(10);

        // when
        List<PlatformUser> result = runTX(new Callable<List<PlatformUser>>() {
            @Override
            public List<PlatformUser> call() throws Exception {
                return dao.executeQueryGroupUsers(pagination,
                        String.valueOf(userGroup.getKey()));
            }
        });

        // then
        assertEquals(pagination.getLimit(), result.size());
    }

    @Test
    public void executeQueryGroupUsers_greaterOffset() throws Exception {
        final PaginationUsersInUnit pagination = new PaginationUsersInUnit();
        pagination.setOffset(5);
        pagination.setLimit(10);

        // when
        List<PlatformUser> result = runTX(new Callable<List<PlatformUser>>() {
            @Override
            public List<PlatformUser> call() throws Exception {
                return dao.executeQueryGroupUsers(pagination,
                        String.valueOf(userGroup.getKey()));
            }
        });

        // then
        assertEquals(7, result.size());
    }

    @Test
    public void executeQueryGroupUsers_filter() throws Exception {
        final PaginationUsersInUnit pagination = new PaginationUsersInUnit();
        pagination.setOffset(0);
        pagination.setLimit(10);
        Set<Filter> filterSet = createFilterSet("user1", "Mi", "Kn", "U");
        pagination.setFilterSet(filterSet);

        // when
        List<PlatformUser> result = runTX(new Callable<List<PlatformUser>>() {
            @Override
            public List<PlatformUser> call() throws Exception {
                return dao.executeQueryGroupUsers(pagination,
                        String.valueOf(userGroup.getKey()));
            }
        });

        // then
        assertEquals(3, result.size());
    }

    private Set<Filter> createFilterSet(String id, String firstName,
            String lastName, String roleInUnit) {
        final Filter userId = new Filter(TableColumns.USER_ID, id);
        final Filter userFirstName = new Filter(TableColumns.FIRST_NAME,
                firstName);
        final Filter userLastName = new Filter(TableColumns.LAST_NAME, lastName);
        Set<Filter> filterSet = new HashSet<Filter>();
        Collections.addAll(filterSet, userId, userFirstName, userLastName);
        return filterSet;
    }

}