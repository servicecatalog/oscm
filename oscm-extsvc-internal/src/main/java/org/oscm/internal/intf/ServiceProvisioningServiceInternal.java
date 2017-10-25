/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 20.10.2011                                                      
 *                                                                              
 *  Completion Time:  20.10.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Remote interface for defining and provisioning services. This interface
 * contains methods that provide performance optimized access. It is intended
 * only as a temporary optimization and will be replaced by other means. Only
 * for internal usage.
 * 
 */
@Remote
public interface ServiceProvisioningServiceInternal {

    /**
     * Retrieves the marketable services the calling user's organization can
     * subscribe to in the context of the given marketplace.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * 
     * @return the list of services
     */
    public List<VOService> getServicesForMarketplace(String marketplaceId,
            PerformanceHint performanceHint);

    /**
     * Retrieves a list of the marketable services supplied by the calling
     * user's organization.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * 
     * @return the list of services
     */
    public List<VOService> getSuppliedServices(PerformanceHint performanceHint);

    /**
     * Retrieves the technical services which are visible to the calling user's
     * organization according to the given organization role.
     * <p>
     * <ul>
     * <li>If the organization role is {@link OrganizationRoleType#SUPPLIER},
     * the method returns all technical services for which the calling user's
     * organization has been appointed as a supplier by a technology provider.
     * <li>If the organization role is
     * {@link OrganizationRoleType#TECHNOLOGY_PROVIDER}, all technical services
     * provided by the calling user's organization are returned.
     * </ul>
     * <p>
     * An empty list is returned for other organization roles or if the calling
     * user's organization does not have the specified role.
     * <p>
     * Required role: service manager of a supplier organization to retrieve the
     * technical services for the supplier role; technology manager of a
     * technology provider organization to retrieve the technical services for
     * the technology provider role
     * 
     * @param role
     *            an <code>OrganizationRoleType</code> object specifying the
     *            organization role for which the list is to be returned
     * @param performanceHint
     *            a <code>performanceHint</code> constant specifying the data to
     *            include in the result. This can be used to increase the search
     *            performance.
     * @return the list of technical services
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             specified role
     */
    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType role, PerformanceHint performanceHint)
            throws OrganizationAuthoritiesException;

    /**
     * In case the service is a partner service, the partner organization is
     * returned. Otherwise, the ObjectNotFoundException is thrown.
     * 
     * @param serviceKey
     *            the service
     * @param locale
     *            language
     * @return the partner as VOOrganization
     * @throws ObjectNotFoundException
     */
    public VOOrganization getPartnerForService(long serviceKey, String locale)
            throws ObjectNotFoundException;

}
