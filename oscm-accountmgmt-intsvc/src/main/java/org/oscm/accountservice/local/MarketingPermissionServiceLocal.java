/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 02.12.2011                                                      
 *                                                                              
 *  Completion Time: 05.12.2011                                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.MarketingPermissionNotFoundException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * Service for marketing permission related operations.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface MarketingPermissionServiceLocal {

    /**
     * Removes the marketing permissions for the technical service and the
     * specified organizations.
     * 
     * @param technicalServiceKey
     *            The key of the technical service.
     * @param organizationIds
     *            The identifiers of the supplier organizations.
     * @throws ObjectNotFoundException
     *             in case the technical service is not found
     * @throws OperationNotPermittedException
     *             in case the technical service does not belong to the caller's
     *             organization
     * @throws MarketingPermissionNotFoundException
     *             in case there is no marketing permission to be removed
     */
    public void removeMarketingPermission(long technicalServiceKey,
            List<String> organizationIds) throws ObjectNotFoundException,
            OperationNotPermittedException,
            MarketingPermissionNotFoundException;

    /**
     * Creates for the given suppliers and the technical service marketing
     * permissions. An organization reference of type
     * <code>OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER</code> is
     * created, if not already present. The status of services which are based
     * on the technical product are set to <code>ServiceStatus.INACTIVE</code>.
     * 
     * @param provider
     *            The technical provider
     * @param technicalServiceKey
     *            The technical service key
     * @param organizationIds
     *            List of supplier organization ids
     * @throws ObjectNotFoundException
     *             If the technical product could not be found
     * @throws AddMarketingPermissionException
     *             if a supplier could not be loaded or the organization does
     *             not have the OrganizationRoleType.SUPPLIER role.
     */
    public void addMarketingPermission(Organization provider,
            long technicalServiceKey, List<String> organizationIds)
            throws ObjectNotFoundException, AddMarketingPermissionException;

    /**
     * Determines the suppliers that are allowed to use the specified technical
     * service.
     * 
     * @param technicalService
     *            The technical service.
     * @return The supplier organizations allowed to use the technical service.
     * @throws ObjectNotFoundException
     *             if the technical service cannot be found.
     * @throws OperationNotPermittedException
     *             if the caller is not owner of the technical service
     */
    public List<Organization> getSuppliersForTechnicalService(
            long technicalServiceKey) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Returns all technical services the specified supplier organization has
     * marketing permissions for.
     * 
     * @param supplier
     *            The supplier to retrieve the usable technical services for.
     * @return The usable technical services.
     */
    public List<TechnicalProduct> getTechnicalServicesForSupplier(
            Organization supplier);

    /**
     * Removes all marketing permissions pointing to the specified technical
     * product. If the organization reference does not have other marketing
     * permissions the reference will be deleted, too.
     * 
     * @param technicalProduct
     *            The technical product for which marketing permissions should
     *            be removed.
     */
    public void removeMarketingPermissions(TechnicalProduct technicalProduct);

}
