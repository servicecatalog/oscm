/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.tenant.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;
import org.oscm.tenant.dao.TenantDao;
import org.oscm.tenant.local.TenantServiceLocal;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;

/**
 * Created by PLGrubskiM on 2016-09-15.
 */
public class TenantServiceBeanIT extends EJBTestBase {

    protected DataService dm;
    protected TenantServiceBean tenantService;
    protected TenantServiceLocal tenantServiceLocal;

    final VOTenant tenant1 = createTenant(1L);
    final VOTenant tenant2 = createTenant(2L);
    final VOTenant tenant3 = createTenant(3L);
    final VOTenant tenant4 = createTenant(4L);

    @Override
    protected void setup(TestContainer container) throws Exception {
        dm = new DataServiceBean();
        container.addBean(new ConfigurationServiceStub());
        container.addBean(dm);
        container.addBean(new TenantDao());
        container.addBean(new TenantServiceLocalBean());
        container.addBean(new TenantServiceBean());

        tenantService = container.get(TenantServiceBean.class);
        tenantServiceLocal = container.get(TenantServiceLocal.class);

        tenantService.setTenantServiceLocal(tenantServiceLocal);

        container.login("setup", ROLE_PLATFORM_OPERATOR);
    }

    @Test
    public void getTenants() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(tenant1);
                tenantService.addTenant(tenant2);
                tenantService.addTenant(tenant3);
                tenantService.addTenant(tenant4);
                return null;
            }
        });

        List<VOTenant> tenants = runTX(new Callable<List<VOTenant>>() {
            @Override
            public List<VOTenant> call() throws NonUniqueBusinessKeyException {
                final List<VOTenant> tenants = tenantService.getTenants();
                return tenants;
            }
        });

        assertEquals(4, tenants.size());
    }

    @Test
    public void getTenantByTenantId() throws Throwable {
        final VOTenant newTenant = createTenant(10L);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(newTenant);
                return null;
            }
        });
        VOTenant resultTenant = runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws ObjectNotFoundException {
                return tenantService.getTenantByTenantId("tenantID10");
            }
        });
        assertTrue(resultTenant != null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getTenantByTenantId_ONFException() throws Throwable {
        runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws ObjectNotFoundException {
                return tenantService.getTenantByTenantId("doesNotExist");
            }
        });
    }

    @Test
    public void addTenant() throws Throwable {
        final VOTenant tenant = createTenant(5L);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(tenant);
                return null;
            }
        });
        VOTenant returnedTenant = runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws NonUniqueBusinessKeyException,
                    ObjectNotFoundException {
                return tenantService.getTenantByTenantId("tenantID5");

            }
        });
        assertEquals(tenant.getTenantId(), returnedTenant.getTenantId());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void addTenant_NonUnique() throws Throwable {
        final VOTenant tenant = createTenant(5L);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(tenant);
                tenantService.addTenant(tenant);
                return null;
            }
        });
    }

    @Test
    public void updateTenant() throws Throwable {
        final VOTenant newTenant = createTenant(20L);
        newTenant.setTenantId("notUpdated");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(newTenant);
                return null;
            }
        });
        final VOTenant resultTenant = runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws ObjectNotFoundException {
                return tenantService.getTenantByTenantId("notUpdated");
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws ConcurrentModificationException,
                    ObjectNotFoundException, NonUniqueBusinessKeyException {
                resultTenant.setTenantId("updated");
                tenantService.updateTenant(resultTenant);
                return null;
            }
        });
        List<VOTenant> tenants = runTX(new Callable<List<VOTenant>>() {
            @Override
            public List<VOTenant> call() throws NonUniqueBusinessKeyException {
                final List<VOTenant> tenants = tenantService.getTenants();
                return tenants;
            }
        });
        boolean result = false;
        for (VOTenant retrievedTenant : tenants) {
            if (retrievedTenant.getTenantId().equals("updated")) {
                result = true;
            }
        }
        for (VOTenant retrievedTenant : tenants) {
            if (retrievedTenant.getTenantId().equals("notUpdated")) {
                result = false;
            }
        }
        assertTrue(result);
    }

    @Test
    public void removeTenant() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(tenant1);
                tenantService.addTenant(tenant2);
                tenantService.addTenant(tenant3);
                tenantService.addTenant(tenant4);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException,
                    TenantDeletionConstraintException, ObjectNotFoundException {
                final VOTenant returned1 = tenantService
                        .getTenantByTenantId(tenant1.getTenantId());
                final VOTenant returned3 = tenantService
                        .getTenantByTenantId(tenant3.getTenantId());
                tenantService.removeTenant(returned1);
                tenantService.removeTenant(returned3);
                return null;
            }
        });
        List<VOTenant> tenants = runTX(new Callable<List<VOTenant>>() {
            @Override
            public List<VOTenant> call() throws NonUniqueBusinessKeyException {
                final List<VOTenant> tenants = tenantService.getTenants();
                return tenants;
            }
        });
        assertTrue(tenants.size() == 2);
    }

    @Test
    public void addTenantSettings() throws Throwable {
        final VOTenant tenant = createTenant(6L);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                tenantService.addTenant(tenant);
                return null;
            }
        });
        final VOTenant returnedTenant = runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws ObjectNotFoundException,
                    NonUniqueBusinessKeyException,
                    ConcurrentModificationException {
                VOTenant tenant = tenantService
                        .getTenantByTenantId("tenantID6");

                List<VOTenantSetting> tenantSettings = new ArrayList<>();
                tenantSettings.add(createTenantSetting(
                        IdpSettingType.SSO_IDP_URL, 6L, tenant));

                tenantService.addTenantSettings(tenantSettings, tenant);
                return tenant;
            }
        });
        final List<VOTenantSetting> voTenantSettings = runTX(new Callable<List<VOTenantSetting>>() {
            @Override
            public List<VOTenantSetting> call() throws ObjectNotFoundException {
                final List<VOTenantSetting> settingsForTenant = tenantService
                        .getSettingsForTenant(returnedTenant.getKey());
                return settingsForTenant;
            }
        });
        assertTrue(voTenantSettings.size() == 1);
    }

    @Test
    public void removeTenantIdpProperties() throws Throwable {
        final VOTenant tenant = createTenant(123L);
        tenant.setTenantSettings(prepareIdpSettings("before"));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(tenant);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws ObjectNotFoundException {
                tenantService.removeTenantSettings(123L);
                return null;
            }
        });
        VOTenant resultTenant = runTX(new Callable<VOTenant>() {
            @Override
            public VOTenant call() throws ObjectNotFoundException {
                return tenantService.getTenantByTenantId("tenantID123");
            }
        });
        assertTrue(resultTenant != null
                && resultTenant.getTenantSettings().isEmpty());
    }

    @Test
    public void getTenantsByIdPattern() throws Throwable {
        final VOTenant tenant1 = createTenant(345L);
        final VOTenant tenant2 = createTenant(3456L);
        final VOTenant tenant3 = createTenant(300L);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws NonUniqueBusinessKeyException {
                tenantService.addTenant(tenant1);
                tenantService.addTenant(tenant2);
                tenantService.addTenant(tenant3);
                return null;
            }
        });
        final List<VOTenant> voTenants = runTX(new Callable<List<VOTenant>>() {
            @Override
            public List<VOTenant> call() {
                final List<VOTenant> tenants = tenantService
                        .getTenantsByIdPattern("tenantID345");
                return tenants;
            }
        });
        assertTrue(voTenants.size() == 1);
    }

    private Map<IdpSettingType, String> prepareIdpSettings(String modifier) {
        Map<IdpSettingType, String> map = new HashMap<>();
        IdpSettingType httpMethod = IdpSettingType.SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD;
        IdpSettingType idpUrl = IdpSettingType.SSO_IDP_URL;
        IdpSettingType issuerId = IdpSettingType.SSO_ISSUER_ID;
        IdpSettingType logoutUrl = IdpSettingType.SSO_LOGOUT_URL;
        IdpSettingType stsUrl = IdpSettingType.SSO_STS_URL;
        IdpSettingType stsMetadataUrl = IdpSettingType.SSO_STS_METADATA_URL;
        IdpSettingType stsEnckeyLength = IdpSettingType.SSO_STS_ENCKEY_LEN;

        map.put(httpMethod, "POST");
        map.put(idpUrl, "http://someIdpURL/login" + modifier);
        map.put(issuerId, "Issuer" + modifier);
        map.put(logoutUrl, "http://someIdpUrl/logout" + modifier);
        map.put(stsUrl, "http://someStsUrl/sts" + modifier);
        map.put(stsMetadataUrl, "http://someStsUrl/metadata" + modifier);
        map.put(stsEnckeyLength, "256");

        return map;
    }

    private VOTenant createTenant(long modifier) {
        final VOTenant tenant = new VOTenant();
        tenant.setDescription("desc" + modifier);
        tenant.setTenantId("tenantID" + modifier);
        tenant.setName("tenant name");
        tenant.setTenantSettings(prepareIdpSettings(Long.toString(modifier)));
        return tenant;
    }

    private VOTenantSetting createTenantSetting(IdpSettingType type,
            long modifier, VOTenant tenant) {
        final VOTenantSetting voTenantSetting = new VOTenantSetting();
        voTenantSetting.setKey(modifier);
        voTenantSetting.setName(type);
        voTenantSetting.setValue("settingValue" + modifier);
        voTenantSetting.setVoTenant(tenant);
        return voTenantSetting;
    }

}
