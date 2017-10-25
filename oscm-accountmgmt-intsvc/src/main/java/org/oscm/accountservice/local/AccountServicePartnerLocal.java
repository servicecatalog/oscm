/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 2, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.local;

import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.Organization;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;

@Local
public interface AccountServicePartnerLocal {

    /**
     * Returns all broker organizations.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @return all brokers available to the platform
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     */
    public List<Organization> getBrokers()
            throws OrganizationAuthoritiesException;

    /**
     * Returns all reseller organizations.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @return all resellers available to the platform
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     */
    public List<Organization> getResellers()
            throws OrganizationAuthoritiesException;
}
