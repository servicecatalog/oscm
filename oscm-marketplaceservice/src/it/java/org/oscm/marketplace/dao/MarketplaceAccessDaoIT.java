/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.marketplace.dao;

import static junit.framework.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;

/**
 * Created by PLGrubskiM on 2016-12-07.
 */
public class MarketplaceAccessDaoIT extends EJBTestBase {

    protected MarketplaceAccessDao dao;
    protected DataService ds;

    Marketplace marketplaceDefaultTenant;
    Marketplace marketplaceTenant1;

    Organization orgDefaultTenant1_1;
    Organization orgDefaultTenant1_2;

    Organization orgWithTenant1_1;
    Organization orgWithTenant1_2;
    Organization orgWithTenant1_3;
    Organization orgWithTenant1_4;
    Organization orgWithTenant1_5;
    Organization orgWithTenant1_6;

    Organization orgWithTenant2_1;

    private static final String MARKETPLACE_ID_NO_TENANT = "marketplaceNoTenant";
    private static final String MARKETPLACE_ID_TENANT1 = "marketplaceTenant1";

    private static final String TENANT_1_ID = "tenant1";
    private static final String TENANT_2_ID = "tenant2";

    @Override
    protected void setup(TestContainer container) throws Exception {

        ds = new DataServiceBean();
        container.addBean(ds);
        container.addBean(new MarketplaceAccessDao());
        dao = container.get(MarketplaceAccessDao.class);
        container.login("setup", ROLE_PLATFORM_OPERATOR);

        final Tenant tenant1 = createTenant(TENANT_1_ID);
        final Tenant tenant2 = createTenant(TENANT_2_ID);

        orgDefaultTenant1_1 = createOrganization(createOrganizationObject("orgDefaultTenant1_1"));
        orgDefaultTenant1_2 = createOrganization(createOrganizationObject("orgDefaultTenant1_2"));

        orgWithTenant1_1 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant1_1", tenant1),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        orgWithTenant1_2 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant1_2", tenant1),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        orgWithTenant1_3 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant1_3", tenant1),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        orgWithTenant1_4 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant1_4", tenant1),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        orgWithTenant1_5 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant1_5", tenant1),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        orgWithTenant1_6 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant1_6", tenant1),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        orgWithTenant2_1 = createOrganization(
                createOrganizationObjectWithTenant("orgTenant2_1", tenant2),
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        marketplaceDefaultTenant = createMarketplace(orgDefaultTenant1_1,
                MARKETPLACE_ID_NO_TENANT);
        marketplaceTenant1 = createMarketplace(orgWithTenant1_1,
                MARKETPLACE_ID_TENANT1);
    }

    @Test
    public void getOrganizationsWithMplAndSubscriptions_DefaultTenant()
            throws Exception {

        // US#8470
        //
        // Test if the results returned by the query are limited to the organizations with
        // the same tenant as selected marketplace
        //
        // In this case we created 6 organizations with no tenant

        final long marketplaceKey = marketplaceDefaultTenant.getKey();
        final long tenantKey = 0L;

        final List<Object[]> objects = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getOrganizationsWithMplAndSubscriptions(
                        marketplaceKey, tenantKey);
            }
        });
        assertTrue(objects.size() == 2);
    }

    @Test
    public void getOrganizationsWithMplAndSubscriptions_Tenant1()
            throws Exception {

        // US#8470
        //
        // Test if the results returned by the query are limited to the organizations with
        // the same tenant as selected marketplace
        //
        // In this case we created 6 organizations with tenant1

        final long marketplaceKey = marketplaceTenant1.getKey();
        final long tenantKey = orgWithTenant1_1.getTenant().getKey();

        final List<Object[]> objects = runTX(new Callable<List<Object[]>>() {
            @Override
            public List<Object[]> call() throws Exception {
                return dao.getOrganizationsWithMplAndSubscriptions(
                        marketplaceKey, tenantKey);
            }
        });
        assertTrue(objects.size() == 6);
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

    private Marketplace createMarketplace(final Organization mpOwner,
            final String marketplaceId) throws Exception {
        return runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, marketplaceId,
                        true, ds);
            }
        });
    }

    private Tenant createTenant(final String tenantId) throws Exception {
        final Tenant tenant = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                Tenant tenant = new Tenant();
                tenant.setTenantId(tenantId);
                tenant.getDataContainer().setName(tenantId + "Name");
                ds.persist(tenant);
                return tenant;
            }
        });

        return runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return (Tenant) ds.getReferenceByBusinessKey(tenant);
            }
        });
    }

    private Organization createOrganization(final Organization org,
            final OrganizationRoleType... roles) throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(ds, org, roles);
            }
        });
    }

    private Organization createOrganizationObject(String orgId)
            throws Exception {
        Organization org = new Organization();
        org.setOrganizationId(orgId);
        org.setName("Name of organization " + org.getOrganizationId());
        org.setAddress("Address of organization " + org.getOrganizationId());
        org.setEmail(org.getOrganizationId() + "@organization.com");
        org.setPhone("012345/678" + org.getOrganizationId());
        org.setLocale("en");
        org.setUrl("http://www.organization.com");
        org.setCutOffDay(1);
        return org;
    }

    private Organization createOrganizationObjectWithTenant(String orgId, Tenant tenant) throws Exception {
        final Organization organizationObject = createOrganizationObject(orgId);
        organizationObject.setTenant(tenant);
        return organizationObject;
    }
}
