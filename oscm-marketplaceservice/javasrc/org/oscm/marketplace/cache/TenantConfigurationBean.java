/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *******************************************************************************/
package org.oscm.marketplace.cache;

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

import org.oscm.dataservice.local.DataService;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.intf.TenantConfigurationService;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.vo.VOTenant;

/**
 * Created by PLGrubskiM on 2017-06-30.
 */
@Deprecated
@Singleton
@Startup
@Remote(TenantConfigurationService.class)
@Interceptors({ExceptionMapper.class})
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class TenantConfigurationBean implements TenantConfigurationService {

    @EJB(beanInterface = DataService.class)
    DataService dm;

    List<VOTenant> tenants;

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Schedule(minute = "*/10")
    @Lock(LockType.WRITE)
    public void refreshCache() {
    	// tenants = getAllTenants();
    }

    @Override
    public String getHttpMethodForTenant(String tenantId) {
        return getSettingValue(IdpSettingType.SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD, tenantId);
    }

    @Override
    public String getIssuerForTenant(String tenantId) {
        return getSettingValue(IdpSettingType.SSO_ISSUER_ID, tenantId);
    }

    private String getSettingValue(IdpSettingType type, String tenantId) {

       return null;
    }

	/* (non-Javadoc)
	 * @see org.oscm.internal.intf.TenantConfigurationService#getIdpUrlForTenant(java.lang.String)
	 */
	@Override
	public String getIdpUrlForTenant(String tenantId) {
		return null;
	}
}
