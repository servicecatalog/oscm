/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 18.07.2012
 *
 *******************************************************************************/
package org.oscm.internal.tenant;

import java.util.List;
import java.util.Properties;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.*;

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
     * Persists tenant IDP settings.
     * @param properties list of settings to persist.
     * @param tenantId tenant to which settings belongs
     * @throws NonUniqueBusinessKeyException if settings' business key is not unique
     * @throws ObjectNotFoundException if tenant is not found
     */
    void setTenantSettings(Properties properties, String tenantId)
        throws ObjectNotFoundException, NonUniqueBusinessKeyException;

    /**
     * Retrieves settings for tenant
     * @param tenantKey tenant key to which settings are being returned.
     * @return
     */
    Properties getTenantSettings(long tenantKey);

    /**
     * Removes all settings for tenant
     * @param tenantKey tenant key to which settings are being removed
     * @throws ObjectNotFoundException if tenant is not found.
     */
    void removeTenantSettings(long tenantKey) throws ObjectNotFoundException;

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
