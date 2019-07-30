/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 18.07.2012
 *
 *******************************************************************************/
package org.oscm.internal.tenant;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Service used by view controllers to manage tenant presentation objects.
 */
@Remote
public interface ManageTenantService {

    /**
     * Method which lists all the tenants from system.
     * @return ArrayList of value objects.
     */
    List<POTenant> getAllTenants();
    
    /**
     * Method which lists all the tenants from system including the default tenant.
     * @return ArrayList of value objects.
     * @see #getAllTenants();
     */
    List<POTenant> getAllTenantsWithDefaultTenant();

    /**
     * Queries db for tenant by its tenantID.
     * @param tenantId Id of tenant to find.
     * @return Tenant value object
     * @throws ObjectNotFoundException if tenant is not found.
     */
    POTenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException;

    /**
     * Adds new tenant to system
     * @param poTenant Tenant representation.
     * @return Added tenant ID
     * @throws NonUniqueBusinessKeyException if tenant with business key already exists.
     */
    String addTenant(POTenant poTenant) throws NonUniqueBusinessKeyException;

    /**
     * Modified already existing tenant.
     * @param poTenant value object with new values.
     * @throws NonUniqueBusinessKeyException if new business key is not unique
     * @throws ObjectNotFoundException if tenant does not exist in db anymore
     * @throws ConcurrentModificationException if version of tenant object is different than currently existing in DB.
     */
    void updateTenant(POTenant poTenant)
        throws ConcurrentModificationException, ObjectNotFoundException, NonUniqueBusinessKeyException;

    /**
     * Removes tenant from DB
     * @param poTenant value object which represents tenant to be removed.
     * @throws ObjectNotFoundException if tenant does not exists in DB
     * @throws TenantDeletionConstraintException if tenant is being used by organization or marketplace
     */
    void removeTenant(POTenant poTenant) throws ObjectNotFoundException, TenantDeletionConstraintException;

      
    /**
     * Finds tenant by its id pattern.
     * @param tenantIdPattern set of characters to which tenant should be found.
     * @return
     */
    List<POTenant> getTenantsByIdPattern(String tenantIdPattern);

    /**
     * Checks if at least one user exists for tenant.
     * @param orgId Organization id
     * @param tenantKey tenant tkey
     * @return
     */
    void validateOrgUsersUniqnessInTenant(String orgId, long tenantKey) throws ValidationException;

    
}
