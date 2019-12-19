/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import org.oscm.domobjects.Tenant;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException.Reason;
import org.oscm.internal.vo.VOTenant;

import org.oscm.tenant.assembler.TenantAssembler;
import org.oscm.tenant.local.TenantServiceLocal;

@Stateless
@Remote(TenantService.class)
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class TenantServiceBean implements TenantService {

    @EJB
    TenantServiceLocal tenantServiceLocal;

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public List<VOTenant> getTenants() {
        List<VOTenant> voTenants = new ArrayList<>();
        for (Tenant tenant : tenantServiceLocal.getAllTenants()) {
            voTenants.add(TenantAssembler.toVOTenant(tenant));
        }
        return voTenants;
    }

    @Override
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public VOTenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException {
        return TenantAssembler.toVOTenant(tenantServiceLocal.getTenantByTenantId(tenantId));
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public void addTenant(VOTenant voTenant) throws NonUniqueBusinessKeyException {
        tenantServiceLocal.saveTenant(TenantAssembler.toTenant(voTenant));
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void updateTenant(VOTenant voTenant)
        throws NonUniqueBusinessKeyException, ObjectNotFoundException, ConcurrentModificationException {
        Tenant tenantToUpdate = tenantServiceLocal.getTenantByKey(voTenant.getKey());
        tenantServiceLocal.saveTenant(TenantAssembler.updateTenantData(voTenant,
            tenantToUpdate));
    }

    @Override
    @RolesAllowed("PLATFORM_OPERATOR")
    public void removeTenant(VOTenant voTenant) throws ObjectNotFoundException, TenantDeletionConstraintException {
        Tenant tenantToRemove = tenantServiceLocal.getTenantByKey(voTenant.getKey());
        if (tenantServiceLocal.doesOrganizationAssignedToTenantExist(tenantToRemove)) {
            throw new TenantDeletionConstraintException("org", Reason.RELATED_ORGANIZATION_EXISTS);
        }
        if (tenantServiceLocal.doesMarketplaceAssignedToTenantExist(tenantToRemove)) {
            throw new TenantDeletionConstraintException("mark", Reason.RELATED_MARKETPLACE_EXISTS);
        }
        tenantServiceLocal.removeTenant(tenantToRemove);
    }
    
    
    @Override
    public List<VOTenant> getTenantsByIdPattern(String tenantIdPattern) {
        List<VOTenant> voTenants = new ArrayList<>();
        List<Tenant> tenants = tenantServiceLocal.getTenantsByIdPattern(tenantIdPattern);
        for (Tenant tenant : tenants) {
            voTenants.add(TenantAssembler.toVOTenant(tenant));
        }
        return voTenants;
    }

    public void setTenantServiceLocal(TenantServiceLocal tenantServiceLocal) {
        this.tenantServiceLocal = tenantServiceLocal;
    }
    
    @Override
    public boolean doOrgUsersExistInTenant(String orgId, long tenantKey) {
        return this.tenantServiceLocal.doOrgUsersExistInTenant(orgId, tenantKey);
    }

    @Override
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    public VOTenant getTenantByKey(long key) throws ObjectNotFoundException {
        Tenant tenant = tenantServiceLocal.getTenantByKey(key);
        return TenantAssembler.toVOTenant(tenant);
    }

	@Override
	public void removeTenantSettings(long key) throws ObjectNotFoundException {
		// nope
	}
}
