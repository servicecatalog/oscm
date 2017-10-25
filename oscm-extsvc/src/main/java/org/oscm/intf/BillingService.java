/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-07-23                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.enumtypes.BillingSharesResultType;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;

/**
 * Remote interface for the export of billing data and revenue share data.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface BillingService {

    /**
     * Exports the billing data of customers belonging to the calling user's
     * organization in the saved raw XML format.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @param from
     *            the start of the time period for which to export the billing
     *            data
     * @param to
     *            the end of the time period for which to export the billing
     *            data
     * @param organizationIds
     *            optionally the list of customer IDs for which to export the
     *            billing data, or <code>null</code> to export the data of all
     *            customers
     * @return the billing data in XML format
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             or reseller role
     */
    @WebMethod
    byte[] getCustomerBillingData(@WebParam(name = "from") Long from,
            @WebParam(name = "to") Long to,
            @WebParam(name = "organizationIds") List<String> organizationIds)
            throws OrganizationAuthoritiesException;

    /**
     * Exports revenue share data for the given organization role in XML format.
     * <p>
     * Required role: service manager of a supplier organization, reseller of a
     * reseller organization, broker of a broker organization, operator of an
     * operator organization, or marketplace manager of a marketplace owner
     * organization
     * 
     * @param from
     *            the start of the time period for which to export the revenue
     *            share data
     * @param to
     *            the end of the time period for which to export the revenue
     *            share data
     * @param resultType
     *            the organization role whose revenue share is to be considered
     *            in the data export
     * @return an XML structure with the revenue share data for the given
     *         organization role
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, reseller, broker, operator, or marketplace owner
     *             role
     * @throws OperationNotPermittedException
     *             if the calling user does not have the service manager,
     *             reseller, broker, operator, or marketplace manager role
     * @throws ValidationException
     *             if the specified time period is invalid
     */
    @WebMethod
    public byte[] getRevenueShareData(@WebParam(name = "from") Long from,
            @WebParam(name = "to") Long to,
            @WebParam(name = "resultType") BillingSharesResultType resultType)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException, ValidationException;
}
