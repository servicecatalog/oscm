/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.landingpageService.local;

import java.util.List;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;
import org.oscm.internal.vo.VOService;

import javax.ejb.Local;

@Local
public interface LandingpageServiceLocal {

    /**
     * Returns the type of the landing page for the given marketplace. To
     * landing pages can be used: "public" for large marketplaces which is
     * indented for offerings and "enterprise" which is intended for
     * small private marketplaces.
     * 
     * @param marketplaceId
     *            identifier of the marketplace
     * @return type of landing page (or enterprise)
     * @throws ObjectNotFoundException
     */
    LandingpageType loadLandingpageType(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Returns the landing page configuration for a given marketplace.
     * 
     * Required role: marketplace manager
     * 
     * @param marketplaceId
     *            identifier of the marketplace
     * @return landingpageConfiguration VO landing page object
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its marketplace id
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     */
    VOPublicLandingpage loadPublicLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Saves the landing page configuration for a given marketplace.
     * 
     * Required role: marketplace manager
     * 
     * @param landingpage
     *            VOLandingpage to be saved
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its key
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     */
    void savePublicLandingpageConfig(VOPublicLandingpage voLandingpage)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, FillinOptionNotSupportedException;

    /**
     * Sets the enterprise landing page for a given marketplace.
     * 
     * Required role: marketplace manager
     * 
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its key
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     */
    void saveEnterpriseLandingpageConfig(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            NonUniqueBusinessKeyException;

    /**
     * Resets the landing page configuration to the default values and deletes
     * all landing page services.
     * 
     * Required role: marketplace manager
     * 
     * @param marketplaceId
     *            identifier of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its key
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     */
    void resetLandingpage(String marketplaceId)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            OperationNotPermittedException;

    /**
     * Returns a list of services published on that marketplace. The services
     * can be of any status except 'deleted'. Customer specific services are not
     * contained in that list.
     * 
     * Required role: marketplace manager
     * 
     * @param marketplaceId
     * @return list of available services
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its key
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             marketplace
     */
    List<VOService> availableServices(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * The marketplace contains a landingpage which provides a list of featured
     * products. If the given product is deleted, unpublished or re-published
     * the product must be removed from the featured list of the landingpage.
     * 
     * @param marketplace
     *            where the product is/was published
     * @param product
     *            which was deleted, unpublished or re-published
     * 
     */
    void removeProductFromLandingpage(Marketplace marketplace,
            Product product);

    /**
     * Creates a default landingpage
     * 
     * @return landingpage with default values
     */
    PublicLandingpage createDefaultLandingpage();

    /**
     * get services to be displayed on landingpage
     * 
     * @param marketplaceId
     * @param locale
     * @return
     * @throws ObjectNotFoundException
     */
    List<VOService> servicesForPublicLandingpage(String marketplaceId,
            String locale) throws ObjectNotFoundException;

}
