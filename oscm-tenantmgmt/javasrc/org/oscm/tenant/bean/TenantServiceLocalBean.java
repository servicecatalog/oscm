/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.bean;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Tenant;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.tenant.dao.TenantDao;
import org.oscm.tenant.local.TenantServiceLocal;

@Interceptors({ InvocationDateContainer.class, org.oscm.interceptor.ExceptionMapper.class })
@Stateless
@Local(TenantServiceLocal.class)
public class TenantServiceLocalBean implements TenantServiceLocal {

    @EJB
    TenantDao tenantDao;

    @EJB
    DataService dataManager;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @RolesAllowed("PLATFORM_OPERATOR")
    @Override
    public List<Tenant> getAllTenants() {
        return tenantDao.getAllTenants();
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @Override
    public Tenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException {
        return tenantDao.getTenantByTenantId(tenantId);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void saveTenant(Tenant tenant) throws NonUniqueBusinessKeyException {
        dataManager.persist(tenant);
    }

    @Override
    public Tenant getTenantByKey(long tkey) throws ObjectNotFoundException {
        return dataManager.getReference(Tenant.class, tkey);
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void removeTenant(Tenant tenant) {
        dataManager.remove(tenant);
    }
  

    @Override
    public List<Tenant> getTenantsByIdPattern(String tenantIdPattern) {
        return tenantDao.getTenantsByIdPattern(tenantIdPattern);
    }

    @Override
    public boolean doesOrganizationAssignedToTenantExist(Tenant tenant) {
        return tenantDao.doesOrganizationForTenantExist(tenant) > 0L;
    }

    @Override
    public boolean doesMarketplaceAssignedToTenantExist(Tenant tenant) {
        return tenantDao.doesMarketplaceAssignedToTenantExist(tenant) > 0L;
    }

    public void setDataManager(DataService dataManager) {
        this.dataManager = dataManager;
    }

    public void setTenantDao(TenantDao tenantDao) {
        this.tenantDao = tenantDao;
    }

    @Override
    public boolean doOrgUsersExistInTenant(String orgId, long tenantKey) {
        List<String> userIds = this.tenantDao
                .getNonUniqueOrgUserIdsInTenant(orgId, tenantKey);

        if (userIds.size() > 0) {
            return true;
        }

        return false;
    }
    
}
