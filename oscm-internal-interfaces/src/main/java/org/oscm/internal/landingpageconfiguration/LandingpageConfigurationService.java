/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.landingpageconfiguration;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;

@Remote
public interface LandingpageConfigurationService {

    /**
     * Returns a list of selectable marketplaces
     * <p>
     * Required roles: marketplace manager of the owning organization
     */
    List<POMarketplace> getMarketplaceSelections();

    /**
     * Save the configuration of a marketplace landing page
     * <p>
     * Required roles: marketplace manager of the organization owning the
     * specified marketplace
     * <p>
     * if save is successful:
     * 
     * - return updated POPublicLandingpageConfig in Result
     * 
     * - return updated List of availableServices (List<POService>) in Result
     * 
     * @throws FillinOptionNotSupportedException
     *             if the landing page should be sorted by the ratings and the
     *             marketplace does not support ratings
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     * @throws ConcurrentModificationException
     *             if the landing page configuration was modified by another
     *             user between loading and saving time.
     * @throws ValidationException
     *             if the attributes of the landing page configuration to be
     *             saved are invalid
     * @throws NonUniqueBusinessKeyException
     *             if the landing page configuration was never persisted before
     *             and another object with the same business key already exists
     *             in the database
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its key
     */
    Response savePublicLandingpageConfig(
            POPublicLandingpageConfig landingpageConfig)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, ConcurrentModificationException,
            OperationNotPermittedException, FillinOptionNotSupportedException;

    /**
     * Resets the landingpageConfiguration to its default
     * <p>
     * Required roles: marketplace manager of the organization owning the
     * specified marketplace
     * 
     * @param marketplaceId
     *            the identifier of the marketplace to reset the landing page
     *            for
     * @return response a <code>Response</code> object containing the updated
     *         POLandingpageConfig and the updated List of availableServices
     *         (List<POService>).
     * @throws OperationNotPermittedException
     *             if the calling user does not belong to the organization
     *             owning the specified marketplace
     * @throws NonUniqueBusinessKeyException
     *             if a landing page with the same business key exists for the
     *             specified marketplace
     * @throws ObjectNotFoundException
     *             if no marketplace is found for the specified identifier
     */
    Response resetLandingPage(String marketplaceId)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            OperationNotPermittedException;

    /**
     * Returns the landing page configuration for the selected marketplaceId.
     * <p>
     * 
     * if save is successful:
     * 
     * - return POLandingpageConfig in Response
     * 
     * - return List of availableServices (List<POService>) in Result
     * 
     * - return List of FillinOptions in Result
     * 
     * @param marketplaceId
     * @throws OperationNotPermittedException
     *             if the calling user does not belong to the organization
     *             owning the specified marketplace
     * @throws ObjectNotFoundException
     *             if no marketplace is found for the specified identifier
     * 
     */
    Response loadPublicLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves a list of the indexes of the landing page services range.
     */
    List<Integer> getNumOfServicesRange();

    /**
     * Returns the fill-in options. The option RATING_DESCENDING is only part of
     * the result if the specified marketplace has reviews enabled.
     * 
     * @param marketplaceId
     *            the identifier of the marketplace to retrieve the fill-in
     *            options for
     * @return a list of fill-in options. The list is empty if no marketplace
     *         could be found with the specified identifier.
     */
    List<FillinCriterion> getFillinOptions(String marketplaceId);

    /**
     * Returns the type of the landingpage for specific marketplace
     * 
     * @param marketplaceId
     * @return
     * @throws ObjectNotFoundException
     */
    LandingpageType loadLandingpageType(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Save the configuration of a enetrprise marketplace landing page
     * <p>
     * Required roles: marketplace manager of the organization owning the
     * specified marketplace
     * <p>
     * if save is successful:
     * 
     * - return updated POPublicLandingpageConfig in Result
     * 
     * - return updated List of availableServices (List<POService>) in Result
     * 
     * @throws FillinOptionNotSupportedException
     *             if the landing page should be sorted by the ratings and the
     *             marketplace does not support ratings
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     * @throws ConcurrentModificationException
     *             if the landing page configuration was modified by another
     *             user between loading and saving time.
     * @throws ValidationException
     *             if the attributes of the landing page configuration to be
     *             saved are invalid
     * @throws NonUniqueBusinessKeyException
     *             if the landing page configuration was never persisted before
     *             and another object with the same business key already exists
     *             in the database
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its key
     */
    Response saveEnterpriseLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ConcurrentModificationException, OperationNotPermittedException;
}
