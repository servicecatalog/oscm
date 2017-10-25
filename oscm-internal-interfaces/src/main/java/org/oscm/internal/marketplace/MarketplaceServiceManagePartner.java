/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 06.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.marketplace;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Use case specific top level service for marketplace management.
 * 
 * @author barzu
 */
@Remote
public interface MarketplaceServiceManagePartner {

    /**
     * Modifies the name, revenue share and/or owner of the given marketplace.
     * <p>
     * To set a new marketplace owner, specify the ID of the organization in the
     * <code>VOMarketplace</code> object. The organization is automatically
     * assigned the marketplace owner role. Its administrators are notified by
     * email and assigned the marketplace manager role. They can assign the
     * marketplace manager role to additional users as required.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * to change the name; operator of the platform operator organization to
     * change the marketplace owner.
     * 
     * @param marketplace
     *            the value object specifying the marketplace and the data to be
     *            stored
     * @param marketplacePriceModel
     *            the presentation object holding the revenue share for the
     *            marketplace
     * @param partnerPriceModel
     *            the presentation object holding the revenue share for the
     *            partners
     * @return A <code>Response</code> object holding the updated marketplace
     *         information as <code>VOMarketplace</code>, the marketplace price
     *         model as <code>POMarketplacePriceModel</code> and the partner
     *         price model as <code>POPartnerPriceModel</code>.
     * 
     * @throws ObjectNotFoundException
     *             if the marketplace or any referenced object is not found by
     *             its ID
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ConcurrentModificationException
     *             if the data stored for the given marketplace is changed by
     *             another user in the time between reading and writing it
     * @throws ValidationException
     *             if the marketplace ID in the value object is invalid
     * @throws AddMarketingPermissionException
     *             if an organization to be authorized as a supplier, reseller,
     *             or broker cannot be retrieved or does not have the required
     *             role
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     */
    public Response updateMarketplace(VOMarketplace marketplace,
            POMarketplacePriceModel marketplacePriceModel,
            POPartnerPriceModel partnerPriceModel)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, OperationNotPermittedException,
            AddMarketingPermissionException, UserRoleAssignmentException;

}
