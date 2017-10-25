/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 06.11.2009                                                      
 *                                                                              
 *  Completion Time: 06.11.2009                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.intf;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Remote interface providing the functionality to manage the properties,
 * translations and images of a supplier.
 * 
 * @author pock
 * 
 */
@Remote
public interface BrandService {

    /**
     * Deletes the image resources with the given image type of the current
     * supplier.
     * 
     * @param imageTypes
     *            a list of image types to delete.
     */
    public void deleteImages(List<ImageType> imageTypes);

    /**
     * Deletes the message properties (customized translations) for the current
     * supplier and the passed marketplace.
     * 
     * @throws OperationNotPermittedException
     *             thrown in case the caller is not the owner of the marketplace
     * @throws ObjectNotFoundException
     *             Thrown in case the marketplace for the passed if was not
     *             found.
     */
    public void deleteAllMessageProperties(String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException;

    /**
     * Loads the image resource for the given marketplace and image type.
     * 
     * @param marketplaceId
     *            the marketplace identifier
     * @param imageType
     *            the type of the image
     * 
     * @return the image resource object.
     */
    public VOImageResource loadImage(String marketplaceId, ImageType imageType);

    /**
     * Loads the message properties (customized translations) for the given
     * marketplace.
     * 
     * @param marketplaceId
     *            the marketplace identifier
     * @param localeString
     *            the string representation of the language for which the
     *            properties are loaded.
     * 
     * @return the loaded message properties.
     */
    public Properties loadMessageProperties(String marketplaceId,
            String localeString);

    /**
     * Loads the message properties (customized translations) for the given
     * marketplace from database.
     * 
     * @param marketplaceId
     *            the marketplace identifier
     * @param localeString
     *            the string representation of the language for which the
     *            properties are loaded.
     * 
     * @return the loaded message properties.
     */
    public Properties loadMessagePropertiesFromDB(String marketplaceId,
            String localeString);

    /**
     * Saves the image resources for the current supplier. The image resources
     * are applied to the supplier and all his customers.
     * 
     * @param imageResources
     *            the image resources to store.
     * @param marketplaceId
     *            identifies the marketplace for which the properties should be
     *            saved.
     * @throws ObjectNotFoundException
     *             Thrown in case the marketplace for the passed ID could not be
     *             found.
     * @throws OperationNotPermittedException
     *             Thrown in case the caller is not permitted to save the
     *             properties for the marketplace identified by the passed ID.
     * @throws ValidationException
     *             Thrown in case the image does not meet the given properties
     *             like file type or dimensions.
     */
    public void saveImages(List<VOImageResource> imageResources,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, ValidationException;

    /**
     * Saves the message properties (customized translations) for the current
     * supplier. The properties are applied to the supplier and all his
     * customers.
     * 
     * @param propertiesMap
     *            the map containing the locale strings and the corresponding
     *            properties to store.
     * @param marketplaceId
     *            identifies the marketplace for which the properties should be
     *            saved.
     * @throws ObjectNotFoundException
     *             Thrown in case the marketplace for the passed ID could not be
     *             found.
     * @throws OperationNotPermittedException
     *             Thrown in case the caller is not permitted to save the
     *             properties for the marketplace identified by the passed ID.
     */
    public void saveMessageProperties(Map<String, Properties> propertiesMap,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Returns the content of the stage for a specific marketplace in a defined
     * language.
     * 
     * @param marketplaceId
     *            the ID of the marketplace the stage content should be
     *            retrieved.
     * @param localeString
     *            defined the locale of stage content. If <code>null</code> will
     *            be passed the default locale is used.
     * @return the localized content of the marketplace stage if it exists for
     *         the passed parameters. If there is no content available for the
     *         passed locale the default locale will be used. If there is no
     *         content for the default locale a empty string will be returned.
     *         If the passed id is invalid or the belonging marketplace can not
     *         be found, a empty string will be returned.
     */
    public String getMarketplaceStage(String marketplaceId, String localeString);

    /**
     * Sets the content of the stage for a specific marketplace and a specific
     * locale. If the passed string for the stage is "" the stage content will
     * be removed from the database for the given locale.
     * 
     * @param stageContent
     *            the content of the stage to set. The content can be any HTML
     *            code. The caller of the function is responsible for the
     *            correctness of the content.
     * @param marketplaceId
     *            the Id for which the stage content should be set.
     * @param localeString
     *            the locale string for which the stage content should be set.
     * @throws ObjectNotFoundException
     *             thrown in case the marketplace could not be found for the
     *             passed Id.
     * @throws OperationNotPermittedException
     *             thrown in case the caller is not the owner of the marketplace
     *             identified by the passed Id.
     * 
     */
    public void setMarketplaceStage(String stageContent, String marketplaceId,
            String localeString) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Returns all saved stages and their locale for the marketplace identified
     * by the passed id.
     * 
     * @param marketplaceId
     *            the Id for which the stage contents should be read
     * @return the list of existing stages and their locale
     * @throws ObjectNotFoundException
     *             in case thrown in case the marketplace could not be found for
     *             the passed Id.
     * @throws OperationNotPermittedException
     *             thrown in case the caller is not the owner of the marketplace
     *             identified by the passed Id.
     */
    public List<VOLocalizedText> getMarketplaceStageLocalization(
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException;

}
