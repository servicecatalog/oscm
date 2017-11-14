/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 13.10.16 11:25
 *
 ******************************************************************************/
package org.oscm.webservices.handler;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Callable;

import javax.persistence.Query;
import javax.sql.DataSource;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;

/**
 * Authored by dawidch
 */
public class KeyQueryIT extends EJBTestBase {
    
    private DataService ds;
    
    private AbstractKeyQuery kq;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(new DataServiceBean());
        ds = container.get(DataService.class);

        Organization supplier = createOrg("administrator", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        createOrgUser("administrator", supplier, "en", "tenantID");
        Organization orgWithTenant = createOrgWithTenant("administrator2", "tenantID", OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        createOrgUser("administrator", orgWithTenant, "en", "tenantID");
    }

    @Test
    public void testQuery() throws Exception {
        
        kq = new UserKeyQuery(mock(DataSource.class), "");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query nativeQuery = ds.createNativeQuery(kq.getStatement().substring(0, kq.getStatement().length() - 1) + "'administrator'");
                Object singleResult = nativeQuery.getSingleResult();
                assertNotNull(singleResult);
                return null;
            }
        });
    }

    private Organization createOrgWithTenant(final String organizationId,
                                             final String tenantId, final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganizationWithTenant(ds, organizationId, tenantId,
                        roles);
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
                                       final Organization organization, final String locale, final String tenantID) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return Organizations.createNormalUserForOrg(ds, organization, true, userId, locale, tenantID);
            }
        });
    }
}
