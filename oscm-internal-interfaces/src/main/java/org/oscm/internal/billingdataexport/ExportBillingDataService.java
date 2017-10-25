/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.billingdataexport;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.NoBilingSharesDataAvailableException;

@Remote
public interface ExportBillingDataService {

    /**
     * triggers data export for revenue shares
     * <p>
     * Required roles: manager of the supplier, reseller, broker or marketplace
     * owner organization owning the marketplace, or the manger of the platform
     * operator organization
     * 
     * if successful:
     * 
     * - return PORevenueShareExport in Result
     * 
     * @param export
     *            holding revenue share type and period to be exported
     * 
     * @return response
     * @throws OperationNotPermittedException
     *             if the calling user does not have the service manager,
     *             reseller, broker, operator, or marketplace manager role
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, reseller, broker, operator, or marketplace owner
     *             role
     * @throws NoBilingSharesDataAvailableException
     *             if no billing shares export data exists for the given period,
     *             organization and billing shares result type
     * @throws ValidationException
     *             if specified period not valid
     */
    public Response exportRevenueShares(PORevenueShareExport export)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException,
            NoBilingSharesDataAvailableException, ValidationException;

    /**
     * triggers data export for customer billing data
     * <p>
     * Required roles: manager of the supplier or reseller organization
     * <p>
     * if successful:
     * 
     * - return POBillingDataExport in Result
     * 
     * @param exportParam
     *            holding period and customerIds for export
     * 
     * @return response
     * @throws NoBilingSharesDataAvailableException
     *             if no billing shares export data exists for the given period,
     *             organization and billing shares result type
     * @throws OrganizationAuthoritiesException
     *             if the organization has not the expected role
     */
    public Response exportBillingData(POBillingDataExport exportParam)
            throws NoBilingSharesDataAvailableException,
            OrganizationAuthoritiesException;

    /**
     * 
     * list of BillingSharesResultTypes the current users organization is
     * entitled to export billing data
     * 
     */
    List<BillingSharesResultType> getBillingShareResultTypes();

    /**
     * returns the list of customer organization of the current organization
     * 
     * @return
     */
    public List<POOrganization> getCustomers();

    /**
     * return true if the current user is platform operator
     */
    public boolean isPlatformOperator();

    /**
     * return true if the current user is supplier or reseller
     */
    public boolean isSupplierOrReseller();

}
