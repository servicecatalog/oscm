/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.marketplace.cache;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TenantSetting;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.intf.TenantConfigurationService;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.vo.VOTenantSetting;
import org.oscm.tenant.assembler.TenantAssembler;

/**
 * Created by PLGrubskiM on 2017-06-30.
 */
@Singleton
@Startup
@Remote(TenantConfigurationService.class)
@Interceptors({ExceptionMapper.class})
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class TenantConfigurationBean implements TenantConfigurationService {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    List<VOTenantSetting> tenantSettings;

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Schedule(minute = "*/10")
    @Lock(LockType.WRITE)
    public void refreshCache() {
        tenantSettings = getAllSettings();
    }

    @Override
    public String getHttpMethodForTenant(String tenantId) {
        return getSettingValue(IdpSettingType.SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD, tenantId);
    }

    @Override
    public String getIssuerForTenant(String tenantId) {
        return getSettingValue(IdpSettingType.SSO_ISSUER_ID, tenantId);
    }

    @Override
    public String getIdpUrlForTenant(String tenantId) {
        return getSettingValue(IdpSettingType.SSO_IDP_URL, tenantId);
    }

    private List<VOTenantSetting> getAllSettings() {
        Query query = dm.createNamedQuery("TenantSetting.getAll",
                TenantSetting.class);
        final List<TenantSetting> resultList = ParameterizedTypes.list(query.getResultList(), TenantSetting.class);

        List<VOTenantSetting> voTenantSettings = new ArrayList<>();

        for (TenantSetting tenantSetting : resultList) {
            voTenantSettings.add(TenantAssembler.toVOTenantSetting(tenantSetting));
        }

        return voTenantSettings;
    }

    private String getSettingValue(IdpSettingType type, String tenantId) {
        String result = null;

        for (VOTenantSetting setting : tenantSettings) {
            if (setting.getVoTenant().getTenantId().equals(tenantId)
                    && setting.getName().equals(type)) {
                return setting.getValue();
            }
        }
        return result;
    }
}
