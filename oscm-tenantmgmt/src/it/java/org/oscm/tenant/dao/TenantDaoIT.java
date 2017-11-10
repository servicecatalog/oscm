/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.tenant.dao;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.TenantSetting;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;
import org.oscm.tenant.assembler.TenantAssembler;
import org.oscm.tenant.bean.TenantServiceBean;
import org.oscm.tenant.bean.TenantServiceLocalBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Created by PLGrubskiM on 2016-09-15.
 */
public class TenantDaoIT extends EJBTestBase {

    protected DataService dm;
    protected TenantDao tenantDao;
    protected TenantServiceBean bean;

    final Tenant tenant1 = createTenant(1L);
    final Tenant tenant2 = createTenant(2L);
    final Tenant tenant3 = createTenant(3L);
    final Tenant tenant4 = createTenant(4L);

    private Collection<TenantSetting> tenantSettings = Collections.emptyList();

    @Override
    protected void setup(TestContainer container) throws Exception {

        dm = new DataServiceBean();
        container.addBean(new ConfigurationServiceStub());
        container.addBean(dm);
        container.addBean(new TenantDao());
        container.addBean(new TenantServiceLocalBean());
        container.addBean(new TenantServiceBean());

        bean = container.get(TenantServiceBean.class);
        tenantDao = container.get(TenantDao.class);

        container.login("setup", ROLE_PLATFORM_OPERATOR);

        tenant1.setTenantSettings(tenantSettings);
        tenant2.setTenantSettings(tenantSettings);
        tenant3.setTenantSettings(tenantSettings);
        tenant4.setTenantSettings(tenantSettings);
    }

    @Test
    public void getAllTenants_empty() throws Exception {
        final List<Tenant> tenants = runTX(new Callable<List<Tenant>>() {
            @Override
            public List<Tenant> call() throws NonUniqueBusinessKeyException {
                return tenantDao.getAllTenants();
            }
        });
        assertTrue(tenants.isEmpty());

    }

