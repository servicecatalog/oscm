/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-07-23                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Remote interface for the export of billing data.
 * 
 */
@Remote
public interface BillingService {

    /**
     * Collects the billing data for the last month and stores it in the
     * database. Used by JMX.
     * 
     * @param currentTime
     *            The time the billing job was triggered.
     * @return <code>true</code> in case the billing run passed without any
     *         problems (including psp invocation), <code>false</code>
     *         otherwise.
     */
    public boolean startBillingRun(long currentTime);

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

    byte[] getCustomerBillingData(Long from, Long to,
            List<String> organizationIds)
            throws OrganizationAuthoritiesException;

    /**
     * Exports the billing data related to the revenue share of the given
     * organization role in XML format.
     * <p>
     * Required role: service manager of a supplier organization, reseller of a
     * reseller organization, broker of a broker organization, operator of an
     * operator organization, or marketplace manager of a marketplace owner
     * organization
     * 
     * @param from
     *            the start of the time period for which to export the billing
     *            data
     * @param to
     *            the end of the time period for which to export the billing
     *            data
     * @param resultType
     *            the organization role whose revenue share is to be considered
     *            in the billing data export
     * @return an XML structure with the billing data related to the revenue
     *         share of the given organization role
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

    public byte[] getRevenueShareData(Long from, Long to,
            BillingSharesResultType resultType)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException, ValidationException;
}
