/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;

/**
 * Class for managing tenants
 */
@Remote
public interface TenantService {

    /**
     * Method which lists all the tenants from system.
     * @return ArrayList of value objects.
     */
    List<VOTenant> getTenants();

    /**
     * Queries db for tenant by its tenantID.
     * @param tenantId Id of tenant to find.
     * @return Tenant value object
     * @throws ObjectNotFoundException if tenant is not found.
     */
    VOTenant getTenantByTenantId(String tenantId) throws ObjectNotFoundException;

    /**
     * Adds new tenant to system
     * @param voTenant Tenant representation.
     * @throws NonUniqueBusinessKeyException if tenant with business key already exists.
     */
    void addTenant(VOTenant voTenant) throws NonUniqueBusinessKeyException;

    /**
     * Modified already existing tenant.
     * @param voTenant value object with new values.
     * @throws NonUniqueBusinessKeyException if new business key is not unique
     * @throws ObjectNotFoundException if tenant does not exist in db anymore
     * @throws ConcurrentModificationException if version of tenant object is different than currently existing in DB.
     */
    void updateTenant(VOTenant voTenant)
        throws NonUniqueBusinessKeyException, ObjectNotFoundException, ConcurrentModificationException;

    /**
     * Removes tenant from DB
     * @param voTenant value object which represents tenant to be removed.
     * @throws ObjectNotFoundException if tenant does not exists in DB
     * @throws TenantDeletionConstraintException if tenant is being used by organization or marketplace
     */
    void removeTenant(VOTenant voTenant) throws ObjectNotFoundException, TenantDeletionConstraintException;
    
    VOTenant getTenantByKey(long key) throws ObjectNotFoundException;

    /**
     * Persists tenant IDP settings.
     * @param tenantSettings list of settings to persist.
     * @param voTenant tenant to which settings belongs
     * @throws NonUniqueBusinessKeyException if settings' business key is not unique
     * @throws ObjectNotFoundException if tenant is not found
     */
    void addTenantSettings(List<VOTenantSetting> tenantSettings, VOTenant voTenant) throws
        NonUniqueBusinessKeyException, ObjectNotFoundException;

    /**
     * Removes all settings for tenant
     * @param key tenant key to which settings are being removed
     * @throws ObjectNotFoundException if tenant is not found.
     */
    void removeTenantSettings(long key) throws ObjectNotFoundException;

    /**
     * Retrieves settings for tenant
     * @param key tenant key to which settings are being returned.
     * @return
     */
    List<VOTenantSetting> getSettingsForTenant(long key);

    /**
     * Finds tenant by its id pattern.
     * @param tenantIdPattern set of characters to which tenant should be found.
     * @return
     */
    List<VOTenant> getTenantsByIdPattern(String tenantIdPattern);

    /**
     * Checks if at least one user exists for tenant.
     * @param orgId Organization id
     * @param tenantKey tenant tkey
     * @return
     */
    boolean doOrgUsersExistInTenant(String orgId, long tenantKey);
}
