/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 31.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

@Local
public interface TenantServiceLocal {

	List<Tenant> getAllTenants();

	Tenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException;

	void saveTenant(Tenant tenant) throws NonUniqueBusinessKeyException;

	Tenant getTenantByKey(long tkey) throws ObjectNotFoundException;

	void removeTenant(Tenant tenant);

	List<Tenant> getTenantsByIdPattern(String tenantIdPattern);

	boolean doesOrganizationAssignedToTenantExist(Tenant tenant);

	boolean doesMarketplaceAssignedToTenantExist(Tenant tenant);

	boolean doOrgUsersExistInTenant(String orgId, long tenantKey);

}