    @Test
    public void getAllTenants() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                bean.addTenant(TenantAssembler.toVOTenant(tenant2));
                bean.addTenant(TenantAssembler.toVOTenant(tenant3));
                bean.addTenant(TenantAssembler.toVOTenant(tenant4));
                return null;
            }
        });
        final List<Tenant> tenants = runTX(new Callable<List<Tenant>>() {
            @Override
            public List<Tenant> call() throws NonUniqueBusinessKeyException {
                return tenantDao.getAllTenants();
            }
        });
        assertTrue(tenants.size() == 4);

    }

    @Test
    public void getTenantByTenantId() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                return null;
            }
        });
        final Tenant returnedTenant = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws NonUniqueBusinessKeyException,
                    ObjectNotFoundException {
                return tenantDao.getTenantByTenantId("tenantID1");
            }
        });
        assertTrue(returnedTenant != null);
    }

    @Test
    public void getAllTenantSettingsForTenant() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Tenant tenant = new Tenant();
                tenant.setTenantId("tenantID1");
                tenant.getDataContainer().setName("tenant name");
                dm.persist(tenant);
                return null;
            }
        });
        final VOTenant returnedTenant = runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws Exception {
                return bean.getTenantByTenantId("tenantID1");
            }
        });
        final List<VOTenantSetting> settingsList = new ArrayList<>();

        final VOTenantSetting settingOne = new VOTenantSetting();
        settingOne.setName(IdpSettingType.SSO_IDP_URL);
        settingOne.setValue("someUrl");
        settingOne.setVoTenant(returnedTenant);

        VOTenantSetting settingTwo = new VOTenantSetting();
        settingTwo.setName(IdpSettingType.SSO_ISSUER_ID);
        settingTwo.setValue("someIssuer");
        settingTwo.setVoTenant(returnedTenant);

        settingsList.add(settingOne);
        settingsList.add(settingTwo);

        runTX(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (VOTenantSetting voSetting : settingsList) {
                    final TenantSetting domSetting = TenantAssembler
                            .toTenantSetting(voSetting);
                    dm.persist(domSetting);
                }
                return null;
            }
        });
        final List<TenantSetting> tenantSettings = runTX(
                new Callable<List<TenantSetting>>() {
                    @Override
                    public List<TenantSetting> call() throws Exception {
                        return tenantDao.getAllTenantSettingsForTenant(
                                TenantAssembler.toTenant(returnedTenant));
                    }
                });
        assertTrue(tenantSettings.size() == 2);
    }

    @Test
    public void getTenantsByIdPattern() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                bean.addTenant(TenantAssembler.toVOTenant(tenant2));
                bean.addTenant(TenantAssembler.toVOTenant(tenant3));
                bean.addTenant(TenantAssembler.toVOTenant(tenant4));
                return null;
            }
        });
        final List<Tenant> tenants = runTX(new Callable<List<Tenant>>() {
            @Override
            public List<Tenant> call() throws Exception {
                return tenantDao.getTenantsByIdPattern("tenantID%");
            }
        });
        assertTrue(tenants.size() == 4);
    }

    @Test
    public void find() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                bean.addTenant(TenantAssembler.toVOTenant(tenant2));
                bean.addTenant(TenantAssembler.toVOTenant(tenant3));
                bean.addTenant(TenantAssembler.toVOTenant(tenant4));
                return null;
            }
        });
        final Long returnedTenantId = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return tenantDao.getTenantByTenantId("tenantID1").getKey();
            }
        });
        final Tenant foundTenant = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.find(returnedTenantId);
            }
        });
        assertTrue(foundTenant != null);
    }

    @Test
    public void find_not_exists() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                bean.addTenant(TenantAssembler.toVOTenant(tenant2));
                bean.addTenant(TenantAssembler.toVOTenant(tenant3));
                bean.addTenant(TenantAssembler.toVOTenant(tenant4));
                return null;
            }
        });
        final Tenant foundTenant = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.find(200000L);
            }
        });
        assertTrue(foundTenant == null);
    }

    @Test
    public void doesOrganizationForTenantExist() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                return null;
            }
        });
        final Tenant returnedTenant = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.getTenantByTenantId("tenantID1");
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId("orgId");
                org.setTenant(returnedTenant);
                org.setCutOffDay(1);
                dm.persist(org);
                return null;
            }
        });
        final Long count = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return tenantDao.doesOrganizationForTenantExist(returnedTenant);
            }
        });
        assertTrue(count == 1L);
    }

    @Test
    public void doesMarketplaceAssignedToTenantExist() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bean.addTenant(TenantAssembler.toVOTenant(tenant1));
                return null;
            }
        });
        final Tenant returnedTenant = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.getTenantByTenantId("tenantID1");
            }
        });
        final Organization returnedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId("orgId");
                org.setTenant(returnedTenant);
                org.setCutOffDay(1);
                dm.persist(org);
                return org;
            }
        });
        final RevenueShareModel returnedPriceModel = runTX(
                new Callable<RevenueShareModel>() {
                    @Override
                    public RevenueShareModel call() throws Exception {
                        RevenueShareModel pm = new RevenueShareModel();
                        pm.setRevenueShareModelType(
                                RevenueShareModelType.RESELLER_REVENUE_SHARE);
                        dm.persist(pm);
                        return pm;
                    }
                });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId("marketplaceId");
                mp.setPriceModel(returnedPriceModel);
                mp.setOrganization(returnedOrg);
                mp.setTenant(returnedTenant);
                mp.setBrokerPriceModel(returnedPriceModel);
                mp.setResellerPriceModel(returnedPriceModel);
                dm.persist(mp);
                return null;
            }
        });
        final Long count = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return tenantDao
                        .doesMarketplaceAssignedToTenantExist(returnedTenant);
            }
        });
        assertTrue(count == 1L);
    }

    @Test
    public void testGetNonUniqueOrgUserIdsInTenant() throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Tenant tenant1 = new Tenant();
                tenant1.setTenantId("tenantID1");
                tenant1.getDataContainer().setName("tenant name");
                dm.persist(tenant1);

                final Tenant tenant2 = new Tenant();
                tenant2.setTenantId("tenantID2");
                tenant2.getDataContainer().setName("tenant name");
                dm.persist(tenant2);
                return null;
            }
        });

        // given
        final Tenant tenant1 = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.getTenantByTenantId("tenantID1");
            }
        });

        final Tenant tenant2 = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.getTenantByTenantId("tenantID2");
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId("orgId1");
                org.setTenant(tenant1);
                org.setCutOffDay(1);
                dm.persist(org);

                Organizations.createUserForOrgWithGivenId(dm, "user1", org);
                Organizations.createUserForOrgWithGivenId(dm, "user2", org);
                Organizations.createUserForOrgWithGivenId(dm, "user3", org);
                return org;
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId("orgId2");
                org.setTenant(tenant2);
                org.setCutOffDay(1);
                dm.persist(org);

                Organizations.createUserForOrgWithGivenId(dm, "user1", org);
                Organizations.createUserForOrgWithGivenId(dm, "user4", org);
                Organizations.createUserForOrgWithGivenId(dm, "user5", org);
                return org;
            }
        });

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = new Organization();
                org.setOrganizationId("orgId3");
                org.setTenant(tenant2);
                org.setCutOffDay(1);
                dm.persist(org);

                Organizations.createUserForOrgWithGivenId(dm, "user2", org);
                Organizations.createUserForOrgWithGivenId(dm, "user6", org);
                Organizations.createUserForOrgWithGivenId(dm, "user7", org);
                return org;
            }
        });

        // when
        final List<String> userIds = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return tenantDao.getNonUniqueOrgUserIdsInTenant("orgId1",
                        tenant2.getKey());
            }
        });

        // then
        assertTrue(userIds.size() == 2);
        assertTrue(userIds.contains("user1"));
        assertTrue(userIds.contains("user2"));
    }

    @Test
    public void testGetNonUniqueOrgUserIdsInTenantWithoutOrg()
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Tenant tenant = new Tenant();
                tenant.setTenantId("tenantID1");
                tenant.getDataContainer().setName("tenant name");
                dm.persist(tenant);

                final Tenant tenantWithoutOrg = new Tenant();
                tenantWithoutOrg.setTenantId("tenantWithoutOrg");
                tenantWithoutOrg.getDataContainer().setName("tenant name");
                dm.persist(tenantWithoutOrg);

                Organization org = new Organization();
                org.setOrganizationId("orgId1");
                org.setTenant(tenant);
                org.setCutOffDay(1);
                dm.persist(org);

                Organizations.createUserForOrgWithGivenId(dm, "user1", org);
                Organizations.createUserForOrgWithGivenId(dm, "user2", org);
                Organizations.createUserForOrgWithGivenId(dm, "user3", org);
                Organizations.createUserForOrgWithGivenId(dm, "user4", org);
                Organizations.createUserForOrgWithGivenId(dm, "user5", org);

                return null;
            }
        });

        final Tenant tenantWithoutOrg = runTX(new Callable<Tenant>() {
            @Override
            public Tenant call() throws Exception {
                return tenantDao.getTenantByTenantId("tenantWithoutOrg");
            }
        });

        // when
        final List<String> userIds = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return tenantDao.getNonUniqueOrgUserIdsInTenant("orgId1",
                        tenantWithoutOrg.getKey());
            }
        });

        // then
        assertTrue(userIds.isEmpty());
    }

    @Test
    public void testGetNonUniqueOrgUserIdsInTenantForDefaultTenant()
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Tenant tenant = new Tenant();
                tenant.setTenantId("tenantID1");
                tenant.getDataContainer().setName("tenant name");
                dm.persist(tenant);

                Organization org = new Organization();
                org.setOrganizationId("orgId1");
                org.setTenant(tenant);
                org.setCutOffDay(1);
                dm.persist(org);

                Organizations.createUserForOrgWithGivenId(dm, "user1", org);
                Organizations.createUserForOrgWithGivenId(dm, "user2", org);
                Organizations.createUserForOrgWithGivenId(dm, "user3", org);
                Organizations.createUserForOrgWithGivenId(dm, "user4", org);
                Organizations.createUserForOrgWithGivenId(dm, "user5", org);

                Organization orgWithoutTenat = Organizations.createOrganization(
                        dm, "orgWithoutTenat", OrganizationRoleType.SUPPLIER);

                Organizations.createUserForOrgWithGivenId(dm, "user1",
                        orgWithoutTenat);
                Organizations.createUserForOrgWithGivenId(dm, "user3",
                        orgWithoutTenat);
                Organizations.createUserForOrgWithGivenId(dm, "user4",
                        orgWithoutTenat);

                return null;
            }
        });

        // when
        final List<String> userIds = runTX(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return tenantDao.getNonUniqueOrgUserIdsInTenant("orgId1", 0);
            }
        });

        // then
        assertTrue(userIds.size() == 3);
        assertTrue(userIds.contains("user1"));
        assertTrue(userIds.contains("user3"));
        assertTrue(userIds.contains("user4"));
    }

    private Tenant createTenant(long modifier) {
        final Tenant tenant = new Tenant();
        tenant.setTenantId("tenantID" + modifier);
        tenant.getDataContainer().setName("tenant name");
        tenant.setTenantSettings(tenantSettings);
        tenant.setHistoryModificationTime(10000000000L);
        tenant.setMarketplaces(createMarketplaces());
        tenant.setOrganizations(createOrganizations());
        return tenant;
    }

    private Collection<Marketplace> createMarketplaces() {
        List<Marketplace> marketplaceList = new ArrayList<>();
        Marketplace marketplace = new Marketplace();
        marketplaceList.add(marketplace);
        return marketplaceList;
    }

    private Collection<Organization> createOrganizations() {
        List<Organization> organizationList = new ArrayList<>();
        Organization org = new Organization();
        organizationList.add(org);
        return organizationList;
    }
}
